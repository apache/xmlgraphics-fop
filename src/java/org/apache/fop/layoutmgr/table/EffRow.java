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

import java.util.Iterator;
import java.util.List;

import org.apache.fop.traits.MinOptMax;

/**
 * This class represents an effective row in a table and holds a list of grid units occupying
 * the row as well as some additional values.
 */
public class EffRow {
    
    private List gridUnits = new java.util.ArrayList();
    private int index;
    private int bodyType;
    private MinOptMax height;
    private MinOptMax explicitHeight;
    
    public EffRow(int index, int bodyType) {
        this.index = index;
        this.bodyType = bodyType;
        this.height = height;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    public int getBodyType() {
        return this.bodyType;
    }
    
    public MinOptMax getHeight() {
        return this.height;
    }
    
    public void setHeight(MinOptMax mom) {
        this.height = mom;
    }
    
    public MinOptMax getExplicitHeight() {
        return this.explicitHeight;
    }
    
    public void setExplicitHeight(MinOptMax mom) {
        this.explicitHeight = mom;
    }
    
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
    
    public void setFlagForAllGridUnits(int flag, boolean value) {
        Iterator iter = gridUnits.iterator();
        while (iter.hasNext()) {
            GridUnit gu = (GridUnit)iter.next();
            gu.setFlag(flag, value);
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