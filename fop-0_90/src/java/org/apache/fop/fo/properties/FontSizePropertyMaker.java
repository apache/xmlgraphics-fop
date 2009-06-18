/*
 * Copyright 2005 The Apache Software Foundation.
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

/* $Id: $ */

package org.apache.fop.fo.properties;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * This subclass of LengthProperty.Maker handles the special treatment of 
 * relative font sizes described in 7.8.4.
 */
public class FontSizePropertyMaker 
    extends LengthProperty.Maker implements Constants {

    /** The default normal font size in mpt */
    private static final int FONT_SIZE_NORMAL = 12000;
    /** The factor to be applied when stepping font sizes upwards */
    private static final double FONT_SIZE_GROWTH_FACTOR = 1.2;
    
    /**
     * Create a length property which can handle relative font sizes 
     * @param propId the font size property id.
     */
    public FontSizePropertyMaker(int propId) {
        super(propId);
    }
    
    /**
     * @see PropertyMaker#convertProperty
     * Implements the parts of 7.8.4 relevant to relative font sizes
     */
    public Property convertProperty(Property p,
                                    PropertyList propertyList,
                                    FObj fo) throws PropertyException {
        if (p.getEnum() == EN_LARGER || p.getEnum() == EN_SMALLER) {
            // get the corresponding property from parent
            Property pp = propertyList.getFromParent(this.getPropId());
            int baseFontSize = computeClosestAbsoluteFontSize(pp.getLength().getValue());
            if (p.getEnum() == EN_LARGER) {
                return new FixedLength((int)Math.round((baseFontSize * FONT_SIZE_GROWTH_FACTOR)));
            } else {
                return new FixedLength((int)Math.round((baseFontSize / FONT_SIZE_GROWTH_FACTOR)));
            }
        }
        return super.convertProperty(p, propertyList, fo);
    }
    
    /**
     * Calculates the nearest absolute font size to the given
     * font size.
     * @param baseFontSize the font size in mpt of the parent fo
     * @return the closest absolute font size
     */
    private int computeClosestAbsoluteFontSize(int baseFontSize) {
        double scale = FONT_SIZE_GROWTH_FACTOR;
        int lastStepFontSize = FONT_SIZE_NORMAL;
        if (baseFontSize < FONT_SIZE_NORMAL) {
            // Need to shrink the font sizes = scale downwards
            scale = 1 / FONT_SIZE_GROWTH_FACTOR;
        }
        // Calculate the inital next step font size
        int nextStepFontSize = (int)Math.round(lastStepFontSize * scale);
        while (scale < 1 && nextStepFontSize > baseFontSize
                || scale > 1 && nextStepFontSize < baseFontSize) {
            // baseFontSize is still bigger (if we grow) or smaller (if we shrink)
            // than the last caculated step
            lastStepFontSize = nextStepFontSize;
            nextStepFontSize = (int)Math.round(lastStepFontSize * scale);
        }
        // baseFontSize is between last and next step font size
        // Return the step value closer to the baseFontSize
        if (Math.abs(lastStepFontSize - baseFontSize) 
                <= Math.abs(baseFontSize - nextStepFontSize)) {
            return lastStepFontSize;
        }
        return nextStepFontSize;
    }

}
