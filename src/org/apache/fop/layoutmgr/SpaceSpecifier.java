/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.area.MinOptMax;
import org.apache.fop.traits.SpaceVal;
import java.util.Vector;

/**
 * Accumulate a sequence of space-specifiers (XSL space type) on
 * areas with a stacking constraint. Provide a way to resolve these into
 * a single MinOptMax value.
 */
public class SpaceSpecifier implements Cloneable {


    private boolean m_bStartsRefArea;
    private boolean m_bHasForcing=false;
    private Vector m_vecSpaceVals = new Vector(3);


    public SpaceSpecifier(boolean bStartsRefArea) {
	m_bStartsRefArea = bStartsRefArea;
    }

    public Object clone() {
	try {
	    SpaceSpecifier ss = (SpaceSpecifier)super.clone();
	    // Clone the vector, but share the objects in it!
	    ss.m_vecSpaceVals = new Vector(this.m_vecSpaceVals.size());
	    ss.m_vecSpaceVals.addAll(this.m_vecSpaceVals);
	    return ss;
	} catch (CloneNotSupportedException cnse) {
	    return null;
	}
	
    }

    /**
     * Clear all space specifiers
     */
    public void clear() {
	m_bHasForcing=false;
	m_vecSpaceVals.clear();
    }


    /** Return true if any space-specifiers have been added. */
    public boolean hasSpaces() {
	return (m_vecSpaceVals.size() > 0);
    }

    /**
     * Add a new space to the sequence. If this sequence starts a reference
     * area, and the added space is conditional, and there are no
     * non-conditional values in the sequence yet, then ignore it. Otherwise
     * add it to the sequence.
     */
    public void addSpace(SpaceVal moreSpace) {
	if (!m_bStartsRefArea || !moreSpace.bConditional ||
	    !m_vecSpaceVals.isEmpty()) {
	    if (moreSpace.bForcing) {
		if (m_bHasForcing == false) {
		    // Remove all other values (must all be non-forcing)
		    m_vecSpaceVals.clear();
		    m_bHasForcing = true;
		}
		m_vecSpaceVals.add(moreSpace);
	    }
	    else if (m_bHasForcing==false) {
		// Don't bother adding all 0 space-specifier if not forcing
		if (moreSpace.space.min != 0 || moreSpace.space.opt != 0 ||
		    moreSpace.space.max != 0) {
		    m_vecSpaceVals.add(moreSpace);
		}
	    }
	}
    }


    /**
     * Resolve the current sequence of space-specifiers, accounting for
     * forcing values.
     * @param bEndsReferenceArea True if the sequence should be resolved
     * at the trailing edge of reference area.
     * @return The resolved value as a min/opt/max triple.
     */
    public MinOptMax resolve(boolean bEndsReferenceArea) {
	int lastIndex = m_vecSpaceVals.size();
	if (bEndsReferenceArea) {
	    // Start from the end and count conditional specifiers
	    // Stop at first non-conditional
	    for (; lastIndex > 0; --lastIndex) {
		SpaceVal sval =
		    (SpaceVal)m_vecSpaceVals.elementAt(lastIndex-1);
		if (!sval.bConditional) {
		    break;
		}
	    }
	}
	MinOptMax resSpace = new MinOptMax(0);
	int iMaxPrec = -1;
	for(int index=0; index < lastIndex; index++) {
	    SpaceVal sval = (SpaceVal)m_vecSpaceVals.elementAt(index);
	    if (m_bHasForcing) {
		resSpace.add(sval.space);
	    }
	    else if (sval.iPrecedence > iMaxPrec) {
		iMaxPrec = sval.iPrecedence;
		resSpace = sval.space;
	    }
	    else if (sval.iPrecedence == iMaxPrec)  {
		if (sval.space.opt > resSpace.opt) {
		    resSpace = sval.space;
		}
		else if (sval.space.opt == resSpace.opt) {
		    if (resSpace.min < sval.space.min) {
			resSpace.min = sval.space.min;
		    }
		    if (resSpace.max > sval.space.max) {
			resSpace.max = sval.space.max;
		    }
		}
	    }
	    
	}
	return resSpace;
    }
}
