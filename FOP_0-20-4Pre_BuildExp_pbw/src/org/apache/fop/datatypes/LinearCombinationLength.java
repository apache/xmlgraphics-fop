/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

import java.util.Vector;

public class LinearCombinationLength extends Length {

    protected Vector factors;
    protected Vector lengths;

    public LinearCombinationLength() {
        factors = new Vector();
        lengths = new Vector();
    }

    public void addTerm(double factor, Length length) {
        factors.addElement(new Double(factor));
        lengths.addElement(length);
    }

    /**
     * Return the computed value in millipoints.
     */
    protected void computeValue() {
        int result = 0;
        int numFactors = factors.size();
        for (int i = 0; i < numFactors; ++i) {
            result +=
                (int)(((Double)factors.elementAt(i)).doubleValue()
                      * (double)((Length)lengths.elementAt(i)).mvalue());
        }
        setComputedValue(result);
    }

}
