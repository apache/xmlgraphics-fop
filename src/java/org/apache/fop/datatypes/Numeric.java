/*
 * $Id$
 *
 * 
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;


/**
 * An abstraction of "numeric" values as defined by the XSL FO Specification.
 * Numerics include absolute numbers, absolute lengths and relative lengths
 * (percentages and ems).
 * <p>
 * Relative lengths are converted immediately to a pure numeric factor, i.e.
 * an absolute number (a number with unit power zero.)  They retain a
 * baseunit of PERCENTAGE or EMS respectively.
 * <p>
 * Relative lengths resolve to absolute lengths as soon as they are involved
 * in a multop with any Numeric with a baseunit of MILLIPOINTS.
 * <p>
 * Therefore, only a number, its power and its baseunit need be provided for
 * in this class.
 * All numeric values are represented as a value and a unit raised to a
 * power.  For absolute numbers and relative lengths the unit power is zero.
 *
 * Whenever the power associated with a number is non-zero, it is a length.
 * N.B. this includes relative lengths.  The resolution of a relative
 * length is NOT performed by multiplying the relative length by its
 * reference length in a standard * operation, but by specific methods.
 * This allows invalid multiplication operations (any multiplication
 * of a relative length is invalid in an expression.)
 * <p>
 * It is an error for the end result of an expression to be a numeric with
 * a power other than 0 or 1. (Rec. 5.9.6)
 * <pre>
 * Operations defined on combinations of the types are (where
 *   length    = MILLIPOINTS
 *   number    = NUMBER
 *   rel-len   = ( PERCENTAGE | EMS )
 *   notnumber = ( length | rel-len )
 *   absunit   = ( number | length )
 *   numeric   = ( number | notnumber )
 * )
 * numeric^n   addop  numeric^m   = Illegal
 * numeric1    addop  numeric2    = Illegal
 * rel-len1    addop  any         = Illegal  See 5.9.6 Absolute Numerics
 * "...only the mod, addition, and subtraction operators require that the
 * "numerics on either side of the operation be absolute numerics of the
 * "same unit power."
 *
 * absnum1^n   addop  absnum1^n  = absnum1^n   includes number + number
 *
 * number      multop anyunit     = anyunit      universal multiplier
 *                                               includes number * relunit
 *
 * unit1       multop unit2       = Illegal      includes relunit * length
 *
 * relunit     multop relunit     = Illegal
 *
 * unit1^n     multop unit1^m     = unit1^(n+m)  includes number * number
 *                                               excludes relunit* relunit
 *
 * In fact, all lengths are maintained internally
 * in millipoints.
 */
public class Numeric extends AbstractPropertyValue implements Cloneable {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * Integer constant encoding a valid Numeric subclass
     * base unit
     */
    public static final int
             NUMBER = 1
        ,PERCENTAGE = 2
               ,EMS = 4
       ,MILLIPOINTS = 8

     ,LAST_BASEUNIT = MILLIPOINTS
                  ;

    /**
     * Integer constants for the subunits of numbers.
     */
    public static final int NUMERIC = NUMBER | PERCENTAGE | EMS;

    /**
     * Integer constants for the subunits of relative lengths.
     */
    public static final int REL_LENGTH = PERCENTAGE | EMS;

    /**
     * Integer constants for the absolute-valued units.
     */
    public static final int ABS_UNIT = NUMBER | MILLIPOINTS;

    /**
     * Integer constants for non-numbers.
     */
    public static final int NOT_NUMBER = MILLIPOINTS | REL_LENGTH;

    /**
     * Integer constants for distances.
     */
    public static final int DISTANCE = MILLIPOINTS | REL_LENGTH;

    /**
     * The numerical contents of this instance.
     */
    protected double value;

    /**
     * The power to which the Numeric is raised
     */
    protected int power;

    /**
     * The current baseunit of this instance.
     * One of the constants defined here.  Defaults to millipoints.
     */
    private int baseunit = MILLIPOINTS;

    /**
     * The baseunit in which this <tt>Numeric</tt> was originally defined.
     */
    private int originalBaseUnit = MILLIPOINTS;

