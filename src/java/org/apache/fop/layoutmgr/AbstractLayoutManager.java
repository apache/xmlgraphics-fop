/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
import org.apache.fop.fo.FONode;
import org.apache.fop.area.Area;
import org.apache.fop.area.Resolvable;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.flow.RetrieveMarker;
import org.apache.fop.fo.flow.Marker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Map;

/**
 * The base class for all LayoutManagers.
 */
public abstract class AbstractLayoutManager implements LayoutManager, Constants {
    protected LayoutManager parentLM = null;
    protected List childLMs = null;
    protected ListIterator fobjIter = null;
    protected Map markers = null;

    /** True if this LayoutManager has handled all of its content. */
    private boolean bFinished = false;
    protected boolean bInited = false;
    
    /**
     * Used during addAreas(): signals that a BreakPoss is not generating areas
     * and therefore doesn't add IDs and markers to the current page.
     * @see org.apache.fop.layoutmgr.AbstractLayoutManager#isBogus
     */
    protected boolean bBogus = false;

    /** child LM and child LM iterator during getNextBreakPoss phase */
    protected LayoutManager curChildLM = null;
    protected ListIterator childLMiter = null;

    /**
     * logging instance
     */
    protected static Log log = LogFactory.getLog(LayoutManager.class);

    /**
     * Abstract layout manager.
     */
    public AbstractLayoutManager() {
    }

    /**
     * Abstract layout manager.
     *
     * @param fo the formatting object for this layout manager
     */
    public AbstractLayoutManager(FObj fo) {
        if (fo == null) {
            throw new IllegalStateException("Null formatting object found.");
        }
        setFObj(fo);
    }

    /**
     * Set the FO object for this layout manager
     *
     * @param fo the formatting object for this layout manager
     */
    public void setFObj(FObj fo) {
        markers = fo.getMarkers();
        fobjIter = fo.getChildNodes();
        childLMiter = new LMiter(this);
    }

    /**
     * This method provides a hook for a LayoutManager to initialize traits
     * for the areas it will create, based on Properties set on its FO.
     */
    public void initialize() {
        if (bInited == false) {
            initProperties();
            bInited = true;
        }
    }

    /**
     * This method is called by initialize() to set any method variables
     * based on Properties set on its FO.
     */
    protected void initProperties() {
    }

    public void setParent(LayoutManager lm) {
        this.parentLM = lm;
    }

