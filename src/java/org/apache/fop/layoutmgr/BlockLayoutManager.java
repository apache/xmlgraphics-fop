/*
 * Copyright 1999-2006 The Apache Software Foundation.
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
import java.util.List;
import java.util.ListIterator;

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.LineArea;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fonts.Font;
import org.apache.fop.layoutmgr.inline.InlineLayoutManager;
import org.apache.fop.layoutmgr.inline.InlineLevelLayoutManager;
import org.apache.fop.layoutmgr.inline.LineLayoutManager;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;

/**
 * LayoutManager for a block FO.
 */
public class BlockLayoutManager extends BlockStackingLayoutManager 
            implements ConditionalElementListener {
    
    private Block curBlockArea;

    /** Iterator over the child layout managers. */
    protected ListIterator proxyLMiter;

    private int lead = 12000;
    private Length lineHeight;
    private int follow = 2000;
    private int middleShift = 0;
    
    private boolean discardBorderBefore;
    private boolean discardBorderAfter;
    private boolean discardPaddingBefore;
    private boolean discardPaddingAfter;
    private MinOptMax effSpaceBefore;
    private MinOptMax effSpaceAfter;

    /** The list of child BreakPoss instances. */
    protected List childBreaks = new java.util.ArrayList();

    /**
     * Creates a new BlockLayoutManager.
     * @param inBlock the block FO object to create the layout manager for.
     */
    public BlockLayoutManager(org.apache.fop.fo.flow.Block inBlock) {
        super(inBlock);
        proxyLMiter = new ProxyLMiter();
    }

    public void initialize() {
        super.initialize();
        Font fs = getBlockFO().getCommonFont().getFontState(
                  getBlockFO().getFOEventHandler().getFontInfo(), this);
        
        lead = fs.getAscender();
        follow = -fs.getDescender();
        middleShift = -fs.getXHeight() / 2;
        lineHeight = getBlockFO().getLineHeight().getOptimum(this).getLength();
        startIndent = getBlockFO().getCommonMarginBlock().startIndent.getValue(this);
        endIndent = getBlockFO().getCommonMarginBlock().endIndent.getValue(this); 
        foSpaceBefore = new SpaceVal(getBlockFO().getCommonMarginBlock().spaceBefore, this)
                            .getSpace();
        foSpaceAfter = new SpaceVal(getBlockFO().getCommonMarginBlock().spaceAfter, this)
                            .getSpace();
        bpUnit = 0; // non-standard extension
        if (bpUnit == 0) {
            // use optimum space values
            adjustedSpaceBefore = getBlockFO().getCommonMarginBlock().spaceBefore.getSpace()
                                        .getOptimum(this).getLength().getValue(this);
            adjustedSpaceAfter = getBlockFO().getCommonMarginBlock().spaceAfter.getSpace()
                                        .getOptimum(this).getLength().getValue(this);
        } else {
            // use minimum space values
            adjustedSpaceBefore = getBlockFO().getCommonMarginBlock().spaceBefore.getSpace()
                                        .getMinimum(this).getLength().getValue(this);
            adjustedSpaceAfter = getBlockFO().getCommonMarginBlock().spaceAfter.getSpace()
                                        .getMinimum(this).getLength().getValue(this);
        }
    }

    /** @see org.apache.fop.layoutmgr.BlockStackingLayoutManager */
    public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
        resetSpaces(); 
        return super.getNextKnuthElements(context, alignment);
    }
   
    private void resetSpaces() {
        this.discardBorderBefore = false;        
        this.discardBorderAfter = false;        
        this.discardPaddingBefore = false;        
        this.discardPaddingAfter = false;
        this.effSpaceBefore = null;
        this.effSpaceAfter = null;
    }
    
    /**
     * Proxy iterator for Block LM.
     * This iterator creates and holds the complete list
     * of child LMs.
     * It uses fobjIter as its base iterator.
     * Block LM's createNextChildLMs uses this iterator
     * as its base iterator.
     */
    protected class ProxyLMiter extends LMiter {

        /*
         * Constructs a proxy iterator for Block LM.
         */
        public ProxyLMiter() {
            super(BlockLayoutManager.this);
            listLMs = new java.util.ArrayList(10);
        }

        /**
         * @return true if there are more child lms
         */
        public boolean hasNext() {
            return (curPos < listLMs.size()) ? true : createNextChildLMs(curPos);
        }

        /**
         * @return true if new child lms were added
         */
        protected boolean createNextChildLMs(int pos) {
            List newLMs = createChildLMs(pos + 1 - listLMs.size());
            if (newLMs != null) {
                listLMs.addAll(newLMs);
            }
            return pos < listLMs.size();
        }
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#createNextChildLMs
     */
    public boolean createNextChildLMs(int pos) {

        while (proxyLMiter.hasNext()) {
            LayoutManager lm = (LayoutManager) proxyLMiter.next();
            if (lm instanceof InlineLevelLayoutManager) {
                LineLayoutManager lineLM = createLineManager(lm);
                addChildLM(lineLM);
            } else {
                addChildLM(lm);
            }
            if (pos < childLMs.size()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create a new LineLM, and collect all consecutive
     * inline generating LMs as its child LMs.
     * @param firstlm First LM in new LineLM
     * @return the newly created LineLM
     */
    private LineLayoutManager createLineManager(LayoutManager firstlm) {
        LineLayoutManager llm;
        llm = new LineLayoutManager(getBlockFO(), lineHeight, lead, follow);
        List inlines = new java.util.ArrayList();
        inlines.add(firstlm);
        while (proxyLMiter.hasNext()) {
            LayoutManager lm = (LayoutManager) proxyLMiter.next();
            if (lm instanceof InlineLevelLayoutManager) {
                inlines.add(lm);
            } else {
                proxyLMiter.previous();
                break;
            }
        }
        llm.addChildLMs(inlines);
        return llm;
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepTogether()
     */
    public boolean mustKeepTogether() {
        // TODO Keeps will have to be more sophisticated sooner or later
        // TODO This is a quick fix for the fact that the parent is not always a BlockLevelLM;
        // eventually mustKeepTogether() must be moved up to the LM interface
        return (!getBlockFO().getKeepTogether().getWithinPage().isAuto()
                || !getBlockFO().getKeepTogether().getWithinColumn().isAuto()
                || (getParent() instanceof BlockLevelLayoutManager
                    && ((BlockLevelLayoutManager) getParent()).mustKeepTogether())
                || (getParent() instanceof InlineLayoutManager
                    && ((InlineLayoutManager) getParent()).mustKeepTogether()));
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepWithPrevious()
     */
    public boolean mustKeepWithPrevious() {
        return !getBlockFO().getKeepWithPrevious().getWithinPage().isAuto()
                || !getBlockFO().getKeepWithPrevious().getWithinColumn().isAuto();
    }

    /**
     * @see org.apache.fop.layoutmgr.BlockLevelLayoutManager#mustKeepWithNext()
     */
    public boolean mustKeepWithNext() {
        return !getBlockFO().getKeepWithNext().getWithinPage().isAuto()
                || !getBlockFO().getKeepWithNext().getWithinColumn().isAuto();
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#addAreas(org.apache.fop.layoutmgr.PositionIterator, org.apache.fop.layoutmgr.LayoutContext)
     */
    public void addAreas(PositionIterator parentIter,
            LayoutContext layoutContext) {
        getParentArea(null);

        // if this will create the first block area in a page
        // and display-align is after or center, add space before
        if (layoutContext.getSpaceBefore() > 0) {
            addBlockSpacing(0.0, new MinOptMax(layoutContext.getSpaceBefore()));
        }

        LayoutManager childLM = null;
        LayoutManager lastLM = null;
        LayoutContext lc = new LayoutContext(0);
        lc.setSpaceAdjust(layoutContext.getSpaceAdjust());
        // set space after in the LayoutContext for children
        if (layoutContext.getSpaceAfter() > 0) {
            lc.setSpaceAfter(layoutContext.getSpaceAfter());
        }
        PositionIterator childPosIter;

        // "unwrap" the NonLeafPositions stored in parentIter
        // and put them in a new list;
        LinkedList positionList = new LinkedList();
        Position pos;
        boolean bSpaceBefore = false;
        boolean bSpaceAfter = false;
        Position firstPos = null;
        Position lastPos = null;
        while (parentIter.hasNext()) {
            pos = (Position) parentIter.next();
            //log.trace("pos = " + pos.getClass().getName() + "; " + pos);
            if (pos.getIndex() >= 0) {
                if (firstPos == null) {
                    firstPos = pos;
                }
                lastPos = pos;
            }
            Position innerPosition = pos;
            if (pos instanceof NonLeafPosition) {
                //Not all elements are wrapped
                innerPosition = ((NonLeafPosition) pos).getPosition();
            }
            if (innerPosition == null) {
                // pos was created by this BlockLM and was inside an element
                // representing space before or after
                // this means the space was not discarded
                if (positionList.size() == 0) {
                    // pos was in the element representing space-before
                    bSpaceBefore = true;
                    //log.trace(" space before");
                } else {
                    // pos was in the element representing space-after
                    bSpaceAfter = true;
                    //log.trace(" space-after");
                }
            } else if (innerPosition.getLM() == this
                    && !(innerPosition instanceof MappingPosition)) {
                // pos was created by this BlockLM and was inside a penalty
                // allowing or forbidding a page break
                // nothing to do
                //log.trace(" penalty");
            } else {
                // innerPosition was created by another LM
                positionList.add(innerPosition);
                lastLM = innerPosition.getLM();
                //log.trace(" " + innerPosition.getClass().getName());
            }
        }

        getPSLM().addIDToPage(getBlockFO().getId());
        if (markers != null) {
            getCurrentPV().addMarkers(markers, true, isFirst(firstPos), isLast(lastPos));
        }

        if (bpUnit == 0) {
            // the Positions in positionList were inside the elements
            // created by the LineLM
            childPosIter = new StackingIter(positionList.listIterator());
            } else {
            // the Positions in positionList were inside the elements
            // created by the BlockLM in the createUnitElements() method
            //if (((Position) positionList.getLast()) instanceof
                  // LeafPosition) {
            //    // the last item inside positionList is a LeafPosition
            //    // (a LineBreakPosition, more precisely); this means that
            //    // the whole paragraph is on the same page
            //    childPosIter = new KnuthPossPosIter(storedList, 0,
                  // storedList.size());
            //} else {
            //    // the last item inside positionList is a Position;
            //    // this means that the paragraph has been split
            //    // between consecutive pages
            LinkedList splitList = new LinkedList();
            int splitLength = 0;
            int iFirst = ((MappingPosition) positionList.getFirst()).getFirstIndex();
            int iLast = ((MappingPosition) positionList.getLast()).getLastIndex();
            // copy from storedList to splitList all the elements from
            // iFirst to iLast
            ListIterator storedListIterator = storedList.listIterator(iFirst);
            while (storedListIterator.nextIndex() <= iLast) {
                KnuthElement element = (KnuthElement) storedListIterator
                        .next();
                // some elements in storedList (i.e. penalty items) were created
                // by this BlockLM, and must be ignored
                if (element.getLayoutManager() != this) {
                    splitList.add(element);
                    splitLength += element.getW();
                    lastLM = element.getLayoutManager();
                }
            }
            //log.debug("Adding areas from " + iFirst + " to " + iLast);
            //log.debug("splitLength= " + splitLength
            //                   + " (" + neededUnits(splitLength) + " units') "
            //                   + (neededUnits(splitLength) * bpUnit - splitLength) 
            //                   + " spacing");
            // add space before and / or after the paragraph
            // to reach a multiple of bpUnit
            if (bSpaceBefore && bSpaceAfter) {
                foSpaceBefore = new SpaceVal(getBlockFO()
                                    .getCommonMarginBlock().spaceBefore, this).getSpace();
                foSpaceAfter = new SpaceVal(getBlockFO()
                                    .getCommonMarginBlock().spaceAfter, this).getSpace();
                adjustedSpaceBefore = (neededUnits(splitLength
                        + foSpaceBefore.min
                        + foSpaceAfter.min)
                        * bpUnit - splitLength) / 2;
                adjustedSpaceAfter = neededUnits(splitLength
                        + foSpaceBefore.min
                        + foSpaceAfter.min)
                        * bpUnit - splitLength - adjustedSpaceBefore;
                } else if (bSpaceBefore) {
                adjustedSpaceBefore = neededUnits(splitLength
                        + foSpaceBefore.min)
                        * bpUnit - splitLength;
                } else {
                adjustedSpaceAfter = neededUnits(splitLength
                        + foSpaceAfter.min)
                        * bpUnit - splitLength;
                }
            //log.debug("spazio prima = " + adjustedSpaceBefore
                  // + " spazio dopo = " + adjustedSpaceAfter + " totale = " +
                  // (adjustedSpaceBefore + adjustedSpaceAfter + splitLength));
            childPosIter = new KnuthPossPosIter(splitList, 0, splitList
                    .size());
            //}
        }

        while ((childLM = childPosIter.getNextChildLM()) != null) {
            // set last area flag
            lc.setFlags(LayoutContext.LAST_AREA,
                    (layoutContext.isLastArea() && childLM == lastLM));
            lc.setStackLimit(layoutContext.getStackLimit());
            // Add the line areas to Area
            childLM.addAreas(childPosIter, lc);
        }

        if (markers != null) {
            getCurrentPV().addMarkers(markers, false, isFirst(firstPos), isLast(lastPos));
        }

        TraitSetter.addSpaceBeforeAfter(curBlockArea, layoutContext.getSpaceAdjust(), 
                effSpaceBefore, effSpaceAfter);
        flush();

        curBlockArea = null;
        resetSpaces();
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
     * @param childArea area to get the parent area for
     * @return the parent area
     */
    public Area getParentArea(Area childArea) {
        if (curBlockArea == null) {
            curBlockArea = new Block();

            curBlockArea.setIPD(super.getContentAreaIPD());

            TraitSetter.addBreaks(curBlockArea, 
                    getBlockFO().getBreakBefore(), getBlockFO().getBreakAfter());

            // Must get dimensions from parent area
            //Don't optimize this line away. It can have ugly side-effects.
            /*Area parentArea =*/ parentLM.getParentArea(curBlockArea);

            // set traits
            TraitSetter.setProducerID(curBlockArea, getBlockFO().getId());
            TraitSetter.addBorders(curBlockArea, 
                    getBlockFO().getCommonBorderPaddingBackground(), 
                    discardBorderBefore, discardBorderAfter, false, false, this);
            TraitSetter.addPadding(curBlockArea, 
                    getBlockFO().getCommonBorderPaddingBackground(), 
                    discardPaddingBefore, discardPaddingAfter, false, false, this);
            TraitSetter.addMargins(curBlockArea,
                    getBlockFO().getCommonBorderPaddingBackground(), 
                    startIndent, endIndent,
                    this);

            setCurrentArea(curBlockArea); // ??? for generic operations
        }
        return curBlockArea;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#addChildArea(Area)
     */
    public void addChildArea(Area childArea) {
        if (curBlockArea != null) {
            if (childArea instanceof LineArea) {
                curBlockArea.addLineArea((LineArea) childArea);
            } else {
                curBlockArea.addBlock((Block) childArea);
            }
        }
    }

    /**
     * Force current area to be added to parent area.
     * @see org.apache.fop.layoutmgr.BlockStackingLayoutManager#flush()
     */
    protected void flush() {
        if (curBlockArea != null) {
            TraitSetter.addBackground(curBlockArea, 
                    getBlockFO().getCommonBorderPaddingBackground(),
                    this);
            super.flush();
        }
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#resetPosition(org.apache.fop.layoutmgr.Position)
     */
    public void resetPosition(Position resetPos) {
        if (resetPos == null) {
            reset(null);
            childBreaks.clear();
        } else {
            //reset(resetPos);
            LayoutManager lm = resetPos.getLM();
        }
    }

    /**
     * convenience method that returns the Block node
     * @return the block node
     */
    protected org.apache.fop.fo.flow.Block getBlockFO() {
        return (org.apache.fop.fo.flow.Block) fobj;
    }
    
    // --------- Property Resolution related functions --------- //
    
    /**
     * Returns the IPD of the content area
     * @return the IPD of the content area
     */
    public int getContentAreaIPD() {
        if (curBlockArea != null) {
            return curBlockArea.getIPD();
        }
        return super.getContentAreaIPD();
    }
   

    /**
     * Returns the BPD of the content area
     * @return the BPD of the content area
     */
    public int getContentAreaBPD() {
        if (curBlockArea != null) {
            return curBlockArea.getBPD();
        }
        return -1;
    }
   
    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getGeneratesBlockArea
     */
    public boolean getGeneratesBlockArea() {
        return true;
    }

    /** @see org.apache.fop.layoutmgr.ConditionalElementListener */
    public void notifySpace(RelSide side, MinOptMax effectiveLength) {
        if (RelSide.BEFORE == side) {
            if (log.isDebugEnabled()) {
                log.debug(this + ": Space " + side + ", " 
                        + this.effSpaceBefore + "-> " + effectiveLength);
            }
            this.effSpaceBefore = effectiveLength;
        } else {
            if (log.isDebugEnabled()) {
                log.debug(this + ": Space " + side + ", " 
                        + this.effSpaceAfter + "-> " + effectiveLength);
            }
            this.effSpaceAfter = effectiveLength;
        }
    }

    /** @see org.apache.fop.layoutmgr.ConditionalElementListener */
    public void notifyBorder(RelSide side, MinOptMax effectiveLength) {
        if (effectiveLength == null) {
            if (RelSide.BEFORE == side) {
                this.discardBorderBefore = true;
            } else {
                this.discardBorderAfter = true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(this + ": Border " + side + " -> " + effectiveLength);
        }
    }

    /** @see org.apache.fop.layoutmgr.ConditionalElementListener */
    public void notifyPadding(RelSide side, MinOptMax effectiveLength) {
        if (effectiveLength == null) {
            if (RelSide.BEFORE == side) {
                this.discardPaddingBefore = true;
            } else {
                this.discardPaddingAfter = true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(this + ": Padding " + side + " -> " + effectiveLength);
        }
    }

}

