/*
 * $Id: ContentLayoutManager.java,v 1.17 2003/03/07 07:58:51 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.layoutmgr;

import org.apache.fop.fo.FObj;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.area.Area;
import org.apache.fop.area.Resolveable;
import org.apache.fop.area.PageViewport;

import org.apache.avalon.framework.logger.Logger;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

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

    /**
     * Returns the logger
     * @return the logger
     */
    protected Logger getLogger() {
        return userAgent.getLogger();
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public void setParent(LayoutProcessor lm) {
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
    public void addMarkerMap(Map marks, boolean start, boolean isfirst) {
        parentLM.addMarkerMap(marks, start, isfirst);
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public Marker retrieveMarker(String name, int pos, int boundary) {
        return parentLM.retrieveMarker(name, pos, boundary);
    }
}

