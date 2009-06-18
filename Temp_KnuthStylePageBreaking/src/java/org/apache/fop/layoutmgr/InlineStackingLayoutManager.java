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

import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.HashMap;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.traits.InlineProps;
import org.apache.fop.traits.SpaceVal;
import org.apache.fop.area.Area;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.Space;
import org.apache.fop.traits.MinOptMax;

/**
 * Class modelling the commonalities of layoutmanagers for objects
 * which stack children in the inline direction, such as Inline or
 * Line. It should not be instantiated directly.
 */
public class InlineStackingLayoutManager extends AbstractLayoutManager 
                                         implements InlineLevelLayoutManager {


    private static class StackingIter extends PositionIterator {

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

    private BreakPoss prevBP;
    protected LayoutContext childLC;

    private LayoutManager lastChildLM = null; // Set when return last breakposs
    private boolean bAreaCreated = false;

    private LayoutManager currentLM = null;

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
     * Check if this generates inline areas.
     * This creates inline areas that contain other inline areas.
     *
     * @return true
     */
    public boolean generatesInlineAreas() {
        return true;
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
            if (prevBP != null
                    && prevBP.getLayoutManager() != childPos.getLM()) {
                childLC = null;
            }
            prevBP = new BreakPoss(childPos);
        } else {
            // Backup to start of first child layout manager
            prevBP = null;
            // super.resetPosition(prevPos);
            reset(prevPos);
            // If any areas created, we are restarting!
            bAreaCreated = false;
        }
        // Do we need to reset some context like pending or prevContent?
        // What about prevBP?
    }

    /**
     * Return value indicating whether the next area to be generated could
     * start a new line. This should only be called in the "START" condition
     * if a previous inline BP couldn't end the line.
     * Return true if any space-start, border-start or padding-start, else
     * propagate to first child LM
     */
    public boolean canBreakBefore(LayoutContext context) {
        LayoutManager lm = getChildLM();
        if (lm != null) {
            return lm.canBreakBefore(context);
        } else {
            return false; // ??? NO child LM?
        }
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
     * Get the next break position for this layout manager.
     * The next break position will be an position within the
     * areas return by the child inline layout managers.
     *
     * @param lc the layout context for finding breaks
     * @return the next break position
     */
    public BreakPoss getNextBreakPoss(LayoutContext lc) {
        // Get a break from currently active child LM
        BreakPoss bp = null;
        LayoutManager curLM;
        SpaceSpecifier leadingSpace = lc.getLeadingSpace();

        if (lc.startsNewArea()) {
            // First call to this LM in new parent "area", but this may
            // not be the first area created by this inline
            childLC = new LayoutContext(lc);
            if (getSpaceStart() != null) {
                lc.getLeadingSpace().addSpace(new SpaceVal(getSpaceStart()));
            }

            // Check for "fence"
            if (hasLeadingFence(!lc.isFirstArea())) {
                // Reset leading space sequence for child areas
                leadingSpace = new SpaceSpecifier(false);
            }
            // Reset state variables
            clearPrevIPD(); // Clear stored prev content dimensions
        }

        // We only do this loop more than once if a childLM returns
        // a null BreakPoss, meaning it has nothing (more) to layout.
        while ((curLM = getChildLM()) != null) {

            // ignore nested blocks for now
            if (!curLM.generatesInlineAreas()) {
                log.warn("ignoring block inside inline fo");
                curLM.setFinished(true);
                continue;
            }
            /* If first break for this child LM, set START_AREA flag
             * and initialize pending space from previous LM sibling's
             * trailing space specifiers.
             */
            boolean bFirstChildBP = (prevBP == null
                                     || prevBP.getLayoutManager() != curLM);

            initChildLC(childLC, prevBP, lc.startsNewArea(),
                        bFirstChildBP, leadingSpace);
            if (lc.tryHyphenate()) {
                childLC.setHyphContext(lc.getHyphContext());
            }

            if (((bp = curLM.getNextBreakPoss(childLC)) != null)
                    || (lc.tryHyphenate()
                    && !lc.getHyphContext().hasMoreHyphPoints())) {
                break;
            }
            // If LM has no content, should it generate any area? If not,
            // should trailing space from a previous area interact with
            // leading space from a following area?
        }
        if (bp == null) {
            setFinished(true);
            return null; // There was no childLM with anything to layout
            // Alternative is to return a BP with the isLast flag set
        } else {
            boolean bIsLast = false;
            if (getChildLM() == null) {
                bIsLast = true;
                setFinished(true);
            } else if (bp.couldEndLine()) {
                /* Child LM ends with suppressible spaces. See if it could
                 * end this LM's area too. Child LM finish flag is NOT set!
                 */
                bIsLast = !hasMoreLM(bp.getLayoutManager());
            }
            return makeBreakPoss(bp, lc, bIsLast);
        }
    }

    /** ATTENTION: ALSO USED BY LineLayoutManager! */
    protected void initChildLC(LayoutContext childLC, BreakPoss prevBP,
                               boolean bStartParent, boolean bFirstChildBP,
                               SpaceSpecifier leadingSpace) {

        childLC.setFlags(LayoutContext.NEW_AREA,
                         (bFirstChildBP || bStartParent));
        if (bStartParent) {
            // Start of a new line area or inline parent area
            childLC.setFlags(LayoutContext.FIRST_AREA, bFirstChildBP);
            childLC.setLeadingSpace(leadingSpace);
        } else if (bFirstChildBP) {
            // Space-after sequence from previous "area"
            childLC.setFlags(LayoutContext.FIRST_AREA, true);
            childLC.setLeadingSpace(prevBP.getTrailingSpace());
        } else {
            childLC.setLeadingSpace(null);
        }
    }


    private BreakPoss makeBreakPoss(BreakPoss bp, LayoutContext lc,
                                    boolean bIsLast) {
        NonLeafPosition inlbp = new NonLeafPosition(this, bp.getPosition());
        BreakPoss myBP = new BreakPoss(inlbp, bp.getFlags());

        myBP.setFlag(BreakPoss.ISFIRST, lc.isFirstArea());
        myBP.setFlag(BreakPoss.ISLAST, bIsLast);

        if (bIsLast) {
            lastChildLM = bp.getLayoutManager();
        }

        // Update dimension information for our allocation area,
        // including child areas
        // generated by previous childLM which have completed layout
        // Update pending area measure
        // This includes all previous breakinfo

        MinOptMax bpDim = (MinOptMax) bp.getStackingSize().clone();
        MinOptMax prevIPD = updatePrevIPD(bp, prevBP, lc.startsNewArea(),
                                          lc.isFirstArea());

        if (lc.startsNewArea()) {
            myBP.setLeadingSpace(lc.getLeadingSpace());
        }


        // Add size of previous child areas which are finished
        bpDim.add(prevIPD);

        SpaceSpecifier trailingSpace = bp.getTrailingSpace();
        if (hasTrailingFence(!bIsLast)) {
            bpDim.add(bp.resolveTrailingSpace(false));
            trailingSpace = new SpaceSpecifier(false);
        } else {
            // Need this to avoid modifying pending space specifiers
            // on previous BP from child as we use these on the next
            // call in this LM
            trailingSpace = (SpaceSpecifier) trailingSpace.clone();
        }
        if (getSpaceEnd() != null) {
            trailingSpace.addSpace(new SpaceVal(getSpaceEnd()));
        }
        myBP.setTrailingSpace(trailingSpace);

        // Add start and end borders and padding
        bpDim.add(getExtraIPD(!lc.isFirstArea(), !bIsLast));
        myBP.setStackingSize(bpDim);
        myBP.setNonStackingSize(
          MinOptMax.add(bp.getNonStackingSize(), extraBPD));

        prevBP = bp;
        //         if (bIsLast) {
        //     setFinished(true);  // Our last area, so indicate done
        //         }
        return myBP;
    }


    /** ATTENTION: ALSO USED BY LineLayoutManager! */
    protected MinOptMax updatePrevIPD(BreakPoss bp, BreakPoss prevBP,
                                      boolean bStartParent, boolean bFirstArea) {
        MinOptMax prevIPD = new MinOptMax(0);

        if (bStartParent) {
            if (hasLeadingFence(!bFirstArea)) {
                // Space-start before first child area placed
                prevIPD.add(bp.resolveLeadingSpace());
            }
            hmPrevIPD.put(bp.getLayoutManager(), prevIPD);
        } else {
            // In case of reset to a previous position, it may already
            // be calculated
            prevIPD = (MinOptMax) hmPrevIPD.get(bp.getLayoutManager());
            if (prevIPD == null) {
                // ASSERT(prevBP.getLayoutManager() != bp.getLayoutManager());
                /* This is first bp generated by child (in this parent area).
                 * Calculate space-start on this area in combination with any
                 * pending space-end with previous break.
                 * Corresponds to Space between two child areas.
                 */
                prevIPD = (MinOptMax) hmPrevIPD.get(
                            prevBP.getLayoutManager());
                prevIPD = MinOptMax.add(prevIPD, bp.resolveLeadingSpace());
                prevIPD.add(prevBP.getStackingSize());
                hmPrevIPD.put(bp.getLayoutManager(), prevIPD);
            }
        }
        return prevIPD;
    }

    public void getWordChars(StringBuffer sbChars, Position bp1,
                             Position bp2) {
        Position endPos = ((NonLeafPosition) bp2).getPosition();
        Position prevPos = null;
        if (bp1 != null) {
            prevPos = ((NonLeafPosition) bp1).getPosition();
            if (prevPos.getLM() != endPos.getLM()) {
                prevPos = null;
            }
        }
        endPos.getLM().getWordChars(sbChars, prevPos, endPos);
    }

    /******
      protected BreakableText getText(BreakPoss prevBP, BreakPoss lastBP) {
      }
     *****/

    protected InlineParent createArea() {
        return new InlineParent();
    }

    /**
     * Generate and add areas to parent area.
     * Set size of each area. This should only create and return one
     * inline area for any inline parent area.
     *
     * @param parentIter Iterator over Position information returned
     * by this LayoutManager.
     * @param dSpaceAdjust Factor controlling how much extra space to add
     * in order to justify the line.
     */
    public void addAreas(PositionIterator parentIter,
                         LayoutContext context) {
        InlineParent parent = createArea();
        parent.setBPD(context.getLineHeight());
        parent.setOffset(0);
        setCurrentArea(parent);

        setChildContext(new LayoutContext(context)); // Store current value

        // If has fence, make a new leadingSS
        /* How to know if first area created by this LM? Keep a count and
         * reset it if getNextBreakPoss() is called again.
         */
        if (hasLeadingFence(bAreaCreated)) {
            getContext().setLeadingSpace(new SpaceSpecifier(false));
            getContext().setFlags(LayoutContext.RESOLVE_LEADING_SPACE,
                                  true);
        } else {
            getContext().setFlags(LayoutContext.RESOLVE_LEADING_SPACE,
                                  false);
        }

        if (getSpaceStart() != null) {
            context.getLeadingSpace().addSpace(new SpaceVal(getSpaceStart()));
        }

        // "unwrap" the NonLeafPositions stored in parentIter
        // and put them in a new list; 
        // also set lastLM to be the LayoutManager which created
        // the last Position: if the LAST_AREA flag is set in context,
        // it must be also set in the LayoutContext given to lastLM,
        // but unset in the LayoutContext given to the other LMs
        LinkedList positionList = new LinkedList();
        NonLeafPosition pos;
        LayoutManager lastLM = null; // last child LM in this iterator
        while (parentIter.hasNext()) {
            pos = (NonLeafPosition) parentIter.next();
            lastLM = pos.getPosition().getLM();
            positionList.add(pos.getPosition());
        }

        StackingIter childPosIter
            = new StackingIter(positionList.listIterator());

        LayoutManager prevLM = null;
        InlineLevelLayoutManager childLM ;
        while ((childLM = (InlineLevelLayoutManager) childPosIter.getNextChildLM())
               != null) {
            getContext().setFlags(LayoutContext.LAST_AREA,
                                  context.isLastArea() && childLM == lastLM);
            childLM.addAreas(childPosIter, getContext());
            getContext().setLeadingSpace(getContext().getTrailingSpace());
            getContext().setFlags(LayoutContext.RESOLVE_LEADING_SPACE, true);
            prevLM = childLM;
        }

        /* If has trailing fence,
         * resolve trailing space specs from descendants.
         * Otherwise, propagate any trailing space specs to parent LM via
         * the context object.
         * If the last child LM called return ISLAST in the context object
         * and it is the last child LM for this LM, then this must be
         * the last area for the current LM also.
         */
        boolean bIsLast =
          (getContext().isLastArea() && prevLM == lastChildLM);
        if (hasTrailingFence(bIsLast)) {
            addSpace(getCurrentArea(),
                     getContext().getTrailingSpace().resolve(false),
                     getContext().getSpaceAdjust());
            context.setTrailingSpace(new SpaceSpecifier(false));
        } else {
            // Propagate trailing space-spec sequence to parent LM in context
            context.setTrailingSpace(getContext().getTrailingSpace());
        }
        // Add own trailing space to parent context (or set on area?)
        if (context.getTrailingSpace() != null  && getSpaceEnd() != null) {
            context.getTrailingSpace().addSpace(new SpaceVal(getSpaceEnd()));
        }
        setTraits(bAreaCreated, !bIsLast);
        
        parentLM.addChildArea(getCurrentArea());
        context.setFlags(LayoutContext.LAST_AREA, bIsLast);
        bAreaCreated = true;
    }

    protected Area getCurrentArea() {
        return currentArea;
    }

    protected void setCurrentArea(Area area) {
        currentArea = area;
    }

    protected void setTraits(boolean bNotFirst, boolean bNotLast) {
        
    }

    public void addChildArea(Area childArea) {
        // Make sure childArea is inline area
        if (childArea instanceof InlineArea) {
            Area parent = getCurrentArea();
            if (getContext().resolveLeadingSpace()) {
                addSpace(parent,
                         getContext().getLeadingSpace().resolve(false),
                         getContext().getSpaceAdjust());
            }
            parent.addChildArea(childArea);
        }
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

    public LinkedList getNextKnuthElements(LayoutContext lc, int alignment) {
        InlineLevelLayoutManager curLM;

        // the list returned by child LM
        LinkedList returnedList;
        KnuthElement returnedElement;

        // the list which will be returned to the parent LM
        LinkedList returnList = new LinkedList();

        SpaceSpecifier leadingSpace = lc.getLeadingSpace();

        if (lc.startsNewArea()) {
            // First call to this LM in new parent "area", but this may
            // not be the first area created by this inline
            childLC = new LayoutContext(lc);
            lc.getLeadingSpace().addSpace(new SpaceVal(getSpaceStart()));

            // Check for "fence"
            if (hasLeadingFence(!lc.isFirstArea())) {
                // Reset leading space sequence for child areas
                leadingSpace = new SpaceSpecifier(false);
            }
            // Reset state variables
            clearPrevIPD(); // Clear stored prev content dimensions
        }

        while ((curLM = (InlineLevelLayoutManager) getChildLM()) != null) {
            // get KnuthElements from curLM
            returnedList = curLM.getNextKnuthElements(lc, alignment);
            if (returnedList != null) {
                // "wrap" the Position stored in each element of returnedList
                ListIterator listIter = returnedList.listIterator();
                while (listIter.hasNext()) {
                    returnedElement = (KnuthElement) listIter.next();
                    returnedElement.setPosition
                        (new NonLeafPosition(this,
                                             returnedElement.getPosition()));
                    returnList.add(returnedElement);
                }
                return returnList;
            } else {
                // curLM returned null because it finished;
                // just iterate once more to see if there is another child
            }
        }
        setFinished(true);
        return null;
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

        // "wrap" againg the Position stored in each element of oldList
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

