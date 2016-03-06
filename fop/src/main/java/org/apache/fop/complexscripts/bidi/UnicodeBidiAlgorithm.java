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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.traits.Direction;
import org.apache.fop.util.CharUtilities;

// CSOFF: LineLengthCheck

/**
 * <p>The <code>UnicodeBidiAlgorithm</code> class implements functionality prescribed by
 * the Unicode Bidirectional Algorithm, Unicode Standard Annex #9.</p>
 *
 * <p>This work was originally authored by Glenn Adams (gadams@apache.org).</p>
 */
public final class UnicodeBidiAlgorithm implements BidiConstants {

    /**
     * logging instance
     */
    private static final Log log = LogFactory.getLog(UnicodeBidiAlgorithm.class);

    private UnicodeBidiAlgorithm() {
    }

    /**
     * Resolve the directionality levels of each character in a character seqeunce.
     * If some character is encoded in the character sequence as a Unicode Surrogate Pair,
     * then the directionality level of each of the two members of the  pair will be identical.
     * @return null if bidirectional processing is not required; otherwise, returns an array
     * of integers, where each integer corresponds to exactly one UTF-16
     * encoding element present in the input character sequence, and where each integer denotes
     * the directionality level of the corresponding encoding element
     * @param cs input character sequence representing a UTF-16 encoded string
     * @param defaultLevel the default paragraph level, which must be zero (LR) or one (RL)
     */
    public static int[] resolveLevels(CharSequence cs, Direction defaultLevel) {
        int[] chars = new int [ cs.length() ];
        if (convertToScalar(cs, chars) || (defaultLevel == Direction.RL)) {
            return resolveLevels(chars, (defaultLevel == Direction.RL) ? 1 : 0, new int [ chars.length ]);
        } else {
            return null;
        }
    }

    /**
     * Resolve the directionality levels of each character in a character seqeunce.
     * @return null if bidirectional processing is not required; otherwise, returns an array
     * of integers, where each integer corresponds to exactly one UTF-16
     * encoding element present in the input character sequence, and where each integer denotes
     * the directionality level of the corresponding encoding element
     * @param chars array of input characters represented as unicode scalar values
     * @param defaultLevel the default paragraph level, which must be zero (LR) or one (RL)
     * @param levels array to receive levels, one for each character in chars array
     */
    public static int[] resolveLevels(int[] chars, int defaultLevel, int[] levels) {
        return resolveLevels(chars, getClasses(chars), defaultLevel, levels, false);
    }

    /**
     * Resolve the directionality levels of each character in a character seqeunce.
     * @return null if bidirectional processing is not required; otherwise, returns an array
     * of integers, where each integer corresponds to exactly one UTF-16
     * encoding element present in the input character sequence, and where each integer denotes
     * the directionality level of the corresponding encoding element
     * @param chars array of input characters represented as unicode scalar values
     * @param classes array containing one bidi class per character in chars array
     * @param defaultLevel the default paragraph level, which must be zero (LR) or one (RL)
     * @param levels array to receive levels, one for each character in chars array
     * @param useRuleL1 true if rule L1 should be used
     */
    public static int[] resolveLevels(int[] chars, int[] classes, int defaultLevel, int[] levels, boolean useRuleL1) {
        int[] ica = classes;
        int[] wca = copySequence(ica);
        int[] ea  = new int [ levels.length ];
        resolveExplicit(wca, defaultLevel, ea);
        resolveRuns(wca, defaultLevel, ea, levelsFromEmbeddings(ea, levels));
        if (useRuleL1) {
            resolveSeparators(ica, wca, defaultLevel, levels);
        }
        dump("RL: CC(" + ((chars != null) ? chars.length : -1) + ")", chars, classes, defaultLevel, levels);
        return levels;
    }

    private static int[] copySequence(int[] ta) {
        int[] na = new int [ ta.length ];
        System.arraycopy(ta, 0, na, 0, na.length);
        return na;
    }

