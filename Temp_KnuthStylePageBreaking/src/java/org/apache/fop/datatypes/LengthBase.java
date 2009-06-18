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

package org.apache.fop.datatypes;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Models a length which can be used as a factor in a percentage length
 * calculation
 */
public class LengthBase implements PercentBase {
    // Standard kinds of percent-based length
    /** constant for a custom percent-based length */
    public static final int CUSTOM_BASE = 0;
    /** constant for a font-size percent-based length */
    public static final int FONTSIZE = 1;
    /** constant for an inh font-size percent-based length */
    public static final int INH_FONTSIZE = 2;
    /** constant for a containing box percent-based length */
    public static final int CONTAINING_BOX = 3;
    /** constant for a containing refarea percent-based length */
    public static final int CONTAINING_REFAREA = 4;
    /** constant for a containing block percent-based length */
    public static final int BLOCK_WIDTH = 5;
    /** constant for a containing block percent-based length */
    public static final int BLOCK_HEIGHT = 6;
    /** constant for a image intrinsic percent-based length */
    public static final int IMAGE_INTRINSIC_WIDTH = 7;
    /** constant for a image intrinsic percent-based length */
    public static final int IMAGE_INTRINSIC_HEIGHT = 8;

    /** array of valid percent-based length types */
    public static final int[] PERCENT_BASED_LENGTH_TYPES
            = {CUSTOM_BASE, FONTSIZE, INH_FONTSIZE, CONTAINING_BOX,
               CONTAINING_REFAREA, 
               IMAGE_INTRINSIC_WIDTH, IMAGE_INTRINSIC_HEIGHT};

    /**
     * FO parent of the FO for which this property is to be calculated.
     */
    protected /* final */ FObj parentFO;

    /**
     * PropertyList for the FO where this property is calculated.
     */
    private /* final */ PropertyList propertyList;

    /**
     * One of the defined types of LengthBase
     */
    private /* final */ int iBaseType;

    /**
     * Constructor
     * @param parentFO parent FO for this
     * @param plist property list for this
     * @param iBaseType a member of {@link #PERCENT_BASED_LENGTH_TYPES}
     */
    public LengthBase(FObj parentFO, PropertyList plist, int iBaseType) {
        this.parentFO = parentFO;
        this.propertyList = plist;
        this.iBaseType = iBaseType;
    }

    /**
     * Accessor for parentFO object from subclasses which define
     * custom kinds of LengthBase calculations.
     * @return this object's parent FO
     */
    protected FObj getParentFO() {
        return parentFO;
    }

    /**
     * Accessor for propertyList object from subclasses which define
     * custom kinds of LengthBase calculations.
     * @return this object's PropertyList
     */
    protected PropertyList getPropertyList() {
        return propertyList;
    }

    /**
     * @return the dimension of this object (always 1)
     */
    public int getDimension() {
        return 1;
    }

    /**
     * @return the base value of this object (always 1.0)
     */
    public double getBaseValue() {
        return 1.0;
    }

    /** @see org.apache.fop.datatypes.PercentBase#getBaseLength() */
    public int getBaseLength() throws PropertyException {
        //TODO Don't use propertyList here
        //See http://nagoya.apache.org/eyebrowse/ReadMsg?listName=fop-dev@xml.apache.org&msgNo=10342
        switch (iBaseType) {
        case FONTSIZE:
            return propertyList.get(Constants.PR_FONT_SIZE).getLength().getValue();
        case INH_FONTSIZE:
            return propertyList.getInherited(Constants.PR_FONT_SIZE).getLength().getValue();
        case BLOCK_WIDTH:
            return parentFO.getLayoutDimension(PercentBase.BLOCK_IPD).intValue();
        case BLOCK_HEIGHT:
            return parentFO.getLayoutDimension(PercentBase.BLOCK_BPD).intValue();
        case CONTAINING_REFAREA:    // example: start-indent, end-indent
            FObj fo;
            fo = parentFO;
            while (fo != null && !fo.generatesReferenceAreas()) {
                fo = fo.findNearestAncestorFObj();
            }
            if (fo != null && fo instanceof FObj) {
                return fo.getLayoutDimension(PercentBase.BLOCK_IPD).intValue();
            } else {
                return 0;
            }
        case IMAGE_INTRINSIC_WIDTH:
            return propertyList.getFObj()
                .getLayoutDimension(PercentBase.IMAGE_INTRINSIC_WIDTH).intValue();
        case IMAGE_INTRINSIC_HEIGHT:
            return propertyList.getFObj()
                .getLayoutDimension(PercentBase.IMAGE_INTRINSIC_HEIGHT).intValue();
        case CUSTOM_BASE:
            //log.debug("!!! LengthBase.getBaseLength() called on CUSTOM_BASE type !!!");
            return 0;
        default:
            parentFO.getLogger().error("Unknown base type for LengthBase.");
            return 0;
        }
    }

}

