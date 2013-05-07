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
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.layoutmgr.AbstractBreaker.PageBreakPosition;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.util.ListUtil;

class PageBreakingAlgorithm extends BreakingAlgorithm {

    /** the logger for the class */
    private static Log log = LogFactory.getLog(PageBreakingAlgorithm.class);

    private LayoutManager topLevelLM;
    private PageProvider pageProvider;
    private PageBreakingLayoutListener layoutListener;
    /** List of PageBreakPosition elements. */
    private LinkedList<PageBreakPosition> pageBreaks = null;

    /** Footnotes which are cited between the currently considered active node (previous
     * break) and the current considered break. Its type is
     * List&lt;List&lt;KnuthElement&gt;&gt;, it contains the sequences of KnuthElement
     * representing the footnotes bodies.
     */
    private List<List<KnuthElement>> footnotesList = null;
    /** Cumulated bpd of unhandled footnotes. */
    private List<Integer> lengthList = null;
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
    /** True if the elements met after the previous break point contain footnote citations. */
    private boolean newFootnotes = false;
    /** Index of the first footnote met after the previous break point. */
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

    private int ipdDifference;
    private KnuthNode bestNodeForIPDChange;

    //Used to keep track of switches in keep-context
    private int currentKeepContext = Constants.EN_AUTO;
    private KnuthNode lastBeforeKeepContextSwitch;

    /**
     * Construct a page breaking algorithm.
     * @param topLevelLM the top level layout manager
     * @param pageProvider the page provider
     * @param layoutListener the layout listener
     * @param alignment     alignment of the paragraph/page. One of {@link Constants#EN_START},
     *                  {@link Constants#EN_JUSTIFY}, {@link Constants#EN_CENTER},
     *                  {@link Constants#EN_END}.
     *                  For pages, {@link Constants#EN_BEFORE} and {@link Constants#EN_AFTER}
     *                  are mapped to the corresponding inline properties,
     *                  {@link Constants#EN_START} and {@link Constants#EN_END}.
     * @param alignmentLast alignment of the paragraph's last line
     * @param footnoteSeparatorLength length of footnote separator
     * @param partOverflowRecovery  {@code true} if too long elements should be moved to
     *                              the next line/part
     * @param autoHeight true if auto height
     * @param favorSinglePart true if favoring single part
     * @see BreakingAlgorithm
     */
    public PageBreakingAlgorithm(LayoutManager topLevelLM,      // CSOK: ParameterNumber
                                 PageProvider pageProvider,
                                 PageBreakingLayoutListener layoutListener,
                                 int alignment, int alignmentLast,
                                 MinOptMax footnoteSeparatorLength,
                                 boolean partOverflowRecovery, boolean autoHeight,
                                 boolean favorSinglePart) {
        super(alignment, alignmentLast, true, partOverflowRecovery, 0);
        this.topLevelLM = topLevelLM;
        this.pageProvider = pageProvider;
        this.layoutListener = layoutListener;
        best = new BestPageRecords();
        this.footnoteSeparatorLength = footnoteSeparatorLength;
        this.autoHeight = autoHeight;
        this.favorSinglePart = favorSinglePart;
    }

    /**
     * This class represents a feasible breaking point
     * with extra information about footnotes.
     */
    protected class KnuthPageNode extends KnuthNode {

        /** Additional length due to footnotes. */
        public int totalFootnotes;                              // CSOK: VisibilityModifier

        /** Index of the last inserted footnote. */
        public int footnoteListIndex;                           // CSOK: VisibilityModifier

        /** Index of the last inserted element of the last inserted footnote. */
        public int footnoteElementIndex;                        // CSOK: VisibilityModifier

