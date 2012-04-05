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

package org.apache.fop.text.linebreak;

/**
 * This class is meant for supporting the Unicode line breaking algorithm.
 * See: <a href="http://unicode.org/reports/tr14/">UTR 14</a>
 *
 */
public class LineBreakStatus {

    /** Constant indicating a Direct Break */
    public static final byte DIRECT_BREAK = LineBreakUtils.DIRECT_BREAK;
    /** Constant indicating an Indirect Break */
    public static final byte INDIRECT_BREAK = LineBreakUtils.INDIRECT_BREAK;
    /** Constant indicating a Combining Indirect Break */
    public static final byte COMBINING_INDIRECT_BREAK = LineBreakUtils.COMBINING_INDIRECT_BREAK;
    /** Constant indicating a Combining Prohibited Break */
    public static final byte COMBINING_PROHIBITED_BREAK = LineBreakUtils.COMBINING_PROHIBITED_BREAK;
    /** Constant indicating a Prohibited Break */
    public static final byte PROHIBITED_BREAK = LineBreakUtils.PROHIBITED_BREAK;
    /** Constant indicating a Explicit Break */
    public static final byte EXPLICIT_BREAK = LineBreakUtils.EXPLICIT_BREAK;

    private byte leftClass;
    private boolean hadSpace;

    /**
     * Resets the class to the same state as if new LineBreakStatus() had just been called.
     */
    public LineBreakStatus() {
        reset();
    }


    /**
     * Reset the status.
     * This method will reset the status to the initial state. It is meant
     * for recycling objects.
     */
    public void reset() {
        leftClass = -1;
        hadSpace = false;
    }

