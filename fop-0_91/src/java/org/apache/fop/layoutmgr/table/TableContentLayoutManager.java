/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.MinOptMaxUtil;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.SpaceResolver;
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
    private TableRowIterator trIter;
    private TableRowIterator headerIter;
    private TableRowIterator footerIter;
    private LinkedList headerList;
    private LinkedList footerList;
    private int headerNetHeight = 0;
    private int footerNetHeight = 0;
    private boolean firstBreakBeforeServed = false;

    private int startXOffset;
    private int usedBPD;
    
    /**
     * Main constructor
     * @param parent Parent layout manager
     */
    public TableContentLayoutManager(TableLayoutManager parent) {
        this.tableLM = parent;
        Table table = getTableLM().getTable();
        this.trIter = new TableRowIterator(table, getTableLM().getColumns(), TableRowIterator.BODY);
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
    private boolean isSeparateBorderModel() {
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
        return this.headerNetHeight;
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
            this.headerNetHeight = ElementListUtils.calcContentLength(this.headerList);
            if (log.isDebugEnabled()) {
                log.debug("==> Header: " + headerNetHeight + " - " + this.headerList);
            }
            TableHeaderFooterPosition pos = new TableHeaderFooterPosition(
                    getTableLM(), true, this.headerList);
            KnuthBox box = new KnuthBox(headerNetHeight, pos, false);
            if (getTableLM().getTable().omitHeaderAtBreak()) {
                //We can simply add the table header at the beginning of the whole list
                headerAsFirst = box;
            } else {
                headerAsSecondToLast = box;
            }
        }
        if (footerIter != null && footerList == null) {
            this.footerList = getKnuthElementsForRowIterator(
                    footerIter, context, alignment, TableRowIterator.FOOTER);
            ElementListUtils.removeLegalBreaks(this.footerList);
            this.footerNetHeight = ElementListUtils.calcContentLength(this.footerList);
            if (log.isDebugEnabled()) {
                log.debug("==> Footer: " + footerNetHeight + " - " + this.footerList);
            }
            if (true /*getTableLM().getTable().omitFooterAtBreak()*/) {
                //We can simply add the table header at the end of the whole list
                TableHeaderFooterPosition pos = new TableHeaderFooterPosition(
                        getTableLM(), false, this.footerList);
                KnuthBox box = new KnuthBox(footerNetHeight, pos, false);
                footerAsLast = box;
            }
        }
        LinkedList returnList = getKnuthElementsForRowIterator(
                trIter, context, alignment, TableRowIterator.BODY);
        if (headerAsFirst != null) {
            returnList.add(0, headerAsFirst);
        } else if (headerAsSecondToLast != null) {
            returnList.add(headerAsSecondToLast);
        }
        if (footerAsLast != null) {
            returnList.add(footerAsLast);
        }
        return returnList;
    }
    
    /**
     * Creates Knuth elements by iterating over a TableRowIterator.
     * @param iter TableRowIterator instance to fetch rows from
     * @param context Active LayoutContext
     * @param alignment alignment indicator
     * @param bodyType Indicates what kind of body is being processed (BODY, HEADER or FOOTER)
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
                    } else if (last instanceof BreakElement) {
                        BreakElement breakPoss = (BreakElement)last;
                        breakPoss.setPenaltyValue(-KnuthPenalty.INFINITE);
                        breakPoss.setBreakClass(rowFO.getBreakBefore());
                    }
                } else {
                    if (!firstBreakBeforeServed) {
                        //returnList.add(new KnuthPenalty(0, -KnuthPenalty.INFINITE, 
                        //        false, rowFO.getBreakBefore(), new Position(getTableLM()), true));
                        returnList.add(new BreakElement(new Position(getTableLM()),
                                0, -KnuthPenalty.INFINITE, rowFO.getBreakBefore(), context));
                        iter.backToPreviousRow();
                        firstBreakBeforeServed = true;
                        break;
                    }
                }
            }
            firstBreakBeforeServed = true;
            
            //Border resolution
            if (!isSeparateBorderModel()) {
                resolveNormalBeforeAfterBordersForRowGroup(rowGroup, iter);
            }
            
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
                log.info("break-after found");
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
            //Remove last penalty
            ListElement last = (ListElement)returnList.getLast();
            if (last.isPenalty() || last instanceof BreakElement) {
                if (!last.isForcedBreak()) {
                    //Only remove if we don't signal a forced break
                    returnList.removeLast();
                }
            }
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
            EffRow prev = iter.getCachedRow(row.getIndex() - 1);
            EffRow next = iter.getCachedRow(row.getIndex() + 1);
            if (next == null) {
                //It wasn't read, yet, or we are at the last row
                next = iter.getNextRow();
                iter.backToPreviousRow();
            }
            if ((prev == null) && (iter == this.trIter) && (this.headerIter != null)) {
                prev = this.headerIter.getLastRow();
            }
            if ((next == null) && (iter == this.headerIter)) {
                next = this.trIter.getFirstRow();
            }
            if ((next == null) && (iter == this.trIter) && (this.footerIter != null)) {
                next = this.footerIter.getFirstRow();
            }
            if ((prev == null) && (iter == this.footerIter)) {
                //TODO This could be bad for memory consumption because it already causes the
                //whole body iterator to be prefetched!
                prev = this.trIter.getLastRow();
            }
            log.debug(prev + " - " + row + " - " + next);
            
            //Determine the grid units necessary for getting all the borders right
            int guCount = row.getGridUnits().size();
            if (prev != null) {
                guCount = Math.max(guCount, prev.getGridUnits().size());
            }
            if (next != null) {
                guCount = Math.max(guCount, next.getGridUnits().size());
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
                    if (prev != null && i < prev.getGridUnits().size()) {
                        other = prev.getGridUnit(i);
                    } else {
                        other = null;
                    }
                    if (other == null 
                            || other.isEmpty() 
                            || gu.isEmpty() 
                            || gu.getPrimary() != other.getPrimary()) {
                        if ((iter == this.trIter)
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
                    if (next != null && i < next.getGridUnits().size()) {
                        other = next.getGridUnit(i);
                    } else {
                        other = null;
                    }
                    if (other == null 
                            || other.isEmpty() 
                            || gu.isEmpty() 
                            || gu.getPrimary() != other.getPrimary()) {
                        if ((iter == this.trIter)
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
            int minContentHeight = 0;
            int maxCellHeight = 0;
            int effRowContentHeight = 0;
            for (int j = 0; j < row.getGridUnits().size(); j++) {
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
        //TODO It may make sense to reuse the stepper since it allocates quite some space
        TableStepper stepper = new TableStepper(this);
        LinkedList returnedList = stepper.getCombinedKnuthElementsForRowGroup(
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
     * Adds the areas generated my this layout manager to the area tree.
     * @param parentIter the position iterator
     * @param layoutContext the layout context for adding areas
     */
    public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
        this.usedBPD = 0;
        RowPainter painter = new RowPainter(layoutContext);

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
            painter.addAreasAndFlushRow(true);
        }
        
        //Iterate over all steps
        Iterator posIter = positions.iterator();
        iterateAndPaintPositions(posIter, painter);
        painter.addAreasAndFlushRow(true);

        if (footerElements != null) {
            //Positions for footers are simply added at the end
            PositionIterator nestedIter = new KnuthPossPosIter(footerElements);
            iterateAndPaintPositions(nestedIter, painter);
            painter.addAreasAndFlushRow(true);
        }
        
        painter.notifyEndOfSequence();
        this.usedBPD += painter.getAccumulatedBPD();

        if (markers != null) {
            getTableLM().getCurrentPV().addMarkers(markers, 
                    false, getTableLM().isFirst(firstPos), getTableLM().isLast(lastCheckPos));
        }
    }
    
    private void iterateAndPaintPositions(Iterator iterator, RowPainter painter) {
        List lst = new java.util.ArrayList();
        boolean firstPos = false;
        boolean lastPos = false;
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
                        && tcpos.row.getFlag(EffRow.FIRST_IN_BODY)) {
                    firstPos = true;

                }
                if (tcpos.getFlag(TableContentPosition.LAST_IN_ROWGROUP) 
                        && tcpos.row.getFlag(EffRow.LAST_IN_BODY)) {
                    lastPos = true;
                    getTableLM().getCurrentPV().addMarkers(body.getMarkers(), 
                            true, firstPos, lastPos);
                    int size = lst.size();
                    for (int i = 0; i < size; i++) {
                        painter.handleTableContentPosition((TableContentPosition)lst.get(i));
                    }
                    getTableLM().getCurrentPV().addMarkers(body.getMarkers(), 
                            false, firstPos, lastPos);
                    //reset
                    firstPos = false;
                    lastPos = false;
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
            getTableLM().getCurrentPV().addMarkers(body.getMarkers(), 
                    true, firstPos, lastPos);
            int size = lst.size();
            for (int i = 0; i < size; i++) {
                painter.handleTableContentPosition((TableContentPosition)lst.get(i));
            }
            getTableLM().getCurrentPV().addMarkers(body.getMarkers(), 
                    false, firstPos, lastPos);
        }
    }
   
    private class RowPainter {
        
        private TableRow rowFO = null;
        private int colCount = getColumns().getColumnCount();
        private int yoffset = 0;
        private int accumulatedBPD = 0;
        private EffRow lastRow = null;
        private LayoutContext layoutContext;
        private int lastRowHeight = 0;
        private int[] firstRow = new int[3];
        private Map[] rowOffsets = new Map[] {new java.util.HashMap(), 
                new java.util.HashMap(), new java.util.HashMap()};

        //These three variables are our buffer to recombine the individual steps into cells
        private PrimaryGridUnit[] gridUnits = new PrimaryGridUnit[colCount];
        private int[] start = new int[colCount];
        private int[] end = new int[colCount];
        private int[] partLength = new int[colCount];
        
        public RowPainter(LayoutContext layoutContext) {
            this.layoutContext = layoutContext;
            Arrays.fill(firstRow, -1);
            Arrays.fill(end, -1);
        }
        
        public int getAccumulatedBPD() {
            return this.accumulatedBPD;
        }
        
        public void notifyEndOfSequence() {
            this.accumulatedBPD += lastRowHeight; //for last row
        }
        
        public void handleTableContentPosition(TableContentPosition tcpos) {
            if (lastRow != tcpos.row && lastRow != null) {
                addAreasAndFlushRow(false);
                yoffset += lastRowHeight;
                this.accumulatedBPD += lastRowHeight;
            }
            if (log.isDebugEnabled()) {
                log.debug("===handleTableContentPosition(" + tcpos);
            }
            rowFO = tcpos.row.getTableRow();
            lastRow = tcpos.row;
            Iterator partIter = tcpos.gridUnitParts.iterator();
            //Iterate over all grid units in the current step
            while (partIter.hasNext()) {
                GridUnitPart gup = (GridUnitPart)partIter.next();
                if (log.isDebugEnabled()) {
                    log.debug(">" + gup);
                }
                int colIndex = gup.pgu.getStartCol();
                if (gridUnits[colIndex] != gup.pgu) {
                    if (gridUnits[colIndex] != null) {
                        log.warn("Replacing GU in slot " + colIndex 
                                + ". Some content may not be painted.");
                    }
                    gridUnits[colIndex] = gup.pgu;
                    start[colIndex] = gup.start;
                    end[colIndex] = gup.end;
                } else {
                    if (gup.end < end[colIndex]) {
                        throw new IllegalStateException("Internal Error: stepper problem");
                    }
                    end[colIndex] = gup.end;
                }
            }
        }
        
        public int addAreasAndFlushRow(boolean forcedFlush) {
            int actualRowHeight = 0;
            int readyCount = 0;
            
            int bt = lastRow.getBodyType();
            if (log.isDebugEnabled()) {
                log.debug("Remembering yoffset for row " + lastRow.getIndex() + ": " + yoffset);
            }
            rowOffsets[bt].put(new Integer(lastRow.getIndex()), new Integer(yoffset));

            for (int i = 0; i < gridUnits.length; i++) {
                if ((gridUnits[i] != null) 
                        && (forcedFlush || (end[i] == gridUnits[i].getElements().size() - 1))) {
                    if (log.isTraceEnabled()) {
                        log.trace("getting len for " + i + " " 
                                + start[i] + "-" + end[i]);
                    }
                    readyCount++;
                    int len = ElementListUtils.calcContentLength(
                            gridUnits[i].getElements(), start[i], end[i]);
                    partLength[i] = len;
                    if (log.isTraceEnabled()) {
                        log.trace("len of part: " + len);
                    }

                    if (start[i] == 0) {
                        LengthRangeProperty bpd = gridUnits[i].getCell()
                                .getBlockProgressionDimension();
                        if (!bpd.getMinimum(getTableLM()).isAuto()) {
                            int min = bpd.getMinimum(getTableLM())
                                        .getLength().getValue(getTableLM()); 
                            if (min > 0) {
                                len = Math.max(len, bpd.getMinimum(getTableLM())
                                        .getLength().getValue(getTableLM()));
                            }
                        }
                        if (!bpd.getOptimum(getTableLM()).isAuto()) {
                            int opt = bpd.getOptimum(getTableLM())
                                        .getLength().getValue(getTableLM());
                            if (opt > 0) {
                                len = Math.max(len, opt);
                            }
                        }
                        if (gridUnits[i].getRow() != null) {
                            bpd = gridUnits[i].getRow().getBlockProgressionDimension();
                            if (!bpd.getMinimum(getTableLM()).isAuto()) {
                                int min = bpd.getMinimum(getTableLM()).getLength()
                                            .getValue(getTableLM()); 
                                if (min > 0) {
                                    len = Math.max(len, min);
                                }
                            }
                        }
                    }
                    
                    // Add the padding if any
                    len += gridUnits[i].getBorders()
                                    .getPaddingBefore(false, gridUnits[i].getCellLM());
                    len += gridUnits[i].getBorders()
                                    .getPaddingAfter(false, gridUnits[i].getCellLM());

                    //Now add the borders to the contentLength
                    if (isSeparateBorderModel()) {
                        len += gridUnits[i].getBorders().getBorderBeforeWidth(false);
                        len += gridUnits[i].getBorders().getBorderAfterWidth(false);
                    }
                    int startRow = Math.max(gridUnits[i].getStartRow(), firstRow[bt]);
                    Integer storedOffset = (Integer)rowOffsets[bt].get(new Integer(startRow));
                    int effYOffset;
                    if (storedOffset != null) {
                        effYOffset = storedOffset.intValue();
                    } else {
                        effYOffset = yoffset;
                    }
                    len -= yoffset - effYOffset;
                    actualRowHeight = Math.max(actualRowHeight, len);
                }
            }
            if (readyCount == 0) {
                return 0;
            }
            actualRowHeight += 2 * getTableLM().getHalfBorderSeparationBPD();
            lastRowHeight = actualRowHeight;
            
            //Add areas for row
            addRowBackgroundArea(rowFO, actualRowHeight, layoutContext.getRefIPD(), yoffset);
            for (int i = 0; i < gridUnits.length; i++) {
                GridUnit currentGU = lastRow.safelyGetGridUnit(i);
                if ((gridUnits[i] != null) 
                        && (forcedFlush || ((end[i] == gridUnits[i].getElements().size() - 1))
                                && (currentGU == null || currentGU.isLastGridUnitRowSpan()))
                    || (gridUnits[i] == null && currentGU != null)) {
                    //the last line in the "if" above is to avoid a premature end of an 
                    //row-spanned cell because no GridUnitParts are generated after a cell is
                    //finished with its content. currentGU can be null if there's no grid unit
                    //at this place in the current row (empty cell and no borders to process)
                    if (log.isDebugEnabled()) {
                        log.debug((forcedFlush ? "FORCED " : "") + "flushing..." + i + " " 
                                + start[i] + "-" + end[i]);
                    }
                    PrimaryGridUnit gu = gridUnits[i];
                    if (gu == null 
                            && !currentGU.isEmpty() 
                            && currentGU.getColSpanIndex() == 0 
                            && currentGU.isLastGridUnitColSpan()
                            && (forcedFlush || currentGU.isLastGridUnitRowSpan())) {
                        gu = currentGU.getPrimary();
                    }
                    if (gu != null) {
                        addAreasForCell(gu, start[i], end[i], 
                                lastRow,  
                                partLength[i], actualRowHeight);
                        gridUnits[i] = null;
                        start[i] = 0;
                        end[i] = -1;
                        partLength[i] = 0;
                    }
                }
            }
            return actualRowHeight;
        }

        private void addAreasForCell(PrimaryGridUnit pgu, int startPos, int endPos, 
                EffRow row, int contentHeight, int rowHeight) {
            int bt = row.getBodyType();
            if (firstRow[bt] < 0) {
                firstRow[bt] = row.getIndex();
            }
            //Determine the first row in this sequence
            int startRow = Math.max(pgu.getStartRow(), firstRow[bt]);
            //Determine y offset for the cell
            Integer offset = (Integer)rowOffsets[bt].get(new Integer(startRow));
            while (offset == null) {
                startRow--;
                offset = (Integer)rowOffsets[bt].get(new Integer(startRow));
            }
            int effYOffset = offset.intValue();
            int effCellHeight = rowHeight;
            effCellHeight += yoffset - effYOffset;
            if (log.isDebugEnabled()) {
                log.debug("Creating area for cell:");
                log.debug("  current row: " + row.getIndex());
                log.debug("  start row: " + pgu.getStartRow() + " " + yoffset + " " + effYOffset);
                log.debug("  contentHeight: " + contentHeight + " rowHeight=" + rowHeight 
                        + " effCellHeight=" + effCellHeight);
            }
            TableCellLayoutManager cellLM = pgu.getCellLM();
            cellLM.setXOffset(getXOffsetOfGridUnit(pgu));
            cellLM.setYOffset(effYOffset);
            cellLM.setContentHeight(contentHeight);
            cellLM.setRowHeight(effCellHeight);
            //cellLM.setRowHeight(row.getHeight().opt);
            int prevBreak = ElementListUtils.determinePreviousBreak(pgu.getElements(), startPos);
            if (endPos >= 0) {
                SpaceResolver.performConditionalsNotification(pgu.getElements(), 
                        startPos, endPos, prevBreak);
            }
            cellLM.addAreas(new KnuthPossPosIter(pgu.getElements(), 
                    startPos, endPos + 1), layoutContext);
        }
        
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
    
    /**
     * Represents a non-dividable part of a grid unit. Used by the table stepper.
     */
    protected static class GridUnitPart {
        
        /** Primary grid unit */
        protected PrimaryGridUnit pgu;
        /** Index of the starting element of this part */
        protected int start;
        /** Index of the ending element of this part */
        protected int end;
        
        /**
         * Creates a new GridUnitPart.
         * @param pgu Primary grid unit
         * @param start starting element
         * @param end ending element
         */
        protected GridUnitPart(PrimaryGridUnit pgu, int start, int end) {
            this.pgu = pgu;
            this.start = start;
            this.end = end;
        }
        
        /** @return true if this part is the first part of a cell */
        public boolean isFirstPart() {
            return (start == 0);
        }
        
        /** @return true if this part is the last part of a cell */
        public boolean isLastPart() {
            return (end >= 0 && end == pgu.getElements().size() - 1);
        }
        
        /** @see java.lang.Object#toString() */
        public String toString() {
            StringBuffer sb = new StringBuffer("Part: ");
            sb.append(start).append("-").append(end);
            sb.append(" [").append(isFirstPart() ? "F" : "-").append(isLastPart() ? "L" : "-");
            sb.append("] ").append(pgu);
            return sb.toString();
        }
        
    }
    
    /**
     * This class represents a Position specific to this layout manager. Used for normal content
     * cases.
     */
    public static class TableContentPosition extends Position {

        /** The position is the first of the row group. */ 
        public static final int FIRST_IN_ROWGROUP = 1;
        /** The position is the last of the row group. */ 
        public static final int LAST_IN_ROWGROUP = 2;
        
        /** the list of GridUnitParts making up this position */
        protected List gridUnitParts;
        /** effective row this position belongs to */
        protected EffRow row;
        /** flags for the position */
        protected int flags;
        
        /**
         * Creates a new TableContentPosition.
         * @param lm applicable layout manager
         * @param gridUnitParts the list of GridUnitPart instances
         * @param row effective row this position belongs to
         */
        protected TableContentPosition(LayoutManager lm, List gridUnitParts, 
                EffRow row) {
            super(lm);
            this.gridUnitParts = gridUnitParts;
            this.row = row;
        }
        
        /**
         * Returns a flag for this GridUnit.
         * @param which the requested flag
         * @return the value of the flag
         */
        public boolean getFlag(int which) {
            return (flags & (1 << which)) != 0;
        }
        
        /**
         * Sets a flag on a GridUnit.
         * @param which the flag to set
         * @param value the new value for the flag
         */
        public void setFlag(int which, boolean value) {
            if (value) {
                flags |= (1 << which); //set flag
            } else {
                flags &= ~(1 << which); //clear flag
            }
        }
        
        /** @see java.lang.Object#toString() */
        public String toString() {
            StringBuffer sb = new StringBuffer("TableContentPosition:");
            sb.append(getIndex());
            sb.append("[");
            sb.append(row.getIndex()).append("/");
            sb.append(getFlag(FIRST_IN_ROWGROUP) ? "F" : "-");
            sb.append(getFlag(LAST_IN_ROWGROUP) ? "L" : "-").append("]");
            sb.append("(");
            sb.append(gridUnitParts);
            sb.append(")");
            return sb.toString();
        }
    }
    
    /**
     * This class represents a Position specific to this layout manager. Used for table
     * headers and footers at the beginning and end of a table.
     */
    public static class TableHeaderFooterPosition extends Position {

        /** True indicates a position for a header, false for a footer. */
        protected boolean header;
        /** Element list representing the header/footer */
        protected List nestedElements;
        
        /**
         * Creates a new TableHeaderFooterPosition.
         * @param lm applicable layout manager
         * @param header True indicates a position for a header, false for a footer.
         * @param nestedElements Element list representing the header/footer
         */
        protected TableHeaderFooterPosition(LayoutManager lm, 
                boolean header, List nestedElements) {
            super(lm);
            this.header = header;
            this.nestedElements = nestedElements;
        }
        
        /** @see java.lang.Object#toString() */
        public String toString() {
            StringBuffer sb = new StringBuffer("Table");
            sb.append(header ? "Header" : "Footer");
            sb.append("Position:");
            sb.append(getIndex()).append("(");
            sb.append(nestedElements);
            sb.append(")");
            return sb.toString();
        }
    }

    /**
    * This class represents a Position specific to this layout manager. Used for table
    * headers and footers at breaks.
    */
    public static class TableHFPenaltyPosition extends Position {
        
        /** Element list for the header */
        protected List headerElements;
        /** Element list for the footer */
        protected List footerElements;
        
        /**
         * Creates a new TableHFPenaltyPosition
         * @param lm applicable layout manager
         */
        protected TableHFPenaltyPosition(LayoutManager lm) {
            super(lm);
        }
        
        /** @see java.lang.Object#toString() */
        public String toString() {
            StringBuffer sb = new StringBuffer("TableHFPenaltyPosition:");
            sb.append(getIndex()).append("(");
            sb.append("header:");
            sb.append(headerElements);
            sb.append(", footer:");
            sb.append(footerElements);
            sb.append(")");
            return sb.toString();
        }
    }
    
    // --------- Property Resolution related functions --------- //
    
    /**
     * @see org.apache.fop.datatypes.PercentBaseContext#getBaseLength(int, FObj)
     */
    public int getBaseLength(int lengthBase, FObj fobj) {
        return tableLM.getBaseLength(lengthBase, fobj);
    }

}
