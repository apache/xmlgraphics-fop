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

package org.apache.fop.pdf.xref;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;

import static org.junit.Assert.assertArrayEquals;

import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFInfo;
import org.apache.fop.pdf.PDFPages;
import org.apache.fop.pdf.PDFRoot;

public abstract class CrossReferenceObjectTest {

    protected static final int STARTXREF = 12345;

    protected PDFDocument pdfDocument;

    protected TrailerDictionary trailerDictionary;

    private CrossReferenceObject crossReferenceObject;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        pdfDocument = new PDFDocument("Apache FOP");
        Map<String, List<String>> filterMap = pdfDocument.getFilterMap();
        filterMap.put("default", Arrays.asList("null"));
        PDFRoot root = new PDFRoot(1, new PDFPages(10));
        PDFInfo info = new PDFInfo();
        info.setObjectNumber(2);
        byte[] fileID =
                new byte[] {0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef};
        trailerDictionary = new TrailerDictionary(pdfDocument)
                .setRoot(root)
                .setInfo(info)
                .setFileID(fileID, fileID);
    }

    protected void runTest() throws IOException {
        crossReferenceObject = createCrossReferenceObject();
        byte[] expected = createExpectedCrossReferenceData();
        byte[] actual = createActualCrossReferenceData();
        assertArrayEquals(expected, actual);
    }

    protected abstract CrossReferenceObject createCrossReferenceObject();

    protected abstract byte[] createExpectedCrossReferenceData() throws IOException;

    protected byte[] createActualCrossReferenceData() throws IOException {
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        crossReferenceObject.output(pdf);
        pdf.close();
        return pdf.toByteArray();
    }

    protected byte[] getBytes(StringBuilder stringBuilder) {
        return getBytes(stringBuilder.toString());
    }

    protected byte[] getBytes(String string) {
        try {
            return string.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Outputs the given byte array to a file with the given name. Use for debugging
     * purpose.
     */
    protected void streamToFile(byte[] bytes, String filename) throws IOException {
        OutputStream output = new FileOutputStream(filename);
        output.write(bytes);
        output.close();
    }

}
