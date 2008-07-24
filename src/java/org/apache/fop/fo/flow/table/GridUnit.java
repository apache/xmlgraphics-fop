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

import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground.BorderInfo;
import org.apache.fop.layoutmgr.table.CollapsingBorderModel;

/**
 * This class represents one grid unit inside a table.
 */
public class GridUnit {

    /**
     * Indicates that the grid unit is in the first row of the table part (header, footer,
     * body).
     */
    public static final int FIRST_IN_PART = 0;

    /**
     * Indicates that the grid unit is in the last row of the table part (header, footer,
     * body).
     */
    public static final int LAST_IN_PART = 1;

    /** Indicates that the primary grid unit has a pending keep-with-next. */
    public static final int KEEP_WITH_NEXT_PENDING = 2;

    /** Indicates that the primary grid unit has a pending keep-with-previous. */
    public static final int KEEP_WITH_PREVIOUS_PENDING = 3;

    /** Primary grid unit */
    private PrimaryGridUnit primary;

    /** Table cell which occupies this grid unit */
    protected TableCell cell;

    /** Table row occupied by this grid unit (may be null). */
    private TableRow row;

    /** index of grid unit within cell in column direction */
    private int colSpanIndex;

    /** index of grid unit within cell in row direction */
    private int rowSpanIndex;

    /** flags for the grid unit */
    private byte flags = 0;

    /** the border-before specification */
    ConditionalBorder borderBefore;
    /** the border-after specification */
    ConditionalBorder borderAfter;
    /** the border-start specification */
    BorderSpecification borderStart;
    /** the border-end specification */
    BorderSpecification borderEnd;

    /** The border model helper associated with the table */
    protected CollapsingBorderModel collapsingBorderModel;

    /**
     * Creates a new grid unit.
     *
     * @param table the containing table
     * @param colSpanIndex index of this grid unit in the span, in column direction
     * @param rowSpanIndex index of this grid unit in the span, in row direction
     */
    protected GridUnit(Table table, int colSpanIndex, int rowSpanIndex) {
        this(colSpanIndex, rowSpanIndex);
        setBorders(table);
    }

    /**
     * Creates a new grid unit.
     *
     * @param cell table cell which occupies this grid unit
     * @param colSpanIndex index of this grid unit in the span, in column direction
     * @param rowSpanIndex index of this grid unit in the span, in row direction
     */
    protected GridUnit(TableCell cell, int colSpanIndex, int rowSpanIndex) {
        this(colSpanIndex, rowSpanIndex);
        this.cell = cell;
        setBorders(cell.getTable());
    }

    /**
     * Creates a new grid unit.
     *
     * @param primary the before-start grid unit of the cell containing this grid unit
     * @param colSpanIndex index of this grid unit in the span, in column direction
     * @param rowSpanIndex index of this grid unit in the span, in row direction
     */
    GridUnit(PrimaryGridUnit primary, int colSpanIndex, int rowSpanIndex) {
        this(primary.getCell(), colSpanIndex, rowSpanIndex);
        this.primary = primary;
    }

    private GridUnit(int colSpanIndex, int rowSpanIndex) {
        this.colSpanIndex = colSpanIndex;
        this.rowSpanIndex = rowSpanIndex;
    }

    private void setBorders(Table table/*TODO*/) {
        if (!table.isSeparateBorderModel()) {
            collapsingBorderModel = CollapsingBorderModel.getBorderModelFor(table
                    .getBorderCollapse());
            setBordersFromCell();
        }
    }

    /**
     * Prepares the borders of this grid unit for upcoming resolution, in the collapsing
     * model.
     */
    protected void setBordersFromCell() {
        borderBefore = cell.borderBefore.copy();
        if (rowSpanIndex > 0) {
            borderBefore.normal = BorderSpecification.getDefaultBorder();
        }
        borderAfter = cell.borderAfter.copy();
        if (!isLastGridUnitRowSpan()) {
            borderAfter.normal = BorderSpecification.getDefaultBorder();
        }
        if (colSpanIndex == 0) {
            borderStart = cell.borderStart;
        } else {
            borderStart = BorderSpecification.getDefaultBorder();
        }
        if (isLastGridUnitColSpan()) {
            borderEnd = cell.borderEnd;
        } else {
            borderEnd = BorderSpecification.getDefaultBorder();
        }
    }

