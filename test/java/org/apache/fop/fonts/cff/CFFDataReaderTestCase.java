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

package org.apache.fop.fonts.cff;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import org.apache.fontbox.cff.CFFDataInput;

import org.apache.fop.fonts.cff.CFFDataReader.CFFIndexData;
import org.apache.fop.fonts.cff.CFFDataReader.DICTEntry;
import org.apache.fop.fonts.truetype.OTFSubSetFile;

import static org.junit.Assert.assertEquals;

public class CFFDataReaderTestCase {
    private CFFDataReader cffReader;

    /**
     * Initializes the CFFDataReader for testing purposes
     */
    @Before
    public void setUp() {
        cffReader = new CFFDataReader();
    }

    /**
     * Parses a test dictionary to verify whether the stored data is read correctly.
     * @throws IOException
     */
    @Test
    public void parseDictData() throws IOException {
        byte[] testDictData = prepareDictData();
        Map<String, DICTEntry> testTopDict = cffReader.parseDictData(testDictData);
        validateDictData(testTopDict);
    }

    private byte[] prepareDictData() {
        byte[] testDictData = new byte[0];
        //Version
        testDictData = OTFSubSetFile.concatArray(testDictData, OTFSubSetFile.createNewRef(
                392, new int[] { 0 }, -1));
        //Notice
        testDictData = OTFSubSetFile.concatArray(testDictData, OTFSubSetFile.createNewRef(
                393, new int[] { 1 }, -1));
        //Copyright
        testDictData = OTFSubSetFile.concatArray(testDictData, OTFSubSetFile.createNewRef(
                394, new int[] { 12, 0 }, -1));
        //FullName
        testDictData = OTFSubSetFile.concatArray(testDictData, OTFSubSetFile.createNewRef(
                395, new int[] { 2 }, -1));
        //FamilyName
        testDictData = OTFSubSetFile.concatArray(testDictData, OTFSubSetFile.createNewRef(
                396, new int[] { 3 }, -1));
        //Weight
        testDictData = OTFSubSetFile.concatArray(testDictData, OTFSubSetFile.createNewRef(
                397, new int[] { 4 }, -1));
        //isFixedPitch (boolean = false)
        testDictData = OTFSubSetFile.concatArray(testDictData, OTFSubSetFile.createNewRef(
                0, new int[] { 12, 1 }, -1));
        //FontBBox
        testDictData = OTFSubSetFile.concatArray(testDictData, OTFSubSetFile.createNewRef(
                -50, new int[0], -1));
        testDictData = OTFSubSetFile.concatArray(testDictData, OTFSubSetFile.createNewRef(
                -40, new int[0], -1));
        testDictData = OTFSubSetFile.concatArray(testDictData, OTFSubSetFile.createNewRef(
                100, new int[0], -1));
        testDictData = OTFSubSetFile.concatArray(testDictData, OTFSubSetFile.createNewRef(
                120, new int[] { 5 }, -1));
        //charset
        testDictData = OTFSubSetFile.concatArray(testDictData, OTFSubSetFile.createNewRef(
                1234, new int[] { 15 }, -1));
        //CharStrings
        testDictData = OTFSubSetFile.concatArray(testDictData, OTFSubSetFile.createNewRef(
                3654, new int[] { 17 }, -1));
        //Private
        testDictData = OTFSubSetFile.concatArray(testDictData, OTFSubSetFile.createNewRef(
                11454, new int[] { 18 }, -1));
        return testDictData;
    }

    private void validateDictData(Map<String, DICTEntry> dictMap) {
        //SID Values (numbers)
        assertEquals(dictMap.get("version").getOperands().get(0).intValue(), 392);
        assertEquals(dictMap.get("Notice").getOperands().get(0).intValue(), 393);
        assertEquals(dictMap.get("Copyright").getOperands().get(0).intValue(), 394);
        assertEquals(dictMap.get("FullName").getOperands().get(0).intValue(), 395);
        assertEquals(dictMap.get("FamilyName").getOperands().get(0).intValue(), 396);
        assertEquals(dictMap.get("Weight").getOperands().get(0).intValue(), 397);
        //Boolean comparison
        assertEquals(dictMap.get("isFixedPitch").getOperands().get(0).intValue(), 0);
        //Array comparison
        int[] fontBBox = { -50, -40, 100, 120 };
        DICTEntry fontBBoxEntry = dictMap.get("FontBBox");
        for (int i = 0;i < fontBBoxEntry.getOperands().size();i++) {
            assertEquals(fontBBoxEntry.getOperands().get(i).intValue(), fontBBox[i]);
        }
        //Multi-byte offset (number)
        assertEquals(dictMap.get("charset").getOperands().get(0).intValue(), 1234);
        assertEquals(dictMap.get("CharStrings").getOperands().get(0).intValue(), 3654);
        //Larger offset
        assertEquals(dictMap.get("Private").getOperands().get(0).intValue(), 11454);
    }

    /**
     * Tests the parsing of an example byte data index structure
     * @throws IOException
     */
    @Test
    public void testIndexParsing() throws IOException {
        byte[] testIndex = {
                0, 5,   //Number of objects
                1,      //Offset size
                1,      //Offsets...
                5,
                12,
                24,
                27,
                32
        };
        Random randGen = new Random();
        byte[] data = new byte[31];
        for (int i = 0;i < data.length;i++) {
            data[i] = (byte)randGen.nextInt(255);
        }
        testIndex = OTFSubSetFile.concatArray(testIndex, data);
        CFFIndexData indexData = cffReader.readIndex(new CFFDataInput(testIndex));
        assertEquals(indexData.getNumObjects(), 5);
        assertEquals(indexData.getOffSize(), 1);
        assertEquals(indexData.getOffsets().length, 6);
        assertEquals(indexData.getOffsets()[5], 32);
    }
}
