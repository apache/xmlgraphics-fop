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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.Block;
import org.apache.fop.area.Trait;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.layoutmgr.BreakElement;
import org.apache.fop.layoutmgr.ElementListObserver;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.MinOptMaxUtil;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.layoutmgr.SpaceResolver.SpaceHandlingBreakPosition;
import org.apache.fop.traits.MinOptMax;

/**
 * Layout manager for table contents, particularly managing the creation of combined element lists.
 */
public class TableContentLayoutManager implements PercentBaseContext {

    /** Logger **/
    private static Log log = LogFactory.getLog(TableContentLayoutManager.class);

    private TableLayoutManager tableLM;
    private TableRowIterator bodyIter;
    private TableRowIterator headerIter;
    private TableRowIterator footerIter;
    private LinkedList headerList;
    private LinkedList footerList;
    private int headerNetHeight = 0;
    private int footerNetHeight = 0;

    private int startXOffset;
    private int usedBPD;
    
    private TableStepper stepper = new TableStepper(this);
        
    /**
     * Main constructor
     * @param parent Parent layout manager
     */
    public TableContentLayoutManager(TableLayoutManager parent) {
        this.tableLM = parent;
        Table table = getTableLM().getTable();
        this.bodyIter = new TableRowIterator(table, getTableLM().getColumns(),
                TableRowIterator.BODY);
        if (table.getTableHeader() != null) {
            headerIter = new TableRowIterator(table, 
                    getTableLM().getColumns(), TableRowIterator.HEADER);
        }
        if (table.getTableFooter() != null) {
            footerIter = new TableRowIterator(table, 
                    getTableLM().getColumns(), TableRowIterator.FOOTER);
        }
    }
    
    /**
     * @return the table layout manager
     */
    public TableLayoutManager getTableLM() {
        return this.tableLM;
    }
    
    /** @return true if the table uses the separate border model. */
    boolean isSeparateBorderModel() {
        return getTableLM().getTable().isSeparateBorderModel();
    }
    
    /**
     * @return the column setup of this table
     */
    public ColumnSetup getColumns() {
        return getTableLM().getColumns();
    }
    
    /** @return the net header height */
    protected int getHeaderNetHeight() {
        return this.headerNetHeight;
    }

    /** @return the net footer height */
    protected int getFooterNetHeight() {
        return this.footerNetHeight;
    }

    /** @return the header element list */
    protected LinkedList getHeaderElements() {
        return this.headerList;
    }

    /** @return the footer element list */
    protected LinkedList getFooterElements() {
        return this.footerList;
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
        log.debug("==> Columns: " + getTableLM().getColumns());
        KnuthBox headerAsFirst = null;
        KnuthBox headerAsSecondToLast = null;
        KnuthBox footerAsLast = null;
        if (headerIter != null && headerList == null) {
            this.headerList = getKnuthElementsForRowIterator(
                    headerIter, context, alignment, TableRowIterator.HEADER);
            ElementListUtils.removeLegalBreaks(this.headerList);
            this.headerNetHeight
                    = ElementListUtils.calcContentLength(this.headerList);
            if (log.isDebugEnabled()) {
                log.debug("==> Header: " 
                        + headerNetHeight + " - " + this.headerList);
            }
            TableHeaderFooterPosition pos = new TableHeaderFooterPosition(
                    getTableLM(), true, this.headerList);
            KnuthBox box = new KnuthBox(headerNetHeight, pos, false);
            if (getTableLM().getTable().omitHeaderAtBreak()) {
                //We can simply add the table header at the start 
                //of the whole list
                headerAsFirst = box;
            } else {
                headerAsSecondToLast = box;
            }
        }
        if (footerIter != null && footerList == null) {
            this.footerList = getKnuthElementsForRowIterator(
                    footerIter, context, alignment, TableRowIterator.FOOTER);
            ElementListUtils.removeLegalBreaks(this.footerList);
            this.footerNetHeight
                    = ElementListUtils.calcContentLength(this.footerList);
            if (log.isDebugEnabled()) {
                log.debug("==> Footer: " 
                        + footerNetHeight + " - " + this.footerList);
            }
            //We can simply add the table footer at the end of the whole list
            TableHeaderFooterPosition pos = new TableHeaderFooterPosition(
                    getTableLM(), false, this.footerList);
            KnuthBox box = new KnuthBox(footerNetHeight, pos, false);
            footerAsLast = box;
        }
        LinkedList returnList = getKnuthElementsForRowIterator(
                bodyIter, context, alignment, TableRowIterator.BODY);
        if (headerAsFirst != null) {
            int insertionPoint = 0;
            if (returnList.size() > 0 && ((ListElement)returnList.getFirst()).isForcedBreak()) {
                insertionPoint++;
            }
            returnList.add(insertionPoint, headerAsFirst);
        } else if (headerAsSecondToLast != null) {
            int insertionPoint = returnList.size();
            if (returnList.size() > 0 && ((ListElement)returnList.getLast()).isForcedBreak()) {
                insertionPoint--;
            }
            returnList.add(insertionPoint, headerAsSecondToLast);
        }
        if (footerAsLast != null) {
            int insertionPoint = returnList.size();
            if (returnList.size() > 0 && ((ListElement)returnList.getLast()).isForcedBreak()) {
                insertionPoint--;
            }
            returnList.add(insertionPoint, footerAsLast);
        }
        return returnList;
    }
    
