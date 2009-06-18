/*
 * Copyright 2004-2005 The Apache Software Foundation.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.Constants;
import org.apache.fop.traits.MinOptMax;

/**
 * Abstract base class for breakers (page breakers, static region handlers etc.).
 */
public abstract class AbstractBreaker {

    /** logging instance */
    protected static Log log = LogFactory.getLog(AbstractBreaker.class);

    public static class PageBreakPosition extends LeafPosition {
        double bpdAdjust; // Percentage to adjust (stretch or shrink)
        int difference;
        int footnoteFirstListIndex;
        int footnoteFirstElementIndex;
        int footnoteLastListIndex;
        int footnoteLastElementIndex;

        PageBreakPosition(LayoutManager lm, int iBreakIndex,
                          int ffli, int ffei, int flli, int flei,
                          double bpdA, int diff) {
            super(lm, iBreakIndex);
            bpdAdjust = bpdA;
            difference = diff;
            footnoteFirstListIndex = ffli;
            footnoteFirstElementIndex = ffei;
            footnoteLastListIndex = flli;
            footnoteLastElementIndex = flei;
        }
    }

    public class BlockSequence extends BlockKnuthSequence {

        /** Number of elements to ignore at the beginning of the list. */ 
        public int ignoreAtStart = 0;
        /** Number of elements to ignore at the end of the list. */
        public int ignoreAtEnd = 0;

        /**
         * startOn represents where on the page/which page layout
         * should start for this BlockSequence.  Acceptable values:
         * Constants.EN_ANY (can continue from finished location 
         * of previous BlockSequence?), EN_COLUMN, EN_ODD_PAGE, 
         * EN_EVEN_PAGE. 
         */
        private int startOn;

        public BlockSequence(int iStartOn) {
            super();
            startOn = iStartOn;
        }
        
        public int getStartOn() {
            return this.startOn;
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
                   && !((KnuthElement)this.get(this.size() - 1)).isBox()) {
                this.remove(this.size() - 1);
            }
            if (this.size() > ignoreAtStart) {
                // add the elements representing the space at the end of the last line
                // and the forced break
                this.add(new KnuthPenalty(0, KnuthElement.INFINITE, false, null, false));
                this.add(new KnuthGlue(0, 10000000, 0, null, false));
                this.add(new KnuthPenalty(0, -KnuthElement.INFINITE, false, breakPosition, false));
                ignoreAtEnd = 3;
                return this;
            } else {
                this.clear();
                return null;
            }
        }

