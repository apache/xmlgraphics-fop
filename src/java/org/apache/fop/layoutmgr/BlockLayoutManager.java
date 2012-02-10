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

package org.apache.fop.layoutmgr;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.LineArea;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.layoutmgr.inline.InlineLevelLayoutManager;
import org.apache.fop.layoutmgr.inline.LineLayoutManager;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;

/**
 * LayoutManager for a block FO.
 */
public class BlockLayoutManager extends BlockStackingLayoutManager
            implements ConditionalElementListener {

    /** logging instance */
    private static Log log = LogFactory.getLog(BlockLayoutManager.class);

    private Block curBlockArea;

    /** Iterator over the child layout managers. */
    protected ListIterator<LayoutManager> proxyLMiter;

    private int lead = 12000;
    private Length lineHeight;
    private int follow = 2000;
    //private int middleShift = 0;

    private boolean discardBorderBefore;
    private boolean discardBorderAfter;
    private boolean discardPaddingBefore;
    private boolean discardPaddingAfter;
    private MinOptMax effSpaceBefore;
    private MinOptMax effSpaceAfter;

    /**
     * Creates a new BlockLayoutManager.
     * @param inBlock the block FO object to create the layout manager for.
     */
    public BlockLayoutManager(org.apache.fop.fo.flow.Block inBlock) {
        super(inBlock);
        proxyLMiter = new ProxyLMiter();
    }

    /** {@inheritDoc} */
    @Override
    public void initialize() {
        super.initialize();
        org.apache.fop.fo.flow.Block fo = getBlockFO();
        FontInfo fi = fo.getFOEventHandler().getFontInfo();
        FontTriplet[] fontkeys = fo.getCommonFont().getFontState(fi);
        Font initFont = fi.getFontInstance(fontkeys[0],
                getBlockFO().getCommonFont().fontSize.getValue(this));
        lead = initFont.getAscender();
        follow = -initFont.getDescender();
        //middleShift = -fs.getXHeight() / 2;
        lineHeight = fo.getLineHeight().getOptimum(this).getLength();
        startIndent = fo.getCommonMarginBlock().startIndent.getValue(this);
        endIndent = fo.getCommonMarginBlock().endIndent.getValue(this);
        foSpaceBefore = new SpaceVal(fo.getCommonMarginBlock().spaceBefore, this).getSpace();
        foSpaceAfter = new SpaceVal(fo.getCommonMarginBlock().spaceAfter, this).getSpace();
        // use optimum space values
        adjustedSpaceBefore = fo.getCommonMarginBlock().spaceBefore.getSpace()
                                    .getOptimum(this).getLength().getValue(this);
        adjustedSpaceAfter = fo.getCommonMarginBlock().spaceAfter.getSpace()
                                    .getOptimum(this).getLength().getValue(this);
    }

    /** {@inheritDoc} */
    @Override
    public List getNextKnuthElements(LayoutContext context, int alignment) {
        return getNextKnuthElements(context, alignment, null, null, null);
    }

    /** {@inheritDoc} */
    @Override
    public List getNextKnuthElements(LayoutContext context, int alignment, Stack lmStack,
            Position restartPosition, LayoutManager restartAtLM) {
        resetSpaces();
        return super.getNextKnuthElements(
                context, alignment, lmStack, restartPosition, restartAtLM);
    }

    /**
     * Overridden to take into account that the childLM may be the block's
     * {@link LineLayoutManager}.
     * {@inheritDoc}
     */
    @Override
    protected List<ListElement> getNextChildElements(LayoutManager childLM, LayoutContext context,
           LayoutContext childLC, int alignment, Stack lmStack, Position restartPosition,
           LayoutManager restartAtLM) {

        childLC.copyPendingMarksFrom(context);

        if (childLM instanceof LineLayoutManager) {
            childLC.setRefIPD(getContentAreaIPD());
        } else {
            // nop; will have been properly set by makeChildLayoutContext()
        }

        if (childLM == this.childLMs.get(0)) {
            childLC.setFlags(LayoutContext.SUPPRESS_BREAK_BEFORE);
            //Handled already by the parent (break collapsing, see above)
        }

        if (lmStack == null) {
            return childLM.getNextKnuthElements(childLC, alignment);
        } else {
            if (childLM instanceof LineLayoutManager) {
                assert (restartPosition instanceof LeafPosition);
                return ((LineLayoutManager) childLM).getNextKnuthElements(childLC, alignment,
                        (LeafPosition) restartPosition);
            } else {
                return childLM.getNextKnuthElements(childLC, alignment,
                        lmStack, restartPosition, restartAtLM);
            }
        }
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

        /**
         * Constructs a proxy iterator for Block LM.
         */
        public ProxyLMiter() {
            super(BlockLayoutManager.this);
            listLMs = new java.util.ArrayList<LayoutManager>(10);
        }

        /**
         * @return true if there are more child lms
         */
        public boolean hasNext() {
            return (curPos < listLMs.size()) || createNextChildLMs(curPos);
        }

        /**
         * @param pos ...
         * @return true if new child lms were added
         */
        protected boolean createNextChildLMs(int pos) {
            List<LayoutManager> newLMs = createChildLMs(pos + 1 - listLMs.size());
            if (newLMs != null) {
                listLMs.addAll(newLMs);
            }
            return pos < listLMs.size();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean createNextChildLMs(int pos) {

        while (proxyLMiter.hasNext()) {
            LayoutManager lm = proxyLMiter.next();
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
        List<LayoutManager> inlines = new java.util.ArrayList<LayoutManager>();
        inlines.add(firstlm);
        while (proxyLMiter.hasNext()) {
            LayoutManager lm = proxyLMiter.next();
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

    /** {@inheritDoc} */
    @Override
    public KeepProperty getKeepTogetherProperty() {
        return getBlockFO().getKeepTogether();
    }

    /** {@inheritDoc} */
    @Override
    public KeepProperty getKeepWithPreviousProperty() {
        return getBlockFO().getKeepWithPrevious();
    }

    /** {@inheritDoc} */
    @Override
    public KeepProperty getKeepWithNextProperty() {
        return getBlockFO().getKeepWithNext();
    }

    /** {@inheritDoc} */
    @Override
    public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
        getParentArea(null);

        // if this will create the first block area in a page
        // and display-align is after or center, add space before
        if (layoutContext.getSpaceBefore() > 0) {
            addBlockSpacing(0.0, MinOptMax.getInstance(layoutContext.getSpaceBefore()));
        }

        LayoutManager childLM;
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
        LinkedList<Position> positionList = new LinkedList<Position>();
        Position pos;
        Position firstPos = null;
        Position lastPos = null;
        while (parentIter.hasNext()) {
            pos = parentIter.next();
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
                innerPosition = pos.getPosition();
            }

            if (innerPosition != null
                    && (innerPosition.getLM() != this
                        || innerPosition instanceof MappingPosition)) {
                // innerPosition was created by another LM
                positionList.add(innerPosition);
                lastLM = innerPosition.getLM();
            }
        }

        addId();

        addMarkersToPage(true, isFirst(firstPos), isLast(lastPos));

        // the Positions in positionList were inside the elements
        // created by the LineLM
        childPosIter = new PositionIterator(positionList.listIterator());

        while ((childLM = childPosIter.getNextChildLM()) != null) {
            // set last area flag
            lc.setFlags(LayoutContext.LAST_AREA,
                    (layoutContext.isLastArea() && childLM == lastLM));
            lc.setStackLimitBP(layoutContext.getStackLimitBP());
            // Add the line areas to Area
            childLM.addAreas(childPosIter, lc);
        }

        addMarkersToPage(false, isFirst(firstPos), isLast(lastPos));

        TraitSetter.addSpaceBeforeAfter(curBlockArea, layoutContext.getSpaceAdjust(),
                effSpaceBefore, effSpaceAfter);
        flush();

        curBlockArea = null;
        resetSpaces();

        //Notify end of block layout manager to the PSLM
        checkEndOfLayout(lastPos);
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
    @Override
    public Area getParentArea(Area childArea) {
        if (curBlockArea == null) {
            curBlockArea = new Block();

            curBlockArea.setIPD(super.getContentAreaIPD());

            TraitSetter.addBreaks(curBlockArea,
                    getBlockFO().getBreakBefore(), getBlockFO().getBreakAfter());

            // Must get dimensions from parent area
            //Don't optimize this line away. It can have ugly side-effects.
            /*Area parentArea =*/ parentLayoutManager.getParentArea(curBlockArea);

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

    /** {@inheritDoc} */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    protected void flush() {
        if (curBlockArea != null) {
            TraitSetter.addBackground(curBlockArea,
                    getBlockFO().getCommonBorderPaddingBackground(),
                    this);
            super.flush();
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
    @Override
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
    @Override
    public int getContentAreaBPD() {
        if (curBlockArea != null) {
            return curBlockArea.getBPD();
        }
        return -1;
    }

    /** {@inheritDoc} */
    @Override
    public boolean getGeneratesBlockArea() {
        return true;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public boolean isRestartable() {
        return true;
    }

}

