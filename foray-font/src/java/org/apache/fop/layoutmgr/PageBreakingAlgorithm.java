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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.layoutmgr.AbstractBreaker.PageBreakPosition;

import org.apache.fop.traits.MinOptMax;

class PageBreakingAlgorithm extends BreakingAlgorithm {

    /** the logger for the class */
    protected static Log classLog = LogFactory.getLog(PageBreakingAlgorithm.class);

    private LayoutManager topLevelLM;
    private PageSequenceLayoutManager.PageProvider pageProvider;
    private PageBreakingLayoutListener layoutListener;
    /** List of PageBreakPosition elements. */
    private LinkedList pageBreaks = null;

    /** Footnotes which are cited between the currently considered active node (previous
     * break) and the current considered break. Its type is
     * List&lt;List&lt;KnuthElement&gt;&gt;, it contains the sequences of KnuthElement
     * representing the footnotes bodies.
     */
    private ArrayList footnotesList = null;
    /** Cumulated bpd of unhandled footnotes. */
    private ArrayList lengthList = null;
    /** Length of all the footnotes which will be put on the current page. */
    private int totalFootnotesLength = 0;
    /**
     * Length of all the footnotes which have already been inserted, up to the currently
     * considered element. That is, footnotes from the currently considered page plus
     * footnotes from its preceding pages.
     */
    private int insertedFootnotesLength = 0;
    /** True if footnote citations have been met since the beginning of the page sequence. */
    private boolean footnotesPending = false;
    /**
     * True if the elements met after the previous break point contain footnote citations.
     */
    private boolean newFootnotes = false;
    /**
     * Index of the first footnote met after the previous break point.
     */
    private int firstNewFootnoteIndex = 0;
    /** Index of the last footnote inserted on the current page. */
    private int footnoteListIndex = 0;
    /** Index of the last element of the last footnote inserted on the current page. */
    private int footnoteElementIndex = -1;

    // demerits for a page break that splits a footnote 
    private int splitFootnoteDemerits = 5000;
    // demerits for a page break that defers a whole footnote to the following page 
    private int deferredFootnoteDemerits = 10000;
    private MinOptMax footnoteSeparatorLength = null;

    // the method noBreakBetween(int, int) uses these variables 
    // to store parameters and result of the last call, in order
    // to reuse them and take less time
    private int storedPrevBreakIndex = -1;
    private int storedBreakIndex = -1;
    private boolean storedValue = false;

    //Controls whether overflows should be warned about or not
    private boolean autoHeight = false;
    
    //Controls whether a single part should be forced if possible (ex. block-container)
    private boolean favorSinglePart = false;
    
    public PageBreakingAlgorithm(LayoutManager topLevelLM,
                                 PageSequenceLayoutManager.PageProvider pageProvider,
                                 PageBreakingLayoutListener layoutListener,
                                 int alignment, int alignmentLast,
                                 MinOptMax footnoteSeparatorLength,
                                 boolean partOverflowRecovery, boolean autoHeight,
                                 boolean favorSinglePart) {
        super(alignment, alignmentLast, true, partOverflowRecovery, 0);
        this.log = classLog;
        this.topLevelLM = topLevelLM;
        this.pageProvider = pageProvider;
        this.layoutListener = layoutListener;
        best = new BestPageRecords();
        this.footnoteSeparatorLength = (MinOptMax) footnoteSeparatorLength.clone();
        // add some stretch, to avoid a restart for every page containing footnotes
        if (footnoteSeparatorLength.min == footnoteSeparatorLength.max) {
            footnoteSeparatorLength.max += 10000;
        }
        this.autoHeight = autoHeight;
        this.favorSinglePart = favorSinglePart;
    }

    /**
     * This class represents a feasible breaking point
     * with extra information about footnotes.
     */
    protected class KnuthPageNode extends KnuthNode {

        /** Additional length due to footnotes. */
        public int totalFootnotes;

        /** Index of the last inserted footnote. */
        public int footnoteListIndex;

        /** Index of the last inserted element of the last inserted footnote. */
        public int footnoteElementIndex;

