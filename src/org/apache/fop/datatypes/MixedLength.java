/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

/**
 * A length quantity in XSL which is specified with a mixture
 * of absolute and relative and/or percent components.
 * The actual value may not be computable before layout is done.
 */
public class MixedLength extends Length {

    private PercentLength pcPart;

    /**
     * construct an object based on a factor (the percent, as a
     * a factor) and an object which has a method to return the
     * Length which provides the "base" for this calculation.
     */
    public MixedLength(int absPart, PercentLength pcPart) {
        super(absPart);
        this.pcPart = pcPart;
        super.setIsComputed(false);
    }

    protected int computeValue() {
        int rslt = super.computeValue();    // absolute part
        if (pcPart != null) {
            rslt += pcPart.computeValue();
        }
        return rslt;
    }

    public String toString() {
        // return the factor as a percent
        // What about the base value?
        StringBuffer rslt = new StringBuffer(super.toString());

        if (pcPart != null) {
            rslt.append("+" + pcPart.toString());
        }
        return rslt.toString();
    }

}
