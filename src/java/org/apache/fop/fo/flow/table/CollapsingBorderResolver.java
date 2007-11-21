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

import java.util.Iterator;
import java.util.List;

import org.apache.fop.fo.properties.CommonBorderPaddingBackground;

/**
 * A class that implements the border-collapsing model.
 */
class CollapsingBorderResolver implements BorderResolver {

    private Table table;

    private List previousRow;

    /**
     * The flow of rows is interrupted by the table-footer. Save the header's last row (if
     * any) for resolution between it and the body's first row.
     */
    private List previousRowSave;

    private TableBody currentTablePart;

    private boolean firstInTable;

    private boolean firstInPart;

    private List footerFirstRow;

    private List footerLastRow;

    private boolean inFooter;

    CollapsingBorderResolver(Table table) {
        this.table = table;
        firstInTable = true;
    }

    /** {@inheritDoc} */
    public void endRow(List row, TableCellContainer container) {
        // Resolve before- and after-borders for the table-row
        if (container instanceof TableRow) {
            TableRow tableRow = (TableRow) container;
            for (Iterator iter = row.iterator(); iter.hasNext();) {
                GridUnit gu = (GridUnit) iter.next();
                if (gu.getRowSpanIndex() == 0) {
                    gu.resolveBorder(CommonBorderPaddingBackground.BEFORE, tableRow);
                }
                if (gu.isLastGridUnitRowSpan()) {
                    gu.resolveBorder(CommonBorderPaddingBackground.AFTER, tableRow);
                }
            }
        }
        if (inFooter) {
            if (footerFirstRow == null) {
                footerFirstRow = row;
            }
            footerLastRow = row;
        } else if (firstInTable) {
            // Resolve border-before for the first row in the table
            for (int i = 0; i < row.size(); i++) {
                TableColumn column = table.getColumn(i);
                ((GridUnit) row.get(i)).resolveBorder(CommonBorderPaddingBackground.BEFORE, column);
            }
            firstInTable = false;
        }
        if (firstInPart) {
            // Resolve border-before for the first row in the part
            for (int i = 0; i < row.size(); i++) {
                ((GridUnit) row.get(i)).resolveBorder(CommonBorderPaddingBackground.BEFORE,
                        currentTablePart);
            }
            firstInPart = false;
        }
        if (previousRow != null) {
            // Resolve after/before borders between rows
            for (int i = 0; i < row.size(); i++) {
                GridUnit gu = (GridUnit) row.get(i);
                if (gu.getRowSpanIndex() == 0) {
                    GridUnit beforeGU = (GridUnit) previousRow.get(i);
                    gu.resolveBorder(beforeGU, CommonBorderPaddingBackground.BEFORE);
                }
            }
        }
        // Resolve start/end borders in the row
        Iterator guIter = row.iterator();
        GridUnit gu = (GridUnit) guIter.next();
        gu.resolveBorder(CommonBorderPaddingBackground.START, container);
        while (guIter.hasNext()) {
            GridUnit guEnd = (GridUnit) guIter.next();
            if (gu.isLastGridUnitColSpan()) {
                gu.resolveBorder(guEnd, CommonBorderPaddingBackground.END);
            }
            gu = guEnd;
        }
        gu.resolveBorder(CommonBorderPaddingBackground.END, container);

        previousRow = row;
    }

    /** {@inheritDoc} */
    public void startPart(TableBody part) {
        firstInPart = true;
        currentTablePart = part;
        if (part.isTableFooter()) {
            inFooter = true;
            previousRowSave = previousRow;
            previousRow = null;
        }
    }

    /** {@inheritDoc} */
    public void endPart(TableBody part) {
        // Resolve border-after for the last row in the part
        for (int i = 0; i < previousRow.size(); i++) {
            ((GridUnit) previousRow.get(i))
                    .resolveBorder(CommonBorderPaddingBackground.AFTER, part);
        }
        if (inFooter) {
            inFooter = false;
            previousRow = previousRowSave;
        }
    }

    /** {@inheritDoc} */
    public void endTable() {
        if (footerFirstRow != null) {
            // Resolve after/before border between the last row of table-body and the
            // first row of table-footer
            for (int i = 0; i < footerFirstRow.size(); i++) {
                GridUnit gu = (GridUnit) footerFirstRow.get(i);
                GridUnit beforeGU = (GridUnit) previousRow.get(i);
                gu.resolveBorder(beforeGU, CommonBorderPaddingBackground.BEFORE);
            }
        }
        List lastRow;
        if (footerLastRow != null) {
            lastRow = footerLastRow;
        } else {
            lastRow = previousRow;
        }
        // Resolve border-after for the last row of the table
        for (int i = 0; i < lastRow.size(); i++) {
            TableColumn column = table.getColumn(i);
            ((GridUnit) lastRow.get(i)).resolveBorder(CommonBorderPaddingBackground.AFTER, column);
        }
    }
}
