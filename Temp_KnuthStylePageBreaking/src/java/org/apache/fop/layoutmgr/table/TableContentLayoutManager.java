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
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.Block;
import org.apache.fop.area.Trait;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.MinOptMaxUtil;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;

/**
 * Layout manager for table contents, particularly managing the creation of combined element lists.
 */
public class TableContentLayoutManager {

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

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getNextKnuthElements(org.apache.fop.layoutmgr.LayoutContext, int)
     */
    public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
        log.debug("==> Columns: " + getTableLM().getColumns());
        KnuthBox headerAsFirst = null;
        KnuthBox headerAsSecondToLast = null;
        KnuthBox footerAsLast = null;
        if (headerIter != null) {
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
                //returnList.add(0, box);
            } else {
                headerAsSecondToLast = box;
                //returnList.add(box);
            }
        }
        if (footerIter != null) {
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
                //returnList.add(box);
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
            if (!isSeparateBorderModel()) {
                resolveNormalBeforeAfterBordersForRowGroup(rowGroup, iter);
            }
            createElementsForRowGroup(context, alignment, bodyType, 
                        returnList, rowGroup);
        }
        
        //Remove last penalty
        KnuthElement last = (KnuthElement)returnList.getLast();
        if (last.isPenalty() && last.getW() == 0 && last.getP() == 0) {
            returnList.removeLast();
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
                iter.backToPreviewRow();
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
                //TODO This block in untested!
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
            for (int j = 0; j < row.getGridUnits().size(); j++) {
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
                            if (!bpd.getMinimum().isAuto()) {
                                minContentHeight = Math.max(minContentHeight, 
                                        bpd.getMinimum().getLength().getValue());
                            }
                            MinOptMaxUtil.restrict(explicitRowHeights[rgi], bpd);
                            
                        }

                        //Calculate width of cell
                        int spanWidth = 0;
                        for (int i = primary.getStartCol(); 
                                i < primary.getStartCol() + primary.getCell().getNumberColumnsSpanned();
                                i++) {
                            spanWidth += getTableLM().getColumns().getColumn(i + 1)
                                .getColumnWidth().getValue();
                        }
                        LayoutContext childLC = new LayoutContext(0);
                        childLC.setStackLimit(context.getStackLimit()); //necessary?
                        childLC.setRefIPD(spanWidth);
                        
                        //Get the element list for the cell contents
                        LinkedList elems = primary.getCellLM().getNextKnuthElements(childLC, alignment);
                        primary.setElements(elems);
                        log.debug("Elements: " + elems);
                    }

                    
                    //Calculate height of cell contents
                    primary.setContentLength(ElementListUtils.calcContentLength(
                            primary.getElements()));
                    maxCellHeight = Math.max(maxCellHeight, primary.getContentLength());

                    //Calculate height of row, see CSS21, 17.5.3 Table height algorithms
                    if (gu.isLastGridUnitRowSpan()) {
                        int effCellContentHeight = minContentHeight;
                        LengthRangeProperty bpd = primary.getCell().getBlockProgressionDimension();
                        if (!bpd.getMinimum().isAuto()) {
                            effCellContentHeight = Math.max(effCellContentHeight,
                                    bpd.getMinimum().getLength().getValue());
                        }
                        if (gu.getRowSpanIndex() == 0) {
                            //TODO ATM only non-row-spanned cells are taken for this
                            MinOptMaxUtil.restrict(explicitRowHeights[rgi], bpd);
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
                        CommonBorderPaddingBackground cbpb 
                            = primary.getCell().getCommonBorderPaddingBackground(); 
                        padding += cbpb.getPaddingBefore(false);
                        padding += cbpb.getPaddingAfter(false);
                        int effRowHeight = effCellContentHeight + padding + borderWidths;
                        for (int previous = 0; previous < gu.getRowSpanIndex(); previous++) {
                            effRowHeight -= rowHeights[rgi - previous - 1].opt;
                        }
                        if (effRowHeight > rowHeights[rgi].min) {
                            //This is the new height of the (grid) row
                            MinOptMaxUtil.extendMinimum(rowHeights[rgi], effRowHeight, false);
                            row.setHeight(rowHeights[rgi]);
                        }
                    }
                    
                    if (gu.isPrimary()) {
                        pgus.add(primary);
                    }
                }
            }

            row.setExplicitHeight(explicitRowHeights[rgi]);
            if (row.getHeight().opt > row.getExplicitHeight().max) {
                log.warn("Contents of row " + row.getIndex() + " violate a maximum constraint "
                        + "in block-progression-dimension. Due to its contents the row grows "
                        + "to " + row.getHeight().opt + " millipoints. The row constraint resolve "
                        + "to " + row.getExplicitHeight());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("rowGroup:");
            for (int i = 0; i < rowHeights.length; i++) {
                log.debug("  height=" + rowHeights[i] + " explicit=" + explicitRowHeights[i]);
            }
        }
        TableStepper stepper = new TableStepper(this);
        LinkedList returnedList = stepper.getCombinedKnuthElementsForRowGroup(
                rowGroup, maxColumnCount, bodyType);
        if (returnedList != null) {
            returnList.addAll(returnedList);
        }
        
    }

    protected int getXOffsetOfGridUnit(GridUnit gu) {
        int col = gu.getStartCol();
        return startXOffset + getTableLM().getColumns().getXOffset(col + 1);
    }
    
    public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
        this.usedBPD = 0;
        RowPainter painter = new RowPainter(layoutContext);

        List positions = new java.util.ArrayList();
        List footerElements = null;
        Position lastPos = null;
        while (parentIter.hasNext()) {
            Position pos = (Position)parentIter.next();
            lastPos = pos;
            if (pos instanceof TableHeaderFooterPosition) {
                TableHeaderFooterPosition thfpos = (TableHeaderFooterPosition)pos;
                //these positions need to be unpacked
                if (thfpos.header) {
                    //header positions for the last part are the second-to-last element and need to
                    //be handled first before all other TableContentPositions
                    PositionIterator nestedIter = new KnuthPossPosIter(thfpos.nestedElements);
                    while (nestedIter.hasNext()) {
                        Position containedPos = (Position)nestedIter.next();
                        if (containedPos instanceof TableContentPosition) {
                            TableContentPosition tcpos = (TableContentPosition)containedPos;
                            painter.handleTableContentPosition(tcpos);
                        } else {
                            log.debug("Ignoring position: " + containedPos);
                        }
                    }
                    painter.addAreasAndFlushRow(true);
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
                PositionIterator nestedIter = new KnuthPossPosIter(penaltyPos.headerElements);
                while (nestedIter.hasNext()) {
                    Position containedPos = (Position)nestedIter.next();
                    if (containedPos instanceof TableContentPosition) {
                        TableContentPosition tcpos = (TableContentPosition)containedPos;
                        painter.handleTableContentPosition(tcpos);
                    } else {
                        log.debug("Ignoring position: " + containedPos);
                    }
                }
                painter.addAreasAndFlushRow(true);
            }
            if (penaltyPos.footerElements != null) {
                footerElements = penaltyPos.footerElements; 
            }
        }

        
        Iterator posIter = positions.iterator();
        //Iterate over all steps
        while (posIter.hasNext()) {
            Position pos = (Position)posIter.next();
            if (pos instanceof TableContentPosition) {
                TableContentPosition tcpos = (TableContentPosition)pos;
                painter.handleTableContentPosition(tcpos);
            } else {
                log.debug("Ignoring position: " + pos);
            }
        }
        painter.addAreasAndFlushRow(true);

        if (footerElements != null) {
            //Positions for footers are simply added at the end
            PositionIterator iter = new KnuthPossPosIter(footerElements);
            while (iter.hasNext()) {
                Position pos = (Position)iter.next();
                if (pos instanceof TableContentPosition) {
                    TableContentPosition tcpos = (TableContentPosition)pos;
                    painter.handleTableContentPosition(tcpos);
                } else {
                    log.debug("Ignoring position: " + pos);
                }
            }
            painter.addAreasAndFlushRow(true);
        }
        
        painter.notifyEndOfSequence();
        this.usedBPD += painter.getAccumulatedBPD();
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
        }
        
        public int getAccumulatedBPD() {
            return this.accumulatedBPD;
        }
        
        public void notifyEndOfSequence() {
            this.accumulatedBPD += lastRowHeight; //for last row
        }
        
        public void handleTableContentPosition(TableContentPosition tcpos) {
            log.debug("===handleTableContentPosition(" + tcpos);
            rowFO = null;
            if (lastRow != tcpos.row && lastRow != null) {
                addAreasAndFlushRow(false);
                yoffset += lastRowHeight;
                this.accumulatedBPD += lastRowHeight;
            }
            lastRow = tcpos.row;
            Iterator partIter = tcpos.gridUnitParts.iterator();
            //Iterate over all grid units in the current step
            while (partIter.hasNext()) {
                GridUnitPart gup = (GridUnitPart)partIter.next();
                log.debug(">" + gup);
                int colIndex = gup.pgu.getStartCol();
                if (gridUnits[colIndex] != gup.pgu) {
                    gridUnits[colIndex] = gup.pgu;
                    start[colIndex] = gup.start;
                    end[colIndex] = gup.end;
                } else {
                    if (gup.end < end[colIndex]) {
                        throw new IllegalStateException("Internal Error: stepper problem");
                    }
                    end[colIndex] = gup.end;
                }
                if (rowFO == null) {
                    //Find the row if any
                    rowFO = gridUnits[colIndex].getRow();
                }
            }
        }
        
        public int addAreasAndFlushRow(boolean forcedFlush) {
            int actualRowHeight = 0;
            int readyCount = 0;
            
            int bt = lastRow.getBodyType();
            rowOffsets[bt].put(new Integer(lastRow.getIndex()), new Integer(yoffset));

            for (int i = 0; i < gridUnits.length; i++) {
                if ((gridUnits[i] != null) 
                        && (forcedFlush || (end[i] == gridUnits[i].getElements().size() - 1))) {
                    log.debug("getting len for " + i + " " 
                            + start[i] + "-" + end[i]);
                    readyCount++;
                    int len = ElementListUtils.calcContentLength(
                            gridUnits[i].getElements(), start[i], end[i]);
                    partLength[i] = len;
                    log.debug("len of part: " + len);
                    if (start[i] == 0 && lastRow.getExplicitHeight().min > 0) {
                        len = Math.max(len, lastRow.getExplicitHeight().opt);
                    }
                    
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
            lastRowHeight = actualRowHeight;
            
            //Add areas for row
            addRowBackgroundArea(rowFO, actualRowHeight, layoutContext.getRefIPD(), yoffset);
            for (int i = 0; i < gridUnits.length; i++) {
                GridUnit currentGU = lastRow.safelyGetGridUnit(i);
                if ((gridUnits[i] != null) 
                        && (forcedFlush || (end[i] == gridUnits[i].getElements().size() - 1))
                        && (currentGU == null || currentGU.isLastGridUnitRowSpan())) {
                    //the last line in the "if" above is to avoid a premature end of an 
                    //row-spanned cell because no GridUnitParts are generated after a cell is
                    //finished with its content. currentGU can be null if there's no grid unit
                    //at this place in the current row (empty cell and no borders to process)
                    if (log.isDebugEnabled()) {
                        log.debug((forcedFlush ? "FORCED " : "") + "flushing..." + i + " " 
                                + start[i] + "-" + end[i]);
                    }
                    addAreasForCell(gridUnits[i], start[i], end[i], 
                            layoutContext, lastRow, yoffset, 
                            partLength[i], actualRowHeight);
                    gridUnits[i] = null;
                    start[i] = 0;
                    end[i] = 0;
                    partLength[i] = 0;
                }
            }
            return actualRowHeight;
        }

        private void addAreasForCell(PrimaryGridUnit pgu, int start, int end, 
                LayoutContext layoutContext, EffRow row, 
                int yoffset, int contentHeight, int rowHeight) {
            int bt = row.getBodyType();
            if (firstRow[bt] < 0) {
                firstRow[bt] = row.getIndex();
            }
            //Determine the first row in this sequence
            //TODO Maybe optimize since addAreasAndFlushRow uses almost the same code
            int startRow = Math.max(pgu.getStartRow(), firstRow[bt]);
            int effYOffset = ((Integer)rowOffsets[bt].get(new Integer(startRow))).intValue();
            int effCellHeight = rowHeight;
            effCellHeight += yoffset - effYOffset;
            log.debug("current row: " + row.getIndex());
            log.debug("start row: " + pgu.getStartRow() + " " + yoffset + " " + effYOffset);
            log.debug("contentHeight: " + contentHeight + " rowHeight=" + rowHeight 
                    + " effCellHeight=" + effCellHeight);
            Cell cellLM = pgu.getCellLM();
            cellLM.setXOffset(getXOffsetOfGridUnit(pgu));
            cellLM.setYOffset(effYOffset);
            cellLM.setContentHeight(contentHeight);
            cellLM.setRowHeight(effCellHeight);
            //cellLM.setRowHeight(row.getHeight().opt);
            cellLM.addAreas(new KnuthPossPosIter(pgu.getElements(), 
                    start, end + 1), layoutContext);
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
            TraitSetter.addBackground(block, row.getCommonBorderPaddingBackground());
            return block;
        }
    }

    public void addRowBackgroundArea(TableRow row, int bpd, int ipd, int yoffset) {
        //Add row background if any
        Block rowBackground = getRowArea(row);
        if (rowBackground != null) {
            rowBackground.setBPD(bpd);
            rowBackground.setIPD(ipd);
            rowBackground.setXOffset(this.startXOffset);
            rowBackground.setYOffset(yoffset);
            getTableLM().addChildArea(rowBackground);
        }
    }
    
    
    /**
     * Sets the overall starting x-offset. Used for proper placement of cells.
     * @param startXOffset starting x-offset (table's start-indent)
     */
    public void setStartXOffset(int startXOffset) {
        this.startXOffset = startXOffset;
    }

    public int getUsedBPD() {
        return this.usedBPD;
    }
    
    protected static class GridUnitPart {
        
        protected PrimaryGridUnit pgu;
        protected int start;
        protected int end;
        
        protected GridUnitPart(PrimaryGridUnit pgu, int start, int end) {
            this.pgu = pgu;
            this.start = start;
            this.end = end;
        }
        
        /** @see java.lang.Object#toString() */
        public String toString() {
            StringBuffer sb = new StringBuffer("Part: ");
            sb.append(start).append("-").append(end);
            sb.append(" ").append(pgu);
            return sb.toString();
        }
        
    }
    
    public static class TableContentPosition extends Position {

        protected List gridUnitParts;
        protected EffRow row;
        
        protected TableContentPosition(LayoutManager lm, List gridUnitParts, 
                EffRow row) {
            super(lm);
            this.gridUnitParts = gridUnitParts;
            this.row = row;
        }
        
        /** @see java.lang.Object#toString() */
        public String toString() {
            StringBuffer sb = new StringBuffer("TableContentPosition {");
            sb.append(gridUnitParts);
            sb.append("}");
            return sb.toString();
        }
    }
    
    public static class TableHeaderFooterPosition extends Position {
        
        protected boolean header;
        protected List nestedElements;
        
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
            sb.append("Position {");
            sb.append(nestedElements);
            sb.append("}");
            return sb.toString();
        }
    }

    public static class TableHFPenaltyPosition extends Position {
        
        protected List headerElements;
        protected List footerElements;
        
        protected TableHFPenaltyPosition(LayoutManager lm) {
            super(lm);
        }
        
        /** @see java.lang.Object#toString() */
        public String toString() {
            StringBuffer sb = new StringBuffer("TableHFPenaltyPosition");
            sb.append(" {");
            sb.append("header:");
            sb.append(headerElements);
            sb.append(", footer:");
            sb.append(footerElements);
            sb.append("}");
            return sb.toString();
        }
    }
    
    private class KnuthBoxCellWithBPD extends KnuthBox {
        
        private PrimaryGridUnit pgu;
        
        public KnuthBoxCellWithBPD(int w, PrimaryGridUnit pgu) {
            super(w, null, true);
            this.pgu = pgu;
        }
    }

}
