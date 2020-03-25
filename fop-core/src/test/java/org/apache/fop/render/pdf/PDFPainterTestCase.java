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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fo.Constants;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFProfile;
import org.apache.fop.pdf.PDFResources;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.PDFTextUtil;
import org.apache.fop.pdf.PDFUAMode;
import org.apache.fop.pdf.StandardStructureTypes;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.traits.BorderProps;

public class PDFPainterTestCase {

    private FOUserAgent foUserAgent;
    private PDFContentGenerator pdfContentGenerator;
    private PDFDocumentHandler pdfDocumentHandler;
    private PDFPainter pdfPainter;
    private PDFStructElem elem = new PDFStructElem();

    @Test
    public void testDrawBorderRect() throws Exception {
        // the goal of this test is to check that the drawing of rounded corners in PDF calls
        // PDFGraphicsPaiter.cubicBezierTo(); the check is done by verifying that a " c " command is written
        // to the PDFContentGenerator
        createPDFPainter(false);
        // build rectangle 200 x 50 (points, which are converted to milipoints)
        Rectangle rectangle = new Rectangle(0, 0, 200000, 50000);
        // build border properties: width 4pt, radius 30pt
        BorderProps border = new BorderProps(Constants.EN_SOLID, 4000, 30000, 30000, Color.BLACK,
                BorderProps.Mode.SEPARATE);
        pdfPainter.drawBorderRect(rectangle, border, border, border, border, Color.WHITE);
        // since we cannot mock the PDFContentGenerator.format() static method we have to restrict the
        // verification to commands that end with " c ".
        verify(pdfContentGenerator, times(16)).add(endsWith(" c "));
    }

    private void createPDFPainter(boolean accessibility) {
        mockFOUserAgent(accessibility);
        mockPDFContentGenerator();
        mockPDFDocumentHandler();
        PDFLogicalStructureHandler handler = mock(PDFLogicalStructureHandler.class);
        pdfPainter = new PDFPainter(pdfDocumentHandler, handler);
    }

    private void mockFOUserAgent(boolean accessibility) {
        foUserAgent = mock(FOUserAgent.class);
        when(foUserAgent.isAccessibilityEnabled()).thenReturn(accessibility);
    }

    private void mockPDFContentGenerator() {
        pdfContentGenerator = mock(PDFContentGenerator.class);
    }

    private void mockPDFDocumentHandler() {
        pdfDocumentHandler = mock(PDFDocumentHandler.class);
        when(pdfDocumentHandler.getGenerator()).thenReturn(pdfContentGenerator);
        IFContext ifContext = mock(IFContext.class);
        when(ifContext.getUserAgent()).thenReturn(foUserAgent);
        when(pdfDocumentHandler.getContext()).thenReturn(ifContext);
        when(ifContext.getStructureTreeElement()).thenReturn(elem);
    }

    private PDFDocument createMockPDFDocument() {
        PDFDocument pdfDoc = mock(PDFDocument.class);
        when(pdfContentGenerator.getDocument()).thenReturn(pdfDoc);
        when(pdfDocumentHandler.getPDFDocument()).thenReturn(pdfDoc);
        when(pdfDoc.getProfile()).thenReturn(new PDFProfile(pdfDoc));
        return pdfDoc;
    }

    @Test
    public void testPageNumber() throws IFException {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        foUserAgent = fopFactory.newFOUserAgent();
        pdfDocumentHandler = new PDFDocumentHandler(new IFContext(foUserAgent));
        pdfDocumentHandler.setResult(new StreamResult(new ByteArrayOutputStream()));
        pdfDocumentHandler.startDocument();
        pdfDocumentHandler.startPage(0, "", "", new Dimension());
        pdfDocumentHandler.getContext().setPageNumber(3);
        MyPDFPainter pdfPainter = new MyPDFPainter(pdfDocumentHandler, null);
        pdfPainter.drawImage("test/resources/images/cmyk.jpg", new Rectangle());
        assertEquals(pdfPainter.renderingContext.getHints().get("page-number"), 3);
    }

