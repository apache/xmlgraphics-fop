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

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.CTM;
import org.apache.fop.area.Trait;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.properties.CommonAbsolutePosition;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;

/**
 * LayoutManager for a block-container FO.
 */
public class BlockContainerLayoutManager extends BlockStackingLayoutManager implements
        ConditionalElementListener, BreakOpportunity {

    /**
     * logging instance
     */
    private static Log log = LogFactory.getLog(BlockContainerLayoutManager.class);

    private BlockViewport viewportBlockArea;
    private Block referenceArea;

    private CommonAbsolutePosition abProps;
    private FODimension relDims;
    private CTM absoluteCTM;
    private Length width;
    private Length height;
    //private int vpContentIPD;
    private int vpContentBPD;

    // When viewport should grow with the content.
    private boolean autoHeight = true;
    private boolean inlineElementList = false;

    /* holds the (one-time use) fo:block space-before
    and -after properties.  Large fo:blocks are split
    into multiple Area.Blocks to accomodate the subsequent
    regions (pages) they are placed on.  space-before
    is applied at the beginning of the first
    Block and space-after at the end of the last Block
    used in rendering the fo:block.
    */
    //TODO space-before|after: handle space-resolution rules
    private MinOptMax foBlockSpaceBefore;
    private MinOptMax foBlockSpaceAfter;

    private boolean discardBorderBefore;
    private boolean discardBorderAfter;
    private boolean discardPaddingBefore;
    private boolean discardPaddingAfter;
    private MinOptMax effSpaceBefore;
    private MinOptMax effSpaceAfter;

    private int horizontalOverflow;
    private double contentRectOffsetX = 0;
    private double contentRectOffsetY = 0;

    /**
     * Create a new block container layout manager.
     * @param node block-container node to create the layout manager for.
     */
    public BlockContainerLayoutManager(BlockContainer node) {
        super(node);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize() {
        abProps = getBlockContainerFO().getCommonAbsolutePosition();
        foBlockSpaceBefore = new SpaceVal(getBlockContainerFO().getCommonMarginBlock()
                    .spaceBefore, this).getSpace();
        foBlockSpaceAfter = new SpaceVal(getBlockContainerFO().getCommonMarginBlock()
                    .spaceAfter, this).getSpace();
        startIndent = getBlockContainerFO().getCommonMarginBlock().startIndent.getValue(this);
        endIndent = getBlockContainerFO().getCommonMarginBlock().endIndent.getValue(this);

        if (blockProgressionDirectionChanges()) {
            height = getBlockContainerFO().getInlineProgressionDimension()
                            .getOptimum(this).getLength();
            width = getBlockContainerFO().getBlockProgressionDimension()
                            .getOptimum(this).getLength();
        } else {
            height = getBlockContainerFO().getBlockProgressionDimension()
                            .getOptimum(this).getLength();
            width = getBlockContainerFO().getInlineProgressionDimension()
                            .getOptimum(this).getLength();
        }

        // use optimum space values
        adjustedSpaceBefore = getBlockContainerFO().getCommonMarginBlock()
            .spaceBefore.getSpace().getOptimum(this).getLength().getValue(this);
        adjustedSpaceAfter = getBlockContainerFO().getCommonMarginBlock()
            .spaceAfter.getSpace().getOptimum(this).getLength().getValue(this);
    }

    private void resetSpaces() {
        this.discardBorderBefore = false;
        this.discardBorderAfter = false;
        this.discardPaddingBefore = false;
        this.discardPaddingAfter = false;
        this.effSpaceBefore = null;
        this.effSpaceAfter = null;
    }

    /** @return the content IPD */
    protected int getRotatedIPD() {
        return getBlockContainerFO().getInlineProgressionDimension()
                .getOptimum(this).getLength().getValue(this);
    }

    private boolean needClip() {
        int overflow = getBlockContainerFO().getOverflow();
        return (overflow == EN_HIDDEN || overflow == EN_ERROR_IF_OVERFLOW);
    }

    private int getBPIndents() {
        int indents = 0;
        /* TODO This is wrong isn't it?
        indents += getBlockContainerFO().getCommonMarginBlock()
                    .spaceBefore.getOptimum(this).getLength().getValue(this);
        indents += getBlockContainerFO().getCommonMarginBlock()
                    .spaceAfter.getOptimum(this).getLength().getValue(this);
        */
        indents += getBlockContainerFO().getCommonBorderPaddingBackground()
                    .getBPPaddingAndBorder(false, this);
        return indents;
    }

    private boolean isAbsoluteOrFixed() {
        return (abProps.absolutePosition == EN_ABSOLUTE
                || abProps.absolutePosition == EN_FIXED);
    }

    private boolean isFixed() {
        return (abProps.absolutePosition == EN_FIXED);
    }

    /** {@inheritDoc} */
    @Override
    public int getContentAreaBPD() {
        if (autoHeight) {
            return -1;
        } else {
            return this.vpContentBPD;
        }
    }

    /** {@inheritDoc} */
    @Override
    public List getNextKnuthElements(LayoutContext context, int alignment) {
        return getNextKnuthElements(context, alignment, null, null, null);
    }

    /**
     * Overridden to handle writing-mode, and different stack limit
     * setup.
     * {@inheritDoc}
     */
    @Override
    protected LayoutContext makeChildLayoutContext(LayoutContext context) {
        LayoutContext childLC = LayoutContext.newInstance();
        childLC.setStackLimitBP(
                context.getStackLimitBP().minus(MinOptMax.getInstance(relDims.bpd)));
        childLC.setRefIPD(relDims.ipd);
        childLC.setWritingMode(getBlockContainerFO().getWritingMode());
        return childLC;
    }

    /** {@inheritDoc} */
    @Override
    public List getNextKnuthElements(LayoutContext context, int alignment, Stack lmStack,
        Position restartPosition, LayoutManager restartAtLM) {

        resetSpaces();
        // special treatment for position="absolute|fixed"
        if (isAbsoluteOrFixed()) {
            return getNextKnuthElementsAbsolute(context);
        }

        boolean isRestart = (lmStack != null);
        boolean emptyStack = (!isRestart || lmStack.isEmpty());

        setupAreaDimensions(context);

        List<ListElement> returnedList;
        List<ListElement> contentList = new LinkedList<ListElement>();
        List<ListElement> returnList = new LinkedList<ListElement>();

        if (!breakBeforeServed(context, returnList)) {
            return returnList;
        }

        addFirstVisibleMarks(returnList, context, alignment);

        if (autoHeight && inlineElementList) {

            LayoutManager curLM; // currently active LM
            LayoutManager prevLM = null; // previously active LM

            LayoutContext childLC;
            if (isRestart) {
                if (emptyStack) {
                    assert restartAtLM != null && restartAtLM.getParent() == this;
                    curLM = restartAtLM;
                } else {
                    curLM = (LayoutManager) lmStack.pop();
                }
                setCurrentChildLM(curLM);
            } else {
                curLM = getChildLM();
            }

            while (curLM != null) {
                childLC = makeChildLayoutContext(context);

                // get elements from curLM
                if (!isRestart || emptyStack) {
                    if (isRestart) {
                        curLM.reset();
                    }
                    returnedList = getNextChildElements(curLM, context, childLC, alignment,
                            null, null, null);
                } else {
                    returnedList = getNextChildElements(curLM, context, childLC, alignment,
                            lmStack, restartPosition, restartAtLM);
                    // once encountered, irrelevant for following child LMs
                    emptyStack = true;
                }
                if (contentList.isEmpty() && childLC.isKeepWithPreviousPending()) {
                    //Propagate keep-with-previous up from the first child
                    context.updateKeepWithPreviousPending(childLC.getKeepWithPreviousPending());
                    childLC.clearKeepWithPreviousPending();
                }
                if (returnedList.size() == 1
                        && ElementListUtils.startsWithForcedBreak(returnedList)) {
                    // a descendant of this block has break-before
                    contentList.addAll(returnedList);

                    // "wrap" the Position inside each element
                    // moving the elements from contentList to returnList
                    wrapPositionElements(contentList, returnList);

                    return returnList;
                } else {
                    if (prevLM != null) {
                        // there is a block handled by prevLM
                        // before the one handled by curLM
                        addInBetweenBreak(contentList, context, childLC);
                    }
                    contentList.addAll(returnedList);
                    if (returnedList.isEmpty()) {
                        //Avoid NoSuchElementException below (happens with empty blocks)
                        continue;
                    }
                    if (ElementListUtils.endsWithForcedBreak(returnedList)) {
                        // a descendant of this block has break-after
                        if (curLM.isFinished() && !hasNextChildLM()) {
                            // there is no other content in this block;
                            // it's useless to add space after before a page break
                            setFinished(true);
                        }

                        wrapPositionElements(contentList, returnList);
                        return returnList;
                    }
                }
                // propagate and clear
                context.updateKeepWithNextPending(childLC.getKeepWithNextPending());
                childLC.clearKeepsPending();
                prevLM = curLM;
                curLM = getChildLM();
            }
            wrapPositionElements(contentList, returnList);
        } else {
            returnList.add(generateNonInlinedBox());
        }

        addLastVisibleMarks(returnList, context, alignment);

        addKnuthElementsForBreakAfter(returnList, context);

        context.updateKeepWithNextPending(getKeepWithNext());

        setFinished(true);
        return returnList;
    }

    private void setupAreaDimensions(LayoutContext context) {
        autoHeight = false;
        int maxbpd = context.getStackLimitBP().getOpt();
        int allocBPD;
        BlockContainer fo = getBlockContainerFO();
        if (height.getEnum() == EN_AUTO
                || (!height.isAbsolute() && getAncestorBlockAreaBPD() <= 0)) {
            //auto height when height="auto" or "if that dimension is not specified explicitly
            //(i.e., it depends on content's block-progression-dimension)" (XSL 1.0, 7.14.1)
            allocBPD = maxbpd;
            autoHeight = true;
            //Cannot easily inline element list when ref-or<>"0"
            inlineElementList = (fo.getReferenceOrientation() == 0);
        } else {
            allocBPD = height.getValue(this); //this is the content-height
            allocBPD += getBPIndents();
        }
        vpContentBPD = allocBPD - getBPIndents();

        referenceIPD = context.getRefIPD();
        if (width.getEnum() == EN_AUTO) {
            updateContentAreaIPDwithOverconstrainedAdjust();
        } else {
            int contentWidth = width.getValue(this);
            updateContentAreaIPDwithOverconstrainedAdjust(contentWidth);
        }

        contentRectOffsetX = 0;
        contentRectOffsetY = 0;

        int level = fo.getBidiLevel();
        if ((level < 0) || ((level & 1) == 0)) {
            contentRectOffsetX += fo.getCommonMarginBlock().startIndent.getValue(this);
        } else {
            contentRectOffsetX += fo.getCommonMarginBlock().endIndent.getValue(this);
        }
        contentRectOffsetY += fo.getCommonBorderPaddingBackground().getBorderBeforeWidth(false);
        contentRectOffsetY += fo.getCommonBorderPaddingBackground().getPaddingBefore(false, this);

        updateRelDims();

        int availableIPD = referenceIPD - getIPIndents();
        if (getContentAreaIPD() > availableIPD) {
            BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(
                    fo.getUserAgent().getEventBroadcaster());
            eventProducer.objectTooWide(this, fo.getName(),
                    getContentAreaIPD(), context.getRefIPD(),
                    fo.getLocator());
        }
    }

    private KnuthBox generateNonInlinedBox() {

        MinOptMax range = MinOptMax.getInstance(relDims.ipd);
        BlockContainerBreaker breaker = new BlockContainerBreaker(this, range);
        breaker.doLayout(relDims.bpd, autoHeight);
        boolean contentOverflows = breaker.isOverflow();
        if (autoHeight) {
            //Update content BPD now that it is known
            int newHeight = breaker.deferredAlg.totalWidth;
            if (blockProgressionDirectionChanges()) {
                setContentAreaIPD(newHeight);
            } else {
                vpContentBPD = newHeight;
            }
            updateRelDims();
        }

        Position bcPosition = new BlockContainerPosition(this, breaker);
        KnuthBox knuthBox = new KnuthBox(vpContentBPD, notifyPos(bcPosition), false);
        //TODO Handle min/opt/max for block-progression-dimension
        /* These two elements will be used to add stretchability to the above box
        returnList.add(new KnuthPenalty(0, KnuthElement.INFINITE,
                               false, returnPosition, false));
        returnList.add(new KnuthGlue(0, 1 * constantLineHeight, 0,
                               LINE_NUMBER_ADJUSTMENT, returnPosition, false));
        */

        if (contentOverflows) {
            BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(
                    getBlockContainerFO().getUserAgent().getEventBroadcaster());
            boolean canRecover = (getBlockContainerFO().getOverflow() != EN_ERROR_IF_OVERFLOW);
            eventProducer.viewportBPDOverflow(this, getBlockContainerFO().getName(),
                    breaker.getOverflowAmount(), needClip(), canRecover,
                    getBlockContainerFO().getLocator());
        }
        return knuthBox;
    }

    private boolean blockProgressionDirectionChanges() {
        return getBlockContainerFO().getReferenceOrientation() % 180 != 0;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRestartable() {
        return true;
    }

    private List<ListElement> getNextKnuthElementsAbsolute(LayoutContext context) {
        autoHeight = false;

        boolean bpDirectionChanges = blockProgressionDirectionChanges();
        Point offset = getAbsOffset();
        int allocBPD;
        int allocIPD;
        if (height.getEnum() == EN_AUTO
                || (!height.isAbsolute() && getAncestorBlockAreaBPD() <= 0)) {
            //auto height when height="auto" or "if that dimension is not specified explicitly
            //(i.e., it depends on content's blockprogression-dimension)" (XSL 1.0, 7.14.1)
            allocBPD = 0;
            if (abProps.bottom.getEnum() != EN_AUTO) {
                int availHeight;
                if (isFixed()) {
                    availHeight = (int)getCurrentPV().getViewArea().getHeight();
                } else {
                    availHeight = context.getStackLimitBP().getOpt();
                }
                allocBPD = availHeight;
                allocBPD -= offset.y;
                if (abProps.bottom.getEnum() != EN_AUTO) {
                    allocBPD -= abProps.bottom.getValue(this);
                    if (allocBPD < 0) {
                        //TODO Fix absolute b-c layout, layout may need to be defferred until
                        //after page breaking when the size of the containing box is known.
                        /* Warning disabled due to a interpretation mistake.
                         * See: http://marc.theaimsgroup.com/?l=fop-dev&m=113189981926163&w=2
                        log.error("The current combination of top and bottom properties results"
                                + " in a negative extent for the block-container. 'bottom' may be"
                                + " at most " + (allocBPD + abProps.bottom.getValue(this)) + " mpt,"
                                + " but was actually " + abProps.bottom.getValue(this) + " mpt."
                                + " The nominal available height is " + availHeight + " mpt.");
                        */
                        allocBPD = 0;
                    }
                } else {
                    if (allocBPD < 0) {
                        /* Warning disabled due to a interpretation mistake.
                         * See: http://marc.theaimsgroup.com/?l=fop-dev&m=113189981926163&w=2
                        log.error("The current combination of top and bottom properties results"
                                + " in a negative extent for the block-container. 'top' may be"
                                + " at most " + availHeight + " mpt,"
                                + " but was actually " + offset.y + " mpt."
                                + " The nominal available height is " + availHeight + " mpt.");
                        */
                        allocBPD = 0;
                    }
                }
            } else {
                allocBPD = context.getStackLimitBP().getOpt();
                if (!bpDirectionChanges) {
                    autoHeight = true;
                }
            }
        } else {
            allocBPD = height.getValue(this); //this is the content-height
            allocBPD += getBPIndents();
        }
        if (width.getEnum() == EN_AUTO) {
            int availWidth;
            if (isFixed()) {
                availWidth = (int)getCurrentPV().getViewArea().getWidth();
            } else {
                availWidth = context.getRefIPD();
            }
            allocIPD = availWidth;
            if (abProps.left.getEnum() != EN_AUTO) {
                allocIPD -= abProps.left.getValue(this);
            }
            if (abProps.right.getEnum() != EN_AUTO) {
                allocIPD -= abProps.right.getValue(this);
                if (allocIPD < 0) {
                    /* Warning disabled due to a interpretation mistake.
                     * See: http://marc.theaimsgroup.com/?l=fop-dev&m=113189981926163&w=2
                    log.error("The current combination of left and right properties results"
                            + " in a negative extent for the block-container. 'right' may be"
                            + " at most " + (allocIPD + abProps.right.getValue(this)) + " mpt,"
                            + " but was actually " + abProps.right.getValue(this) + " mpt."
                            + " The nominal available width is " + availWidth + " mpt.");
                    */
                    allocIPD = 0;
                }
            } else {
                if (allocIPD < 0) {
                    /* Warning disabled due to a interpretation mistake.
                     * See: http://marc.theaimsgroup.com/?l=fop-dev&m=113189981926163&w=2
                    log.error("The current combination of left and right properties results"
                            + " in a negative extent for the block-container. 'left' may be"
                            + " at most " + allocIPD + " mpt,"
                            + " but was actually " + abProps.left.getValue(this) + " mpt."
                            + " The nominal available width is " + availWidth + " mpt.");
                    */
                    allocIPD = 0;
                }
                if (bpDirectionChanges) {
                    autoHeight = true;
                }
            }
        } else {
            allocIPD = width.getValue(this); //this is the content-width
            allocIPD += getIPIndents();
        }

        vpContentBPD = allocBPD - getBPIndents();
        setContentAreaIPD(allocIPD - getIPIndents());

        contentRectOffsetX = 0;
        contentRectOffsetY = 0;
        updateRelDims();

        MinOptMax range = MinOptMax.getInstance(relDims.ipd);
        BlockContainerBreaker breaker = new BlockContainerBreaker(this, range);
        breaker.doLayout((autoHeight ? 0 : relDims.bpd), autoHeight);
        boolean contentOverflows = breaker.isOverflow();
        if (autoHeight) {
            //Update content BPD now that it is known
            int newHeight = breaker.deferredAlg.totalWidth;
            if (bpDirectionChanges) {
                setContentAreaIPD(newHeight);
            } else {
                vpContentBPD = newHeight;
            }
            updateRelDims();
        }
        List<ListElement> returnList = new LinkedList<ListElement>();
        if (!breaker.isEmpty()) {
            Position bcPosition = new BlockContainerPosition(this, breaker);
            returnList.add(new KnuthBox(0, notifyPos(bcPosition), false));

            //TODO Maybe check for page overflow when autoHeight=true
            if (!autoHeight & (contentOverflows)) {
                BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(
                        getBlockContainerFO().getUserAgent().getEventBroadcaster());
                boolean canRecover = (getBlockContainerFO().getOverflow() != EN_ERROR_IF_OVERFLOW);
                eventProducer.viewportBPDOverflow(this, getBlockContainerFO().getName(),
                        breaker.getOverflowAmount(), needClip(), canRecover,
                        getBlockContainerFO().getLocator());
            }
            // this handles the IPD (horizontal) overflow
            if (this.horizontalOverflow > 0) {
                BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider
                        .get(getBlockContainerFO().getUserAgent().getEventBroadcaster());
                boolean canRecover = (getBlockContainerFO().getOverflow() != EN_ERROR_IF_OVERFLOW);
                eventProducer.viewportIPDOverflow(this, getBlockContainerFO().getName(),
                        this.horizontalOverflow, needClip(), canRecover, getBlockContainerFO().getLocator());
            }
        }

        setFinished(true);
        return returnList;
    }

    private void updateRelDims() {
        Rectangle2D rect = new Rectangle2D.Double(
                contentRectOffsetX, contentRectOffsetY,
                getContentAreaIPD(),
                this.vpContentBPD);
        relDims = new FODimension(0, 0);
        absoluteCTM = CTM.getCTMandRelDims(
                getBlockContainerFO().getReferenceOrientation(),
                getBlockContainerFO().getWritingMode(),
                rect, relDims);
    }

    private class BlockContainerPosition extends NonLeafPosition {

        private BlockContainerBreaker breaker;

        public BlockContainerPosition(LayoutManager lm, BlockContainerBreaker breaker) {
            super(lm, null);
            this.breaker = breaker;
        }

        public BlockContainerBreaker getBreaker() {
            return this.breaker;
        }

    }

    private class BlockContainerBreaker extends AbstractBreaker {

        private BlockContainerLayoutManager bclm;
        private MinOptMax ipd;

        //Info for deferred adding of areas
        private PageBreakingAlgorithm deferredAlg;
        private BlockSequence deferredOriginalList;
        private BlockSequence deferredEffectiveList;

        public BlockContainerBreaker(BlockContainerLayoutManager bclm, MinOptMax ipd) {
            this.bclm = bclm;
            this.ipd = ipd;
        }

        /** {@inheritDoc} */
        protected void observeElementList(List elementList) {
            ElementListObserver.observe(elementList, "block-container",
                    bclm.getBlockContainerFO().getId());
        }

        /** {@inheritDoc} */
        protected boolean isPartOverflowRecoveryActivated() {
            //For block-containers, this must be disabled because of wanted overflow.
            return false;
        }

        /** {@inheritDoc} */
        protected boolean isSinglePartFavored() {
            return true;
        }

        public int getDifferenceOfFirstPart() {
            PageBreakPosition pbp = this.deferredAlg.getPageBreaks().getFirst();
            return pbp.difference;
        }

        public boolean isOverflow() {
            return !isEmpty()
                    && ((deferredAlg.getPageBreaks().size() > 1)
                        || (deferredAlg.totalWidth - deferredAlg.totalShrink)
                            > deferredAlg.getLineWidth());
        }

        public int getOverflowAmount() {
            return (deferredAlg.totalWidth - deferredAlg.totalShrink)
                - deferredAlg.getLineWidth();
        }

        protected LayoutManager getTopLevelLM() {
            return bclm;
        }

        protected LayoutContext createLayoutContext() {
            LayoutContext lc = super.createLayoutContext();
            lc.setRefIPD(ipd.getOpt());
            lc.setWritingMode(getBlockContainerFO().getWritingMode());
            return lc;
        }

        protected List getNextKnuthElements(LayoutContext context, int alignment) {
            LayoutManager curLM; // currently active LM
            List<ListElement> returnList = new LinkedList<ListElement>();

            while ((curLM = getChildLM()) != null) {
                LayoutContext childLC = makeChildLayoutContext(context);

                List returnedList = null;
                if (!curLM.isFinished()) {
                    returnedList = curLM.getNextKnuthElements(childLC, alignment);
                }
                if (returnedList != null) {
                    bclm.wrapPositionElements(returnedList, returnList);
                }
            }
            SpaceResolver.resolveElementList(returnList);
            setFinished(true);
            return returnList;
        }

        protected int getCurrentDisplayAlign() {
            return getBlockContainerFO().getDisplayAlign();
        }

        protected boolean hasMoreContent() {
            return !isFinished();
        }

        protected void addAreas(PositionIterator posIter, LayoutContext context) {
            AreaAdditionUtil.addAreas(bclm, posIter, context);
        }

        protected void doPhase3(PageBreakingAlgorithm alg, int partCount,
                BlockSequence originalList, BlockSequence effectiveList) {
            //Defer adding of areas until addAreas is called by the parent LM
            this.deferredAlg = alg;
            this.deferredOriginalList = originalList;
            this.deferredEffectiveList = effectiveList;
        }

        protected void finishPart(PageBreakingAlgorithm alg, PageBreakPosition pbp) {
            //nop for bclm
        }

        protected LayoutManager getCurrentChildLM() {
            return curChildLM;
        }

        public void addContainedAreas(LayoutContext layoutContext) {
            if (isEmpty()) {
                return;
            }
            //Rendering all parts (not just the first) at once for the case where the parts that
            //overflow should be visible.
            this.deferredAlg.removeAllPageBreaks();
            this.addAreas(this.deferredAlg,
                          0,
                          this.deferredAlg.getPageBreaks().size(),
                          this.deferredOriginalList, this.deferredEffectiveList,
                          LayoutContext.offspringOf(layoutContext));
        }

    }

    private Point getAbsOffset() {
        int x = 0;
        int y = 0;
        if (abProps.left.getEnum() != EN_AUTO) {
            x = abProps.left.getValue(this);
        } else if (abProps.right.getEnum() != EN_AUTO
                && width.getEnum() != EN_AUTO) {
            x = getReferenceAreaIPD()
                - abProps.right.getValue(this) - width.getValue(this);
        }
        if (abProps.top.getEnum() != EN_AUTO) {
            y = abProps.top.getValue(this);
        } else if (abProps.bottom.getEnum() != EN_AUTO
                && height.getEnum() != EN_AUTO) {
            y = getReferenceAreaBPD()
                - abProps.bottom.getValue(this) - height.getValue(this);
        }
        return new Point(x, y);
    }

    /** {@inheritDoc} */
    @Override
    public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
        getParentArea(null);

        // if this will create the first block area in a page
        // and display-align is bottom or center, add space before
        if (layoutContext.getSpaceBefore() > 0) {
            addBlockSpacing(0.0, MinOptMax.getInstance(layoutContext.getSpaceBefore()));
        }

        LayoutManager childLM;
        LayoutManager lastLM = null;
        LayoutContext lc = LayoutContext.offspringOf(layoutContext);
        lc.setSpaceAdjust(layoutContext.getSpaceAdjust());
        // set space after in the LayoutContext for children
        if (layoutContext.getSpaceAfter() > 0) {
            lc.setSpaceAfter(layoutContext.getSpaceAfter());
        }
        BlockContainerPosition bcpos = null;
        PositionIterator childPosIter;

        // "unwrap" the NonLeafPositions stored in parentIter
        // and put them in a new list;
        List<Position> positionList = new LinkedList<Position>();
        Position pos;
        Position firstPos = null;
        Position lastPos = null;
        while (parentIter.hasNext()) {
            pos = parentIter.next();
            if (pos.getIndex() >= 0) {
                if (firstPos == null) {
                    firstPos = pos;
                }
                lastPos = pos;
            }
            Position innerPosition = pos;
            if (pos instanceof NonLeafPosition) {
                innerPosition = pos.getPosition();
            }
            if (pos instanceof BlockContainerPosition) {
                if (bcpos != null) {
                    throw new IllegalStateException("Only one BlockContainerPosition allowed");
                }
                bcpos = (BlockContainerPosition)pos;
                //Add child areas inside the reference area
                //bcpos.getBreaker().addContainedAreas();
            } else if (innerPosition == null) {
                //ignore (probably a Position for a simple penalty between blocks)
            } else if (innerPosition.getLM() == this
                    && !(innerPosition instanceof MappingPosition)) {
                // pos was created by this BlockLM and was inside a penalty
                // allowing or forbidding a page break
                // nothing to do
            } else {
                // innerPosition was created by another LM
                positionList.add(innerPosition);
                lastLM = innerPosition.getLM();
            }
        }

        addId();

        registerMarkers(true, isFirst(firstPos), isLast(lastPos));

        if (bcpos == null) {
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
        } else {
            //Add child areas inside the reference area
            bcpos.getBreaker().addContainedAreas(layoutContext);
        }

        registerMarkers(false, isFirst(firstPos), isLast(lastPos));

        TraitSetter.addSpaceBeforeAfter(viewportBlockArea, layoutContext.getSpaceAdjust(),
                effSpaceBefore, effSpaceAfter);
        flush();

        viewportBlockArea = null;
        referenceArea = null;
        resetSpaces();

        notifyEndOfLayout();
    }

    /**
     * Get the parent area for children of this block container.
     * This returns the current block container area
     * and creates it if required.
     *
     * {@inheritDoc}
     */
    @Override
    public Area getParentArea(Area childArea) {
        if (referenceArea == null) {
            boolean switchedProgressionDirection = blockProgressionDirectionChanges();
            boolean allowBPDUpdate = autoHeight && !switchedProgressionDirection;
            int level = getBlockContainerFO().getBidiLevel();

            viewportBlockArea = new BlockViewport(allowBPDUpdate);
            viewportBlockArea.addTrait(Trait.IS_VIEWPORT_AREA, Boolean.TRUE);
            if (level >= 0) {
                viewportBlockArea.setBidiLevel(level);
            }
            viewportBlockArea.setIPD(getContentAreaIPD());
            if (allowBPDUpdate) {
                viewportBlockArea.setBPD(0);
            } else {
                viewportBlockArea.setBPD(this.vpContentBPD);
            }
            transferForeignAttributes(viewportBlockArea);

            TraitSetter.setProducerID(viewportBlockArea, getBlockContainerFO().getId());
            TraitSetter.setLayer(viewportBlockArea, getBlockContainerFO().getLayer());
            TraitSetter.addBorders(viewportBlockArea,
                    getBlockContainerFO().getCommonBorderPaddingBackground(),
                    discardBorderBefore, discardBorderAfter, false, false, this);
            TraitSetter.addPadding(viewportBlockArea,
                    getBlockContainerFO().getCommonBorderPaddingBackground(),
                    discardPaddingBefore, discardPaddingAfter, false, false, this);
            TraitSetter.addMargins(viewportBlockArea,
                    getBlockContainerFO().getCommonBorderPaddingBackground(),
                    startIndent, endIndent,
                    this);

            viewportBlockArea.setCTM(absoluteCTM);
            viewportBlockArea.setClip(needClip());

            if (abProps.absolutePosition == EN_ABSOLUTE
                    || abProps.absolutePosition == EN_FIXED) {
                Point offset = getAbsOffset();
                viewportBlockArea.setXOffset(offset.x);
                viewportBlockArea.setYOffset(offset.y);
            } else {
                //nop
            }

            referenceArea = new Block();
            referenceArea.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
            if (level >= 0) {
                referenceArea.setBidiLevel(level);
            }
            TraitSetter.setProducerID(referenceArea, getBlockContainerFO().getId());

            if (abProps.absolutePosition == EN_ABSOLUTE) {
                viewportBlockArea.setPositioning(Block.ABSOLUTE);
            } else if (abProps.absolutePosition == EN_FIXED) {
                viewportBlockArea.setPositioning(Block.FIXED);
            }

            // Set up dimensions
            // Must get dimensions from parent area
            /*Area parentArea =*/ parentLayoutManager.getParentArea(referenceArea);
            //int referenceIPD = parentArea.getIPD();
            referenceArea.setIPD(relDims.ipd);
            // Get reference IPD from parentArea
            setCurrentArea(viewportBlockArea); // ??? for generic operations
        }
        return referenceArea;
    }

    /**
     * Add the child to the block container.
     *
     * {@inheritDoc}
     */
    @Override
    public void addChildArea(Area childArea) {
        if (referenceArea != null) {
            referenceArea.addBlock((Block) childArea);
        }
    }

    /**
     * Force current area to be added to parent area.
     * {@inheritDoc}
     */
    @Override
    protected void flush() {
        viewportBlockArea.addBlock(referenceArea, autoHeight);

        TraitSetter.addBackground(viewportBlockArea,
                getBlockContainerFO().getCommonBorderPaddingBackground(),
                this);

        super.flush();
    }

    /** {@inheritDoc} */
    @Override
    public int negotiateBPDAdjustment(int adj, KnuthElement lastElement) {
        // TODO Auto-generated method stub
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public void discardSpace(KnuthGlue spaceGlue) {
        // TODO Auto-generated method stub
    }

    /** {@inheritDoc} */
    @Override
    public KeepProperty getKeepTogetherProperty() {
        return getBlockContainerFO().getKeepTogether();
    }

    /** {@inheritDoc} */
    @Override
    public KeepProperty getKeepWithPreviousProperty() {
        return getBlockContainerFO().getKeepWithPrevious();
    }

    /** {@inheritDoc} */
    @Override
    public KeepProperty getKeepWithNextProperty() {
        return getBlockContainerFO().getKeepWithNext();
    }

    /**
     * @return the BlockContainer node
     */
    protected BlockContainer getBlockContainerFO() {
        return (BlockContainer) fobj;
    }

    // --------- Property Resolution related functions --------- //

    /** {@inheritDoc} */
    @Override
    public boolean getGeneratesReferenceArea() {
        return true;
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
    public boolean handleOverflow(int milliPoints) {
        if (milliPoints > this.horizontalOverflow) {
            this.horizontalOverflow = milliPoints;
        }
        return true;
    }

}


