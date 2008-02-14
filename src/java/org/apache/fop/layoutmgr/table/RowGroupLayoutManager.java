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

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.table.EffRow;
import org.apache.fop.fo.flow.table.GridUnit;
import org.apache.fop.fo.flow.table.PrimaryGridUnit;
import org.apache.fop.fo.flow.table.TableRow;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.layoutmgr.BreakElement;
import org.apache.fop.layoutmgr.ElementListObserver;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.MinOptMaxUtil;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.util.BreakUtil;

class RowGroupLayoutManager {

    private static Log log = LogFactory.getLog(TableContentLayoutManager.class);

    private EffRow[] rowGroup;

    private TableLayoutManager tableLM;

    private TableStepper tableStepper;

    RowGroupLayoutManager(TableLayoutManager tableLM, EffRow[] rowGroup,
            TableStepper tableStepper) {
        this.tableLM = tableLM;
        this.rowGroup = rowGroup;
        this.tableStepper = tableStepper;
    }

    /**
     * 
     * @return one of {@link Constants#EN_AUTO}, {@link Constants#EN_COLUMN},
     * {@link Constants#EN_PAGE}, {@link Constants#EN_EVEN_PAGE}, or
     * {@link Constants#EN_ODD_PAGE}
     */
    int getBreakBefore() {
        TableRow rowFO = rowGroup[0].getTableRow();
        int breakBefore;
        if (rowFO == null) {
            breakBefore = Constants.EN_AUTO;
        } else {
            breakBefore = rowFO.getBreakBefore(); 
        }
        return BreakUtil.compareBreakClasses(breakBefore, rowGroup[0].getBreakBefore());
    }

    /**
     * 
     * @return one of {@link Constants#EN_AUTO}, {@link Constants#EN_COLUMN},
     * {@link Constants#EN_PAGE}, {@link Constants#EN_EVEN_PAGE}, or
     * {@link Constants#EN_ODD_PAGE}
     */
    int getBreakAfter() {
        TableRow rowFO = rowGroup[rowGroup.length - 1].getTableRow();
        int breakAfter;
        if (rowFO == null) {
            breakAfter = Constants.EN_AUTO;
        } else {
            breakAfter = rowFO.getBreakAfter(); 
        }
        return BreakUtil.compareBreakClasses(breakAfter,
                rowGroup[rowGroup.length - 1].getBreakAfter());
    }

    public LinkedList getNextKnuthElements(LayoutContext context, int alignment, int bodyType) {
        LinkedList returnList = new LinkedList();

        //Reset keep-with-next when remaining inside the table.
        //The context flag is only used to propagate keep-with-next to the outside.
        //The clearing is ok here because createElementsForRowGroup already handles
        //the keep when inside a table.
        context.setFlags(LayoutContext.KEEP_WITH_NEXT_PENDING, false);

        //Element list creation
        createElementsForRowGroup(context, alignment, bodyType, returnList);

        //Handle keeps
        if (context.isKeepWithNextPending()) {
            log.debug("child LM (row group) signals pending keep-with-next");
        }
        if (context.isKeepWithPreviousPending()) {
            log.debug("child LM (row group) signals pending keep-with-previous");
            if (returnList.size() > 0) {
                //Modify last penalty
                ListElement last = (ListElement)returnList.getLast();
                if (last.isPenalty()) {
                    BreakElement breakPoss = (BreakElement)last;
                    //Only honor keep if there's no forced break
                    if (!breakPoss.isForcedBreak()) {
                        breakPoss.setPenaltyValue(KnuthPenalty.INFINITE);
                    }
                }
            }
        }

        return returnList;
    }

