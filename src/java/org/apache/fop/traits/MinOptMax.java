/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
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
        this.min = min;
        this.opt = opt;
        this.max = max;
    }

    /**
     * @see java.lang.Object#clone()
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
     * Subtracts from this instance using another.
     * @param op the other instance
     */
    public void subtract(MinOptMax op) {
        min -= op.max;
        opt -= op.opt;
        max -= op.min;
    }

    public String toString() {
        return "MinOptMax: min=" + min + "; opt=" + opt + "; max=" + max;
    }
}

