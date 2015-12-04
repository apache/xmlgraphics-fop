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

import javax.xml.transform.stream.StreamResult;

import org.junit.Test;

import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fo.Constants;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFProfile;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.traits.BorderProps;

import junit.framework.Assert;

public class PDFPainterTestCase {

    private FOUserAgent foUserAgent;
    private  PDFContentGenerator pdfContentGenerator;
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

    private void createPDFPainter(boolean value) {
        mockFOUserAgent(value);
        mockPDFContentGenerator();
        mockPDFDocumentHandler();
        PDFLogicalStructureHandler handler = mock(PDFLogicalStructureHandler.class);
        pdfPainter = new PDFPainter(pdfDocumentHandler, handler);
    }

    private void mockFOUserAgent(boolean value) {
        foUserAgent = mock(FOUserAgent.class);
        when(foUserAgent.isAccessibilityEnabled()).thenReturn(value);
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
        Assert.assertEquals(pdfPainter.renderingContext.getHints().get("page-number"), 3);
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
}
