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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.fontbox.cff.CFFStandardString;
import org.apache.fontbox.cff.encoding.CFFEncoding;

import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.cff.CFFDataReader;
import org.apache.fop.fonts.cff.CFFDataReader.CFFIndexData;
import org.apache.fop.fonts.cff.CFFDataReader.DICTEntry;
import org.apache.fop.fonts.cff.CFFDataReader.FDSelect;
import org.apache.fop.fonts.cff.CFFDataReader.FontDict;
import org.apache.fop.fonts.cff.CFFDataReader.Format0FDSelect;
import org.apache.fop.fonts.cff.CFFDataReader.Format3FDSelect;

/**
 * Reads an OpenType CFF file and generates a subset
 * The OpenType specification can be found at the Microsoft
 * Typography site: http://www.microsoft.com/typography/otspec/
 */
public class OTFSubSetFile extends OTFFile {

    private byte[] output;
    private int currentPos = 0;
    private int realSize = 0;

    /** A map containing each glyph to be included in the subset
      * with their existing and new GID's **/
    private LinkedHashMap<Integer, Integer> subsetGlyphs;

    /** A map of the new GID to SID used to construct the charset table **/
    private LinkedHashMap<Integer, Integer> gidToSID;

    private CFFIndexData localIndexSubr;
    private CFFIndexData globalIndexSubr;

    /** List of subroutines to write to the local / global indexes in the subset font **/
    private List<byte[]> subsetLocalIndexSubr;
    private List<byte[]> subsetGlobalIndexSubr;

    /** For fonts which have an FDSelect or ROS flag in Top Dict, this is used to store the
     * local subroutine indexes for each group as opposed to the above subsetLocalIndexSubr */
    private ArrayList<List<byte[]>> fdSubrs;

    /** The subset FD Select table used to store the mappings between glyphs and their
     * associated FDFont object which point to a private dict and local subroutines. */
    private LinkedHashMap<Integer, FDIndexReference> subsetFDSelect;

    /** A list of unique subroutines from the global / local subroutine indexes */
    private List<Integer> localUniques;
    private List<Integer> globalUniques;

    /** A store of the number of subroutines each global / local subroutine will store **/
    private int subsetLocalSubrCount;
    private int subsetGlobalSubrCount;

    /** A list of char string data for each glyph to be stored in the subset font **/
    private List<byte[]> subsetCharStringsIndex;

    /** The embedded name to change in the name table **/
    private String embeddedName;

    /** An array used to hold the string index data for the subset font **/
    private List<byte[]> stringIndexData = new ArrayList<byte[]>();

    /** The CFF reader object used to read data and offsets from the original font file */
    private CFFDataReader cffReader = null;

    /** The class used to represent this font **/
    private MultiByteFont mbFont;

    /** The number of standard strings in CFF **/
    private static final int NUM_STANDARD_STRINGS = 391;
    /** The operator used to identify a local subroutine reference */
    private static final int LOCAL_SUBROUTINE = 10;
    /** The operator used to identify a global subroutine reference */
    private static final int GLOBAL_SUBROUTINE = 29;

    public OTFSubSetFile() throws IOException {
        super();
    }

    public void readFont(FontFileReader in, String embeddedName, String header,
            MultiByteFont mbFont) throws IOException {
        this.mbFont = mbFont;
        readFont(in, embeddedName, header, mbFont.getUsedGlyphs());
    }

    /**
     * Reads and creates a subset of the font.
     *
     * @param in FontFileReader to read from
     * @param name Name to be checked for in the font file
     * @param header The header of the font file
     * @param glyphs Map of glyphs (glyphs has old index as (Integer) key and
     * new index as (Integer) value)
     * @throws IOException in case of an I/O problem
     */
    void readFont(FontFileReader in, String embeddedName, String header,
            Map<Integer, Integer> usedGlyphs) throws IOException {
        fontFile = in;

        currentPos = 0;
        realSize = 0;

        this.embeddedName = embeddedName;

        //Sort by the new GID and store in a LinkedHashMap
        subsetGlyphs = sortByValue(usedGlyphs);

        output = new byte[in.getFileSize()];

        initializeFont(in);

        cffReader = new CFFDataReader(fontFile);

        //Create the CIDFontType0C data
        createCFF();
    }

    private LinkedHashMap<Integer, Integer> sortByValue(Map<Integer, Integer> map) {
        List<Entry<Integer, Integer>> list = new ArrayList<Entry<Integer, Integer>>(map.entrySet());
        Collections.sort(list, new Comparator<Entry<Integer, Integer>>() {
             public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
                  return ((Comparable<Integer>) o1.getValue()).compareTo(o2.getValue());
             }
        });