    /**
     * The actual unit in which this <tt>Numeric</tt> was originally defined.
     * This is a constant defined in each of the original unit types
     * (currently only Length).
     */
    private int originalUnit = Length.PT;

    /**
     * Construct a fully specified <tt>Numeric</tt> object.
     * @param property <tt>int</tt> index of the property.
     * @param value the actual value.
     * @param baseunit the baseunit for this <tt>Numeric</tt>.
     * @param power The dimension of the value. 0 for a Number,
     * 0 for EMs or Percentage, 1 for a Length (any type),
     * >1, <0 if Lengths have been multiplied or divided.
     * @param unit <tt>int</tt> enumeration of the subtype of the
     * <i>baseunit</i> in which this <i>Numeric</i> is being defined
     * e.g. Length.PX or Length.MM.
     */
    protected Numeric
        (int property, double value, int baseunit, int power, int unit)
        throws PropertyException
    {
        super(property, PropertyValue.NUMERIC);
        // baseunit must be a power of 2 <= the LAST_BASEUNIT
        if ((baseunit & (baseunit - 1)) != 0
            || baseunit > LAST_BASEUNIT)
            throw new PropertyException
                    ("Invalid baseunit: " + baseunit);
        if ((baseunit & NUMERIC) != 0 && power != 0)
            throw new PropertyException
                    ("Invalid power for NUMERIC: " + power);

        this.value = value;
        this.power = power;
        this.baseunit = baseunit;
        originalBaseUnit = baseunit;
        originalUnit = unit;
    }

    /**
     * Construct a fully specified <tt>Numeric</tt> object.
     * @param propertyName <tt>String</tt> name of the property.
     * @param value the actual value.
     * @param baseunit the baseunit for this <tt>Numeric</tt>.  Because a
     * <i>power</i> is being specified, <i>NUMBER</i> baseunits are invalid.
     * @param power The dimension of the value. 0 for a Number, 1 for a Length
     * (any type), >1, <0 if Lengths have been multiplied or divided.
     * @param unit <tt>int</tt> enumeration of the subtype of the
     * <i>baseunit</i> in which this <i>Numeric</i> is being defined.
     */
    protected Numeric (String propertyName, double value, int baseunit,
                       int power, int unit)
        throws PropertyException
    {
        this(PropNames.getPropertyIndex(propertyName),
             value, baseunit, power, unit);
    }

    /**
     * Construct a <tt>Numeric</tt> object of dimension 0 from a
     * <tt>double</tt>.
     * @param propertyName <tt>String</tt> property name.
     * @param value the number as a <tt>double</tt>.
     */
    public Numeric(String propertyName, double value)
        throws PropertyException
    {
         this(PropNames.getPropertyIndex(propertyName), value);
    }

    /**
     * Construct a <tt>Numeric</tt> object of dimension 0 from a
     * <tt>double</tt>.
     * @param property <tt>int</tt> property index.
     * @param value the number as a <tt>double</tt>.
     */
    public Numeric(int property, double value) throws PropertyException {
        this(property, value, NUMBER, 0, 0);
    }

    /**
     * Construct a <tt>Numeric</tt> object of dimension 0 from a
     * <tt>long</tt>.
     * @param propertyName <tt>String</tt> property name.
     * @param value the number as a <tt>long</tt>.
     */
    public Numeric(String propertyName, long value)
        throws PropertyException
    {
        this(propertyName, (double)value);
    }

    /**
     * Construct a <tt>Numeric</tt> object of dimension 0 from a
     * <tt>long</tt>.
     * @param property <tt>int</tt> property index.
     * @param value the number as a <tt>long</tt>.
     */
    public Numeric(int property, long value)throws PropertyException {
        this(property, (double)value);
    }

    /**
     * Construct a <tt>Numeric</tt> object from a Number.
     * @param propertyName <tt>String</tt> property name.
     * @param num an absolute number.
     */
    public Numeric(String propertyName, Number num)
        throws PropertyException
    {
        this(propertyName, num.doubleValue());
    }

    /**
     * Construct a <tt>Numeric</tt> object from a Number.
     * @param property <tt>int</tt> property index.
     * @param num an absolute number.
     */
    public Numeric(int property, Number num) throws PropertyException {
        this(property, num.doubleValue());
    }

