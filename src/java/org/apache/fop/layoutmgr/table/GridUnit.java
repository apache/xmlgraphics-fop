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

/**
 * This class represents one grid unit inside a table.
 */
public class GridUnit {

    /** Table cell which occupies this grid unit */
    private TableCell cell;
    /** Table column that this grid unit belongs to */
    private TableColumn column;
    
    /** start index of grid unit within row in column direction */
    private int startCol;
    /** index of grid unit within cell in column direction */
    private int colSpanIndex;
    /** index of grid unit within cell in row direction */
    private int rowSpanIndex;
    
    
    public GridUnit(TableCell cell, TableColumn column, int startCol, int colSpanIndex) {
        this.cell = cell;
        this.column = column;
        this.startCol = startCol;
        this.colSpanIndex = colSpanIndex;
    }
    
    public TableCell getCell() {
        return this.cell;
    }
    
    public TableColumn getColumn() {
        return this.column;
    }
    
    public TableRow getRow() {
        if (getCell().getParent() instanceof TableRow) {
            return (TableRow)getCell().getParent();
        } else {
            return null;
        }
    }
    
    public TableBody getBody() {
        FONode node = getCell();
        while (!(node instanceof TableBody)) {
            node = node.getParent();
        }
        return (TableBody)node;
    }
    
    public Table getTable() {
        FONode node = getCell();
        while (!(node instanceof Table)) {
            node = node.getParent();
        }
        return (Table)node;
    }
    
    public boolean isPrimary() {
        return false;
    }
    
    public boolean isEmpty() {
        return this.cell == null;
    }
    
    public int getStartCol() {
        return this.startCol;
    }
    
    /** @see java.lang.Object#toString() */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (isEmpty()) {
            sb.append("EMPTY");
        } else if (isPrimary()) {
            sb.append("Primary");
        }
        sb.append("GridUnit:");
        if (colSpanIndex > 0) {
            sb.append(" colSpan=").append(colSpanIndex);
        }
        if (rowSpanIndex > 0) {
            sb.append(" rowSpan=").append(rowSpanIndex);
        }
        sb.append(" startCol=").append(startCol);
        return sb.toString();
    }
    
}