        public KnuthPageNode(int position, int line, int fitness,
                             int totalWidth, int totalStretch, int totalShrink,
                             int totalFootnotes, int footnoteListIndex, int footnoteElementIndex,
                             double adjustRatio, int availableShrink, int availableStretch,
                             int difference, double totalDemerits, KnuthNode previous) {
            super(position, line, fitness,
                  totalWidth, totalStretch, totalShrink,
                  adjustRatio, availableShrink, availableStretch,
                  difference, totalDemerits, previous);
            this.totalFootnotes = totalFootnotes;
            this.footnoteListIndex = footnoteListIndex;
            this.footnoteElementIndex = footnoteElementIndex;
        }

    }

    /**
     * this class stores information about how the nodes
     * which could start a line ending at the current element
     */
    protected class BestPageRecords extends BestRecords {

        private int[] bestFootnotesLength = new int[4];
        private int[] bestFootnoteListIndex = new int[4];
        private int[] bestFootnoteElementIndex = new int[4];

        public void addRecord(double demerits, KnuthNode node, double adjust,
                              int availableShrink, int availableStretch,
                              int difference, int fitness) {
            super.addRecord(demerits, node, adjust,
                            availableShrink, availableStretch,
                            difference, fitness);
            bestFootnotesLength[fitness] = insertedFootnotesLength;
            bestFootnoteListIndex[fitness] = footnoteListIndex;
            bestFootnoteElementIndex[fitness] = footnoteElementIndex;
        }

        public int getFootnotesLength(int fitness) {
            return bestFootnotesLength[fitness];
        }

        public int getFootnoteListIndex(int fitness) {
            return bestFootnoteListIndex[fitness];
        }

        public int getFootnoteElementIndex(int fitness) {
            return bestFootnoteElementIndex[fitness];
        }
    }

    protected void initialize() {
        super.initialize();
        insertedFootnotesLength = 0;
        footnoteListIndex = 0;
        footnoteElementIndex = -1;
    }

    protected KnuthNode createNode(int position, int line, int fitness,
                                   int totalWidth, int totalStretch, int totalShrink,
                                   double adjustRatio, int availableShrink, int availableStretch,
                                   int difference, double totalDemerits, KnuthNode previous) {
        return new KnuthPageNode(position, line, fitness,
                                 totalWidth, totalStretch, totalShrink,
                                 insertedFootnotesLength, footnoteListIndex, footnoteElementIndex,
                                 adjustRatio, availableShrink, availableStretch,
                                 difference, totalDemerits, previous);
    }

    protected KnuthNode createNode(int position, int line, int fitness,
                                   int totalWidth, int totalStretch, int totalShrink) {
        return new KnuthPageNode(position, line, fitness,
                                 totalWidth, totalStretch, totalShrink,
                                 ((BestPageRecords) best).getFootnotesLength(fitness),
                                 ((BestPageRecords) best).getFootnoteListIndex(fitness),
                                 ((BestPageRecords) best).getFootnoteElementIndex(fitness), 
                                 best.getAdjust(fitness), best.getAvailableShrink(fitness),
                                 best.getAvailableStretch(fitness), best.getDifference(fitness),
                                 best.getDemerits(fitness), best.getNode(fitness));
    }

    /**
     * Page-breaking specific handling of the given box. Currently it adds the footnotes
     * cited in the given box to the list of to-be-handled footnotes.
     * @param box a block-level element possibly containing foonotes citations
     */
    protected void handleBox(KnuthBox box) {
        if (box instanceof KnuthBlockBox
            && ((KnuthBlockBox) box).hasAnchors()) {
            handleFootnotes(((KnuthBlockBox) box).getElementLists());
            if (!newFootnotes) {
                newFootnotes = true;
                firstNewFootnoteIndex = footnotesList.size() - 1;
            }
        }
    }

