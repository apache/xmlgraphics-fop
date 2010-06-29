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

package org.apache.fop.render.ps;

import junit.framework.TestCase;

/**
 * Test case for the conversion of characters into hex-encoded strings.
 */
public class HexEncoderTestCase extends TestCase {

    private static char successor(char d) {
        if (d == '9') {
            return 'A';
        } else if (d == 'F') {
            return '0';
        } else {
            return (char) (d + 1);
        }
    }

    private static void increment(char[] digits) {
        int d = 4;
        do {
            d--;
            digits[d] = successor(digits[d]);
        } while (digits[d] == '0' && d > 0);
    }

    /**
     * Tests that characters are properly encoded into hex strings.
     */
    public void testEncodeChar() {
        char[] digits = new char[] {'0', '0', '0', '0'};
        for (int c = 0; c <= 0xFFFF; c++) {
            assertEquals(new String(digits), HexEncoder.encode((char) c));
            increment(digits);
        }
    }

}
