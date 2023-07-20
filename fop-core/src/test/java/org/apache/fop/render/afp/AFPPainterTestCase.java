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

package org.apache.fop.render.afp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.impl.DefaultImageContext;
import org.apache.xmlgraphics.image.loader.impl.DefaultImageSessionContext;
import org.apache.xmlgraphics.image.loader.impl.ImageBuffered;
import org.apache.xmlgraphics.util.QName;

import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.afp.fonts.CharacterSet;
import org.apache.fop.afp.fonts.CharactersetEncoder;
import org.apache.fop.afp.fonts.OutlineFontTestCase;
import org.apache.fop.afp.fonts.RasterFont;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.events.Event;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventListener;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.ImageHandlerRegistry;
import org.apache.fop.render.afp.extensions.AFPElementMapping;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.util.ColorUtil;

public class AFPPainterTestCase {

    @Test
    public void testDrawBorderRect() throws Exception {
        // the goal of this test is to check that the drawing of rounded corners in AFP uses a bitmap of the
        // rounded corners (in fact the whole rectangle with rounded corners). the check is done by verifying
        // that the AFPImageHandlerRenderedImage.handleImage() method is called
        // mock
        AFPPaintingState afpPaintingState = mock(AFPPaintingState.class);
        when(afpPaintingState.getResolution()).thenReturn(72);
        // mock
        EventBroadcaster eventBroadcaster = mock(EventBroadcaster.class);
        // mock
        DefaultImageContext defaultImageContext = mock(DefaultImageContext.class);
        when(defaultImageContext.getSourceResolution()).thenReturn(72000f);
        // mock
        DefaultImageSessionContext defaultImageSessionContxt = mock(DefaultImageSessionContext.class);
        when(defaultImageSessionContxt.getParentContext()).thenReturn(defaultImageContext);
        when(defaultImageSessionContxt.getTargetResolution()).thenReturn(72000f);
        // mock
        ImageBuffered imageBuffered = mock(ImageBuffered.class);
        // mock
        ImageManager imageManager = mock(ImageManager.class);
        // mock
        AFPImageHandlerRenderedImage afpImageHandlerRenderedImage = mock(AFPImageHandlerRenderedImage.class);
        // mock
        ImageHandlerRegistry imageHandlerRegistry = mock(ImageHandlerRegistry.class);
        when(imageHandlerRegistry.getHandler(any(AFPRenderingContext.class), nullable(Image.class))).thenReturn(
                afpImageHandlerRenderedImage);
        // mock
        FOUserAgent foUserAgent = mock(FOUserAgent.class);
        when(foUserAgent.getEventBroadcaster()).thenReturn(eventBroadcaster);
        when(foUserAgent.getImageSessionContext()).thenReturn(defaultImageSessionContxt);
        when(foUserAgent.getImageManager()).thenReturn(imageManager);
        when(foUserAgent.getImageHandlerRegistry()).thenReturn(imageHandlerRegistry);
        // mock
        AFPEventProducer afpEventProducer = mock(AFPEventProducer.class);
        when(AFPEventProducer.Provider.get(eventBroadcaster)).thenReturn(afpEventProducer);
        // mock
        AFPResourceManager afpResourceManager = mock(AFPResourceManager.class);
        when(afpResourceManager.isObjectCached(null)).thenReturn(false);
        // mock
        IFContext ifContext = mock(IFContext.class);
        when(ifContext.getUserAgent()).thenReturn(foUserAgent);
        // mock
        AFPDocumentHandler afpDocumentHandler = mock(AFPDocumentHandler.class);
        when(afpDocumentHandler.getPaintingState()).thenReturn(afpPaintingState);
        when(afpDocumentHandler.getContext()).thenReturn(ifContext);
        when(afpDocumentHandler.getResourceManager()).thenReturn(afpResourceManager);
        when(afpDocumentHandler.cacheRoundedCorner("a2a48964ba2d")).thenReturn("RC000000");
        // real instance, no mock
        AFPPainter afpPainter = new AFPPainter(afpDocumentHandler);
        // build rectangle 200 x 50 (points, which are converted to millipoints)
        Rectangle rectangle = new Rectangle(0, 0, 200000, 50000);
        // build border properties
        int style = Constants.EN_SOLID;
        BorderProps.Mode mode = BorderProps.Mode.SEPARATE;
        Color color = Color.BLACK;
        int borderWidth = 4000;
        int radiusStart = 30000;
        int radiusEnd = 30000;
        BorderProps border1 = new BorderProps(style, borderWidth, radiusStart, radiusEnd, color, mode);
        BorderProps border2 = new BorderProps(style, borderWidth, radiusStart, radiusEnd, color, mode);
        BorderProps border3 = new BorderProps(style, borderWidth, radiusStart, radiusEnd, color, mode);
        BorderProps border4 = new BorderProps(style, borderWidth, radiusStart, radiusEnd, color, mode);
        when(imageManager.convertImage(any(Image.class), any(ImageFlavor[].class), any(Map.class)))
                .thenReturn(imageBuffered);
        afpPainter.drawBorderRect(rectangle, border1, border2, border3, border4, Color.WHITE);
        // note: here we would really like to verify that the second and third arguments passed to
        // handleImage() are the instances ib and rect declared above but that causes mockito to throw
        // an exception, probably because we cannot declare the AFPRenderingContext and are forced to
        // use any(), which forces the use of any() for all arguments
        verify(afpImageHandlerRenderedImage).handleImage(any(AFPRenderingContext.class),
                nullable(Image.class), any(Rectangle.class));
    }

