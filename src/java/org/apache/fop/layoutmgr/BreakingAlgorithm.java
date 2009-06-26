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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.fo.FONode;

/**
 * The set of nodes is sorted into lines indexed into activeLines.
 * The nodes in each line are linked together in a single linked list by the
 * KnuthNode.next field. The activeLines array contains a link to the head of
 * the linked list in index 'line*2' and a link to the tail at index 'line*2+1'.
 * <p>
 * The set of active nodes can be traversed by
 * <pre>
 * for (int line = startLine; line &lt; endLine; line++) {
 *     for (KnuthNode node = getNode(line); node != null; node = node.next) {
 *         // Do something with 'node'
 *     }
 * }
 * </pre>
 */
public abstract class BreakingAlgorithm {

    /** the logger for the class */
    protected static Log log = LogFactory.getLog(BreakingAlgorithm.class);

    /** Maximum adjustment ration */
    protected static final int INFINITE_RATIO = 1000;

    private static final int MAX_RECOVERY_ATTEMPTS = 5;

    // constants identifying a subset of the feasible breaks
    /** All feasible breaks are ok. */
    public static final int ALL_BREAKS = 0;
    /** This forbids hyphenation. */
    public static final int NO_FLAGGED_PENALTIES = 1;
    /** wrap-option = "no-wrap". */
    public static final int ONLY_FORCED_BREAKS = 2;

    // parameters of Knuth's algorithm:
    /** Penalty value for flagged penalties. */
    private int flaggedPenalty = 50;
    /** Demerit for consecutive lines ending at flagged penalties. */
    protected int repeatedFlaggedDemerit = 50;
    /** Demerit for consecutive lines belonging to incompatible fitness classes . */
    protected int incompatibleFitnessDemerit = 50;
    /** Maximum number of consecutive lines ending with a flagged penalty.
     * Only a value >= 1 is a significant limit.
     */
    protected int maxFlaggedPenaltiesCount;

    /**
     * The threshold for considering breaks to be acceptable. The adjustment ratio must be
     * inferior to this threshold.
     */
    private double threshold;

    /**
     * The paragraph of KnuthElements.
     */
    protected KnuthSequence par;

    /**
     * The width of a line (or height of a column in page-breaking mode).
     * -1 indicates that the line widths are different for each line.
     */
    protected int lineWidth = -1;
    /** Force the algorithm to find a set of breakpoints, even if no feasible breakpoints
     * exist.
     */
    private boolean force =  false;
    /** If set to true, doesn't ignore break possibilities which are definitely too short. */
    protected boolean considerTooShort = false;

    /** When in forced mode, the best node leading to a too long line. The line will be
     * too long anyway, but this one will lead to a paragraph with fewest demerits.
     */
    private KnuthNode lastTooLong;
    /** When in forced mode, the best node leading to a too short line. The line will be
     * too short anyway, but this one will lead to a paragraph with fewest demerits.
     */
    private KnuthNode lastTooShort;
    /** The node to be reactivated if no set of feasible breakpoints can be found for this
     * paragraph.
     */
    private KnuthNode lastDeactivated;

    /** Alignment of the paragraph/page. One of EN_START, EN_JUSTIFY, etc. */
    protected int alignment;
    /** Alignment of the paragraph's last line. */
    protected int alignmentLast;
    /** Used to handle the text-indent property (indent the first line of a paragraph). */
    protected boolean bFirst;

    /**
     * The set of active nodes in ascending line order. For each line l, activeLines[2l] contains a
     * link to l's first active node, and activeLines[2l+1] a link to l's last active node. The
     * line number l corresponds to the number of the line ending at the node's breakpoint.
     */
    protected KnuthNode[] activeLines;

    /**
     * The number of active nodes.
     */
    protected int activeNodeCount;

    /**
     * The lowest available line in the set of active nodes.
     */
    protected int startLine = 0;

    /**
     * The highest + 1 available line in the set of active nodes.
     */
    protected int endLine = 0;

    /**
     * The total width of all elements handled so far.
     */
    protected int totalWidth;

    /**
     * The total stretch of all elements handled so far.
     */
    protected int totalStretch = 0;

    /**
     * The total shrink of all elements handled so far.
     */
    protected int totalShrink = 0;

    protected BestRecords best;

    /** {@inheritDoc} */
    private boolean partOverflowRecoveryActivated = true;
    private KnuthNode lastRecovered;

