/*
 * $Id: PercentLength.java,v 1.6 2003/03/05 20:38:23 jeremias Exp $
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
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.Numeric;

/**
 * a percent specified length quantity in XSL
 */
public class PercentLength extends Length {

    /**
     * The percentage itself, expressed as a decimal value, e.g. for 95%, set
     * the value to .95
     */
    private double factor;

    /**
     * A PercentBase implementation that contains the base length to which the
     * {@link #factor} should be applied to compute the actual length
     */
    private PercentBase lbase = null;

    /**
     * Main constructor. Construct an object based on a factor (the percent,
     * as a factor) and an object which has a method to return the Length which
     * provides the "base" for the actual length that is modeled.
     * @param factor the percentage factor, expressed as a decimal (e.g. use
     * .95 to represent 95%)
     * @param lbase base property to which the factor should be applied
     */
    public PercentLength(double factor, PercentBase lbase) {
        this.factor = factor;
        this.lbase = lbase;
    }

    /**
     * Convenience constructor when only the factor is known
     * @param factor the percentage factor, expressed as a decimal (e.g. use
     * .95 to represent 95%)
     */
    public PercentLength(double factor) {
        this(factor, null);
    }

    /**
     * @param lbase the base to set
     */
    public void setBaseLength(PercentBase lbase) {
        this.lbase = lbase;
    }

    /**
     * @return the base
     */
    public PercentBase getBaseLength() {
        return this.lbase;
    }

    /**
     * Return the computed value in millipoints. This assumes that the
     * base length has been resolved to an absolute length value.
     */
    protected void computeValue() {
        setComputedValue((int)(factor * (double)lbase.getBaseLength()));
    }

    /**
     *
     * @return the factor
     */
    public double value() {
        return factor;
    }

    /**
     * @return the String equivalent of this
     */
    public String toString() {
        // TODO: What about the base value?
        return (new Double(factor * 100.0).toString()) + "%";
    }

    /**
     * @return new Numeric object that is equivalent to this
     */
    public Numeric asNumeric() {
        return new Numeric(this);
    }

}
