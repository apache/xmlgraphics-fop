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
package org.apache.fop.render.ps;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.verification.VerificationMode;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.dsc.ResourceTracker;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fo.Constants;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFState;
import org.apache.fop.traits.BorderProps;

public class PSPainterTestCase {

    private PSDocumentHandler docHandler;
    private PSPainter psPainter;
    private PSGenerator gen;
    private IFState state;

    @Before
    public void setup() {
        state = IFState.create();
        FOUserAgent userAgent = mock(FOUserAgent.class);
        when(userAgent.getRendererOptions()).thenReturn(Collections.EMPTY_MAP);
        IFContext context = mock(IFContext.class);
        when(context.getUserAgent()).thenReturn(userAgent);
        docHandler = new PSDocumentHandler(context);
        gen = mock(PSGenerator.class);
        docHandler.gen = gen;
        state = IFState.create();
        psPainter = new PSPainter(docHandler, state);
    }

    @Test
    public void testNonZeroFontSize() throws IOException {
        testFontSize(6, times(1));
    }

    @Test
    public void testZeroFontSize() throws IOException {
        testFontSize(0, never());
    }

    private void testFontSize(int fontSize, VerificationMode test) throws IOException {
        state.setFontSize(fontSize);
        try {
            psPainter.drawText(10, 10, 2, 2, null, "Test");
        } catch (Exception ex) {
            //Expected
        }
        verify(gen, test).useColor(state.getTextColor());
    }

    @Test
    public void testDrawBorderRect() {
        // the goal of this test is to check that the drawing of rounded corners in PS calls
        // PSGraphicsPaiter.cubicBezierTo(); the check is done by verifying that a curveto command is written
        // to the PSGenerator
        PSGenerator psGenerator = mock(PSGenerator.class);
        when(psGenerator.formatDouble(anyFloat())).thenReturn("20.0"); // simplify!
        PSRenderingUtil psRenderingUtil = mock(PSRenderingUtil.class);
        PSDocumentHandler psDocumentHandler = mock(PSDocumentHandler.class);
        when(psDocumentHandler.getGenerator()).thenReturn(psGenerator);
        when(psDocumentHandler.getPSUtil()).thenReturn(psRenderingUtil);
        PSPainter psPainter = new PSPainter(psDocumentHandler);
        // build rectangle 200 x 50 (points, which are converted to milipoints)
        Rectangle rectangle = new Rectangle(0, 0, 200000, 50000);
        // build border properties: width 4pt, radius 30pt
        BorderProps border = new BorderProps(Constants.EN_SOLID, 4000, 30000, 30000, Color.BLACK,
                BorderProps.Mode.SEPARATE);
        try {
            psPainter.drawBorderRect(rectangle, border, border, border, border, Color.WHITE);
            verify(psGenerator, times(16)).writeln("20.0 20.0 20.0 20.0 20.0 20.0 curveto ");
        } catch (Exception e) {
            fail("something broke...");
        }
    }

