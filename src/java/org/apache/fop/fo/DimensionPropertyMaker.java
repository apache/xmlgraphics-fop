/*
 * Created on Jan 1, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.fop.fo;

import org.apache.fop.apps.FOPException;

/**
 * @author me
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DimensionPropertyMaker extends CorrespondingPropertyMaker {
    int[][] extraCorresponding = null;

    public DimensionPropertyMaker(Property.Maker baseMaker) {
        super(baseMaker);
    }
    
    public void setExtraCorresponding(int[][] extraCorresponding) {
        this.extraCorresponding = extraCorresponding;
    }

    public boolean isCorrespondingForced(PropertyList propertyList) {
        if (super.isCorrespondingForced(propertyList))
            return true;
        for (int i = 0; i < extraCorresponding.length; i++) {
            int wmcorr = extraCorresponding[i][0]; //propertyList.getWritingMode()];
            if (propertyList.getExplicit(wmcorr) != null)
                return true;
        }            
        return false;
    }

    public Property compute(PropertyList propertyList) throws FOPException {
        // Based on [width|height]
        Property p = super.compute(propertyList);
        if (p == null) {
            p = baseMaker.make(propertyList);
        }

        // Based on min-[width|height]
        int wmcorr = propertyList.wmMap(extraCorresponding[0][0], 
                                        extraCorresponding[0][1], 
                                        extraCorresponding[0][2]);
        Property subprop = propertyList.getExplicitOrShorthand(wmcorr);
        if (subprop != null) {
            baseMaker.setSubprop(p, Constants.CP_MINIMUM, subprop);
        }

        // Based on max-[width|height]
        wmcorr = propertyList.wmMap(extraCorresponding[1][0], 
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
