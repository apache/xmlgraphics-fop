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

import java.util.LinkedList;
import java.util.List;

import org.apache.fop.fo.FObj;
import org.apache.fop.layoutmgr.PageBreakingAlgorithm.PageBreakingLayoutListener;
import org.apache.fop.layoutmgr.inline.TextLayoutManager;

public abstract class LocalBreaker extends AbstractBreaker {
    protected BlockStackingLayoutManager lm;
    private int displayAlign;
    private int ipd;
    private int overflow;
    private boolean repeatedHeader;
    private boolean isDescendantOfTableFooter;
    private boolean repeatedFooter;

    public void setRepeatedFooter(boolean repeatedFooter) {
        this.repeatedFooter = repeatedFooter;
    }

    public void setDescendantOfTableFooter(boolean isDescendantOfTableFooter) {
        this.isDescendantOfTableFooter = isDescendantOfTableFooter;
    }

    public LocalBreaker(BlockStackingLayoutManager lm, int ipd, int displayAlign) {
        this.lm = lm;
        this.ipd = ipd;
        this.displayAlign = displayAlign;
    }

    public void setRepeatedHeader(boolean repeatedHeader) {
        this.repeatedHeader = repeatedHeader;
    }

    /** {@inheritDoc} */
    protected boolean isPartOverflowRecoveryActivated() {
        // For side regions, this must be disabled because of wanted overflow.
        return false;
    }

    public boolean isOverflow() {
        return (this.overflow != 0);
    }

    public int getOverflowAmount() {
        return this.overflow;
    }

    /** {@inheritDoc} */
    protected PageBreakingLayoutListener createLayoutListener() {
        return new PageBreakingLayoutListener() {

            public void notifyOverflow(int part, int amount, FObj obj) {
                if (LocalBreaker.this.overflow == 0) {
                    LocalBreaker.this.overflow = amount;
                }
            }

        };
    }

    protected LayoutManager getTopLevelLM() {
        return lm;
    }

    protected LayoutContext createLayoutContext() {
        LayoutContext lc = super.createLayoutContext();
        lc.setRefIPD(ipd);
        return lc;
    }

    protected List getNextKnuthElements(LayoutContext context, int alignment) {
        LayoutManager curLM; // currently active LM
        List returnList = new LinkedList();

        while ((curLM = lm.getChildLM()) != null) {
            LayoutContext childLC = LayoutContext.newInstance();
            childLC.setStackLimitBP(context.getStackLimitBP());
            childLC.setRefIPD(context.getRefIPD());
            childLC.setWritingMode(context.getWritingMode());

            List returnedList = null;
            // The following is a HACK! Ignore leading and trailing white space
            boolean ignore = curLM instanceof TextLayoutManager;
            if (!curLM.isFinished()) {
                returnedList = curLM.getNextKnuthElements(childLC, alignment);
            }
            if (returnedList != null && !ignore) {
                lm.wrapPositionElements(returnedList, returnList);
            }
        }
        SpaceResolver.resolveElementList(returnList);
        lm.setFinished(true);
        return returnList;
    }

    protected int getCurrentDisplayAlign() {
        return displayAlign;
    }

    protected boolean hasMoreContent() {
        return !lm.isFinished();
    }

    protected void addAreas(PositionIterator posIter, LayoutContext context) {
        if (isDescendantOfTableFooter) {
            if (repeatedHeader) {
                context.setTreatAsArtifact(true);
            }
        } else {
            if (repeatedFooter) {
                context.setTreatAsArtifact(true);
            }
        }
        AreaAdditionUtil.addAreas(lm, posIter, context);
    }

    protected void doPhase3(PageBreakingAlgorithm alg, int partCount, BlockSequence originalList,
            BlockSequence effectiveList) {
        if (partCount > 1) {
            PageBreakPosition pos = alg.getPageBreaks().getFirst();
            int firstPartLength = ElementListUtils.calcContentLength(effectiveList,
                    effectiveList.ignoreAtStart, pos.getLeafPos());
            overflow += alg.totalWidth - firstPartLength;
        }
        // Rendering all parts (not just the first) at once for the case where the parts that
        // overflow should be visible.
        alg.removeAllPageBreaks();
        // Directly add areas after finding the breaks
        this.addAreas(alg, 1, originalList, effectiveList);
    }

    protected void finishPart(PageBreakingAlgorithm alg, PageBreakPosition pbp) {
        // nop for static content
    }

    protected LayoutManager getCurrentChildLM() {
        return null; // TODO NYI
    }
}
