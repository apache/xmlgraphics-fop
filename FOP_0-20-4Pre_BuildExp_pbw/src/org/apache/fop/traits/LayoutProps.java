/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.traits;

import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.properties.Constants;

/**
 * Store properties affecting layout: break-before, break-after, keeps, span.
 * for a block level FO.
 * Public "structure" allows direct member access.
 */
public class LayoutProps {
    public int breakBefore;  // enum constant BreakBefore.xxx
    public int breakAfter;   // enum constant BreakAfter.xxx
    public boolean bIsSpan;
    public SpaceVal spaceBefore;
    public SpaceVal spaceAfter;

    private static final int[] s_breakPriorities = new int[] {
	Constants.AUTO, Constants.COLUMN, Constants.PAGE };


    public LayoutProps() {
	breakBefore = breakAfter = Constants.AUTO;
	bIsSpan = false;
    }

//     public static int higherBreak(int brkParent, int brkChild) {
// 	if (brkParent == brkChild) return brkChild;
// 	for (int i=0; i < s_breakPriorities.length; i++) {
// 	    int bp = s_breakPriorities[i];
// 	    if (bp == brkParent) return brkChild;
// 	    else if (bp == brkChild) return brkParent;
// 	}
// 	return brkChild;
//     }

    public void combineWithParent(LayoutProps parentLP) {
	if (parentLP.breakBefore != breakBefore) {
	    for (int i=0; i < s_breakPriorities.length; i++) {
		int bp = s_breakPriorities[i];
		if (bp == breakBefore) {
		    breakBefore = parentLP.breakBefore;
		    break;
		}
		else if (bp == parentLP.breakBefore) break;
	    }
	}
	// Parent span always overrides child span
	bIsSpan = parentLP.bIsSpan;
    }
}
