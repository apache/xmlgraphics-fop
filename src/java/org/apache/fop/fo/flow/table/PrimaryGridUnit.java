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

import java.util.LinkedList;
import java.util.List;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.table.TableCellLayoutManager;

/**
 * This class represents a primary grid unit of a spanned cell. This is the "before-start"
 * (top-left, usually) grid unit of the span.
 */
public class PrimaryGridUnit extends GridUnit {

    /** Cell layout manager. */
    private TableCellLayoutManager cellLM;
    /** List of Knuth elements representing the contents of the cell. */
    private LinkedList elements;

    /** Index of the row where this cell starts. */
    private int rowIndex;

    /** Index of the column where this cell starts. */
    private int colIndex;

    /** Links to the spanned grid units. (List of GridUnit arrays, one array represents a row) */
    private List rows;
    /** The calculated size of the cell's content. (cached value) */
    private int contentLength = -1;

    private boolean isSeparateBorderModel;
    private int halfBorderSeparationBPD;

    private int breakBefore = Constants.EN_AUTO;
    private int breakAfter = Constants.EN_AUTO;

    /**
     * Creates a new primary grid unit.
     *
     * @param cell table cell which occupies this grid unit
     * @param row the table-row element this grid unit belongs to (if any)
     * @param colIndex index of the column this grid unit belongs to, zero-based
     */
    PrimaryGridUnit(TableCell cell, TableRow row, int colIndex) {
        super(cell, row, 0, 0);
        this.colIndex = colIndex;
        this.isSeparateBorderModel = cell.getTable().isSeparateBorderModel(); // TODO
        this.halfBorderSeparationBPD = cell.getTable().getBorderSeparation().getBPD().getLength()
                .getValue() / 2;  // TODO
    }

    public TableCellLayoutManager getCellLM() {
        assert cellLM != null;
        return cellLM;
    }

    /** {@inheritDoc} */
    public PrimaryGridUnit getPrimary() {
        return this;
    }

    /** {@inheritDoc} */
    public boolean isPrimary() {
        return true;
    }

    /**
     * Sets the Knuth elements for the table cell containing this grid unit.
     *
     * @param elements a list of ListElement (?)
     */
    public void setElements(LinkedList elements) {
        this.elements = elements;
    }

    public LinkedList getElements() {
        return this.elements;
    }

    /**
     * Returns the widths of the border-before and -after for this cell. In the separate
     * border model the border-separation is included. In the collapsing model only half
     * of them is counted, since the other halves belong to the neighbouring cells; also,
     * the returned value is the maximum of the segments of each applicable grid unit.
     * 
     * @return the sum of the before and after border widths
     */
    public int getBeforeAfterBorderWidth() {
        return getBeforeBorderWidth(0, ConditionalBorder.NORMAL)
                + getAfterBorderWidth(ConditionalBorder.NORMAL);
    }

    /**
     * Returns the width of the before-border for the given row-span of this cell. In the
     * separate border model half of the border-separation is included. In the collapsing
     * model only half of the border is counted, since the other half belongs to the
     * preceding cell; also, the returned value is the maximum of the segments of each
     * applicable grid unit.
     * 
     * @param rowIndex index of the span for which the border must be computed, 0-based
     * @param which one of {@link ConditionalBorder#NORMAL},
     * {@link ConditionalBorder#LEADING_TRAILING} or {@link ConditionalBorder#REST}
     * @return the before border width
     */
    public int getBeforeBorderWidth(int rowIndex, int which) {
        if (isSeparateBorderModel) {
            if (getCell() == null) {
                return 0;
            } else {
                CommonBorderPaddingBackground cellBorders = getCell()
                        .getCommonBorderPaddingBackground();
                switch (which) {
                case ConditionalBorder.NORMAL:
                case ConditionalBorder.LEADING_TRAILING:
                    return cellBorders.getBorderBeforeWidth(false) + halfBorderSeparationBPD;
                case ConditionalBorder.REST:
                    if (cellBorders.getBorderInfo(CommonBorderPaddingBackground.BEFORE).getWidth()
                            .isDiscard()) {
                        return 0;
                    } else {
                        return cellBorders.getBorderBeforeWidth(true) + halfBorderSeparationBPD;
                    }
                default:
                    assert false;
                    return 0;
                }
            }
        } else {
            int width = 0;
            GridUnit[] row = (GridUnit[]) rows.get(rowIndex);
            for (int i = 0; i < row.length; i++) {
                width = Math.max(width,
                        row[i].getBorderBefore(which).getRetainedWidth());
            }
            return width / 2;
        }
    }

    /**
     * Returns the width of the before-after for the given row-span of this cell. In the
     * separate border model half of the border-separation is included. In the collapsing
     * model only half of the border is counted, since the other half belongs to the
     * following cell; also, the returned value is the maximum of the segments of each
     * applicable grid unit.
     * 
     * @param rowIndex index of the span for which the border must be computed, 0-based
     * @param which one of {@link ConditionalBorder#NORMAL},
     * {@link ConditionalBorder#LEADING_TRAILING} or {@link ConditionalBorder#REST}
     * @return the after border width
     */
    public int getAfterBorderWidth(int rowIndex, int which) {
        if (isSeparateBorderModel) {
            if (getCell() == null) {
                return 0;
            } else {
                CommonBorderPaddingBackground cellBorders = getCell()
                        .getCommonBorderPaddingBackground();
                switch (which) {
                case ConditionalBorder.NORMAL:
                case ConditionalBorder.LEADING_TRAILING:
                    return cellBorders.getBorderAfterWidth(false) + halfBorderSeparationBPD;
                case ConditionalBorder.REST:
                    if (cellBorders.getBorderInfo(CommonBorderPaddingBackground.AFTER).getWidth()
                            .isDiscard()) {
                        return 0;
                    } else {
                        return cellBorders.getBorderAfterWidth(true) + halfBorderSeparationBPD;
                    }
                default:
                    assert false;
                    return 0;
                }
            }
        } else {
            int width = 0;
            GridUnit[] row = (GridUnit[]) rows.get(rowIndex);
            for (int i = 0; i < row.length; i++) {
                width = Math.max(width,
                        row[i].getBorderAfter(which).getRetainedWidth());
            }
            return width / 2;
        }
    }

