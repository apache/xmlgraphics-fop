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

import java.util.Iterator;
import java.util.List;

import org.apache.fop.fo.Constants;
import org.apache.fop.layoutmgr.table.TableRowIterator;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.util.BreakUtil;

/**
 * This class represents an effective row in a table and holds a list of grid units occupying
 * the row as well as some additional values.
 */
public class EffRow {
    
    /** Indicates that the row is the first in a table-body */
    public static final int FIRST_IN_PART = GridUnit.FIRST_IN_PART;
    /** Indicates that the row is the last in a table-body */
    public static final int LAST_IN_PART = GridUnit.LAST_IN_PART;
    
    private List gridUnits = new java.util.ArrayList();
    private int index;
    /** One of HEADER, FOOTER, BODY */
    private int bodyType;
    private MinOptMax height;
    private MinOptMax explicitHeight;
    
    /**
     * Creates a new effective row instance.
     * @param index index of the row
     * @param bodyType type of body (one of HEADER, FOOTER, BODY as found on TableRowIterator)
     * @param gridUnits the grid units this row is made of
     */
    public EffRow(int index, int bodyType, List gridUnits) {
        this.index = index;
        this.bodyType = bodyType;
        this.gridUnits = gridUnits;
        // TODO this is ugly, but we may eventually be able to do without that index
        for (Iterator guIter = gridUnits.iterator(); guIter.hasNext();) {
            Object gu = guIter.next();
            if (gu instanceof PrimaryGridUnit) {
                ((PrimaryGridUnit) gu).setRowIndex(index);
            }
        }
    }

    /** @return the index of the EffRow in the sequence of rows */
    public int getIndex() {
        return this.index;
    }
    
    /**
     * @return an indicator what type of body this EffRow is in (one of HEADER, FOOTER, BODY 
     * as found on TableRowIterator)
     */
    public int getBodyType() {
        return this.bodyType;
    }
    
    /** @return the table-row FO for this EffRow, or null if there is no table-row. */
    public TableRow getTableRow() {
        return getGridUnit(0).getRow();
    }
    
    /**
     * Returns the calculated height for this EffRow, including the cells'
     * bpds/paddings/borders, and the table's border-separation.
     * 
     * @return the row's height
     */
    public MinOptMax getHeight() {
        return this.height;
    }
    
    /**
     * Sets the calculated height for this EffRow, including everything (cells' bpds,
     * paddings, borders, and border-separation).
     * 
     * @param mom the calculated height
     */
    public void setHeight(MinOptMax mom) {
        this.height = mom;
    }
    
    /** @return the explicit height of the EffRow (as specified through properties) */
    public MinOptMax getExplicitHeight() {
        return this.explicitHeight;
    }
    
    /**
     * Sets the height for this row that resulted from the explicit height properties specified
     * by the user.
     * @param mom the height
     */
    public void setExplicitHeight(MinOptMax mom) {
        this.explicitHeight = mom;
    }
    
    /** @return the list of GridUnits for this EffRow */
    public List getGridUnits() {
        return gridUnits;
    }
    
    /**
     * Returns the grid unit at a given position.
     * @param column index of the grid unit in the row (zero based)
     * @return the requested grid unit.
     */
    public GridUnit getGridUnit(int column) {
        return (GridUnit)gridUnits.get(column);
    }
    
    /**
     * Returns the grid unit at a given position. In contrast to getGridUnit() this 
     * method returns null if there's no grid unit at the given position. The number of
     * grid units for row x can be smaller than the number of grid units for row x-1.
     * @param column index of the grid unit in the row (zero based)
     * @return the requested grid unit or null if there's no grid unit at this position.
     */
    public GridUnit safelyGetGridUnit(int column) {
        if (column < gridUnits.size()) {
            return (GridUnit)gridUnits.get(column);
        } else {
            return null;
        }
    }

    /**
     * Returns a flag for this effective row. Only a subset of the flags on GridUnit is supported.
     * The flag is determined by inspecting flags on the EffRow's GridUnits.
     * @param which the requested flag (one of {@link EffRow#FIRST_IN_PART} or {@link
     * EffRow#LAST_IN_PART})
     * @return true if the flag is set
     */
    public boolean getFlag(int which) {
        if (which == FIRST_IN_PART) {
            return getGridUnit(0).getFlag(GridUnit.FIRST_IN_PART);
        } else if (which == LAST_IN_PART) {
            return getGridUnit(0).getFlag(GridUnit.LAST_IN_PART);
        } else {
            throw new IllegalArgumentException("Illegal flag queried: " +  which);
        }
    }


    /**
     * Returns the break class for this row. This is a combination of break-before set on
     * the first children of any cells starting on this row.
     * <p><strong>Note:</strong> this works only after getNextKuthElements on the
     * corresponding TableCellLM have been called!</p>
     * 
     * @return one of {@link Constants#EN_AUTO}, {@link Constants#EN_COLUMN}, {@link
     * Constants#EN_PAGE}, {@link Constants#EN_EVEN_PAGE}, {@link Constants#EN_ODD_PAGE}
     */
    public int getBreakBefore() {
        int breakBefore = Constants.EN_AUTO;
        for (Iterator iter = gridUnits.iterator(); iter.hasNext();) {
            GridUnit gu = (GridUnit) iter.next();
            if (gu.isPrimary()) {
                breakBefore = BreakUtil.compareBreakClasses(breakBefore,
                        gu.getPrimary().getBreakBefore());
            }
        }
        return breakBefore;
    }

    /**
     * Returns the break class for this row. This is a combination of break-after set on
     * the last children of any cells ending on this row.
     * <p><strong>Note:</strong> this works only after getNextKuthElements on the
     * corresponding TableCellLM have been called!</p>
     * 
     * @return one of {@link Constants#EN_AUTO}, {@link Constants#EN_COLUMN}, {@link
     * Constants#EN_PAGE}, {@link Constants#EN_EVEN_PAGE}, {@link Constants#EN_ODD_PAGE}
     */
    public int getBreakAfter() {
        int breakAfter = Constants.EN_AUTO;
        for (Iterator iter = gridUnits.iterator(); iter.hasNext();) {
            GridUnit gu = (GridUnit) iter.next();
            if (!gu.isEmpty() && gu.getColSpanIndex() == 0 && gu.isLastGridUnitRowSpan()) {
                breakAfter = BreakUtil.compareBreakClasses(breakAfter,
                        gu.getPrimary().getBreakAfter());
            }
        }
        return breakAfter;
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer("EffRow {");
        sb.append(index);
        if (getBodyType() == TableRowIterator.BODY) {
            sb.append(" in body");
        } else if (getBodyType() == TableRowIterator.HEADER) {
            sb.append(" in header");
        } else {
            sb.append(" in footer");
        }
        sb.append(", ").append(height);
        sb.append(", ").append(explicitHeight);
        sb.append(", ").append(gridUnits.size()).append(" gu");
        sb.append("}");
        return sb.toString();
    }
}
