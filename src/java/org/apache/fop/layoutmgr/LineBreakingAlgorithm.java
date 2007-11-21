/*
 * Copyright 2007 The Apache Software Foundation.
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

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.layoutmgr.inline.KnuthParagraph;
import org.apache.fop.layoutmgr.inline.LineLayoutManager;
import org.apache.fop.layoutmgr.inline.LineLayoutPossibilities;
import org.apache.fop.traits.MinOptMax;


/**
 * 
 */
public class LineBreakingAlgorithm extends BreakingAlgorithm {
    private LineLayoutManager thisLLM;
    private KnuthParagraph par;
    private int pageAlignment;
    private int activePossibility;
    private int addedPositions;
    private int textIndent;
    private int lineWidth;
    private int maxDiff;
    private LineLayoutPossibilities lineLayouts;
    private static final double MAX_DEMERITS = 10e6;

    public LineBreakingAlgorithm(int pageAlign, int lineWidth,
                                 KnuthParagraph par) {
        super(par.textAlignment, par.textAlignmentLast, false, par.maxFlaggedPenaltiesCount);

        this.thisLLM = par.getLineLayoutManager();
        this.pageAlignment = pageAlign;
        this.lineWidth = lineWidth;
        this.par = par;

        this.textIndent = par.textIndent;

        activePossibility = -1;
        maxDiff = ((Block) thisLLM.getFObj()).getWidows() >= ((Block) thisLLM.getFObj()).getOrphans() 
                ? ((Block) thisLLM.getFObj()).getWidows()
                : ((Block) thisLLM.getFObj()).getOrphans();
    }

    public void updateData1(int lineCount, double demerits) {
        lineLayouts.addPossibility(lineCount, demerits);
        if (super.log.isTraceEnabled()) {
            super.log.trace(
                    "Layout possibility in " + lineCount + " lines; break at position:");
        }
    }

    public void updateData2(KnuthNode bestActiveNode,
                            KnuthSequence seq,
                            int total) {
        KnuthParagraph par = (KnuthParagraph) seq;
        // compute indent and adjustment ratio, according to
        // the value of text-align and text-align-last
        int indent = 0;
        int difference = bestActiveNode.difference;
        int textAlign = (bestActiveNode.line < total) ? alignment : alignmentLast;
        indent += (textAlign == Constants.EN_CENTER)
                  ? difference / 2 : (textAlign == Constants.EN_END) ? difference : 0;
        indent += (bestActiveNode.line == 1) ? textIndent : 0;
        double ratio = (textAlign == Constants.EN_JUSTIFY
            || difference < 0 && -difference <= bestActiveNode.availableShrink)
                    ? bestActiveNode.adjustRatio : 0;

        // add nodes at the beginning of the list, as they are found
        // backwards, from the last one to the first one

        // the first time this method is called, initialize activePossibility
        if (activePossibility == -1) {
            activePossibility = 0;
            addedPositions = 0;
        }

        if (addedPositions == lineLayouts.getLineCount(activePossibility)) {
            activePossibility++;
            addedPositions = 0;
        }

        if (log.isWarnEnabled()) {
            int lack = difference + bestActiveNode.availableShrink; 
            if (lack < 0) {
                String textDiff = (lack < -50000 ? "more than 50 points" : (-lack) + "mpt");
                log.warn(FONode.decorateWithContextInfo
                         ("Line " + (addedPositions + 1) 
                          + " of a paragraph overflows the available area by "
                          + textDiff + ".", thisLLM.getFObj()));
            }
        }
        
        //log.debug("LLM> (" + (lineLayouts.getLineNumber(activePossibility) - addedPositions) 
        //    + ") difference = " + difference + " ratio = " + ratio);
        MinOptMax lineFiller = par.getLineFiller();
        lineLayouts.addBreakPosition(thisLLM.makeLineBreakPosition(par,
               (bestActiveNode.line > 1 ? bestActiveNode.previous.position + 1 : 0),
               bestActiveNode.position,
               bestActiveNode.availableShrink - (addedPositions > 0 
                   ? 0 : lineFiller.opt - lineFiller.min), 
               bestActiveNode.availableStretch, 
               difference, ratio, indent), activePossibility);
        addedPositions++;
    }

    /* reset activePossibility, as if breakpoints have not yet been computed
     */
    public void resetAlgorithm() {
        activePossibility = -1;
    }

    protected int filterActiveNodes() {
        KnuthNode bestActiveNode = null;

        if (pageAlignment == Constants.EN_JUSTIFY) {
            // leave all active nodes and find the optimum line number
            //log.debug("LBA.filterActiveNodes> " + activeNodeCount + " layouts");
            for (int i = startLine; i < endLine; i++) {
                for (KnuthNode node = getNode(i); node != null; node = node.next) {
                    //log.debug("                       + lines = " + node.line + " demerits = " + node.totalDemerits);
                    bestActiveNode = compareNodes(bestActiveNode, node);
                }
            }

            // scan the node set once again and remove some nodes
            //log.debug("LBA.filterActiveList> layout selection");
            for (int i = startLine; i < endLine; i++) {
                for (KnuthNode node = getNode(i); node != null; node = node.next) {
                    //if (Math.abs(node.line - bestActiveNode.line) > maxDiff) {
                    //if (false) {
                    if (node.line != bestActiveNode.line
                        && node.totalDemerits > MAX_DEMERITS) {
                        //log.debug("                     XXX lines = " + node.line + " demerits = " + node.totalDemerits);
                        removeNode(i, node);
                    } else {
                        //log.debug("                      ok lines = " + node.line + " demerits = " + node.totalDemerits);
                    }
                }
            }
        } else {
            // leave only the active node with fewest total demerits
            for (int i = startLine; i < endLine; i++) {
                for (KnuthNode node = getNode(i); node != null; node = node.next) {
                    bestActiveNode = compareNodes(bestActiveNode, node);
                    if (node != bestActiveNode) {
                        removeNode(i, node);
                    }
                }
            }
        }
        return bestActiveNode.line;
    }

