/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
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
 * This class can be extended to handle the creation and adding of the
 * inline area.
 */
public class LeafNodeLayoutManager extends AbstractLayoutManager {
    /**
     * The inline area that this leafnode will add.
     */
    protected InlineArea curArea = null;
    private int alignment;
    private int lead;
    private MinOptMax ipd;

    /**
     * Create a Leaf node layout mananger.
     * @param fobj the fo object that created this manager
     */
    public LeafNodeLayoutManager(FObj fobj) {
        super(fobj);
    }

    /**
     * get the inline area.
     * @param context the context used to create the area
     * @return the current inline area for this layout manager
     */
    public InlineArea get(LayoutContext context) {
        return curArea;
    }

    /**
     * Check if this generates inline areas.
     * @return true always since this is an inline area manager
     */
    public boolean generatesInlineAreas() {
        return true;
    }

    /**
     * Check if this inline area is resolved due to changes in
     * page or ipd.
     * Currently not used.
     * @return true if the area is resolved when adding
     */
    public boolean resolved() {
        return false;
    }

    /**
     * Set the current inline area.
     * @param ia the inline area to set for this layout manager
     */
    public void setCurrentArea(InlineArea ia) {
        curArea = ia;
    }

    /**
     * Set the alignment of the inline area.
     * @param al the vertical alignment positioning
     */
    public void setAlignment(int al) {
        alignment = al;
    }

    /**
     * Set the lead for this inline area.
     * The lead is the distance from the top of the object
     * to the baseline.
     * Currently not used.
     * @param l the lead value
     */
    public void setLead(int l) {
        lead = l;
    }

    /**
     * This is a leaf-node, so this method is never called.
     * @param childArea the childArea to add
     * @return not used
     */
    public boolean addChild(Area childArea) {
        return false;
    }

    /**
     * This is a leaf-node, so this method is never called.
     * @param childArea the childArea to get the parent for
     * @return the parent area
     */
    public Area getParentArea(Area childArea) {
        return null;
    }

    /**
     * Get the next break position.
     * Since this holds an inline area it will return a single
     * break position.
     * @param context the layout context for this inline area
     * @return the break poisition for adding this inline area
     */
    public BreakPoss getNextBreakPoss(LayoutContext context) {
        curArea = get(context);
        if (curArea == null) {
            setFinished(true);
            return null;
        }
        BreakPoss bp = new BreakPoss(new LeafPosition(this, 0),
                                     BreakPoss.CAN_BREAK_AFTER
                                     | BreakPoss.CAN_BREAK_BEFORE | BreakPoss.ISFIRST
                                     | BreakPoss.ISLAST);
        ipd = getAllocationIPD(context.getRefIPD());
        bp.setStackingSize(ipd);
        bp.setNonStackingSize(curArea.getAllocationBPD());
        bp.setTrailingSpace(new SpaceSpecifier(false));

        int bpd = curArea.getHeight();
        switch (alignment) {
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

    /**
     * Get the allocation ipd of the inline area.
     * This method may be overridden to handle percentage values.
     * @param refIPD the ipd of the parent reference area
     * @return the min/opt/max ipd of the inline area
     */
    protected MinOptMax getAllocationIPD(int refIPD) {
        return new MinOptMax(curArea.getIPD());
    }

    /**
     * Reset the position.
     * If the reset position is null then this inline area should be
     * restarted.
     * @param resetPos the position to reset.
     */
    public void resetPosition(Position resetPos) {
        // only reset if setting null, start again
        if (resetPos == null) {
            setFinished(false);
        }
    }

    /**
     * Add the area for this layout manager.
     * This adds the single inline area to the parent.
     * @param posIter the position iterator
     * @param context the layout context for adding the area
     */
    public void addAreas(PositionIterator posIter, LayoutContext context) {
        parentLM.addChild(curArea);

        addID();

        offsetArea(context);
        widthAdjustArea(context);

        while (posIter.hasNext()) {
            posIter.next();
        }
    }

    /**
     * Offset this area.
     * Offset the inline area in the bpd direction when adding the
     * inline area.
     * This is used for vertical alignment.
     * Subclasses should override this if necessary.
     * @param context the layout context used for adding the area
     */
    protected void offsetArea(LayoutContext context) {
        int bpd = curArea.getHeight();
        switch (alignment) {
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
    }

    /**
     * Adjust the width of the area when adding.
     * This uses the min/opt/max values to adjust the with
     * of the inline area by a percentage.
     * @param context the layout context for adding this area
     */
    protected void widthAdjustArea(LayoutContext context) {
        double dAdjust = context.getIPDAdjust();
        int width = ipd.opt;
        if (dAdjust < 0) {
            width = (int)(width + dAdjust * (ipd.opt - ipd.min));
        } else if (dAdjust > 0) {
            width = (int)(width + dAdjust * (ipd.max - ipd.opt));
        }
        curArea.setWidth(width);
    }

    /**
     * Check if can break before this area.
     * @param context the layout context to check for the break
     * @return true if can break before this area in the context
     */
    public boolean canBreakBefore(LayoutContext context) {
        return true;
    }
}

