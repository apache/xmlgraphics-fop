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
import org.apache.fop.area.PageViewport;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.flow.RetrieveMarker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Map;

/**
 * The base class for most LayoutManagers.
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
     * and therefore shouldn't add IDs and markers to the current page.
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

    public LinkedList getNextKnuthElements(LayoutContext context,
                                           int alignment) {
        log.warn("null implementation of getNextKnuthElements() called!");
        setFinished(true);
        return null;
    }

    public KnuthElement addALetterSpaceTo(KnuthElement element) {
        log.warn("null implementation of addALetterSpaceTo() called!");
        return element;
    }

    public void getWordChars(StringBuffer sbChars, Position pos) {
        log.warn("null implementation of getWordChars() called!");
    }

    public void hyphenate(Position pos, HyphContext hc) {
        log.warn("null implementation of hyphenate called!");
    }

    public boolean applyChanges(List oldList) {
        log.warn("null implementation of applyChanges() called!");
        return false;
    }

    public LinkedList getChangedKnuthElements(List oldList,
                                              /*int flaggedPenalty,*/
                                              int alignment) {
        log.warn("null implementation of getChangeKnuthElement() called!");
        return null;
    }

    public int getWordSpaceIPD() {
        log.warn("null implementation of getWordSpaceIPD() called!");
        return 0;
    }

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
    public Area getParentArea(Area childArea) {
        return null;
    }

    public void addChildArea(Area childArea) {
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
                if (foNode instanceof RetrieveMarker) {
                    foNode = getPSLM().resolveRetrieveMarker(
                        (RetrieveMarker) foNode);
                }
                if (foNode != null) {
                    getPSLM().getLayoutManagerMaker().
                        makeLayoutManagers(foNode, newLMs);
                }
            }
        }
        return newLMs;
    }

    /**
     * @see org.apache.fop.layoutmgr.PageSequenceLayoutManager#getPSLM
     */
    public PageSequenceLayoutManager getPSLM() {
        return parentLM.getPSLM();
    }
    
    /**
     * @see org.apache.fop.layoutmgr.PageSequenceLayoutManager#getCurrentPV
     */
    public PageViewport getCurrentPV() {
        return getPSLM().getCurrentPV();
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
