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

import java.util.Stack;

import org.apache.fop.fo.Property;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.FObj;
import org.apache.fop.datatypes.PercentBase;


/**
 * This class holds context information needed during property expression
 * evaluation.
 * It holds the Maker object for the property, the PropertyList being
 * built, and the FObj parent of the FObj for which the property is being set.
 */
public class PropertyInfo {
    private Property.Maker maker;
    private PropertyList plist;
    private FObj fo;
    private Stack stkFunction;    // Stack of functions being evaluated

    public PropertyInfo(Property.Maker maker, PropertyList plist, FObj fo) {
        this.maker = maker;
        this.plist = plist;
        this.fo = fo;
    }

    /**
     * Return whether this property inherits specified values.
     * Propagates to the Maker.
     * @return true if the property inherits specified values, false if it
     * inherits computed values.
     */
    public boolean inheritsSpecified() {
        return maker.inheritsSpecified();
    }

    /**
     * Return the PercentBase object used to calculate the absolute value from
     * a percent specification.
     * Propagates to the Maker.
     * @return The PercentBase object or null if percentLengthOK()=false.
     */
    public PercentBase getPercentBase() {
        PercentBase pcbase = getFunctionPercentBase();
        return (pcbase != null) ? pcbase : maker.getPercentBase(fo, plist);
    }

    /**
     * Return the current font-size value as base units (milli-points).
     */
    public int currentFontSize() {
        return plist.get("font-size").getLength().mvalue();
    }

    public FObj getFO() {
        return fo;
    }

    public PropertyList getPropertyList() {
        return plist;
    }

    public void pushFunction(Function func) {
        if (stkFunction == null) {
            stkFunction = new Stack();
        }
        stkFunction.push(func);
    }

    public void popFunction() {
        if (stkFunction != null)
            stkFunction.pop();
    }

    private PercentBase getFunctionPercentBase() {
        if (stkFunction != null) {
            Function f = (Function)stkFunction.peek();
            if (f != null) {
                return f.getPercentBase();
            }
        }
        return null;
    }

}
