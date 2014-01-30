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
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ObjectStreamTestCase {

    private static final String OBJECT_CONTENT = "<<\n  /Foo True\n  /Bar False\n>>\n";

    private PDFDocument pdfDocument;

    private ObjectStream objectStream;

    private List<MockCompressedObject> compressedObjects;

    @Before
    public void setUp() throws Exception {
        pdfDocument = new PDFDocument("PDFObjectStreamTestCase");
        objectStream = new ObjectStream();
        pdfDocument.assignObjectNumber(objectStream);
        compressedObjects = Arrays.asList(new MockCompressedObject(), new MockCompressedObject());
    }

    @Test
    public void testSingleObjectStream() throws IOException {
        populateObjectStream();
        testOutput();
    }

    @Test
    public void testObjectStreamCollection() throws IOException {
        objectStream = new ObjectStream(objectStream);
        pdfDocument.assignObjectNumber(objectStream);
        populateObjectStream();
        testOutput();
    }

    @Test(expected = IllegalStateException.class)
    public void directObjectsAreNotAllowed() throws Exception {
        objectStream.addObject(new MockCompressedObject());
    }

    @Test(expected = NullPointerException.class)
    public void nullObjectsAreNotAllowed() throws Exception {
        objectStream.addObject(null);
    }

    private void testOutput() throws IOException {
        String expected = getExpectedOutput();
        String actual = getActualOutput();
        assertEquals(expected, actual);
    }

    private void populateObjectStream() {
        for (MockCompressedObject obj : compressedObjects) {
            pdfDocument.assignObjectNumber(obj);
            objectStream.addObject(obj);
        }
    }

    private String getExpectedOutput() {
        int numObs = compressedObjects.size();
        int objectStreamNumber = objectStream.getObjectNumber();
        int offsetsLength = 9;
        StringBuilder expected = new StringBuilder();
        expected.append("<<\n");
        ObjectStream previous = (ObjectStream) objectStream.get("Extends");
        if (previous != null) {
            expected.append("  /Extends ").append(previous.getObjectNumber()).append(" 0 R\n");
        }
        expected.append("  /Type /ObjStm\n")
                .append("  /N ").append(numObs).append("\n")
                .append("  /First ").append(offsetsLength).append('\n')
                .append("  /Length ").append(OBJECT_CONTENT.length() * 2 + offsetsLength + 1).append('\n')
                .append(">>\n")
                .append("stream\n");
        int offset = 0;
        int num = 1;
        for (PDFObject ob : compressedObjects) {
            expected.append(objectStreamNumber + num++).append(' ').append(offset).append('\n');
            offset += ob.toPDFString().length();
        }
        for (PDFObject ob : compressedObjects) {
            expected.append(ob.toPDFString());
        }
        expected.append("\nendstream");
        return expected.toString();
    }

    private String getActualOutput() throws IOException {
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        objectStream.getFilterList().setDisableAllFilters(true);
        objectStream.output(actual);
        return actual.toString("US-ASCII");
    }

    private static class MockCompressedObject extends PDFObject implements CompressedObject {

        @Override
        protected String toPDFString() {
            return OBJECT_CONTENT;
        }
    }

}
