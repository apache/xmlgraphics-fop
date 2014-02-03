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

package org.apache.fop.afp.fonts;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class IntegerKeyStoreTestCase {

    @Test
    public void getAndPut() {
        IntegerKeyStore<Integer> sut = new IntegerKeyStore<Integer>();
        assertNull(sut.get(0));
        sut.put(0, 0);
        assertEquals(Integer.valueOf(0), sut.get(0));
        sut.put(0, 1);
        assertEquals(Integer.valueOf(1), sut.get(0));
        sut.put(0, null);
        assertNull(sut.get(0));
        try {
            sut.put(-1, 0);
            fail("Negative index");
        } catch (IndexOutOfBoundsException e) {
            // As expected
        }
    }

}