    /**
     * @return <tt>double</tt> value of the <i>Numeric</i>.
     */
    public double getValue() {
        return value;
    }

    /**
     * Set the value.  This used on <tt>Numeric</tt> clones in the <i>abs</i>
     * <i>min</i> and <i>max</i> operations.
     * @param value - the <tt>double</tt> value.
     */
    protected void setValue(double value) {
        this.value = value;
    }

    /**
     * @return <tt>int</tt> unit power of this <i>Numeric</i>.
     */
    public int getPower() {
        return power;
    }

    /**
     * @return <tt>int</tt> current baseunit of this <i>Numeric</i>.
     */
    public int getBaseunit() {
        return baseunit;
    }

    /**
     * @return <tt>int</tt> original base unit of this <i>Numeric</i>.
     */
    public int getOriginalBaseUnit() {
        return originalBaseUnit;
    }

    /**
     * @return <tt>int</tt> original unit in which this <i>Numeric</i> was
     * defined.  Value is defined in terms of the originalBaseUnit type.
     */
    public int getOriginalUnit() {
        return originalUnit;
    }

    /**
     * Validate this <i>Numeric</i>.
     * @exception PropertyException if this numeric is invalid for
     * the associated property.
     */
    public void validate() throws PropertyException {
        switch (baseunit) {
        case NUMBER:
            if (power != 0)
                throw new PropertyException
                        ("Attempt to validate Numeric with unit power of "
                         + power);
            super.validate(Property.NUMBER);
            break;
        case PERCENTAGE:
            if (power != 0)
                throw new PropertyException
                        ("Attempt to validate Percentage with unit power of "
                         + power);
            super.validate(Property.PERCENTAGE);
            break;
        case MILLIPOINTS:
            super.validate(Property.LENGTH);
            if (power != 1)
                throw new PropertyException
                        ("Length with unit power " + power);
            break;
        default: 
            throw new PropertyException
                    ("Unrecognized baseunit type: " + baseunit);
        }
    }

    /**
     * This object has a NUMERIC type if it is a NUMBER, EMS or PERCENTAGE
     * type.
     */
    public boolean isNumeric() {
        return (baseunit & NUMERIC) != 0;
    }

    /**
     * This object is an ABSOLUTE NUMERIC type if it is a NUMBER or an
     * ABSOLUTE LENGTH.
     */
    public boolean isAbsoluteNumeric() {
        return (baseunit & ABS_UNIT) != 0;
    }

    /**
     * This object is a number if the baseunit is NUMBER.  Power is
     * guaranteed to be zero for NUMBER baseunit.
     */
    public boolean isNumber() {
        return (baseunit == NUMBER);
    }

    /**
     * This object is an integer if it is a number and the
     * rounded value is equal to the value.
     */
    public boolean isInteger() {
        return (isNumber() && (Math.round(value)) == value);
    }

    /**
     * This object is an EMS factor if the baseunit is EMS.  Power is
     * guaranteed to be zero for EMS baseunit.
     */
    public boolean isEms() {
        return (baseunit == EMS);
    }

    /**
     * This object is a percentage factor if the baseunit is PERCENTAGE.
     * Power is guaranteed to be zero for PERCENTAGE baseunit.
     */
    public boolean isPercentage() {
        return (baseunit == PERCENTAGE);
    }

    /**
     * This object is a length in millipoints. Same as isAbsLength().
     */
    public boolean isLength() {
        return (baseunit == MILLIPOINTS && power == 1);
    }

    /**
     * This object is a length in millipoints.
     */
    public boolean isAbsLength() {
        return (baseunit == MILLIPOINTS && power == 1);
    }

    /**
     * This object is an absolute or relative length.  I.e., it has a
     * baseunit of MILLIPOINTS with a power of 1, or a baseunit of
     * either PERCENTAGE or EMS.
     */
    public boolean isAbsOrRelLength() {
        return ((baseunit == MILLIPOINTS && power == 1)
                    || baseunit == PERCENTAGE || baseunit == EMS);
    }