        public KnuthPageNode(int position,                      // CSOK: ParameterNumber
                             int line, int fitness,
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

    /** {@inheritDoc} */
    @Override
    protected void initialize() {
        super.initialize();
        insertedFootnotesLength = 0;
        footnoteListIndex = 0;
        footnoteElementIndex = -1;
    }

    /**
     * Overridden to defer a part to the next page, if it
     * must be kept within one page, but is too large to fit in
     * the last column.
     * {@inheritDoc}
     */
    @Override
    protected KnuthNode recoverFromTooLong(KnuthNode lastTooLong) {

        if (log.isDebugEnabled()) {
            log.debug("Recovering from too long: " + lastTooLong);
            log.debug("\tlastTooShort = " + getLastTooShort());
            log.debug("\tlastBeforeKeepContextSwitch = " + lastBeforeKeepContextSwitch);
            log.debug("\tcurrentKeepContext = "
                      + AbstractBreaker.getBreakClassName(currentKeepContext));
        }

        if (lastBeforeKeepContextSwitch == null
                || currentKeepContext == Constants.EN_AUTO) {
            return super.recoverFromTooLong(lastTooLong);
        }

        KnuthNode node = lastBeforeKeepContextSwitch;
        lastBeforeKeepContextSwitch = null;
        // content would overflow, insert empty page/column(s) and try again
        while (!pageProvider.endPage(node.line - 1)) {
            log.trace("Adding node for empty column");
            node = createNode(
                    node.position,
                    node.line + 1, 1,
                    0, 0, 0,
                    0, 0, 0,
                    0, 0, node);
        }
        return node;
    }

    /**
     * Compare two KnuthNodes and return the node with the least demerit.
     *
     * @param node1 The first knuth node.
     * @param node2 The other knuth node.
     * @return the node with the least demerit.
     */
    @Override
    protected KnuthNode compareNodes(KnuthNode node1, KnuthNode node2) {

        /* if either node is null, return the other one */
        if (node1 == null || node2 == null) {
            return (node1 == null) ? node2 : node1;
        }

        /* if either one of the nodes corresponds to a mere column-break,
         * and the other one corresponds to a page-break, return the page-break node
         */
        if (pageProvider != null) {
            if (pageProvider.endPage(node1.line - 1)
                    && !pageProvider.endPage(node2.line - 1)) {
                return node1;
            } else if (pageProvider.endPage(node2.line - 1)
                    && !pageProvider.endPage(node1.line - 1)) {
                return node2;
            }
        }

        /* all other cases: use superclass implementation */
        return super.compareNodes(node1, node2);
    }

    /** {@inheritDoc} */
    @Override
    protected KnuthNode createNode(int position,                // CSOK: ParameterNumber
                                   int line, int fitness,
                                   int totalWidth, int totalStretch, int totalShrink,
                                   double adjustRatio, int availableShrink, int availableStretch,
                                   int difference, double totalDemerits, KnuthNode previous) {
        return new KnuthPageNode(position, line, fitness,
                                 totalWidth, totalStretch, totalShrink,
                                 insertedFootnotesLength, footnoteListIndex, footnoteElementIndex,
                                 adjustRatio, availableShrink, availableStretch,
                                 difference, totalDemerits, previous);
    }

    /** {@inheritDoc} */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    protected void handleBox(KnuthBox box) {
        super.handleBox(box);
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
     * Overridden to consider penalties with value {@link KnuthElement#INFINITE}
     * as legal break-points, if the current keep-context allows this
     * (a keep-*.within-page="always" constraint still permits column-breaks)
     * {@inheritDoc}
     */
    @Override
    protected void handlePenaltyAt(KnuthPenalty penalty, int position,
                                   int allowedBreaks) {
        super.handlePenaltyAt(penalty, position, allowedBreaks);
        /* if the penalty had value INFINITE, default implementation
         * will not have considered it a legal break, but it could still
         * be one.
         */
        if (penalty.getPenalty() == KnuthPenalty.INFINITE) {
            int breakClass = penalty.getBreakClass();
            if (breakClass == Constants.EN_PAGE
                    || breakClass == Constants.EN_COLUMN) {
                considerLegalBreak(penalty, position);
            }
        }
    }

    /**
     * Handles the footnotes cited inside a block-level box. Updates footnotesList and the
     * value of totalFootnotesLength with the lengths of the given footnotes.
     * @param elementLists list of KnuthElement sequences corresponding to the footnotes
     * bodies
     */
    private void handleFootnotes(List<List<KnuthElement>> elementLists) {
        // initialization
        if (!footnotesPending) {
            footnotesPending = true;
            footnotesList = new ArrayList<List<KnuthElement>>();
            lengthList = new ArrayList<Integer>();
            totalFootnotesLength = 0;
        }
        if (!newFootnotes) {
            newFootnotes = true;
            firstNewFootnoteIndex = footnotesList.size();
        }

        // compute the total length of the footnotes
        for (List<KnuthElement> noteList : elementLists) {

            //Space resolution (Note: this does not respect possible stacking constraints
            //between footnotes!)
            SpaceResolver.resolveElementList(noteList);

            int noteLength = 0;
            footnotesList.add(noteList);
            for (KnuthElement element : noteList) {
                if (element.isBox() || element.isGlue()) {
                    noteLength += element.getWidth();
                }
            }
            int prevLength = (lengthList == null || lengthList.isEmpty())
                    ? 0
                    : ListUtil.getLast(lengthList);
            if (lengthList != null) {
                lengthList.add(prevLength + noteLength);
            }
            totalFootnotesLength += noteLength;
        }
    }

    /** {@inheritDoc} */
    @Override
    protected int restartFrom(KnuthNode restartingNode, int currentIndex) {
        int returnValue = super.restartFrom(restartingNode, currentIndex);
        newFootnotes = false;
        if (footnotesPending) {
            // remove from footnotesList the note lists that will be met
            // after the restarting point
            for (int j = currentIndex; j >= restartingNode.position; j--) {
                final KnuthElement resetElement = getElement(j);
                if (resetElement instanceof KnuthBlockBox
                        && ((KnuthBlockBox) resetElement).hasAnchors()) {
                    resetFootnotes(((KnuthBlockBox) resetElement).getElementLists());
                }
            }
        }
        return returnValue;
    }

    private void resetFootnotes(List<List<KnuthElement>> elementLists) {
        for (int i = 0; i < elementLists.size(); i++) {
            ListUtil.removeLast(footnotesList);
            ListUtil.removeLast(lengthList);

            // update totalFootnotesLength
            if (!lengthList.isEmpty()) {
                totalFootnotesLength = ListUtil.getLast(lengthList);
            } else {
                totalFootnotesLength = 0;
            }
        }
        // update footnotesPending;
        if (footnotesList.size() == 0) {
            footnotesPending = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void considerLegalBreak(KnuthElement element, int elementIdx) {
        if (element.isPenalty()) {
            int breakClass = ((KnuthPenalty) element).getBreakClass();
            switch (breakClass) {
            case Constants.EN_PAGE:
                if (this.currentKeepContext != breakClass) {
                    this.lastBeforeKeepContextSwitch = getLastTooShort();
                }
                this.currentKeepContext = breakClass;
                break;
            case Constants.EN_COLUMN:
                if (this.currentKeepContext != breakClass) {
                    this.lastBeforeKeepContextSwitch = getLastTooShort();
                }
                this.currentKeepContext = breakClass;
                break;
            case Constants.EN_AUTO:
                this.currentKeepContext = breakClass;
                break;
            default:
                //nop
            }
        }
        super.considerLegalBreak(element, elementIdx);
        newFootnotes = false;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean elementCanEndLine(KnuthElement element, int line, int difference) {
        if (!(element.isPenalty()) || pageProvider == null) {
            return true;
        } else {
            KnuthPenalty p = (KnuthPenalty) element;
            if (p.getPenalty() <= 0) {
                return true;
            } else {
                int context = p.getBreakClass();
                switch (context) {
                case Constants.EN_LINE:
                case Constants.EN_COLUMN:
                    return p.getPenalty() < KnuthPenalty.INFINITE;
                case Constants.EN_PAGE:
                    return p.getPenalty() < KnuthPenalty.INFINITE
                            || !pageProvider.endPage(line - 1);
                case Constants.EN_AUTO:
                    log.debug("keep is not auto but context is");
                    return true;
                default:
                    if (p.getPenalty() < KnuthPenalty.INFINITE) {
                        log.debug("Non recognized keep context:" + context);
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected int computeDifference(KnuthNode activeNode, KnuthElement element,
                                    int elementIndex) {
        KnuthPageNode pageNode = (KnuthPageNode) activeNode;
        int actualWidth = totalWidth - pageNode.totalWidth;
        int footnoteSplit;
        boolean canDeferOldFN;
        if (element.isPenalty()) {
            actualWidth += element.getWidth();
        }
        if (footnotesPending) {
            // compute the total length of the footnotes not yet inserted
            int allFootnotes = totalFootnotesLength - pageNode.totalFootnotes;
            if (allFootnotes > 0) {
                // this page contains some footnote citations
                // add the footnote separator width
                actualWidth += footnoteSeparatorLength.getOpt();
                if (actualWidth + allFootnotes <= getLineWidth(activeNode.line)) {
                    // there is enough space to insert all footnotes:
                    // add the whole allFootnotes length
                    actualWidth += allFootnotes;
                    insertedFootnotesLength = pageNode.totalFootnotes + allFootnotes;
                    footnoteListIndex = footnotesList.size() - 1;
                    footnoteElementIndex
                        = getFootnoteList(footnoteListIndex).size() - 1;
                } else if (((canDeferOldFN = canDeferOldFootnotes(// CSOK: InnerAssignment
                             pageNode, elementIndex))
                            || newFootnotes)
                           && (footnoteSplit = getFootnoteSplit(// CSOK: InnerAssignment
                               pageNode, getLineWidth(activeNode.line) - actualWidth,
                                canDeferOldFN)) > 0) {
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
                    footnoteElementIndex
                        = getFootnoteList(footnoteListIndex).size() - 1;
                }
            } else {
                // all footnotes have already been placed on previous pages
            }
        } else {
            // there are no footnotes
        }
        int diff = getLineWidth(activeNode.line) - actualWidth;
        if (autoHeight && diff < 0) {
            //getLineWidth() for auto-height parts return 0 so the diff will be negative
            return 0; //...but we don't want to shrink in this case. Stick to optimum.
        } else {
            return diff;
        }
    }

    /**
     * Checks whether footnotes from preceding pages may be deferred to the page after
     * the given element.
     * @param node active node for the preceding page break
     * @param contentElementIndex index of the Knuth element considered for the
     * current page break
     * @return  true if footnotes can be deferred
     */
    private boolean canDeferOldFootnotes(KnuthPageNode node, int contentElementIndex) {
        return (noBreakBetween(node.position, contentElementIndex)
                && deferredFootnotes(node.footnoteListIndex,
                        node.footnoteElementIndex, node.totalFootnotes));
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
                       && ((KnuthElement) par
                           .getElement(index)).getPenalty() < KnuthElement.INFINITE) {
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
                     || elementIndex < getFootnoteList(listIndex).size() - 1))
                || length < totalFootnotesLength);
    }

    /**
     * Tries to split the flow of footnotes to put one part on the current page.
     * @param activeNode currently considered previous page break
     * @param availableLength available space for footnotes
     * @param canDeferOldFootnotes
     * @return ...
     */
    private int getFootnoteSplit(KnuthPageNode activeNode, int availableLength,
                boolean canDeferOldFootnotes) {
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
     * @return ...
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
            ListIterator<KnuthElement> noteListIterator;
            KnuthElement element;
            boolean somethingAdded = false;

            // prevListIndex and prevElementIndex points to the last footnote element
            // already placed in a page: advance to the next element
            int listIndex = prevListIndex;
            int elementIndex = prevElementIndex;
            if (elementIndex == getFootnoteList(listIndex).size() - 1) {
                listIndex++;
                elementIndex = 0;
            } else {
                elementIndex++;
            }

            // try adding whole notes
            if (footnotesList.size() - 1 > listIndex) {
                // add the previous footnotes: these cannot be broken or deferred
                if (!canDeferOldFootnotes && newFootnotes && firstNewFootnoteIndex > 0) {
                    splitLength = lengthList.get(firstNewFootnoteIndex - 1) - prevLength;
                    listIndex = firstNewFootnoteIndex;
                    elementIndex = 0;
                }
                // try adding the new footnotes
                while (lengthList.get(listIndex) - prevLength
                       <= availableLength) {
                    splitLength = lengthList.get(listIndex) - prevLength;
                    somethingAdded = true;
                    listIndex++;
                    elementIndex = 0;
                }
                // as this method is called only if it is not possible to insert
                // all footnotes, at this point listIndex and elementIndex points to
                // an existing element, the next one we will try to insert
            }

            // try adding a split of the next note
            noteListIterator = getFootnoteList(listIndex).listIterator(elementIndex);

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
                boolean boxPreceding = false;
                while (noteListIterator.hasNext()) {
                    // as this method is called only if it is not possible to insert
                    // all footnotes, and we have already tried (and failed) to insert
                    // this whole footnote, the while loop will never reach the end
                    // of the note sequence
                    element = noteListIterator.next();
                    if (element.isBox()) {
                        // element is a box
                        splitLength += element.getWidth();
                        boxPreceding = true;
                    } else if (element.isGlue()) {
                        // element is a glue
                        if (boxPreceding) {
                            // end of the sub-sequence
                            index = noteListIterator.previousIndex();
                            break;
                        }
                        boxPreceding = false;
                        splitLength += element.getWidth();
                    } else {
                        // element is a penalty
                        if (element.getPenalty() < KnuthElement.INFINITE) {
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
            //TODO: check this conditional, as the first one is always false...?
            if (!somethingAdded) {
                // there was not enough space to add a piece of the first new footnote
                // this is not a good break
                prevSplitLength = 0;
            } else if (prevSplitLength > 0) {
                // prevIndex is -1 if we have added only some whole footnotes
                footnoteListIndex = (prevIndex != -1) ? listIndex : listIndex - 1;
                footnoteElementIndex = (prevIndex != -1)
                    ? prevIndex
                    : getFootnoteList(footnoteListIndex).size() - 1;
            }
            return prevSplitLength;
        }
    }

    /** {@inheritDoc} */
    @Override
    protected double computeAdjustmentRatio(KnuthNode activeNode, int difference) {
        // compute the adjustment ratio
        if (difference > 0) {
            int maxAdjustment = totalStretch - activeNode.totalStretch;
            // add the footnote separator stretch if some footnote content will be added
            if (((KnuthPageNode) activeNode).totalFootnotes < totalFootnotesLength) {
                maxAdjustment += footnoteSeparatorLength.getStretch();
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
                maxAdjustment += footnoteSeparatorLength.getShrink();
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

    /** {@inheritDoc} */
    @Override
    protected double computeDemerits(KnuthNode activeNode, KnuthElement element,
                                    int fitnessClass, double r) {
        double demerits = 0;
        // compute demerits
        double f = Math.abs(r);
        f = 1 + 100 * f * f * f;
        if (element.isPenalty()) {
            double penalty = element.getPenalty();
            if (penalty >= 0) {
                f += penalty;
                demerits = f * f;
            } else if (!element.isForcedBreak()) {
                demerits = f * f - penalty * penalty;
            } else {
                demerits = f * f;
            }
        } else {
            demerits = f * f;
        }

        if (element.isPenalty() && ((KnuthPenalty) element).isPenaltyFlagged()
            && getElement(activeNode.position).isPenalty()
            && ((KnuthPenalty) getElement(activeNode.position)).isPenaltyFlagged()) {
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
            if (footnoteListIndex < footnotesList.size()) {
                if (footnoteElementIndex
                        < getFootnoteList(footnoteListIndex).size() - 1) {
                    // add demerits for the footnote split between pages
                    demerits += splitFootnoteDemerits;
                }
            } else {
                //TODO Why can this happen in the first place? Does anybody know? See #44160
            }
        }
        demerits += activeNode.totalDemerits;
        return demerits;
    }

    /** {@inheritDoc} */
    @Override
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
        int availableBPD = getLineWidth(lastNode.line);
        int split = 0;
        KnuthPageNode prevNode = lastNode;

        // create pages containing the remaining footnote bodies
        while (insertedFootnotesLength < totalFootnotesLength) {
            if (totalFootnotesLength - insertedFootnotesLength <= availableBPD) {
                // All the remaining footnotes fit
                insertedFootnotesLength = totalFootnotesLength;
                footnoteListIndex = lengthList.size() - 1;
                footnoteElementIndex = getFootnoteList(footnoteListIndex).size() - 1;
            } else if ((split = getFootnoteSplit(// CSOK: InnerAssignment
                        footnoteListIndex, footnoteElementIndex,
                         insertedFootnotesLength, availableBPD, true)) > 0) {
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
                availableBPD = getLineWidth(node.line);
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
     * @return a list of {@link PageBreakPosition} elements
     *          corresponding to the computed page- and column-breaks
     */
    public LinkedList<PageBreakPosition> getPageBreaks() {
        return pageBreaks;
    }

    /**
     * Insert the given {@link PageBreakPosition} as the first
     * element in the list of page-breaks
     *
     * @param pageBreak the position to insert
     */
    public void insertPageBreakAsFirst(PageBreakPosition pageBreak) {
        if (pageBreaks == null) {
            pageBreaks = new LinkedList<PageBreakPosition>();
        }
        pageBreaks.addFirst(pageBreak);
    }

    /**
     * Removes all page breaks from the result list. This is used by block-containers and
     * static-content when it is only desired to know where there is an overflow but later the
     * whole content should be painted as one part.
     */
    public void removeAllPageBreaks() {
        if (pageBreaks == null || pageBreaks.isEmpty()) {
            return;
        }
        pageBreaks.subList(0, pageBreaks.size() - 1).clear();
    }

    /** {@inheritDoc} */
    @Override
    public void updateData1(int total, double demerits) {
    }

    /** {@inheritDoc} */
    @Override
    public void updateData2(KnuthNode bestActiveNode,
                            KnuthSequence sequence,
                            int total) {
        //int difference = (bestActiveNode.line < total)
        //      ? bestActiveNode.difference : bestActiveNode.difference + fillerMinWidth;
        int difference = bestActiveNode.difference;
        if (difference + bestActiveNode.availableShrink < 0) {
            if (!autoHeight) {
                if (layoutListener != null) {
                    layoutListener.notifyOverflow(bestActiveNode.line - 1, -difference, getFObj());
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
                && firstElementIndex == getFootnoteList(firstListIndex).size() - 1) {
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

    /** {@inheritDoc} */
    @Override
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
        assert (bestActiveNode != null);
        return bestActiveNode.line;
    }

    /**
     * Obtain the element-list corresponding to the footnote at the given index.
     *
     * @param index the index in the list of footnotes
     * @return  the element-list
     */
    protected final List<KnuthElement> getFootnoteList(int index) {
        return footnotesList.get(index);
    }

    /** @return the associated top-level formatting object. */
    public FObj getFObj() {
        return topLevelLM.getFObj();
    }

    /** {@inheritDoc} */
    @Override
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
         * @param amount the amount by which the area overflows (in mpt)
         * @param obj the root FO object where this happens
         */
        void notifyOverflow(int part, int amount, FObj obj);

    }

    @Override
    protected KnuthNode recoverFromOverflow() {
        if (compareIPDs(getLastTooLong().line - 1) != 0) {
            /**
             * If the IPD of the next page changes, disable the recovery mechanism as the
             * inline content has to be re-laid out according to the new IPD anyway.
             */
            return getLastTooLong();
        } else {
            return super.recoverFromOverflow();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected int getIPDdifference() {
        return ipdDifference;
    }

    /** {@inheritDoc} */
    @Override
    protected int handleIpdChange() {
        log.trace("Best node for ipd change:" + bestNodeForIPDChange);
        // TODO finish()
        /*
         * The third parameter is used to determine if this is the last page, so
         * if the content must be vertically justified or not. If we are here
         * this means that there is further content and the next page has a
         * different ipd. So tweak the parameter to fall into the non-last-page
         * case.
         */
        calculateBreakPoints(bestNodeForIPDChange, par, bestNodeForIPDChange.line + 1);
        activeLines = null;
        return bestNodeForIPDChange.line;
    }

    /**
     * Add a node at the end of the given line's existing active nodes.
     * If this is the first node in the line, adjust endLine accordingly.
     * @param line number of the line ending at the node's corresponding breakpoint
     * @param node the active node to add
     */
    @Override
    protected void addNode(int line, KnuthNode node) {
        if (node.position < par.size() - 1 && line > 0
                && (ipdDifference = compareIPDs(line - 1)) != 0) {  // CSOK: InnerAssignment
            log.trace("IPD changes at page " + line);
            if (bestNodeForIPDChange == null
                    || node.totalDemerits < bestNodeForIPDChange.totalDemerits) {
                bestNodeForIPDChange = node;
            }
        } else {
            if (node.position == par.size() - 1) {
                /*
                 * The whole sequence could actually fit on the last page before
                 * the IPD change. No need to do any special handling.
                 */
                ipdDifference = 0;
            }
            super.addNode(line, node);
        }
    }

    KnuthNode getBestNodeBeforeIPDChange() {
        return bestNodeForIPDChange;
    }

    private int compareIPDs(int line) {
        if (pageProvider == null) {
            return 0;
        }
        return pageProvider.compareIPDs(line);
    }
}
