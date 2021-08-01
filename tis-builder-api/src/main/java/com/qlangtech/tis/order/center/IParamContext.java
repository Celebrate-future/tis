/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.order.center;

import com.qlangtech.tis.exec.ExecutePhaseRange;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年12月11日 上午11:09:21
 */
public interface IParamContext {

    public String KEY_PARTITION = "ps";

    public String COMPONENT_START = "component.start";

    public String COMPONENT_END = "component.end";

    public String KEY_TASK_ID = "taskid";

    public String KEY_ASYN_JOB_NAME = "asynJobName";
    public String KEY_ASYN_JOB_SUCCESS = "success";
    public String KEY_ASYN_JOB_COMPLETE = "complete";

    public String KEY_EXEC_RESULT = "execresult";

    public String KEY_BUILD_TARGET_TABLE_NAME = "targetTableName";

    public String KEY_BUILD_INDEXING_ALL_ROWS_COUNT = "indexing.all.rows.count";
    String KEY_REQUEST_DISABLE_TRANSACTION = "disableTransaction";

    ExecutePhaseRange getExecutePhaseRange();

    public String getString(String key);

    public boolean getBoolean(String key);

    public int getInt(String key);

    public long getLong(String key);

    public String getPartitionTimestamp();
}
