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

import org.apache.fop.fo.Constants;
import org.apache.fop.traits.MinOptMax;

/**
 * This is a the breaking algorithm that is responsible for balancing columns in multi-column
 * layout.
 */
public class BalancingColumnBreakingAlgorithm extends PageBreakingAlgorithm {

    private static final Log LOG = LogFactory.getLog(BalancingColumnBreakingAlgorithm.class);

    private int columnCount;
    private int fullLen;
    private int idealPartLen;

    /**
     * Construct a balancing column breaking algorithm.
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
     * @param columnCount number of columns
     * @see PageBreakingAlgorithm
     */
    public BalancingColumnBreakingAlgorithm(                     // CSOK: ParameterNumber
            LayoutManager topLevelLM,
            PageProvider pageProvider,
            PageBreakingLayoutListener layoutListener,
            int alignment, int alignmentLast,
            MinOptMax footnoteSeparatorLength,
            boolean partOverflowRecovery,
            int columnCount) {
        super(topLevelLM, pageProvider, layoutListener,
                alignment, alignmentLast,
                footnoteSeparatorLength, partOverflowRecovery, false, false);
        this.columnCount = columnCount;
        this.considerTooShort = true; //This is important!
    }

    /** {@inheritDoc} */
    protected double computeDemerits(KnuthNode activeNode,
            KnuthElement element, int fitnessClass, double r) {
        double dem = super.computeDemerits(activeNode, element, fitnessClass, r);
        if (LOG.isTraceEnabled()) {
            LOG.trace("original demerit=" + dem + " " + totalWidth
                    + " line=" + activeNode.line + "/" + columnCount
                    + " pos=" + activeNode.position + "/" + (par.size() - 1));
        }
        int remParts = columnCount - activeNode.line;
        int curPos = par.indexOf(element);
        if (fullLen == 0) {
            fullLen = ElementListUtils.calcContentLength(par, activeNode.position, par.size() - 1);
            this.idealPartLen = (fullLen / columnCount);
        }
        int partLen = ElementListUtils.calcContentLength(par, activeNode.position, curPos - 1);
        int restLen = ElementListUtils.calcContentLength(par, curPos - 1, par.size() - 1);
        int avgRestLen = 0;
        if (remParts > 0) {
            avgRestLen = restLen / remParts;
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("remaining parts: " + remParts + " rest len: " + restLen
                    + " avg=" + avgRestLen);
        }
        double balance = (idealPartLen - partLen) / 1000f;
        if (LOG.isTraceEnabled()) {
            LOG.trace("balance=" + balance);
        }
        double absBalance = Math.abs(balance);
        dem = absBalance;
        //Step 1: This does the rough balancing
        if (columnCount > 2) {
            if (balance > 0) {
                //shorter parts are less desired than longer ones
                dem = dem * 1.2f;
            }
        } else {
            if (balance < 0) {
                //shorter parts are less desired than longer ones
                dem = dem * 1.2f;
            }
        }
        //Step 2: This helps keep the trailing parts shorter than the previous ones
        dem += (avgRestLen) / 1000f;

        if (activeNode.line >= columnCount) {
            //We don't want more columns than available
            dem = Double.MAX_VALUE;
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("effective dem=" + dem + " " + totalWidth);
        }
        return dem;
    }
}
