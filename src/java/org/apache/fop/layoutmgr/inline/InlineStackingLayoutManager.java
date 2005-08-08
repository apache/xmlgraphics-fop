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

package org.apache.fop.layoutmgr.inline;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.HashMap;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.layoutmgr.AbstractLayoutManager;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.area.Area;
import org.apache.fop.area.inline.Space;
import org.apache.fop.traits.MinOptMax;

/**
 * Class modelling the commonalities of layoutmanagers for objects
 * which stack children in the inline direction, such as Inline or
 * Line. It should not be instantiated directly.
 */
public class InlineStackingLayoutManager extends AbstractLayoutManager 
                                         implements InlineLevelLayoutManager {


    protected static class StackingIter extends PositionIterator {

        StackingIter(Iterator parentIter) {
            super(parentIter);
        }

        protected LayoutManager getLM(Object nextObj) {
            return ((Position) nextObj).getLM();
        }

        protected Position getPos(Object nextObj) {
            return ((Position) nextObj);
        }
    }


    /**
     * Size of any start or end borders and padding.
     */
    private MinOptMax allocIPD = new MinOptMax(0);

    /**
     * Size of border and padding in BPD (ie, before and after).
     */
    protected MinOptMax extraBPD;

    private Area currentArea; // LineArea or InlineParent

    //private BreakPoss prevBP;
    protected LayoutContext childLC;

    private boolean bAreaCreated = false;

    //private LayoutManager currentLM = null;

    /** Used to store previous content IPD for each child LM. */
    private HashMap hmPrevIPD = new HashMap();

    /**
     * Create an inline stacking layout manager.
     * This is used for fo's that create areas that
     * contain inline areas.
     *
     * @param node the formatting object that creates the area
     */
    protected InlineStackingLayoutManager(FObj node) {
        super(node);
        extraBPD = new MinOptMax(0);
    }

    /**
     * Set the iterator.
     *
     * @param iter the iterator for this LM
     */
    public void setLMiter(ListIterator iter) {
        childLMiter = iter;
    }

    protected MinOptMax getExtraIPD(boolean bNotFirst, boolean bNotLast) {
        return new MinOptMax(0);
    }


    protected boolean hasLeadingFence(boolean bNotFirst) {
        return false;
    }

    protected boolean hasTrailingFence(boolean bNotLast) {
        return false;
    }

    protected SpaceProperty getSpaceStart() {
        return null;
    }
    
    protected SpaceProperty getSpaceEnd() {
        return null;
    }

    /**
     * Reset position for returning next BreakPossibility.
     * @param prevPos a Position returned by this layout manager
     * representing a potential break decision.
     */
    public void resetPosition(Position prevPos) {
        if (prevPos != null) {
            // ASSERT (prevPos.getLM() == this)
            if (prevPos.getLM() != this) {
                //getLogger().error(
                //  "InlineStackingLayoutManager.resetPosition: " +
                //  "LM mismatch!!!");
            }
            // Back up the child LM Position
            Position childPos = prevPos.getPosition();
            reset(childPos);
            /*
            if (prevBP != null
                    && prevBP.getLayoutManager() != childPos.getLM()) {
                childLC = null;
            }
            prevBP = new BreakPoss(childPos);
            */
        } else {
            // Backup to start of first child layout manager
            //prevBP = null;
            // super.resetPosition(prevPos);
            reset(prevPos);
            // If any areas created, we are restarting!
            bAreaCreated = false;
        }
        // Do we need to reset some context like pending or prevContent?
        // What about prevBP?
    }

    protected MinOptMax getPrevIPD(LayoutManager lm) {
        return (MinOptMax) hmPrevIPD.get(lm);
    }

    /**
     * Clear the previous IPD calculation.
     */
    protected void clearPrevIPD() {
        hmPrevIPD.clear();
    }

    /**
     * This method is called by addAreas() so IDs can be added to a page for FOs that 
     * support the 'id' property.
     */
    protected void addId() {
        // Do nothing here, overriden in subclasses that have an 'id' property.
    }
    
    protected Area getCurrentArea() {
        return currentArea;
    }

    protected void setCurrentArea(Area area) {
        currentArea = area;
    }

    protected void setTraits(boolean bNotFirst, boolean bNotLast) {
        
    }

    protected void setChildContext(LayoutContext lc) {
        childLC = lc;
    }

    // Current child layout context
    protected LayoutContext getContext() {
        return childLC;
    }

    protected void addSpace(Area parentArea, MinOptMax spaceRange,
                            double dSpaceAdjust) {
        if (spaceRange != null) {
            int iAdjust = spaceRange.opt;
            if (dSpaceAdjust > 0.0) {
                // Stretch by factor
                iAdjust += (int) ((double) (spaceRange.max
                                          - spaceRange.opt) * dSpaceAdjust);
            } else if (dSpaceAdjust < 0.0) {
                // Shrink by factor
                iAdjust += (int) ((double) (spaceRange.opt
                                          - spaceRange.min) * dSpaceAdjust);
            }
            if (iAdjust != 0) {
                //getLogger().debug("Add leading space: " + iAdjust);
                Space ls = new Space();
                ls.setIPD(iAdjust);
                parentArea.addChildArea(ls);
            }
        }
    }

    public List addALetterSpaceTo(List oldList) {
        // old list contains only a box, or the sequence: box penalty glue box

        ListIterator oldListIterator = oldList.listIterator();
        KnuthElement element = null;
        // "unwrap" the Position stored in each element of oldList
        while (oldListIterator.hasNext()) {
            element = (KnuthElement) oldListIterator.next();
            element.setPosition(((NonLeafPosition)element.getPosition()).getPosition());
        }

        oldList = ((InlineLevelLayoutManager)
                   element.getLayoutManager()).addALetterSpaceTo(oldList);

        // "wrap" again the Position stored in each element of oldList
        oldListIterator = oldList.listIterator();
        while (oldListIterator.hasNext()) {
            element = (KnuthElement) oldListIterator.next();
            element.setPosition(new NonLeafPosition(this, element.getPosition()));
        }

        return oldList;
    }

    public void getWordChars(StringBuffer sbChars, Position pos) {
        Position newPos = ((NonLeafPosition) pos).getPosition();
        ((InlineLevelLayoutManager)
         newPos.getLM()).getWordChars(sbChars, newPos);
    }

    public void hyphenate(Position pos, HyphContext hc) {
        Position newPos = ((NonLeafPosition) pos).getPosition();
        ((InlineLevelLayoutManager)
         newPos.getLM()).hyphenate(newPos, hc);
    }

    public boolean applyChanges(List oldList) {
        // "unwrap" the Positions stored in the elements
        ListIterator oldListIterator = oldList.listIterator();
        KnuthElement oldElement;
        while (oldListIterator.hasNext()) {
            oldElement = (KnuthElement) oldListIterator.next();
            oldElement.setPosition
                (((NonLeafPosition) oldElement.getPosition()).getPosition());
        }
        // reset the iterator
        oldListIterator = oldList.listIterator();

        InlineLevelLayoutManager prevLM = null;
        InlineLevelLayoutManager currLM;
        int fromIndex = 0;

        boolean bSomethingChanged = false;
        while(oldListIterator.hasNext()) {
            oldElement = (KnuthElement) oldListIterator.next();
            currLM = (InlineLevelLayoutManager) oldElement.getLayoutManager();
            // initialize prevLM
            if (prevLM == null) {
                prevLM = currLM;
            }

            if (currLM != prevLM || !oldListIterator.hasNext()) {
                if (oldListIterator.hasNext()) {
                    bSomethingChanged
                        = prevLM.applyChanges(oldList.subList(fromIndex, oldListIterator.previousIndex()))
                        || bSomethingChanged;
                    prevLM = currLM;
                    fromIndex = oldListIterator.previousIndex();
                } else if (currLM == prevLM) {
                    bSomethingChanged
                        = prevLM.applyChanges(oldList.subList(fromIndex, oldList.size()))
                        || bSomethingChanged;
                } else {
                    bSomethingChanged
                        = prevLM.applyChanges(oldList.subList(fromIndex, oldListIterator.previousIndex()))
                        || bSomethingChanged;
                    bSomethingChanged
                        = currLM.applyChanges(oldList.subList(oldListIterator.previousIndex(), oldList.size()))
                        || bSomethingChanged;
                }
            }
        }

        // "wrap" again the Positions stored in the elements
        oldListIterator = oldList.listIterator();
        while (oldListIterator.hasNext()) {
            oldElement = (KnuthElement) oldListIterator.next();
            oldElement.setPosition
                (new NonLeafPosition(this, oldElement.getPosition()));
        }
        return bSomethingChanged;
    }

    public LinkedList getChangedKnuthElements(List oldList, /*int flaggedPenalty,*/ int alignment) {
        // "unwrap" the Positions stored in the elements
        ListIterator oldListIterator = oldList.listIterator();
        KnuthElement oldElement;
        while (oldListIterator.hasNext()) {
            oldElement = (KnuthElement) oldListIterator.next();
            oldElement.setPosition
                (((NonLeafPosition) oldElement.getPosition()).getPosition());
        }
        // reset the iterator
        oldListIterator = oldList.listIterator();

        KnuthElement returnedElement;
        LinkedList returnedList = new LinkedList();
        LinkedList returnList = new LinkedList();
        InlineLevelLayoutManager prevLM = null;
        InlineLevelLayoutManager currLM;
        int fromIndex = 0;

        while(oldListIterator.hasNext()) {
            oldElement = (KnuthElement) oldListIterator.next();
            currLM = (InlineLevelLayoutManager) oldElement.getLayoutManager();
            if (prevLM == null) {
                prevLM = currLM;
            }

            if (currLM != prevLM || !oldListIterator.hasNext()) {
                if (oldListIterator.hasNext()) {
                    returnedList.addAll
                        (prevLM.getChangedKnuthElements
                         (oldList.subList(fromIndex,
                                          oldListIterator.previousIndex()),
                          /*flaggedPenalty,*/ alignment));
                    prevLM = currLM;
                    fromIndex = oldListIterator.previousIndex();
                } else if (currLM == prevLM) {
                    returnedList.addAll
                        (prevLM.getChangedKnuthElements
                         (oldList.subList(fromIndex, oldList.size()),
                          /*flaggedPenalty,*/ alignment));
                } else {
                    returnedList.addAll
                        (prevLM.getChangedKnuthElements
                         (oldList.subList(fromIndex,
                                          oldListIterator.previousIndex()),
                          /*flaggedPenalty,*/ alignment));
                    returnedList.addAll
                        (currLM.getChangedKnuthElements
                         (oldList.subList(oldListIterator.previousIndex(),
                                          oldList.size()),
                          /*flaggedPenalty,*/ alignment));
                }
            }
        }

        // "wrap" the Position stored in each element of returnedList
        ListIterator listIter = returnedList.listIterator();
        while (listIter.hasNext()) {
            returnedElement = (KnuthElement) listIter.next();
            returnedElement.setPosition
                (new NonLeafPosition(this, returnedElement.getPosition()));
            returnList.add(returnedElement);
        }
        return returnList;
    }
}