    public LayoutManager getParent() {
        return this.parentLM;
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
    //     parentLM.addChildArea(area, bFinished); // ????
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


    /** @see org.apache.fop.layoutmgr.LayoutManager#generatesInlineAreas() */
    public boolean generatesInlineAreas() {
        return false;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager#isBogus() */
    public boolean isBogus() {
        if (getParent().isBogus()) {
            return true;
        } else {
            return bBogus;
        }
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
    protected LayoutManager getChildLM() {
        if (curChildLM != null && !curChildLM.isFinished()) {
            return curChildLM;
        }
        while (childLMiter.hasNext()) {
            curChildLM = (LayoutManager) childLMiter.next();
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
    protected void reset(org.apache.fop.layoutmgr.Position pos) {
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
                curChildLM = (LayoutManager) childLMiter.previous();
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


    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#addAreas(org.apache.fop.layoutmgr.PositionIterator, org.apache.fop.layoutmgr.LayoutContext)
     */
    public void addAreas(PositionIterator posIter, LayoutContext context) {
    }


    public void getWordChars(StringBuffer sbChars, Position bp1,
                             Position bp2) {
    }

    /* ---------------------------------------------------------
     * PROVIDE NULL IMPLEMENTATIONS OF METHODS from LayoutManager
     * interface which are declared abstract in AbstractLayoutManager.
     * ---------------------------------------------------------*/

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getParentArea(org.apache.fop.area.Area)
     */
    public Area getParentArea(Area childArea) {
        return null;
    }

    protected void flush() {
    }

    public void addChildArea(Area childArea) {
    }

    /**
     * Delegate getting the current page number to the parent layout manager.
     *
     * @see org.apache.fop.layoutmgr.LayoutManager
     */
    public String getCurrentPageNumberString() {
        return parentLM.getCurrentPageNumberString();
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
    protected void addID(String foID) {
        if (foID != null && foID.length() > 0) {
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
    public void addUnresolvedArea(String id, Resolvable res) {
        parentLM.addUnresolvedArea(id, res);
    }

    /**
     * Add the markers when adding an area.
     */
    protected void addMarkers(boolean starting, boolean isfirst, boolean islast) {
        // add markers
        if (markers != null) {
            addMarkerMap(markers, starting, isfirst, islast);
        }
    }

    /**
     * Delegate adding marker to the parent layout manager.
     *
     * @see org.apache.fop.layoutmgr.LayoutManager
     */
    public void addMarkerMap(Map marks, boolean starting, boolean isfirst, boolean islast) {
        parentLM.addMarkerMap(marks, starting, isfirst, islast);
    }

    /**
     * Delegate retrieve marker to the parent layout manager.
     *
     * @see org.apache.fop.layoutmgr.LayoutManager
     */
    public Marker retrieveMarker(String name, int pos, int boundary) {
        return parentLM.retrieveMarker(name, pos, boundary);
    }

    /**
     * Delegate getAreaTreeHandler to the parent layout manager.
     *
     * @see org.apache.fop.layoutmgr.LayoutManager
     * @return the AreaTreeHandler object.
     */
    public AreaTreeHandler getAreaTreeHandler() {
        return parentLM.getAreaTreeHandler();
    }

    /**
     * Handles retrieve-marker nodes as they occur.
     * @param foNode FO node to check
     * @return the original foNode or in case of a retrieve-marker the replaced
     *     FO node. null if the the replacement results in no nodes to be 
     *     processed.
     */
    private FONode handleRetrieveMarker(FONode foNode) {
        if (foNode instanceof RetrieveMarker) {
            RetrieveMarker rm = (RetrieveMarker) foNode;
            Marker marker = retrieveMarker(rm.getRetrieveClassName(),
                                           rm.getRetrievePosition(),
                                           rm.getRetrieveBoundary());
            if (marker == null) {
                return null;
            }
            rm.bindMarker(marker);
            return rm;
        } else {
            return foNode;
        }
    }
    
    /**
     * Convenience method: preload a number of child LMs
     * @param size the requested number of child LMs
     * @return the list with the preloaded child LMs
     */
    protected List preLoadList(int size) {
        if (fobjIter == null) {
            return null;
        }
        List newLMs = new ArrayList(size);
        while (fobjIter.hasNext() && newLMs.size() < size ) {
            Object theobj = fobjIter.next();
            if (theobj instanceof FONode) {
                FONode foNode = (FONode) theobj;
                foNode = handleRetrieveMarker(foNode);
                if (foNode != null) {
                    getAreaTreeHandler().getLayoutManagerMaker().
                        makeLayoutManagers(foNode, newLMs);
                }
            }
        }
        return newLMs;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#preLoadNext
     */
    public boolean preLoadNext(int pos) {
        List newLMs = preLoadList(pos + 1 - childLMs.size());
        addChildLMs(newLMs);
        return pos < childLMs.size();
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getChildLMs
     */
    public List getChildLMs() {
        if (childLMs == null) {
            childLMs = new java.util.ArrayList(10);
        }
        return childLMs;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#addChildLM
     */
    public void addChildLM(LayoutManager lm) {
        if (lm == null) {
            return;
        }
        lm.setParent(this);
        lm.initialize();
        if (childLMs == null) {
            childLMs = new java.util.ArrayList(10);
        }
        childLMs.add(lm);
        log.trace(this.getClass().getName()
                  + ": Adding child LM " + lm.getClass().getName());
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#addChildLMs
     */
    public void addChildLMs(List newLMs) {
        if (newLMs == null || newLMs.size() == 0) {
            return;
        }
        ListIterator iter = newLMs.listIterator();
        while (iter.hasNext()) {
            LayoutManager lm = (LayoutManager) iter.next();
            addChildLM(lm);
        }
    }

}

