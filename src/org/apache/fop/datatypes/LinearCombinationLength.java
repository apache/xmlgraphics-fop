/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

import java.util.ArrayList;

public class LinearCombinationLength extends Length {

    protected ArrayList factors;
    protected ArrayList lengths;

    public LinearCombinationLength() {
        factors = new ArrayList();
        lengths = new ArrayList();
    }

    public void addTerm(double factor, Length length) {
        factors.add(new Double(factor));
        lengths.add(length);
    }

    /**
     * Return the computed value in millipoints.
     */
    public void computeValue() {
        int result = 0;
        int numFactors = factors.size();
        for (int i = 0; i < numFactors; ++i) {
            result +=
                (int)(((Double)factors.get(i)).doubleValue()
                      * (double)((Length)lengths.get(i)).mvalue());
        }
        setComputedValue(result);
    }

}
