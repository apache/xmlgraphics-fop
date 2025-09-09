/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.accessibility.fo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import static org.junit.Assert.assertTrue;

import org.apache.commons.io.IOUtils;

import org.apache.fop.accessibility.StructureTree2SAXEventAdapter;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.FODocumentParser;
import org.apache.fop.fo.FODocumentParser.FOEventHandlerFactory;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.LoadingException;
import org.apache.fop.fotreetest.DummyFOEventHandler;

public class FO2StructureTreeConverterTestCase {

    private static class FOLoader {

        private final String resourceName;

        FOLoader(String resourceName) {
            this.resourceName = resourceName;
        }

        public InputStream getFoInputStream() {
            return getResource(resourceName);
        }
    }

    private static final String STRUCTURE_TREE_SEQUENCE_NAME = "structure-tree-sequence";

    private FOLoader foLoader;
    private boolean keepEmptyTags = true;

    @Test
    public void testCompleteDocument() throws Exception {
        testConverter("/org/apache/fop/fo/complete_document.fo");
    }

    @Test
    public void testPDFUAMergeWithExternalDocument() throws Exception {
        testConverter("/org/apache/fop/fo/pdfua_with_external_document.fo");
    }

    @Test
    public void testAbbreviationProperty() throws Exception {
        testConverter("abb.fo");
    }

    @Test
    public void testTableFooters() throws Exception {
        testConverter("table-footers.fo");
    }

    @Test
    public void testArtifact() throws Exception {
        testConverter("artifact.fo");
    }

    @Test
    public void testSideRegions() throws Exception {
        testConverter("/org/apache/fop/fo/pagination/side-regions.fo");
    }

    @Test
    public void headerTableCellMustPropagateScope() throws Exception {
        testConverter("table-header_scope.fo");
    }

    @Test
    public void testTableArtifact() throws Exception {
        testConverter("table-artifact.fo");
    }

    @Test
    public void testLanguage() throws Exception {
        testConverter("language.fo");
    }

    private static InputStream getResource(String name) {
        return FO2StructureTreeConverterTestCase.class.getResourceAsStream(name);
    }

    @Test
    public void testRemoveBlocks() throws Exception {
        keepEmptyTags = false;
        compare("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\n"
                        + "  <fo:layout-master-set>\n"
                        + "    <fo:simple-page-master master-name=\"simple\">\n"
                        + "      <fo:region-body />\n"
                        + "    </fo:simple-page-master>\n"
                        + "  </fo:layout-master-set>\n"
                        + "  <fo:page-sequence master-reference=\"simple\">\n"
                        + "    <fo:flow flow-name=\"xsl-region-body\">\n"
                        + "    <fo:block/>"
                        + "    <fo:block><fo:block/></fo:block>\n"
                        + "    <fo:block>a</fo:block>\n"
                        + "    <fo:block><fo:leader/></fo:block>\n"
                        + "    <fo:block>a<fo:leader/></fo:block>\n"
                        + "    </fo:flow>\n"
                        + "  </fo:page-sequence>\n"
                        + "</fo:root>\n",
                        "<structure-tree-sequence>\n"
                        + "<structure-tree xmlns=\"http://xmlgraphics.apache.org/fop/intermediate\" "
                        + "xmlns:foi=\"http://xmlgraphics.apache.org/fop/internal\" "
                        + "xmlns:fox=\"http://xmlgraphics.apache.org/fop/extensions\">\n"
                        + "<fo:flow xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" flow-name=\"xsl-region-body\">\n"
                        + "<fo:block>\n"
                        + "<marked-content/>\n"
                        + "</fo:block>\n"
                        + "<fo:block>\n"
                        + "<marked-content/>\n"
                        + "</fo:block>\n"
                        + "</fo:flow>\n"
                        + "</structure-tree>\n"
                        + "</structure-tree-sequence>\n");
    }