    /**
     * This object is a distance; a absolute or relative length
     */
    public boolean isDistance() {
        return (baseunit & DISTANCE) != 0;
    }

    /**
     * Return this <tt>Numeric</tt> converted (if necessary) to a
     * <tt>double</tt>.  The
     * <i>value</i> field, a double, is returned unchanged.  To check
     * whether this is a good thing to do, call <i>isNumber()</i>.
     * @return a <tt>double</tt> primitive type.
     */
    public double asDouble() {
        return value;
    }

    /**
     * Return the current value as a <tt>long</tt>.
     * The <tt>double</tt> <i>value</i> is converted to an <tt>long</tt> and
     * returned.  No other checking or conversion occurs.
     */
     public long asLong() {
         return (long)value;
     }

    /**
     * Return the current value as an <tt>int</tt>.
     * The <tt>double</tt> <i>value</i> is converted to an <tt>int</tt> and
     * returned.  No other checking or conversion occurs.
     */
     public int asInt() {
         return (int)value;
     }

    /**
     * @param fontSize a <tt>Numeric</tt> containing the reference
     * <i>font-size</i> length
     * @return <i>this</i>, with values changed to reflect the conversion
     * @exception PropertyException
     */
    public Numeric expandEms(Numeric fontSize) throws PropertyException {
        if (baseunit == EMS) {
            value = value *= fontSize.getValue();
            power = power += fontSize.getPower();
            baseunit = fontSize.getBaseunit();
            if (isLength()) return this;

            throw new PropertyException
                    ("Invalid result from expandEms: " + value + power
                     + getBaseunitString());
        }
        throw new PropertyException("Target of expandEms not EMs");
    }

    /**
     * @param ref a <tt>Numeric</tt> containing the reference length
     * @return <i>this</i>, with values changed to reflect the conversion
     * @exception PropertyException
     */
    public Numeric expandPercent(Numeric ref) throws PropertyException {
        if (baseunit == PERCENTAGE) {
            value = value *= ref.getValue();
            power = power += ref.getPower();
            baseunit = ref.getBaseunit();
            if (isLength()) return this;

            throw new PropertyException
                    ("Invalid result from expandPercent: " + value + power
                     + getBaseunitString());
        }
        throw new PropertyException("Target of expandPercent not percentage");
    }

    /**
     * Subtract the operand from the current value.
     * @param op The value to subtract.
     * @return <i>Numeric</i>; this object.
     * @throws PropertyException If the unit power of the operands is different
     */
    public Numeric subtract(Numeric op) throws PropertyException {
        // Check of same dimension
        if (power != op.power)
            throw new PropertyException
                    ("Can't subtract Numerics of different unit powers.");
        if (baseunit != op.baseunit) {
             throw new PropertyException
                     ("Can't subtract Numerics of different baseunits: "
                      + getBaseunitString() + " " + op.getBaseunitString());
        }
        if ((baseunit & REL_LENGTH) != 0) {
            throw new PropertyException
                    ("Can't subtract relative lengths."
                      + getBaseunitString() + " " + op.getBaseunitString());
        }
        
        // Subtract each type of value
        value -= op.value;
        return this;
    }

    /**
     * Subtract a <tt>double</tt> from the current value.
     * @param op - the value to subtract.
     * @return <i>Numeric</i>; this object.
     * @throws PropertyException if this is not a number.
     */
    public Numeric subtract(double op) throws PropertyException {
        // Check of same dimension
        if (power != 0 || baseunit != NUMBER)
            throw new PropertyException
                    ("Can't subtract number from length.");
        value -= op;
        return this;
    }

    /**
     * Add the operand to the current value.
     * @param op The value to add.
     * @return <i>Numeric</i>; this object.
     * @throws PropertyException
     * if the unit power of the operands is different.
     */
    public Numeric add(Numeric op) throws PropertyException {
        // Check of same powerension
        if (power != op.power)
            throw new PropertyException
                    ("Can't add Numerics of different unit powers.");
        if (baseunit != op.baseunit) {
             throw new PropertyException
                     ("Can't add Numerics of different baseunits: "
                      + getBaseunitString() + " " + op.getBaseunitString());
        }
        if ((baseunit & REL_LENGTH) != 0) {
            throw new PropertyException
                    ("Can't add relative lengths."
                      + getBaseunitString() + " " + op.getBaseunitString());
        }

        // Add each type of value
        value += op.value;
        return this;
    }

