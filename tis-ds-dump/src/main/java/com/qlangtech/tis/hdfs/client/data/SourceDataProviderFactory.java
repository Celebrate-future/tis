/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.hdfs.client.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.exception.SourceDataReadException;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus.TableDumpStatus;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;
import com.qlangtech.tis.plugin.ds.DataDumpers;
import com.qlangtech.tis.plugin.ds.IDataSourceDumper;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多线程的数据源读取，每个表或者每个库使用一个线程写入到HDFS集群<br>
 * 子表多情况，可以考虑根据库启线程
 *
 * @author 百岁（baisui@qlangtech.com）
 * @version 1.0
 * @since 2011-8-4 上午12:19:57
 */
public class SourceDataProviderFactory {

    private static final Pattern IP_PATTERN = Pattern.compile("//(.+?):");

    private static final Logger logger = LoggerFactory.getLogger(SourceDataProviderFactory.class);

    protected TSearcherDumpContext dumpContext;

    private List<IReadAccumulator> readAccumulator = Lists.newArrayList();

    public void addReadAccumulator(IReadAccumulator accumulator) {
        this.readAccumulator.add(accumulator);
    }


    public SourceDataProviderFactory() {
    }

    // 所有表总共要dump的记录数目， 當這個有第一次讀出之後以後就不需要每次預先掃描數據庫表的記錄數了，直接從上次數據庫導出數目預估就行了
    private final ScheduledExecutorService statusSendScheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "table_dump_send_scheduler");
            t.setDaemon(true);
            return t;
        }
    });


    public void setDumpContext(TSearcherDumpContext dumpContext) {
        this.dumpContext = dumpContext;
    }

    private String allDumpStartTime;

    private final AtomicBoolean haveInitialize = new AtomicBoolean(false);

    public void init() throws Exception {
        if (!haveInitialize.compareAndSet(false, true)) {
            throw new IllegalStateException(dumpContext.getDumpTable() + ",sourceDataProvider have been initialize again.");
        }

        Assert.assertNotNull("dumpContext can not be null", this.dumpContext);
        // Assert.assertNotNull("timeManager can not be null",
        // this.dumpContext.getTimeProvider());
        // 每隔2秒向状态收集中心发送一次dump执行状态
        statusSendScheduler.scheduleAtFixedRate(() -> {
            reportDumpStatus();
        }, 1, 2, TimeUnit.SECONDS);
        // 记录当前已经读入的数据表

        this.parseSubTablesDesc();

    }

    public void reportDumpStatus() {
        this.reportDumpStatus(false, /* faild */false);
    }

    /**
     * 报告当前dump数据的状态
     */
    public void reportDumpStatus(boolean faild, boolean complete) {
        int read = this.getDbReaderCounter();
        int all = dumpContext.getAllTableDumpRows().get();
        if (all < 1 && !(faild || complete)) {
            return;
        }
        TableDumpStatus tableDumpStatus = new TableDumpStatus(String.valueOf(dumpContext.getDumpTable()), dumpContext.getTaskId());
        tableDumpStatus.setWaiting(false);
        tableDumpStatus.setAllRows(all);
        tableDumpStatus.setFaild(faild);
        tableDumpStatus.setReadRows(read);
        tableDumpStatus.setComplete(complete);
        if (all > 0) {
            logger.info("read:{},all:{},percent:{}%", read, all, (read * 100 / (all)));
        } else {
            logger.info("read:{}", read);
        }

        dumpContext.getStatusReportRPC().reportDumpTableStatus(tableDumpStatus);
    }

    public void parseSubTablesDesc() throws Exception {




        if (dumpContext == null) {
            throw new IllegalStateException("dumpContext can not be null");
        }
        DataDumpers dataDumpers = dumpContext.getDataSourceFactory().getDataDumpers(this.dumpContext.getTisTable());
        Iterator<IDataSourceDumper> dumpers = dataDumpers.dumpers;
        IDataSourceDumper dumper = null;

        CountDownLatch countdown = new CountDownLatch(dataDumpers.splitCount);
        logger.info("dataprovider:" + dumpContext.getDumpTable() + ",split count:" + dataDumpers.splitCount);
        AtomicReference<Throwable> exp = new AtomicReference<Throwable>();

        while (dumpers.hasNext()) {
            dumper = dumpers.next();
            InitialDBTableReaderTask initTask = InitialDBTableReaderTask.create(exp, countdown, dumper, dumpContext);
            MultiThreadDataProvider.dbReaderExecutor.execute(initTask);
        }

        if (!countdown.await(10, TimeUnit.MINUTES)) {
            throw new IllegalStateException("wait 10 minutes expire timeout");
        }

        if (exp.get() != null) {
            throw new SourceDataReadException(exp.get());
        }
        logger.info(this.dumpContext.getDumpTable() + " row count:" + this.dumpContext.getAllTableDumpRows().get());
    }

    /**
     * @param source
     */
    private String getDbIp(DataSource source) {
        if (source instanceof BasicDataSource) {
            BasicDataSource dbcpSource = (BasicDataSource) source;
            // System.out.println("dbcpSource.getUrl:" + dbcpSource.getUrl());
            Matcher matcher = IP_PATTERN.matcher(dbcpSource.getUrl());
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                throw new IllegalStateException(dbcpSource.getUrl() + " is not match pattern:" + IP_PATTERN);
            }
        }
        throw new IllegalStateException("datasoure is illegal:" + source);
    }

    /**
     * @param
     */
    protected void validateDataSource(String dbKey) {
    }

    public int getDbReaderCounter() {
        int read = 0;
        for (IReadAccumulator acc : readAccumulator) {
            read += acc.getReadCount();
        }
        return read;
    }

    private static class InitialDBTableReaderTask extends AbstractDBTableReaderTask {
        private final AtomicReference<Throwable> exceptionCollector;

        public static InitialDBTableReaderTask create(AtomicReference<Throwable> exceptionCollector, CountDownLatch latch, IDataSourceDumper dataProvider, TSearcherDumpContext dumpContext) {
            AtomicInteger dbHostBusyCount = new AtomicInteger();
            AtomicInteger processErrorCount = new AtomicInteger();
            InitialDBTableReaderTask result = new InitialDBTableReaderTask(latch, dataProvider, Maps.newHashMap(), /* threadResult */
                    dbHostBusyCount, processErrorCount, dumpContext, exceptionCollector);
            return result;
        }

        private InitialDBTableReaderTask(CountDownLatch latch, IDataSourceDumper dataProvider, Map<String, Object> threadResult, AtomicInteger dbHostBusyCount, AtomicInteger processErrorCount, TSearcherDumpContext dumpContext, AtomicReference<Throwable> exceptionCollector) {
            super(latch, dataProvider, threadResult, dbHostBusyCount, processErrorCount, dumpContext);
            this.exceptionCollector = exceptionCollector;
        }

        @Override
        public void run() {
            try {
                int rowSize = dumper.getRowSize();
                this.dumpContext.getAllTableDumpRows().addAndGet(rowSize);
                // this.allRows.addAndGet(rowSize);
                logger.info(dumper.getDbHost() + "." + dumpContext.getDumpTable().getTableName() + " row count:" + rowSize);
            } catch (Throwable e) {
                while (latch.getCount() > 0) {
                    latch.countDown();
                }
                exceptionCollector.set(e);
            } finally {
                latch.countDown();
            }
        }
    }

}
