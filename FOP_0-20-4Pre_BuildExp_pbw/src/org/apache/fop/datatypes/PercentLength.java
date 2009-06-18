/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.Numeric;

/**
 * a percent specified length quantity in XSL
 */
public class PercentLength extends Length {

    private double factor;
    private PercentBase lbase = null;

    /**
     * construct an object based on a factor (the percent, as a
     * a factor) and an object which has a method to return the
     * Length which provides the "base" for this calculation.
     */
    public PercentLength(double factor) {
        this(factor, null);
    }

    public PercentLength(double factor, PercentBase lbase) {
        this.factor = factor;
        this.lbase = lbase;
    }

    public void setBaseLength(PercentBase lbase) {
        this.lbase = lbase;
    }

    public PercentBase getBaseLength() {
        return this.lbase;
    }

    /**
     * Return the computed value in millipoints. This assumes that the
     * base length has been resolved to an absolute length value.
     */
    protected void computeValue() {
        setComputedValue((int)(factor * (double)lbase.getBaseLength()));
    }

    public double value() {
        return factor;
    }

    public String toString() {
        // return the factor as a percent
        // What about the base value?
        return (new Double(factor * 100.0).toString()) + "%";
    }

    public Numeric asNumeric() {
	return new Numeric(this);
    }

}