    private static void resolveExplicit(int[] wca, int defaultLevel, int[] ea) {
        int[] es = new int [ MAX_LEVELS ];          /* embeddings stack */
        int ei = 0;                                 /* embeddings stack index */
        int ec = defaultLevel;                      /* current embedding level */
        for (int i = 0, n = wca.length; i < n; i++) {
            int bc = wca [ i ];                     /* bidi class of current char */
            int el;                                 /* embedding level to assign to current char */
            switch (bc) {
            case LRE:                               // start left-to-right embedding
            case RLE:                               // start right-to-left embedding
            case LRO:                               // start left-to-right override
            case RLO:                               // start right-to-left override
                int en;                         /* new embedding level */
                if ((bc == RLE) || (bc == RLO)) {
                    en = ((ec & ~OVERRIDE) + 1) | 1;
                } else {
                    en = ((ec & ~OVERRIDE) + 2) & ~1;
                }
                if (en < (MAX_LEVELS + 1)) {
                    es [ ei++ ] = ec;
                    if ((bc == LRO) || (bc == RLO)) {
                        ec = en | OVERRIDE;
                    } else {
                        ec = en & ~OVERRIDE;
                    }
                } else {
                    // max levels exceeded, so don't change level or override
                }
                el = ec;
                break;
            case PDF:                               // pop directional formatting
                el = ec;
                if (ei > 0) {
                    ec = es [ --ei ];
                } else {
                    // ignore isolated PDF
                }
                break;
            case B:                                 // paragraph separator
                el = ec = defaultLevel;
                ei = 0;
                break;
            default:
                el = ec;
                break;
            }
            switch (bc) {
            case BN:
                break;
            case LRE: case RLE: case LRO: case RLO: case PDF:
                wca [ i ] = BN;
                break;
            default:
                if ((el & OVERRIDE) != 0) {
                    wca [ i ] = directionOfLevel(el);
                }
                break;
            }
            ea [ i ] = el;
        }
    }

    private static int directionOfLevel(int level) {
        return ((level & 1) != 0) ? R : L;
    }

    private static int levelOfEmbedding(int embedding) {
        return embedding & ~OVERRIDE;
    }

    private static int[] levelsFromEmbeddings(int[] ea, int[] la) {
        assert ea != null;
        assert la != null;
        assert la.length == ea.length;
        for (int i = 0, n = la.length; i < n; i++) {
            la [ i ] = levelOfEmbedding(ea [ i ]);
        }
        return la;
    }

    private static void resolveRuns(int[] wca, int defaultLevel, int[] ea, int[] la) {
        if (la.length != wca.length) {
            throw new IllegalArgumentException("levels sequence length must match classes sequence length");
        } else if (la.length != ea.length) {
            throw new IllegalArgumentException("levels sequence length must match embeddings sequence length");
        } else {
            for (int i = 0, n = ea.length, lPrev = defaultLevel; i < n; ) {
                int s = i;
                int e = s;
                int l = findNextNonRetainedFormattingLevel(wca, ea, s, lPrev);
                while (e < n) {
                    if (la [ e ] != l) {
                        if (startsWithRetainedFormattingRun(wca, ea, e)) {
                            e += getLevelRunLength(ea, e);
                        } else {
                            break;
                        }
                    } else {
                        e++;
                    }
                }
                lPrev = resolveRun(wca, defaultLevel, ea, la, s, e, l, lPrev);
                i = e;
            }
        }
    }

    private static int findNextNonRetainedFormattingLevel(int[] wca, int[] ea, int start, int lPrev) {
        int s = start;
        int e = wca.length;
        while (s < e) {
            if (startsWithRetainedFormattingRun(wca, ea, s)) {
                s += getLevelRunLength(ea, s);
            } else {
                break;
            }
        }
        if (s < e) {
            return levelOfEmbedding(ea [ s ]);
        } else {
            return lPrev;
        }
    }

    private static int getLevelRunLength(int[] ea, int start) {
        assert start < ea.length;
        int nl = 0;
        for (int s = start, e = ea.length, l0 = levelOfEmbedding(ea [ start ]); s < e; s++) {
            if (levelOfEmbedding(ea [ s ]) == l0) {
                nl++;
            } else {
                break;
            }
        }
        return nl;
    }

    private static boolean startsWithRetainedFormattingRun(int[] wca, int[] ea, int start) {
        int nl = getLevelRunLength(ea, start);
        if (nl > 0) {
            int nc = getRetainedFormattingRunLength(wca, start);
            return (nc >= nl);
        } else {
            return false;
        }
    }

    private static int getRetainedFormattingRunLength(int[] wca, int start) {
        assert start < wca.length;
        int nc = 0;
        for (int s = start, e = wca.length; s < e; s++) {
            if (wca [ s ] == BidiConstants.BN) {
                nc++;
            } else {
                break;
            }
        }
        return nc;
    }