        public BlockSequence endBlockSequence(Position breakPosition) {
            KnuthSequence temp = endSequence(breakPosition);
            if (temp != null) {
                BlockSequence returnSequence = new BlockSequence(startOn);
                returnSequence.addAll(temp);
                returnSequence.ignoreAtEnd = this.ignoreAtEnd;
                return returnSequence;
            } else {
                return null;
            }
        }
    }

    /** blockListIndex of the current BlockSequence in blockLists */
    private int blockListIndex = 0;

    private List blockLists = null;

    protected int alignment;
    private int alignmentLast;

    protected MinOptMax footnoteSeparatorLength = new MinOptMax(0);

    protected abstract int getCurrentDisplayAlign();
    protected abstract boolean hasMoreContent();
    protected abstract void addAreas(PositionIterator posIter, LayoutContext context);
    protected abstract LayoutManager getTopLevelLM();
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
     * Returns the PageProvider if any. PageBreaker overrides this method because each
     * page may have a different available BPD which needs to be accessible to the breaking
     * algorithm.
     * @return the applicable PageProvider, or null if not applicable
     */
    protected PageSequenceLayoutManager.PageProvider getPageProvider() {
        return null;
    }
    
    /*
     * This method is to contain the logic to determine the LM's
     * getNextKnuthElements() implementation(s) that are to be called. 
     * @return LinkedList of Knuth elements.  
     */
    protected abstract LinkedList getNextKnuthElements(LayoutContext context, int alignment);

    /** @return true if there's no content that could be handled. */
    public boolean isEmpty() {
        return (blockLists.size() == 0);
    }
    
    protected void startPart(BlockSequence list, int breakClass) {
        //nop
    }
    
    /**
     * This method is called when no content is available for a part. Used to force empty pages.
     */
    protected void handleEmptyContent() {
        //nop    
    }
    
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
     */
    public void doLayout(int flowBPD) {
        doLayout(flowBPD, false);
    }
    
    /**
     * Starts the page breaking process.
     * @param flowBPD the constant available block-progression-dimension (used for every part)
     * @param autoHeight true if warnings about overflows should be disabled because the
     *                   the BPD is really undefined (for footnote-separators, for example)
     */
    public void doLayout(int flowBPD, boolean autoHeight) {
        LayoutContext childLC = createLayoutContext();
        childLC.setStackLimit(new MinOptMax(flowBPD));

        if (getCurrentDisplayAlign() == Constants.EN_X_FILL) {
            //EN_FILL is non-standard (by LF)
            alignment = Constants.EN_JUSTIFY;
        } else {
            alignment = Constants.EN_START;
        }
        alignmentLast = Constants.EN_START;
        childLC.setBPAlignment(alignment);

        BlockSequence blockList;
        blockLists = new java.util.ArrayList();

        log.debug("PLM> flow BPD =" + flowBPD);
        
        //*** Phase 1: Get Knuth elements ***
        int nextSequenceStartsOn = Constants.EN_ANY;
        while (hasMoreContent()) {
            blockLists.clear();

            nextSequenceStartsOn = getNextBlockList(childLC, nextSequenceStartsOn, blockLists);

            //*** Phase 2: Alignment and breaking ***
            log.debug("PLM> blockLists.size() = " + blockLists.size());
            for (blockListIndex = 0; blockListIndex < blockLists.size(); blockListIndex++) {
                blockList = (BlockSequence) blockLists.get(blockListIndex);
                
                //debug code start
                if (log.isDebugEnabled()) {
                    log.debug("  blockListIndex = " + blockListIndex);
                    String pagina = (blockList.startOn == Constants.EN_ANY) ? "any page"
                            : (blockList.startOn == Constants.EN_ODD_PAGE) ? "odd page"
                                    : "even page";
                    log.debug("  sequence starts on " + pagina);
                }
                observeElementList(blockList);
                //debug code end

                log.debug("PLM> start of algorithm (" + this.getClass().getName() 
                        + "), flow BPD =" + flowBPD);
                PageBreakingAlgorithm alg = new PageBreakingAlgorithm(getTopLevelLM(),
                        getPageProvider(),
                        alignment, alignmentLast, footnoteSeparatorLength,
                        isPartOverflowRecoveryActivated(), autoHeight);
                int iOptPageCount;

                BlockSequence effectiveList;
                if (alignment == Constants.EN_JUSTIFY) {
                    /* justification */
                    effectiveList = justifyBoxes(blockList, alg, flowBPD);
                } else {
                    /* no justification */
                    effectiveList = blockList;
                }

                //iOptPageCount = alg.firstFit(effectiveList, flowBPD, 1, true);
                alg.setConstantLineWidth(flowBPD);
                iOptPageCount = alg.findBreakingPoints(effectiveList, /*flowBPD,*/
                            1, true, BreakingAlgorithm.ALL_BREAKS);
                log.debug("PLM> iOptPageCount= " + iOptPageCount
                        + " pageBreaks.size()= " + alg.getPageBreaks().size());

                
                //*** Phase 3: Add areas ***
                doPhase3(alg, iOptPageCount, blockList, effectiveList);
            }
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
        LayoutContext childLC;
        // add areas
        ListIterator effectiveListIterator = effectiveList.listIterator();
        int startElementIndex = 0;
        int endElementIndex = 0;
        int lastBreak = -1;
        for (int p = 0; p < partCount; p++) {
            PageBreakPosition pbp = (PageBreakPosition) alg.getPageBreaks().get(p);

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
                    + ", break at pos " + endElementIndex);

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
            effectiveListIterator = effectiveList
                    .listIterator(startElementIndex);
            KnuthElement firstElement;
            while (effectiveListIterator.hasNext()
                    && !(firstElement = (KnuthElement) effectiveListIterator.next())
                            .isBox()) {
                /*
                if (firstElement.isGlue() && firstElement.getLayoutManager() != null) {
                    // discard the space representd by the glue element
                    ((BlockLevelLayoutManager) firstElement
                            .getLayoutManager())
                            .discardSpace((KnuthGlue) firstElement);
                }*/
                startElementIndex++;
            }

            if (startElementIndex <= endElementIndex) {
                log.debug("     addAreas from " + startElementIndex
                        + " to " + endElementIndex);
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
                    effectiveListIterator = effectiveList
                            .listIterator(startElementIndex);
                    while (effectiveListIterator.nextIndex() <= endElementIndex) {
                        KnuthElement tempEl = (KnuthElement)effectiveListIterator.next();
                        if (tempEl.isBox() && tempEl.getW() > 0) {
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
                    int averageLineLength = optimizeLineLength(effectiveList, startElementIndex, endElementIndex);
                    if (averageLineLength != 0) {
                        childLC.setStackLimit(new MinOptMax(averageLineLength));
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
     * @param blockLists list of block lists (sequences)
     * @return the page on which the next content should appear after a hard break
     */
    protected int getNextBlockList(LayoutContext childLC, 
            int nextSequenceStartsOn, 
            List blockLists) {
        updateLayoutContext(childLC);
        //Make sure the span change signal is reset
        childLC.signalSpanChange(Constants.NOT_SET);
        
        LinkedList returnedList;
        BlockSequence blockList;
        if ((returnedList = getNextKnuthElements(childLC, alignment)) != null) {
            if (returnedList.size() == 0) {
                nextSequenceStartsOn = handleSpanChange(childLC, nextSequenceStartsOn);
                return nextSequenceStartsOn;
            }
            blockList = new BlockSequence(nextSequenceStartsOn);
            
            //Only implemented by the PSLM
            nextSequenceStartsOn = handleSpanChange(childLC, nextSequenceStartsOn);
            
            Position breakPosition = null;
            if (((KnuthElement) returnedList.getLast()).isPenalty()
                    && ((KnuthPenalty) returnedList.getLast()).getP() == -KnuthElement.INFINITE) {
                KnuthPenalty breakPenalty = (KnuthPenalty) returnedList
                        .removeLast();
                breakPosition = breakPenalty.getPosition();
                switch (breakPenalty.getBreakClass()) {
                case Constants.EN_PAGE:
                    log.debug("PLM> break - PAGE");
                    nextSequenceStartsOn = Constants.EN_ANY;
                    break;
                case Constants.EN_COLUMN:
                    log.debug("PLM> break - COLUMN");
                    //TODO Fix this when implementing multi-column layout
                    nextSequenceStartsOn = Constants.EN_COLUMN;
                    break;
                case Constants.EN_ODD_PAGE:
                    log.debug("PLM> break - ODD PAGE");
                    nextSequenceStartsOn = Constants.EN_ODD_PAGE;
                    break;
                case Constants.EN_EVEN_PAGE:
                    log.debug("PLM> break - EVEN PAGE");
                    nextSequenceStartsOn = Constants.EN_EVEN_PAGE;
                    break;
                default:
                    throw new IllegalStateException("Invalid break class: " 
                            + breakPenalty.getBreakClass());
                }
            }
            blockList.addAll(returnedList);
            BlockSequence seq = null;
            seq = blockList.endBlockSequence(breakPosition);
            if (seq != null) {
                blockLists.add(seq);
            }
        }
        return nextSequenceStartsOn;
    }

    /**
     * @param effectiveList effective block list to work on
     * @param startElementIndex
     * @param endElementIndex
     * @return the average line length, 0 if there's no content
     */
    private int optimizeLineLength(KnuthSequence effectiveList, int startElementIndex, int endElementIndex) {
        ListIterator effectiveListIterator;
        // optimize line length
        int boxCount = 0;
        int accumulatedLineLength = 0;
        int greatestMinimumLength = 0;
        effectiveListIterator = effectiveList
                .listIterator(startElementIndex);
        while (effectiveListIterator.nextIndex() <= endElementIndex) {
            KnuthElement tempEl = (KnuthElement) effectiveListIterator
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
                if (blockBox.getIPDRange().min > greatestMinimumLength) {
                    greatestMinimumLength = blockBox
                            .getIPDRange().min;
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
    private BlockSequence justifyBoxes(BlockSequence blockList, PageBreakingAlgorithm alg, int availableBPD) {
        int iOptPageNumber;
        alg.setConstantLineWidth(availableBPD);
        iOptPageNumber = alg.findBreakingPoints(blockList, /*availableBPD,*/
                1, true, BreakingAlgorithm.ALL_BREAKS);
        log.debug("PLM> iOptPageNumber= " + iOptPageNumber);

        // 
        ListIterator sequenceIterator = blockList.listIterator();
        ListIterator breakIterator = alg.getPageBreaks().listIterator();
        KnuthElement thisElement = null;
        PageBreakPosition thisBreak;
        int accumulatedS; // accumulated stretch or shrink
        int adjustedDiff; // difference already adjusted
        int firstElementIndex;

        while (breakIterator.hasNext()) {
            thisBreak = (PageBreakPosition) breakIterator.next();
            if (log.isDebugEnabled()) {
                log.debug("| first page: break= "
                        + thisBreak.getLeafPos() + " difference= "
                        + thisBreak.difference + " ratio= "
                        + thisBreak.bpdAdjust);
            }
            accumulatedS = 0;
            adjustedDiff = 0;

            // glue and penalty items at the beginning of the page must
            // be ignored:
            // the first element returned by sequenceIterator.next()
            // inside the
            // while loop must be a box
            KnuthElement firstElement;
            while (!(firstElement = (KnuthElement) sequenceIterator
                    .next()).isBox()) {
                // 
                log.debug("PLM> ignoring glue or penalty element "
                        + "at the beginning of the sequence");
                if (firstElement.isGlue()) {
                    ((BlockLevelLayoutManager) firstElement
                            .getLayoutManager())
                            .discardSpace((KnuthGlue) firstElement);
                }
            }
            firstElementIndex = sequenceIterator.previousIndex();
            sequenceIterator.previous();

            // scan the sub-sequence representing a page,
            // collecting information about potential adjustments
            MinOptMax lineNumberMaxAdjustment = new MinOptMax(0);
            MinOptMax spaceMaxAdjustment = new MinOptMax(0);
            double spaceAdjustmentRatio = 0.0;
            LinkedList blockSpacesList = new LinkedList();
            LinkedList unconfirmedList = new LinkedList();
            LinkedList adjustableLinesList = new LinkedList();
            boolean bBoxSeen = false;
            while (sequenceIterator.hasNext()
                    && sequenceIterator.nextIndex() <= thisBreak
                            .getLeafPos()) {
                thisElement = (KnuthElement) sequenceIterator.next();
                if (thisElement.isGlue()) {
                    // glue elements are used to represent adjustable
                    // lines
                    // and adjustable spaces between blocks
                    switch (((KnuthGlue) thisElement)
                            .getAdjustmentClass()) {
                    case BlockLevelLayoutManager.SPACE_BEFORE_ADJUSTMENT:
                    // fall through
                    case BlockLevelLayoutManager.SPACE_AFTER_ADJUSTMENT:
                        // potential space adjustment
                        // glue items before the first box or after the
                        // last one
                        // must be ignored
                        unconfirmedList.add(thisElement);
                        break;
                    case BlockLevelLayoutManager.LINE_NUMBER_ADJUSTMENT:
                        // potential line number adjustment
                        lineNumberMaxAdjustment.max += ((KnuthGlue) thisElement)
                                .getY();
                        lineNumberMaxAdjustment.min -= ((KnuthGlue) thisElement)
                                .getZ();
                        adjustableLinesList.add(thisElement);
                        break;
                    case BlockLevelLayoutManager.LINE_HEIGHT_ADJUSTMENT:
                        // potential line height adjustment
                        break;
                    default:
                    // nothing
                    }
                } else if (thisElement.isBox()) {
                    if (!bBoxSeen) {
                        // this is the first box met in this page
                        bBoxSeen = true;
                    } else if (unconfirmedList.size() > 0) {
                        // glue items in unconfirmedList were not after
                        // the last box
                        // in this page; they must be added to
                        // blockSpaceList
                        while (unconfirmedList.size() > 0) {
                            KnuthGlue blockSpace = (KnuthGlue) unconfirmedList
                                    .removeFirst();
                            spaceMaxAdjustment.max += ((KnuthGlue) blockSpace)
                                    .getY();
                            spaceMaxAdjustment.min -= ((KnuthGlue) blockSpace)
                                    .getZ();
                            blockSpacesList.add(blockSpace);
                        }
                    }
                }
            }
            log.debug("| line number adj= "
                    + lineNumberMaxAdjustment);
            log.debug("| space adj      = "
                    + spaceMaxAdjustment);

            if (thisElement.isPenalty() && thisElement.getW() > 0) {
                log.debug("  mandatory variation to the number of lines!");
                ((BlockLevelLayoutManager) thisElement
                        .getLayoutManager()).negotiateBPDAdjustment(
                        thisElement.getW(), thisElement);
            }

            if (thisBreak.bpdAdjust != 0
                    && (thisBreak.difference > 0 && thisBreak.difference <= spaceMaxAdjustment.max)
                    || (thisBreak.difference < 0 && thisBreak.difference >= spaceMaxAdjustment.min)) {
                // modify only the spaces between blocks
                spaceAdjustmentRatio = ((double) thisBreak.difference / (thisBreak.difference > 0 ? spaceMaxAdjustment.max
                        : spaceMaxAdjustment.min));
                adjustedDiff += adjustBlockSpaces(
                        blockSpacesList,
                        thisBreak.difference,
                        (thisBreak.difference > 0 ? spaceMaxAdjustment.max
                                : -spaceMaxAdjustment.min));
                log.debug("single space: "
                        + (adjustedDiff == thisBreak.difference
                                || thisBreak.bpdAdjust == 0 ? "ok"
                                : "ERROR"));
            } else if (thisBreak.bpdAdjust != 0) {
                adjustedDiff += adjustLineNumbers(
                        adjustableLinesList,
                        thisBreak.difference,
                        (thisBreak.difference > 0 ? lineNumberMaxAdjustment.max
                                : -lineNumberMaxAdjustment.min));
                adjustedDiff += adjustBlockSpaces(
                        blockSpacesList,
                        thisBreak.difference - adjustedDiff,
                        ((thisBreak.difference - adjustedDiff) > 0 ? spaceMaxAdjustment.max
                                : -spaceMaxAdjustment.min));
                log.debug("lines and space: "
                        + (adjustedDiff == thisBreak.difference
                                || thisBreak.bpdAdjust == 0 ? "ok"
                                : "ERROR"));

            }
        }

        // create a new sequence: the new elements will contain the
        // Positions
        // which will be used in the addAreas() phase
        BlockSequence effectiveList = new BlockSequence(blockList.getStartOn());
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

    private int adjustBlockSpaces(LinkedList spaceList, int difference, int total) {
        if (log.isDebugEnabled()) {
            log.debug("AdjustBlockSpaces: difference " + difference + " / " + total 
                    + " on " + spaceList.size() + " spaces in block");
        }
        ListIterator spaceListIterator = spaceList.listIterator();
        int adjustedDiff = 0;
        int partial = 0;
        while (spaceListIterator.hasNext()) {
            KnuthGlue blockSpace = (KnuthGlue)spaceListIterator.next();
            partial += (difference > 0 ? blockSpace.getY() : blockSpace.getZ());
            if (log.isDebugEnabled()) {
                log.debug("available = " + partial +  " / " + total);
                log.debug("competenza  = " 
                        + (((int)((float) partial * difference / total)) - adjustedDiff) 
                        + " / " + difference);
            }
            int newAdjust = ((BlockLevelLayoutManager) blockSpace.getLayoutManager()).negotiateBPDAdjustment(((int) ((float) partial * difference / total)) - adjustedDiff, blockSpace);
            adjustedDiff += newAdjust;
        }
        return adjustedDiff;
    }

    private int adjustLineNumbers(LinkedList lineList, int difference, int total) {
        if (log.isDebugEnabled()) {
            log.debug("AdjustLineNumbers: difference " + difference + " / " + total + " on " + lineList.size() + " elements");
        }

//            int adjustedDiff = 0;
//            int partial = 0;
//            KnuthGlue prevLine = null;
//            KnuthGlue currLine = null;
//            ListIterator lineListIterator = lineList.listIterator();
//            while (lineListIterator.hasNext()) {
//                currLine = (KnuthGlue)lineListIterator.next();
//                if (prevLine != null
//                    && prevLine.getLayoutManager() != currLine.getLayoutManager()) {
//                    int newAdjust = ((BlockLevelLayoutManager) prevLine.getLayoutManager())
//                                    .negotiateBPDAdjustment(((int) ((float) partial * difference / total)) - adjustedDiff, prevLine);
//                    adjustedDiff += newAdjust;
//                }
//                partial += (difference > 0 ? currLine.getY() : currLine.getZ());
//                prevLine = currLine;
//            }
//            if (currLine != null) {
//                int newAdjust = ((BlockLevelLayoutManager) currLine.getLayoutManager())
//                                .negotiateBPDAdjustment(((int) ((float) partial * difference / total)) - adjustedDiff, currLine);
//                adjustedDiff += newAdjust;
//            }
//            return adjustedDiff;

        ListIterator lineListIterator = lineList.listIterator();
        int adjustedDiff = 0;
        int partial = 0;
        while (lineListIterator.hasNext()) {
            KnuthGlue line = (KnuthGlue)lineListIterator.next();
            partial += (difference > 0 ? line.getY() : line.getZ());
            int newAdjust = ((BlockLevelLayoutManager) line.getLayoutManager()).negotiateBPDAdjustment(((int) ((float) partial * difference / total)) - adjustedDiff, line);
            adjustedDiff += newAdjust;
        }
        return adjustedDiff;
    }
    
}
