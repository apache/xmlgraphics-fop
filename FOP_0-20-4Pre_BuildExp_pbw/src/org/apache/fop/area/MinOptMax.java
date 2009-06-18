/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

/**
 * This class holds the resolved (as mpoints) form of a LengthRange or
 * Space type Property value.
 * MinOptMax values are used during layout calculations. The instance
 * variables are package visible.
 */

public class MinOptMax implements java.io.Serializable, Cloneable {

    /** Publicly visible min(imum), opt(imum) and max(imum) values.*/
    public int min;
    public int opt;
    public int max;

    public MinOptMax() {
	this(0);
    }

    public MinOptMax(int val) {
	this(val, val, val);
    }

    public MinOptMax(int min, int opt, int max) {
	this.min = min;
	this.opt = opt;
	this.max = max;
    }

    public Object clone() {
	try {
	    return super.clone();
	} catch (CloneNotSupportedException ex) {
	    // SHOULD NEVER OCCUR - all members are primitive types!
	    return null;
	}
    }

    public static MinOptMax subtract(MinOptMax op1, MinOptMax op2) {
	return new MinOptMax(op1.min - op2.max, op1.opt - op2.opt,
			     op1.max - op2.min);
    }

    public static MinOptMax add(MinOptMax op1, MinOptMax op2) {
	return new MinOptMax(op1.min + op2.min, op1.opt + op2.opt,
			     op1.max + op2.max);
    }

    public static MinOptMax multiply(MinOptMax op1, double mult) {
	return new MinOptMax((int)(op1.min * mult),
			     (int)(op1.opt * mult),
			     (int)(op1.max * mult));
    }

    public void add(MinOptMax op) {
	min += op.min;
	opt += op.opt;
	max += op.max;
    }

    public void subtract(MinOptMax op) {
	min -= op.max;
	opt -= op.opt;
	max -= op.min;
    }


}
