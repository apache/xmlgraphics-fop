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
import java.util.List;

abstract class ObjectReferenceTest {

    protected ObjectReference sut;

    protected long computeNumberFromBytes(List<Integer> expectedOffsetBytes) {
        assert expectedOffsetBytes.size() <= 8;
        long offset = 0;
        for (int b : expectedOffsetBytes) {
            offset = offset << 8 | b;
        }
        return offset;
    }

    protected byte[] createExpectedOutput(byte field1, List<Integer> field2, int field3) {
        assert field2.size() == 8;
        assert (field3 & 0xffff) == field3;
        byte[] expected = new byte[11];
        int index = 0;
        expected[index++] = field1;
        for (Integer b : field2) {
            expected[index++] = b.byteValue();
        }
        expected[index++] = (byte) ((field3 & 0xff00) >> 8);
        expected[index++] = (byte) (field3 & 0xff);
        return expected;
    }

    protected byte[] getActualOutput() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        sut.output(dataOutputStream);
        dataOutputStream.close();
        return out.toByteArray();
    }

}