    /**
     * Create a new instance.
     * @param align alignment of the paragraph/page. One of EN_START, EN_JUSTIFY, etc. For
     * pages EN_BEFORE, EN_AFTER are mapped to the corresponding inline properties
     * (EN_START, EN_END)
     * @param alignLast alignment of the paragraph's last line
     * @param first for the text-indent property (indent the first line of a paragraph)
     * @param partOverflowRecovery true if too long elements should be moved to the next line/part
     * @param maxFlagCount maximum allowed number of consecutive lines ending at a flagged penalty
     * item
     */
    public BreakingAlgorithm(int align, int alignLast,
                             boolean first, boolean partOverflowRecovery,
                             int maxFlagCount) {
        alignment = align;
        alignmentLast = alignLast;
        bFirst = first;
        this.partOverflowRecoveryActivated = partOverflowRecovery;
        this.best = new BestRecords();
        maxFlaggedPenaltiesCount = maxFlagCount;
    }


    /**
     * Class recording all the informations of a feasible breaking point.
     */
    public class KnuthNode {
        /** index of the breakpoint represented by this node */
        public int position;

        /** number of the line ending at this breakpoint */
        public int line;

        /** fitness class of the line ending at this breakpoint. One of 0, 1, 2, 3. */
        public int fitness;

        /** accumulated width of the KnuthElements up to after this breakpoint. */
        public int totalWidth;

        /** accumulated stretchability of the KnuthElements up to after this breakpoint. */
        public int totalStretch;

        /** accumulated shrinkability of the KnuthElements up to after this breakpoint. */
        public int totalShrink;

        /** adjustment ratio if the line ends at this breakpoint */
        public double adjustRatio;

        /** available stretch of the line ending at this breakpoint */
        public int availableShrink;

        /** available shrink of the line ending at this breakpoint */
        public int availableStretch;

        /** difference between target and actual line width */
        public int difference;

        /** minimum total demerits up to this breakpoint */
        public double totalDemerits;

        /** best node for the preceding breakpoint */
        public KnuthNode previous;

        /** next possible node in the same line */
        public KnuthNode next;

        /**
         * Holds the number of subsequent recovery attempty that are made to get content fit
         * into a line.
         */
        public int fitRecoveryCounter = 0;

        public KnuthNode(int position, int line, int fitness,
                         int totalWidth, int totalStretch, int totalShrink,
                         double adjustRatio, int availableShrink, int availableStretch,
                         int difference, double totalDemerits, KnuthNode previous) {
            this.position = position;
            this.line = line;
            this.fitness = fitness;
            this.totalWidth = totalWidth;
            this.totalStretch = totalStretch;
            this.totalShrink = totalShrink;
            this.adjustRatio = adjustRatio;
            this.availableShrink = availableShrink;
            this.availableStretch = availableStretch;
            this.difference = difference;
            this.totalDemerits = totalDemerits;
            this.previous = previous;
        }

        public String toString() {
            return "<KnuthNode at " + position + " "
                    + totalWidth + "+" + totalStretch + "-" + totalShrink
                    + " line:" + line + " prev:" + (previous != null ? previous.position : -1)
                    + " dem:" + totalDemerits + ">";
        }
    }

    /** Class that stores, for each fitness class, the best active node that could start
     * a line of the corresponding fitness ending at the current element.
     */
    protected class BestRecords {
        private static final double INFINITE_DEMERITS = Double.POSITIVE_INFINITY;
        //private static final double INFINITE_DEMERITS = 1E11;

        private double[] bestDemerits = new double[4];
        private KnuthNode[] bestNode = new KnuthNode[4];
        private double[] bestAdjust = new double[4];
        private int[] bestDifference = new int[4];
        private int[] bestAvailableShrink = new int[4];
        private int[] bestAvailableStretch = new int[4];
        /** Points to the fitness class which currently leads to the best demerits. */
        private int bestIndex = -1;

        public BestRecords() {
            reset();
        }

        /** Registers the new best active node for the given fitness class.
         * @param demerits the total demerits of the new optimal set of breakpoints
         * @param node the node starting the line ending at the current element
         * @param adjust adjustment ratio of the current line
         * @param availableShrink how much the current line can be shrinked
         * @param availableStretch how much the current line can be stretched
         * @param difference difference between the width of the considered line and the
         * width of the "real" line
         * @param fitness fitness class of the current line
         */
        public void addRecord(double demerits, KnuthNode node, double adjust,
                              int availableShrink, int availableStretch,
                              int difference, int fitness) {
            if (demerits > bestDemerits[fitness]) {
                log.error("New demerits value greater than the old one");
            }
            bestDemerits[fitness] = demerits;
            bestNode[fitness] = node;
            bestAdjust[fitness] = adjust;
            bestAvailableShrink[fitness] = availableShrink;
            bestAvailableStretch[fitness] = availableStretch;
            bestDifference[fitness] = difference;
            if (bestIndex == -1 || demerits < bestDemerits[bestIndex]) {
                bestIndex = fitness;
            }
        }

