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
import org.apache.fop.fo.Property;
import org.apache.fop.fo.PropertyList;

/**
 * @author me
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DimensionPropertyMaker extends CorrespondingPropertyMaker {
    int[][] extraCorresponding = null;

    public DimensionPropertyMaker(PropertyMaker baseMaker) {
        super(baseMaker);
    }
    
    public void setExtraCorresponding(int[][] extraCorresponding) {
        this.extraCorresponding = extraCorresponding;
    }

    public boolean isCorrespondingForced(PropertyList propertyList) {
        if (super.isCorrespondingForced(propertyList))
            return true;
        for (int i = 0; i < extraCorresponding.length; i++) {
            int wmcorr = extraCorresponding[i][0]; //propertyList.getWritingMode()];
            if (propertyList.getExplicit(wmcorr) != null)
                return true;
        }            
        return false;
    }

    public Property compute(PropertyList propertyList) throws FOPException {
        // Based on [width|height]
        Property p = super.compute(propertyList);
        if (p == null) {
            p = baseMaker.make(propertyList);
        }

        // Based on min-[width|height]
        int wmcorr = propertyList.wmMap(extraCorresponding[0][0], 
                                        extraCorresponding[0][1], 
                                        extraCorresponding[0][2]);
        Property subprop = propertyList.getExplicitOrShorthand(wmcorr);
        if (subprop != null) {
            baseMaker.setSubprop(p, Constants.CP_MINIMUM, subprop);
        }

        // Based on max-[width|height]
        wmcorr = propertyList.wmMap(extraCorresponding[1][0], 
                                    extraCorresponding[1][1], 
                                    extraCorresponding[1][2]);
        subprop = propertyList.getExplicitOrShorthand(wmcorr);
        // TODO: Don't set when NONE.
        if (subprop != null) {
            baseMaker.setSubprop(p, Constants.CP_MAXIMUM, subprop);
        }

        return p;
    }   
}
