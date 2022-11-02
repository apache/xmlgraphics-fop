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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This is a test case for ({@link Version}.
 */
public class VersionTestCase {

    /**
     * Test the <code>getValue()</code> method. This should return {@link Version} given a
     * {@link String}.
     */
    @Test
    public void testGetValue() {
        int index = 0;
        for (Version version : Version.values()) {
            assertEquals(version, Version.getValueOf("1." + index++));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueIllegalArgument() {
        Version.getValueOf("blah");
    }

    /**
     * Tests that the <code>toString()</method> method returns the PDF version string of the proper
     * format.
     */
    @Test
    public void testToString() {
        // Test all the normal values
        int index = 0;
        for (Version version : Version.values()) {
            assertTrue(version.toString().equals("1." + index++));
        }
    }

    /**
     * Tests that the <code>compareTo()</code> contract is obeyed.
     */
    @Test
    public void testCompareTo() {
        // Ensure that the implicit comparison contract is satisfied
        Version[] expected = {
                Version.V1_0,
                Version.V1_1,
                Version.V1_2,
                Version.V1_3,
                Version.V1_4,
                Version.V1_5,
                Version.V1_6,
                Version.V1_7
        };

        Version[] actual = Version.values();

        for (int i = 0; i < actual.length - 1; i++) {
            assertEquals(-1, actual[i].compareTo(expected[i + 1]));

            assertEquals(0, actual[i].compareTo(expected[i]));

            assertEquals(1, actual[i + 1].compareTo(expected[i]));
        }
    }
}