        public boolean hasRecords() {
            return (bestIndex != -1);
        }

        /**
         * @param fitness fitness class (0, 1, 2 or 3, i.e. "tight" to "very loose")
         * @return true if there is a set of feasible breakpoints registered for the
         *              given fitness.
         */
        public boolean notInfiniteDemerits(int fitness) {
            return (bestDemerits[fitness] != INFINITE_DEMERITS);
        }

        public double getDemerits(int fitness) {
            return bestDemerits[fitness];
        }

        public KnuthNode getNode(int fitness) {
            return bestNode[fitness];
        }

        public double getAdjust(int fitness) {
            return bestAdjust[fitness];
        }

        public int getAvailableShrink(int fitness) {
            return bestAvailableShrink[fitness];
        }

        public int getAvailableStretch(int fitness) {
            return bestAvailableStretch[fitness];
        }

       public int getDifference(int fitness) {
            return bestDifference[fitness];
        }

        public double getMinDemerits() {
            if (bestIndex != -1) {
                return getDemerits(bestIndex);
            } else {
                // anyway, this should never happen
                return INFINITE_DEMERITS;
            }
        }

        /** Reset when a new breakpoint is being considered. */
        public void reset() {
            for (int i = 0; i < 4; i++) {
                bestDemerits[i] = INFINITE_DEMERITS;
                // there is no need to reset the other arrays
            }
            bestIndex = -1;
        }
    }

    /**
     * @return the number of times the algorithm should try to move overflowing content to the
     * next line/page.
     */
    protected int getMaxRecoveryAttempts() {
        return MAX_RECOVERY_ATTEMPTS;
    }

    /**
     * Controls the behaviour of the algorithm in cases where the first element of a part
     * overflows a line/page.
     * @return true if the algorithm should try to send the element to the next line/page.
     */
    protected boolean isPartOverflowRecoveryActivated() {
        return this.partOverflowRecoveryActivated;
    }

    /** Empty method, hook for subclasses. Called before determining the optimal
     * breakpoints corresponding to a given active node.
     * @param total number of lines for the active node
     * @param demerits total demerits of the paragraph for the active node
     */
    public abstract void updateData1(int total, double demerits);

    /** Empty method, hook for subclasses. Called when determining the optimal breakpoints
     * for a given active node.
     * @param bestActiveNode a node in the chain of best active nodes, corresponding to
     * one of the optimal breakpoints
     * @param sequence the corresponding paragraph
     * @param total the number of lines into which the paragraph will be broken
     * @see #calculateBreakPoints(KnuthNode, KnuthSequence, int)
     */
    public abstract void updateData2(KnuthNode bestActiveNode,
                                     KnuthSequence sequence,
                                     int total);

    public void setConstantLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    /** @see #findBreakingPoints(KnuthSequence, int, double, boolean, int) */
    public int findBreakingPoints(KnuthSequence par,
                                  double threshold,
                                  boolean force,
                                  int allowedBreaks) {
        return findBreakingPoints(par, 0, threshold, force, allowedBreaks);
    }

