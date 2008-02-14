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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.flow.table.ConditionalBorder;
import org.apache.fop.fo.flow.table.EffRow;
import org.apache.fop.fo.flow.table.GridUnit;
import org.apache.fop.fo.flow.table.PrimaryGridUnit;
import org.apache.fop.fo.flow.table.TableRow;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.SpaceResolver;
import org.apache.fop.traits.BorderProps;

class RowPainter {
    private static Log log = LogFactory.getLog(RowPainter.class);
    /** The fo:table-row containing the currently handled grid rows. */
    private TableRow rowFO = null;
    private int colCount;
    private int currentRowOffset = 0;
    /** Currently handled row (= last encountered row). */
    private EffRow currentRow = null;
    private LayoutContext layoutContext;
    /**
     * Index of the first row of the current part present on the current page.
     */
    private int firstRowIndex;

    /**
     * Index of the very first row on the current page. Needed to properly handle
     * {@link BorderProps#COLLAPSE_OUTER}. This is not the same as {@link #firstRowIndex}
     * when the table has headers!
     */
    private int firstRowOnPageIndex;

    /**
     * Keeps track of the y-offsets of each row on a page.
     * This is particularly needed for spanned cells where you need to know the y-offset
     * of the starting row when the area is generated at the time the cell is closed.
     */
    private List rowOffsets = new ArrayList();

    private int[] cellHeights;
    private boolean[] firstCellOnPage;
    private CellPart[] firstCellParts;
    private CellPart[] lastCellParts;

    private TableContentLayoutManager tclm;

    RowPainter(TableContentLayoutManager tclm, LayoutContext layoutContext) {
        this.tclm = tclm;
        this.layoutContext = layoutContext;
        this.colCount = tclm.getColumns().getColumnCount();
        this.cellHeights = new int[colCount];
        this.firstCellOnPage = new boolean[colCount];
        this.firstCellParts = new CellPart[colCount];
        this.lastCellParts = new CellPart[colCount];
        this.firstRowIndex = -1;
        this.firstRowOnPageIndex = -1;
    }

    int getAccumulatedBPD() {
        return currentRowOffset;
    }

    /**
     * Records the fragment of row represented by the given position. If it belongs to
     * another (grid) row than the current one, that latter is painted and flushed first.
     * 
     * @param tcpos a position representing the row fragment
     */
    void handleTableContentPosition(TableContentPosition tcpos) {
        if (log.isDebugEnabled()) {
            log.debug("===handleTableContentPosition(" + tcpos);
        }
        if (currentRow == null) {
            currentRow = tcpos.getNewPageRow();
        } else {
            EffRow row = tcpos.getRow();
            if (row.getIndex() > currentRow.getIndex()) {
                addAreasAndFlushRow(false, false);
                currentRow = row;
            }
        }
        rowFO = currentRow.getTableRow();
        if (firstRowIndex < 0) {
            firstRowIndex = currentRow.getIndex();
            if (firstRowOnPageIndex < 0) {
                firstRowOnPageIndex = firstRowIndex;
            }
        }
        Iterator partIter = tcpos.cellParts.iterator();
        //Iterate over all grid units in the current step
        while (partIter.hasNext()) {
            CellPart cellPart = (CellPart)partIter.next();
            if (log.isDebugEnabled()) {
                log.debug(">" + cellPart);
            }
            int colIndex = cellPart.pgu.getColIndex();
            if (firstCellParts[colIndex] == null) {
                firstCellParts[colIndex] = cellPart;
                cellHeights[colIndex] = cellPart.getBorderPaddingBefore(firstCellOnPage[colIndex]);
            } else {
                assert firstCellParts[colIndex].pgu == cellPart.pgu; 
                cellHeights[colIndex] += cellPart.getConditionalBeforeContentLength();
            }
            cellHeights[colIndex] += cellPart.getLength();
            lastCellParts[colIndex] = cellPart;
        }
    }

