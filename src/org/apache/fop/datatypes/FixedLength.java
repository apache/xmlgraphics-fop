/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.Property;
import org.apache.fop.fo.expr.Numeric;

/**
 * a length quantity in XSL
 */
public class FixedLength extends Length {

    /**
     * Set the length given a number of relative units and the current
     * font size in base units.
     */
    public FixedLength(double numRelUnits, int iCurFontSize) {
        setComputedValue((int)(numRelUnits * (double)iCurFontSize));
    }

    /**
     * Set the length given a number of units and a unit name.
     */
    public FixedLength(double numUnits, String units) {
        convert(numUnits, units);
    }

    /**
     * set the length as a number of base units
     */
    public FixedLength(int baseUnits) {
        setComputedValue(baseUnits);
    }

    /**
     * Convert the given length to a dimensionless integer representing
     * a whole number of base units (milli-points).
     */
    protected void convert(double dvalue, String unit) {

        int assumed_resolution = 1;    // points/pixel

        if (unit.equals("in"))
            dvalue = dvalue * 72;
        else if (unit.equals("cm"))
            dvalue = dvalue * 28.3464567;
        else if (unit.equals("mm"))
            dvalue = dvalue * 2.83464567;
        else if (unit.equals("pt"))
            dvalue = dvalue;
        else if (unit.equals("pc"))
            dvalue = dvalue * 12;
            /*
             * else if (unit.equals("em"))
             * dvalue = dvalue * fontsize;
             */
        else if (unit.equals("px"))
            dvalue = dvalue * assumed_resolution;
        else {
            dvalue = 0;
            //log.error("unknown length unit '" + unit
            //                       + "'");
        }
        setComputedValue((int)(dvalue * 1000));
    }

    public Numeric asNumeric() {
	return new Numeric(this);
    }
}