    /**
     * Handles the footnotes cited inside a block-level box. Updates footnotesList and the
     * value of totalFootnotesLength with the lengths of the given footnotes.
     * @param elementLists list of KnuthElement sequences corresponding to the footnotes
     * bodies
     */
    private void handleFootnotes(LinkedList elementLists) {
        // initialization
        if (!footnotesPending) {
            footnotesPending = true;
            footnotesList = new ArrayList();
            lengthList = new ArrayList();
            totalFootnotesLength = 0;
        }
        if (!newFootnotes) {
            newFootnotes = true;
            firstNewFootnoteIndex = footnotesList.size();
        }

        // compute the total length of the footnotes
        ListIterator elementListsIterator = elementLists.listIterator();
        while (elementListsIterator.hasNext()) {
            LinkedList noteList = (LinkedList) elementListsIterator.next();
            
            //Space resolution (Note: this does not respect possible stacking constraints 
            //between footnotes!)
            SpaceResolver.resolveElementList(noteList);
            
            int noteLength = 0;
            footnotesList.add(noteList);
            ListIterator noteListIterator = noteList.listIterator();
            while (noteListIterator.hasNext()) {
                KnuthElement element = (KnuthElement) noteListIterator.next();
                if (element.isBox() || element.isGlue()) {
                    noteLength += element.getW();
                }
            }
            int prevLength = (lengthList.size() == 0 
                    ? 0 
                    : ((Integer) lengthList.get(lengthList.size() - 1)).intValue());
            lengthList.add(new Integer(prevLength + noteLength));
            totalFootnotesLength += noteLength;
        }
    }

    protected int restartFrom(KnuthNode restartingNode, int currentIndex) {
        int returnValue = super.restartFrom(restartingNode, currentIndex);
        newFootnotes = false;
        if (footnotesPending) {
            // remove from footnotesList the note lists that will be met
            // after the restarting point
            for (int j = currentIndex; j >= restartingNode.position; j--) {
                KnuthElement resettedElement = getElement(j);
                if (resettedElement instanceof KnuthBlockBox
                    && ((KnuthBlockBox) resettedElement).hasAnchors()) {
                    resetFootnotes(((KnuthBlockBox) resettedElement).getElementLists());
                }
            }
        }
        return returnValue;
    }

    private void resetFootnotes(LinkedList elementLists) {
        for (int i = 0; i < elementLists.size(); i++) {
            LinkedList removedList = (LinkedList) footnotesList.remove(footnotesList.size() - 1);
            lengthList.remove(lengthList.size() - 1);

            // update totalFootnotesLength
            if (lengthList.size() > 0) {
                totalFootnotesLength = ((Integer) lengthList.get(lengthList.size() - 1)).intValue();
            } else {
                totalFootnotesLength = 0;
            }
        }
        // update footnotesPending;
        if (footnotesList.size() == 0) {
            footnotesPending = false;
        }
    }

    protected void considerLegalBreak(KnuthElement element, int elementIdx) {
        super.considerLegalBreak(element, elementIdx);
        newFootnotes = false;
    }

