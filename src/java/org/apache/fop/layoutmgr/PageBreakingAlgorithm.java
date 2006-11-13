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

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.layoutmgr.AbstractBreaker.PageBreakPosition;
import org.apache.fop.layoutmgr.breaking.BeforeFloatsRecord;
import org.apache.fop.layoutmgr.breaking.ElasticLength;
import org.apache.fop.layoutmgr.breaking.FootnotesRecord;
import org.apache.fop.layoutmgr.breaking.BeforeFloatsRecord.BeforeFloatsProgress;
import org.apache.fop.layoutmgr.breaking.FootnotesRecord.FootnotesProgress;

import org.apache.fop.traits.MinOptMax;

public class PageBreakingAlgorithm extends BreakingAlgorithm {

    /* TODO vh: all of the following parameters should be available through a config
     * option
     */
    /**
     * Minimum allowed fill ratio for pages. Underfull pages which are filled at
     * least this ratio are considered to be feasible breaks.
     */
    /* Set to 1.0 for now, otherwise this breaks testcases. */
    public static final double MIN_NORMAL_PAGE_FILL_RATIO = 1.0;

    /**
     * Minimum allowed fill ratio for float-only pages. Underfull pages which are filled
     * at least this ratio are considered to be feasible breaks.
     */
    public static final double MIN_FLOAT_PAGE_FILL_RATIO = 1.0;

    /**
     * Minimum acceptable ratio of normal content on pages containing both normal text and
     * out-of-lines.
     */
    public static final double TEXT_FRACTION = 0.05;

    /**
     * Are float-only pages allowed?
     */
    public static final boolean FLOAT_PAGES_ALLOWED = true;

    /**
     * Are footnotes allowed on float-only pages?
     */
    public static final boolean FOOTNOTES_ALLOWED_ON_FLOAT_PAGES = true;

    /**
     * Are footnotes-only pages allowed?
     */
    public static final boolean FOOTNOTES_ONLY_PAGES_ALLOWED = true;

    /**
     * Additional demerits for an underfull page, which however has an acceptable fill ratio.
     */
    private static final double UNDERFULL_PAGE_DEMERITS = 20000;

    /**
     * This mode is chosen when out-of-lines must be typeset on a page containing normal
     * content.
     */
    public static final int NORMAL_MODE = 0;

    /**
     * This mode is chosen when out-of-lines must be typeset on a float-only page.
     */
    public static final int FLOAT_PAGE_MODE = 1;

    /**
     * This mode is chosen when out-of-lines must be typeset on a float-only page at the
     * end of a page-sequence.
     */
    public static final int FLUSH_MODE = 2;

    /** the logger for the class */
    protected static Log classLog = LogFactory.getLog(PageBreakingAlgorithm.class);

    private LayoutManager topLevelLM;
    private PageSequenceLayoutManager.PageProvider pageProvider;
    private PageBreakingLayoutListener layoutListener;
    /** List of PageBreakPosition elements. */
    private LinkedList pageBreaks = null;

    private NormalContentProgressInfo normalContentProgress = new NormalContentProgressInfo();
    private FootnotesRecord footnotesRecord;
    private BeforeFloatsRecord beforeFloatsRecord;
    private FootnotesRecord.FootnotesProgress footnotesProgress;
    private BeforeFloatsRecord.BeforeFloatsProgress beforeFloatsProgress;

    private ActiveNodeRecorder activeNodeRecorder = new ActiveNodeRecorder();
    
    // demerits for a page break that splits a footnote 
    private int splitFootnoteDemerits = 5000;
    // demerits for a page break that defers a whole footnote to the following page 
    private int deferredFootnoteDemerits = 10000;
    private int deferredFloatDemerits = 2000;

    //Controls whether overflows should be warned about or not
    private boolean autoHeight = false;
    
    //Controls whether a single part should be forced if possible (ex. block-container)
    private boolean favorSinglePart = false;
    
