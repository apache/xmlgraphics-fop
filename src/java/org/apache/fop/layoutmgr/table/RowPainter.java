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

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.SpaceResolver;

class RowPainter {
    private static Log log = LogFactory.getLog(RowPainter.class);
    /** The fo:table-row containing the currently handled grid rows. */
    private TableRow rowFO = null;
    private int colCount;
    private int yoffset = 0;
    private int accumulatedBPD = 0;
    /** Currently handled row (= last encountered row). */
    private EffRow lastRow = null;
    private LayoutContext layoutContext;
    /**
     * For each part of the table (header, footer, body), index of the first row of that
     * part present on the current page.
     */
    private int[] firstRow = new int[3];
    /**
     * Keeps track of the y-offsets of each row on a page (for body, header and footer separately).
     * This is particularly needed for spanned cells where you need to know the y-offset
     * of the starting row when the area is generated at the time the cell is closed.
     */
    private Map[] rowOffsets = new Map[] {new java.util.HashMap(),
            new java.util.HashMap(), new java.util.HashMap()};

    //These three variables are our buffer to recombine the individual steps into cells
    /** Primary grid units corresponding to the currently handled grid units, per row. */
    private PrimaryGridUnit[] primaryGridUnits;
    /**
     * Index, in the corresponding table cell's list of Knuth elements, of the first
     * element present on the current page, per column.
     */
    private int[] start;
    /**
     * Index, in the corresponding table cell's list of Knuth elements, of the last
     * element present on the current page, per column.
     */
    private int[] end;
    /**
     * Length, for each column, of the elements from the current cell put on the
     * current page. This is the corresponding area's bpd.
     */
    private int[] partBPD;
    private TableContentLayoutManager tclm;

    public RowPainter(TableContentLayoutManager tclm, LayoutContext layoutContext) {
        this.tclm = tclm;
        this.layoutContext = layoutContext;
        this.colCount = tclm.getColumns().getColumnCount();
        this.primaryGridUnits = new PrimaryGridUnit[colCount];
        this.start = new int[colCount];
        this.end = new int[colCount];
        this.partBPD = new int[colCount];
        Arrays.fill(firstRow, -1);
        Arrays.fill(end, -1);
    }

    public int getAccumulatedBPD() {
        return this.accumulatedBPD;
    }

    /**
     * Records the fragment of row represented by the given position. If it belongs to
     * another (grid) row than the current one, that latter is painted and flushed first.
     * 
     * @param tcpos a position representing the row fragment
     */
    public void handleTableContentPosition(TableContentPosition tcpos) {
        if (lastRow != tcpos.row && lastRow != null) {
            addAreasAndFlushRow(false);
        }
        if (log.isDebugEnabled()) {
            log.debug("===handleTableContentPosition(" + tcpos);
        }
        rowFO = tcpos.row.getTableRow();
        lastRow = tcpos.row;
        Iterator partIter = tcpos.gridUnitParts.iterator();
        //Iterate over all grid units in the current step
        while (partIter.hasNext()) {
            GridUnitPart gup = (GridUnitPart)partIter.next();
            if (log.isDebugEnabled()) {
                log.debug(">" + gup);
            }
            int colIndex = gup.pgu.getStartCol();
            if (primaryGridUnits[colIndex] != gup.pgu) {
                if (primaryGridUnits[colIndex] != null) {
                    log.warn("Replacing GU in slot " + colIndex
                            + ". Some content may not be painted.");
                }
                primaryGridUnits[colIndex] = gup.pgu;
                start[colIndex] = gup.start;
                end[colIndex] = gup.end;
            } else {
                if (gup.end < end[colIndex]) {
                    throw new IllegalStateException("Internal Error: stepper problem");
                }
                end[colIndex] = gup.end;
            }
        }
    }

