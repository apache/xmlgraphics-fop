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
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class CrossReferenceStreamTestCase extends CrossReferenceObjectTest {

    private List<Long> uncompressedObjectOffsets;

    private List<CompressedObjectReference> compressedObjectReferences;

    @Test
    public void testWithNoOffset() throws IOException {
        List<Long> emptyList = Collections.emptyList();
        test(emptyList);
    }

    @Test
    public void testWithOffsets() throws IOException {
        test(new ArrayList<Long>(Arrays.asList(0L, 1L, 2L, 3L, 4L)));
    }

    @Test
    public void testWithBigOffsets() throws IOException {
        test(new ArrayList<Long>(Arrays.asList(0xffL, 0xffffL, 0xffffffffL, 0xffffffffffffffffL)));
    }

    @Test
    public void testWithObjectStreams1() throws IOException {
        List<CompressedObjectReference> compressedObjectReferences =
                Arrays.asList(new CompressedObjectReference(2, 1, 0));
        test(Arrays.asList(0L, null), compressedObjectReferences);
    }

    @Test
    public void testWithObjectStreams2() throws IOException {
        int numIndirectObjects = 2;
        int numCompressedObjects = 1;
        List<Long> indirectObjectOffsets
                = new ArrayList<Long>(numIndirectObjects + numCompressedObjects);
        for (long i = 0; i < numIndirectObjects; i++) {
            indirectObjectOffsets.add(i);
        }
        List<CompressedObjectReference> compressedObjectReferences
                = new ArrayList<CompressedObjectReference>();
        for (int index = 0; index < numCompressedObjects; index++) {
            indirectObjectOffsets.add(null);
            int obNum = numIndirectObjects + index + 1;
            compressedObjectReferences.add(new CompressedObjectReference(obNum,
                    numIndirectObjects, index));
        }
        test(indirectObjectOffsets, compressedObjectReferences);
    }

    private void test(List<Long> indirectObjectOffsets) throws IOException {
        List<CompressedObjectReference> compressedObjectReferences = Collections.emptyList();
        test(indirectObjectOffsets, compressedObjectReferences);
    }

    private void test(List<Long> indirectObjectOffsets,
            List<CompressedObjectReference> compressedObjectReferences) throws IOException {
        this.uncompressedObjectOffsets = indirectObjectOffsets;
        this.compressedObjectReferences = compressedObjectReferences;
        runTest();
    }

    @Override
    protected CrossReferenceObject createCrossReferenceObject() {
        return new CrossReferenceStream(pdfDocument,
                uncompressedObjectOffsets.size() + 1,
                trailerDictionary,
                STARTXREF,
                uncompressedObjectOffsets,
                compressedObjectReferences);
    }

    @Override
    protected byte[] createExpectedCrossReferenceData() throws IOException {
        List<ObjectReference> objectReferences
                = new ArrayList<ObjectReference>(uncompressedObjectOffsets.size());
        for (Long offset : uncompressedObjectOffsets) {
            objectReferences.add(offset == null ? null : new UncompressedObjectReference(offset));
        }
        for (CompressedObjectReference ref : compressedObjectReferences) {
            objectReferences.set(ref.getObjectNumber() - 1, ref);
        }
        int maxObjectNumber = objectReferences.size() + 1;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        StringBuilder expected = new StringBuilder(256);
        expected.append(maxObjectNumber + " 0 obj\n")
                .append("<<\n")
                .append("  /Root 1 0 R\n")
                .append("  /Info 2 0 R\n")
                .append("  /ID [<0123456789ABCDEF> <0123456789ABCDEF>]\n")
                .append("  /Type /XRef\n")
                .append("  /Size ").append(Integer.toString(maxObjectNumber + 1)).append('\n')
                .append("  /W [1 8 2]\n")
                .append("  /Length ").append(Integer.toString((maxObjectNumber + 1) * 11 + 1)).append('\n')
                .append(">>\n")
                .append("stream\n");
        stream.write(getBytes(expected));
        DataOutputStream data = new DataOutputStream(stream);
        data.write(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xff, (byte) 0xff});
        for (ObjectReference objectReference : objectReferences) {
            objectReference.output(data);
        }
        data.write(1);
        data.writeLong(STARTXREF);
        data.write(0);
        data.write(0);
        data.close();
        stream.write(getBytes("\nendstream\nendobj\n"));
        return stream.toByteArray();
    }

}
