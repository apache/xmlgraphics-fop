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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.fo.Constants;
import org.apache.fop.layoutmgr.BreakingAlgorithm.KnuthNode;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.util.ListUtil;

/**
 * Abstract base class for breakers (page breakers, static region handlers etc.).
 */
public abstract class AbstractBreaker {

    /** logging instance */
    protected static final Log log = LogFactory.getLog(AbstractBreaker.class);

    /**
     * A page break position.
     */
    public static class PageBreakPosition extends LeafPosition {
        // Percentage to adjust (stretch or shrink)
        double bpdAdjust;                                       // CSOK: VisibilityModifier
        int difference;                                         // CSOK: VisibilityModifier
        int footnoteFirstListIndex;                             // CSOK: VisibilityModifier
        int footnoteFirstElementIndex;                          // CSOK: VisibilityModifier
        int footnoteLastListIndex;                              // CSOK: VisibilityModifier
        int footnoteLastElementIndex;                           // CSOK: VisibilityModifier

        PageBreakPosition(LayoutManager lm, int breakIndex,     // CSOK: ParameterNumber
                          int ffli, int ffei, int flli, int flei,
                          double bpdA, int diff) {
            super(lm, breakIndex);
            bpdAdjust = bpdA;
            difference = diff;
            footnoteFirstListIndex = ffli;
            footnoteFirstElementIndex = ffei;
            footnoteLastListIndex = flli;
            footnoteLastElementIndex = flei;
        }
    }

    /**
     * Helper method, mainly used to improve debug/trace output
     * @param breakClassId  the {@link Constants} enum value.
     * @return the break class name
     */
    static String getBreakClassName(int breakClassId) {
        switch (breakClassId) {
        case Constants.EN_ALL: return "ALL";
        case Constants.EN_ANY: return "ANY";
        case Constants.EN_AUTO: return "AUTO";
        case Constants.EN_COLUMN: return "COLUMN";
        case Constants.EN_EVEN_PAGE: return "EVEN PAGE";
        case Constants.EN_LINE: return "LINE";
        case Constants.EN_NONE: return "NONE";
        case Constants.EN_ODD_PAGE: return "ODD PAGE";
        case Constants.EN_PAGE: return "PAGE";
        default: return "??? (" + String.valueOf(breakClassId) + ")";
        }
    }

    /**
     * Helper class, extending the functionality of the
     * basic {@link BlockKnuthSequence}.
     */
    public class BlockSequence extends BlockKnuthSequence {

        private static final long serialVersionUID = -5348831120146774118L;

        /** Number of elements to ignore at the beginning of the list. */
        int ignoreAtStart = 0;                                  // CSOK: VisibilityModifier
        /** Number of elements to ignore at the end of the list. */
        int ignoreAtEnd = 0;                                    // CSOK: VisibilityModifier

        /**
         * startOn represents where on the page/which page layout
         * should start for this BlockSequence.  Acceptable values:
         * Constants.EN_ANY (can continue from finished location
         * of previous BlockSequence?), EN_COLUMN, EN_ODD_PAGE,
         * EN_EVEN_PAGE.
         */
        private int startOn;

        private int displayAlign;

        /**
         * Creates a new BlockSequence.
         * @param  startOn  the kind of page the sequence should start on.
         *                  One of {@link Constants#EN_ANY}, {@link Constants#EN_COLUMN},
         *                  {@link Constants#EN_ODD_PAGE}, or {@link Constants#EN_EVEN_PAGE}.
         * @param displayAlign the value for the display-align property
         */
        public BlockSequence(int startOn, int displayAlign) {
            super();
            this.startOn =  startOn;
            this.displayAlign = displayAlign;
        }

        /**
         * @return the kind of page the sequence should start on.
         *         One of {@link Constants#EN_ANY}, {@link Constants#EN_COLUMN},
         *         {@link Constants#EN_ODD_PAGE}, or {@link Constants#EN_EVEN_PAGE}.
         */
        public int getStartOn() {
            return this.startOn;
        }

        /** @return the value for the display-align property */
        public int getDisplayAlign() {
            return this.displayAlign;
        }

        /**
         * Finalizes a Knuth sequence.
         * @return a finalized sequence.
         */
        public KnuthSequence endSequence() {
            return endSequence(null);
        }

        /**
         * Finalizes a Knuth sequence.
         * @param breakPosition a Position instance for the last penalty (may be null)
         * @return a finalized sequence.
         */
        public KnuthSequence endSequence(Position breakPosition) {
            // remove glue and penalty item at the end of the paragraph
            while (this.size() > ignoreAtStart
                    && !((KnuthElement) ListUtil.getLast(this)).isBox()) {
                ListUtil.removeLast(this);
            }
            if (this.size() > ignoreAtStart) {
                // add the elements representing the space at the end of the last line
                // and the forced break
                if (getDisplayAlign() == Constants.EN_X_DISTRIBUTE && isSinglePartFavored()) {
                    this.add(new KnuthPenalty(0, -KnuthElement.INFINITE,
                                false, breakPosition, false));
                    ignoreAtEnd = 1;
                } else {
                    this.add(new KnuthPenalty(0, KnuthElement.INFINITE,
                                false, null, false));
                    this.add(new KnuthGlue(0, 10000000, 0, null, false));
                    this.add(new KnuthPenalty(0, -KnuthElement.INFINITE,
                                false, breakPosition, false));
                    ignoreAtEnd = 3;
                }
                return this;
            } else {
                this.clear();
                return null;
            }
        }

