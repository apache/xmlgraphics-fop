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
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class CompressedObjectReferenceTestCase extends ObjectReferenceTest {

    @Test
    public void testOutput() throws IOException {
        runTest(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0), 0);
        runTest(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0x1), 4);
        runTest(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0xf3), 16);
        runTest(Arrays.asList(0, 0, 0, 0, 0, 0, 0x5, 0xf7), 128);
        runTest(Arrays.asList(0, 0, 0, 0, 0, 0x9, 0xfb, 0xd), 0xae);
        runTest(Arrays.asList(0, 0, 0, 0, 0x11, 0xff, 0x15, 0xe9), 0xff);
    }

    private void runTest(List<Integer> expectedObjectStreamBytes, int index) throws IOException {
        int objectStreamNumber = (int) computeNumberFromBytes(expectedObjectStreamBytes);
        sut = new CompressedObjectReference(0, objectStreamNumber, index);
        byte[] expected = createExpectedOutput((byte) 2, expectedObjectStreamBytes, index);
        byte[] actual = getActualOutput();
        assertArrayEquals(expected, actual);
    }

}
