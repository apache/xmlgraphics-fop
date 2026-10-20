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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;

/**
 * DejaVuLGCSerif carries a Turkish language system under latn that omits the standard
 * ligatures, as Turkish fonts do because of the dotless i. With the FO language {@code tr},
 * which is what a producer writes, that system must be the one used; with {@code en}, which
 * the font has no system for, the script's default applies. Before FOP-3341 the language code
 * was never translated to the font's tag, so {@code tr} took the fallback and ligated.
 */
public class LanguageSystemTestCase {

    private MultiByteFont font;

    @Before
    public void setUp() throws Exception {
        InternalResourceResolver resolver =
                ResourceResolverFactory.createDefaultInternalResourceResolver(new File(".").toURI());
        File file = new File("test/resources/fonts/ttf/DejaVuLGCSerif.ttf");
        CustomFont loaded = FontLoader.loadFont(new FontUris(file.toURI(), null), "", true,
                EmbeddingMode.AUTO, EncodingMode.AUTO, false, true, resolver, false, false, true);
        assertTrue(loaded instanceof MultiByteFont);
        font = (MultiByteFont) loaded;
        assertTrue(font.performsSubstitution());
    }

    private String fi(String language) {
        return font.performSubstitution("fi", "latn", language, null, false).toString();
    }

    @Test
    public void testTurkishUsesTheFontsTurkishSystem() {
        assertEquals("fi", fi("tr"));
        assertEquals("fi", fi("TRK"));
    }

    @Test
    public void testLanguageWithoutASystemUsesTheScriptsDefault() {
        assertEquals("\uFB01", fi("en"));
        assertEquals("\uFB01", fi("dflt"));
    }
}
