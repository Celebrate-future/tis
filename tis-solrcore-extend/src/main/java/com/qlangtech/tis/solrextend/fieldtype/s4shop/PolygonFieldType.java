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
package com.qlangtech.tis.solrextend.fieldtype.s4shop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通过用户传过来的点集合构建WKT可识别的字符串内容
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class PolygonFieldType extends BaseShapeFieldType {

    static final Logger logger = LoggerFactory.getLogger(PolygonFieldType.class);

    static final String KEY_LAT = "latitude";

    static final String KEY_LONGITUDE = "longitude";

    static final String POLYGON_PREFIX = "POLYGON((";

    @Override
    protected StringBuffer buildShapLiteria(Object val) {
        JSONObject point;
        JSONArray array = null;
        try {
            point = null;
            JSONTokener t = new JSONTokener(String.valueOf(val));
            array = new JSONArray(t);
        } catch (JSONException e) {
            logger.warn("field{}", val, e);
            return null;
        }
        int len = array.length();
        if (len < 3) {
            // 多边形至少要三个点以上
            return null;
        }
        // POLYGON (( 120.1596 30.2447, 120.1595 30.3447, 120.1695 30.2447,120.1596 //
        // 30.2447))
        StringBuffer buffer = new StringBuffer(POLYGON_PREFIX);
        for (int i = 0; i < len; i++) {
            point = array.getJSONObject(i);
            buffer.append(point.get(KEY_LONGITUDE)).append(" ").append(point.get(KEY_LAT));
            buffer.append(",");
        }
        // 形成一个环
        point = array.getJSONObject(0);
        buffer.append(point.get(KEY_LONGITUDE)).append(" ").append(point.get(KEY_LAT));
        buffer.append(" ))");
        return buffer;
    }

    @Override
    protected boolean isShapeLiteria(Object val) {
        return String.valueOf(val).startsWith(POLYGON_PREFIX);
    }
}
