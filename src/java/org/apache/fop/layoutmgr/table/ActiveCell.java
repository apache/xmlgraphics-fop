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

package org.apache.fop.layoutmgr.table;

import java.util.List;
import java.util.ListIterator;

import org.apache.fop.fo.flow.table.ConditionalBorder;
import org.apache.fop.fo.flow.table.EffRow;
import org.apache.fop.fo.flow.table.GridUnit;
import org.apache.fop.fo.flow.table.PrimaryGridUnit;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPenalty;

/**
 * A cell playing in the construction of steps for a row-group.
 */
class ActiveCell {
    private PrimaryGridUnit pgu;
    /** Knuth elements for this active cell. */
    private List elementList;
    /** Iterator over the Knuth element list. */
    private ListIterator knuthIter;
    /** Number of the row where the row-span ends, zero-based. */
    private int endRowIndex;
    /** Length of the Knuth elements not yet included in the steps. */
    private int remainingLength;
    /** Heights of the rows (in the row-group) preceding the one where this cell starts. */
    private int previousRowsLength;
    /** Total length of this cell's content plus the lengths of the previous rows. */
    private int totalLength;
    /** Length of the Knuth elements already included in the steps. */
    private int includedLength;

    private int borderBeforeNormal;
    private int borderBeforeLeading;
    private int borderAfterNormal;
    private int borderAfterTrailing;
    private int paddingBeforeNormal;
    private int paddingBeforeLeading;
    private int paddingAfterNormal;
    private int paddingAfterTrailing;

    private boolean keepWithNextSignal;

    private int spanIndex = 0;
    private CellPart lastCellPart;

    private Step previousStep;
    private Step nextStep;

    /**
     * Auxiliary class to store all the informations related to a breaking step.
     */
    private static class Step {
        /** Index, in the list of Knuth elements, of the element starting this step. */
        private int start;
        /** Index, in the list of Knuth elements, of the element ending this step. */
        private int end;
        /** Length of the Knuth elements up to this step. */
        private int contentLength;
        /** Total length up to this step, including paddings and borders. */
        private int totalLength;
        /** Length of the penalty ending this step, if any. */
        private int penaltyLength;
        /**
         * Length of the optional content for the next step. That is, content that will
         * not appear if the next step starts a new page.
         */
        private int nextCondBeforeContentLength;

        Step(int contentLength) {
            this.contentLength = contentLength;
            // TODO necessary if a cell part must be created while this cell hasn't
            // contributed any content yet. To be removed along with the 900-penalty
            // mechanism
            this.end = -1;
        }

        Step(Step other) {
            set(other);
        }

        void set(Step other) {
            this.start         = other.start;
            this.end           = other.end;
            this.contentLength = other.contentLength;
            this.totalLength   = other.totalLength;
            this.penaltyLength = other.penaltyLength;
            this.nextCondBeforeContentLength = other.nextCondBeforeContentLength;
        }
    }

    ActiveCell(PrimaryGridUnit pgu, EffRow row, int rowIndex, int previousRowsLength,
            TableLayoutManager tableLM) {
        this.pgu = pgu;
        CommonBorderPaddingBackground bordersPaddings = pgu.getCell()
                .getCommonBorderPaddingBackground();
        borderBeforeNormal = pgu.getBeforeBorderWidth(0, ConditionalBorder.NORMAL);
        borderBeforeLeading = pgu.getBeforeBorderWidth(0, ConditionalBorder.REST);
        borderAfterNormal = pgu.getAfterBorderWidth(ConditionalBorder.NORMAL);
        borderAfterTrailing = pgu.getAfterBorderWidth(0, ConditionalBorder.REST);
        TableCellLayoutManager cellLM = pgu.getCellLM();
        paddingBeforeNormal = bordersPaddings.getPaddingBefore(false, cellLM);
        paddingBeforeLeading = bordersPaddings.getPaddingBefore(true, cellLM);
        paddingAfterNormal = bordersPaddings.getPaddingAfter(false, cellLM);
        paddingAfterTrailing = bordersPaddings.getPaddingAfter(true, cellLM);

        boolean makeBoxForWholeRow = false;
        if (row.getExplicitHeight().min > 0) {
            boolean contentsSmaller = ElementListUtils.removeLegalBreaks(
                    pgu.getElements(), row.getExplicitHeight());
            if (contentsSmaller) {
                makeBoxForWholeRow = true;
            }
        }
        if (pgu.isLastGridUnitRowSpan() && pgu.getRow() != null) {
            makeBoxForWholeRow |= pgu.getRow().mustKeepTogether();
            makeBoxForWholeRow |= tableLM.getTable().mustKeepTogether();
        }
        if (makeBoxForWholeRow) {
            elementList = new java.util.ArrayList(1);
            int height = row.getHeight().opt;
            height -= 2 * tableLM.getHalfBorderSeparationBPD();
            height -= borderBeforeNormal + borderAfterNormal;  // TODO conditionals
            height -= paddingBeforeNormal + paddingAfterNormal;
            elementList.add(new KnuthBoxCellWithBPD(height));
        } else {
            elementList = pgu.getElements();
        }
        knuthIter = elementList.listIterator();
        includedLength = -1;  // Avoid troubles with cells having content of zero length
        this.previousRowsLength = previousRowsLength;
        totalLength = previousRowsLength + ElementListUtils.calcContentLength(elementList);
        endRowIndex = rowIndex + pgu.getCell().getNumberRowsSpanned() - 1;
        keepWithNextSignal = false;
        remainingLength = totalLength - previousRowsLength;

        nextStep = new Step(previousRowsLength);
        previousStep = new Step(nextStep);
        goToNextLegalBreak();
    }

