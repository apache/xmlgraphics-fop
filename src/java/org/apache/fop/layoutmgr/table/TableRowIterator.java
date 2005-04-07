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
    
    /** Holds the current row (TableCell instances) */
    private List currentRow = new java.util.ArrayList();
    /** Holds the grid units of cell from the last row while will span over the current row 
     * (GridUnit instance) */
    private List lastRowsSpanningCells = new java.util.ArrayList();
    private int currentRowIndex = -1;
    //TODO rows should later be a Jakarta Commons LinkedList so concurrent modifications while 
    //using a ListIterator are possible
    private List rows = new java.util.ArrayList();
    //private int indexOfFirstRowInList;
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

    private void safelySetListItem(List list, int position, Object obj) {
        while (position >= list.size()) {
            list.add(null);
        }
        list.set(position, obj);
    }
    
    private Object safelyGetListItem(List list, int position) {
        if (position >= list.size()) {
            return null;
        } else {
            return list.get(position);
        }
    }
    
    private EffRow buildGridRow(List cells) {
        EffRow row = new EffRow(this.currentRowIndex);
        List gridUnits = row.getGridUnits();
        
        TableRow rowFO = null;
        TableBody bodyFO = null;
        
        //Create all row-spanned grid units based on information from the last row
        int colnum = 1;
        ListIterator spanIter = lastRowsSpanningCells.listIterator();
        while (spanIter.hasNext()) {
            GridUnit gu = (GridUnit)spanIter.next();
            if (gu != null) {
                GridUnit newGU = gu.createNextRowSpanningGridUnit();
                safelySetListItem(gridUnits, colnum - 1, newGU);
                if (newGU.isLastGridUnitRowSpan()) {
                    spanIter.set(null);
                } else {
                    spanIter.set(newGU);
                }
            }
            colnum++;
        }
        
        //Transfer available cells to their slots
        colnum = 1;
        ListIterator iter = cells.listIterator();
        while (iter.hasNext()) {
            TableCell cell = (TableCell)iter.next();
            
            if (cell.hasColumnNumber()) {
                colnum = cell.getColumnNumber();
            } else {
                //Skip columns with spanning grid units
                while (safelyGetListItem(gridUnits, colnum - 1) != null) {
                    colnum++;
                }
            }

            if (safelyGetListItem(gridUnits, colnum - 1) != null) {
                log.error("Overlapping cell at position " + colnum);
                //TODO throw layout exception
            }
            TableColumn col = columns.getColumn(colnum);

            //Add grid unit for primary grid unit
            PrimaryGridUnit gu = new PrimaryGridUnit(cell, col, colnum - 1, this.currentRowIndex);
            safelySetListItem(gridUnits, colnum - 1, gu);
            boolean hasRowSpanningLeft = !gu.isLastGridUnitRowSpan();
            if (hasRowSpanningLeft) {
                safelySetListItem(lastRowsSpanningCells, colnum - 1, gu);
            }
            
            if (gu.hasSpanning()) {
                //Add grid units on spanned slots if any
                GridUnit[] horzSpan = new GridUnit[cell.getNumberColumnsSpanned()];
                horzSpan[0] = gu;
                for (int j = 1; j < cell.getNumberColumnsSpanned(); j++) {
                    colnum++;
                    GridUnit guSpan = new GridUnit(cell, columns.getColumn(colnum), colnum - 1, j);
                    if (safelyGetListItem(gridUnits, colnum - 1) != null) {
                        log.error("Overlapping cell at position " + colnum);
                        //TODO throw layout exception
                    }
                    safelySetListItem(gridUnits, colnum - 1, guSpan);
                    if (hasRowSpanningLeft) {
                        safelySetListItem(lastRowsSpanningCells, colnum - 1, gu);
                    }
                    horzSpan[j] = guSpan;
                }
                gu.addRow(horzSpan);
            }
            
            //Gather info for empty grid units (used later)
            if (rowFO == null) {
                rowFO = gu.getRow();
            }
            if (bodyFO == null) {
                bodyFO = gu.getBody();
            }
            
            colnum++;
        }
        
        //Post-processing the list (looking for gaps and resolve start and end borders)
        fillEmptyGridUnits(gridUnits, rowFO, bodyFO);
        resolveStartEndBorders(gridUnits);
        
        return row;
    }
    
    private void fillEmptyGridUnits(List gridUnits, TableRow row, TableBody body) {
        for (int pos = 1; pos <= gridUnits.size(); pos++) {
            GridUnit gu = (GridUnit)gridUnits.get(pos - 1);
            
            //Empty grid units
            if (gu == null) {
                //Add grid unit
                gu = new EmptyGridUnit(row, columns.getColumn(pos), body, 
                        pos - 1);
                gridUnits.set(pos - 1, gu);
            }
            
            //Set flags
            gu.setFlag(GridUnit.IN_FIRST_COLUMN, (pos == 1));
            gu.setFlag(GridUnit.IN_LAST_COLUMN, (pos == gridUnits.size()));
        }
    }
    
    private void resolveStartEndBorders(List gridUnits) {
        for (int pos = 1; pos <= gridUnits.size(); pos++) {
            GridUnit starting = (GridUnit)gridUnits.get(pos - 1);
         
            //Border resolution
            if (table.isSeparateBorderModel()) {
                starting.assignBorderForSeparateBorderModel();
            } else {
                //Neighbouring grid unit at start edge 
                GridUnit start = null;
                int find = pos - 1;
                while (find >= 1) {
                    GridUnit candidate = (GridUnit)gridUnits.get(find - 1);
                    if (candidate.isLastGridUnitColSpan()) {
                        start = candidate;
                        break;
                    }
                    find--;
                }
                
                //Ending grid unit for current cell
                GridUnit ending = null;
                if (starting.getCell() != null) {
                    pos += starting.getCell().getNumberColumnsSpanned() - 1;
                }
                ending = (GridUnit)gridUnits.get(pos - 1);
                
                //Neighbouring grid unit at end edge 
                GridUnit end = null;
                find = pos + 1;
                while (find <= gridUnits.size()) {
                    GridUnit candidate = (GridUnit)gridUnits.get(find - 1);
                    if (candidate.isPrimary()) {
                        end = candidate;
                        break;
                    }
                    find++;
                }
                //CommonBorderPaddingBackground borders = new CommonBorderPaddingBackground();
                starting.resolveBorder(start, 
                        CommonBorderPaddingBackground.START);
                //starting.setBorders(borders);
                /*
                if (starting != ending) {
                    borders = new CommonBorderPaddingBackground();
                }*/
                ending.resolveBorder(end, 
                        CommonBorderPaddingBackground.END);
                //ending.setBorders(borders);
                //Only start and end borders here, before and after during layout
                //TODO resolve before and after borders during layout
            }
        }
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
