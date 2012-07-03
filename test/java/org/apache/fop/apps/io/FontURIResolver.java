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

package org.apache.fop.apps.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.xml.sax.SAXException;

import org.apache.fop.apps.FopConfBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.PDFRendererConfBuilder;

import static org.junit.Assert.assertTrue;

public class FontURIResolver extends BaseURIResolutionTest {

    public enum Event {
        TTF,
        TYPE1;
    }

    private static final InputStream FOP_CONF_STREAM = new FopConfBuilder()
            .setBaseURI(".")
            .setFontBaseURI("fonts:///")
            .useCache(false)
            .startRendererConfig(PDFRendererConfBuilder.class)
                .startFontsConfig()
                    .startFont(null, "gladiator?type=ttf")
                        .addTriplet("gladttf", "normal", "normal")
                    .endFont()
                    .startFont(null, "gladiator?type=type1")
                        .addTriplet("gladtype1", "normal", "normal")
                    .endFont()
                .endFontConfig()
            .endRendererConfig().build();

    private static final class CustomFontURIResolver extends TestingResourceResolver {

        private final File fontsDir = new File("test/resources/fonts/ttf/");

        public Resource getResource(URI uri) throws IOException {
            if (uri.getScheme().equals("fonts") && uri.getPath().equals("/gladiator")) {
                if (uri.getQuery().startsWith("type")) {
                    String typeArg = uri.getQuery().split("=")[1];
                    if (typeArg.equals("ttf")) {
                        recordProperty(uri, Event.TTF);
                        return new Resource(new FileInputStream(new File(fontsDir, "glb12.ttf")));
                    } else if (typeArg.equals("type1")) {
                        recordProperty(uri, Event.TYPE1);
                        return new Resource(new FileInputStream(new File(fontsDir, "glb12.ttf")));
                    }
                }
            }
            return null;
        }

        public OutputStream getOutputStream(URI uri) throws IOException {
            return null;
        }
    }

    private static final CustomFontURIResolver RESOLVER = new CustomFontURIResolver();

    public FontURIResolver() throws TransformerException, SAXException, IOException {
        super(FOP_CONF_STREAM, RESOLVER, new File(getFODirectory(), "font.fo"));
    }

    @Test
    @Override
    public void testAssertions() {
        Map<URI, Object> expectedEvent = new HashMap<URI, Object>();
        expectedEvent.put(URI.create("fonts:/gladiator?type=type1"), Event.TYPE1);
        expectedEvent.put(URI.create("fonts:/gladiator?type=ttf"), Event.TTF);

        Map<URI, Object> propertyMap = RESOLVER.getMap();
        assertTrue(propertyMap.equals(expectedEvent));
    }

}
