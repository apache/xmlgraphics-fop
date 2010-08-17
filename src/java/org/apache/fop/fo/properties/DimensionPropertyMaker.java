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
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * @author me
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DimensionPropertyMaker extends CorrespondingPropertyMaker {
    
    private int[][] extraCorresponding = null;

    /**
     * Construct a dimension property maker.
     * @param baseMaker the base property maker
     */
    public DimensionPropertyMaker(PropertyMaker baseMaker) {
        super(baseMaker);
    }

    /**
     * Set extra correspondences.
     * @param extraCorresponding the extra correspondences
     */
    public void setExtraCorresponding(int[][] extraCorresponding) {
        this.extraCorresponding = extraCorresponding;
    }

    /**
     * Determine if corresponding property is forced.
     * @param propertyList the property list to use
     * @return true if it is forced
     */
    public boolean isCorrespondingForced(PropertyList propertyList) {
        if (super.isCorrespondingForced(propertyList)) {
            return true;
        }
        for (int i = 0; i < extraCorresponding.length; i++) {
            int wmcorr = extraCorresponding[i][0]; //propertyList.getWritingMode()];
            if (propertyList.getExplicit(wmcorr) != null) {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    public Property compute(PropertyList propertyList) throws PropertyException {
        // Based on [width|height]
        Property p = super.compute(propertyList);
        if (p == null) {
            p = baseMaker.make(propertyList);
        }

        // Based on min-[width|height]
        int wmcorr = propertyList.getWritingMode(extraCorresponding[0][0],
                                        extraCorresponding[0][1],
                                        extraCorresponding[0][2]);
        Property subprop = propertyList.getExplicitOrShorthand(wmcorr);
        if (subprop != null) {
            baseMaker.setSubprop(p, Constants.CP_MINIMUM, subprop);
        }

        // Based on max-[width|height]
        wmcorr = propertyList.getWritingMode(extraCorresponding[1][0],
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
