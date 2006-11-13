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

import java.util.ListIterator;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.layoutmgr.BreakingAlgorithm;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.inline.AlignmentContext;
import org.apache.fop.layoutmgr.inline.KnuthInlineBox;
import org.apache.fop.layoutmgr.inline.LineLayoutManager;
import org.apache.fop.layoutmgr.inline.LineLayoutManager.Paragraph;

public class LineBreakingAlgorithm extends BreakingAlgorithm {
    private LineLayoutManager thisLLM;
    private int pageAlignment;
    private int activePossibility;
    private int addedPositions;
    private int textIndent;
    private int fillerMinWidth;
    private int lineHeight;
    private int lead;
    private int follow;
//    private int maxDiff;
    private static final double MAX_DEMERITS = 10e6;

    public LineBreakingAlgorithm (int pageAlign,
                                  int textAlign, int textAlignLast,
                                  int indent, int fillerWidth,
                                  int lh, int ld, int fl, boolean first,
                                  int maxFlagCount, LineLayoutManager llm) {
        super(textAlign, textAlignLast, first, false, maxFlagCount);
        pageAlignment = pageAlign;
        textIndent = indent;
        fillerMinWidth = fillerWidth;
        lineHeight = lh;
        lead = ld;
        follow = fl;
        thisLLM = llm;
        activePossibility = -1;
//        maxDiff = fobj.getWidows() >= fobj.getOrphans() 
//                ? fobj.getWidows()
//                : fobj.getOrphans();
    }

    public void updateData1(int lineCount, double demerits) {
        thisLLM.getLineLayouts().addPossibility(lineCount, demerits);
        log.trace("Layout possibility in " + lineCount + " lines; break at position:");
    }

    public void updateData2(KnuthNode bestActiveNode,
                            KnuthSequence par,
                            int total) {
        // compute indent and adjustment ratio, according to
        // the value of text-align and text-align-last
        int indent = 0;
        int difference = bestActiveNode.difference;
        int textAlign = (bestActiveNode.line < total) ? alignment : alignmentLast;
        indent += (textAlign == Constants.EN_CENTER)
                  ? difference / 2 : (textAlign == Constants.EN_END) ? difference : 0;
        indent += (bestActiveNode.line == 1 && bFirst && thisLLM.isFirstInBlock()) ? textIndent : 0;
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

        if (addedPositions == thisLLM.getLineLayouts().getLineCount(activePossibility)) {
            activePossibility++;
            addedPositions = 0;
        }

        if (difference + bestActiveNode.availableShrink < 0) {
            if (log.isWarnEnabled()) {
                log.warn(FONode.decorateWithContextInfo(
                        "Line " + (addedPositions + 1) 
                        + " of a paragraph overflows the available area.", thisLLM.getFObj()));
            }
        }
        
        //log.debug("LLM> (" + (thisLLM.getLineLayouts().getLineNumber(activePossibility) - addedPositions) 
        //    + ") difference = " + difference + " ratio = " + ratio);
        thisLLM.getLineLayouts().addBreakPosition(makeLineBreakPosition(par,
               (bestActiveNode.line > 1 ? bestActiveNode.previous.position + 1 : 0),
               bestActiveNode.position,
               bestActiveNode.availableShrink - (addedPositions > 0 
                   ? 0 : ((Paragraph)par).getLineFiller().opt - ((Paragraph)par).getLineFiller().min), 
               bestActiveNode.availableStretch, 
               difference, ratio, indent), activePossibility);
        addedPositions++;
    }

    /* reset activePossibility, as if breakpoints have not yet been computed
     */
    public void resetAlgorithm() {
        activePossibility = -1;
    }

    private LineBreakPosition makeLineBreakPosition(KnuthSequence par,
                                                    int firstElementIndex,
                                                    int lastElementIndex,
                                                    int availableShrink, 
                                                    int availableStretch, 
                                                    int difference,
                                                    double ratio,
                                                    int indent) {
        // line height calculation - spaceBefore may differ from spaceAfter
        // by 1mpt due to rounding
        int spaceBefore = (lineHeight - lead - follow) / 2;
        int spaceAfter = lineHeight - lead - follow - spaceBefore;
        // height before the main baseline
        int lineLead = lead;
        // maximum follow 
        int lineFollow = follow;
        // true if this line contains only zero-height, auxiliary boxes
        // and the actual line width is 0; in this case, the line "collapses"
        // i.e. the line area will have bpd = 0
        boolean bZeroHeightLine = (difference == thisLLM.getLineWidth());

        // if line-stacking-strategy is "font-height", the line height
        // is not affected by its content
        if (((Block) thisLLM.getFObj()).getLineStackingStrategy() != Constants.EN_FONT_HEIGHT) {
            ListIterator inlineIterator
                = par.listIterator(firstElementIndex);
            AlignmentContext lastAC = null;
            int maxIgnoredHeight = 0; // See spec 7.13
            for (int j = firstElementIndex;
                 j <= lastElementIndex;
                 j++) {
                KnuthElement element = (KnuthElement) inlineIterator.next();
                if (element instanceof KnuthInlineBox ) {
                    AlignmentContext ac = ((KnuthInlineBox) element).getAlignmentContext();
                    if (ac != null && lastAC != ac) {
                        if (!ac.usesInitialBaselineTable()
                            || ac.getAlignmentBaselineIdentifier() != Constants.EN_BEFORE_EDGE
                               && ac.getAlignmentBaselineIdentifier() != Constants.EN_AFTER_EDGE) {
                            int alignmentOffset = ac.getTotalAlignmentBaselineOffset();
                            if (alignmentOffset + ac.getAltitude() > lineLead) {
                                lineLead = alignmentOffset + ac.getAltitude();
                            }
                            if (ac.getDepth() - alignmentOffset > lineFollow)  {
                                lineFollow = ac.getDepth() - alignmentOffset;
                            }
                        } else {
                            if (ac.getHeight() > maxIgnoredHeight) {
                                maxIgnoredHeight = ac.getHeight();
                            }
                        }
                        lastAC = ac;
                    }
                    if (bZeroHeightLine
                        && (!element.isAuxiliary() || ac != null && ac.getHeight() > 0)) {
                        bZeroHeightLine = false;
                    }
                }
            }

            if (lineFollow < maxIgnoredHeight - lineLead) {
                lineFollow = maxIgnoredHeight - lineLead;
            }
        }

        thisLLM.setConstantLineHeight(lineLead + lineFollow);

        if (bZeroHeightLine) {
            return new LineBreakPosition(thisLLM,
                                         thisLLM.getKnuthParagraphs().indexOf(par),
                                         firstElementIndex, lastElementIndex,
                                         availableShrink, availableStretch,
                                         difference, ratio, 0, indent,
                                         0, thisLLM.getLineWidth(), 0, 0, 0);
        } else {
            return new LineBreakPosition(thisLLM,
                                         thisLLM.getKnuthParagraphs().indexOf(par),
                                         firstElementIndex, lastElementIndex,
                                         availableShrink, availableStretch,
                                         difference, ratio, 0, indent,
                                         lineLead + lineFollow, 
                                         thisLLM.getLineWidth(), spaceBefore, spaceAfter,
                                         lineLead);
        }
    }

    public int findBreakingPoints(Paragraph par, /*int lineWidth,*/
                                  double threshold, boolean force,
                                  int allowedBreaks) {
        return super.findBreakingPoints(par, /*lineWidth,*/ 
                threshold, force, allowedBreaks);
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
                        deactivateNode(node);
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
                        deactivateNode(node);
                    }
                }
            }
        }
        return bestActiveNode.line;
    }
}