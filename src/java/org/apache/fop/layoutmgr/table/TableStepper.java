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
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.Position;

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
    private int activeRowIndex;
    private boolean rowBacktrackForLastStep;
    private boolean skippedStep;

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
                        rowGroup));
            }
        }
        for (int i = activeRowIndex + (rowBacktrackForLastStep ? 0 : 1); i < rowGroup.length; i++) {
            maxW += rowGroup[i].getHeight().opt;
        }
        log.debug("maxRemainingHeight=" + maxW);
        return maxW;
    }

    /**
     * Initializes the informations relative to the Knuth elements, to handle a new row in
     * the current row group.
     */
    private void initializeElementLists() {
        log.trace("Entering initializeElementLists()");
        EffRow row = getActiveRow();
        for (int i = 0; i < columnCount; i++) {
            GridUnit gu = getActiveGridUnit(i);
            if (gu != null && !gu.isEmpty() && gu.isPrimary()) {
                activeCells.add(new ActiveCell((PrimaryGridUnit) gu, row, activeRowIndex, rowGroup, getTableLM()));
            }
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
            int penaltyOrGlueLen = step + getMaxRemainingHeight() - totalHeight;
            int boxLen = step - addedBoxLen - Math.max(0, penaltyOrGlueLen);
            addedBoxLen += boxLen;

            boolean forcedBreak = false;
            int breakClass = -1;
            //Put all involved grid units into a list
            List gridUnitParts = new java.util.ArrayList(maxColumnCount);
            for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
                ActiveCell activeCell = (ActiveCell) iter.next();
                if (activeCell.contributesContent()) {
                    GridUnitPart gup = activeCell.createGridUnitPart();
                    gridUnitParts.add(gup);
                    forcedBreak = activeCell.isLastForcedBreak();
                    if (forcedBreak) {
                        breakClass = activeCell.getLastBreakClass();
                    }
                    if (returnList.size() == 0 && gup.isFirstPart() && gup.mustKeepWithPrevious()) {
                        context.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING);
                    }
                }
            }
            //log.debug(">>> guPARTS: " + gridUnitParts);

            //Create elements for step
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
            boolean allCellsHaveContributed = true;
            signalKeepWithNext = false;
            for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
                ActiveCell activeCell = (ActiveCell) iter.next();
                allCellsHaveContributed &= activeCell.hasStarted();
                signalKeepWithNext |= activeCell.keepWithNextSignal();
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
            if (penaltyOrGlueLen < 0) {
                returnList.add(new KnuthGlue(-penaltyOrGlueLen, 0, 0, new Position(null), true));
            }

            if (log.isDebugEnabled()) {
                log.debug("step=" + step + " (+" + increase + ")"
                        + " box=" + boxLen
                        + " penalty=" + penaltyOrGlueLen
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
        //Check for forced break conditions
        /*
        if (isBreakCondition()) {
            return -1;
        }*/

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
        // boolean will be reset
        boolean currentGridRowFinished = true;
        for (Iterator iter = activeCells.iterator(); iter.hasNext();) {
            ActiveCell activeCell = (ActiveCell) iter.next();
            if (activeCell.endsOnRow(activeRowIndex)) {
                currentGridRowFinished &= activeCell.isFinished();
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
}