    /**
     * Check whether a line break may happen according to the rules described in
     * the <a href="http://unicode.org/reports/tr14/#Algorithm">Unicode Line Breaking
     * Algorithm</a>. The function returns the line breaking status of the point
     * <em>before</em> the given character.
     * The algorithm is the table-driven algorithm, as described in
     * <a href="http://unicode.org/reports/tr14/#PairBasedImplementation">
     * Unicode Technical Report #14</a>.
     * The pair table is taken from {@link LineBreakUtils}.
     *
     * TODO: Better handling for AI, SA, SG and XX line break classes.
     *
     * @param c the character to check
     * @return the break action to be taken
     *          one of: {@link #DIRECT_BREAK},
     *                  {@link #INDIRECT_BREAK},
     *                  {@link #COMBINING_INDIRECT_BREAK},
     *                  {@link #COMBINING_PROHIBITED_BREAK},
     *                  {@link #PROHIBITED_BREAK},
     *                  {@link #EXPLICIT_BREAK}
     */
    public byte nextChar(char c) {

        byte currentClass = LineBreakUtils.getLineBreakProperty(c);

        /* Initial conversions */
        switch (currentClass) {
            case 0: // Unassigned codepoint: same treatment as AI
            case LineBreakUtils.LINE_BREAK_PROPERTY_AI:
            case LineBreakUtils.LINE_BREAK_PROPERTY_SG:
            case LineBreakUtils.LINE_BREAK_PROPERTY_XX:
                // LB 1: Resolve AI, ... SG and XX into other line breaking classes
                //       depending on criteria outside the scope of this algorithm.
                //       In the absence of such criteria, it is recommended that
                //       classes AI, ... SG and XX be resolved to AL
                currentClass = LineBreakUtils.LINE_BREAK_PROPERTY_AL;
                break;

            case LineBreakUtils.LINE_BREAK_PROPERTY_SA:
                // LB 1: Resolve ... SA ... into other line breaking classes
                //       depending on criteria outside the scope of this algorithm.
                //       In the absence of such criteria, it is recommended that
                //       ... SA be resolved to AL, except that characters of
                //       class SA that have General_Category Mn or Mc be resolved to CM
                switch (Character.getType(c)) {
                    case Character.COMBINING_SPACING_MARK: //General_Category "Mc"
                    case Character.NON_SPACING_MARK: //General_Category "Mn"
                        currentClass = LineBreakUtils.LINE_BREAK_PROPERTY_CM;
                        break;
                    default:
                        currentClass = LineBreakUtils.LINE_BREAK_PROPERTY_AL;
                }

            default:
                //nop
        }

        /* Check 1: First character or initial character after a reset/mandatory break? */
        switch (leftClass) {
            case -1:
                //first character or initial character after a reset()
                leftClass = currentClass;
                if (leftClass == LineBreakUtils.LINE_BREAK_PROPERTY_CM) {
                    // LB 10: Treat any remaining combining marks as AL
                    leftClass = LineBreakUtils.LINE_BREAK_PROPERTY_AL;
                }
                // LB 2: Never break at the start of text
                return PROHIBITED_BREAK;

            case LineBreakUtils.LINE_BREAK_PROPERTY_BK:
            case LineBreakUtils.LINE_BREAK_PROPERTY_LF:
            case LineBreakUtils.LINE_BREAK_PROPERTY_NL:
                //first character after mandatory break
                // LB 4: Always break after hard line breaks
                // LB 5: Treat ... LF and NL has hard line breaks
                reset();
                leftClass = currentClass;
                return EXPLICIT_BREAK;

            case LineBreakUtils.LINE_BREAK_PROPERTY_CR:
                //first character after a carriage return:
                // LB 5: Treat CR followed by LF, as well as CR ... as hard line breaks
                // If current is LF, then fall through to Check 2 (see below),
                // and the hard break will be signaled for the character after LF (see above)
                if (currentClass != LineBreakUtils.LINE_BREAK_PROPERTY_LF) {
                    reset();
                    leftClass = currentClass;
                    return EXPLICIT_BREAK;
                }

            default:
                //nop
        }

        /* Check 2: current is a mandatory break or space? */
        switch (currentClass) {
            case LineBreakUtils.LINE_BREAK_PROPERTY_BK:
            case LineBreakUtils.LINE_BREAK_PROPERTY_LF:
            case LineBreakUtils.LINE_BREAK_PROPERTY_NL:
            case LineBreakUtils.LINE_BREAK_PROPERTY_CR:
                // LB 6: Do not break before a hard break
                leftClass = currentClass;
                return PROHIBITED_BREAK;

            case LineBreakUtils.LINE_BREAK_PROPERTY_SP:
                // LB 7: Do not break before spaces ...
                // Zero-width spaces are in the pair-table (see below)
                hadSpace = true;
                return PROHIBITED_BREAK;

            default:
                //nop
        }

        /* Normal treatment, if the first two checks did not return */
        boolean savedHadSpace = hadSpace;
        hadSpace = false;
        byte breakAction = LineBreakUtils.getLineBreakPairProperty(leftClass, currentClass);
        switch (breakAction) {
            case PROHIBITED_BREAK:
            case DIRECT_BREAK:
                leftClass = currentClass;
                return breakAction;

            case INDIRECT_BREAK:
                leftClass = currentClass;
                if (savedHadSpace) {
                    return INDIRECT_BREAK;
                } else {
                    return PROHIBITED_BREAK;
                }

            case COMBINING_INDIRECT_BREAK:
                if (savedHadSpace) {
                    leftClass = currentClass;
                    return COMBINING_INDIRECT_BREAK;
                } else {
                    return PROHIBITED_BREAK;
                }

            case COMBINING_PROHIBITED_BREAK:
                if (savedHadSpace) {
                    leftClass = currentClass;
                }
                return COMBINING_PROHIBITED_BREAK;

            default:
                assert false;
                return breakAction;
        }
    }

    /**
     * for debugging only
     */
    /*
    public static void main(String args[]) {
        LineBreakStatus lbs = new LineBreakStatus();
        lbs.nextChar('\n');
        lbs.nextChar('\n');
        lbs.nextChar('x');
    }
    */
}
