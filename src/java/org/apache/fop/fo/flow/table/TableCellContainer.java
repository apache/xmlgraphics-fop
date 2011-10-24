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

import java.util.List;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;

/**
 * A common class for fo:table-body and fo:table-row which both can contain fo:table-cell.
 */
public abstract class TableCellContainer extends TableFObj
        implements ColumnNumberManagerHolder, CommonAccessibilityHolder {

    private CommonAccessibility commonAccessibility;

    /** list of pending spans */
    protected List pendingSpans;

    /** column number manager */
    protected ColumnNumberManager columnNumberManager;

    /**
     * Construct table cell container.
     * @param parent the parent node of the cell container
     */
    public TableCellContainer(FONode parent) {
        super(parent);
    }

    @Override
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        commonAccessibility = CommonAccessibility.getInstance(pList);
    }

    /**
     * Add cell to current row.
     * @param cell a table cell to add
     * @param firstRow true is first row
     * @throws FOPException if exception occurs
     */
    protected void addTableCellChild(TableCell cell, boolean firstRow) throws FOPException {
        int colNumber = cell.getColumnNumber();
        int colSpan = cell.getNumberColumnsSpanned();
        int rowSpan = cell.getNumberRowsSpanned();

        Table t = getTable();
        if (t.hasExplicitColumns()) {
            if (colNumber + colSpan - 1 > t.getNumberOfColumns()) {
                TableEventProducer eventProducer = TableEventProducer.Provider.get(
                        getUserAgent().getEventBroadcaster());
                eventProducer.tooManyCells(this, getLocator());
            }
        } else {
            t.ensureColumnNumber(colNumber + colSpan - 1);
            // re-cap the size of pendingSpans
            while (pendingSpans.size() < colNumber + colSpan - 1) {
                pendingSpans.add(null);
            }
        }
        if (firstRow) {
            handleCellWidth(cell, colNumber, colSpan);
        }

        /* if the current cell spans more than one row,
         * update pending span list for the next row
         */
        if (rowSpan > 1) {
            for (int i = 0; i < colSpan; i++) {
                pendingSpans.set(colNumber - 1 + i, new PendingSpan(rowSpan));
            }
        }

        columnNumberManager.signalUsedColumnNumbers(colNumber, colNumber + colSpan - 1);

        t.getRowGroupBuilder().addTableCell(cell);
    }

    private void handleCellWidth(TableCell cell, int colNumber, int colSpan) throws FOPException {
        Table t = getTable();
        Length colWidth = null;

        if (cell.getWidth().getEnum() != EN_AUTO
                && colSpan == 1) {
            colWidth = cell.getWidth();
        }

        for (int i = colNumber; i < colNumber + colSpan; ++i) {
            TableColumn col = t.getColumn(i - 1);
            if (colWidth != null) {
                col.setColumnWidth(colWidth);
            }
        }
    }

    /**
     * Returns the enclosing table-header/footer/body of this container.
     *
     * @return <code>this</code> for TablePart, or the parent element for TableRow
     */
    abstract TablePart getTablePart();

    /** {@inheritDoc} */
    public ColumnNumberManager getColumnNumberManager() {
        return columnNumberManager;
    }

    public CommonAccessibility getCommonAccessibility() {
        return commonAccessibility;
    }

}