    public PageBreakingAlgorithm(LayoutManager topLevelLM,
                                 PageSequenceLayoutManager.PageProvider pageProvider,
                                 PageBreakingLayoutListener layoutListener,
                                 int alignment, int alignmentLast,
                                 MinOptMax footnoteSeparatorLength, MinOptMax floatSeparatorLength,
                                 boolean partOverflowRecovery, boolean autoHeight,
                                 boolean favorSinglePart) {
        super(alignment, alignmentLast, true, partOverflowRecovery, 0);
        this.log = classLog;
        this.topLevelLM = topLevelLM;
        this.pageProvider = pageProvider;
        this.layoutListener = layoutListener;
        best = new BestPageRecords(log);
        footnotesRecord = new FootnotesRecord(footnoteSeparatorLength);
        beforeFloatsRecord = new BeforeFloatsRecord(floatSeparatorLength);
        footnotesProgress = footnotesRecord.new FootnotesProgress();
        beforeFloatsProgress = beforeFloatsRecord.new BeforeFloatsProgress();
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

        public FootnotesRecord.FootnotesProgress footnotesProgress;
        public BeforeFloatsRecord.BeforeFloatsProgress beforeFloatsProgress;

        public KnuthPageNode(int position, int line, int fitness,
                             int totalWidth, int totalStretch, int totalShrink,
                             FootnotesRecord.FootnotesProgress footnotesProgress,
                             BeforeFloatsRecord.BeforeFloatsProgress floatsProgress,
                             double adjustRatio, int availableShrink, int availableStretch,
                             int difference, double totalDemerits, KnuthNode previous) {
            super(position, line, fitness,
                  totalWidth, totalStretch, totalShrink,
                  adjustRatio, availableShrink, availableStretch,
                  difference, totalDemerits, previous);
            this.footnotesProgress = footnotesRecord.new FootnotesProgress(footnotesProgress);
            this.beforeFloatsProgress = beforeFloatsRecord.new BeforeFloatsProgress(floatsProgress);
        }

    }

    /**
     * this class stores information about how the nodes
     * which could start a line ending at the current element
     */
    protected class BestPageRecords extends BestRecords {

        private FootnotesRecord.FootnotesProgress[] bestFootnotesProgress
                = new FootnotesRecord.FootnotesProgress[4];
        private BeforeFloatsRecord.BeforeFloatsProgress[] bestFloatsProgress
                = new BeforeFloatsRecord.BeforeFloatsProgress[4];

        public BestPageRecords(Log log) {
            super(log);
        }

        public void addRecord(double demerits, KnuthNode node, double adjust,
                              int availableShrink, int availableStretch,
                              int difference, int fitness,
                              FootnotesRecord.FootnotesProgress footnotesProgress,
                              BeforeFloatsRecord.BeforeFloatsProgress beforeFloatsProgress) {
            super.addRecord(demerits, node, adjust,
                            availableShrink, availableStretch,
                            difference, fitness);
            bestFootnotesProgress[fitness]
                    = footnotesRecord.new FootnotesProgress(footnotesProgress);
            bestFloatsProgress[fitness]
                    = beforeFloatsRecord.new BeforeFloatsProgress(beforeFloatsProgress);
        }

        public FootnotesRecord.FootnotesProgress getFootnoteProgress(int fitness) {
            return bestFootnotesProgress[fitness];
        }

        public BeforeFloatsRecord.BeforeFloatsProgress getFloatProgress(int fitness) {
            return bestFloatsProgress[fitness];
        }
    }

    /**
     * This class records information about the amount of normal content that has been
     * handled so far.
     */
    public class NormalContentProgressInfo {

        /**
         * Position in the Knuth sequence.
         */
        int position;

        /**
         * Cumulative lengths of normal content inserted so far. This corresponds to the
         * totalWidth, totalStretch, totalShrink described in Knuth's algorithm.
         */
        ElasticLength insertedDims = new ElasticLength();

        /**
         * Initializes this record to handle the given Knuth sequence, such that no
         * content has been inserted yet.
         *
         * @param par the sequence of normal content that will have to be typeset
         */
        void initialize(KnuthSequence par) {
            insertedDims.reset();
        }

        public String toString() {
            return "Position: " + position + "; inserted: " + insertedDims;
        }
    }

    /**
     * Tests candidate nodes to determine whether they are feasible, and if so records
     * them.
     */
    public class ActiveNodeRecorder {

        /** Adjustment ratio for the currently tested page. */
        private double adjustmentRatio;

        /** Fill ratio of the currently tested page. */
        private double fillRatio;

        private int fitnessClass;

        /**
         * Difference between the physical page's BPD and the BPD of the page's content.
         */
        private int difference;

