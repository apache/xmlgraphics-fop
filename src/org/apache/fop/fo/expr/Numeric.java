/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.expr;

import java.util.Vector;

import org.apache.fop.fo.Property;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.FixedLength;
import org.apache.fop.datatypes.PercentLength;
import org.apache.fop.datatypes.LinearCombinationLength;
import org.apache.fop.datatypes.MixedLength;
import org.apache.fop.datatypes.TableColLength;
import org.apache.fop.datatypes.PercentBase;

/**
 * Represents a "numeric" value as defined by the XSL FO Specification.
 * This consists of one or more kinds of value specifications, from
 * absolute numbers (units power of 0) to lengths (unit power of 1),
 * relative lengths (ems), percentage lengths.
 * A Numeric can be constructed from other Property types representing
 * Numbers or Length-type values.
 * Numeric provides methods to return Number and Length values based on
 * its current value.
 * It supports basic arithmetic operations involving Numerics.
 */
public class Numeric {
    // Bit fields
    public static final int ABS_LENGTH = 1;     // abs units (or number)
    public static final int PC_LENGTH = 2;      // Percentage
    public static final int TCOL_LENGTH = 4;    // Table units

    private int valType;
    private double absValue;
    private double pcValue;
    private PercentBase pcBase = null;    // base value for PC_LENGTH component
    private double tcolValue;
    private int dim;


    /**
     * Construct a Numeric object by specifying one or more components,
     * including absolute length, percent length, table units.
     * @param valType A combination of bits representing the value types.
     * @param absValue The value of a Number or resolved Length value if
     * the ABS_LENGTH flag is set.
     * @param pcValue The decimal percent value if the PC_LENGTH flag is set
     * @param tcolValue The decimal table unit value if the TCOL_LENGTH flag
     * is set.
     * @param dim The dimension of the value. 0 for a Number, 1 for a Length
     * (any type), >1, <0 if Lengths have been multiplied or divided.
     * @pcBase The PercentBase object used to calculate an actual value for
     * a PC_LENGTH.
     */
    protected Numeric(int valType, double absValue, double pcValue,
                      double tcolValue, int dim, PercentBase pcBase) {
        this.valType = valType;
        this.absValue = absValue;
        this.pcValue = pcValue;
        this.tcolValue = tcolValue;
        this.dim = dim;
        this.pcBase = pcBase;
    }

    /**
     * Construct a Numeric object of dimension 0 from a double.
     * @param valType A combination of bits representing the value types.
     * @param absValue The value of a Number or resolved Length value.
     */

    /**
     * *
     * protected Numeric(int valType, double absValue) {
     * this.valType = valType;
     * this.absValue = absValue;
     * }
     */

    /**
     * Construct a Numeric object from a Number.
     * @param num The number.
     */
    public Numeric(Number num) {
        this(ABS_LENGTH, num.doubleValue(), 0.0, 0.0, 0, null);
    }

    /**
     * Construct a Numeric object from a Length.
     * @param l The Length.
     */
    public Numeric(FixedLength l) {
        this(ABS_LENGTH, (double)l.mvalue(), 0.0, 0.0, 1, null);
    }

    /**
     * Construct a Numeric object from a PercentLength.
     * @param pclen The PercentLength.
     */
    public Numeric(PercentLength pclen) {
        this(PC_LENGTH, 0.0, pclen.value(), 0.0, 1, pclen.getBaseLength());
    }

    /**
     * Construct a Numeric object from a TableColLength.
     * @param tclen The TableColLength.
     */
    public Numeric(TableColLength tclen) {
        this(TCOL_LENGTH, 0.0, 0.0, tclen.getTableUnits(), 1, null);
    }


