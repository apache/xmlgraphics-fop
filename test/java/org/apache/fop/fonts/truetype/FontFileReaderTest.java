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
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * A test class for org.apache.fop.truetype.FontFileReader
 */
public class FontFileReaderTest extends TestCase {
    private FontFileReader fontReader;
    private final InputStream in;
    private final byte[] byteArray;

    /**
     * Constructor - initialises an array that only needs to be created once. It creates a byte[]
     * of form { 0x00, 0x01, 0x02, 0x03..., 0xff};
     */
    public FontFileReaderTest() {
        byteArray = new byte[256];
        for (int i = 0; i < 256; i++) {
            byteArray[i] = (byte) i;
        }
        in = new ByteArrayInputStream(byteArray);
    }

    /**
     * sets up the test subject object for testing.
     */
    public void setUp() {
        try {
            fontReader = new FontFileReader(in);
        } catch (Exception e) {
            fail("Error: " + e.getMessage());
        }
    }

    /**
     * the "destructor" method.
     *
     */
    public void tearDown() {
        fontReader = null;
    }

    /**
     * Test readTTFByte()
     * @throws IOException exception
     */
    public void testReadTTFByte() throws IOException {
        for (int i = 0; i < 256; i++) {
            assertEquals((byte) i, fontReader.readTTFByte());
        }
    }

    /**
     * Test seekSet() - check that it moves to the correct position and enforce a failure case.
     * @throws IOException exception
     */
    public void testSeekSet() throws IOException {
        fontReader.seekSet(10);
        assertEquals(10, fontReader.readTTFByte());
        try {
            fontReader.seekSet(257);
            fail("FileFontReaderTest Failed testSeekSet");
        } catch (IOException e) {
            // Passed
        }
    }

    /**
     * Test skip() - check that it moves to the correct position and enforce a failure case.
     * @throws IOException exception
     */
    public void testSkip() throws IOException {
        fontReader.skip(100);
        assertEquals(100, fontReader.readTTFByte());
        try {
            // 100 (seekAdd) + 1 (read() = 1 byte) + 156 = 257
            fontReader.skip(156);
            fail("FileFontReaderTest Failed testSkip");
        } catch (IOException e) {
            // Passed
        }
    }

    /**
     * Test getCurrentPos() - 3 checks:
     * 1) test with seekSet(int)
     * 2) test with skip(int)
     * 3) test with a readTTFByte() (this moves the position by the size of the data being read)
     * @throws IOException exception
     */
    public void testGetCurrentPos() throws IOException {
        fontReader.seekSet(10);
        fontReader.skip(100);
        assertEquals(110, fontReader.getCurrentPos());
        fontReader.readTTFByte();
        assertEquals(111, fontReader.getCurrentPos());
    }

    /**
     * Test getFileSize()
     */
    public void testGetFileSize() {
        assertEquals(256, fontReader.getFileSize());
    }

    /**
     * Test readTTFUByte()
     * @throws IOException exception
     */
    public void testReadTTFUByte() throws IOException {
        for (int i = 0; i < 256; i++) {
            assertEquals(i, fontReader.readTTFUByte());
        }
    }

    /**
     * Test readTTFShort() - Test positive and negative numbers (two's compliment).
     * @throws IOException exception
     */
    public void testReadTTFShort() throws IOException {
        // 0x0001 = 1
        assertEquals("Should have been 1 (0x0001)", 1, fontReader.readTTFShort());
        // 0x0203 = 515
        assertEquals(515, fontReader.readTTFShort());
        // now test negative numbers
        fontReader.seekSet(250);
        // 0xfafb
        assertEquals(-1285, fontReader.readTTFShort());
    }

    /**
     * Test readTTFUShort() - Test positive and potentially negative numbers (two's compliment).
     * @throws IOException exception
     */
    public void testReadTTFUShort() throws IOException {
        // 0x0001
        assertEquals(1, fontReader.readTTFUShort());
        // 0x0203
        assertEquals(515, fontReader.readTTFUShort());
        // test potential negatives
        fontReader.seekSet(250);
        // 0xfafb
        assertEquals((250 << 8)  + 251, fontReader.readTTFUShort());
    }