    /**
     * Returns true if this cell ends on the given row.
     * 
     * @param rowIndex index of a row in the row-group, zero-based
     * @return true if this cell ends on the given row
     */
    boolean endsOnRow(int rowIndex) {
        return rowIndex == endRowIndex;
    }

    /**
     * Returns the length of this cell's content not yet included in the steps, plus the
     * cell's borders and paddings if applicable.
     * 
     * @param activeRowIndex index of the row currently considered
     * @return the remaining length, or zero if the cell doesn't end on the given row.
     */
    int getRemainingHeight(int activeRowIndex) {
        if (!endsOnRow(activeRowIndex)) {
            return 0;
        } else if (includedLength == totalLength) {
            return 0;
        } else {
            return borderBeforeLeading + paddingBeforeLeading + remainingLength
                    + paddingAfterNormal + borderAfterNormal;
        }
    }

    private void goToNextLegalBreak() {
        nextStep.penaltyLength = 0;
        boolean breakFound = false;
        boolean prevIsBox = false;
        while (!breakFound && knuthIter.hasNext()) {
            KnuthElement el = (KnuthElement) knuthIter.next();
            if (el.isPenalty()) {
                prevIsBox = false;
                if (el.getP() < KnuthElement.INFINITE) {
                    // First legal break point
                    nextStep.penaltyLength = el.getW();
                    breakFound = true;
                }
            } else if (el.isGlue()) {
                if (prevIsBox) {
                    // Second legal break point
                    breakFound = true;
                } else {
                    nextStep.contentLength += el.getW();
                }
                prevIsBox = false;
            } else {
                prevIsBox = true;
                nextStep.contentLength += el.getW();
            }
        }
        nextStep.end = knuthIter.nextIndex() - 1;
        if (nextStep.end == elementList.size() - 1) {
            // TODO wait that every cell on the row has finished before including border-after!!
            nextStep.totalLength = borderBeforeNormal + paddingBeforeNormal
                    + nextStep.contentLength + nextStep.penaltyLength
                    + paddingAfterNormal + borderAfterNormal;
        } else {
            nextStep.totalLength = borderBeforeNormal + paddingBeforeNormal
                    + nextStep.contentLength + nextStep.penaltyLength
                    + paddingAfterTrailing + borderAfterTrailing; 
        }
    }

    /**
     * Returns the total length up to the next legal break, not yet included in the steps.
     * 
     * @return the total length up to the next legal break (-1 signals no further step)
     */
    int getNextStep() {
        if (includedInLastStep()) {
            previousStep.set(nextStep);
            nextStep.start = nextStep.end + 1;
            if (!knuthIter.hasNext()) {
                return -1;
            } else {
                goToNextLegalBreak();
            }
        }
        return nextStep.totalLength;
    }

    private boolean includedInLastStep() {
        return includedLength == nextStep.contentLength;
    }

    /**
     * Signals the length of the chosen next step, so that this cell determines whether
     * its own step may be included or not.
     * 
     * @param minStep length of the chosen next step
     * @return
     */
    boolean signalMinStep(int minStep) {
        if (nextStep.totalLength <= minStep) {
            includedLength = nextStep.contentLength;
            computeRemainingLength();
            return false;
        } else {
            return borderBeforeNormal + paddingBeforeNormal + previousRowsLength
                    + paddingAfterTrailing + borderAfterTrailing > minStep;
        }
    }

    void endRow(int rowIndex) {
        if (endsOnRow(rowIndex)) {
            int bpAfterNormal = paddingAfterNormal + borderAfterNormal;
            int bpAfterLast = paddingAfterNormal
                    + pgu.getAfterBorderWidth(ConditionalBorder.LEADING_TRAILING);
            lastCellPart.setLast(bpAfterNormal, bpAfterLast);
        } else {
            spanIndex++;
            borderBeforeLeading = pgu.getBeforeBorderWidth(spanIndex, ConditionalBorder.REST);
            borderAfterTrailing = pgu.getAfterBorderWidth(spanIndex, ConditionalBorder.REST);
        }
    }

