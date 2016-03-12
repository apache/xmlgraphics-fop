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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

import org.xml.sax.SAXException;

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

import junit.framework.Assert;

public class AFPTrueTypeTestCase {
    private String font;

    @Test
    public void testAFPTrueType() throws IOException, SAXException, TransformerException, URISyntaxException {
        String fopxconf = "<fop version=\"1.0\">\n"
                + "  <renderers>\n"
                + "    <renderer mime=\"application/x-afp\">\n"
                + "      <fonts>\n"
                + "        <font name=\"Univers\" embed-url=\"test/resources/fonts/ttf/DejaVuLGCSerif.ttf\">\n"
                + "          <font-triplet name=\"Univers\" style=\"normal\" weight=\"normal\"/>\n"
                + "          <font-triplet name=\"any\" style=\"normal\" weight=\"normal\"/>\n"
                + "        </font>\n"
                + "      </fonts>\n"
                + "    </renderer>\n"
                + "  </renderers>\n"
                + "</fop>";
        String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\n"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master master-name=\"simple\">\n"
                + "      <fo:region-body />\n"
                + "    </fo:simple-page-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"simple\">\n"
                + "    <fo:flow flow-name=\"xsl-region-body\">\n"
                + "      <fo:block font-family=\"Univers\">Univers</fo:block>\n"
                + "    </fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>";

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

        StringBuilder sb = new StringBuilder();
        InputStream bis = new ByteArrayInputStream(bos.toByteArray());
        new AFPParser(false).read(bis, sb);

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
                + "MAP DATA_RESOURCE Triplets: 0x01,FULLY_QUALIFIED_NAME,FULLY_QUALIFIED_NAME"
                + ",OBJECT_CLASSIFICATION,DATA_OBJECT_FONT_DESCRIPTOR,\n"
                + "DESCRIPTOR PAGE\n"
                + "MIGRATION PRESENTATION_TEXT\n"
                + "END ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "BEGIN PRESENTATION_TEXT PT000001\n"
                + "DATA PRESENTATION_TEXT\n"
                + "END PRESENTATION_TEXT PT000001\n"
                + "END PAGE PGN00001\n"
                + "END PAGE_GROUP PGP00001\n"
                + "END DOCUMENT DOC00001\n";

        Assert.assertEquals(sb.toString(), format);
        Assert.assertEquals("test/resources/fonts/ttf/DejaVuLGCSerif.ttf", font);
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

        AFPPainter afpPainter = new MyAFPPainter(afpDocumentHandler);
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

    class MyAFPPainter extends AFPPainter {
        public MyAFPPainter(AFPDocumentHandler documentHandler) {
            super(documentHandler);
        }

        protected FOUserAgent getUserAgent() {
            FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
            return fopFactory.newFOUserAgent();
        }

        protected FontInfo getFontInfo() {
            FontInfo f = new FontInfo();
            f.addFontProperties("any", FontTriplet.DEFAULT_FONT_TRIPLET);
            MultiByteFont font = new MultiByteFont(null, EmbeddingMode.AUTO);
            font.setWidthArray(new int[100]);
            f.addMetrics("any", new AFPFontConfig.AFPTrueTypeFont("", true,
                    new FopCharacterSet("", "UTF-16BE", "", font, null, null), null, null, null));
            return f;
        }
    }
}
