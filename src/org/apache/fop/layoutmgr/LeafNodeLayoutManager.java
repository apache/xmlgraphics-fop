/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.FObj;
import org.apache.fop.area.Area;
import org.apache.fop.area.MinOptMax;
import org.apache.fop.area.inline.InlineArea;


/**
 * Base LayoutManager for leaf-node FObj, ie: ones which have no children.
 * These are all inline objects. Most of them cannot be split (Text is
 * an exception to this rule.)
 */
public class LeafNodeLayoutManager extends AbstractBPLayoutManager {

    private InlineArea curArea = null;

    public LeafNodeLayoutManager(FObj fobj) {
        super(fobj);
    }

    public int size() {
        return 1;
    }

    public InlineArea get(int index) {
        if (index > 0)
            return null;
        return curArea;
    }

    public boolean generatesInlineAreas() {
        return true;
    }

    public boolean resolved() {
        return false;
    }

    public void setCurrentArea(InlineArea ia) {
        curArea = ia;
    }

    public boolean generateAreas() {
        return flush();
    }

    protected boolean flush() {
        return false;
    }

    /**
     * This is a leaf-node, so this method is never called.
     */
    public boolean addChild(Area childArea) {
        return false;
    }


    /**
     * This is a leaf-node, so this method is never called.
     */
    public Area getParentArea(Area childArea) {
        return null;
    }

    public BreakPoss getNextBreakPoss(LayoutContext context,
                                      Position prevBreakPoss) {
        curArea = get(0);
        if (curArea == null) {
            setFinished(true);
            return null;
        }
        BreakPoss bp = new BreakPoss(new LeafPosition(this, 0),
                                     BreakPoss.CAN_BREAK_AFTER |
                                     BreakPoss.CAN_BREAK_BEFORE | BreakPoss.ISFIRST |
                                     BreakPoss.ISLAST | BreakPoss.REST_ARE_SUPPRESS_AT_LB);
        bp.setStackingSize(curArea.getAllocationIPD());
        setFinished(true);
        return bp;
    }

    public void addAreas(PositionIterator posIter, LayoutContext context) {
        parentLM.addChild(curArea);
        while (posIter.hasNext()) {
            posIter.next();
        }
    }

    public boolean canBreakBefore(LayoutContext context) {
        return true;
    }
}