    protected int computeDifference(KnuthNode activeNode, KnuthElement element,
                                    int elementIndex) {
        KnuthPageNode pageNode = (KnuthPageNode) activeNode;
        int actualWidth = totalWidth - pageNode.totalWidth;
        int footnoteSplit;
        boolean canDeferOldFootnotes;
        if (element.isPenalty()) {
            actualWidth += element.getW();
        }
        if (footnotesPending) {
            // compute the total length of the footnotes not yet inserted
            int allFootnotes = totalFootnotesLength - pageNode.totalFootnotes;
            if (allFootnotes > 0) {
                // this page contains some footnote citations
                // add the footnote separator width
                actualWidth += footnoteSeparatorLength.opt;
                if (actualWidth + allFootnotes <= getLineWidth()) {
                    // there is enough space to insert all footnotes:
                    // add the whole allFootnotes length
                    actualWidth += allFootnotes;
                    insertedFootnotesLength = pageNode.totalFootnotes + allFootnotes;
                    footnoteListIndex = footnotesList.size() - 1;
                    footnoteElementIndex = ((LinkedList) footnotesList.get(footnoteListIndex)).size() - 1;
                } else if (((canDeferOldFootnotes = checkCanDeferOldFootnotes(pageNode, elementIndex))
                            || newFootnotes)
                           && (footnoteSplit = getFootnoteSplit(pageNode, getLineWidth() - actualWidth,
                                                                canDeferOldFootnotes)) > 0) {
                    // it is allowed to break or even defer footnotes if either:
                    //  - there are new footnotes in the last piece of content, and
                    //    there is space to add at least a piece of the first one
                    //  - or the previous page break deferred some footnote lines, and
                    //    this is the first feasible break; in this case it is allowed
                    //    to break and defer, if necessary, old and new footnotes
                    actualWidth += footnoteSplit;
                    insertedFootnotesLength = pageNode.totalFootnotes + footnoteSplit;
                    // footnoteListIndex has been set in getFootnoteSplit()
                    // footnoteElementIndex has been set in getFootnoteSplit()
                } else {
                    // there is no space to add the smallest piece of footnote,
                    // or we are trying to add a piece of content with no footnotes and
                    // it does not fit in the page, because of previous footnote bodies
                    // that cannot be broken:
                    // add the whole allFootnotes length, so this breakpoint will be discarded
                    actualWidth += allFootnotes;
                    insertedFootnotesLength = pageNode.totalFootnotes + allFootnotes;
                    footnoteListIndex = footnotesList.size() - 1;
                    footnoteElementIndex = ((LinkedList) footnotesList.get(footnoteListIndex)).size() - 1;
                }
            } else {
                // all footnotes have already been placed on previous pages
            }
        } else {
            // there are no footnotes
        }
        return getLineWidth(activeNode.line) - actualWidth;
    }

    /** Checks whether footnotes from preceding pages may be deferred to the page after
     * the given element.
     * @param node active node for the preceding page break
     * @param contentElementIndex index of the Knuth element considered for the
     * current page break
     */
    private boolean checkCanDeferOldFootnotes(KnuthPageNode node, int contentElementIndex) {
        return (noBreakBetween(node.position, contentElementIndex)
                && deferredFootnotes(node.footnoteListIndex, node.footnoteElementIndex, node.totalFootnotes));
    }

    /**
     * Returns true if there may be no breakpoint between the two given elements.
     * @param prevBreakIndex index of the element from the currently considered active
     * node
     * @param breakIndex index of the currently considered breakpoint
     * @return true if no element between the two can be a breakpoint
     */
    private boolean noBreakBetween(int prevBreakIndex, int breakIndex) {
        // this method stores the parameters and the return value from previous calls
        // in order to avoid scanning the element list unnecessarily:
        //  - if there is no break between element #i and element #j
        //    there will not be a break between #(i+h) and #j too
        //  - if there is a break between element #i and element #j
        //    there will be a break between #(i-h) and #(j+k) too
        if (storedPrevBreakIndex != -1
            && ((prevBreakIndex >= storedPrevBreakIndex
                 && breakIndex == storedBreakIndex
                 && storedValue)
                || (prevBreakIndex <= storedPrevBreakIndex
                    && breakIndex >= storedBreakIndex
                    && !storedValue))) {
            // use the stored value, do nothing
        } else {
            // compute the new value
            int index;
            // ignore suppressed elements
            for (index = prevBreakIndex + 1;
                    !par.getElement(index).isBox();
                    index++) {
                //nop
            }
            // find the next break
            for (;
                 index < breakIndex;
                 index++) {
                if (par.getElement(index).isGlue() && par.getElement(index - 1).isBox()
                    || par.getElement(index).isPenalty() 
                       && ((KnuthElement) par.getElement(index)).getP() < KnuthElement.INFINITE) {
                    // break found
                    break;
                }
            }
            // update stored parameters and value
            storedPrevBreakIndex = prevBreakIndex;
            storedBreakIndex = breakIndex;
            storedValue = (index == breakIndex);
        }
        return storedValue;
    }

