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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground.BorderInfo;
import org.apache.fop.layoutmgr.table.CollapsingBorderModel;

/**
 * This class represents one grid unit inside a table.
 */
public class GridUnit {

    private static Log log = LogFactory.getLog(GridUnit.class);

    /** Indicates that the grid unit is in the first column. */
    public static final int IN_FIRST_COLUMN = 0;

    /** Indicates that the grid unit is in the last column. */
    public static final int IN_LAST_COLUMN = 1;

    /** Indicates that the grid unit is in the first row of the table. */
    public static final int FIRST_IN_TABLE = 2;

    /**
     * Indicates that the grid unit is in the first row of the table part (header, footer,
     * body).
     */
    public static final int FIRST_IN_PART = 3;

    /**
     * Indicates that the grid unit is in the last row of the table part (header, footer,
     * body).
     */
    public static final int LAST_IN_PART = 4;

    /** Indicates that the grid unit is in the last row of the table. */
    public static final int LAST_IN_TABLE = 5;

    /** Indicates that the primary grid unit has a pending keep-with-next. */
    public static final int KEEP_WITH_NEXT_PENDING = 6;

    /** Indicates that the primary grid unit has a pending keep-with-previous. */
    public static final int KEEP_WITH_PREVIOUS_PENDING = 7;

    /** Primary grid unit */
    private PrimaryGridUnit primary;

    /** Table cell which occupies this grid unit */
    protected TableCell cell;

    /** Table row occupied by this grid unit (may be null). */
    private TableRow row;

    /** Table column that this grid unit belongs to */
    private TableColumn column;

    /** start index of grid unit within row in column direction */
    private int startCol;

    /** index of grid unit within cell in column direction */
    private int colSpanIndex;

    /** index of grid unit within cell in row direction */
    private int rowSpanIndex;

    /** effective borders for a cell slot */
    private CommonBorderPaddingBackground effectiveBorders;

    /** flags for the grid unit */
    private byte flags = 0;

    protected BorderSpecification[] resolvedBorders;

    private CollapsingBorderModel collapsingBorderModel;

    /**
     * Creates a new grid unit.
     * 
     * @param table the containing table
     * @param row the table-row element this grid unit belongs to (if any)
     * @param column table column this grid unit belongs to
     * @param startCol index of the column this grid unit belongs to
     * @param colSpanIndex index of this grid unit in the span, in column direction
     * @param rowSpanIndex index of this grid unit in the span, in row direction
     */
    protected GridUnit(Table table, TableRow row, TableColumn column, int startCol,
            int colSpanIndex, int rowSpanIndex) {
        this(row, column, startCol, colSpanIndex, rowSpanIndex);
        setBorders(table);
    }

    /**
     * Creates a new grid unit.
     * 
     * @param cell table cell which occupies this grid unit
     * @param row the table-row element this grid unit belongs to (if any)
     * @param column table column this grid unit belongs to
     * @param startCol index of the column this grid unit belongs to
     * @param colSpanIndex index of this grid unit in the span, in column direction
     * @param rowSpanIndex index of this grid unit in the span, in row direction
     */
    protected GridUnit(TableCell cell, TableRow row, TableColumn column, int startCol,
            int colSpanIndex, int rowSpanIndex) {
        this(row, column, startCol, colSpanIndex, rowSpanIndex);
        this.cell = cell;
        setBorders(cell.getTable());
    }

    /**
     * Creates a new grid unit.
     * 
     * @param primary the before-start grid unit of the cell containing this grid unit
     * @param row the table-row element this grid unit belongs to (if any)
     * @param column table column this grid unit belongs to
     * @param startCol index of the column this grid unit belongs to
     * @param colSpanIndex index of this grid unit in the span, in column direction
     * @param rowSpanIndex index of this grid unit in the span, in row direction
     */
    GridUnit(PrimaryGridUnit primary, TableRow row, TableColumn column, int startCol,
            int colSpanIndex, int rowSpanIndex) {
        this(primary.getCell(), row, column, startCol, colSpanIndex, rowSpanIndex);
        this.primary = primary;
    }

