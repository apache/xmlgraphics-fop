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
package org.apache.fop.fo.properties;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.LengthProperty;
import org.apache.fop.fo.Property;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.LengthProperty.Maker;

/**
 * This subclass of LengthProperty.Maker handles the special treatment of 
 * border width described in 7.7.20.
 */
public class BorderWidthPropertyMaker extends LengthProperty.Maker {
    int borderStyleId = 0;    
    
    /**
     * Create a length property which check the value of the border-*-style
     * property and return a length of 0 when the style is "none". 
     * @param propId the border-*-width of the property.
     */
    public BorderWidthPropertyMaker(int propId) {
        super(propId);
    }
    
    /**
     * Set the propId of the style property for the same side.
     * @param borderStyleId
     */
    public void setBorderStyleId(int borderStyleId) {
        this.borderStyleId = borderStyleId;
    }

    /**
     * Check the value of the style property and return a length of 0 when
     * the style is NONE.
     * @see org.apache.fop.fo.Property.Maker#get(int, PropertyList, boolean, boolean)
     */
   
    public Property get(int subpropId, PropertyList propertyList,
                        boolean bTryInherit, boolean bTryDefault)
        throws FOPException
    {
        Property p = super.get(subpropId, propertyList,
                               bTryInherit, bTryDefault);

        // Calculate the values as described in 7.7.20.
        Property style = propertyList.get(borderStyleId);
        if (style.getEnum() == Constants.NONE) {
            // TODO: bckfnn reenable
            return p; // new LengthProperty(new FixedLength(0));
        }
        return p;
    }
}
