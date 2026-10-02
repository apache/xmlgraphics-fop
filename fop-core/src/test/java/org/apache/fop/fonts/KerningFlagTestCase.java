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

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.complexscripts.fonts.GlyphPositioningTable;

/**
 * The font configuration's kerning attribute disables GPOS kerning as well as the legacy kern
 * table (FOP-3343). DejaVuLGCSerif has a GPOS kern feature under its Turkish language system,
 * which is named here by its tag so that the lookup is found whatever the fallback does.
 */
public class KerningFlagTestCase {

    private MultiByteFont load(boolean kerning) throws Exception {
        InternalResourceResolver resolver =
                ResourceResolverFactory.createDefaultInternalResourceResolver(new File(".").toURI());
        File file = new File("test/resources/fonts/ttf/DejaVuLGCSerif.ttf");
        return (MultiByteFont) FontLoader.loadFont(new FontUris(file.toURI(), null), "", true,
                EmbeddingMode.AUTO, EncodingMode.AUTO, kerning, true, resolver, false, false, true);
    }

    @Test
    public void testFlagIsRecordedOnTheFont() throws Exception {
        assertTrue(load(true).isKerningEnabled());
        assertFalse(load(false).isKerningEnabled());
    }

    @Test
    public void testKerningEnabledKernsThroughGpos() throws Exception {
        int[][] gpa = load(true).performPositioning("AV", "latn", "TRK", 1000);
        assertNotNull(gpa);
        assertTrue(gpa[0][GlyphPositioningTable.Value.IDX_X_ADVANCE] < 0);
    }

    @Test
    public void testKerningDisabledDoesNotKernThroughGpos() throws Exception {
        assertNull(load(false).performPositioning("AV", "latn", "TRK", 1000));
    }
}
