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
     * Check whether a line break may happen.
     * The function returns the line breaking status of the point before the given character. 
     * The algorithm is the table driven algorithm described in the Unicode
     * <a href="http://unicode.org/reports/tr14/#PairBasedImplementation">technical report #14</a>.
     * The pair table is taken from @see LineBreakUtils
     * 
     * TODO: Better handling for AI, SA, CB and other line break classes.
     * 
     * @param c The character.
     * @return the break action to be taken
     */
    public byte nextChar(char c) {
        byte currentClass = LineBreakUtils.getLineBreakProperty(c);
        if (currentClass == LineBreakUtils.LINE_BREAK_PROPERTY_AI
            || leftClass == LineBreakUtils.LINE_BREAK_PROPERTY_XX) {
            currentClass = LineBreakUtils.LINE_BREAK_PROPERTY_AL;
        } else if (currentClass == LineBreakUtils.LINE_BREAK_PROPERTY_NL) {
            currentClass = LineBreakUtils.LINE_BREAK_PROPERTY_BK;
        }
        if (leftClass == -1) {
            if (currentClass == LineBreakUtils.LINE_BREAK_PROPERTY_LF) {
                leftClass = LineBreakUtils.LINE_BREAK_PROPERTY_BK;
            } else {
                leftClass = currentClass;
                if (leftClass == LineBreakUtils.LINE_BREAK_PROPERTY_CM) {
                    leftClass = LineBreakUtils.LINE_BREAK_PROPERTY_ID;
                }
            }
            // LB 2a
            return PROHIBITED_BREAK;
        } else if (!(leftClass != LineBreakUtils.LINE_BREAK_PROPERTY_BK
                    && (leftClass != LineBreakUtils.LINE_BREAK_PROPERTY_CR
                        || currentClass == LineBreakUtils.LINE_BREAK_PROPERTY_LF)
                )) {
            reset();
            if (currentClass == LineBreakUtils.LINE_BREAK_PROPERTY_LF) {
                leftClass = LineBreakUtils.LINE_BREAK_PROPERTY_BK;
            }
            return EXPLICIT_BREAK;
        } else if (currentClass == LineBreakUtils.LINE_BREAK_PROPERTY_BK
                    || currentClass == LineBreakUtils.LINE_BREAK_PROPERTY_LF) {
            leftClass = LineBreakUtils.LINE_BREAK_PROPERTY_BK;
            return PROHIBITED_BREAK;
        } else if (currentClass == LineBreakUtils.LINE_BREAK_PROPERTY_CR) {
            leftClass = LineBreakUtils.LINE_BREAK_PROPERTY_CR;
            return PROHIBITED_BREAK;
        } else if (currentClass == LineBreakUtils.LINE_BREAK_PROPERTY_SP) {
            hadSpace = true;
            return PROHIBITED_BREAK;
        } else {
            boolean savedHadSpace = hadSpace;
            hadSpace = false;
            switch (LineBreakUtils.getLineBreakPairProperty(leftClass, currentClass)) {
                case LineBreakUtils.PROHIBITED_BREAK :
                    leftClass = currentClass;
                    return PROHIBITED_BREAK;
                case LineBreakUtils.DIRECT_BREAK :
                    leftClass = currentClass;
                    return DIRECT_BREAK;
                case LineBreakUtils.INDIRECT_BREAK :
                    leftClass = currentClass;
                    if (savedHadSpace) {
                        return INDIRECT_BREAK;
                    } else {
                        return PROHIBITED_BREAK;
                    }
                case LineBreakUtils.COMBINING_INDIRECT_BREAK :
                    if (savedHadSpace) {
                        leftClass = currentClass;
                        return COMBINING_INDIRECT_BREAK;
                    } else {
                        return PROHIBITED_BREAK;
                    }
                case LineBreakUtils.COMBINING_PROHIBITED_BREAK :
                    if (savedHadSpace) {
                        leftClass = currentClass;
                    }
                    return COMBINING_PROHIBITED_BREAK;
                default :
                    throw new RuntimeException("duh");
            }

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
