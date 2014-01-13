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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.fontbox.cff.CFFDataInput;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFParser;
import org.apache.fontbox.cff.IndexData;
import org.apache.fontbox.cff.Type2CharStringParser;

import org.apache.fop.fonts.cff.CFFDataReader;
import org.apache.fop.fonts.cff.CFFDataReader.CFFIndexData;
import org.apache.fop.fonts.cff.CFFDataReader.DICTEntry;

public class OTFSubSetFileTestCase extends OTFFileTestCase {

    CFFDataReader cffReaderSourceSans;
    private OTFSubSetFile sourceSansSubset;
    private byte[] sourceSansData;
    CFFDataReader cffReaderHeitiStd;

    public OTFSubSetFileTestCase() throws IOException {
        super();
    }

    /**
     * Initialises the test by creating the font subset. A CFFDataReader is
     * also created based on the subset data for use in the tests.
     * @throws IOException
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        Map<Integer, Integer> glyphs = new HashMap<Integer, Integer>();
        for (int i = 0; i < 256; i++) {
            glyphs.put(i, i);
        }

        sourceSansSubset = new OTFSubSetFile();
        String sourceSansHeader = OFFontLoader.readHeader(sourceSansReader);
        sourceSansSubset.readFont(sourceSansReader, "SourceSansProBold", sourceSansHeader, glyphs);
        sourceSansData = sourceSansSubset.getFontSubset();
        cffReaderSourceSans = new CFFDataReader(sourceSansData);
    }

    /**
     * Validates the CharString data against the original font
     * @throws IOException
     */
    @Test
    public void testCharStringIndex() throws IOException {
        assertEquals(256, cffReaderSourceSans.getCharStringIndex().getNumObjects());
        assertTrue(checkCorrectOffsets(cffReaderSourceSans.getCharStringIndex()));
        validateCharStrings(cffReaderSourceSans);
    }

    private boolean checkCorrectOffsets(CFFIndexData indexData) {
        int last = 0;
        for (int i = 0; i < indexData.getOffsets().length; i++) {
            if (indexData.getOffsets()[i] < last) {
                return false;
            }
            last = indexData.getOffsets()[i];
        }
        return true;
    }

    private void validateCharStrings(CFFDataReader cffReader) throws IOException {
        CFFFont sourceSansOriginal = sourceSansProBold.fileFont;
        Map<String, byte[]> origCharStringData = sourceSansOriginal.getCharStringsDict();
        IndexData origGlobalIndex = sourceSansOriginal.getGlobalSubrIndex();
        IndexData origLocalIndex = sourceSansOriginal.getLocalSubrIndex();

        CFFDataInput globalSubrs = new CFFDataInput(cffReader.getGlobalIndexSubr().getByteData());
        CFFDataInput localSubrs = new CFFDataInput(cffReader.getLocalIndexSubr().getByteData());

        IndexData globalIndexData = CFFParser.readIndexData(globalSubrs);
        IndexData localIndexData = CFFParser.readIndexData(localSubrs);

        CFFIndexData charStrings = cffReader.getCharStringIndex();
        for (int i = 0; i < charStrings.getNumObjects(); i++) {
            byte[] charData = charStrings.getValue(i);
            Type2CharStringParser parser = new Type2CharStringParser();

            byte[] origCharData = origCharStringData.get(origCharStringData.keySet().toArray(
                    new String[0])[i]);
            List<Object> origSeq = parser.parse(origCharData, origGlobalIndex, origLocalIndex);

            List<Object> subsetSeq = parser.parse(charData, globalIndexData, localIndexData);

            //Validates the subset glyph render routines against the originals
            assertEquals(origSeq, subsetSeq);
        }
    }

    /**
     * Validates the String index data and size
     * @throws IOException
     */
    @Test
    public void testStringIndex() throws IOException {
        assertEquals(164, cffReaderSourceSans.getStringIndex().getNumObjects());
        assertTrue(checkCorrectOffsets(cffReaderSourceSans.getStringIndex()));
        assertEquals("Amacron", new String(cffReaderSourceSans.getStringIndex().getValue(5)));
        assertEquals("Edotaccent", new String(cffReaderSourceSans.getStringIndex().getValue(32)));
        assertEquals("uni0122", new String(cffReaderSourceSans.getStringIndex().getValue(45)));
    }

    /**
     * Validates the Top Dict data
     * @throws IOException
     */
    @Test
    public void testTopDictData() throws IOException {
        Map<String, DICTEntry> topDictEntries = cffReaderSourceSans.parseDictData(
                cffReaderSourceSans.getTopDictIndex().getData());
        assertEquals(10, topDictEntries.size());
    }
}