        /**
         * Finalizes a this {@link BlockSequence}, adding a terminating
         * penalty-glue-penalty sequence
         * @param breakPosition a Position instance pointing to the last penalty
         * @return the finalized {@link BlockSequence}
         */
        public BlockSequence endBlockSequence(Position breakPosition) {
            KnuthSequence temp = endSequence(breakPosition);
            if (temp != null) {
                BlockSequence returnSequence = new BlockSequence(startOn, displayAlign);
                returnSequence.addAll(temp);
                returnSequence.ignoreAtEnd = this.ignoreAtEnd;
                return returnSequence;
            } else {
                return null;
            }
        }

    }

    // used by doLayout and getNextBlockList*
    private List<BlockSequence> blockLists;

    private boolean empty = true;

    /** desired text alignment */
    protected int alignment;

    private int alignmentLast;

    /** footnote separator length */
    protected MinOptMax footnoteSeparatorLength = MinOptMax.ZERO;

    /** @return current display alignment */
    protected abstract int getCurrentDisplayAlign();

    /** @return true if content not exhausted */
    protected abstract boolean hasMoreContent();

    /**
     * Tell the layout manager to add all the child areas implied
     * by Position objects which will be returned by the
     * Iterator.
     *
     * @param posIter the position iterator
     * @param context the context
     */
    protected abstract void addAreas(PositionIterator posIter, LayoutContext context);

    /** @return top level layout manager */
    protected abstract LayoutManager getTopLevelLM();

    /** @return current child layout manager */
    protected abstract LayoutManager getCurrentChildLM();

    /**
     * Controls the behaviour of the algorithm in cases where the first element of a part
     * overflows a line/page.
     * @return true if the algorithm should try to send the element to the next line/page.
     */
    protected boolean isPartOverflowRecoveryActivated() {
        return true;
    }

    /**
     * @return true if one a single part should be produced if possible (ex. for block-containers)
     */
    protected boolean isSinglePartFavored() {
        return false;
    }

    /**
     * Returns the PageProvider if any. PageBreaker overrides this method because each
     * page may have a different available BPD which needs to be accessible to the breaking
     * algorithm.
     * @return the applicable PageProvider, or null if not applicable
     */
    protected PageProvider getPageProvider() {
        return null;
    }

    /**
     * Creates and returns a PageBreakingLayoutListener for the PageBreakingAlgorithm to
     * notify about layout problems.
     * @return the listener instance or null if no notifications are needed
     */
    protected PageBreakingAlgorithm.PageBreakingLayoutListener createLayoutListener() {
        return null;
    }

    /**
     * Get a sequence of KnuthElements representing the content
     * of the node assigned to the LM
     *
     * @param context   the LayoutContext used to store layout information
     * @param alignment the desired text alignment
     * @return          the list of KnuthElements
     */
    protected abstract List<KnuthElement> getNextKnuthElements(LayoutContext context,
                                                               int alignment);

    /**
     * Get a sequence of KnuthElements representing the content
     * of the node assigned to the LM
     *
     * @param context   the LayoutContext used to store layout information
     * @param alignment the desired text alignment
     * @param positionAtIPDChange last element on the part before an IPD change
     * @param restartAtLM the layout manager from which to restart, if IPD
     * change occurs between two LMs
     * @return          the list of KnuthElements
     */
    protected List<KnuthElement> getNextKnuthElements(LayoutContext context, int alignment,
            Position positionAtIPDChange, LayoutManager restartAtLM) {
        throw new UnsupportedOperationException("TODO: implement acceptable fallback");
    }

    /** @return true if there's no content that could be handled. */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * Start part.
     * @param list a block sequence
     * @param breakClass a break class
     */
    protected void startPart(BlockSequence list, int breakClass) {
        //nop
    }

    /**
     * This method is called when no content is available for a part. Used to force empty pages.
     */
    protected void handleEmptyContent() {
        //nop
    }

    /**
     * Finish part.
     * @param alg a page breaking algorithm
     * @param pbp a page break posittion
     */
    protected abstract void finishPart(PageBreakingAlgorithm alg, PageBreakPosition pbp);

    /**
     * Creates the top-level LayoutContext for the breaker operation.
     * @return the top-level LayoutContext
     */
    protected LayoutContext createLayoutContext() {
        return new LayoutContext(0);
    }

    /**
     * Used to update the LayoutContext in subclasses prior to starting a new element list.
     * @param context the LayoutContext to update
     */
    protected void updateLayoutContext(LayoutContext context) {
        //nop
    }

