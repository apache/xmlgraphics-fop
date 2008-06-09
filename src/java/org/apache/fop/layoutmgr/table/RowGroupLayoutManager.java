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
import org.apache.fop.fo.flow.table.TableColumn;
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

        context.updateKeepWithPreviousPending(rowGroup[0].getKeepWithPreviousStrength());
        context.updateKeepWithNextPending(rowGroup[rowGroup.length - 1].getKeepWithNextStrength());

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
        EffRow row;
        for (int rgi = 0; rgi < rowGroup.length; rgi++) {
            row = rowGroup[rgi];
            for (Iterator iter = row.getGridUnits().iterator(); iter.hasNext();) {
                GridUnit gu = (GridUnit) iter.next();
                if (gu.isPrimary()) {
                    PrimaryGridUnit primary = gu.getPrimary();
                    // TODO a new LM must be created for every new static-content
                    primary.createCellLM();
                    primary.getCellLM().setParent(tableLM);
                    //Calculate width of cell
                    int spanWidth = 0;
                    Iterator colIter = tableLM.getTable().getColumns().listIterator(
                            primary.getColIndex());
                    for (int i = 0, c = primary.getCell().getNumberColumnsSpanned(); i < c; i++) {
                        spanWidth += ((TableColumn) colIter.next()).getColumnWidth().getValue(
                                tableLM);
                    }
                    LayoutContext childLC = new LayoutContext(0);
                    childLC.setStackLimitBP(context.getStackLimitBP()); //necessary?
                    childLC.setRefIPD(spanWidth);
                    
                    //Get the element list for the cell contents
                    List elems = primary.getCellLM().getNextKnuthElements(
                                            childLC, alignment);
                    ElementListObserver.observe(elems, "table-cell", primary.getCell().getId());
                    primary.setElements(elems);
                }
            }
        }
        computeRowHeights();
        List elements = tableStepper.getCombinedKnuthElementsForRowGroup(context,
                rowGroup, bodyType);
        returnList.addAll(elements);
    }

    /**
     * Calculate the heights of the rows in the row group, see CSS21, 17.5.3 Table height
     * algorithms.
     * 
     * TODO this method will need to be adapted once clarification has been made by the
     * W3C regarding whether borders or border-separation must be included or not
     */
    private void computeRowHeights() {
        log.debug("rowGroup:");
        MinOptMax[] rowHeights = new MinOptMax[rowGroup.length];
        EffRow row;
        for (int rgi = 0; rgi < rowGroup.length; rgi++) {
            row = rowGroup[rgi];
            // The BPD of the biggest cell in the row
//            int maxCellBPD = 0;
            MinOptMax explicitRowHeight;
            TableRow tableRowFO = rowGroup[rgi].getTableRow();
            if (tableRowFO == null) {
                rowHeights[rgi] = new MinOptMax(0, 0, Integer.MAX_VALUE);
                explicitRowHeight = new MinOptMax(0, 0, Integer.MAX_VALUE);
            } else {
                LengthRangeProperty rowBPD = tableRowFO.getBlockProgressionDimension();
                rowHeights[rgi] = MinOptMaxUtil.toMinOptMax(rowBPD, tableLM);
                explicitRowHeight = MinOptMaxUtil.toMinOptMax(rowBPD, tableLM);
            }
            for (Iterator iter = row.getGridUnits().iterator(); iter.hasNext();) {
                GridUnit gu = (GridUnit) iter.next();
                if (!gu.isEmpty() && gu.getColSpanIndex() == 0 && gu.isLastGridUnitRowSpan()) {
                    PrimaryGridUnit primary = gu.getPrimary();
                    int effectiveCellBPD = 0;
                    LengthRangeProperty cellBPD = primary.getCell().getBlockProgressionDimension();
                    if (!cellBPD.getMinimum(tableLM).isAuto()) {
                        effectiveCellBPD = cellBPD.getMinimum(tableLM).getLength()
                                .getValue(tableLM);
                    }
                    if (!cellBPD.getOptimum(tableLM).isAuto()) {
                        effectiveCellBPD = cellBPD.getOptimum(tableLM).getLength()
                                .getValue(tableLM);
                    }
                    if (gu.getRowSpanIndex() == 0) {
                        effectiveCellBPD = Math.max(effectiveCellBPD, explicitRowHeight.opt);
                    }
                    effectiveCellBPD = Math.max(effectiveCellBPD, primary.getContentLength());
                    int borderWidths = primary.getBeforeAfterBorderWidth();
                    int padding = 0;
                    CommonBorderPaddingBackground cbpb = primary.getCell()
                            .getCommonBorderPaddingBackground(); 
                    padding += cbpb.getPaddingBefore(false, primary.getCellLM());
                    padding += cbpb.getPaddingAfter(false, primary.getCellLM());
                    int effRowHeight = effectiveCellBPD + padding + borderWidths;
                    for (int prev = rgi - 1; prev >= rgi - gu.getRowSpanIndex(); prev--) {
                        effRowHeight -= rowHeights[prev].opt;
                    }
                    if (effRowHeight > rowHeights[rgi].min) {
                        // This is the new height of the (grid) row
                        MinOptMaxUtil.extendMinimum(rowHeights[rgi], effRowHeight);
                    }
                }
            }

            row.setHeight(rowHeights[rgi]);
            row.setExplicitHeight(explicitRowHeight);
            // TODO re-enable and improve after clarification
            //See http://markmail.org/message/h25ycwwu7qglr4k4
//            if (maxCellBPD > row.getExplicitHeight().max) {
//old:
//                log.warn(FONode.decorateWithContextInfo(
//                        "The contents of row " + (row.getIndex() + 1) 
//                        + " are taller than they should be (there is a"
//                        + " block-progression-dimension or height constraint
//                        + " on the indicated row)."
//                        + " Due to its contents the row grows"
//                        + " to " + maxCellBPD + " millipoints, but the row shouldn't get"
//                        + " any taller than " + row.getExplicitHeight() + " millipoints.", 
//                        row.getTableRow()));
//new (with events):
//                BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Factory.create(
//                        tableRow.getUserAgent().getEventBroadcaster());
//                eventProducer.rowTooTall(this, row.getIndex() + 1,
//                        maxCellBPD, row.getExplicitHeight().max, tableRow.getLocator());
//            }
        }
    }
}
