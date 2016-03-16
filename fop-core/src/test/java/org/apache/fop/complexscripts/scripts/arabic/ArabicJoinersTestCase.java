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

package org.apache.fop.complexscripts.scripts.arabic;

import java.nio.IntBuffer;
import java.util.BitSet;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

import org.apache.fop.complexscripts.scripts.ScriptProcessor;
import org.apache.fop.complexscripts.util.CharScript;
import org.apache.fop.complexscripts.util.GlyphContextTester;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.complexscripts.util.ScriptContextTester;
import org.apache.fop.complexscripts.util.UTF32;

// CSOFF: LineLength

/**
 * Tests for joiner (ZWJ, ZWNJ) functionality related to the arabic script.
 */
public class ArabicJoinersTestCase {

    private static final String[][] ZWJ_TESTS_ISOL = new String[][] {
        { "\u0643",               "1",          },
        { "\u0643\u200D",         "00",         },
        { "\u200D\u0643",         "00",         },
        { "\u200D\u0643\u200D",   "000",        },
    };

    private static final String[][] ZWJ_TESTS_INIT = new String[][] {
        { "\u0643",               "0",          },
        { "\u0643\u200D",         "10",         },
        { "\u200D\u0643",         "00",         },
        { "\u200D\u0643\u200D",   "000",        },
    };

    private static final String[][] ZWJ_TESTS_MEDI = new String[][] {
        { "\u0643",               "0",          },
        { "\u0643\u200D",         "00",         },
        { "\u200D\u0643",         "00",         },
        { "\u200D\u0643\u200D",   "010",        },
    };

    private static final String[][] ZWJ_TESTS_FINA = new String[][] {
        { "\u0643",               "0",          },
        { "\u0643\u200D",         "00",         },
        { "\u200D\u0643",         "01",         },
        { "\u200D\u0643\u200D",   "000",        },
    };

    private static final String[][] ZWJ_TESTS_LIGA = new String[][] {
    };

    @Test
    public void testArabicJoiners() {
        String script = CharScript.scriptTagFromCode(CharScript.SCRIPT_ARABIC);
        ScriptProcessor sp = ScriptProcessor.getInstance(script);
        assertTrue(sp != null);
        ScriptContextTester sct = sp.getSubstitutionContextTester();
        assertTrue(sct != null);
        String language = "dflt";
        int flags = 0;
        testZWJ(sct, script, language, "isol", flags, ZWJ_TESTS_ISOL);
        testZWJ(sct, script, language, "init", flags, ZWJ_TESTS_INIT);
        testZWJ(sct, script, language, "medi", flags, ZWJ_TESTS_MEDI);
        testZWJ(sct, script, language, "fina", flags, ZWJ_TESTS_FINA);
        testZWJ(sct, script, language, "liga", flags, ZWJ_TESTS_LIGA);
    }

    private void testZWJ(ScriptContextTester sct, String script, String language, String feature, int flags, String[][] tests) {
        GlyphContextTester gct = sct.getTester(feature);
        assertTrue(gct != null);
        for (String[] t : tests) {
            testZWJ(gct, script, language, feature, flags, t);
        }
    }

    private void testZWJ(GlyphContextTester gct, String script, String language, String feature, int flags, String[] test) {
        assert test.length == 2;
        String str = test[0];
        BitSet act = new BitSet();
        GlyphSequence gs = makeGlyphSequence(str);
        for (int i = 0, n = str.length(); i < n; ++i) {
            if (gct.test(script, language, feature, gs, i, flags)) {
                act.set(i);
            }
        }
        BitSet exp = parseBitSet(test[1]);
        assertTrue(act.equals(exp));
    }

    private GlyphSequence makeGlyphSequence(String s) {
        Integer[] ca = UTF32.toUTF32(s, 0, true);
        IntBuffer cb = IntBuffer.allocate(ca.length);
        for (Integer c : ca) {
            cb.put(c);
        }
        cb.rewind();
        return new GlyphSequence(cb, null, null);
    }

    private BitSet parseBitSet(String s) {
        BitSet bits = new BitSet();
        for (int i = 0, n = s.length(); i < n; ++i) {
            char c = s.charAt(i);
            assert (c == '0') || (c == '1');
            if (c == '1') {
                bits.set(i);
            }
        }
        return bits;
    }

    @Test
    public void testArabicNonJoiners() {
    }

}