        /** Used to record feasible breaks in flush mode. */
        private LinkedList queue;

        /**
         * <code>true</code> if a layout must be found, even if there is no feasible
         * break. This usually consists of selecting a too-short or too-long node.
         */
        private boolean force;

        /**
         * Sets the behavior of the algorithm when no feasible break is found.
         *
         * @param force if <code>true</code>, a too-short or too-long node must be chosen
         * as a feasible break; otherwise no node is created.
         */
        void setForce(boolean force) {
            this.force = force;
        }

        /**
         * Computes the adjustment ratio for the current page.
         *
         * @param totalLength total amount of content on the page (including floats and
         * footnotes)
         * @param pageBPD available space on the page
         * @param minFillRatio minimum acceptable fill ratio for the page. If the content
         * must be too much stretched to fill the page, it is allowed to stretch less
         * provided the resulting fill ratio is superior or equal to this ratio
         */
        void computeAdjustmentRatio(ElasticLength totalLength, int pageBPD, double minFillRatio) {
            difference = pageBPD - totalLength.getLength();
            fillRatio = 1.0;
            if (difference > 0) {  // too short
                double totalStretch = totalLength.getStretch();
                if (totalStretch <= 0) {
                    fillRatio = ((double) totalLength.getLength()) / pageBPD;
                    if (fillRatio >= minFillRatio) {
                        adjustmentRatio = 0;
                    } else {
                        adjustmentRatio = INFINITE_RATIO;
                    }
                } else {
                    adjustmentRatio = difference / totalStretch;
                    if (adjustmentRatio > threshold) {
                        fillRatio = (totalLength.getLength() + totalStretch * threshold) / pageBPD;
                        if (fillRatio >= minFillRatio) {
                            adjustmentRatio = threshold;
                        }
                    }
                }
            } else if (difference < 0) {  // too long
                double totalShrink = totalLength.getShrink();
                if (totalShrink > 0) {
                    adjustmentRatio = difference / totalShrink;
                } else {
                    adjustmentRatio = -INFINITE_RATIO;
                }
            } else {
                adjustmentRatio = 0;
            }
        }

        /**
         * Computes the total demerits of the page layout up to the current page.
         *
         * @param footnotes information about footnotes put on the current page
         * @param beforeFloats information about before-floats put on the current page
         * @param lastNormalElementIdx index of the last Knuth element representing normal
         * content put on the current page
         * @param activeNode node representing the previous page break
         * @param mode one of {@link PageBreakingAlgorithm#NORMAL_MODE}, {@link
         * PageBreakingAlgorithm#FLOAT_PAGE_MODE} or {@link
         * PageBreakingAlgorithm#FLUSH_MODE}
         */
        double computeDemerits(FootnotesRecord.FootnotesProgress footnotes,
                               BeforeFloatsRecord.BeforeFloatsProgress beforeFloats,
                               int lastNormalElementIdx,
                               KnuthPageNode activeNode,
                               int mode) {
            // TODO penalty for footnotes of floats ending on penalty elements
            KnuthElement lastNormalElement = (KnuthElement) par.get(lastNormalElementIdx);
            double demerits = 0;
            double f = Math.abs(adjustmentRatio);
            /*
             * If the adjustment ratio is too high, the demerits will be "almost
             * infinite" (10^22). Adding demerits for a deferred float (10000) thus
             * won't change the demerits value. We may end up with two breakpoints
             * with the same demerits, whereas in one case there are deferred floats
             * and not in the other case. The case with no deferred floats is still
             * preferable, so we must have the possibility to distinguish it. By
             * forcing f to threshold it becomes possible to make the difference
             * when there are deferred floats.
             */
            if (f > threshold) {
                f = threshold;
            }
            f = 1 + 100 * f * f * f;
            double minPageFillRatio;
            if (mode == NORMAL_MODE) {
                minPageFillRatio = MIN_NORMAL_PAGE_FILL_RATIO;
                if (!lastNormalElement.isPenalty()) {
                    demerits = f * f;
                } else {
                    double penalty = lastNormalElement.getP();
                    if (penalty >= 0) {
                        f += penalty;
                        demerits = f * f;
                    } else if (!lastNormalElement.isForcedBreak()) {
                        demerits = f * f - penalty * penalty;
                    } else {
                        demerits = f * f;
                    }
                    if (((KnuthPenalty) lastNormalElement).isFlagged()
                            && getElement(activeNode.position).isPenalty()
                            && ((KnuthPenalty) getElement(activeNode.position)).isFlagged()) {
                        // add demerit for consecutive breaks at flagged penalties
                        demerits += repeatedFlaggedDemerit;
                    }
                }
            } else {
                minPageFillRatio = MIN_FLOAT_PAGE_FILL_RATIO;
                demerits = f * f;
            }
            fitnessClass = computeFitness(adjustmentRatio);
            if (Math.abs(fitnessClass - activeNode.fitness) > 1) {
                // add demerit for consecutive breaks
                // with very different fitness classes
                demerits += incompatibleFitnessDemerit;
            }

            demerits += footnotes.getNbOfDeferred() * deferredFootnoteDemerits; 
            if (footnotes.isLastSplit()) {
                demerits += footnotes.getNbSplit() * splitFootnoteDemerits;
            }
            demerits += beforeFloats.getNbOfDeferred() * deferredFloatDemerits; 
            if (beforeFloats.isLastSplit()) {
                demerits += beforeFloats.getNbSplit() * splitFootnoteDemerits;
            }
            if (fillRatio < minPageFillRatio) {
                /* To select too-short nodes among the least too underfull pages. This formula
                 * will give smaller results than below but, anyway, too-short nodes are
                 * handled separately
                 */
                demerits += (2.0 - fillRatio) * UNDERFULL_PAGE_DEMERITS;
            } else if (minPageFillRatio < 1.0) {
                /* demerits += x * UNDERFULL_PAGE_DEMERITS
                 * The idea is that x tends to 1.0 when fillRatio tends to
                 * MIN_PAGE_FILL_RATIO, to give the preference to full pages. Of course the
                 * following formula works only if MIN_PAGE_FILL_RATIO != 1.0, hence the test
                 */
                demerits += (1.0 - fillRatio) / (1.0 - minPageFillRatio) * UNDERFULL_PAGE_DEMERITS;
            }
            demerits += activeNode.totalDemerits;
            return demerits;
        }

