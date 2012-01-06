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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test case for {@link PDFArray}.
 */
public class PDFArrayTestCase extends PDFObjectTestCase {
    private PDFArray intArray;
    private String intArrayOutput;
    private PDFArray doubleArray;
    private String doubleArrayOutput;
    private PDFArray collectionArray;
    private String collectionArrayOutput;
    private PDFArray objArray;
    private String objArrayOutput;

    /** A PDF object used solely for testing */
    private PDFNumber num;

    @Before
    public void setUp() {
        intArray = new PDFArray(parent, new int[] {1, 2, 3, 4, 5});
        intArrayOutput = "[1 2 3 4 5]";

        doubleArray = new PDFArray(parent, new double[] {1.1, 2.2, 3.3, 4.4, 5.5});
        doubleArrayOutput = "[1.1 2.2 3.3 4.4 5.5]";

        List<Object> strList = new ArrayList<Object>();
        strList.add("one");
        strList.add("two");
        strList.add("three");
        collectionArray = new PDFArray(parent, strList);
        collectionArrayOutput = "[(one) (two) (three)]";

        // Set arbitrary values here
        num = new PDFNumber();
        num.setNumber(20);
        num.setObjectNumber(4);
        objArray = new PDFArray(parent, new Object[] {"one", 2, 3.0f, num});
        objArrayOutput = "[(one) 2 3 4 0 R]";

        // set the document
        intArray.setDocument(doc);
        doubleArray.setDocument(doc);
        collectionArray.setDocument(doc);
        objArray.setDocument(doc);

        // Test the progenitor in the inheritance stack
        objArray.setParent(parent);
        pdfObjectUnderTest = objArray;
    }

    private void intArrayContainsTests() {
        for (int i = 1; i <= 5; i++) {
            assertTrue(intArray.contains(i));
        }
        assertFalse(intArray.contains(6));
        assertFalse(intArray.contains(0));
    }

    private void doubleArrayContainsTests() {
        assertTrue(doubleArray.contains(1.1));
        assertTrue(doubleArray.contains(2.2));
        assertTrue(doubleArray.contains(3.3));
        assertTrue(doubleArray.contains(4.4));
        assertTrue(doubleArray.contains(5.5));
        assertFalse(doubleArray.contains(10.0));
        assertFalse(doubleArray.contains(0.0));
    }

    private void collectionArrayContainsTests() {
        assertTrue(collectionArray.contains("one"));
        assertTrue(collectionArray.contains("two"));
        assertTrue(collectionArray.contains("three"));
        assertFalse(collectionArray.contains("zero"));
        assertFalse(collectionArray.contains("four"));
    }

    private void objectArrayContainsTests() {
        assertTrue(objArray.contains("one"));
        assertTrue(objArray.contains(2));
        assertTrue(objArray.contains(3.0f));
        assertTrue(objArray.contains(num));
        assertFalse(objArray.contains("four"));
        assertFalse(objArray.contains(0.0));
    }

    /**
     * Test contains() - test whether this PDFArray contains an object.
     */
    @Test
    public void testContains() {
        // Test some arbitrary values
        intArrayContainsTests();
        doubleArrayContainsTests();
        collectionArrayContainsTests();
        objectArrayContainsTests();
    }

    /**
     * Test length() - tests the length of an array.
     */
    @Test
    public void testLength() {
        assertEquals(5, intArray.length());
        assertEquals(5, doubleArray.length());
        assertEquals(3, collectionArray.length());
        assertEquals(4, objArray.length());

        // Test the count is incremented when an object is added (this only
        // needs to be tested once)
        intArray.add(6);
        assertEquals(6, intArray.length());
    }

    /**
     * Test set() - tests that a particular point has been properly set.
     */
    @Test
    public void testSet() {
        PDFName name = new PDFName("zero test");
        objArray.set(0, name);
        assertEquals(name, objArray.get(0));

        objArray.set(1, "test");
        assertEquals("test", objArray.get(1));
        // This goes through the set(int, double) code path rather than set(int, Object)
        objArray.set(2, 5);
        assertEquals(5.0, objArray.get(2));
        try {
            objArray.set(4, 2);
            fail("out of bounds");
        } catch (IndexOutOfBoundsException e) {
            // Pass
        }
    }

    /**
     * Test get() - gets the object stored at a given index.
     */
    @Test
    public void testGet() {
        // Test some arbitrary values
        for (int i = 1; i <= 5; i++) {
            assertEquals(i, intArray.get(i - 1));
        }

        assertEquals(1.1, doubleArray.get(0));
        assertEquals(2.2, doubleArray.get(1));
        assertEquals(3.3, doubleArray.get(2));
        assertEquals(4.4, doubleArray.get(3));
        assertEquals(5.5, doubleArray.get(4));

        assertEquals("one", collectionArray.get(0));
        assertEquals("two", collectionArray.get(1));
        assertEquals("three", collectionArray.get(2));

        assertEquals("one", objArray.get(0));
        assertEquals(2, objArray.get(1));
        assertEquals(0, Double.compare(3.0, (Float) objArray.get(2)));
        assertEquals(num, objArray.get(3));
    }

    /**
     * Tests add() - tests that objects are appended to the end of the array as expected.
     */
    @Test
    public void testAdd() {
        intArray.add(new Integer(6));
        doubleArray.add(6.6);
        // Test some arbitrary values
        for (int i = 1; i <= 6; i++) {
            assertEquals(i, intArray.get(i - 1));
        }

        assertEquals(1.1, doubleArray.get(0));
        assertEquals(2.2, doubleArray.get(1));
        assertEquals(3.3, doubleArray.get(2));
        assertEquals(4.4, doubleArray.get(3));
        assertEquals(5.5, doubleArray.get(4));
        assertEquals(6.6, doubleArray.get(5));

        collectionArray.add(1);
        assertEquals("one", collectionArray.get(0));
        assertEquals("two", collectionArray.get(1));
        assertEquals("three", collectionArray.get(2));
        assertEquals(1.0, collectionArray.get(3));

        objArray.add("four");
        assertEquals("one", objArray.get(0));
        assertEquals(2, objArray.get(1));
        assertEquals(0, Double.compare(3.0, (Float) objArray.get(2)));
        assertEquals("four", objArray.get(4));
    }

    /**
     * Tests output() - tests that this object is properly streamed to the PDF document.
     * @throws IOException error caused by I/O
     */
    @Test
    public void testOutput() throws IOException {
        testOutputStreams(intArrayOutput, intArray);
        testOutputStreams(doubleArrayOutput, doubleArray);
        testOutputStreams(collectionArrayOutput, collectionArray);
        testOutputStreams(objArrayOutput, objArray);
    }
}
