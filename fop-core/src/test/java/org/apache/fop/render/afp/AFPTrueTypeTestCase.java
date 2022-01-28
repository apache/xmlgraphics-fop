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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;

import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.afp.DataStream;
import org.apache.fop.afp.Factory;
import org.apache.fop.afp.fonts.FopCharacterSet;
import org.apache.fop.afp.modca.PageObject;
import org.apache.fop.afp.parser.MODCAParser;
import org.apache.fop.afp.parser.UnparsedStructuredField;
import org.apache.fop.apps.EnvironmentalProfileFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.render.intermediate.IFException;

public class AFPTrueTypeTestCase {
    private String font;
    private String fopxconf = "<fop version=\"1.0\">\n"
            + "  <renderers>\n"
            + "    <renderer mime=\"application/x-afp\">\n"
            + "      <fonts>\n"
            + "        <font name=\"Univers\" embed-url=\"test/resources/fonts/ttf/DejaVuLGCSerif.ttf\">\n"
            + "          <font-triplet name=\"Univers\" style=\"normal\" weight=\"normal\"/>\n"
            + "          <font-triplet name=\"any\" style=\"normal\" weight=\"normal\"/>\n"
            + "        </font>\n"
            + "        <font>\n"
            + "          <afp-font name=\"Times Roman\" type=\"raster\" codepage=\"T1V10500\" encoding=\"Cp500\">\n"
            + "            <afp-raster-font size=\"12\" characterset=\"C0N200B0\" base14-font=\"TimesRoman\"/>\n"
            + "          </afp-font>\n"
            + "          <font-triplet name=\"Times\" style=\"normal\" weight=\"normal\"/>\n"
            + "        </font>"
            + "      </fonts>\n"
            + "    </renderer>\n"
            + "  </renderers>\n"
            + "</fop>";

    @Test
    public void testAFPTrueType() throws Exception {
        String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\n"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master master-name=\"simple\">\n"
                + "      <fo:region-body />\n"
                + "    </fo:simple-page-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"simple\">\n"
                + "    <fo:flow flow-name=\"xsl-region-body\">\n"
                + "      <fo:block font-family=\"Univers\">Univers</fo:block>\n"
                + "      <fo:block font-family=\"Times\">Times</fo:block>\n"
                + "    </fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>";
        String format = "BEGIN RESOURCE_GROUP RG000001\n"
                + "BEGIN NAME_RESOURCE RES00001 Triplets: OBJECT_FUNCTION_SET_SPECIFICATION"
                + ",OBJECT_CLASSIFICATION,0x01,FULLY_QUALIFIED_NAME,\n"
                + "BEGIN OBJECT_CONTAINER OC000001 Triplets: 0x41,0x00,0x00,\n";
        for (int i = 0; i < 29; i++) {
            format += "DATA OBJECT_CONTAINER\n";
        }
        format += "END OBJECT_CONTAINER OC000001\n"
                + "END NAME_RESOURCE RES00001\n"
                + "END RESOURCE_GROUP RG000001\n"
                + "BEGIN DOCUMENT DOC00001\n"
                + "BEGIN PAGE_GROUP PGP00001\n"
                + "BEGIN PAGE PGN00001\n"
                + "BEGIN ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "MAP CODED_FONT Triplets: FULLY_QUALIFIED_NAME,FULLY_QUALIFIED_NAME,CHARACTER_ROTATION,"
                + "RESOURCE_LOCAL_IDENTIFIER,\n"
                + "MAP DATA_RESOURCE Triplets: 0x01,FULLY_QUALIFIED_NAME,FULLY_QUALIFIED_NAME,OBJECT_CLASSIFICATION,"
                + "DATA_OBJECT_FONT_DESCRIPTOR,\n"
                + "DESCRIPTOR PAGE\n"
                + "MIGRATION PRESENTATION_TEXT\n"
                + "END ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "BEGIN PRESENTATION_TEXT PT000001\n"
                + "DATA PRESENTATION_TEXT\n"
                + "END PRESENTATION_TEXT PT000001\n"
                + "END PAGE PGN00001\n"
                + "END PAGE_GROUP PGP00001\n"
                + "END DOCUMENT DOC00001\n";

        Assert.assertEquals(getAFP(fo), format);
        Assert.assertEquals("test/resources/fonts/ttf/DejaVuLGCSerif.ttf", font);
    }

