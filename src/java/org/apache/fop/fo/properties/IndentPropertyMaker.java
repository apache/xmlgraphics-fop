/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo.properties;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FOPropertyMapping;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.NumericOp;

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
    public IndentPropertyMaker(PropertyMaker baseMaker) {
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
            Numeric v = new FixedLength(0);
            /*
            if (!propertyList.getFObj().generatesInlineAreas()) {
                String propName = FOPropertyMapping.getPropertyName(this.propId);
                v = v.add(propertyList.getInherited(propName).getNumeric());
            }
            */
            v = NumericOp.addition(v, propertyList.get(propertyList.wmMap(lr_tb, rl_tb, tb_rl)).getNumeric());
            v = NumericOp.addition(v, getCorresponding(paddingCorresponding, propertyList).getNumeric());
            v = NumericOp.addition(v, getCorresponding(borderWidthCorresponding, propertyList).getNumeric());
            return (Property) v;
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