    /**
     * Computes the length of the cell's content after the current legal break. Discards
     * every glue or penalty following the break if needed. The cell's borders and
     * paddings are not considered here.
     */
    private void computeRemainingLength() {
        remainingLength = totalLength - nextStep.contentLength;
        nextStep.nextCondBeforeContentLength = 0;
        // Save the current location in the element list
        int oldIndex = knuthIter.nextIndex();
        KnuthElement el;
        while (knuthIter.hasNext() && !(el = (KnuthElement) knuthIter.next()).isBox()) {
            if (el.isGlue()) {
                remainingLength -= el.getW();
                nextStep.nextCondBeforeContentLength += el.getW();
            }
        }
        // Reset the iterator to the current location
        while (knuthIter.nextIndex() > oldIndex) {
            knuthIter.previous();
        }
    }

    /**
     * Returns true if some content of this cell is part of the chosen next step.
     * 
     * @return true if this cell's next step is inferior or equal to the next minimal step
     */
    boolean contributesContent() {
        // return includedInLastStep() && the cell hasn't finished yet, otherwise there's
        // nothing more to contribute
        return includedInLastStep() && nextStep.end >= nextStep.start;
    }

    /**
     * Returns true if this cell has already started to contribute some content to the steps.
     * 
     * @return true if this cell's first step is inferior or equal to the current one 
     */
    boolean hasStarted() {
        return includedLength >= 0;
    }

    /**
     * Returns true if this cell has contributed all of its content to the steps.
     * 
     * @return true if the end of this cell is reached
     */
    boolean isFinished() {
        return includedInLastStep() && (nextStep.end == elementList.size() - 1);
    }

    /**
     * Creates and returns a CellPart instance for the content of this cell which
     * is included in the next step.
     * 
     * @return a CellPart instance
     */
    CellPart createCellPart() {
        if (nextStep.end + 1 == elementList.size()) {
            if (pgu.getFlag(GridUnit.KEEP_WITH_NEXT_PENDING)) {
                keepWithNextSignal = true;
            }
            if (pgu.getRow() != null && pgu.getRow().mustKeepWithNext()) {
                keepWithNextSignal = true;
            }
        }
        int bpBeforeNormal;
        int bpBeforeFirst;
        int bpAfterNormal;
        int bpAfterLast;
        if (nextStep.start == 0) {
            bpBeforeNormal = borderBeforeNormal + paddingBeforeNormal;
            bpBeforeFirst = pgu.getBeforeBorderWidth(0, ConditionalBorder.LEADING_TRAILING)
                    + paddingBeforeNormal;
        } else {
            bpBeforeNormal = 0;
            bpBeforeFirst = borderBeforeLeading + paddingBeforeLeading;
        }
        bpAfterNormal = 0;
        bpAfterLast = paddingAfterTrailing + borderAfterTrailing;
        int length = nextStep.contentLength - previousStep.contentLength
                - previousStep.nextCondBeforeContentLength;
        if (!includedInLastStep() || nextStep.start == elementList.size()) {
            lastCellPart = new CellPart(pgu, nextStep.start, previousStep.end,
                    0, 0, previousStep.penaltyLength,
                    bpBeforeNormal, bpBeforeFirst, bpAfterNormal, bpAfterLast);
        } else if (nextStep.start == 0 && nextStep.end == 0
                && elementList.size() == 1
                && elementList.get(0) instanceof KnuthBoxCellWithBPD) {
            //Special case: Cell with fixed BPD
            lastCellPart = new CellPart(pgu, 0, pgu.getElements().size() - 1,
                    previousStep.nextCondBeforeContentLength, length, nextStep.penaltyLength,
                    bpBeforeNormal, bpBeforeFirst, bpAfterNormal, bpAfterLast);
        } else {
            lastCellPart = new CellPart(pgu, nextStep.start, nextStep.end,
                    previousStep.nextCondBeforeContentLength, length, nextStep.penaltyLength,
                    bpBeforeNormal, bpBeforeFirst, bpAfterNormal, bpAfterLast);
        }
        return lastCellPart;
    }

    boolean isLastForcedBreak() {
        return ((KnuthElement)elementList.get(nextStep.end)).isForcedBreak();
    }

    int getLastBreakClass() {
        return ((KnuthPenalty)elementList.get(nextStep.end)).getBreakClass();
    }

    boolean keepWithNextSignal() {
        return keepWithNextSignal;
    }

    /**
     * Marker class denoting table cells fitting in just one box (no legal break inside).
     */
    private static class KnuthBoxCellWithBPD extends KnuthBox {

        public KnuthBoxCellWithBPD(int w) {
            super(w, null, true);
        }
    }
}