    @Test
    public void testTTFMixedWithRaster() throws Exception {
        String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\n"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master master-name=\"simple\">\n"
                + "      <fo:region-body />\n"
                + "    </fo:simple-page-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"simple\">\n"
                + "    <fo:flow flow-name=\"xsl-region-body\">\n"
                + "    <fo:block font-family=\"Times New Roman\">a</fo:block>\n"
                + "      <fo:block font-family=\"1\">a</fo:block>\n"
                + "      <fo:block font-family=\"2\">b</fo:block>\n"
                + "      <fo:block font-family=\"3\">c</fo:block>\n"
                + "      <fo:block font-family=\"4\">d</fo:block>"
                + "    </fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>";
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < 5; i++) {
            sb.append("<font>\n"
                    + "  <afp-font name=\"3OF9\" type=\"raster\" codepage=\"T1V10500\" encoding=\"Cp500\">\n"
                    + "      <afp-raster-font size=\"18\" characterset=\"C0920AB0\" base14-font=\"TimesRoman\"/>\n"
                    + "  </afp-font>\n"
                    + "  <font-triplet name=\"" + i + "\" style=\"normal\" weight=\"normal\"/>\n"
                    + "</font>");
        }
        fopxconf = "<fop version=\"1.0\">\n"
                + "  <renderers>\n"
                + "    <renderer mime=\"application/x-afp\">\n"
                + "      <fonts>\n"
                + sb.toString()
                + "<font embed-url=\"test/resources/fonts/ttf/DejaVuLGCSerif.ttf\" name=\"TimesNewRomanBold\"> \n"
                + "  <font-triplet name=\"Times New Roman\" style=\"normal\" weight=\"bold\"/> \n"
                + "  <font-triplet name=\"any\" style=\"normal\" weight=\"normal\"/> \n"
                + "</font>"
                + "      </fonts>\n"
                + "    </renderer>\n"
                + "  </renderers>\n"
                + "</fop>";
        Assert.assertTrue(getAFP(fo).contains("BEGIN ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "MAP CODED_FONT Triplets: FULLY_QUALIFIED_NAME,FULLY_QUALIFIED_NAME,CHARACTER_ROTATION,"
                + "RESOURCE_LOCAL_IDENTIFIER,EXTENDED_RESOURCE_LOCAL_IDENTIFIER,FULLY_QUALIFIED_NAME,"
                + "FULLY_QUALIFIED_NAME,CHARACTER_ROTATION,RESOURCE_LOCAL_IDENTIFIER,"
                + "EXTENDED_RESOURCE_LOCAL_IDENTIFIER,FULLY_QUALIFIED_NAME,FULLY_QUALIFIED_NAME,CHARACTER_ROTATION,"
                + "RESOURCE_LOCAL_IDENTIFIER,EXTENDED_RESOURCE_LOCAL_IDENTIFIER,FULLY_QUALIFIED_NAME,"
                + "FULLY_QUALIFIED_NAME,CHARACTER_ROTATION,RESOURCE_LOCAL_IDENTIFIER,\n"
                + "MAP DATA_RESOURCE Triplets: 0x01,FULLY_QUALIFIED_NAME,FULLY_QUALIFIED_NAME,OBJECT_CLASSIFICATION,"
                + "DATA_OBJECT_FONT_DESCRIPTOR,\n"
                + "DESCRIPTOR PAGE"));
    }

    @Test
    public void testSVGAFPTrueType() throws Exception {
        String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" "
                + "xmlns:fox=\"http://xmlgraphics.apache.org/fop/extensions\" "
                + "xmlns:svg=\"http://www.w3.org/2000/svg\">\n"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master master-name=\"simple\" page-height=\"27.9cm\" page-width=\"21.6cm\">\n"
                + "      <fo:region-body />\n"
                + "    </fo:simple-page-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"simple\">\n"
                + "    <fo:flow flow-name=\"xsl-region-body\">   \n"
                + "      <fo:block font-size=\"0\">\n"
                + "        <fo:instream-foreign-object content-height=\"792pt\" content-width=\"612pt\">\n"
                + "          <svg:svg viewBox=\"0 0 816 1056\" height=\"1056\" width=\"816\" id=\"svg2\" "
                + "version=\"1.1\">\n"
                + "            <svg:g transform=\"matrix(1.3333333,0,0,-1.3333333,0,1056)\" id=\"g10\">\n"
                + "              <svg:g id=\"g12\">\n"
                + "                <svg:g id=\"g14\">\n"
                + "                  <svg:g transform=\"translate(36,18)\" id=\"g40\">\n"
                + "                    <svg:text id=\"text44\" style=\"font-variant:normal;font-weight:normal;"
                + "font-size:9px;font-family:Univers;-inkscape-font-specification:ArialMT;writing-mode:lr-tb;"
                + "fill:#000000;fill-opacity:1;fill-rule:nonzero;stroke:none\" "
                + "transform=\"matrix(1,0,0,-1,44.92,11.4)\">\n"
                + "                      <svg:tspan id=\"tspan42\" y=\"0\" x=\"0.0\">W</svg:tspan>\n"
                + "                    </svg:text>\n"
                + "                  </svg:g>\n"
                + "                  <svg:g id=\"g2672\"/>\n"
                + "                </svg:g>\n"
                + "              </svg:g>\n"
                + "            </svg:g>\n"
                + "          </svg:svg>\n"
                + "        </fo:instream-foreign-object>\n"
                + "      </fo:block>\n"
                + "    </fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>";
        Assert.assertTrue(getAFP(fo).contains("DATA GRAPHICS"));
    }

    @Test
    public void testSVGAnchorAFP() throws Exception {
        String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" "
                + "xmlns:svg=\"http://www.w3.org/2000/svg\">\n"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master master-name=\"simple\" page-height=\"27.9cm\" page-width=\"21.6cm\">\n"
                + "      <fo:region-body />\n"
                + "    </fo:simple-page-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"simple\">\n"
                + "    <fo:flow flow-name=\"xsl-region-body\">   \n"
                + "      <fo:block font-size=\"0\">\n"
                + "        <fo:instream-foreign-object content-height=\"792pt\" content-width=\"612pt\">\n"
                + "<svg:svg xmlns=\"http://www.w3.org/2000/svg\" height=\"3mm\" viewBox=\"0 0 49.94 3\" "
                + "width=\"49.94mm\">\n"
                + "  <svg:g fill=\"black\" stroke=\"none\">\n"
                + "    <svg:text font-family=\"ExpertSans\" font-size=\"2.8219\" text-anchor=\"middle\" x=\"24.97\" "
                + "y=\"2.6228\">0000210122010000000100010004</svg:text>\n"
                + "  </svg:g>\n"
                + "</svg:svg>\n"
                + "        </fo:instream-foreign-object>\n"
                + "      </fo:block>\n"
                + "    </fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>";
        ByteArrayOutputStream bos = getAFPBytes(fo);
        MODCAParser parser = new MODCAParser(new ByteArrayInputStream(bos.toByteArray()));
        UnparsedStructuredField structuredField;
        while ((structuredField = parser.readNextStructuredField()) != null) {
            if (structuredField.toString().contains("Data Graphics")) {
                break;
            }
        }
        DataInputStream bis = new DataInputStream(new ByteArrayInputStream(structuredField.getData()));
        bis.skip(34);
        //X Y coordinates:
        Assert.assertEquals(bis.readShort(), 3);
        Assert.assertEquals(bis.readShort(), 5);
    }

    private String getAFP(String fo) throws Exception {
        ByteArrayOutputStream bos = getAFPBytes(fo);
        StringBuilder sb = new StringBuilder();
        InputStream bis = new ByteArrayInputStream(bos.toByteArray());
        new AFPParser(false).read(bis, sb);
        return sb.toString();
    }

    private ByteArrayOutputStream getAFPBytes(String fo) throws Exception {
        FopFactoryBuilder confBuilder = new FopConfParser(
                new ByteArrayInputStream(fopxconf.getBytes()),
                EnvironmentalProfileFactory.createRestrictedIO(new URI("."),
                        new MyResourceResolver())).getFopFactoryBuilder();
        FopFactory fopFactory = confBuilder.build();
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop("application/x-afp", foUserAgent, bos);
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        Source src = new StreamSource(new ByteArrayInputStream(fo.getBytes()));
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
        bos.close();
        return bos;
    }

    class MyResourceResolver implements ResourceResolver {
        private ResourceResolver defaultResourceResolver = ResourceResolverFactory.createDefaultResourceResolver();
        public Resource getResource(URI uri) throws IOException {
            if (!"tmp".equals(uri.getScheme())) {
                font = uri.getPath();
                uri = new File(".", uri.getPath()).toURI();
            }
            return defaultResourceResolver.getResource(uri);
        }

        public OutputStream getOutputStream(URI uri) throws IOException {
            return defaultResourceResolver.getOutputStream(uri);
        }
    }

    @Test
    public void testAFPPainter() throws IFException, IOException {
        AFPDocumentHandler afpDocumentHandler = mock(AFPDocumentHandler.class);
        when(afpDocumentHandler.getPaintingState()).thenReturn(new AFPPaintingState());
        when(afpDocumentHandler.getResourceManager()).thenReturn(new AFPResourceManager(null));

        DataStream ds = mock(DataStream.class);
        when(afpDocumentHandler.getDataStream()).thenReturn(ds);
        PageObject po = new PageObject(new Factory(), "PAGE0001", 0, 0, 0, 0, 0);
        when(ds.getCurrentPage()).thenReturn(po);

        AFPPainter afpPainter = new MyAFPPainter(afpDocumentHandler, true);
        afpPainter.setFont("any", "normal", 400, null, null, Color.BLACK);
        afpPainter.drawText(0, 0, 0, 0, null, "test");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        po.writeToStream(bos);

        InputStream bis = new ByteArrayInputStream(bos.toByteArray());
        StringBuilder sb = new StringBuilder();
        new AFPParser(true).read(bis, sb);
        Assert.assertTrue(sb.toString(),
                sb.toString().contains("DATA PRESENTATION_TEXT AMB AMI SCFL TRN t TRN e TRN s TRN t"));
    }

    @Test
    public void testAFPPainterWidths() throws IFException, IOException {
        String s = getAFPPainterWidths(true, "abcdefghijklmno");
        Assert.assertTrue(s, s.contains("DATA PRESENTATION_TEXT AMB AMI 0 SCFL SVI TRN a AMI"
                + " 9 TRN b AMI 29 TRN c AMI 59 TRN d AMI 99 TRN e AMI 149 TRN f AMI 209 TRN g AMI 24 TRN h AMI 105 TRN"
                + " i AMI 196 TRN j AMI 42 TRN k AMI 153 TRN l AMI 19 TRN m AMI 151 TRN n AMI 38 TRN o AMI 190"));
    }

    @Test
    public void testAFPPainterWidthsNoPositionByChar() throws IFException, IOException {
        String s = getAFPPainterWidths(false, "abcdefghijklmno");
        Assert.assertTrue(s, s.contains("DATA PRESENTATION_TEXT AMB AMI 0 SCFL SVI TRN abcdefghijklmno"));
    }

    @Test
    public void testAFPPainterWidthsNoPositionByCharLongText() throws IFException, IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            sb.append("test ");
        }
        String s = getAFPPainterWidths(false, sb.toString());
        Assert.assertTrue(s, s.contains("DATA PRESENTATION_TEXT AMB AMI 0 SCFL SVI TRN test test test test test test "
                + "test test test test test test test test test test test test test test test test test test test t "
                + "TRN est test test test test"));
    }

    private String getAFPPainterWidths(boolean positionByChar, String text) throws IFException, IOException {
        AFPDocumentHandler afpDocumentHandler = mock(AFPDocumentHandler.class);
        when(afpDocumentHandler.getPaintingState()).thenReturn(new AFPPaintingState());
        when(afpDocumentHandler.getResourceManager()).thenReturn(new AFPResourceManager(null));

        DataStream ds = mock(DataStream.class);
        when(afpDocumentHandler.getDataStream()).thenReturn(ds);
        PageObject po = new PageObject(new Factory(), "PAGE0001", 0, 0, 0, 0, 0);
        when(ds.getCurrentPage()).thenReturn(po);

        AFPPainter afpPainter = new MyAFPPainter(afpDocumentHandler, positionByChar);
        afpPainter.setFont("any", "normal", 400, null, 12000, Color.BLACK);
        afpPainter.drawText(0, 0, 0, 0, null, text);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        po.writeToStream(bos);
        InputStream bis = new ByteArrayInputStream(bos.toByteArray());
        StringBuilder sb = new StringBuilder();
        AFPParser afpParser = new AFPParser(true);
        afpParser.readWidths = true;
        afpParser.read(bis, sb);
        return sb.toString();
    }

    static class MyAFPPainter extends AFPPainter {
        boolean positionByChar;

        MyAFPPainter(AFPDocumentHandler documentHandler, boolean positionByChar) {
            super(documentHandler);
            this.positionByChar = positionByChar;
        }

        protected FOUserAgent getUserAgent() {
            FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
            return fopFactory.newFOUserAgent();
        }

        protected FontInfo getFontInfo() {
            FontInfo f = new FontInfo();
            f.addFontProperties("any", FontTriplet.DEFAULT_FONT_TRIPLET);
            MultiByteFont font = new MultiByteFont(null, EmbeddingMode.AUTO) {
                public void setWidthArray(int[] wds) {
                    super.setWidthArray(wds);
                    for (int i = 'a'; i <= 'z'; i++) {
                        addPrivateUseMapping(i, i);
                    }
                }
            };
            int[] widths = new int[200];
            for (int i = 0; i < widths.length; i++) {
                widths[i] = 1000 + (i * 256);
            }
            font.setWidthArray(widths);
            f.addMetrics("any", new AFPFontConfig.AFPTrueTypeFont("", true,
                    new FopCharacterSet("", "UTF-16BE", "", font, null, null), null, null, null, positionByChar));
            return f;
        }
    }
}