    /**
     * Creates the areas corresponding to the last row. That is, an area with background
     * for the row, plus areas for all the cells that finish on the row (not spanning over
     * further rows).
     * 
     * @param lastInPart true if the row is the last from its table part to be displayed
     * on the current page. In which case all the cells must be flushed even if they
     * aren't finished, plus the proper collapsed borders must be selected (trailing
     * instead of normal, or rest if the cell is unfinished)
     * @param lastOnPage true if the row is the very last row of the table that will be
     * displayed on the current page. In which case collapsed after borders must be drawn
     * in the outer mode
     */
    void addAreasAndFlushRow(boolean lastInPart, boolean lastOnPage) {
        if (log.isDebugEnabled()) {
            log.debug("Remembering yoffset for row " + currentRow.getIndex() + ": "
                    + currentRowOffset);
        }
        recordRowOffset(currentRow.getIndex(), currentRowOffset);

        // Need to compute the actual row height first
        int actualRowHeight = 0;
        for (int i = 0; i < colCount; i++) {
            GridUnit currentGU = currentRow.getGridUnit(i);            
            if (!currentGU.isEmpty() && currentGU.getColSpanIndex() == 0
                    && (lastInPart || currentGU.isLastGridUnitRowSpan())
                    && firstCellParts[i] != null) {
                // TODO
                // The last test above is a workaround for the stepping algorithm's
                // fundamental flaw making it unable to produce the right element list for
                // multiple breaks inside a same row group.
                // (see http://wiki.apache.org/xmlgraphics-fop/TableLayout/KnownProblems)
                // In some extremely rare cases (forced breaks, very small page height), a
                // TableContentPosition produced during row delaying may end up alone on a
                // page. It will not contain the CellPart instances for the cells starting
                // the next row, so firstCellParts[i] will still be null for those ones.
                int cellHeight = cellHeights[i];
                cellHeight += lastCellParts[i].getConditionalAfterContentLength();
                cellHeight += lastCellParts[i].getBorderPaddingAfter(lastInPart);
                int cellOffset = getRowOffset(Math.max(firstCellParts[i].pgu.getRowIndex(),
                        firstRowIndex));
                actualRowHeight = Math.max(actualRowHeight, cellOffset + cellHeight
                        - currentRowOffset);
            }
        }

        // Then add areas for cells finishing on the current row
        tclm.addRowBackgroundArea(rowFO, actualRowHeight, layoutContext.getRefIPD(),
                currentRowOffset);
        for (int i = 0; i < colCount; i++) {
            GridUnit currentGU = currentRow.getGridUnit(i);            
            if (!currentGU.isEmpty() && currentGU.getColSpanIndex() == 0
                    && (lastInPart || currentGU.isLastGridUnitRowSpan())
                    && firstCellParts[i] != null) {
                assert firstCellParts[i].pgu == currentGU.getPrimary();
                int borderBeforeWhich;
                if (firstCellParts[i].start == 0) {
                    if (firstCellOnPage[i]) {
                        borderBeforeWhich = ConditionalBorder.LEADING_TRAILING;
                    } else {
                        borderBeforeWhich = ConditionalBorder.NORMAL;
                    }
                } else {
                    assert firstCellOnPage[i];
                    borderBeforeWhich = ConditionalBorder.REST;
                }
                int borderAfterWhich;
                if (lastCellParts[i].isLastPart()) {
                    if (lastInPart) {
                        borderAfterWhich = ConditionalBorder.LEADING_TRAILING;
                    } else {
                        borderAfterWhich = ConditionalBorder.NORMAL;
                    }
                } else {
                    borderAfterWhich = ConditionalBorder.REST;
                }
                addAreasForCell(firstCellParts[i].pgu,
                        firstCellParts[i].start, lastCellParts[i].end,
                        actualRowHeight, borderBeforeWhich, borderAfterWhich,
                        lastOnPage);
                firstCellParts[i] = null;
                firstCellOnPage[i] = false;
            }
        }
        currentRowOffset += actualRowHeight;
        if (lastInPart) {
            /*
             * Either the end of the page is reached, then this was the last call of this
             * method and we no longer care about currentRow; or the end of a table-part
             * (header, footer, body) has been reached, and the next row will anyway be
             * different from the current one, and this is unnecessary to call this method
             * again in the first lines of handleTableContentPosition, so we may reset the
             * following variables.
             */
            currentRow = null;
            firstRowIndex = -1;
            rowOffsets.clear();
            /*
             * The current table part has just been handled. Be it the first one or not,
             * the header or the body, in any case the borders-before of the next row
             * (i.e., the first row of the next part if any) must be painted in
             * COLLAPSE_INNER mode. So the firstRowOnPageIndex indicator must be kept
             * disabled. The following way is not the most elegant one but will be good
             * enough.
             */
            firstRowOnPageIndex = Integer.MAX_VALUE;
        }
    }

    // TODO this is not very efficient and should probably be done another way
    // this method is only necessary when display-align = center or after, in which case
    // the exact content length is needed to compute the size of the empty block that will
    // be used as padding.
    // This should be handled automatically by a proper use of Knuth elements
    private int computeContentLength(PrimaryGridUnit pgu, int startIndex, int endIndex) {
        if (startIndex > endIndex) {
             // May happen if the cell contributes no content on the current page (empty
             // cell, in most cases)
            return 0;
        } else {
            int actualStart = startIndex;
            // Skip from the content length calculation glues and penalties occurring at the
            // beginning of the page
            while (actualStart <= endIndex
                    && !((KnuthElement) pgu.getElements().get(actualStart)).isBox()) {
                actualStart++;
            }
            int len = ElementListUtils.calcContentLength(
                    pgu.getElements(), actualStart, endIndex);
            KnuthElement el = (KnuthElement)pgu.getElements().get(endIndex);
            if (el.isPenalty()) {
                len += el.getW();
            }
            return len;
        }
    }

