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

import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableColumn;

/**
 * This class represents a primary grid unit of a spanned cell.
 */
public class PrimaryGridUnit extends GridUnit {

    private Cell cellLM;
    private LinkedList elements;
    /** index of row where this cell starts */
    private int startRow;

    
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
    
    public int getStartRow() {
        return this.startRow;
    }

    /** @see java.lang.Object#toString() */
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(" startRow=").append(startRow);
        return sb.toString();
    }
    
}
