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
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * A maker which calculates the line-height property.
 * This property maker is special because line-height inherits the specified
 * value, instead of the computed value.
 * So when a line-height is create based on an attribute, the specified value
 * is stored in the property and in compute() the stored specified value of
 * the nearest specified is used to recalculate the line-height.  
 */

public class LineHeightPropertyMaker extends SpaceProperty.Maker {
    /**
     * Create a maker for line-height.
     * @param propId the is for linehight.
     */
    public LineHeightPropertyMaker(int propId) {
        super(propId);
    }

    /**
     * Recalculate the line-height value based on the nearest specified
     * value.
     * @see PropertyMaker#compute(PropertyList)
     */
    protected Property compute(PropertyList propertyList) throws PropertyException {
        // recalculate based on last specified value
        // Climb up propertylist and find last spec'd value
        Property specProp = propertyList.getNearestSpecified(propId);
        if (specProp != null) {
            String specVal = specProp.getSpecifiedValue();
            if (specVal != null) {
                return make(propertyList, specVal,
                            propertyList.getParentFObj());
            }
        }
        return null;
    }

    /**
     * @see SpaceProperty.Maker#convertProperty(Property, PropertyList, FObj)
     */
    public Property convertProperty(Property p,
            PropertyList propertyList,
            FObj fo) throws PropertyException {
        Numeric numval = p.getNumeric();    
        if (numval != null && numval.getDimension() == 0) {
            p = new PercentLength(numval.getNumericValue(), getPercentBase(fo,propertyList));
            Property spaceProp = super.convertProperty(p, propertyList, fo);
            spaceProp.setSpecifiedValue(String.valueOf(numval.getNumericValue()));
            return spaceProp;
        }
        return super.convertProperty(p, propertyList, fo);
    }
}