        /**
         * Tests if the node corresponding to the given parameters represents a feasible
         * break, and if so computes its demerits and records it. This methods returns
         * <code>true</code> if there is potential room for putting additional content on
         * the page. Otherwise, this indicates that it is not even worth trying.
         *
         * @param mode one of {@link PageBreakingAlgorithm#NORMAL_MODE}, {@link
         * PageBreakingAlgorithm#FLOAT_PAGE_MODE} or {@link
         * PageBreakingAlgorithm#FLUSH_MODE}
         * @param normalProgress progress information for the normal content
         * @param footnotesProgress progress information for footnotes
         * @param beforeFloatsProgress progress information for before-floats
         * @param previousNode node representing the previous page break
         * @return <code>true</code> if some additional content may potentially be added
         * on the page (adjustment ratio &gt; -1); otherwise <code>false</code>
         * (adjustment ratio &lt;= -1)
         */
        public boolean handleNode(int mode,
                           NormalContentProgressInfo normalProgress,
                           FootnotesRecord.FootnotesProgress footnotesProgress,
                           BeforeFloatsRecord.BeforeFloatsProgress beforeFloatsProgress,
                           KnuthPageNode previousNode) {
            int pageBPD = getLineWidth(previousNode.line);
            ElasticLength totalLength = new ElasticLength(footnotesProgress.getInserted());
            totalLength.add(beforeFloatsProgress.getInserted());
            if (mode == NORMAL_MODE) {
                totalLength.add(normalProgress.insertedDims);
                computeAdjustmentRatio(totalLength, pageBPD, MIN_NORMAL_PAGE_FILL_RATIO);
            } else {
                computeAdjustmentRatio(totalLength, pageBPD, MIN_FLOAT_PAGE_FILL_RATIO);
            }
            switch (mode) {
            case NORMAL_MODE: {
                int beforeFloatActualBPD = beforeFloatsProgress.getInserted().getLength();
                if (adjustmentRatio < 0) {
                    beforeFloatActualBPD += beforeFloatsProgress.getInserted().getShrink()
                            * adjustmentRatio; 
                } else if (adjustmentRatio > 0) {
                    beforeFloatActualBPD += beforeFloatsProgress.getInserted().getStretch()
                            * adjustmentRatio;
                }
                if (((double) beforeFloatActualBPD) / pageBPD >= 1.0 - TEXT_FRACTION) {
                    // Not acceptable page, but if some further footnotes are
                    // added, may become feasible thanks to shrinking
                    double minBeforeFloatFraction
                            = ((double) (beforeFloatsProgress.getInserted().getLength()
                            - beforeFloatsProgress.getInserted().getShrink())) / pageBPD; 
                    return adjustmentRatio > -1 && minBeforeFloatFraction < 1.0 - TEXT_FRACTION;
                } else {
                    if (-1 <= adjustmentRatio && adjustmentRatio <= threshold) {
                        double demerits = computeDemerits(footnotesProgress,
                                beforeFloatsProgress,
                                normalProgress.position,
                                previousNode,
                                NORMAL_MODE);
                        if (demerits < best.getDemerits(fitnessClass)) {
                            ((BestPageRecords) best).addRecord(demerits,
                                    previousNode,
                                    adjustmentRatio,
                                    totalLength.getShrink(),
                                    totalLength.getStretch(),
                                    difference, fitnessClass,
                                    footnotesProgress, beforeFloatsProgress);
                            lastTooShort = null;
                        }
                        return true;
                    } else if (force) {
                        double demerits = computeDemerits(footnotesProgress,
                                beforeFloatsProgress,
                                normalProgress.position,
                                previousNode,
                                NORMAL_MODE);
                        sumsAfter.compute(normalProgress.position,
                                totalWidth, totalStretch, totalShrink);
                        if (adjustmentRatio < -1) {
                            if (lastTooLong == null || demerits < lastTooLong.totalDemerits) {
                                lastTooLong = createNode(normalProgress.position,
                                        previousNode.line + 1,
                                        fitnessClass,
                                        sumsAfter.getWidthAfter(),
                                        sumsAfter.getStretchAfter(),
                                        sumsAfter.getShrinkAfter(),
                                        adjustmentRatio,
                                        totalLength.getShrink(),
                                        totalLength.getStretch(),
                                        difference, demerits, previousNode);
                            }
                            return false;
                        } else {
                            if (lastTooShort == null || demerits <= lastTooShort.totalDemerits) {
                                if (considerTooShort) {
                                    ((BestPageRecords) best).addRecord(demerits,
                                            previousNode,
                                            adjustmentRatio, 
                                            totalLength.getShrink(),
                                            totalLength.getStretch(),
                                            difference, fitnessClass,
                                            footnotesProgress, beforeFloatsProgress);
                                }
                                if (log.isDebugEnabled()) {
                                    NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
                                    nf.setMaximumFractionDigits(2);
                                    nf.setGroupingUsed(false);
                                    log.debug("Registering too-short node [demerits="
                                            + nf.format(demerits)
                                            + ", adjRatio=" + nf.format(adjustmentRatio)
                                            + ", page " + (previousNode.line + 1)
                                            + "]");
                                }
                                lastTooShort = createNode(normalProgress.position,
                                        previousNode.line + 1,
                                        fitnessClass,
                                        sumsAfter.getWidthAfter(),
                                        sumsAfter.getStretchAfter(),
                                        sumsAfter.getShrinkAfter(),
                                        adjustmentRatio,
                                        totalLength.getShrink(),
                                        totalLength.getStretch(),
                                        difference, demerits, previousNode);
                            }
                            return true;
                        }
                    }
                    return adjustmentRatio >= -1;
                }
            }
            case FLOAT_PAGE_MODE:
                if (-1 <= adjustmentRatio && adjustmentRatio <= threshold) {
                    double demerits = computeDemerits(footnotesProgress,
                            beforeFloatsProgress,
                            normalProgress.position,
                            previousNode,
                            NORMAL_MODE);
                    KnuthNode node = createNode(normalProgress.position,
                            previousNode.line + 1,
                            fitnessClass,
                            previousNode.totalWidth,
                            previousNode.totalStretch,
                            previousNode.totalShrink,
                            adjustmentRatio,
                            totalLength.getShrink(),
                            totalLength.getStretch(),
                            difference,
                            demerits,
                            previousNode);
                    registerActiveNode(node);
                    lastTooShort = null;
                }
                return adjustmentRatio >= -1;
            default:  // case FLUSH_MODE:
                if (-1 <= adjustmentRatio && adjustmentRatio <= threshold) {
                    double demerits = computeDemerits(footnotesProgress,
                            beforeFloatsProgress,
                            par.size() - 1,
                            previousNode,
                            NORMAL_MODE);
                    KnuthNode node = createNode(par.size() - 1,
                            previousNode.line + 1,
                            fitnessClass,
                            0, 0, 0,
                            adjustmentRatio,
                            totalLength.getShrink(), totalLength.getStretch(),
                            difference, demerits, previousNode);
                    queue.addLast(node);
                    lastTooShort = null;
                    return true;
                } else if (force) {
                    double demerits = computeDemerits(footnotesProgress,
                            beforeFloatsProgress,
                            par.size() - 1,
                            previousNode,
                            NORMAL_MODE);
                    if (adjustmentRatio < -1) {
                        if (lastTooLong == null || demerits < lastTooLong.totalDemerits) {
                            lastTooLong = createNode(par.size() - 1,
                                    previousNode.line + 1,
                                    fitnessClass,
                                    0, 0, 0,
                                    adjustmentRatio,
                                    totalLength.getShrink(), totalLength.getStretch(),
                                    difference, demerits, previousNode);
                        }
                        return false;
                    } else {
                        if (lastTooShort == null || demerits <= lastTooShort.totalDemerits) {
                            lastTooShort = createNode(par.size() - 1,
                                    previousNode.line + 1,
                                    fitnessClass,
                                    0, 0, 0,
                                    adjustmentRatio,
                                    totalLength.getShrink(), totalLength.getStretch(),
                                    difference, demerits, previousNode);
                            if (considerTooShort) {
                                queue.addLast(lastTooShort);
                            }
                        }
                        return true;
                    }
                }
            return adjustmentRatio >= -1;
            }
        }