    /**
     * Create the areas corresponding to the last row. This method is called either
     * because the row is finished (all of the elements present on this row have been
     * added), or because this is the last row on the current page, and the part of it
     * lying on the current page must be drawn.
     * 
     * @param forcedFlush true if the elements must be drawn even if the row isn't
     * finished yet (last row on the page), or if the row is the last of the current table
     * part
     * @return the height of the (grid) row
     */
    public int addAreasAndFlushRow(boolean forcedFlush) {
        int actualRowHeight = 0;

        int bt = lastRow.getBodyType();
        if (log.isDebugEnabled()) {
            log.debug("Remembering yoffset for row " + lastRow.getIndex() + ": " + yoffset);
        }
        rowOffsets[bt].put(new Integer(lastRow.getIndex()), new Integer(yoffset));

        for (int i = 0; i < primaryGridUnits.length; i++) {
            if ((primaryGridUnits[i] != null)
                    && (forcedFlush || (end[i] == primaryGridUnits[i].getElements().size() - 1))) {
                actualRowHeight = Math.max(actualRowHeight, computeSpanHeight(
                        primaryGridUnits[i], start[i], end[i], i, bt));
            }
        }
        actualRowHeight += 2 * tclm.getTableLM().getHalfBorderSeparationBPD();

        //Add areas for row
        tclm.addRowBackgroundArea(rowFO, actualRowHeight, layoutContext.getRefIPD(), yoffset);
        for (int i = 0; i < primaryGridUnits.length; i++) {
            GridUnit currentGU = lastRow.safelyGetGridUnit(i);
            //currentGU can be null if there's no grid unit
            //at this place in the current row (empty cell and no borders to process)
            
            if (primaryGridUnits[i] != null) {
                if (forcedFlush || ((end[i] == primaryGridUnits[i].getElements().size() - 1)
                        && (currentGU == null || currentGU.isLastGridUnitRowSpan()))) {
                    //the last line in the "if" above is to avoid a premature end of a
                    //row-spanned cell because no GridUnitParts are generated after a cell is
                    //finished with its content.
                    //See table-cell_number-rows-spanned_bug38397.xml
                    addAreasForCell(primaryGridUnits[i], start[i], end[i], lastRow, partBPD[i],
                            actualRowHeight);
                    primaryGridUnits[i] = null;
                    start[i] = 0;
                    end[i] = -1;
                    partBPD[i] = 0;
                }
            } else if (currentGU != null && !currentGU.isEmpty()
                    && currentGU.getColSpanIndex() == 0
                    && (forcedFlush || currentGU.isLastGridUnitRowSpan())) {
                //A row-spanned cell has finished contributing content on the previous page
                //and now still has to cause grid units to be painted.
                //See table-cell_page-break_span.xml
                addAreasForCell(currentGU.getPrimary(), start[i], end[i], lastRow, partBPD[i],
                        actualRowHeight);
                start[i] = 0;
                end[i] = -1;
                partBPD[i] = 0;
            }
        }
        yoffset += actualRowHeight;
        accumulatedBPD += actualRowHeight;
        if (forcedFlush) {
            // Either the end of the page is reached, then this was the last call of this
            // method and we no longer care about lastRow; or the end of a table-part
            // (header, footer, body) has been reached, and the next row will anyway be
            // different from the current one, and this is unnecessary to recall this
            // method in the first lines of handleTableContentPosition, so we may reset
            // lastRow
            lastRow = null;
        }
        return actualRowHeight;
    }

    /**
     * Computes the total height of the part of the given cell spanning on the current
     * active row, including borders and paddings. The bpd is also stored in partBPD, and
     * it is ensured that the cell's or row's explicit height is respected. yoffset is
     * updated accordingly.
     * 
     * @param pgu primary grid unit corresponding to the cell
     * @param start index of the first element of the cell occuring on the current page
     * @param end index of the last element of the cell occuring on the current page
     * @param columnIndex column index of the cell
     * @param bodyType {@link TableRowIterator#HEADER}, {@link TableRowIterator#FOOTER}, or
     * {@link TableRowIterator#BODY}
     * @return the cell's height
     */
    private int computeSpanHeight(PrimaryGridUnit pgu, int start, int end, int columnIndex,
            int bodyType) {
        if (log.isTraceEnabled()) {
            log.trace("getting len for " + columnIndex + " "
                    + start + "-" + end);
        }
        int actualStart = start;
        // Skip from the content length calculation glues and penalties occuring at the
        // beginning of the page
        while (actualStart <= end && !((KnuthElement)pgu.getElements().get(actualStart)).isBox()) {
            actualStart++;
        }
        int len = ElementListUtils.calcContentLength(
                pgu.getElements(), actualStart, end);
        KnuthElement el = (KnuthElement)pgu.getElements().get(end);
        if (el.isPenalty()) {
            len += el.getW();
        }
        partBPD[columnIndex] = len;
        if (log.isTraceEnabled()) {
            log.trace("len of part: " + len);
        }

        if (start == 0) {
            LengthRangeProperty bpd = pgu.getCell()
                    .getBlockProgressionDimension();
            if (!bpd.getMinimum(tclm.getTableLM()).isAuto()) {
                int min = bpd.getMinimum(tclm.getTableLM())
                            .getLength().getValue(tclm.getTableLM());
                if (min > 0) {
                    len = Math.max(len, min);
                }
            }
            if (!bpd.getOptimum(tclm.getTableLM()).isAuto()) {
                int opt = bpd.getOptimum(tclm.getTableLM())
                            .getLength().getValue(tclm.getTableLM());
                if (opt > 0) {
                    len = Math.max(len, opt);
                }
            }
            if (pgu.getRow() != null) {
                bpd = pgu.getRow().getBlockProgressionDimension();
                if (!bpd.getMinimum(tclm.getTableLM()).isAuto()) {
                    int min = bpd.getMinimum(tclm.getTableLM()).getLength()
                                .getValue(tclm.getTableLM());
                    if (min > 0) {
                        len = Math.max(len, min);
                    }
                }
            }
        }

        // Add the padding if any
        len += pgu.getBorders()
                        .getPaddingBefore(false, pgu.getCellLM());
        len += pgu.getBorders()
                        .getPaddingAfter(false, pgu.getCellLM());

        //Now add the borders to the contentLength
        if (tclm.isSeparateBorderModel()) {
            len += pgu.getBorders().getBorderBeforeWidth(false);
            len += pgu.getBorders().getBorderAfterWidth(false);
        } else {
            len += pgu.getHalfMaxBeforeBorderWidth();
            len += pgu.getHalfMaxAfterBorderWidth();
        }
        int startRow = Math.max(pgu.getStartRow(), firstRow[bodyType]);
        Integer storedOffset = (Integer)rowOffsets[bodyType].get(new Integer(startRow));
        int effYOffset;
        if (storedOffset != null) {
            effYOffset = storedOffset.intValue();
        } else {
            effYOffset = yoffset;
        }
        len -= yoffset - effYOffset;
        return len;
    }

