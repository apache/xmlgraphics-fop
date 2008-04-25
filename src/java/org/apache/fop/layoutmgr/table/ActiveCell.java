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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.flow.table.ConditionalBorder;
import org.apache.fop.fo.flow.table.EffRow;
import org.apache.fop.fo.flow.table.PrimaryGridUnit;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.layoutmgr.BlockLevelLayoutManager;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.MinOptMaxUtil;
import org.apache.fop.traits.MinOptMax;

/**
 * A cell playing in the construction of steps for a row-group.
 */
class ActiveCell {

    private static Log log = LogFactory.getLog(ActiveCell.class);

    private PrimaryGridUnit pgu;
    /** Knuth elements for this active cell. */
    private List elementList;
    /** Iterator over the Knuth element list. */
    private ListIterator knuthIter;
    /** Number of the row where the row-span ends, zero-based. */
    private int endRowIndex;
    /** Length of the Knuth elements not yet included in the steps. */
    private int remainingLength;
    /** Total length of this cell's content plus the lengths of the previous rows. */
    private int totalLength;
    /** Length of the Knuth elements already included in the steps. */
    private int includedLength;

    private int paddingBeforeNormal;
    private int paddingBeforeLeading;
    private int paddingAfterNormal;
    private int paddingAfterTrailing;

    private int bpBeforeNormal;
    private int bpBeforeLeading;
    private int bpAfterNormal;
    private int bpAfterTrailing;

    /** True if the next CellPart that will be created will be the last one for this cell. */
    private boolean lastCellPart;

    private int keepWithNextStrength;

    private int spanIndex = 0;