    /**
     * Used for debugging purposes. Notifies all registered observers about the element list.
     * Override to set different parameters.
     * @param elementList the Knuth element list
     */
    protected void observeElementList(List elementList) {
        ElementListObserver.observe(elementList, "breaker", null);
    }

    /**
     * Starts the page breaking process.
     * @param flowBPD the constant available block-progression-dimension (used for every part)
     * @param autoHeight true if warnings about overflows should be disabled because the
     *                   the BPD is really undefined (for footnote-separators, for example)
     */
    public void doLayout(int flowBPD, boolean autoHeight) {
        LayoutContext childLC = createLayoutContext();
        childLC.setStackLimitBP(MinOptMax.getInstance(flowBPD));

        if (getCurrentDisplayAlign() == Constants.EN_X_FILL) {
            //EN_X_FILL is non-standard (by LF)
            alignment = Constants.EN_JUSTIFY;
        } else if (getCurrentDisplayAlign() == Constants.EN_X_DISTRIBUTE) {
            //EN_X_DISTRIBUTE is non-standard (by LF)
            alignment = Constants.EN_JUSTIFY;
        } else {
            alignment = Constants.EN_START;
        }
        alignmentLast = Constants.EN_START;
        if (isSinglePartFavored() && alignment == Constants.EN_JUSTIFY) {
            alignmentLast = Constants.EN_JUSTIFY;
        }
        childLC.setBPAlignment(alignment);

        BlockSequence blockList;
        blockLists = new java.util.ArrayList<BlockSequence>();

        log.debug("PLM> flow BPD =" + flowBPD);

        int nextSequenceStartsOn = Constants.EN_ANY;
        while (hasMoreContent()) {
            blockLists.clear();

            //*** Phase 1: Get Knuth elements ***
            nextSequenceStartsOn = getNextBlockList(childLC, nextSequenceStartsOn);
            empty = empty && blockLists.size() == 0;

            //*** Phases 2 and 3 ***
            log.debug("PLM> blockLists.size() = " + blockLists.size());
            for (int blockListIndex = 0; blockListIndex < blockLists.size(); blockListIndex++) {
                blockList = blockLists.get(blockListIndex);

                //debug code start
                if (log.isDebugEnabled()) {
                    log.debug("  blockListIndex = " + blockListIndex);
                    log.debug("  sequence starts on " + getBreakClassName(blockList.startOn));
                }
                observeElementList(blockList);
                //debug code end

                //*** Phase 2: Alignment and breaking ***
                log.debug("PLM> start of algorithm (" + this.getClass().getName()
                        + "), flow BPD =" + flowBPD);
                PageBreakingAlgorithm alg = new PageBreakingAlgorithm(getTopLevelLM(),
                         getPageProvider(), createLayoutListener(),
                         alignment, alignmentLast, footnoteSeparatorLength,
                         isPartOverflowRecoveryActivated(), autoHeight, isSinglePartFavored());

                BlockSequence effectiveList;
                if (getCurrentDisplayAlign() == Constants.EN_X_FILL) {
                    /* justification */
                    effectiveList = justifyBoxes(blockList, alg, flowBPD);
                } else {
                    /* no justification */
                    effectiveList = blockList;
                }

                alg.setConstantLineWidth(flowBPD);
                int optimalPageCount = alg.findBreakingPoints(effectiveList, 1, true,
                        BreakingAlgorithm.ALL_BREAKS);
                if (alg.getIPDdifference() != 0) {
                    addAreas(alg, optimalPageCount, blockList, effectiveList);
                    // *** redo Phase 1 ***
                    log.trace("IPD changes after page " + optimalPageCount);
                    blockLists.clear();
                    nextSequenceStartsOn = getNextBlockListChangedIPD(childLC, alg,
                                                                      effectiveList);
                    blockListIndex = -1;
                } else {
                    log.debug("PLM> optimalPageCount= " + optimalPageCount
                            + " pageBreaks.size()= " + alg.getPageBreaks().size());

                    //*** Phase 3: Add areas ***
                    doPhase3(alg, optimalPageCount, blockList, effectiveList);
                }
            }
        }

        // done
        blockLists = null;
    }

    /**
     * Returns {@code true} if the given position or one of its descendants
     * corresponds to a non-restartable LM.
     *
     * @param position a position
     * @return {@code true} if there is a non-restartable LM in the hierarchy
     */
    private boolean containsNonRestartableLM(Position position) {
        LayoutManager lm = position.getLM();
        if (lm != null && !lm.isRestartable()) {
            return true;
        } else {
            Position subPosition = position.getPosition();
            return subPosition != null && containsNonRestartableLM(subPosition);
        }
    }

    /**
     * Phase 3 of Knuth algorithm: Adds the areas
     * @param alg PageBreakingAlgorithm instance which determined the breaks
     * @param partCount number of parts (pages) to be rendered
     * @param originalList original Knuth element list
     * @param effectiveList effective Knuth element list (after adjustments)
     */
    protected abstract void doPhase3(PageBreakingAlgorithm alg, int partCount,
            BlockSequence originalList, BlockSequence effectiveList);

