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

package org.apache.fop.apps;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.Permission;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.config.BaseConstructiveUserConfigTest;
import org.apache.fop.render.RendererConfig.RendererConfigParser;
import org.apache.fop.render.pdf.PDFRendererConfig;

public class FopFactoryTestCase extends BaseConstructiveUserConfigTest {

    public FopFactoryTestCase() throws SAXException, IOException {
        super(new FopConfBuilder().setStrictValidation(true)
                .startRendererConfig(PDFRendererConfBuilder.class)
                .startFontsConfig()
                    .startFont(null, "test/resources/fonts/ttf/glb12.ttf.xml")
                        .addTriplet("Gladiator", "normal", "normal")
                    .endFont()
                .endFontConfig()
            .endRendererConfig().build());
    }

    @Test
    @Override
    public void testUserConfig() throws Exception {
        RendererConfigParser mock = mock(RendererConfigParser.class);
        when(mock.getMimeType()).thenReturn(MimeConstants.MIME_PDF);
        try {
            convertFO();
            PDFRendererConfig config = (PDFRendererConfig) fopFactory.getRendererConfig(null, null,
                    mock);
            convertFO();
            assertEquals(config, fopFactory.getRendererConfig(null, null, mock));
        } catch (Exception e) {
            // this should *not* happen!
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testSecurityManager() throws Exception {
        try {
            System.setSecurityManager(new SecurityManager() {
                public void checkPermission(Permission perm) {
                    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                        if (element.toString().contains("java.security.AccessController.doPrivileged")
                                || element.toString().contains("newFop(")
                                || element.toString().contains("setSecurityManager(")) {
                            return;
                        }
                    }
                    throw new RuntimeException("doPrivileged not used for " + perm);
                }
            });
            FopFactory fopFactory = FopFactory.newInstance(new URI("."));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" "
                + "xmlns:fox=\"http://xmlgraphics.apache.org/fop/extensions\">\n"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master master-name=\"simple\" page-height=\"27.9cm\" page-width=\"21.6cm\">\n"
                + "      <fo:region-body />\n"
                + "    </fo:simple-page-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"simple\">\n"
                + "    <fo:flow flow-name=\"xsl-region-body\">\n"
                + " <fo:block font-size=\"100pt\">test2test2test2test2test2test2test2test2test2test2te"
                + "st2test2test2test2test2test2test2</fo:block>     \n"
                + "</fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>\n";
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, fopFactory.newFOUserAgent(), out);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Source src = new StreamSource(new ByteArrayInputStream(fo.getBytes()));
            Result res = new SAXResult(fop.getDefaultHandler());
            transformer.transform(src, res);
            System.setSecurityManager(null);
        } catch (UnsupportedOperationException e) {
            //skip on java 18
        }
    }
}
