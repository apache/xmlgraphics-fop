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

import java.util.LinkedList;
import java.util.List;

import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableColumn;

/**
 * This class represents a primary grid unit of a spanned cell.
 */
public class PrimaryGridUnit extends GridUnit {

    /** Cell layout manager. */
    private Cell cellLM;
    /** List of Knuth elements representing the contents of the cell. */
    private LinkedList elements;
    /** Index of row where this cell starts */
    private int startRow;
    /** Links to the spanned grid units. (List of GridUnit arrays, one array represents a row) */ 
    private List rows;
    
    public PrimaryGridUnit(TableCell cell, TableColumn column, int startCol, int startRow) {
        super(cell, column, startCol, 0);
        this.startRow = startRow;
        if (cell != null) {
            cellLM = new Cell(cell, this);
        }
    }
    
    public Cell getCellLM() {
        return cellLM;
    }
    
    public boolean isPrimary() {
        return true;
    }
    
    public void setElements(LinkedList elements) {
        this.elements = elements;
    }
    
    public LinkedList getElements() {
        return this.elements;
    }
    
    public List getRows() {
        return this.rows;
    }
    
    public void addRow(GridUnit[] row) {
        if (rows == null) {
            rows = new java.util.ArrayList();
        }
        rows.add(row);
    }
    
    public int getStartRow() {
        return this.startRow;
    }

    public int[] getStartEndBorderWidths() {
        int[] widths = new int[2];
        if (rows == null) {
            widths[0] = getBorders().getBorderStartWidth(false);
            widths[1] = getBorders().getBorderEndWidth(false);
        } else {
            for (int i = 0; i < rows.size(); i++) {
                GridUnit[] gridUnits = (GridUnit[])rows.get(i);
                widths[0] = Math.max(widths[0], 
                        (gridUnits[0]).
                            getBorders().getBorderStartWidth(false));
                widths[1] = Math.max(widths[1], 
                        (gridUnits[gridUnits.length - 1]).
                            getBorders().getBorderEndWidth(false));
            }
        }
        return widths;
    }
    
    /** @see java.lang.Object#toString() */
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(" startRow=").append(startRow);
        return sb.toString();
    }

    /** @return true if this cell spans over more than one grid unit. */
    public boolean hasSpanning() {
        return (getCell().getNumberColumnsSpanned() > 1) 
            || (getCell().getNumberRowsSpanned() > 1);
    }
    
}
