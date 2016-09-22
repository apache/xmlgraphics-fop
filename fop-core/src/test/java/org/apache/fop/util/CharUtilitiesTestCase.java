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

package org.apache.fop.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class CharUtilitiesTestCase {

    @Test
    public void testIsBmpCodePoint() {
        for (int i = 0; i < 0x10FFFF; i++) {
            assertEquals(i <= 0xFFFF, CharUtilities.isBmpCodePoint(i));
        }
    }

    @Test
    public void testIncrementIfNonBMP() {
        for (int i = 0; i < 0x10FFFF; i++) {
            if (i <= 0xFFFF) {
                assertEquals(0, CharUtilities.incrementIfNonBMP(i));
            } else {
                assertEquals(1, CharUtilities.incrementIfNonBMP(i));
            }
        }
    }

    @Test
    public void testIsSurrogatePair() {
        for (char i = 0; i < 0xFFFF; i++) {
            if (i < 0xD800 || i > 0xDFFF) {
                assertFalse(CharUtilities.isSurrogatePair(i));
            } else {
                assertTrue(CharUtilities.isSurrogatePair(i));
            }
        }
    }

    @Test
    public void testContainsSurrogatePairAt() {
        String withSurrogatePair = "012\uD83D\uDCA94";

        assertTrue(CharUtilities.containsSurrogatePairAt(withSurrogatePair, 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testContainsSurrogatePairAtWithMalformedUTF8Sequence() {
        String malformedUTF8Sequence = "012\uD83D4";

        CharUtilities.containsSurrogatePairAt(malformedUTF8Sequence, 3);
    }
}
