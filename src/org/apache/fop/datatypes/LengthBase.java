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

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.messaging.MessageHandler;

public class LengthBase implements PercentBase {
    // Standard kinds of percent-based length
    public static final int CUSTOM_BASE = 0;
    public static final int FONTSIZE = 1;
    public static final int INH_FONTSIZE = 2;
    public static final int CONTAINING_BOX = 3;
    public static final int CONTAINING_REFAREA = 4;

    /**
     * FO parent of the FO for which this property is to be calculated.
     */
    protected /* final */ FObj parentFO;

    /**
     * PropertyList for the FO where this property is calculated.
     */
    private /* final */ PropertyList propertyList;

    /**
     * One of the defined types of LengthBase
     */
    private /* final */ int iBaseType;

    public LengthBase(FObj parentFO, PropertyList plist, int iBaseType) {
        this.parentFO = parentFO;
        this.propertyList = plist;
        this.iBaseType = iBaseType;
    }

    /**
     * Accessor for parentFO object from subclasses which define
     * custom kinds of LengthBase calculations.
     */
    protected FObj getParentFO() {
        return parentFO;
    }

    /**
     * Accessor for propertyList object from subclasses which define
     * custom kinds of LengthBase calculations.
     */
    protected PropertyList getPropertyList() {
        return propertyList;
    }

    public int getDimension() {
        return 1;
    }

    public double getBaseValue() {
        return 1.0;
    }

    public int getBaseLength() {
        switch (iBaseType) {
        case FONTSIZE:
            return propertyList.get("font-size").getLength().mvalue();
        case INH_FONTSIZE:
            return propertyList.getInherited("font-size").getLength().mvalue();
        case CONTAINING_BOX:
            // depends on property?? inline-progression vs block-progression
            return parentFO.getContentWidth();
        case CONTAINING_REFAREA:    // example: start-indent, end-indent
         {
            FObj fo;
            for (fo = parentFO; fo != null &&!fo.generatesReferenceAreas();
                    fo = fo.getParent());
            return (fo != null ? fo.getContentWidth() : 0);
        }
        case CUSTOM_BASE:
            MessageHandler.errorln("!!! LengthBase.getBaseLength() called on CUSTOM_BASE type !!!");
            return 0;
        default:
            MessageHandler.errorln("Unknown base type for LengthBase.");
            return 0;
        }
    }

}

