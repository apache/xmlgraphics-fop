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
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.layoutmgr.BreakElement;
import org.apache.fop.layoutmgr.ElementListObserver;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.MinOptMaxUtil;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.traits.MinOptMax;

class RowGroupLayoutManager {

    private static Log log = LogFactory.getLog(TableContentLayoutManager.class);

    private EffRow[] rowGroup;

    private TableLayoutManager tableLM;

    private TableRowIterator bodyIter;
    private TableRowIterator headerIter;
    private TableRowIterator footerIter;
    private TableRowIterator thisIter;
    private TableStepper tableStepper;

    RowGroupLayoutManager(TableLayoutManager tableLM, EffRow[] rowGroup, TableRowIterator bodyIter,
            TableRowIterator headerIter, TableRowIterator footerIter, TableRowIterator thisIter,
            TableStepper tableStepper) {
        this.tableLM = tableLM;
        this.rowGroup = rowGroup;
        this.bodyIter = bodyIter;
        this.headerIter = headerIter;
        this.footerIter = footerIter;
        this.thisIter = thisIter;
        this.tableStepper = tableStepper;
    }

    public LinkedList getNextKnuthElements(LayoutContext context, int alignment, int bodyType) {
        LinkedList returnList = new LinkedList();
            //Check for break-before on the table-row at the start of the row group
            TableRow rowFO = rowGroup[0].getTableRow(); 
            if (rowFO != null && rowFO.getBreakBefore() != Constants.EN_AUTO) {
                log.info("break-before found");
                if (returnList.size() > 0) {
                    ListElement last = (ListElement)returnList.getLast();
                    if (last.isPenalty()) {
                        KnuthPenalty pen = (KnuthPenalty)last;
                        pen.setP(-KnuthPenalty.INFINITE);
                        pen.setBreakClass(rowFO.getBreakBefore());
                    } else {//if (last instanceof BreakElement) { // TODO vh: seems the only possibility
                        BreakElement breakPoss = (BreakElement) last;
                        breakPoss.setPenaltyValue(-KnuthPenalty.INFINITE);
                        breakPoss.setBreakClass(rowFO.getBreakBefore());
                    }
                } else {
                    returnList.add(new BreakElement(new Position(tableLM),
                            0, -KnuthPenalty.INFINITE, rowFO.getBreakBefore(), context));
                }
            }
            
            //Border resolution
            if (!tableLM.getTable().isSeparateBorderModel()) {
                resolveNormalBeforeAfterBordersForRowGroup();
            }

            //Reset keep-with-next when remaining inside the table.
            //The context flag is only used to propagate keep-with-next to the outside.
            //The clearing is ok here because createElementsForRowGroup already handles
            //the keep when inside a table.
            context.setFlags(LayoutContext.KEEP_WITH_NEXT_PENDING, false);
            
            //Element list creation
            createElementsForRowGroup(context, alignment, bodyType, 
                        returnList, rowGroup);
            
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
            
            //Check for break-after on the table-row at the end of the row group
            rowFO = rowGroup[rowGroup.length - 1].getTableRow(); 
            if (rowFO != null && rowFO.getBreakAfter() != Constants.EN_AUTO) {
                if (returnList.size() > 0) {
                    ListElement last = (ListElement)returnList.getLast();
                    if (last instanceof KnuthPenalty) {
                        KnuthPenalty pen = (KnuthPenalty)last;
                        pen.setP(-KnuthPenalty.INFINITE);
                        pen.setBreakClass(rowFO.getBreakAfter());
                    } else if (last instanceof BreakElement) {
                        BreakElement breakPoss = (BreakElement)last;
                        breakPoss.setPenaltyValue(-KnuthPenalty.INFINITE);
                        breakPoss.setBreakClass(rowFO.getBreakAfter());
                    }
                }
            }
        return returnList;
    }