    private void addAreasForCell(PrimaryGridUnit pgu, int startPos, int endPos,
            EffRow row, int contentHeight, int rowHeight) {
        int bt = row.getBodyType();
        if (firstRow[bt] < 0) {
            firstRow[bt] = row.getIndex();
        }
        //Determine the first row in this sequence
        int startRowIndex = Math.max(pgu.getStartRow(), firstRow[bt]);
        int lastRowIndex = lastRow.getIndex();

        // In collapsing-border model, if the cell spans over several columns/rows then
        // dedicated areas will be created for each grid unit to hold the corresponding
        // borders. For that we need to know the height of each grid unit, that is of each
        // grid row spanned over by the cell
        int[] spannedGridRowHeights = null;
        if (!tclm.getTableLM().getTable().isSeparateBorderModel() && pgu.hasSpanning()) {
            spannedGridRowHeights = new int[lastRowIndex - startRowIndex + 1];
            int prevOffset = ((Integer)rowOffsets[bt].get(new Integer(startRowIndex))).intValue();
            for (int i = 0; i < lastRowIndex - startRowIndex; i++) {
                int newOffset = ((Integer) rowOffsets[bt].get(new Integer(startRowIndex + i + 1)))
                        .intValue();
                spannedGridRowHeights[i] = newOffset - prevOffset;
                prevOffset = newOffset;
            }
            spannedGridRowHeights[lastRowIndex - startRowIndex] = rowHeight;
        }

        //Determine y offset for the cell
        Integer offset = (Integer)rowOffsets[bt].get(new Integer(startRowIndex));
        while (offset == null) {
            //TODO Figure out what this does and when it's triggered
            //This block is probably never used, at least it's not triggered by any of our tests
            startRowIndex--;
            offset = (Integer)rowOffsets[bt].get(new Integer(startRowIndex));
        }
        int effYOffset = offset.intValue();
        int effCellHeight = rowHeight;
        effCellHeight += yoffset - effYOffset;
        if (log.isDebugEnabled()) {
            log.debug("Creating area for cell:");
            log.debug("  current row: " + row.getIndex());
            log.debug("  start row: " + pgu.getStartRow() + " " + yoffset + " " + effYOffset);
            log.debug("  contentHeight: " + contentHeight + " rowHeight=" + rowHeight
                    + " effCellHeight=" + effCellHeight);
        }
        TableCellLayoutManager cellLM = pgu.getCellLM();
        cellLM.setXOffset(tclm.getXOffsetOfGridUnit(pgu));
        cellLM.setYOffset(effYOffset);
        cellLM.setContentHeight(contentHeight);
        cellLM.setRowHeight(effCellHeight);
        //cellLM.setRowHeight(row.getHeight().opt);
        int prevBreak = ElementListUtils.determinePreviousBreak(pgu.getElements(), startPos);
        if (endPos >= 0) {
            SpaceResolver.performConditionalsNotification(pgu.getElements(),
                    startPos, endPos, prevBreak);
        }
        cellLM.addAreas(new KnuthPossPosIter(pgu.getElements(), startPos, endPos + 1),
                layoutContext, spannedGridRowHeights, startRowIndex - pgu.getStartRow(),
                lastRowIndex - pgu.getStartRow() + 1);
    }
}