    /**
     * Returns the table cell associated with this grid unit.
     * @return the table cell
     */
    public TableCell getCell() {
        return cell;
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

    void setRow(TableRow row) {
        this.row = row;
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
     * Returns the resolved border-before of this grid unit, in the collapsing-border
     * model.
     *
     * @param which one of {@link ConditionalBorder#NORMAL},
     * {@link ConditionalBorder#LEADING_TRAILING} or {@link ConditionalBorder#REST}
     * @return the corresponding border
     */
    public BorderInfo getBorderBefore(int which) {
        switch (which) {
        case ConditionalBorder.NORMAL:
            return borderBefore.normal.getBorderInfo();
        case ConditionalBorder.LEADING_TRAILING:
            return borderBefore.leadingTrailing.getBorderInfo();
        case ConditionalBorder.REST:
            return borderBefore.rest.getBorderInfo();
        default:
            assert false;
            return null;
        }
    }

    /**
     * Returns the resolved border-after of this grid unit, in the collapsing-border
     * model.
     *
     * @param which one of {@link ConditionalBorder#NORMAL},
     * {@link ConditionalBorder#LEADING_TRAILING} or {@link ConditionalBorder#REST}
     * @return the corresponding border
     */
    public BorderInfo getBorderAfter(int which) {
        switch (which) {
        case ConditionalBorder.NORMAL:
            return borderAfter.normal.getBorderInfo();
        case ConditionalBorder.LEADING_TRAILING:
            return borderAfter.leadingTrailing.getBorderInfo();
        case ConditionalBorder.REST:
            return borderAfter.rest.getBorderInfo();
        default:
            assert false;
            return null;
        }
    }

    /**
     * Returns the resolved border-start of this grid unit, in the collapsing-border
     * model.
     *
     * @return the corresponding border
     */
    public BorderInfo getBorderStart() {
        return borderStart.getBorderInfo();
    }

    /**
     * Returns the resolved border-end of this grid unit, in the collapsing-border
     * model.
     *
     * @return the corresponding border
     */
    public BorderInfo getBorderEnd() {
        return borderEnd.getBorderInfo();
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
        switch (side) {
        case CommonBorderPaddingBackground.BEFORE:
            borderBefore.resolve(other.borderAfter, true, false, false);
            break;
        case CommonBorderPaddingBackground.AFTER:
            borderAfter.resolve(other.borderBefore, true, false, false);
            break;
        case CommonBorderPaddingBackground.START:
            BorderSpecification resolvedBorder = collapsingBorderModel.determineWinner(
                    borderStart, other.borderEnd);
            if (resolvedBorder != null) {
                this.borderStart = resolvedBorder;
                other.borderEnd = resolvedBorder;
            }
            break;
        case CommonBorderPaddingBackground.END:
            resolvedBorder = collapsingBorderModel.determineWinner(
                    borderEnd, other.borderStart);
            if (resolvedBorder != null) {
                this.borderEnd = resolvedBorder;
                other.borderStart = resolvedBorder;
            }
            break;
        default: assert false;
        }
    }

    /**
     * For the given side, integrates in the conflict resolution the border segment of the
     * given parent element.
     *
     * @param side the side to consider (either CommonBorderPaddingBackground.BEFORE or
     * AFTER)
     * @param parent a table element whose corresponding border coincides on the given
     * side
     */
    void integrateBorderSegment(int side, TableFObj parent, boolean withNormal,
            boolean withLeadingTrailing, boolean withRest) {
        switch (side) {
        case CommonBorderPaddingBackground.BEFORE:
            borderBefore.integrateSegment(parent.borderBefore, withNormal,
                    withLeadingTrailing, withRest);
            break;
        case CommonBorderPaddingBackground.AFTER:
            borderAfter.integrateSegment(parent.borderAfter, withNormal,
                    withLeadingTrailing, withRest);
            break;
        default: assert false;
        }
    }

    /**
     * For the given side, integrates in the conflict resolution the border segment of the
     * given parent element.
     *
     * @param side the side to consider (one of
     * CommonBorderPaddingBackground.BEFORE|AFTER|START|END)
     * @param parent a table element whose corresponding border coincides on the given side
     */
    void integrateBorderSegment(int side, TableFObj parent) {
        switch (side) {
        case CommonBorderPaddingBackground.BEFORE:
        case CommonBorderPaddingBackground.AFTER:
            integrateBorderSegment(side, parent, true, true, true);
            break;
        case CommonBorderPaddingBackground.START:
            borderStart = collapsingBorderModel.determineWinner(borderStart,
                    parent.borderStart);
            break;
        case CommonBorderPaddingBackground.END:
            borderEnd = collapsingBorderModel.determineWinner(borderEnd,
                    parent.borderEnd);
            break;
        default: assert false;
        }
    }

    /**
     * For the given side, integrates in the conflict resolution the given border segment.
     *
     * @param side the side to consider (one of CommonBorderPaddingBackground.START|END)
     * @param segment a border specification to integrate at the given side
     */
    void integrateBorderSegment(int side, BorderSpecification segment) {
        switch(side) {
        case CommonBorderPaddingBackground.START:
            borderStart = collapsingBorderModel.determineWinner(borderStart, segment);
            break;
        case CommonBorderPaddingBackground.END:
            borderEnd = collapsingBorderModel.determineWinner(borderEnd, segment);
            break;
        default: assert false;
        }
    }

    void integrateCompetingBorder(int side, ConditionalBorder competitor,
            boolean withNormal, boolean withLeadingTrailing, boolean withRest) {
        switch (side) {
        case CommonBorderPaddingBackground.BEFORE:
            borderBefore.integrateCompetingSegment(competitor, withNormal,
                    withLeadingTrailing, withRest);
            break;
        case CommonBorderPaddingBackground.AFTER:
            borderAfter.integrateCompetingSegment(competitor, withNormal,
                    withLeadingTrailing, withRest);
            break;
        default: assert false;
        }
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
        if (!isPrimary() && getPrimary() != null) {
            buffer.append(" primary=").append(getPrimary().getRowIndex());
            buffer.append("/").append(getPrimary().getColIndex());
            if (getPrimary().getCell() != null) {
                buffer.append(" id=" + getPrimary().getCell().getId());
            }
        }
        buffer.append(" flags=").append(Integer.toBinaryString(flags));
        return buffer.toString();
    }

}
