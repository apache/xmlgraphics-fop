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
import java.awt.geom.AffineTransform;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.spi.ImageImplRegistry;
import org.apache.xmlgraphics.java2d.color.ColorSpaces;
import org.apache.xmlgraphics.java2d.color.ColorWithAlternatives;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.events.Event;
import org.apache.fop.events.EventListener;
import org.apache.fop.fo.Constants;
import org.apache.fop.fonts.CMapSegment;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.base14.Helvetica;
import org.apache.fop.fonts.truetype.SVGGlyphData;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.pdf.PDFImage;
import org.apache.fop.pdf.PDFImageXObject;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFProfile;
import org.apache.fop.pdf.PDFResourceContext;
import org.apache.fop.pdf.PDFResources;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.PDFTextUtil;
import org.apache.fop.pdf.PDFUAMode;
import org.apache.fop.pdf.PDFXObject;
import org.apache.fop.pdf.StandardStructureTypes;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.ImageHandlerRegistry;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.util.CharUtilities;
import org.apache.fop.util.ImageObjectCache;

public class PDFPainterTestCase {

    private static final int PAGE_WIDTH = 100000;

    private static final int PAGE_HEIGHT = 100000;

    private static final int IMAGE_X = 10000;

    private static final int IMAGE_Y = 15000;

    private static final int IMAGE_WIDTH = 20000;

    private static final int IMAGE_HEIGHT = 20000;

    private FOUserAgent foUserAgent;
    private PDFContentGenerator pdfContentGenerator;
    private PDFDocumentHandler pdfDocumentHandler;

    private PDFLogicalStructureHandler handler;

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
        handler = mock(PDFLogicalStructureHandler.class);
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

    @Test
    public void testDrawCachedImage() throws IFException {
        createPDFPainter(false);
        String uri = "image.xxx";
        String pdfCacheKey = "PDF:" + uri;
        int x = 0;
        int y = 0;
        int w = 200;
        int h = 50;
        Rectangle rect = new Rectangle(x, y, w * 1000, h * 1000);
        ImageObjectCache objCache = createMockImageObjectCache();
        PDFDocument pdfDoc = createMockPDFDocument();
        PDFImage pdfImage = mock(PDFImage.class);
        PDFImageXObject pdfImageXObj = mock(PDFImageXObject.class);
        when(objCache.getImage(pdfCacheKey)).thenReturn(pdfImage);
        when(pdfDoc.getXObject(uri)).thenReturn(null);
        PDFResourceContext pdfContext = mock(PDFResourceContext.class);
        when(pdfContentGenerator.getResourceContext()).thenReturn(pdfContext);
        when(pdfDoc.addImage(pdfContext, pdfImage)).thenReturn(pdfImageXObj);
        PDFXObject xobj = pdfImageXObj;
        pdfPainter.drawImage(uri, rect);
        verify(pdfContentGenerator).placeImage(x * 1f, y * 1f, w * 1f, h * 1f, xobj);
    }

    private PDFDocument createMockPDFDocument() {
        PDFDocument pdfDoc = mock(PDFDocument.class);
        when(pdfContentGenerator.getDocument()).thenReturn(pdfDoc);
        when(pdfDocumentHandler.getPDFDocument()).thenReturn(pdfDoc);
        when(pdfDoc.getProfile()).thenReturn(new PDFProfile(pdfDoc));
        return pdfDoc;
    }

    private ImageObjectCache createMockImageObjectCache() {
        ImageObjectCache objCache = mock(ImageObjectCache.class);
        when(foUserAgent.getImageObjectCache()).thenReturn(objCache);
        return objCache;
    }