    /**
     * Returns true if their are (pieces of) footnotes to be typeset on the current page.
     * @param listIndex index of the last inserted footnote for the currently considered
     * active node
     * @param elementIndex index of the last element of the last inserted footnote
     * @param length total length of all footnotes inserted so far
     */
    private boolean deferredFootnotes(int listIndex, int elementIndex, int length) {
        return ((newFootnotes
                 && firstNewFootnoteIndex != 0
                 && (listIndex < firstNewFootnoteIndex - 1
                     || elementIndex < ((LinkedList) footnotesList.get(listIndex)).size() - 1))
                || length < totalFootnotesLength);
    }

    /**
     * Tries to split the flow of footnotes to put one part on the current page.
     * @param activeNode currently considered previous page break
     * @param availableLength available space for footnotes
     * @param canDeferOldFootnotes
     */
    private int getFootnoteSplit(KnuthPageNode activeNode, int availableLength, boolean canDeferOldFootnotes) {
        return getFootnoteSplit(activeNode.footnoteListIndex,
                                activeNode.footnoteElementIndex,
                                activeNode.totalFootnotes,
                                availableLength, canDeferOldFootnotes);
    }

    /**
     * Tries to split the flow of footnotes to put one part on the current page.
     * @param prevListIndex index of the last footnote on the previous page
     * @param prevElementIndex index of the last element of the last footnote
     * @param prevLength total length of footnotes inserted so far
     * @param availableLength available space for footnotes on this page
     * @param canDeferOldFootnotes
     */
    private int getFootnoteSplit(int prevListIndex, int prevElementIndex, int prevLength,
                                 int availableLength, boolean canDeferOldFootnotes) {
        if (availableLength <= 0) {
            return 0;
        } else {
            // the split should contain a piece of the last footnote
            // together with all previous, not yet inserted footnotes;
            // but if this is not possible, try adding as much content as possible
            int splitLength = 0;
            ListIterator noteListIterator = null;
            KnuthElement element = null;
            boolean somethingAdded = false;

            // prevListIndex and prevElementIndex points to the last footnote element
            // already placed in a page: advance to the next element
            int listIndex = prevListIndex;
            int elementIndex = prevElementIndex;
            if (elementIndex == ((LinkedList) footnotesList.get(listIndex)).size() - 1) {
                listIndex++;
                elementIndex = 0;
            } else {
                elementIndex++;
            }

            // try adding whole notes
            if (footnotesList.size() - 1 > listIndex) {
                // add the previous footnotes: these cannot be broken or deferred
                if (!canDeferOldFootnotes
                    && newFootnotes
                    && firstNewFootnoteIndex > 0) {
                    splitLength = ((Integer) lengthList.get(firstNewFootnoteIndex - 1)).intValue()
                                  - prevLength;
                    listIndex = firstNewFootnoteIndex;
                    elementIndex = 0;
                }
                // try adding the new footnotes
                while (((Integer) lengthList.get(listIndex)).intValue() - prevLength
                       <= availableLength) {
                    splitLength = ((Integer) lengthList.get(listIndex)).intValue()
                                  - prevLength;
                    somethingAdded = true;
                    listIndex++;
                    elementIndex = 0;
                }
                // as this method is called only if it is not possible to insert
                // all footnotes, at this point listIndex and elementIndex points to
                // an existing element, the next one we will try to insert 
            }

            // try adding a split of the next note
            noteListIterator = ((LinkedList) footnotesList.get(listIndex)).listIterator(elementIndex);

            int prevSplitLength = 0;
            int prevIndex = -1;
            int index = -1;

            while (!(somethingAdded && splitLength > availableLength)) {
                if (!somethingAdded) {
                    somethingAdded = true;
                } else {
                    prevSplitLength = splitLength;
                    prevIndex = index;
                }
                // get a sub-sequence from the note element list
                boolean bPrevIsBox = false;
                while (noteListIterator.hasNext()) {
                    // as this method is called only if it is not possible to insert
                    // all footnotes, and we have already tried (and failed) to insert
                    // this whole footnote, the while loop will never reach the end
                    // of the note sequence
                    element = (KnuthElement) noteListIterator.next();
                    if (element.isBox()) {
                        // element is a box
                        splitLength += element.getW();
                        bPrevIsBox = true;
                    } else if (element.isGlue()) {
                        // element is a glue
                        if (bPrevIsBox) {
                            // end of the sub-sequence
                            index = noteListIterator.previousIndex();
                            break;
                        }
                        bPrevIsBox = false;
                        splitLength += element.getW();
                    } else {
                        // element is a penalty
                        if (element.getP() < KnuthElement.INFINITE) {
                            // end of the sub-sequence
                            index = noteListIterator.previousIndex();
                            break;
                        }
                    }
                }
            }
            // if prevSplitLength is 0, this means that the available length isn't enough
            // to insert even the smallest split of the last footnote, so we cannot end a
            // page here
            // if prevSplitLength is > 0 we can insert some footnote content in this page
            // and insert the remaining in the following one
            if (!somethingAdded) {
                // there was not enough space to add a piece of the first new footnote
                // this is not a good break
                prevSplitLength = 0;
            } else if (prevSplitLength > 0) {
                // prevIndex is -1 if we have added only some whole footnotes
                footnoteListIndex = (prevIndex != -1) ? listIndex : listIndex - 1;
                footnoteElementIndex = (prevIndex != -1)
                    ? prevIndex 
                    : ((LinkedList) footnotesList.get(footnoteListIndex)).size() - 1;
            }
            return prevSplitLength;
        }
    }

