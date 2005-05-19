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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.fop.layoutmgr.AbstractBreaker.PageBreakPosition;

import org.apache.fop.traits.MinOptMax;

class PageBreakingAlgorithm extends BreakingAlgorithm {
    private LayoutManager topLevelLM;
    private LinkedList pageBreaks = null;

    private ArrayList footnotesList = null;
    private ArrayList lengthList = null;
    private int totalFootnotesLength = 0;
    private int insertedFootnotesLength = 0;
    private boolean bPendingFootnotes = false;
    /**
     * bNewFootnotes is true if the elements met after the previous break point
     * contain footnote citations
     */
    private boolean bNewFootnotes = false;
    /**
     * iNewFootnoteIndex is the index of the first footnote met after the 
     * previous break point
     */
    private int iNewFootnoteIndex = 0;
    private int footnoteListIndex = 0;
    private int footnoteElementIndex = -1;

    // demerits for a page break that splits a footnote 
    private int splitFootnoteDemerits = 5000;
    // demerits for a page break that defers a whole footnote to the following page 
    private int deferredFootnoteDemerits = 10000;
    private MinOptMax footnoteSeparatorLength = new MinOptMax(0);

    public PageBreakingAlgorithm(LayoutManager topLevelLM,
                                 int alignment, int alignmentLast,
                                 MinOptMax fnSeparatorLength) {
        super(alignment, alignmentLast, true);
        this.topLevelLM = topLevelLM;
        best = new BestPageRecords();
        footnoteSeparatorLength = (MinOptMax) fnSeparatorLength.clone();
        // add some stretch, to avoid a restart for every page containing footnotes
        if (footnoteSeparatorLength.min == footnoteSeparatorLength.max) {
            footnoteSeparatorLength.max += 10000;
        }
    }

    /**
     * this class represent a feasible breaking point
     * with extra information about footnotes
     */
    protected class KnuthPageNode extends KnuthNode {

        // additional length due to footnotes
        public int totalFootnotes;

        // index of the last inserted footnote
        public int footnoteListIndex;

        // index of the last inserted element of the last inserted footnote
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

