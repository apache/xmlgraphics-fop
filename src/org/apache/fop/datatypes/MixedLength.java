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

import java.util.ArrayList;

import org.apache.fop.fo.expr.Numeric;
import org.apache.fop.fo.expr.PropertyException;

/**
 * A length quantity in XSL which is specified with a mixture
 * of absolute and relative and/or percent components.
 * The actual value may not be computable before layout is done.
 */
public class MixedLength extends Length {

    private ArrayList lengths ;

    public MixedLength(ArrayList lengths) {
        this.lengths = lengths;
    }

    protected void computeValue() {
        int computedValue =0;
        boolean bAllComputed = true;
        for (int i = 0; i < lengths.size(); i++) {
            Length l = (Length)lengths.get(i);
            computedValue += l.mvalue();
            if (! l.isComputed()) {
                bAllComputed = false;
            }
        }
        setComputedValue(computedValue, bAllComputed);
    }

    public double getTableUnits() {
        double tableUnits = 0.0;
        for (int i = 0; i < lengths.size(); i++) {
            tableUnits += ((Length)lengths.get(i)).getTableUnits();
        }
        return tableUnits;
    }

    public void resolveTableUnit(double dTableUnit) {
        for (int i = 0; i < lengths.size(); i++) {
            ((Length)lengths.get(i)).resolveTableUnit(dTableUnit);
        }
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        for (int i = 0; i < lengths.size(); i++) {
            if (sbuf.length()>0) {
                sbuf.append('+');
            }
            sbuf.append(lengths.get(i).toString());
        }
        return sbuf.toString();
    }

    public Numeric asNumeric() {
        Numeric numeric = null;
        for (int i = 0; i < lengths.size(); i++) {
            Length l = (Length)lengths.get(i);
            if (numeric == null) {
                numeric = l.asNumeric();
            } else {
                try {
                    Numeric sum = numeric.add(l.asNumeric());
                    numeric = sum;
                } catch (PropertyException pe) {
                    System.err.println("Can't convert MixedLength to Numeric: " +
                            pe);
                }
            }
        }
        return numeric;
    }

}
