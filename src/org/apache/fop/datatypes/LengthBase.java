/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;

public class LengthBase implements PercentBase {
    // Standard kinds of percent-based length
    public static final int CUSTOM_BASE = 0;
    public static final int FONTSIZE = 1;
    public static final int INH_FONTSIZE = 2;
    public static final int CONTAINING_BOX = 3;
    public static final int CONTAINING_REFAREA = 4;

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

    public LengthBase(FObj parentFO, PropertyList plist, int iBaseType) {
        this.parentFO = parentFO;
        this.propertyList = plist;
        this.iBaseType = iBaseType;
    }

    /**
     * Accessor for parentFO object from subclasses which define
     * custom kinds of LengthBase calculations.
     */
    protected FObj getParentFO() {
        return parentFO;
    }

    /**
     * Accessor for propertyList object from subclasses which define
     * custom kinds of LengthBase calculations.
     */
    protected PropertyList getPropertyList() {
        return propertyList;
    }

    public int getDimension() {
        return 1;
    }

    public double getBaseValue() {
        return 1.0;
    }

    public int getBaseLength() {
        switch (iBaseType) {
        case FONTSIZE:
            return propertyList.get("font-size").getLength().mvalue();
        case INH_FONTSIZE:
            return propertyList.getInherited("font-size").getLength().mvalue();
        case CONTAINING_BOX:
            // depends on property?? inline-progression vs block-progression
            return parentFO.getContentWidth();
        case CONTAINING_REFAREA:    // example: start-indent, end-indent
         {
            //FONode fo;
            //for (fo = parentFO; fo != null && !fo.generatesReferenceAreas();
            //        fo = fo.getParent());
            //return (((fo != null) && (fo instanceof FObj)) ? ((FObj)fo).getContentWidth() : 0);
            return 0;
        }
        case CUSTOM_BASE:
            //log.debug("!!! LengthBase.getBaseLength() called on CUSTOM_BASE type !!!");
            return 0;
        default:
            //log.error("Unknown base type for LengthBase.");
            return 0;
        }
    }

}

