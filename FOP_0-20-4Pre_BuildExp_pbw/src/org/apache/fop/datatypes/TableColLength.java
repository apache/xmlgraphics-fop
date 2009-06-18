/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.Numeric;

/**
 * A table-column width specification, possibly including some
 * number of proportional "column-units". The absolute size of a
 * column-unit depends on the fixed and proportional sizes of all
 * columns in the table, and on the overall size of the table.
 * It can't be calculated until all columns have been specified and until
 * the actual width of the table is known. Since this can be specified
 * as a percent of its parent containing width, the calculation is done
 * during layout.
 * NOTE: this is only supposed to be allowed if table-layout=fixed.
 */
public class TableColLength extends Length {

    /**
     * Number of table-column proportional units
     */
    double tcolUnits;

    /**
     * Construct an object with tcolUnits of proportional measure.
     */
    public TableColLength(double tcolUnits) {
        this.tcolUnits = tcolUnits;
    }



    /**
     * Override the method in Length to return the number of specified
     * proportional table-column units.
     */
    public double getTableUnits() {
        return tcolUnits;
    }

    /**
     * Calculate the number of millipoints and set it.
     */
    public void resolveTableUnit(double mpointsPerUnit) {
	setComputedValue((int)(tcolUnits * mpointsPerUnit));
    }

// 	If the table-unit can be resolved, set the computed value
//     protected void computeValue() {
// 	if (tblUnitBase.canResolveUnit()) {
// 	    rslt += (int)(tcolUnits * (double)tblUnitBase.getUnitValue());
// 	    setComputedValue(rslt);
// 	}
//     }


    public String toString() {
        return (Double.toString(tcolUnits) + " table-column-units");
    }

    public Numeric asNumeric() {
	return new Numeric(this);
    }
}
