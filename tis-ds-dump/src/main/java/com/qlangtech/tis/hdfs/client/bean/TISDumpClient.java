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
package com.qlangtech.tis.hdfs.client.bean;

import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.fs.IPath;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.fs.ITaskContext;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;
import com.qlangtech.tis.hdfs.client.context.impl.TSearcherDumpContextImpl;

import com.qlangtech.tis.hdfs.client.data.MultiThreadDataProvider;
import com.qlangtech.tis.offline.TableDumpFactory;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.trigger.util.TriggerParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-6-29
 */
public class TISDumpClient {

    public static final String DEFAULT_SERVLET_CONTEXT = "tis-search";

    private static final Logger logger = LoggerFactory.getLogger(TISDumpClient.class);

    private TSearcherDumpContextImpl dumpContext;

    @SuppressWarnings("all")
    private MultiThreadDataProvider fullHdfsProvider;

    private String servletContextName = DEFAULT_SERVLET_CONTEXT;

    private final TableDumpFactory flatTableBuilder;

    private final ITISFileSystem fileSystem;

    public TISDumpClient(TableDumpFactory flatTableBuilder) {
        super();
        Objects.requireNonNull(flatTableBuilder, "flatTableBuilder can not be null");
        this.flatTableBuilder = flatTableBuilder;
        this.fileSystem = this.flatTableBuilder.getFileSystem();
    }

    @SuppressWarnings("all")
    public MultiThreadDataProvider getFullHdfsProvider() {
        return fullHdfsProvider;
    }

    @SuppressWarnings("all")
    public void setFullHdfsProvider(MultiThreadDataProvider fullHdfsProvider) {
        this.fullHdfsProvider = fullHdfsProvider;
    }

    public String getServletContextName() {
        return servletContextName;
    }

    public void setServletContextName(String servletContextName) {
        this.servletContextName = servletContextName;
    }

    public void close() {
        if (this.fileSystem != null) {
            fileSystem.close();
        }
    }

    public void copyToLocalFile(String src, String dst) {
        IPath srcPath = fileSystem.getPath(src);
        File dstPath = new File(dst);
        fileSystem.copyToLocalFile(srcPath, dstPath);
    }

    @SuppressWarnings("all")
    public void executeDumpTask(boolean isIncre, boolean force, TriggerParamProcess triggerParamProcess, String startTime, ITaskContext context) throws Exception {
        // JobExecutionContext
        TSearcherDumpContext dumpContext = this.getDumpContext();
        final Map result = new HashMap();
        MultiThreadDataProvider.setDumpLaunchTime(result, startTime);
        MultiThreadDataProvider dataProvider = this.getFullHdfsProvider();
        Assert.assertNotNull("full dump hdfs provider can not be null", dataProvider);
        EntityName dumptable = dumpContext.getDumpTable();

        // 判断是否有必要执行本次dump任务
        if (!dataProvider.shallProcessDumpTask(startTime, force, context)) {
            return;
        }
        // 删除历史文件
        flatTableBuilder.deleteHistoryFile(dumptable, context);
        dataProvider.importServiceData(result);
        // 导入完成,需要绑定hive表
        flatTableBuilder.bindTables(Collections.singleton(dumpContext.getDumpTable()), MultiThreadDataProvider.getDumpLaunchTimestamp(result), context);
        // 最终生成成功
        dataProvider.createSuccessToken(startTime);


    }

    public TSearcherDumpContext getDumpContext() {
        return this.dumpContext;
    }

    public void setDumpContext(TSearcherDumpContextImpl dumpContext) {
        this.dumpContext = dumpContext;
    }

    public interface TriggerParamProcess {

        void callback(TriggerParam param);
    }
}