    /**
     * Phase 3 of Knuth algorithm: Adds the areas
     * @param alg PageBreakingAlgorithm instance which determined the breaks
     * @param partCount number of parts (pages) to be rendered
     * @param originalList original Knuth element list
     * @param effectiveList effective Knuth element list (after adjustments)
     */
    protected void addAreas(PageBreakingAlgorithm alg, int partCount,
            BlockSequence originalList, BlockSequence effectiveList) {
        addAreas(alg, 0, partCount, originalList, effectiveList);
    }

    /**
     * Phase 3 of Knuth algorithm: Adds the areas
     * @param alg PageBreakingAlgorithm instance which determined the breaks
     * @param startPart index of the first part (page) to be rendered
     * @param partCount number of parts (pages) to be rendered
     * @param originalList original Knuth element list
     * @param effectiveList effective Knuth element list (after adjustments)
     */
    protected void addAreas(PageBreakingAlgorithm alg, int startPart, int partCount,
            BlockSequence originalList, BlockSequence effectiveList) {
        LayoutContext childLC;
        // add areas
        int startElementIndex = 0;
        int endElementIndex = 0;
        int lastBreak = -1;
        for (int p = startPart; p < startPart + partCount; p++) {
            PageBreakPosition pbp = alg.getPageBreaks().get(p);

            //Check the last break position for forced breaks
            int lastBreakClass;
            if (p == 0) {
                lastBreakClass = effectiveList.getStartOn();
            } else {
                ListElement lastBreakElement = effectiveList.getElement(endElementIndex);
                if (lastBreakElement.isPenalty()) {
                    KnuthPenalty pen = (KnuthPenalty)lastBreakElement;
                    lastBreakClass = pen.getBreakClass();
                } else {
                    lastBreakClass = Constants.EN_COLUMN;
                }
            }

            //the end of the new part
            endElementIndex = pbp.getLeafPos();

            // ignore the first elements added by the
            // PageSequenceLayoutManager
            startElementIndex += (startElementIndex == 0)
                    ? effectiveList.ignoreAtStart
                    : 0;

            log.debug("PLM> part: " + (p + 1)
                    + ", start at pos " + startElementIndex
                    + ", break at pos " + endElementIndex
                    + ", break class = " + getBreakClassName(lastBreakClass));

            startPart(effectiveList, lastBreakClass);

            int displayAlign = getCurrentDisplayAlign();

            //The following is needed by SpaceResolver.performConditionalsNotification()
            //further down as there may be important Position elements in the element list trailer
            int notificationEndElementIndex = endElementIndex;

            // ignore the last elements added by the
            // PageSequenceLayoutManager
            endElementIndex -= (endElementIndex == (originalList.size() - 1))
                    ? effectiveList.ignoreAtEnd
                    : 0;

            // ignore the last element in the page if it is a KnuthGlue
            // object
            if (((KnuthElement) effectiveList.get(endElementIndex))
                    .isGlue()) {
                endElementIndex--;
            }

            // ignore KnuthGlue and KnuthPenalty objects
            // at the beginning of the line
            ListIterator<KnuthElement> effectiveListIterator
                = effectiveList.listIterator(startElementIndex);
            while (effectiveListIterator.hasNext()
                    && !(effectiveListIterator.next()).isBox()) {
                startElementIndex++;
            }

            if (startElementIndex <= endElementIndex) {
                if (log.isDebugEnabled()) {
                    log.debug("     addAreas from " + startElementIndex
                            + " to " + endElementIndex);
                }
                childLC = new LayoutContext(0);
                // set the space adjustment ratio
                childLC.setSpaceAdjust(pbp.bpdAdjust);
                // add space before if display-align is center or bottom
                // add space after if display-align is distribute and
                // this is not the last page
                if (pbp.difference != 0 && displayAlign == Constants.EN_CENTER) {
                    childLC.setSpaceBefore(pbp.difference / 2);
                } else if (pbp.difference != 0 && displayAlign == Constants.EN_AFTER) {
                    childLC.setSpaceBefore(pbp.difference);
                } else if (pbp.difference != 0 && displayAlign == Constants.EN_X_DISTRIBUTE
                        && p < (partCount - 1)) {
                    // count the boxes whose width is not 0
                    int boxCount = 0;
                    effectiveListIterator = effectiveList.listIterator(startElementIndex);
                    while (effectiveListIterator.nextIndex() <= endElementIndex) {
                        KnuthElement tempEl = effectiveListIterator.next();
                        if (tempEl.isBox() && tempEl.getWidth() > 0) {
                            boxCount++;
                        }
                    }
                    // split the difference
                    if (boxCount >= 2) {
                        childLC.setSpaceAfter(pbp.difference / (boxCount - 1));
                    }
                }

                /* *** *** non-standard extension *** *** */
                if (displayAlign == Constants.EN_X_FILL) {
                    int averageLineLength = optimizeLineLength(effectiveList,
                            startElementIndex, endElementIndex);
                    if (averageLineLength != 0) {
                        childLC.setStackLimitBP(MinOptMax.getInstance(averageLineLength));
                    }
                }
                /* *** *** non-standard extension *** *** */

                // Handle SpaceHandling(Break)Positions, see SpaceResolver!
                SpaceResolver.performConditionalsNotification(effectiveList,
                        startElementIndex, notificationEndElementIndex, lastBreak);

                // Add areas now!
                addAreas(new KnuthPossPosIter(effectiveList,
                        startElementIndex, endElementIndex + 1), childLC);
            } else {
                //no content for this part
                handleEmptyContent();
            }

            finishPart(alg, pbp);

            lastBreak = endElementIndex;
            startElementIndex = pbp.getLeafPos() + 1;
        }
    }
    /**
     * Notifies the layout managers about the space and conditional length situation based on
     * the break decisions.
     * @param effectiveList Element list to be painted
     * @param startElementIndex start index of the part
     * @param endElementIndex end index of the part
     * @param lastBreak index of the last break element
     */
    /**
     * Handles span changes reported through the <code>LayoutContext</code>.
     * Only used by the PSLM and called by <code>getNextBlockList()</code>.
     * @param childLC the LayoutContext
     * @param nextSequenceStartsOn previous value for break handling
     * @return effective value for break handling
     */
    protected int handleSpanChange(LayoutContext childLC, int nextSequenceStartsOn) {
        return nextSequenceStartsOn;
    }

