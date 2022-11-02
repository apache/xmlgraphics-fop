/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
    private int[] paddingCorresponding;

    /**
     * The corresponding border-*-width propIds
     */
    private int[] borderWidthCorresponding;

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
        if ((paddingCorresponding == null) || (paddingCorresponding.length != 4)) {
            throw new IllegalArgumentException();
        }
        this.paddingCorresponding = paddingCorresponding;
    }

    /**
     * Set the corresponding values for the border-*-width properties.
     * @param borderWidthCorresponding the corresping propids.
     */
    public void setBorderWidthCorresponding(int[] borderWidthCorresponding) {
        if ((borderWidthCorresponding == null) || (borderWidthCorresponding.length != 4)) {
            throw new IllegalArgumentException();
        }
        this.borderWidthCorresponding = borderWidthCorresponding;
    }

    /**
     * Calculate the corresponding value for start-indent and end-indent.
     * @param propertyList the property list to use in the computation
     * @return the computed indent property
     * @throws PropertyException if a property exception occurs
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
     * @param propertyList the property list to use in the computation
     * @return the computed indent property
     * @throws PropertyException if a property exception occurs
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

        int marginProp = pList.selectFromWritingMode(lrtb, rltb, tbrl, tblr);
        // Calculate the absolute margin.
        if (propertyList.getExplicitOrShorthand(marginProp) == null) {
            Property indent = propertyList.getExplicit(baseMaker.propId);
            if (indent == null) {
                //Neither indent nor margin is specified, use inherited
                return null;
            } else {
                //Use explicit indent directly
                return indent;
            }
        } else {
            //Margin is used
            Numeric margin = propertyList.get(marginProp).getNumeric();

            Numeric v = FixedLength.ZERO_FIXED_LENGTH;
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
     * @param propertyList the property list to use in the computation
     * @return the computed indent property
     * @throws PropertyException if a property exception occurs
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

        int marginProp = pList.selectFromWritingMode(lrtb, rltb, tbrl, tblr);

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

        // Calculate the absolute margin.
        if (propertyList.getExplicitOrShorthand(marginProp) == null) {
            Property indent = propertyList.getExplicit(baseMaker.propId);
            if (indent == null) {
                //Neither start-indent nor margin is specified, use inherited
                if (isInherited(propertyList) || !marginNearest) {
                    return null;
                } else {
                    return FixedLength.ZERO_FIXED_LENGTH;
                }
            } else {
                return indent;
            }
        } else {
            //Margin is used
            Numeric margin = propertyList.get(marginProp).getNumeric();

            Numeric v = FixedLength.ZERO_FIXED_LENGTH;
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
    }

    private Property getCorresponding(int[] corresponding, PropertyList propertyList)
                throws PropertyException {
        PropertyList pList = getWMPropertyList(propertyList);
        if (pList != null) {
            int wmcorr = pList.selectFromWritingMode(
                corresponding[0], corresponding[1], corresponding[2], corresponding[3]);
            return propertyList.get(wmcorr);
        } else {
            return null;
        }
    }
}