    /**
     * Add a <tt>double</tt> to the current value.
     * @param op - the value to add.
     * @return <i>Numeric</i>; this object.
     * @throws PropertyException if this is not a number.
     */
    public Numeric add(double op) throws PropertyException {
        // Check of same dimension
        if (power != 0 || baseunit != NUMBER)
            throw new PropertyException
                    ("Can't add number to length.");
        value += op;
        return this;
    }

    /**
     * Derive the remainder from a truncating division (mod).  As with
     * additive operators, the values must be absolute <tt>Numeric</tt>s
     * of the same unit value. (5.9.6)
     * @param op a <tt>Numeric</tt> representing the divisor
     * @return <i>Numeric</i>; this object.
     * @throws PropertyException If the unit power of the operands is
     * different or if the operands have different baseunits.
     */
    public Numeric mod(Numeric op) throws PropertyException {
        if (power != op.power)
            throw new PropertyException
                    ("Can't mod Numerics of different unit powers.");
        if (baseunit != op.baseunit) {
             throw new PropertyException
                     ("Can't mod Numerics of different baseunits: "
                      + getBaseunitString() + " " + op.getBaseunitString());
        }
        if ((baseunit & REL_LENGTH) != 0) {
            throw new PropertyException
                    ("Can't mod relative lengths."
                      + getBaseunitString() + " " + op.getBaseunitString());
        }

        value %= op.value;
        return this;
    }

    /**
     * Derive the remainder from a truncating division (mod).  As with
     * additive operators, the values must be absolute <tt>Numeric</tt>s
     * of the same unit value. (5.9.6)
     * In this case, the argument is a <tt>double</tt>, i.e., an absolute
     * Numeric with a unit value of zero.
     * <p> Originally the restriction for this method was lifted as noted
     * here.  There is no indication of why.  The restriction is now in place. 
     * <p>In this case only, the restriction
     * on the same unit power is lifted.
     * @param op a <tt>double</tt> containing the divisor
     * @return <i>Numeric</i>; this object.
     */
    public Numeric mod(double op) throws PropertyException {
        if (power != 0)
            throw new PropertyException
                    ("Can't mod Numerics of different unit powers.");
        if (baseunit != NUMBER) {
             throw new PropertyException
                     ("Can't mod Numerics of different baseunits: "
                      + getBaseunitString() + " literal double");
        }
        if ((baseunit & REL_LENGTH) != 0) {
            throw new PropertyException
                    ("Can't mod relative lengths."
                      + getBaseunitString());
        }

        value %= op;
        return this;
    }

    /**
     * Multiply the the current value by the operand.
     * @param op The multiplier.
     * @return <i>Numeric</i>; this object.
     * @throws PropertyException for invalid combinations.
     */
    public Numeric multiply(Numeric op) throws PropertyException {
        if (baseunit == NUMBER || op.baseunit == NUMBER) {
            // NUMBER is the universal multiplier
            // Multiply and convert to the basetype and power of the arg
            value *= op.value;
            power += op.power;
            if (baseunit == NUMBER) baseunit = op.baseunit;
        } else { // this is not a NUMBER and op not a NUMBER
            // baseunits must both be absolute lengths
            if (baseunit == MILLIPOINTS &&  op.baseunit == MILLIPOINTS)
            {
                value *= op.value;
                power += op.power;
            } else { // Invalid combination
                throw new PropertyException
                        ("Can't multiply relative lengths or an absolute "
                         + "length and a relative length: "
                         + getBaseunitString()
                         + " " + op.getBaseunitString());
            }
        }
        // Perfom some validity checks
        if (isNumeric() && power != 0)
            throw new PropertyException
                    ("Number, Ems or Percentage with non-zero power");
        // if the operation has resulted in a non-NUMERIC reducing to
        // a unit power of 0, change the type to NUMBER
        if (power == 0 && ! this.isNumeric()) baseunit = NUMBER;
        return this;
    }

