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
import org.apache.fop.fo.properties.VerticalAlign;

/**
 * Base LayoutManager for leaf-node FObj, ie: ones which have no children.
 * These are all inline objects. Most of them cannot be split (Text is
 * an exception to this rule.)
 */
public class LeafNodeLayoutManager extends AbstractBPLayoutManager {

    private InlineArea curArea = null;
    private int alignment;
    private int lead;

    public LeafNodeLayoutManager(FObj fobj) {
        super(fobj);
    }

    public InlineArea get(LayoutContext context) {
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

    public void setAlignment(int al) {
        alignment = al;
    }

    public void setLead(int l) {
        lead = l;
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
        curArea = get(context);
        if (curArea == null) {
            setFinished(true);
            return null;
        }
        BreakPoss bp = new BreakPoss(new LeafPosition(this, 0),
                                     BreakPoss.CAN_BREAK_AFTER |
                                     BreakPoss.CAN_BREAK_BEFORE | BreakPoss.ISFIRST |
                                     BreakPoss.ISLAST);
        bp.setStackingSize(curArea.getAllocationIPD());
        bp.setNonStackingSize(curArea.getAllocationBPD());
        bp.setTrailingSpace(new SpaceSpecifier(false));

        int bpd = curArea.getHeight();
        switch(alignment) {
            case VerticalAlign.MIDDLE:
                bp.setMiddle(bpd / 2 /* - fontLead/2 */);
                bp.setLead(bpd / 2 /* + fontLead/2 */);
            break;
            case VerticalAlign.TOP:
                bp.setTotal(bpd);
            break;
            case VerticalAlign.BOTTOM:
                bp.setTotal(bpd);
            break;
            case VerticalAlign.BASELINE:
            default:
                bp.setLead(bpd);
            break;
        }
        setFinished(true);
        return bp;
    }

    public void resetPosition(Position resetPos) {
        // only reset if setting null, start again
        if(resetPos == null) {
            setFinished(false);
        }
    }

    public void addAreas(PositionIterator posIter, LayoutContext context) {
        parentLM.addChild(curArea);

        int bpd = curArea.getHeight();
        switch(alignment) {
            case VerticalAlign.MIDDLE:
                curArea.setOffset(context.getBaseline() - bpd / 2 /* - fontLead/2 */);
            break;
            case VerticalAlign.TOP:
                //curArea.setOffset(0);
            break;
            case VerticalAlign.BOTTOM:
                curArea.setOffset(context.getLineHeight() - bpd);
            break;
            case VerticalAlign.BASELINE:
            default:
                curArea.setOffset(context.getBaseline() - bpd);
            break;
        }

        double dAdjust = context.getIPDAdjust();
        MinOptMax ipd = curArea.getAllocationIPD();
        int width = ipd.opt;
        if(dAdjust < 0) {
            width = (int)(width + dAdjust * (ipd.opt - ipd.min));
        } else if(dAdjust > 0) {
            width = (int)(width + dAdjust * (ipd.max - ipd.opt));
        }
        curArea.setWidth(width);

        while (posIter.hasNext()) {
            posIter.next();
        }
    }

    public boolean canBreakBefore(LayoutContext context) {
        return true;
    }
}

