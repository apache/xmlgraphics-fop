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
package org.apache.fop.fo.expr;

import org.apache.fop.fo.Property;
import org.apache.fop.fo.ColorTypeProperty;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.PercentBase;

class RGBColorFunction extends FunctionBase {
    public int nbArgs() {
        return 3;
    }

    /**
     * Return an object which implements the PercentBase interface.
     * Percents in arguments to this function are interpreted relative
     * to 255.
     */
    public PercentBase getPercentBase() {
        return new RGBPercentBase();
    }

    public Property eval(Property[] args,
                         PropertyInfo pInfo) throws PropertyException {
        // Using CSS rules, numbers are either supposed to be 0-255
        // or percentage values. If percentages value, they've already
        // been converted to reals.
        float[] cfvals = new float[3];    // RGB
        for (int i = 0; i < 3; i++) {
            Number cval = args[i].getNumber();
            if (cval == null) {
                throw new PropertyException("Argument to rgb() must be a Number");
            }
            float colorVal = cval.floatValue() / 255f;
            if (colorVal < 0.0 || colorVal > 255.0) {
                throw new PropertyException("Arguments to rgb() must normalize to the range 0 to 1");
            }
            cfvals[i] = colorVal;
        }
        return new ColorTypeProperty(new ColorType(cfvals[0], cfvals[1],
                                                   cfvals[2]));

    }

    static class RGBPercentBase implements PercentBase {
        public int getDimension() {
            return 0;
        }

        public double getBaseValue() {
            return 255f;
        }

        public int getBaseLength() {
            return 0;
        }

    }
}
