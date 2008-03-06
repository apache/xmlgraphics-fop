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
import org.apache.fop.layoutmgr.ElementListObserver;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.MinOptMaxUtil;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.util.BreakUtil;

class RowGroupLayoutManager {

    private static Log log = LogFactory.getLog(RowGroupLayoutManager.class);

    private EffRow[] rowGroup;

    private TableLayoutManager tableLM;

    private TableStepper tableStepper;

    RowGroupLayoutManager(TableLayoutManager tableLM, EffRow[] rowGroup,
            TableStepper tableStepper) {
        this.tableLM = tableLM;
        this.rowGroup = rowGroup;
        this.tableStepper = tableStepper;
    }

    public LinkedList getNextKnuthElements(LayoutContext context, int alignment, int bodyType) {
        LinkedList returnList = new LinkedList();
        createElementsForRowGroup(context, alignment, bodyType, returnList);

        context.setFlags(LayoutContext.KEEP_WITH_PREVIOUS_PENDING,
                rowGroup[0].mustKeepWithPrevious());
        context.setFlags(LayoutContext.KEEP_WITH_NEXT_PENDING,
                rowGroup[rowGroup.length - 1].mustKeepWithNext());

        int breakBefore = Constants.EN_AUTO;
        TableRow firstRow = rowGroup[0].getTableRow();
        if (firstRow != null) {
            breakBefore = firstRow.getBreakBefore(); 
        }
        context.setBreakBefore(BreakUtil.compareBreakClasses(breakBefore,
                rowGroup[0].getBreakBefore()));

        int breakAfter = Constants.EN_AUTO;
        TableRow lastRow = rowGroup[rowGroup.length - 1].getTableRow();
        if (lastRow != null) {
            breakAfter = lastRow.getBreakAfter(); 
        }
        context.setBreakAfter(BreakUtil.compareBreakClasses(breakAfter,
                rowGroup[rowGroup.length - 1].getBreakAfter()));

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
                        // TODO a new LM must be created for every new static-content
                        primary.createCellLM();
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
                        primary.setElements(elems);
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
        LinkedList elements = tableStepper.getCombinedKnuthElementsForRowGroup(context,
                rowGroup, bodyType);
        returnList.addAll(elements);
    }
}