    @Test
    public void testPresentationText() throws Exception {
        List<String> strings = new ArrayList<String>();
        strings.add("test");
        Assert.assertEquals(writeText(strings), "BEGIN DOCUMENT DOC00001\n"
                + "BEGIN PAGE PGN00001\n"
                + "BEGIN ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "DESCRIPTOR PAGE\n"
                + "MIGRATION PRESENTATION_TEXT\n"
                + "END ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "BEGIN PRESENTATION_TEXT PT000001\n"
                + "DATA PRESENTATION_TEXT\n"
                + "END PRESENTATION_TEXT PT000001\n"
                + "END PAGE PGN00001\n"
                + "END DOCUMENT DOC00001\n");

        for (int i = 0; i < 5000; i++) {
            strings.add("test");
        }
        Assert.assertEquals(writeText(strings), "BEGIN DOCUMENT DOC00001\n"
                + "BEGIN PAGE PGN00001\n"
                + "BEGIN ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "DESCRIPTOR PAGE\n"
                + "MIGRATION PRESENTATION_TEXT\n"
                + "END ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "BEGIN PRESENTATION_TEXT PT000001\n"
                + "DATA PRESENTATION_TEXT\n"
                + "END PRESENTATION_TEXT PT000001\n"
                + "BEGIN PRESENTATION_TEXT PT000002\n"
                + "DATA PRESENTATION_TEXT\n"
                + "END PRESENTATION_TEXT PT000002\n"
                + "END PAGE PGN00001\n"
                + "END DOCUMENT DOC00001\n");
    }

    @Test
    public void testPresentationText2() throws Exception {
        List<String> strings = new ArrayList<String>();
        for (int i = 0; i < 5000; i++) {
            strings.add("tes");
        }
        Assert.assertEquals(writeText(strings), "BEGIN DOCUMENT DOC00001\n"
                + "BEGIN PAGE PGN00001\n"
                + "BEGIN ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "DESCRIPTOR PAGE\n"
                + "MIGRATION PRESENTATION_TEXT\n"
                + "END ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "BEGIN PRESENTATION_TEXT PT000001\n"
                + "DATA PRESENTATION_TEXT\n"
                + "END PRESENTATION_TEXT PT000001\n"
                + "BEGIN PRESENTATION_TEXT PT000002\n"
                + "DATA PRESENTATION_TEXT\n"
                + "END PRESENTATION_TEXT PT000002\n"
                + "END PAGE PGN00001\n"
                + "END DOCUMENT DOC00001\n");
    }

    /**
     * Checks that letter spacing is reset to 0 after the relevant text block.
     */
    @Test
    public void testLetterSpacingReset() throws Exception {
        List<String> strings = new ArrayList<>();
        strings.add("xxxx");
        InputStream inputStream = getDocResultInputStream(strings, 10000);
        byte[] bytes = IOUtils.toByteArray(inputStream);
        // The 134th byte is incremented by 5 to account for the 5 extra bytes inserted for the reset.
        Assert.assertEquals((byte)39, bytes[134]);
        // And these are the 5 reset bytes.
        Assert.assertEquals((byte)5, bytes[163]);
        Assert.assertEquals((byte)195, bytes[164]);
        Assert.assertEquals((byte)0, bytes[165]);
        Assert.assertEquals((byte)0, bytes[166]);
        Assert.assertEquals((byte)0, bytes[167]);
    }

    private String writeText(List<String> text) throws Exception {
        InputStream bis = getDocResultInputStream(text);
        StringBuilder sb = new StringBuilder();
        new AFPParser(false).read(bis, sb);
        return sb.toString();
    }

    private static InputStream getDocResultInputStream(List<String> text) throws Exception {
        return getDocResultInputStream(text, 0);
    }

