/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.traits;

import org.apache.fop.datatypes.Space;
import org.apache.fop.area.MinOptMax;
import org.apache.fop.fo.Property;
import org.apache.fop.fo.properties.Constants;

/**
 * Store a single Space property value in simplified form, with all
 * Length values resolved.
 */
public class SpaceVal {
    public final MinOptMax space;
    public final boolean bConditional;
    public final boolean bForcing;
    public final int iPrecedence; //  Numeric only, if forcing, set to 0

    public SpaceVal(Space spaceprop) {
	space = new MinOptMax(
			      spaceprop.getMinimum().getLength().mvalue(),
			      spaceprop.getOptimum().getLength().mvalue(),
			      spaceprop.getMaximum().getLength().mvalue());
	bConditional = (spaceprop.getConditionality().getEnum() ==
			Constants.DISCARD);
	Property precProp = spaceprop.getPrecedence();
	if (precProp.getNumber() != null) {
	    iPrecedence = precProp.getNumber().intValue();
	    bForcing = false;
	}
	else {
	    bForcing = (precProp.getEnum() == Constants.FORCE);
	    iPrecedence=0;
	}
    }

    public SpaceVal(MinOptMax space, boolean bConditional, boolean bForcing,
		    int iPrecedence) {
	this.space = space;
	this.bConditional = bConditional;
	this.bForcing = bForcing;
	this.iPrecedence = iPrecedence;
    }

}