    protected double computeAdjustmentRatio(KnuthNode activeNode, int difference) {
        // compute the adjustment ratio
        if (difference > 0) {
            int maxAdjustment = totalStretch - activeNode.totalStretch;
            // add the footnote separator stretch if some footnote content will be added
            if (((KnuthPageNode) activeNode).totalFootnotes < totalFootnotesLength) {
                maxAdjustment += footnoteSeparatorLength.max - footnoteSeparatorLength.opt;
            }
            if (maxAdjustment > 0) {
                return (double) difference / maxAdjustment;
            } else {
                return INFINITE_RATIO;
            }
        } else if (difference < 0) {
            int maxAdjustment = totalShrink - activeNode.totalShrink;
            // add the footnote separator shrink if some footnote content will be added
            if (((KnuthPageNode) activeNode).totalFootnotes < totalFootnotesLength) {
                maxAdjustment += footnoteSeparatorLength.opt - footnoteSeparatorLength.min;
            }
            if (maxAdjustment > 0) {
                return (double) difference / maxAdjustment;
            } else {
                return -INFINITE_RATIO;
            }
        } else {
            return 0;
        }
    }

    protected double computeDemerits(KnuthNode activeNode, KnuthElement element, 
                                    int fitnessClass, double r) {
        double demerits = 0;
        // compute demerits
        double f = Math.abs(r);
        f = 1 + 100 * f * f * f;
        if (element.isPenalty() && element.getP() >= 0) {
            f += element.getP();
            demerits = f * f;
        } else if (element.isPenalty() && !element.isForcedBreak()) {
            double penalty = element.getP();
            demerits = f * f - penalty * penalty;
        } else {
            demerits = f * f;
        }

        if (element.isPenalty() && ((KnuthPenalty) element).isFlagged()
            && getElement(activeNode.position).isPenalty()
            && ((KnuthPenalty) getElement(activeNode.position)).isFlagged()) {
            // add demerit for consecutive breaks at flagged penalties
            demerits += repeatedFlaggedDemerit;
        }
        if (Math.abs(fitnessClass - activeNode.fitness) > 1) {
            // add demerit for consecutive breaks
            // with very different fitness classes
            demerits += incompatibleFitnessDemerit;
        }

        if (footnotesPending) {
            if (footnoteListIndex < footnotesList.size() - 1) {
                // add demerits for the deferred footnotes
                demerits += (footnotesList.size() - 1 - footnoteListIndex) 
                                * deferredFootnoteDemerits;
            }
            if (footnoteElementIndex 
                    < ((LinkedList) footnotesList.get(footnoteListIndex)).size() - 1) {
                // add demerits for the footnote split between pages
                demerits += splitFootnoteDemerits;
            }
        }
        demerits += activeNode.totalDemerits;
        return demerits;
    }