    @Test
    public void testCacheImageObjects() throws IFException, ImageException, IOException {
        createPDFPainter(true);
        String uri = "image.xxx";
        String pdfCacheKey = "PDF:" + uri;
        Rectangle rect = new Rectangle(0, 0, 1, 1);
        ImageObjectCache objCache = createMockImageObjectCache();
        PDFDocument pdfDoc = createMockPDFDocument();
        PDFImageXObject pdfImageXObj = mock(PDFImageXObject.class);
        PDFImage pdfImage = mock(PDFImage.class);
        when(pdfDoc.getXObject(uri)).thenReturn(null).thenReturn(pdfImageXObj);
        when(pdfImageXObj.getPDFImage()).thenReturn(pdfImage);
        ImageManager imageManager = mock(ImageManager.class);
        when(foUserAgent.getImageManager()).thenReturn(imageManager);
        ImageSessionContext imageSessionContext = mock(ImageSessionContext.class);
        when(foUserAgent.getImageSessionContext()).thenReturn(imageSessionContext);
        ImageContext imageContext = mock(ImageContext.class);
        when(imageSessionContext.getParentContext()).thenReturn(imageContext);
        when(imageContext.getSourceResolution()).thenReturn(72f);
        when(imageSessionContext.getTargetResolution()).thenReturn(240f);
        ImageInfo imageInfo = mock(ImageInfo.class);
        when(imageManager.getImageInfo(uri, imageSessionContext)).thenReturn(imageInfo);
        when(imageInfo.getMimeType()).thenReturn("application/pdf");
        ImageHandlerRegistry imageHandlerRegistry = mock(ImageHandlerRegistry.class);
        when(foUserAgent.getImageHandlerRegistry()).thenReturn(imageHandlerRegistry);
        ImageFlavor imageFlavor = mock(ImageFlavor.class);
        ImageFlavor[] flavors = {imageFlavor};
        when(imageHandlerRegistry.getSupportedFlavors(any(RenderingContext.class), nullable(Image.class)))
                .thenReturn(flavors);
        Map<String, Boolean> customObjects = new HashMap<String, Boolean>();
        when(imageInfo.getCustomObjects()).thenReturn(customObjects);
        Image image = mock(Image.class);
        when(imageManager.getImage(eq(imageInfo), eq(flavors), any(HashMap.class), eq(imageSessionContext)))
                .thenReturn(image);
        ImageImplRegistry imageRegistry = mock(ImageImplRegistry.class);
        when(imageManager.getRegistry()).thenReturn(imageRegistry);
        ImageHandler imageHandler = mock(ImageHandler.class);
        when(imageHandlerRegistry.getHandler(any(RenderingContext.class), eq(image)))
                .thenReturn(imageHandler);
        pdfPainter.drawImage(uri, rect);
        verify(handler, never()).addImageContentItem(elem);
        verify(objCache).putImage(pdfCacheKey, pdfImage);
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

    static class MyPDFPainter extends PDFPainter {
        RenderingContext renderingContext;
        MyPDFPainter(PDFDocumentHandler documentHandler, PDFLogicalStructureHandler logicalStructureHandler) {
            super(documentHandler, logicalStructureHandler);
        }

        protected RenderingContext createRenderingContext() {
            renderingContext = super.createRenderingContext();
            return renderingContext;
        }

        protected void drawImageUsingImageHandler(ImageInfo info, Rectangle rect) throws ImageException, IOException {
            super.drawImageUsingImageHandler(info, rect);
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
        verify(pdfContentGenerator).add("2 Tr 0.31543 w\n");
        verify(pdfContentGenerator).add("0 Tr\n");
    }

    @Test
    public void testSoftHyphenNotReplaced() throws IFException {
        pdfDocumentHandler = makePDFDocumentHandler(new StringBuilder());

        final StringBuilder sb = new StringBuilder();
        PDFTextUtil pdfTextUtil = new PDFTextUtil() {
            protected void write(String code) {
                //no need for current test
            }

            protected void write(StringBuffer code) {
                //no need for current test
            }

            @Override
            public void writeTJMappedCodePoint(int ch) {
                sb.append((char) ch);
            }
        };
        pdfTextUtil.beginTextObject();
        when(pdfContentGenerator.getTextUtil()).thenReturn(pdfTextUtil);

        FontInfo fi = new FontInfo();
        fi.addFontProperties("f1", new FontTriplet("a", "italic", 700));
        fi.addMetrics("f1", new Helvetica(false));
        pdfDocumentHandler.setFontInfo(fi);

        MyPDFPainter pdfPainter = new MyPDFPainter(pdfDocumentHandler, null);
        pdfPainter.setFont("a", "italic", 700, null, 12, null);
        pdfPainter.drawText(0, 0, 0, 0, null, String.valueOf(CharUtilities.SOFT_HYPHEN));

        assertEquals("Soft hyphen must not be replaced by a normal hyphen",
                String.valueOf(CharUtilities.SOFT_HYPHEN), sb.toString());
    }

    /**
     * Tests that letter spacing is set and reset appropriately.
     * @throws IFException
     */
    @Test
    public void testLetterSpacing() throws IFException {
        final StringBuilder sb = new StringBuilder();
        pdfDocumentHandler = makePDFDocumentHandler(sb);

        FontInfo fi = new FontInfo();
        fi.addFontProperties("f1", new FontTriplet("a", "normal", 400));
        MultiByteFont font = new MultiByteFont(null, null);
        fi.addMetrics("f1", font);
        pdfDocumentHandler.setFontInfo(fi);
        MyPDFPainter pdfPainter = new MyPDFPainter(pdfDocumentHandler, null);
        pdfPainter.setFont("a", "normal", 400, null, 12, null);
        pdfPainter.drawText(0, 0, 4321, 0, null, "test");

        assertEquals("BT\n"
                + "/f1 0.012 Tf\n"
                + "1 0 0 -1 0 0 Tm [<0000000000000000>] TJ\n",
                sb.toString());
        verify(pdfContentGenerator).updateCharacterSpacing(4.321f);
        verify(pdfContentGenerator).resetCharacterSpacing();
    }

    @Test
    public void testSimulateStyleColor() throws Exception {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        foUserAgent = fopFactory.newFOUserAgent();
        PDFDocumentHandler pdfDocumentHandler = new PDFDocumentHandler(new IFContext(foUserAgent));

        pdfDocumentHandler.setResult(new StreamResult(new ByteArrayOutputStream()));
        pdfDocumentHandler.startDocument();
        pdfDocumentHandler.startPage(0, "", "", new Dimension());

        FontInfo fi = new FontInfo();
        fi.addFontProperties("f1", new FontTriplet("a", "italic", 700));
        MultiByteFont font = new MultiByteFont(null, null);
        font.setSimulateStyle(true);
        fi.addMetrics("f1", font);
        pdfDocumentHandler.setFontInfo(fi);
        PDFPainter pdfPainter = new PDFPainter(pdfDocumentHandler, null);
        pdfPainter.setFont("a", "italic", 700, null, 12, Color.red);
        pdfPainter.drawText(0, 0, 0, 0, null, "test");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PDFFilterList filters = pdfPainter.generator.getStream().getFilterList();
        filters.setDisableAllFilters(true);
        pdfPainter.generator.getStream().output(bos);
        Assert.assertEquals(bos.toString(), "<< /Length 1 0 R >>\n"
                + "stream\n"
                + "q\n"
                + "1 0 0 -1 0 0 cm\n"
                + "1 0 0 rg\n"
                + "BT\n"
                + "/f1 0.012 Tf\n"
                + "1 0 0 RG\n"
                + "2 Tr 0.31543 w\n"
                + "1 0 0.3333 -1 0 0 Tm [<0000000000000000>] TJ\n"
                + "0 Tr\n"
                + "\n"
                + "endstream");
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

    @Test
    public void testDrawDpTextWithMultiByteFont() throws IFException {
        StringBuilder output = new StringBuilder();
        PDFDocumentHandler pdfDocumentHandler = makePDFDocumentHandler(output);
        String text = "Hi\uD83D\uDCA9";
        MultiByteFont font = new MultiByteFont(null, null);
        font.setWidthArray(new int[10]);
        font.setCMap(new CMapSegment[]{new CMapSegment(128169, 128169, 1)});
        FontInfo fi = new FontInfo();
        fi.addFontProperties("f1", new FontTriplet("a", "normal", 400));
        fi.addMetrics("f1", font);
        pdfDocumentHandler.setFontInfo(fi);
        MyPDFPainter pdfPainter = new MyPDFPainter(pdfDocumentHandler, null);
        pdfPainter.setFont("a", "normal", 400, null, 12, null);
        int[][] dp = new int[1][0];
        dp[0] = new int[]{0, 1, 2, 3};
        pdfPainter.drawText(0, 0, 0, 0, dp, text);
        assertEquals("BT\n"
                + "1 0 0 -1 0 0 Tm /f1 0.012 Tf\n"
                + "0 0.001 Td\n"
                + "<0000> Tj\n"
                + "0.002 0.002 Td\n"
                + "<0000> Tj\n"
                + "0 0 Td\n"
                + "<0001> Tj\n", output.toString());
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
        for (int i = 0; i < 2; i++) {
            foUserAgent = fopFactory.newFOUserAgent();
            foUserAgent.setAccessibility(true);
            pdfDocumentHandler = new PDFDocumentHandler(new IFContext(foUserAgent));
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
            pdfPainter.getContext().setLanguage(Locale.US);
            Assert.assertTrue(drawImage(doc, pdfPainter, new Rectangle()).contains("/BBox [0 0 0 0]"));
            Assert.assertTrue(drawImage(doc, pdfPainter, new Rectangle()).contains("/BBox [0 0 0 0]"));
        }
    }

    @Test
    public void testPDFUAImageWithoutPageRegionMargins() throws IOException, IFException {
        defaultTestPDFUAImageWithImagePosition(Collections.singletonList(new AffineTransform()));
    }

    @Test
    public void testPDFUAImageWithPageWithRegionMargins() throws IOException, IFException {
        defaultTestPDFUAImageWithImagePosition(
                Collections.singletonList(new AffineTransform(0d, 0d, 0d, 0d, 5000d, 5000d)));
    }

    @Test
    public void testPDFUAImageNullPageRegionMargins() throws IOException, IFException {
        defaultTestPDFUAImageWithImagePosition(new ArrayList<>());
    }

    @Test
    public void testPDFUAImageSecondTransformConcatenated() throws IOException, IFException {
        defaultTestPDFUAImageWithImagePosition(Arrays.asList(new AffineTransform(0d, 0d, 0d, 0d, 5000d, 5000d),
                new AffineTransform(1d, 0d, 0d, 1d, 10d, 10d)));
    }

    private void defaultTestPDFUAImageWithImagePosition(List<AffineTransform> transforms)
            throws IOException, IFException {
        String output = defaultTestPDFUAImage(new Dimension(PAGE_WIDTH, PAGE_HEIGHT),
                new Rectangle(IMAGE_X, IMAGE_Y, IMAGE_WIDTH, IMAGE_HEIGHT), transforms);

        int rectX = 0;
        int rectY = 0;
        if (!transforms.isEmpty()) {
            AffineTransform mainTransform = transforms.get(0);
            for (int i = 1; i < transforms.size(); i++) {
                mainTransform.concatenate(transforms.get(i));
            }

            rectX = (int) mainTransform.getTranslateX();
            rectY = (int) mainTransform.getTranslateY();
        }

        output = output.substring(output.indexOf("[") + 1);
        output = output.substring(0, output.indexOf("]"));

        String[] array = output.split(" ");

        // on the X axis, 0 is on the left
        // on the Y axis, 0 is at the bottom
        int position = (IMAGE_X + rectX) / 1000;
        assertEquals("Left box boundary must be the X image position, adjusted to the region margins",
                position, Integer.parseInt(array[0]));

        position = (PAGE_HEIGHT - IMAGE_Y - rectY - IMAGE_HEIGHT) / 1000;
        assertEquals("Top box boundary must be the page height minus the image height and the "
                        + "Y image position, adjusted to the region margins",
                position, Integer.parseInt(array[1]));

        position = (IMAGE_X + rectX + IMAGE_WIDTH) / 1000;
        assertEquals("Right box boundary must be the left box boundary plus the image width,"
                        + " adjusted to the region margins",
                position, Integer.parseInt(array[2]));

        position = (PAGE_HEIGHT - IMAGE_Y - rectY) / 1000;
        assertEquals("Bottom box boundary must be the image height minus the Y image position, "
                        + "adjusted to the region margins",
                position, Integer.parseInt(array[3]));
    }

    private String defaultTestPDFUAImage(Dimension dimension, Rectangle rectangle, List<AffineTransform> transforms)
            throws IFException, IOException {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        foUserAgent = fopFactory.newFOUserAgent();
        foUserAgent.setAccessibility(true);

        IFContext ifContext = new IFContext(foUserAgent);
        pdfDocumentHandler = new PDFDocumentHandler(ifContext);
        pdfDocumentHandler.getStructureTreeEventHandler();
        pdfDocumentHandler.setResult(new StreamResult(new ByteArrayOutputStream()));
        pdfDocumentHandler.startDocument();
        pdfDocumentHandler.startPage(0, "", "", dimension);

        PDFDocument doc = pdfDocumentHandler.getPDFDocument();
        doc.getProfile().setPDFUAMode(PDFUAMode.PDFUA_1);
        doc.getInfo().setTitle("a");

        PDFLogicalStructureHandler structureHandler = new PDFLogicalStructureHandler(doc);
        structureHandler.startPage(new PDFPage(new PDFResources(doc), 0,
                new Rectangle(), new Rectangle(), new Rectangle(), new Rectangle()));

        PDFPainter pdfPainter = new PDFPainter(pdfDocumentHandler, structureHandler);
        for (AffineTransform affineTransform : transforms) {
            pdfPainter.startViewport(affineTransform, dimension, null);
        }

        ifContext.setLanguage(Locale.US);

        drawImage(doc, pdfPainter, rectangle);
        return drawImage(doc, pdfPainter, rectangle);
    }

    private String drawImage(PDFDocument doc, PDFPainter pdfPainter, Rectangle rectangle)
        throws IOException, IFException {
        PDFStructElem structElem = new PDFStructElem(doc.getRoot(), StandardStructureTypes.InlineLevelStructure.NOTE);
        structElem.setDocument(doc);
        pdfPainter.getContext().setStructureTreeElement(structElem);
        pdfPainter.drawImage("test/resources/images/cmyk.jpg", rectangle);
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

    @Test
    public void testSVGFont() throws IFException, IOException {
        String out = drawSVGFont("<svg xmlns=\"http://www.w3.org/2000/svg\">\n"
                + "<circle cx=\"50\" cy=\"50\" r=\"40\" stroke=\"black\" stroke-width=\"3\" fill=\"red\" />\n"
                + "</svg>");
        assertTrue(out.contains("0.00012 0 0 0.00012 0 0 cm"));
        assertTrue(out.contains("1 0 0 rg"));
    }

    @Test
    public void testSVGFontFallback() throws IFException, IOException {
        String out = drawSVGFont(null);
        Assert.assertEquals(out, "<< /Length 1 0 R >>\n"
                + "stream\n"
                + "q\n"
                + "1 0 0 -1 0 0 cm\n"
                + "BT\n"
                + "/f1 0.012 Tf\n"
                + "1 0 0 -1 0 0 Tm [<0000000000000000>] TJ\n"
                + "\n"
                + "endstream");
    }

    @Test
    public void testSVGFontScale() throws IFException, IOException {
        String out = drawSVGFont("<svg xmlns=\"http://www.w3.org/2000/svg\">\n"
                + "<g transform=\"translate(0 0) translate(0 0) scale(50)\"/>"
                + "<circle cx=\"50\" cy=\"50\" r=\"40\" stroke=\"black\" stroke-width=\"3\" fill=\"red\" />\n"
                + "</svg>");
        assertTrue(out.contains("0.00012 0 0 0.00012 0 0 cm"));
        assertTrue(out.contains("1 0 0 rg"));
    }

    private String drawSVGFont(String svg) throws IFException, IOException {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        foUserAgent = fopFactory.newFOUserAgent();
        PDFDocumentHandler pdfDocumentHandler = new PDFDocumentHandler(new IFContext(foUserAgent));
        pdfDocumentHandler.setResult(new StreamResult(new ByteArrayOutputStream()));
        pdfDocumentHandler.startDocument();
        pdfDocumentHandler.startPage(0, "", "", new Dimension());
        FontInfo fi = new FontInfo();
        fi.addFontProperties("f1", new FontTriplet("a", "normal", 400));
        MultiByteFont font = new MultiByteFont(null, null);
        font.setWidthArray(new int[1]);
        Map<Integer, SVGGlyphData> svgs = new HashMap<>();
        if (svg != null) {
            SVGGlyphData svgGlyph = new SVGGlyphData();
            svgGlyph.setSVG(svg);
            svgs.put(0, svgGlyph);
        }
        font.setSVG(svgs);
        font.setBBoxArray(new Rectangle[] {new Rectangle()});
        fi.addMetrics("f1", font);
        pdfDocumentHandler.setFontInfo(fi);
        PDFPainter pdfPainter = new PDFPainter(pdfDocumentHandler, null);
        pdfPainter.setFont("a", "normal", 400, null, 12, null);
        pdfPainter.drawText(0, 0, 0, 0, null, "test");
        PDFFilterList filters = pdfPainter.generator.getStream().getFilterList();
        filters.setDisableAllFilters(true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        pdfPainter.generator.getStream().output(bos);
        return bos.toString();
    }

    @Test
    public void testEventOnImageParseException() throws Exception {
        FOUserAgent ua = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        PDFDocumentHandler dh = new PDFDocumentHandler(new IFContext(ua));
        dh.setResult(new StreamResult(new ByteArrayOutputStream()));
        dh.startDocument();
        dh.startPage(0, "", "", new Dimension());
        MyPDFPainter pdfPainter = new MyPDFPainter(dh, null) {
            protected void drawImage(Image image, Rectangle rect, RenderingContext context) {
                throw new RuntimeException();
            }
        };
        ImageInfo info = new ImageInfo("test/resources/fop/image/logo.jpg", "image/jpeg");
        final Event[] event = new Event[1];
        ua.getEventBroadcaster().addEventListener(new EventListener() {
            public void processEvent(Event e) {
                event[0] = e;
            }
        });
        pdfPainter.drawImageUsingImageHandler(info, new Rectangle());
        Assert.assertEquals(event[0].getEventKey(), "imageWritingError");
    }

    @Test
    public void testAlphaColor() throws Exception {
        fillColor(new Color(0, 0, 0, 102), "0 g\n");
        Color deviceColor = new Color(ColorSpaces.getDeviceCMYKColorSpace(), new float[4], 0.4f);
        fillColor(new ColorWithAlternatives(0, 0, 0, 102, new Color[]{deviceColor}), "0 0 0 0 k\n");
    }

    private void fillColor(Color color, String cmd) throws Exception {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        foUserAgent = fopFactory.newFOUserAgent();
        PDFDocumentHandler pdfDocumentHandler = new PDFDocumentHandler(new IFContext(foUserAgent));
        pdfDocumentHandler.setResult(new StreamResult(new ByteArrayOutputStream()));
        pdfDocumentHandler.startDocument();
        pdfDocumentHandler.startPage(0, "", "", new Dimension());
        PDFPainter pdfPainter = new PDFPainter(pdfDocumentHandler, null);
        pdfPainter.fillRect(new Rectangle(10, 10), color);
        PDFFilterList filters = pdfPainter.generator.getStream().getFilterList();
        filters.setDisableAllFilters(true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        pdfPainter.generator.getStream().output(bos);
        Assert.assertEquals("<< /Length 1 0 R >>\n"
                + "stream\n"
                + "q\n"
                + "1 0 0 -1 0 0 cm\n"
                + "/GS1 gs\n"
                + cmd
                + "0 0 0.01 0.01 re f\n"
                + "\n"
                + "endstream", bos.toString());
        bos.reset();
        pdfDocumentHandler.getCurrentPage().getGStates().iterator().next().output(bos);
        Assert.assertEquals(bos.toString(), "<<\n"
                + "/Type /ExtGState\n"
                + "/ca 0.4\n"
                + "/CA 1.0\n"
                + ">>");
    }
}
