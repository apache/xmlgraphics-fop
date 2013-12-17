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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.fop.pdf.xref.CompressedObjectReference;

public class ObjectStreamManagerTestCase {

    private List<CompressedObjectReference> compressedObjectReferences;

    private MockPdfDocument pdfDocument;

    @Test
    public void add() {
        final int expectedCapacity = 100;
        final int numCompressedObjects = expectedCapacity * 2 + 1;
        createCompressObjectReferences(numCompressedObjects);
        assertEquals(numCompressedObjects, compressedObjectReferences.size());
        int objectStreamNumber1 = assertSameObjectStream(0, expectedCapacity);
        int objectStreamNumber2 = assertSameObjectStream(expectedCapacity, expectedCapacity * 2);
        int objectStreamNumber3 = assertSameObjectStream(expectedCapacity * 2, numCompressedObjects);
        assertDifferent(objectStreamNumber1, objectStreamNumber2, objectStreamNumber3);
        assertEquals(objectStreamNumber3, pdfDocument.previous.getObjectNumber());
    }

    private void createCompressObjectReferences(int numObjects) {
        pdfDocument = new MockPdfDocument();
        ObjectStreamManager sut = new ObjectStreamManager(pdfDocument);
        for (int obNum = 1; obNum <= numObjects; obNum++) {
            sut.add(createCompressedObject(obNum));
        }
        compressedObjectReferences = sut.getCompressedObjectReferences();
    }

    private static class MockPdfDocument extends PDFDocument {

        private ObjectStream previous;

        public MockPdfDocument() {
            super("");
        }

        public void assignObjectNumber(PDFObject obj) {
            super.assignObjectNumber(obj);
            if (obj instanceof ObjectStream) {
                ObjectStream  objStream = (ObjectStream) obj;
                ObjectStream previous = (ObjectStream) objStream.get("Extends");
                if (previous == null) {
                    assertEquals(this.previous, previous);
                }
                this.previous = objStream;
            }
        }
    }

    private CompressedObject createCompressedObject(final int objectNumber) {
        return new CompressedObject() {

            public int getObjectNumber() {
                return objectNumber;
            }

            public int output(OutputStream outputStream) throws IOException {
                throw new UnsupportedOperationException();
            }
        };
    }

    private int assertSameObjectStream(int from, int to) {
        int objectStreamNumber = getObjectStreamNumber(from);
        for (int i = from + 1; i < to; i++) {
            assertEquals(objectStreamNumber, getObjectStreamNumber(i));
        }
        return objectStreamNumber;
    }

    private int getObjectStreamNumber(int index) {
        return compressedObjectReferences.get(index).getObjectStreamNumber();
    }

    private void assertDifferent(int objectStreamNumber1, int objectStreamNumber2,
            int objectStreamNumber3) {
        assertTrue(objectStreamNumber1 != objectStreamNumber2);
        assertTrue(objectStreamNumber1 != objectStreamNumber3);
        assertTrue(objectStreamNumber2 != objectStreamNumber3);
    }
}