    /**
     * Gets the next block list (sequence) and adds it to a list of block lists if it's not empty.
     * @param childLC LayoutContext to use
     * @param nextSequenceStartsOn indicates on what page the next sequence should start
     * @return the page on which the next content should appear after a hard break
     */
    protected int getNextBlockList(LayoutContext childLC, int nextSequenceStartsOn) {
        return getNextBlockList(childLC, nextSequenceStartsOn, null, null, null);
    }

    /**
     * Gets the next block list (sequence) and adds it to a list of block lists
     * if it's not empty.
     *
     * @param childLC LayoutContext to use
     * @param nextSequenceStartsOn indicates on what page the next sequence
     * should start
     * @param positionAtIPDChange last element on the part before an IPD change
     * @param restartAtLM the layout manager from which to restart, if IPD
     * change occurs between two LMs
     * @param firstElements elements from non-restartable LMs on the new page
     * @return the page on which the next content should appear after a hard
     * break
     */
    protected int getNextBlockList(LayoutContext childLC, int nextSequenceStartsOn,
            Position positionAtIPDChange, LayoutManager restartAtLM,
            List<KnuthElement> firstElements) {
        updateLayoutContext(childLC);
        //Make sure the span change signal is reset
        childLC.signalSpanChange(Constants.NOT_SET);

        BlockSequence blockList;
        List<KnuthElement> returnedList;
        if (firstElements == null) {
            returnedList = getNextKnuthElements(childLC, alignment);
        } else if (positionAtIPDChange == null) {
            /*
             * No restartable element found after changing IPD break. Simply add the
             * non-restartable elements found after the break.
             */
            returnedList = firstElements;
            /*
             * Remove the last 3 penalty-filler-forced break elements that were added by
             * the Knuth algorithm. They will be re-added later on.
             */
            ListIterator iter = returnedList.listIterator(returnedList.size());
            for (int i = 0; i < 3; i++) {
                iter.previous();
                iter.remove();
            }
        } else {
            returnedList = getNextKnuthElements(childLC, alignment, positionAtIPDChange,
                    restartAtLM);
            returnedList.addAll(0, firstElements);
        }
        if (returnedList != null) {
            if (returnedList.isEmpty()) {
                nextSequenceStartsOn = handleSpanChange(childLC, nextSequenceStartsOn);
                return nextSequenceStartsOn;
            }
            blockList = new BlockSequence(nextSequenceStartsOn, getCurrentDisplayAlign());

            //Only implemented by the PSLM
            nextSequenceStartsOn = handleSpanChange(childLC, nextSequenceStartsOn);

            Position breakPosition = null;
            if (ElementListUtils.endsWithForcedBreak(returnedList)) {
                KnuthPenalty breakPenalty = (KnuthPenalty) ListUtil
                        .removeLast(returnedList);
                breakPosition = breakPenalty.getPosition();
                log.debug("PLM> break - " + getBreakClassName(breakPenalty.getBreakClass()));
                switch (breakPenalty.getBreakClass()) {
                case Constants.EN_PAGE:
                    nextSequenceStartsOn = Constants.EN_ANY;
                    break;
                case Constants.EN_COLUMN:
                    //TODO Fix this when implementing multi-column layout
                    nextSequenceStartsOn = Constants.EN_COLUMN;
                    break;
                case Constants.EN_ODD_PAGE:
                    nextSequenceStartsOn = Constants.EN_ODD_PAGE;
                    break;
                case Constants.EN_EVEN_PAGE:
                    nextSequenceStartsOn = Constants.EN_EVEN_PAGE;
                    break;
                default:
                    throw new IllegalStateException("Invalid break class: "
                            + breakPenalty.getBreakClass());
                }
            }
            blockList.addAll(returnedList);
            BlockSequence seq;
            seq = blockList.endBlockSequence(breakPosition);
            if (seq != null) {
                blockLists.add(seq);
            }
        }
        return nextSequenceStartsOn;
    }

