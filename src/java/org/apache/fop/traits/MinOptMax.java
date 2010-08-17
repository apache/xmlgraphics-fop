/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.traits;

import java.io.Serializable;

import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.fo.properties.SpaceProperty;

/**
 * This class holds the resolved (as mpoints) form of a {@link LengthRangeProperty LengthRange} or
 * {@link SpaceProperty Space} type property value.
 * <p/>
 * Instances of this class are immutable. All arithmetic methods like {@link #plus(MinOptMax) plus},
 * {@link #minus(MinOptMax) minus} or {@link #mult(int) mult} return a different instance. So it is
 * possible to pass around instances without copying.
 * <p/>
 * <code>MinOptMax</code> values are used during layout calculations.
 */
public final class MinOptMax implements Serializable {

    private static final long serialVersionUID = -4791524475122206142L;

    /**
     * The zero <code>MinOptMax</code> instance with <code>min == opt == max == 0</code>.
     */
    public static final MinOptMax ZERO = getInstance(0);

    private final int min;
    private final int opt;
    private final int max;

    /**
     * Returns an instance of <code>MinOptMax</code> with the given values.
     *
     * @param min the minimum value
     * @param opt the optimum value
     * @param max the maximum value
     * @return the corresponding instance
     * @throws IllegalArgumentException if <code>min > opt || max < opt</code>.
     */
    public static MinOptMax getInstance(int min, int opt, int max)
        throws IllegalArgumentException {
        if (min > opt) {
            throw new IllegalArgumentException("min (" + min + ") > opt (" + opt + ")");
        }
        if (max < opt) {
            throw new IllegalArgumentException("max (" + max + ") < opt (" + opt + ")");
        }
        return new MinOptMax(min, opt, max);
    }

    /**
     * Returns an instance of <code>MinOptMax</code> with one fixed value for all three
     * properties (min, opt, max).
     *
     * @param value the value for min, opt and max
     * @return the corresponding instance
     * @see #isStiff()
     */
    public static MinOptMax getInstance(int value) {
        return new MinOptMax(value, value, value);
    }

    // Private constructor without consistency checks
    private MinOptMax(int min, int opt, int max) {
        assert min <= opt && opt <= max;
        this.min = min;
        this.opt = opt;
        this.max = max;
    }

    /**
     * Returns the minimum value of this <code>MinOptMax</code>.
     *
     * @return the minimum value of this <code>MinOptMax</code>.
     */
    public int getMin() {
        return min;
    }

    /**
     * Returns the optimum value of this <code>MinOptMax</code>.
     *
     * @return the optimum value of this <code>MinOptMax</code>.
     */
    public int getOpt() {
        return opt;
    }

    /**
     * Returns the maximum value of this <code>MinOptMax</code>.
     *
     * @return the maximum value of this <code>MinOptMax</code>.
     */
    public int getMax() {
        return max;
    }

    /**
     * Returns the shrinkability of this <code>MinOptMax</code> which is the absolute difference
     * between <code>min</code> and <code>opt</code>.
     *
     * @return the shrinkability of this <code>MinOptMax</code> which is always non-negative.
     */
    public int getShrink() {
        return opt - min;
    }

    /**
     * Returns the stretchability of this <code>MinOptMax</code> which is the absolute difference
     * between <code>opt</code> and <code>max</code>.
     *
     * @return the stretchability of this <code>MinOptMax</code> which is always non-negative.
     */
    public int getStretch() {
        return max - opt;
    }

    /**
     * Returns the sum of this <code>MinOptMax</code> and the given <code>MinOptMax</code>.
     *
     * @param operand the second operand of the sum (the first is this instance itself),
     * @return the sum of this <code>MinOptMax</code> and the given <code>MinOptMax</code>.
     */
    public MinOptMax plus(MinOptMax operand) {
        return new MinOptMax(min + operand.min, opt + operand.opt, max + operand.max);
    }


    /**
     * Adds the given value to all three components of this instance and returns the result.
     *
     * @param value value to add to the min, opt, max components
     * @return the result of the addition
     */
    public MinOptMax plus(int value) {
        return new MinOptMax(min + value, opt + value, max + value);
    }

    /**
     * Returns the difference of this <code>MinOptMax</code> and the given
     * <code>MinOptMax</code>. This instance must be a compound of the operand and another
     * <code>MinOptMax</code>, that is, there must exist a <code>MinOptMax</code> <i>m</i>
     * such that <code>this.equals(m.plus(operand))</code>. In other words, the operand
     * must have less shrink and stretch than this instance.
     *
     * @param operand the value to be subtracted
     * @return the difference of this <code>MinOptMax</code> and the given
     * <code>MinOptMax</code>.
     * @throws ArithmeticException if this instance has strictly less shrink or stretch
     * than the operand
     */
    public MinOptMax minus(MinOptMax operand)
        throws ArithmeticException {
        checkCompatibility(getShrink(), operand.getShrink(), "shrink");
        checkCompatibility(getStretch(), operand.getStretch(), "stretch");
        return new MinOptMax(min - operand.min, opt - operand.opt, max - operand.max);
    }

