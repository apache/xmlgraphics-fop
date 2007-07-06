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
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.layoutmgr.BreakElement;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.LayoutContext;

/**
 * This class processes row groups to create combined element lists for tables.
 */
public class TableStepper {

    private static class ActiveCell {
        private PrimaryGridUnit pgu;
        /** Knuth elements for this active cell. */
        private List elementList;
        /** Number of the row where the row-span begins, zero-based. */
        private int startRow;
        /** Index, in the list of Knuth elements, of the element starting the current step. */
        private int start;
        /** Index, in the list of Knuth elements, of the element ending the current step. */
        private int end;
        /**
         * Total length of the Knuth elements already included in the steps, up to the
         * current one.
         */
        private int width;
        private int backupWidth;
        private int baseWidth;
        private int borderBefore;
        private int borderAfter;
        private int paddingBefore;
        private int paddingAfter;
        private boolean keepWithNextSignal;
        private int lastPenaltyLength;
        private TableLayoutManager tableLM;

        ActiveCell(PrimaryGridUnit pgu, EffRow row, int rowIndex, EffRow[] rowGroup, TableLayoutManager tableLM) {
            this.tableLM = tableLM;
            this.pgu = pgu;
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
                makeBoxForWholeRow |= pgu.getTable().mustKeepTogether();
            }
            if (makeBoxForWholeRow) {
                elementList = new java.util.ArrayList(1);
                int height = row.getExplicitHeight().opt;
                if (height == 0) {
                    height = row.getHeight().opt;
                }
                elementList.add(new KnuthBoxCellWithBPD(height));
            } else {
                //Copy elements (LinkedList) to array lists to improve 
                //element access performance
                elementList = new java.util.ArrayList(pgu.getElements());
//                if (log.isTraceEnabled()) {
//                    log.trace("column " + (column+1) + ": recording " + elementLists.size() + " element(s)");
//                }
            }
            if (pgu.getTable().isSeparateBorderModel()) {
                borderBefore = pgu.getBorders().getBorderBeforeWidth(false);
                borderAfter = pgu.getBorders().getBorderAfterWidth(false);
            } else {
                borderBefore = pgu.getHalfMaxBeforeBorderWidth();
                borderAfter = pgu.getHalfMaxAfterBorderWidth();
            }
            paddingBefore = pgu.getBorders().getPaddingBefore(false, pgu.getCellLM());
            paddingAfter = pgu.getBorders().getPaddingAfter(false, pgu.getCellLM());
            start = 0;
            end = -1;
            width = 0;
            startRow = rowIndex;
            keepWithNextSignal = false;
            computeBaseWidth(rowGroup);
        }

        private void computeBaseWidth(EffRow[] rowGroup) {
            baseWidth = 0;
            for (int prevRow = 0; prevRow < startRow; prevRow++) {
                baseWidth += rowGroup[prevRow].getHeight().opt;
            }
        }

        private boolean endsOnRow(int rowIndex) {
            return rowIndex == startRow + pgu.getCell().getNumberRowsSpanned() - 1;
        }

        int getRemainingHeight(int activeRowIndex, int halfBorderSeparationBPD, EffRow[] rowGroup) {
            if (end == elementList.size() - 1) {
                return 0;
            }
            if (!endsOnRow(activeRowIndex)) {
                return 0;
            }
            int len = width;
            if (len > 0) {
                len += 2 * halfBorderSeparationBPD;
                len += borderBefore + borderAfter;
                len += paddingBefore + paddingAfter;
            }
            int nominalHeight = 0;
            for (int r = startRow; r < startRow + pgu.getCell().getNumberRowsSpanned(); r++) {
                nominalHeight += rowGroup[r].getHeight().opt;
            }
            return nominalHeight - len;
        }

        void backupWidth() {
            backupWidth = width;
        }

        int getNextStep() {
            lastPenaltyLength = 0;
            while (end + 1 < elementList.size()) {
                end++;
                KnuthElement el = (KnuthElement)elementList.get(end);
                if (el.isPenalty()) {
                    if (el.getP() < KnuthElement.INFINITE) {
                        //First legal break point
                        lastPenaltyLength = el.getW();
                        break;
                    }
                } else if (el.isGlue()) {
                    if (end > 0) {
                        KnuthElement prev = (KnuthElement)elementList.get(end - 1);
                        if (prev.isBox()) {
                            //Second legal break point
                            break;
                        }
                    }
                    width += el.getW();
                } else {
                    width += el.getW();
                }
            }
            if (end < start) {
//              if (log.isTraceEnabled()) {
//              log.trace("column " + (i + 1) + ": (end=" + end + ") < (start=" + start
//              + ") => resetting width to backupWidth");
//              }
                width = backupWidth;
                return 0;
            } else {
                return baseWidth + width + borderBefore + borderAfter + paddingBefore
                        + paddingAfter + 2 * tableLM.getHalfBorderSeparationBPD();
            }
        }