    /**
     * @param childLC LayoutContext to use
     * @param alg the pagebreaking algorithm
     * @param effectiveList the list of Knuth elements to be reused
     * @return the page on which the next content should appear after a hard break
     */
    private int getNextBlockListChangedIPD(LayoutContext childLC, PageBreakingAlgorithm alg,
                    BlockSequence effectiveList) {
        int nextSequenceStartsOn;
        KnuthNode optimalBreak = alg.getBestNodeBeforeIPDChange();
        int positionIndex = optimalBreak.position;
        log.trace("IPD changes at index " + positionIndex);
        KnuthElement elementAtBreak = alg.getElement(positionIndex);
        Position positionAtBreak = elementAtBreak.getPosition();
        if (!(positionAtBreak instanceof SpaceResolver.SpaceHandlingBreakPosition)) {
            throw new UnsupportedOperationException(
                    "Don't know how to restart at position " + positionAtBreak);
        }
        /* Retrieve the original position wrapped into this space position */
        positionAtBreak = positionAtBreak.getPosition();
        LayoutManager restartAtLM = null;
        List<KnuthElement> firstElements = Collections.emptyList();
        if (containsNonRestartableLM(positionAtBreak)) {
            if (alg.getIPDdifference() > 0) {
                EventBroadcaster eventBroadcaster = getCurrentChildLM().getFObj()
                        .getUserAgent().getEventBroadcaster();
                BlockLevelEventProducer eventProducer
                        = BlockLevelEventProducer.Provider.get(eventBroadcaster);
                eventProducer.nonRestartableContentFlowingToNarrowerPage(this);
            }
            firstElements = new LinkedList<KnuthElement>();
            boolean boxFound = false;
            Iterator<KnuthElement> iter = effectiveList.listIterator(positionIndex + 1);
            Position position = null;
            while (iter.hasNext()
                    && (position == null || containsNonRestartableLM(position))) {
                positionIndex++;
                KnuthElement element = iter.next();
                position = element.getPosition();
                if (element.isBox()) {
                    boxFound = true;
                    firstElements.add(element);
                } else if (boxFound) {
                    firstElements.add(element);
                }
            }
            if (position instanceof SpaceResolver.SpaceHandlingBreakPosition) {
                /* Retrieve the original position wrapped into this space position */
                positionAtBreak = position.getPosition();
            } else {
                positionAtBreak = null;
            }
        }
        if (positionAtBreak != null && positionAtBreak.getIndex() == -1) {
            /*
             * This is an indication that we are between two blocks
             * (possibly surrounded by another block), not inside a
             * paragraph.
             */
            Position position;
            Iterator<KnuthElement> iter = effectiveList.listIterator(positionIndex + 1);
            do {
                KnuthElement nextElement = iter.next();
                position = nextElement.getPosition();
            } while (position == null
                    || position instanceof SpaceResolver.SpaceHandlingPosition
                    || position instanceof SpaceResolver.SpaceHandlingBreakPosition
                        && position.getPosition().getIndex() == -1);
            LayoutManager surroundingLM = positionAtBreak.getLM();
            while (position.getLM() != surroundingLM) {
                position = position.getPosition();
            }
            restartAtLM = position.getPosition().getLM();
        }

        nextSequenceStartsOn = getNextBlockList(childLC, Constants.EN_COLUMN,
                positionAtBreak, restartAtLM, firstElements);
        return nextSequenceStartsOn;
    }

    /**
     * Returns the average width of all the lines in the given range.
     * @param effectiveList effective block list to work on
     * @param startElementIndex index of the element starting the range
     * @param endElementIndex   index of the element ending the range
     * @return the average line length, 0 if there's no content
     */
    private int optimizeLineLength(KnuthSequence effectiveList, int startElementIndex,
            int endElementIndex) {
        ListIterator<KnuthElement> effectiveListIterator;
        // optimize line length
        int boxCount = 0;
        int accumulatedLineLength = 0;
        int greatestMinimumLength = 0;
        effectiveListIterator = effectiveList.listIterator(startElementIndex);
        while (effectiveListIterator.nextIndex() <= endElementIndex) {
            KnuthElement tempEl = effectiveListIterator
                    .next();
            if (tempEl instanceof KnuthBlockBox) {
                KnuthBlockBox blockBox = (KnuthBlockBox) tempEl;
                if (blockBox.getBPD() > 0) {
                    log.debug("PSLM> nominal length of line = " + blockBox.getBPD());
                    log.debug("      range = "
                            + blockBox.getIPDRange());
                    boxCount++;
                    accumulatedLineLength += ((KnuthBlockBox) tempEl)
                            .getBPD();
                }
                if (blockBox.getIPDRange().getMin() > greatestMinimumLength) {
                    greatestMinimumLength = blockBox
                            .getIPDRange().getMin();
                }
            }
        }
        int averageLineLength = 0;
        if (accumulatedLineLength > 0 && boxCount > 0) {
            averageLineLength = (int) (accumulatedLineLength / boxCount);
            log.debug("Average line length = " + averageLineLength);
            if (averageLineLength < greatestMinimumLength) {
                averageLineLength = greatestMinimumLength;
                log.debug("  Correction to: " + averageLineLength);
            }
        }
        return averageLineLength;
    }