    /**
     * Creates Knuth elements by iterating over a TableRowIterator.
     * @param iter TableRowIterator instance to fetch rows from
     * @param context Active LayoutContext
     * @param alignment alignment indicator
     * @param bodyType Indicates what kind of body is being processed 
     *                  (BODY, HEADER or FOOTER)
     * @return An element list
     */
    private LinkedList getKnuthElementsForRowIterator(TableRowIterator iter, 
            LayoutContext context, int alignment, int bodyType) {
        LinkedList returnList = new LinkedList();
        EffRow[] rowGroup = null;
        while ((rowGroup = iter.getNextRowGroup()) != null) {
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
                    returnList.add(new BreakElement(new Position(getTableLM()),
                            0, -KnuthPenalty.INFINITE, rowFO.getBreakBefore(), context));
                }
            }
            
            //Border resolution
            if (!isSeparateBorderModel()) {
                resolveNormalBeforeAfterBordersForRowGroup(rowGroup, iter);
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
        }
        
        if (returnList.size() > 0) {
            //Remove the last penalty produced by the combining algorithm (see TableStepper), for the last step
            ListElement last = (ListElement)returnList.getLast();
            if (last.isPenalty() || last instanceof BreakElement) {
                if (!last.isForcedBreak()) {
                    //Only remove if we don't signal a forced break
                    returnList.removeLast();
                }
            }
        }

        //fox:widow-content-limit
        int widowContentLimit = getTableLM().getTable().getWidowContentLimit().getValue(); 
        if (widowContentLimit != 0 && bodyType == TableRowIterator.BODY) {
            ElementListUtils.removeLegalBreaks(returnList, widowContentLimit);
        }
        //fox:orphan-content-limit
        int orphanContentLimit = getTableLM().getTable().getOrphanContentLimit().getValue(); 
        if (orphanContentLimit != 0 && bodyType == TableRowIterator.BODY) {
            ElementListUtils.removeLegalBreaksFromEnd(returnList, orphanContentLimit);
        }
        