    /** Finds an optimal set of breakpoints for the given paragraph.
     * @param par the paragraph to break
     * @param startIndex index of the Knuth element at which the breaking must start
     * @param threshold upper bound of the adjustment ratio
     * @param force true if a set of breakpoints must be found even if there are no
     * feasible ones
     * @param allowedBreaks one of ONLY_FORCED_BREAKS, NO_FLAGGED_PENALTIES, ALL_BREAKS
     */
    public int findBreakingPoints(KnuthSequence par, int startIndex,
                                  double threshold, boolean force,
                                  int allowedBreaks) {
        this.par = par;
        this.threshold = threshold;
        this.force = force;
        //this.lineWidth = lineWidth;
        initialize();

        activeLines = new KnuthNode[20];

        // reset lastTooShort and lastTooLong, as they could be not null
        // because of previous calls to findBreakingPoints
        lastTooShort = lastTooLong = null;
        // reset startLine and endLine
        startLine = endLine = 0;
        // current element in the paragraph
        KnuthElement thisElement = null;
        // previous element in the paragraph is a KnuthBox?
        boolean previousIsBox = false;

        // index of the first KnuthBox in the sequence
        int firstBoxIndex = startIndex;
        if (alignment != org.apache.fop.fo.Constants.EN_CENTER) {
            while (par.size() > firstBoxIndex
                    && !((KnuthElement) par.get(firstBoxIndex)).isBox()) {
                firstBoxIndex++;
            }
        }

        // create an active node representing the starting point
        activeLines = new KnuthNode[20];
        addNode(0, createNode(firstBoxIndex, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, null));

        if (log.isTraceEnabled()) {
            log.trace("Looping over " + (par.size() - startIndex) + " elements");
        }

        KnuthNode lastForced = getNode(0);

        // main loop
        for (int i = startIndex; i < par.size(); i++) {
            thisElement = getElement(i);
            if (thisElement.isBox()) {
                // a KnuthBox object is not a legal line break
                totalWidth += thisElement.getW();
                previousIsBox = true;
                handleBox((KnuthBox) thisElement);
            } else if (thisElement.isGlue()) {
                // a KnuthGlue object is a legal line break
                // only if the previous object is a KnuthBox
                // consider these glues according to the value of allowedBreaks
                if (previousIsBox
                    && !(allowedBreaks == ONLY_FORCED_BREAKS)) {
                    considerLegalBreak(thisElement, i);
                }
                totalWidth += thisElement.getW();
                totalStretch += thisElement.getY();
                totalShrink += thisElement.getZ();
                previousIsBox = false;
            } else {
                // a KnuthPenalty is a legal line break
                // only if its penalty is not infinite;
                // consider all penalties, non-flagged penalties or non-forcing penalties
                // according to the value of allowedBreaks
                if (((KnuthPenalty) thisElement).getP() < KnuthElement.INFINITE
                    && (!(allowedBreaks == NO_FLAGGED_PENALTIES)
                            || !(((KnuthPenalty) thisElement).isFlagged()))
                    && (!(allowedBreaks == ONLY_FORCED_BREAKS)
                            || ((KnuthPenalty) thisElement).getP() == -KnuthElement.INFINITE)) {
                    considerLegalBreak(thisElement, i);
                }
                previousIsBox = false;
            }
            if (activeNodeCount == 0) {
                if (ipdChanged()) {
                    return handleIpdChange();
                }
                if (!force) {
                    log.debug("Could not find a set of breaking points " + threshold);
                    return 0;
                }
                // lastDeactivated was a "good" break, while lastTooShort and lastTooLong
                // were "bad" breaks since the beginning;
                // if it is not the node we just restarted from, lastDeactivated can
                // replace either lastTooShort or lastTooLong
                if (lastDeactivated != null && lastDeactivated != lastForced) {
                    if (lastDeactivated.adjustRatio > 0) {
                        lastTooShort = lastDeactivated;
                    } else {
                        lastTooLong = lastDeactivated;
                    }
                }
                if (lastTooShort == null || lastForced.position == lastTooShort.position) {
                    if (isPartOverflowRecoveryActivated()) {
                        if (this.lastRecovered == null) {
                            this.lastRecovered = lastTooLong;
                            if (log.isDebugEnabled()) {
                                log.debug("Recovery point: " + lastRecovered);
                            }
                        }
                        // content would overflow, insert empty line/page and try again
                        KnuthNode node = createNode(
                                lastTooLong.previous.position, lastTooLong.previous.line + 1, 1,
                                0, 0, 0,
                                0, 0, 0,
                                0, 0, lastTooLong.previous);
                        lastForced = node;
                        node.fitRecoveryCounter = lastTooLong.previous.fitRecoveryCounter + 1;
                        if (log.isDebugEnabled()) {
                            log.debug("first part doesn't fit into line, recovering: "
                                    + node.fitRecoveryCounter);
                        }
                        if (node.fitRecoveryCounter > getMaxRecoveryAttempts()) {
                            while (lastForced.fitRecoveryCounter > 0) {
                                lastForced = lastForced.previous;
                                lastDeactivated = lastForced.previous;
                                startLine--;
                                endLine--;
                            }
                            lastForced = this.lastRecovered;
                            this.lastRecovered = null;
                            startLine = lastForced.line;
                            endLine = lastForced.line;
                            log.debug("rolled back...");
                        }
                    } else {
                        lastForced = lastTooLong;
                    }
                } else {
                    lastForced = lastTooShort;
                    this.lastRecovered = null;
                }

                if (log.isDebugEnabled()) {
                    log.debug("Restarting at node " + lastForced);
                }
                i = restartFrom(lastForced, i);
            }
        }
        finish();
        if (log.isTraceEnabled()) {
            log.trace("Main loop completed " + activeNodeCount);
            log.trace("Active nodes=" + toString(""));
        }

        // there is at least one set of breaking points
        // select one or more active nodes, removing the others from the list
        int line = filterActiveNodes();

        // for each active node, create a set of breaking points
        for (int i = startLine; i < endLine; i++) {
            for (KnuthNode node = getNode(i); node != null; node = node.next) {
                updateData1(node.line, node.totalDemerits);
                calculateBreakPoints(node, par, node.line);
            }
        }

        activeLines = null;
        return line;
    }

