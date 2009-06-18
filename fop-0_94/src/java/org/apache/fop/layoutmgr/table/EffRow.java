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

import java.util.Iterator;
import java.util.List;

import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.traits.MinOptMax;

/**
 * This class represents an effective row in a table and holds a list of grid units occupying
 * the row as well as some additional values.
 */
class EffRow {
    
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
     */
    public EffRow(int index, int bodyType) {
        this.index = index;
        this.bodyType = bodyType;
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
    
    /** @return the calculated height for this EffRow. */
    public MinOptMax getHeight() {
        return this.height;
    }
    
    /**
     * Sets the calculated height for this EffRow.
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
     * Sets a flag on all grid units of this effective row.
     * @param flag which flag to set (on of the GridUnit.* constants)
     * @param value new value for the flag
     */
    public void setFlagForAllGridUnits(int flag, boolean value) {
        Iterator iter = gridUnits.iterator();
        while (iter.hasNext()) {
            GridUnit gu = (GridUnit)iter.next();
            gu.setFlag(flag, value);
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
    
    /** @see java.lang.Object#toString() */
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