    /**
     * Multiply the the current value by the <tt>double</tt> operand.
     * @param op The multiplier.
     * @return <i>Numeric</i>; this object.
     */
    public Numeric multiply(double op) {
        value *= op;
        return this;
    }

    /**
     * Divide the the current value by the operand.
     * @param op the divisor.
     * @return <i>Numeric</i>; this object.
     * @throws PropertyException for invalid combinations.
     */
    public Numeric divide(Numeric op) throws PropertyException {
        if (baseunit == NUMBER || op.baseunit == NUMBER) {
            // NUMBER is the universal divisor
            // Divide and convert to the basetype and power of the arg
            value /= op.value;
            power -= op.power;
            if (baseunit == NUMBER) baseunit = op.baseunit;
        } else { // this is not a NUMBER and op not a NUMBER
            // baseunits must both be absolute lengths
            if (baseunit == MILLIPOINTS &&  op.baseunit == MILLIPOINTS)
            {
                value /= op.value;
                power -= op.power;
            } else { // Invalid combination
                throw new PropertyException
                        ("Can't divide relative lengths or an absolute "
                         + "length and a relative length: "
                         + getBaseunitString()
                         + " " + op.getBaseunitString());
            }
        }
        // Perfom some validity checks
        if (isNumeric() && power != 0)
            throw new PropertyException
                    ("Number, Ems or Percentage with non-zero power");
        // if the operation has resulted in a non-NUMERIC reducing to
        // a unit power of 0, change the type to NUMBER
        if (power == 0 && ! this.isNumeric()) baseunit = NUMBER;
        return this;
    }

    /**
     * Divide the the current value by the <tt>double</tt> operand.
     * @param op The divisor.
     * @return <i>Numeric</i>; this object.
     */
    public Numeric divide(double op) {
        value /= op;
        return this;
    }

    /**
     * Negate the value of the property.
     * @return <i>Numeric</i>; this object.
     */
    public Numeric negate() {
        value = -value;
        return this;
    }

    /**
     * Return a new <tt>Numeric</tt> with the absolute value of this.
     * This is an
     * implementation of the core function library <tt>abs</tt> function.
     * @return A <tt>double</tt> containing the absolute value.
     * @exception PropertyException if cloning fails.
     */
    public Numeric abs() throws PropertyException {
        Numeric n;
        try {
            n = (Numeric)(this.clone());
        } catch (CloneNotSupportedException e) {
            throw new PropertyException(e);
        }
        n.setValue(Math.abs(value));
        return n;
    }

    /**
     * Return a <tt>Numeric</tt> which is the maximum of the current value and
     * the operand.  This is an implementation of the core function library
     * <tt>max</tt> function.  It is only valid for comparison of two
     * values of the same unit power and same type, i.e. both absolute or
     * both percentages.
     * @param op a <tt>Numeric</tt> representing the comparison value.
     * @return a <tt>double</tt> representing the <i>max</i> of
     * <i>this.value</i> and the <i>value</i> of <i>op</i>.
     * @throws PropertyException If the baseunit or power of this
     * object and the operand are different, or if cloning fails.
     */
    public Numeric max(Numeric op) throws PropertyException {
        Numeric n;
        // Only compare if both have same unit power and same baseunit
        if (power == op.power && baseunit == op.baseunit) {
            try {
                n = (Numeric)(this.clone());
            } catch (CloneNotSupportedException e) {
                throw new PropertyException(e);
            }
            n.setValue(Math.max(value, op.value));
            return n;
        }
        throw new PropertyException
                ("max() must compare numerics of same baseunit & unit power.");
    }

    /**
     * Return a <tt>Numeric</tt> which is the minimum of the current value and
     * the operand.  This is an implementation of the core function library
     * <tt>min</tt> function.  It is only valid for comparison of two
     * values of the same unit power and same type, i.e. both absolute or
     * both percentages.
     * @param op a <tt>Numeric</tt> representing the comparison value.
     * @return a <tt>double</tt> representing the <i>min</i> of
     * <i>this.value</i> and the <i>value</i> of <i>op</i>.
     * @throws PropertyException If the baseunit or power of this
     * object and the operand are different, or if cloning fails.
     */
    public Numeric min(Numeric op) throws PropertyException {
        Numeric n;
        // Only compare if both have same unit power and same baseunit
        if (power == op.power && baseunit == op.baseunit) {
            try {
                n = (Numeric)(this.clone());
            } catch (CloneNotSupportedException e) {
                throw new PropertyException(e);
            }
            n.setValue(Math.min(value, op.value));
            return n;
        }
        throw new PropertyException
                ("min() must compare numerics of same baseunit & unit power.");
    }

