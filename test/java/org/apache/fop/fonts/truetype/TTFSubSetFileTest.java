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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class tests TTFSubSetFile
 * TODO: Test with more than just a single font
 */
public class TTFSubSetFileTest extends TTFFileTest {
    private TTFSubSetFile ttfSubset;
    private byte[] subset;
    /**
     * Constructor
     * @throws IOException exception
     */
    public TTFSubSetFileTest() throws IOException {
        super();
    }

    /**
     * setUp()
     * @exception IOException file read error
     */
    public void setUp() throws IOException {
        ttfSubset = new TTFSubSetFile();
        Map<Integer, Integer> glyphs = new HashMap<Integer, Integer>();
        for (int i = 0; i < 255; i++) {
            glyphs.put(i, i);
        }
        ttfSubset.readFont(dejavuReader, "DejaVu", glyphs);
        subset = ttfSubset.getFontSubset();
    }
    /**
     * Test readFont(FontFileReader, String, Map) - Reads the font and tests the output by injecting
     * it into a TTFFile object to check the validity of the file as a font. This currently doesn't
     * create a cmap table, and so the font doesn't contain ALL of the mandatory tables.
     * @throws IOException exception
     */
    public void testReadFont3Args() throws IOException {

        ByteArrayInputStream byteArray = new ByteArrayInputStream(subset);
        dejavuTTFFile.readFont(new FontFileReader(byteArray));
        // Test a couple arbitrary values
        assertEquals(dejavuTTFFile.convertTTFUnit2PDFUnit(-1576), dejavuTTFFile.getFontBBox()[0]);
        assertEquals(dejavuTTFFile.getFullName(), "DejaVu LGC Serif");
    }
}
