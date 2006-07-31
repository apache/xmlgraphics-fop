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

package org.apache.fop.layoutmgr.breaking;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.PageBreakingAlgorithm;
import org.apache.fop.layoutmgr.SpaceResolver;
import org.apache.fop.layoutmgr.PageBreakingAlgorithm.KnuthPageNode;
import org.apache.fop.traits.MinOptMax;

/**
 * Helper class for dealing with out-of-line objects (before-floats and footnotes) when
 * breaking text into pages. It stores the necessary informations to place out-of-line
 * objects, and provides methods to manipulate them.
 * 
 * @see PageBreakingAlgorithm
 */
public class OutOfLineRecord {
    
    /**
     * Stores informations about how many out-of-line objects have already been handled.
     */
    public static class ProgressInfo {

        /** Cumulated BPD length of all out-of-line objects inserted so far. */
        private int alreadyInsertedLength = 0;

        /** Index of the last inserted out-of-line object. */
        private int lastInsertedIndex = -1;

        /**
         * Index of the last inserted Knuth element of the last inserted out-of-line
         * object. Currently only used for footnotes, as before-floats may not be split on
         * several pages. Might be useful when later dealing with floats that cannot even
         * be put on a page alone, however.
         */
        private int lastElementOfLastInsertedIndex = -1;
        
        /**
         * Initializes this record, as if no out-of-line object were handled yet.
         */
        private void initialize() {
            alreadyInsertedLength = 0;
            lastInsertedIndex = -1;
            lastElementOfLastInsertedIndex = -1;
        }

        /**
         * @return a copy of this record
         */
        public ProgressInfo copy() {
            ProgressInfo info = new ProgressInfo();
            info.alreadyInsertedLength = alreadyInsertedLength;
            info.lastInsertedIndex = lastInsertedIndex;
            info.lastElementOfLastInsertedIndex = lastElementOfLastInsertedIndex;
            return info;
        }

        /**
         * Returns the cumulated length of all already typeset out-of-line objects.
         * @return the total length in the block-progression-direction
         */
        public int getAlreadyInsertedLength() {
            return alreadyInsertedLength;
        }

        /**
         * Returns the index of the last element of the last already typeset out-of-line
         * object.
         * @return the index of the last placed KnuthElement
         */
        public int getLastElementOfLastInsertedIndex() {
            return lastElementOfLastInsertedIndex;
        }

        /**
         * @return the index of the last already typeset out-of-line object.
         */
        public int getLastInsertedIndex() {
            return lastInsertedIndex;
        }

        public String toString() {
            return "length=" + alreadyInsertedLength
                    + ", index=" + lastInsertedIndex
                    + ", elt=" + lastElementOfLastInsertedIndex;
        }
    }

    /**
     * Sequences of KnuthElement corresponding to already encountered out-of-line objects.
     * This is a List of List of KnuthElement.
     */
    private List knuthSequences = null;

    /**
     * Each element of this list corresponds to the cumulated length in the BPD of all the
     * out-of-line objects up to the given index. This is a List of Integer.
     * 
     * @see OutOfLineRecord#knuthSequences 
     */
    private List cumulativeLengths = null;

    /**
     * True if new out-of-line objects are cited in the sequence of Knuth elements since
     * the last encountered legal breakpoint.
     * 
     * @see OutOfLineRecord#newSinceLastBreakpoint()
     */
    private boolean newSinceLastBreakpoint = false;

    /**
     * Index of the first newly encountered out-of-line object since the last legal
     * breakpoint.
     * 
     * @see OutOfLineRecord#knuthSequences
     */
    private int firstNewIndex = 0;

    /**
     * Dimension in the BPD of the separator between the out-of-line area and the main
     * area.
     */
    private MinOptMax separatorLength = null;

    /**
     * Record of already handled out-of-line objects.
     * 
     * @see ProgressInfo
     */
    private ProgressInfo progressInfo;

    public OutOfLineRecord(MinOptMax separatorLength) {
        this.separatorLength = separatorLength;
        this.progressInfo = new ProgressInfo();
    }

