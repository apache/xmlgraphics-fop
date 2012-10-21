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
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.area.Block;
import org.apache.fop.area.Trait;
import org.apache.fop.fo.flow.table.ConditionalBorder;
import org.apache.fop.fo.flow.table.EffRow;
import org.apache.fop.fo.flow.table.EmptyGridUnit;
import org.apache.fop.fo.flow.table.GridUnit;
import org.apache.fop.fo.flow.table.PrimaryGridUnit;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableColumn;
import org.apache.fop.fo.flow.table.TablePart;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground.BorderInfo;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.SpaceResolver;
import org.apache.fop.layoutmgr.TraitSetter;

class RowPainter {
    private static Log log = LogFactory.getLog(RowPainter.class);
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

    /** y-offset of the current table part. */
    private int tablePartOffset = 0;
    /** See {@link RowPainter#registerPartBackgroundArea(Block)}. */
    private CommonBorderPaddingBackground tablePartBackground;
    /** See {@link RowPainter#registerPartBackgroundArea(Block)}. */
    private List tablePartBackgroundAreas;

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

    void startTablePart(TablePart tablePart) {
        CommonBorderPaddingBackground background = tablePart.getCommonBorderPaddingBackground();
        if (background.hasBackground()) {
            tablePartBackground = background;
            if (tablePartBackgroundAreas == null) {
                tablePartBackgroundAreas = new ArrayList();
            }
        }
        tablePartOffset = currentRowOffset;
    }