    protected boolean ipdChanged() {
        return false;
    }

    protected int handleIpdChange() {
        throw new IllegalStateException();
    }

    /**
     * This method tries to find the context FO for a position in a KnuthSequence.
     * @param seq the KnuthSequence to inspect
     * @param position the index of the position in the KnuthSequence
     * @return the requested context FO note or null, if no context node could be determined
     */
    private FONode findContextFO(KnuthSequence seq, int position) {
        ListElement el = seq.getElement(position);
        while (el.getLayoutManager() == null && position < seq.size() - 1) {
            position++;
            el = seq.getElement(position);
        }
        Position pos = (el != null ? el.getPosition() : null);
        LayoutManager lm = (pos != null ? pos.getLM() : null);
        while (pos instanceof NonLeafPosition) {
            pos = ((NonLeafPosition)pos).getPosition();
            if (pos != null && pos.getLM() != null) {
                lm = pos.getLM();
            }
        }
        if (lm != null) {
            return lm.getFObj();
        } else {
            return null;
        }
    }

    /** Resets the algorithm's variables. */
    protected void initialize() {
        this.totalWidth = 0;
        this.totalStretch = 0;
        this.totalShrink = 0;
    }

    /** Creates a new active node for a feasible breakpoint at the given position. Only
     * called in forced mode.
     * @param position index of the element in the Knuth sequence
     * @param line number of the line ending at the breakpoint
     * @param fitness fitness class of the line ending at the breakpoint. One of 0, 1, 2, 3.
     * @param totalWidth accumulated width of the KnuthElements up to after the breakpoint
     * @param totalStretch accumulated stretchability of the KnuthElements up to after the
     * breakpoint
     * @param totalShrink accumulated shrinkability of the KnuthElements up to after the
     * breakpoint
     * @param adjustRatio adjustment ratio if the line ends at this breakpoint
     * @param availableShrink available stretch of the line ending at this breakpoint
     * @param availableStretch available shrink of the line ending at this breakpoint
     * @param difference difference between target and actual line width
     * @param totalDemerits minimum total demerits up to the breakpoint
     * @param previous active node for the preceding breakpoint
     */
    protected KnuthNode createNode(int position, int line, int fitness,
                                   int totalWidth, int totalStretch, int totalShrink,
                                   double adjustRatio, int availableShrink, int availableStretch,
                                   int difference, double totalDemerits, KnuthNode previous) {
        return new KnuthNode(position, line, fitness,
                             totalWidth, totalStretch, totalShrink,
                             adjustRatio, availableShrink, availableStretch,
                             difference, totalDemerits, previous);
    }

    /** Creates a new active node for a break from the best active node of the given
     * fitness class to the element at the given position.
     * @see #createNode(int, int, int, int, int, int, double, int, int, int, double, org.apache.fop.layoutmgr.BreakingAlgorithm.KnuthNode)
     * @see BreakingAlgorithm.BestRecords
     */
    protected KnuthNode createNode(int position, int line, int fitness,
                                   int totalWidth, int totalStretch, int totalShrink) {
        return new KnuthNode(position, line, fitness,
                             totalWidth, totalStretch, totalShrink, best.getAdjust(fitness),
                             best.getAvailableShrink(fitness), best.getAvailableStretch(fitness),
                             best.getDifference(fitness), best.getDemerits(fitness),
                             best.getNode(fitness));
    }

    /** Empty method, hook for subclasses. */
    protected void handleBox(KnuthBox box) {
    }

    protected int restartFrom(KnuthNode restartingNode, int currentIndex) {
        restartingNode.totalDemerits = 0;
        addNode(restartingNode.line, restartingNode);
        startLine = restartingNode.line;
        endLine = startLine + 1;
        totalWidth = restartingNode.totalWidth;
        totalStretch = restartingNode.totalStretch;
        totalShrink = restartingNode.totalShrink;
        lastTooShort = null;
        lastTooLong = null;
        // the width, stretch and shrink already include the width,
        // stretch and shrink of the suppressed glues;
        // advance in the sequence in order to avoid taking into account
        // these elements twice
        int restartingIndex = restartingNode.position;
        while (restartingIndex + 1 < par.size()
               && !(getElement(restartingIndex + 1).isBox())) {
            restartingIndex++;
        }
        return restartingIndex;
    }

