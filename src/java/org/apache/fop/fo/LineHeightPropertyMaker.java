/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2004 The Apache Software Foundation. All rights reserved.
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
package org.apache.fop.fo;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.PercentLength;

/**
 * A maker which calculates the line-height property.
 * This property maker is special because line-height inherit the specified
 * value, instead of the computed value.
 * So when a line-height is create based on a attribute, the specified value
 * is stored in the property and in compute() the stored specified value of
 * the nearest specified is used to recalculate the line-height.  
 */

public class LineHeightPropertyMaker extends LengthProperty.Maker {
    /**
     * Create a maker for line-height.
     * @param propId the is for linehight.
     */
    public LineHeightPropertyMaker(int propId) {
        super(propId);
    }

    /**
     * Make a property as normal, and save the specified value.
     * @see Property.Maker.make(propertyList, String, FObj)
     */
    public Property make(PropertyList propertyList, String value,
                         FObj fo) throws FOPException {
        Property p = super.make(propertyList, value, fo);
        p.setSpecifiedValue(checkValueKeywords(value));
        return p;
    }
    
    /**
     * Recalculate the line-height value based on the nearest specified
     * value.
     * @see Property.Maker.compute(propertyList)
     */
    protected Property compute(PropertyList propertyList) throws FOPException {
        // recalculate based on last specified value
        // Climb up propertylist and find last spec'd value
        Property specProp = propertyList.getNearestSpecified(propId);
        if (specProp != null) {
            String specVal = specProp.getSpecifiedValue();
            if (specVal != null) {
                try {
                    return make(propertyList, specVal,
                            propertyList.getParentFObj());
                } catch (FOPException e) {
                    //getLogger()error("Error computing property value for "
                    //                       + propName + " from "
                    //                       + specVal);
                    return null;
                }
            }
        }
        return null;
    }
    
    protected Property convertPropertyDatatype(Property p, 
                                               PropertyList propertyList,
                                               FObj fo) {
        Number numval = p.getNumber();
        if (numval != null) {
            return new LengthProperty(
                    new PercentLength(numval.doubleValue(), getPercentBase(fo,propertyList)));
        }
        return super.convertPropertyDatatype(p, propertyList, fo);
    }
}
