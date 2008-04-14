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

package org.apache.fop.fo.flow.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.ValidationException;


/**
 * A row group builder optimised for a fixed number of columns, known before the parsing
 * of cells starts (that is, if the fo:table has explicit fo:table-column children).
 */
class FixedColRowGroupBuilder extends RowGroupBuilder {

    /** Number of columns in the corresponding table. */
    private int numberOfColumns;

    private TableRow currentTableRow = null;

    /** 0-based, index in the row group. */
    private int currentRowIndex;

    /** The rows belonging to this row group. List of List of {@link GridUnit}s. */
    private List/*<List<GridUnit>>*/ rows;

    private boolean firstInPart = true;

    /** The last encountered row. This is the last row of the table if it has no footer. */
    private List lastRow;

    private BorderResolver borderResolver;

    FixedColRowGroupBuilder(Table t) {
        super(t);
        numberOfColumns = t.getNumberOfColumns();
        if (t.isSeparateBorderModel()) {
            borderResolver = new SeparateBorderResolver();
        } else {
            borderResolver = new CollapsingBorderResolver(t);
        }
        initialize();
    }

    /**
     * Prepares this builder for creating a new row group.
     */
    private void initialize() {
        rows = new ArrayList();
        currentRowIndex = 0;
    }

    /** {@inheritDoc} */
    void addTableCell(TableCell cell) {
        for (int i = rows.size(); i < currentRowIndex + cell.getNumberRowsSpanned(); i++) {
            List effRow = new ArrayList(numberOfColumns);
            for (int j = 0; j < numberOfColumns; j++) {
                effRow.add(null);
            }
            rows.add(effRow);
        }
        int columnIndex = cell.getColumnNumber() - 1;
        PrimaryGridUnit pgu = new PrimaryGridUnit(cell, columnIndex);
        List row = (List) rows.get(currentRowIndex);
        row.set(columnIndex, pgu);
        // TODO
        GridUnit[] cellRow = new GridUnit[cell.getNumberColumnsSpanned()];
        cellRow[0] = pgu;
        for (int j = 1; j < cell.getNumberColumnsSpanned(); j++) {
            GridUnit gu = new GridUnit(pgu, j, 0);
            row.set(columnIndex + j, gu);
            cellRow[j] = gu;
        }
        pgu.addRow(cellRow);
        for (int i = 1; i < cell.getNumberRowsSpanned(); i++) {
            row = (List) rows.get(currentRowIndex + i);
            cellRow = new GridUnit[cell.getNumberColumnsSpanned()];
            for (int j = 0; j < cell.getNumberColumnsSpanned(); j++) {
                GridUnit gu = new GridUnit(pgu, j, i);
                row.set(columnIndex + j, gu);
                cellRow[j] = gu;
            }
            pgu.addRow(cellRow);
        }
    }

    private static void setFlagForCols(int flag, List row) {
        for (ListIterator iter = row.listIterator(); iter.hasNext();) {
            ((GridUnit) iter.next()).setFlag(flag);
        }
    }

    /** {@inheritDoc} */
    void startTableRow(TableRow tableRow) {
        currentTableRow = tableRow;
    }

    /** {@inheritDoc} */
    void endTableRow() {
        assert currentTableRow != null;
        if (currentRowIndex > 0 && currentTableRow.getBreakBefore() != Constants.EN_AUTO) {
            TableEventProducer eventProducer = TableEventProducer.Provider.get(
                    currentTableRow.getUserAgent().getEventBroadcaster());
            eventProducer.breakIgnoredDueToRowSpanning(this, currentTableRow.getName(), true,
                    currentTableRow.getLocator());
        }
        if (currentRowIndex < rows.size() - 1
                && currentTableRow.getBreakAfter() != Constants.EN_AUTO) {
            TableEventProducer eventProducer = TableEventProducer.Provider.get(
                    currentTableRow.getUserAgent().getEventBroadcaster());
            eventProducer.breakIgnoredDueToRowSpanning(this, currentTableRow.getName(), false,
                    currentTableRow.getLocator());
        }
        for (Iterator iter = ((List) rows.get(currentRowIndex)).iterator(); iter.hasNext();) {
            GridUnit gu = (GridUnit) iter.next();
            // The row hasn't been filled with empty grid units yet
            if (gu != null) {
                gu.setRow(currentTableRow);
            }
        }
        handleRowEnd(currentTableRow);
    }

    /** {@inheritDoc} */
    void endRow(TableBody body) {
        handleRowEnd(body);
    }

    private void handleRowEnd(TableCellContainer container) {
        List currentRow = (List) rows.get(currentRowIndex);
        lastRow = currentRow;
        // Fill gaps with empty grid units
        for (int i = 0; i < numberOfColumns; i++) {
            if (currentRow.get(i) == null) {
                currentRow.set(i, new EmptyGridUnit(table, currentTableRow, i));
            }
        }
        borderResolver.endRow(currentRow, container);
        if (firstInPart) {
            setFlagForCols(GridUnit.FIRST_IN_PART, currentRow);
            firstInPart = false;
        }
        if (currentRowIndex == rows.size() - 1) {
            // Means that the current row has no cell spanning over following rows
            container.getTablePart().addRowGroup(rows);
            initialize();
        } else {
            currentRowIndex++;
        }
        currentTableRow = null;
    }

    /** {@inheritDoc} */
    void startTablePart(TableBody part) {
        firstInPart = true;
        borderResolver.startPart(part);
    }

    /** {@inheritDoc} */
    void endTablePart() throws ValidationException {
        if (rows.size() > 0) {
            throw new ValidationException(
                    "A table-cell is spanning more rows than available in its parent element.");
        }
        setFlagForCols(GridUnit.LAST_IN_PART, lastRow);
        borderResolver.endPart();
    }

    /** {@inheritDoc} */
    void endTable() {
        borderResolver.endTable();
    }
}
