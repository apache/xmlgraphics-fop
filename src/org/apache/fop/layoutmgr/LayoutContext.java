/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.layoutmgr;

import org.apache.fop.area.MinOptMax;

/**
 * This class is used to pass information to the getNextBreakPoss()
 * method. It is set up by higher level LM and used by lower level LM.
 */
public class LayoutContext {
    /**
     * Values for flags.
     */
    public static final int LINEBREAK_AT_LF_ONLY = 0x01;
    /** Generated break possibility is first in a new area */
    public static final int NEW_AREA =          0x02;
    public static final int IPD_UNKNOWN =          0x04;
    /** Signal to a Line LM that a higher level LM may provoke a change
     *  in the reference area, thus ref area IPD. The LineLM should return
     *  without looking for a line break.
     */
    public static final int CHECK_REF_AREA =       0x08;

    /**
     * If this flag is set, it indicates that any leading fo:character
     * objects with suppress-at-line-break="suppress" should not generate
     * areas. This is the case at the beginning of each new LineArea
     * except the first.
     */
    public static final int SUPPRESS_LEADING_SPACE =       0x10;
    public static final int FIRST_AREA =                   0x20;


    public int flags;  // Contains some set of flags defined above
    /**
     * Total available stacking dimension for a "galley-level" layout
     * manager (Line or Flow). It is passed by the parent LM. For LineLM,
     * the block LM determines this based on indent properties.
     * These LM <b>may</b> wish to pass this information down to lower
     * level LM to allow them to optimize returned break possibilities.
     */
    MinOptMax m_stackLimit;


    /** True if current top-level reference area is spanning. */
    boolean bIsSpan;

    /** inline-progression-dimension of nearest ancestor reference area */
    int refIPD;

    /** Current pending space-after or space-end from preceding area */
    SpaceSpecifier m_pendingSpace;

    public LayoutContext(LayoutContext parentLC) {
        this.flags = parentLC.flags;
        this.refIPD = parentLC.refIPD;
        this.m_stackLimit = null; // Don't reference parent MinOptMax!
	this.m_pendingSpace = parentLC.m_pendingSpace; //???
        // Copy other fields as necessary. Use clone???
    }

    public LayoutContext(int flags) {
        this.flags = flags;
        this.refIPD = 0;
	m_stackLimit = new MinOptMax(0);
	m_pendingSpace = null;
    }

    public void setFlags(int flags) {
	setFlags(flags, true);
    }

    public void setFlags(int flags, boolean bSet) {
	if (bSet) {
	    this.flags |= flags;
	}
	else {
	    this.flags &= ~flags;
	}
    }

    public void unsetFlags(int flags) {
	setFlags(flags, false);
    }

    public boolean isStart() {
	return ((this.flags & NEW_AREA) != 0);
    }

    public boolean startsNewArea() {
	return ((this.flags & NEW_AREA) != 0 && m_pendingSpace != null);
    }

    public boolean isFirstArea() {
	return ((this.flags & FIRST_AREA) != 0);
    }

    public boolean suppressLeadingSpace() {
	return ((this.flags & SUPPRESS_LEADING_SPACE) != 0);
    }

    public void setPendingSpace(SpaceSpecifier space) {
	m_pendingSpace = space;
    }

    public SpaceSpecifier getPendingSpace() {
	return m_pendingSpace;
    }

    public void setStackLimit(MinOptMax stackLimit) {
	m_stackLimit = stackLimit;
    }

    public MinOptMax getStackLimit() {
	return m_stackLimit ;
    }
}
