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
import org.apache.fop.layoutmgr.PageBreakingAlgorithm.KnuthPageNode;
import org.apache.fop.traits.MinOptMax;

/**
 * A class that handles the placement of before-floats. Stores informations about
 * existing before-floats, already placed ones, split floats, etc.
 */
public class BeforeFloatsRecord extends OutOfLineRecord {

    /**
     * Dimensions of the recorded before-floats. As the whole dimensions of floats are
     * often required to determine placements, they are computed only once and stored in a
     * dedicated field.
     */
    private List elasticLengths = new ArrayList();

    /**
     * Progress informations for before-floats. Among other things, this class handles the
     * splitting of floats over several pages.
     */
    public class BeforeFloatsProgress extends ProgressInfo {

        /**
         * Creates and initializes a new record.
         */
        public BeforeFloatsProgress() {
            super();
        }

        /**
         * Creates a copy of the given record.
         *
         * @param beforeFloatProgress original progress information
         */
        public BeforeFloatsProgress(BeforeFloatsProgress beforeFloatProgress) {
            super(beforeFloatProgress);
        }

        /**
         * Checks whether there is still an entire float which has not been handled.
         *
         * @return <code>true</code> if there is still at least one entire non-handled
         * float
         */
        private boolean hasNextFull() {
            return lastInsertedIndex < knuthSequences.size() - 1;
        }

        /**
         * Places on the current page the next entire float, without considering to split
         * it.
         */
        private void nextFull() {
            lastInsertedIndex++;
            lastElementOfLastInsertedIndex
                    = ((List) knuthSequences.get(lastInsertedIndex)).size() - 1;
            alreadyInserted.add((ElasticLength) elasticLengths.get(lastInsertedIndex)); 
        }

        /**
         * If the float on the previous page was split, put the rest of it on the current
         * page. This method is only called when typesetting a page which also has normal
         * content. If the float on the previous page was split, this implies that this
         * was a float-only page with only one float (no normal content, no footnote, no
         * other float). This is the only case where a before-float is allowed to be
         * split.
         */
        public void handleSplit() {
            if (isLastSplit()) {
                do {
                    nextInsideBreak();
                } while (!endOfOutOfLine());
                addSeparator();
            }
        }

        /**
         * If the float on the previous page was split, put the rest of it on the current
         * page. This method is called when building a float-only page, either inside a
         * page sequence or at the end of it.
         *
         * @param mode one of {@link PageBreakingAlgorithm#FLOAT_PAGE_MODE} or {@link
         * PageBreakingAlgorithm#FLUSH_MODE}
         * @param activeNodeRecorder
         * @param normalContentProgress information about normal content already typeset
         * @param footnotesProgress information about footnotes already typeset
         * @param previousNode node ending the previous page
         */
        public void handleSplit(int mode,
                         PageBreakingAlgorithm.ActiveNodeRecorder activeNodeRecorder,
                         PageBreakingAlgorithm.NormalContentProgressInfo normalContentProgress,
                         FootnotesRecord.FootnotesProgress footnotesProgress,
                         KnuthPageNode previousNode) {
            if (isLastSplit()) {
                do {
                    nextInsideBreak();
                    if (!activeNodeRecorder.handleNode(mode, normalContentProgress,
                            footnotesProgress, this, previousNode)) {
                        break;
                    }
                } while (!endOfOutOfLine());
            }
        }

        /**
         * Considers the placement of floats on the current page.
         *
         * @param mode one of {@link PageBreakingAlgorithm#NORMAL_MODE}, {@link
         * PageBreakingAlgorithm#FLOAT_PAGE_MODE} or {@link
         * PageBreakingAlgorithm#FLUSH_MODE}
         * @param activeNodeRecorder
         * @param normalContentProgress information about normal content already typeset
         * @param footnotesProgress information about footnotes already typeset
         * @param previousNode node ending the previous page
         */
        public void consider(int mode,
                             PageBreakingAlgorithm.ActiveNodeRecorder activeNodeRecorder,
                             PageBreakingAlgorithm.NormalContentProgressInfo normalContentProgress,
                             FootnotesRecord.FootnotesProgress footnotesProgress,
                             KnuthPageNode previousNode) {
            setPrevious(previousNode.beforeFloatsProgress);
            switch (mode) {
            case PageBreakingAlgorithm.NORMAL_MODE:
                if (alreadyInserted.getLength() == 0) {
                    addSeparator();
                }
                while (hasNextFull()) {
                    nextFull();
                    if (!activeNodeRecorder.handleNode(mode, normalContentProgress,
                            footnotesProgress, this, previousNode)) {
                        break;
                    }
                }
                break;
            case PageBreakingAlgorithm.FLOAT_PAGE_MODE:
            case PageBreakingAlgorithm.FLUSH_MODE:
                if (footnotesProgress.alreadyInserted.getLength() == 0
                        && alreadyInserted.getLength() == 0
                        && hasNextFull()) {  // first split allowed
                    lastInsertedIndex++;
                    lastElementOfLastInsertedIndex = -1;
                    do {
                        nextInsideBreak();
                        if (!activeNodeRecorder.handleNode(mode, normalContentProgress,
                                footnotesProgress, this, previousNode)) {
                            break;
                        }
                    } while (!endOfOutOfLine());
                }
                while (hasNextFull()) {
                    nextFull();
                    if (!activeNodeRecorder.handleNode(mode, normalContentProgress,
                            footnotesProgress, this, previousNode)) {
                        break;
                    }
                }
                break;
            }
        }
    }
    
    /**
     * Creates a new record for handling before-floats.
     *
     * @param separator dimensions of the separator between the before-float area
     * and the normal content
     */
    public BeforeFloatsRecord(MinOptMax separator) {
        super(separator);
    }

    public void initialize() {
        super.initialize();
        elasticLengths = new ArrayList();
    }

    public void add(List elementLists) {
        // compute the total length of the footnotes
        ListIterator elementListsIterator = elementLists.listIterator();
        while (elementListsIterator.hasNext()) {
            List knuthSequence = (List) elementListsIterator.next();
            
            //Space resolution (Note: this does not respect possible stacking constraints 
            //between footnotes!)
            SpaceResolver.resolveElementList(knuthSequence);
            
            knuthSequences.add(knuthSequence);
            Iterator elementIter = knuthSequence.iterator();
            ElasticLength floatDims = new ElasticLength();
            while (elementIter.hasNext()) {
                KnuthElement element = (KnuthElement) elementIter.next();
                if (element.isBox()) {
                    floatDims.add(0, element.getW(), 0);
                } else if (element.isGlue()) {
                    floatDims.add(element.getZ(), element.getW(), element.getY());
                }
            }
            elasticLengths.add(floatDims);
        }
    }

    public void reset(List elementLists) {
        super.reset(elementLists);
        for (int i = 0; i < elementLists.size(); i++) {
            elasticLengths.remove(elasticLengths.size() - 1);
        }
    }
}