    /** Determines if the given breakpoint is a feasible breakpoint. That is, if a decent
     * line may be built between one of the currently active nodes and this breakpoint.
     * @param element the paragraph's element to consider
     * @param elementIdx the element's index inside the paragraph
     */
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
            for (KnuthNode node = getNode(line); node != null; node = node.next) {
                if (node.position == elementIdx) {
                    continue;
                }
                int difference = computeDifference(node, element, elementIdx);
                double r = computeAdjustmentRatio(node, difference);
                int availableShrink = totalShrink - node.totalShrink;
                int availableStretch = totalStretch - node.totalStretch;
                if (log.isTraceEnabled()) {
                    log.trace("\tr=" + r + " difference=" + difference);
                    log.trace("\tline=" + line);
                }

                // The line would be too long.
                if (r < -1 || element.isForcedBreak()) {
                    // Deactivate node.
                    if (log.isTraceEnabled()) {
                        log.trace("Removing " + node);
                    }
                    removeNode(line, node);
                    lastDeactivated = compareNodes(lastDeactivated, node);
                }

                // The line is within the available shrink and the threshold.
                if (r >= -1 && r <= threshold) {
                    int fitnessClass = computeFitness(r);
                    double demerits = computeDemerits(node, element, fitnessClass, r);

                    if (log.isTraceEnabled()) {
                        log.trace("\tDemerits=" + demerits);
                        log.trace("\tFitness class=" + fitnessClass);
                    }

                    if (demerits < best.getDemerits(fitnessClass)) {
                        // updates best demerits data
                        best.addRecord(demerits, node, r, availableShrink, availableStretch,
                                       difference, fitnessClass);
                        lastTooShort = null;
                    }
                }

                // The line is way too short, but we are in forcing mode, so a node is
                // calculated and stored in lastValidNode.
                if (force && (r <= -1 || r > threshold)) {
                    int fitnessClass = computeFitness(r);
                    double demerits = computeDemerits(node, element, fitnessClass, r);
                    int newWidth = totalWidth;
                    int newStretch = totalStretch;
                    int newShrink = totalShrink;

                    // add the width, stretch and shrink of glue elements after
                    // the break
                    // this does not affect the dimension of the line / page, only
                    // the values stored in the node; these would be as if the break
                    // was just before the next box element, thus ignoring glues and
                    // penalties between the "real" break and the following box
                    for (int i = elementIdx; i < par.size(); i++) {
                        KnuthElement tempElement = getElement(i);
                        if (tempElement.isBox()) {
                            break;
                        } else if (tempElement.isGlue()) {
                            newWidth += tempElement.getW();
                            newStretch += tempElement.getY();
                            newShrink += tempElement.getZ();
                        } else if (tempElement.isForcedBreak() && i != elementIdx) {
                            break;
                        }
                    }

                    if (r <= -1) {
                        if (lastTooLong == null || demerits < lastTooLong.totalDemerits) {
                            lastTooLong = createNode(elementIdx, line + 1, fitnessClass,
                                    newWidth, newStretch, newShrink,
                                    r, availableShrink, availableStretch,
                                    difference, demerits, node);
                            if (log.isTraceEnabled()) {
                                log.trace("Picking tooLong " + lastTooLong);
                            }
                        }
                    } else {
                        if (lastTooShort == null || demerits <= lastTooShort.totalDemerits) {
                            if (considerTooShort) {
                                //consider possibilities which are too short
                                best.addRecord(demerits, node, r,
                                        availableShrink, availableStretch,
                                        difference, fitnessClass);
                            }
                            lastTooShort = createNode(elementIdx, line + 1, fitnessClass,
                                    newWidth, newStretch, newShrink,
                                    r, availableShrink, availableStretch,
                                    difference, demerits, node);
                            if (log.isTraceEnabled()) {
                                log.trace("Picking tooShort " + lastTooShort);
                            }
                        }
                    }
                }
            }
            addBreaks(line, elementIdx);
        }
    }

    /**
     * Adds new active nodes for breaks at the given element.
     * @param line number of the previous line; this element will end line number (line+1)
     * @param elementIdx the element's index
     */
    private void addBreaks(int line, int elementIdx) {
        if (!best.hasRecords()) {
            return;
        }

        int newWidth = totalWidth;
        int newStretch = totalStretch;
        int newShrink = totalShrink;

        // add the width, stretch and shrink of glue elements after
        // the break
        // this does not affect the dimension of the line / page, only
        // the values stored in the node; these would be as if the break
        // was just before the next box element, thus ignoring glues and
        // penalties between the "real" break and the following box
        for (int i = elementIdx; i < par.size(); i++) {
            KnuthElement tempElement = getElement(i);
            if (tempElement.isBox()) {
                break;
            } else if (tempElement.isGlue()) {
                newWidth += tempElement.getW();
                newStretch += tempElement.getY();
                newShrink += tempElement.getZ();
            } else if (tempElement.isForcedBreak() && i != elementIdx) {
                break;
            }
        }

        // add nodes to the active nodes list
        double minimumDemerits = best.getMinDemerits() + incompatibleFitnessDemerit;
        for (int i = 0; i <= 3; i++) {
            if (best.notInfiniteDemerits(i) && best.getDemerits(i) <= minimumDemerits) {
                // the nodes in activeList must be ordered
                // by line number and position;
                if (log.isTraceEnabled()) {
                    log.trace("\tInsert new break in list of " + activeNodeCount
                            + " from fitness class " + i);
                }
                KnuthNode newNode = createNode(elementIdx, line + 1, i,
                                               newWidth, newStretch, newShrink);
                addNode(line + 1, newNode);
            }
        }
        best.reset();
    }

    /**
     * Return the difference between the natural width of a line that would be made
     * between the given active node and the given element, and the available width of the
     * real line.
     * @param activeNode node for the previous breakpoint
     * @param element currently considered breakpoint
     * @return The difference in width. Positive numbers mean extra space in the line,
     * negative number that the line overflows.
     */
    protected int computeDifference(KnuthNode activeNode, KnuthElement element,
                                    int elementIndex) {
        // compute the adjustment ratio
        int actualWidth = totalWidth - activeNode.totalWidth;
        if (element.isPenalty()) {
            actualWidth += element.getW();
        }
        return getLineWidth() - actualWidth;
    }

    /**
     * Return the adjust ration needed to make up for the difference. A ration of
     * <ul>
     *    <li>0 means that the break has the exact right width</li>
     *    <li>&gt;= -1 &amp;&amp; &lt; 0  means that the break is wider than the line,
     *        but within the minimim values of the glues.</li>
     *    <li>&gt;0 &amp;&amp; &lt; 1 means that the break is smaller than the line width,
     *        but within the maximum values of the glues.</li>
     *    <li>&gt; 1 means that the break is too small to make up for the glues.</li>
     * </ul>
     * @param activeNode
     * @param difference
     * @return The ration.
     */
    protected double computeAdjustmentRatio(KnuthNode activeNode, int difference) {
        // compute the adjustment ratio
        if (difference > 0) {
            int maxAdjustment = totalStretch - activeNode.totalStretch;
            if (maxAdjustment > 0) {
                return (double) difference / maxAdjustment;
            } else {
                return INFINITE_RATIO;
            }
        } else if (difference < 0) {
            int maxAdjustment = totalShrink - activeNode.totalShrink;
            if (maxAdjustment > 0) {
                return (double) difference / maxAdjustment;
            } else {
                return -INFINITE_RATIO;
            }
        } else {
            return 0;
        }
    }

    /**
     * Figure out the fitness class of this line (tight, loose,
     * very tight or very loose).
     * See the section on "More Bells and Whistles" in Knuth's
     * "Breaking Paragraphs Into Lines".
     * @param r
     * @return the fitness class
     */
    private int computeFitness(double r) {
        if (r < -0.5) {
            return 0;
        } else if (r <= 0.5) {
            return 1;
        } else if (r <= 1) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * Computes the demerits of the current breaking (that is, up to the given element),
     * if the next-to-last chosen breakpoint is the given active node. This adds to the
     * total demerits of the given active node, the demerits of a line starting at this
     * node and ending at the given element.
     * @param activeNode considered preceding line break
     * @param element considered current line break
     * @param fitnessClass fitness of the current line
     * @param r adjustment ratio for the current line
     * @return the demerit of the current line
     */
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
            // there are at least two consecutive lines ending with a flagged penalty;
            // check if the previous line end with a flagged penalty too,
            // and if this situation is allowed
            int flaggedPenaltiesCount = 2;
            for (KnuthNode prevNode = activeNode.previous;
                 prevNode != null && flaggedPenaltiesCount <= maxFlaggedPenaltiesCount;
                 prevNode = prevNode.previous) {
                KnuthElement prevElement = getElement(prevNode.position);
                if (prevElement.isPenalty()
                    && ((KnuthPenalty) prevElement).isFlagged()) {
                    // the previous line ends with a flagged penalty too
                    flaggedPenaltiesCount++;
                } else {
                    // the previous line does not end with a flagged penalty,
                    // exit the loop
                    break;
                }
            }
            if (maxFlaggedPenaltiesCount >= 1
                && flaggedPenaltiesCount > maxFlaggedPenaltiesCount) {
                // add infinite demerits, so this break will not be chosen
                // unless there isn't any alternative break
                demerits += BestRecords.INFINITE_DEMERITS;
            }
        }
        if (Math.abs(fitnessClass - activeNode.fitness) > 1) {
            // add demerit for consecutive breaks
            // with very different fitness classes
            demerits += incompatibleFitnessDemerit;
        }
        demerits += activeNode.totalDemerits;
        return demerits;
    }

    protected void finish() {
    }

    /**
     * Return the element at index idx in the paragraph.
     * @param idx index of the element.
     * @return the element at index idx in the paragraph.
     */
    protected KnuthElement getElement(int idx) {
        return (KnuthElement) par.get(idx);
    }

    /**
     * Compare two KnuthNodes and return the node with the least demerit.
     * @param node1 The first knuth node.
     * @param node2 The other knuth node.
     * @return the node with the least demerit.
     */
    protected KnuthNode compareNodes(KnuthNode node1, KnuthNode node2) {
        if (node1 == null || node2.position > node1.position) {
            return node2;
        }
        if (node2.position == node1.position) {
            if (node2.totalDemerits < node1.totalDemerits) {
                return node2;
            }
        }
        return node1;
    }

    /**
     * Add a node at the end of the given line's existing active nodes.
     * If this is the first node in the line, adjust endLine accordingly.
     * @param line number of the line ending at the node's corresponding breakpoint
     * @param node the active node to add
     */
    protected void addNode(int line, KnuthNode node) {
        int headIdx = line * 2;
        if (headIdx >= activeLines.length) {
            KnuthNode[] oldList = activeLines;
            activeLines = new KnuthNode[headIdx + headIdx];
            System.arraycopy(oldList, 0, activeLines, 0, oldList.length);
        }
        node.next = null;
        if (activeLines[headIdx + 1] != null) {
            activeLines[headIdx + 1].next = node;
        } else {
            activeLines[headIdx] = node;
            endLine = line + 1;
        }
        activeLines[headIdx + 1] = node;
        activeNodeCount++;
    }

    /**
     * Remove the given active node registered for the given line. If there are no more active nodes
     * for this line, adjust the startLine accordingly.
     * @param line number of the line ending at the node's corresponding breakpoint
     * @param node the node to deactivate
     */
    protected void removeNode(int line, KnuthNode node) {
        int headIdx = line * 2;
        KnuthNode n = getNode(line);
        if (n != node) {
            // nodes could be rightly deactivated in a different order
            KnuthNode prevNode = null;
            while (n != node) {
                prevNode = n;
                n = n.next;
            }
            prevNode.next = n.next;
            if (prevNode.next == null) {
                activeLines[headIdx + 1] = prevNode;
            }
        } else {
            activeLines[headIdx] = node.next;
            if (node.next == null) {
                activeLines[headIdx + 1] = null;
            }
            while (startLine < endLine && getNode(startLine) == null) {
                startLine++;
            }
        }
        activeNodeCount--;
    }

    /**
     * Returns the first active node for the given line.
     * @param line the line/part number
     * @return the requested active node
     */
    protected KnuthNode getNode(int line) {
        return activeLines[line * 2];
    }

    /**
     * Returns the line/part width of a given line/part.
     * @param line the line/part number
     * @return the width/length in millipoints
     */
    protected int getLineWidth(int line) {
        assert lineWidth >= 0;
        return this.lineWidth;
    }

    /** @return the constant line/part width or -1 if there is no such value */
    protected int getLineWidth() {
        return this.lineWidth;
    }

    /**
     * Creates a string representation of the active nodes. Used for debugging.
     * @param prepend a string to prepend on each entry
     * @return the requested string
     */
    public String toString(String prepend) {
        StringBuffer sb = new StringBuffer();
        sb.append("[\n");
        for (int i = startLine; i < endLine; i++) {
            for (KnuthNode node = getNode(i); node != null; node = node.next) {
                sb.append(prepend + "\t" + node + ",\n");
            }
        }
        sb.append(prepend + "]");
        return sb.toString();
    }

    protected abstract int filterActiveNodes();

    /**
     * Determines the set of optimal breakpoints corresponding to the given active node.
     * @param node the active node
     * @param par the corresponding paragraph
     * @param total the number of lines into which the paragraph will be broken
     */
    protected void calculateBreakPoints(KnuthNode node, KnuthSequence par,
                                      int total) {
        KnuthNode bestActiveNode = node;
        // use bestActiveNode to determine the optimum breakpoints
        for (int i = node.line; i > 0; i--) {
            updateData2(bestActiveNode, par, total);
            bestActiveNode = bestActiveNode.previous;
        }
    }

    /** @return the alignment for normal lines/parts */
    public int getAlignment() {
        return this.alignment;
    }

    /** @return the alignment for the last line/part */
    public int getAlignmentLast() {
        return this.alignmentLast;
    }

}