    private void addAreasForCell(PrimaryGridUnit pgu, int startPos, int endPos,
            int rowHeight, int borderBeforeWhich, int borderAfterWhich, boolean lastOnPage) {
        /*
         * Determine the index of the first row of this cell that will be displayed on the
         * current page.
         */
        int startRowIndex = Math.max(pgu.getRowIndex(), firstRowIndex);
        int currentRowIndex = currentRow.getIndex();

        /*
         * In collapsing-border model, if the cell spans over several columns/rows then
         * dedicated areas will be created for each grid unit to hold the corresponding
         * borders. For that we need to know the height of each grid unit, that is of each
         * grid row spanned over by the cell
         */
        int[] spannedGridRowHeights = null;
        if (!tclm.getTableLM().getTable().isSeparateBorderModel() && pgu.hasSpanning()) {
            spannedGridRowHeights = new int[currentRowIndex - startRowIndex + 1];
            int prevOffset = getRowOffset(startRowIndex);
            for (int i = 0; i < currentRowIndex - startRowIndex; i++) {
                int newOffset = getRowOffset(startRowIndex + i + 1);
                spannedGridRowHeights[i] = newOffset - prevOffset;
                prevOffset = newOffset;
            }
            spannedGridRowHeights[currentRowIndex - startRowIndex] = rowHeight;
        }
        int cellOffset = getRowOffset(startRowIndex);
        int cellTotalHeight = rowHeight + currentRowOffset - cellOffset;
        if (log.isDebugEnabled()) {
            log.debug("Creating area for cell:");
            log.debug("  start row: " + pgu.getRowIndex() + " " + currentRowOffset + " "
                    + cellOffset);
            log.debug(" rowHeight=" + rowHeight + " cellTotalHeight=" + cellTotalHeight);
        }
        TableCellLayoutManager cellLM = pgu.getCellLM();
        cellLM.setXOffset(tclm.getXOffsetOfGridUnit(pgu));
        cellLM.setYOffset(cellOffset);
        cellLM.setContentHeight(computeContentLength(pgu, startPos, endPos));
        cellLM.setTotalHeight(cellTotalHeight);
        int prevBreak = ElementListUtils.determinePreviousBreak(pgu.getElements(), startPos);
        if (endPos >= 0) {
            SpaceResolver.performConditionalsNotification(pgu.getElements(),
                    startPos, endPos, prevBreak);
        }
        cellLM.addAreas(new KnuthPossPosIter(pgu.getElements(), startPos, endPos + 1),
                layoutContext, spannedGridRowHeights, startRowIndex - pgu.getRowIndex(),
                currentRowIndex - pgu.getRowIndex(), borderBeforeWhich, borderAfterWhich,
                startRowIndex == firstRowOnPageIndex, lastOnPage);
    }

    /**
     * Records the y-offset of the row with the given index.
     * 
     * @param rowIndex index of the row
     * @param offset y-offset of the row on the page
     */
    private void recordRowOffset(int rowIndex, int offset) {
        /*
         * In some very rare cases a row may be skipped. See for example Bugzilla #43633:
         * in a two-column table, a row contains a row-spanning cell and a missing cell.
         * In TableStepper#goToNextRowIfCurrentFinished this row will immediately be
         * considered as finished, since it contains no cell ending on this row. Thus no
         * TableContentPosition will be created for this row. Thus its index will never be
         * recorded by the #handleTableContentPosition method.
         * 
         * The offset of such a row is the same as the next non-empty row. It's needed
         * to correctly offset blocks for cells starting on this row. Hence the loop
         * below.
         */
        for (int i = rowOffsets.size(); i <= rowIndex - firstRowIndex; i++) {
            rowOffsets.add(new Integer(offset));
        }
    }

    /**
     * Returns the offset of the row with the given index.
     * 
     * @param rowIndex index of the row
     * @return its y-offset on the page
     */
    private int getRowOffset(int rowIndex) {
        return ((Integer) rowOffsets.get(rowIndex - firstRowIndex)).intValue();
    }

    // TODO get rid of that
    void startBody() {
        Arrays.fill(firstCellOnPage, true);
    }

    // TODO get rid of that
    void endBody() {
        Arrays.fill(firstCellOnPage, false);
    }
}