    class MyPDFPainter extends PDFPainter {
        protected RenderingContext renderingContext;
        public MyPDFPainter(PDFDocumentHandler documentHandler, PDFLogicalStructureHandler logicalStructureHandler) {
            super(documentHandler, logicalStructureHandler);
        }

        protected RenderingContext createRenderingContext() {
            renderingContext = super.createRenderingContext();
            return renderingContext;
        }
    }

    @Test
    public void testSimulateStyle() throws IFException {
        final StringBuilder sb = new StringBuilder();
        pdfDocumentHandler = makePDFDocumentHandler(sb);

        FontInfo fi = new FontInfo();
        fi.addFontProperties("f1", new FontTriplet("a", "italic", 700));
        MultiByteFont font = new MultiByteFont(null, null);
        font.setSimulateStyle(true);
        fi.addMetrics("f1", font);
        pdfDocumentHandler.setFontInfo(fi);
        MyPDFPainter pdfPainter = new MyPDFPainter(pdfDocumentHandler, null);
        pdfPainter.setFont("a", "italic", 700, null, 12, null);
        pdfPainter.drawText(0, 0, 0, 0, null, "test");

        assertEquals(sb.toString(), "BT\n/f1 0.012 Tf\n1 0 0.3333 -1 0 0 Tm [<0000000000000000>] TJ\n");
        verify(pdfContentGenerator).add("q\n");
        verify(pdfContentGenerator).add("2 Tr 0.31543 w\n");
        verify(pdfContentGenerator).add("Q\n");
    }

    @Test
    public void testDrawTextWithMultiByteFont() throws IFException {
        StringBuilder output = new StringBuilder();
        PDFDocumentHandler pdfDocumentHandler = makePDFDocumentHandler(output);
        //0x48 0x65 0x6C 0x6C 0x6F 0x20 0x4D 0x6F 0x63 0x6B 0x21 0x1F4A9
        String text = "Hello Mock!\uD83D\uDCA9";
        String expectedHex = "00480065006C006C006F0020004D006F0063006B002101F4A9";

        MultiByteFont font = spy(new MultiByteFont(null, null));
        when(font.mapCodePoint(anyInt())).thenAnswer(new FontMapCodepointAnswer());

        FontInfo fi = new FontInfo();
        fi.addFontProperties("f1", new FontTriplet("a", "normal", 400));
        fi.addMetrics("f1", font);
        pdfDocumentHandler.setFontInfo(fi);

        MyPDFPainter pdfPainter = new MyPDFPainter(pdfDocumentHandler, null);
        pdfPainter.setFont("a", "normal", 400, null, 12, null);
        pdfPainter.drawText(0, 0, 0, 0, null, text);

        assertEquals("BT\n/f1 0.012 Tf\n1 0 0 -1 0 0 Tm [<" + expectedHex + ">] TJ\n", output.toString());
    }

    private PDFDocumentHandler makePDFDocumentHandler(final StringBuilder sb) throws IFException {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        foUserAgent = fopFactory.newFOUserAgent();
        mockPDFContentGenerator();
        PDFTextUtil pdfTextUtil = new PDFTextUtil() {
            protected void write(String code) {
                sb.append(code);
            }
            protected void write(StringBuffer code) {
                sb.append(code);
            }
        };
        pdfTextUtil.beginTextObject();

        when(pdfContentGenerator.getTextUtil()).thenReturn(pdfTextUtil);
        PDFDocumentHandler pdfDocumentHandler = new PDFDocumentHandler(new IFContext(foUserAgent)) {
            PDFContentGenerator getGenerator() {
                return pdfContentGenerator;
            }
        };

        pdfDocumentHandler.setResult(new StreamResult(new ByteArrayOutputStream()));
        pdfDocumentHandler.startDocument();
        pdfDocumentHandler.startPage(0, "", "", new Dimension());
        return pdfDocumentHandler;
    }

    private static class FontMapCodepointAnswer implements Answer<Integer> {

        @Override
        public Integer answer(InvocationOnMock invocation) throws Throwable {
            return (Integer) invocation.getArguments()[0];
        }
    }

