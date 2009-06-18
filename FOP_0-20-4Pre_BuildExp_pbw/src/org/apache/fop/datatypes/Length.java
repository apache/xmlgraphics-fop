/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.Numeric;
import org.apache.fop.fo.Property;

/**
 * a length quantity in XSL
 */
public class Length {

    protected int millipoints = 0;
    protected boolean bIsComputed = false;

    /**
     * return the length in 1/1000ths of a point
     */
    public int mvalue() {
        if (!bIsComputed) {
            computeValue();
	}
        return millipoints;
    }

    protected void computeValue() {
    }


    protected void setComputedValue(int millipoints) {
	setComputedValue(millipoints, true);
    }

    protected void setComputedValue(int millipoints, boolean bSetComputed) {
        this.millipoints = millipoints;
        this.bIsComputed = bSetComputed;
    }

    public boolean isAuto() {
        return false;
    }

    public boolean isComputed() {
	return this.bIsComputed;
    }

    /**
     * Return the number of table units which are included in this
     * length specification.
     * This will always be 0 unless the property specification used
     * the proportional-column-width() function (only only table
     * column FOs).
     * <p>If this value is not 0, the actual value of the Length cannot
     * be known without looking at all of the columns in the table to
     * determine the value of a "table-unit".
     * @return The number of table units which are included in this
     * length specification.
     */
    public double getTableUnits() {
        return 0.0;
    }

    public void resolveTableUnit(double dTableUnit) {
    }

    public Numeric asNumeric() {
	return null;
    }

    public String toString() {
        String s = millipoints + "mpt";
        return s;
    }

}