       LinkedHashMap<Integer, Integer> result = new LinkedHashMap<Integer, Integer>();
       for (Entry<Integer, Integer> entry : list) {
           result.put(entry.getKey(), entry.getValue());
       }
       return result;
    }

    private void createCFF() throws IOException {
        //Header
        writeBytes(cffReader.getHeader());

        //Name Index
        writeIndex(Arrays.asList(embeddedName.getBytes()));

        //Keep offset of the topDICT so it can be updated once all data has been written
        int topDictOffset = currentPos;
        //Top DICT Index and Data
        byte[] topDictIndex = cffReader.getTopDictIndex().getByteData();
        int offSize = topDictIndex[2];
        writeBytes(topDictIndex, 0, 3 + (offSize * 2));
        int topDictDataOffset = currentPos;
        writeTopDICT();

        //Create the char string index data and related local / global subroutines
        if (cffReader.getFDSelect() == null) {
            createCharStringData();
        } else {
            createCharStringDataCID();
        }

        //If it is a CID-Keyed font, store each FD font and add each SID
        List<Integer> fontNameSIDs = null;
        List<Integer> subsetFDFonts = null;
        if (cffReader.getFDSelect() != null) {
            subsetFDFonts = getUsedFDFonts();
            fontNameSIDs = storeFDStrings(subsetFDFonts);
        }

        //String index
        writeStringIndex();

        //Global subroutine index
        writeIndex(subsetGlobalIndexSubr);

        //Encoding
        int encodingOffset = currentPos;
        writeEncoding(fileFont.getEncoding());

        //Charset table
        int charsetOffset = currentPos;
        writeCharsetTable(cffReader.getFDSelect() != null);

        //FDSelect table
        int fdSelectOffset = currentPos;
        if (cffReader.getFDSelect() != null) {
            writeFDSelect();
        }

        //Char Strings Index
        int charStringOffset = currentPos;
        writeIndex(subsetCharStringsIndex);

        if (cffReader.getFDSelect() == null) {
            //Keep offset to modify later with the local subroutine index offset
            int privateDictOffset = currentPos;
            writePrivateDict();

            //Local subroutine index
            int localIndexOffset = currentPos;
            writeIndex(subsetLocalIndexSubr);

            //Update the offsets
            updateOffsets(topDictOffset, charsetOffset, charStringOffset, privateDictOffset,
                    localIndexOffset, encodingOffset);
        } else {
            List<Integer> privateDictOffsets = writeCIDDictsAndSubrs(subsetFDFonts);
            int fdArrayOffset = writeFDArray(subsetFDFonts, privateDictOffsets, fontNameSIDs);

            updateCIDOffsets(topDictDataOffset, fdArrayOffset, fdSelectOffset, charsetOffset,
                    charStringOffset, encodingOffset);
        }
    }

    private List<Integer> storeFDStrings(List<Integer> uniqueNewRefs) throws IOException {
        ArrayList<Integer> fontNameSIDs = new ArrayList<Integer>();
        List<FontDict> fdFonts = cffReader.getFDFonts();
        for (int i = 0; i < uniqueNewRefs.size(); i++) {
            FontDict fdFont = fdFonts.get(uniqueNewRefs.get(i));
            byte[] fdFontByteData = fdFont.getByteData();
            Map<String, DICTEntry> fdFontDict = cffReader.parseDictData(fdFontByteData);
            fontNameSIDs.add(stringIndexData.size() + NUM_STANDARD_STRINGS);
            stringIndexData.add(cffReader.getStringIndex().getValue(fdFontDict.get("FontName")
                    .getOperands().get(0).intValue() - NUM_STANDARD_STRINGS));
        }
        return fontNameSIDs;
    }

    private void writeBytes(byte[] out) {
        for (int i = 0; i < out.length; i++) {
            output[currentPos++] = out[i];
            realSize++;
        }
    }

    private void writeBytes(byte[] out, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            output[currentPos++] = out[i];
            realSize++;
        }
    }

    private void writeEncoding(CFFEncoding encoding) throws IOException {
        LinkedHashMap<String, DICTEntry> topDICT = cffReader.getTopDictEntries();
        DICTEntry encodingEntry = topDICT.get("Encoding");
        if (encodingEntry != null && encodingEntry.getOperands().get(0).intValue() != 0
                && encodingEntry.getOperands().get(0).intValue() != 1) {
            writeByte(0);
            writeByte(gidToSID.size());
            for (int gid : gidToSID.keySet()) {
                int code = encoding.getCode(gidToSID.get(gid));
                writeByte(code);
            }
        }
    }

    private void writeTopDICT() throws IOException {
        LinkedHashMap<String, DICTEntry> topDICT = cffReader.getTopDictEntries();
        List<String> topDictStringEntries = Arrays.asList("version", "Notice", "Copyright",
                "FullName", "FamilyName", "Weight", "PostScript");
        for (Map.Entry<String, DICTEntry> dictEntry : topDICT.entrySet()) {
            String dictKey = dictEntry.getKey();
            DICTEntry entry = dictEntry.getValue();
            //If the value is an SID, update the reference but keep the size the same
            if (dictKey.equals("ROS")) {
                writeROSEntry(entry);
            } else if (dictKey.equals("CIDCount")) {
                writeCIDCount(entry);
            } else if (topDictStringEntries.contains(dictKey)) {
                writeTopDictStringEntry(entry);
            } else {
                writeBytes(entry.getByteData());
            }
        }
    }

    private void writeROSEntry(DICTEntry dictEntry) throws IOException {
        int sidA = dictEntry.getOperands().get(0).intValue();
        if (sidA > 390) {
            stringIndexData.add(cffReader.getStringIndex().getValue(sidA - NUM_STANDARD_STRINGS));
        }
        int sidAStringIndex = stringIndexData.size() + 390;
        int sidB = dictEntry.getOperands().get(1).intValue();
        if (sidB > 390) {
            stringIndexData.add("Identity".getBytes());
        }
        int sidBStringIndex = stringIndexData.size() + 390;
        byte[] cidEntryByteData = dictEntry.getByteData();
        cidEntryByteData = updateOffset(cidEntryByteData, 0, dictEntry.getOperandLengths().get(0),
                sidAStringIndex);
        cidEntryByteData = updateOffset(cidEntryByteData, dictEntry.getOperandLengths().get(0),
                dictEntry.getOperandLengths().get(1), sidBStringIndex);
        cidEntryByteData = updateOffset(cidEntryByteData, dictEntry.getOperandLengths().get(0)
                + dictEntry.getOperandLengths().get(1), dictEntry.getOperandLengths().get(2), 139);
        writeBytes(cidEntryByteData);
    }

    private void writeCIDCount(DICTEntry dictEntry) throws IOException {
        byte[] cidCountByteData = dictEntry.getByteData();
        cidCountByteData = updateOffset(cidCountByteData, 0, dictEntry.getOperandLengths().get(0),
                subsetGlyphs.size());
        writeBytes(cidCountByteData);
    }

    private void writeTopDictStringEntry(DICTEntry dictEntry) throws IOException {
        int sid = dictEntry.getOperands().get(0).intValue();
        if (sid > 391) {
            stringIndexData.add(cffReader.getStringIndex().getValue(sid - 391));
        }

        byte[] newDictEntry = createNewRef(stringIndexData.size() + 390, dictEntry.getOperator(),
                dictEntry.getOperandLength());
        writeBytes(newDictEntry);
    }

    private void writeStringIndex() throws IOException {
        Map<String, DICTEntry> topDICT = cffReader.getTopDictEntries();
        int charsetOffset = topDICT.get("charset").getOperands().get(0).intValue();

        gidToSID = new LinkedHashMap<Integer, Integer>();

        for (int gid : subsetGlyphs.keySet()) {
            int sid = cffReader.getSIDFromGID(charsetOffset, gid);
            //Check whether the SID falls into the standard string set
            if (sid < NUM_STANDARD_STRINGS) {
                gidToSID.put(subsetGlyphs.get(gid), sid);
                if (mbFont != null) {
                    mbFont.mapUsedGlyphName(subsetGlyphs.get(gid),
                            CFFStandardString.getName(sid));
                }
            } else {
                int index = sid - NUM_STANDARD_STRINGS;
                if (index <= cffReader.getStringIndex().getNumObjects()) {
                    if (mbFont != null) {
                        mbFont.mapUsedGlyphName(subsetGlyphs.get(gid),
                                new String(cffReader.getStringIndex().getValue(index)));
                    }
                    gidToSID.put(subsetGlyphs.get(gid), stringIndexData.size() + 391);
                    stringIndexData.add(cffReader.getStringIndex().getValue(index));
                } else {
                    if (mbFont != null) {
                        mbFont.mapUsedGlyphName(subsetGlyphs.get(gid), ".notdef");
                    }
                    gidToSID.put(subsetGlyphs.get(gid), index);
                }
            }
        }
        //Write the String Index
        writeIndex(stringIndexData);
    }

    private void createCharStringDataCID() throws IOException {
        CFFIndexData charStringsIndex = cffReader.getCharStringIndex();

        FDSelect fontDictionary = cffReader.getFDSelect();
        if (fontDictionary instanceof Format0FDSelect) {
            throw new UnsupportedOperationException("OTF CFF CID Format0 currently not implemented");
        } else if (fontDictionary instanceof Format3FDSelect) {
            Format3FDSelect fdSelect = (Format3FDSelect)fontDictionary;
            Map<Integer, Integer> subsetGroups = new HashMap<Integer, Integer>();

            List<Integer> uniqueGroups = new ArrayList<Integer>();
            for (int gid : subsetGlyphs.keySet()) {
                Integer[] ranges = fdSelect.getRanges().keySet().toArray(new Integer[0]);
                for (int i = 0; i < ranges.length; i++) {
                    int nextRange = -1;
                    if (i < ranges.length - 1) {
                        nextRange = ranges[i + 1];
                    } else {
                        nextRange = fdSelect.getSentinelGID();
                    }
                    if (gid >= ranges[i] && gid < nextRange) {
                        subsetGroups.put(gid, fdSelect.getRanges().get(ranges[i]));
                        if (!uniqueGroups.contains(fdSelect.getRanges().get(ranges[i]))) {
                            uniqueGroups.add(fdSelect.getRanges().get(ranges[i]));
                        }
                    }
                }
            }

            //Prepare resources
            globalIndexSubr = cffReader.getGlobalIndexSubr();

            //Create the new char string index
            subsetCharStringsIndex = new ArrayList<byte[]>();

            globalUniques = new ArrayList<Integer>();

            subsetFDSelect = new LinkedHashMap<Integer, FDIndexReference>();

            List<List<Integer>> foundLocalUniques = new ArrayList<List<Integer>>();
            for (int i = 0; i < uniqueGroups.size(); i++) {
                foundLocalUniques.add(new ArrayList<Integer>());
            }
            for (int gid : subsetGlyphs.keySet()) {
                int group = subsetGroups.get(gid);
                localIndexSubr = cffReader.getFDFonts().get(group).getLocalSubrData();
                localUniques = foundLocalUniques.get(uniqueGroups.indexOf(subsetGroups.get(gid)));

                FDIndexReference newFDReference = new FDIndexReference(
                        uniqueGroups.indexOf(subsetGroups.get(gid)), subsetGroups.get(gid));
                subsetFDSelect.put(subsetGlyphs.get(gid), newFDReference);
                byte[] data = charStringsIndex.getValue(gid);
                preScanForSubsetIndexSize(data);
            }

            //Create the two lists which are to store the local and global subroutines
            subsetGlobalIndexSubr = new ArrayList<byte[]>();

            fdSubrs = new ArrayList<List<byte[]>>();
            subsetGlobalSubrCount = globalUniques.size();
            globalUniques.clear();
            localUniques = null;

            for (int l = 0; l < foundLocalUniques.size(); l++) {
                fdSubrs.add(new ArrayList<byte[]>());
            }
            List<List<Integer>> foundLocalUniquesB = new ArrayList<List<Integer>>();
            for (int k = 0; k < uniqueGroups.size(); k++) {
                foundLocalUniquesB.add(new ArrayList<Integer>());
            }
            for (Integer gid : subsetGlyphs.keySet()) {
                int group = subsetGroups.get(gid);
                localIndexSubr = cffReader.getFDFonts().get(group).getLocalSubrData();
                localUniques = foundLocalUniquesB.get(subsetFDSelect.get(subsetGlyphs.get(gid)).getNewFDIndex());
                byte[] data = charStringsIndex.getValue(gid);
                subsetLocalIndexSubr = fdSubrs.get(subsetFDSelect.get(subsetGlyphs.get(gid)).getNewFDIndex());
                subsetLocalSubrCount = foundLocalUniques.get(subsetFDSelect.get(subsetGlyphs.get(gid)).getNewFDIndex()).size();
                data = readCharStringData(data, subsetLocalSubrCount);
                subsetCharStringsIndex.add(data);
            }
        }
    }

    private void writeFDSelect() {
        writeByte(0); //Format
        for (Integer gid : subsetFDSelect.keySet()) {
            writeByte(subsetFDSelect.get(gid).getNewFDIndex());
        }
    }

    private List<Integer> getUsedFDFonts() {
        List<Integer> uniqueNewRefs = new ArrayList<Integer>();
        for (int gid : subsetFDSelect.keySet()) {
            int fdIndex = subsetFDSelect.get(gid).getOldFDIndex();
            if (!uniqueNewRefs.contains(fdIndex)) {
                uniqueNewRefs.add(fdIndex);
            }
        }
        return uniqueNewRefs;
    }

    private List<Integer> writeCIDDictsAndSubrs(List<Integer> uniqueNewRefs)
            throws IOException {
        List<Integer> privateDictOffsets = new ArrayList<Integer>();
        List<FontDict> fdFonts = cffReader.getFDFonts();
        for (int i = 0; i < uniqueNewRefs.size(); i++) {
            FontDict curFDFont = fdFonts.get(uniqueNewRefs.get(i));
            HashMap<String, DICTEntry> fdPrivateDict = cffReader.parseDictData(
                    curFDFont.getPrivateDictData());
            int privateDictOffset = currentPos;
            privateDictOffsets.add(privateDictOffset);
            byte[] fdPrivateDictByteData = curFDFont.getPrivateDictData();
            if (fdPrivateDict.get("Subrs") != null) {
                fdPrivateDictByteData = updateOffset(fdPrivateDictByteData, fdPrivateDict.get("Subrs").getOffset(),
                        fdPrivateDict.get("Subrs").getOperandLength(),
                        fdPrivateDictByteData.length);
            }
            writeBytes(fdPrivateDictByteData);
            writeIndex(fdSubrs.get(i));
        }
        return privateDictOffsets;
    }

    private int writeFDArray(List<Integer> uniqueNewRefs, List<Integer> privateDictOffsets,
            List<Integer> fontNameSIDs)
            throws IOException {
        int offset = currentPos;
        List<FontDict> fdFonts = cffReader.getFDFonts();

        writeCard16(uniqueNewRefs.size());
        writeByte(1); //Offset size
        writeByte(1); //First offset

        int count = 1;
        for (int i = 0; i < uniqueNewRefs.size(); i++) {
            FontDict fdFont = fdFonts.get(uniqueNewRefs.get(i));
            count += fdFont.getByteData().length;
            writeByte(count);
        }

        for (int i = 0; i < uniqueNewRefs.size(); i++) {
            FontDict fdFont = fdFonts.get(uniqueNewRefs.get(i));
            byte[] fdFontByteData = fdFont.getByteData();
            Map<String, DICTEntry> fdFontDict = cffReader.parseDictData(fdFontByteData);
            //Update the SID to the FontName
            fdFontByteData = updateOffset(fdFontByteData, fdFontDict.get("FontName").getOffset() - 1,
                    fdFontDict.get("FontName").getOperandLengths().get(0),
                    fontNameSIDs.get(i));
            //Update the Private dict reference
            fdFontByteData = updateOffset(fdFontByteData, fdFontDict.get("Private").getOffset()
                    + fdFontDict.get("Private").getOperandLengths().get(0),
                    fdFontDict.get("Private").getOperandLengths().get(1),
                    privateDictOffsets.get(i));
            writeBytes(fdFontByteData);
        }
        return offset;
    }

    private class FDIndexReference {
        private int newFDIndex;
        private int oldFDIndex;

        public FDIndexReference(int newFDIndex, int oldFDIndex) {
            this.newFDIndex = newFDIndex;
            this.oldFDIndex = oldFDIndex;
        }

        public int getNewFDIndex() {
            return newFDIndex;
        }

        public int getOldFDIndex() {
            return oldFDIndex;
        }
    }

    private void createCharStringData() throws IOException {
        Map<String, DICTEntry> topDICT = cffReader.getTopDictEntries();

        CFFIndexData charStringsIndex = cffReader.getCharStringIndex();

        DICTEntry privateEntry = topDICT.get("Private");
        if (privateEntry != null) {
            int privateOffset = privateEntry.getOperands().get(1).intValue();
            Map<String, DICTEntry> privateDICT = cffReader.getPrivateDict(privateEntry);

            if (privateDICT.get("Subrs") != null) {
                int localSubrOffset = privateOffset + privateDICT.get("Subrs").getOperands().get(0).intValue();
                localIndexSubr = cffReader.readIndex(localSubrOffset);
            } else {
                localIndexSubr = cffReader.readIndex(null);
            }
        }

        globalIndexSubr = cffReader.getGlobalIndexSubr();

        //Create the two lists which are to store the local and global subroutines
        subsetLocalIndexSubr = new ArrayList<byte[]>();
        subsetGlobalIndexSubr = new ArrayList<byte[]>();

        //Create the new char string index
        subsetCharStringsIndex = new ArrayList<byte[]>();

        localUniques = new ArrayList<Integer>();
        globalUniques = new ArrayList<Integer>();

        for (int gid : subsetGlyphs.keySet()) {
            byte[] data = charStringsIndex.getValue(gid);
            preScanForSubsetIndexSize(data);
        }

        //Store the size of each subset index and clear the unique arrays
        subsetLocalSubrCount = localUniques.size();
        subsetGlobalSubrCount = globalUniques.size();
        localUniques.clear();
        globalUniques.clear();

        for (int gid : subsetGlyphs.keySet()) {
            byte[] data = charStringsIndex.getValue(gid);
            //Retrieve modified char string data and fill local / global subroutine arrays
            data = readCharStringData(data, subsetLocalSubrCount);
            subsetCharStringsIndex.add(data);
        }
    }

    private void preScanForSubsetIndexSize(byte[] data) throws IOException {
        boolean hasLocalSubroutines = localIndexSubr != null && localIndexSubr.getNumObjects() > 0;
        boolean hasGlobalSubroutines = globalIndexSubr != null && globalIndexSubr.getNumObjects() > 0;
        BytesNumber operand = new BytesNumber(-1, -1);
        for (int dataPos = 0; dataPos < data.length; dataPos++) {
            int b0 = data[dataPos] & 0xff;
            if (b0 == LOCAL_SUBROUTINE && hasLocalSubroutines) {
                int subrNumber = getSubrNumber(localIndexSubr.getNumObjects(), operand.getNumber());

                if (!localUniques.contains(subrNumber) && subrNumber < localIndexSubr.getNumObjects()) {
                    localUniques.add(subrNumber);
                    byte[] subr = localIndexSubr.getValue(subrNumber);
                    preScanForSubsetIndexSize(subr);
                }
                operand.clearNumber();
            } else if (b0 == GLOBAL_SUBROUTINE && hasGlobalSubroutines) {
                int subrNumber = getSubrNumber(globalIndexSubr.getNumObjects(), operand.getNumber());

                if (!globalUniques.contains(subrNumber) && subrNumber < globalIndexSubr.getNumObjects()) {
                    globalUniques.add(subrNumber);
                    byte[] subr = globalIndexSubr.getValue(subrNumber);
                    preScanForSubsetIndexSize(subr);
                }
                operand.clearNumber();
            } else if ((b0 >= 0 && b0 <= 27) || (b0 >= 29 && b0 <= 31)) {
                operand.clearNumber();
                if (b0 == 19 || b0 == 20) {
                    dataPos += 1;
                }
            } else if (b0 == 28 || (b0 >= 32 && b0 <= 255)) {
                operand = readNumber(b0, data, dataPos);
                dataPos += operand.getNumBytes() - 1;
            }
        }
    }

    private int getSubrNumber(int numSubroutines, int operand) {
        int bias = getBias(numSubroutines);
        return bias + operand;
    }

    private byte[] readCharStringData(byte[] data, int subsetLocalSubrCount) throws IOException {
        boolean hasLocalSubroutines = localIndexSubr != null && localIndexSubr.getNumObjects() > 0;
        boolean hasGlobalSubroutines = globalIndexSubr != null && globalIndexSubr.getNumObjects() > 0;
        BytesNumber operand = new BytesNumber(-1, -1);
        for (int dataPos = 0; dataPos < data.length; dataPos++) {
            int b0 = data[dataPos] & 0xff;
            if (b0 == 10 && hasLocalSubroutines) {
                int subrNumber = getSubrNumber(localIndexSubr.getNumObjects(), operand.getNumber());

                int newRef = getNewRefForReference(subrNumber, localUniques, localIndexSubr, subsetLocalIndexSubr,
                        subsetLocalSubrCount);

                if (newRef != -1) {
                    byte[] newData = constructNewRefData(dataPos, data, operand, subsetLocalSubrCount,
                            newRef, new int[] {10});
                    dataPos -= data.length - newData.length;
                    data = newData;
                }

                operand.clearNumber();
            } else if (b0 == 29 && hasGlobalSubroutines) {
                int subrNumber = getSubrNumber(globalIndexSubr.getNumObjects(), operand.getNumber());

                int newRef = getNewRefForReference(subrNumber, globalUniques, globalIndexSubr, subsetGlobalIndexSubr,
                        subsetGlobalSubrCount);

                if (newRef != -1) {
                    byte[] newData = constructNewRefData(dataPos, data, operand, subsetGlobalSubrCount,
                            newRef, new int[] {29});
                    dataPos -= (data.length - newData.length);
                    data = newData;
                }

                operand.clearNumber();
            } else if ((b0 >= 0 && b0 <= 27) || (b0 >= 29 && b0 <= 31)) {
                operand.clearNumber();
                if (b0 == 19 || b0 == 20) {
                    dataPos += 1;
                }
            } else if (b0 == 28 || (b0 >= 32 && b0 <= 255)) {
                operand = readNumber(b0, data, dataPos);
                dataPos += operand.getNumBytes() - 1;
            }
        }

        //Return the data with the modified references to our arrays
        return data;
    }

    private int getNewRefForReference(int subrNumber, List<Integer> uniquesArray,
            CFFIndexData indexSubr, List<byte[]> subsetIndexSubr, int subrCount) throws IOException {
        int newRef = -1;
        if (!uniquesArray.contains(subrNumber)) {
            if (subrNumber < indexSubr.getNumObjects()) {
                byte[] subr = indexSubr.getValue(subrNumber);
                subr = readCharStringData(subr, subrCount);
                if (!uniquesArray.contains(subrNumber)) {
                    uniquesArray.add(subrNumber);
                    subsetIndexSubr.add(subr);
                    newRef = subsetIndexSubr.size() - 1;
                } else {
                    newRef = uniquesArray.indexOf(subrNumber);
                }
            }
        } else {
            newRef = uniquesArray.indexOf(subrNumber);
        }
        return newRef;
    }

    private int getBias(int subrCount) {
        if (subrCount < 1240) {
            return 107;
        } else if (subrCount < 33900) {
            return 1131;
        } else {
            return 32768;
        }
    }

    private byte[] constructNewRefData(int curDataPos, byte[] currentData, BytesNumber operand,
            int fullSubsetIndexSize, int curSubsetIndexSize, int[] operatorCode) {
        //Create the new array with the modified reference
        byte[] newData;
        int startRef = curDataPos - operand.getNumBytes();
        int length = operand.getNumBytes() + 1;
        byte[] preBytes = new byte[startRef];
        System.arraycopy(currentData, 0, preBytes, 0, startRef);
        int newBias = getBias(fullSubsetIndexSize);
        int newRef = curSubsetIndexSize - newBias;
        byte[] newRefBytes = createNewRef(newRef, operatorCode, -1);
        newData = concatArray(preBytes, newRefBytes);
        byte[] postBytes = new byte[currentData.length - (startRef + length)];
        System.arraycopy(currentData, startRef + length, postBytes, 0,
                currentData.length - (startRef + length));
        return concatArray(newData, postBytes);
    }

    public static byte[] createNewRef(int newRef, int[] operatorCode, int forceLength) {
        byte[] newRefBytes;
        int sizeOfOperator = operatorCode.length;
        if ((forceLength == -1 && newRef <= 107) || forceLength == 1) {
            newRefBytes = new byte[1 + sizeOfOperator];
            //The index values are 0 indexed
            newRefBytes[0] = (byte)(newRef + 139);
            for (int i = 0; i < operatorCode.length; i++) {
                newRefBytes[1 + i] = (byte)operatorCode[i];
            }
        } else if ((forceLength == -1 && newRef <= 1131) || forceLength == 2) {
            newRefBytes = new byte[2 + sizeOfOperator];
            if (newRef <= 363) {
                newRefBytes[0] = (byte)247;
            } else if (newRef <= 619) {
                newRefBytes[0] = (byte)248;
            } else if (newRef <= 875) {
                newRefBytes[0] = (byte)249;
            } else {
                newRefBytes[0] = (byte)250;
            }
            newRefBytes[1] = (byte)(newRef - 108);
            for (int i = 0; i < operatorCode.length; i++) {
                newRefBytes[2 + i] = (byte)operatorCode[i];
            }
        } else if ((forceLength == -1 && newRef <= 32767) || forceLength == 3) {
            newRefBytes = new byte[3 + sizeOfOperator];
            newRefBytes[0] = 28;
            newRefBytes[1] = (byte)(newRef >> 8);
            newRefBytes[2] = (byte)newRef;
            for (int i = 0; i < operatorCode.length; i++) {
                newRefBytes[3 + i] = (byte)operatorCode[i];
            }
        } else {
            newRefBytes = new byte[5 + sizeOfOperator];
            newRefBytes[0] = 29;
            newRefBytes[1] = (byte)(newRef >> 24);
            newRefBytes[2] = (byte)(newRef >> 16);
            newRefBytes[3] = (byte)(newRef >> 8);
            newRefBytes[4] = (byte)newRef;
            for (int i = 0; i < operatorCode.length; i++) {
                newRefBytes[5 + i] = (byte)operatorCode[i];
            }
        }
        return newRefBytes;
    }

    public static byte[] concatArray(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    private int writeIndex(List<byte[]> dataArray) {
        int hdrTotal = 3;
        //2 byte number of items
        this.writeCard16(dataArray.size());
        //Offset Size: 1 byte = 256, 2 bytes = 65536 etc.
        int totLength = 0;
        for (int i = 0; i < dataArray.size(); i++) {
            totLength += dataArray.get(i).length;
        }
        int offSize = 1;
        if (totLength <= (1 << 8)) {
            offSize = 1;
        } else if (totLength <= (1 << 16)) {
            offSize = 2;
        } else if (totLength <= (1 << 24)) {
            offSize = 3;
        } else {
            offSize = 4;
        }
        this.writeByte(offSize);
        //Count the first offset 1
        hdrTotal += offSize;
        int total = 0;
        for (int i = 0; i < dataArray.size(); i++) {
            hdrTotal += offSize;
            int length = dataArray.get(i).length;
            switch (offSize) {
            case 1:
                if (i == 0) {
                    writeByte(1);
                }
                total += length;
                writeByte(total + 1);
                break;
            case 2:
                if (i == 0) {
                    writeCard16(1);
                }
                total += length;
                writeCard16(total + 1);
                break;
            case 3:
                if (i == 0) {
                    writeThreeByteNumber(1);
                }
                total += length;
                writeThreeByteNumber(total + 1);
                break;
            case 4:
                if (i == 0) {
                    writeULong(1);
                }
                total += length;
                writeULong(total + 1);
                break;
            default:
                throw new AssertionError("Offset Size was not an expected value.");
            }
        }
        for (int i = 0; i < dataArray.size(); i++) {
            writeBytes(dataArray.get(i));
        }
        return hdrTotal + total;
    }


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
     * A class used to store the last number operand and also it's size in bytes
     */
    private static final class BytesNumber {
        private int number;
        private int numBytes;

        public BytesNumber(int number, int numBytes) {
            this.number = number;
            this.numBytes = numBytes;
        }

        public int getNumber() {
            return this.number;
        }

        public int getNumBytes() {
            return this.numBytes;
        }

        public void clearNumber() {
            this.number = -1;
            this.numBytes = -1;
        }
    }

    private void writeCharsetTable(boolean cidFont) throws IOException {
        writeByte(0);
        for (int gid : gidToSID.keySet()) {
            if (cidFont && gid == 0) {
                continue;
            }
            writeCard16((cidFont) ? gid : gidToSID.get(gid));
        }
    }

    private void writePrivateDict() throws IOException {
        Map<String, DICTEntry> topDICT = cffReader.getTopDictEntries();

        DICTEntry privateEntry = topDICT.get("Private");
        if (privateEntry != null) {
            writeBytes(cffReader.getPrivateDictBytes(privateEntry));
        }
    }

    private void updateOffsets(int topDictOffset, int charsetOffset, int charStringOffset,
            int privateDictOffset, int localIndexOffset, int encodingOffset)
            throws IOException {
        Map<String, DICTEntry> topDICT = cffReader.getTopDictEntries();
        Map<String, DICTEntry> privateDICT = null;

        DICTEntry privateEntry = topDICT.get("Private");
        if (privateEntry != null) {
            privateDICT = cffReader.getPrivateDict(privateEntry);
        }

        int dataPos = 3 + (cffReader.getTopDictIndex().getOffSize()
                * cffReader.getTopDictIndex().getOffsets().length);
        int dataTopDictOffset = topDictOffset + dataPos;

        updateFixedOffsets(topDICT, dataTopDictOffset, charsetOffset, charStringOffset, encodingOffset);

        if (privateDICT != null) {
            //Private index offset in the top dict
            int oldPrivateOffset = dataTopDictOffset + privateEntry.getOffset();
            output = updateOffset(output, oldPrivateOffset + privateEntry.getOperandLengths().get(0),
                    privateEntry.getOperandLengths().get(1), privateDictOffset);

            //Update the local subroutine index offset in the private dict
            DICTEntry subroutines = privateDICT.get("Subrs");
            int oldLocalSubrOffset = privateDictOffset + subroutines.getOffset();
            //Value needs to be converted to -139 etc.
            int encodeValue = 0;
            if (subroutines.getOperandLength() == 1) {
                encodeValue = 139;
            }
            output = updateOffset(output, oldLocalSubrOffset, subroutines.getOperandLength(),
                    (localIndexOffset - privateDictOffset) + encodeValue);
        }
    }

    private void updateFixedOffsets(Map<String, DICTEntry> topDICT, int dataTopDictOffset,
            int charsetOffset, int charStringOffset, int encodingOffset) {
        //Charset offset in the top dict
        DICTEntry charset = topDICT.get("charset");
        int oldCharsetOffset = dataTopDictOffset + charset.getOffset();
        output = updateOffset(output, oldCharsetOffset, charset.getOperandLength(), charsetOffset);

        //Char string index offset in the private dict
        DICTEntry charString = topDICT.get("CharStrings");
        int oldCharStringOffset = dataTopDictOffset + charString.getOffset();
        output = updateOffset(output, oldCharStringOffset, charString.getOperandLength(), charStringOffset);

        DICTEntry encodingEntry = topDICT.get("Encoding");
        if (encodingEntry != null && encodingEntry.getOperands().get(0).intValue() != 0
                && encodingEntry.getOperands().get(0).intValue() != 1) {
            int oldEncodingOffset = dataTopDictOffset + encodingEntry.getOffset();
            output = updateOffset(output, oldEncodingOffset, encodingEntry.getOperandLength(), encodingOffset);
        }
    }

    private void updateCIDOffsets(int topDictDataOffset, int fdArrayOffset, int fdSelectOffset,
            int charsetOffset, int charStringOffset, int encodingOffset) {
        LinkedHashMap<String, DICTEntry> topDict = cffReader.getTopDictEntries();

        DICTEntry fdArrayEntry = topDict.get("FDArray");
        if (fdArrayEntry != null) {
            output = updateOffset(output, topDictDataOffset + fdArrayEntry.getOffset() - 1,
                    fdArrayEntry.getOperandLength(), fdArrayOffset);
        }

        DICTEntry fdSelect = topDict.get("FDSelect");
        if (fdSelect != null) {
            output = updateOffset(output, topDictDataOffset + fdSelect.getOffset() - 1,
                    fdSelect.getOperandLength(), fdSelectOffset);
        }

        updateFixedOffsets(topDict, topDictDataOffset, charsetOffset, charStringOffset, encodingOffset);
    }

    private byte[] updateOffset(byte[] out, int position, int length, int replacement) {
        switch (length) {
        case 1:
            out[position] = (byte)(replacement & 0xFF);
            break;
        case 2:
            if (replacement <= 363) {
                out[position] = (byte)247;
            } else if (replacement <= 619) {
                out[position] = (byte)248;
            } else if (replacement <= 875) {
                out[position] = (byte)249;
            } else {
                out[position] = (byte)250;
            }
            out[position + 1] = (byte)(replacement - 108);
            break;
        case 3:
            out[position] = (byte)28;
            out[position + 1] = (byte)((replacement >> 8) & 0xFF);
            out[position + 2] = (byte)(replacement & 0xFF);
            break;
        case 5:
            out[position] = (byte)29;
            out[position + 1] = (byte)((replacement >> 24) & 0xFF);
            out[position + 2] = (byte)((replacement >> 16) & 0xFF);
            out[position + 3] = (byte)((replacement >> 8) & 0xFF);
            out[position + 4] = (byte)(replacement & 0xFF);
            break;
        default:
        }
        return out;
    }

    /**
     * Appends a byte to the output array,
     * updates currentPost but not realSize
     */
    private void writeByte(int b) {
        output[currentPos++] = (byte)b;
        realSize++;
    }

    /**
     * Appends a USHORT to the output array,
     * updates currentPost but not realSize
     */
    private void writeCard16(int s) {
        byte b1 = (byte)((s >> 8) & 0xff);
        byte b2 = (byte)(s & 0xff);
        writeByte(b1);
        writeByte(b2);
    }

    private void writeThreeByteNumber(int s) {
        byte b1 = (byte)((s >> 16) & 0xFF);
        byte b2 = (byte)((s >> 8) & 0xFF);
        byte b3 = (byte)(s & 0xFF);
        output[currentPos++] = b1;
        output[currentPos++] = b2;
        output[currentPos++] = b3;
        realSize += 3;
    }

    /**
     * Appends a ULONG to the output array,
     * at the given position
     */
    private void writeULong(int s) {
        byte b1 = (byte)((s >> 24) & 0xff);
        byte b2 = (byte)((s >> 16) & 0xff);
        byte b3 = (byte)((s >> 8) & 0xff);
        byte b4 = (byte)(s & 0xff);
        output[currentPos++] = b1;
        output[currentPos++] = b2;
        output[currentPos++] = b3;
        output[currentPos++] = b4;
        realSize += 4;
    }

    /**
     * Returns a subset of the fonts (readFont() MUST be called first in order to create the
     * subset).
     * @return byte array
     */
    public byte[] getFontSubset() {
        byte[] ret = new byte[realSize];
        System.arraycopy(output, 0, ret, 0, realSize);
        return ret;
    }
}