    /**
     * Signals that the end of the current table part is reached.
     *
     * @param lastInBody true if the part is the last table-body element to be displayed
     * on the current page. In which case all the cells must be flushed even if they
     * aren't finished, plus the proper collapsed borders must be selected (trailing
     * instead of normal, or rest if the cell is unfinished)
     * @param lastOnPage true if the part is the last to be displayed on the current page.
     * In which case collapsed after borders for the cells on the last row must be drawn
     * in the outer mode
     */
    void endTablePart(boolean lastInBody, boolean lastOnPage) {
        addAreasAndFlushRow(lastInBody, lastOnPage);

        if (tablePartBackground != null) {
            TableLayoutManager tableLM = tclm.getTableLM();
            for (Iterator iter = tablePartBackgroundAreas.iterator(); iter.hasNext();) {
                Block backgroundArea = (Block) iter.next();
                TraitSetter.addBackground(backgroundArea, tablePartBackground, tableLM,
                        -backgroundArea.getXOffset(), tablePartOffset - backgroundArea.getYOffset(),
                        tableLM.getContentAreaIPD(), currentRowOffset - tablePartOffset);
            }
            tablePartBackground = null;
            tablePartBackgroundAreas.clear();
        }
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
    private void addAreasAndFlushRow(boolean lastInPart, boolean lastOnPage) {
        if (log.isDebugEnabled()) {
            log.debug("Remembering yoffset for row " + currentRow.getIndex() + ": "
                    + currentRowOffset);
        }
        recordRowOffset(currentRow.getIndex(), currentRowOffset);

        // Need to compute the actual row height first
        // and determine border behaviour for empty cells
        boolean firstCellPart = true;
        boolean lastCellPart = true;
        int actualRowHeight = 0;
        for (int i = 0; i < colCount; i++) {
            GridUnit currentGU = currentRow.getGridUnit(i);
            if (currentGU.isEmpty()) {
                continue;
            }
            if (currentGU.getColSpanIndex() == 0
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

            if (firstCellParts[i] != null && !firstCellParts[i].isFirstPart()) {
                firstCellPart = false;
            }
            if (lastCellParts[i] != null && !lastCellParts[i].isLastPart()) {
                lastCellPart = false;
            }
        }

        // Then add areas for cells finishing on the current row
        for (int i = 0; i < colCount; i++) {
            GridUnit currentGU = currentRow.getGridUnit(i);
            if (currentGU.isEmpty() && !tclm.isSeparateBorderModel()) {
                int borderBeforeWhich;
                if (firstCellPart) {
                    if (firstCellOnPage[i]) {
                        borderBeforeWhich = ConditionalBorder.LEADING_TRAILING;
                    } else {
                        borderBeforeWhich = ConditionalBorder.NORMAL;
                    }
                } else {
                    borderBeforeWhich = ConditionalBorder.REST;
                }
                int borderAfterWhich;
                if (lastCellPart) {
                    if (lastInPart) {
                        borderAfterWhich = ConditionalBorder.LEADING_TRAILING;
                    } else {
                        borderAfterWhich = ConditionalBorder.NORMAL;
                    }
                } else {
                    borderAfterWhich = ConditionalBorder.REST;
                }
                addAreaForEmptyGridUnit((EmptyGridUnit)currentGU,
                        currentRow.getIndex(), i,
                        actualRowHeight,
                        borderBeforeWhich, borderAfterWhich,
                        lastOnPage);

                firstCellOnPage[i] = false;
            } else if (currentGU.getColSpanIndex() == 0
                    && (lastInPart || currentGU.isLastGridUnitRowSpan())
                    && firstCellParts[i] != null) {
                assert firstCellParts[i].pgu == currentGU.getPrimary();

                int borderBeforeWhich;
                if (firstCellParts[i].isFirstPart()) {
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

                // when adding the areas for the TableCellLayoutManager this helps with the isLast trait
                // if, say, the first cell of a row has content that fits in the page, but the content of
                // the second cell does not fit this will assure that the isLast trait for the first cell
                // will also be false
                lastCellParts[i].pgu.getCellLM().setLastTrait(lastCellParts[i].isLastPart());
                addAreasForCell(firstCellParts[i].pgu,
                        firstCellParts[i].start, lastCellParts[i].end,
                        actualRowHeight, borderBeforeWhich, borderAfterWhich,
                        lastOnPage);
                firstCellParts[i] = null; // why? what about the lastCellParts[i]?
                Arrays.fill(firstCellOnPage, i, i + currentGU.getCell().getNumberColumnsSpanned(),
                        false);
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
            ListIterator iter = pgu.getElements().listIterator(startIndex);
            // Skip from the content length calculation glues and penalties occurring at the
            // beginning of the page
            boolean nextIsBox = false;
            while (iter.nextIndex() <= endIndex && !nextIsBox) {
                nextIsBox = ((KnuthElement) iter.next()).isBox();
            }
            int len = 0;
            if (((KnuthElement) iter.previous()).isBox()) {
                while (iter.nextIndex() < endIndex) {
                    KnuthElement el = (KnuthElement) iter.next();
                    if (el.isBox() || el.isGlue()) {
                        len += el.getWidth();
                    }
                }
                len += ActiveCell.getElementContentLength((KnuthElement) iter.next());
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
        int currentRowIndex = currentRow.getIndex();
        int startRowIndex;
        int firstRowHeight;
        if (pgu.getRowIndex() >= firstRowIndex) {
            startRowIndex = pgu.getRowIndex();
            if (startRowIndex < currentRowIndex) {
                firstRowHeight = getRowOffset(startRowIndex + 1) - getRowOffset(startRowIndex);
            } else {
                firstRowHeight = rowHeight;
            }
        } else {
            startRowIndex = firstRowIndex;
            firstRowHeight = 0;
        }

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
                startRowIndex == firstRowOnPageIndex, lastOnPage, this, firstRowHeight);
    }

    private void addAreaForEmptyGridUnit(EmptyGridUnit gu, int rowIndex, int colIndex,
            int actualRowHeight,
            int borderBeforeWhich, int borderAfterWhich, boolean lastOnPage) {

        //get effective borders
        BorderInfo borderBefore = gu.getBorderBefore(borderBeforeWhich);
        BorderInfo borderAfter = gu.getBorderAfter(borderAfterWhich);
        BorderInfo borderStart = gu.getBorderStart();
        BorderInfo borderEnd = gu.getBorderEnd();
        if (borderBefore.getRetainedWidth() == 0
                && borderAfter.getRetainedWidth() == 0
                && borderStart.getRetainedWidth() == 0
                && borderEnd.getRetainedWidth() == 0) {
            return; //no borders, no area necessary
        }

        TableLayoutManager tableLM = tclm.getTableLM();
        Table table = tableLM.getTable();
        TableColumn col = tclm.getColumns().getColumn(colIndex + 1);

        //position information
        boolean firstOnPage = (rowIndex == firstRowOnPageIndex);
        boolean inFirstColumn = (colIndex == 0);
        boolean inLastColumn = (colIndex == table.getNumberOfColumns() - 1);

        //determine the block area's size
        int ipd = col.getColumnWidth().getValue(tableLM);
        ipd -= (borderStart.getRetainedWidth() + borderEnd.getRetainedWidth()) / 2;
        int bpd = actualRowHeight;
        bpd -= (borderBefore.getRetainedWidth() + borderAfter.getRetainedWidth()) / 2;

        //generate the block area
        Block block = new Block();
        block.setPositioning(Block.ABSOLUTE);
        block.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
        block.setIPD(ipd);
        block.setBPD(bpd);
        block.setXOffset(tclm.getXOffsetOfGridUnit(colIndex, 1)
                + (borderStart.getRetainedWidth() / 2));
        block.setYOffset(getRowOffset(rowIndex)
                - (borderBefore.getRetainedWidth() / 2));
        boolean[] outer = new boolean[] {firstOnPage, lastOnPage, inFirstColumn,
                inLastColumn};
        TraitSetter.addCollapsingBorders(block,
                borderBefore,
                borderAfter,
                borderStart,
                borderEnd, outer);
        tableLM.addChildArea(block);
    }

    /**
     * Registers the given area, that will be used to render the part of
     * table-header/footer/body background covered by a table-cell. If percentages are
     * used to place the background image, the final bpd of the (fraction of) table part
     * that will be rendered on the current page must be known. The traits can't then be
     * set when the areas for the cell are created since at that moment this bpd is yet
     * unknown. So they will instead be set in
     * {@link #addAreasAndFlushRow(boolean, boolean)}.
     *
     * @param backgroundArea the block of the cell's dimensions that will hold the part
     * background
     */
    void registerPartBackgroundArea(Block backgroundArea) {
        tclm.getTableLM().addBackgroundArea(backgroundArea);
        tablePartBackgroundAreas.add(backgroundArea);
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
    /** Signals that the first table-body instance has started. */
    void startBody() {
        Arrays.fill(firstCellOnPage, true);
    }

    // TODO get rid of that
    /** Signals that the last table-body instance has ended. */
    void endBody() {
        Arrays.fill(firstCellOnPage, false);
    }
}
