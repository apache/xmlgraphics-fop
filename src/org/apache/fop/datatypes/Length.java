/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.Property;
import org.apache.fop.messaging.MessageHandler;

/**
 * a length quantity in XSL
 */
public class Length {
    public static final Length AUTO = new Length(0);

    static {
        AUTO.bAuto = true;
    }

    protected int millipoints = 0;
    protected boolean bIsComputed = false;
    private boolean bAuto = false;

    /**
     * Set the length given a number of relative units and the current
     * font size in base units.
     */
    public Length(double numRelUnits, int iCurFontSize) {
        millipoints = (int)(numRelUnits * (double)iCurFontSize);
        setIsComputed(true);
    }

    /**
     * Set the length given a number of units and a unit name.
     */
    public Length(double numUnits, String units) {
        convert(numUnits, units);
    }

    /**
     * set the length as a number of base units
     */
    public Length(int baseUnits) {
        millipoints = baseUnits;
        setIsComputed(true);
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
            MessageHandler.errorln("unknown length unit '" + unit
                                   + "'");
        }
        this.millipoints = (int)(dvalue * 1000);
        setIsComputed(true);
    }

    protected void setIsComputed(boolean bIsComputed) {
        this.bIsComputed = bIsComputed;
    }

    /**
     * return the length in 1/1000ths of a point
     */
    public int mvalue() {
        if (!bIsComputed)
            millipoints = computeValue();
        return millipoints;
    }

    protected int computeValue() {
        return millipoints;
    }

    protected void setValue(int millipoints) {
        this.millipoints = millipoints;
        setIsComputed(true);
    }

    public boolean isAuto() {
        return bAuto;
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

    public String toString() {
        String s = millipoints + "mpt";
        return s;
    }

}
