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
package com.qlangtech.tis.order.dump.task;

import com.qlangtech.tis.build.task.TaskMapper;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.hdfs.client.bean.TISDumpClient;
import com.qlangtech.tis.hdfs.client.bean.TISDumpClient.TriggerParamProcess;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;
import com.qlangtech.tis.offline.TableDumpFactory;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.trigger.util.TriggerParam;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年1月29日 上午11:46:56
 */
public abstract class AbstractTableDumpTask implements TaskMapper {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTableDumpTask.class);

    protected final TableDumpFactory tableDumpFactory;
    protected final DataSourceFactory dataSourceFactory;

    private TISDumpClient dumpbeans;

    public int getAllTableDumpRows() {
        Objects.requireNonNull(this.dumpbeans, "dumpBean can not be null");
        return getDumpContext().getAllTableDumpRows().get();
    }

    public TSearcherDumpContext getDumpContext() {
        Objects.requireNonNull(this.dumpbeans, "dumpBean can not be null");
        return this.dumpbeans.getDumpContext();
    }

    public AbstractTableDumpTask(TableDumpFactory tableDumpFactory, DataSourceFactory dataSourceFactory) {
        super();
        this.tableDumpFactory = tableDumpFactory;
        this.dataSourceFactory = dataSourceFactory;
    }

    protected abstract TISDumpClient getDumpBeans(TaskContext context) throws Exception;

//    /**
//     * 在容器中注册额外的bean,比如大量的datasource
//     *
//     * @param factory
//     */
//    protected abstract void registerExtraBeanDefinition(DefaultListableBeanFactory factory);

    // @Override
    @SuppressWarnings("all")
    public void map(TaskContext context) {
        // Map<String, DumpResult> /* 索引名称 */          dumpResultMap = new HashMap<>();
        Objects.requireNonNull(tableDumpFactory, "fs2Table has not be initial");
        logger.info("static initialize start");
        // initialSpringContext(context);
        logger.info("static initialize success");
        // StatusRpcClient rpcClient = new StatusRpcClient();
        try {
            final String startTime = context.get(ITableDumpConstant.DUMP_START_TIME);
            logger.info("dump startTime:" + startTime);
            final boolean force = getParamForce(context);
            // leader 选举代码
            // https://git-wip-us.apache.org/repos/asf?p=curator.git;a=blob;f=curator-examples/src/main/java/leader/LeaderSelectorExample.java;h=85f0598a62537952f072db6bdb5c16f049bab38f;hb=HEAD
            // ExecutorCompletionService<DumpResult> executeService = new ExecutorCompletionService(threadPool);
            // AtomicBoolean joinTableClear = new AtomicBoolean(false);
            // 取得所有的dump bean 以表为单位
            this.dumpbeans = getDumpBeans(context);
            if (dumpbeans == null) {
                throw new IllegalStateException("dumpbeans list size can not small than 1");
            }
            tableDumpFactory.startTask((connContext) -> {
                dumpbeans.executeDumpTask(false, force, new TriggerParamProcess() {
                    @Override
                    public void callback(TriggerParam param) {
                        return;
                    }
                }, startTime, connContext);
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 当历史数据已经存在，是否还要再导入一份数据？
     *
     * @param context
     * @return
     */
    private boolean getParamForce(TaskContext context) {
        boolean force = false;
        try {
            force = Boolean.parseBoolean(context.get(ITableDumpConstant.DUMP_FORCE));
        } catch (Throwable e) {
        }
        return force;
    }

//    public void initialSpringContext(TaskContext context) {
//        final DBConfig dbLinkMetaData = parseDbLinkMetaData(context);
//        springContext = new ClassPathXmlApplicationContext("dump-app-context.xml", this.getClass()) {
//
//            protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
//                DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
//                // DataSourceRegister.setApplicationContext(factory,
//                // dbMetaList);
//                SpringDBRegister dbRegister = new SpringDBRegister(dbLinkMetaData.getName(), dbLinkMetaData, factory);
//                dbRegister.visitAll();
//                registerExtraBeanDefinition(factory);
//                super.prepareBeanFactory(beanFactory);
//            }
//        };
//    }

    /**
     * @param dumpResultMap
     * @return
     */
    private JSONObject createDumpResultDesc(Map<String, DumpResult> dumpResultMap) {
        try {
            JSONObject dumpResultDesc = new JSONObject();
            JSONArray importTabs = new JSONArray();
            for (String indexName : dumpResultMap.keySet()) {
                importTabs.put(indexName);
            }
            dumpResultDesc.put("tabs", importTabs);
            return dumpResultDesc;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class DumpResult {

        private EntityName dumpTable;

        private Exception error;

        public void setDumpTable(EntityName dumpTable) {
            this.dumpTable = dumpTable;
        }

        boolean isSuccess() {
            return // serviceConfig != null &&
                    error == null;
        }
    }

    public static class DumpResultException extends Exception {

        private final DumpResult dumpResult;

        public DumpResultException(DumpResult dumpResult) {
            super(String.valueOf(dumpResult.dumpTable));
            this.dumpResult = dumpResult;
        }
    }
}
