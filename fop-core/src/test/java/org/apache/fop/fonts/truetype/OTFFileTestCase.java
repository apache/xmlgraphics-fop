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

import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OTFFileTestCase {
    protected OTFFile sourceSansProBold;
    protected FontFileReader sourceSansReader;
    protected OTFFile alexBrush;
    protected FontFileReader alexBrushReader;

    /**
     * Initializes fonts used for the testing of reading OTF CFF
     * @throws java.io.IOException
     */
    @Before
    public void setUp() throws Exception {
        sourceSansProBold = new OTFFile();
        InputStream sourceSansStream = new FileInputStream("test/resources/fonts/otf/SourceSansProBold.otf");
        sourceSansReader = new FontFileReader(sourceSansStream);
        String sourceSansHeader = OFFontLoader.readHeader(sourceSansReader);
        sourceSansProBold.readFont(sourceSansReader, sourceSansHeader);
        sourceSansStream.close();

        InputStream alexBrushStream = new FileInputStream("test/resources/fonts/otf/AlexBrushRegular.otf");
        alexBrush = new OTFFile();
        alexBrushReader = new FontFileReader(alexBrushStream);
        String carolynaHeader = OFFontLoader.readHeader(alexBrushReader);
        alexBrush.readFont(alexBrushReader, carolynaHeader);
        alexBrushStream.close();
    }

    /**
     * Tests the font names being read from the file
     */
    @Test
    public void testFontNames() {
        assertTrue(sourceSansProBold.getFamilyNames().contains("Source Sans Pro"));
        assertTrue(alexBrush.getFamilyNames().contains("Alex Brush"));
    }

    /**
     * Tests the number of glyphs and a select number of widths from each font
     */
    @Test
    public void testGlyphNumberAndWidths() {
        assertEquals(824, sourceSansProBold.numberOfGlyphs);
        assertEquals(256, alexBrush.numberOfGlyphs);

        int[] gids = {32, 42, 44, 47};
        int[] sourceSansWidths = {516, 555, 572, 383};
        for (int i = 0; i < gids.length; i++) {
            assertEquals(sourceSansWidths[i], sourceSansProBold.getWidths()[gids[i]]);
        }
        int[] carolynaWidths = {842, 822, 658, 784};
        for (int i = 0; i < gids.length; i++) {
            assertEquals(carolynaWidths[i], alexBrush.getWidths()[gids[i]]);
        }
    }
}
