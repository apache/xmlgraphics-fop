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
    public static final int START_BLOCK =          0x02;
    public static final int START_AREA =          0x02; // inline too
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


    public int flags;  // Contains some set of flags defined above
    /**
     * Total available stacking dimension for a "galley-level" layout
     * manager (Line or Flow). It is passed by the parent LM. For LineLM,
     * the block LM determines this based on indent properties.
     * These LM <b>may</b> wish to pass this information down to lower
     * level LM to allow them to optimize returned break possibilities.
     */
    MinOptMax stackLimit;

    /** Current stacking dimension, either IPD or BPD, depending on
     * caller. This is generally the value returned by a previous call
     * to getNextBreakPoss().
     */
    MinOptMax m_stackSize;

    /** True if current top-level reference area is spanning. */
    boolean bIsSpan;

    /** inline-progression-dimension of nearest ancestor reference area */
    int refIPD;

    /** Current pending space-after or space-end from preceding area */
    SpaceSpecifier m_pendingSpace;

    public LayoutContext(LayoutContext parentLC) {
        this.flags = parentLC.flags;
        this.refIPD = parentLC.refIPD;
        this.stackLimit = null; // Don't reference parent MinOptMax!
	this.m_pendingSpace = parentLC.m_pendingSpace; //???
        // Copy other fields as necessary. Use clone???
    }

    public LayoutContext() {
        this.flags = 0;
        this.refIPD = 0;
	stackLimit = new MinOptMax(0);
    }

    public void setFlags(int flags) {
	this.flags |= flags;
    }

    public void unsetFlags(int flags) {
	this.flags &= ~flags;
    }

    public boolean isStart() {
	return ((this.flags & START_BLOCK) != 0);
    }

    public void setPendingSpace(SpaceSpecifier space) {
	m_pendingSpace = space;
    }

    public SpaceSpecifier getPendingSpace() {
	return m_pendingSpace;
    }

    public void setStackSize(MinOptMax stackSize) {
	m_stackSize = stackSize;
    }

    public MinOptMax getStackSize() {
	return m_stackSize ;
    }
}
