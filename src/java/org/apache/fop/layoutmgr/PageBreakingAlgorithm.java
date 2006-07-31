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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.layoutmgr.AbstractBreaker.PageBreakPosition;
import org.apache.fop.layoutmgr.breaking.OutOfLineRecord;

import org.apache.fop.traits.MinOptMax;

public class PageBreakingAlgorithm extends BreakingAlgorithm {

    /** the logger for the class */
    protected static Log classLog = LogFactory.getLog(PageBreakingAlgorithm.class);

    private LayoutManager topLevelLM;
    private PageSequenceLayoutManager.PageProvider pageProvider;
    /** List of PageBreakPosition elements. */
    private LinkedList pageBreaks = null;

    private OutOfLineRecord footnotes;
    private OutOfLineRecord floats;

    // demerits for a page break that splits a footnote 
    private int splitFootnoteDemerits = 5000;
    // demerits for a page break that defers a whole footnote to the following page 
    private int deferredFootnoteDemerits = 10000;
    private int deferredFloatDemerits = 10000;

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
                                 int alignment, int alignmentLast,
                                 MinOptMax footnoteSeparatorLength, MinOptMax floatSeparatorLength,
                                 boolean partOverflowRecovery, boolean autoHeight,
                                 boolean favorSinglePart) {
        super(alignment, alignmentLast, true, partOverflowRecovery, 0);
        this.log = classLog;
        this.topLevelLM = topLevelLM;
        this.pageProvider = pageProvider;
        best = new BestPageRecords();
        footnotes = new OutOfLineRecord((MinOptMax) footnoteSeparatorLength.clone());
        floats = new OutOfLineRecord((MinOptMax) floatSeparatorLength.clone());
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
    public class KnuthPageNode extends KnuthNode {

        public OutOfLineRecord.ProgressInfo footnotesProgress;
        public OutOfLineRecord.ProgressInfo floatsProgress;

        public KnuthPageNode(int position, int line, int fitness,
                             int totalWidth, int totalStretch, int totalShrink,
                             OutOfLineRecord.ProgressInfo footnotesProgress,
                             OutOfLineRecord.ProgressInfo floatsProgress,
                             double adjustRatio, int availableShrink, int availableStretch,
                             int difference, double totalDemerits, KnuthNode previous) {
            super(position, line, fitness,
                  totalWidth, totalStretch, totalShrink,
                  adjustRatio, availableShrink, availableStretch,
                  difference, totalDemerits, previous);
            this.footnotesProgress = footnotesProgress.copy();
            this.floatsProgress = floatsProgress.copy();
        }

    }

    /**
     * this class stores information about how the nodes
     * which could start a line ending at the current element
     */
    protected class BestPageRecords extends BestRecords {

        private OutOfLineRecord.ProgressInfo[] bestFootnotesProgress
                = new OutOfLineRecord.ProgressInfo[4];
        private OutOfLineRecord.ProgressInfo[] bestFloatsProgress
                = new OutOfLineRecord.ProgressInfo[4];
        
        public void addRecord(double demerits, KnuthNode node, double adjust,
                              int availableShrink, int availableStretch,
                              int difference, int fitness) {
            super.addRecord(demerits, node, adjust,
                            availableShrink, availableStretch,
                            difference, fitness);
            bestFootnotesProgress[fitness] = footnotes.getProgress().copy();
            bestFloatsProgress[fitness] = floats.getProgress().copy();
        }

        public int getFootnotesLength(int fitness) {
            return bestFootnotesProgress[fitness].getAlreadyInsertedLength();
        }

        public int getFootnoteListIndex(int fitness) {
            return bestFootnotesProgress[fitness].getLastInsertedIndex();
        }

        public int getFootnoteElementIndex(int fitness) {
            return bestFootnotesProgress[fitness].getLastElementOfLastInsertedIndex();
        }

        public OutOfLineRecord.ProgressInfo getFootnoteProgress(int fitness) {
            return bestFootnotesProgress[fitness];
        }

        public OutOfLineRecord.ProgressInfo getFloatProgress(int fitness) {
            return bestFloatsProgress[fitness];
        }
    }

    protected void initialize() {
        super.initialize();
        footnotes.initialize();
        floats.initialize();
    }

    public KnuthNode createNode(int position, int line, int fitness,
                                   int totalWidth, int totalStretch, int totalShrink,
                                   double adjustRatio, int availableShrink, int availableStretch,
                                   int difference, double totalDemerits, KnuthNode previous) {
        return new KnuthPageNode(position, line, fitness,
                                 totalWidth, totalStretch, totalShrink,
                                 footnotes.getProgress(), floats.getProgress(),
                                 adjustRatio, availableShrink, availableStretch,
                                 difference, totalDemerits, previous);
    }

    protected KnuthNode createNode(int position, int line, int fitness,
                                   int totalWidth, int totalStretch, int totalShrink) {
        return new KnuthPageNode(position, line, fitness,
                                 totalWidth, totalStretch, totalShrink,
                                 ((BestPageRecords) best).getFootnoteProgress(fitness),
                                 ((BestPageRecords) best).getFloatProgress(fitness),
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
                && ((KnuthBlockBox) box).hasFootnoteAnchors()) {
            footnotes.add(((KnuthBlockBox) box).getFootnoteElementLists());
        }
        if (box instanceof KnuthBlockBox
                && ((KnuthBlockBox) box).hasFloatAnchors()) {
            floats.add(((KnuthBlockBox) box).getFloatElementLists());
        }
    }


    protected int restartFrom(KnuthNode restartingNode, int currentIndex) {
        int returnValue = super.restartFrom(restartingNode, currentIndex);
        footnotes.resetNewSinceLastBreakpoint();
        floats.resetNewSinceLastBreakpoint();
        if (footnotes.existing() || floats.existing()) {
            // remove from footnotesList the note lists that will be met
            // after the restarting point
            for (int j = currentIndex; j >= restartingNode.position; j--) {
                KnuthElement resettedElement = getElement(j);
                if (resettedElement instanceof KnuthBlockBox
                        && ((KnuthBlockBox) resettedElement).hasFootnoteAnchors()) {
                    footnotes.reset(((KnuthBlockBox) resettedElement).getFootnoteElementLists());
                }
                if (resettedElement instanceof KnuthBlockBox
                        && ((KnuthBlockBox) resettedElement).hasFloatAnchors()) {
                    floats.reset(((KnuthBlockBox) resettedElement).getFloatElementLists());//TODO
                }
            }
        }
        return returnValue;
    }

    protected void considerLegalBreak(KnuthElement element, int elementIdx) {
        super.considerLegalBreak(element, elementIdx);
        footnotes.resetNewSinceLastBreakpoint();
        floats.resetNewSinceLastBreakpoint();
    }

    protected int computeDifference(KnuthNode activeNode, KnuthElement element,
                                    int elementIndex) {
        KnuthPageNode pageNode = (KnuthPageNode) activeNode;
        int actualWidth = totalWidth - pageNode.totalWidth;
        if (element.isPenalty()) {
            actualWidth += element.getW();
        }
        if (footnotes.existing()) {
            footnotes.setProgress(pageNode.footnotesProgress);
            // compute the total length of the footnotes not yet inserted
            int allFootnotes = footnotes.getTotalLength()
                    - pageNode.footnotesProgress.getAlreadyInsertedLength();
            if (allFootnotes > 0) {
                // this page contains some footnote citations
                // add the footnote separator width
                actualWidth += footnotes.getSeparatorLength().opt;
                if (actualWidth + allFootnotes <= getLineWidth()) {
                    // there is enough space to insert all footnotes:
                    // add the whole allFootnotes length
                    actualWidth += allFootnotes;
                    footnotes.insertAll();
                } else {
                    boolean canDeferOldFootnotes = checkCanDeferOldOutOfLines(footnotes,
                            pageNode.position, elementIndex);
                    int footnoteSplit;
                    if ((canDeferOldFootnotes || footnotes.newSinceLastBreakpoint())
                            && (footnoteSplit = footnotes.getFootnoteSplit(
                                    pageNode.footnotesProgress,
                                    getLineWidth() - actualWidth, canDeferOldFootnotes)) > 0) {
                        // it is allowed to break or even defer footnotes if either:
                        //  - there are new footnotes in the last piece of content, and
                        //    there is space to add at least a piece of the first one
                        //  - or the previous page break deferred some footnote lines, and
                        //    this is the first feasible break; in this case it is allowed
                        //    to break and defer, if necessary, old and new footnotes
                        actualWidth += footnoteSplit;
                    } else {
                        // there is no space to add the smallest piece of footnote,
                        // or we are trying to add a piece of content with no footnotes and
                        // it does not fit in the page, because of previous footnote bodies
                        // that cannot be broken:
                        // add the whole allFootnotes length, so this breakpoint will be discarded
                        actualWidth += allFootnotes;
                        footnotes.insertAll();
                    }
                }
            } // else: all footnotes have already been placed on previous pages
        }
        if (floats.existing()) {
            floats.setProgress(pageNode.floatsProgress);
            // compute the total length of the floats not yet inserted
            int allFloats = floats.getTotalLength()
                    - pageNode.floatsProgress.getAlreadyInsertedLength();
            if (allFloats > 0
                    && getLineWidth() - actualWidth - floats.getSeparatorLength().opt > 0) {
                // this page contains some float citations
                // add the float separator width
                int split = floats.getFloatSplit(pageNode.floatsProgress,
                        getLineWidth() - actualWidth - floats.getSeparatorLength().opt);
                if (split > 0) {
                    actualWidth += floats.getSeparatorLength().opt + split;
                }
            }
        }
        /* Another algorithm exactly mimicing the handling of footnotes: it should force
         * more floats to be on the same page as their citations, at the price of more
         * underfull pages (thus a higher total number of pages). If the current method
         * works well enough, we may keep it.
         */
//      if (floats.existing()) {
//          floats.setProgress(pageNode.floatsProgress);
//          // compute the total length of the floats not yet inserted
//          int allFloats = floats.getTotalLength()
//                  - pageNode.floatsProgress.getAlreadyInsertedLength();
//          if (allFloats > 0) {
//              // this page contains some float citations
//              // add the float separator width
//              actualWidth += floats.getSeparatorLength().opt;
//              if (actualWidth + allFloats <= getLineWidth()) {
//                  // there is enough space to insert all floats:
//                  // add the whole allFloats length
//                  actualWidth += allFloats;
//                  floats.insertAll();
//              } else {
//                  boolean canDeferOldFloats = checkCanDeferOldOutOfLines(floats,
//                          pageNode.position, elementIndex);
//                  int floatSplit;
//                  if ((canDeferOldFloats || floats.newSinceLastBreakpoint())
//                          && (floatSplit = floats.getFloatSplit(
//                                  pageNode.floatsProgress,
//                                  getLineWidth() - actualWidth)) > 0) {
//                      actualWidth += floatSplit;
//                  } else {
//                      actualWidth += allFloats;
//                      floats.insertAll();
//                  }
//              }
//          } // else: all floats have already been placed on previous pages
//      }
        return getLineWidth(activeNode.line) - actualWidth;
    }

    /**
     * Checks whether out-of-line objects from preceding pages may be deferred
     * to the page after the given element.
     * 
     * @param outOfLine informations about the out-of-line objects
     * @param activeNodePosition index in the Knuth sequence of the currently considered
     * active node
     * @param contentElementIndex index in the Knuth sequence of the currently considered
     * legal breakpoint
     * @return <code>true</code> if it is allowed to defer some out-of-line objects on
     * following pages
     */
    private boolean checkCanDeferOldOutOfLines(OutOfLineRecord outOfLine,
                                               int activeNodePosition,
                                               int contentElementIndex) {
        return (noBreakBetween(activeNodePosition, contentElementIndex)
                && outOfLine.deferred());
    }

    /**
     * Returns true if there is no legal breakpoint between the two given elements.
     * @param prevBreakIndex index of the element from the currently considered active
     * node
     * @param breakIndex index of the currently considered breakpoint
     * @return true if no element between the two is a legal breakpoint
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

    protected double computeAdjustmentRatio(KnuthNode activeNode, int difference) {
        // compute the adjustment ratio
        if (difference > 0) {
            int maxAdjustment = totalStretch - activeNode.totalStretch;
            // add the footnote separator stretch if some footnote content will be added
            if (((KnuthPageNode) activeNode).footnotesProgress.getAlreadyInsertedLength() < footnotes.getTotalLength()) {
                maxAdjustment += footnotes.getSeparatorLength().max - footnotes.getSeparatorLength().opt;
            }
            // add the float separator stretch if some float content will be added
            if (((KnuthPageNode) activeNode).floatsProgress.getAlreadyInsertedLength() < floats.getTotalLength()) {
                maxAdjustment += floats.getSeparatorLength().max - floats.getSeparatorLength().opt;
            }
            if (maxAdjustment > 0) {
                return (double) difference / maxAdjustment;
            } else {
                return INFINITE_RATIO;
            }
        } else if (difference < 0) {
            int maxAdjustment = totalShrink - activeNode.totalShrink;
            // add the footnote separator shrink if some footnote content will be added
            if (((KnuthPageNode) activeNode).footnotesProgress.getAlreadyInsertedLength() < footnotes.getTotalLength()) {
                maxAdjustment += footnotes.getSeparatorLength().opt - footnotes.getSeparatorLength().min;
            }
            // add the float separator shrink if some float content will be added
            if (((KnuthPageNode) activeNode).floatsProgress.getAlreadyInsertedLength() < floats.getTotalLength()) {
                maxAdjustment += floats.getSeparatorLength().opt - floats.getSeparatorLength().min;
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
        /* If the adjustment ratio is too high, the demerits will be "almost infinite"
         * (10^22). Adding demerits for a deferred float (10000) thus won't change the
         * demerits value. We may end up with two breakpoints with the same demerits,
         * whereas in one case there are deferred floats and not in the other case. The
         * case with no deferred floats is still preferable, so we must have the
         * possibility to distinguish it. By forcing f to 1 it becomes possible to make
         * the difference when there are deferred floats.
         * TODO vh: use threshold instead of 1 (currently threshold == 1 but it might be
         * configurable)
         */
        if (f > 1) {
            f = 1;
        }
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

        if (footnotes.existing()) {
            demerits += footnotes.getNbOfDeferred() * deferredFootnoteDemerits; 
            if (footnotes.isSplit()) {
                demerits += splitFootnoteDemerits;
            }
        }
        if (floats.existing()) {
            demerits += floats.getNbOfDeferred() * deferredFloatDemerits; 
        }
        demerits += activeNode.totalDemerits;
        return demerits;
    }

    protected void finish() {
        for (int i = startLine; i < endLine; i++) {
            for (KnuthPageNode node = (KnuthPageNode) getNode(i);
                 node != null;
                 node = (KnuthPageNode) node.next) {
                if (node.footnotesProgress.getAlreadyInsertedLength()
                        < footnotes.getTotalLength()) {
                    // layout remaining footnote bodies
                    footnotes.createFootnotePages(node, this, getLineWidth());
                }
                if (node.floatsProgress.getAlreadyInsertedLength() < floats.getTotalLength()) {
                    // layout remaining float bodies
                    floats.createFloatPages(node, this, getLineWidth());
                }
            }
        }
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
            if (!autoHeight && log.isWarnEnabled()) {
                log.warn(FONode.decorateWithContextInfo(
                        "Part/page " + (getPartCount() + 1) 
                        + " overflows the available area in block-progression dimension.", 
                        getFObj()));
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
        int firstFootnoteListIndex = ((KnuthPageNode) bestActiveNode.previous).
                footnotesProgress.getLastInsertedIndex();
        int firstFootnoteElementIndex = ((KnuthPageNode) bestActiveNode.previous).
                footnotesProgress.getLastElementOfLastInsertedIndex();
        if (firstFootnoteListIndex == -1) {
            firstFootnoteListIndex++;
            firstFootnoteElementIndex = 0;
        } else if (footnotes.getSequence(firstFootnoteListIndex) != null
                && firstFootnoteElementIndex == ((LinkedList) footnotes.
                        getSequence(firstFootnoteListIndex)).size() - 1) {
            // advance to the next list
            firstFootnoteListIndex++;
            firstFootnoteElementIndex = 0;
        } else {
            firstFootnoteElementIndex++;
        }
        // compute the indexes of the first float list
        int firstFloatListIndex = ((KnuthPageNode) bestActiveNode.previous).
                floatsProgress.getLastInsertedIndex() + 1;

        // add nodes at the beginning of the list, as they are found
        // backwards, from the last one to the first one
        if (log.isDebugEnabled()) {
            log.debug("BBA> difference=" + difference + " ratio=" + ratio 
                    + " position=" + bestActiveNode.position);
        }
        insertPageBreakAsFirst(new PageBreakPosition(this.topLevelLM, 
                bestActiveNode.position,
                firstFootnoteListIndex, firstFootnoteElementIndex,
                ((KnuthPageNode) bestActiveNode).footnotesProgress.getLastInsertedIndex(),
                ((KnuthPageNode) bestActiveNode).footnotesProgress.
                        getLastElementOfLastInsertedIndex(),
                firstFloatListIndex,
                ((KnuthPageNode) bestActiveNode).floatsProgress.getLastInsertedIndex(),
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
        return (LinkedList) footnotes.getSequence(index);
    }

    public LinkedList getFloatList(int index) {
        return (LinkedList) floats.getSequence(index);
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
    
}
