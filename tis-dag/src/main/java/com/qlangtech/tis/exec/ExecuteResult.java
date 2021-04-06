/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.exec;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年12月15日 上午11:49:59
 */
public class ExecuteResult {

    final boolean success;

    private String message;

    public static ExecuteResult SUCCESS = new ExecuteResult(true);

    public static ExecuteResult createFaild() {
        return new ExecuteResult(false);
    }

    public static ExecuteResult createSuccess() {
        return new ExecuteResult(true);
    }

    public ExecuteResult(boolean success) {
        super();
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public ExecuteResult setMessage(String message) {
        this.message = message;
        return this;
    }

    public boolean isSuccess() {
        return this.success;
    }
}