    private static int resolveRun(int[] wca, int defaultLevel, int[] ea, int[] la, int start, int end, int level, int levelPrev) {

        // determine start of run direction
        int sor = directionOfLevel(max(levelPrev, level));

        // determine end of run direction
        int le = -1;
        if (end == wca.length) {
            le = max(level, defaultLevel);
        } else {
            for (int i = end; i < wca.length; i++) {
                if (wca [ i ] != BidiConstants.BN) {
                    le = max(level, la [ i ]);
                    break;
                }
            }
            if (le < 0) {
                le = max(level, defaultLevel);
            }
        }
        int eor = directionOfLevel(le);

        if (log.isDebugEnabled()) {
            log.debug("BR[" + padLeft(start, 3) + "," + padLeft(end, 3) + "] :" + padLeft(level, 2) + ": SOR(" + getClassName(sor) + "), EOR(" + getClassName(eor) + ")");
        }

        resolveWeak(wca, defaultLevel, ea, la, start, end, level, sor, eor);
        resolveNeutrals(wca, defaultLevel, ea, la, start, end, level, sor, eor);
        resolveImplicit(wca, defaultLevel, ea, la, start, end, level, sor, eor);

        // if this run is all retained formatting, then return prior level, otherwise this run's level
        return isRetainedFormatting(wca, start, end) ? levelPrev : level;
    }

    private static void resolveWeak(int[] wca, int defaultLevel, int[] ea, int[] la, int start, int end, int level, int sor, int eor) {

        // W1 - X BN* NSM -> X BN* X
        for (int i = start, n = end, bcPrev = sor; i < n; i++) {
            int bc = wca [ i ];
            if (bc == NSM) {
                wca [ i ] = bcPrev;
            } else if (bc != BN) {
                bcPrev = bc;
            }
        }

        // W2 - AL ... EN -> AL ... AN
        for (int i = start, n = end, bcPrev = sor; i < n; i++) {
            int bc = wca [ i ];
            if (bc == EN) {
                if (bcPrev == AL) {
                    wca [ i ] = AN;
                }
            } else if (isStrong(bc)) {
                bcPrev = bc;
            }
        }

        // W3 - AL -> R
        for (int i = start, n = end; i < n; i++) {
            int bc = wca [ i ];
            if (bc == AL) {
                wca [ i ] = R;
            }
        }

        // W4 - EN BN* ES BN* EN -> EN BN* EN BN* EN; XN BN* CS BN* XN -> XN BN* XN BN* XN
        for (int i = start, n = end, bcPrev = sor; i < n; i++) {
            int bc = wca [ i ];
            if (bc == ES) {
                int bcNext = eor;
                for (int j = i + 1; j < n; j++) {
                    if ((bc = wca [ j ]) != BN) {
                        bcNext = bc;
                        break;
                    }
                }
                if ((bcPrev == EN) && (bcNext == EN)) {
                    wca [ i ] = EN;
                }
            } else if (bc == CS) {
                int bcNext = eor;
                for (int j = i + 1; j < n; j++) {
                    if ((bc = wca [ j ]) != BN) {
                        bcNext = bc;
                        break;
                    }
                }
                if ((bcPrev == EN) && (bcNext == EN)) {
                    wca [ i ] = EN;
                } else if ((bcPrev == AN) && (bcNext == AN)) {
                    wca [ i ] = AN;
                }
            }
            if (bc != BN) {
                bcPrev = bc;
            }
        }

        // W5 - EN (ET|BN)* -> EN (EN|BN)*; (ET|BN)* EN -> (EN|BN)* EN
        for (int i = start, n = end, bcPrev = sor; i < n; i++) {
            int bc = wca [ i ];
            if (bc == ET) {
                int bcNext = eor;
                for (int j = i + 1; j < n; j++) {
                    bc = wca [ j ];
                    if ((bc != BN) && (bc != ET)) {
                        bcNext = bc;
                        break;
                    }
                }
                if ((bcPrev == EN) || (bcNext == EN)) {
                    wca [ i ] = EN;
                }
            } else if ((bc != BN) && (bc != ET)) {
                bcPrev = bc;
            }
        }

        // W6 - BN* (ET|ES|CS) BN* -> ON* ON ON*
        for (int i = start, n = end; i < n; i++) {
            int bc = wca [ i ];
            if ((bc == ET) || (bc == ES) || (bc == CS)) {
                wca [ i ] = ON;
                resolveAdjacentBoundaryNeutrals(wca, start, end, i, ON);
            }
        }

        // W7 - L ... EN -> L ... L
        for (int i = start, n = end, bcPrev = sor; i < n; i++) {
            int bc = wca [ i ];
            if (bc == EN) {
                if (bcPrev == L) {
                    wca [ i ] = L;
                }
            } else if ((bc == L) || (bc == R)) {
                bcPrev = bc;
            }
        }

    }

