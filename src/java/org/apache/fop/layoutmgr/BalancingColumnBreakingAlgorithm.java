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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.fop.traits.MinOptMax;

/**
 * This is a the breaking algorithm that is responsible for balancing columns in multi-column
 * layout.
 */
public class BalancingColumnBreakingAlgorithm extends PageBreakingAlgorithm {

    private int columnCount;
    private List<Integer> idealBreaks;

    public BalancingColumnBreakingAlgorithm(LayoutManager topLevelLM,
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
        double demerits = Double.MAX_VALUE;
        if (idealBreaks == null) {
            idealBreaks = calculateIdealBreaks(activeNode.position);
        }
        LinkedList<Integer> curPossibility = getPossibilityTrail(activeNode);
        boolean notIdeal = false;
        int idealDemerit = columnCount + 1 - curPossibility.size();
        if (curPossibility.size() > idealBreaks.size()) {
            return demerits;
        }
        for (int breakPos = 0; breakPos < curPossibility.size(); breakPos++) {
            if (curPossibility.get(breakPos) != 0 && curPossibility.get(breakPos)
                    != idealBreaks.get(breakPos)) {
                notIdeal = true;
                break;
            }
        }
        if (!notIdeal) {
            demerits = idealDemerit;
        }
        return demerits;
    }

    private List<Integer> calculateIdealBreaks(int startPos) {
        List<ColumnContent> previousPreviousBreaks = null;
        List<ColumnContent> previousBreaks = null;
        List<ColumnContent> breaks = new ArrayList<ColumnContent>();
        breaks.add(new ColumnContent(startPos, par.size() - 1));
        do {
            previousPreviousBreaks = previousBreaks;
            previousBreaks = breaks;
            breaks = getInitialBreaks(startPos, getAverageColumnLength(breaks));
        } while (!breaks.equals(previousBreaks) && !breaks.equals(previousPreviousBreaks));
        breaks = sortElementsForBreaks(breaks);
        return getElementIdBreaks(breaks, startPos);
    }

    private static final class ColumnContent {

        public final int startIndex;

        public final int endIndex;