    private static InputStream getDocResultInputStream(List<String> text, int letterSpacing) throws Exception {
        FOUserAgent agent = FopFactory.newInstance(new URI(".")).newFOUserAgent();
        IFContext context = new IFContext(agent);
        AFPDocumentHandler doc = new AFPDocumentHandler(context);
        AFPPainter afpPainter = new AFPPainter(doc);
        FontInfo fi = new FontInfo();
        fi.addFontProperties("", Font.DEFAULT_FONT);
        RasterFont rf = new RasterFont("", true);
        CharacterSet cs = mock(CharacterSet.class);
        CharactersetEncoder.EncodedChars encoder = mock(CharactersetEncoder.EncodedChars.class);
        when(cs.encodeChars(any(CharSequence.class))).thenReturn(encoder);
        when(encoder.getLength()).thenReturn(text.get(0).length());
        rf.addCharacterSet(12000, cs);
        fi.addMetrics("", rf);
        doc.setFontInfo(fi);
        afpPainter.setFont("any", "normal", 400, "", 12000, Color.BLACK);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.setResult(new StreamResult(outputStream));
        doc.startDocument();
        doc.startPage(0, "", "", new Dimension());
        for (String s : text) {
            afpPainter.drawText(0, 0, letterSpacing, 0, null, s);
        }
        doc.endDocument();

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    @Test
    public void testDrawBorderRect3() throws IFException, PropertyException, IOException {
        FOUserAgent ua = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        AFPDocumentHandler documentHandler = new AFPDocumentHandler(new IFContext(ua));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        documentHandler.setResult(new StreamResult(os));
        documentHandler.startDocument();
        documentHandler.startPage(0, "", "", new Dimension());
        AFPPainter afpPainter = new AFPPainter(documentHandler);
        drawBorder(afpPainter, ua);
        documentHandler.endDocument();

        InputStream bis = new ByteArrayInputStream(os.toByteArray());
        StringBuilder sb = new StringBuilder();
        new AFPParser(false).read(bis, sb);
        Assert.assertEquals(sb.toString(), "BEGIN DOCUMENT DOC00001\n"
                + "BEGIN PAGE PGN00001\n"
                + "BEGIN ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "DESCRIPTOR PAGE\n"
                + "MIGRATION PRESENTATION_TEXT\n"
                + "END ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "BEGIN PRESENTATION_TEXT PT000001\n"
                + "DATA PRESENTATION_TEXT\n"
                + "END PRESENTATION_TEXT PT000001\n"
                + "BEGIN PRESENTATION_TEXT PT000002\n"
                + "DATA PRESENTATION_TEXT\n"
                + "END PRESENTATION_TEXT PT000002\n"
                + "END PAGE PGN00001\n"
                + "END DOCUMENT DOC00001\n");
    }

    @Test
    public void testDrawBorderRectAndText() throws IFException, PropertyException, IOException {
        FOUserAgent ua = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        AFPDocumentHandler documentHandler = new AFPDocumentHandler(new IFContext(ua));
        documentHandler.setResolution(480);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        documentHandler.setResult(new StreamResult(os));
        documentHandler.startDocument();
        documentHandler.startPage(0, "", "", new Dimension());
        AFPPainter afpPainter = new AFPPainter(documentHandler);
        setFont(documentHandler, afpPainter);
        drawBorder(afpPainter, ua);
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 4770; i++) {
            text.append("a");
        }
        afpPainter.drawText(0, 0, 0, 0, null, text.toString());
        documentHandler.endDocument();

        InputStream bis = new ByteArrayInputStream(os.toByteArray());
        StringBuilder sb = new StringBuilder();
        new AFPParser(false).read(bis, sb);
        Assert.assertEquals(sb.toString(), "BEGIN DOCUMENT DOC00001\n"
                + "BEGIN PAGE PGN00001\n"
                + "BEGIN ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "DESCRIPTOR PAGE\n"
                + "MIGRATION PRESENTATION_TEXT\n"
                + "END ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "BEGIN PRESENTATION_TEXT PT000001\n"
                + "DATA PRESENTATION_TEXT\n"
                + "END PRESENTATION_TEXT PT000001\n"
                + "BEGIN PRESENTATION_TEXT PT000002\n"
                + "DATA PRESENTATION_TEXT\n"
                + "END PRESENTATION_TEXT PT000002\n"
                + "BEGIN PRESENTATION_TEXT PT000003\n"
                + "DATA PRESENTATION_TEXT\n"
                + "END PRESENTATION_TEXT PT000003\n"
                + "END PAGE PGN00001\n"
                + "END DOCUMENT DOC00001\n");
    }

    private void setFont(AFPDocumentHandler doc, AFPPainter afpPainter) throws IFException {
        FontInfo fi = new FontInfo();
        fi.addFontProperties("", Font.DEFAULT_FONT);
        RasterFont rf = new RasterFont("", true);
        CharacterSet cs = OutlineFontTestCase.getCharacterSet();
        rf.addCharacterSet(12000, cs);
        fi.addMetrics("", rf);
        doc.setFontInfo(fi);
        afpPainter.setFont("any", "normal", 400, "", 12000, Color.BLACK);
    }

