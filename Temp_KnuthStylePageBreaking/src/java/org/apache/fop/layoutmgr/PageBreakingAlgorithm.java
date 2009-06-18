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

import java.util.LinkedList;

import org.apache.fop.layoutmgr.AbstractBreaker.PageBreakPosition;

class PageBreakingAlgorithm extends BreakingAlgorithm {
    private LayoutManager topLevelLM;
    private LinkedList pageBreaks = null;

    public PageBreakingAlgorithm(LayoutManager topLevelLM,
                                  int alignment, int alignmentLast) {
        super(alignment, alignmentLast, true);
        this.topLevelLM = topLevelLM;
    }
    
    public LinkedList getPageBreaks() {
        return pageBreaks;
    }

    public void insertPageBreakAsFirst(PageBreakPosition pageBreak) {
        if (pageBreaks == null) {
            pageBreaks = new LinkedList();
        }
        pageBreaks.addFirst(pageBreak);
    }
    
    public void updateData1(int total, double demerits) {
    }

    public void updateData2(KnuthNode bestActiveNode,
                            KnuthSequence sequence,
                            int total) {
        //int difference = (bestActiveNode.line < total) ? bestActiveNode.difference : bestActiveNode.difference + fillerMinWidth;
        int difference = bestActiveNode.difference;
        int blockAlignment = (bestActiveNode.line < total) ? alignment : alignmentLast;
        double ratio = (blockAlignment == org.apache.fop.fo.Constants.EN_JUSTIFY
                        || bestActiveNode.adjustRatio < 0) ? bestActiveNode.adjustRatio : 0;


        // add nodes at the beginning of the list, as they are found
        // backwards, from the last one to the first one
        System.out.println("BBA> difference= " + difference + " ratio= " + ratio 
                           + " posizione= " + bestActiveNode.position);
        insertPageBreakAsFirst(new PageBreakPosition(this.topLevelLM, 
                bestActiveNode.position, ratio, difference));
    }

    protected int filterActiveNodes() {
        // leave only the active node with fewest total demerits
        KnuthNode bestActiveNode = null;
        for (int i = startLine; i < endLine; i++) {
            for (KnuthNode node = getNode(i); node != null; node = node.next) {
                bestActiveNode = compareNodes(bestActiveNode, node);
                if (node != bestActiveNode) {
                    removeNode(i, node);
                }
            }
        }
        return bestActiveNode.line;
    }

}