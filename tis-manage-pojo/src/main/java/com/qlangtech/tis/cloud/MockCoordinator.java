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
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class MockCoordinator implements ITISCoordinator {
    @Override
    public <T> T unwrap() {
        return null;
    }

    @Override
    public boolean shallConnect2RemoteIncrStatusServer() {
        return false;
    }

    @Override
    public List<String> getChildren(String zkPath, Watcher watcher, boolean b) {
        throw new UnsupportedOperationException("zkPath:" + zkPath);
    }

    @Override
    public void addOnReconnect(IOnReconnect onReconnect) {
    }

    @Override
    public void create(String path, byte[] data, boolean persistent, boolean sequential) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists(String s, boolean b) {
        return false;
    }

    @Override
    public byte[] getData(String s, Watcher o, Stat stat, boolean b) {
        throw new UnsupportedOperationException();
    }
}