    /**
     * Return a <tt>double</tt> which is the ceiling of the current value.
     * This is an implementation of the core function library
     * <tt>ceiling</tt> function.  It is only valid for an absolute
     * numeric value of unit power 0.
     * @return a <tt>double</tt> representing the <i>ceiling</i> value.
     * @throws PropertyException If the unit power of the
     * object is not 0.
     */
    public double ceiling() throws PropertyException {

        if (power == 0) {
            return Math.ceil(value); 
        }
        throw new PropertyException
                ("ceiling() requires unit power 0.");
    }

    /**
     * Return a <tt>double</tt> which is the floor of the current value.
     * This is an implementation of the core function library
     * <tt>floor</tt> function.  It is only valid for an absolute
     * numeric value of unit power 0.
     * @return a <tt>double</tt> representing the <i>floor</i> value.
     * @throws PropertyException If the unit power of the
     * object is not 0.
     */
    public double floor() throws PropertyException {

        if (power == 0) {
            return Math.floor(value); 
        }
        throw new PropertyException
                ("floor() requires unit power 0.");
    }

    /**
     * Return a <tt>long</tt> which is the rounded current value.
     * This is an implementation of the core function library
     * <tt>round</tt> function.  It is only valid for an absolute
     * numeric value of unit power 0.
     * @return a <tt>long</tt> representing the <i>round</i>ed value.
     * Note that although the method returns a <tt>long</tt>,
     * the XSL funtion is expressed in terms of a <i>numeric</i>.
     * @throws PropertyException If the unit power of the
     * object is not 0.
     */
    public long round() throws PropertyException {

        if (power == 0) {
            return Math.round(value); 
        }
        throw new PropertyException
                ("round() requires unit power 0.");
    }

    /**
     * @param baseunit an <tt>init</tt> encoding the base unit.
     * @return a String containing the text name of the <i>baseunit</i>
     * type or notification of an unrecognized baseunit/
     */
    public String getUnitTypeString(int baseunit) {
        switch (baseunit) {
        case NUMBER:
            return "numeric";
        case PERCENTAGE:
            return "percentage";
        case EMS:
            return "ems";
        case MILLIPOINTS:
            return "millipoints";
        default: 
            return "Unrecognized baseunit type: " + baseunit;
        }
        
    }

    /**
     * @return a String containing the text name of the current <i>baseunit</i>
     * type or notification of an unrecognized baseunit/
     */
    public String getBaseunitString() {
        return getUnitTypeString(baseunit);
    }

    /**
     * @return a String containing the text name of the original
     * <i>baseunit</i> type or notification of an unrecognized baseunit/
     */
    public String getOriginalBaseunitString() {
        return getUnitTypeString(originalBaseUnit);
    }

    /**
     * @return a String containing the text name of the original
     * <i>unit</i> type or notification of an unrecognized unit.
     * Defined relative to the <i>originalBaseUnit</i>.
     */
    public String getOriginalUnitString() {
        return getUnitName(originalBaseUnit, originalUnit);
    }

    /**
     * @param unit an <tt>int</tt> encoding a unit.
     * @return the <tt>String</tt> name of the unit.
     */
    public static String getUnitName(int baseunit, int unit) {
        switch (baseunit) {
        case NUMBER:
            return "";
        case PERCENTAGE:
            return "%";
        case EMS:
            return "em";
        case MILLIPOINTS:
            return Length.getUnitName(unit);
        default:
            return "Unrecognized original baseunit type: " + baseunit;
        }
    }

    public String toString() {
        return "" + value + getBaseunitString()
                + (power != 0 ? "^" + power : "")
                + "\n" + super.toString();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
