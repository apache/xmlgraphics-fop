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

package org.apache.fop.render.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamResult;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.helpers.AttributesImpl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFactory;
import org.apache.fop.pdf.PDFResources;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.StandardStructureTypes;
import org.apache.fop.render.intermediate.IFContext;

public class PDFStructureTreeBuilderTestCase {
    private PDFFactory pdfFactory;

    private static final List<String> TABLE_TAGS = Arrays.asList("table-header", "table-footer", "table-body");

    @Before
    public void setUp() {
        PDFDocument doc = new PDFDocument("");
        pdfFactory = new PDFFactory(doc);
    }
    @Test
    public void testAddImageContentItem() throws IOException {
        PDFStructElem structElem = new PDFStructElem(null, StandardStructureTypes.Illustration.FIGURE);
        structElem.setDocument(pdfFactory.getDocument());
        PDFLogicalStructureHandler logicalStructureHandler = new PDFLogicalStructureHandler(null);
        logicalStructureHandler.startPage(pdfFactory.makePage(new PDFResources(pdfFactory.getDocument()), 0, 0));
        logicalStructureHandler.addImageContentItem(structElem);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        structElem.output(bos);
        assertEquals(bos.toString(), "<< /S /Figure /K [<<\n"
                + "  /Type /MCR\n"
                + "  /Pg 1 0 R\n"
                + "  /MCID 0\n"
                + ">>] >>");
    }

    @Test
    public void testMemoryLeak() throws IOException {
        PDFStructElem structElem = new PDFStructElem(new PDFStructElem(), StandardStructureTypes.Table.TABLE);
        structElem.addKid(new PDFStructElem());
        structElem.output(new ByteArrayOutputStream());
        assertNull(structElem.getParent());
        assertNull(structElem.getParentStructElem());
        assertNull(structElem.getKids());
    }

    @Test
    public void testExternalDocumentBuilder() {
        pdfFactory.getDocument().makeStructTreeRoot(null);

        PDFStructElem elem = PDFStructureTreeBuilder.createStructureElement("external-document", new PDFStructElem(),
                new AttributesImpl(), pdfFactory, null);

        assertNotNull("Must create a PDFStructElem", elem);
        assertEquals("Elem must be of type figure", StandardStructureTypes.Illustration.FIGURE,
                elem.getStructureType());
    }

    @Test
    public void testNonFilteredTag() throws Exception {
        Result result = defaultStartNode(new HashMap<>(), "table-row");
        assertNotNull("Table row is not one of the filtered tags", result.structTree);
    }

    @Test
    public void testPDFATableTags() throws Exception {
        List<String> invalidPDFAModes = Arrays.asList("PDF/A-1a", "PDF/A-1b");

        Result result;
        for (String pdfMode : invalidPDFAModes) {
            Map<String, String> rendererOptionalMap = new HashMap<>();
            rendererOptionalMap.put("pdf-a-mode", pdfMode);

            for (String tag : TABLE_TAGS) {
                result = defaultStartNode(rendererOptionalMap, tag);
                assertNull("These tags are not valid for PDF/A-1", result.structTree);
            }
        }

        List<String> validPDFAModes = Arrays.asList("PDF/A-2a", "PDF/A-2b", "PDF/A-2u",
                "PDF/A-3a", "PDF/A-3b", "PDF/A-3u");

        for (String pdfMode : validPDFAModes) {
            Map<String, String> rendererOptionalMap = new HashMap<>();
            rendererOptionalMap.put("pdf-a-mode", pdfMode);

            for (String tag : TABLE_TAGS) {
                result = defaultStartNode(rendererOptionalMap, tag);
                assertNotNull("These tags are valid on for PDF/A-2 and PDFA/-3", result.structTree);
            }
        }
    }

    @Test
    public void testPDFUATableTags() throws Exception {
        Map<String, String> rendererOptionalMap = new HashMap<>();
        rendererOptionalMap.put("pdf-ua-mode", "PDF/UA");

        Result result;
        for (String tag : TABLE_TAGS) {
            result = defaultStartNode(rendererOptionalMap, tag);
            assertNotNull("Tags are valid for PDF/UA", result.structTree);
        }
    }

    private static class Result {
        public StructureTreeElement structTree;
        public PDFDocument pdfDoc;

        public Result(StructureTreeElement structTree, PDFDocument pdfDoc) {
            this.structTree = structTree;
            this.pdfDoc = pdfDoc;
        }
    }

    private Result defaultStartNode(Map<String, String> rendererOptionalMap, String nodeName)
            throws Exception {
        FOUserAgent userAgent = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        userAgent.getRendererOptions().putAll(rendererOptionalMap);
        userAgent.setAccessibility(true);

        PDFDocumentHandler documentHandler = new PDFDocumentHandler(new IFContext(userAgent));
        documentHandler.setResult(new StreamResult(new ByteArrayOutputStream()));
        documentHandler.getStructureTreeEventHandler();
        documentHandler.startDocument();

        PDFStructElem divParent = new PDFStructElem(null, StandardStructureTypes.Grouping.DIV);
        PDFStructElem tableParent = PDFStructureTreeBuilder.createStructureElement("table", divParent,
                new AttributesImpl(), new PDFFactory(mock(PDFDocument.class)), null);

        StructureTreeElement structElem = documentHandler.getStructureTreeEventHandler()
                .startNode(nodeName, new AttributesImpl(), tableParent);

        return new Result(structElem, documentHandler.getPDFDocument());
    }
}
