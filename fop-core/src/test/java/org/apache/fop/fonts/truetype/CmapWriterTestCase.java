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

package org.apache.fop.fonts.truetype;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.IOUtils;

import org.apache.fop.fonts.CMapSegment;

public class CmapWriterTestCase {

    private static final String FONT = "test/resources/fonts/ttf/DejaVuLGCSerif.ttf";

    @Test
    public void testAppendCmapResolvesPrivateUseMappings() throws IOException {
        byte[] original = readFont();
        List<CMapSegment> originalCmap = loadCmap(original);
        int gidForA = lookup(originalCmap, 'A');
        int gidForB = lookup(originalCmap, 'B');
        assertTrue("test font must map 'A'", gidForA > 0);
        assertTrue("test font must map 'B'", gidForB > 0);
        List<CMapSegment> augmented = new ArrayList<>(originalCmap);
        augmented.add(new CMapSegment(0xE000, 0xE000, gidForA));
        augmented.add(new CMapSegment(0xE001, 0xE001, gidForB));
        byte[] rewritten = CmapWriter.appendCmap(original, augmented.toArray(new CMapSegment[0]));
        List<CMapSegment> rewrittenCmap = loadCmap(rewritten);
        assertEquals(gidForA, lookup(rewrittenCmap, 0xE000));
        assertEquals(gidForB, lookup(rewrittenCmap, 0xE001));
        assertEquals(gidForA, lookup(rewrittenCmap, 'A'));
        assertEquals(gidForB, lookup(rewrittenCmap, 'B'));
    }

    private byte[] readFont() throws IOException {
        try (InputStream is = new FileInputStream(FONT)) {
            return IOUtils.toByteArray(is);
        }
    }

    private List<CMapSegment> loadCmap(byte[] font) throws IOException {
        FontFileReader reader = new FontFileReader(new ByteArrayInputStream(font));
        TTFFile ttf = new TTFFile();
        ttf.readFont(reader, OFFontLoader.readHeader(reader));
        return ttf.getCMaps();
    }

    private int lookup(List<CMapSegment> cmap, int codePoint) {
        for (CMapSegment segment : cmap) {
            if (codePoint >= segment.getUnicodeStart() && codePoint <= segment.getUnicodeEnd()) {
                return segment.getGlyphStartIndex() + (codePoint - segment.getUnicodeStart());
            }
        }
        return 0;
    }
}
