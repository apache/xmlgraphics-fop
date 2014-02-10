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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.apache.fontbox.cff.CFFFont;

import org.apache.fop.fonts.cff.CFFDataReader;
import org.apache.fop.fonts.cff.CFFDataReader.CFFIndexData;
import org.apache.fop.fonts.cff.CFFDataReader.DICTEntry;
import org.apache.fop.fonts.truetype.OTFSubSetFile.BytesNumber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OTFSubSetFileTestCase extends OTFFileTestCase {

    CFFDataReader cffReaderSourceSans;
    private OTFSubSetFile sourceSansSubset;
    private byte[] sourceSansData;
    CFFDataReader cffReaderHeitiStd;

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
        validateCharStrings(cffReaderSourceSans, sourceSansSubset.getCFFReader());
    }

    /**
     * Checks the index data to ensure that the offsets are valid
     * @param indexData The index data to check
     * @return Returns true if it is found to be valid
     */
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

    /**
     * Validates the subset font CharString data by comparing it with the original.
     * @param subsetCFF The subset CFFDataReader containing the CharString data
     * @param origCFF The original CFFDataReader containing the CharString data
     * @throws IOException
     */
    private void validateCharStrings(CFFDataReader subsetCFF, CFFDataReader origCFF)
            throws IOException {
        CFFFont sourceSansOriginal = sourceSansProBold.fileFont;
        CFFIndexData charStrings = subsetCFF.getCharStringIndex();
        Map<String, byte[]> origCharStringData = sourceSansOriginal.getCharStringsDict();
        for (int i = 0; i < charStrings.getNumObjects(); i++) {
            byte[] origCharData = origCharStringData.get(origCharStringData.keySet().toArray(
                    new String[0])[i]);
            byte[] charData = charStrings.getValue(i);
            List<BytesNumber> origOperands = getFullCharString(origCharData, origCFF);
            List<BytesNumber> subsetOperands = getFullCharString(charData, subsetCFF);
            for (int j = 0;j < origOperands.size();j++) {
                assertTrue(origOperands.get(j).equals(subsetOperands.get(j)));
            }
        }
    }

    /**
     * Recursively reads and constructs the full CharString for comparison
     * @param data The original byte data of the CharString
     * @param cffData The CFFDataReader containing the subroutine indexes
     * @return Returns a list of parsed operands and operators
     * @throws IOException
     */
    private List<BytesNumber> getFullCharString(byte[] data, CFFDataReader cffData) throws IOException {
        CFFIndexData localIndexSubr = cffData.getLocalIndexSubr();
        CFFIndexData globalIndexSubr = cffData.getGlobalIndexSubr();
        boolean hasLocalSubroutines = localIndexSubr != null && localIndexSubr.getNumObjects() > 0;
        boolean hasGlobalSubroutines = globalIndexSubr != null && globalIndexSubr.getNumObjects() > 0;
        ArrayList<BytesNumber> operands = new ArrayList<BytesNumber>();
        for (int dataPos = 0; dataPos < data.length; dataPos++) {
            int b0 = data[dataPos] & 0xff;
            if (b0 == 10 && hasLocalSubroutines) {
                int subrNumber = getSubrNumber(localIndexSubr.getNumObjects(),
                        operands.get(operands.size() - 1).getNumber());
                byte[] subr = localIndexSubr.getValue(subrNumber);
                List<BytesNumber> subrOperands = getFullCharString(subr, cffData);
                operands = mergeOperands(operands, subrOperands);
            } else if (b0 == 29 && hasGlobalSubroutines) {
                int subrNumber = getSubrNumber(globalIndexSubr.getNumObjects(),
                        operands.get(operands.size() - 1).getNumber());
                byte[] subr = globalIndexSubr.getValue(subrNumber);
                ArrayList<BytesNumber> subrOperands = (ArrayList<BytesNumber>)getFullCharString(subr, cffData);
                operands = mergeOperands(operands, subrOperands);
            } else if ((b0 >= 0 && b0 <= 27) || (b0 >= 29 && b0 <= 31)) {
                int size = 1;
                int b1 = -1;
                if (b0 == 12) {
                    b1 = data[dataPos++] & 0xff;
                    size = 2;
                }
                if (b0 == 19 || b0 == 20) {
                    dataPos += 1;
                    size = 2;
                }
                operands.add(new Operator(b0, size, getOperatorName(b0, b1)));
            } else if (b0 == 28 || (b0 >= 32 && b0 <= 255)) {
                operands.add(readNumber(b0, data, dataPos));
                dataPos += operands.get(operands.size() - 1).getNumBytes() - 1;
            }
        }
        return operands;
    }

    /**
     * Merges two lists of operands. This is typically used to merge the CharString
     * data with that of a parsed and referenced subroutine.
     * @param charString The parsed CharString data so far
     * @param subroutine The parsed elements from a subroutine
     * @return Returns a merged list of both CharString and subroutine elements.
     */
    private ArrayList<BytesNumber> mergeOperands(List<BytesNumber> charString,
            List<BytesNumber> subroutine) {
        BytesNumber[] charStringOperands = charString.toArray(new BytesNumber[0]);
        BytesNumber[] subroutineOperands = subroutine.toArray(new BytesNumber[0]);
        BytesNumber[] mergeData = new BytesNumber[charStringOperands.length - 1 +
                                                  subroutineOperands.length - 1];
        System.arraycopy(charStringOperands, 0, mergeData, 0, charStringOperands.length - 1);
        System.arraycopy(subroutineOperands, 0, mergeData, charStringOperands.length - 1,
                subroutineOperands.length - 1);
        ArrayList<BytesNumber> hello = new ArrayList<BytesNumber>();
        hello.addAll(Arrays.asList(mergeData));
        return hello;
    }

    /**
     * Parses a number from one or more bytes
     * @param b0 The first byte to identify how to interpret the number
     * @param input The original byte data containing the number
     * @param curPos The current position of the number
     * @return Returns the number
     * @throws IOException
     */
    private BytesNumber readNumber(int b0, byte[] input, int curPos) throws IOException {
        if (b0 == 28) {
            int b1 = input[curPos + 1] & 0xff;
            int b2 = input[curPos + 2] & 0xff;
            return new BytesNumber(Integer.valueOf((short) (b1 << 8 | b2)), 3);
        } else if (b0 >= 32 && b0 <= 246) {
            return new BytesNumber(Integer.valueOf(b0 - 139), 1);
        } else if (b0 >= 247 && b0 <= 250) {
            int b1 = input[curPos + 1] & 0xff;
            return new BytesNumber(Integer.valueOf((b0 - 247) * 256 + b1 + 108), 2);
        } else if (b0 >= 251 && b0 <= 254) {
            int b1 = input[curPos + 1] & 0xff;
            return new BytesNumber(Integer.valueOf(-(b0 - 251) * 256 - b1 - 108), 2);
        } else if (b0 == 255) {
            int b1 = input[curPos + 1] & 0xff;
            int b2 = input[curPos + 2] & 0xff;
            return new BytesNumber(Integer.valueOf((short)(b1 << 8 | b2)), 5);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Gets the subroutine number according to the number of subroutines
     * and the provided operand.
     * @param numSubroutines The number of subroutines used to calculate the
     * subroutine reference.
     * @param operand The operand for the subroutine
     * @return Returns the calculated subroutine number
     */
    private int getSubrNumber(int numSubroutines, int operand) {
        int bias = getBias(numSubroutines);
        return bias + operand;
    }

    /**
     * Gets the bias give the number of subroutines. This is used in the
     * calculation to determine a subroutine's number
     * @param subrCount The number of subroutines for a given index
     * @return Returns the bias value
     */
    private int getBias(int subrCount) {
        if (subrCount < 1240) {
            return 107;
        } else if (subrCount < 33900) {
            return 1131;
        } else {
            return 32768;
        }
    }

    /**
     * A class representing an operator from the CharString data
     */
    private class Operator extends BytesNumber {
        private String opName = "";

        public Operator(int number, int numBytes, String opName) {
            super(number, numBytes);
            this.opName = opName;
        }
        public String toString() {
            return String.format("[%s]", opName);
        }
    }

    /**
     * Gets the identifying name for the given operator. This is primarily
     * used for debugging purposes. See the Type 2 CharString Format specification
     * document (Technical Note #5177) Appendix A (Command Codes).
     * @param operator The operator code
     * @param codeb The second byte of the operator
     * @return Returns the operator name.
     */
    private String getOperatorName(int operator, int operatorB) {
        switch (operator) {
        case 0: return "Reserved";
        case 1: return "hstem";
        case 2: return "Reserved";
        case 3: return "vstem";
        case 4: return "vmoveto";
        case 5: return "rlineto";
        case 6: return "hlineto";
        case 7: return "vlineto";
        case 8: return "rrcurveto";
        case 9: return "Reserved";
        case 10: return "callsubr";
        case 11: return "return";
        case 12: return getDoubleOpName(operatorB);
        case 13: return "Reserved";
        case 14: return "enchar";
        case 15:
        case 16:
        case 17: return "Reserved";
        case 18: return "hstemhm";
        case 19: return "hintmask";
        case 20: return "cntrmask";
        case 21: return "rmoveto";
        case 22: return "hmoveto";
        case 23: return "vstemhm";
        case 24: return "rcurveline";
        case 25: return "rlinecurve";
        case 26: return "vvcurveto";
        case 27: return "hhcurveto";
        case 28: return "shortint";
        case 29: return "callgsubr";
        case 30: return "vhcurveto";
        case 31: return "hvcurveto";
        default: return "Unknown";
        }
    }

    /**
     * Gets the name of a double byte operator code
     * @param operator The second byte of the operator
     * @return Returns the name
     */
    private String getDoubleOpName(int operator) {
        switch (operator) {
        case 0:
        case 1:
        case 2: return "Reserved";
        case 3: return "and";
        case 4: return "or";
        case 5: return "not";
        case 6:
        case 7:
        case 8: return "Reserved";
        case 9: return "abs";
        case 10: return "add";
        case 11: return "sub";
        case 12: return "div";
        case 13: return "Reserved";
        case 14: return "neg";
        case 15: return "eq";
        case 16:
        case 17: return "Reserved";
        case 18: return "drop";
        case 19: return "Reserved";
        case 20: return "put";
        case 21: return "get";
        case 22: return "ifelse";
        case 23: return "random";
        case 24: return "mul";
        case 25: return "Reserved";
        case 26: return "sqrt";
        case 27: return "dup";
        case 28: return "exch";
        case 29: return "index";
        case 30: return "roll";
        case 31:
        case 32:
        case 33: return "Reserved";
        case 34: return "hflex";
        case 35: return "flex";
        case 36: return "hflex1";
        case 37: return "flex1";
        case 38: return "Reserved";
        default: return "Unknown";
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
