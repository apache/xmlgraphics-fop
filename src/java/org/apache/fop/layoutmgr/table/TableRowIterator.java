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

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.traits.MinOptMax;

/**
 * Iterator that lets the table layout manager step over all rows of a table.
 */
public class TableRowIterator {

    /** Logger **/
    private static Log log = LogFactory.getLog(TableRowIterator.class);

    private Table table;
    private ColumnSetup columns;
    
    private List currentRow = new java.util.ArrayList();
    private int currentRowIndex = -1;
    //TODO rows should later be a Jakarta Commons LinkedList so concurrent modifications while 
    //using a ListIterator are possible
    private List rows = new java.util.ArrayList();
    private int indexOfFirstRowInList;
    private int currentIndex = -1;
    
    //prefetch state
    private ListIterator bodyIterator = null;
    private ListIterator childInBodyIterator = null;
    
    public TableRowIterator(Table table, ColumnSetup columns) {
        this.table = table;
        this.columns = columns;
        this.bodyIterator = table.getChildNodes();
    }
    
    public void prefetchAll() {
        while (prefetchNext()) {
            System.out.println("found row...");
        }
    }
    
    public EffRow getNextRow() {
        currentIndex++;
        boolean moreRows = true;
        while (moreRows && rows.size() < currentIndex + 1) {
            moreRows = prefetchNext();
        }
        if (currentIndex < rows.size()) {
            return getCachedRow(currentIndex);
        } else {
            return null;
        }
    }
    
    public EffRow getCachedRow(int index) {
        return (EffRow)rows.get(index);
    }
    
    private boolean prefetchNext() {
        if (childInBodyIterator != null) {
            if (!childInBodyIterator.hasNext()) {
                //force skip on to next body
                childInBodyIterator = null;
            }
        }
        if (childInBodyIterator == null) {
            if (bodyIterator.hasNext()) {
                childInBodyIterator = ((TableBody)bodyIterator.next()).getChildNodes();
            } else {
                //no more rows
                return false;
            }
        }
        Object node = childInBodyIterator.next();
        this.currentRow.clear();
        this.currentRowIndex++;
        if (node instanceof TableRow) {
            TableRow row = (TableRow)node;
            ListIterator cellIterator = row.getChildNodes();
            while (cellIterator.hasNext()) {
                this.currentRow.add(cellIterator.next());
            }
        } else if (node instanceof TableCell) {
            this.currentRow.add(node);
            if (!((TableCell)node).endsRow()) {
                while (childInBodyIterator.hasNext()) {
                    TableCell cell = (TableCell)childInBodyIterator.next();
                    if (cell.startsRow()) {
                        //next row already starts here, one step back
                        childInBodyIterator.previous();
                        break;
                    }
                    this.currentRow.add(cell);
                    if (cell.endsRow()) {
                        break;
                    }
                }
            }
        } else {
            throw new IllegalStateException("Illegal class found: " + node.getClass().getName());
        }
        EffRow gridUnits = buildGridRow(this.currentRow);
        log.debug(gridUnits);
        rows.add(gridUnits);
        return true;
    }

    private EffRow buildGridRow(List cells) {
        EffRow row = new EffRow(this.currentRowIndex);
        List gridUnits = row.getGridUnits();
        
        //Transfer available cells to their slots
        int colnum = 1;
        ListIterator iter = cells.listIterator();
        while (iter.hasNext()) {
            TableCell cell = (TableCell)iter.next();
            if (cell.hasColumnNumber()) {
                colnum = cell.getColumnNumber();
            }
            while (colnum > gridUnits.size()) {
                gridUnits.add(null);
            }
            if (gridUnits.get(colnum - 1) != null) {
                log.error("Overlapping cell at position " + colnum);
                //TODO throw layout exception
            }
            TableColumn col = columns.getColumn(colnum);

            //Add grid unit for primary grid unit
            PrimaryGridUnit gu = new PrimaryGridUnit(cell, col, colnum - 1, this.currentRowIndex);
            gridUnits.set(colnum - 1, gu);
            
            //Add cell infos on spanned slots if any
            for (int j = 1; j < cell.getNumberColumnsSpanned(); j++) {
                colnum++;
                GridUnit guSpan = new GridUnit(cell, columns.getColumn(colnum), colnum - 1, j);
                if (colnum > gridUnits.size()) {
                    gridUnits.add(guSpan);
                } else {
                    if (gridUnits.get(colnum - 1) != null) {
                        log.error("Overlapping cell at position " + colnum);
                        //TODO throw layout exception
                    }
                    gridUnits.set(colnum - 1, guSpan);
                }
            }
            colnum++;
        }
        
        //Post-processing the list (looking for gaps and resolve start and end borders)
        postProcessGridUnits(gridUnits);
        
        return row;
    }
    
