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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.traits.MinOptMax;

/**
 * The set of nodes is sorted into lines indexed into activeLines.
 * The nodes in each line are linked together in a single linked list by the 
 * KnuthNode.next field. The activeLines array contains a link to the head of
 * the linked list in index 'line*2' and a link to the tail at index 'line*2+1'.
 * <p>
 * The set of active nodes can be traversed by 
 * <pre>
 * for (int line = startLine; line < endLine; line++) {
 *     for (KnuthNode node = getNode(line); node != null; node = node.next) {
 *         // Do something with 'node'
 *     }
 * }
 * </pre> 
 */
public abstract class BreakingAlgorithm {
    // parameters of Knuth's algorithm:
    // penalty value for flagged penalties
    private int flaggedPenalty = 50;
    // demerit for consecutive lines ending at flagged penalties
    private int repeatedFlaggedDemerit = 50;
    // demerit for consecutive lines belonging to incompatible fitness classes 
    private int incompatibleFitnessDemerit = 50;
    // suggested modification to the "optimum" number of lines
    private int looseness = 0;

    /**
     * The threshold for considering breaks to be acceptable.
     */
    private double threshold;

    /**
     * The paragraph of KnuthElements.
     */
    private KnuthSequence par;
    
    /**
     * The width of a line.
     */
    private int lineWidth = 0;
    private boolean force =  false;

    protected KnuthNode lastDeactivatedNode = null;
    private KnuthNode lastTooLong;
    private KnuthNode lastTooShort;
    private KnuthNode lastDeactivated;

    protected int alignment;
    protected int alignmentLast;
    protected boolean bFirst;

    /**
     * The set of active nodes.
     */
    private KnuthNode[] activeLines;
    
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
    private int totalWidth;

    /**
     * The total stretch of all elements handled so far.
     */
    private int totalStretch = 0;

    /**
     * The total shrink of all elements handled so far.
     */
    private int totalShrink = 0;

    private BestRecords best;
    private KnuthNode[] positions;

    private static final int INFINITE_RATIO = 1000;

    protected static Log log = LogFactory.getLog(KnuthParagraph.class);

    public BreakingAlgorithm(int align, int alignLast,
                             boolean first) {
        alignment = align;
        alignmentLast = alignLast;
        bFirst = first;
        this.best = new BestRecords();
    }


    // this class represent a feasible breaking point
    public class KnuthNode {
        // index of the breakpoint represented by this node
        public int position;

        // number of the line ending at this breakpoint
        public int line;

        // fitness class of the line ending at his breakpoint
        public int fitness;

        // accumulated width of the KnuthElements
        public int totalWidth;

        // accumulated stretchability of the KnuthElements
        public int totalStretch;

        // accumulated shrinkability of the KnuthElements
        public int totalShrink;

        // adjustment ratio if the line ends at this breakpoint
        public double adjustRatio;

        // available stretch of the line ending at this breakpoint
        public int availableShrink;

        // available shrink of the line ending at this breakpoint
        public int availableStretch;

        // difference between target and actual line width
        public int difference;

        // minimum total demerits up to this breakpoint
        public double totalDemerits;

        // best node for the preceding breakpoint
        public KnuthNode previous;

        // next possible node in the same line 
        public KnuthNode next;


        public KnuthNode(int position, int line, int fitness,
                         int totalWidth, int totalStretch, int totalShrink,
                         double adjustRatio, int availableShrink, int availableStretch, int difference,
                         double totalDemerits, KnuthNode previous) {
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
            return "<KnuthNode at " + position + " " +
            totalWidth + "+" + totalStretch + "-" + totalShrink +
            " line:" + line +
            " prev:" + (previous != null ? previous.position : -1) +
            " dem:" + totalDemerits +
            ">"; 
        }
    }

    // this class stores information about how the nodes
    // which could start a line
    // ending at the current element
    private class BestRecords {
        private static final double INFINITE_DEMERITS = Double.POSITIVE_INFINITY;
        //private static final double INFINITE_DEMERITS = 1E11;

        private double bestDemerits[] = new double[4];
        private KnuthNode bestNode[] = new KnuthNode[4];
        private double bestAdjust[] = new double[4];
        private int bestDifference[] = new int[4];
        private int bestAvailableShrink[] = new int[4];
        private int bestAvailableStretch[] = new int[4];
        private int bestIndex = -1;

        public BestRecords() {
            reset();
        }

