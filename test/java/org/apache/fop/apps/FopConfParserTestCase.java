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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test case for {@link FopConfParser}.
 */
public class FopConfParserTestCase {

    private final URI baseURI = URI.create("test/config/fop_factory_tests/");
    private FopConfBuilder builder;

    @Before
    public void setUp() {
        builder = new FopConfBuilder();
    }

    public static FopFactory getFopFactory(InputStream fopConfStream, URI baseURI) {
        FopConfParser confParser;
        try {
            confParser = new FopConfParser(fopConfStream, baseURI);
            return confParser.getFopFactoryBuilder().build();
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FopFactory buildFactory() {
        FopConfParser confParser;
        try {
            confParser = new FopConfParser(builder.build(), baseURI);
            return confParser.getFopFactoryBuilder().build();
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDefaults() {
        FopFactory config = buildFactory();
        FopFactoryBuilderTestCase.testDefaults(config, baseURI);
    }

    @Test
    public void testStrictFOValidation() {
        builder.setStrictValidation(false);
        assertFalse(buildFactory().validateStrictly());
    }

    @Test
    public void testStrictUserValidation() {
        builder.setStrictConfiguration(false);
        assertFalse(buildFactory().validateUserConfigStrictly());
    }

    @Test
    public void testAccessibility() {
        builder.setAccessibility(false);
        assertFalse(buildFactory().isAccessibilityEnabled());
    }

    @Test
    public void testSourceResolution() {
        float srcRes = 123.456f;
        builder.setSourceResolution(srcRes);
        assertEquals(srcRes, buildFactory().getSourceResolution(), 0.0001f);
    }

    @Test
    public void testTargetResolution() {
        float targetRes = 123.456f;
        builder.setTargetResolution(targetRes);
        assertEquals(targetRes, buildFactory().getTargetResolution(), 0.0001f);
    }

    @Test
    public void testBreakIndentInheritance() {
        builder.setBreakIndentInheritance(true);
        assertTrue(buildFactory().isBreakIndentInheritanceOnReferenceAreaBoundary());
    }

    @Test
    public void testDefaultPageSettings() {
        float height = 12.345f;
        float width = 67.89f;
        builder.setDefaultPageSettings(height, width);
        FopFactory factory = buildFactory();
        assertEquals("12.345", factory.getPageHeight());
        assertEquals("67.89", factory.getPageWidth());
    }

    @Test
    public void testPreferRenderer() {
        builder.setPreferRenderer(true);
        assertTrue(buildFactory().getRendererFactory().isRendererPreferred());
    }
}
