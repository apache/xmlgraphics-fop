/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
public abstract class InlineStackingLayoutManager extends AbstractLayoutManager
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

    /** The child layout context */
    protected LayoutContext childLC;

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

    /**
     * Returns the extra IPD needed for any leading or trailing fences for the
     * current area.
     * @param bNotFirst true if not the first area for this layout manager
     * @param bNotLast true if not the last area for this layout manager
     * @return the extra IPD as a MinOptMax spec
     */
    protected MinOptMax getExtraIPD(boolean bNotFirst, boolean bNotLast) {
        return new MinOptMax(0);
    }


    /**
     * Indication if the current area has a leading fence.
     * @param bNotFirst true if not the first area for this layout manager
     * @return the leading fence flag
     */
    protected boolean hasLeadingFence(boolean bNotFirst) {
        return false;
    }

    /**
     * Indication if the current area has a trailing fence.
     * @param bNotLast true if not the last area for this layout manager
     * @return the trailing fence flag
     */
    protected boolean hasTrailingFence(boolean bNotLast) {
        return false;
    }

    /**
     * Get the space at the start of the inline area.
     * @return the space property describing the space
     */
    protected SpaceProperty getSpaceStart() {
        return null;
    }

    /**
     * Get the space at the end of the inline area.
     * @return the space property describing the space
     */
    protected SpaceProperty getSpaceEnd() {
        return null;
    }

    /**
     * TODO: Explain this method
     * @param lm ???
     * @return ???
     */
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
     * Returns the current area.
     * @return the current area
     */
    protected Area getCurrentArea() {
        return currentArea;
    }

    /**
     * Set the current area.
     * @param area the current area
     */
    protected void setCurrentArea(Area area) {
        currentArea = area;
    }

    /**
     * Trait setter to be overridden by subclasses.
     * @param bNotFirst true if this is not the first child area added
     * @param bNotLast true if this is not the last child area added
     */
    protected void setTraits(boolean bNotFirst, boolean bNotLast) {
    }

    /**
     * Set the current child layout context
     * @param lc the child layout context
     */
    protected void setChildContext(LayoutContext lc) {
        childLC = lc;
    }

    /**
     * Current child layout context
     * @return the current child layout context
     */
    protected LayoutContext getContext() {
        return childLC;
    }

    /**
     * Adds a space to the area
     * @param parentArea the area to which to add the space
     * @param spaceRange the space range specifier
     * @param dSpaceAdjust the factor by which to stretch or shrink the space
     */
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

    /** {@inheritDoc} */
    public List addALetterSpaceTo(List oldList) {
        // old list contains only a box, or the sequence: box penalty glue box

        ListIterator oldListIterator = oldList.listIterator();
        KnuthElement element = null;
        // "unwrap" the Position stored in each element of oldList
        while (oldListIterator.hasNext()) {
            element = (KnuthElement) oldListIterator.next();
            element.setPosition(element.getPosition().getPosition());
        }

        // The last element may not have a layout manager (its position == null);
        // this may happen if it is a padding box; see bug 39571.
        InlineLevelLayoutManager LM =
            (InlineLevelLayoutManager) element.getLayoutManager();
        if (LM != null) {
            oldList = LM.addALetterSpaceTo(oldList);
        }
        // "wrap" again the Position stored in each element of oldList
        oldListIterator = oldList.listIterator();
        while (oldListIterator.hasNext()) {
            element = (KnuthElement) oldListIterator.next();
            element.setPosition(notifyPos(new NonLeafPosition(this, element.getPosition())));
        }

        return oldList;
    }

    /**
     * remove the AreaInfo object represented by the given elements,
     * so that it won't generate any element when getChangedKnuthElements
     * will be called
     *
     * @param oldList the elements representing the word space
     */
    public void removeWordSpace(List oldList) {
        ListIterator oldListIterator = oldList.listIterator();
        KnuthElement element = null;
        // "unwrap" the Position stored in each element of oldList
        while (oldListIterator.hasNext()) {
            element = (KnuthElement) oldListIterator.next();
            element.setPosition(element.getPosition().getPosition());
        }

        ((InlineLevelLayoutManager)
                   element.getLayoutManager()).removeWordSpace(oldList);

    }

    /** {@inheritDoc} */
    public void getWordChars(StringBuffer sbChars, Position pos) {
        Position newPos = pos.getPosition();
        ((InlineLevelLayoutManager)
         newPos.getLM()).getWordChars(sbChars, newPos);
    }

    /** {@inheritDoc} */
    public void hyphenate(Position pos, HyphContext hc) {
        Position newPos = pos.getPosition();
        ((InlineLevelLayoutManager)
         newPos.getLM()).hyphenate(newPos, hc);
    }

    /** {@inheritDoc} */
    public boolean applyChanges(List oldList) {
        // "unwrap" the Positions stored in the elements
        ListIterator oldListIterator = oldList.listIterator();
        KnuthElement oldElement;
        while (oldListIterator.hasNext()) {
            oldElement = (KnuthElement) oldListIterator.next();
            oldElement.setPosition
                (oldElement.getPosition().getPosition());
        }
        // reset the iterator
        oldListIterator = oldList.listIterator();

        InlineLevelLayoutManager prevLM = null;
        InlineLevelLayoutManager currLM;
        int fromIndex = 0;

        boolean bSomethingChanged = false;
        while (oldListIterator.hasNext()) {
            oldElement = (KnuthElement) oldListIterator.next();
            currLM = (InlineLevelLayoutManager) oldElement.getLayoutManager();
            // initialize prevLM
            if (prevLM == null) {
                prevLM = currLM;
            }

            if (currLM != prevLM || !oldListIterator.hasNext()) {
                if (prevLM == this || currLM == this) {
                    prevLM = currLM;
                } else if (oldListIterator.hasNext()) {
                    bSomethingChanged
                        = prevLM.applyChanges(oldList.subList(fromIndex
                                                              , oldListIterator.previousIndex()))
                        || bSomethingChanged;
                    prevLM = currLM;
                    fromIndex = oldListIterator.previousIndex();
                } else if (currLM == prevLM) {
                    bSomethingChanged
                        = prevLM.applyChanges(oldList.subList(fromIndex, oldList.size()))
                            || bSomethingChanged;
                } else {
                    bSomethingChanged
                        = prevLM.applyChanges(oldList.subList(fromIndex
                                                              , oldListIterator.previousIndex()))
                            || bSomethingChanged;
                    if (currLM != null) {
                        bSomethingChanged
                            = currLM.applyChanges(oldList.subList(oldListIterator.previousIndex()
                                                                  , oldList.size()))
                            || bSomethingChanged;
                    }
                }
            }
        }

        // "wrap" again the Positions stored in the elements
        oldListIterator = oldList.listIterator();
        while (oldListIterator.hasNext()) {
            oldElement = (KnuthElement) oldListIterator.next();
            oldElement.setPosition
                (notifyPos(new NonLeafPosition(this, oldElement.getPosition())));
        }
        return bSomethingChanged;
    }

    /**
     * {@inheritDoc}
     */
    public List getChangedKnuthElements(List oldList, int alignment) {
        // "unwrap" the Positions stored in the elements
        ListIterator oldListIterator = oldList.listIterator();
        KnuthElement oldElement;
        while (oldListIterator.hasNext()) {
            oldElement = (KnuthElement) oldListIterator.next();
            oldElement.setPosition
                (oldElement.getPosition().getPosition());
        }
        // reset the iterator
        oldListIterator = oldList.listIterator();

        KnuthElement returnedElement;
        LinkedList returnedList = new LinkedList();
        LinkedList returnList = new LinkedList();
        InlineLevelLayoutManager prevLM = null;
        InlineLevelLayoutManager currLM;
        int fromIndex = 0;

        while (oldListIterator.hasNext()) {
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
                    if (currLM != null) {
                        returnedList.addAll
                            (currLM.getChangedKnuthElements
                             (oldList.subList(oldListIterator.previousIndex(),
                                              oldList.size()),
                              /*flaggedPenalty,*/ alignment));
                    }
                }
            }
        }

        // "wrap" the Position stored in each element of returnedList
        ListIterator listIter = returnedList.listIterator();
        while (listIter.hasNext()) {
            returnedElement = (KnuthElement) listIter.next();
            returnedElement.setPosition
                (notifyPos(new NonLeafPosition(this, returnedElement.getPosition())));
            returnList.add(returnedElement);
        }
        return returnList;
    }
}