    private void checkCompatibility(int thisElasticity, int operandElasticity, String msge) {
        if (thisElasticity < operandElasticity) {
            throw new ArithmeticException(
                    "Cannot subtract a MinOptMax from another MinOptMax that has less " + msge
                            + " (" + thisElasticity + " < " + operandElasticity + ")");
        }
    }

    /**
     * Subtracts the given value from all three components of this instance and returns the result.
     *
     * @param value value to subtract from the min, opt, max components
     * @return the result of the subtraction
     */
    public MinOptMax minus(int value) {
        return new MinOptMax(min - value, opt - value, max - value);
    }

    /**
     * Returns an instance with the given value added to the minimal value.
     *
     * @param minOperand the minimal value to be added.
     * @return an instance with the given value added to the minimal value.
     * @throws IllegalArgumentException if <code>min + minOperand > opt || max < opt</code>.
     */
    // [GA] remove deprecation - no alternative specified
    // @deprecated Do not use! It's only for backwards compatibility.
    public MinOptMax plusMin(int minOperand)
        throws IllegalArgumentException {
        return getInstance(min + minOperand, opt, max);
    }

    /**
     * Returns an instance with the given value subtracted to the minimal value.
     *
     * @param minOperand the minimal value to be subtracted.
     * @return an instance with the given value subtracted to the minimal value.
     * @throws IllegalArgumentException if <code>min - minOperand > opt || max < opt</code>.
     */
    // [GA] remove deprecation - no alternative specified
    // @deprecated Do not use! It's only for backwards compatibility.
    public MinOptMax minusMin(int minOperand)
        throws IllegalArgumentException {
        return getInstance(min - minOperand, opt, max);
    }

    /**
     * Returns an instance with the given value added to the maximal value.
     *
     * @param maxOperand the maximal value to be added.
     * @return an instance with the given value added to the maximal value.
     * @throws IllegalArgumentException if <code>min > opt || max < opt + maxOperand</code>.
     */
    // [GA] remove deprecation - no alternative specified
    // @deprecated Do not use! It's only for backwards compatibility.
    public MinOptMax plusMax(int maxOperand)
        throws IllegalArgumentException {
        return getInstance(min, opt, max + maxOperand);
    }

    /**
     * Returns an instance with the given value subtracted to the maximal value.
     *
     * @param maxOperand the maximal value to be subtracted.
     * @return an instance with the given value subtracted to the maximal value.
     * @throws IllegalArgumentException if <code>min > opt || max < opt - maxOperand</code>.
     */
    // [GA] remove deprecation - no alternative specified
    // @deprecated Do not use! It's only for backwards compatibility.
    public MinOptMax minusMax(int maxOperand)
        throws IllegalArgumentException {
        return getInstance(min, opt, max - maxOperand);
    }

    /**
     * Returns the product of this <code>MinOptMax</code> and the given factor.
     *
     * @param factor the factor
     * @return the product of this <code>MinOptMax</code> and the given factor
     * @throws IllegalArgumentException if the factor is negative
     */
    public MinOptMax mult(int factor)
        throws IllegalArgumentException {
        if (factor < 0) {
            throw new IllegalArgumentException("factor < 0; was: " + factor);
        } else if (factor == 1) {
            return this;
        } else {
            return getInstance(min * factor, opt * factor, max * factor);
        }
    }

    /**
     * Determines whether this <code>MinOptMax</code> represents a non-zero dimension, which means
     * that not all values (min, opt, max) are zero.
     *
     * @return <code>true</code> if this <code>MinOptMax</code> represents a non-zero dimension;
     *         <code>false</code> otherwise.
     */
    public boolean isNonZero() {
        return min != 0 || max != 0;
    }

    /**
     * Determines whether this <code>MinOptMax</code> doesn't allow for shrinking or stretching,
     * which means that all values (min, opt, max) are the same.
     *
     * @return <code>true</code> if whether this <code>MinOptMax</code> doesn't allow for shrinking
     *         or stretching; <code>false</code> otherwise.
     * @see #isElastic()
     */
    public boolean isStiff() {
        return min == max;
    }

    /**
     * Determines whether this <code>MinOptMax</code> allows for shrinking or stretching, which
     * means that at least one of the min or max values isn't equal to the opt value.
     *
     * @return <code>true</code> if this <code>MinOptMax</code> allows for shrinking or stretching;
     *         <code>false</code> otherwise.
     * @see #isStiff()
     */
    public boolean isElastic() {
        return min != opt || opt != max;
    }

    /**
     * Extends the minimum length to the given length if necessary, and adjusts opt and max
     * accordingly.
     *
     * @param newMin the new minimum length
     * @return a <code>MinOptMax</code> instance with the minimum length extended
     */
    public MinOptMax extendMinimum(int newMin) {
        if (min < newMin) {
            int newOpt = Math.max(newMin, opt);
            int newMax = Math.max(newOpt, max);
            return getInstance(newMin, newOpt, newMax);
        } else {
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        MinOptMax minOptMax = (MinOptMax) obj;

        return opt == minOptMax.opt && max == minOptMax.max && min == minOptMax.min;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int result = min;
        result = 31 * result + opt;
        result = 31 * result + max;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "MinOptMax[min = " + min + ", opt = " + opt + ", max = " + max + "]";
    }
}

