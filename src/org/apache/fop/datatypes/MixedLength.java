/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

import java.util.ArrayList;

import org.apache.fop.fo.expr.Numeric;
import org.apache.fop.fo.expr.PropertyException;

/**
 * A length quantity in XSL which is specified with a mixture
 * of absolute and relative and/or percent components.
 * The actual value may not be computable before layout is done.
 */
public class MixedLength extends Length {

    private ArrayList lengths ;

    public MixedLength(ArrayList lengths) {
	this.lengths = lengths;
    }

    protected void computeValue() {
	int computedValue =0;
	boolean bAllComputed = true;
	for (int i = 0; i < lengths.size(); i++) {
	    Length l = (Length)lengths.get(i);
	    computedValue += l.mvalue();
	    if (! l.isComputed()) {
		bAllComputed = false;
	    }
        }
        setComputedValue(computedValue, bAllComputed);
    }

    public double getTableUnits() {
	double tableUnits = 0.0;
	for (int i = 0; i < lengths.size(); i++) {
	    tableUnits += ((Length)lengths.get(i)).getTableUnits();
        }
        return tableUnits;
    }

    public void resolveTableUnit(double dTableUnit) {
	for (int i = 0; i < lengths.size(); i++) {
	    ((Length)lengths.get(i)).resolveTableUnit(dTableUnit);
        }
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
	for (int i = 0; i < lengths.size(); i++) {
	    if (sbuf.length()>0) {
		sbuf.append('+');
	    }
	    sbuf.append(lengths.get(i).toString());
        }
	return sbuf.toString();
    }

    public Numeric asNumeric() {
	Numeric numeric = null;
	for (int i = 0; i < lengths.size(); i++) {
	    Length l = (Length)lengths.get(i);
	    if (numeric == null) {
		numeric = l.asNumeric();
	    } else {
		try {
		    Numeric sum = numeric.add(l.asNumeric());
		    numeric = sum;
		} catch (PropertyException pe) {
		    System.err.println("Can't convert MixedLength to Numeric: " +
				       pe);
		}
	    }
	}
	return numeric;
    }

}