    @Test
    public void testDrawText() {
        int fontSize = 12000;
        String fontName = "MockFont";
        PSGenerator psGenerator = mock(PSGenerator.class);
        PSRenderingUtil psRenderingUtil = mock(PSRenderingUtil.class);
        PSDocumentHandler psDocumentHandler = mock(PSDocumentHandler.class);
        FontInfo fontInfo = mock(FontInfo.class);
        PSFontResource psFontResource = mock(PSFontResource.class);
        MultiByteFont multiByteFont = mock(MultiByteFont.class);
        Font font = mock(Font.class);
        when(psDocumentHandler.getGenerator()).thenReturn(psGenerator);
        when(psDocumentHandler.getPSUtil()).thenReturn(psRenderingUtil);
        when(psDocumentHandler.getFontInfo()).thenReturn(fontInfo);
        when(psDocumentHandler.getPSResourceForFontKey(fontName)).thenReturn(psFontResource);
        when(fontInfo.getInternalFontKey(any(FontTriplet.class))).thenReturn(fontName);
        when(fontInfo.getFontInstance(any(FontTriplet.class), anyInt())).thenReturn(font);
        Map<String, Typeface> fonts = new HashMap<String, Typeface>();
        fonts.put(fontName, multiByteFont);
        when(fontInfo.getFonts()).thenReturn(fonts);

        IFState ifState = IFState.create();
        ifState.setFontSize(fontSize);

        PSPainter psPainter = new PSPainter(psDocumentHandler, ifState);

        int x = 100000;
        int y = 100000;
        int letterSpacing = 0;
        int wordSpacing = 0;
        int[][] dp = {{100, 100, 0, 0}, null, null, {200, 200, -100, -100}};
        double xAsDouble = (x + dp[0][0]) / 1000.0;
        double yAsDouble = (y - dp[0][1]) / 1000.0;
        when(psGenerator.formatDouble(xAsDouble)).thenReturn("100.100");
        when(psGenerator.formatDouble(yAsDouble)).thenReturn("99.900");
        String text = "Hello Mock!";
        try {
            psPainter.drawText(x, y, letterSpacing, wordSpacing, dp, text);
            verify(psGenerator).writeln("1 0 0 -1 100.100 99.900 Tm");
            verify(psGenerator).writeln("[<0000> [-100 100] <00000000> [200 -200] <0000> [-300 300] "
                            + "<0000000000000000000000000000>] TJ");
        } catch (Exception e) {
            fail("something broke...");
        }
    }

    @Test
    public void testOTF() throws IFException, IOException {
        FOUserAgent ua = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        final IFState state = IFState.create();
        PSDocumentHandler dh = new PSDocumentHandler(new IFContext(ua)) {
            protected PSFontResource getPSResourceForFontKey(String key) {
                return new PSFontResource() {
                    String getName() {
                        return state.getFontFamily();
                    }
                    void notifyResourceUsageOnPage(ResourceTracker resourceTracker) {
                    }
                };
            }
        };
        FontInfo fi = new FontInfo();
        addFont(fi, "OTFFont", true);
        addFont(fi, "TTFFont", false);

        dh.setFontInfo(fi);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        dh.setResult(new StreamResult(bos));
        dh.startDocument();
        state.setFontSize(10);
        state.setTextColor(Color.BLACK);
        state.setFontStyle("");
        PSPainter p = new PSPainter(dh, state) {
            protected String getFontKey(FontTriplet triplet) throws IFException {
                return state.getFontFamily();
            }
        };

        state.setFontFamily("TTFFont");
        p.drawText(0, 0, 0, 0, null, "test1");

        state.setFontFamily("OTFFont");
        p.drawText(0, 0, 0, 0, null, "test2");
        p.drawText(0, 0, 0, 0, null, "test3");

        state.setFontFamily("TTFFont");
        p.drawText(0, 0, 0, 0, null, "test4");

        Assert.assertTrue(bos.toString(), bos.toString().endsWith("BT\n"
                + "/TTFFont 0.01 F\n"
                + "1 0 0 -1 0 0 Tm\n"
                + "<00000000000000000000> t\n"
                + "/OTFFont.0 0.01 F\n"
                + "1 0 0 -1 0 0 Tm\n"
                + "<FFFFFFFFFF> t\n"
                + "/OTFFont.0 0.01 F\n"
                + "1 0 0 -1 0 0 Tm\n"
                + "<FFFFFFFFFF> t\n"
                + "/TTFFont 0.01 F\n"
                + "1 0 0 -1 0 0 Tm\n"
                + "<00000000000000000000> t\n"));
    }

    private void addFont(FontInfo fi, String name, boolean otf) {
        fi.addFontProperties(name, name, "", 0);
        MultiByteFont mbf = new MultiByteFont(null, EmbeddingMode.AUTO);
        mbf.setWidthArray(new int[100]);
        mbf.setIsOTFFile(otf);
        fi.addMetrics(name, mbf);
    }
}