    private static void resolveNeutrals(int[] wca, int defaultLevel, int[] ea, int[] la, int start, int end, int level, int sor, int eor) {

        // N1 - (L|R) N+ (L|R) -> L L+ L | R R+ R; (AN|EN) N+ R -> (AN|EN) R+ R; R N+ (AN|EN) -> R R+ (AN|EN)
        for (int i = start, n = end, bcPrev = sor; i < n; i++) {
            int bc = wca [ i ];
            if (isNeutral(bc)) {
                int bcNext = eor;
                for (int j = i + 1; j < n; j++) {
                    bc = wca [ j ];
                    if ((bc == L) || (bc == R)) {
                        bcNext = bc;
                        break;
                    } else if ((bc == AN) || (bc == EN)) {
                        bcNext = R;
                        break;
                    } else if (isNeutral(bc)) {
                        continue;
                    } else if (isRetainedFormatting(bc)) {
                        continue;
                    } else {
                        break;
                    }
                }
                if (bcPrev == bcNext) {
                    wca [ i ] = bcPrev;
                    resolveAdjacentBoundaryNeutrals(wca, start, end, i, bcPrev);
                }
            } else if ((bc == L) || (bc == R)) {
                bcPrev = bc;
            } else if ((bc == AN) || (bc == EN)) {
                bcPrev = R;
            }
        }

        // N2 - N -> embedding level
        for (int i = start, n = end; i < n; i++) {
            int bc = wca [ i ];
            if (isNeutral(bc)) {
                int bcEmbedding = directionOfLevel(levelOfEmbedding(ea [ i ]));
                wca [ i ] = bcEmbedding;
                resolveAdjacentBoundaryNeutrals(wca, start, end, i, bcEmbedding);
            }
        }

    }

    private static void resolveAdjacentBoundaryNeutrals(int[] wca, int start, int end, int index, int bcNew) {
        if ((index < start) || (index >= end)) {
            throw new IllegalArgumentException();
        } else {
            for (int i = index - 1; i >= start; i--) {
                int bc = wca [ i ];
                if (bc == BN) {
                    wca [ i ] = bcNew;
                } else {
                    break;
                }
            }
            for (int i = index + 1; i < end; i++) {
                int bc = wca [ i ];
                if (bc == BN) {
                    wca [ i ] = bcNew;
                } else {
                    break;
                }
            }
        }
    }

    private static void resolveImplicit(int[] wca, int defaultLevel, int[] ea, int[] la, int start, int end, int level, int sor, int eor) {
        for (int i = start, n = end; i < n; i++) {
            int bc = wca [ i ];                     // bidi class
            int el = la [ i ];                      // embedding level
            int ed = 0;                             // embedding level delta
            if ((el & 1) == 0) {                // even
                if (bc == R) {
                    ed = 1;
                } else if (bc == AN) {
                    ed = 2;
                } else if (bc == EN) {
                    ed = 2;
                }
            } else {                                // odd
                if (bc == L) {
                    ed = 1;
                } else if (bc == EN) {
                    ed = 1;
                } else if (bc == AN) {
                    ed = 1;
                }
            }
            la [ i ] = el + ed;
        }
    }