    /**
     * Resolves normal borders for a row group.
     * @param iter Table row iterator to operate on
     */
    private void resolveNormalBeforeAfterBordersForRowGroup() {
        for (int rgi = 0; rgi < rowGroup.length; rgi++) {
            EffRow row = rowGroup[rgi];
            EffRow prevRow = thisIter.getPrecedingRow(row);
            EffRow nextRow = thisIter.getFollowingRow(row);
            if ((prevRow == null) && (thisIter == bodyIter) && (headerIter != null)) {
                prevRow = headerIter.getLastRow();
            }
            if ((nextRow == null) && (thisIter == headerIter)) {
                nextRow = bodyIter.getFirstRow();
            }
            if ((nextRow == null) && (thisIter == bodyIter) && (footerIter != null)) {
                nextRow = footerIter.getFirstRow();
            }
            if ((prevRow == null) && (thisIter == footerIter)) {
                //TODO This could be bad for memory consumption because it already causes the
                //whole body iterator to be prefetched!
                prevRow = bodyIter.getLastRow();
            }
            log.debug("prevRow-row-nextRow: " + prevRow + " - " + row + " - " + nextRow);
            
            //Determine the grid units necessary for getting all the borders right
            int guCount = row.getGridUnits().size();
            if (prevRow != null) {
                guCount = Math.max(guCount, prevRow.getGridUnits().size());
            }
            if (nextRow != null) {
                guCount = Math.max(guCount, nextRow.getGridUnits().size());
            }
            GridUnit gu = row.getGridUnit(0);
            //Create empty grid units to hold resolved borders of neighbouring cells
            //TODO maybe this needs to be done differently (and sooner)
            for (int i = 0; i < guCount - row.getGridUnits().size(); i++) {
                //TODO This block is untested!
                int pos = row.getGridUnits().size() + i;
                row.getGridUnits().add(new EmptyGridUnit(gu.getRow(), 
                        tableLM.getColumns().getColumn(pos + 1), gu.getBody(), 
                        pos));
            }
            
            //Now resolve normal borders
            if (tableLM.getTable().isSeparateBorderModel()) {
                //nop, borders are already assigned at this point
            } else {
                for (int i = 0; i < row.getGridUnits().size(); i++) {
                    gu = row.getGridUnit(i);
                    GridUnit other;
                    int flags = 0;
                    if (prevRow != null && i < prevRow.getGridUnits().size()) {
                        other = prevRow.getGridUnit(i);
                    } else {
                        other = null;
                    }
                    if (other == null 
                            || other.isEmpty() 
                            || gu.isEmpty() 
                            || gu.getPrimary() != other.getPrimary()) {
                        if ((thisIter == bodyIter)
                                && gu.getFlag(GridUnit.FIRST_IN_TABLE)
                                && (headerIter == null)) {
                            flags |= CollapsingBorderModel.VERTICAL_START_END_OF_TABLE;
                        }
                        if ((thisIter == headerIter)
                                && gu.getFlag(GridUnit.FIRST_IN_TABLE)) {
                            flags |= CollapsingBorderModel.VERTICAL_START_END_OF_TABLE;
                        }
                        gu.resolveBorder(other, 
                                CommonBorderPaddingBackground.BEFORE, flags);
                    }
                    
                    flags = 0;
                    if (nextRow != null && i < nextRow.getGridUnits().size()) {
                        other = nextRow.getGridUnit(i);
                    } else {
                        other = null;
                    }
                    if (other == null 
                            || other.isEmpty() 
                            || gu.isEmpty() 
                            || gu.getPrimary() != other.getPrimary()) {
                        if ((thisIter == bodyIter)
                                && gu.getFlag(GridUnit.LAST_IN_TABLE)
                                && (footerIter == null)) {
                            flags |= CollapsingBorderModel.VERTICAL_START_END_OF_TABLE;
                        }
                        if ((thisIter == footerIter)
                                && gu.getFlag(GridUnit.LAST_IN_TABLE)) {
                            flags |= CollapsingBorderModel.VERTICAL_START_END_OF_TABLE;
                        }
                        gu.resolveBorder(other, 
                                CommonBorderPaddingBackground.AFTER, flags);
                    }
                }
            }
        }
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
            int bodyType, LinkedList returnList, 
            EffRow[] rowGroup) {
        log.debug("Handling row group with " + rowGroup.length + " rows...");
        MinOptMax[] rowHeights = new MinOptMax[rowGroup.length];
        MinOptMax[] explicitRowHeights = new MinOptMax[rowGroup.length];
        EffRow row;
        int maxColumnCount = 0;
        List pgus = new java.util.ArrayList(); //holds a list of a row's primary grid units
        for (int rgi = 0; rgi < rowGroup.length; rgi++) {
            row = rowGroup[rgi];
            rowHeights[rgi] = new MinOptMax(0, 0, Integer.MAX_VALUE);
            explicitRowHeights[rgi] = new MinOptMax(0, 0, Integer.MAX_VALUE);
            
            pgus.clear();
            TableRow tableRow = null;
            // The row's minimum content height; 0 if the row's height is auto, otherwise
            // the .minimum component of the explicitely specified value
            int minContentHeight = 0;
            int maxCellHeight = 0;
            int effRowContentHeight = 0;
            for (int j = 0; j < row.getGridUnits().size(); j++) {
//                assert maxColumnCount == 0 || maxColumnCount == row.getGridUnits().size(); // TODO vh
                maxColumnCount = Math.max(maxColumnCount, row.getGridUnits().size());
                GridUnit gu = row.getGridUnit(j);
                if ((gu.isPrimary() || (gu.getColSpanIndex() == 0 && gu.isLastGridUnitRowSpan())) 
                        && !gu.isEmpty()) {
                    PrimaryGridUnit primary = gu.getPrimary();
                    
                    if (gu.isPrimary()) {
                        primary.getCellLM().setParent(tableLM);
                     
                        //Determine the table-row if any
                        if (tableRow == null && primary.getRow() != null) {
                            tableRow = primary.getRow();
                            
                            //Check for bpd on row, see CSS21, 17.5.3 Table height algorithms
                            LengthRangeProperty bpd = tableRow.getBlockProgressionDimension();
                            if (!bpd.getMinimum(tableLM).isAuto()) {
                                minContentHeight = Math.max(
                                        minContentHeight, 
                                        bpd.getMinimum(
                                                tableLM).getLength().getValue(tableLM));
                            }
                            MinOptMaxUtil.restrict(explicitRowHeights[rgi], bpd, tableLM);
                            
                        }

                        //Calculate width of cell
                        int spanWidth = 0;
                        for (int i = primary.getStartCol(); 
                                i < primary.getStartCol() 
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
                        //Temporary? Multiple calls in case of break conditions.
                        //TODO Revisit when table layout is restartable
                        while (!primary.getCellLM().isFinished()) {
                            LinkedList additionalElems = primary.getCellLM().getNextKnuthElements(
                                    childLC, alignment);
                            elems.addAll(additionalElems);
                        }
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

                    
                    //Calculate height of cell contents
                    primary.setContentLength(ElementListUtils.calcContentLength(
                            primary.getElements()));
                    maxCellHeight = Math.max(maxCellHeight, primary.getContentLength());

                    //Calculate height of row, see CSS21, 17.5.3 Table height algorithms
                    if (gu.isLastGridUnitRowSpan()) {
                        int effCellContentHeight = minContentHeight;
                        LengthRangeProperty bpd = primary.getCell().getBlockProgressionDimension();
                        if (!bpd.getMinimum(tableLM).isAuto()) {
                            effCellContentHeight = Math.max(
                                effCellContentHeight,
                                bpd.getMinimum(tableLM).getLength().getValue(tableLM));
                        }
                        if (!bpd.getOptimum(tableLM).isAuto()) {
                            effCellContentHeight = Math.max(
                                effCellContentHeight,
                                bpd.getOptimum(tableLM).getLength().getValue(tableLM));
                        }
                        if (gu.getRowSpanIndex() == 0) {
                            //TODO ATM only non-row-spanned cells are taken for this
                            MinOptMaxUtil.restrict(explicitRowHeights[rgi], bpd, tableLM);
                        }
                        effCellContentHeight = Math.max(effCellContentHeight, 
                                primary.getContentLength());
                        
                        int borderWidths;
                        if (tableLM.getTable().isSeparateBorderModel()) {
                            borderWidths = primary.getBorders().getBorderBeforeWidth(false)
                                    + primary.getBorders().getBorderAfterWidth(false);
                        } else {
                            borderWidths = primary.getHalfMaxBorderWidth();
                        }
                        int padding = 0;
                        effRowContentHeight = Math.max(effRowContentHeight,
                                effCellContentHeight);
                        CommonBorderPaddingBackground cbpb 
                            = primary.getCell().getCommonBorderPaddingBackground(); 
                        padding += cbpb.getPaddingBefore(false, primary.getCellLM());
                        padding += cbpb.getPaddingAfter(false, primary.getCellLM());
                        int effRowHeight = effCellContentHeight 
                                + padding + borderWidths
                                + 2 * tableLM.getHalfBorderSeparationBPD();
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
            if (effRowContentHeight > row.getExplicitHeight().max) {
                log.warn(FONode.decorateWithContextInfo(
                        "The contents of row " + (row.getIndex() + 1) 
                        + " are taller than they should be (there is a"
                        + " block-progression-dimension or height constraint on the indicated row)."
                        + " Due to its contents the row grows"
                        + " to " + effRowContentHeight + " millipoints, but the row shouldn't get"
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
        LinkedList returnedList = tableStepper.getCombinedKnuthElementsForRowGroup(
                context, rowGroup, maxColumnCount, bodyType);
        if (returnedList != null) {
            returnList.addAll(returnedList);
        }
        
    }
}