    /**
     * Justifies the boxes and returns them as a new KnuthSequence.
     * @param blockList block list to justify
     * @param alg reference to the algorithm instance
     * @param availableBPD the available BPD
     * @return the effective list
     */
    private BlockSequence justifyBoxes                          // CSOK: MethodLength
        (BlockSequence blockList, PageBreakingAlgorithm alg, int availableBPD) {
        int optimalPageCount;
        alg.setConstantLineWidth(availableBPD);
        optimalPageCount = alg.findBreakingPoints(blockList, /*availableBPD,*/
                1, true, BreakingAlgorithm.ALL_BREAKS);
        log.debug("PLM> optimalPageCount= " + optimalPageCount);

        //
        ListIterator<KnuthElement> sequenceIterator = blockList.listIterator();
        ListIterator<PageBreakPosition> breakIterator = alg.getPageBreaks().listIterator();
        KnuthElement thisElement = null;
        PageBreakPosition thisBreak;
        int adjustedDiff; // difference already adjusted

        while (breakIterator.hasNext()) {
            thisBreak = breakIterator.next();
            if (log.isDebugEnabled()) {
                log.debug("| first page: break= "
                        + thisBreak.getLeafPos() + " difference= "
                        + thisBreak.difference + " ratio= "
                        + thisBreak.bpdAdjust);
            }
            adjustedDiff = 0;

            // glue and penalty items at the beginning of the page must
            // be ignored:
            // the first element returned by sequenceIterator.next()
            // inside the
            // while loop must be a box
            KnuthElement firstElement;
            while (sequenceIterator.hasNext()) {
                firstElement = sequenceIterator.next();
                if ( !firstElement.isBox() ) {
                    log.debug("PLM> ignoring glue or penalty element "
                              + "at the beginning of the sequence");
                    if (firstElement.isGlue()) {
                        ((BlockLevelLayoutManager) firstElement
                         .getLayoutManager())
                            .discardSpace((KnuthGlue) firstElement);
                    }
                } else {
                    break;
                }
            }
            sequenceIterator.previous();

            // scan the sub-sequence representing a page,
            // collecting information about potential adjustments
            MinOptMax lineNumberMaxAdjustment = MinOptMax.ZERO;
            MinOptMax spaceMaxAdjustment = MinOptMax.ZERO;
            LinkedList<KnuthGlue> blockSpacesList = new LinkedList<KnuthGlue>();
            LinkedList<KnuthGlue> unconfirmedList = new LinkedList<KnuthGlue>();
            LinkedList<KnuthGlue> adjustableLinesList = new LinkedList<KnuthGlue>();
            boolean bBoxSeen = false;
            while (sequenceIterator.hasNext()
                    && sequenceIterator.nextIndex() <= thisBreak.getLeafPos()) {
                thisElement = sequenceIterator.next();
                if (thisElement.isGlue()) {
                    // glue elements are used to represent adjustable
                    // lines
                    // and adjustable spaces between blocks
                    KnuthGlue thisGlue = (KnuthGlue) thisElement;
                    Adjustment adjustment = thisGlue.getAdjustmentClass();
                    if (adjustment.equals(Adjustment.SPACE_BEFORE_ADJUSTMENT)
                            || adjustment.equals(Adjustment.SPACE_AFTER_ADJUSTMENT)) {
                        // potential space adjustment
                        // glue items before the first box or after the
                        // last one
                        // must be ignored
                        unconfirmedList.add(thisGlue);
                    } else if (adjustment.equals(Adjustment.LINE_NUMBER_ADJUSTMENT)) {
                        // potential line number adjustment
                        lineNumberMaxAdjustment
                                = lineNumberMaxAdjustment.plusMax(thisElement.getStretch());
                        lineNumberMaxAdjustment
                                = lineNumberMaxAdjustment.minusMin(thisElement.getShrink());
                        adjustableLinesList.add(thisGlue);
                    } else if (adjustment.equals(Adjustment.LINE_HEIGHT_ADJUSTMENT)) {
                        // potential line height adjustment
                    }
                } else if (thisElement.isBox()) {
                    if (!bBoxSeen) {
                        // this is the first box met in this page
                        bBoxSeen = true;
                    } else {
                        while (!unconfirmedList.isEmpty()) {
                            // glue items in unconfirmedList were not after
                            // the last box
                            // in this page; they must be added to
                            // blockSpaceList
                            KnuthGlue blockSpace = unconfirmedList.removeFirst();
                            spaceMaxAdjustment
                                = spaceMaxAdjustment.plusMax(blockSpace.getStretch());
                            spaceMaxAdjustment
                                = spaceMaxAdjustment.minusMin(blockSpace.getShrink());
                            blockSpacesList.add(blockSpace);
                        }
                    }
                }
            }
            log.debug("| line number adj= "
                    + lineNumberMaxAdjustment);
            log.debug("| space adj      = "
                    + spaceMaxAdjustment);

            if (thisElement.isPenalty() && thisElement.getWidth() > 0) {
                log.debug("  mandatory variation to the number of lines!");
                ((BlockLevelLayoutManager) thisElement
                        .getLayoutManager()).negotiateBPDAdjustment(
                        thisElement.getWidth(), thisElement);
            }

            if (thisBreak.bpdAdjust != 0
                    && (thisBreak.difference > 0 && thisBreak.difference <= spaceMaxAdjustment
                            .getMax())
                    || (thisBreak.difference < 0 && thisBreak.difference >= spaceMaxAdjustment
                            .getMin())) {
                // modify only the spaces between blocks
                adjustedDiff += adjustBlockSpaces(
                        blockSpacesList,
                        thisBreak.difference,
                        (thisBreak.difference > 0 ? spaceMaxAdjustment.getMax()
                                : -spaceMaxAdjustment.getMin()));
                log.debug("single space: "
                        + (adjustedDiff == thisBreak.difference
                                || thisBreak.bpdAdjust == 0 ? "ok"
                                : "ERROR"));
            } else if (thisBreak.bpdAdjust != 0) {
                adjustedDiff += adjustLineNumbers(
                        adjustableLinesList,
                        thisBreak.difference,
                        (thisBreak.difference > 0 ? lineNumberMaxAdjustment.getMax()
                                : -lineNumberMaxAdjustment.getMin()));
                adjustedDiff += adjustBlockSpaces(
                        blockSpacesList,
                        thisBreak.difference - adjustedDiff,
                        ((thisBreak.difference - adjustedDiff) > 0 ? spaceMaxAdjustment.getMax()
                                : -spaceMaxAdjustment.getMin()));
                log.debug("lines and space: "
                        + (adjustedDiff == thisBreak.difference
                                || thisBreak.bpdAdjust == 0 ? "ok"
                                : "ERROR"));

            }
        }

        // create a new sequence: the new elements will contain the
        // Positions
        // which will be used in the addAreas() phase
        BlockSequence effectiveList = new BlockSequence(blockList.getStartOn(),
                                                blockList.getDisplayAlign());
        effectiveList.addAll(getCurrentChildLM().getChangedKnuthElements(
                blockList.subList(0, blockList.size() - blockList.ignoreAtEnd),
                /* 0, */0));
        //effectiveList.add(new KnuthPenalty(0, -KnuthElement.INFINITE,
        // false, new Position(this), false));
        effectiveList.endSequence();

        ElementListObserver.observe(effectiveList, "breaker-effective", null);

        alg.getPageBreaks().clear(); //Why this?
        return effectiveList;
    }

