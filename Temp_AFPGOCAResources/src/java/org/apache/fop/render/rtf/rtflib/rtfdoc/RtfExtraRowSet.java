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

package org.apache.fop.render.rtf.rtflib.rtfdoc;

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

import java.io.Writer;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collections;


/**
 * Used to add extra table rows after a row that contains a nested table:
 * <li> created by RtfTableRow before generating RTF code
 * <li> an RtfTableCell that contains a nested table can ask this to put
 *         some of its children in extra rows that after the current row
 * <li> once RtfTableRow is done rendering its children, it renders this,
 *         causing extra rows to be generated, with content that can come
 *         from several RtfTableCells
 *
 * See org.apache.fop.rtf.rtflib.testdocs.NestedTable for an example of
 * usage.
 * @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

public class RtfExtraRowSet extends RtfContainer {
    // TODO what is idnum?
    static final int DEFAULT_IDNUM = 0;

    /** Parent table context
     * (added by Boris Poudérous on july 2002 in order to process nested tables)
     */
    private ITableColumnsInfo parentITableColumnsInfo = null;

    /** While a top-level RtfTableRow is being rendered, we build a list of
     *  RtfTableCells that must be rendered in extra rows.
     *  This holds a cell with positioning information
     */
    private final List cells = new LinkedList();
    private static class PositionedCell
    implements Comparable {
        private final RtfTableCell cell;
        private final int xOffset;
        private final int rowIndex;

        PositionedCell(RtfTableCell c, int index, int offset) {
            cell = c;
            xOffset = offset;
            rowIndex = index;
        }

        /** debugging dump */
        public String toString() {
            return "PositionedCell: row " + rowIndex + ", offset " + xOffset;
        }

        /** cells need to be sorted by row index and then by x offset */
        public int compareTo(Object o) {
            int result = 0;
            if (o == null) {
                result = 1;
            } else if (!(o instanceof PositionedCell)) {
                result = 1;
            } else {
                final PositionedCell pc = (PositionedCell)o;
                if (this.rowIndex < pc.rowIndex) {
                    result = -1;
                } else if (this.rowIndex > pc.rowIndex) {
                    result = 1;
                } else if (this.xOffset < pc.xOffset) {
                    result = -1;
                } else if (this.xOffset > pc.xOffset) {
                    result = 1;
                }
            }

            return result;
        }

        public boolean equals(Object o) {
            return o != null && this.compareTo(o) == 0;
        }
    }

    /** our maximum row index */
    private int maxRowIndex;

    /** an RtfExtraRowSet has no parent, it is only used temporary during
     *  generation of RTF for an RtfTableRow
     */
    RtfExtraRowSet(Writer w)
    throws IOException {
        super(null, w);
    }

    /** Add all cells of given Table to this set for later rendering in extra rows
     *  @return index of extra row to use for elements that follow this table in the same cell
     *  @param rowIndex index of first extra row to create to hold cells of tbl
     *  @param xOffset horizontal position of left edge of first column of tbl
     */
    int addTable(RtfTable tbl, int rowIndex, int xOffset) {
        // process all rows of the table
        for (Iterator it = tbl.getChildren().iterator(); it.hasNext();) {
            final RtfElement e = (RtfElement)it.next();
            if (e instanceof RtfTableRow) {
                addRow((RtfTableRow)e, rowIndex, xOffset);
                rowIndex++;
                maxRowIndex = Math.max(rowIndex, maxRowIndex);
            }
        }
        return rowIndex;
    }

    /** add all cells of given row to this set */
    private void addRow(RtfTableRow row, int rowIndex, int xOffset) {
        for (Iterator it = row.getChildren().iterator(); it.hasNext();) {
            final RtfElement e = (RtfElement)it.next();
            if (e instanceof RtfTableCell) {
                final RtfTableCell c = (RtfTableCell)e;
                cells.add(new PositionedCell(c, rowIndex, xOffset));
                xOffset += c.getCellWidth();
            }
        }
    }

    /** create an extra cell to hold content that comes after a nested table in a cell
     *  Modified by Boris Poudérous in order to permit the extra cell to have
     *  the attributes of its parent cell
     */
    RtfTableCell createExtraCell(int rowIndex, int xOffset, int cellWidth,
                                 RtfAttributes parentCellAttributes)
    throws IOException {
        final RtfTableCell c = new RtfTableCell(null, writer, cellWidth,
                parentCellAttributes, DEFAULT_IDNUM);
        cells.add(new PositionedCell(c, rowIndex, xOffset));
        return c;
    }

    /**
     * render extra RtfTableRows containing all the extra RtfTableCells that we
     * contain
     * @throws IOException for I/O problems
     */
    protected void writeRtfContent() throws IOException {
        // sort cells by rowIndex and xOffset
        Collections.sort(cells);

        // process all extra cells by rendering them into extra rows
        List rowCells = null;
        int rowIndex = -1;
        for (Iterator it = cells.iterator(); it.hasNext();) {
            final PositionedCell pc = (PositionedCell)it.next();
            if (pc.rowIndex != rowIndex) {
                // starting a new row, render previous one
                if (rowCells != null) {
                    writeRow(rowCells);
                }
                rowIndex = pc.rowIndex;
                rowCells = new LinkedList();
            }
            rowCells.add(pc);
        }

        // render last row
        if (rowCells != null) {
            writeRow(rowCells);
        }
    }

    /** write one RtfTableRow containing given PositionedCells */
    private void writeRow(List cells)
    throws IOException {
        if (allCellsEmpty(cells)) {
            return;
        }

        final RtfTableRow row = new RtfTableRow(null, writer, DEFAULT_IDNUM);
        int cellIndex = 0;

        // Get the context of the table that holds the nested table
        ITableColumnsInfo parentITableColumnsInfo = getParentITableColumnsInfo();
        parentITableColumnsInfo.selectFirstColumn();

        // X offset of the current empty cell to add
        float xOffset = 0;
        float xOffsetOfLastPositionedCell = 0;

        for (Iterator it = cells.iterator(); it.hasNext();) {
            final PositionedCell pc = (PositionedCell)it.next();

            // if first cell is not at offset 0, add placeholder cell
            // TODO should be merged with the cell that is above it
            if (cellIndex == 0 && pc.xOffset > 0) {
               /**
                * Added by Boris Poudérous
                */
               // Add empty cells merged vertically with the cells above and with the same widths
               // (BEFORE the cell that contains the nested table)
                for (int i = 0; (xOffset < pc.xOffset)
                        && (i < parentITableColumnsInfo.getNumberOfColumns()); i++) {
                    // Get the width of the cell above
                    xOffset += parentITableColumnsInfo.getColumnWidth();
                    // Create the empty cell merged vertically
                    row.newTableCellMergedVertically((int)parentITableColumnsInfo.getColumnWidth(),
                           pc.cell.attrib);
                    // Select next column in order to have its width
                    parentITableColumnsInfo.selectNextColumn();
                }
            }

            row.addChild(pc.cell);
            // Line added by Boris Poudérous
            xOffsetOfLastPositionedCell = pc.xOffset + pc.cell.getCellWidth();
            cellIndex++;
        }

        /**
         * Added by Boris Poudérous
         */
        // Add empty cells merged vertically with the cells above AFTER the cell
        // that contains the nested table
        // The cells added have the same widths than the cells above.
        if (parentITableColumnsInfo.getColumnIndex()
                < (parentITableColumnsInfo.getNumberOfColumns() - 1)) {
            parentITableColumnsInfo.selectNextColumn();

            while (parentITableColumnsInfo.getColumnIndex()
                   < parentITableColumnsInfo.getNumberOfColumns()) {
                  // Create the empty cell merged vertically
                  // TODO : the new cells after the extra cell don't have its
                  // attributes as we did for the previous cells.
                  //        => in fact the m_attrib below (last argument) is
                  // empty => should be the attributes of the above cells.
                  row.newTableCellMergedVertically((int)parentITableColumnsInfo.getColumnWidth(),
                          attrib);
                  // Select next column in order to have its width
                  parentITableColumnsInfo.selectNextColumn();
                }
           }

        row.writeRtf();
    }

    /** true if all cells of given list are empty
     *  @param cells List of PositionedCell objects
     */
    private static boolean allCellsEmpty(List cells) {
        boolean empty = true;
        for (Iterator it = cells.iterator(); it.hasNext();) {
            final PositionedCell pc = (PositionedCell)it.next();
            if (pc.cell.containsText()) {
                empty = false;
                break;
            }
        }
        return empty;
    }

    /**
     * As this contains cells from several rows, we say that it's empty
     * only if we have no cells.
     * writeRow makes the decision about rendering specific rows
     * @return false (always)
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * @return The table context of the parent table
     * Added by Boris Poudérous on july 2002 in order to process nested tables
     */
     public ITableColumnsInfo getParentITableColumnsInfo() {
       return this.parentITableColumnsInfo;
     }

     /**
      *
      * @param parentITableColumnsInfo table context to set
      */
     public void setParentITableColumnsInfo (ITableColumnsInfo parentITableColumnsInfo) {
       this.parentITableColumnsInfo = parentITableColumnsInfo;
     }
     /** - end - */
}
