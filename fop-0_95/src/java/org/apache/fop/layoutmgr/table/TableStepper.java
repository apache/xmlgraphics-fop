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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.flow.table.EffRow;
import org.apache.fop.fo.flow.table.GridUnit;
import org.apache.fop.fo.flow.table.PrimaryGridUnit;
import org.apache.fop.layoutmgr.BreakElement;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.util.BreakUtil;

/**
 * This class processes row groups to create combined element lists for tables.
 */
public class TableStepper {

    /** Logger **/
    private static Log log = LogFactory.getLog(TableStepper.class);

    private TableContentLayoutManager tclm;

    private EffRow[] rowGroup;
    /** Number of columns in the row group. */
    private int columnCount;
    private int totalHeight;
    private int previousRowsLength;
    private int activeRowIndex;

    private boolean rowFinished;

    /** Cells spanning the current row. */
    private List activeCells = new LinkedList();

    /** Cells that will start the next row. */
    private List nextActiveCells = new LinkedList();

    /**
     * True if the next row is being delayed, that is, if cells spanning the current and
     * the next row have steps smaller than the next row's first step. In this case the
     * next row may be extended to offer additional break possibilities.
     */
    private boolean delayingNextRow;

    /**
     * The first step for a row. This is the minimal step necessary to include some
     * content from all the cells starting the row.
     */
    private int rowFirstStep;

    /**
     * Flag used to produce an infinite penalty if the height of the current row is
     * smaller than the first step for that row (may happen with row-spanning cells).
     * 
     * @see #considerRowLastStep(int)
     */
    private boolean rowHeightSmallerThanFirstStep;

    /**
     * The class of the next break. One of {@link Constants#EN_AUTO},
     * {@link Constants#EN_COLUMN}, {@link Constants#EN_PAGE},
     * {@link Constants#EN_EVEN_PAGE}, {@link Constants#EN_ODD_PAGE}. Defaults to
     * EN_AUTO.
     */
    private int nextBreakClass;

    /**
     * Main constructor
     * @param tclm The parent TableContentLayoutManager
     */
    public TableStepper(TableContentLayoutManager tclm) {
        this.tclm = tclm;
        this.columnCount = tclm.getTableLM().getTable().getNumberOfColumns();
    }

    /**
     * Initializes the fields of this instance to handle a new row group.
     * 
     * @param rows the new row group to handle
     */
    private void setup(EffRow[] rows) {
        rowGroup = rows;
        previousRowsLength = 0;
        activeRowIndex = 0;
        activeCells.clear();
        nextActiveCells.clear();
        delayingNextRow = false;
        rowFirstStep = 0;
        rowHeightSmallerThanFirstStep = false;
    }

    private void calcTotalHeight() {
        totalHeight = 0;
        for (int i = 0; i < rowGroup.length; i++) {
            totalHeight += rowGroup[i].getHeight().opt;
        }
        if (log.isDebugEnabled()) {
            log.debug("totalHeight=" + totalHeight);
        }
    }

