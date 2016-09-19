package org.apache.fop.util;

import static org.junit.Assert.*;

import org.junit.Test;

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
    public void testContainsSurrogatePairAt_malformedUTF8Sequence() {
        String malformedUTF8Sequence = "012\uD83D4";

        CharUtilities.containsSurrogatePairAt(malformedUTF8Sequence, 3);
    }
}