    /**
     * Initializes this record, as if no out-of-line object were handled yet.
     */
    public void initialize() {
        knuthSequences = null;
        cumulativeLengths = null;
        newSinceLastBreakpoint = false;
        firstNewIndex = 0;
        progressInfo.initialize();
    }

    /**
     * @return the informations about already handled out-of-line objects
     */
    public ProgressInfo getProgress() {
        return this.progressInfo;
    }

    /**
     * @return the length in the BPD of the separator between the out-of-line area and the
     * main area.
     */
    public MinOptMax getSeparatorLength() {
        return separatorLength;
    }

    /**
     * @return the total length of already encountered out-of-line objects
     */
    public int getTotalLength() {
        if (cumulativeLengths == null || cumulativeLengths.size() == 0) {
            return 0;
        } else {
            return ((Integer) cumulativeLengths.get(cumulativeLengths.size() - 1)).intValue(); 
        }
    }

    /**
     * @return true if out-of-line objects have already been encountered (but not
     * necessarily typeset yet)
     */
    public boolean existing() {
        return (knuthSequences != null && knuthSequences.size() > 0);
    }

    public void resetNewSinceLastBreakpoint() {
        newSinceLastBreakpoint = false;
    }

    /**
     * @return true if new out-of-line objects are cited in the sequence of Knuth
     * elements since the last encountered legal breakpoint.
     */
    public boolean newSinceLastBreakpoint() {
        return newSinceLastBreakpoint;
    }

    /**
     * Records one or more newly encountered out-of-line objects.
     * @param elementLists the list of corresponding Knuth sequences
     */
    public void add(List elementLists) {
        // Initialize stuff if necessary
        if (knuthSequences == null) {
            knuthSequences = new ArrayList();
            cumulativeLengths = new ArrayList();
        }
        if (!newSinceLastBreakpoint) {
            newSinceLastBreakpoint = true;
            firstNewIndex = knuthSequences.size();
        }
        // compute the total length of the footnotes
        ListIterator elementListsIterator = elementLists.listIterator();
        while (elementListsIterator.hasNext()) {
            LinkedList noteList = (LinkedList) elementListsIterator.next();
            
            //Space resolution (Note: this does not respect possible stacking constraints 
            //between footnotes!)
            SpaceResolver.resolveElementList(noteList);
            
            int noteLength = 0;
            knuthSequences.add(noteList);
            ListIterator noteListIterator = noteList.listIterator();
            while (noteListIterator.hasNext()) {
                KnuthElement element = (KnuthElement) noteListIterator.next();
                if (element.isBox() || element.isGlue()) {
                    noteLength += element.getW();
                }
            }
            cumulativeLengths.add(new Integer(getTotalLength() + noteLength));
        }
    }

    /**
     * Sets the progress informations to the given values. Called whenever a new active
     * node is considered; the informations regarding already handled out-of-line objects
     * must be set to the active node's values in order to know from where to start the
     * placement of further objects.
     *
     * @param info progress informations of the currently considered active node
     */
    public void setProgress(ProgressInfo info) {
        this.progressInfo.alreadyInsertedLength = info.alreadyInsertedLength;
        this.progressInfo.lastElementOfLastInsertedIndex = info.lastElementOfLastInsertedIndex;
        this.progressInfo.lastInsertedIndex = info.lastInsertedIndex;
    }

    /* Unless I'm wrong, newOnThisPagePlusPiecesFromPrevious always implies
     * notAllInserted. And if A => B, then A && B <=> B
     * So this code may be simplified, see deferred() below
     */
    /**
//   * Returns true if their are (pieces of) footnotes to be typeset on the
//   * current page.
//   * @param listIndex index of the last inserted footnote for the
//   * currently considered active node
//   * @param elementIndex index of the last element of the last inserted footnote
//   * @param length total length of all footnotes inserted so far
//   */
//  public boolean deferredFootnotes(ProgressInfo progressInfo) {
//      boolean newOnThisPagePlusPiecesFromPrevious =
//              newSinceLastBreakpoint()
//              && firstNewIndex != 0
//              && (progressInfo.lastInsertedIndex < firstNewIndex - 1
//                  || progressInfo.lastElementOfLastInsertedIndex <
//                      ((LinkedList) knuthSequences.get(progressInfo.lastInsertedIndex)).size() - 1);
//      boolean notAllInserted = progressInfo.alreadyInsertedLength < getTotalLength();
//      return notAllInserted;
//  }