    private int getMaxRemainingHeight() {
        int maxW = 0;
        for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
            ActiveCell activeCell = (ActiveCell) iter.next();
            int remain = activeCell.getRemainingLength();
            PrimaryGridUnit pgu = activeCell.getPrimaryGridUnit();
            for (int i = activeRowIndex + 1; i < pgu.getRowIndex() - rowGroup[0].getIndex()
                    + pgu.getCell().getNumberRowsSpanned(); i++) {
                remain -= rowGroup[i].getHeight().opt;
            }
            maxW = Math.max(maxW, remain);
        }
        for (int i = activeRowIndex + 1; i < rowGroup.length; i++) {
            maxW += rowGroup[i].getHeight().opt;
        }
        return maxW;
    }

    /**
     * Creates ActiveCell instances for cells starting on the row at the given index.
     * 
     * @param activeCellList the list that will hold the active cells
     * @param rowIndex the index of the row from which cells must be activated
     */
    private void activateCells(List activeCellList, int rowIndex) {
        EffRow row = rowGroup[rowIndex];
        for (int i = 0; i < columnCount; i++) {
            GridUnit gu = row.getGridUnit(i);
            if (!gu.isEmpty() && gu.isPrimary()) {
                activeCellList.add(new ActiveCell((PrimaryGridUnit) gu, row, rowIndex,
                        previousRowsLength, getTableLM()));
            }
        }
    }

    /**
     * Creates the combined element list for a row group.
     * @param context Active LayoutContext
     * @param rows the row group
     * @param bodyType Indicates what type of body is processed (body, header or footer)
     * @return the combined element list
     */
    public LinkedList getCombinedKnuthElementsForRowGroup(LayoutContext context, EffRow[] rows,
            int bodyType) {
        setup(rows);
        activateCells(activeCells, 0);
        calcTotalHeight();

        int cumulateLength = 0; // Length of the content accumulated before the break
        TableContentPosition lastTCPos = null;
        LinkedList returnList = new LinkedList();
        int laststep = 0;
        int step = getFirstStep();
        do {
            int maxRemainingHeight = getMaxRemainingHeight();
            int penaltyOrGlueLen = step + maxRemainingHeight - totalHeight;
            int boxLen = step - cumulateLength - Math.max(0, penaltyOrGlueLen)/* penalty, if any */;
            cumulateLength += boxLen + Math.max(0, -penaltyOrGlueLen)/* the glue, if any */;

            if (log.isDebugEnabled()) {
                log.debug("Next step: " + step + " (+" + (step - laststep) + ")");
                log.debug("           max remaining height: " + maxRemainingHeight);
                if (penaltyOrGlueLen >= 0) {
                    log.debug("           box = " + boxLen + " penalty = " + penaltyOrGlueLen);
                } else {
                    log.debug("           box = " + boxLen + " glue = " + (-penaltyOrGlueLen));
                }
            }

            //Put all involved grid units into a list
            List cellParts = new java.util.ArrayList(columnCount);
            for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
                ActiveCell activeCell = (ActiveCell) iter.next();
                CellPart part = activeCell.createCellPart();
                cellParts.add(part);
            }

            //Create elements for step
            TableContentPosition tcpos = new TableContentPosition(getTableLM(),
                    cellParts, rowGroup[activeRowIndex]);
            if (delayingNextRow) {
                tcpos.setNewPageRow(rowGroup[activeRowIndex + 1]);
            }
            if (returnList.size() == 0) {
                tcpos.setFlag(TableContentPosition.FIRST_IN_ROWGROUP, true);
            }
            lastTCPos = tcpos;
            returnList.add(new KnuthBox(boxLen, tcpos, false));

            int effPenaltyLen = Math.max(0, penaltyOrGlueLen);
            TableHFPenaltyPosition penaltyPos = new TableHFPenaltyPosition(getTableLM());
            if (bodyType == TableRowIterator.BODY) {
                if (!getTableLM().getTable().omitHeaderAtBreak()) {
                    effPenaltyLen += tclm.getHeaderNetHeight();
                    penaltyPos.headerElements = tclm.getHeaderElements();
                }
                if (!getTableLM().getTable().omitFooterAtBreak()) {
                    effPenaltyLen += tclm.getFooterNetHeight();
                    penaltyPos.footerElements = tclm.getFooterElements();
                }
            }

            int p = 0;
            boolean keepWithNext = false;
            for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
                ActiveCell activeCell = (ActiveCell) iter.next();
                keepWithNext |= activeCell.keepWithNextSignal();
            }
            if (keepWithNext || getTableLM().mustKeepTogether()) {
                p = KnuthPenalty.INFINITE;
            }
            if (!rowFinished) {
                if (rowGroup[activeRowIndex].mustKeepTogether()) {
                    p = KnuthPenalty.INFINITE;
                }
            } else if (activeRowIndex < rowGroup.length - 1) {
                if (rowGroup[activeRowIndex].mustKeepWithNext()
                        || rowGroup[activeRowIndex + 1].mustKeepWithPrevious()) {
                    p = KnuthPenalty.INFINITE;
                }
                nextBreakClass = BreakUtil.compareBreakClasses(nextBreakClass,
                        rowGroup[activeRowIndex].getBreakAfter());
                nextBreakClass = BreakUtil.compareBreakClasses(nextBreakClass,
                        rowGroup[activeRowIndex + 1].getBreakBefore());
            }
            if (nextBreakClass != Constants.EN_AUTO) {
                log.trace("Forced break encountered");
                p = -KnuthPenalty.INFINITE; //Overrides any keeps (see 4.8 in XSL 1.0)
            }
            if (rowHeightSmallerThanFirstStep) {
                rowHeightSmallerThanFirstStep = false;
                p = KnuthPenalty.INFINITE;
            }
            returnList.add(new BreakElement(penaltyPos, effPenaltyLen, p, nextBreakClass, context));
            if (penaltyOrGlueLen < 0) {
                returnList.add(new KnuthGlue(-penaltyOrGlueLen, 0, 0, new Position(null), true));
            }

            laststep = step;
            step = getNextStep();
        } while (step >= 0);
        assert !returnList.isEmpty();
        lastTCPos.setFlag(TableContentPosition.LAST_IN_ROWGROUP, true);
        return returnList;
    }

    /**
     * Returns the first step for the current row group.
     * 
     * @return the first step for the current row group
     */
    private int getFirstStep() {
        computeRowFirstStep(activeCells);
        signalRowFirstStep();
        int minStep = considerRowLastStep(rowFirstStep);
        signalNextStep(minStep);
        return minStep;
    }

    /**
     * Returns the next break possibility.
     * 
     * @return the next step
     */
    private int getNextStep() {
        if (rowFinished) {
            if (activeRowIndex == rowGroup.length - 1) {
                // The row group is finished, no next step
                return -1;
            }
            rowFinished = false;
            removeCellsEndingOnCurrentRow();
            log.trace("Delaying next row");
            delayingNextRow = true;
        }
        if (delayingNextRow) {
            int minStep = computeMinStep();
            if (minStep < 0 || minStep >= rowFirstStep
                    || minStep > rowGroup[activeRowIndex].getExplicitHeight().max) {
                if (log.isTraceEnabled()) {
                    log.trace("Step = " + minStep);
                }
                delayingNextRow = false;
                minStep = rowFirstStep;
                switchToNextRow();
                signalRowFirstStep();
                minStep = considerRowLastStep(minStep);
            }
            signalNextStep(minStep);
            return minStep;
        } else {
            int minStep = computeMinStep();
            minStep = considerRowLastStep(minStep);
            signalNextStep(minStep);
            return minStep;
        }
    }

    /**
     * Computes the minimal necessary step to make the next row fit. That is, so such as
     * cell on the next row can contribute some content.
     * 
     * @param cells the cells occupying the next row (may include cells starting on
     * previous rows and spanning over this one)
     */
    private void computeRowFirstStep(List cells) {
        for (Iterator iter = cells.iterator(); iter.hasNext();) {
            ActiveCell activeCell = (ActiveCell) iter.next();
            rowFirstStep = Math.max(rowFirstStep, activeCell.getFirstStep());
        }
    }

    /**
     * Computes the next minimal step.
     * 
     * @return the minimal step from the active cells, &lt; 0 if there is no such step
     */
    private int computeMinStep() {
        int minStep = Integer.MAX_VALUE;
        boolean stepFound = false;
        for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
            ActiveCell activeCell = (ActiveCell) iter.next();
            int nextStep = activeCell.getNextStep();
            if (nextStep >= 0) {
                stepFound = true;
                minStep = Math.min(minStep, nextStep);
            }
        }
        if (stepFound) {
            return minStep;
        } else {
            return -1;
        }
    }

    /**
     * Signals the first step to the active cells, to allow them to add more content to
     * the step if possible.
     * 
     * @see ActiveCell#signalRowFirstStep(int)
     */
    private void signalRowFirstStep() {
        for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
            ActiveCell activeCell = (ActiveCell) iter.next();
            activeCell.signalRowFirstStep(rowFirstStep);
        }
    }

    /**
     * Signals the next selected step to the active cells.
     *  
     * @param step the next step
     */
    private void signalNextStep(int step) {
        nextBreakClass = Constants.EN_AUTO;
        for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
            ActiveCell activeCell = (ActiveCell) iter.next();
            nextBreakClass = BreakUtil.compareBreakClasses(nextBreakClass,
                    activeCell.signalNextStep(step));
        }
    }

    /**
     * Determines if the given step will finish the current row, and if so switch to the
     * last step for this row.
     * <p>If the row is finished then the after borders for the cell may change (their
     * conditionalities no longer apply for the cells ending on the current row). Thus the
     * final step may grow with respect to the given one.</p>
     * <p>In more rare occasions, the given step may correspond to the first step of a
     * row-spanning cell, and may be greater than the height of the current row (consider,
     * for example, an unbreakable cell spanning three rows). In such a case the returned
     * step will correspond to the row height and a flag will be set to produce an
     * infinite penalty for this step. This will prevent the breaking algorithm from
     * choosing this break, but still allow to create the appropriate TableContentPosition
     * for the cells ending on the current row.</p>
     * 
     * @param step the next step
     * @return the updated step if any
     */
    private int considerRowLastStep(int step) {
        rowFinished = true;
        for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
            ActiveCell activeCell = (ActiveCell) iter.next();
            if (activeCell.endsOnRow(activeRowIndex)) {
                rowFinished &= activeCell.finishes(step);
            }
        }
        if (rowFinished) {
            if (log.isTraceEnabled()) {
                log.trace("Step = " + step);
                log.trace("Row finished, computing last step");
            }
            int maxStep = 0;
            for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
                ActiveCell activeCell = (ActiveCell) iter.next();
                if (activeCell.endsOnRow(activeRowIndex)) {
                    maxStep = Math.max(maxStep, activeCell.getLastStep());
                }
            }
            if (log.isTraceEnabled()) {
                log.trace("Max step: " + maxStep);
            }
            for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
                ActiveCell activeCell = (ActiveCell) iter.next();
                activeCell.endRow(activeRowIndex);                        
                if (!activeCell.endsOnRow(activeRowIndex)) {
                    activeCell.signalRowLastStep(maxStep);
                }
            }
            if (maxStep < step) {
                log.trace("Row height smaller than first step, produced penalty will be infinite");
                rowHeightSmallerThanFirstStep = true;
            }
            step = maxStep;
            prepareNextRow();
        }
        return step;
    }

    /**
     * Pre-activates the cells that will start the next row, and computes the first step
     * for that row.
     */
    private void prepareNextRow() {
        if (activeRowIndex < rowGroup.length - 1) {
            previousRowsLength += rowGroup[activeRowIndex].getHeight().opt;
            activateCells(nextActiveCells, activeRowIndex + 1);
            if (log.isTraceEnabled()) {
                log.trace("Computing first step for row " + (activeRowIndex + 2));
            }
            computeRowFirstStep(nextActiveCells);
            if (log.isTraceEnabled()) {
                log.trace("Next first step = " + rowFirstStep);
            }
        }
    }

    private void removeCellsEndingOnCurrentRow() {
        for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
            ActiveCell activeCell = (ActiveCell) iter.next();
            if (activeCell.endsOnRow(activeRowIndex)) {
                iter.remove();
            }
        }
    }

    /**
     * Actually switches to the next row, increasing activeRowIndex and transferring to
     * activeCells the cells starting on the next row.
     */
    private void switchToNextRow() {
        activeRowIndex++;
        if (log.isTraceEnabled()) {
            log.trace("Switching to row " + (activeRowIndex + 1));
        }
        for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
            ActiveCell activeCell = (ActiveCell) iter.next();
            activeCell.nextRowStarts();
        }
        activeCells.addAll(nextActiveCells);
        nextActiveCells.clear();
    }

    /** @return the table layout manager */
    private TableLayoutManager getTableLM() {
        return this.tclm.getTableLM();
    }
}
