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

package org.apache.fop.traits;

import org.apache.fop.datatypes.PercentBaseContext;
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
    private final boolean conditional;
    private final boolean forcing;
    private final int precedence; //  Numeric only, if forcing, set to 0

    /**
     * Constructor for SpaceVal objects based on Space objects.
     * @param spaceprop Space object to use
     * @param context Percentage evaluation context
     */
    public SpaceVal(SpaceProperty spaceprop, PercentBaseContext context) {
        space = createSpaceProperty(spaceprop, context);
        conditional = (spaceprop.getConditionality().getEnum() == Constants.EN_DISCARD);
        Property precProp = spaceprop.getPrecedence();
        if (precProp.getNumber() != null) {
            precedence = precProp.getNumber().intValue();
            forcing = false;
        } else {
            forcing = (precProp.getEnum() == Constants.EN_FORCE);
            precedence = 0;
        }
    }

    private static MinOptMax createSpaceProperty(SpaceProperty spaceprop,
                                                 PercentBaseContext context) {
        int min = spaceprop.getMinimum(context).getLength().getValue(context);
        int opt = spaceprop.getOptimum(context).getLength().getValue(context);
        int max = spaceprop.getMaximum(context).getLength().getValue(context);
        return MinOptMax.getInstance(min, opt, max);
    }

    /**
     * Constructor for SpaceVal objects based on the full set of properties.
     * @param space space to use
     * @param bConditional Conditionality value
     * @param bForcing Forcing value
     * @param iPrecedence Precedence value
     */
    public SpaceVal(MinOptMax space, boolean conditional, boolean forcing, int precedence) {
        this.space = space;
        this.conditional = conditional;
        this.forcing = forcing;
        this.precedence = precedence;
    }

    public static SpaceVal makeWordSpacing(Property wordSpacing, SpaceVal letterSpacing, Font fs) {
        if (wordSpacing.getEnum() == Constants.EN_NORMAL) {
            // give word spaces the possibility to shrink by a third,
            // and stretch by a half;
            int spaceCharIPD = fs.getCharWidth(' ');
            MinOptMax space = MinOptMax.getInstance(-spaceCharIPD / 3, 0, spaceCharIPD / 2);
            //TODO Adding 2 letter spaces here is not 100% correct. Spaces don't have letter spacing
            return new SpaceVal(space.plus(letterSpacing.getSpace().mult(2)), true, true, 0);
        } else {
            return new SpaceVal(wordSpacing.getSpace(), null);
        }
    }

    public static SpaceVal makeLetterSpacing(Property letterSpacing) {
        if (letterSpacing.getEnum() == Constants.EN_NORMAL) {
            // letter spaces are set to zero (or use different values?)
            return new SpaceVal(MinOptMax.ZERO, true, true, 0);
        } else {
            return new SpaceVal(letterSpacing.getSpace(), null);
        }
    }

    /**
     * Returns the Conditionality value.
     * @return the Conditionality value
     */
    public boolean isConditional() {
        return conditional;
    }

    /**
     * Returns the Forcing value.
     * @return the Forcing value
     */
    public boolean isForcing() {
        return forcing;
    }

    /**
     * Returns the Precedence value.
     * @return the Precedence value
     */
    public int getPrecedence() {
        return precedence;
    }

    /**
     * Returns the Space value.
     * @return the Space value
     */
    public MinOptMax getSpace() {
        return space;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "SpaceVal: " + getSpace().toString();
    }
}

