/*
 * $Id: MixedLength.java,v 1.6 2003/03/05 20:38:23 jeremias Exp $
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

import java.util.Enumeration;
import java.util.Vector;

import org.apache.fop.fo.LengthProperty;
import org.apache.fop.fo.expr.NumericProperty;
import org.apache.fop.fo.expr.PropertyException;

/**
 * A length quantity in XSL which is specified with a mixture
 * of absolute and relative and/or percent components.
 * The actual value may not be computable before layout is done.
 */
public class MixedLength extends LengthProperty {

    /** The collection of Length objects comprising this MixedLength object */
    private Vector lengths ;

    /**
     * Constructor
     * @param lengths the collection of Length objects which comprise the new
     * MixedLength object
     */
    public MixedLength(Vector lengths) {
        this.lengths = lengths;
    }

    /**
     * Iterates through each internal component, computing and
     * summarizing the values, then setting the value for this
     */
    protected void computeValue() {
        int computedValue = 0;
        boolean bAllComputed = true;
        Enumeration e = lengths.elements();
        while (e.hasMoreElements()) {
            LengthProperty l = (LengthProperty) e.nextElement();
            computedValue += l.getValue();
            if (!l.isComputed()) {
                bAllComputed = false;
            }
        }
        setComputedValue(computedValue, bAllComputed);
    }


    public double getTableUnits() {
        double tableUnits = 0.0;
        Enumeration e = lengths.elements();
        while (e.hasMoreElements()) {
            tableUnits += ((LengthProperty) e.nextElement()).getTableUnits();
        }
        return tableUnits;
    }

    public void resolveTableUnit(double dTableUnit) {
        Enumeration e = lengths.elements();
        while (e.hasMoreElements()) {
            ((LengthProperty) e.nextElement()).resolveTableUnit(dTableUnit);
        }
    }

    /**
     * @return String equivalent of this
     */
    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        Enumeration e = lengths.elements();
        while (e.hasMoreElements()) {
            if (sbuf.length() > 0) {
                sbuf.append('+');
            }
            sbuf.append(e.nextElement().toString());
        }
        return sbuf.toString();
    }

    /**
     * @return Numeric equivalent of this
     */
    public NumericProperty asNumeric() {
        NumericProperty numeric = null;
        for (Enumeration e = lengths.elements(); e.hasMoreElements();) {
            LengthProperty l = (LengthProperty) e.nextElement();
            if (numeric == null) {
                numeric = l.asNumeric();
            } else {
                try {
                    NumericProperty sum = numeric.add(l.asNumeric());
                    numeric = sum;
                } catch (PropertyException pe) {
                    System.err.println(
                      "Can't convert MixedLength to Numeric: " + pe);
                }
            }
        }
        return numeric;
    }
}