        private int bestFootnotesLength[] = new int[4];
        private int bestFootnoteListIndex[] = new int[4];
        private int bestFootnoteElementIndex[] = new int[4];

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
                                 best.getAdjust(fitness), best.getAvailableShrink(fitness), best.getAvailableStretch(fitness),
                                 best.getDifference(fitness), best.getDemerits(fitness), best.getNode(fitness));
    }

    protected void handleBox(KnuthBox box) {
        if (box instanceof KnuthBlockBox
            && ((KnuthBlockBox) box).hasAnchors()) {
            handleFootnotes(((KnuthBlockBox) box).getElementLists());
            if (!bNewFootnotes) {
                bNewFootnotes = true;
                iNewFootnoteIndex = footnotesList.size() - 1;
            }
        }
    }

    private void handleFootnotes(LinkedList elementLists) {
        // initialization
        if (!bPendingFootnotes) {
            bPendingFootnotes = true;
            footnotesList = new ArrayList();
            lengthList = new ArrayList();
            totalFootnotesLength = 0;
        }

        // compute the total length of the footnotes
        ListIterator elementListsIterator = elementLists.listIterator();
        while (elementListsIterator.hasNext()) {
            LinkedList noteList = (LinkedList) elementListsIterator.next();
            int noteLength = 0;
            footnotesList.add(noteList);
            ListIterator noteListIterator = noteList.listIterator();
            while (noteListIterator.hasNext()) {
                KnuthElement element = (KnuthElement) noteListIterator.next();
                if (element.isBox() || element.isGlue()) {
                    noteLength += element.getW();
                }
            }
            int prevLength = (lengthList.size() == 0 ? 0 : ((Integer) lengthList.get(lengthList.size() - 1)).intValue());
            lengthList.add(new Integer(prevLength + noteLength));
            totalFootnotesLength += noteLength;
        }
    }

    protected void restartFrom(KnuthNode restartingNode, int currentIndex) {
        super.restartFrom(restartingNode, currentIndex);
        bNewFootnotes = false;
        if (bPendingFootnotes) {
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
        // update bPendingFootnotes;
        if (footnotesList.size() == 0) {
            bPendingFootnotes = false;
        }
    }

    protected void considerLegalBreak(KnuthElement element, int elementIdx) {
        super.considerLegalBreak(element, elementIdx);
        bNewFootnotes = false;
    }

    /**
     * Return the difference between the line width and the width of the break that
     * ends in 'element'.
     * @param activeNode
     * @param element
     * @return The difference in width. Positive numbers mean extra space in the line,
     * negative number that the line overflows. 
     */
    protected int computeDifference(KnuthNode activeNode, KnuthElement element) {
        int actualWidth = totalWidth - activeNode.totalWidth;
        int footnoteSplit;
        if (element.isPenalty()) {
            actualWidth += element.getW();
        }
        if (bPendingFootnotes) {
            // compute the total length of the footnotes not yet inserted
            int newFootnotes = totalFootnotesLength - ((KnuthPageNode) activeNode).totalFootnotes;
            if (newFootnotes > 0) {
                // this page contains some footnote citations
                // add the footnote separator width
                actualWidth += footnoteSeparatorLength.opt;
                if (actualWidth + newFootnotes <= lineWidth) {
                    // there is enough space to insert all footnotes:
                    // add the whole newFootnotes length
                    actualWidth += newFootnotes;
                    insertedFootnotesLength = ((KnuthPageNode) activeNode).totalFootnotes + newFootnotes;
                    footnoteListIndex = footnotesList.size() - 1;
                    footnoteElementIndex = ((LinkedList) footnotesList.get(footnoteListIndex)).size() - 1;
                } else if (bNewFootnotes
                    && (footnoteSplit = getFootnoteSplit((KnuthPageNode) activeNode, lineWidth - actualWidth)) > 0) {
                    // the last footnote, whose citation is in the last piece of content
                    // added to the page, will be split:
                    // add as much footnote content as possible
                    actualWidth += footnoteSplit;
                    insertedFootnotesLength = ((KnuthPageNode) activeNode).totalFootnotes + footnoteSplit;
                    footnoteListIndex = footnotesList.size() - 1;
                    // footnoteElementIndex has been set in getFootnoteSplit()
                } else {
                    // there is no space to add the smallest piece of the last footnote,
                    // or we are trying to add a piece of content with no footnotes and
                    // it does not fit in the page, because of the previous footnote bodies
                    // that cannot be broken:
                    // add the whole newFootnotes length, so this breakpoint will be discarded
                    actualWidth += newFootnotes;
                    insertedFootnotesLength = ((KnuthPageNode) activeNode).totalFootnotes + newFootnotes;
                    footnoteListIndex = footnotesList.size() - 1;
                    footnoteElementIndex = ((LinkedList) footnotesList.get(footnoteListIndex)).size() - 1;
                }
            } else {
                // all footnotes have already been placed on previous pages
            }
        } else {
            // there are no footnotes
        }
        return lineWidth - actualWidth;
    }

    private int getFootnoteSplit(KnuthPageNode activeNode, int availableLength) {
        if (availableLength <= 0) {
            return 0;
        } else {
            // the split must contain a piece of the last footnote
            // together with all previous, not yet inserted footnotes
            int splitLength = 0;
            ListIterator noteListIterator = null;
            KnuthElement element = null;

            // add previous notes
            if (footnotesList.size() > 1) {
                splitLength = ((Integer) lengthList.get(footnotesList.size() - 2)).intValue()
                              - activeNode.totalFootnotes;
            }

            // add a split of the last note
            noteListIterator = ((LinkedList) footnotesList.get(footnotesList.size() - 1)).listIterator();
            boolean bSomethingAdded = false;
            int prevSplitLength = 0;
            int prevIndex = 0;
            int index = 0;

            while (!(bSomethingAdded && splitLength > availableLength)) {
                if (!bSomethingAdded) {
                    bSomethingAdded = true;
                } else {
                    prevSplitLength = splitLength;
                    prevIndex = index;
                }
                // get a sub-sequence from the note element list
                boolean bPrevIsBox = false;
                while (noteListIterator.hasNext()) {
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
            if (prevSplitLength > 0 ) {
                footnoteElementIndex = prevIndex;
            }
            return prevSplitLength;
        }
    }

    /**
     * Return the adjust ration needed to make up for the difference. A ration of 
     * <ul>
     *    <li>0 means that the break has the exact right width</li>
     *    <li>&gt;= -1 && &lt; 0  means that the break is to wider than the line, 
     *        but within the minimim values of the glues.</li> 
     *    <li>&gt;0 && &lt 1 means that the break is smaller than the line width, 
     *        but within the maximum values of the glues.</li>
     *    <li>&gt 1 means that the break is too small to make up for the glues.</li> 
     * </ul>
     * @param activeNode
     * @param difference
     * @return The ration.
     */
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

        if (bPendingFootnotes) {
            if (footnoteListIndex < footnotesList.size() - 1) {
                // add demerits for the deferred footnotes
                demerits += deferredFootnoteDemerits;
            } else if (footnoteElementIndex < ((LinkedList) footnotesList.get(footnoteListIndex)).size() - 1) {
                // add demerits for the footnote split between pages
                demerits += splitFootnoteDemerits;
            }
        }
        demerits += activeNode.totalDemerits;
        return demerits;
    }

    /**
     * Remove the first node in line 'line'. If the line then becomes empty, adjust the
     * startLine accordingly.
     * @param line
     * @param node
     */
    protected void removeNode(int line, KnuthNode node) {
        KnuthNode n = getNode(line);
        if (n != node) {
            if (bPendingFootnotes) {
                // nodes could be rightly deactivated in a different order
                KnuthNode prevNode = null;
                while (n != node) {
                    prevNode = n;
                    n = n.next;
                }
                prevNode.next = n.next;
                if (prevNode.next == null) {
                    activeLines[line*2+1] = prevNode;
                }
            } else {
                log.error("Should be first");
            }
        } else {
            activeLines[line*2] = node.next;
            if (node.next == null) {
                activeLines[line*2+1] = null;
            }
            while (startLine < endLine && getNode(startLine) == null) {
                startLine++;
            }
        }
        activeNodeCount--;
    }
    
    public LinkedList getPageBreaks() {
        return pageBreaks;
    }

    public void insertPageBreakAsFirst(PageBreakPosition pageBreak) {
        if (pageBreaks == null) {
            pageBreaks = new LinkedList();
        }
        pageBreaks.addFirst(pageBreak);
    }
    
    public void updateData1(int total, double demerits) {
    }

    public void updateData2(KnuthNode bestActiveNode,
                            KnuthSequence sequence,
                            int total) {
        //int difference = (bestActiveNode.line < total) ? bestActiveNode.difference : bestActiveNode.difference + fillerMinWidth;
        int difference = bestActiveNode.difference;
        int blockAlignment = (bestActiveNode.line < total) ? alignment : alignmentLast;
        double ratio = (blockAlignment == org.apache.fop.fo.Constants.EN_JUSTIFY
                        || bestActiveNode.adjustRatio < 0) ? bestActiveNode.adjustRatio : 0;

        // compute the indexes of the first footnote list and the first element in that list
        int firstListIndex = ((KnuthPageNode) bestActiveNode.previous).footnoteListIndex;
        int firstElementIndex = ((KnuthPageNode) bestActiveNode.previous).footnoteElementIndex;
        if (footnotesList != null
            && firstElementIndex == ((LinkedList) footnotesList.get(firstListIndex)).size() - 1) {
            // advance to the next list
            firstListIndex ++;
            firstElementIndex = 0;
        } else {
            firstElementIndex ++;
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
                bestActiveNode = compareNodes(bestActiveNode, node);
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
}
