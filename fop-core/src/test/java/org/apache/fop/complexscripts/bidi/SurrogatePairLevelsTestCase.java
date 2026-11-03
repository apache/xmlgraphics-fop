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

    /** U+1F300 CYCLONE, a neutral (ON) outside the BMP that FOP's bidi class table knows as one. */
    private static final String CYCLONE = "\uD83C\uDF00";

    private static final String SHALOM = "\u05E9\u05DC\u05D5\u05DD";

    private static final String OLAM = "\u05E2\u05D5\u05DC\u05DD";

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

    /**
     * A neutral outside the BMP inside right-to-left text resolves as a neutral in the BMP does
     * (U+263A here): it takes the text's direction by rule N1, so the run is not cut in two. The
     * placeholder for the low surrogate, as a class of its own, ended the run of neutrals, and the
     * pair fell to the embedding direction; copying the level after resolution cannot mend that.
     */
    @Test
    public void testNeutralPairInsideRightToLeftText() {
        assertArrayEquals(new int[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                UnicodeBidiAlgorithm.resolveLevels(SHALOM + "\u263A " + OLAM, Direction.LR));
        assertArrayEquals(new int[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                UnicodeBidiAlgorithm.resolveLevels(SHALOM + CYCLONE + " " + OLAM, Direction.LR));
        assertArrayEquals(new int[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                UnicodeBidiAlgorithm.resolveLevels(SHALOM + " " + CYCLONE + " " + OLAM, Direction.LR));
    }

    /** Between left-to-right and right-to-left text the same neutral takes the embedding direction (N2). */
    @Test
    public void testNeutralPairBetweenDirections() {
        assertArrayEquals(new int[] {0, 0, 0, 0, 0, 1, 1, 1, 1},
                UnicodeBidiAlgorithm.resolveLevels("ab" + CYCLONE + " " + OLAM, Direction.LR));
        assertArrayEquals(new int[] {2, 2, 1, 1, 1, 1, 1, 1, 1},
                UnicodeBidiAlgorithm.resolveLevels("ab" + CYCLONE + " " + OLAM, Direction.RL));
    }
}