    /**
     * Return the current value as a Length if possible. This constructs
     * a new Length or Length subclass based on the current value type
     * of the Numeric.
     * If the stored value has a unit dimension other than 1, null
     * is returned.
     */
    public Length asLength() {
        if (dim == 1) {
	    Vector len = new Vector(3);
            if ((valType & ABS_LENGTH) != 0) {
                len.add(new FixedLength((int)absValue));
            }
            if ((valType & PC_LENGTH) != 0) {
                len.add(new PercentLength(pcValue, pcBase));
            }
            if ((valType & TCOL_LENGTH) != 0) {
                len.add(new TableColLength(tcolValue));
            }
	    if (len.size() == 1) {
		return (Length)len.elementAt(0);
	    }
	    else {
		return new MixedLength(len);
	    }
        } else {
            // or throw exception???
            // can't make Length if dimension != 1
            return null;
        }
    }

    /**
     * Return the current value as a Number if possible.
     * Calls asDouble().
     */
    public Number asNumber() {
        return asDouble();
    }

    public Double asDouble() {
        if (dim == 0 && valType == ABS_LENGTH) {
            return new Double(absValue);
        } else {
            // or throw exception???
            // can't make Number if dimension != 0
            return null;
        }
    }

    /**
     * Return the current value as a Integer if possible.
     * If the unit dimension is 0 and the value type is ABSOLUTE, an Integer
     * is returned. Otherwise null is returned. Note: the current value is
     * truncated if necessary to make an integer value.
     */

    /**
     * public Integer asInteger() {
     * if (dim == 0 && valType==ABS_LENGTH) {
     * return new Integer((int)absValue);
     * }
     * else {
     * // or throw exception???
     * // can't make Number if dimension != 0
     * return null;
     * }
     * }
     */

    /**
     * Return a boolean value indiciating whether the currently stored
     * value consists of different "types" of values (absolute, percent,
     * and/or table-unit.)
     */
    private boolean isMixedType() {
        int ntype = 0;
        for (int t = valType; t != 0; t = t >> 1) {
            if ((t & 1) != 0)
                ++ntype;
        }
        return ntype > 1;
    }

    /**
     * Subtract the operand from the current value and return a new Numeric
     * representing the result.
     * @param op The value to subtract.
     * @return A Numeric representing the result.
     * @throws PropertyException If the dimension of the operand is different
     * from the dimension of this Numeric.
     */
    public Numeric subtract(Numeric op) throws PropertyException {
        // Check of same dimension
        // Add together absolute and table units
        // What about percentages??? Treat as colUnits if they can't be
        // in same property!
        if (dim == op.dim) {
            PercentBase npcBase = ((valType & PC_LENGTH) != 0) ? pcBase
                                  : op.pcBase;
            // Subtract each type of value
            return new Numeric(valType | op.valType, absValue - op.absValue,
                               pcValue - op.pcValue,
                               tcolValue - op.tcolValue, dim, npcBase);
        } else {
            throw new PropertyException("Can't add Numerics of different dimensions");
        }
    }

    /**
     * Add the operand from the current value and return a new Numeric
     * representing the result.
     * @param op The value to add.
     * @return A Numeric representing the result.
     * @throws PropertyException If the dimension of the operand is different
     * from the dimension of this Numeric.
     */
    public Numeric add(Numeric op) throws PropertyException {
        // Check of same dimension
        // Add together absolute and table units
        // What about percentages??? Treat as colUnits if they can't be
        // in same property!
        if (dim == op.dim) {
            PercentBase npcBase = ((valType & PC_LENGTH) != 0) ? pcBase
                                  : op.pcBase;
            // Add each type of value
            return new Numeric(valType | op.valType, absValue + op.absValue,
                               pcValue + op.pcValue,
                               tcolValue + op.tcolValue, dim, npcBase);
        } else {
            throw new PropertyException("Can't add Numerics of different dimensions");
        }
    }

