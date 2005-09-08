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

import java.util.ListIterator;
import java.util.LinkedList;

import org.apache.fop.area.Area;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineBlockParent;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.InlineLevel;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.layoutmgr.SpaceSpecifier;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;

/**
 * LayoutManager for objects which stack children in the inline direction,
 * such as Inline or Line
 */
public class InlineLayoutManager extends InlineStackingLayoutManager 
                                         implements InlineLevelLayoutManager {
    private InlineLevel fobj;

    private CommonMarginInline inlineProps = null;
    private CommonBorderPaddingBackground borderProps = null;

    private boolean bAreaCreated = false;
    private LayoutManager lastChildLM = null; // Set when return last breakposs;

    /**
     * Create an inline layout manager.
     * This is used for fo's that create areas that
     * contain inline areas.
     *
     * @param node the formatting object that creates the area
     */
    // The node should be FObjMixed
    public InlineLayoutManager(InlineLevel node) {
        super(node);
        fobj = node;
    }
    
    private Inline getInlineFO() {
        return (Inline)fobj;
    }
    
    public void initialize() {
        inlineProps = fobj.getCommonMarginInline();
        borderProps = fobj.getCommonBorderPaddingBackground();

        int iPad = borderProps.getPadding(CommonBorderPaddingBackground.BEFORE, false, this);
        iPad += borderProps.getBorderWidth(CommonBorderPaddingBackground.BEFORE,
                                             false);
        iPad += borderProps.getPadding(CommonBorderPaddingBackground.AFTER, false, this);
        iPad += borderProps.getBorderWidth(CommonBorderPaddingBackground.AFTER, false);
        extraBPD = new MinOptMax(iPad);
    }

    protected MinOptMax getExtraIPD(boolean bNotFirst, boolean bNotLast) {
        int iBP = borderProps.getPadding(CommonBorderPaddingBackground.START,
                                           bNotFirst, this);
        iBP += borderProps.getBorderWidth(CommonBorderPaddingBackground.START,
                                            bNotFirst);
        iBP += borderProps.getPadding(CommonBorderPaddingBackground.END, bNotLast, this);
        iBP += borderProps.getBorderWidth(CommonBorderPaddingBackground.END, bNotLast);
        return new MinOptMax(iBP);
    }


    protected boolean hasLeadingFence(boolean bNotFirst) {
        int iBP = borderProps.getPadding(CommonBorderPaddingBackground.START,
                                           bNotFirst, this);
        iBP += borderProps.getBorderWidth(CommonBorderPaddingBackground.START,
                                            bNotFirst);
        return (iBP > 0);
    }

    protected boolean hasTrailingFence(boolean bNotLast) {
        int iBP = borderProps.getPadding(CommonBorderPaddingBackground.END, bNotLast, this);
        iBP += borderProps.getBorderWidth(CommonBorderPaddingBackground.END, bNotLast);
        return (iBP > 0);
    }

    protected SpaceProperty getSpaceStart() {
        return inlineProps.spaceStart;
    }
    protected SpaceProperty getSpaceEnd() {
        return inlineProps.spaceEnd;
    }
    
    /** @see org.apache.fop.layoutmgr.inline.InlineLayoutManager#createArea(boolean) */
    protected InlineArea createArea(boolean bInlineParent) {
        InlineArea area;
        if (bInlineParent) {
            area = new InlineParent();
            area.setOffset(0);
        } else {
            area = new InlineBlockParent();
        }
        TraitSetter.setProducerID(area, getInlineFO().getId());
        return area;
    }
    
    /**
     * @see org.apache.fop.layoutmgr.inline.InlineStackingLayoutManager#setTraits(boolean, boolean)
     */
    protected void setTraits(boolean bNotFirst, boolean bNotLast) {
        
        // Add border and padding to current area and set flags (FIRST, LAST ...)
        TraitSetter.setBorderPaddingTraits(getCurrentArea(),
                                           borderProps, bNotFirst, bNotLast, this);

        if (borderProps != null) {
            TraitSetter.addBorders(getCurrentArea(), borderProps, this);
            TraitSetter.addBackground(getCurrentArea(), borderProps, this);
        }
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public LinkedList getNextKnuthElements(LayoutContext lc, int alignment) {
        InlineLevelLayoutManager curILM;
        LayoutManager curLM, lastLM = null;

        // the list returned by child LM
        LinkedList returnedList;
        KnuthElement returnedElement;

        // the list which will be returned to the parent LM
        LinkedList returnList = new LinkedList();
        KnuthSequence lastSequence = null;

        SpaceSpecifier leadingSpace = lc.getLeadingSpace();

        if (lc.startsNewArea()) {
            // First call to this LM in new parent "area", but this may
            // not be the first area created by this inline
            childLC = new LayoutContext(lc);
            if (getSpaceStart() != null) {
                lc.getLeadingSpace().addSpace(new SpaceVal(getSpaceStart(), this));
            }

            // Check for "fence"
            if (hasLeadingFence(!lc.isFirstArea())) {
                // Reset leading space sequence for child areas
                leadingSpace = new SpaceSpecifier(false);
            }
            // Reset state variables
            clearPrevIPD(); // Clear stored prev content dimensions
        }

        StringBuffer trace = new StringBuffer("InlineLM:");

        while ((curLM = (LayoutManager) getChildLM()) != null) {
            // get KnuthElements from curLM
            returnedList = curLM.getNextKnuthElements(lc, alignment);
            if (returnedList == null) {
                // curLM returned null because it finished;
                // just iterate once more to see if there is another child
                continue;
            }
            if (curLM instanceof InlineLevelLayoutManager) {
                // close the last block sequence 
                if (lastSequence != null && !lastSequence.isInlineSequence()) {
                    lastSequence = null;
                    if (log.isTraceEnabled()) {
                        trace.append(" ]");
                    }
                }
                // "wrap" the Position stored in each element of returnedList
                ListIterator seqIter = returnedList.listIterator();
                while (seqIter.hasNext()) {
                    KnuthSequence sequence = (KnuthSequence) seqIter.next();
                    ListIterator listIter = sequence.listIterator();
                    while (listIter.hasNext()) {
                        returnedElement = (KnuthElement) listIter.next();
                        returnedElement.setPosition
                        (new NonLeafPosition(this,
                                returnedElement.getPosition()));
                    }
                    if (!sequence.isInlineSequence()) {
                        if (lastSequence != null && lastSequence.isInlineSequence()) {
                            // log.error("Last inline sequence should be closed before a block sequence");
                            lastSequence.add(new KnuthPenalty(0, -KnuthElement.INFINITE,
                                                   false, null, false));
                            lastSequence = null;
                            if (log.isTraceEnabled()) {
                                trace.append(" ]");
                            }
                        }
                        returnList.add(sequence);
                        if (log.isTraceEnabled()) {
                            trace.append(" B");
                        }
                    } else {
                        if (lastSequence == null) {
                            lastSequence = new KnuthSequence(true);
                            returnList.add(lastSequence);
                            if (log.isTraceEnabled()) {
                                trace.append(" [");
                            }
                        } else {
                            if (log.isTraceEnabled()) {
                                trace.append(" +");
                            }
                        }
                        lastSequence.addAll(sequence);
                        if (log.isTraceEnabled()) {
                            trace.append(" I");
                        }
                       // finish last paragraph if it was closed with a linefeed
                        KnuthElement lastElement = (KnuthElement) sequence.getLast();
                        if (lastElement.isPenalty()
                                && ((KnuthPenalty) lastElement).getP()
                                == -KnuthPenalty.INFINITE) {
                            // a penalty item whose value is -inf
                            // represents a preserved linefeed,
                            // wich forces a line break
                            lastSequence = null;
                            if (log.isTraceEnabled()) {
                                trace.append(" ]");
                            }
                        }
                    }
                }
            } else { // A block LM
                // close the last inline sequence 
                if (lastSequence != null && lastSequence.isInlineSequence()) {
                    lastSequence.add(new KnuthPenalty(0, -KnuthElement.INFINITE,
                                           false, null, false));
                    lastSequence = null;
                    if (log.isTraceEnabled()) {
                        trace.append(" ]");
                    }
                }
                if (curLM != lastLM) {
                    // close the last block sequence
                    if (lastSequence != null && !lastSequence.isInlineSequence()) {
                        lastSequence = null;
                        if (log.isTraceEnabled()) {
                            trace.append(" ]");
                        }
                    }
                    lastLM = curLM;
                }
                if (lastSequence == null) {
                    lastSequence = new KnuthSequence(false);
                    returnList.add(lastSequence);
                    if (log.isTraceEnabled()) {
                        trace.append(" [");
                    }
                } else {
                    if (log.isTraceEnabled()) {
                        trace.append(" +");
                    }
                }
                ListIterator iter = returnedList.listIterator();
                while (iter.hasNext()) {
                    KnuthElement element = (KnuthElement) iter.next();
                    element.setPosition
                    (new NonLeafPosition(this,
                            element.getPosition()));
                }
                lastSequence.addAll(returnedList);
                if (log.isTraceEnabled()) {
                    trace.append(" L");
                }
            }
        }
        setFinished(true);
        log.trace(trace);
        return returnList.size() == 0 ? null : returnList;
    }

    /**
     * Generate and add areas to parent area.
     * Set size of each area. This should only create and return one
     * inline area for any inline parent area.
     *
     * @param parentIter Iterator over Position information returned
     * by this LayoutManager.
     * @param context layout context.
     */
    public void addAreas(PositionIterator parentIter,
                         LayoutContext context) {
        addId();

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
            context.getLeadingSpace().addSpace(new SpaceVal(getSpaceStart(), this));
        }

        // "unwrap" the NonLeafPositions stored in parentIter
        // and put them in a new list; 
        // also set lastLM to be the LayoutManager which created
        // the last Position: if the LAST_AREA flag is set in context,
        // it must be also set in the LayoutContext given to lastLM,
        // but unset in the LayoutContext given to the other LMs
        LinkedList positionList = new LinkedList();
        NonLeafPosition pos = null;
        LayoutManager lastLM = null; // last child LM in this iterator
        while (parentIter.hasNext()) {
            pos = (NonLeafPosition) parentIter.next();
            positionList.add(pos.getPosition());
        }
        if (pos != null) {
            lastLM = pos.getPosition().getLM();
        }

        InlineArea parent = createArea(lastLM == null || lastLM instanceof InlineLevelLayoutManager);
        parent.setBPD(context.getLineHeight());
        setCurrentArea(parent);
        
        StackingIter childPosIter
            = new StackingIter(positionList.listIterator());

        LayoutManager prevLM = null;
        LayoutManager childLM;
        while ((childLM = childPosIter.getNextChildLM())
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
            context.getTrailingSpace().addSpace(new SpaceVal(getSpaceEnd(), this));
        }
        
        parentLM.addChildArea(getCurrentArea());
        setTraits(bAreaCreated, !bIsLast);

        context.setFlags(LayoutContext.LAST_AREA, bIsLast);
        bAreaCreated = true;
    }

    public void addChildArea(Area childArea) {
        Area parent = getCurrentArea();
        if (getContext().resolveLeadingSpace()) {
            addSpace(parent,
                    getContext().getLeadingSpace().resolve(false),
                    getContext().getSpaceAdjust());
        }
        parent.addChildArea(childArea);
    }

    /*
    public KnuthElement addALetterSpaceTo(KnuthElement element) {
        NonLeafPosition savedPos = (NonLeafPosition) element.getPosition();
        element.setPosition(savedPos.getPosition());

        KnuthElement newElement
            = ((InlineLevelLayoutManager)
               element.getLayoutManager()).addALetterSpaceTo(element);
        newElement.setPosition
            (new NonLeafPosition(this, newElement.getPosition()));
        element.setPosition(savedPos);
        return newElement;
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

    public LinkedList getChangedKnuthElements(List oldList, int flaggedPenalty, int alignment) {
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
                          flaggedPenalty, alignment));
                    prevLM = currLM;
                    fromIndex = oldListIterator.previousIndex();
                } else if (currLM == prevLM) {
                    returnedList.addAll
                        (prevLM.getChangedKnuthElements
                         (oldList.subList(fromIndex, oldList.size()),
                          flaggedPenalty, alignment));
                } else {
                    returnedList.addAll
                        (prevLM.getChangedKnuthElements
                         (oldList.subList(fromIndex,
                                          oldListIterator.previousIndex()),
                          flaggedPenalty, alignment));
                    returnedList.addAll
                        (currLM.getChangedKnuthElements
                         (oldList.subList(oldListIterator.previousIndex(),
                                          oldList.size()),
                          flaggedPenalty, alignment));
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
    }*/
    
    /** @see org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager#addId() */
    protected void addId() {
        getPSLM().addIDToPage(getInlineFO().getId());
    }
    
}

