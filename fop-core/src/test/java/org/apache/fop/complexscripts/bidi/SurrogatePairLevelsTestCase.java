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

package org.apache.fop.complexscripts.bidi;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;

import org.apache.fop.traits.Direction;

/**
 * Both UTF-16 units of a supplementary-plane character must resolve to one bidi level, as
 * {@link UnicodeBidiAlgorithm#resolveLevels(CharSequence, Direction)} documents. U+10826 is
 * a Cypriot syllable, a right-to-left script outside the BMP (FOP-2918).
 */
public class SurrogatePairLevelsTestCase {

    private static final String CYPRIOT = "𐠦";

    @Test
    public void testPairAlone() {
        assertArrayEquals(new int[] {1, 1}, UnicodeBidiAlgorithm.resolveLevels(CYPRIOT, Direction.LR));
    }

    @Test
    public void testPairBetweenLatinLetters() {
        assertArrayEquals(new int[] {0, 1, 1, 0},
                UnicodeBidiAlgorithm.resolveLevels("a" + CYPRIOT + "b", Direction.LR));
    }

    @Test
    public void testTwoPairs() {
        assertArrayEquals(new int[] {1, 1, 1, 1},
                UnicodeBidiAlgorithm.resolveLevels(CYPRIOT + CYPRIOT, Direction.LR));
    }
}