        /**
         * When in flush mode, uses the given queue for registering new active nodes. TODO
         * vh: highly temporary! As in flush mode the handling is a bit different,
         * activeLines cannot be re-used. Will have to unify the handling of active nodes
         * eventually.
         *
         * @param queue FIFO structure for registering active nodes in flush mode
         */
        void setQueue(LinkedList queue) {
            this.queue = queue;
        }
    }

    protected void initialize() {
        super.initialize();
        normalContentProgress.initialize(par);
        footnotesRecord.initialize();
        beforeFloatsRecord.initialize();
        footnotesProgress.initialize();
        beforeFloatsProgress.initialize();
        activeNodeRecorder.setForce(force);
    }

    public KnuthNode createNode(int position, int line, int fitness,
                                   int totalWidth, int totalStretch, int totalShrink,
                                   double adjustRatio, int availableShrink, int availableStretch,
                                   int difference, double totalDemerits, KnuthNode previous) {
        return new KnuthPageNode(position, line, fitness,
                                 totalWidth, totalStretch, totalShrink,
                                 footnotesProgress, beforeFloatsProgress,
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
            footnotesRecord.add(((KnuthBlockBox) box).getFootnoteElementLists());
        }
        if (box instanceof KnuthBlockBox
                && ((KnuthBlockBox) box).hasFloatAnchors()) {
            beforeFloatsRecord.add(((KnuthBlockBox) box).getFloatElementLists());
        }
    }


