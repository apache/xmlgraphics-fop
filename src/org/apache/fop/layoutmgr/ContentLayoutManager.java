/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;


import org.apache.fop.area.Area;
import org.apache.fop.area.MinOptMax;
import org.apache.fop.area.Resolveable;
import org.apache.fop.area.PageViewport;

import java.util.ArrayList;

/**
 * Content Layout Manager.
 * For use with objects that contain inline areas such as
 * leader use-content and title.
 */
public class ContentLayoutManager implements LayoutManager {
    Area holder;
    int stackSize;
    LayoutManager parentLM;

    public ContentLayoutManager(Area area) {
        holder = area;
    }

    public void fillArea(LayoutManager curLM) {

        ArrayList childBreaks = new ArrayList();
        MinOptMax stack = new MinOptMax();
        int ipd = 1000000;
        BreakPoss bp;

        LayoutContext childLC = new LayoutContext(LayoutContext.NEW_AREA);
        childLC.setLeadingSpace(new SpaceSpecifier(false));
        childLC.setTrailingSpace(new SpaceSpecifier(false));
        // set stackLimit for lines
        childLC.setStackLimit(new MinOptMax(ipd));
        childLC.setRefIPD(ipd);

        while (!curLM.isFinished()) {
            if ((bp = curLM.getNextBreakPoss(childLC)) != null) {
                stack.add(bp.getStackingSize());
                childBreaks.add(bp);
            }
        }

        LayoutContext lc = new LayoutContext(0);
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

    public boolean generatesInlineAreas() {
        return true;
    }

    public Area getParentArea (Area childArea) {
        return holder;
    }

    public boolean addChild (Area childArea) {
        holder.addChild(childArea);
        return true;
    }

    public void setParentLM(LayoutManager lm) {
        parentLM = lm;
    }

    public boolean canBreakBefore(LayoutContext lc) {
        return false;
    }

    public BreakPoss getNextBreakPoss(LayoutContext context) {
        return null;
    }

    public boolean isFinished() {
        return false;
    }

    public void setFinished(boolean isFinished) {
    }

    public void addAreas(PositionIterator posIter, LayoutContext context) {
    }

    public void init() {
    }

    public void resetPosition(Position position) {
    }

    public void getWordChars(StringBuffer sbChars, Position bp1,
                             Position bp2) {
    }

    public String getCurrentPageNumber() {
        return parentLM.getCurrentPageNumber();
    }

    public PageViewport resolveRefID(String ref) {
        return parentLM.resolveRefID(ref);;
    }

    public void addIDToPage(String id) {
        parentLM.addIDToPage(id);
    }

    public void addUnresolvedArea(String id, Resolveable res) {
        parentLM.addUnresolvedArea(id, res);
    }
}

