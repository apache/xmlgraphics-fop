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

import org.apache.fop.fo.ValidationException;

/**
 * A class that creates groups of rows belonging to a same set of spans. The first row of
 * such a group has only cells which don't span over several rows, or whose spanning
 * starts on this row. Similarly, the last row has only non-row spanning cells or spans
 * which end on this row.
 */
abstract class RowGroupBuilder {

    protected Table table;

    /**
     * Creates and initialises a new builder for the given table.
     * 
     * @param t a table
     */
    protected RowGroupBuilder(Table t) {
        table = t;
    }


    /**
     * Adds a table-cell to the current row-group, creating {@link GridUnit}s accordingly.
     * 
     * @param cell the cell to add
     */
    abstract void addTableCell(TableCell cell);

    /**
     * Receives notification of the start of an fo:table-row element.
     * 
     * @param tableRow the row being started
     */
    abstract void startTableRow(TableRow tableRow);

    /**
     * Receives notification of the end of the current row. If the current row finishes
     * the row group, the {@link TableBody#addRowGroup(List)} method of the parent table
     * part will be called.
     */
    abstract void endTableRow();

    /**
     * Receives notification of the end of the current row, when the source contains no
     * fo:table-row element. If the current row finishes the row group, the
     * {@link TableBody#addRowGroup(List)} method of the given table part will be called.
     * 
     * <p>If the source does contain explicit fo:table-row elements, then the
     * {@link #endTableRow()} method will be called instead.</p>
     * 
     * @param part the part containing the current row
     */
    abstract void endRow(TableBody part);

    /**
     * Receives notification of the start of a table-header/footer/body.
     * 
     * @param part the part being started
     */
    abstract void startTablePart(TableBody part);

    /**
     * Receives notification of the end of a table-header/footer/body. The current
     * row-group is checked for emptiness. This row group builder is reset for handling
     * further possible table parts.
     * 
     * @throws ValidationException if a row-spanning cell overflows the given table part
     */
    abstract void endTablePart() throws ValidationException;

    /**
     * Receives notification of the end of the table.
     * 
     * @throws ValidationException if a row-spanning cell overflows one of the table's parts
     */
    abstract void endTable() throws ValidationException;
}
