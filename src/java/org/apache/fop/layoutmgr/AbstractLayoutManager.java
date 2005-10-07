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

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Map;

/**
 * The base class for most LayoutManagers.
 */
public abstract class AbstractLayoutManager extends AbstractBaseLayoutManager 
    implements Constants {

    /** Parent LayoutManager for this LayoutManager */
    protected LayoutManager parentLM = null;
    /** List of child LayoutManagers */
    protected List childLMs = null;
    /** Iterator for child LayoutManagers */
    protected ListIterator fobjIter = null;
    /** Marker map for markers related to this LayoutManager */
    protected Map markers = null;

    /** True if this LayoutManager has handled all of its content. */
    private boolean bFinished = false;
    
    /** child LM and child LM iterator during getNextKnuthElement phase */
    protected LayoutManager curChildLM = null;
    /** child LM and child LM iterator during getNextKnuthElement phase */
    protected ListIterator childLMiter = null;
    
    private int lastGeneratedPosition = -1;
    private int smallestPosNumberChecked = Integer.MAX_VALUE;

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
        super(fo);
        if (fo == null) {
            throw new IllegalStateException("Null formatting object found.");
        }
        markers = fo.getMarkers();
        fobjIter = fo.getChildNodes();
        childLMiter = new LMiter(this);
    }

    /** @see LayoutManager#setParent(LayoutManager) */
    public void setParent(LayoutManager lm) {
        this.parentLM = lm;
    }

    /** @see LayoutManager#getParent */
    public LayoutManager getParent() {
        return this.parentLM;
    }

    /** @see LayoutManager#initialize */
    public void initialize() {
        // Empty
    }

    /**
     * Return currently active child LayoutManager or null if
     * all children have finished layout.
     * Note: child must implement LayoutManager! If it doesn't, skip it
     * and print a warning.
     * @return the current child LayoutManager
     */
    protected LayoutManager getChildLM() {
        if (curChildLM != null && !curChildLM.isFinished()) {
            return curChildLM;
        }
        while (childLMiter.hasNext()) {
            curChildLM = (LayoutManager) childLMiter.next();
            curChildLM.initialize();
            return curChildLM;
        }
        return null;
    }

    /**
     * Return indication if getChildLM will return another LM.
     * @return true if another child LM is still available
     */
    protected boolean hasNextChildLM() {
        return childLMiter.hasNext();
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

    /** @see LayoutManager#resetPosition(Position) */
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

    /**
     * Set the flag indicating the LayoutManager has handled all of its content.
     * @param fin the flag value to be set
     */
    public void setFinished(boolean fin) {
        bFinished = fin;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#addAreas(
     *                                              org.apache.fop.layoutmgr.PositionIterator
     *                                              , org.apache.fop.layoutmgr.LayoutContext)
     */
    public void addAreas(PositionIterator posIter, LayoutContext context) {
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getNextKnuthElements(LayoutContext, int)
     */
    public LinkedList getNextKnuthElements(LayoutContext context,
                                           int alignment) {
        log.warn("null implementation of getNextKnuthElements() called!");
        setFinished(true);
        return null;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getChangedKnuthElements(List, int)
     */
    public LinkedList getChangedKnuthElements(List oldList,
                                              int alignment) {
        log.warn("null implementation of getChangeKnuthElement() called!");
        return null;
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
     * @param childArea the child area for which the parent area is wanted
     * @return the parent area for the given child
     */
    public Area getParentArea(Area childArea) {
        return null;
    }

    /**
     * Add a child area to the current area. If this causes the maximum
     * dimension of the current area to be exceeded, the parent LM is called
     * to add it.
     * @param childArea the child area to be added
     */
    public void addChildArea(Area childArea) {
    }

    /**
     * Create the LM instances for the children of the
     * formatting object being handled by this LM.
     * @param size the requested number of child LMs
     * @return the list with the preloaded child LMs
     */
    protected List createChildLMs(int size) {
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
     * @see org.apache.fop.layoutmgr.LayoutManager#createNextChildLMs
     */
    public boolean createNextChildLMs(int pos) {
        List newLMs = createChildLMs(pos + 1 - childLMs.size());
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

    /**
     * Adds a Position to the Position participating in the first|last determination by assigning
     * it a unique position index.
     * @param pos the Position
     * @return the same Position but with a position index
     */
    protected Position notifyPos(Position pos) {
        if (pos.getIndex() >= 0) {
            throw new IllegalStateException("Position already got its index");
        }
        lastGeneratedPosition++;
        pos.setIndex(lastGeneratedPosition);
        return pos;
    }
    
    /**
     * Indicates whether the given Position is the first area-generating Position of this LM.
     * @param pos the Position (must be one with a position index)
     * @return True if it is the first Position
     */
    public boolean isFirst(Position pos) {
        //log.trace("isFirst() smallestPosNumberChecked=" + smallestPosNumberChecked + " " + pos);
        if (pos.getIndex() < 0) {
            throw new IllegalArgumentException("Only Positions with an index can be checked");
        }
        if (pos.getIndex() == this.smallestPosNumberChecked) {
            return true;
        } else if (pos.getIndex() < this.smallestPosNumberChecked) {
            this.smallestPosNumberChecked = pos.getIndex();
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Indicates whether the given Position is the last area-generating Position of this LM.
     * @param pos the Position (must be one with a position index)
     * @return True if it is the last Position
     */
    public boolean isLast(Position pos) {
        //log.trace("isLast() lastGenPos=" + lastGeneratedPosition + " " + pos);
        if (pos.getIndex() < 0) {
            throw new IllegalArgumentException("Only Positions with an index can be checked");
        }
        return (pos.getIndex() == this.lastGeneratedPosition
                && isFinished());
    }

}
