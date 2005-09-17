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

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;


/**
 * <p>Iterator that lets the table layout manager step over all rows of a table.
 * </p>
 * <p>Note: This class is not thread-safe.
 * </p>
 */
public class TableRowIterator {

    /** Selects the list of table-body elements for iteration. */
    public static final int BODY = 0;
    /** Selects the table-header element for iteration. */
    public static final int HEADER = 1;
    /** Selects the table-footer element for iteration. */
    public static final int FOOTER = 2; 
    
    /** Logger **/
    private static Log log = LogFactory.getLog(TableRowIterator.class);

    /** The table on with this instance operates. */
    protected Table table;
    private ColumnSetup columns;
    private int type;
    
    /** Holds the current row (TableCell instances) */
    private List currentRow = new java.util.ArrayList();
    /** Holds the grid units of cell from the last row while will span over the current row 
     * (GridUnit instance) */
    private List lastRowsSpanningCells = new java.util.ArrayList();
    private int currentRowIndex = -1;
    //TODO rows should later be a Jakarta Commons LinkedList so concurrent modifications while 
    //using a ListIterator are possible
    /** List of cache rows. */
    private List rows = new java.util.ArrayList();
    //private int indexOfFirstRowInList;
    private int currentIndex = -1;
    private int pendingRowSpans;
    
    //prefetch state
    private ListIterator bodyIterator = null;
    private ListIterator childInBodyIterator = null;
    
    /**
     * Creates a new TableRowIterator.
     * @param table the table to iterate over
     * @param columns the column setup for the table
     * @param what indicates what part of the table to iterate over (HEADER, FOOTER, BODY)
     */
    public TableRowIterator(Table table, ColumnSetup columns, int what) {
        this.table = table;
        this.columns = columns;
        this.type = what;
        switch(what) {
            case HEADER: {
                List bodyList = new java.util.ArrayList();
                bodyList.add(table.getTableHeader());
                this.bodyIterator = bodyList.listIterator();
                break;
            }
            case FOOTER: {
                List bodyList = new java.util.ArrayList();
                bodyList.add(table.getTableFooter());
                this.bodyIterator = bodyList.listIterator();
                break;
            }
            default: {
                this.bodyIterator = table.getChildNodes();
            }
        }
    }
    
    /**
     * <p>Preloads the whole table. 
     * </p>
     * <p>Note:This is inefficient for large tables.
     * </p>
     */
    public void prefetchAll() {
        while (prefetchNext()) {
            log.trace("found row...");
        }
    }
    
    /**
     * Returns the next row group if any. A row group in this context is the minimum number of 
     * consecutive rows which contains all spanned grid units of its cells.
     * @return the next row group, or null
     */
    public EffRow[] getNextRowGroup() {
        EffRow firstRowInGroup = getNextRow();
        if (firstRowInGroup == null) {
            return null;
        }
        EffRow lastRowInGroup = firstRowInGroup;
        int lastIndex = lastRowInGroup.getIndex();
        boolean allFinished;
        do {
            allFinished = true;
            Iterator iter = lastRowInGroup.getGridUnits().iterator();
            while (iter.hasNext()) {
                GridUnit gu = (GridUnit)iter.next();
                if (!gu.isLastGridUnitRowSpan()) {
                    allFinished = false;
                    break;
                }
            }
            lastIndex = lastRowInGroup.getIndex();
            if (!allFinished) {
                lastRowInGroup = getNextRow();
                if (lastRowInGroup == null) {
                    allFinished = true;
                }
            }
        } while (!allFinished);
        int rowCount = lastIndex - firstRowInGroup.getIndex() + 1;
        EffRow[] rowGroup = new EffRow[rowCount];
        for (int i = 0; i < rowCount; i++) {
            rowGroup[i] = getCachedRow(i + firstRowInGroup.getIndex());
        }
        return rowGroup;
    }
    
    /**
     * Retuns the next effective row.
     * @return the requested effective row.
     */
    public EffRow getNextRow() {
        currentIndex++;
        boolean moreRows = true;
        while (moreRows && rows.size() < currentIndex + 1) {
            moreRows = prefetchNext();
        }
        if (currentIndex < rows.size()) {
            return getCachedRow(currentIndex);
        } else {
            return null;
        }
    }
    
    /**
     * Sets the iterator to the previous row.
     */
    public void backToPreviousRow() {
        currentIndex--;
    }
    
    /**
     * Returns the first effective row.
     * @return the requested effective row.
     */
    public EffRow getFirstRow() {
        if (rows.size() == 0) {
            prefetchNext();
        }
        return getCachedRow(0);
    }
    
    /**
     * <p>Returns the last effective row.
     * </p>
     * <p>Note:This is inefficient for large tables because the whole table
     * if preloaded.
     * </p>
     * @return the requested effective row.
     */
    public EffRow getLastRow() {
        while (prefetchNext()) {
            //nop
        }
        return getCachedRow(rows.size() - 1);
    }
    
