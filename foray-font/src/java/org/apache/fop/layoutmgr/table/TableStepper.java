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
import org.apache.fop.layoutmgr.table.TableContentLayoutManager.GridUnitPart;
import org.apache.fop.layoutmgr.table.TableContentLayoutManager.TableContentPosition;
import org.apache.fop.layoutmgr.table.TableContentLayoutManager.TableHFPenaltyPosition;

/**
 * This class processes row groups to create combined element lists for tables.
 */
public class TableStepper {

    /** Logger **/
    private static Log log = LogFactory.getLog(TableStepper.class);

    private TableContentLayoutManager tclm;
    
    private EffRow[] rowGroup;
    private int totalHeight;
    private int activeRow;
    private List[] elementLists;
    private int[] startRow;
    private int[] start;
    private int[] end;
    private int[] widths;
    private int[] baseWidth;
    private int[] borderBefore;
    private int[] paddingBefore;
    private int[] borderAfter;
    private int[] paddingAfter;
    private boolean rowBacktrackForLastStep;
    private boolean skippedStep;
    private boolean[] keepWithNextSignals;
    private boolean[] forcedBreaks;
    
    /**
     * Main constructor
     * @param tclm The parent TableContentLayoutManager
     */
    public TableStepper(TableContentLayoutManager tclm) {
        this.tclm = tclm;
    }
    
    private void setup(int columnCount) {
        this.activeRow = 0;
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
        forcedBreaks = new boolean[columnCount];
        Arrays.fill(end, -1);
    }
    
    private void clearBreakCondition() {
        Arrays.fill(forcedBreaks, false);
    }
    
    private boolean isBreakCondition() {
        for (int i = 0; i < forcedBreaks.length; i++) {
            if (forcedBreaks[i]) {
                return true;
            }
        }
        return false;
    }
    
