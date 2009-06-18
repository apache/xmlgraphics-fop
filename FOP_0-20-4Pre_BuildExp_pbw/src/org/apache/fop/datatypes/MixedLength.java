/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

import java.util.Vector;
import java.util.Enumeration;

import org.apache.fop.fo.expr.Numeric;
import org.apache.fop.fo.expr.PropertyException;

/**
 * A length quantity in XSL which is specified with a mixture
 * of absolute and relative and/or percent components.
 * The actual value may not be computable before layout is done.
 */
public class MixedLength extends Length {

    private Vector lengths ;

    public MixedLength(Vector lengths) {
	this.lengths = lengths;
    }

    protected void computeValue() {
	int computedValue =0;
	boolean bAllComputed = true;
	Enumeration e = lengths.elements();
	while (e.hasMoreElements()) {
	    Length l = (Length)e.nextElement();
	    computedValue += l.mvalue();
	    if (! l.isComputed()) {
		bAllComputed = false;
	    }
        }
        setComputedValue(computedValue, bAllComputed);
    }


    public double getTableUnits() {
	double tableUnits = 0.0;
	Enumeration e = lengths.elements();
	while (e.hasMoreElements()) {
	    tableUnits += ((Length)e.nextElement()).getTableUnits();
        }
        return tableUnits;
    }

    public void resolveTableUnit(double dTableUnit) {
	Enumeration e = lengths.elements();
	while (e.hasMoreElements()) {
	    ((Length)e.nextElement()).resolveTableUnit(dTableUnit);
        }
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
	Enumeration e = lengths.elements();
	while (e.hasMoreElements()) {
	    if (sbuf.length()>0) {
		sbuf.append('+');
	    }
	    sbuf.append(e.nextElement().toString());
        }
	return sbuf.toString();
    }

    public Numeric asNumeric() {
	Numeric numeric = null;
	for (Enumeration e = lengths.elements(); e.hasMoreElements();) {
	    Length l = (Length)e.nextElement();
	    if (numeric == null) {
		numeric = l.asNumeric();
	    }
	    else {
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
