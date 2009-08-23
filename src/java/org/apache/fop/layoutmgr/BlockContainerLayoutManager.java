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
import java.util.ListIterator;

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
import org.apache.fop.util.ListUtil;

/**
 * LayoutManager for a block-container FO.
 */
public class BlockContainerLayoutManager extends BlockStackingLayoutManager
                implements ConditionalElementListener {

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


    /**
     * Create a new block container layout manager.
     * @param node block-container node to create the layout manager for.
     */
    public BlockContainerLayoutManager(BlockContainer node) {
        super(node);
    }

    /** {@inheritDoc} */
    public void initialize() {
        abProps = getBlockContainerFO().getCommonAbsolutePosition();
        foBlockSpaceBefore = new SpaceVal(getBlockContainerFO().getCommonMarginBlock()
                    .spaceBefore, this).getSpace();
        foBlockSpaceAfter = new SpaceVal(getBlockContainerFO().getCommonMarginBlock()
                    .spaceAfter, this).getSpace();
        startIndent = getBlockContainerFO().getCommonMarginBlock().startIndent.getValue(this);
        endIndent = getBlockContainerFO().getCommonMarginBlock().endIndent.getValue(this);

        boolean rotated = (getBlockContainerFO().getReferenceOrientation() % 180 != 0);
        if (rotated) {
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

        bpUnit = 0; //layoutProps.blockProgressionUnit;
        if (bpUnit == 0) {
            // use optimum space values
            adjustedSpaceBefore = getBlockContainerFO().getCommonMarginBlock()
                .spaceBefore.getSpace().getOptimum(this).getLength().getValue(this);
            adjustedSpaceAfter = getBlockContainerFO().getCommonMarginBlock()
                .spaceAfter.getSpace().getOptimum(this).getLength().getValue(this);
        } else {
            // use minimum space values
            adjustedSpaceBefore = getBlockContainerFO().getCommonMarginBlock()
                .spaceBefore.getSpace().getMinimum(this).getLength().getValue(this);
            adjustedSpaceAfter = getBlockContainerFO().getCommonMarginBlock()
                .spaceAfter.getSpace().getMinimum(this).getLength().getValue(this);
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

    /** @return the content IPD */
    protected int getRotatedIPD() {
        return getBlockContainerFO().getInlineProgressionDimension()
                .getOptimum(this).getLength().getValue(this);
    }

    private boolean needClip() {
        int overflow = getBlockContainerFO().getOverflow();
        return (overflow == EN_HIDDEN || overflow == EN_ERROR_IF_OVERFLOW);
    }

    private int getSpaceBefore() {
        return foBlockSpaceBefore.opt;
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
        return (abProps.absolutePosition == EN_ABSOLUTE)
                || (abProps.absolutePosition == EN_FIXED);
    }

    private boolean isFixed() {
        return (abProps.absolutePosition == EN_FIXED);
    }

    /** {@inheritDoc} */
    public int getContentAreaBPD() {
        if (autoHeight) {
            return -1;
        } else {
            return this.vpContentBPD;
        }
    }

    /** {@inheritDoc} */
    public List getNextKnuthElements(LayoutContext context, int alignment) {
        resetSpaces();
        if (isAbsoluteOrFixed()) {
            return getNextKnuthElementsAbsolute(context, alignment);
        }

        autoHeight = false;
        //boolean rotated = (getBlockContainerFO().getReferenceOrientation() % 180 != 0);
        int maxbpd = context.getStackLimitBP().opt;
        int allocBPD;
        if (height.getEnum() == EN_AUTO
                || (!height.isAbsolute() && getAncestorBlockAreaBPD() <= 0)) {
            //auto height when height="auto" or "if that dimension is not specified explicitly
            //(i.e., it depends on content's block-progression-dimension)" (XSL 1.0, 7.14.1)
            allocBPD = maxbpd;
            autoHeight = true;
            if (getBlockContainerFO().getReferenceOrientation() == 0) {
                //Cannot easily inline element list when ref-or="180"
                inlineElementList = true;
            }
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

        double contentRectOffsetX = 0;
        contentRectOffsetX += getBlockContainerFO()
                .getCommonMarginBlock().startIndent.getValue(this);
        double contentRectOffsetY = 0;
        contentRectOffsetY += getBlockContainerFO()
                .getCommonBorderPaddingBackground().getBorderBeforeWidth(false);
        contentRectOffsetY += getBlockContainerFO()
                .getCommonBorderPaddingBackground().getPaddingBefore(false, this);

        updateRelDims(contentRectOffsetX, contentRectOffsetY, autoHeight);

        int availableIPD = referenceIPD - getIPIndents();
        if (getContentAreaIPD() > availableIPD) {
            BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(
                    getBlockContainerFO().getUserAgent().getEventBroadcaster());
            eventProducer.objectTooWide(this, getBlockContainerFO().getName(),
                    getContentAreaIPD(), context.getRefIPD(),
                    getBlockContainerFO().getLocator());
        }

        MinOptMax stackLimit = new MinOptMax(relDims.bpd);

        List returnedList;
        List contentList = new LinkedList();
        List returnList = new LinkedList();

        if (!breakBeforeServed) {
            breakBeforeServed = true;
            if (!context.suppressBreakBefore()) {
                if (addKnuthElementsForBreakBefore(returnList, context)) {
                    return returnList;
                }
            }
        }

        if (!firstVisibleMarkServed) {
            addKnuthElementsForSpaceBefore(returnList, alignment);
            context.updateKeepWithPreviousPending(getKeepWithPrevious());
        }

        addKnuthElementsForBorderPaddingBefore(returnList, !firstVisibleMarkServed);
        firstVisibleMarkServed = true;

        if (autoHeight && inlineElementList) {
            //Spaces, border and padding to be repeated at each break
            addPendingMarks(context);

            LayoutManager curLM; // currently active LM
            LayoutManager prevLM = null; // previously active LM
            while ((curLM = getChildLM()) != null) {
                LayoutContext childLC = new LayoutContext(0);
                childLC.copyPendingMarksFrom(context);
                // curLM is a ?
                childLC.setStackLimitBP(MinOptMax.subtract(context.getStackLimitBP(), stackLimit));
                childLC.setRefIPD(relDims.ipd);
                childLC.setWritingMode(getBlockContainerFO().getWritingMode());
                if (curLM == this.childLMs.get(0)) {
                    childLC.setFlags(LayoutContext.SUPPRESS_BREAK_BEFORE);
                    //Handled already by the parent (break collapsing, see above)
                }

                // get elements from curLM
                returnedList = curLM.getNextKnuthElements(childLC, alignment);
                if (contentList.isEmpty() && childLC.isKeepWithPreviousPending()) {
                    //Propagate keep-with-previous up from the first child
                    context.updateKeepWithPreviousPending(childLC.getKeepWithPreviousPending());
                    childLC.clearKeepWithPreviousPending();
                }
                if (returnedList.size() == 1
                        && ((ListElement)returnedList.get(0)).isForcedBreak()) {
                    // a descendant of this block has break-before
                    /*
                    if (returnList.size() == 0) {
                        // the first child (or its first child ...) has
                        // break-before;
                        // all this block, including space before, will be put in
                        // the
                        // following page
                        bSpaceBeforeServed = false;
                    }*/
                    contentList.addAll(returnedList);

                    // "wrap" the Position inside each element
                    // moving the elements from contentList to returnList
                    returnedList = new LinkedList();
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
                        if (curLM.isFinished()) {
                            // there is no other content in this block;
                            // it's useless to add space after before a page break
                            setFinished(true);
                        }

                        returnedList = new LinkedList();
                        wrapPositionElements(contentList, returnList);

                        return returnList;
                    }
                }
                // propagate and clear
                context.updateKeepWithNextPending(childLC.getKeepWithNextPending());
                childLC.clearKeepsPending();
                prevLM = curLM;
            }

            returnedList = new LinkedList();
            wrapPositionElements(contentList, returnList);

        } else {
            MinOptMax range = new MinOptMax(relDims.ipd);
            BlockContainerBreaker breaker = new BlockContainerBreaker(this, range);
            breaker.doLayout(relDims.bpd, autoHeight);
            boolean contentOverflows = breaker.isOverflow();
            if (autoHeight) {
                //Update content BPD now that it is known
                int newHeight = breaker.deferredAlg.totalWidth;
                boolean switchedProgressionDirection
                    = (getBlockContainerFO().getReferenceOrientation() % 180 != 0);
                if (switchedProgressionDirection) {
                    setContentAreaIPD(newHeight);
                } else {
                    vpContentBPD = newHeight;
                }
                updateRelDims(contentRectOffsetX, contentRectOffsetY, false);
            }

            Position bcPosition = new BlockContainerPosition(this, breaker);
            returnList.add(new KnuthBox(vpContentBPD, notifyPos(bcPosition), false));
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
                eventProducer.viewportOverflow(this, getBlockContainerFO().getName(),
                        breaker.getOverflowAmount(), needClip(), canRecover,
                        getBlockContainerFO().getLocator());
            }
        }
        addKnuthElementsForBorderPaddingAfter(returnList, true);
        addKnuthElementsForSpaceAfter(returnList, alignment);

        //All child content is processed. Only break-after can occur now, so...
        context.clearPendingMarks();
        addKnuthElementsForBreakAfter(returnList, context);

        context.updateKeepWithNextPending(getKeepWithNext());

        setFinished(true);
        return returnList;
    }

    private List getNextKnuthElementsAbsolute(LayoutContext context, int alignment) {
        autoHeight = false;

        boolean switchedProgressionDirection
            = (getBlockContainerFO().getReferenceOrientation() % 180 != 0);
        Point offset = getAbsOffset();
        int allocBPD, allocIPD;
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
                    availHeight = context.getStackLimitBP().opt;
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
                allocBPD = context.getStackLimitBP().opt;
                if (!switchedProgressionDirection) {
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
                if (switchedProgressionDirection) {
                    autoHeight = true;
                }
            }
        } else {
            allocIPD = width.getValue(this); //this is the content-width
            allocIPD += getIPIndents();
        }

        vpContentBPD = allocBPD - getBPIndents();
        setContentAreaIPD(allocIPD - getIPIndents());

        updateRelDims(0, 0, autoHeight);

        MinOptMax range = new MinOptMax(relDims.ipd);
        BlockContainerBreaker breaker = new BlockContainerBreaker(this, range);
        breaker.doLayout((autoHeight ? 0 : relDims.bpd), autoHeight);
        boolean contentOverflows = breaker.isOverflow();
        if (autoHeight) {
            //Update content BPD now that it is known
            int newHeight = breaker.deferredAlg.totalWidth;
            if (switchedProgressionDirection) {
                setContentAreaIPD(newHeight);
            } else {
                vpContentBPD = newHeight;
            }
            updateRelDims(0, 0, false);
        }
        List returnList = new LinkedList();
        if (!breaker.isEmpty()) {
            Position bcPosition = new BlockContainerPosition(this, breaker);
            returnList.add(new KnuthBox(0, notifyPos(bcPosition), false));

            //TODO Maybe check for page overflow when autoHeight=true
            if (!autoHeight & (contentOverflows)) {
                BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(
                        getBlockContainerFO().getUserAgent().getEventBroadcaster());
                boolean canRecover = (getBlockContainerFO().getOverflow() != EN_ERROR_IF_OVERFLOW);
                eventProducer.viewportOverflow(this, getBlockContainerFO().getName(),
                        breaker.getOverflowAmount(), needClip(), canRecover,
                        getBlockContainerFO().getLocator());
            }
        }

        setFinished(true);
        return returnList;
    }

    private void updateRelDims(double xOffset, double yOffset, boolean skipAutoHeight) {
        Rectangle2D rect = new Rectangle2D.Double(
                xOffset, yOffset,
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
            PageBreakPosition pbp = (PageBreakPosition)this.deferredAlg.getPageBreaks().getFirst();
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
            lc.setRefIPD(ipd.opt);
            lc.setWritingMode(getBlockContainerFO().getWritingMode());
            return lc;
        }

        protected List getNextKnuthElements(LayoutContext context, int alignment) {
            LayoutManager curLM; // currently active LM
            List returnList = new LinkedList();

            while ((curLM = getChildLM()) != null) {
                LayoutContext childLC = new LayoutContext(0);
                childLC.setStackLimitBP(context.getStackLimitBP());
                childLC.setRefIPD(context.getRefIPD());
                childLC.setWritingMode(getBlockContainerFO().getWritingMode());

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

        public void addContainedAreas() {
            if (isEmpty()) {
                return;
            }
            //Rendering all parts (not just the first) at once for the case where the parts that
            //overflow should be visible.
            this.deferredAlg.removeAllPageBreaks();
            this.addAreas(this.deferredAlg,
                          this.deferredAlg.getPageBreaks().size(),
                          this.deferredOriginalList, this.deferredEffectiveList);
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
    public void addAreas(PositionIterator parentIter,
            LayoutContext layoutContext) {
        getParentArea(null);

        // if this will create the first block area in a page
        // and display-align is bottom or center, add space before
        if (layoutContext.getSpaceBefore() > 0) {
            addBlockSpacing(0.0, new MinOptMax(layoutContext.getSpaceBefore()));
        }

        LayoutManager childLM;
        LayoutManager lastLM = null;
        LayoutContext lc = new LayoutContext(0);
        lc.setSpaceAdjust(layoutContext.getSpaceAdjust());
        // set space after in the LayoutContext for children
        if (layoutContext.getSpaceAfter() > 0) {
            lc.setSpaceAfter(layoutContext.getSpaceAfter());
        }
        BlockContainerPosition bcpos = null;
        PositionIterator childPosIter;

        // "unwrap" the NonLeafPositions stored in parentIter
        // and put them in a new list;
        List positionList = new LinkedList();
        Position pos;
        boolean bSpaceBefore = false;
        boolean bSpaceAfter = false;
        Position firstPos = null;
        Position lastPos = null;
        while (parentIter.hasNext()) {
            pos = (Position) parentIter.next();
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
                if (pos instanceof NonLeafPosition) {
                    // pos was created by this BCLM and was inside an element
                    // representing space before or after
                    // this means the space was not discarded
                    if (positionList.isEmpty() && bcpos == null) {
                        // pos was in the element representing space-before
                        bSpaceBefore = true;
                    } else {
                        // pos was in the element representing space-after
                        bSpaceAfter = true;
                    }
                } else {
                    //ignore (probably a Position for a simple penalty between blocks)
                }
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

        addMarkersToPage(true, isFirst(firstPos), isLast(lastPos));

        if (bcpos == null) {
            if (bpUnit == 0) {
                // the Positions in positionList were inside the elements
                // created by the LineLM
                childPosIter = new StackingIter(positionList.listIterator());
            } else {
                // the Positions in positionList were inside the elements
                // created by the BCLM in the createUnitElements() method
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
                List splitList = new LinkedList();
                int splitLength = 0;
                int iFirst = ((MappingPosition) positionList.get(0))
                        .getFirstIndex();
                int iLast = ((MappingPosition) ListUtil.getLast(positionList))
                        .getLastIndex();
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
                    foBlockSpaceBefore = new SpaceVal(getBlockContainerFO()
                                .getCommonMarginBlock().spaceBefore, this).getSpace();
                    foBlockSpaceAfter = new SpaceVal(getBlockContainerFO()
                                .getCommonMarginBlock().spaceAfter, this).getSpace();
                    adjustedSpaceBefore = (neededUnits(splitLength
                            + foBlockSpaceBefore.min
                            + foBlockSpaceAfter.min)
                            * bpUnit - splitLength) / 2;
                    adjustedSpaceAfter = neededUnits(splitLength
                            + foBlockSpaceBefore.min
                            + foBlockSpaceAfter.min)
                            * bpUnit - splitLength - adjustedSpaceBefore;
                } else if (bSpaceBefore) {
                    adjustedSpaceBefore = neededUnits(splitLength
                            + foBlockSpaceBefore.min)
                            * bpUnit - splitLength;
                } else {
                    adjustedSpaceAfter = neededUnits(splitLength
                            + foBlockSpaceAfter.min)
                            * bpUnit - splitLength;
                }
                //log.debug("space before = " + adjustedSpaceBefore
                      // + " space after = " + adjustedSpaceAfter + " total = " +
                      // (adjustedSpaceBefore + adjustedSpaceAfter + splitLength));
                childPosIter = new KnuthPossPosIter(splitList, 0, splitList
                        .size());
                //}
            }

            while ((childLM = childPosIter.getNextChildLM()) != null) {
                // set last area flag
                lc.setFlags(LayoutContext.LAST_AREA,
                        (layoutContext.isLastArea() && childLM == lastLM));
                /*LF*/lc.setStackLimitBP(layoutContext.getStackLimitBP());
                // Add the line areas to Area
                childLM.addAreas(childPosIter, lc);
            }
        } else {
            //Add child areas inside the reference area
            bcpos.getBreaker().addContainedAreas();
        }

        addMarkersToPage(false, isFirst(firstPos), isLast(lastPos));

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
    public Area getParentArea(Area childArea) {
        if (referenceArea == null) {
            boolean switchedProgressionDirection
                = (getBlockContainerFO().getReferenceOrientation() % 180 != 0);
            boolean allowBPDUpdate = autoHeight && !switchedProgressionDirection;

            viewportBlockArea = new BlockViewport(allowBPDUpdate);
            viewportBlockArea.addTrait(Trait.IS_VIEWPORT_AREA, Boolean.TRUE);

            viewportBlockArea.setIPD(getContentAreaIPD());
            if (allowBPDUpdate) {
                viewportBlockArea.setBPD(0);
            } else {
                viewportBlockArea.setBPD(this.vpContentBPD);
            }
            transferForeignAttributes(viewportBlockArea);

            TraitSetter.setProducerID(viewportBlockArea, getBlockContainerFO().getId());
            TraitSetter.addBorders(viewportBlockArea,
                    getBlockContainerFO().getCommonBorderPaddingBackground(),
                    discardBorderBefore, discardBorderAfter, false, false, this);
            TraitSetter.addPadding(viewportBlockArea,
                    getBlockContainerFO().getCommonBorderPaddingBackground(),
                    discardPaddingBefore, discardPaddingAfter, false, false, this);
            // TraitSetter.addBackground(viewportBlockArea,
            //        getBlockContainerFO().getCommonBorderPaddingBackground(),
            //        this);
            TraitSetter.addMargins(viewportBlockArea,
                    getBlockContainerFO().getCommonBorderPaddingBackground(),
                    startIndent, endIndent,
                    this);

            viewportBlockArea.setCTM(absoluteCTM);
            viewportBlockArea.setClip(needClip());
            /*
            if (getSpaceBefore() != 0) {
                viewportBlockArea.addTrait(Trait.SPACE_BEFORE, new Integer(getSpaceBefore()));
            }
            if (foBlockSpaceAfter.opt != 0) {
                viewportBlockArea.addTrait(Trait.SPACE_AFTER, new Integer(foBlockSpaceAfter.opt));
            }*/

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
            TraitSetter.setProducerID(referenceArea, getBlockContainerFO().getId());

            if (abProps.absolutePosition == EN_ABSOLUTE) {
                viewportBlockArea.setPositioning(Block.ABSOLUTE);
            } else if (abProps.absolutePosition == EN_FIXED) {
                viewportBlockArea.setPositioning(Block.FIXED);
            }

            // Set up dimensions
            // Must get dimensions from parent area
            /*Area parentArea =*/ parentLM.getParentArea(referenceArea);
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
    public void addChildArea(Area childArea) {
        if (referenceArea != null) {
            referenceArea.addBlock((Block) childArea);
        }
    }

    /**
     * Force current area to be added to parent area.
     * {@inheritDoc}
     */
    protected void flush() {
        viewportBlockArea.addBlock(referenceArea, autoHeight);

        TraitSetter.addBackground(viewportBlockArea,
                getBlockContainerFO().getCommonBorderPaddingBackground(),
                this);

        super.flush();
    }

    /** {@inheritDoc} */
    public int negotiateBPDAdjustment(int adj, KnuthElement lastElement) {
        // TODO Auto-generated method stub
        return 0;
    }

    /** {@inheritDoc} */
    public void discardSpace(KnuthGlue spaceGlue) {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public KeepProperty getKeepTogetherProperty() {
        return getBlockContainerFO().getKeepTogether();
    }

    /** {@inheritDoc} */
    public KeepProperty getKeepWithPreviousProperty() {
        return getBlockContainerFO().getKeepWithPrevious();
    }

    /** {@inheritDoc} */
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
    public boolean getGeneratesReferenceArea() {
        return true;
    }

    /** {@inheritDoc} */
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

}


