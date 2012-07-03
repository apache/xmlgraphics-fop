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

package org.apache.fop.fonts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FopConfBuilder;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;

import static org.apache.fop.apps.FopConfParserTestCase.getFopFactory;
import static org.junit.Assert.assertEquals;

/**
 * A test case for {@link FontManagerConfigurator}.
 */
public class FontManagerConfiguratorTestCase {

    private FopConfBuilder builder;
    public final URI baseURI = new File("test/config/").getAbsoluteFile().toURI();

    @Before
    public void setUp() {
        builder = new FopConfBuilder();
    }

    private FontManager setBaseAndGetManager(String fontBase) {
        builder.setFontBaseURI(fontBase);
        return getManager();
    }

    private FontManager getManager() {
        FopFactory factory = getFopFactory(builder.build(), baseURI);
        return factory.getFontManager();
    }

    @Test(expected = FOPException.class)
    public void invalidURI() throws SAXException, IOException {
        builder.setFontBaseURI("$$%%**~{}][");
        FopConfParser confParser = new FopConfParser(builder.build(), baseURI);
        confParser.getFopFactoryBuilder().build();
    }

    @Test
    public void relativeFontBaseURITest() {
        String actualBase = "../../resources/fonts/ttf/";
        FontManager fontManager = setBaseAndGetManager(actualBase);
        URI expectedURI = baseURI.resolve(actualBase);
        assertEquals(expectedURI, fontManager.getResourceResolver().getBaseURI());
    }

    @Test
    public void currentRelativeFontBaseTest() {
        String actualBase = ".";
        FontManager fontManager = setBaseAndGetManager(actualBase);
        assertEquals(baseURI, fontManager.getResourceResolver().getBaseURI());
    }

    /**
     * This test is an interesting one; it's basically testing that if a base URI pointing to a
     * directory that doesn't exist is used, an error is not thrown. The URI resolver should handle
     * any {@link FileNotFoundException}s, not the configuration. We're NOT testing whether a font
     * can be resolved here, just that the URI resolver accepts it as its base URI.
     */
    @Test
    public void fontBaseDoesntExist() {
        // TODO: Sort this out
        String actualBase = "non-existing-dir/";
        FontManager fontManager = setBaseAndGetManager(actualBase);
        assertEquals(baseURI.resolve("non-existing-dir/"),
                fontManager.getResourceResolver().getBaseURI());
    }

    /**
     * Tests that when no &lt;font-base&gt; is given, it falls back to the URI used in &lt;base&gt;.
     */
    @Test
    public void noFontBaseURITest() {
        String actualBase = "../../resources/images/";
        builder.setBaseURI(actualBase);
        FontManager fontManager = getManager();
        assertEquals(baseURI.resolve(actualBase),
                fontManager.getResourceResolver().getBaseURI());
    }

    @Test
    public void absoluteBaseURI() {
        String absoluteBase = "test:///absolute/";
        FontManager fontManager = setBaseAndGetManager(absoluteBase);
        assertEquals(URI.create(absoluteBase), fontManager.getResourceResolver().getBaseURI());
    }
}