    /**
     * @return <code>true</code> if some out-of-line objects have not already been
     * typeset.
     */
    public boolean deferred() {
        return progressInfo.alreadyInsertedLength < getTotalLength();
    }

    /**
     * @return the number of not yet typeset out-of-line objects.
     */
    public int getNbOfDeferred() {
        return knuthSequences.size() - 1 - progressInfo.lastInsertedIndex;
    }

    /**
     * @return <code>true</code> if the last typeset out-of-line object must be split on
     * several pages.
     */
    public boolean isSplit() {
        return (progressInfo.lastElementOfLastInsertedIndex 
                < ((LinkedList) knuthSequences.get(progressInfo.lastInsertedIndex)).size() - 1);
    }

    /**
     * Returns the out-of-line object corresponding to the given index.
     * @param index index of the object
     * @return a List of KnuthElement corresponding to the object, or <code>null</code> if
     * it does not exist
     */
    public List getSequence(int index) {
        /*TODO vh: bof */
        if (knuthSequences == null) {
            return null;
        } else {
            return (List) knuthSequences.get(index);
        }
    }

    /**
     * Tries to split the flow of footnotes to put one part on the current page.
     * @param prevNodeProgress informations about footnotes already inserted on the
     * previous page
     * @param availableLength available space for footnotes on this page
     * @param canDeferOldFootnotes
     * @return the length of footnotes which could be inserted on this page
     */
    public int getFootnoteSplit(ProgressInfo prevNodeProgress,
                                int availableLength, boolean canDeferOldFootnotes) {
        if (availableLength <= 0) {
            progressInfo.alreadyInsertedLength = prevNodeProgress.getAlreadyInsertedLength();
            return 0;
        } else {
            // the split should contain a piece of the last footnote
            // together with all previous, not yet inserted footnotes;
            // but if this is not possible, try adding as much content as possible
            int splitLength = 0;
            ListIterator noteListIterator = null;
            KnuthElement element = null;
            boolean somethingAdded = false;

            // prevNodeProgress.lastInsertedIndex and
            // prevNodeProgress.lastElementOfLastInsertedIndex points to the last footnote element
            // already placed in a page: advance to the next element
            int listIndex = prevNodeProgress.lastInsertedIndex;
            int elementIndex = prevNodeProgress.lastElementOfLastInsertedIndex;
            if (listIndex == -1
                    || elementIndex == ((LinkedList) knuthSequences.get(listIndex)).size() - 1) {
                listIndex++;
                elementIndex = 0;
            } else {
                elementIndex++;
            }

            // try adding whole notes
            // if there are more than 1 footnote to insert
            if (knuthSequences.size() - 1 > listIndex) {
                // add the previous footnotes: these cannot be broken or deferred
                if (!canDeferOldFootnotes
                    && newSinceLastBreakpoint()
                    && firstNewIndex > 0) {
                    splitLength = ((Integer) cumulativeLengths.get(firstNewIndex - 1)).intValue()
                                  - prevNodeProgress.alreadyInsertedLength;
                    listIndex = firstNewIndex;
                    elementIndex = 0;
                }
                // try adding the new footnotes
                while (((Integer) cumulativeLengths.get(listIndex)).intValue()
                        - prevNodeProgress.alreadyInsertedLength <= availableLength) {
                    splitLength = ((Integer) cumulativeLengths.get(listIndex)).intValue()
                                  - prevNodeProgress.alreadyInsertedLength;
                    somethingAdded = true;
                    listIndex++;
                    elementIndex = 0;
                }
                // as this method is called only if it is not possible to insert
                // all footnotes, at this point listIndex and elementIndex points to
                // an existing element, the next one we will try to insert 
            }

            // try adding a split of the next note
            noteListIterator = ((List) knuthSequences.get(listIndex)).listIterator(elementIndex);

            int prevSplitLength = 0;
            int prevIndex = -1;
            int index = -1;

            while (!somethingAdded || splitLength <= availableLength) {
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
                progressInfo.lastInsertedIndex = (prevIndex != -1) ? listIndex : listIndex - 1;
                progressInfo.lastElementOfLastInsertedIndex = (prevIndex != -1)
                    ? prevIndex 
                    : ((LinkedList) knuthSequences.get(progressInfo.lastInsertedIndex)).size() - 1;
            }
            progressInfo.alreadyInsertedLength
                    = prevNodeProgress.getAlreadyInsertedLength() + prevSplitLength;
            return prevSplitLength;
        }
    }