    /**
     * Creates Knuth elements for a row group (see TableRowIterator.getNextRowGroup()).
     * @param context Active LayoutContext
     * @param alignment alignment indicator
     * @param bodyType Indicates what kind of body is being processed (BODY, HEADER or FOOTER)
     * @param returnList List to received the generated elements
     * @param rowGroup row group to process
     */
    private void createElementsForRowGroup(LayoutContext context, int alignment, 
            int bodyType, LinkedList returnList) {
        log.debug("Handling row group with " + rowGroup.length + " rows...");
        MinOptMax[] rowHeights = new MinOptMax[rowGroup.length];
        MinOptMax[] explicitRowHeights = new MinOptMax[rowGroup.length];
        EffRow row;
        List pgus = new java.util.ArrayList(); //holds a list of a row's primary grid units
        for (int rgi = 0; rgi < rowGroup.length; rgi++) {
            row = rowGroup[rgi];
            rowHeights[rgi] = new MinOptMax(0, 0, Integer.MAX_VALUE);
            explicitRowHeights[rgi] = new MinOptMax(0, 0, Integer.MAX_VALUE);
            
            pgus.clear();
            TableRow tableRow = null;
            // The row's minimum content height; 0 if the row's height is auto, otherwise
            // the .minimum component of the explicitly specified value
            int minRowBPD = 0;
            // The BPD of the biggest cell in the row
            int maxCellBPD = 0;
            for (int j = 0; j < row.getGridUnits().size(); j++) {
                GridUnit gu = row.getGridUnit(j);
                if ((gu.isPrimary() || (gu.getColSpanIndex() == 0 && gu.isLastGridUnitRowSpan())) 
                        && !gu.isEmpty()) {
                    PrimaryGridUnit primary = gu.getPrimary();
                    
                    if (gu.isPrimary()) {
                        primary.createCellLM(); // TODO a new LM must be created for every new static-content
                        primary.getCellLM().setParent(tableLM);
                     
                        //Determine the table-row if any
                        if (tableRow == null && primary.getRow() != null) {
                            tableRow = primary.getRow();
                            
                            //Check for bpd on row, see CSS21, 17.5.3 Table height algorithms
                            LengthRangeProperty rowBPD = tableRow.getBlockProgressionDimension();
                            if (!rowBPD.getMinimum(tableLM).isAuto()) {
                                minRowBPD = Math.max(minRowBPD,
                                        rowBPD.getMinimum(tableLM).getLength().getValue(tableLM));
                            }
                            MinOptMaxUtil.restrict(explicitRowHeights[rgi], rowBPD, tableLM);
                            
                        }

                        //Calculate width of cell
                        int spanWidth = 0;
                        for (int i = primary.getColIndex(); 
                                i < primary.getColIndex() 
                                        + primary.getCell().getNumberColumnsSpanned();
                                i++) {
                            if (tableLM.getColumns().getColumn(i + 1) != null) {
                                spanWidth += tableLM.getColumns().getColumn(i + 1)
                                    .getColumnWidth().getValue(tableLM);
                            }
                        }
                        LayoutContext childLC = new LayoutContext(0);
                        childLC.setStackLimit(context.getStackLimit()); //necessary?
                        childLC.setRefIPD(spanWidth);
                        
                        //Get the element list for the cell contents
                        LinkedList elems = primary.getCellLM().getNextKnuthElements(
                                                childLC, alignment);
                        ElementListObserver.observe(elems, "table-cell", primary.getCell().getId());

                        if ((elems.size() > 0) 
                                && ((KnuthElement)elems.getLast()).isForcedBreak()) {
                            // a descendant of this block has break-after
                            log.debug("Descendant of table-cell signals break: " 
                                    + primary.getCellLM().isFinished());
                        }
                        
                        primary.setElements(elems);
                        
                        if (childLC.isKeepWithNextPending()) {
                            log.debug("child LM signals pending keep-with-next");
                            primary.setFlag(GridUnit.KEEP_WITH_NEXT_PENDING, true);
                        }
                        if (childLC.isKeepWithPreviousPending()) {
                            log.debug("child LM signals pending keep-with-previous");
                            primary.setFlag(GridUnit.KEEP_WITH_PREVIOUS_PENDING, true);
                        }
                    }

                    //Calculate height of row, see CSS21, 17.5.3 Table height algorithms
                    if (gu.isLastGridUnitRowSpan()) {
                        // The effective cell's bpd, after taking into account bpd
                        // (possibly explicitly) set on the row or on the cell, and the
                        // cell's content length
                        int effectiveCellBPD = minRowBPD;
                        LengthRangeProperty cellBPD = primary.getCell()
                                .getBlockProgressionDimension();
                        if (!cellBPD.getMinimum(tableLM).isAuto()) {
                            effectiveCellBPD = Math.max(effectiveCellBPD,
                                    cellBPD.getMinimum(tableLM).getLength().getValue(tableLM));
                        }
                        if (!cellBPD.getOptimum(tableLM).isAuto()) {
                            effectiveCellBPD = Math.max(effectiveCellBPD,
                                    cellBPD.getOptimum(tableLM).getLength().getValue(tableLM));
                        }
                        if (gu.getRowSpanIndex() == 0) {
                            //TODO ATM only non-row-spanned cells are taken for this
                            MinOptMaxUtil.restrict(explicitRowHeights[rgi], cellBPD, tableLM);
                        }
                        effectiveCellBPD = Math.max(effectiveCellBPD, 
                                primary.getContentLength());
                        
                        int borderWidths = primary.getBeforeAfterBorderWidth();
                        int padding = 0;
                        maxCellBPD = Math.max(maxCellBPD, effectiveCellBPD);
                        CommonBorderPaddingBackground cbpb 
                            = primary.getCell().getCommonBorderPaddingBackground(); 
                        padding += cbpb.getPaddingBefore(false, primary.getCellLM());
                        padding += cbpb.getPaddingAfter(false, primary.getCellLM());
                        int effRowHeight = effectiveCellBPD 
                                + padding + borderWidths;
                        for (int previous = 0; previous < gu.getRowSpanIndex(); previous++) {
                            effRowHeight -= rowHeights[rgi - previous - 1].opt;
                        }
                        if (effRowHeight > rowHeights[rgi].min) {
                            //This is the new height of the (grid) row
                            MinOptMaxUtil.extendMinimum(rowHeights[rgi], effRowHeight, false);
                        }
                    }
                    
                    if (gu.isPrimary()) {
                        pgus.add(primary);
                    }
                }
            }

            row.setHeight(rowHeights[rgi]);
            row.setExplicitHeight(explicitRowHeights[rgi]);
            if (maxCellBPD > row.getExplicitHeight().max) {
                log.warn(FONode.decorateWithContextInfo(
                        "The contents of row " + (row.getIndex() + 1) 
                        + " are taller than they should be (there is a"
                        + " block-progression-dimension or height constraint on the indicated row)."
                        + " Due to its contents the row grows"
                        + " to " + maxCellBPD + " millipoints, but the row shouldn't get"
                        + " any taller than " + row.getExplicitHeight() + " millipoints.", 
                        row.getTableRow()));
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("rowGroup:");
            for (int i = 0; i < rowHeights.length; i++) {
                log.debug("  height=" + rowHeights[i] + " explicit=" + explicitRowHeights[i]);
            }
        }
        LinkedList returnedList = tableStepper.getCombinedKnuthElementsForRowGroup(context,
                rowGroup, bodyType);
        if (returnedList != null) {
            returnList.addAll(returnedList);
        }
        
    }
}
