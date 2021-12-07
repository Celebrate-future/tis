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

import java.util.Map;
import com.qlangtech.tis.exception.SourceDataReadException;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface SourceDataProvider<K, V> {

//    public String getDbHost();
//
//    public String getDsName();
//
//    // baisui add for init the obj
//    public void setDumpContext(TSearcherDumpContext context);
//
//    /**
//     * 初始化操作
//     */
//    public void init() throws SourceDataReadException;
//
//    /**
//     * 打开资源连接如数据库,文件句柄
//     *
//     * @throws Exception
//     */
//    public void openResource() throws SourceDataReadException;
//
//    /**
//     * 释放数据库或者文件资源
//     *
//     * @throws SourceDataReadException
//     */
//    public void closeResource() throws SourceDataReadException;
//
//    /**
//     * 取得行记录条数
//     * @return
//     */
//    public int getRowSize();
//
//    /**
//     * 判断是否还有数据
//     *
//     * @return
//     * @throws Exception
//     */
//    public boolean hasNext() throws SourceDataReadException;
//
//    /**
//     * 获取一条记录
//     *
//     * @return
//     * @throws Exception
//     */
//    public Map<K, V> next() throws SourceDataReadException;
}
