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

    /*LF*/
    public static class PageBreakPosition extends LeafPosition {
        double bpdAdjust; // Percentage to adjust (stretch or shrink)
        int difference;

        PageBreakPosition(LayoutManager lm, int iBreakIndex,
                          double bpdA, int diff) {
            super(lm, iBreakIndex);
            bpdAdjust = bpdA;
            difference = diff;
        }
    }

    public class BlockSequence extends KnuthSequence {

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

        public BlockSequence endBlockSequence() {
            KnuthSequence temp = super.endSequence();
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
/*LF*/
    /*LF*/
    private List blockLists = null;

    private int alignment;
    private int alignmentLast;
    /*LF*/

    protected abstract int getCurrentDisplayAlign();
    protected abstract boolean hasMoreContent();
    protected abstract void addAreas(PositionIterator posIter, LayoutContext context);
    protected abstract LayoutManager getTopLevelLM();
    protected abstract LayoutManager getCurrentChildLM();
    protected abstract LinkedList getNextKnuthElements(LayoutContext context, int alignment);

    /** @return true if there's no content that could be handled. */
    public boolean isEmpty() {
        return (blockLists.size() == 0);
    }
    
    protected void startPart(BlockSequence list, boolean bIsFirstPage) {
        //nop
    }
    
    protected abstract void finishPart();

    protected LayoutContext createLayoutContext() {
        return new LayoutContext(0);
    }
    
    public void doLayout(int flowBPD) {
        LayoutContext childLC = createLayoutContext();
        childLC.setStackLimit(new MinOptMax(flowBPD));

        //System.err.println("Vertical alignment: " +
        // currentSimplePageMaster.getRegion(FO_REGION_BODY).getDisplayAlign());
        if (getCurrentDisplayAlign() == Constants.EN_X_FILL) {
            //EN_FILL is non-standard (by LF)
            alignment = Constants.EN_JUSTIFY;
        } else {
            alignment = Constants.EN_START;
        }
        alignmentLast = Constants.EN_START;

        BlockSequence blockList;
        blockLists = new java.util.ArrayList();

        System.out.println("PLM> flow BPD =" + flowBPD);
        
        //*** Phase 1: Get Knuth elements ***
        int nextSequenceStartsOn = Constants.EN_ANY;
        while (hasMoreContent()) {
            nextSequenceStartsOn = getNextBlockList(childLC, nextSequenceStartsOn, blockLists);
        }

        //*** Phase 2: Alignment and breaking ***
        System.out.println("PLM> blockLists.size() = " + blockLists.size());
        for (blockListIndex = 0; blockListIndex < blockLists.size(); blockListIndex++) {
            blockList = (BlockSequence) blockLists.get(blockListIndex);
            
            //debug code start
            System.err.println("  blockListIndex = " + blockListIndex);
            String pagina = (blockList.startOn == Constants.EN_ANY) ? "any page"
                    : (blockList.startOn == Constants.EN_ODD_PAGE) ? "odd page"
                            : "even page";
            System.err.println("  sequence starts on " + pagina);
            logBlocklist(blockList);
            //debug code end

            System.out.println("PLM> start of algorithm (" + this.getClass().getName() 
                    + "), flow BPD =" + flowBPD);
            PageBreakingAlgorithm alg = new PageBreakingAlgorithm(getTopLevelLM(),
                    alignment, alignmentLast);
            int iOptPageNumber;

            BlockSequence effectiveList;
            if (alignment == Constants.EN_JUSTIFY) {
                /* justification */
                effectiveList = justifyBoxes(blockList, alg, flowBPD);
            } else {
                /* no justification */
                effectiveList = blockList;
            }

            //iOptPageNumber = alg.firstFit(effectiveList, flowBPD, 1, true);
            iOptPageNumber = alg.findBreakingPoints(effectiveList, flowBPD, 1,
                    true, true);
            System.out.println("PLM> iOptPageNumber= " + iOptPageNumber
                    + " pageBreaks.size()= " + alg.getPageBreaks().size());

            
            //*** Phase 3: Add areas ***
            doPhase3(alg, iOptPageNumber, blockList, effectiveList);
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
        for (int p = 0; p < partCount; p++) {
            PageBreakPosition pbp = (PageBreakPosition) alg.getPageBreaks().get(p);
            endElementIndex = pbp.getLeafPos();
            System.out.println("PLM> part: " + (p + 1)
                    + ", break at position " + endElementIndex);

            startPart(effectiveList, (p == 0));
            
            int displayAlign = getCurrentDisplayAlign();
            
            // ignore the first elements added by the
            // PageSequenceLayoutManager
            startElementIndex += (startElementIndex == 0) 
                    ? effectiveList.ignoreAtStart
                    : 0;

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
            while (effectiveListIterator.hasNext()
                    && !((KnuthElement) effectiveListIterator.next())
                            .isBox()) {
                startElementIndex++;
            }

            if (startElementIndex <= endElementIndex) {
                System.out.println("     addAreas da " + startElementIndex
                        + " a " + endElementIndex);
                childLC = new LayoutContext(0);
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

                addAreas(new KnuthPossPosIter(effectiveList,
                        startElementIndex, endElementIndex + 1), childLC);
            }

            finishPart();

            startElementIndex = pbp.getLeafPos() + 1;
        }
    }
    
    /**
     * Gets the next block list (sequence) and adds it to a list of block lists if it's not empty.
     * @param childLC LayoutContext to use
     * @param nextSequenceStartsOn indicates on what page the next sequence should start
     * @param blockLists list of block lists (sequences)
     * @return the page on which the next content should appear after a hard break
     */
    private int getNextBlockList(LayoutContext childLC, int nextSequenceStartsOn, List blockLists) {
        LinkedList returnedList;
        BlockSequence blockList;
        if ((returnedList = getNextKnuthElements(childLC, alignment)) != null) {
            if (returnedList.size() == 0) {
                return nextSequenceStartsOn;
            }
            blockList = new BlockSequence(nextSequenceStartsOn);
            if (((KnuthElement) returnedList.getLast()).isPenalty()
                    && ((KnuthPenalty) returnedList.getLast()).getP() == -KnuthElement.INFINITE) {
                KnuthPenalty breakPenalty = (KnuthPenalty) returnedList
                        .removeLast();
                switch (breakPenalty.getBreakClass()) {
                case Constants.EN_PAGE:
                    System.err.println("PLM> break - PAGE");
                    nextSequenceStartsOn = Constants.EN_ANY;
                    break;
                case Constants.EN_COLUMN:
                    System.err.println("PLM> break - COLUMN");
                    //TODO Fix this when implementing multi-column layout
                    nextSequenceStartsOn = Constants.EN_COLUMN;
                    break;
                case Constants.EN_ODD_PAGE:
                    System.err.println("PLM> break - ODD PAGE");
                    nextSequenceStartsOn = Constants.EN_ODD_PAGE;
                    break;
                case Constants.EN_EVEN_PAGE:
                    System.err.println("PLM> break - EVEN PAGE");
                    nextSequenceStartsOn = Constants.EN_EVEN_PAGE;
                    break;
                default:
                    throw new IllegalStateException("Invalid break class: " 
                            + breakPenalty.getBreakClass());
                }
            }
            blockList.addAll(returnedList);
            BlockSequence seq = null;
            seq = blockList.endBlockSequence();
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
        //System.out.println(" ");
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
            //System.out.println("PSLM> lunghezza media = " + averageLineLength);
            if (averageLineLength < greatestMinimumLength) {
                averageLineLength = greatestMinimumLength;
                //System.out.println("      correzione, ora e' = " + averageLineLength);
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
        iOptPageNumber = alg.findBreakingPoints(blockList, availableBPD, 1,
                true, true);
        System.out.println("PLM> iOptPageNumber= " + iOptPageNumber);

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
            System.out.println("| first page: break= "
                    + thisBreak.getLeafPos() + " difference= "
                    + thisBreak.difference + " ratio= "
                    + thisBreak.bpdAdjust);
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
                System.out.println("PLM> ignoring glue or penalty element "
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
            System.out.println("| line number adj= "
                    + lineNumberMaxAdjustment);
            System.out.println("| space adj      = "
                    + spaceMaxAdjustment);

            if (thisElement.isPenalty() && thisElement.getW() > 0) {
                System.out
                        .println("  mandatory variation to the number of lines!");
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
                System.out.println("single space: "
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
                System.out.println("lines and space: "
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

        logEffectiveList(effectiveList);

        alg.getPageBreaks().clear(); //Why this?
        return effectiveList;
    }

    /**
     * Logs the contents of a block list for debugging purposes
     * @param blockList block list to log
     */
    private void logBlocklist(KnuthSequence blockList) {
        ListIterator tempIter = blockList.listIterator();

        KnuthElement temp;
        System.out.println(" ");
        while (tempIter.hasNext()) {
            temp = (KnuthElement) tempIter.next();
            if (temp.isBox()) {
                System.out.println(tempIter.previousIndex()
                        + ") " + temp);
            } else if (temp.isGlue()) {
                System.out.println(tempIter.previousIndex()
                        + ") " + temp);
            } else {
                System.out.println(tempIter.previousIndex()
                        + ") " + temp);
            }
            if (temp.getPosition() != null) {
                System.out.println("            " + temp.getPosition());
            }
        }
        System.out.println(" ");
    }

    /**
     * Logs the contents of an effective block list for debugging purposes
     * @param effectiveList block list to log
     */
    private void logEffectiveList(KnuthSequence effectiveList) {
        System.out.println("Effective list");
        logBlocklist(effectiveList);
    }

    private int adjustBlockSpaces(LinkedList spaceList, int difference, int total) {
    /*LF*/  System.out.println("AdjustBlockSpaces: difference " + difference + " / " + total + " on " + spaceList.size() + " spaces in block");
            ListIterator spaceListIterator = spaceList.listIterator();
            int adjustedDiff = 0;
            int partial = 0;
            while (spaceListIterator.hasNext()) {
                KnuthGlue blockSpace = (KnuthGlue)spaceListIterator.next();
                partial += (difference > 0 ? blockSpace.getY() : blockSpace.getZ());
                System.out.println("available = " + partial +  " / " + total);
                System.out.println("competenza  = " + (((int) ((float) partial * difference / total)) - adjustedDiff) + " / " + difference);
                int newAdjust = ((BlockLevelLayoutManager) blockSpace.getLayoutManager()).negotiateBPDAdjustment(((int) ((float) partial * difference / total)) - adjustedDiff, blockSpace);
                adjustedDiff += newAdjust;
            }
            return adjustedDiff;
        }

    private int adjustLineNumbers(LinkedList lineList, int difference, int total) {
    /*LF*/  System.out.println("AdjustLineNumbers: difference " + difference + " / " + total + " on " + lineList.size() + " elements");

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
