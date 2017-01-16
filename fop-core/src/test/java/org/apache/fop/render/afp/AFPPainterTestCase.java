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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.impl.DefaultImageContext;
import org.apache.xmlgraphics.image.loader.impl.DefaultImageSessionContext;
import org.apache.xmlgraphics.image.loader.impl.ImageBuffered;

import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.afp.fonts.CharacterSet;
import org.apache.fop.afp.fonts.CharactersetEncoder;
import org.apache.fop.afp.fonts.RasterFont;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.ImageHandlerRegistry;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.util.ColorUtil;

public class AFPPainterTestCase {

    @Test
    public void testDrawBorderRect() {
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
        when(imageHandlerRegistry.getHandler(any(AFPRenderingContext.class), any(Image.class))).thenReturn(
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
        try {
            when(imageManager.convertImage(any(Image.class), any(ImageFlavor[].class), any(Map.class)))
                    .thenReturn(imageBuffered);
            afpPainter.drawBorderRect(rectangle, border1, border2, border3, border4, Color.WHITE);
            // note: here we would really like to verify that the second and third arguments passed to
            // handleImage() are the instances ib and rect declared above but that causes mockito to throw
            // an exception, probably because we cannot declare the AFPRenderingContext and are forced to
            // use any(), which forces the use of any() for all arguments
            verify(afpImageHandlerRenderedImage).handleImage(any(AFPRenderingContext.class),
                    any(Image.class), any(Rectangle.class));
        } catch (Exception e) {
            fail("something broke...");
        }
    }

    @Test
    public void testPresentationText() throws URISyntaxException, IFException, IOException {
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
    public void testPresentationText2() throws URISyntaxException, IFException, IOException {
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

    private String writeText(List<String> text) throws URISyntaxException, IOException, IFException {
        FOUserAgent agent = FopFactory.newInstance(new URI(".")).newFOUserAgent();
        IFContext context = new IFContext(agent);
        AFPDocumentHandler doc = new AFPDocumentHandler(context);
        AFPPainter afpPainter = new AFPPainter(doc);
        FontInfo fi = new FontInfo();
        fi.addFontProperties("", Font.DEFAULT_FONT);
        RasterFont rf = new RasterFont("", true);
        CharacterSet cs = mock(CharacterSet.class);
        CharactersetEncoder.EncodedChars encoder = mock(CharactersetEncoder.EncodedChars.class);
        when(cs.encodeChars(anyString())).thenReturn(encoder);
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
            afpPainter.drawText(0, 0, 0, 0, null, s);
        }
        doc.endDocument();

        InputStream bis = new ByteArrayInputStream(outputStream.toByteArray());
        StringBuilder sb = new StringBuilder();
        new AFPParser(false).read(bis, sb);
        return sb.toString();
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
        int style = Constants.EN_DOTTED;
        BorderProps.Mode mode = BorderProps.Mode.COLLAPSE_OUTER;
        Color color = ColorUtil.parseColorString(ua, "fop-rgb-icc(0.5019608,0.5019608,0.5019608,#CMYK,,0,0,0,0.5)");
        int borderWidth = 500;
        int radiusStart = 0;
        int radiusEnd = 0;
        BorderProps border1 = new BorderProps(style, borderWidth, radiusStart, radiusEnd, color, mode);
        afpPainter.drawBorderRect(new Rectangle(0, 0, 552755, 16090), null, border1, null, null, Color.WHITE);
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
}
