/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.area.Area;


/**
 * Evaluate and store the cost of breaking an Area at a given point.
 */
public class BreakCost {
    private Area breakArea;

    private int cost; // Will be more complicated than this!

    public BreakCost(Area breakArea, int cost) {
	this.breakArea = breakArea;
	this.cost = cost;
    }

    Area getArea() {
	return breakArea;
    }

    public BreakCost chooseLowest(BreakCost otherCost) {
	return this;
    }
}
