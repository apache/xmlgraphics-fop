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
package org.apache.fop.render.pcl.fonts;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class PCLByteWriterUtilTestCase {
    private PCLByteWriterUtil byteWriter;

    @Before
    public void setUp() {
        byteWriter = new PCLByteWriterUtil();
    }

    @Test
    public void testWriteMethods() throws IOException {
        byte[] output = byteWriter.writeCommand("(s4X");
        // 27 = PCL escape character with rest in ASCII format
        byte[] command = {27, 40, 115, 52, 88};
        assertArrayEquals(command, output);

        byte[] resultB = byteWriter.unsignedLongInt(102494);
        byte[] compareB = {0, 1, -112, 94};
        assertArrayEquals(compareB, resultB);

        byte[] resultC = byteWriter.unsignedInt(1024);
        byte[] compareC = {4, 0};
        assertArrayEquals(compareC, resultC);
    }

    @Test
    public void testUtilMethods() throws IOException {
        byte[] anArray = {1, 2, 3, 4, 5, 9, 10};
        byte[] insertArray = {6, 7, 8};
        byte[] result = byteWriter.insertIntoArray(5, anArray, insertArray);
        byte[] compareA = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        assertArrayEquals(compareA, result);

        byte[] reverse = {10, 9, 8, 7, 6};
        byteWriter.updateDataAtLocation(compareA, reverse, 5);
        byte[] compareB = {1, 2, 3, 4, 5, 10, 9, 8, 7, 6};
        assertArrayEquals(compareB, compareA);

        byte[] anArrayC = {1, 2, 3, 4, 5};
        byte[] resultC = byteWriter.padBytes(anArrayC, 10);
        byte[] compareC = {1, 2, 3, 4, 5, 0, 0, 0, 0, 0};
        assertArrayEquals(compareC, resultC);

        byte[] resultD = byteWriter.padBytes(anArrayC, 10, 1);
        byte[] compareD = {1, 2, 3, 4, 5, 1, 1, 1, 1, 1};
        assertArrayEquals(compareD, resultD);
    }
}