    protected int restartFrom(KnuthNode restartingNode, int currentIndex) {
        int returnValue = super.restartFrom(restartingNode, currentIndex);
        if (footnotesRecord.existing() || beforeFloatsRecord.existing()) {
            // remove from footnotesList the note lists that will be met
            // after the restarting point
            for (int j = currentIndex; j >= restartingNode.position; j--) {
                KnuthElement resetElement = getElement(j);
                if (resetElement instanceof KnuthBlockBox
                        && ((KnuthBlockBox) resetElement).hasFootnoteAnchors()) {
                    footnotesRecord.reset(((KnuthBlockBox) resetElement).getFootnoteElementLists());
                }
                if (resetElement instanceof KnuthBlockBox
                        && ((KnuthBlockBox) resetElement).hasFloatAnchors()) {
                    beforeFloatsRecord.reset(((KnuthBlockBox) resetElement).getFloatElementLists());
                }
            }
        }
        return returnValue;
    }

    protected void considerLegalBreak(KnuthElement element, int elementIdx) {

        if (log.isTraceEnabled()) {
            log.trace("considerLegalBreak() at " + elementIdx 
                    + " (" + totalWidth + "+" + totalStretch + "-" + totalShrink 
                    + "), parts/lines: " + startLine + "-" + endLine);
            log.trace("\tCurrent active node list: " + activeNodeCount + " " + this.toString("\t"));
        }

        lastDeactivated = null;
        lastTooLong = null;
        for (int line = startLine; line < endLine; line++) {
            for (KnuthPageNode node = (KnuthPageNode) getNode(line);
                    node != null;
                    node = (KnuthPageNode) node.next) {
                if (node.position == elementIdx) {
                    continue;
                }
                footnotesProgress.setPrevious(node.footnotesProgress);
                beforeFloatsProgress.setPrevious(node.beforeFloatsProgress);
                footnotesProgress.handleSplit();
                beforeFloatsProgress.handleSplit();
                normalContentProgress.insertedDims.set(totalShrink - node.totalShrink,
                        totalWidth - node.totalWidth, totalStretch - node.totalStretch);
                normalContentProgress.position = elementIdx;
                if (element.isPenalty()) {
                    normalContentProgress.insertedDims.add(0, element.getW(), 0);
                }
                boolean recorded = activeNodeRecorder.handleNode(NORMAL_MODE,
                        normalContentProgress, footnotesProgress, beforeFloatsProgress, node); 
                if (!recorded || element.isForcedBreak()) {
                    deactivateNode(node);
                    lastDeactivated = compareNodes(lastDeactivated, node);
                }
                if (recorded) {
                    footnotesProgress.consider(NORMAL_MODE, activeNodeRecorder,
                            normalContentProgress, beforeFloatsProgress, node);
                }

            }
            addBreaks(line, elementIdx);
        }
    }