    /**
     * Fint the optimal linebreaks for a paragraph
     * @param alignment alignment of the paragraph
     * @param currPar the Paragraph for which the linebreaks are found
     * @return the line layout possibilities for the paragraph
     */
    public LineLayoutPossibilities findOptimalBreakingPoints() {
        // use the member lineLayouts, which is read by LineBreakingAlgorithm.updateData1 and 2
        lineLayouts = new LineLayoutPossibilities();
        double maxAdjustment = 1;
        int iBPcount = 0;
        if (thisLLM.hyphenationProperties.hyphenate.getEnum() == Constants.EN_TRUE 
                && ((Block) thisLLM.getFObj()).getWrapOption() != Constants.EN_NO_WRAP) {
            thisLLM.findHyphenationPoints(par);
        }
   
        // first try
        int allowedBreaks;
        if (((Block) thisLLM.getFObj()).getWrapOption() == Constants.EN_NO_WRAP) {
            allowedBreaks = BreakingAlgorithm.ONLY_FORCED_BREAKS;
        } else {
            allowedBreaks = BreakingAlgorithm.NO_FLAGGED_PENALTIES;
        }
        setConstantLineWidth(lineWidth);
        iBPcount = findBreakingPoints(par, maxAdjustment, false, allowedBreaks);
        if (iBPcount == 0 || alignment == Constants.EN_JUSTIFY) {
            // if the first try found a set of breaking points, save them
            if (iBPcount > 0) {
                resetAlgorithm();
                lineLayouts.savePossibilities(false);
            } else {
                // the first try failed
                log.debug("No set of breaking points found with maxAdjustment = " + maxAdjustment);
            }
   
            // now try something different
            log.debug("Hyphenation possible? "
                      + (thisLLM.hyphenationProperties.hyphenate.getEnum() == Constants.EN_TRUE));
            if (thisLLM.hyphenationProperties.hyphenate.getEnum() == Constants.EN_TRUE
                && !(allowedBreaks == BreakingAlgorithm.ONLY_FORCED_BREAKS)) {
                // consider every hyphenation point as a legal break
                allowedBreaks = BreakingAlgorithm.ALL_BREAKS;
            } else {
                // try with a higher threshold
                maxAdjustment = 5;
            }
   
            if ((iBPcount
                 = findBreakingPoints(par, maxAdjustment, false, allowedBreaks)) == 0) {
                // the second try failed too, try with a huge threshold
                // and force the algorithm to find
                // a set of breaking points
                log.debug("No set of breaking points found with maxAdjustment = "
                          + maxAdjustment
                          + (thisLLM.hyphenationProperties.hyphenate.getEnum() == Constants.EN_TRUE
                                  ? " and hyphenation" : ""));
                maxAdjustment = 20;
                iBPcount
                    = findBreakingPoints(par, maxAdjustment, true, allowedBreaks);
            }
   
            // use non-hyphenated breaks, when possible
            lineLayouts.restorePossibilities();
   
            /* extension (not in the XSL FO recommendation): if vertical alignment
               is justify and the paragraph has only one layout, try using 
               shorter or longer lines */
            //TODO This code snippet is disabled. Reenable?
            if (false && alignment == Constants.EN_JUSTIFY && alignment == Constants.EN_JUSTIFY) {
                //log.debug("LLM.getNextKnuthElements> layouts with more lines? " + lineLayouts.canUseMoreLines());
                //log.debug("                          layouts with fewer lines? " + lineLayouts.canUseLessLines());
                if (!lineLayouts.canUseMoreLines()) {
                    resetAlgorithm();
                    lineLayouts.savePossibilities(true);
                    // try with shorter lines
                    int savedLineWidth = lineWidth;
                    lineWidth = (int) (lineWidth * 0.95);
                    iBPcount = findBreakingPoints(par, maxAdjustment, true, allowedBreaks);
                    // use normal lines, when possible
                    lineLayouts.restorePossibilities();
                    lineWidth = savedLineWidth;
                }
                if (!lineLayouts.canUseLessLines()) {
                    resetAlgorithm();
                    lineLayouts.savePossibilities(true);
                    // try with longer lines
                    int savedLineWidth = lineWidth;
                    lineWidth = (int) (lineWidth * 1.05);
                    setConstantLineWidth(lineWidth);
                    iBPcount = findBreakingPoints(par, maxAdjustment, true, allowedBreaks);
                    // use normal lines, when possible
                    lineLayouts.restorePossibilities();
                    lineWidth = savedLineWidth;
                }
                //log.debug("LLM.getNextKnuthElements> now, layouts with more lines? " + lineLayouts.canUseMoreLines());
                //log.debug("                          now, layouts with fewer lines? " + lineLayouts.canUseLessLines());
            }
        }
        return lineLayouts;
    }

}
