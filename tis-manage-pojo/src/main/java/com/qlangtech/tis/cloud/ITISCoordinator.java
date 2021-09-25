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
package com.qlangtech.tis.cloud;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * ZK的抽象
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface ITISCoordinator extends ICoordinator {

    /**
     * 是否应该连接Assemble日志收集服务，单元测试过程中需要返回false
     *
     * @return
     */
    boolean shallConnect2RemoteIncrStatusServer();

    List<String> getChildren(String zkPath, Watcher watcher, boolean b);

    void addOnReconnect(IOnReconnect onReconnect);

    byte[] getData(String s, Watcher o, Stat stat, boolean b);

    void create(String path, byte[] data, boolean persistent, boolean sequential);

    boolean exists(String path, boolean watch);

    public interface IOnReconnect {

        public void command();
    }
}