    // TODO vh: refactor
    // It may happen that several successive float-only pages are created; in such cases
    // the progress informations must be saved as they'll still be used after this method
    // call
    private Stack footnotesStack = new Stack();

    private Stack beforeFloatsStack = new Stack();

    public void registerActiveNode(KnuthNode node) {
        super.registerActiveNode(node);
        KnuthPageNode pageNode = (KnuthPageNode) node;
        if (pageNode.position < par.size() - 1
                && (pageNode.footnotesProgress.remaining() || pageNode.beforeFloatsProgress.remaining())
                && FLOAT_PAGES_ALLOWED) {
            footnotesStack.push(footnotesProgress);
            footnotesProgress = footnotesRecord.new FootnotesProgress();
            beforeFloatsStack.push(beforeFloatsProgress);
            beforeFloatsProgress = beforeFloatsRecord.new BeforeFloatsProgress();
            
            footnotesProgress.setPrevious(pageNode.footnotesProgress);
            beforeFloatsProgress.setPrevious(pageNode.beforeFloatsProgress);
            footnotesProgress.handleSplit(FLOAT_PAGE_MODE, activeNodeRecorder,
                    normalContentProgress, beforeFloatsProgress, pageNode);
            beforeFloatsProgress.handleSplit(FLOAT_PAGE_MODE, activeNodeRecorder,
                    normalContentProgress, footnotesProgress, pageNode);
            footnotesProgress.consider(FLOAT_PAGE_MODE, activeNodeRecorder,
                    normalContentProgress, beforeFloatsProgress, pageNode);
            
            footnotesProgress = (FootnotesProgress) footnotesStack.pop();
            beforeFloatsProgress = (BeforeFloatsProgress) beforeFloatsStack.pop();
        }
    }