    /**
     * Tries to split the flow of floats to put some floats on the current page.
     * @param prevProgress floats already inserted on the previous page
     * @param availableLength available space for floats
     * @return the length of floats which could be placed on the current page
     */
    public int getFloatSplit(ProgressInfo prevProgress, int availableLength) {
        /*
         * Normally this method is called only when there is some place for
         * floats => availableLength > 0
         */
        int splitLength = 0;
        int listIndex = prevProgress.lastInsertedIndex + 1;

        while (listIndex < knuthSequences.size()
                && ((Integer) cumulativeLengths.get(listIndex)).intValue()
                    - prevProgress.alreadyInsertedLength <= availableLength) {
            splitLength = ((Integer) cumulativeLengths.get(listIndex)).intValue()
                    - prevProgress.alreadyInsertedLength;
            listIndex++;
        }
        progressInfo.lastInsertedIndex = listIndex - 1;
        progressInfo.alreadyInsertedLength = prevProgress.alreadyInsertedLength + splitLength;
        return splitLength;
    }

    /**
     * Places on the current page all of the out-of-line objects not yet inserted.
     */
    public void insertAll() {
        progressInfo.alreadyInsertedLength = getTotalLength();
        progressInfo.lastInsertedIndex = knuthSequences.size() - 1;
        progressInfo.lastElementOfLastInsertedIndex
                = ((List) knuthSequences.get(progressInfo.lastInsertedIndex)).size() - 1;        
    }

    /**
     * When restarting the algorithm from a given point, reset the informations about
     * out-of-line objects to the values at that point.
     * @param elementLists out-of-line sequences which are met after the restarting point,
     * and thus must be removed from the list of already encoutered objects.
     */
    public void reset(List elementLists) {
        for (int i = 0; i < elementLists.size(); i++) {
            knuthSequences.remove(knuthSequences.size() - 1);
            cumulativeLengths.remove(cumulativeLengths.size() - 1);
        }
    }

