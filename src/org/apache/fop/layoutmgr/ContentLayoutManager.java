/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.layoutmgr;

import java.util.List;

import org.apache.fop.area.Area;
import org.apache.fop.area.MinOptMax;
import org.apache.fop.area.Resolveable;
import org.apache.fop.area.PageViewport;

/**
 * Content Layout Manager.
 * For use with objects that contain inline areas such as
 * leader use-content and title.
 */
public class ContentLayoutManager implements LayoutManager {

    private Area holder;
    private int stackSize;
    private LayoutManager parentLM;

    /**
     * Constructs a new ContentLayoutManager
     *
     * @param area  The parent area
     */
    public ContentLayoutManager(Area area) {
        holder = area;
    }

    public void fillArea(LayoutManager curLM) {

        List childBreaks = new java.util.ArrayList();
        MinOptMax stack = new MinOptMax();
        int ipd = 1000000;
        BreakPoss bp;

        LayoutContext childLC = new LayoutContext(LayoutContext.NEW_AREA);
        childLC.setLeadingSpace(new SpaceSpecifier(false));
        childLC.setTrailingSpace(new SpaceSpecifier(false));
        // set stackLimit for lines
        childLC.setStackLimit(new MinOptMax(ipd));
        childLC.setRefIPD(ipd);

        int lineHeight = 14000;
        int lead = 12000;
        int follow = 2000;

        int halfLeading = (lineHeight - lead - follow) / 2;
        // height before baseline
        int lineLead = lead + halfLeading;
        // maximum size of top and bottom alignment
        int maxtb = follow + halfLeading;
        // max size of middle alignment below baseline
        int middlefollow = maxtb;

        while (!curLM.isFinished()) {
            if ((bp = curLM.getNextBreakPoss(childLC)) != null) {
                stack.add(bp.getStackingSize());
                childBreaks.add(bp);

                if (bp.getLead() > lineLead) {
                    lineLead = bp.getLead();
                }
                if (bp.getTotal() > maxtb) {
                    maxtb = bp.getTotal();
                }
                if (bp.getMiddle() > middlefollow) {
                    middlefollow = bp.getMiddle();
                }
            }
        }

        if (maxtb - lineLead > middlefollow) {
            middlefollow = maxtb - lineLead;
        }

        //if(holder instanceof InlineParent) {
        //    ((InlineParent)holder).setHeight(lineHeight);
        //}

        LayoutContext lc = new LayoutContext(0);
        lc.setBaseline(lineLead);
        lc.setLineHeight(lineHeight);

        lc.setFlags(LayoutContext.RESOLVE_LEADING_SPACE, true);
        lc.setLeadingSpace(new SpaceSpecifier(false));
        lc.setTrailingSpace(new SpaceSpecifier(false));
        PositionIterator breakPosIter =
                new BreakPossPosIter(childBreaks, 0, childBreaks.size());
        curLM.addAreas(breakPosIter, lc);
        stackSize = stack.opt;
    }

    public int getStackingSize() {
        return stackSize;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public boolean generatesInlineAreas() {
        return true;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public Area getParentArea(Area childArea) {
        return holder;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public boolean addChild(Area childArea) {
        holder.addChild(childArea);
        return true;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void setParentLM(LayoutManager lm) {
        parentLM = lm;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public boolean canBreakBefore(LayoutContext lc) {
        return false;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public BreakPoss getNextBreakPoss(LayoutContext context) {
        return null;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public boolean isFinished() {
        return false;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void setFinished(boolean isFinished) {
        //to be done
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void addAreas(PositionIterator posIter, LayoutContext context) { }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void init() {
        //to be done
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void resetPosition(Position position) {
        //to be done
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void getWordChars(StringBuffer sbChars, Position bp1,
            Position bp2) { }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public String getCurrentPageNumber() {
        return parentLM.getCurrentPageNumber();
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public PageViewport resolveRefID(String ref) {
        return parentLM.resolveRefID(ref);
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void addIDToPage(String id) {
        parentLM.addIDToPage(id);
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void addUnresolvedArea(String id, Resolveable res) {
        parentLM.addUnresolvedArea(id, res);
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void addMarker(String name, LayoutManager lm, boolean start) {
        parentLM.addMarker(name, lm, start);
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public LayoutManager retrieveMarker(String name, int pos, int boundary) {
        return parentLM.retrieveMarker(name, pos, boundary);
    }
}