    // TODO vh: refactor
    protected void finish() {
        lastTooShort = null;
        lastTooLong = null;
        Vector bestNodes = new Vector();
        int startIndex = startLine;
        int endIndex = endLine;
        bestNodes.setSize(endIndex - startIndex);
        // Declared as a LinkedList as we need access to the queue-like methods defined
        // in LinkedList
        LinkedList queue = new LinkedList();
        activeNodeRecorder.setQueue(queue);
        Object n = null;
        do {
            for (int i = startIndex; i < endIndex; i++) {
                for (KnuthPageNode node = (KnuthPageNode) getNode(i);
                     node != null;
                     node = (KnuthPageNode) node.next) {
                    deactivateNode(node);
                    if (node.footnotesProgress.remaining()
                            || node.beforeFloatsProgress.remaining()) {
                        queue.addFirst(node);
                        flush(queue, bestNodes, startIndex);
                    } else {
                        if (bestNodes.get(i - startIndex) == null
                                || node.totalDemerits < ((KnuthNode) bestNodes.get(i - startIndex)).totalDemerits) {
                            bestNodes.set(i - startIndex, node);
                        }
                    }
                }
            }
            Iterator iter = bestNodes.iterator();
            while (iter.hasNext() && (n = iter.next()) == null);
            if (n == null) {
                log.debug("Recovering");
                KnuthNode recovered = null;
                if (lastTooShort == null) {
                    if (lastTooLong == null) {
                        log.debug("Both lastTooShort and lastTooLong null!");
                    } else {
                        recovered = lastTooLong;
                    }
                } else {
                    recovered = lastTooShort;
                }
                recovered.totalDemerits = 0;
                registerActiveNode(recovered);
                startIndex = recovered.line;
                endIndex = startIndex + 1;
                lastTooLong = null;
                lastTooShort = null;
            }
        } while (n == null);
        Iterator nodeIter = bestNodes.iterator();
        while (nodeIter.hasNext()) {
            KnuthNode node = (KnuthNode) nodeIter.next();
            if (node != null) { // TODO
                int tmp = endLine;
                registerActiveNode(node);
                if (startLine > node.line) {
                    startLine = node.line;
                }
                if (endLine < tmp) {
                    endLine = tmp;
                }
            }
        }
    }

    // TODO vh: refactor
    private void flush(LinkedList queue, Vector bestNodes, int startIndex) {
        do {
            KnuthPageNode node = (KnuthPageNode) queue.removeFirst();
            if (node.footnotesProgress.remaining() || node.beforeFloatsProgress.remaining()) {
                footnotesProgress.setPrevious(node.footnotesProgress);
                beforeFloatsProgress.setPrevious(node.beforeFloatsProgress);
                footnotesProgress.handleSplit(FLUSH_MODE, activeNodeRecorder,
                        null, beforeFloatsProgress, node);
                beforeFloatsProgress.handleSplit(FLUSH_MODE, activeNodeRecorder,
                        null, footnotesProgress, node);
                footnotesProgress.consider(FLUSH_MODE, activeNodeRecorder,
                        null, beforeFloatsProgress, node);
            } else {
                int index = node.line - startIndex;
                if (index >= bestNodes.size()) {
                    bestNodes.setSize(index + 1);
                }
                if (bestNodes.get(index) == null
                        || node.totalDemerits < ((KnuthNode) bestNodes.get(index)).totalDemerits) {
                    bestNodes.set(index, node);
                }                
            }
        } while (!queue.isEmpty());
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
        int firstFootnoteListIndex = ((KnuthPageNode) bestActiveNode.previous).
                footnotesProgress.getLastInsertedIndex();
        int firstFootnoteElementIndex = ((KnuthPageNode) bestActiveNode.previous).
                footnotesProgress.getLastElementOfLastInsertedIndex();
        if (firstFootnoteListIndex == -1) {
            firstFootnoteListIndex++;
            firstFootnoteElementIndex = 0;
        } else if (footnotesRecord.getSequence(firstFootnoteListIndex) != null
                && firstFootnoteElementIndex == ((LinkedList) footnotesRecord.
                        getSequence(firstFootnoteListIndex)).size() - 1) {
            // advance to the next list
            firstFootnoteListIndex++;
            firstFootnoteElementIndex = 0;
        } else {
            firstFootnoteElementIndex++;
        }
        // compute the indexes of the first float list
        int firstFloatListIndex = ((KnuthPageNode) bestActiveNode.previous).
                beforeFloatsProgress.getLastInsertedIndex() + 1;

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
                ((KnuthPageNode) bestActiveNode).beforeFloatsProgress.getLastInsertedIndex(),
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
                    deactivateNode(node);
                }
            }
        }
        return bestActiveNode.line;
    }

    public LinkedList getFootnoteList(int index) {
        return (LinkedList) footnotesRecord.getSequence(index);
    }

    public LinkedList getFloatList(int index) {
        return (LinkedList) beforeFloatsRecord.getSequence(index);
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
