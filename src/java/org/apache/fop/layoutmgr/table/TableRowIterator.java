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
 * Iterator that lets the table layout manager step over all of the rows of a part of the
 * table (table-header, table-footer or table-body).
 * <p>Note: This class is not thread-safe.</p>
 */
public class TableRowIterator {

    /** Selects the table-body elements for iteration. */
    public static final int BODY = 0;
    /** Selects the table-header elements for iteration. */
    public static final int HEADER = 1;
    /** Selects the table-footer elements for iteration. */
    public static final int FOOTER = 2;

    /** Logger **/
    private static Log log = LogFactory.getLog(TableRowIterator.class);

    /** The table on which this instance operates. */
    protected Table table;
    /** Column setup of the operated table. */
    private ColumnSetup columns;

    /** Part of the table over which to iterate. One of BODY, HEADER or FOOTER. */
    private int tablePart;

    /** Holds the currently fetched row (TableCell instances). */
    private List currentRow = new java.util.ArrayList();

    /**
     * Holds the grid units of cells from the previous row which will span over the
     * current row. Should be read "previous row's spanning cells". List of GridUnit
     * instances.
     */
    private List previousRowsSpanningCells = new java.util.ArrayList();

    /** Index of the row currently being fetched. */
    private int fetchIndex = -1;

    /** Spans found on the current row which will also span over the next row. */
    private int pendingRowSpans;

    //TODO rows should later be a Jakarta Commons LinkedList so concurrent modifications while
    //using a ListIterator are possible
    /** List of cached rows. This a list of EffRow elements. */
    private List fetchedRows = new java.util.ArrayList();

    /**
     * Index of the row that will be returned at the next iteration step. Note that there
     * is no direct relation between this field and {@link
     * TableRowIterator#fetchIndex}. The fetching of rows and the iterating over them are
     * two different processes. Hence the two indices. */
    private int iteratorIndex = 0;

    //prefetch state
    /**
     * Iterator over the requested table's part(s) (header, footer, body). Note that
     * a table may have several table-body children, hence the iterator.
     */
    private ListIterator tablePartIterator = null;
    /** Iterator over a part's child elements (either table-rows or table-cells). */
    private ListIterator tablePartChildIterator = null;

    /**
     * Creates a new TableRowIterator.
     * @param table the table to iterate over
     * @param columns the column setup for the table
     * @param tablePart indicates what part of the table to iterate over (HEADER, FOOTER, BODY)
     */
    public TableRowIterator(Table table, ColumnSetup columns, int tablePart) {
        this.table = table;
        this.columns = columns;
        this.tablePart = tablePart;
        switch(tablePart) {
            case HEADER: {
                List bodyList = new java.util.ArrayList();
                bodyList.add(table.getTableHeader());
                this.tablePartIterator = bodyList.listIterator();
                break;
            }
            case FOOTER: {
                List bodyList = new java.util.ArrayList();
                bodyList.add(table.getTableFooter());
                this.tablePartIterator = bodyList.listIterator();
                break;
            }
            default: {
                this.tablePartIterator = table.getChildNodes();
            }
        }
    }

    /**
     * Preloads the whole table.
     * <p>Note:This is inefficient for large tables.</p>
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
     * Returns the row at the given index, fetching rows up to the requested one if
     * necessary.
     *
     * @return the requested row, or null if there is no row at the given index (index
     * &lt; 0 or end of table-part reached)
     */
    private EffRow getRow(int index) {
        boolean moreRows = true;
        while (moreRows && fetchedRows.size() <= index) {
            moreRows = prefetchNext();
        }
        // Whatever the value of index, getCachedRow will handle it nicely
        return getCachedRow(index);
    }

    /**
     * Returns the next effective row.
     * @return the requested effective row or null if there is no more row.
     */
    private EffRow getNextRow() {
        return getRow(iteratorIndex++);
    }

    /**
     * Returns the row preceding the given row, without moving the iterator.
     *
     * @param row a row in the iterated table part
     * @return the preceding row, or null if there is no such row (the given row is the
     * first one in the table part)
     */
    public EffRow getPrecedingRow(EffRow row) {
        return getRow(row.getIndex() - 1);
    }

    /**
     * Returns the row following the given row, without moving the iterator.
     *
     * @param row a row in the iterated table part
     * @return the following row, or null if there is no more row
     */
    public EffRow getFollowingRow(EffRow row) {
        return getRow(row.getIndex() + 1);
    }

    /**
     * Sets the iterator to the previous row.
     */
    public void backToPreviousRow() {
        iteratorIndex--;
    }

    /**
     * Returns the first effective row.
     * @return the requested effective row.
     */
    public EffRow getFirstRow() {
        if (fetchedRows.size() == 0) {
            prefetchNext();
        }
        return getCachedRow(0);
    }

    /**
     * Returns the last effective row.
     * <p>Note:This is inefficient for large tables because the whole table
     * if preloaded.</p>
     * @return the requested effective row.
     */
    public EffRow getLastRow() {
        while (prefetchNext()) {
            //nop
        }
        return getCachedRow(fetchedRows.size() - 1);
    }

    /**
     * Returns a cached effective row. If the given index points outside the range of rows
     * (negative or greater than the number of already fetched rows), this methods
     * terminates nicely by returning null.
     * 
     * @param index index of the row (zero-based)
     * @return the requested effective row or null if (index &lt; 0 || index &gt;= the
     * number of already fetched rows)
     */
    public EffRow getCachedRow(int index) {
        if (index < 0 || index >= fetchedRows.size()) {
            return null;
        } else {
            return (EffRow)fetchedRows.get(index);
        }
    }