        public void addRecord(double demerits, KnuthNode node, double adjust,
                              int availableShrink, int availableStretch, int difference, int fitness) {
            if (demerits > bestDemerits[fitness]) {
                log.error("New demerits value greter than the old one");
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

        public void reset() {
            for (int i = 0; i < 4; i ++) {
                bestDemerits[i] = INFINITE_DEMERITS;
                bestNode[i] = null;
                bestAdjust[i] = 0.0;
                bestDifference[i] = 0;
                bestAvailableShrink[i] = 0;
                bestAvailableStretch[i] = 0;
            }
            bestIndex = -1;
        }
    }



    public abstract void updateData1(int total, double demerits) ;

    public abstract void updateData2(KnuthNode bestActiveNode,
                                     KnuthSequence sequence,
                                     int total) ;

    public int findBreakingPoints(KnuthSequence par, int lineWidth,
                                  double threshold, boolean force,
                                  boolean hyphenationAllowed) {
        this.par = par;
        this.threshold = threshold;
        this.force = force;
        this.lineWidth = lineWidth;
        this.totalWidth = 0;
        this.totalStretch = 0;
        this.totalShrink = 0;

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
        int firstBoxIndex = 0;
        while (alignment != org.apache.fop.fo.Constants.EN_CENTER
               && ! ((KnuthElement) par.get(firstBoxIndex)).isBox()) {
            firstBoxIndex++;
        }

        // create an active node representing the starting point
        activeLines = new KnuthNode[20];
        addNode(0, new KnuthNode(firstBoxIndex, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, null));

        if (log.isTraceEnabled()) {
            log.trace("Looping over " + par.size() + " box objects");
        }
        
        KnuthNode lastForced = getNode(0);

        // main loop
        for (int i = 0; i < par.size(); i++) {
            thisElement = getElement(i);
            if (thisElement.isBox()) {
                // a KnuthBox object is not a legal line break
                totalWidth += thisElement.getW();
                previousIsBox = true;
            } else if (thisElement.isGlue()) {
                // a KnuthGlue object is a legal line break
                // only if the previous object is a KnuthBox
                if (previousIsBox) {
                    considerLegalBreak(thisElement, i);
                }
                totalWidth += thisElement.getW();
                totalStretch += thisElement.getY();
                totalShrink += thisElement.getZ();
                previousIsBox = false;
            } else {
                // a KnuthPenalty is a legal line break
                // only if its penalty is not infinite;
                // if hyphenationAllowed is false, ignore flagged penalties
                if (((KnuthPenalty) thisElement).getP()
                    < KnuthElement.INFINITE
                    && (hyphenationAllowed || !((KnuthPenalty) thisElement).isFlagged())) {
                    considerLegalBreak(thisElement, i);
                }
                previousIsBox = false;
            }
            if (activeNodeCount == 0) {
                if (!force) {
                    log.debug("Could not find a set of breaking points " + threshold);
                    return 0;
                }
                if (lastTooShort == null || lastForced.position == lastTooShort.position) {
                    lastForced = lastTooLong;
                } else {
                    lastForced = lastTooShort;
                }

                log.debug("Restarting at node " + lastForced);
                lastForced.totalDemerits = 0;
                addNode(lastForced.line, lastForced);
                i = lastForced.position;
                startLine = lastForced.line;
                endLine = startLine + 1;
                totalWidth = lastForced.totalWidth;
                totalStretch = lastForced.totalStretch;
                totalShrink = lastForced.totalShrink;
                lastTooShort = lastTooLong = null;
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("Main loop completed " + activeNodeCount);
            log.trace("Active nodes=" + toString(""));
        }

        // there is at least one set of breaking points
        // select one or more active nodes, removing the others from the list
        int line = filterActiveNodes();

        // for each active node, create a set of breaking points
        for (int i = startLine; i < endLine; i++) {
            for (KnuthNode node = getNode(line); node != null; node = node.next) {
                updateData1(node.line, node.totalDemerits);
                calculateBreakPoints(node, par, node.line);
            }
        }

        activeLines = null;
        return line;
    }

    private void considerLegalBreak(KnuthElement element, int elementIdx) {

        if (log.isTraceEnabled()) {
            log.trace("Feasible breakpoint at " + par.indexOf(element) + " " + totalWidth + "+" + totalStretch + "-" + totalShrink);
            log.trace("\tCurrent active node list: " + activeNodeCount + " " + this.toString("\t"));
        }

        lastDeactivated = null;
        lastTooLong = null;
        for (int line = startLine; line < endLine; line++) {
            for (KnuthNode node = getNode(line); node != null; node = node.next) {
                if (node.position == elementIdx) {
                    continue;
                }
                int difference = computeDifference(node, element);
                double r = computeAdjustmentRatio(node, difference);
                int availableShrink = totalShrink - node.totalShrink;
                int availableStretch = totalStretch - node.totalStretch;
                if (log.isTraceEnabled()) {
                    log.trace("\tr=" + r);
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
                    if (r <= -1) {
                        if (lastTooLong == null || demerits < lastTooLong.totalDemerits) {
                            lastTooLong = new KnuthNode(elementIdx, line + 1, fitnessClass,
                                    totalWidth, totalStretch, totalShrink,
                                    r, availableShrink, availableStretch,
                                    difference, demerits, node);
                            if (log.isTraceEnabled()) {
                                log.trace("Picking tooLong " + lastTooLong);
                            }
                        }
                    } else {
                        if (lastTooShort == null || demerits <= lastTooShort.totalDemerits) {
                            lastTooShort = new KnuthNode(elementIdx, line + 1, fitnessClass,
                                    totalWidth, totalStretch, totalShrink,
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

    private void addBreaks(int line, int elementIdx) {
        if (!best.hasRecords()) {
            return;
        }

        int newWidth = totalWidth;
        int newStretch = totalStretch;
        int newShrink = totalShrink;

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
                    log.trace("\tInsert new break in list of " + activeNodeCount);
                }
                KnuthNode newNode = new KnuthNode(elementIdx, line + 1, i,
                                   newWidth, newStretch, newShrink,
                                   best.getAdjust(i),
                                   best.getAvailableShrink(i),
                                   best.getAvailableStretch(i),
                                   best.getDifference(i),
                                   best.getDemerits(i),
                                   best.getNode(i));
                addNode(line + 1, newNode);
            }
        }
        best.reset();
    }

    /**
     * Return the difference between the line width and the width of the break that
     * ends in 'element'.
     * @param activeNode
     * @param element
     * @return The difference in width. Positive numbers mean extra space in the line,
     * negative number that the line overflows. 
     */
    private int computeDifference(KnuthNode activeNode, KnuthElement element) {
        // compute the adjustment ratio
        int actualWidth = totalWidth - activeNode.totalWidth;
        if (element.isPenalty()) {
            actualWidth += element.getW();
        }
        return lineWidth - actualWidth;
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
    private double computeAdjustmentRatio(KnuthNode activeNode, int difference) {
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
     * @param r
     * @return
     */
    private int computeFitness(double r) {
        int newFitnessClass;
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

    private double computeDemerits(KnuthNode activeNode, KnuthElement element, 
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
        demerits += activeNode.totalDemerits;
        return demerits;
    }

    /**
     * Return the element at index idx in the paragraph.
     * @param idx index of the element.
     * @return
     */
    private KnuthElement getElement(int idx) {
        return (KnuthElement) par.get(idx);
    }

    /**
     * Compare two KnuthNodes and return the node with the least demerit. 
     * @param node1 The first knuth node.
     * @param node2 The other knuth node.
     * @return
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
     * Add a KnuthNode at the end of line 'line'. 
     * If this is the first node in the line, adjust endLine accordingly.
     * @param line
     * @param node
     */
    private void addNode(int line, KnuthNode node) {
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
            endLine = line+1;
        }
        activeLines[headIdx + 1] = node;
        activeNodeCount++;
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
            log.error("Should be first");
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

    protected KnuthNode getNode(int line) {
        return activeLines[line * 2];
    }

    /**
     * Return true if the position 'idx' is a legal breakpoint.
     * @param idx
     * @return
     */
    private boolean isLegalBreakpoint(int idx) {
        KnuthElement elm = getElement(idx);
        if (elm.isPenalty() && elm.getP() != KnuthElement.INFINITE) {
            return true;
        } else if (idx > 0 && elm.isGlue() && getElement(idx-1).isBox()) {
            return true;
        } else {
            return false;
        }
    }
    
    public int getDifference(int line) {
        return positions[line].difference;
    }

    public double getAdjustRatio(int line) {
        return positions[line].adjustRatio;
    }

    public int getStart(int line) {
        KnuthNode previous = positions[line].previous;
        return line == 0 ? 0 : previous.position + 1; 
    }

    public int getEnd(int line) {
        return positions[line].position;
    }

    /**
     * Return a string representation of a MinOptMax in the form of a 
     * "width+stretch-shrink". Useful only for debugging.
     * @param mom
     * @return 
     */
    private static String width(MinOptMax mom) {
        return mom.opt + "+" + (mom.max - mom.opt) + "-" + (mom.opt - mom.min); 

    }

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

    protected abstract int filterActiveNodes() ;

    private void calculateBreakPoints(KnuthNode node, KnuthSequence par,
                                      int total) {
        KnuthNode bestActiveNode = node;
        // use bestActiveNode to determine the optimum breakpoints
        for (int i = node.line; i > 0; i--) {
            updateData2(bestActiveNode, par, total);
            bestActiveNode = bestActiveNode.previous;
        }
    }
}