    @Test
    public void testRemoveTableHeader() throws Exception {
        keepEmptyTags = false;
        String fo = IOUtils.toString(getResource("table-artifact.fo"), StandardCharsets.UTF_8)
                .replace("role=\"artifact\"", "");
        compare(fo, "<structure-tree-sequence>\n"
                        + "<structure-tree xmlns=\"http://xmlgraphics.apache.org/fop/intermediate\" "
                        + "xmlns:foi=\"http://xmlgraphics.apache.org/fop/internal\" "
                        + "xmlns:fox=\"http://xmlgraphics.apache.org/fop/extensions\">\n"
                        + "<fo:flow xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" flow-name=\"xsl-region-body\">\n"
                        + "<fo:table>\n"
                        + "<fo:table-body>\n"
                        + "<fo:table-row>\n"
                        + "<fo:table-cell>\n"
                        + "<fo:block>\n"
                        + "<marked-content/>\n"
                        + "<fo:block>\n"
                        + "<marked-content/>\n"
                        + "</fo:block>\n"
                        + "<marked-content/>\n"
                        + "</fo:block>\n"
                        + "</fo:table-cell>\n"
                        + "</fo:table-row>\n"
                        + "</fo:table-body>\n"
                        + "</fo:table>\n"
                        + "</fo:flow>\n"
                        + "</structure-tree>\n"
                        + "</structure-tree-sequence>");
    }

    @Test
    public void testExternalGraphicArtifact() throws Exception {
        String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\n"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master master-name=\"simple\" page-height=\"27.9cm\" page-width=\"21.6cm\">\n"
                + "      <fo:region-body />\n"
                + "    </fo:simple-page-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"simple\">\n"
                + "    <fo:flow flow-name=\"xsl-region-body\">\n"
                + "<fo:block><fo:external-graphic src=\"test/resources/fop/image/logo.jpg\" role=\"artifact\"/>"
                + "</fo:block></fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>\n";
        compare(fo, "<structure-tree-sequence>"
                + "<structure-tree xmlns=\"http://xmlgraphics.apache.org/fop/intermediate\" "
                + "xmlns:foi=\"http://xmlgraphics.apache.org/fop/internal\" "
                + "xmlns:fox=\"http://xmlgraphics.apache.org/fop/extensions\">"
                + "<fo:flow xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" flow-name=\"xsl-region-body\">"
                + "<fo:block/></fo:flow></structure-tree></structure-tree-sequence>");
    }

    @Test
    public void testSVGArtifact() throws Exception {
        String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" xmlns:svg=\"http://www.w3.org/2000/svg\">"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master page-width=\"8.5in\" page-height=\"11in\" master-name=\"Page\">\n"
                + "      <fo:region-body region-name=\"Body\"/>\n"
                + "    </fo:simple-page-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"Page\">\n"
                + "    <fo:flow flow-name=\"Body\">\n"
                + "      <fo:block>\n"
                + "        <fo:instream-foreign-object role=\"artifact\">\n"
                + "          <svg:svg width=\"12cm\" height=\"12cm\">\n"
                + "          </svg:svg>\n"
                + "        </fo:instream-foreign-object>\n"
                + "      </fo:block>\n"
                + "    </fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>";
        compare(fo, "<structure-tree-sequence><structure-tree xmlns=\"http://xmlgraphics.apache.org/fop/intermediate\">"
                + "<fo:flow xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" flow-name=\"xsl-region-body\">"
                + "<fo:block><marked-content/><marked-content/></fo:block></fo:flow></structure-tree>"
                + "</structure-tree-sequence>");
    }

    private void compare(final String fo, String tree) throws Exception {
        foLoader = new FOLoader("") {
            public InputStream getFoInputStream() {
                return new ByteArrayInputStream(fo.getBytes());
            }
        };
        DOMResult actualStructureTree = buildActualStructureTree();
        Document doc = (Document) actualStructureTree.getNode();
        StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(tree, sw.toString());
    }

