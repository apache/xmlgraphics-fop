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

import org.apache.fop.layoutmgr.PageBreakingAlgorithm;
import org.apache.fop.layoutmgr.PageBreakingAlgorithm.KnuthPageNode;
import org.apache.fop.traits.MinOptMax;

/**
 * A class that handles the placement of footnotes. Stores informations about
 * existing footnotes, already placed ones, split footnotes, etc.
 */
public class FootnotesRecord extends OutOfLineRecord {

    /**
     * Progress informations for footnotes. When building pages, footnotes content will
     * be put one unbreakable part at a time.
     */
    public class FootnotesProgress extends ProgressInfo {

        /**
         * Creates and initializes a new record.
         */
        public FootnotesProgress() {
            super();
        }

        /**
         * Creates a copy of the given record.
         *
         * @param footnotesProgress original progress information
         */
        public FootnotesProgress(FootnotesProgress footnotesProgress) {
            super(footnotesProgress);
        }

        /**
         * Checks whether there is some not yet typeset footnote content.
         *
         * @return <code>true</code> if the end of the footnote element list has not been
         * reached yet
         */
        private boolean hasNext() {
            return knuthSequences.size() > 0
                    && (lastInsertedIndex < knuthSequences.size() - 1 || !endOfOutOfLine());
        }

        /**
         * Insert footnote content on the current page up to the next legal break. This
         * may be within a footnote or at the end of one.
         */
        private void next() {
            if (lastInsertedIndex < 0 || endOfOutOfLine()) {
                // Go to the next footnote
                lastInsertedIndex++;
                lastElementOfLastInsertedIndex = -1;
                nbSplit = 1;
            } else {  // We are still inside a footnote
                nextInsideBreak();
            }
        }

        /**
         * If the last footnote of the previous page was split, places at least one more
         * chunk of it on the current page. This would look very weird if the rest of a
         * footnote split on one page would appear only two pages further. So this is
         * necessary to put at least one chunk on the current page. If this leads to an
         * unfeasible page, then the previous page will never appear in the optimal page
         * layout anyway.
         */
        public void handleSplit() {
            if (isLastSplit()) {
                next();
                addSeparator();
            }
        }

        /**
         * If the current page is a float-only page, handles the splitting of the last
         * footnote of the previous page. Usually by adding at least a chunk of it on the
         * current page, unless footnotes are not allowed on float-only pages (TODO this
         * may lead to weird results (footnote continued only two pages further)).
         *
         * @param mode one of {@link PageBreakingAlgorithm#FLOAT_PAGE_MODE} or {@link
         * PageBreakingAlgorithm#FLUSH_MODE}
         * @param activeNodeRecorder
         * @param normalContentProgress information about normal content already typeset
         * @param beforeFloatsProgress information about before-floats already typeset
         * @param previousNode node ending the previous page
         */
        public void handleSplit(int mode,
                         PageBreakingAlgorithm.ActiveNodeRecorder activeNodeRecorder,
                         PageBreakingAlgorithm.NormalContentProgressInfo normalContentProgress,
                         BeforeFloatsRecord.BeforeFloatsProgress beforeFloatsProgress,
                         KnuthPageNode previousNode) {
            if (isLastSplit()) {
                if (mode == PageBreakingAlgorithm.FLOAT_PAGE_MODE) {
                    if (PageBreakingAlgorithm.FOOTNOTES_ALLOWED_ON_FLOAT_PAGES) {
                        next();
                        addSeparator();
                        if (PageBreakingAlgorithm.FOOTNOTES_ONLY_PAGES_ALLOWED) {
                            activeNodeRecorder.handleNode(mode, normalContentProgress,
                                    this, beforeFloatsProgress, previousNode);
                        }
                    }
                } else {  // mode == PageBreakingAlgorithm.FLUSH_MODE
                    next();
                    addSeparator();
                    activeNodeRecorder.handleNode(mode, normalContentProgress,
                            this, beforeFloatsProgress, previousNode);
                }
            }
        }

        /**
         * Considers the placement of footnotes on the current page.
         *
         * @param mode one of {@link PageBreakingAlgorithm#NORMAL_MODE}, {@link
         * PageBreakingAlgorithm#FLOAT_PAGE_MODE} or {@link
         * PageBreakingAlgorithm#FLUSH_MODE}
         * @param activeNodeRecorder
         * @param normalContentProgress information about normal content already typeset
         * @param beforeFloatsProgress information about before-floats already typeset
         * @param previousNode node ending the previous page
         */
        public void consider(int mode,
                             PageBreakingAlgorithm.ActiveNodeRecorder activeNodeRecorder,
                             PageBreakingAlgorithm.NormalContentProgressInfo normalContentProgress,
                             BeforeFloatsRecord.BeforeFloatsProgress beforeFloatsProgress,
                             KnuthPageNode previousNode) {
            beforeFloatsProgress.consider(mode, activeNodeRecorder,
                    normalContentProgress, this, previousNode);
            if (alreadyInserted.getLength() == 0) {
                addSeparator();
            }
            switch (mode) {
            case PageBreakingAlgorithm.NORMAL_MODE:
                while (hasNext()) {
                    next();
                    if (!activeNodeRecorder.handleNode(mode, normalContentProgress,
                            this, beforeFloatsProgress, previousNode)) {
                        break;
                    }
                    beforeFloatsProgress.consider(mode, activeNodeRecorder,
                            normalContentProgress, this, previousNode);
                }
                break;
            case PageBreakingAlgorithm.FLOAT_PAGE_MODE:
                if (PageBreakingAlgorithm.FOOTNOTES_ALLOWED_ON_FLOAT_PAGES) {
                    while (hasNext()) {
                        next();
                        if (PageBreakingAlgorithm.FOOTNOTES_ONLY_PAGES_ALLOWED) {
                            if (!activeNodeRecorder.handleNode(mode, normalContentProgress,
                                    this, beforeFloatsProgress, previousNode)) {
                                break;
                            }
                        }
                        beforeFloatsProgress.consider(mode, activeNodeRecorder,
                                normalContentProgress, this, previousNode);
                    }
                }
                break;
            case PageBreakingAlgorithm.FLUSH_MODE:
                while (hasNext()) {
                    next();
                    if (!activeNodeRecorder.handleNode(mode, normalContentProgress,
                            this, beforeFloatsProgress, previousNode)) {
                        break;
                    }
                    beforeFloatsProgress.consider(mode, activeNodeRecorder,
                            normalContentProgress, this, previousNode);
                }
                break;
            }
        }
    }

    /**
     * Creates a new record for handling footnotes.
     *
     * @param footnoteSeparator dimensions of the separator between the normal content and
     * the footnote area
     */
    public FootnotesRecord(MinOptMax footnoteSeparator) {
        super(footnoteSeparator);
    }
}
