/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.FObj;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.area.Area;
import org.apache.fop.area.Resolveable;
import org.apache.fop.area.PageViewport;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.apache.fop.traits.MinOptMax;

/**
 * Content Layout Manager.
 * For use with objects that contain inline areas such as
 * leader use-content and title.
 */
public class ContentLayoutManager implements LayoutProcessor {
    private FOUserAgent userAgent;
    private Area holder;
    private int stackSize;
    private LayoutProcessor parentLM;

    /**
     * Constructs a new ContentLayoutManager
     *
     * @param area  The parent area
     */
    public ContentLayoutManager(Area area) {
        holder = area;
    }

    /**
     * Set the FO object for this layout manager
     *
     * @param fo the fo for this layout manager
     */
    public void setFObj(FObj fo) {
    }

    public void fillArea(LayoutProcessor curLM) {

        List childBreaks = new ArrayList();
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
            MinOptMax lastSize = null;
            if ((bp = curLM.getNextBreakPoss(childLC)) != null) {
                lastSize = bp.getStackingSize();
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
            if (lastSize != null) {
                stack.add(lastSize);
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
    public void addChild(Area childArea) {
        holder.addChild(childArea);
    }

    /**
     * Set the user agent.
     *
     * @param ua the user agent
     */
    public void setUserAgent(FOUserAgent ua) {
        userAgent = ua;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getUserAgent()
     */
    public FOUserAgent getUserAgent() {
        return userAgent;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void setParent(LayoutProcessor lm) {
        parentLM = lm;
    }

    public LayoutProcessor getParent() {
        return this.parentLM;
    }

    public LayoutManagerLS getLayoutManagerLS() {
        return getParent().getLayoutManagerLS();
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
    public void initialize() {
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
    public void addMarkerMap(Map marks, boolean start, boolean isfirst) {
        parentLM.addMarkerMap(marks, start, isfirst);
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public Marker retrieveMarker(String name, int pos, int boundary) {
        return parentLM.retrieveMarker(name, pos, boundary);
    }
}