        return returnList;
    }

    /**
     * Resolves normal borders for a row group.
     * @param iter Table row iterator to operate on
     */
    private void resolveNormalBeforeAfterBordersForRowGroup(EffRow[] rowGroup, 
            TableRowIterator iter) {
        for (int rgi = 0; rgi < rowGroup.length; rgi++) {
            EffRow row = rowGroup[rgi];
            EffRow prevRow = iter.getPrecedingRow(row);
            EffRow nextRow = iter.getFollowingRow(row);
            if ((prevRow == null) && (iter == this.bodyIter) && (this.headerIter != null)) {
                prevRow = this.headerIter.getLastRow();
            }
            if ((nextRow == null) && (iter == this.headerIter)) {
                nextRow = this.bodyIter.getFirstRow();
            }
            if ((nextRow == null) && (iter == this.bodyIter) && (this.footerIter != null)) {
                nextRow = this.footerIter.getFirstRow();
            }
            if ((prevRow == null) && (iter == this.footerIter)) {
                //TODO This could be bad for memory consumption because it already causes the
                //whole body iterator to be prefetched!
                prevRow = this.bodyIter.getLastRow();
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
                        this.tableLM.getColumns().getColumn(pos + 1), gu.getBody(), 
                        pos));
            }
            
            //Now resolve normal borders
            if (getTableLM().getTable().isSeparateBorderModel()) {
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
                        if ((iter == this.bodyIter)
                                && gu.getFlag(GridUnit.FIRST_IN_TABLE)
                                && (this.headerIter == null)) {
                            flags |= CollapsingBorderModel.VERTICAL_START_END_OF_TABLE;
                        }
                        if ((iter == this.headerIter)
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
                        if ((iter == this.bodyIter)
                                && gu.getFlag(GridUnit.LAST_IN_TABLE)
                                && (this.footerIter == null)) {
                            flags |= CollapsingBorderModel.VERTICAL_START_END_OF_TABLE;
                        }
                        if ((iter == this.footerIter)
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
                        primary.getCellLM().setParent(getTableLM());
                     
                        //Determine the table-row if any
                        if (tableRow == null && primary.getRow() != null) {
                            tableRow = primary.getRow();
                            
                            //Check for bpd on row, see CSS21, 17.5.3 Table height algorithms
                            LengthRangeProperty bpd = tableRow.getBlockProgressionDimension();
                            if (!bpd.getMinimum(getTableLM()).isAuto()) {
                                minContentHeight = Math.max(
                                        minContentHeight, 
                                        bpd.getMinimum(
                                                getTableLM()).getLength().getValue(getTableLM()));
                            }
                            MinOptMaxUtil.restrict(explicitRowHeights[rgi], bpd, getTableLM());
                            
                        }

                        //Calculate width of cell
                        int spanWidth = 0;
                        for (int i = primary.getStartCol(); 
                                i < primary.getStartCol() 
                                        + primary.getCell().getNumberColumnsSpanned();
                                i++) {
                            if (getTableLM().getColumns().getColumn(i + 1) != null) {
                                spanWidth += getTableLM().getColumns().getColumn(i + 1)
                                    .getColumnWidth().getValue(getTableLM());
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
                        if (!bpd.getMinimum(getTableLM()).isAuto()) {
                            effCellContentHeight = Math.max(
                                effCellContentHeight,
                                bpd.getMinimum(getTableLM()).getLength().getValue(getTableLM()));
                        }
                        if (!bpd.getOptimum(getTableLM()).isAuto()) {
                            effCellContentHeight = Math.max(
                                effCellContentHeight,
                                bpd.getOptimum(getTableLM()).getLength().getValue(getTableLM()));
                        }
                        if (gu.getRowSpanIndex() == 0) {
                            //TODO ATM only non-row-spanned cells are taken for this
                            MinOptMaxUtil.restrict(explicitRowHeights[rgi], bpd, tableLM);
                        }
                        effCellContentHeight = Math.max(effCellContentHeight, 
                                primary.getContentLength());
                        
                        int borderWidths;
                        if (isSeparateBorderModel()) {
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
                                + 2 * getTableLM().getHalfBorderSeparationBPD();
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
        LinkedList returnedList = this.stepper.getCombinedKnuthElementsForRowGroup(
                context, rowGroup, maxColumnCount, bodyType);
        if (returnedList != null) {
            returnList.addAll(returnedList);
        }
        
    }

    /**
     * Retuns the X offset of the given grid unit.
     * @param gu the grid unit
     * @return the requested X offset
     */
    protected int getXOffsetOfGridUnit(GridUnit gu) {
        int col = gu.getStartCol();
        return startXOffset + getTableLM().getColumns().getXOffset(col + 1, getTableLM());
    }
    
    /**
     * Adds the areas generated by this layout manager to the area tree.
     * @param parentIter the position iterator
     * @param layoutContext the layout context for adding areas
     */
    public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
        this.usedBPD = 0;
        RowPainter painter = new RowPainter(this, layoutContext);

        List positions = new java.util.ArrayList();
        List headerElements = null;
        List footerElements = null;
        Position firstPos = null;
        Position lastPos = null;
        Position lastCheckPos = null;
        while (parentIter.hasNext()) {
            Position pos = (Position)parentIter.next();
            if (pos instanceof SpaceHandlingBreakPosition) {
                //This position has only been needed before addAreas was called, now we need the
                //original one created by the layout manager.
                pos = ((SpaceHandlingBreakPosition)pos).getOriginalBreakPosition();
            }
            if (pos == null) {
                continue;
            }
            if (firstPos == null) {
                firstPos = pos;
            }
            lastPos = pos;
            if (pos.getIndex() >= 0) {
                lastCheckPos = pos;
            }
            if (pos instanceof TableHeaderFooterPosition) {
                TableHeaderFooterPosition thfpos = (TableHeaderFooterPosition)pos;
                //these positions need to be unpacked
                if (thfpos.header) {
                    //Positions for header will be added first
                    headerElements = thfpos.nestedElements;
                } else {
                    //Positions for footers are simply added at the end
                    footerElements = thfpos.nestedElements;
                }
            } else if (pos instanceof TableHFPenaltyPosition) {
                //ignore for now, see special handling below if break is at a penalty
                //Only if the last position in this part/page us such a position it will be used 
            } else {
                //leave order as is for the rest
                positions.add(pos);
            }
        }
        if (lastPos instanceof TableHFPenaltyPosition) {
            TableHFPenaltyPosition penaltyPos = (TableHFPenaltyPosition)lastPos;
            log.debug("Break at penalty!");
            if (penaltyPos.headerElements != null) {
                //Header positions for the penalty position are in the last element and need to
                //be handled first before all other TableContentPositions
                headerElements = penaltyPos.headerElements;
            }
            if (penaltyPos.footerElements != null) {
                footerElements = penaltyPos.footerElements; 
            }
        }

        Map markers = getTableLM().getTable().getMarkers();
        if (markers != null) {
            getTableLM().getCurrentPV().addMarkers(markers, 
                    true, getTableLM().isFirst(firstPos), getTableLM().isLast(lastCheckPos));
        }
        
        if (headerElements != null) {
            //header positions for the last part are the second-to-last element and need to
            //be handled first before all other TableContentPositions
            PositionIterator nestedIter = new KnuthPossPosIter(headerElements);
            iterateAndPaintPositions(nestedIter, painter);
        }
        
        //Iterate over all steps
        Iterator posIter = positions.iterator();
        iterateAndPaintPositions(posIter, painter);

        if (footerElements != null) {
            //Positions for footers are simply added at the end
            PositionIterator nestedIter = new KnuthPossPosIter(footerElements);
            iterateAndPaintPositions(nestedIter, painter);
        }
        
        this.usedBPD += painter.getAccumulatedBPD();

        if (markers != null) {
            getTableLM().getCurrentPV().addMarkers(markers, 
                    false, getTableLM().isFirst(firstPos), getTableLM().isLast(lastCheckPos));
        }
    }

    /**
     * Iterates over a part of the table (header, footer, body) and paints the related
     * elements.
     * 
     * @param iterator iterator over Position elements. Those positions correspond to the
     * elements of the table present on the current page
     * @param painter
     */
    private void iterateAndPaintPositions(Iterator iterator, RowPainter painter) {
        List lst = new java.util.ArrayList();
        boolean firstPos = false;
        TableBody body = null;
        while (iterator.hasNext()) {
            Position pos = (Position)iterator.next();
            if (pos instanceof TableContentPosition) {
                TableContentPosition tcpos = (TableContentPosition)pos;
                lst.add(tcpos);
                GridUnitPart part = (GridUnitPart)tcpos.gridUnitParts.get(0);
                if (body == null) {
                    body = part.pgu.getBody();
                }
                if (tcpos.getFlag(TableContentPosition.FIRST_IN_ROWGROUP)
                        && tcpos.row.getFlag(EffRow.FIRST_IN_PART)) {
                    firstPos = true;

                }
                if (tcpos.getFlag(TableContentPosition.LAST_IN_ROWGROUP) 
                        && tcpos.row.getFlag(EffRow.LAST_IN_PART)) {
                    log.trace("LAST_IN_ROWGROUP + LAST_IN_PART");
                    handleMarkersAndPositions(lst, body, firstPos, true, painter);
                    //reset
                    firstPos = false;
                    body = null;
                    lst.clear();
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Ignoring position: " + pos);
                }
            }
        }
        if (body != null) {
            // Entering this block means that the end of the current table-part hasn't
            // been reached (otherwise it would have been caught by the test above). So
            // lastPos is necessarily false
            handleMarkersAndPositions(lst, body, firstPos, false, painter);
        }
        painter.addAreasAndFlushRow(true);
    }

    private void handleMarkersAndPositions(List positions, TableBody body, boolean firstPos,
            boolean lastPos, RowPainter painter) {
        getTableLM().getCurrentPV().addMarkers(body.getMarkers(), 
                true, firstPos, lastPos);
        int size = positions.size();
        for (int i = 0; i < size; i++) {
            painter.handleTableContentPosition((TableContentPosition)positions.get(i));
        }
        getTableLM().getCurrentPV().addMarkers(body.getMarkers(), 
                false, firstPos, lastPos);
    }

    /**
     * Get the area for a row for background.
     * @param row the table-row object or null
     * @return the row area or null if there's no background to paint
     */
    public Block getRowArea(TableRow row) {
        if (row == null || !row.getCommonBorderPaddingBackground().hasBackground()) {
            return null;
        } else {
            Block block = new Block();
            block.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
            block.setPositioning(Block.ABSOLUTE);
            return block;
        }
    }

    /**
     * Adds the area for the row background if any.
     * @param row row for which to generate the background
     * @param bpd block-progression-dimension of the row
     * @param ipd inline-progression-dimension of the row
     * @param yoffset Y offset at which to paint
     */
    public void addRowBackgroundArea(TableRow row, int bpd, int ipd, int yoffset) {
        //Add row background if any
        Block rowBackground = getRowArea(row);
        if (rowBackground != null) {
            rowBackground.setBPD(bpd);
            rowBackground.setIPD(ipd);
            rowBackground.setXOffset(this.startXOffset);
            rowBackground.setYOffset(yoffset);
            getTableLM().addChildArea(rowBackground);
            TraitSetter.addBackground(rowBackground, 
                    row.getCommonBorderPaddingBackground(), getTableLM());
        }
    }
    
    
    /**
     * Sets the overall starting x-offset. Used for proper placement of cells.
     * @param startXOffset starting x-offset (table's start-indent)
     */
    public void setStartXOffset(int startXOffset) {
        this.startXOffset = startXOffset;
    }

    /**
     * @return the amount of block-progression-dimension used by the content
     */
    public int getUsedBPD() {
        return this.usedBPD;
    }

    // --------- Property Resolution related functions --------- //

    /**
     * @see org.apache.fop.datatypes.PercentBaseContext#getBaseLength(int, FObj)
     */
    public int getBaseLength(int lengthBase, FObj fobj) {
        return tableLM.getBaseLength(lengthBase, fobj);
    }

}
