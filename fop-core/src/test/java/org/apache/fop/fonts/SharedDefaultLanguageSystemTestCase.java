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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.complexscripts.fonts.GlyphPositioningTable;
import org.apache.fop.complexscripts.fonts.GlyphTable;

/**
 * DejaVuLGCSerif's GPOS {@code latn} script points its default language system and its {@code AZE}
 * system at one table, as FontForge does for every language whose features equal the default's,
 * and GPOS has no {@code DFLT} script. The reader used to alias the default to the named language
 * and register nothing under {@code dflt}, so a request for the script's default found no kern
 * feature and the font was never kerned under a default language (FOP-3342). The GSUB side of the same font
 * does not share its table, which is why ligatures never showed the defect.
 */
public class SharedDefaultLanguageSystemTestCase {

    private MultiByteFont font;

    @Before
    public void setUp() throws Exception {
        InternalResourceResolver resolver =
                ResourceResolverFactory.createDefaultInternalResourceResolver(new File(".").toURI());
        File file = new File("test/resources/fonts/ttf/DejaVuLGCSerif.ttf");
        CustomFont loaded = FontLoader.loadFont(new FontUris(file.toURI(), null), "", true,
                EmbeddingMode.AUTO, EncodingMode.AUTO, true, true, resolver, false, false, true);
        assertTrue(loaded instanceof MultiByteFont);
        font = (MultiByteFont) loaded;
        assertTrue(font.performsPositioning());
    }

    @Test
    public void testSharedDefaultLanguageSystemIsRegisteredUnderDflt() {
        assertTrue(font.getGPOS().matchLookupSpecs("latn", "dflt", "kern").size() > 0);
        assertTrue(font.getGPOS().matchLookupSpecs("latn", "AZE", "kern").size() > 0);
        assertTrue(font.hasFeature(GlyphTable.GLYPH_TABLE_TYPE_POSITIONING, "latn", "dflt", "kern"));
    }

    @Test
    public void testSharedDefaultLanguageSystemKerns() {
        int[][] gpa = font.performPositioning("AV", "latn", "dflt", 1000);
        assertNotNull(gpa);
        assertTrue(gpa[0][GlyphPositioningTable.Value.IDX_X_ADVANCE] < 0);
    }
}
