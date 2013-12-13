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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Testcase for {@link EmbedFontInfo}.
 */
public class EmbedFontInfoTestCase {

    public EmbedFontInfoTestCase() {}

    private EmbedFontInfo sut;

    private final URI metricsURI = URI.create("test/resources/fonts/ttf/glb12.ttf.xml");
    private final URI embedURI = URI.create("test/resources/fonts/ttf/glb12.ttf");
    private final boolean kerning = false;
    private final boolean useAdvanced = false;
    private final String subFontName = "Gladiator Bold";
    private final EncodingMode encMode = EncodingMode.CID;
    private final EmbeddingMode embedMode = EmbeddingMode.AUTO;
    private final FontTriplet triplet = new FontTriplet(subFontName, "bold", Font.WEIGHT_BOLD);

    @Before
    public void setUp() {
        List<FontTriplet> triplets = new ArrayList<FontTriplet>();
        triplets.add(triplet);
        sut = new EmbedFontInfo(metricsURI, kerning, useAdvanced, triplets, embedURI, subFontName,
                encMode, embedMode);
    }

    @Test
    public void testImmutableGetters() {
        assertEquals(metricsURI, sut.getMetricsURI());
        assertEquals(embedURI, sut.getEmbedURI());
        assertEquals(kerning, sut.getKerning());
        assertEquals(subFontName, sut.getSubFontName());
        assertEquals(encMode, sut.getEncodingMode());

        assertEquals(1, sut.getFontTriplets().size());
        assertEquals(triplet, sut.getFontTriplets().get(0));

        assertTrue(sut.isEmbedded());
    }

    @Test
    public void testMutableGetterSetters() {
        String psName = "Test Name";
        sut.setPostScriptName(psName);
        assertEquals(psName, sut.getPostScriptName());

        sut.setEmbedded(false);
        assertFalse(sut.isEmbedded());
    }

    @Test
    public void testQuirkyBoundaryCasesIsEmbedded() {
        sut = new EmbedFontInfo(metricsURI, kerning, useAdvanced, sut.getFontTriplets(), null,
                subFontName, encMode, embedMode);
        sut.setEmbedded(true);
        assertFalse(sut.isEmbedded());

        sut.setEmbedded(false);
        assertFalse(sut.isEmbedded());
    }

}
