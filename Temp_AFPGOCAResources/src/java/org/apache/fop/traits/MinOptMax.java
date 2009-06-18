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

    /**
     * New min/opt/max with zero values.
     */
    public MinOptMax() {
        this(0);
    }

    /**
     * New min/opt/max with one fixed value.
     *
     * @param val the value for min, opt and max
     */
    public MinOptMax(int val) {
        this(val, val, val);
    }

    /**
     * New min/opt/max with the three values.
     *
     * @param min the minimum value
     * @param opt the optimum value
     * @param max the maximum value
     */
    public MinOptMax(int min, int opt, int max) {
        // TODO: assert min<=opt<=max
        this.min = min;
        this.opt = opt;
        this.max = max;
    }

    /**
     * Copy constructor.
     *
     * @param op the MinOptMax object to copy
     */
    public MinOptMax(MinOptMax op) {
        this.min = op.min;
        this.opt = op.opt;
        this.max = op.max;
    }

    // TODO: remove this.
    /**
     * {@inheritDoc}
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            // SHOULD NEVER OCCUR - all members are primitive types!
            return null;
        }
    }

    /**
     * Subtracts one MinOptMax instance from another returning a new one.
     * @param op1 first instance to subtract from
     * @param op2 second instance
     * @return MinOptMax new instance
     */
    public static MinOptMax subtract(MinOptMax op1, MinOptMax op2) {
        return new MinOptMax(op1.min - op2.max, op1.opt - op2.opt,
                             op1.max - op2.min);
    }

    /**
     * Adds one MinOptMax instance to another returning a new one.
     * @param op1 first instance
     * @param op2 second instance
     * @return MinOptMax new instance
     */
    public static MinOptMax add(MinOptMax op1, MinOptMax op2) {
        return new MinOptMax(op1.min + op2.min, op1.opt + op2.opt,
                             op1.max + op2.max);
    }

    /**
     * Multiplies a MinOptMax instance with a factor returning a new instance.
     * @param op1 MinOptMax instance
     * @param mult multiplier
     * @return MinOptMax new instance
     */
    public static MinOptMax multiply(MinOptMax op1, double mult) {
        // TODO: assert mult>0
        return new MinOptMax((int)(op1.min * mult),
                             (int)(op1.opt * mult), (int)(op1.max * mult));
    }

    /**
     * Adds another MinOptMax instance to this one.
     * @param op the other instance
     */
    public void add(MinOptMax op) {
        min += op.min;
        opt += op.opt;
        max += op.max;
    }

    /**
     * Adds min, opt and max to their counterpart components.
     * @param min the value to add to the minimum value
     * @param opt the value to add to the optimum value
     * @param max the value to add to the maximum value
     */
    public void add(int min, int opt, int max) {
        this.min += min;
        this.opt += opt;
        this.max += max;
        // TODO: assert min<=opt<=max
    }

    /**
     * Adds a length to all components.
     * @param len the length to add
     */
    public void add(int len) {
        this.min += len;
        this.opt += len;
        this.max += len;
    }


    /**
     * Subtracts another MinOptMax instance from this one.
     * @param op the other instance
     */
    public void subtract(MinOptMax op) {
        min -= op.max;
        opt -= op.opt;
        max -= op.min;
    }

    /** @return true if this instance represents a zero-width length (min=opt=max=0) */
    public boolean isNonZero() {
        return (min != 0 || max != 0);
    }

    /** @return true if this instance allows for shrinking or stretching */
    public boolean isElastic() {
        return (min != opt || opt != max);
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("MinOptMax[min=");
        if (min != opt) {
            sb.append(min).append("; ");
        }
        sb.append("opt=");
        if (opt != max) {
            sb.append(opt).append("; ");
        }
        sb.append("max=").append(max);
        sb.append("]");
        return sb.toString();
    }
}

