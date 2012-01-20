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

package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.output.CountingOutputStream;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link PDFName}.
 */
public class PDFNameTestCase extends PDFObjectTestCase {
    private PDFName pdfName;

    /**
     * Sets up the local variables
     */
    @Before
    public void setUp() {
        pdfName = new PDFName("TestName");
        pdfName.setParent(parent);
        pdfName.setDocument(doc);

        pdfObjectUnderTest = pdfName;
    }

    /**
     * Tests escapeName() - tests that this method escapes the necessary characters.
     */
    @Test
    public void testEscapeName() {
        try {
            // Test for null, this is a programming error thus the NPE
            PDFName.escapeName(null);
            fail("NPE not thrown when null object given to escapeName()");
        } catch (NullPointerException e) {
            // PASS
        }
        // All names are prefixed by "/", check the PDF spec for further details.
        assertEquals("/Test", PDFName.escapeName("Test"));
        // Check that if the name is already prefixed with "/" it doens't do it twice
        assertEquals("/Test", PDFName.escapeName("/Test"));
        // Test with a space in the middle
        assertEquals("/Test#20test", PDFName.escapeName("Test test"));
        // Test that all chars apart from ASCII '!' --> '~' are escaped
        nonEscapedCharactersTests();
        escapedCharactersTests();
    }

    private void escapedCharactersTests() {
        for (char i = 0; i < '!'; i++) {
            String str = Integer.toHexString(i >>> 4 & 0x0f).toUpperCase();
            str += Integer.toHexString(i & 0x0f).toUpperCase();
            assertEquals("/#" + str, PDFName.escapeName(String.valueOf(i)));
        }
        for (char i = '~' + 1; i < 256; i++) {
            String str = Integer.toHexString(i >>> 4 & 0x0f).toUpperCase();
            str += Integer.toHexString(i & 0x0f).toUpperCase();
            assertEquals("/#" + str, PDFName.escapeName(String.valueOf(i)));
        }
        checkCharacterIsEscaped('#');
        checkCharacterIsEscaped('%');
        checkCharacterIsEscaped('(');
        checkCharacterIsEscaped(')');
        checkCharacterIsEscaped('<');
        checkCharacterIsEscaped('>');
        checkCharacterIsEscaped('[');
        checkCharacterIsEscaped(']');
        checkCharacterIsEscaped('>');
    }

    private void checkCharacterIsEscaped(char c) {
        String str = Integer.toHexString(c >>> 4 & 0x0f).toUpperCase();
        str += Integer.toHexString(c & 0x0f).toUpperCase();
        assertEquals("/#" + str, PDFName.escapeName(String.valueOf(c)));
    }

    private void nonEscapedCharactersTests() {
        charactersNotEscapedBetween('!', '"');
        charactersNotEscapedBetween('*', ';');
        charactersNotEscapedBetween('?', 'Z');
        charactersNotEscapedBetween('^', '~');
    }

    private void charactersNotEscapedBetween(char c1, char c2) {
        for (char i = c1; i <= c2; i++) {
            String str = String.valueOf(i);
            String expected = !str.equals("/") ? "/" + str : str;
            assertEquals(expected, PDFName.escapeName(str));
        }
    }

    /**
     * Tests toString() - this has been overridden to return the String that PDFName wraps.
     */
    @Test
    public void testToString() {
        // The escape characters have already been tested in testEscapeName() so this doesn't need
        // to be done twice.
        PDFName test1 = new PDFName("test1");
        assertEquals("/test1", test1.toString());
        PDFName test2 = new PDFName("another test");
        assertEquals("/another#20test", test2.toString());
        try {
            new PDFName(null);
            fail("NPE not thrown when null passed to constructor");
        } catch (NullPointerException e) {
            // PASS
        }
    }

    /**
     * Tests output() - check that this object can stream itself in the correct format.
     * @throws IOException error caused by I/O
     */
    @Test
    public void testOutput() throws IOException {
        testOutputStreams("/TestName", pdfName);
        testOutputStreams("/test#20test", new PDFName("test test"));
    }

    /**
     * Test outputInline() - this writes the object reference if it is a direct object (has an
     * object number), or writes the String representation if there is no object number.
     */
    @Test
    public void testOutputInline() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        CountingOutputStream cout = new CountingOutputStream(outStream);
        StringBuilder textBuffer = new StringBuilder();
        try {
            // test with no object number set.
            pdfName.outputInline(outStream, textBuffer);
            PDFDocument.flushTextBuffer(textBuffer, cout);
            assertEquals("/TestName", outStream.toString());

            outStream.reset();
            // test with object number set
            pdfName.setObjectNumber(1);
            pdfName.outputInline(outStream, textBuffer);
            PDFDocument.flushTextBuffer(textBuffer, cout);
            assertEquals("1 0 R", outStream.toString());
        } catch (IOException e) {
            fail("IOException: " + e.getMessage());
        }
    }
}
