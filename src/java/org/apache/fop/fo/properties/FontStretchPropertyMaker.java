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
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * This subclass of EnumProperty.Maker handles the special treatment of 
 * relative font stretch values described in 7.8.5.
 */
public class FontStretchPropertyMaker 
    extends EnumProperty.Maker implements Constants {
    
    /* Ordered list of absolute font stretch properties so we can easily find the next /
     * previous one */
    private Property[] orderedFontStretchValues = null;
        
    /**
     * Create an enum property which can handle relative font stretches 
     * @param propId the font size property id.
     */
    public FontStretchPropertyMaker(int propId) {
        super(propId);
    }
    
    /**
     * @see PropertyMaker#convertProperty
     * Implements the parts of 7.8.5 relevant to relative font stretches
     */
    public Property convertProperty(Property p,
                                    PropertyList propertyList,
                                    FObj fo) throws PropertyException {
        // if it is a relative font stretch value get current parent value and step
        // up or down accordingly
        if (p.getEnum() == EN_NARROWER) {
            return computeNextAbsoluteFontStretch(propertyList.getFromParent(this.getPropId()), -1);
        } else if (p.getEnum() == EN_WIDER) {
            return computeNextAbsoluteFontStretch(propertyList.getFromParent(this.getPropId()), 1);
        }
        return super.convertProperty(p, propertyList, fo);
    }

    /**
     * Calculates the nearest absolute font stretch property to the given
     * font stretch
     * @param baseProperty the font stretch property as set on the parent fo
     * @param direction should be -1 to get the next narrower value or +1 for the next wider value
     */
    private Property computeNextAbsoluteFontStretch(Property baseProperty, int direction) {
        // Create the table entries the first time around
        // @todo is this thread safe, do we need to worry about this here?
        if (orderedFontStretchValues == null) {
            orderedFontStretchValues = new Property[] {
                checkEnumValues("ultra-condensed"),
                checkEnumValues("extra-condensed"),
                checkEnumValues("condensed"),
                checkEnumValues("semi-condensed"),
                checkEnumValues("normal"),
                checkEnumValues("semi-expanded"),
                checkEnumValues("expanded"),
                checkEnumValues("extra-expanded"),
                checkEnumValues("ultra-expanded")
            };
        }
        int baseValue = baseProperty.getEnum();
        for (int i = 0; i < orderedFontStretchValues.length; i++) {
            if (baseValue == orderedFontStretchValues[i].getEnum()) {
                // increment/decrement the index and make sure its within the array bounds
                i = Math.min(Math.max(0, i + direction), orderedFontStretchValues.length - 1);
                return orderedFontStretchValues[i];
            }
        }
        // return the normal value
        return orderedFontStretchValues[4];
    }

}