    /**
     * Returns a cached effective row.
     * @param index index of the row (zero-based)
     * @return the requested effective row
     */
    public EffRow getCachedRow(int index) {
        if (index < 0 || index >= rows.size()) {
            return null;
        } else {
            return (EffRow)rows.get(index);
        }
    }
    
    private boolean prefetchNext() {
        boolean firstInTable = false;
        boolean firstInBody = false;
        if (childInBodyIterator != null) {
            if (!childInBodyIterator.hasNext()) {
                //force skip on to next body
                if (pendingRowSpans > 0) {
                    this.currentRow.clear();
                    this.currentRowIndex++;
                    EffRow gridUnits = buildGridRow(this.currentRow, null);
                    log.debug(gridUnits);
                    rows.add(gridUnits);
                    return true;
                }
                childInBodyIterator = null;
                if (rows.size() > 0) {
                    getCachedRow(rows.size() - 1).setFlagForAllGridUnits(
                            GridUnit.LAST_IN_BODY, true);
                }
            }
        }
        if (childInBodyIterator == null) {
            if (bodyIterator.hasNext()) {
                childInBodyIterator = ((TableBody)bodyIterator.next()).getChildNodes();
                if (rows.size() == 0) {
                    firstInTable = true;
                }
                firstInBody = true;
            } else {
                //no more rows
                if (rows.size() > 0) {
                    getCachedRow(rows.size() - 1).setFlagForAllGridUnits(
                            GridUnit.LAST_IN_BODY, true);
                    if ((type == FOOTER || table.getTableFooter() == null) 
                            && type != HEADER) {
                        getCachedRow(rows.size() - 1).setFlagForAllGridUnits(
                                GridUnit.LAST_IN_TABLE, true);
                    }
                }
                return false;
            }
        }
        Object node = childInBodyIterator.next();
        while (node instanceof Marker) {
            node = childInBodyIterator.next();
        }
        this.currentRow.clear();
        this.currentRowIndex++;
        TableRow rowFO = null;
        if (node instanceof TableRow) {
            rowFO = (TableRow)node;
            ListIterator cellIterator = rowFO.getChildNodes();
            while (cellIterator.hasNext()) {
                this.currentRow.add(cellIterator.next());
            }
        } else if (node instanceof TableCell) {
            this.currentRow.add(node);
            if (!((TableCell)node).endsRow()) {
                while (childInBodyIterator.hasNext()) {
                    TableCell cell = (TableCell)childInBodyIterator.next();
                    if (cell.startsRow()) {
                        //next row already starts here, one step back
                        childInBodyIterator.previous();
                        break;
                    }
                    this.currentRow.add(cell);
                    if (cell.endsRow()) {
                        break;
                    }
                }
            }
        } else {
            throw new IllegalStateException("Illegal class found: " + node.getClass().getName());
        }
        EffRow gridUnits = buildGridRow(this.currentRow, rowFO);
        if (firstInBody) {
            gridUnits.setFlagForAllGridUnits(GridUnit.FIRST_IN_BODY, true);
        }
        if (firstInTable && (type == HEADER || table.getTableHeader() == null)
                && type != FOOTER) {
            gridUnits.setFlagForAllGridUnits(GridUnit.FIRST_IN_TABLE, true);
        }
        log.debug(gridUnits);
        rows.add(gridUnits);
        return true;
    }

    private void safelySetListItem(List list, int position, Object obj) {
        while (position >= list.size()) {
            list.add(null);
        }
        list.set(position, obj);
    }
    
    private Object safelyGetListItem(List list, int position) {
        if (position >= list.size()) {
            return null;
        } else {
            return list.get(position);
        }
    }
    
