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
package com.qlangtech.tis.fullbuild.taskflow;

import com.qlangtech.tis.fs.IFs2Table;
import com.qlangtech.tis.fs.ITaskContext;
import com.qlangtech.tis.fullbuild.phasestatus.IJoinTaskStatus;
import com.qlangtech.tis.sql.parser.ISqlTask;

/**
 * 执行打宽表任务
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年10月31日 下午10:14:48
 */
public interface ITaskFactory {

    /**
     * 创建打宽表join节点
     *
     * @param nodeMeta
     * @param isFinalNode    是否是DF的最终节点
     * @param tplContext
     * @param taskContext
     * @param fs2Table
     * @param joinTaskStatus
     * @return
     */
    public DataflowTask createTask(ISqlTask nodeMeta, boolean isFinalNode, ITemplateContext tplContext, ITaskContext taskContext, IFs2Table fs2Table, IJoinTaskStatus joinTaskStatus);
}
