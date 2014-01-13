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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.apache.commons.io.output.CountingOutputStream;


/**
 * Test case for {@link PDFDictionary}.
 */
public class PDFDictionaryTestCase extends PDFObjectTestCase {
    /** The test subject */
    private PDFDictionary pdfDictUnderTest;
    private PDFArray testArray;
    private PDFNumber testNumber;
    /** The order in which these objects are put into the dictionary MUST be maintained. */
    private String expectedOutput = "<<\n"
                                  + "  /String (TestValue)\n"
                                  + "  /int 10\n"
                                  + "  /double 3.1\n"
                                  + "  /array [1 (two) 20]\n"
                                  + "  /number 20\n"
                                  + "  /null null\n"
                                  + ">>";

    @Before
    public void setUp() {
        // A PDFNumber for testing, this DOES have a parent
        testNumber = new PDFNumber();
        testNumber.setParent(parent);
        testNumber.setNumber(20);
        // An array for testing, this DOES NOT have a parent
        testArray = new PDFArray();
        testArray.add(1);
        testArray.add("two");
        testArray.add(testNumber);
        // Populating the dictionary with a parent, document and the various objects
        pdfDictUnderTest = new PDFDictionary(parent);
        pdfDictUnderTest.setDocument(doc);
        pdfDictUnderTest.put("String", "TestValue");
        pdfDictUnderTest.put("int", 10);
        pdfDictUnderTest.put("double", Double.valueOf(3.1));
        pdfDictUnderTest.put("array", testArray);
        pdfDictUnderTest.put("number", testNumber);
        // null is a valid PDF object
        pdfDictUnderTest.put("null", null);
        // test that the interface is maintained
        pdfObjectUnderTest = pdfDictUnderTest;
    }

    /**
     * Tests put() - tests that the object is put into the dictionary and it is handled if it is a
     * {@link PDFObject}.
     */
    @Test
    public void testPut() {
        // The "put()" commands have already been done in setUp(), so just test them.
        assertEquals("TestValue", pdfDictUnderTest.get("String"));
        assertEquals(10, pdfDictUnderTest.get("int"));
        assertEquals(3.1, pdfDictUnderTest.get("double"));
        // With PDFObjects, if they DO NOT have a parent, the dict becomes their parent.
        assertEquals(testArray, pdfDictUnderTest.get("array"));
        assertEquals(pdfDictUnderTest, testArray.getParent());
        // With PDFObjects, if they DO have a parent, the dict DOES NOT change the parent object.
        assertEquals(testNumber, pdfDictUnderTest.get("number"));
        // Test it doesn't explode when we try to get a non-existent entry
        assertNull(pdfDictUnderTest.get("Not in dictionary"));
        // Tests that we can over-write objects
        pdfDictUnderTest.put("array", 10);
        assertEquals(10, pdfDictUnderTest.get("array"));
        // Test that nulls are handled appropriately
        assertNull(pdfDictUnderTest.get("null"));
    }

    /**
     * Tests get() - tests that objects can be properly retrieved from the dictionary.
     */
    @Test
    public void testGet() {
        // Tested fairly comprehensively in testPut().
    }

    /**
     * Tests writeDictionary() - tests that the dictionary is properly written to the output-stream.
     */
    @Test
    public void testWriteDictionary() {
        // Ensure that the objects stored in the dictionary are streamed in the correct format.
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        CountingOutputStream cout = new CountingOutputStream(outStream);
        StringBuilder textBuffer = new StringBuilder();
        try {
            pdfDictUnderTest.writeDictionary(cout, textBuffer);
            PDFDocument.flushTextBuffer(textBuffer, cout);
            assertEquals(expectedOutput, outStream.toString());
        } catch (IOException e) {
            fail("IOException: " + e.getMessage());
        }
    }

    /**
     * Tests output() - test that this object can write itself to an output stream.
     * @throws IOException error caused by I/O
     */
    @Test
    public void testOutput() throws IOException {
        testOutputStreams(expectedOutput, pdfDictUnderTest);
    }
}