        boolean signalMinStep(int minStep) {
            int len = baseWidth + width + borderBefore + borderAfter + paddingBefore + paddingAfter
                    + 2 * tableLM.getHalfBorderSeparationBPD();
            if (len > minStep) {
                width = backupWidth;
                end = start - 1;
                return baseWidth + borderBefore + borderAfter + paddingBefore
                        + paddingAfter + 2 * tableLM.getHalfBorderSeparationBPD() + width > minStep;
            } else {
                return false;
            }
        }

        int getLastPenaltyLength() {
            return lastPenaltyLength;
        }
    }
    /** Logger **/
    private static Log log = LogFactory.getLog(TableStepper.class);

    private TableContentLayoutManager tclm;

    private EffRow[] rowGroup;
    /** Number of columns in the row group. */
    private int columnCount;
    private int totalHeight;
    private int activeRowIndex;
    private boolean rowBacktrackForLastStep;
    private boolean skippedStep;
    private int lastMaxPenaltyLength;

    private List activeCells = new LinkedList();

    /**
     * Main constructor
     * @param tclm The parent TableContentLayoutManager
     */
    public TableStepper(TableContentLayoutManager tclm) {
        this.tclm = tclm;
    }

    /**
     * Initializes the fields of this instance to handle a new row group.
     * 
     * @param columnCount number of columns the row group has 
     */
    private void setup(int columnCount) {
        this.columnCount = columnCount;
        this.activeRowIndex = 0;
    }

    /**
     * Returns the row currently being processed.
     *
     * @return the row currently being processed
     */
    private EffRow getActiveRow() {
        return rowGroup[activeRowIndex];
    }

    /**
     * Returns the grid unit at the given column number on the active row.
     *
     * @param column column number of the grid unit to get
     * @return the corresponding grid unit (may be null)
     * @see TableStepper#getActiveRow
     */
    private GridUnit getActiveGridUnit(int column) {
        return getActiveRow().safelyGetGridUnit(column);
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
        if (!rowBacktrackForLastStep) {
            for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
                maxW = Math.max(maxW, ((ActiveCell) iter.next()).getRemainingHeight(activeRowIndex,
                        getTableLM().getHalfBorderSeparationBPD(), rowGroup));
            }
        }
        for (int i = activeRowIndex + (rowBacktrackForLastStep ? 0 : 1); i < rowGroup.length; i++) {
            maxW += rowGroup[i].getHeight().opt;
        }
        log.debug("maxRemainingHeight=" + maxW);
        return maxW;
    }

    private void setupElementList(int column) {
        GridUnit gu = getActiveGridUnit(column);
        EffRow row = getActiveRow();
        if (gu != null && !gu.isEmpty() && gu.isPrimary()) {
            activeCells.add(new ActiveCell((PrimaryGridUnit) gu, row, activeRowIndex, rowGroup, getTableLM()));
        }
    }

    /**
     * Initializes the informations relative to the Knuth elements, to handle a new row in
     * the current row group.
     */
    private void initializeElementLists() {
        log.trace("Entering initializeElementLists()");
        for (int i = 0; i < columnCount; i++) {
            setupElementList(i);
        }
    }

    /**
     * Creates the combined element list for a row group.
     * @param context Active LayoutContext
     * @param rowGroup the row group
     * @param maxColumnCount the maximum number of columns to expect
     * @param bodyType Indicates what type of body is processed (body, header or footer)
     * @return the combined element list
     */
    public LinkedList getCombinedKnuthElementsForRowGroup(
            LayoutContext context,
            EffRow[] rowGroup, int maxColumnCount, int bodyType) {
        this.rowGroup = rowGroup;
        setup(maxColumnCount);
        initializeElementLists();
        calcTotalHeight();

        boolean signalKeepWithNext = false;
        int laststep = 0;
        int step;
        int addedBoxLen = 0;
        TableContentPosition lastTCPos = null;
        LinkedList returnList = new LinkedList();
        while ((step = getNextStep()) >= 0) {
            int normalRow = activeRowIndex;
            int increase = step - laststep;
            int penaltyLen = step + getMaxRemainingHeight() - totalHeight;
            int boxLen = step - addedBoxLen - penaltyLen;
            addedBoxLen += boxLen;

            boolean forcedBreak = false;
            int breakClass = -1;
            //Put all involved grid units into a list
            List gridUnitParts = new java.util.ArrayList(maxColumnCount);
            for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
                ActiveCell activeCell = (ActiveCell) iter.next();
                if (activeCell.end >= activeCell.start) {
                    PrimaryGridUnit pgu = activeCell.pgu;
                    if (activeCell.start == 0 && activeCell.end == 0
                            && activeCell.elementList.size() == 1
                            && activeCell.elementList.get(0) instanceof KnuthBoxCellWithBPD) {
                        //Special case: Cell with fixed BPD
                        gridUnitParts.add(new GridUnitPart(pgu,
                                0, pgu.getElements().size() - 1));
                    } else {
                        gridUnitParts.add(new GridUnitPart(pgu, activeCell.start, activeCell.end));
                        if (((KnuthElement)activeCell.elementList.get(activeCell.end)).isForcedBreak()) {
                            forcedBreak = true;
                            breakClass = ((KnuthPenalty)activeCell.elementList.get(activeCell.end)).getBreakClass();
                        }
                    }
                    if (activeCell.end + 1 == activeCell.elementList.size()) {
                        if (pgu.getFlag(GridUnit.KEEP_WITH_NEXT_PENDING)) {
                            log.debug("PGU has pending keep-with-next");
                            activeCell.keepWithNextSignal = true;
                        }
                        if (pgu.getRow() != null && pgu.getRow().mustKeepWithNext()) {
                            log.debug("table-row causes keep-with-next");
                            activeCell.keepWithNextSignal = true;
                        }
                    }
                    if (activeCell.start == 0 && activeCell.end >= 0) {
                        if (pgu.getFlag(GridUnit.KEEP_WITH_PREVIOUS_PENDING)) {
                            log.debug("PGU has pending keep-with-previous");
                            if (returnList.size() == 0) {
                                context.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING);
                            }
                        }
                        if (pgu.getRow() != null && pgu.getRow().mustKeepWithPrevious()) {
                            log.debug("table-row causes keep-with-previous");
                            if (returnList.size() == 0) {
                                context.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING);
                            }
                        }
                    }
                }
            }
            //log.debug(">>> guPARTS: " + gridUnitParts);

            //Create elements for step
            int effPenaltyLen = penaltyLen;
            TableContentPosition tcpos = new TableContentPosition(getTableLM(),
                    gridUnitParts, rowGroup[normalRow]);
            if (returnList.size() == 0) {
                tcpos.setFlag(TableContentPosition.FIRST_IN_ROWGROUP, true);
            }
            lastTCPos = tcpos;
            if (log.isDebugEnabled()) {
                log.debug(" - backtrack=" + rowBacktrackForLastStep
                        + " - row=" + activeRowIndex + " - " + tcpos);
            }
            returnList.add(new KnuthBox(boxLen, tcpos, false));
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

            //Handle a penalty length coming from nested content
            //Example: nested table with header/footer
            if (this.lastMaxPenaltyLength != 0) {
                penaltyPos.nestedPenaltyLength = this.lastMaxPenaltyLength;
                if (log.isDebugEnabled()) {
                    log.debug("Additional penalty length from table-cell break: "
                            + this.lastMaxPenaltyLength);
                }
            }
            effPenaltyLen += this.lastMaxPenaltyLength;

            int p = 0;
            boolean allCellsHaveContributed = true;
            signalKeepWithNext = false;
            for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
                ActiveCell activeCell = (ActiveCell) iter.next();
                if (activeCell.start == 0 && activeCell.end < 0 && activeCell.elementList != null) {
                    allCellsHaveContributed = false;
                }
                signalKeepWithNext |= activeCell.keepWithNextSignal;
            }
            if (!allCellsHaveContributed) {
                //Not all cells have contributed to a newly started row. The penalty here is
                //used to avoid breaks resulting in badly broken tables.
                //See also: http://marc.theaimsgroup.com/?t=112248999600005&r=1&w=2
                p = 900; //KnuthPenalty.INFINITE; //TODO Arbitrary value. Please refine.
            }
            if (signalKeepWithNext || getTableLM().mustKeepTogether()) {
                p = KnuthPenalty.INFINITE;
            }
            if (skippedStep) {
                p = KnuthPenalty.INFINITE;
                //Need to avoid breaking because borders and/or paddding from other columns would
                //not fit in the available space (see getNextStep())
            }
            if (forcedBreak) {
                if (skippedStep) {
                    log.error("This is a conflict situation. The output may be wrong."
                            + " Please send your FO file to fop-dev@xmlgraphics.apache.org!");
                }
                p = -KnuthPenalty.INFINITE; //Overrides any keeps (see 4.8 in XSL 1.0)
            }
            returnList.add(new BreakElement(penaltyPos, effPenaltyLen, p, breakClass, context));

            if (log.isDebugEnabled()) {
                log.debug("step=" + step + " (+" + increase + ")"
                        + " box=" + boxLen
                        + " penalty=" + penaltyLen
                        + " effPenalty=" + effPenaltyLen);
            }

            laststep = step;
        }
        if (signalKeepWithNext) {
            //Last step signalled a keep-with-next. Since the last penalty will be removed,
            //we have to signal the still pending last keep-with-next using the LayoutContext.
            context.setFlags(LayoutContext.KEEP_WITH_NEXT_PENDING);
        }
        if (lastTCPos != null) {
            lastTCPos.setFlag(TableContentPosition.LAST_IN_ROWGROUP, true);
        }
        return returnList;
    }

    /**
     * Finds the smallest increment leading to the next legal break inside the row-group.
     * 
     * @return the size of the increment, -1 if no next step is available (end of row-group reached)
     */
    private int getNextStep() {
        log.trace("Entering getNextStep");
        this.lastMaxPenaltyLength = 0;
        //Check for forced break conditions
        /*
        if (isBreakCondition()) {
            return -1;
        }*/

        for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
            ((ActiveCell) iter.next()).backupWidth();
        }

        //set starting points
        goToNextRowIfCurrentFinished();

        //Get next possible sequence for each cell
        //Determine smallest possible step
        int minStep = Integer.MAX_VALUE;
        boolean stepFound = false;
        for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
            ActiveCell activeCell = (ActiveCell) iter.next();
            int nextStep = activeCell.getNextStep();
            if (nextStep > 0) {
                stepFound = true;
                minStep = Math.min(minStep, nextStep);
                lastMaxPenaltyLength = Math.max(lastMaxPenaltyLength, activeCell
                        .getLastPenaltyLength());
            }
        }
        if (!stepFound) {
            return -1;
        }


        //Reset bigger-than-minimum sequences
        //See http://people.apache.org/~jeremias/fop/NextStepAlgoNotes.pdf
        rowBacktrackForLastStep = false;
        skippedStep = false;
        for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
            ActiveCell activeCell = (ActiveCell) iter.next();
            if (activeCell.signalMinStep(minStep)) {
                if (activeRowIndex == 0) {
                    log.debug("  First row. Skip this step.");
                    skippedStep = true;
                } else {
                    log.debug("  row-span situation: backtracking to last row");
                    //Stay on the previous row for another step because borders and padding on 
                    //columns may make their contribution to the step bigger than the addition
                    //of the next element for this step would make the step to grow.
                    rowBacktrackForLastStep = true;
                }
            }
        }

        return minStep;
    }

    private void removeCellsEndingOnCurrentRow() {
        for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
            ActiveCell activeCell = (ActiveCell) iter.next();
            if (activeCell.endsOnRow(activeRowIndex)) {
                iter.remove();
            }
        }
    }

    private void goToNextRowIfCurrentFinished() {
        // We assume that the current grid row is finished. If this is not the case this
        // boolean will be reset (see below)
        boolean currentGridRowFinished = true;
        for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
            ActiveCell activeCell = (ActiveCell) iter.next();
            if (activeCell.end < activeCell.elementList.size()) {
                activeCell.start = activeCell.end + 1;
                if (activeCell.end + 1 < activeCell.elementList.size()
                        && activeCell.endsOnRow(activeRowIndex)) {
                    // Ok, so this grid unit is the last in the row-spanning direction and
                    // there are still unhandled Knuth elements. They /will/ have to be
                    // put on the current grid row, which means that this row isn't
                    // finished yet
                    currentGridRowFinished = false;
                }
            }
        }

        if (currentGridRowFinished) {
            removeCellsEndingOnCurrentRow();
            if (activeRowIndex < rowGroup.length - 1) {
                TableRow rowFO = getActiveRow().getTableRow();
                if (rowFO != null && rowFO.getBreakAfter() != Constants.EN_AUTO) {
                    log.warn(FONode.decorateWithContextInfo(
                            "break-after ignored on table-row because of row spanning "
                            + "in progress (See XSL 1.0, 7.19.1)", rowFO));
                }
                activeRowIndex++;
                if (log.isDebugEnabled()) {
                    log.debug("===> new row: " + activeRowIndex);
                }
                initializeElementLists();
                rowFO = getActiveRow().getTableRow();
                if (rowFO != null && rowFO.getBreakBefore() != Constants.EN_AUTO) {
                    log.warn(FONode.decorateWithContextInfo(
                            "break-before ignored on table-row because of row spanning "
                            + "in progress (See XSL 1.0, 7.19.2)", rowFO));
                }
            }
        }
    }

    /** @return the table layout manager */
    private TableLayoutManager getTableLM() {
        return this.tclm.getTableLM();
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
