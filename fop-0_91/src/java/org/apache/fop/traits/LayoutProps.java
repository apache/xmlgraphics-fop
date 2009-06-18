/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 
package org.apache.fop.traits;

import org.apache.fop.datatypes.KeepValue;
import org.apache.fop.fo.Constants;

/**
 * Store properties affecting layout: break-before, break-after, keeps, span.
 * for a block level FO.
 * Public "structure" allows direct member access.
 */
public class LayoutProps {

    public int breakBefore; // enum constant BreakBefore.xxx
    public int breakAfter; // enum constant BreakAfter.xxx
    public KeepValue keepWithPrevious;  /*LF*/
    public KeepValue keepWithNext;      /*LF*/
    public KeepValue keepTogether;      /*LF*/
    public int orphans;                 /*LF*/
    public int widows;                  /*LF*/
    public int blockProgressionUnit;    /*LF*/
    public int lineStackingStrategy;    /*LF*/
    public boolean bIsSpan;
    public SpaceVal spaceBefore;
    public SpaceVal spaceAfter;

    private static final int[] BREAK_PRIORITIES =
        new int[]{ Constants.EN_AUTO, Constants.EN_COLUMN, Constants.EN_PAGE };


    public LayoutProps() {
        breakBefore = breakAfter = Constants.EN_AUTO;
        bIsSpan = false;
    }

    //     public static int higherBreak(int brkParent, int brkChild) {
    // if (brkParent == brkChild) return brkChild;
    // for (int i=0; i < s_breakPriorities.length; i++) {
    //     int bp = s_breakPriorities[i];
    //     if (bp == brkParent) return brkChild;
    //     else if (bp == brkChild) return brkParent;
    // }
    // return brkChild;
    //     }

    public void combineWithParent(LayoutProps parentLP) {
        if (parentLP.breakBefore != breakBefore) {
            for (int i = 0; i < BREAK_PRIORITIES.length; i++) {
                int bp = BREAK_PRIORITIES[i];
                if (bp == breakBefore) {
                    breakBefore = parentLP.breakBefore;
                    break;
                } else if (bp == parentLP.breakBefore) {
                    break;
                }
            }
        }
        // Parent span always overrides child span
        bIsSpan = parentLP.bIsSpan;
    }

    public String toString() {
        return "LayoutProps:\n" +
        "breakBefore = " + breakBefore + "; breakAfter = " + breakAfter + "\n" +
        "spaceBefore = " + ((spaceBefore != null) ? spaceBefore.toString() : "null") + "\n" +
        "spaceAfter = " + ((spaceAfter != null) ? spaceAfter.toString() : "null") + "\n" +
        "bIsSpan = " + bIsSpan + "\n";
    }
}

