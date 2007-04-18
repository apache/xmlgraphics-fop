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

import java.util.Arrays;
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

    /** Logger **/
    private static Log log = LogFactory.getLog(TableStepper.class);

    private TableContentLayoutManager tclm;
    
    private EffRow[] rowGroup;
    /** Number of columns in the row group. */
    private int columnCount;
    private int totalHeight;
    private int activeRowIndex;
    /**
     * Knuth elements for active cells, per column. Active cells are cells spanning over
     * the currently active row.
     */
    private List[] elementLists;
    /**
     * Number of the row where the row-span begins, per column. Zero-based.
     */
    private int[] startRow;
    /**
     * For each column, index, in the cell's list of Knuth elements, of the element
     * starting the current step.
     */
    private int[] start;
    /**
     * For each column, index, in the cell's list of Knuth elements, of the element
     * ending the current step.
     */
    private int[] end;
    /**
     * For each column, widths of the Knuth elements already included in the steps, up to
     * the current one.
     */
    private int[] widths;
    /**
     * ?? Width from the start of the row-group up to the current row.
     */
    private int[] baseWidth;
    private int[] borderBefore;
    private int[] paddingBefore;
    private int[] borderAfter;
    private int[] paddingAfter;
    private boolean rowBacktrackForLastStep;
    private boolean skippedStep;
    private boolean[] keepWithNextSignals;
    private boolean forcedBreak;
    private int lastMaxPenaltyLength;
    
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
        elementLists = new List[columnCount];
        startRow = new int[columnCount];
        start = new int[columnCount];
        end = new int[columnCount];
        widths = new int[columnCount];
        baseWidth = new int[columnCount];
        borderBefore = new int[columnCount];
        paddingBefore = new int[columnCount];
        borderAfter = new int[columnCount];
        paddingAfter = new int[columnCount];
        keepWithNextSignals = new boolean[columnCount];
        Arrays.fill(end, -1);
    }
    
    private void clearBreakCondition() {
        forcedBreak = false;
    }
    
    private boolean isBreakCondition() {
        return forcedBreak;
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
    
    private PrimaryGridUnit getActivePrimaryGridUnit(int column) {
        GridUnit gu = getActiveGridUnit(column);
        if (gu == null) {
            return null;
        } else {
            return gu.getPrimary();
        }
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
            for (int i = 0; i < columnCount; i++) {
                if (elementLists[i] == null) {
                    continue;
                }
                if (end[i] == elementLists[i].size() - 1) {
                    continue;
                }
                GridUnit gu = getActiveGridUnit(i); 
                if (!gu.isLastGridUnitRowSpan()) {
                    continue;
                }
                int len = widths[i]; 
                if (len > 0) {
                    len += 2 * getTableLM().getHalfBorderSeparationBPD();
                    len += borderBefore[i] + borderAfter[i]; 
                    len += paddingBefore[i] + paddingAfter[i]; 
                }
                int nominalHeight = rowGroup[activeRowIndex].getHeight().opt;
                for (int r = 0; r < gu.getRowSpanIndex(); r++) {
                    nominalHeight += rowGroup[activeRowIndex - r - 1].getHeight().opt;
                }
                if (len == nominalHeight) {
                    //row is filled
                    maxW = 0;
                    break;
                }
                maxW = Math.max(maxW, nominalHeight - len);
            }
        }
        for (int i = activeRowIndex + 1; i < rowGroup.length; i++) {
            maxW += rowGroup[i].getHeight().opt;
        }
        log.debug("maxRemainingHeight=" + maxW);
        return maxW;
    }

    private void setupElementList(int column) {
        GridUnit gu = getActiveGridUnit(column);
        EffRow row = getActiveRow();
        if (gu == null || gu.isEmpty()) {
            elementLists[column] = null;
            start[column] = 0;
            end[column] = -1;
            widths[column] = 0;
            startRow[column] = activeRowIndex;
            keepWithNextSignals[column] = false;
        } else if (gu.isPrimary()) {
            PrimaryGridUnit pgu = (PrimaryGridUnit)gu;
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
                List list = new java.util.ArrayList(1);
                int height = row.getExplicitHeight().opt;
                if (height == 0) {
                    height = row.getHeight().opt;
                }
                list.add(new KnuthBoxCellWithBPD(height));
                elementLists[column] = list;
            } else {
                //Copy elements (LinkedList) to array lists to improve 
                //element access performance
                elementLists[column] = new java.util.ArrayList(pgu.getElements());
                if (log.isTraceEnabled()) {
                    log.trace("column " + (column+1) + ": recording " + elementLists[column].size() + " element(s)");
                }
            }
            if (isSeparateBorderModel()) {
                borderBefore[column] = pgu.getBorders().getBorderBeforeWidth(false);
            } else {
                borderBefore[column] = pgu.getBorders().getBorderBeforeWidth(false) / 2;
                if (log.isTraceEnabled()) {
                    log.trace("border before for column " + column + ": " + borderBefore[column]);
                }
            }
            paddingBefore[column] = pgu.getBorders().getPaddingBefore(false, pgu.getCellLM());
            paddingAfter[column] = pgu.getBorders().getPaddingAfter(false, pgu.getCellLM());
            start[column] = 0;
            end[column] = -1;
            widths[column] = 0;
            startRow[column] = activeRowIndex;
            keepWithNextSignals[column] = false;
        }
    }

    /**
     * Initializes the informations relative to the Knuth elements, to handle a new row in
     * the current row group.
     */
    private void initializeElementLists() {
        log.trace("Entering initializeElementLists()");
        for (int i = 0; i < start.length; i++) {
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
            if (rowBacktrackForLastStep) {
                //Even though we've already switched to the next row, we have to 
                //calculate as if we were still on the previous row
                activeRowIndex--;
            }
            int increase = step - laststep;
            int penaltyLen = step + getMaxRemainingHeight() - totalHeight;
            int boxLen = step - addedBoxLen - penaltyLen;
            addedBoxLen += boxLen;

            //Put all involved grid units into a list
            List gridUnitParts = new java.util.ArrayList(maxColumnCount);
            for (int i = 0; i < columnCount; i++) {
                if (end[i] >= start[i]) {
                    PrimaryGridUnit pgu = rowGroup[startRow[i]].getGridUnit(i).getPrimary();
                    if (start[i] == 0 && end[i] == 0 
                            && elementLists[i].size() == 1
                            && elementLists[i].get(0) instanceof KnuthBoxCellWithBPD) {
                        //Special case: Cell with fixed BPD
                        gridUnitParts.add(new GridUnitPart(pgu, 
                                0, pgu.getElements().size() - 1));
                    } else {
                        gridUnitParts.add(new GridUnitPart(pgu, start[i], end[i]));
                    }
                    if (end[i] + 1 == elementLists[i].size()) {
                        if (pgu.getFlag(GridUnit.KEEP_WITH_NEXT_PENDING)) {
                            log.debug("PGU has pending keep-with-next");
                            keepWithNextSignals[i] = true;
                        }
                        if (pgu.getRow() != null && pgu.getRow().mustKeepWithNext()) {
                            log.debug("table-row causes keep-with-next");
                            keepWithNextSignals[i] = true;
                        }
                    }
                    if (start[i] == 0 && end[i] >= 0) {
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
            for (int i = 0; i < columnCount; i++) {
                if (start[i] == 0 && end[i] < 0 && elementLists[i] != null) {
                    allCellsHaveContributed = false;
                }
                signalKeepWithNext |= keepWithNextSignals[i];
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
            if (isBreakCondition()) {
                if (skippedStep) {
                    log.error("This is a conflict situation. The output may be wrong." 
                            + " Please send your FO file to fop-dev@xmlgraphics.apache.org!");
                }
                p = -KnuthPenalty.INFINITE; //Overrides any keeps (see 4.8 in XSL 1.0)
                clearBreakCondition();
            }
            returnList.add(new BreakElement(penaltyPos, effPenaltyLen, p, -1, context));

            if (log.isDebugEnabled()) {
                log.debug("step=" + step + " (+" + increase + ")"
                        + " box=" + boxLen 
                        + " penalty=" + penaltyLen
                        + " effPenalty=" + effPenaltyLen);
            }
            
            laststep = step;
            if (rowBacktrackForLastStep) {
                //If row was set to previous, restore now
                activeRowIndex++;
            }
        }
        if (signalKeepWithNext) {
            //Last step signalled a keep-with-next. Since the last penalty will be removed,
            //we have to signal the still pending last keep-with-next using the LayoutContext.
            context.setFlags(LayoutContext.KEEP_WITH_NEXT_PENDING);
        }
        if (isBreakCondition()) {
            ((BreakElement)returnList.getLast()).setPenaltyValue(-KnuthPenalty.INFINITE);
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
        
        int[] backupWidths = new int[columnCount];
        System.arraycopy(widths, 0, backupWidths, 0, columnCount);

        //set starting points
        // We assume that the current grid row is finished. If this is not the case this
        // boolean will be reset (see below)
        boolean currentGridRowFinished = true;
        for (int i = 0; i < columnCount; i++) {
            // null element lists probably correspond to empty cells
            if (elementLists[i] == null) {
                continue;
            }
            if (end[i] < elementLists[i].size()) {
                start[i] = end[i] + 1;
                if (end[i] + 1 < elementLists[i].size() 
                        && getActiveGridUnit(i).isLastGridUnitRowSpan()) {
                    // Ok, so this grid unit is the last in the row-spanning direction and
                    // there are still unhandled Knuth elements. They /will/ have to be
                    // put on the current grid row, which means that this row isn't
                    // finished yet
                    currentGridRowFinished = false;
                }
            } else {
                throw new IllegalStateException("end[i] overflows elementList[i].size()");
//                start[i] = -1; //end of list reached
//                end[i] = -1;
            }
        }

        if (currentGridRowFinished) {
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
                for (int i = 0; i < columnCount; i++) {
                    if (end[i] < 0) {
                        backupWidths[i] = 0;
                    }
                }
                rowFO = getActiveRow().getTableRow();
                if (rowFO != null && rowFO.getBreakBefore() != Constants.EN_AUTO) {
                    log.warn(FONode.decorateWithContextInfo(
                            "break-before ignored on table-row because of row spanning "
                            + "in progress (See XSL 1.0, 7.19.2)", rowFO));
                }
            }
        }

        //Get next possible sequence for each cell
        boolean stepFound = false;
        for (int i = 0; i < columnCount; i++) {
            if (elementLists[i] == null) {
                continue;
            }
            while (end[i] + 1 < elementLists[i].size()) {
                end[i]++;
                KnuthElement el = (KnuthElement)elementLists[i].get(end[i]);
                if (el.isPenalty()) {
                    this.lastMaxPenaltyLength = Math.max(this.lastMaxPenaltyLength, el.getW());
                    if (el.getP() <= -KnuthElement.INFINITE) {
                        log.debug("FORCED break encountered!");
                        forcedBreak = true;
                        break;
                    } else if (el.getP() < KnuthElement.INFINITE) {
                        //First legal break point
                        break;
                    }
                } else if (el.isGlue()) {
                    if (end[i] > 0) {
                        KnuthElement prev = (KnuthElement)elementLists[i].get(end[i] - 1);
                        if (prev.isBox()) {
                            //Second legal break point
                            break;
                        }
                    }
                    widths[i] += el.getW();
                } else {
                    widths[i] += el.getW();
                }
            }
            if (end[i] < start[i]) {
                if (log.isTraceEnabled()) {
                    log.trace("column " + (i + 1) + ": (end=" + end[i] + ") < (start=" + start[i]
                            + ") => resetting width to backupWidth");
                }
                widths[i] = backupWidths[i];
            } else {
                stepFound = true;
            }
            //log.debug("part " + start[i] + "-" + end[i] + " " + widths[i]);
            if (end[i] + 1 >= elementLists[i].size()) {
                //element list for this cell is finished
                if (isSeparateBorderModel()) {
                    borderAfter[i] = getActivePrimaryGridUnit(i)
                            .getBorders().getBorderAfterWidth(false);
                } else {
                    borderAfter[i] = getActivePrimaryGridUnit(i).getHalfMaxAfterBorderWidth();
                }
            } else {
                //element list for this cell is not finished
                if (isSeparateBorderModel()) {
                    borderAfter[i] = getActivePrimaryGridUnit(i)
                            .getBorders().getBorderAfterWidth(false);
                } else {
                    //TODO fix me!
                    borderAfter[i] = getActivePrimaryGridUnit(i).getHalfMaxAfterBorderWidth();
                }
            }
            if (log.isTraceEnabled()) {
                log.trace("column " + (i+1) + ": borders before=" + borderBefore[i] + " after=" + borderAfter[i]);
                log.trace("column " + (i+1) + ": padding before=" + paddingBefore[i] + " after=" + paddingAfter[i]);
            }
        }
        if (stepFound) {
            return -1;
        }

        //Determine smallest possible step
        int minStep = Integer.MAX_VALUE;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < columnCount; i++) {
            baseWidth[i] = 0;
            for (int prevRow = 0; prevRow < startRow[i]; prevRow++) {
                baseWidth[i] += rowGroup[prevRow].getHeight().opt;
            }
            baseWidth[i] += 2 * getTableLM().getHalfBorderSeparationBPD();
            baseWidth[i] += borderBefore[i] + borderAfter[i];
            baseWidth[i] += paddingBefore[i] + paddingAfter[i];
            if (end[i] >= start[i]) {
                int len = baseWidth[i] + widths[i];
                sb.append(len + " ");
                minStep = Math.min(len, minStep);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("candidate steps: " + sb);
        }

        //Check for constellations that would result in overlapping borders
        /*
        for (int i = 0; i < columnCount; i++) {
            
        }*/
        
        //Reset bigger-than-minimum sequences
        //See http://people.apache.org/~jeremias/fop/NextStepAlgoNotes.pdf
        rowBacktrackForLastStep = false;
        skippedStep = false;
        for (int i = 0; i < columnCount; i++) {
            int len = baseWidth[i] + widths[i];
            if (len > minStep) {
                widths[i] = backupWidths[i];
                end[i] = start[i] - 1;
                if (baseWidth[i] + widths[i] > minStep) {
                    if (log.isDebugEnabled()) {
                        log.debug("column "
                                + (i + 1)
                                + ": minStep vs. border/padding increase conflict: basewidth + width = "
                                + baseWidth[i] + " + " + widths[i] + " = "
                                + (baseWidth[i] + widths[i]));                        
                    }
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
        }
        if (log.isDebugEnabled()) {
            /*StringBuffer*/ sb = new StringBuffer("[col nb: start-end(width)] ");
            for (int i = 0; i < columnCount; i++) {
                if (end[i] >= start[i]) {
                    sb.append(i + ": " + start[i] + "-" + end[i] + "(" + widths[i] + "), ");
                } else {
                    sb.append(i + ": skip, ");
                }
            }
            log.debug(sb.toString());
        }

        return minStep;
    }
    
    
    /** @return true if the table uses the separate border model. */
    private boolean isSeparateBorderModel() {
        return getTableLM().getTable().isSeparateBorderModel();
    }

    /** @return the table layout manager */
    private TableLayoutManager getTableLM() {
        return this.tclm.getTableLM();
    }

    /**
     * Marker class denoting table cells fitting in just one box (no legal break inside).
     */
    private class KnuthBoxCellWithBPD extends KnuthBox {
        
        public KnuthBoxCellWithBPD(int w) {
            super(w, null, true);
        }
    }
    
}
