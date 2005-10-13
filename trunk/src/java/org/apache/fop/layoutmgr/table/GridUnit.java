/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground.BorderInfo;

/**
 * This class represents one grid unit inside a table.
 */
public class GridUnit {

    /** Indicates that the grid unit is in the first column. */
    public static final int IN_FIRST_COLUMN = 0;
    /** Indicates that the grid unit is in the last column. */
    public static final int IN_LAST_COLUMN = 1;
    /** Indicates that the grid unit is in the first row (context: table). */
    public static final int FIRST_IN_TABLE = 2;
    /** Indicates that the grid unit is in the first row (context: body). */
    public static final int FIRST_IN_BODY = 3;
    /** Indicates that the grid unit is in the last row (context: body). */
    public static final int LAST_IN_BODY = 4;
    /** Indicates that the grid unit is in the last row (context: table). */
    public static final int LAST_IN_TABLE = 5;
    /** Indicates that the primary grid unit has a pending keep-with-next. */
    public static final int KEEP_WITH_NEXT_PENDING = 6;
    /** Indicates that the primary grid unit has a pending keep-with-previous. */
    public static final int KEEP_WITH_PREVIOUS_PENDING = 7;
    
    /** Primary grid unit */
    private PrimaryGridUnit primary;
    /** Table cell which occupies this grid unit */
    private TableCell cell;
    /** Table row which occupied this grid unit (may be null) */
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
    
    
    public GridUnit(TableCell cell, TableColumn column, int startCol, int colSpanIndex) {
        this(null, cell, column, startCol, colSpanIndex);
    }
    
    public GridUnit(PrimaryGridUnit primary, TableColumn column, int startCol, int colSpanIndex) {
        this(primary, primary.getCell(), column, startCol, colSpanIndex);
    }
    
    protected GridUnit(PrimaryGridUnit primary, TableCell cell, TableColumn column, int startCol, int colSpanIndex) {
        this.primary = primary;
        this.cell = cell;
        this.column = column;
        this.startCol = startCol;
        this.colSpanIndex = colSpanIndex;
    }
    
    public TableCell getCell() {
        return cell;
    }
    
    public TableColumn getColumn() {
        return column;
    }
    
    public TableRow getRow() {
        if (row != null) {
            return row;
        } else if (getCell().getParent() instanceof TableRow) {
            return (TableRow)getCell().getParent();
        } else {
            return null;
        }
    }
    
    /**
     * Sets the table-row FO, if applicable.
     * @param row the table-row FO
     */
    public void setRow(TableRow row) {
        this.row = row;
    }

    public TableBody getBody() {
        FONode node = getCell();
        while (node != null && !(node instanceof TableBody)) {
            node = node.getParent();
        }
        return (TableBody)node;
    }
    
    public Table getTable() {
        FONode node = getBody();
        while (node != null && !(node instanceof Table)) {
            node = node.getParent();
        }
        if (node == null && getColumn() != null) {
            node = getColumn().getParent();
        }
        return (Table)node;
    }
    
    /**
     * @return the primary grid unit if this is a spanned grid unit
     */
    public PrimaryGridUnit getPrimary() {
        return (isPrimary() ? (PrimaryGridUnit)this : primary);
    }

    public boolean isPrimary() {
        return false;
    }
    
    public boolean isEmpty() {
        return cell == null;
    }
    
    public int getStartCol() {
        return startCol;
    }
    
    /** @return true if the grid unit is the last in column spanning direction */
    public boolean isLastGridUnitColSpan() {
        if (cell != null) {
            return (colSpanIndex == cell.getNumberColumnsSpanned() - 1);
        } else {
            return true;
        }
    }
    
    /** @return true if the grid unit is the last in column spanning direction */
    public boolean isLastGridUnitRowSpan() {
        if (cell != null) {
            return (rowSpanIndex == cell.getNumberRowsSpanned() - 1);
        } else {
            return true;
        }
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
     * Returns a BorderInfo instance for a side of the currently applicable cell before border
     * resolution (i.e. the value from the FO). A return value of null indicates an empty cell.
     * See CollapsingBorderModel(EyeCatching) where this method is used. 
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
        return effectiveBorders;
    }
    
    /**
     * @return true if the grid unit has any borders.
     */
    public boolean hasBorders() {
        return (getBorders() != null) && getBorders().hasBorder();
    }
    
    /**
     * Assigns the borders from the given cell to this cell info. Used in
     * case of separate border model.
     */
    public void assignBorderForSeparateBorderModel() {
        if (cell != null) {
            effectiveBorders = cell.getCommonBorderPaddingBackground();
        }
    }
    
    /**
     * Resolve collapsing borders for the given cell. Used in case of the collapsing border model.
     * @param other neighbouring grid unit if any
     * @param side the side to resolve (one of CommonBorderPaddingBackground.BEFORE|AFTER|START|END)
     */
    public void resolveBorder(GridUnit other, int side) {
        resolveBorder(other, side, 0);
    }
    
    /**
     * Resolve collapsing borders for the given cell. Used in case of the collapsing border model.
     * @param other neighbouring grid unit if any
     * @param side the side to resolve (one of CommonBorderPaddingBackground.BEFORE|AFTER|START|END)
     * @param resFlags flags for the border resolution
     */
    public void resolveBorder(GridUnit other, int side, int resFlags) {
        CollapsingBorderModel borderModel = CollapsingBorderModel.getBorderModelFor(
                getTable().getBorderCollapse());
        if (effectiveBorders == null) {
            effectiveBorders = new CommonBorderPaddingBackground();
        }
        effectiveBorders.setBorderInfo(borderModel.determineWinner(this, other, 
                        side, resFlags), side);
        if (cell != null) {
            effectiveBorders.setPadding(cell.getCommonBorderPaddingBackground());
        }
    }
    
    /**
     * Returns a flag for this GridUnit.
     * @param which the requested flag
     * @return the value of the flag
     */
    public boolean getFlag(int which) {
        return (flags & (1 << which)) != 0;
    }
    
    /**
     * Sets a flag on a GridUnit.
     * @param which the flag to set
     * @param value the new value for the flag
     */
    public void setFlag(int which, boolean value) {
        if (value) {
            flags |= (1 << which); //set flag
        } else {
            flags &= ~(1 << which); //clear flag
        }
    }
    
    /**
     * @return the grid unit just below this grid unit if the cell is spanning.
     */
    public GridUnit createNextRowSpanningGridUnit() {
        if (isLastGridUnitRowSpan()) {
            return null;
        } else {
            //cloning the current GridUnit with adjustments
            GridUnit gu = new GridUnit(getPrimary(), getColumn(), startCol, colSpanIndex);
            gu.rowSpanIndex = rowSpanIndex + 1;
            return gu;
        }
    }

    /** @see java.lang.Object#toString() */
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
        }
        if (rowSpanIndex > 0) {
            buffer.append(" rowSpan=").append(rowSpanIndex);
        }
        buffer.append(" startCol=").append(startCol);
        buffer.append(" flags=").append(Integer.toBinaryString(flags));
        return buffer.toString();
    }

}