    /**
     * When the whole normal flow has been typeset and there are still footnotes to be
     * placed, creates as many pages as necessary to place them.
     */
    public void createFootnotePages(KnuthPageNode lastNode, PageBreakingAlgorithm algo, int lineWidth) {
        progressInfo.alreadyInsertedLength = lastNode.footnotesProgress.getAlreadyInsertedLength();
        progressInfo.lastInsertedIndex = lastNode.footnotesProgress.getLastInsertedIndex();
        progressInfo.lastElementOfLastInsertedIndex = lastNode.footnotesProgress.getLastElementOfLastInsertedIndex();
        int availableBPD = lineWidth;
        int split = 0;
        KnuthPageNode prevNode = lastNode;

        // create pages containing the remaining footnote bodies
        while (progressInfo.alreadyInsertedLength < getTotalLength()) {
            // try adding some more content
            if (((Integer) cumulativeLengths.get(progressInfo.lastInsertedIndex)).intValue() - progressInfo.alreadyInsertedLength
                <= availableBPD) {
                // add a whole footnote
                availableBPD -= ((Integer) cumulativeLengths.get(progressInfo.lastInsertedIndex)).intValue()
                                - progressInfo.alreadyInsertedLength;
                progressInfo.alreadyInsertedLength = ((Integer)cumulativeLengths.get(progressInfo.lastInsertedIndex)).intValue();
                progressInfo.lastElementOfLastInsertedIndex
                    = ((LinkedList)knuthSequences.get(progressInfo.lastInsertedIndex)).size() - 1;
            } else if ((split = getFootnoteSplit(progressInfo, availableBPD, true))
                       > 0) {
                // add a piece of a footnote
                availableBPD -= split;
                // footnoteListIndex has already been set in getFootnoteSplit()
                // footnoteElementIndex has already been set in getFootnoteSplit()
            } else {
                // cannot add any content: create a new node and start again
                KnuthPageNode node = (KnuthPageNode)
                                     algo.createNode(lastNode.position, prevNode.line + 1, 1,
                                                progressInfo.alreadyInsertedLength - prevNode.footnotesProgress.getAlreadyInsertedLength(), 
                                                0, 0,
                                                0, 0, 0,
                                                0, 0, prevNode);
                algo.addNode(node.line, node);
                algo.removeNode(prevNode.line, prevNode);

                prevNode = node;
                availableBPD = lineWidth;
            }
        }
        // create the last node
        KnuthPageNode node = (KnuthPageNode)
                             algo.createNode(lastNode.position, prevNode.line + 1, 1,
                                        getTotalLength() - prevNode.footnotesProgress.getAlreadyInsertedLength(), 0, 0,
                                        0, 0, 0,
                                        0, 0, prevNode);
        algo.addNode(node.line, node);
        algo.removeNode(prevNode.line, prevNode);
    }

    /* TODO vh: won't work when there are also footnotes. To be merged with createFootnotePages */
    public void createFloatPages(KnuthPageNode lastNode, PageBreakingAlgorithm algo, int lineWidth) {
        progressInfo.alreadyInsertedLength = lastNode.floatsProgress.getAlreadyInsertedLength();
        progressInfo.lastInsertedIndex = lastNode.floatsProgress.getLastInsertedIndex();
        int availableBPD = lineWidth;
        KnuthPageNode prevNode = lastNode;

        // create pages containing the remaining float bodies
        while (progressInfo.alreadyInsertedLength < getTotalLength()) {
            // try adding some more content
            if (((Integer) cumulativeLengths.get(progressInfo.lastInsertedIndex + 1)).intValue() - progressInfo.alreadyInsertedLength
                <= availableBPD) {
                // add a whole float
                progressInfo.lastInsertedIndex++;
                availableBPD -= ((Integer) cumulativeLengths.get(progressInfo.lastInsertedIndex)).intValue()
                                - progressInfo.alreadyInsertedLength;
                progressInfo.alreadyInsertedLength = ((Integer)cumulativeLengths.get(progressInfo.lastInsertedIndex)).intValue();
            } else {
                // cannot add any content: create a new node and start again
                KnuthPageNode node = (KnuthPageNode)
                                     algo.createNode(lastNode.position, prevNode.line + 1, 1,
                                                progressInfo.alreadyInsertedLength - prevNode.floatsProgress.getAlreadyInsertedLength(), 
                                                0, 0,
                                                0, 0, 0,
                                                0, prevNode.totalDemerits + (progressInfo.lastInsertedIndex - prevNode.floatsProgress.lastInsertedIndex) * 10000, prevNode);
                algo.addNode(node.line, node);
                algo.removeNode(prevNode.line, prevNode);

                prevNode = node;
                availableBPD = lineWidth;
            }
        }
        // create the last node
        KnuthPageNode node = (KnuthPageNode)
                             algo.createNode(lastNode.position, prevNode.line + 1, 1,
                                        getTotalLength() - prevNode.floatsProgress.getAlreadyInsertedLength(), 0, 0,
                                        0, 0, 0,
                                        0, prevNode.totalDemerits + (progressInfo.lastInsertedIndex - prevNode.floatsProgress.lastInsertedIndex) * 10000, prevNode);
        algo.addNode(node.line, node);
        algo.removeNode(prevNode.line, prevNode);
    }
}