        ColumnContent(int startIndex, int endIndex) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        @Override
        public int hashCode() {
            return startIndex << 16 | endIndex;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ColumnContent)) {
                return false;
            } else {
                ColumnContent other = (ColumnContent) obj;
                return other.startIndex == startIndex && other.endIndex == endIndex;
            }
        }

        @Override
        public String toString() {
            return startIndex + "-" + endIndex;
        }

    }

    private int getAverageColumnLength(List<ColumnContent> columns) {
        int totalLength = 0;
        for (ColumnContent col : columns) {
            totalLength += calcContentLength(par, col.startIndex, col.endIndex);
        }
        return totalLength / columnCount;
    }

    private List<ColumnContent> getInitialBreaks(int startIndex, int averageColLength) {
        List<ColumnContent> initialColumns = new ArrayList<ColumnContent>();
        int colStartIndex = startIndex;
        int totalLength = 0;
        int idealBreakLength = averageColLength;
        int previousBreakLength = 0;
        int prevBreakIndex = startIndex;
        boolean prevIsBox = false;
        int colNumber = 1;
        for (int i = startIndex; i < par.size(); i++) {
            KnuthElement element = (KnuthElement) par.get(i);
            if (isLegalBreak(i, prevIsBox)) {
                int breakLength = totalLength
                        + (element instanceof KnuthPenalty ? element.getWidth() : 0);
                if (breakLength > idealBreakLength && colNumber < columnCount) {
                    int breakIndex;
                    if (breakLength - idealBreakLength > idealBreakLength - previousBreakLength) {
                        breakIndex = prevBreakIndex;
                        totalLength = previousBreakLength;
                    } else {
                        breakIndex = element instanceof KnuthPenalty ? i : i - 1;
                        totalLength = breakLength;
                    }
                    initialColumns.add(new ColumnContent(colStartIndex, breakIndex));
                    i = getNextStartIndex(breakIndex);
                    colStartIndex = i--;
                    colNumber++;
                    idealBreakLength += averageColLength;
                } else {
                    previousBreakLength = breakLength;
                    prevBreakIndex = element instanceof KnuthPenalty ? i : i - 1;
                    prevIsBox = false;
                }
            } else {
                totalLength += element instanceof KnuthPenalty ? 0 : element.getWidth();
                prevIsBox = element instanceof KnuthBox;
            }
        }
        assert initialColumns.size() == columnCount - 1;
        initialColumns.add(new ColumnContent(colStartIndex, par.size() - 1));
        return initialColumns;
    }

    private int getNextStartIndex(int breakIndex) {
        int startIndex = breakIndex;
        @SuppressWarnings("unchecked")
        Iterator<KnuthElement> iter = par.listIterator(breakIndex);
        while (iter.hasNext() && !(iter.next() instanceof KnuthBox)) {
            startIndex++;
        }
        return startIndex;
    }

    private List<ColumnContent> sortElementsForBreaks(List<ColumnContent> breaks) {
        boolean changes;
        /* Relax factor to make balancing more visually appealing as in some cases
         * strict balancing would lead to ragged column endings. */
        int fFactor = 4000;
        do {
            changes = false;
            ColumnContent curColumn = breaks.get(breaks.size() - 1);
            int curColLength = calcContentLength(par, curColumn.startIndex, curColumn.endIndex);
            for (int colIndex = (breaks.size() - 1); colIndex > 0; colIndex--) {
                ColumnContent prevColumn = breaks.get(colIndex - 1);
                int prevColLength = calcContentLength(par, prevColumn.startIndex, prevColumn.endIndex);
                if (prevColLength < curColLength) {
                    int newBreakIndex = curColumn.startIndex;
                    boolean prevIsBox = true;
                    while (newBreakIndex <= curColumn.endIndex && !(isLegalBreak(newBreakIndex, prevIsBox))) {
                        newBreakIndex++;
                        prevIsBox = par.get(newBreakIndex) instanceof KnuthBox;
                    }
                    if (newBreakIndex < curColumn.endIndex) {
                        if (prevIsBox) {
                            newBreakIndex--;
                        }
                        int newStartIndex = getNextStartIndex(newBreakIndex);
                        int newPrevColLength = calcContentLength(par, prevColumn.startIndex, newBreakIndex);
                        if (newPrevColLength <= fFactor + curColLength) {
                            prevColumn = new ColumnContent(prevColumn.startIndex, newBreakIndex);
                            breaks.set(colIndex - 1, prevColumn);
                            breaks.set(colIndex, new ColumnContent(newStartIndex, curColumn.endIndex));
                            prevColLength = calcContentLength(par, prevColumn.startIndex, newBreakIndex);
                            changes = true;
                        }
                    }
                }
                curColLength = prevColLength;
                curColumn = prevColumn;
            }
        } while (changes);
        return breaks;
    }

    private boolean isLegalBreak(int index, boolean prevIsBox) {
        KnuthElement element = (KnuthElement) par.get(index);
        return element instanceof KnuthPenalty && element.getPenalty() < KnuthPenalty.INFINITE
                || prevIsBox && element instanceof KnuthGlue;
    }

    private int calcContentLength(KnuthSequence par, int startIndex, int endIndex) {
        return ElementListUtils.calcContentLength(par, startIndex, endIndex) + getPenaltyWidth(endIndex);
    }

    private int getPenaltyWidth(int index) {
        KnuthElement element = (KnuthElement) par.get(index);
        return element instanceof KnuthPenalty ? element.getWidth() : 0;
    }

    private List<Integer> getElementIdBreaks(List<ColumnContent> breaks, int startPos) {
        List<Integer> elementIdBreaks = new ArrayList<Integer>();
        elementIdBreaks.add(startPos);
        for (ColumnContent column : breaks) {
            if (breaks.get(breaks.size() - 1).equals(column)) {
                continue;
            }
            elementIdBreaks.add(column.endIndex);
        }
        return elementIdBreaks;
    }

    private LinkedList<Integer> getPossibilityTrail(KnuthNode activeNode) {
        LinkedList<Integer> trail = new LinkedList<Integer>();
        KnuthNode previous = activeNode;
        do {
            trail.addFirst(previous.position);
            previous = previous.previous;
        } while (previous != null);
        return trail;
    }
}
