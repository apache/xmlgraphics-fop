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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * This test tests PDFNumber's doubleOut() methods.
 */
public class PDFNumberTestCase extends PDFObjectTestCase {
    /**
     * Sets up the local variables, most of these are inherited from PDFObjectTestCase
     */
    @Before
    public void setUp() {
        pdfObjectUnderTest = new PDFNumber();
        pdfObjectUnderTest.setParent(parent);
        pdfObjectUnderTest.setDocument(doc);
    }

    /**
     * Tests PDFNumber.doubleOut().
     * @throws Exception if the test fails
     */
    @Test
    public void testDoubleOut1() throws Exception {
        //Default is 6 decimal digits
        assertEquals("0", PDFNumber.doubleOut(0.0f));
        assertEquals("0", PDFNumber.doubleOut(0.0000000000000000000123f));
        assertEquals("0.1", PDFNumber.doubleOut(0.1f));
        assertEquals("100", PDFNumber.doubleOut(100.0f));
        assertEquals("100", PDFNumber.doubleOut(99.99999999999999999999999f));

        //You'd expect 100.123456 here but DecimalFormat uses the BigDecimal.ROUND_HALF_EVEN
        //strategy. I don't know if that's a problem. The strange thing testDoubleOut2
        //seems to return the normally expected value. Weird.
        assertEquals("100.123459", PDFNumber.doubleOut(100.12345611111111f));
        assertEquals("-100.123459", PDFNumber.doubleOut(-100.12345611111111f));
    }

    /**
     * Tests PDFNumber.doubleOut().
     * @throws Exception if the test fails
     */
    public void testDoubleOut2() throws Exception {
        //4 decimal digits in this case
        assertEquals("0", PDFNumber.doubleOut(0.0f, 4));
        assertEquals("0", PDFNumber.doubleOut(0.0000000000000000000123f, 4));
        assertEquals("0.1", PDFNumber.doubleOut(0.1f, 4));
        assertEquals("100", PDFNumber.doubleOut(100.0f, 4));
        assertEquals("100", PDFNumber.doubleOut(99.99999999999999999999999f, 4));
        assertEquals("100.1234", PDFNumber.doubleOut(100.12341111111111f, 4));
        assertEquals("-100.1234", PDFNumber.doubleOut(-100.12341111111111f, 4));
    }

    /**
     * Tests PDFNumber.doubleOut().
     * @throws Exception if the test fails
     */
    public void testDoubleOut3() throws Exception {
        //0 decimal digits in this case
        assertEquals("0", PDFNumber.doubleOut(0.0f, 0));
        assertEquals("0", PDFNumber.doubleOut(0.1f, 0));
        assertEquals("1", PDFNumber.doubleOut(0.6f, 0));
        assertEquals("100", PDFNumber.doubleOut(100.1234f, 0));
        assertEquals("-100", PDFNumber.doubleOut(-100.1234f, 0));
    }

    /**
     * Tests PDFNumber.doubleOut(). Special cases (former bugs).
     * @throws Exception if the test fails
     */
    public void testDoubleOut4() throws Exception {
        double d = Double.parseDouble("5.7220458984375E-6");
        assertEquals("0.000006", PDFNumber.doubleOut(d));
        assertEquals("0", PDFNumber.doubleOut(d, 4));
        assertEquals("0.00000572", PDFNumber.doubleOut(d, 8));
    }

    /**
     * Tests PDFNumber.doubleOut(). Tests for wrong parameters.
     * @throws Exception if the test fails
     */
    public void testDoubleOutWrongParameters() throws Exception {
        try {
            PDFNumber.doubleOut(0.1f, -1);
            fail("IllegalArgument expected!");
        } catch (IllegalArgumentException iae) {
            //we want that
        }
        try {
            PDFNumber.doubleOut(0.1f, 17); //We support max 16 decimal digits
            fail("IllegalArgument expected!");
        } catch (IllegalArgumentException iae) {
            //we want that
        }
        try {
            PDFNumber.doubleOut(0.1f, 98274659);
            fail("IllegalArgument expected!");
        } catch (IllegalArgumentException iae) {
            //we want that
        }
        try {
            PDFNumber.doubleOut(null);
            fail("NullPointer expected!");
        } catch (NullPointerException e) {
            // PASS
        }
    }

    /**
     * Tests both getNumber() and setNumber() - basic getter/setter methods... Why there isn't a
     * constructor is beyond me...
     */
    public void testGetSetNumber() {
        PDFNumber pdfNum = new PDFNumber();
        // Check with a floating point number
        pdfNum.setNumber(1.111f);
        assertEquals(1.111f, pdfNum.getNumber());
        // try with an int
        pdfNum.setNumber(2);
        assertEquals(2, pdfNum.getNumber());
        // See what happens with a null... make sure it doesn't explode
        pdfNum.setNumber(null);
        assertEquals(null, pdfNum.getNumber());
    }

    /**
     * Tests toPDFString() - this serializes PDFNumber to PDF format.
     * @throws IOException error caused by I/O
     */
    public void testToPDFString() throws IOException {
        PDFNumber testSubject = new PDFNumber();
        testSubject.setNumber(1.0001);
        testOutputStreams("1.0001", testSubject);
        testSubject.setNumber(999);
        testOutputStreams("999", testSubject);
    }
}
