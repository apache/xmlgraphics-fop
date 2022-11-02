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

package org.apache.fop.svg.font;

import java.util.Collections;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.apache.fop.fonts.FontInfo;

/**
 * Specifically tests glyph positioning from a real font.
 */
public class GlyphLayoutTestCase extends FOPGVTGlyphVectorTest {

    /**
     * Glyph positioning using the legacy kern table.
     */
    @Test
    public void testBasicGlyphPositioning() throws Exception {
        testGlyphLayout(false);
    }

    /**
     * Glyph positioning using GPOS sub-tables.
     */
    @Test
    public void testAdvancedGlyphPositioning() throws Exception {
        testGlyphLayout(true);
    }

    private void testGlyphLayout(boolean useAdvanced) {
        FOPGVTFont font = loadFont(useAdvanced);
        glyphVector = (FOPGVTGlyphVector) font.createGlyphVector(null, "L\u201DP,V.F,A\u2019LT.", "DFLT", "dflt");
        glyphVector.performDefaultLayout();
        // Values in font units (unitsPerEm = 2048), glyph width - kern
        int[] widths = {
                /* L */ 1360 - 491,
                /* " */ 1047,
                /* P */ 1378 - 415,
                /* , */ 651,
                /* V */ 1479 - 358,
                /* . */ 651,
                /* F */ 1421 - 319,
                /* , */ 651,
                /* A */ 1479 - 301,
                /* ' */ 651,
                /* L */ 1360 - 167,
                /* T */ 1366 - 301,
                /* . */ 651};
        checkGlyphPositions(13, widths);
    }

    private FOPGVTFont loadFont(boolean useAdvanced) {
        FontInfo fontInfo = new FontInfoBuilder().useDejaVuLGCSerif(useAdvanced).build();
        FOPFontFamilyResolver resolver = new FOPFontFamilyResolverImpl(fontInfo);
        FOPGVTFontFamily family = resolver.resolve(FontInfoBuilder.DEJAVU_LGC_SERIF);
        return family.deriveFont(1000, Collections.emptyMap());
    }

    private void checkGlyphPositions(int expectedGlyphCount, int[] widths) {
        assertEquals(expectedGlyphCount, glyphVector.getNumGlyphs());
        float[] positions = new float[2 * (widths.length + 1)];
        for (int i = 0, n = 2; i < widths.length; i++, n += 2) {
            positions[n] = positions[n - 2] + widths[i] / 2.048f;
        }
        for (int i = 0; i <= widths.length; i++) {
            assertEquals(positions[2 * i], glyphVector.getGlyphPosition(i).getX(), 3);
        }
    }

}
