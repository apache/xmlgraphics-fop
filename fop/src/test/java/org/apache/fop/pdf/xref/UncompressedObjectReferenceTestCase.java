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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class UncompressedObjectReferenceTestCase extends ObjectReferenceTest {

    @Test
    public void test1ByteOffsets() throws IOException {
        run1ByteOffsetTest(0x0);
        run1ByteOffsetTest(0xf);
        run1ByteOffsetTest(0x10);
        run1ByteOffsetTest(0xff);
    }

    private void run1ByteOffsetTest(int offset) throws IOException {
        runIntegerOffsetTest(Arrays.asList(0, 0, 0, offset));
    }

    @Test
    public void test2ByteOffsets() throws IOException {
        runIntegerOffsetTest(Arrays.asList(0, 0, 1, 0xff));
        runIntegerOffsetTest(Arrays.asList(0, 0, 0xa0, 0xff));
    }

    @Test
    public void test3ByteOffsets() throws IOException {
        runIntegerOffsetTest(Arrays.asList(0, 2, 0x12, 0x34));
        runIntegerOffsetTest(Arrays.asList(0, 0xee, 0x56, 0x78));
    }

    @Test
    public void test4ByteOffsets() throws IOException {
        runIntegerOffsetTest(Arrays.asList(0x6, 0x12, 0x34, 0x56));
        runIntegerOffsetTest(Arrays.asList(0xf1, 0x9a, 0xbc, 0xde));
    }

    @Test
    public void test5ByteOffsets() throws IOException {
        runTest(Arrays.asList(0, 0, 0, 0x7, 0x78, 0x9a, 0xbc, 0xde));
        runTest(Arrays.asList(0, 0, 0, 0xbf, 0xf0, 0, 0x1, 0x2));
    }

    @Test
    public void test8ByteOffsets() throws IOException {
        runTest(Arrays.asList(0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8));
        runTest(Arrays.asList(0xf9, 0xe8, 0xd7, 0xc6, 0xb5, 0xa4, 0x93, 0x82));
    }

    private void runIntegerOffsetTest(List<Integer> expectedOffsetBytes) throws IOException {
        List<Integer> expectedLongOffset = new ArrayList<Integer>(8);
        expectedLongOffset.addAll(Arrays.asList(0, 0, 0, 0));
        expectedLongOffset.addAll(expectedOffsetBytes);
        runTest(expectedLongOffset);
    }

    private void runTest(List<Integer> expectedOffsetBytes) throws IOException {
        long offset = computeNumberFromBytes(expectedOffsetBytes);
        sut = new UncompressedObjectReference(offset);
        byte[] expected = createExpectedOutput((byte) 1, expectedOffsetBytes, (byte) 0);
        byte[] actual = getActualOutput();
        assertArrayEquals(expected, actual);
    }

}
