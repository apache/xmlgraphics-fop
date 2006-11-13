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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.PageBreakingAlgorithm;
import org.apache.fop.layoutmgr.SpaceResolver;
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
     * During page breaking, records which out-of-line objects have already been handled,
     * and how much of them are placed on the current page.
     */
    class ProgressInfo {

        /** Index of the last inserted out-of-line object. */
        int lastInsertedIndex;

        /**
         * Index of the last inserted Knuth element of the last inserted out-of-line
         * object.
         */
        int lastElementOfLastInsertedIndex;

        /**
         * Amount of out-of-lines put on the current page.
         */
        ElasticLength alreadyInserted = new ElasticLength();

        /**
         * Number of times the last out-of-line is split. The initial value is 1, and is
         * reset each time the end of an out-of-line is reached. The purpose is to compute
         * additional demerits for split out-of-lines.
         */
        int nbSplit;

        /**
         * Creates and initializes a new record.
         *
         * @see OutOfLineRecord#initialize()
         */
        ProgressInfo() {
            initialize();
        }

        /**
         * Creates a copy of the given record.
         *
         * @param progressInfo original record
         */
        ProgressInfo(ProgressInfo progressInfo) {
            this.lastInsertedIndex = progressInfo.lastInsertedIndex;
            this.lastElementOfLastInsertedIndex = progressInfo.lastElementOfLastInsertedIndex;
            this.alreadyInserted.set(progressInfo.alreadyInserted);
            this.nbSplit = progressInfo.nbSplit;
        }

        /**
         * Returns the amount of out-of-lines inserted on the current page.
         *
         * @return the amount of out-of-lines inserted on the current page
         */
        public ElasticLength getInserted() {
            return alreadyInserted;
        }

        /**
         * Returns the number of times the last out-of-line is split.
         *
         * @return the number of times the last out-of-line is split
         */
        public int getNbSplit() {
            return nbSplit;
        }

        /**
         * Returns the index of the last element of the last inserted out-of-line.
         *
         * @return the index of the last element of the last inserted out-of-line
         */
        public int getLastElementOfLastInsertedIndex() {
            return lastElementOfLastInsertedIndex;
        }

        /**
         * Returns the index of the last inserted out-of-line.
         *
         * @return the index of the last inserted out-of-line
         */
        public int getLastInsertedIndex() {
            return lastInsertedIndex;
        }

        /**
         * Initializes this record such that no out-of-line has been inserted yet.
         */
        public void initialize() {
            lastInsertedIndex = -1;
            lastElementOfLastInsertedIndex = -1;
            alreadyInserted.reset();
            nbSplit = 1;
        }

        /**
         * Records progress status for out-of-lines up to the previous page.
         *
         * @param info progress informations for the previous page
         */
        public void setPrevious(ProgressInfo info) {
            lastInsertedIndex = info.lastInsertedIndex;
            lastElementOfLastInsertedIndex = info.lastElementOfLastInsertedIndex;
            if (lastInsertedIndex >= 0) {
                List lastOutOfLine = ((List) knuthSequences.get(lastInsertedIndex));
                // If the last out-of-line was split, go just before the first next box
                while (lastElementOfLastInsertedIndex < lastOutOfLine.size() - 2
                        && !((KnuthElement) lastOutOfLine.get(lastElementOfLastInsertedIndex + 1)).isBox()) {
                    lastElementOfLastInsertedIndex++;
                }
                if (lastElementOfLastInsertedIndex < lastOutOfLine.size() - 1) {
                    // We haven't reached the end of the out-of-line yet
                    nbSplit = info.nbSplit + 1;
                } else {
                    nbSplit = 1;
                }
            }
            alreadyInserted.reset();
        }

        /**
         * Checks whether there are still out-of-line objects to be placed.
         *
         * @return <code>true</code> if not all out-of-lines have been placed yet,
         * otherwise <code>false</code>
         */
        public boolean remaining() {
            return (lastInsertedIndex < knuthSequences.size() - 1) || isLastSplit();
        }

        /**
         * Returns the number of still to-be-placed out-of-lines.
         * @return the number of not yet typeset out-of-line objects.
         */
        public int getNbOfDeferred() {
            return knuthSequences.size() - 1 - lastInsertedIndex;
        }

        /**
         * Checks whether the end of the current out-of-line has been reached.
         *
         * @return <code>true</code> if the whole out-of-line has been placed, otherwise
         * <code>false</code>
         */
        boolean endOfOutOfLine() {
            return lastElementOfLastInsertedIndex
                    >= ((List) knuthSequences.get(lastInsertedIndex)).size() - 1;
        }

        /**
         * Checks whether the last out-of-line placed on the current page must be split or
         * not.
         *
         * @return <code>true</code> if the last typeset out-of-line object must be split on
         * several pages.
         */
        public boolean isLastSplit() {
            return lastInsertedIndex >= 0 && !endOfOutOfLine();
        }

        /**
         * Adds the dimensions of the separator between the out-of-line area and the main
         * content to the amount of out-of-lines already placed on the current page.
         */
        public void addSeparator() {
            alreadyInserted.add(separator);
        }

        /**
         * Places on the current page out-of-line content up to the next legal break in the current
         * out-of-line. This method is meant to be called by subclasses of this class, not
         * by external classes even in the same package.
         * <p><strong>Pre-condition:</strong> it is supposed that we are <em>inside</em>
         * the out-of-line, and that we haven't reached its end yet.
         */
        void nextInsideBreak() {
            List knuthSequence = (List) knuthSequences.get(lastInsertedIndex);
            Iterator elementIter = knuthSequence.listIterator(lastElementOfLastInsertedIndex + 1);
            boolean prevIsBox = false;
            do {
                lastElementOfLastInsertedIndex++;
                KnuthElement element = (KnuthElement) elementIter.next();
                if (element.isBox()) {
                    alreadyInserted.add(0, element.getW(), 0);
                    prevIsBox = true;
                } else if (element.isGlue()) {
                    if (prevIsBox) {
                        break;
                    }
                    alreadyInserted.add(element.getZ(), element.getW(), element.getY());
                    prevIsBox = false;
                } else {
                    if (element.getP() < KnuthElement.INFINITE) {
                        alreadyInserted.add(0, element.getW(), 0);
                        break;
                    }
                    prevIsBox = false;
                }
            } while (lastElementOfLastInsertedIndex < knuthSequence.size() - 1);
            if (lastElementOfLastInsertedIndex == knuthSequence.size() - 1) {
                nbSplit = 0;
            }
        }

        public String toString() {
            return "index=" + lastInsertedIndex
                    + ", elt=" + lastElementOfLastInsertedIndex
                    + ", inserted=" + alreadyInserted
                    + ", splits=" + nbSplit;
        }
    }

    /**
     * Sequences of KnuthElement corresponding to already encountered out-of-line objects.
     * This is a List of List of KnuthElement.
     */
    List knuthSequences = new ArrayList();

    /**
     * Dimension in the BPD of the separator between the out-of-line area and the main
     * area.
     */
    private ElasticLength separator = null;

    /**
     * Creates a new record for a given type of out-of-lines.
     *
     * @param separator dimensions of the separator between the out-of-line area and the
     * main area
     */
    public OutOfLineRecord(MinOptMax separator) {
        this.separator = new ElasticLength(separator.opt - separator.min,
                                           separator.opt,
                                           separator.max - separator.opt);
    }

    /**
     * Initializes this record, as if no out-of-line object were handled yet.
     */
    public void initialize() {
        knuthSequences = new ArrayList();
    }

    /**
     * Records one or more newly encountered out-of-line objects.
     *
     * @param elementLists the list of corresponding Knuth sequences. This is a
     * List&lt;List&lt;KnuthElement&gt;&gt;.
     */
    public void add(List elementLists) {
        ListIterator elementListsIterator = elementLists.listIterator();
        while (elementListsIterator.hasNext()) {
            List knuthSequence = (List) elementListsIterator.next();

            // Space resolution (Note: this does not respect possible stacking constraints
            // /between/ out-of-lines!)
            SpaceResolver.resolveElementList(knuthSequence);

            knuthSequences.add(knuthSequence);
        }
    }

    /**
     * Returns <code>true</code> if out-of-lines have already been encountered.
     *
     * @return <code>true</code> if out-of-lines are recorded, possibly not yet typeset
     */
    public boolean existing() {
        return knuthSequences.size() > 0;
    }

    /**
     * Returns the out-of-line object corresponding to the given index.
     * @param index index of the object
     * @return a List of KnuthElement corresponding to the object, or <code>null</code> if
     * it does not exist
     */
    public List getSequence(int index) {
        return (List) knuthSequences.get(index);
    }


    /**
     * When restarting the algorithm from a given point, reset the informations about
     * out-of-line objects to the values at that point.
     * @param elementLists out-of-line sequences which are met after the restarting point,
     * and thus must be removed from the list of already encountered objects.
     */
    public void reset(List elementLists) {
        for (int i = 0; i < elementLists.size(); i++) {
            knuthSequences.remove(knuthSequences.size() - 1);
        }
    }
}
