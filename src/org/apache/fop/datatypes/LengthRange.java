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
package org.apache.fop.datatypes;

import org.apache.fop.fo.Property;

/**
 * a "progression-dimension" quantity
 * ex. block-progression-dimension, inline-progression-dimension
 * corresponds to the triplet min-height, height, max-height (or width)
 */
public class LengthRange implements CompoundDatatype {

    private Property minimum;
    private Property optimum;
    private Property maximum;
    private static final int MINSET = 1;
    private static final int OPTSET = 2;
    private static final int MAXSET = 4;
    private int bfSet = 0;    // bit field
    private boolean bChecked = false;

    // From CompoundDatatype
    public void setComponent(String sCmpnName, Property cmpnValue,
                             boolean bIsDefault) {
        if (sCmpnName.equals("minimum"))
            setMinimum(cmpnValue, bIsDefault);
        else if (sCmpnName.equals("optimum"))
            setOptimum(cmpnValue, bIsDefault);
        else if (sCmpnName.equals("maximum"))
            setMaximum(cmpnValue, bIsDefault);
    }

    // From CompoundDatatype
    public Property getComponent(String sCmpnName) {
        if (sCmpnName.equals("minimum"))
            return getMinimum();
        else if (sCmpnName.equals("optimum"))
            return getOptimum();
        else if (sCmpnName.equals("maximum"))
            return getMaximum();
        else
            return null;    // SHOULDN'T HAPPEN
    }

    /**
     * Set minimum value to min.
     * @param min A Length value specifying the minimum value for this
     * LengthRange.
     * @param bIsDefault If true, this is set as a "default" value
     * and not a user-specified explicit value.
     */
    protected void setMinimum(Property minimum, boolean bIsDefault) {
        this.minimum = minimum;
        if (!bIsDefault)
            bfSet |= MINSET;
    }


    /**
     * Set maximum value to max if it is >= optimum or optimum isn't set.
     * @param max A Length value specifying the maximum value for this
     * @param bIsDefault If true, this is set as a "default" value
     * and not a user-specified explicit value.
     */
    protected void setMaximum(Property max, boolean bIsDefault) {
        maximum = max;
        if (!bIsDefault)
            bfSet |= MAXSET;
    }


    /**
     * Set the optimum value.
     * @param opt A Length value specifying the optimum value for this
     * @param bIsDefault If true, this is set as a "default" value
     * and not a user-specified explicit value.
     */
    protected void setOptimum(Property opt, boolean bIsDefault) {
        optimum = opt;
        if (!bIsDefault)
            bfSet |= OPTSET;
    }

    // Minimum is prioritaire, if explicit
    private void checkConsistency() {
        if (bChecked)
            return;
            // Make sure max >= min
            // Must also control if have any allowed enum values!

            /**
             * *******************
             * if (minimum.mvalue() > maximum.mvalue()) {
             * if ((bfSet&MINSET)!=0) {
             * // if minimum is explicit, force max to min
             * if ((bfSet&MAXSET)!=0) {
             * // Warning: min>max, resetting max to min
             * MessageHandler.errorln("forcing max to min in LengthRange");
             * }
             * maximum = minimum ;
             * }
             * else {
             * minimum = maximum; // minimum was default value
             * }
             * }
             * // Now make sure opt <= max and opt >= min
             * if (optimum.mvalue() > maximum.mvalue()) {
             * if ((bfSet&OPTSET)!=0) {
             * if ((bfSet&MAXSET)!=0) {
             * // Warning: opt > max, resetting opt to max
             * MessageHandler.errorln("forcing opt to max in LengthRange");
             * optimum = maximum ;
             * }
             * else {
             * maximum = optimum; // maximum was default value
             * }
             * }
             * else {
             * // opt is default and max is explicit or default
             * optimum = maximum ;
             * }
             * }
             * else if (optimum.mvalue() < minimum.mvalue()) {
             * if ((bfSet&MINSET)!=0) {
             * // if minimum is explicit, force opt to min
             * if ((bfSet&OPTSET)!=0) {
             * MessageHandler.errorln("forcing opt to min in LengthRange");
             * }
             * optimum = minimum ;
             * }
             * else {
             * minimum = optimum; // minimum was default value
             * }
             * }
             * *******$*******
             */
        bChecked = true;
    }

    public Property getMinimum() {
        checkConsistency();
        return this.minimum;
    }

    public Property getMaximum() {
        checkConsistency();
        return this.maximum;
    }

    public Property getOptimum() {
        checkConsistency();
        return this.optimum;
    }

}