    private EffRow getActiveRow() {
        return rowGroup[activeRow];
    }
    
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
        log.debug("totalHeight=" + totalHeight);
    }
    
    private int getMaxRemainingHeight() {
        int maxW = 0;
        if (!rowBacktrackForLastStep) {
            for (int i = 0; i < widths.length; i++) {
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
                int nominalHeight = rowGroup[activeRow].getHeight().opt;
                for (int r = 0; r < gu.getRowSpanIndex(); r++) {
                    nominalHeight += rowGroup[activeRow - r - 1].getHeight().opt;
                }
                if (len == nominalHeight) {
                    //row is filled
                    maxW = 0;
                    break;
                }
                maxW = Math.max(maxW, nominalHeight - len);
            }
        }
        for (int i = activeRow + 1; i < rowGroup.length; i++) {
            maxW += rowGroup[i].getHeight().opt;
        }
        //log.debug("maxRemainingHeight=" + maxW);
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
            startRow[column] = activeRow;
            keepWithNextSignals[column] = false;
            forcedBreaks[column] = false;
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
                list.add(new KnuthBoxCellWithBPD(height, pgu));
                elementLists[column] = list;
            } else {
                //Copy elements (LinkedList) to array lists to improve 
                //element access performance
                elementLists[column] = new java.util.ArrayList(pgu.getElements());
            }
            if (isSeparateBorderModel()) {
                borderBefore[column] = pgu.getBorders().getBorderBeforeWidth(false);
            } else {
                borderBefore[column] = pgu.getBorders().getBorderBeforeWidth(false) / 2;
            }
            paddingBefore[column] = pgu.getBorders().getPaddingBefore(false, pgu.getCellLM());
            paddingAfter[column] = pgu.getBorders().getPaddingAfter(false, pgu.getCellLM());
            start[column] = 0;
            end[column] = -1;
            widths[column] = 0;
            startRow[column] = activeRow;
            keepWithNextSignals[column] = false;
            forcedBreaks[column] = false;
        }
    }
    
    private void initializeElementLists() {
        for (int i = 0; i < start.length; i++) {
            setupElementList(i);
        }
    }

    /**
     * Creates the combined element list for a row group.
     * @param context Active LayoutContext
     * @param rowGroup the row group
     * @param maxColumnCount the maximum number of columns to expect
     * @param bodyType Indicates what type of body is processed (boder, header or footer)
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
        while ((step = getNextStep(laststep)) >= 0) {
            int normalRow = activeRow;
            if (rowBacktrackForLastStep) {
                //Even though we've already switched to the next row, we have to 
                //calculate as if we were still on the previous row
                activeRow--;
            }
            int increase = step - laststep;
            int penaltyLen = step + getMaxRemainingHeight() - totalHeight;
            int boxLen = step - addedBoxLen - penaltyLen;
            addedBoxLen += boxLen;
            
            //Put all involved grid units into a list
            List gridUnitParts = new java.util.ArrayList(maxColumnCount);
            for (int i = 0; i < start.length; i++) {
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
                        + " - row=" + activeRow + " - " + tcpos);
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
            int p = 0;
            boolean allCellsHaveContributed = true;
            signalKeepWithNext = false;
            for (int i = 0; i < start.length; i++) {
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
            //returnList.add(new KnuthPenalty(effPenaltyLen, p, false, penaltyPos, false));
            returnList.add(new BreakElement(penaltyPos, effPenaltyLen, p, -1, context));

            log.debug("step=" + step + " (+" + increase + ")"
                    + " box=" + boxLen 
                    + " penalty=" + penaltyLen
                    + " effPenalty=" + effPenaltyLen);
            
            laststep = step;
            if (rowBacktrackForLastStep) {
                //If row was set to previous, restore now
                activeRow++;
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
    
    private int getNextStep(int lastStep) {
        //Check for forced break conditions
        /*
        if (isBreakCondition()) {
            return -1;
        }*/
        
        int[] backupWidths = new int[start.length];
        System.arraycopy(widths, 0, backupWidths, 0, backupWidths.length);

        //set starting points
        int rowPendingIndicator = 0;
        for (int i = 0; i < start.length; i++) {
            if (elementLists[i] == null) {
                continue;
            }
            if (end[i] < elementLists[i].size()) {
                start[i] = end[i] + 1;
                if (end[i] + 1 < elementLists[i].size() 
                        && getActiveGridUnit(i).isLastGridUnitRowSpan()) {
                    rowPendingIndicator++;
                }
            } else {
                start[i] = -1; //end of list reached
                end[i] = -1;
            }
        }

        if (rowPendingIndicator == 0) {
            if (activeRow < rowGroup.length - 1) {
                TableRow rowFO = getActiveRow().getTableRow();
                if (rowFO != null && rowFO.getBreakAfter() != Constants.EN_AUTO) {
                    log.warn(FONode.decorateWithContextInfo(
                            "break-after ignored on table-row because of row spanning "
                            + "in progress (See XSL 1.0, 7.19.1)", rowFO));
                }
                activeRow++;
                if (log.isDebugEnabled()) {
                    log.debug("===> new row: " + activeRow);
                }
                initializeElementLists();
                for (int i = 0; i < backupWidths.length; i++) {
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
        int seqCount = 0;
        for (int i = 0; i < start.length; i++) {
            if (elementLists[i] == null) {
                continue;
            }
            while (end[i] + 1 < elementLists[i].size()) {
                end[i]++;
                KnuthElement el = (KnuthElement)elementLists[i].get(end[i]);
                if (el.isPenalty()) {
                    if (el.getP() <= -KnuthElement.INFINITE) {
                        log.debug("FORCED break encountered!");
                        forcedBreaks[i] = true;
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
                widths[i] = backupWidths[i];
            } else {
                seqCount++;
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
                log.trace("borders before=" + borderBefore[i] + " after=" + borderAfter[i]);
                log.trace("padding before=" + paddingBefore[i] + " after=" + paddingAfter[i]);
            }
        }
        if (seqCount == 0) {
            return -1;
        }

        //Determine smallest possible step
        int minStep = Integer.MAX_VALUE;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < widths.length; i++) {
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
            log.debug("candidate steps: " + sb + " lastStep=" + lastStep);
        }

        //Check for constellations that would result in overlapping borders
        /*
        for (int i = 0; i < widths.length; i++) {
            
        }*/
        
        //Reset bigger-than-minimum sequences
        //See http://people.apache.org/~jeremias/fop/NextStepAlgoNotes.pdf
        rowBacktrackForLastStep = false;
        skippedStep = false;
        for (int i = 0; i < widths.length; i++) {
            int len = baseWidth[i] + widths[i];
            if (len > minStep) {
                widths[i] = backupWidths[i];
                end[i] = start[i] - 1;
                if (baseWidth[i] + widths[i] > minStep) {
                    log.debug("minStep vs. border/padding increase conflict:");
                    if (activeRow == 0) {
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
            /*StringBuffer*/ sb = new StringBuffer();
            for (int i = 0; i < widths.length; i++) {
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

    private class KnuthBoxCellWithBPD extends KnuthBox {
        
        private PrimaryGridUnit pgu;
        
        public KnuthBoxCellWithBPD(int w, PrimaryGridUnit pgu) {
            super(w, null, true);
            this.pgu = pgu;
        }
    }
    
}
