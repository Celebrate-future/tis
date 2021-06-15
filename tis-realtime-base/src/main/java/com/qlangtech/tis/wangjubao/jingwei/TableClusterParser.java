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
package com.qlangtech.tis.wangjubao.jingwei;

import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.solrdao.impl.ParseResult;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年6月25日上午11:55:52
 */
public class TableClusterParser {

    public static final DocumentBuilderFactory schemaDocumentBuilderFactory;

    static final XPathFactory xpathFactory = XPathFactory.newInstance();

    static {
        schemaDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
        schemaDocumentBuilderFactory.setValidating(false);
    }

    /**
     * @param value 配置文件内容
     * @param schemaFieldsRefect
     * @return
     * @throws Exception
     */
    public static TableCluster parse(String value, ParseResult schemaFieldsRefect) throws Exception {
        DocumentBuilder builder = schemaDocumentBuilderFactory.newDocumentBuilder();
        builder.setEntityResolver(new EntityResolver() {

            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                InputSource source = new InputSource();
                source.setCharacterStream(new StringReader(""));
                return source;
            }
        });
        TableCluster tableCluster = new TableCluster(schemaFieldsRefect);
        Table table = null;
        InputSource source = new InputSource();
        StringReader reader = null;
        try {
            reader = new StringReader(value);
            source.setCharacterStream(reader);
            Document document = builder.parse(source);
            final XPath xpath = xpathFactory.newXPath();
            String expression = "/doc/shareKey";
            String shareKey = (String) xpath.evaluate(expression, document, XPathConstants.STRING);
            // if (StringUtils.isBlank(shareKey)) {
            // throw new IllegalStateException("sharekey id not define");
            // }
            // tableCluster.setSharedKey(StringUtils.trimToEmpty(shareKey));
            expression = "/doc/table";
            NodeList tables = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
            NodeList fields = null;
            String tabName = null;
            Node fieldNode = null;
            NamedNodeMap attrs = null;
            String name = null;
            // String alias = null;
            // String primaryKey = null;
            // String indexName = null;
            TabField tabField = null;
            String pk = null;
            for (int i = 0; i < tables.getLength(); i++) {
                Node node = tables.item(i);
                attrs = node.getAttributes();
                tabName = getAttr(attrs, "name");
                // primaryKey = getAttr(attrs, "primaryKey");
                // Assert.assertNotNull("primaryKey", primaryKey);
                // indexName = getAttr(attrs, "index");
                // Assert.assertNotNull("index", indexName);
                // int groupSize = Integer.parseInt(getAttr(attrs, "groupSize"));
                table = new Table(tabName, schemaFieldsRefect);
                // table.setPrimaryKey(primaryKey);
                if (attrs.getNamedItem("logkeys") != null) {
                    String[] logkeys = StringUtils.split(getAttr(attrs, "logkeys"), ",");
                    table.setLogKeys(logkeys);
                }
                fields = node.getChildNodes();
                for (int fcount = 0; fcount < fields.getLength(); fcount++) {
                    fieldNode = fields.item(fcount);
                    if (fieldNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }
                    if ("alias".equals(fieldNode.getNodeName())) {
                        attrs = fieldNode.getAttributes();
                        name = getAttr(attrs, "name");
                        Assert.assertNotNull("name can not be null", name);
                        tabField = new TabField(tabName, name);
                        tabField.setGroovyScript(StringUtils.trim(fieldNode.getTextContent()));
                        table.addAliasField(tabField);
                        continue;
                    }
                    // <remove cols="sellerId" />
                    String cols = null;
                    if ("remove".equals(fieldNode.getNodeName())) {
                        attrs = fieldNode.getAttributes();
                        cols = getAttr(attrs, "cols");
                        if (StringUtils.isEmpty(cols)) {
                            throw new IllegalStateException("element remove's attribute cols can not be null");
                        }
                        table.setIgnorFiles(cols);
                        continue;
                    }
                    String script = null;
                    if ("ignor".equals(fieldNode.getNodeName())) {
                        script = StringUtils.trim(fieldNode.getTextContent());
                        if (StringUtils.isEmpty(script)) {
                            throw new IllegalStateException("element ignor's content can not be null");
                        }
                        TableIgnorRule ignorRule = new TableIgnorRule();
                        ignorRule.setGroovyScript(table.getName(), fcount, script);
                        table.addRecordIgnorRule(ignorRule);
                        continue;
                    }
                    if ("deletecriteria".equalsIgnoreCase(fieldNode.getNodeName())) {
                        tabField = new TabField(tabName, "deletecriteria");
                        tabField.setGroovyScript(StringUtils.trim(fieldNode.getTextContent()));
                        table.setDeleteCriteria(tabField);
                        continue;
                    }
                }
                tableCluster.add(table);
            }
        } finally {
            reader.close();
        }
        return tableCluster;
    }

    private static String getAttr(NamedNodeMap attrs, String name) {
        Node attr = attrs == null ? null : attrs.getNamedItem(name);
        if (attr == null) {
            throw new RuntimeException(name + ": missing mandatory attribute '" + name + "'");
        }
        String val = attr.getNodeValue();
        return val;
    }

    public static void main(String[] args) throws Exception {
    // TableClusterParser parse = new TableClusterParser();
    // 
    // TableCluster cluster = parse
    // .parse(FileUtils
    // .readFileToString(new File(
    // "D:\\j2ee_solution\\eclipse-standard-kepler-SR2-win32-x86_64\\workspace\\tis-realtime-transfer\\src\\main\\resources\\wjb.xml_bak")));
    // 
    // System.out.println(cluster.getSharedKey());
    // 
    // Table table = cluster.getTable("t_buyer");
    // System.out.println(table.getName());
    // //System.out.println(table.getPrimaryKey());
    // 
    // System.out.println(table.shallIgnor("sellerId"));
    // <field column="aaa" solrFieldName="bbbb" type="" />
    // String alias = table.findAliasColumn("aaa");
    // 
    // System.out.println(alias);
    // System.out.println(field.getType());
    }
}