    private Step previousStep;
    private Step nextStep;
    /**
     * The step following nextStep. Computing it early allows to calculate
     * {@link Step#condBeforeContentLength}, thus to easily determine the remaining
     * length. That also helps for {@link #increaseCurrentStep(int)}.
     */
    private Step afterNextStep;

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
         * One of {@link Constants#EN_AUTO}, {@link Constants#EN_COLUMN},
         * {@link Constants#EN_PAGE}, {@link Constants#EN_EVEN_PAGE},
         * {@link Constants#EN_ODD_PAGE}. Set to auto if the break isn't at a penalty
         * element.
         */
        private int breakClass;
        /**
         * Length of the optional content at the beginning of the step. That is, content
         * that will not appear if this step starts a new page.
         */
        private int condBeforeContentLength;

        Step(int contentLength) {
            this.contentLength = contentLength;
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
            this.condBeforeContentLength = other.condBeforeContentLength;
            this.breakClass    = other.breakClass;
        }

        /** {@inheritDoc} */
        public String toString() {
            return "Step: start=" + start + " end=" + end + " length=" + totalLength;
        }
    }

    // TODO to be removed along with the RowPainter#computeContentLength method
    /** See {@link ActiveCell#handleExplicitHeight(MinOptMax, MinOptMax)}. */
    private static class FillerPenalty extends KnuthPenalty {

        private int contentLength;

        FillerPenalty(KnuthPenalty p, int length) {
            super(length, p.getP(), p.isFlagged(), p.getBreakClass(),
                    p.getPosition(), p.isAuxiliary());
            contentLength = p.getW();
        }

        FillerPenalty(int length) {
            super(length, 0, false, null, true);
            contentLength = 0;
        }
    }

    /** See {@link ActiveCell#handleExplicitHeight(MinOptMax, MinOptMax)}. */
    private static class FillerBox extends KnuthBox {
        FillerBox(int length) {
            super(length, null, true);
        }
    }

    /**
     * Returns the actual length of the content represented by the given element. In the
     * case where this element is used as a filler to match a row's fixed height, the
     * value returned by the getW() method will be higher than the actual content.
     * 
     * @param el an element
     * @return the actual content length corresponding to the element
     */
    static int getElementContentLength(KnuthElement el) {
        if (el instanceof FillerPenalty) {
            return ((FillerPenalty) el).contentLength;
        } else if (el instanceof FillerBox) {
            return 0;
        } else {
            return el.getW();
        }
    }

    ActiveCell(PrimaryGridUnit pgu, EffRow row, int rowIndex, int previousRowsLength,
            TableLayoutManager tableLM) {
        this.pgu = pgu;
        CommonBorderPaddingBackground bordersPaddings = pgu.getCell()
                .getCommonBorderPaddingBackground();
        TableCellLayoutManager cellLM = pgu.getCellLM();
        paddingBeforeNormal = bordersPaddings.getPaddingBefore(false, cellLM);
        paddingBeforeLeading = bordersPaddings.getPaddingBefore(true, cellLM);
        paddingAfterNormal = bordersPaddings.getPaddingAfter(false, cellLM);
        paddingAfterTrailing = bordersPaddings.getPaddingAfter(true, cellLM);
        bpBeforeNormal = paddingBeforeNormal
                + pgu.getBeforeBorderWidth(0, ConditionalBorder.NORMAL);
        bpBeforeLeading = paddingBeforeLeading
                + pgu.getBeforeBorderWidth(0, ConditionalBorder.REST);
        bpAfterNormal = paddingAfterNormal + pgu.getAfterBorderWidth(ConditionalBorder.NORMAL);
        bpAfterTrailing = paddingAfterTrailing + pgu.getAfterBorderWidth(0, ConditionalBorder.REST);
        elementList = pgu.getElements();
        handleExplicitHeight(
                MinOptMaxUtil.toMinOptMax(pgu.getCell().getBlockProgressionDimension(), tableLM),
                row.getExplicitHeight());
        knuthIter = elementList.listIterator();
        includedLength = -1;  // Avoid troubles with cells having content of zero length
        totalLength = previousRowsLength + ElementListUtils.calcContentLength(elementList);
        endRowIndex = rowIndex + pgu.getCell().getNumberRowsSpanned() - 1;
        keepWithNextStrength = BlockLevelLayoutManager.KEEP_AUTO;
        remainingLength = totalLength - previousRowsLength;

        afterNextStep = new Step(previousRowsLength);
        previousStep = new Step(afterNextStep);
        gotoNextLegalBreak();
        nextStep = new Step(afterNextStep);
        if (afterNextStep.end < elementList.size() - 1) {
            gotoNextLegalBreak();
        }
    }

    /**
     * Modifies the cell's element list by putting filler elements, so that the cell's or
     * row's explicit height is always reached.
     * 
     * TODO this will work properly only for the first break. Then the limitation
     * explained on http://wiki.apache.org/xmlgraphics-fop/TableLayout/KnownProblems
     * occurs. The list of elements needs to be re-adjusted after each break.
     */
    private void handleExplicitHeight(MinOptMax cellBPD, MinOptMax rowBPD) {
        int minBPD = Math.max(cellBPD.min, rowBPD.min);
        if (minBPD > 0) {
            ListIterator iter = elementList.listIterator();
            int cumulateLength = 0;
            boolean prevIsBox = false;
            while (iter.hasNext() && cumulateLength < minBPD) {
                KnuthElement el = (KnuthElement) iter.next();
                if (el.isBox()) {
                    prevIsBox = true;
                    cumulateLength += el.getW();
                } else if (el.isGlue()) {
                    if (prevIsBox) {
                        elementList.add(iter.nextIndex() - 1,
                                new FillerPenalty(minBPD - cumulateLength));
                    }
                    prevIsBox = false;
                    cumulateLength += el.getW();
                } else {
                    prevIsBox = false;
                    if (cumulateLength + el.getW() < minBPD) {
                        iter.set(new FillerPenalty((KnuthPenalty) el, minBPD - cumulateLength));
                    }
                }
            }
        }
        int optBPD = Math.max(minBPD, Math.max(cellBPD.opt, rowBPD.opt));
        if (pgu.getContentLength() < optBPD) {
            elementList.add(new FillerBox(optBPD - pgu.getContentLength()));
        }
    }

    PrimaryGridUnit getPrimaryGridUnit() {
        return pgu;
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
     * @return the remaining length, zero if the cell is finished
     */
    int getRemainingLength() {
        if (includedInLastStep() && (nextStep.end == elementList.size() - 1)) {
            // The cell is finished
            return 0;
        } else {
            return bpBeforeLeading + remainingLength + bpAfterNormal;
        }
    }

    private void gotoNextLegalBreak() {
        afterNextStep.penaltyLength = 0;
        afterNextStep.condBeforeContentLength = 0;
        afterNextStep.breakClass = Constants.EN_AUTO;
        boolean breakFound = false;
        boolean prevIsBox = false;
        boolean boxFound = false;
        while (!breakFound && knuthIter.hasNext()) {
            KnuthElement el = (KnuthElement) knuthIter.next();
            if (el.isPenalty()) {
                prevIsBox = false;
                if (el.getP() < KnuthElement.INFINITE) {
                    // First legal break point
                    breakFound = true;
                    afterNextStep.penaltyLength = el.getW();
                    KnuthPenalty p = (KnuthPenalty) el;
                    if (p.isForcedBreak()) {
                        afterNextStep.breakClass = p.getBreakClass();
                    }
                }
            } else if (el.isGlue()) {
                if (prevIsBox) {
                    // Second legal break point
                    breakFound = true;
                } else {
                    afterNextStep.contentLength += el.getW();
                    if (!boxFound) {
                        afterNextStep.condBeforeContentLength += el.getW();
                    }
                }
                prevIsBox = false;
            } else {
                prevIsBox = true;
                boxFound = true;
                afterNextStep.contentLength += el.getW();
            }
        }
        afterNextStep.end = knuthIter.nextIndex() - 1;
        afterNextStep.totalLength = bpBeforeNormal
                + afterNextStep.contentLength + afterNextStep.penaltyLength
                + bpAfterTrailing; 
    }

    /**
     * Returns the minimal step that is needed for this cell to contribute some content.
     *  
     * @return the step for this cell's first legal break
     */
    int getFirstStep() {
        log.debug(this + ": min first step = " + nextStep.totalLength);
        return nextStep.totalLength;
    }

    /**
     * Returns the last step for this cell. This includes the normal border- and
     * padding-before, the whole content, the normal padding-after, and the
     * <em>trailing</em> after border. Indeed, if the normal border is taken instead,
     * and appears to be smaller than the trailing one, the last step may be smaller than
     * the current step (see TableStepper#considerRowLastStep). This will produce a wrong
     * infinite penalty, plus the cell's content won't be taken into account since the
     * final step will be smaller than the current one (see {@link #signalNextStep(int)}).
     * This actually means that the content will be swallowed.
     * 
     * @return the length of last step
     */
    int getLastStep() {
        assert nextStep.end == elementList.size() - 1;
        assert nextStep.contentLength == totalLength && nextStep.penaltyLength == 0;
        int lastStep = bpBeforeNormal + totalLength + paddingAfterNormal
                + pgu.getAfterBorderWidth(ConditionalBorder.LEADING_TRAILING);
        log.debug(this + ": last step = " + lastStep);
        return lastStep;
    }

    /**
     * Increases the next step up to the given limit.
     * 
     * @param limit the length up to which the next step is allowed to increase
     * @see #signalRowFirstStep(int)
     * @see #signalRowLastStep(int)
     */
    private void increaseCurrentStep(int limit) {
        if (nextStep.end < elementList.size() - 1) {
            while (afterNextStep.totalLength <= limit && nextStep.breakClass == Constants.EN_AUTO) {
                nextStep.set(afterNextStep);
                if (afterNextStep.end >= elementList.size() - 1) {
                    break;
                }
                gotoNextLegalBreak();
            }
        }
    }

    /**
     * Gets the selected first step for the current row. If this cell's first step is
     * smaller, then it may be able to add some more of its content, since there will be
     * no break before the given step anyway.
     * 
     * @param firstStep the current row's first step
     */
    void signalRowFirstStep(int firstStep) {
        increaseCurrentStep(firstStep);
        if (log.isTraceEnabled()) {
            log.trace(this + ": first step increased to " + nextStep.totalLength);
        }
    }

    /** See {@link #signalRowFirstStep(int)}. */
    void signalRowLastStep(int lastStep) {
        increaseCurrentStep(lastStep);
        if (log.isTraceEnabled()) {
            log.trace(this + ": next step increased to " + nextStep.totalLength);
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
            if (nextStep.end >= elementList.size() - 1) {
                nextStep.start = elementList.size();
                return -1;
            } else {
                nextStep.set(afterNextStep);
                nextStep.start = previousStep.end + 1;
                afterNextStep.start = nextStep.start;
                if (afterNextStep.end < elementList.size() - 1) {
                    gotoNextLegalBreak();
                }
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
     * @return the break class of the step, if any. One of {@link Constants#EN_AUTO},
     * {@link Constants#EN_COLUMN}, {@link Constants#EN_PAGE},
     * {@link Constants#EN_EVEN_PAGE}, {@link Constants#EN_ODD_PAGE}. EN_AUTO if this
     * cell's step is not included in the next step.
     */
    int signalNextStep(int minStep) {
        if (nextStep.totalLength <= minStep) {
            includedLength = nextStep.contentLength;
            remainingLength = totalLength - includedLength - afterNextStep.condBeforeContentLength;
            return nextStep.breakClass;
        } else {
            return Constants.EN_AUTO;
        }
    }

    /**
     * Receives indication that the next row is about to start, and that (collapse)
     * borders must be updated accordingly.
     */
    void nextRowStarts() {
        spanIndex++;
        // Subtract the old value of bpAfterTrailing...
        nextStep.totalLength -= bpAfterTrailing;
        afterNextStep.totalLength -= bpAfterTrailing;

        bpAfterTrailing = paddingAfterTrailing
                + pgu.getAfterBorderWidth(spanIndex, ConditionalBorder.REST);

        // ... and add the new one
        nextStep.totalLength += bpAfterTrailing;
        afterNextStep.totalLength += bpAfterTrailing;
        // TODO if the new after border is greater than the previous one the next step may
        // increase further than the row's first step, which can lead to wrong output in
        // some cases
    }

    /**
     * Receives indication that the current row is ending, and that (collapse) borders
     * must be updated accordingly.
     * 
     * @param rowIndex the index of the ending row
     */
    void endRow(int rowIndex) {
        if (endsOnRow(rowIndex)) {
            // Subtract the old value of bpAfterTrailing...
            nextStep.totalLength -= bpAfterTrailing;
            bpAfterTrailing = paddingAfterNormal
                    + pgu.getAfterBorderWidth(ConditionalBorder.LEADING_TRAILING);
            // ... and add the new one
            nextStep.totalLength += bpAfterTrailing;
            lastCellPart = true;
        } else {
            bpBeforeLeading = paddingBeforeLeading
                    + pgu.getBeforeBorderWidth(spanIndex + 1, ConditionalBorder.REST);
        }
    }

    /**
     * Returns true if this cell would be finished after the given step. That is, it would
     * be included in the step and the end of its content would be reached.
     * 
     * @param step the next step
     * @return true if this cell finishes at the given step
     */
    boolean finishes(int step) {
        return nextStep.totalLength <= step && (nextStep.end == elementList.size() - 1);
    }

    /**
     * Creates and returns a CellPart instance for the content of this cell which
     * is included in the next step.
     * 
     * @return a CellPart instance
     */
    CellPart createCellPart() {
        if (nextStep.end + 1 == elementList.size()) {
            keepWithNextStrength = pgu.getKeepWithNextStrength();
            // TODO if keep-with-next is set on the row, must every cell of the row
            // contribute some content from children blocks?
            // see http://mail-archives.apache.org/mod_mbox/xmlgraphics-fop-dev/200802.mbox/
            // %3c47BDA379.4050606@anyware-tech.com%3e
            // Assuming no, but if yes the following code should enable this behaviour
//            if (pgu.getRow() != null && pgu.getRow().mustKeepWithNext()) {
//                keepWithNextSignal = true; //to be converted to integer strengths
//            }
        }
        int bpBeforeFirst;
        if (nextStep.start == 0) {
            bpBeforeFirst = pgu.getBeforeBorderWidth(0, ConditionalBorder.LEADING_TRAILING)
                    + paddingBeforeNormal;
        } else {
            bpBeforeFirst = bpBeforeLeading;
        }
        int length = nextStep.contentLength - nextStep.condBeforeContentLength
                - previousStep.contentLength;
        if (!includedInLastStep() || nextStep.start == elementList.size()) {
            return new CellPart(pgu, nextStep.start, previousStep.end, lastCellPart,
                    0, 0, previousStep.penaltyLength,
                    bpBeforeNormal, bpBeforeFirst, bpAfterNormal, bpAfterTrailing);
        } else {
            return new CellPart(pgu, nextStep.start, nextStep.end, lastCellPart,
                    nextStep.condBeforeContentLength, length, nextStep.penaltyLength,
                    bpBeforeNormal, bpBeforeFirst, bpAfterNormal, bpAfterTrailing);
        }
    }

    int getKeepWithNextStrength() {
        return keepWithNextStrength;
    }

    
    /** {@inheritDoc} */
    public String toString() {
        return "Cell " + (pgu.getRowIndex() + 1) + "." + (pgu.getColIndex() + 1);
    }
}
