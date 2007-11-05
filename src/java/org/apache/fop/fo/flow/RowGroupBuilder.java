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

package org.apache.fop.fo.flow;

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.layoutmgr.table.GridUnit;
import org.apache.fop.layoutmgr.table.PrimaryGridUnit;

/**
 * A class that creates groups of rows belonging to a same set of spans. The first row of
 * such a group has only cells which don't span over several rows, or whose spanning
 * starts on this row. Similarly, the last row has only non-row spanning cells or spans
 * which end on this row.
 */
abstract class RowGroupBuilder {

    /** Number of columns in the corresponding table. */
    protected int numberOfColumns;

    /** 0-based, index in the row group. */
    private int currentRowIndex;

    private Table table;

    /** The rows belonging to this row group. List of List of {@link GridUnit}s. */
    protected List rows;

    /**
     * Creates and initialises a new builder for the given table.
     * 
     * @param t a table
     */
    protected RowGroupBuilder(Table t) {
        table = t;
        initialize();
    }

    /**
     * Prepares this builder for creating a new row group.
     */
    private void initialize() {
        rows = new ArrayList();
        currentRowIndex = 0;
    }

    /**
     * Adds a table-cell to the row-group, creating {@link GridUnit}s accordingly.
     * 
     * @param cell
     */
    void addTableCell(TableCell cell) {
        for (int i = rows.size(); i < currentRowIndex + cell.getNumberRowsSpanned(); i++) {
            List effRow = new ArrayList(numberOfColumns);
            for (int j = 0; j < numberOfColumns; j++) {
                effRow.add(null);
            }
            rows.add(effRow);
        }
        int columnIndex = cell.getColumnNumber() - 1;
        PrimaryGridUnit pgu = new PrimaryGridUnit(cell, table.getColumn(columnIndex), columnIndex,
                currentRowIndex);
        List row = (List) rows.get(currentRowIndex);
        row.set(columnIndex, pgu);
        for (int j = 1; j < cell.getNumberColumnsSpanned(); j++) {
            row.set(j + columnIndex,
                    new GridUnit(pgu, table.getColumn(columnIndex + j), columnIndex + j, j));
        }
        for (int i = 1; i < cell.getNumberRowsSpanned(); i++) {
            row = (List) rows.get(currentRowIndex + i);
            for (int j = 0; j < cell.getNumberColumnsSpanned(); j++) {
                row.set(j + columnIndex,
                        new GridUnit(pgu, table.getColumn(columnIndex + j), columnIndex + j, j));
            }
        }
        
    }

    /**
     * Signals that a table row has just ended, potentially finishing the current row
     * group.
     * 
     * @param body the table-body containing the row. Its
     * {@link TableBody#addRowGroup(List)} method will be called if the current row group
     * is finished.
     */
    void signalNewRow(TableBody body) {
        if (currentRowIndex == rows.size() - 1) {
            // Means that the current row has no cell spanning over following rows
            body.addRowGroup(rows);
            initialize();
        } else {
            currentRowIndex++;
        }
    }

    /**
     * Finishes and records the last row-group of the given table-body, if any. If there
     * is no fo:table-row and the last cell of the table-body didn't have ends-row="true",
     * then the {@link signalNewRow} method has not been called and the last row group has
     * yet to be recorded.
     * 
     * @param tableBody
     */
    void finishLastRowGroup(TableBody tableBody) {
        if (rows.size() > 0) {
            tableBody.addRowGroup(rows);
        }
        // Reset, in case this rowGroupBuilder is re-used for other
        // table-header/footer/body
        initialize();
    }

}
