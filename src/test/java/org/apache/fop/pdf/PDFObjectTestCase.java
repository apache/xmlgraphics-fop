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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the PDFObject class.
 */
public class PDFObjectTestCase {
    /** The document behind this object */
    protected final PDFDocument doc = new PDFDocument("test");
    /** The parent of this object */
    protected final PDFObject parent = new DummyPDFObject();
    /** The test subject */
    protected PDFObject pdfObjectUnderTest;

    private static class DummyPDFObject extends PDFObject {

    };

    @Before
    public void setUp() {
        pdfObjectUnderTest = new DummyPDFObject();
        pdfObjectUnderTest.setDocument(doc);
        pdfObjectUnderTest.setParent(parent);
    }

    /**
     * Tests setObjectNumber()
     */
    @Test
    public void testSetObjectNumber() {
        pdfObjectUnderTest.setObjectNumber(1);
        assertEquals(1, pdfObjectUnderTest.getObjectNumber().getNumber());

        pdfObjectUnderTest.setObjectNumber(5);
        assertEquals(5, pdfObjectUnderTest.getObjectNumber().getNumber());
    }

    /**
     * Tests hasObjectNumber() - returns the object number of the underlying PDF object.
     */
    @Test
    public void testHasObjectNumber() {
        assertFalse(pdfObjectUnderTest.hasObjectNumber());

        pdfObjectUnderTest.setObjectNumber(1);
        assertTrue(pdfObjectUnderTest.hasObjectNumber());
    }

    /**
     * Tests getGeneration() - returns the generation number of the underlying PDF object.
     */
    @Test
    public void testGetGeneration() {
        // Default should be 0
        assertEquals(0, pdfObjectUnderTest.getGeneration());
        // apparently there is no way to set this to anything other than 0
    }

    /**
     * Tests setDocument() - returns the document to which this object is bound.
     */
    @Test
    public void testSetDocument() {
        assertEquals(doc, pdfObjectUnderTest.getDocument());
        // assign a different document to the object and test (this should be immutable but isn't)
        PDFDocument anotherDoc = new PDFDocument("another test");
        pdfObjectUnderTest.setDocument(anotherDoc);
        assertEquals(anotherDoc, pdfObjectUnderTest.getDocument());
    }

    /**
     * Tests setParent() - assigns the object a parent.
     */
    @Test
    public void testSetParent() {
        assertEquals(parent, pdfObjectUnderTest.getParent());
        // assign another parent (this probably shouldn't me mutable)
        DummyPDFObject anotherParent = new DummyPDFObject();
        pdfObjectUnderTest.setParent(anotherParent);
        assertEquals(anotherParent, pdfObjectUnderTest.getParent());
    }

    /**
     * Test getObjectID() - returns the PDF object ID.
     */
    @Test
    public void testGetObjectID() {
        pdfObjectUnderTest.setObjectNumber(10);
        // String is of the format "<object#> <generation#> obj\n"
        assertEquals("10 0 obj\n", pdfObjectUnderTest.getObjectID());
    }

    /**
     * Test referencePDF() - returns a {@link String} in PDF format to reference this object.
     */
    @Test
    public void testReferencePDF() {
        try {
            pdfObjectUnderTest.referencePDF();
            fail("The object number is not set, an exception should be thrown");
        } catch (IllegalArgumentException e) {
            // PASS
        }
        pdfObjectUnderTest.setObjectNumber(10);
        // Referencing this object is in the format "<obj#> <gen#> R"
        assertEquals("10 0 R", pdfObjectUnderTest.referencePDF());
    }

    /**
     * Test makeReference() - returns this object represented as a {@link PDFReference}.
     */
    @Test
    public void testMakeReference() {
        // Not very intelligent but, there's not much to test here
        pdfObjectUnderTest.setObjectNumber(10);
        PDFReference ref = pdfObjectUnderTest.makeReference();
        assertEquals(pdfObjectUnderTest.getObjectNumber(), ref.getObjectNumber());
        assertEquals(pdfObjectUnderTest, ref.getObject());
        assertEquals(pdfObjectUnderTest.referencePDF(), ref.toString());
    }

    /**
     * Tests PDF object references.
     * @throws Exception if an error occurs
     */
    @Test
    public void testReference() throws Exception {
        PDFDictionary dict = new PDFDictionary();
        dict.setObjectNumber(7);
        PDFReference ref = dict.makeReference();
        assertEquals(ref.getObjectNumber().getNumber(), 7);
        assertEquals(ref.getGeneration(), 0);
        assertEquals(ref.toString(), "7 0 R");

        ref = new PDFReference("8 0 R");
        assertEquals(ref.getObjectNumber().getNumber(), 8);
        assertEquals(ref.getGeneration(), 0);
        assertEquals(ref.toString(), "8 0 R");
    }

    /**
     * A generic method to test output() for sub-classes of (@link PDFObject}. The expected String
     * should be formatted such that the object number and object descriptor aren't printed i.e.
     * for a simple integer object in PDF:
     * <pre>
     * 1 0 obj  ** ommited from expectedString
     * 10
     * endobj   ** ommited from expectedString
     * </pre>
     * Thus the expected string would be "10".
     * @param expectedString the string that is expected.
     * @param object the object being tested
     * @throws IOException error with I/O
     */
    protected void testOutputStreams(String expectedString, PDFObject object) throws IOException {
        // Test both with and without object numbers
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        // Ensure that
        object.setObjectNumber(0);
        assertEquals(expectedString.length(), object.output(outStream));
        assertEquals(expectedString, outStream.toString());
        outStream.reset();
        object.setObjectNumber(1);
        // Test the length of the output string is returned correctly.
        assertEquals(expectedString.length(), object.output(outStream));
        assertEquals(expectedString, outStream.toString());
    }
}
