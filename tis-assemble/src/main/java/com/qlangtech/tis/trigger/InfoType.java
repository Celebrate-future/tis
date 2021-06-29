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

/**
 *
 */
package com.qlangtech.tis.trigger;

public enum InfoType {
    INFO(1), WARN(2), ERROR(3), FATAL(4);

    private final int type;

    private InfoType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static InfoType getType(int type) {

        for (InfoType t : InfoType.values()) {
            if (type == t.type) {
                return t;
            }
        }

        throw new IllegalArgumentException("type:" + type + " is invalid");
    }
}
