/*
 * Copyright 2005 The Apache Software Foundation.
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

import org.apache.fop.traits.MinOptMax;

/**
 * This is a the breaking algorithm that is responsible for balancing columns in multi-column
 * layout.
 */
public class BalancingColumnBreakingAlgorithm extends PageBreakingAlgorithm {

    private int columnCount;
    private int fullLen;
    
    public BalancingColumnBreakingAlgorithm(LayoutManager topLevelLM,
            PageSequenceLayoutManager.PageViewportProvider pvProvider,
            int alignment, int alignmentLast,
            MinOptMax fnSeparatorLength,
            boolean partOverflowRecovery,
            int columnCount) {
        super(topLevelLM, pvProvider, alignment, alignmentLast, 
                fnSeparatorLength, partOverflowRecovery);
        this.columnCount = columnCount;
    }
    
    /** @see org.apache.fop.layoutmgr.BreakingAlgorithm */
    protected double computeDemerits(KnuthNode activeNode,
            KnuthElement element, int fitnessClass, double r) {
        double dem = super.computeDemerits(activeNode, element, fitnessClass, r);
        log.trace("original demerit=" + dem + " " + totalWidth);
        int curPos = par.indexOf(element);
        if (fullLen == 0) {
            fullLen = ElementListUtils.calcContentLength(par);
        }
        int partLen = ElementListUtils.calcContentLength(par, activeNode.position, curPos - 1);
        int meanColumnLen = (fullLen / columnCount);
        double balance = (meanColumnLen - partLen) / 1000f;
        log.trace("balance=" + balance);
        double absBalance = Math.abs(balance);
        if (balance <= 0) {
            dem = absBalance * absBalance;
        } else {
            //shorter parts are less desired than longer ones
            dem = absBalance * absBalance * 2;
        }
        if (activeNode.line >= columnCount) {
            //We don't want more columns than available
            dem = Double.MAX_VALUE;
        }
        log.trace("effective dem=" + dem + " " + totalWidth);
        return dem;
    }
}
