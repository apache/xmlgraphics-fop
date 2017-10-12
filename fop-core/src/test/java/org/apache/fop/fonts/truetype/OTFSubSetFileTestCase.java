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
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFParser;

import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.cff.CFFDataReader;
import org.apache.fop.fonts.cff.CFFDataReader.CFFIndexData;
import org.apache.fop.fonts.cff.CFFDataReader.DICTEntry;
import org.apache.fop.fonts.truetype.OTFSubSetFile.BytesNumber;

public class OTFSubSetFileTestCase extends OTFFileTestCase {
    private Map<Integer, Integer> glyphs = new HashMap<Integer, Integer>();

    /**
     * Initialises the test by creating the font subset. A CFFDataReader is
     * also created based on the subset data for use in the tests.
     * @throws IOException
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        for (int i = 0; i < 256; i++) {
            glyphs.put(i, i);
        }
    }

    private CFFDataReader getCFFReaderSourceSans() throws IOException {
        byte[] sourceSansData = getSourceSansSubset().getFontSubset();
        return new CFFDataReader(sourceSansData);
    }

    private OTFSubSetFile getSourceSansSubset() throws IOException {
        OTFSubSetFile sourceSansSubset = new OTFSubSetFile();
        sourceSansSubset.readFont(sourceSansReader, "SourceSansProBold", null, glyphs);
        return sourceSansSubset;
    }

    /**
     * Validates the CharString data against the original font
     * @throws IOException
     */
    @Test
    public void testCharStringIndex() throws IOException {
        CFFDataReader cffReaderSourceSans = getCFFReaderSourceSans();
        assertEquals(256, cffReaderSourceSans.getCharStringIndex().getNumObjects());
        assertTrue(checkCorrectOffsets(cffReaderSourceSans.getCharStringIndex()));
        validateCharStrings(cffReaderSourceSans, getSourceSansSubset().getCFFReader());
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
        List<byte[]> origCharStringData = sourceSansOriginal.getCharStringBytes();
        for (int i = 0; i < charStrings.getNumObjects(); i++) {
            byte[] origCharData = origCharStringData.get(i);
            byte[] charData = charStrings.getValue(i);
            List<BytesNumber> origOperands = getFullCharString(new Context(), origCharData, origCFF);
            List<BytesNumber> subsetOperands = getFullCharString(new Context(), charData, subsetCFF);
            for (int j = 0; j < origOperands.size(); j++) {
                assertTrue(origOperands.get(j).equals(subsetOperands.get(j)));
            }
        }
    }

    static class Context {
        private ArrayList<BytesNumber> operands = new ArrayList<BytesNumber>();
        private ArrayList<BytesNumber> stack = new ArrayList<BytesNumber>();
        private int hstemCount;
        private int vstemCount;
        private int lastOp = -1;
        private int maskLength = -1;

        public void pushOperand(BytesNumber v) {
            operands.add(v);
            if (v instanceof Operator) {
                if (v.getNumber() != 11 && v.getNumber() != 12) {
                    lastOp = v.getNumber();
                }
            } else {
                stack.add(v);
            }
        }

        public BytesNumber popOperand() {
            operands.remove(operands.size() - 1);
            return stack.remove(stack.size() - 1);
        }

        public BytesNumber lastOperand() {
            return operands.get(operands.size() - 1);
        }

        public void clearStack() {
            stack.clear();
        }

        public int getMaskLength() {
            // The number of data bytes for mask is exactly the number needed, one
            // bit per hint, to reference the number of stem hints declared
            // at the beginning of the charstring program.
            if (maskLength > 0) {
                return maskLength;
            }
            return 1 + (hstemCount + vstemCount  - 1) / 8;
        }

        public List<BytesNumber> getFullOperandsList() {
            return operands;
        }

        public void countHstem() {
            // hstem(hm) operator
            hstemCount += stack.size() / 2;
            clearStack();
        }

        public void countVstem() {
            // vstem(hm) operator
            vstemCount += stack.size() / 2;
            clearStack();
        }