    private GridUnit(TableRow row, TableColumn column, int startCol, int colSpanIndex,
            int rowSpanIndex) {
        this.row = row;
        this.column = column;
        this.startCol = startCol;
        this.colSpanIndex = colSpanIndex;
        this.rowSpanIndex = rowSpanIndex;
    }

    private void setBorders(Table table/*TODO*/) {
        if (table.isSeparateBorderModel()) {
            assignBorderForSeparateBorderModel();
        } else {
            resolvedBorders = new BorderSpecification[4];
            collapsingBorderModel = CollapsingBorderModel.getBorderModelFor(table
                    .getBorderCollapse());
            if (rowSpanIndex == 0) {
                setBorder(CommonBorderPaddingBackground.BEFORE);
            }
            if (isLastGridUnitRowSpan()) {
                setBorder(CommonBorderPaddingBackground.AFTER);
            }
            if (colSpanIndex == 0) {
                setBorder(CommonBorderPaddingBackground.START);
            }
            if (isLastGridUnitColSpan()) {
                setBorder(CommonBorderPaddingBackground.END);
            }
        }
    }

    protected void setBorder(int side) {
        resolvedBorders[side] = cell.resolvedBorders[side];
    }

    public TableCell getCell() {
        return cell;
    }

    public TableColumn getColumn() {
        return column;
    }

    /**
     * Returns the fo:table-row element (if any) this grid unit belongs to.
     * 
     * @return the row containing this grid unit, or null if there is no fo:table-row
     * element in the corresponding table-part
     */
    public TableRow getRow() {
        return row;
    }

    public TableBody getBody() {
        FONode node = getCell();
        while (node != null && !(node instanceof TableBody)) {
            node = node.getParent();
        }
        return (TableBody) node;
    }

    /**
     * Returns the before-start grid unit of the cell containing this grid unit.
     * 
     * @return the before-start grid unit of the cell containing this grid unit.
     */
    public PrimaryGridUnit getPrimary() {
        return primary;
    }

    /**
     * Is this grid unit the before-start grid unit of the cell?
     * 
     * @return true if this grid unit is the before-start grid unit of the cell
     */
    public boolean isPrimary() {
        return false;
    }

    /**
     * Does this grid unit belong to an empty cell?
     * 
     * @return true if this grid unit belongs to an empty cell
     */
    public boolean isEmpty() {
        return cell == null;
    }

    public int getStartCol() {
        return startCol;
    }

    /** @return true if the grid unit is the last in column spanning direction */
    public boolean isLastGridUnitColSpan() {
        return (colSpanIndex == cell.getNumberColumnsSpanned() - 1);
    }

    /** @return true if the grid unit is the last in row spanning direction */
    public boolean isLastGridUnitRowSpan() {
        return (rowSpanIndex == cell.getNumberRowsSpanned() - 1);
    }

    /**
     * @return the index of the grid unit inside a cell in row direction
     */
    public int getRowSpanIndex() {
        return rowSpanIndex;
    }

    /**
     * @return the index of the grid unit inside a cell in column direction
     */
    public int getColSpanIndex() {
        return colSpanIndex;
    }

    /**
     * Returns a BorderInfo instance for a side of the currently applicable cell before
     * border resolution (i.e. the value from the FO). A return value of null indicates an
     * empty cell. See CollapsingBorderModel(EyeCatching) where this method is used.
     * 
     * @param side for which side to return the BorderInfo
     * @return the requested BorderInfo instance or null if the grid unit is an empty cell
     */
    public BorderInfo getOriginalBorderInfoForCell(int side) {
        if (cell != null) {
            return cell.getCommonBorderPaddingBackground().getBorderInfo(side);
        } else {
            return null;
        }
    }

