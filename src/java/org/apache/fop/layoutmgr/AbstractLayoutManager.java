/*
 * $Id: AbstractLayoutManager.java,v 1.24 2003/03/07 07:58:51 jeremias Exp $
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
import org.apache.fop.fo.PropertyManager;

import org.apache.avalon.framework.logger.Logger;

import java.util.ListIterator;
import java.util.Map;

/**
 * The base class for all LayoutManagers.
 */
public abstract class AbstractLayoutManager implements LayoutProcessor {
    protected FOUserAgent userAgent;
    protected LayoutProcessor parentLM = null;
    protected FObj fobj;
    protected String foID = null;
    protected Map markers = null;

    /** True if this LayoutManager has handled all of its content. */
    private boolean bFinished = false;
    protected LayoutProcessor curChildLM = null;
    protected ListIterator childLMiter;
    protected boolean bInited = false;

    /**
     * Abstract layout manager.
     */
    public AbstractLayoutManager() {
    }

    /**
     * Set the FO object for this layout manager
     *
     * @param fo the fo for this layout manager
     */
    public void setFObj(FObj fo) {
        this.fobj = fo;
        foID = fobj.getID();
        markers = fobj.getMarkers();
        childLMiter = new LMiter(this, fobj.getChildren());
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
     * Get the user agent.
     *
     * @see org.apache.fop.layoutmgr.LayoutManager#getUserAgent()
     */
    public FOUserAgent getUserAgent() {
        return userAgent;
    }

    protected Logger getLogger() {
        return userAgent.getLogger();
    }

    public void setParent(LayoutProcessor lm) {
        this.parentLM = lm;
    }

    public LayoutProcessor getParent() {
        return this.parentLM;
    }

    public LayoutManagerLS getLayoutManagerLS() {
        return getParent().getLayoutManagerLS();
    }

    //     /**
    //      * Ask the parent LayoutManager to add the current (full) area to the
    //      * appropriate parent area.
    //      * @param bFinished If true, this area is finished, either because it's
    //      * completely full or because there is no more content to put in it.
    //      * If false, we are in the middle of this area. This can happen,
    //      * for example, if we find floats in a line. We stop the current area,
    //      * and add it (temporarily) to its parent so that we can see if there
    //      * is enough space to place the float(s) anchored in the line.
    //      */
    //     protected void flush(Area area, boolean bFinished) {
    // if (area != null) {
    //     // area.setFinished(true);
    //     parentLM.addChild(area, bFinished); // ????
    //     if (bFinished) {
    // setCurrentArea(null);
    //     }
    // }
    //     }

    /**
     * Return an Area which can contain the passed childArea. The childArea
     * may not yet have any content, but it has essential traits set.
     * In general, if the LayoutManager already has an Area it simply returns
     * it. Otherwise, it makes a new Area of the appropriate class.
     * It gets a parent area for its area by calling its parent LM.
     * Finally, based on the dimensions of the parent area, it initializes
     * its own area. This includes setting the content IPD and the maximum
     * BPD.
     */


    public boolean generatesInlineAreas() {
        return false;
    }

    /**
     * Add a child area to the current area. If this causes the maximum
     * dimension of the current area to be exceeded, the parent LM is called
     * to add it.
     */

    /**
     * Return currently active child LayoutManager or null if
     * all children have finished layout.
     * Note: child must implement LayoutManager! If it doesn't, skip it
     * and print a warning.
     */
    protected LayoutProcessor getChildLM() {
        if (curChildLM != null && !curChildLM.isFinished()) {
            return curChildLM;
        }
        while (childLMiter.hasNext()) {
            curChildLM = (LayoutProcessor) childLMiter.next();
            curChildLM.setUserAgent(getUserAgent());
            curChildLM.setParent(this);
            curChildLM.init();
            return curChildLM;
        }
        return null;
    }

    protected boolean hasMoreLM(LayoutManager prevLM) {
        // prevLM should = curChildLM
        if (prevLM != curChildLM) {
            //log.debug("AbstractLayoutManager.peekNextLM: " +
            //                   "passed LM is not current child LM!");
            return false;
        }
        return !childLMiter.hasNext();
    }


    /**
     * Reset the layoutmanager "iterator" so that it will start
     * with the passed Position's generating LM
     * on the next call to getChildLM.
     * @param pos a Position returned by a child layout manager
     * representing a potential break decision.
     * If pos is null, then back up to the first child LM.
     */
    protected void reset(Position pos) {
        //if (lm == null) return;
        LayoutManager lm = (pos != null) ? pos.getLM() : null;
        if (curChildLM != lm) {
            // ASSERT curChildLM == (LayoutManager)childLMiter.previous()
            if (childLMiter.hasPrevious() && curChildLM
                    != (LayoutManager) childLMiter.previous()) {
                //log.error("LMiter problem!");
            }
            while (curChildLM != lm && childLMiter.hasPrevious()) {
                curChildLM.resetPosition(null);
                curChildLM = (LayoutProcessor) childLMiter.previous();
            }
            // Otherwise next returns same object
            childLMiter.next();
        }
        if (curChildLM != null) {
            curChildLM.resetPosition(pos);
        }
        if (isFinished()) {
            setFinished(false);
        }
    }

    public void resetPosition(Position resetPos) {
        //  if (resetPos == null) {
        //      reset(null);
        //  }
    }


    /**
     * This method provides a hook for a LayoutManager to intialize traits
     * for the areas it will create, based on Properties set on its FO.
     */
    public void init() {
        if (fobj != null && bInited == false) {
            initProperties(fobj.getPropertyManager());
            bInited = true;
        }
    }

    /**
     * This method provides a hook for a LayoutManager to intialize traits
     * for the areas it will create, based on Properties set on its FO.
     */
    protected void initProperties(PropertyManager pm) {
        //log.debug("AbstractLayoutManager.initProperties");
    }


    /**
     * Tell whether this LayoutManager has handled all of its content.
     * @return True if there are no more break possibilities,
     * ie. the last one returned represents the end of the content.
     */
    public boolean isFinished() {
        return bFinished;
    }

    public void setFinished(boolean fin) {
        bFinished = fin;
    }


    /**
     * Generate and return the next break possibility.
     * Each layout manager must implement this.
     * TODO: should this be abstract or is there some reasonable
     * default implementation?
     */
    public BreakPoss getNextBreakPoss(LayoutContext context) {
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


    public void addAreas(PositionIterator posIter, LayoutContext context) {
    }


    public void getWordChars(StringBuffer sbChars, Position bp1,
                             Position bp2) {
    }

    /* ---------------------------------------------------------
     * PROVIDE NULL IMPLEMENTATIONS OF METHODS from LayoutManager
     * interface which are declared abstract in AbstractLayoutManager.
     * ---------------------------------------------------------*/
    public Area getParentArea(Area childArea) {
        return null;
    }

    protected void flush() {
    }

    public void addChild(Area childArea) {
    }

    /**
     * Delegate getting the current page number to the parent layout manager.
     *
     * @see org.apache.fop.layoutmgr.LayoutManager
     */
    public String getCurrentPageNumber() {
        return parentLM.getCurrentPageNumber();
    }

    /**
     * Delegate resolving the id reference to the parent layout manager.
     *
     * @see org.apache.fop.layoutmgr.LayoutManager
     */
    public PageViewport resolveRefID(String ref) {
        return parentLM.resolveRefID(ref);
    }

    /**
     * Add the id to the page.
     * If the id string is not null then add the id to the current page.
     */
    protected void addID() {
        if (foID != null) {
            addIDToPage(foID);
        }
    }

    /**
     * Delegate adding id reference to the parent layout manager.
     *
     * @see org.apache.fop.layoutmgr.LayoutManager
     */
    public void addIDToPage(String id) {
        parentLM.addIDToPage(id);
    }

    /**
     * Delegate adding unresolved area to the parent layout manager.
     *
     * @see org.apache.fop.layoutmgr.LayoutManager
     */
    public void addUnresolvedArea(String id, Resolveable res) {
        parentLM.addUnresolvedArea(id, res);
    }

    /**
     * Add the markers when adding an area.
     */
    protected void addMarkers(boolean start, boolean isfirst) {
        // add markers
        if (markers != null) {
            addMarkerMap(markers, start, isfirst);
        }
    }

    /**
     * Delegate adding marker to the parent layout manager.
     *
     * @see org.apache.fop.layoutmgr.LayoutManager
     */
    public void addMarkerMap(Map marks, boolean start, boolean isfirst) {
        parentLM.addMarkerMap(marks, start, isfirst);
    }

    /**
     * Delegate retrieve marker to the parent layout manager.
     *
     * @see org.apache.fop.layoutmgr.LayoutManager
     */
    public Marker retrieveMarker(String name, int pos, int boundary) {
        return parentLM.retrieveMarker(name, pos, boundary);
    }

}