    @Test
    public void testPDFUAImage() throws IFException, IOException {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        foUserAgent = fopFactory.newFOUserAgent();
        foUserAgent.setAccessibility(true);
        IFContext ifContext = new IFContext(foUserAgent);
        pdfDocumentHandler = new PDFDocumentHandler(ifContext);
        pdfDocumentHandler.getStructureTreeEventHandler();
        pdfDocumentHandler.setResult(new StreamResult(new ByteArrayOutputStream()));
        pdfDocumentHandler.startDocument();
        pdfDocumentHandler.startPage(0, "", "", new Dimension());
        PDFDocument doc = pdfDocumentHandler.getPDFDocument();
        doc.getProfile().setPDFUAMode(PDFUAMode.PDFUA_1);
        doc.getInfo().setTitle("a");
        PDFLogicalStructureHandler structureHandler = new PDFLogicalStructureHandler(doc);
        structureHandler.startPage(new PDFPage(new PDFResources(doc), 0,
                new Rectangle(), new Rectangle(), new Rectangle(), new Rectangle()));
        PDFPainter pdfPainter = new PDFPainter(pdfDocumentHandler, structureHandler);
        ifContext.setLanguage(Locale.US);
        drawImage(doc, pdfPainter, ifContext);
        String output = drawImage(doc, pdfPainter, ifContext);
        Assert.assertTrue(output, output.contains("/BBox [0 0 0 0]"));
    }

    private String drawImage(PDFDocument doc, PDFPainter pdfPainter, IFContext ifContext)
        throws IOException, IFException {
        PDFStructElem structElem = new PDFStructElem(doc.getRoot(), StandardStructureTypes.InlineLevelStructure.NOTE);
        structElem.setDocument(doc);
        ifContext.setStructureTreeElement(structElem);
        pdfPainter.drawImage("test/resources/images/cmyk.jpg", new Rectangle());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        structElem.output(bos);
        return bos.toString();
    }

    @Test
    public void testFooterText() throws IFException, IOException {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        foUserAgent = fopFactory.newFOUserAgent();
        foUserAgent.setAccessibility(true);
        PDFDocumentHandler pdfDocumentHandler = new PDFDocumentHandler(new IFContext(foUserAgent));
        pdfDocumentHandler.getStructureTreeEventHandler();

        pdfDocumentHandler.setResult(new StreamResult(new ByteArrayOutputStream()));
        pdfDocumentHandler.startDocument();
        pdfDocumentHandler.startPage(0, "", "", new Dimension());

        FontInfo fi = new FontInfo();
        fi.addFontProperties("f1", new FontTriplet("a", "italic", 700));
        MultiByteFont font = new MultiByteFont(null, null);
        font.setWidthArray(new int[1]);
        fi.addMetrics("f1", font);
        pdfDocumentHandler.setFontInfo(fi);
        PDFDocument doc = pdfDocumentHandler.getPDFDocument();
        PDFLogicalStructureHandler structureHandler = new PDFLogicalStructureHandler(doc);
        MyPDFPainter pdfPainter = new MyPDFPainter(pdfDocumentHandler, structureHandler);
        pdfPainter.getContext().setRegionType(Constants.FO_REGION_AFTER);
        pdfPainter.setFont("a", "italic", 700, null, 12, null);
        pdfPainter.drawText(0, 0, 0, 0, null, "test");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PDFFilterList filters = pdfPainter.generator.getStream().getFilterList();
        filters.setDisableAllFilters(true);
        pdfPainter.generator.getStream().output(bos);
        Assert.assertEquals(bos.toString(), "<< /Length 1 0 R >>\n"
                + "stream\n"
                + "q\n"
                + "1 0 0 -1 0 0 cm\n"
                + "/Artifact\n"
                + "<</Type /Pagination\n"
                + "/Subtype /Footer>>\n"
                + "BDC\n"
                + "BT\n"
                + "/f1 0.012 Tf\n"
                + "1 0 0 -1 0 0 Tm [<0000000000000000>] TJ\n"
                + "\n"
                + "endstream");
    }
}
