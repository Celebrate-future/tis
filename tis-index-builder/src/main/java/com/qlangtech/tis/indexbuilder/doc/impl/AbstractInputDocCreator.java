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
package com.qlangtech.tis.indexbuilder.doc.impl;

import com.qlangtech.tis.indexbuilder.columnProcessor.ExternalDataColumnProcessor;
import com.qlangtech.tis.indexbuilder.doc.IInputDocCreator;
import com.qlangtech.tis.indexbuilder.doc.ReusableSolrInputDocument;
import com.qlangtech.tis.indexbuilder.map.RawDataProcessor;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.schema.IndexSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AbstractInputDocCreator implements IInputDocCreator {

    public static String DOC_CREATOR_DEFAULT = "default";

    private final RawDataProcessor rawDataProcessor;

    protected final IndexSchema indexSchema;

    private final boolean hasRowProcessor;

    protected final String uniqueKeyFieldName;

    private final String newVersion;

    private static final Logger logger = LoggerFactory.getLogger(AbstractInputDocCreator.class);

    /**
     * @param typeName
     * @param rawDataProcessor
     * @param indexSchema
     * @param newVersion
     * @return
     */
    @SuppressWarnings("all")
    public static IInputDocCreator createDocumentCreator(String typeName, RawDataProcessor rawDataProcessor, IndexSchema indexSchema, String newVersion) {
        if (StringUtils.isBlank(typeName)) {
            throw new IllegalStateException("param typeName can not be null");
        }
        if (DOC_CREATOR_DEFAULT.equals(typeName)) {
            logger.info("inputdoc type:default");
            return new DefaultInputDocCreator(rawDataProcessor, indexSchema, newVersion);
        }
        try {
            Class<IInputDocCreator> clazz = (Class<IInputDocCreator>) Class.forName(typeName);
            Constructor<IInputDocCreator> constructor = clazz.getConstructor(RawDataProcessor.class, IndexSchema.class, String.class);
            return constructor.newInstance(rawDataProcessor, indexSchema, newVersion);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    AbstractInputDocCreator(RawDataProcessor rawDataProcessor, IndexSchema indexSchema, String newVersion) {
        super();
        this.rawDataProcessor = rawDataProcessor;
        this.indexSchema = indexSchema;
        this.hasRowProcessor = (rawDataProcessor.getRowProcess().size() > 0);
        this.uniqueKeyFieldName = indexSchema.getUniqueKeyField().getName();
        this.newVersion = newVersion;
    }

    // public SolrInputDocument createSolrInputDocument(SourceReader
    // recordReader)
    // throws Exception;
    protected final SolrInputDocument getLuceneDocument(Map<String, String> fieldValues) {
        ReusableSolrInputDocument solrDoc = createDocument();
        if (this.hasRowProcessor) {
            for (ExternalDataColumnProcessor rowProcessor : rawDataProcessor.getRowProcess()) {
                rowProcessor.process(solrDoc, fieldValues);
            }
        }
        for (Entry<String, String> entry : fieldValues.entrySet()) {
            // String value = fieldValues.get(name);
            // 只重置值不创建field，减少gc
            // fieldKey = entry.getKey();
            ExternalDataColumnProcessor pas = rawDataProcessor.getExternalColumnProcessors(entry.getKey());
            // if (pas != null) {
            pas.process(solrDoc, entry);
        }
        if (solrDoc.getField(uniqueKeyFieldName) == null) {
            throw new IllegalStateException("lack of pk field:" + uniqueKeyFieldName + ",doc:" + solrDoc.toString());
        }
        if (!solrDoc.containsKey(CommonParams.VERSION_FIELD)) {
            solrDoc.setField(CommonParams.VERSION_FIELD, newVersion);
        }
        return solrDoc;
    }

    protected ReusableSolrInputDocument createDocument() {
        return new ReusableSolrInputDocument(indexSchema);
    }
}