    /**
     * Resolve separators and boundary neutral levels to account for UAX#9 3.4 L1 while taking into
     * account retention of formatting codes (5.2).
     * @param ica original input class array (sequence)
     * @param wca working copy of original intput class array (sequence), as modified by prior steps
     * @param dl default paragraph level
     * @param la array of output levels to be adjusted, as produced by bidi algorithm
     */
    private static void resolveSeparators(int[] ica, int[] wca, int dl, int[] la) {
        // steps (1) through (3)
        for (int i = 0, n = ica.length; i < n; i++) {
            int ic = ica[i];
            if ((ic == BidiConstants.S) || (ic == BidiConstants.B)) {
                la[i] = dl;
                for (int k = i - 1; k >= 0; k--) {
                    int pc = ica[k];
                    if (isRetainedFormatting(pc)) {
                        continue;
                    } else if (pc == BidiConstants.WS) {
                        la[k] = dl;
                    } else {
                        break;
                    }
                }
            }
        }
        // step (4) - consider end of input sequence to be end of line, but skip any trailing boundary neutrals and retained formatting codes
        for (int i = ica.length; i > 0; i--) {
            int k = i - 1;
            int ic = ica[k];
            if (isRetainedFormatting(ic)) {
                continue;
            } else if (ic == BidiConstants.WS) {
                la[k] = dl;
            } else {
                break;
            }
        }
        // step (5) - per section 5.2
        for (int i = 0, n = ica.length; i < n; i++) {
            int ic = ica[i];
            if (isRetainedFormatting(ic)) {
                if (i == 0) {
                    la[i] = dl;
                } else {
                    la[i] = la [ i - 1 ];
                }
            }
        }
    }

    private static boolean isStrong(int bc) {
        switch (bc) {
        case L:
        case R:
        case AL:
            return true;
        default:
            return false;
        }
    }

    private static boolean isNeutral(int bc) {
        switch (bc) {
        case WS:
        case ON:
        case S:
        case B:
            return true;
        default:
            return false;
        }
    }

    private static boolean isRetainedFormatting(int bc) {
        switch (bc) {
        case LRE:
        case LRO:
        case RLE:
        case RLO:
        case PDF:
        case BN:
            return true;
        default:
            return false;
        }
    }

    private static boolean isRetainedFormatting(int[] ca, int s, int e) {
        for (int i = s; i < e; i++) {
            if (!isRetainedFormatting(ca[i])) {
                return false;
            }
        }
        return true;
    }

    private static int max(int x, int y) {
        if (x > y) {
            return x;
        } else {
            return y;
        }
    }

    private static int[] getClasses(int[] chars) {
        int[] classes = new int [ chars.length ];
        int bc;
        for (int i = 0, n = chars.length; i < n; i++) {
            int ch = chars [ i ];
            if (ch >= 0) {
                bc = BidiClass.getBidiClass(chars [ i ]);
            } else {
                bc = SURROGATE;
            }
            classes [ i ] = bc;
        }
        return classes;
    }

    /**
     * Convert character sequence (a UTF-16 encoded string) to an array of unicode scalar values
     * expressed as integers. If a valid UTF-16 surrogate pair is encountered, it is converted to
     * two integers, the first being the equivalent unicode scalar  value, and the second being
     * negative one (-1). This special mechanism is used to track the use of surrogate pairs while
     * working with unicode scalar values, and permits maintaining indices that apply both to the
     * input UTF-16 and out scalar value sequences.
     * @return a boolean indicating that content is present that triggers bidirectional processing
     * @param cs a UTF-16 encoded character sequence
     * @param chars an integer array to accept the converted scalar values, where the length of the
     * array must be the same as the length of the input character sequence
     * @throws IllegalArgumentException if the input sequence is not a valid UTF-16 string, e.g.,
     * if it contains an isolated UTF-16 surrogate
     */
    private static boolean convertToScalar(CharSequence cs, int[] chars) throws IllegalArgumentException {
        boolean triggered = false;
        if (chars.length != cs.length()) {
            throw new IllegalArgumentException("characters array length must match input sequence length");
        }
        for (int i = 0, n = chars.length; i < n; ) {
            int chIn = cs.charAt(i);
            int chOut;
            if (chIn < 0xD800) {
                chOut = chIn;
            } else if (chIn < 0xDC00) {
                int chHi = chIn;
                int chLo;
                if ((i + 1) < n) {
                    chLo = cs.charAt(i + 1);
                    if ((chLo >= 0xDC00) && (chLo <= 0xDFFF)) {
                        chOut = convertToScalar(chHi, chLo);
                    } else {
                        throw new IllegalArgumentException("isolated high surrogate");
                    }
                } else {
                    throw new IllegalArgumentException("truncated surrogate pair");
                }
            } else if (chIn < 0xE000) {
                throw new IllegalArgumentException("isolated low surrogate");
            } else {
                chOut = chIn;
            }
            if (!triggered && triggersBidi(chOut)) {
                triggered = true;
            }
            if ((chOut & 0xFF0000) == 0) {
                chars [ i++ ] = chOut;
            } else {
                chars [ i++ ] = chOut;
                chars [ i++ ] = -1;
            }
        }
        return triggered;
    }