    protected void finish() {
        for (int i = startLine; i < endLine; i++) {
            for (KnuthPageNode node = (KnuthPageNode) getNode(i);
                 node != null;
                 node = (KnuthPageNode) node.next) {
                if (node.totalFootnotes < totalFootnotesLength) {
                    // layout remaining footnote bodies
                    createFootnotePages(node);
                }
            }
        }
    }

    private void createFootnotePages(KnuthPageNode lastNode) {
        insertedFootnotesLength = lastNode.totalFootnotes;
        footnoteListIndex = lastNode.footnoteListIndex;
        footnoteElementIndex = lastNode.footnoteElementIndex;
        int availableBPD = getLineWidth();
        int split = 0;
        KnuthPageNode prevNode = lastNode;

        // create pages containing the remaining footnote bodies
        while (insertedFootnotesLength < totalFootnotesLength) {
            // try adding some more content
            if (((Integer) lengthList.get(footnoteListIndex)).intValue() - insertedFootnotesLength
                <= availableBPD) {
                // add a whole footnote
                availableBPD -= ((Integer) lengthList.get(footnoteListIndex)).intValue()
                                - insertedFootnotesLength;
                insertedFootnotesLength = ((Integer)lengthList.get(footnoteListIndex)).intValue();
                footnoteElementIndex
                    = ((LinkedList)footnotesList.get(footnoteListIndex)).size() - 1;
            } else if ((split = getFootnoteSplit(footnoteListIndex, footnoteElementIndex,
                                                 insertedFootnotesLength, availableBPD, true))
                       > 0) {
                // add a piece of a footnote
                availableBPD -= split;
                insertedFootnotesLength += split;
                // footnoteListIndex has already been set in getFootnoteSplit()
                // footnoteElementIndex has already been set in getFootnoteSplit()
            } else {
                // cannot add any content: create a new node and start again
                KnuthPageNode node = (KnuthPageNode)
                                     createNode(lastNode.position, prevNode.line + 1, 1,
                                                insertedFootnotesLength - prevNode.totalFootnotes, 
                                                0, 0,
                                                0, 0, 0,
                                                0, 0, prevNode);
                addNode(node.line, node);
                removeNode(prevNode.line, prevNode);

                prevNode = node;
                availableBPD = getLineWidth();
            }
        }
        // create the last node
        KnuthPageNode node = (KnuthPageNode)
                             createNode(lastNode.position, prevNode.line + 1, 1,
                                        totalFootnotesLength - prevNode.totalFootnotes, 0, 0,
                                        0, 0, 0,
                                        0, 0, prevNode);
        addNode(node.line, node);
        removeNode(prevNode.line, prevNode);
    }

    /**
     * @return a list of PageBreakPosition elements
     */
    public LinkedList getPageBreaks() {
        return pageBreaks;
    }

    public void insertPageBreakAsFirst(PageBreakPosition pageBreak) {
        if (pageBreaks == null) {
            pageBreaks = new LinkedList();
        }
        pageBreaks.addFirst(pageBreak);
    }
    
    private int getPartCount() {
        if (pageBreaks == null) {
            return 0;
        } else {
            return pageBreaks.size();
        }
    }
    
    public void updateData1(int total, double demerits) {
    }

