/*
 * -- $Id$ --
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

import org.apache.fop.layout.Area;
import java.util.Enumeration;

public class RowSpanMgr {
    class SpanInfo {
        int cellHeight;
        int totalRowHeight;
        int rowsRemaining;
        // int numCols; // both V and H span
        TableCell cell;

        SpanInfo(TableCell cell, int cellHeight, int rowsSpanned) {
            this.cell = cell;
            this.cellHeight = cellHeight;
            this.totalRowHeight = 0;
            this.rowsRemaining = rowsSpanned;
        }

        /**
         * Return the height remaining in the span.
         */
        int heightRemaining() {
            int hl = cellHeight - totalRowHeight;
            return (hl > 0) ? hl : 0;
        }

        boolean isInLastRow() {
            return (rowsRemaining == 1);
        }

        boolean finishRow(int rowHeight) {
            totalRowHeight += rowHeight;
            if (--rowsRemaining == 0) {
                if (cell != null) {
                    cell.setRowHeight(totalRowHeight);
                }
                return true;
            } else
                return false;
        }

    }

    private SpanInfo spanInfo[];

    public RowSpanMgr(int numCols) {
        this.spanInfo = new SpanInfo[numCols];
    }

    public void addRowSpan(TableCell cell, int firstCol, int numCols,
                           int cellHeight, int rowsSpanned) {
        spanInfo[firstCol - 1] = new SpanInfo(cell, cellHeight, rowsSpanned);
        for (int i = 0; i < numCols - 1; i++) {
            spanInfo[firstCol + i] = new SpanInfo(null, cellHeight,
                                                  rowsSpanned);    // copy!
        }
    }

    public boolean isSpanned(int colNum) {
        return (spanInfo[colNum - 1] != null);
    }


    public TableCell getSpanningCell(int colNum) {
        if (spanInfo[colNum - 1] != null) {
            return spanInfo[colNum - 1].cell;
        } else
            return null;
    }


    /**
     * Return true if any column has an unfinished vertical span.
     */
    public boolean hasUnfinishedSpans() {
        for (int i = 0; i < spanInfo.length; i++) {
            if (spanInfo[i] != null)
                return true;
        }
        return false;
    }

    /**
     * Done with a row.
     * Any spans with only one row left are done
     * This means that we can now set the total height for this cell box
     * Loop over all cells with spans and find number of rows remaining
     * if rows remaining  = 1, set the height on the cell area and
     * then remove the cell from the list of spanned cells. For other
     * spans, add the rowHeight to the spanHeight.
     */
    public void finishRow(int rowHeight) {
        for (int i = 0; i < spanInfo.length; i++) {
            if (spanInfo[i] != null && spanInfo[i].finishRow(rowHeight))
                spanInfo[i] = null;
        }
    }

    /**
     * If the cell in this column is in the last row of its vertical
     * span, return the height left. If it's not in the last row, or if
     * the content height <= the content height of the previous rows
     * of the span, return 0.
     */
    public int getRemainingHeight(int colNum) {
        if (spanInfo[colNum - 1] != null) {
            return spanInfo[colNum - 1].heightRemaining();
        } else
            return 0;
    }

    public boolean isInLastRow(int colNum) {
        if (spanInfo[colNum - 1] != null) {
            return spanInfo[colNum - 1].isInLastRow();
        } else
            return false;
    }

}
