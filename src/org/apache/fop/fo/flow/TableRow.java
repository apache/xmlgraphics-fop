/*
 * -- $Id$ --
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;

// Java
import java.util.ArrayList;
import java.util.Iterator;

public class TableRow extends FObj {

    boolean setup = false;

    int breakAfter;
    ColorType backgroundColor;

    KeepValue keepWithNext;
    KeepValue keepWithPrevious;
    KeepValue keepTogether;

    int widthOfCellsSoFar = 0;
    int largestCellHeight = 0;
    int minHeight = 0;    // force row height
    ArrayList columns;

    boolean areaAdded = false;

    private RowSpanMgr rowSpanMgr = null;
    private CellArray cellArray = null;

    private static class CellArray {
        public static final byte EMPTY = 0;
        public static final byte CELLSTART = 1;
        public static final byte CELLSPAN = 2;

        private TableCell[] cells;
        private byte[] states;

        public CellArray(RowSpanMgr rsi, int numColumns) {
            // Initialize the cell array by marking any cell positions
            // occupied by spans from previous rows
            cells = new TableCell[numColumns];
            states = new byte[numColumns];
            for (int i = 0; i < numColumns; i++) {
                if (rsi.isSpanned(i + 1)) {
                    cells[i] = rsi.getSpanningCell(i + 1);
                    states[i] = CELLSPAN;
                } else
                    states[i] = EMPTY;
            }
        }

        /**
         * Return column which doesn't already contain a span or a cell
         * If past the end or no free cells after colNum, return -1
         * Otherwise return value >= input value.
         */
        int getNextFreeCell(int colNum) {
            for (int i = colNum - 1; i < states.length; i++) {
                if (states[i] == EMPTY)
                    return i + 1;
            }
            return -1;
        }


        /**
         * Return type of cell in colNum (1 based)
         */
        int getCellType(int colNum) {
            if (colNum > 0 && colNum <= cells.length) {
                return states[colNum - 1];
            } else
                return -1;    // probably should throw exception
        }

        /**
         * Return cell in colNum (1 based)
         */
        TableCell getCell(int colNum) {
            if (colNum > 0 && colNum <= cells.length) {
                return cells[colNum - 1];
            } else
                return null;    // probably should throw exception
        }

        /**
         * Store cell starting at cellColNum (1 based) and spanning numCols
         * If any of the columns is already occupied, return false, else true
         */
        boolean storeCell(TableCell cell, int colNum, int numCols) {
            boolean rslt = true;
            int index = colNum - 1;
            for (int count = 0; index < cells.length && count < numCols;
                    count++, index++) {
                if (cells[index] == null) {
                    cells[index] = cell;
                    states[index] = (count == 0) ? CELLSTART : CELLSPAN;
                } else {
                    rslt = false;
                    // print a message but continue!!!
                }
            }
            return rslt;
        }

        // private class EnumCells implements Enumeration {
        // private int iNextIndex=0;
        // private Object nextCell = null;
        // EnumCells() {
        // findNextCell();
        // }

        // private void findNextCell() {
        // for (; iNextIndex < cells.length; iNextIndex++) {
        // if (states[iNextIndex] == CELLSTART) {
        // nextCell = cells[iNextIndex];
        // return;
        // }
        // }
        // nextCell = null;
        // }

        // public boolean hasMoreElements() {
        // return (nextCell != null);
        // }

        // public Object nextElement() {
        // if (nextCell != null) {
        // Object cell = nextCell;
        // findNextCell();
        // return cell;
        // }
        // else throw new java.util.NoSuchElementException("No more cells");
        // }
        // }

        // /**
        // * Return an enumeration over all cells in this row
        // * Return each element in cells whose state is CELLSTART or EMPTY?
        // * Skip spanning elements.
        // */
        // Enumeration getCells() {
        // return new EnumCells();
        // }
    }


    public TableRow(FONode parent) {
        super(parent);
    }

    public void setColumns(ArrayList columns) {
        this.columns = columns;
    }

    public KeepValue getKeepWithPrevious() {
        return keepWithPrevious;
    }

    public void doSetup() {

        // Common Accessibility Properties
        AccessibilityProps mAccProps = propMgr.getAccessibilityProps();
            
        // this.properties.get("block-progression-dimension");

        // Common Aural Properties
        AuralProps mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        // only background apply, border apply if border-collapse
        // is collapse.
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // Common Relative Position Properties
        RelativePositionProps mRelProps = propMgr.getRelativePositionProps();
                    
        // this.properties.get("break-before");
        // this.properties.get("break-after");
        setupID();
        // this.properties.get("height");
        // this.properties.get("keep-together");
        // this.properties.get("keep-with-next");
        // this.properties.get("keep-with-previous");


        this.breakAfter = this.properties.get("break-after").getEnum();
        this.backgroundColor =
            this.properties.get("background-color").getColorType();

        this.keepTogether = getKeepValue("keep-together.within-column");
        this.keepWithNext = getKeepValue("keep-with-next.within-column");
        this.keepWithPrevious =
            getKeepValue("keep-with-previous.within-column");

        this.minHeight = this.properties.get("height").getLength().mvalue();
        setup = true;
    }

    private KeepValue getKeepValue(String sPropName) {
        Property p = this.properties.get(sPropName);
        Number n = p.getNumber();
        if (n != null)
            return new KeepValue(KeepValue.KEEP_WITH_VALUE, n.intValue());
        switch (p.getEnum()) {
        case Constants.ALWAYS:
            return new KeepValue(KeepValue.KEEP_WITH_ALWAYS, 0);
        // break;
        case Constants.AUTO:
        default:
            return new KeepValue(KeepValue.KEEP_WITH_AUTO, 0);
        // break;
        }
    }

    /**
     * Called by parent FO to initialize information about
     * cells started in previous rows which span into this row.
     * The layout operation modifies rowSpanMgr
     */
    public void setRowSpanMgr(RowSpanMgr rowSpanMgr) {
        this.rowSpanMgr = rowSpanMgr;
    }

    /**
     * Before starting layout for the first time, initialize information
     * about spanning rows, empty cells and spanning columns.
     */
    private void initCellArray() {
        cellArray = new CellArray(rowSpanMgr, columns.size());
        int colNum = 1;
        Iterator eCells = children.iterator();
        while (eCells.hasNext()) {
            colNum = cellArray.getNextFreeCell(colNum);
            // If off the end, the rest of the cells had better be
            // explicitly positioned!!! (returns -1)

            TableCell cell = (TableCell)eCells.next();
            int numCols = cell.getNumColumnsSpanned();
            int numRows = cell.getNumRowsSpanned();
            int cellColNum = cell.getColumnNumber();

            if (cellColNum == 0) {
                // Not explicitly specified, so put in next available colummn
                // cell.setColumnNumber(colNum);
                // If cellColNum "off the end", this cell is in limbo!
                if (colNum < 1) {
                    // ERROR!!!
                    continue;
                } else
                    cellColNum = colNum;
            } else if (cellColNum > columns.size()) {
                // Explicit specification out of range!
                // Skip it and print an ERROR MESSAGE
                continue;
            }
            // see if it fits and doesn't overwrite anything
            if (cellColNum + numCols - 1 > columns.size()) {
                // MESSAGE: TOO MANY COLUMNS SPANNED!
                numCols = columns.size() - cellColNum + 1;
            }
            // Check for overwriting other cells (returns false)
            if (cellArray.storeCell(cell, cellColNum, numCols) == false) {
                // Print out some kind of warning message.
            }
            if (cellColNum > colNum) {
                // Cells are initialized as empty already
                colNum = cellColNum;
            } else if (cellColNum < colNum) {
                // MESSAGE ? cells out of order?
                colNum = cellColNum;    // CR "to the letter"!
            }
            int cellWidth = getCellWidth(cellColNum, numCols);
            cell.setWidth(cellWidth);
            colNum += numCols;          // next cell in this column
        }
    }

    // ATTENTION if startCol + numCols > number of columns in table!
    private int getCellWidth(int startCol, int numCols) {
        int width = 0;
        for (int count = 0; count < numCols; count++) {
            width += ((TableColumn)columns.get(startCol + count
                                                     - 1)).getColumnWidth();
        }
        return width;
    }

}