    /**
     * Test readTTFShort(int) - test reading ahead of current position and behind current position
     * and in both cases ensure that our current position isn't changed.
     * @throws IOException exception
     */
    public void testReadTTFShortWithArg() throws IOException {
        // 0x6465
        assertEquals(25701, fontReader.readTTFShort(100));
        assertEquals(0, fontReader.getCurrentPos());
        // read behind current position (and negative)
        fontReader.seekSet(255);
        // 0xfafb
        assertEquals(-1285, fontReader.readTTFShort(250));
        assertEquals(255, fontReader.getCurrentPos());
    }

    /**
     * Test readTTFUShort(int arg) - test reading ahead of current position and behind current
     * position and in both cases ensure that our current position isn't changed.
     * @throws IOException exception
     */
    public void testReadTTFUShortWithArg() throws IOException {
        // 0x6465
        assertEquals(25701, fontReader.readTTFUShort(100));
        assertEquals(0, fontReader.getCurrentPos());
        // read behind current position (and potential negative)
        fontReader.seekSet(255);
        // 0xfafb
        assertEquals(64251, fontReader.readTTFUShort(250));
        assertEquals(255, fontReader.getCurrentPos());
    }

    /**
     * Test readTTFLong()
     * @throws IOException exception
     */
    public void testReadTTFLong() throws IOException {
        // 0x00010203
        assertEquals(66051, fontReader.readTTFLong());
        // test negative numbers
        fontReader.seekSet(250);
        // 0xf0f1f2f3
        assertEquals(-84148995, fontReader.readTTFLong());
    }

    /**
     * Test readTTFULong()
     * @throws IOException exception
     */
    public void testReadTTFULong() throws IOException {
        // 0x00010203
        assertEquals(66051, fontReader.readTTFULong());
        // test negative numbers
        fontReader.seekSet(250);
        // 0xfafbfcfd
        assertEquals(4210818301L, fontReader.readTTFULong());
    }

    /**
     * Test readTTFString() - there are two paths to test here:
     * 1) A null terminated string
     * 2) A string not terminated with a null (we expect this to throw an EOFException)
     * @throws IOException exception
     */
    public void testReadTTFString() throws IOException {
        byte[] strByte = {(byte)'t', (byte)'e', (byte)'s', (byte)'t', 0x00};
        fontReader = new FontFileReader(new ByteArrayInputStream(strByte));
        assertEquals("test", fontReader.readTTFString());
        try {
            // not NUL terminated
            byte[] strByteNoNull = {(byte)'t', (byte)'e', (byte)'s', (byte)'t'};
            fontReader = new FontFileReader(new ByteArrayInputStream(strByteNoNull));
            assertEquals("test", fontReader.readTTFString());
            fail("FontFileReaderTest testReadTTFString Fails.");
        } catch (EOFException e) {
            // Pass
        }
    }

    /**
     * Test readTTFString(int arg)
     * @throws IOException exception
     */
    public void testReadTTFStringIntArg() throws IOException {
        byte[] strByte = {(byte)'t', (byte)'e', (byte)'s', (byte)'t'};
        fontReader = new FontFileReader(new ByteArrayInputStream(strByte));
        assertEquals("test", fontReader.readTTFString(4));
        try {
            fontReader = new FontFileReader(new ByteArrayInputStream(strByte));
            assertEquals("test", fontReader.readTTFString(5));
            fail("FontFileReaderTest testReadTTFStringIntArg Fails.");
        } catch (EOFException e) {
            // Pass
        }
    }

    /**
     * Test readTTFString(int arg1, int arg2)
     */
    public void testReadTTFString2IntArgs() {
        // currently the same as above
    }

    /**
     * Test getBytes()
     * @throws IOException exception
     */
    public void testGetBytes() throws IOException {
        byte[] retrievedBytes = fontReader.getBytes(0, 256);
        assertTrue(Arrays.equals(byteArray, retrievedBytes));
    }
}