    /**
     * Multiply the the current value by the operand and return a new Numeric
     * representing the result.
     * @param op The multiplier.
     * @return A Numeric representing the result.
     * @throws PropertyException If both Numerics have "mixed" type.
     */
    public Numeric multiply(Numeric op) throws PropertyException {
        // Multiply together absolute units and add dimensions (exponents)
        // What about percentages??? Treat as colUnits if they can't be
        // in same property!
        if (dim == 0) {
            // This is a dimensionless quantity, ie. a "Number"
            return new Numeric(op.valType, absValue * op.absValue,
                               absValue * op.pcValue,
                               absValue * op.tcolValue, op.dim, op.pcBase);
        } else if (op.dim == 0) {
            double opval = op.absValue;
            return new Numeric(valType, opval * absValue, opval * pcValue,
                               opval * tcolValue, dim, pcBase);
        } else if (valType == op.valType &&!isMixedType()) {
            // Check same relbase and pcbase ???
            PercentBase npcBase = ((valType & PC_LENGTH) != 0) ? pcBase
                                  : op.pcBase;
            return new Numeric(valType, absValue * op.absValue,
                               pcValue * op.pcValue,
                               tcolValue * op.tcolValue, dim + op.dim,
                               npcBase);
        } else {
            throw new PropertyException("Can't multiply mixed Numerics");
        }
    }

    /**
     * Divide the the current value by the operand and return a new Numeric
     * representing the result.
     * @param op The divisor.
     * @return A Numeric representing the result.
     * @throws PropertyException If both Numerics have "mixed" type.
     */
    public Numeric divide(Numeric op) throws PropertyException {
        // Multiply together absolute units and add dimensions (exponents)
        // What about percentages??? Treat as colUnits if they can't be
        // in same property!
        if (dim == 0) {
            // This is a dimensionless quantity, ie. a "Number"
            return new Numeric(op.valType, absValue / op.absValue,
                               absValue / op.pcValue,
                               absValue / op.tcolValue, -op.dim, op.pcBase);
        } else if (op.dim == 0) {
            double opval = op.absValue;
            return new Numeric(valType, absValue / opval, pcValue / opval,
                               tcolValue / opval, dim, pcBase);
        } else if (valType == op.valType &&!isMixedType()) {
            PercentBase npcBase = ((valType & PC_LENGTH) != 0) ? pcBase
                                  : op.pcBase;
            return new Numeric(valType,
                               (valType == ABS_LENGTH ? absValue / op.absValue : 0.0),
                               (valType == PC_LENGTH ? pcValue / op.pcValue : 0.0),
                               (valType == TCOL_LENGTH ? tcolValue / op.tcolValue : 0.0),
                               dim - op.dim, npcBase);
        } else {
            throw new PropertyException("Can't divide mixed Numerics.");
        }
    }

    /**
     * Return the absolute value of this Numeric.
     * @return A new Numeric object representing the absolute value.
     */
    public Numeric abs() {
        return new Numeric(valType, Math.abs(absValue), Math.abs(pcValue),
                           Math.abs(tcolValue), dim, pcBase);
    }

    /**
     * Return a Numeric which is the maximum of the current value and the
     * operand.
     * @throws PropertyException If the dimensions or value types of the
     * object and the operand are different.
     */
    public Numeric max(Numeric op) throws PropertyException {
        double rslt = 0.0;
        // Only compare if have same dimension and value type!
        if (dim == op.dim && valType == op.valType &&!isMixedType()) {
            if (valType == ABS_LENGTH)
                rslt = absValue - op.absValue;
            else if (valType == PC_LENGTH)
                rslt = pcValue - op.pcValue;
            else if (valType == TCOL_LENGTH)
                rslt = tcolValue - op.tcolValue;
            if (rslt > 0.0)
                return this;
            else
                return op;
        }
        throw new PropertyException("Arguments to max() must have same dimension and value type.");
    }

    /**
     * Return a Numeric which is the minimum of the current value and the
     * operand.
     * @throws PropertyException If the dimensions or value types of the
     * object and the operand are different.
     */
    public Numeric min(Numeric op) throws PropertyException {
        double rslt = 0.0;
        // Only compare if have same dimension and value type!
        if (dim == op.dim && valType == op.valType &&!isMixedType()) {
            if (valType == ABS_LENGTH)
                rslt = absValue - op.absValue;
            else if (valType == PC_LENGTH)
                rslt = pcValue - op.pcValue;
            else if (valType == TCOL_LENGTH)
                rslt = tcolValue - op.tcolValue;
            if (rslt > 0.0)
                return op;
            else
                return this;
        }
        throw new PropertyException("Arguments to min() must have same dimension and value type.");
    }

}