        public int calcMaskLength() {
            if (lastOp == 1 || lastOp == 18) {
                //If hstem and vstem hints are both declared at the beginning of
                //a charstring, and this sequence is followed directly by the
                //hintmask or cntrmask operators, the vstem hint operator need
                //not be included.
                vstemCount += stack.size() / 2;
            }
            clearStack();
            return getMaskLength();
        }
    }
    /**
     * Recursively reads and constructs the full CharString for comparison
     * @param data The original byte data of the CharString
     * @param cffData The CFFDataReader containing the subroutine indexes
     * @return Returns a list of parsed operands and operators
     * @throws IOException
     */
    private List<BytesNumber> getFullCharString(Context context, byte[] data, CFFDataReader cffData)
        throws IOException {
        CFFIndexData localIndexSubr = cffData.getLocalIndexSubr();
        CFFIndexData globalIndexSubr = cffData.getGlobalIndexSubr();
        boolean hasLocalSubroutines = localIndexSubr != null && localIndexSubr.getNumObjects() > 0;
        boolean hasGlobalSubroutines = globalIndexSubr != null && globalIndexSubr.getNumObjects() > 0;
        for (int dataPos = 0; dataPos < data.length; dataPos++) {
            int b0 = data[dataPos] & 0xff;
            if (b0 == 10 && hasLocalSubroutines) {
                int subrNumber = getSubrNumber(localIndexSubr.getNumObjects(),
                        context.popOperand().getNumber());
                byte[] subr = localIndexSubr.getValue(subrNumber);
                getFullCharString(context, subr, cffData);
            } else if (b0 == 29 && hasGlobalSubroutines) {
                int subrNumber = getSubrNumber(globalIndexSubr.getNumObjects(),
                        context.popOperand().getNumber());
                byte[] subr = globalIndexSubr.getValue(subrNumber);
                getFullCharString(context, subr, cffData);
            } else if ((b0 >= 0 && b0 <= 27) || (b0 >= 29 && b0 <= 31)) {
                int size = 1;
                int b1 = -1;
                if (b0 == 12) {
                    b1 = data[dataPos++] & 0xff;
                    size = 2;
                } else if (b0 == 1 || b0 == 18) {
                    context.countHstem();
                } else if (b0 == 3 || b0 == 23) {
                    context.countVstem();
                } else if (b0 == 19 || b0 == 20) {
                    int length = context.calcMaskLength();
                    dataPos += length;
                    size = length + 1;
                }
                context.pushOperand(new Operator(b0, size, getOperatorName(b0, b1)));
            } else if (b0 == 28 || (b0 >= 32 && b0 <= 255)) {
                context.pushOperand(readNumber(b0, data, dataPos));
                dataPos += context.lastOperand().getNumBytes() - 1;
            }
        }
        return context.getFullOperandsList();
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
            return new BytesNumber((int) (short) (b1 << 8 | b2), 3);
        } else if (b0 >= 32 && b0 <= 246) {
            return new BytesNumber(b0 - 139, 1);
        } else if (b0 >= 247 && b0 <= 250) {
            int b1 = input[curPos + 1] & 0xff;
            return new BytesNumber((b0 - 247) * 256 + b1 + 108, 2);
        } else if (b0 >= 251 && b0 <= 254) {
            int b1 = input[curPos + 1] & 0xff;
            return new BytesNumber(-(b0 - 251) * 256 - b1 - 108, 2);
        } else if (b0 == 255) {
            int b1 = input[curPos + 1] & 0xff;
            int b2 = input[curPos + 2] & 0xff;
            int b3 = input[curPos + 3] & 0xff;
            int b4 = input[curPos + 4] & 0xff;
            return new BytesNumber((b1 << 24 | b2 << 16 | b3 << 8 | b4), 5);
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

        Operator(int number, int numBytes, String opName) {
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
     * @param operatorB The second byte of the operator
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
        CFFDataReader cffReaderSourceSans = getCFFReaderSourceSans();
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
        CFFDataReader cffReaderSourceSans = getCFFReaderSourceSans();
        Map<String, DICTEntry> topDictEntries = cffReaderSourceSans.parseDictData(
                cffReaderSourceSans.getTopDictIndex().getData());
        assertEquals(10, topDictEntries.size());
    }

    @Test
    public void testFDSelect() throws IOException {
        Assert.assertEquals(getSubset(1).length, 46);
        Assert.assertEquals(getSubset(2).length, 45);
    }

    private byte[] getSubset(final int opLen) throws IOException {
        FontFileReader reader = sourceSansReader;
        OTFSubSetFile otfSubSetFile = new MyOTFSubSetFile(opLen);
        otfSubSetFile.readFont(reader, "StandardOpenType", null, new HashMap<Integer, Integer>());
        return otfSubSetFile.getFontSubset();
    }

    @Test
    public void testOffsets() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2048; i++) {
            sb.append("SourceSansProBold");
        }
        OTFSubSetFile otfSubSetFile = new OTFSubSetFile();
        otfSubSetFile.readFont(sourceSansReader, sb.toString(), null, glyphs);
        new CFFParser().parse(otfSubSetFile.getFontSubset());
    }

    @Test
    public void testCharset() throws IOException {
        FontFileReader reader = sourceSansReader;
        MyOTFSubSetFile otfSubSetFile = new MyOTFSubSetFile(1);
        otfSubSetFile.readFont(reader, "StandardOpenType", null, new HashMap<Integer, Integer>());
        ByteArrayInputStream is = new ByteArrayInputStream(otfSubSetFile.getFontSubset());
        is.skip(otfSubSetFile.charsetOffset);
        Assert.assertEquals(is.read(), 2);
    }

    class MyOTFSubSetFile extends OTFSubSetFile {
        int charsetOffset;
        int opLen;
        MyOTFSubSetFile(int opLen) throws IOException {
            super();
            this.opLen = opLen;
        }

        protected void createCFF() throws IOException {
            cffReader = mock(CFFDataReader.class);
            when(cffReader.getHeader()).thenReturn(new byte[0]);
            when(cffReader.getTopDictIndex()).thenReturn(new CFFDataReader().new CFFIndexData() {
                public byte[] getByteData() throws IOException {
                    return new byte[] {0, 0, 1};
                }
            });

            LinkedHashMap<String, DICTEntry> map = new LinkedHashMap<String, DICTEntry>();
            DICTEntry dict = new DICTEntry();
            dict.setOperands(Collections.<Number>singletonList(1));
            map.put("charset", dict);
            map.put("CharStrings", dict);
            when((cffReader.getTopDictEntries())).thenReturn(map);
            CFFDataReader.Format3FDSelect fdSelect = new CFFDataReader().new Format3FDSelect();
            fdSelect.setRanges(new HashMap<Integer, Integer>());
            when(cffReader.getFDSelect()).thenReturn(fdSelect);
            cffReader.getTopDictEntries().get("CharStrings").setOperandLength(opLen);
            super.createCFF();
        }

        protected void updateFixedOffsets(Map<String, DICTEntry> topDICT, Offsets offsets) throws IOException {
            this.charsetOffset = offsets.charset;
            super.updateFixedOffsets(topDICT, offsets);
        }
    }

    @Test
    public void testResizeOfOperand() throws IOException {
        OTFSubSetFile otfSubSetFile = new OTFSubSetFile() {
            protected void writeFDSelect() {
                super.writeFDSelect();
                writeBytes(new byte[1024 * 100]);
            }
        };
        otfSubSetFile.readFont(sourceSansReader, "StandardOpenType", null, glyphs);
        byte[] fontSubset = otfSubSetFile.getFontSubset();
        CFFDataReader cffReader = new CFFDataReader(fontSubset);
        assertEquals(cffReader.getTopDictEntries().get("CharStrings").getOperandLength(), 5);
        assertEquals(cffReader.getTopDictEntries().get("CharStrings").getByteData().length, 6);
    }

    @Test
    public void testFDArraySize() throws IOException {
        OTFSubSetFileFDArraySize otfSubSetFileFDArraySize = new OTFSubSetFileFDArraySize();
        otfSubSetFileFDArraySize.readFont(sourceSansReader, "StandardOpenType", null, glyphs);
        byte[] fontSubset = otfSubSetFileFDArraySize.getFontSubset();
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(fontSubset));
        dis.skipBytes(otfSubSetFileFDArraySize.offset);
        Assert.assertEquals(dis.readUnsignedShort(), otfSubSetFileFDArraySize.fdFontCount);
        Assert.assertEquals(dis.readByte(), 2);
    }

    static class OTFSubSetFileFDArraySize extends OTFSubSetFile {
        int offset;
        int fdFontCount = 128;

        OTFSubSetFileFDArraySize() throws IOException {
            super();
        }

        protected void createCFF() throws IOException {
            super.createCFF();
            writeFDArray(new ArrayList<Integer>(), new ArrayList<Integer>(), new ArrayList<Integer>());
        }

        protected int writeFDArray(List<Integer> uniqueNewRefs, List<Integer> privateDictOffsets,
                                   List<Integer> fontNameSIDs) throws IOException {
            List<CFFDataReader.FontDict> fdFonts = cffReader.getFDFonts();
            CFFDataReader.FontDict fdFont = cffReader.new FontDict() {
                public byte[] getByteData() throws IOException {
                    return new byte[128];
                }
            };
            cffReader = makeCFFDataReader();
            when(cffReader.getFDFonts()).thenReturn(fdFonts);

            fdFonts.clear();
            uniqueNewRefs.clear();
            privateDictOffsets.clear();
            fontNameSIDs.clear();
            for (int i = 0; i < fdFontCount; i++) {
                fdFonts.add(fdFont);
                uniqueNewRefs.add(i);
                privateDictOffsets.add(i);
                fontNameSIDs.add(i);
            }
            offset = super.writeFDArray(uniqueNewRefs, privateDictOffsets, fontNameSIDs);
            return offset;
        }
    }

    @Test
    public void testOrderOfEntries() throws IOException {
        OTFSubSetFileEntryOrder otfSubSetFile = getFont(3, 2);
        assertTrue(otfSubSetFile.offsets.fdArray < otfSubSetFile.offsets.charString);
        assertEquals(otfSubSetFile.cffReader.getTopDictEntries().get("CharStrings").getOperandLength(), 5);
        otfSubSetFile = getFont(2, 3);
        assertTrue(otfSubSetFile.offsets.fdArray < otfSubSetFile.offsets.charString);
        assertEquals(otfSubSetFile.cffReader.getTopDictEntries().get("CharStrings").getOperandLength(), 5);
    }

    private OTFSubSetFileEntryOrder getFont(int csLen, int fdLen) throws IOException {
        glyphs.clear();
        OTFSubSetFileEntryOrder otfSubSetFile = new OTFSubSetFileEntryOrder(csLen, fdLen);
        otfSubSetFile.readFont(sourceSansReader, "StandardOpenType", null, glyphs);
        return otfSubSetFile;
    }

    static class OTFSubSetFileEntryOrder extends OTFSubSetFile {
        Offsets offsets;
        int csLen;
        int fdLen;

        OTFSubSetFileEntryOrder(int csLen, int fdLen) throws IOException {
            super();
            this.csLen = csLen;
            this.fdLen = fdLen;
        }

        protected void createCFF() throws IOException {
            cffReader = makeCFFDataReader();
            LinkedHashMap<String, DICTEntry> topDict = new LinkedHashMap<String, DICTEntry>();
            DICTEntry entry = new DICTEntry();
            entry.setOperands(Collections.<Number>singletonList(0));
            topDict.put("charset", entry);
            entry.setOperandLength(csLen);
            topDict.put("CharStrings", entry);
            entry = new DICTEntry();
            entry.setOperandLength(fdLen);
            topDict.put("FDArray", entry);
            when(cffReader.getTopDictEntries()).thenReturn(topDict);
            super.createCFF();
        }

        protected void updateCIDOffsets(Offsets offsets) throws IOException {
            super.updateCIDOffsets(offsets);
            this.offsets = offsets;
        }
    }

    private static CFFDataReader makeCFFDataReader() throws IOException {
        CFFDataReader cffReader = mock(CFFDataReader.class);
        when(cffReader.getHeader()).thenReturn(new byte[0]);
        when(cffReader.getTopDictIndex()).thenReturn(cffReader.new CFFIndexData() {
            public byte[] getByteData() throws IOException {
                return new byte[]{0, 0, 1};
            }
        });
        CFFDataReader.Format3FDSelect fdSelect = cffReader.new Format3FDSelect();
        fdSelect.setRanges(new HashMap<Integer, Integer>());
        when(cffReader.getFDSelect()).thenReturn(fdSelect);
        CFFDataReader.FontDict fd = mock(CFFDataReader.FontDict.class);
        when(fd.getPrivateDictData()).thenReturn(new byte[0]);
        when(cffReader.getFDFonts()).thenReturn(Collections.singletonList(fd));

        LinkedHashMap<String, DICTEntry> map = new LinkedHashMap<String, DICTEntry>();
        DICTEntry e = new DICTEntry();
        e.setOffset(1);
        e.setOperandLengths(Arrays.asList(0, 0));
        e.setOperandLength(2);
        map.put("FontName", e);
        map.put("Private", e);
        map.put("Subrs", e);
        when(cffReader.parseDictData(any(byte[].class))).thenReturn(map);
        return cffReader;
    }

    @Test
    public void testWriteCIDDictsAndSubrs() throws IOException {
        OTFSubSetFile subSetFile = new OTFSubSetFile() {
            public void readFont(FontFileReader in, String embeddedName, MultiByteFont mbFont) throws IOException {
                cffReader = makeCFFDataReader();
                fdSubrs = new ArrayList<List<byte[]>>();
                fdSubrs.add(new ArrayList<byte[]>());
                writeCIDDictsAndSubrs(Collections.singletonList(0));
            }
        };
        subSetFile.readFont(null, null, (MultiByteFont) null);

        ByteArrayInputStream is = new ByteArrayInputStream(subSetFile.getFontSubset());
        is.skip(1);
        Assert.assertEquals(is.read(), 247);
        Assert.assertEquals(is.read(), 0);
        final int sizeOfPrivateDictByteData = 108;
        is.skip(sizeOfPrivateDictByteData - 3);
        is.skip(2); //start index
        Assert.assertEquals(is.read(), 1);
    }

    @Test
    public void testResizeOfOperand2() throws IOException {
        OTFSubSetFile otfSubSetFile = new OTFSubSetFile() {
            void readFont(FontFileReader in, String embeddedName, MultiByteFont mbFont,
                          Map<Integer, Integer> usedGlyphs) throws IOException {
                cffReader = makeCFFDataReader();
                LinkedHashMap<String, DICTEntry> topDict = new LinkedHashMap<String, DICTEntry>();
                DICTEntry entry = new DICTEntry();
                entry.setOperandLength(1);
                entry.setOperator(new int[0]);
                entry.setOperands(Collections.<Number>singletonList(0));
                topDict.put("version", entry);
                when(cffReader.getTopDictEntries()).thenReturn(topDict);
                writeTopDICT();
            }
        };
        otfSubSetFile.readFont(sourceSansReader, "StandardOpenType", null, glyphs);
        ByteArrayInputStream fontSubset = new ByteArrayInputStream(otfSubSetFile.getFontSubset());
        fontSubset.skip(5);
        Assert.assertEquals(fontSubset.read(), 248);
        Assert.assertEquals(fontSubset.read(), (byte)(390 - 108));
    }
}
