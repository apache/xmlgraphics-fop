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

import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground.BorderInfo;


public class OldGridUnit {
    
    /** layout manager for the cell occupying this grid unit, may be null */
    public Cell layoutManager;
    /** layout manager for the column that this grid unit belongs to */
    public Column column;
    /** layout manager for the row that this grid unit belongs to */
    public Row row;
    /** index of grid unit within cell in column direction */
    public int colSpanIndex;
    /** index of grid unit within cell in row direction */
    public int rowSpanIndex;
    /** effective borders for a cell slot (used for collapsing border model) */
    public CommonBorderPaddingBackground effBorders;
    
    public OldGridUnit(Cell layoutManager, int colSpanIndex) {
        this.layoutManager = layoutManager;
        this.colSpanIndex = colSpanIndex;
        this.rowSpanIndex = 0;
    }
    
    public OldGridUnit(Cell layoutManager) {
        this(layoutManager, 0);
    }

    /** @return true if the grid unit is the primary of a cell */
    public boolean isPrimaryGridUnit() {
        return (colSpanIndex == 0) && (rowSpanIndex == 0);
    }
    
    /** @return true if the grid unit is the last in column spanning direction */
    public boolean isLastGridUnitColSpan() {
        if (layoutManager != null) {
            return (colSpanIndex == layoutManager.getFObj().getNumberColumnsSpanned() - 1);
        } else {
            return true;
        }
    }
    
    /** @return true if the grid unit is the last in column spanning direction */
    public boolean isLastGridUnitRowSpan() {
        if (layoutManager != null) {
            return (rowSpanIndex == layoutManager.getFObj().getNumberRowsSpanned() - 1);
        } else {
            return true;
        }
    }
    
    /** @return true if the cell is part of a span in column direction */
    public boolean isColSpan() {
        return (colSpanIndex > 0);
    }

    public BorderInfo getOriginalBorderInfoForCell(int side) {
        if (layoutManager != null) {
            return layoutManager.getFObj().getCommonBorderPaddingBackground().getBorderInfo(side);
        } else {
            return null;
        }
    }
    
    /**
     * Assign the borders from the given cell to this cell info. Used in
     * case of separate border model.
     * @param current cell to take the borders from
     */
    public void assignBorder(Cell current) {
        if (current != null) {
            this.effBorders = current.getFObj().getCommonBorderPaddingBackground();
        }
    }
    
    /**
     * Assign the borders directly.
     * @param borders the borders to use
     */
    public void assignBorder(CommonBorderPaddingBackground borders) {
        if (borders != null) {
            this.effBorders = borders;
        }
    }
    
    /**
     * Resolve collapsing borders for the given cell and store the resulting
     * borders in this cell info. Use in case of the collapsing border model.
     * @param current cell to resolve borders for
     * @param before cell before the current cell, if any
     * @param after cell after the current cell, if any
     * @param start cell preceeding the current cell, if any
     * @param end cell succeeding of the current cell, if any
     */
    public static void resolveBorder(Table table,
            CommonBorderPaddingBackground target,
            OldGridUnit current, OldGridUnit other, int side) {
        if (current == null) {
            return;
        }
        
        CollapsingBorderModel borderModel = CollapsingBorderModel.getBorderModelFor(
                table.getBorderCollapse());
        target.setBorderInfo(
                borderModel.determineWinner(current, other, 
                        side, 0), side);
    }
    
}