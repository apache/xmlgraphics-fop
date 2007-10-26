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

import java.util.List;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;

/**
 * A common class for fo:table-body and fo:table-row which both can contain fo:table-cell.
 */
public abstract class TableCellContainer extends TableFObj {

    public TableCellContainer(FONode parent) {
        super(parent);
    }

    protected void addTableCellChild(TableCell cell) throws FOPException {
        Table t = getTable();
        int colNr = cell.getColumnNumber();
        int colSpan = cell.getNumberColumnsSpanned();
        Length colWidth = null;

        if (cell.getWidth().getEnum() != EN_AUTO
                && colSpan == 1) {
            colWidth = cell.getWidth();
        }
        
        for (int i = colNr; i < colNr + colSpan; ++i) {
            if (t.columns.size() < i
                    || t.columns.get(i - 1) == null) {
                t.addDefaultColumn(colWidth, 
                        i == colNr 
                            ? cell.getColumnNumber()
                            : 0);
            } else {
                TableColumn col = 
                    (TableColumn) t.columns.get(i - 1);
                if (!col.isDefaultColumn()
                        && colWidth != null) {
                    col.setColumnWidth(colWidth);
                }
            }
        }
    }

    protected void addChildNode(FONode child) throws FOPException {
        if (!inMarker() 
                && child.getNameId() == FO_TABLE_CELL) {
            /* update current column index for the table-body/table-row */
            updateColumnIndex((TableCell) child);
        }
        super.addChildNode(child);
    }


    private void updateColumnIndex(TableCell cell) {
        
        int rowSpan = cell.getNumberRowsSpanned();
        int colSpan = cell.getNumberColumnsSpanned();
        int columnIndex = getCurrentColumnIndex();
        int i;
        
        if (getNameId() == FO_TABLE_ROW) {
            
            TableRow row = (TableRow) this;
            
            for (i = colSpan; 
                    --i >= 0 || row.pendingSpans.size() < cell.getColumnNumber();) {
                row.pendingSpans.add(null);
            }
            
            /* if the current cell spans more than one row,
             * update pending span list for the next row
             */
            if (rowSpan > 1) {
                for (i = colSpan; --i >= 0;) {
                    row.pendingSpans.set(columnIndex - 1 + i, 
                            new PendingSpan(rowSpan));
                }
            }
        } else {
            
            TableBody body = (TableBody) this;
            
            /* if body.firstRow is still true, and :
             * a) the cell starts a row,
             * b) there was a previous cell 
             * c) that previous cell didn't explicitly end the previous row
             *  => set firstRow flag to false
             */
            if (body.firstRow && cell.startsRow()) {
                if (!body.previousCellEndedRow()) {
                    body.firstRow = false;
                }
            }
            
            /* pendingSpans not initialized for the first row...
             */
            if (body.firstRow) {
                for (i = colSpan; 
                        --i >= 0|| body.pendingSpans.size() < cell.getColumnNumber();) {
                    body.pendingSpans.add(null);
                }
            }
            
            /* if the current cell spans more than one row,
             * update pending span list for the next row
             */
            if (rowSpan > 1) {
                for (i = colSpan; --i >= 0;) {
                    body.pendingSpans.set(columnIndex - 1 + i, 
                            new PendingSpan(rowSpan));
                }
            }
        }

        /* flag column indices used by this cell,
         * take into account that possibly not all column-numbers
         * are used by columns in the parent table (if any),
         * so a cell spanning three columns, might actually
         * take up more than three columnIndices...
         */
        int startIndex = columnIndex - 1;
        int endIndex = startIndex + colSpan;
        if (getTable().columns != null) {
            List cols = getTable().columns;
            int tmpIndex = endIndex;
            for (i = startIndex; i <= tmpIndex; ++i) {
                if (i < cols.size() && cols.get(i) == null) {
                    endIndex++;
                }
            }
        }
        flagColumnIndices(startIndex, endIndex);
        if (getNameId() != FO_TABLE_ROW && cell.endsRow()) {
            ((TableBody) this).firstRow = false;
            ((TableBody) this).resetColumnIndex();
        }
    }

}