    public void updateData2(KnuthNode bestActiveNode,
                            KnuthSequence sequence,
                            int total) {
        //int difference = (bestActiveNode.line < total) 
        //      ? bestActiveNode.difference : bestActiveNode.difference + fillerMinWidth;
        int difference = bestActiveNode.difference;
        if (difference + bestActiveNode.availableShrink < 0) {
            if (!autoHeight) {
                if (layoutListener != null) {
                    layoutListener.notifyOverflow(bestActiveNode.line - 1, getFObj());
                } else if (log.isWarnEnabled()) {
                    log.warn(FONode.decorateWithContextInfo(
                            "Part/page " + (bestActiveNode.line - 1) 
                            + " overflows the available area in block-progression dimension.", 
                            getFObj()));
                }
            }
        }
        boolean isNonLastPage = (bestActiveNode.line < total);
        int blockAlignment = isNonLastPage ? alignment : alignmentLast;
        // it is always allowed to adjust space, so the ratio must be set regardless of
        // the value of the property display-align; the ratio must be <= 1
        double ratio = bestActiveNode.adjustRatio;
        if (ratio < 0) {
            // page break with a negative difference:
            // spaces always have enough shrink
            difference = 0;
        } else if (ratio <= 1 && isNonLastPage) {
            // not-last page break with a positive difference smaller than the available stretch:
            // spaces can stretch to fill the whole difference
            difference = 0;
        } else if (ratio > 1) {
            // not-last page with a positive difference greater than the available stretch
            // spaces can stretch to fill the difference only partially
            ratio = 1;
            difference -= bestActiveNode.availableStretch;
        } else {
            // last page with a positive difference:
            // spaces do not need to stretch
            if (blockAlignment != Constants.EN_JUSTIFY) {
                ratio = 0;
            } else {
                //Stretch as much as possible on last page
                difference = 0;
            }
        }
        // compute the indexes of the first footnote list and the first element in that list
        int firstListIndex = ((KnuthPageNode) bestActiveNode.previous).footnoteListIndex;
        int firstElementIndex = ((KnuthPageNode) bestActiveNode.previous).footnoteElementIndex;
        if (footnotesList != null
            && firstElementIndex == ((LinkedList) footnotesList.get(firstListIndex)).size() - 1) {
            // advance to the next list
            firstListIndex++;
            firstElementIndex = 0;
        } else {
            firstElementIndex++;
        }

        // add nodes at the beginning of the list, as they are found
        // backwards, from the last one to the first one
        if (log.isDebugEnabled()) {
            log.debug("BBA> difference=" + difference + " ratio=" + ratio 
                    + " position=" + bestActiveNode.position);
        }
        insertPageBreakAsFirst(new PageBreakPosition(this.topLevelLM, 
                bestActiveNode.position,
                firstListIndex, firstElementIndex,
                ((KnuthPageNode) bestActiveNode).footnoteListIndex,
                ((KnuthPageNode) bestActiveNode).footnoteElementIndex,
                ratio, difference));
    }

    protected int filterActiveNodes() {
        // leave only the active node with fewest total demerits
        KnuthNode bestActiveNode = null;
        for (int i = startLine; i < endLine; i++) {
            for (KnuthNode node = getNode(i); node != null; node = node.next) {
                if (favorSinglePart 
                        && node.line > 1 
                        && bestActiveNode != null
                        && Math.abs(bestActiveNode.difference) < bestActiveNode.availableShrink) {
                    //favor current best node, so just skip the current node because it would
                    //result in more than one part
                } else {
                    bestActiveNode = compareNodes(bestActiveNode, node);
                }
                if (node != bestActiveNode) {
                    removeNode(i, node);
                }
            }
        }
        return bestActiveNode.line;
    }

    public LinkedList getFootnoteList(int index) {
        return (LinkedList) footnotesList.get(index);
    }
    
    /** @return the associated top-level formatting object. */
    public FObj getFObj() {
        return topLevelLM.getFObj();
    }
    
    /** @see org.apache.fop.layoutmgr.BreakingAlgorithm#getLineWidth(int) */
    protected int getLineWidth(int line) {
        int bpd;
        if (pageProvider != null) {
            bpd = pageProvider.getAvailableBPD(line);
        } else {
            bpd = super.getLineWidth(line);
        }
        if (log.isTraceEnabled()) {
            log.trace("getLineWidth(" + line + ") -> " + bpd);
        }
        return bpd;
    }
    
    /**
     * Interface to notify about layout events during page breaking.
     */
    public interface PageBreakingLayoutListener {

        /**
         * Issued when an overflow is detected
         * @param part the number of the part (page) this happens on
         * @param obj the root FO object where this happens
         */
        void notifyOverflow(int part, FObj obj);
        
    }
    
}