    /**
     * Fetches the next row.
     * 
     * @return true if there was a row to fetch; otherwise, false (the end of the
     * table-part has been reached)
     */
    private boolean prefetchNext() {
        boolean firstInTable = false;
        boolean firstInTablePart = false;
        // If we are at the end of the current table part
        if (tablePartChildIterator != null && !tablePartChildIterator.hasNext()) {
            //force skip on to next component
            if (pendingRowSpans > 0) {
                this.currentRow.clear();
                this.fetchIndex++;
                EffRow gridUnits = buildGridRow(this.currentRow, null);
                log.debug(gridUnits);
                fetchedRows.add(gridUnits);
                return true;
            }
            tablePartChildIterator = null;
            if (fetchedRows.size() > 0) {
                getCachedRow(fetchedRows.size() - 1).setFlagForAllGridUnits(
                        GridUnit.LAST_IN_PART, true);
            }
        }
        // If the iterating over the current table-part has not started yet
        if (tablePartChildIterator == null) {
            if (tablePartIterator.hasNext()) {
                tablePartChildIterator = ((TableBody)tablePartIterator.next()).getChildNodes();
                if (fetchedRows.size() == 0) {
                    firstInTable = true;
                }
                firstInTablePart = true;
            } else {
                //no more rows in that part of the table
                if (fetchedRows.size() > 0) {
                    getCachedRow(fetchedRows.size() - 1).setFlagForAllGridUnits(
                            GridUnit.LAST_IN_PART, true);
                    // If the last row is the last of the table
                    if (tablePart == FOOTER
                            || (tablePart == BODY && table.getTableFooter() == null)) {
                        getCachedRow(fetchedRows.size() - 1).setFlagForAllGridUnits(
                                GridUnit.LAST_IN_TABLE, true);
                    }
                }
                return false;
            }
        }
        Object node = tablePartChildIterator.next();
        while (node instanceof Marker) {
            node = tablePartChildIterator.next();
        }
        this.currentRow.clear();
        this.fetchIndex++;
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
                while (tablePartChildIterator.hasNext()) {
                    TableCell cell = (TableCell)tablePartChildIterator.next();
                    if (cell.startsRow()) {
                        //next row already starts here, one step back
                        tablePartChildIterator.previous();
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
        if (firstInTablePart) {
            gridUnits.setFlagForAllGridUnits(GridUnit.FIRST_IN_PART, true);
        }
        if (firstInTable && (tablePart == HEADER || table.getTableHeader() == null)
                && tablePart != FOOTER) {
            gridUnits.setFlagForAllGridUnits(GridUnit.FIRST_IN_TABLE, true);
        }
        log.debug(gridUnits);
        fetchedRows.add(gridUnits);
        return true;
    }

    /**
     * Places the given object at the given position in the list, first extending it if
     * necessary with null objects to reach the position.
     *
     * @param list the list in which to place the object
     * @param position index at which the object must be placed (0-based)
     * @param obj the object to place
     */
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

    /**
     * Builds the list of grid units corresponding to the given table row.
     *
     * @param cells list of cells belonging to the row
     * @param rowFO the fo:table-row object containing the row, possibly null
     * @return the list of grid units
     */
    private EffRow buildGridRow(List cells, TableRow rowFO) {
        EffRow row = new EffRow(this.fetchIndex, tablePart);
        List gridUnits = row.getGridUnits();

        TableBody bodyFO = null;

        //Create all row-spanned grid units based on information from the previous row
        int colnum = 1;
        GridUnit[] horzSpan = null;  // Grid units horizontally spanned by a single cell
        if (pendingRowSpans > 0) {
            ListIterator spanIter = previousRowsSpanningCells.listIterator();
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
        if (pendingRowSpans < 0) {
            throw new IllegalStateException("pendingRowSpans must not become negative!");
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
            GridUnit other = (GridUnit)safelyGetListItem(gridUnits, colnum - 1);
            if (other != null) {
                String err = "A table-cell ("
                        + cell.getContextInfo()
                        + ") is overlapping with another ("
                        + other.getCell().getContextInfo()
                        + ") in column " + colnum;
                throw new IllegalStateException(err
                        + " (this should have been catched by FO tree validation)");
            }
            TableColumn col = columns.getColumn(colnum);

            //Add grid unit for primary grid unit
            PrimaryGridUnit gu = new PrimaryGridUnit(cell, col, colnum - 1, this.fetchIndex);
            safelySetListItem(gridUnits, colnum - 1, gu);
            boolean hasRowSpanningLeft = !gu.isLastGridUnitRowSpan();
            if (hasRowSpanningLeft) {
                pendingRowSpans++;
                safelySetListItem(previousRowsSpanningCells, colnum - 1, gu);
            }

            if (gu.hasSpanning()) {
                //Add grid units on spanned slots if any
                horzSpan = new GridUnit[cell.getNumberColumnsSpanned()];
                horzSpan[0] = gu;
                for (int j = 1; j < cell.getNumberColumnsSpanned(); j++) {
                    colnum++;
                    GridUnit guSpan = new GridUnit(gu, columns.getColumn(colnum), colnum - 1, j);
                    //TODO: remove the check below???
                    other = (GridUnit)safelyGetListItem(gridUnits, colnum - 1);
                    if (other != null) {
                        String err = "A table-cell ("
                            + cell.getContextInfo()
                            + ") is overlapping with another ("
                            + other.getCell().getContextInfo()
                            + ") in column " + colnum;
                        throw new IllegalStateException(err
                            + " (this should have been catched by FO tree validation)");
                    }
                    safelySetListItem(gridUnits, colnum - 1, guSpan);
                    if (hasRowSpanningLeft) {
                        pendingRowSpans++;
                        safelySetListItem(previousRowsSpanningCells, colnum - 1, gu);
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
