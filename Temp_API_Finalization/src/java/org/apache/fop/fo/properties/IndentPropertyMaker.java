/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.NumericOp;
import org.apache.fop.fo.expr.PropertyException;

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
     * @param baseMaker the property maker to use
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
    public Property compute(PropertyList propertyList) throws PropertyException {
        if (propertyList.getFObj().getUserAgent()
                    .isBreakIndentInheritanceOnReferenceAreaBoundary()) {
            return computeAlternativeRuleset(propertyList);
        } else {
            return computeConforming(propertyList);
        }
    }
    
    /**
     * Calculate the corresponding value for start-indent and end-indent.  
     * @see CorrespondingPropertyMaker#compute(PropertyList)
     */
    public Property computeConforming(PropertyList propertyList) throws PropertyException {
        PropertyList pList = getWMPropertyList(propertyList);
        if (pList == null) {
            return null;
        }
        // Calculate the values as described in 5.3.2.

        Numeric padding = getCorresponding(paddingCorresponding, propertyList).getNumeric();
        Numeric border = getCorresponding(borderWidthCorresponding, propertyList).getNumeric();
        
        int marginProp = pList.getWritingMode(lr_tb, rl_tb, tb_rl);
        Numeric margin;
        // Calculate the absolute margin.
        if (propertyList.getExplicitOrShorthand(marginProp) == null) {
            Property indent = propertyList.getExplicit(baseMaker.propId);
            if (indent == null) {
                //Neither start-indent nor margin is specified, use inherited
                return null;
            }
            margin = propertyList.getExplicit(baseMaker.propId).getNumeric();
            margin = NumericOp.subtraction(margin, 
                    propertyList.getInherited(baseMaker.propId).getNumeric());
            margin = NumericOp.subtraction(margin, padding);
            margin = NumericOp.subtraction(margin, border);
        } else {
            margin = propertyList.get(marginProp).getNumeric();
        }
        
        Numeric v = new FixedLength(0);
        if (!propertyList.getFObj().generatesReferenceAreas()) {
            // The inherited_value_of([start|end]-indent)
            v = NumericOp.addition(v, propertyList.getInherited(baseMaker.propId).getNumeric());
        }
        // The corresponding absolute margin-[right|left}.
        v = NumericOp.addition(v, margin);
        v = NumericOp.addition(v, padding);
        v = NumericOp.addition(v, border);
        return (Property) v;
    }
    
    private boolean isInherited(PropertyList pList) {
        if (pList.getFObj().getUserAgent().isBreakIndentInheritanceOnReferenceAreaBoundary()) {
            FONode nd = pList.getFObj().getParent(); 
            return !((nd instanceof FObj) && ((FObj)nd).generatesReferenceAreas());
        } else {
            return true;
        }
    }
    
    /**
     * Calculate the corresponding value for start-indent and end-indent.
     * This method calculates indent following an alternative rule set that
     * tries to mimic many commercial solutions that chose to violate the
     * XSL specification.  
     * @see CorrespondingPropertyMaker#compute(PropertyList)
     */
    public Property computeAlternativeRuleset(PropertyList propertyList) throws PropertyException {
        PropertyList pList = getWMPropertyList(propertyList);
        if (pList == null) {
            return null;
        }

        // Calculate the values as described in 5.3.2.

        Numeric padding = getCorresponding(paddingCorresponding, propertyList).getNumeric();
        Numeric border = getCorresponding(borderWidthCorresponding, propertyList).getNumeric();
        
        int marginProp = pList.getWritingMode(lr_tb, rl_tb, tb_rl);

        //Determine whether the nearest anscestor indent was specified through 
        //start-indent|end-indent or through a margin property.
        boolean marginNearest = false;
        PropertyList pl = propertyList.getParentPropertyList();
        while (pl != null) {
            if (pl.getExplicit(baseMaker.propId) != null) {
                break;
            } else if (pl.getExplicitOrShorthand(marginProp) != null) {
                marginNearest = true;
                break;
            }
            pl = pl.getParentPropertyList();
        }
        
        Numeric margin;
        // Calculate the absolute margin.
        if (propertyList.getExplicitOrShorthand(marginProp) == null) {
            Property indent = propertyList.getExplicit(baseMaker.propId);
            if (indent == null) {
                //Neither start-indent nor margin is specified, use inherited
                if (isInherited(propertyList) || !marginNearest) {
                    return null;
                } else {
                    return new FixedLength(0);
                }
            } else {
                return indent;
            }
        } else {
            margin = propertyList.get(marginProp).getNumeric();
        }
        
        Numeric v = new FixedLength(0);
        if (isInherited(propertyList)) {
            // The inherited_value_of([start|end]-indent)
            v = NumericOp.addition(v, propertyList.getInherited(baseMaker.propId).getNumeric());
        }
        // The corresponding absolute margin-[right|left}.
        v = NumericOp.addition(v, margin);
        v = NumericOp.addition(v, padding);
        v = NumericOp.addition(v, border);
        return (Property) v;
    }
    
    private Property getCorresponding(int[] corresponding, PropertyList propertyList)
                throws PropertyException {
        PropertyList pList = getWMPropertyList(propertyList);
        if (pList != null) {
            int wmcorr = pList.getWritingMode(corresponding[0], corresponding[1], corresponding[2]);
            return propertyList.get(wmcorr);
        } else {
            return null;
        }
    }
}
