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
import org.apache.fop.datatypes.FixedLength;
import org.apache.fop.fo.expr.Numeric;

/**
 * This property maker handles the calculations described in 5.3.2 which
 * involves the sizes of the corresponding margin-* properties and the
 * padding-* and border-*-width properties.
 */
public class IndentPropertyMaker extends CorrespondingPropertyMaker {
    /**
     * The corresponding padding-* propIds 
     */
    private int[] paddingCorresponding = null;    

    /**
     * The corresponding border-*-width propIds 
     */
    private int[] borderWidthCorresponding = null;
    
    /**
     * Create a start-indent or end-indent property maker.
     * @param baseMaker
     */
    public IndentPropertyMaker(Property.Maker baseMaker) {
        super(baseMaker);
    }

    /**
     * Set the corresponding values for the padding-* properties.
     * @param paddingCorresponding the corresping propids.
     */
    public void setPaddingCorresponding(int[] paddingCorresponding) {
        this.paddingCorresponding = paddingCorresponding;
    }
    
    /**
     * Set the corresponding values for the border-*-width properties.
     * @param borderWidthCorresponding the corresping propids.
     */
    public void setBorderWidthCorresponding(int[] borderWidthCorresponding) {
        this.borderWidthCorresponding = borderWidthCorresponding;
    }
    
    /**
     * Calculate the corresponding value for start-indent and end-indent.  
     * @see CorrespondingPropertyMaker#compute(PropertyList)
     */
    public Property compute(PropertyList propertyList) throws FOPException {
        // TODO: bckfnn reenable
        if (propertyList.getExplicitOrShorthand(propertyList.wmMap(lr_tb, rl_tb, tb_rl)) == null) {
            return null;
        }
        // Calculate the values as described in 5.3.2.
        try {
            Numeric v = new Numeric(new FixedLength(0));
            /*
            if (!propertyList.getFObj().generatesInlineAreas()) {
                String propName = FOPropertyMapping.getPropertyName(this.propId);
                v = v.add(propertyList.getInherited(propName).getNumeric());
            }
            */
            v = v.add(propertyList.get(propertyList.wmMap(lr_tb, rl_tb, tb_rl)).getNumeric());
            v = v.add(getCorresponding(paddingCorresponding, propertyList).getNumeric());
            v = v.add(getCorresponding(borderWidthCorresponding, propertyList).getNumeric());
            return new LengthProperty(v.asLength());
        } catch (org.apache.fop.fo.expr.PropertyException propEx) {
           String propName = FOPropertyMapping.getPropertyName(baseMaker.getPropId());
           throw new FOPException("Error in " + propName 
                   + " calculation " + propEx);
        }    
    }
    
    private Property getCorresponding(int[] corresponding, PropertyList propertyList) {
        int wmcorr = propertyList.wmMap(corresponding[0], corresponding[1], corresponding[2]);
        return propertyList.get(wmcorr);
    }
}