    /**
     * Returns the width of the before-after for the last row-span of this cell. See
     * {@link #getAfterBorderWidth(int, int)}.
     * 
     * @param which one of {@link ConditionalBorder#NORMAL},
     * {@link ConditionalBorder#LEADING_TRAILING} or {@link ConditionalBorder#REST}
     * @return the after border width
     */
    public int getAfterBorderWidth(int which) {
        return getAfterBorderWidth(getCell().getNumberRowsSpanned() - 1, which);
    }

    /** @return the length of the cell content. */
    public int getContentLength() {
        if (contentLength < 0) {
            contentLength = ElementListUtils.calcContentLength(elements);
        }
        return contentLength;
    }

    /** @return true if cell/row has an explicit BPD/height */
    public boolean hasBPD() {
        if (!getCell().getBlockProgressionDimension().getOptimum(null).isAuto()) {
            return true;
        }
        if (getRow() != null
                && !getRow().getBlockProgressionDimension().getOptimum(null).isAuto()) {
            return true;
        }
        return false;
    }

    /**
     * Returns the grid units belonging to the same span as this one.
     *
     * @return a list of GridUnit[], each array corresponds to a row
     */
    public List getRows() {
        return this.rows;
    }

    public void addRow(GridUnit[] row) {
        if (rows == null) {
            rows = new java.util.ArrayList();
        }
        rows.add(row);
    }

    void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    /**
     * Returns the index of the row this grid unit belongs to. This is the index, in the
     * enclosing table part, of the first row spanned by the cell. Note that if the table
     * has several table-body children, then the index grows continuously across them;
     * they are considered to form one single part, the "body of the table".
     * 
     * @return the index of the row this grid unit belongs to, 0-based.
     */
    public int getRowIndex() {
        return rowIndex;
    }

    /**
     * Returns the index of the column this grid unit belongs to.
     * 
     * @return the column index, 0-based
     */
    public int getColIndex() {
        return colIndex;
    }

    /**
     * Returns the widths of the start- and end-borders of the span this grid unit belongs
     * to.
     *
     * @return a two-element array containing the widths of the start-border then the
     * end-border
     */
    public int[] getStartEndBorderWidths() {
        int[] widths = new int[2];
        if (getCell() == null) {
            return widths;
        } else if (getCell().getTable().isSeparateBorderModel()) {
            widths[0] = getCell().getCommonBorderPaddingBackground().getBorderStartWidth(false);
            widths[1] = getCell().getCommonBorderPaddingBackground().getBorderEndWidth(false);
        } else {
            for (int i = 0; i < rows.size(); i++) {
                GridUnit[] gridUnits = (GridUnit[])rows.get(i);
                widths[0] = Math.max(widths[0],
                        gridUnits[0].borderStart.getBorderInfo().getRetainedWidth());
                widths[1] = Math.max(widths[1], gridUnits[gridUnits.length - 1].borderEnd
                        .getBorderInfo().getRetainedWidth());
            }
        }
        return widths;
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(" rowIndex=").append(rowIndex);
        sb.append(" colIndex=").append(colIndex);
        return sb.toString();
    }

    /** @return true if this cell spans over more than one grid unit. */
    public boolean hasSpanning() {
        return (getCell().getNumberColumnsSpanned() > 1)
            || (getCell().getNumberRowsSpanned() > 1);
    }

    /**
     * Creates a cellLM for the corresponding table-cell. A new one must be created
     * for each new static-content (TODO).
     */
    public void createCellLM() {
        cellLM = new TableCellLayoutManager(cell, this);
    }

    /**
     * Returns the class of the before break for the first child element of this cell.
     * 
     * @return one of {@link Constants#EN_AUTO}, {@link Constants#EN_COLUMN}, {@link
     * Constants#EN_PAGE}, {@link Constants#EN_EVEN_PAGE}, {@link Constants#EN_ODD_PAGE}
     */
    public int getBreakBefore() {
        return breakBefore;
    }

    /**
     * Don't use, reserved for TableCellLM. TODO
     * 
     * @param breakBefore the breakBefore to set
     */
    public void setBreakBefore(int breakBefore) {
        this.breakBefore = breakBefore;
    }

    /**
     * Returns the class of the before after for the last child element of this cell.
     * 
     * @return one of {@link Constants#EN_AUTO}, {@link Constants#EN_COLUMN}, {@link
     * Constants#EN_PAGE}, {@link Constants#EN_EVEN_PAGE}, {@link Constants#EN_ODD_PAGE}
     */
    public int getBreakAfter() {
        return breakAfter;
    }

    /**
     * Don't use, reserved for TableCellLM. TODO
     * 
     * @param breakAfter the breakAfter to set
     */
    public void setBreakAfter(int breakAfter) {
        this.breakAfter = breakAfter;
    }

}
