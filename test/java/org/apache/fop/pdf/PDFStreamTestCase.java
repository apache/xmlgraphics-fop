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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PDFStreamTestCase {

    private PDFStream stream;

    @Before
    public void createStream() {
        stream = new PDFStream();
        stream.setObjectNumber(1);
        PDFDocument pdfDocument = new PDFDocument("Apache FOP");
        stream.setDocument(pdfDocument);
    }

    @Test
    public void testFilterSetup() {
        testGetFilterList();
        testSetupFilterList();
    }

    private void testGetFilterList() {
        PDFFilterList filterList = stream.getFilterList();
        assertFalse(filterList.isInitialized());
        assertEquals(0, filterList.getFilters().size());
    }

    private void testSetupFilterList() {
        stream.setupFilterList();
        PDFFilterList filterList = stream.getFilterList();
        assertTrue(filterList.isInitialized());
        assertEquals(1, filterList.getFilters().size());
        PDFFilter filter = filterList.getFilters().get(0);
        assertEquals("/FlateDecode", filter.getName());
    }

    @Test
    public void customFilter() {
        PDFFilterList filters = stream.getFilterList();
        filters.addFilter("null");
        assertTrue(filters.isInitialized());
        assertEquals(1, filters.getFilters().size());
        PDFFilter filter = filters.getFilters().get(0);
        assertEquals("", filter.getName());
    }

    @Test
    public void testStream() throws IOException {
        PDFFilterList filters = stream.getFilterList();
        filters.addFilter("null");
        byte[] bytes = createSampleData();
        stream.setData(bytes);
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        stream.outputRawStreamData(actual);
        assertArrayEquals(bytes, actual.toByteArray());
    }

    @Test
    public void testEncodeStream() throws IOException {
        PDFFilterList filters = stream.getFilterList();
        filters.addFilter("null");
        byte[] bytes = createSampleData();
        stream.setData(bytes);
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        StreamCache streamCache = stream.encodeStream();
        streamCache.outputContents(actual);
        assertArrayEquals(bytes, actual.toByteArray());
    }

    @Test
    public void testEncodeAndWriteStream() throws IOException {
        PDFFilterList filters = stream.getFilterList();
        filters.addFilter("null");
        byte[] bytes = createSampleData();
        stream.setData(bytes);
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        PDFNumber number = new PDFNumber();
        stream.encodeAndWriteStream(actual, number);
        assertArrayEquals(createSampleStreamData(), actual.toByteArray());
    }

    private byte[] createSampleData() {
        byte[] bytes = new byte[10];
        for (int i = 0; i < 10; i++) {
            bytes[i] = (byte) i;
        }
        return bytes;
    }

    private byte[] createSampleStreamData() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write("\nstream\n".getBytes("US-ASCII"));
        stream.write(createSampleData());
        stream.write("\nendstream".getBytes("US-ASCII"));
        return stream.toByteArray();
    }
}