    private EffRow buildGridRow(List cells, TableRow rowFO) {
        EffRow row = new EffRow(this.currentRowIndex, type);
        List gridUnits = row.getGridUnits();
        
        TableBody bodyFO = null;
        
        //Create all row-spanned grid units based on information from the last row
        int colnum = 1;
        GridUnit[] horzSpan = null;
        if (pendingRowSpans > 0) {
            ListIterator spanIter = lastRowsSpanningCells.listIterator();
            while (spanIter.hasNext()) {
                GridUnit gu = (GridUnit)spanIter.next();
                if (gu != null) {
                    if (gu.getColSpanIndex() == 0) {
                        horzSpan = new GridUnit[gu.getCell().getNumberColumnsSpanned()];
                    }
                    GridUnit newGU = gu.createNextRowSpanningGridUnit();
                    newGU.setRow(rowFO);
                    safelySetListItem(gridUnits, colnum - 1, newGU);
                    horzSpan[newGU.getColSpanIndex()] = newGU;
                    if (newGU.isLastGridUnitColSpan()) {
                        //Add the array of row-spanned grid units to the primary grid unit
                        newGU.getPrimary().addRow(horzSpan);
                        horzSpan = null;
                    }
                    if (newGU.isLastGridUnitRowSpan()) {
                        spanIter.set(null);
                        pendingRowSpans--;
                    } else {
                        spanIter.set(newGU);
                    }
                }
                colnum++;
            }
        }
        
        //Transfer available cells to their slots
        colnum = 1;
        ListIterator iter = cells.listIterator();
        while (iter.hasNext()) {
            TableCell cell = (TableCell)iter.next();
            
            colnum = cell.getColumnNumber();

            //TODO: remove the check below???
            //shouldn't happen here, since
            //overlapping cells already caught in 
            //fo.flow.TableCell.bind()...
            if (safelyGetListItem(gridUnits, colnum - 1) != null) {
                log.error("Overlapping cell at position " + colnum);
                //TODO throw layout exception
            }
            TableColumn col = columns.getColumn(colnum);

            //Add grid unit for primary grid unit
            PrimaryGridUnit gu = new PrimaryGridUnit(cell, col, colnum - 1, this.currentRowIndex);
            safelySetListItem(gridUnits, colnum - 1, gu);
            boolean hasRowSpanningLeft = !gu.isLastGridUnitRowSpan();
            if (hasRowSpanningLeft) {
                pendingRowSpans++;
                safelySetListItem(lastRowsSpanningCells, colnum - 1, gu);
            }
            
            if (gu.hasSpanning()) {
                //Add grid units on spanned slots if any
                horzSpan = new GridUnit[cell.getNumberColumnsSpanned()];
                horzSpan[0] = gu;
                for (int j = 1; j < cell.getNumberColumnsSpanned(); j++) {
                    colnum++;
                    GridUnit guSpan = new GridUnit(gu, columns.getColumn(colnum), colnum - 1, j);
                    if (safelyGetListItem(gridUnits, colnum - 1) != null) {
                        log.error("Overlapping cell at position " + colnum);
                        //TODO throw layout exception
                    }
                    safelySetListItem(gridUnits, colnum - 1, guSpan);
                    if (hasRowSpanningLeft) {
                        safelySetListItem(lastRowsSpanningCells, colnum - 1, gu);
                    }
                    horzSpan[j] = guSpan;
                }
                gu.addRow(horzSpan);
            }
            
            //Gather info for empty grid units (used later)
            if (bodyFO == null) {
                bodyFO = gu.getBody();
            }
            
            colnum++;
        }
        
        //Post-processing the list (looking for gaps and resolve start and end borders)
        fillEmptyGridUnits(gridUnits, rowFO, bodyFO);
        resolveStartEndBorders(gridUnits);
        
        return row;
    }
    
    private void fillEmptyGridUnits(List gridUnits, TableRow row, TableBody body) {
        for (int pos = 1; pos <= gridUnits.size(); pos++) {
            GridUnit gu = (GridUnit)gridUnits.get(pos - 1);
            
            //Empty grid units
            if (gu == null) {
                //Add grid unit
                gu = new EmptyGridUnit(row, columns.getColumn(pos), body, 
                        pos - 1);
                gridUnits.set(pos - 1, gu);
            }
            
            //Set flags
            gu.setFlag(GridUnit.IN_FIRST_COLUMN, (pos == 1));
            gu.setFlag(GridUnit.IN_LAST_COLUMN, (pos == gridUnits.size()));
        }
    }
    
    private void resolveStartEndBorders(List gridUnits) {
        for (int pos = 1; pos <= gridUnits.size(); pos++) {
            GridUnit starting = (GridUnit)gridUnits.get(pos - 1);
         
            //Border resolution
            if (table.isSeparateBorderModel()) {
                starting.assignBorderForSeparateBorderModel();
            } else {
                //Neighbouring grid unit at start edge 
                GridUnit start = null;
                int find = pos - 1;
                while (find >= 1) {
                    GridUnit candidate = (GridUnit)gridUnits.get(find - 1);
                    if (candidate.isLastGridUnitColSpan()) {
                        start = candidate;
                        break;
                    }
                    find--;
                }
                
                //Ending grid unit for current cell
                GridUnit ending = null;
                if (starting.getCell() != null) {
                    pos += starting.getCell().getNumberColumnsSpanned() - 1;
                }
                ending = (GridUnit)gridUnits.get(pos - 1);
                
                //Neighbouring grid unit at end edge 
                GridUnit end = null;
                find = pos + 1;
                while (find <= gridUnits.size()) {
                    GridUnit candidate = (GridUnit)gridUnits.get(find - 1);
                    if (candidate.isPrimary()) {
                        end = candidate;
                        break;
                    }
                    find++;
                }
                starting.resolveBorder(start, 
                        CommonBorderPaddingBackground.START);
                ending.resolveBorder(end, 
                        CommonBorderPaddingBackground.END);
                //Only start and end borders here, before and after during layout
            }
        }
    }

}