    private void testConverter(String foResourceName) throws Exception {
        foLoader = new FOLoader(foResourceName);
        DOMResult expectedStructureTree = loadExpectedStructureTree();
        DOMResult actualStructureTree = buildActualStructureTree();
        final Diff diff = createDiff(expectedStructureTree, actualStructureTree);
        assertTrue(diff.toString(), diff.identical());
    }

    private DOMResult loadExpectedStructureTree() {
        DOMResult expectedStructureTree = new DOMResult();
        InputStream xslt = getResource("fo2StructureTree.xsl");
        runXSLT(xslt, foLoader.getFoInputStream(), expectedStructureTree);
        return expectedStructureTree;
    }

    private static void runXSLT(InputStream xslt, InputStream doc, Result result) {
        Source fo = new StreamSource(doc);
        try {
            Transformer transformer = TransformerFactory.newInstance()
                    .newTransformer(new StreamSource(xslt));
            transformer.transform(fo, result);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        } finally {
            closeStream(xslt);
            closeStream(doc);
        }
    }

    private static void closeStream(InputStream stream) {
        try {
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DOMResult buildActualStructureTree() throws Exception {
        DOMResult actualStructureTree = new DOMResult();
        createStructureTreeFromDocument(foLoader.getFoInputStream(), actualStructureTree);
        return actualStructureTree;
    }

    private void createStructureTreeFromDocument(InputStream foInputStream,
            Result result) throws Exception {
        TransformerHandler tHandler = createTransformerHandler(result);
        startStructureTreeSequence(tHandler);
        StructureTreeEventHandler structureTreeEventHandler
                = StructureTree2SAXEventAdapter.newInstance(tHandler);
        FODocumentParser documentParser = createDocumentParser(structureTreeEventHandler);
        FOUserAgent userAgent = createFOUserAgent(documentParser);
        parseDocument(foInputStream, documentParser, userAgent);
        endStructureTreeSequence(tHandler);
    }

    private static TransformerHandler createTransformerHandler(Result domResult)
            throws TransformerConfigurationException, TransformerFactoryConfigurationError {
        SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        TransformerHandler transformerHandler = factory.newTransformerHandler();
        transformerHandler.setResult(domResult);
        return transformerHandler;
    }

    private static void startStructureTreeSequence(TransformerHandler tHandler) throws SAXException {
        tHandler.startDocument();
        tHandler.startElement("", STRUCTURE_TREE_SEQUENCE_NAME, STRUCTURE_TREE_SEQUENCE_NAME,
                new AttributesImpl());
    }

    private static FODocumentParser createDocumentParser(
            final StructureTreeEventHandler structureTreeEventHandler) {
        return FODocumentParser.newInstance(new FOEventHandlerFactory() {
            public FOEventHandler newFOEventHandler(FOUserAgent foUserAgent) {
                return new FO2StructureTreeConverter(structureTreeEventHandler,
                        new DummyFOEventHandler(foUserAgent));
            }
        });
    }

    private FOUserAgent createFOUserAgent(FODocumentParser documentParser) {
        FOUserAgent userAgent = documentParser.createFOUserAgent();
        userAgent.setAccessibility(true);
        userAgent.setKeepEmptyTags(keepEmptyTags);
        return userAgent;
    }

    private static void parseDocument(InputStream foInputStream, FODocumentParser documentParser,
            FOUserAgent userAgent) throws FOPException, LoadingException {
        try {
            documentParser.parse(foInputStream, userAgent);
        } finally {
            closeStream(foInputStream);
        }
    }

    private static void endStructureTreeSequence(TransformerHandler tHandler) throws SAXException {
        tHandler.endElement("", STRUCTURE_TREE_SEQUENCE_NAME, STRUCTURE_TREE_SEQUENCE_NAME);
        tHandler.endDocument();
    }

    private static Diff createDiff(DOMResult expected, DOMResult actual) {
        Diff diff = new Diff(getDocument(expected), getDocument(actual));
        return diff;
    }

    private static Document getDocument(DOMResult result) {
        return (Document) result.getNode();
    }
}