    /**
     * Convert UTF-16 surrogate pair to unicode scalar valuee.
     * @return a unicode scalar value
     * @param chHi high (most significant or first) surrogate
     * @param chLo low (least significant or second) surrogate
     * @throws IllegalArgumentException if one of the input surrogates is not valid
     */
    private static int convertToScalar(int chHi, int chLo) {
        if ((chHi < 0xD800) || (chHi > 0xDBFF)) {
            throw new IllegalArgumentException("bad high surrogate");
        } else if ((chLo < 0xDC00) || (chLo > 0xDFFF)) {
            throw new IllegalArgumentException("bad low surrogate");
        } else {
            return (((chHi & 0x03FF) << 10) | (chLo & 0x03FF)) + 0x10000;
        }
    }

    /**
     * Determine of character CH triggers bidirectional processing. Bidirectional
     * processing is deemed triggerable if CH is a strong right-to-left character,
     * an arabic letter or number, or is a right-to-left embedding or override
     * character.
     * @return true if character triggers bidirectional processing
     * @param ch a unicode scalar value
     */
    private static boolean triggersBidi(int ch) {
        switch (BidiClass.getBidiClass(ch)) {
        case R:
        case AL:
        case AN:
        case RLE:
        case RLO:
            return true;
        default:
            return false;
        }
    }

    private static void dump(String header, int[] chars, int[] classes, int defaultLevel, int[] levels) {
        log.debug(header);
        log.debug("BD: default level(" + defaultLevel + ")");
        StringBuffer sb = new StringBuffer();
        if (chars != null) {
            for (int i = 0, n = chars.length; i < n; i++) {
                int ch = chars [ i ];
                sb.setLength(0);
                if ((ch > 0x20) && (ch < 0x7F)) {
                    sb.append((char) ch);
                } else {
                    sb.append(CharUtilities.charToNCRef(ch));
                }
                for (int k = sb.length(); k < 12; k++) {
                    sb.append(' ');
                }
                sb.append(": " + padRight(getClassName(classes[i]), 4) + " " + levels[i]);
                log.debug(sb);
            }
        } else {
            for (int i = 0, n = classes.length; i < n; i++) {
                sb.setLength(0);
                for (int k = sb.length(); k < 12; k++) {
                    sb.append(' ');
                }
                sb.append(": " + padRight(getClassName(classes[i]), 4) + " " + levels[i]);
                log.debug(sb);
            }
        }
    }

    private static String getClassName(int bc) {
        switch (bc) {
        case L:                                     // left-to-right
            return "L";
        case LRE:                                   // left-to-right embedding
            return "LRE";
        case LRO:                                   // left-to-right override
            return "LRO";
        case R:                                     // right-to-left
            return "R";
        case AL:                                    // right-to-left arabic
            return "AL";
        case RLE:                                   // right-to-left embedding
            return "RLE";
        case RLO:                                   // right-to-left override
            return "RLO";
        case PDF:                                   // pop directional formatting
            return "PDF";
        case EN:                                    // european number
            return "EN";
        case ES:                                    // european number separator
            return "ES";
        case ET:                                    // european number terminator
            return "ET";
        case AN:                                    // arabic number
            return "AN";
        case CS:                                    // common number separator
            return "CS";
        case NSM:                                   // non-spacing mark
            return "NSM";
        case BN:                                    // boundary neutral
            return "BN";
        case B:                                     // paragraph separator
            return "B";
        case S:                                     // segment separator
            return "S";
        case WS:                                    // whitespace
            return "WS";
        case ON:                                    // other neutrals
            return "ON";
        case SURROGATE:                             // placeholder for low surrogate
            return "SUR";
        default:
            return "?";
        }
    }

    private static String padLeft(int n, int width) {
        return padLeft(Integer.toString(n), width);
    }

    private static String padLeft(String s, int width) {
        StringBuffer sb = new StringBuffer();
        for (int i = s.length(); i < width; i++) {
            sb.append(' ');
        }
        sb.append(s);
        return sb.toString();
    }

    /* not used yet
    private static String padRight ( int n, int width ) {
        return padRight ( Integer.toString ( n ), width );
    }
    */

    private static String padRight(String s, int width) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = sb.length(); i < width; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

}
