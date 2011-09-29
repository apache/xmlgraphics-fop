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

package org.apache.fop.afp.modca;

import java.util.Arrays;

/**
 * Tests the {@linkplain AbstractAFPObject} class.
 */
public abstract class AbstractNamedAFPObjectTestCase<S extends  AbstractNamedAFPObject>
        extends AbstractAFPObjectTestCase<S> {

    public void testCopySF() {

        final S sut = getSut();

        byte[] expected = new byte[17];
        S.copySF(expected, (byte) 0xD3, (byte)0, (byte)0);

        byte[] nameData = sut.getNameBytes();
        System.arraycopy(nameData, 0, expected, 9, nameData.length);

        byte[] actual = new byte[17];
        Arrays.fill(actual, (byte)-1);

        getSut().copySF(actual, (byte)0, (byte)0);

        assertTrue(Arrays.equals(actual, expected));

        byte[] expected2 =  new byte[17];
        System.arraycopy(expected, 0, expected2, 0, expected.length);
        System.arraycopy(nameData, 0, expected, 9, nameData.length);

        final byte type = (byte)1;
        final byte catagory = (byte)2;
        expected2[4] = type;
        expected2[5] = catagory;

        getSut().copySF(actual, type, catagory);

        assertTrue(Arrays.equals(actual, expected2));
    }
}
