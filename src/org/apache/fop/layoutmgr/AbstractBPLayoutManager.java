/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyManager;
import org.apache.fop.fo.FONode;
import org.apache.fop.area.Area;

import java.util.ListIterator;
import java.util.ArrayList;

/**
 * The base class for all BPLayoutManagers.
 */
public abstract class AbstractBPLayoutManager extends AbstractLayoutManager
    implements BPLayoutManager {


    /** True if this LayoutManager has handled all of its content. */
    private boolean m_bFinished = false;


    public AbstractBPLayoutManager(FObj fobj) {
	super(fobj);
    }


    /**
     * This method provides a hook for a LayoutManager to intialize traits
     * for the areas it will create, based on Properties set on its FO.
     */
    protected final void initProperties() {
 	if (fobj != null) {
	    initProperties(fobj.getPropertyManager());
 	}
    }


    /**
     * This method provides a hook for a LayoutManager to intialize traits
     * for the areas it will create, based on Properties set on its FO.
     */
    protected void initProperties(PropertyManager pm) {
	System.err.println("AbstractBPLayoutManager.initProperties");
    }


    /**
     * Tell whether this LayoutManager has handled all of its content.
     * @return True if there are no more break possibilities,
     * ie. the last one returned represents the end of the content.
     */
    public boolean isFinished() {
	return m_bFinished;
    }

    public void setFinished(boolean bFinished) {
	m_bFinished = bFinished;
    }


//     /**
//      * Get the BreakPoss at the start of the next "area".
//      * @param lc The LayoutContext for this LayoutManager.
//      * @param bpPrevEnd The Position returned by the previous call
//      * to getNextBreakPoss, or null if none.
//      */
//     public BreakPoss getStartBreakPoss(LayoutContext lc,
// 				       BreakPoss.Position bpPrevEnd) {
// 	return null;
//     }


    /**
     * Generate and return the next break possibility.
     * Each layout manager must implement this.
     * TODO: should this be abstract or is there some reasonable
     * default implementation?
     */
    public BreakPoss getNextBreakPoss(LayoutContext context) {
	return getNextBreakPoss(context, null);
    }


    public BreakPoss getNextBreakPoss(LayoutContext context,
				      BreakPoss.Position prevBreakPoss) {
	return null;
    }

    /**
     * Return value indicating whether the next area to be generated could
     * start a new line or flow area.
     * In general, if can't break at the current level, delegate to
     * the first child LM.
     * NOTE: should only be called if the START_AREA flag is set in context,
     * since the previous sibling LM must have returned a BreakPoss which
     * does not allow break-after.
     * QUESTION: in block-stacked areas, does this mean some kind of keep
     * condition, or is it only used for inline-stacked areas?
     * Default implementation always returns true.
     */
    public boolean canBreakBefore(LayoutContext context) {
	return true;
    }


    public void addAreas(PositionIterator parentIter) {
    }

    /* ---------------------------------------------------------
     * PROVIDE NULL IMPLEMENTATIONS OF METHODS from LayoutManager
     * interface which are declared abstract in AbstractLayoutManager.
     * ---------------------------------------------------------*/
    public Area getParentArea(Area childArea) {
	return null;
    }

    protected boolean flush() {
	return false;
    }



    public boolean addChild(Area childArea) {
        return false;
    }
}