    private void fillEmptyGridUnits(List gridUnits) {
        for (int pos = 1; pos <= gridUnits.size(); pos++) {
            GridUnit gu = (GridUnit)gridUnits.get(pos - 1);
            
            //Empty grid units
            if (gu == null) {
                //Add grid unit
                gu = new PrimaryGridUnit(null, columns.getColumn(pos), 
                        pos - 1, this.currentRowIndex);
                gridUnits.set(pos - 1, gu);
            }
        }
    }
    
    private void postProcessGridUnits(List gridUnits) {
        fillEmptyGridUnits(gridUnits);
            
        /*
        //Border resolution now that the empty grid units are filled
        for (int pos = 1; pos <= gridUnits.size(); pos++) {
            GridUnit starting = (GridUnit)gridUnits.get(pos - 1);
         
            //Border resolution
            if (table.isSeparateBorderModel()) {
                starting.assignBorder(starting.layoutManager);
            } else {
                //Neighbouring grid unit at start edge 
                OldGridUnit start = null;
                int find = pos - 1;
                while (find >= 1) {
                    OldGridUnit candidate = (OldGridUnit)gridUnits.get(find - 1);
                    if (candidate.isLastGridUnitColSpan()) {
                        start = candidate;
                        break;
                    }
                    find--;
                }
                
                //Ending grid unit for current cell
                OldGridUnit ending = null;
                if (starting.layoutManager != null) {
                    pos += starting.layoutManager.getFObj().getNumberColumnsSpanned() - 1;
                }
                ending = (OldGridUnit)gridUnits.get(pos - 1);
                
                //Neighbouring grid unit at end edge 
                OldGridUnit end = null;
                find = pos + 1;
                while (find <= gridUnits.size()) {
                    OldGridUnit candidate = (OldGridUnit)gridUnits.get(find - 1);
                    if (candidate.isPrimaryGridUnit()) {
                        end = candidate;
                        break;
                    }
                    find++;
                }
                CommonBorderPaddingBackground borders = new CommonBorderPaddingBackground();
                OldGridUnit.resolveBorder(table, borders, starting, 
                        (start != null ? start : null), 
                        CommonBorderPaddingBackground.START);
                starting.effBorders = borders;
                if (starting != ending) {
                    borders = new CommonBorderPaddingBackground();
                }
                OldGridUnit.resolveBorder(table, borders, ending, 
                        (end != null ? end : null), 
                        CommonBorderPaddingBackground.END);
                ending.effBorders = borders;
                //Only start and end borders here, before and after during layout
                //TODO resolve before and after borders during layout
            }
        }*/
    }
    
    public class EffRow {
        
        private List gridUnits = new java.util.ArrayList();
        private int index;
        private MinOptMax height = new MinOptMax(0);
        
        public EffRow(int index) {
            this.index = index;
            this.height = height;
        }
        
        public int getIndex() {
            return this.index;
        }
        
        public MinOptMax getHeight() {
            return this.height;
        }
        
        public void setHeight(MinOptMax height) {
            this.height = height;
        }
        
        public List getGridUnits() {
            return gridUnits;
        }
        
        /** @see java.lang.Object#toString() */
        public String toString() {
            StringBuffer sb = new StringBuffer("EffRow {");
            sb.append(index);
            sb.append(", ").append(height);
            sb.append(", ").append(gridUnits.size()).append(" gu");
            sb.append("}");
            return sb.toString();
        }
    }
    
}
