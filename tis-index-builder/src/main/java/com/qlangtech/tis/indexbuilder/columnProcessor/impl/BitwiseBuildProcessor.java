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
package com.qlangtech.tis.indexbuilder.columnProcessor.impl;

import com.qlangtech.tis.indexbuilder.columnProcessor.AdapterExternalDataColumnProcessor;
import com.qlangtech.tis.solrdao.extend.ProcessorSchemaField;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将目标列的内容构建成一个bitwise的組合列
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class BitwiseBuildProcessor extends AdapterExternalDataColumnProcessor {

    public static final String NAME = "bitwiseBuild";

    // 格式可以这样：pickup_flag,delivery_flag,logistics_flag,logistics_flag:2
    private List<FlagCriteria> sourceColsList = null;

    private final int sourceColsListLength;

    private String targetCol = null;

    private static final Pattern PATTERN_FLAG_CRITERIA = Pattern.compile("(\\w+)(:\\d+)?");

    private static final Logger logger = LoggerFactory.getLogger(BitwiseBuildProcessor.class);

    private static final String TRUE_VAL = "1";

    public BitwiseBuildProcessor(ProcessorSchemaField processorMap) {
        final String sourceCols = processorMap.getParam("sourceCols");
        this.sourceColsList = new ArrayList<>();
        Matcher m = PATTERN_FLAG_CRITERIA.matcher(sourceCols);
        String numVal = null;
        while (m.find()) {
            numVal = m.group(2);
            this.sourceColsList.add(new FlagCriteria(m.group(1), numVal != null ? StringUtils.substringAfter(numVal, ":") : TRUE_VAL));
        }
        if (this.sourceColsList.size() < 1) {
            throw new IllegalArgumentException("param sourceCols:" + sourceCols + " lenght can not small than 1");
        }
        int multip = 1;
        this.sourceColsListLength = this.sourceColsList.size();
        for (int i = this.sourceColsListLength - 1; i >= 0; i--) {
            this.sourceColsList.get(i).multip = multip;
            multip *= 2;
        }
        this.targetCol = processorMap.getParam("targetCol");
        if (StringUtils.isEmpty(this.targetCol)) {
            throw new IllegalArgumentException("key 'targetCol' can not be empty");
        }
    }

    @Override
    public void process(SolrInputDocument doc, Map<String, String> entry) {
        int val = 0;
        FlagCriteria criteria = null;
        for (int i = (this.sourceColsListLength - 1); i > -1; i--) {
            criteria = this.sourceColsList.get(i);
            if (criteria.isMeet(entry)) {
                val += criteria.multip;
            }
        }
        String entityid = entry.get("entity_id");
        if ("99934038".equals(entityid)) {
            StringBuffer buffer = new StringBuffer();
            for (FlagCriteria c : sourceColsList) {
                buffer.append(c.colname).append(":").append(entry.get(c.colname)).append(",");
            }
            logger.info("val:" + val + "," + buffer.toString());
        }
        if (val > 0) {
            doc.setField(targetCol, val);
        } else {
            doc.remove(targetCol);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    private class FlagCriteria {

        private final String colname;

        private final String criteria;

        private int multip;

        FlagCriteria(String colname, String criteria) {
            this.colname = colname;
            this.criteria = StringUtils.trim(criteria);
        }

        public boolean isMeet(Map<String, String> row) {
            return criteria.equals(row.get(this.colname));
        }
    }
}
