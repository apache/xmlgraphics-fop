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
 
package org.apache.fop.traits;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.fonts.Font;

/**
 * Store a single Space property value in simplified form, with all
 * Length values resolved. See section 4.3 in the specs.
 */
public class SpaceVal {
    
    private final MinOptMax space;
    private final boolean bConditional;
    private final boolean bForcing;
    private final int iPrecedence; //  Numeric only, if forcing, set to 0

    /**
     * Constructor for SpaceVal objects based on Space objects.
     * @param spaceprop Space object to use
     */
    public SpaceVal(SpaceProperty spaceprop) {
        space = new MinOptMax(spaceprop.getMinimum().getLength().getValue(),
                              spaceprop.getOptimum().getLength().getValue(),
                              spaceprop.getMaximum().getLength().getValue());
        bConditional = 
                (spaceprop.getConditionality().getEnum() == Constants.EN_DISCARD);
        Property precProp = spaceprop.getPrecedence();
        if (precProp.getNumber() != null) {
            iPrecedence = precProp.getNumber().intValue();
            bForcing = false;
        } else {
            bForcing = (precProp.getEnum() == Constants.EN_FORCE);
            iPrecedence = 0;
        }
    }

    /**
     * Constructor for SpaceVal objects based on the full set of properties.
     * @param space space to use
     * @param bConditional Conditionality value
     * @param bForcing Forcing value
     * @param iPrecedence Precedence value
     */
    public SpaceVal(MinOptMax space, boolean bConditional,
                    boolean bForcing, int iPrecedence) {
        this.space = space;
        this.bConditional = bConditional;
        this.bForcing = bForcing;
        this.iPrecedence = iPrecedence;
    }

    static public SpaceVal makeWordSpacing(Property wordSpacing, SpaceVal letterSpacing, Font fs) {
        if (wordSpacing.getEnum() == Constants.EN_NORMAL) {
            // give word spaces the possibility to shrink by a third,
            // and stretch by a half;
            int spaceCharIPD = fs.getCharWidth(' ');
            MinOptMax space = new MinOptMax(-spaceCharIPD / 3, 0, spaceCharIPD / 2);
            return new SpaceVal(
                    MinOptMax.add
                     (space, MinOptMax.multiply(letterSpacing.getSpace(), 2)),
                     true, true, 0);
        } else {
            return new SpaceVal(wordSpacing.getSpace());
        }        
    }

    static public SpaceVal makeLetterSpacing(Property letterSpacing) {
        if (letterSpacing.getEnum() == Constants.EN_NORMAL) {
            // letter spaces are set to zero (or use different values?)
            return new SpaceVal(new MinOptMax(0), true, true, 0);
        } else {
            return new SpaceVal(letterSpacing.getSpace());
        }
    }

    /**
     * Returns the Conditionality value.
     * @return the Conditionality value
     */
    public boolean isConditional() {
        return bConditional;
    }

    /**
     * Returns the Forcing value.
     * @return the Forcing value
     */
    public boolean isForcing() {
        return bForcing;
    }

    /**
     * Returns the Precedence value.
     * @return the Precedence value
     */
    public int getPrecedence() {
        return iPrecedence;
    }

    /**
     * Returns the Space value.
     * @return the Space value
     */
    public MinOptMax getSpace() {
        return space;
    }

    public String toString() {
        return "SpaceVal: " + getSpace().toString();
    }
}