    private void drawBorder(AFPPainter afpPainter, FOUserAgent ua) throws IFException, PropertyException {
        int style = Constants.EN_DOTTED;
        BorderProps.Mode mode = BorderProps.Mode.COLLAPSE_OUTER;
        Color color = ColorUtil.parseColorString(ua, "fop-rgb-icc(0.5019608,0.5019608,0.5019608,#CMYK,,0,0,0,0.5)");
        int borderWidth = 500;
        int radiusStart = 0;
        int radiusEnd = 0;
        BorderProps border1 = new BorderProps(style, borderWidth, radiusStart, radiusEnd, color, mode);
        afpPainter.drawBorderRect(new Rectangle(0, 0, 552755, 16090), null, border1, null, null, Color.WHITE);
    }

    @Test
    public void testPageGroup() throws IFException, IOException {
        FOUserAgent ua = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        AFPDocumentHandler documentHandler = new AFPDocumentHandler(new IFContext(ua));
        Map<QName, String> attributes = new HashMap<>();
        attributes.put(AFPElementMapping.PAGE_GROUP, "false");
        documentHandler.getContext().setForeignAttributes(attributes);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        documentHandler.setResult(new StreamResult(os));
        documentHandler.startDocument();
        documentHandler.startPageSequence(null);
        documentHandler.startPage(0, "", "", new Dimension());
        AFPPainter afpPainter = new AFPPainter(documentHandler);
        setFont(documentHandler, afpPainter);
        afpPainter.drawText(0, 0, 0, 0, null, "a");
        documentHandler.endPage();
        documentHandler.endPageSequence();
        attributes.clear();
        documentHandler.startPageSequence(null);
        documentHandler.startPage(0, "", "", new Dimension());
        afpPainter.drawText(0, 0, 0, 0, null, "a");
        documentHandler.endDocument();

        InputStream bis = new ByteArrayInputStream(os.toByteArray());
        StringBuilder sb = new StringBuilder();
        new AFPParser(false).read(bis, sb);
        Assert.assertEquals(sb.toString(), "BEGIN DOCUMENT DOC00001\n"
                + "BEGIN PAGE PGN00001\n"
                + "BEGIN ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "MAP CODED_FONT Triplets: "
                + "FULLY_QUALIFIED_NAME,FULLY_QUALIFIED_NAME,CHARACTER_ROTATION,RESOURCE_LOCAL_IDENTIFIER,\n"
                + "DESCRIPTOR PAGE\n"
                + "MIGRATION PRESENTATION_TEXT\n"
                + "END ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "BEGIN PRESENTATION_TEXT PT000001\n"
                + "DATA PRESENTATION_TEXT\n"
                + "END PRESENTATION_TEXT PT000001\n"
                + "END PAGE PGN00001\n"
                + "BEGIN PAGE_GROUP PGP00001\n"
                + "BEGIN PAGE PGN00002\n"
                + "BEGIN ACTIVE_ENVIRONMENT_GROUP AEG00002\n"
                + "DESCRIPTOR PAGE\n"
                + "MIGRATION PRESENTATION_TEXT\n"
                + "END ACTIVE_ENVIRONMENT_GROUP AEG00002\n"
                + "BEGIN PRESENTATION_TEXT PT000002\n"
                + "DATA PRESENTATION_TEXT\n"
                + "END PRESENTATION_TEXT PT000002\n"
                + "END PAGE PGN00002\n"
                + "END PAGE_GROUP PGP00001\n"
                + "END DOCUMENT DOC00001\n");
    }

    @Test
    public void testEventOnImageParseException() throws Exception {
        FOUserAgent ua = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        AFPDocumentHandler dh = new AFPDocumentHandler(new IFContext(ua));
        dh.setResult(new StreamResult(new ByteArrayOutputStream()));
        dh.startDocument();
        dh.startPage(0, "", "", new Dimension());
        MyAFPPainter afpPainter = new MyAFPPainter(dh);
        ImageInfo info = new ImageInfo("test/resources/fop/image/logo.jpg", "image/jpeg") {
            public Image getOriginalImage() {
                throw new RuntimeException();
            }
        };
        final Event[] event = new Event[1];
        ua.getEventBroadcaster().addEventListener(new EventListener() {
            public void processEvent(Event e) {
                event[0] = e;
            }
        });
        afpPainter.drawImageUsingImageHandler(info, new Rectangle());
        Assert.assertEquals(event[0].getEventKey(), "imageWritingError");
    }

    static class MyAFPPainter extends AFPPainter {
        MyAFPPainter(AFPDocumentHandler documentHandler) {
            super(documentHandler);
        }

        protected void drawImageUsingImageHandler(ImageInfo info, Rectangle rect)
                throws ImageException, IOException {
            super.drawImageUsingImageHandler(info, rect);
        }
    }
}