    /**
     * @return the resolved normal borders for this grid unit
     */
    public CommonBorderPaddingBackground getBorders() {
        // TODO
        if (effectiveBorders == null) {
            effectiveBorders = new CommonBorderPaddingBackground();
            setBorderInfo(CommonBorderPaddingBackground.BEFORE);
            setBorderInfo(CommonBorderPaddingBackground.AFTER);
            setBorderInfo(CommonBorderPaddingBackground.START);
            setBorderInfo(CommonBorderPaddingBackground.END);
            if (cell != null) {
                effectiveBorders.setPadding(cell.getCommonBorderPaddingBackground());
            }
            if (log.isDebugEnabled()) {
                log.debug(this + " resolved borders: " + "before="
                        + effectiveBorders.getBorderBeforeWidth(false) + ", " + "after="
                        + effectiveBorders.getBorderAfterWidth(false) + ", " + "start="
                        + effectiveBorders.getBorderStartWidth(false) + ", " + "end="
                        + effectiveBorders.getBorderEndWidth(false));
            }
        }
        return effectiveBorders;
    }

    private void setBorderInfo(int side) {
        if (resolvedBorders[side] != null) {
            effectiveBorders.setBorderInfo(resolvedBorders[side].getBorderInfo(), side);
        }
    }

    /**
     * @return true if the grid unit has any borders.
     */
    public boolean hasBorders() {
        return (getBorders() != null) && getBorders().hasBorder();
    }

    /**
     * Assigns the borders from the given cell to this cell info. Used in case of separate
     * border model.
     */
    void assignBorderForSeparateBorderModel() {
        if (cell != null) {
            effectiveBorders = cell.getCommonBorderPaddingBackground();
        }
    }

    /**
     * Resolve collapsing borders for the given cell. Used in case of the collapsing
     * border model.
     * 
     * @param other neighbouring grid unit
     * @param side the side to resolve (one of
     * CommonBorderPaddingBackground.BEFORE|AFTER|START|END)
     */
    void resolveBorder(GridUnit other, int side) {
        BorderSpecification resolvedBorder = collapsingBorderModel.determineWinner(
                resolvedBorders[side], other.resolvedBorders[CollapsingBorderModel
                        .getOtherSide(side)]);
        if (resolvedBorder != null) {
            this.resolvedBorders[side] = resolvedBorder;
            other.resolvedBorders[CollapsingBorderModel.getOtherSide(side)] = resolvedBorder;
        }
    }

    /**
     * Resolves the border on the given side of this grid unit, comparing it against the
     * same border of the given parent element.
     * 
     * @param side the side to resolve (one of
     * CommonBorderPaddingBackground.BEFORE|AFTER|START|END)
     * @param parent the parent element holding a competing border
     */
    void resolveBorder(int side, TableFObj parent) {
        resolvedBorders[side] = collapsingBorderModel.determineWinner(resolvedBorders[side],
                parent.resolvedBorders[side]);
    }

    /**
     * Returns a flag for this GridUnit.
     * 
     * @param which the requested flag
     * @return the value of the flag
     */
    public boolean getFlag(int which) {
        return (flags & (1 << which)) != 0;
    }

    /**
     * Sets a flag on a GridUnit.
     * 
     * @param which the flag to set
     * @param value the new value for the flag
     */
    public void setFlag(int which, boolean value) {
        if (value) {
            flags |= (1 << which); // set flag
        } else {
            flags &= ~(1 << which); // clear flag
        }
    }

    /**
     * Sets the given flag on this grid unit.
     * 
     * @param which the flag to set
     */
    public void setFlag(int which) {
        setFlag(which, true);
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (isEmpty()) {
            buffer.append("EMPTY");
        } else if (isPrimary()) {
            buffer.append("Primary");
        }
        buffer.append("GridUnit:");
        if (colSpanIndex > 0) {
            buffer.append(" colSpan=").append(colSpanIndex);
            if (isLastGridUnitColSpan()) {
                buffer.append("(last)");
            }
        }
        if (rowSpanIndex > 0) {
            buffer.append(" rowSpan=").append(rowSpanIndex);
            if (isLastGridUnitRowSpan()) {
                buffer.append("(last)");
            }
        }
        buffer.append(" startCol=").append(startCol);
        if (!isPrimary() && getPrimary() != null) {
            buffer.append(" primary=").append(getPrimary().getStartRow());
            buffer.append("/").append(getPrimary().getStartCol());
            if (getPrimary().getCell() != null) {
                buffer.append(" id=" + getPrimary().getCell().getId());
            }
        }
        buffer.append(" flags=").append(Integer.toBinaryString(flags));
        return buffer.toString();
    }

}