    private int adjustBlockSpaces(LinkedList<KnuthGlue> spaceList, int difference, int total) {
        if (log.isDebugEnabled()) {
            log.debug("AdjustBlockSpaces: difference " + difference + " / " + total
                    + " on " + spaceList.size() + " spaces in block");
        }
        ListIterator<KnuthGlue> spaceListIterator = spaceList.listIterator();
        int adjustedDiff = 0;
        int partial = 0;
        while (spaceListIterator.hasNext()) {
            KnuthGlue blockSpace = spaceListIterator.next();
            partial += (difference > 0 ? blockSpace.getStretch() : blockSpace.getShrink());
            if (log.isDebugEnabled()) {
                log.debug("available = " + partial +  " / " + total);
                log.debug("competenza  = "
                        + (((int)((float) partial * difference / total)) - adjustedDiff)
                        + " / " + difference);
            }
            int newAdjust = ((BlockLevelLayoutManager) blockSpace.getLayoutManager())
                .negotiateBPDAdjustment
                (((int) ((float) partial * difference / total)) - adjustedDiff, blockSpace);
            adjustedDiff += newAdjust;
        }
        return adjustedDiff;
    }

    private int adjustLineNumbers(LinkedList<KnuthGlue> lineList, int difference, int total) {
        if (log.isDebugEnabled()) {
            log.debug("AdjustLineNumbers: difference "
                      + difference
                      + " / "
                      + total
                      + " on "
                      + lineList.size()
                      + " elements");
        }

        ListIterator<KnuthGlue> lineListIterator = lineList.listIterator();
        int adjustedDiff = 0;
        int partial = 0;
        while (lineListIterator.hasNext()) {
            KnuthGlue line = lineListIterator.next();
            partial += (difference > 0 ? line.getStretch() : line.getShrink());
            int newAdjust = ((BlockLevelLayoutManager) line.getLayoutManager())
                .negotiateBPDAdjustment
                (((int) ((float) partial * difference / total)) - adjustedDiff, line);
            adjustedDiff += newAdjust;
        }
        return adjustedDiff;
    }

}
