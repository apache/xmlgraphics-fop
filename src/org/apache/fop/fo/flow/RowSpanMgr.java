/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.fo.flow;

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

    private boolean ignoreKeeps = false;

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

    /**
     * helper method to prevent infinite loops if
     * keeps or spans are not fitting on a page
     * @param <code>true</code> if keeps and spans should be ignored
     */
    public void setIgnoreKeeps(boolean ignoreKeeps) {
        this.ignoreKeeps = ignoreKeeps;
    }

    /**
     * helper method (i.e. hack ;-) to prevent infinite loops if
     * keeps or spans are not fitting on a page
     * @return true if keeps or spans should be ignored
     */
    public boolean ignoreKeeps() {
        return ignoreKeeps;
    }

}
