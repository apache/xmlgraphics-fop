/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

package org.apache.fop.fo.flow;

// Java
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.StaticPropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonRelativePosition;

/**
 * Class modelling the fo:table-body object.
 */
public class TableBody extends TableFObj {
    // The value of properties relevant for fo:table-body.
    private CommonAccessibility commonAccessibility;
    private CommonAural commonAural;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonRelativePosition commonRelativePosition;
    private int visibility;
    // End of property values
    
    private PropertyList savedPropertyList;

    /**
     * used for validation
     */
    protected boolean tableRowsFound = false;
    protected boolean tableCellsFound = false;
    
    /**
     * used for initial values of column-number property
     */
    protected List pendingSpans;
    protected BitSet usedColumnIndices = new BitSet();
    private int columnIndex = 1;
    private boolean firstRow = true;
    
    /**
     * @param parent FONode that is the parent of the object
     */
    public TableBody(FONode parent) {
        super(parent);
    }

    /**
     * @see FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        commonAccessibility = pList.getAccessibilityProps();
        commonAural = pList.getAuralProps();
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonRelativePosition = pList.getRelativePositionProps();
        visibility = pList.get(PR_VISIBILITY).getEnum();
        super.bind(pList);
        //Used by convertCellsToRows()
        savedPropertyList = pList;
    }
    
    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        initPendingSpans();
        getFOEventHandler().startBody(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
        getFOEventHandler().endBody(this);
        if (!(tableRowsFound || tableCellsFound)) {
            if (getUserAgent().validateStrictly()) {
                missingChildElementError("marker* (table-row+|table-cell+)");
            } else {
                getLogger().error("fo:table-body must not be empty. "
                        + "Expected: marker* (table-row+|table-cell+)");
                getParent().removeChild(this);
            }
        }
        /*
        if (tableCellsFound) {
            convertCellsToRows();
        }*/
        //release references
        savedPropertyList = null;
        pendingSpans = null;
        usedColumnIndices = null;
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: marker* (table-row+|table-cell+)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (localName.equals("marker")) {
                if (tableRowsFound || tableCellsFound) {
                   nodesOutOfOrderError(loc, "fo:marker", "(table-row+|table-cell+)");
                }
            } else if (localName.equals("table-row")) {
                tableRowsFound = true;
                if (tableCellsFound) {
                    invalidChildError(loc, nsURI, localName, "Either fo:table-rows" +
                      " or fo:table-cells may be children of an " + getName() +
                      " but not both");
                }
            } else if (localName.equals("table-cell")) {
                tableCellsFound = true;
                if (tableRowsFound) {
                    invalidChildError(loc, nsURI, localName, "Either fo:table-rows" +
                      " or fo:table-cells may be children of an " + getName() +
                      " but not both");
                }  
            } else {
                invalidChildError(loc, nsURI, localName);
            }
        } else {
            invalidChildError(loc, nsURI, localName);
        }
    }
    
    /**
     * @see org.apache.fop.fo.FONode#addChildNode(FONode)
     */
    protected void addChildNode(FONode child) throws FOPException {
        if (child.getNameId() == FO_TABLE_CELL) {
            addCellNode( (TableCell) child);
        } else {
            super.addChildNode(child);
        }
    }

    /**
     * Adds a cell to the list of child nodes, and updates the columnIndex
     * used for determining the initial value of column-number
     * 
     * @param cell  cell to add
     * @throws FOPException
     */
    private void addCellNode(TableCell cell) throws FOPException {
        //if firstRow flag is still true, the cell starts a row, 
        //and there was a previous cell that didn't explicitly
        //end the previous row => set firstRow flag to false
        if (firstRow && cell.startsRow() && !lastCellEndedRow()) {
            firstRow = false;
        }
        int rowSpan = cell.getNumberRowsSpanned();
        int colSpan = cell.getNumberColumnsSpanned();
        //if there were no explicit columns, pendingSpans
        //will not be properly initialized for the first row
        if (firstRow && getTable().columns == null) {
            if (pendingSpans == null) {
                pendingSpans = new java.util.ArrayList();
            }
            for (int i = colSpan; --i >= 0;) {
                pendingSpans.add(null);
            }
        }
        //if the current cell spans more than one row,
        //update pending span list for the next row
        if (rowSpan > 1) {
            for (int i = colSpan; --i >= 0;) {
                pendingSpans.set(columnIndex - 1 + i, 
                        new PendingSpan(rowSpan));
            }
        }
        //flag column indices used by this cell,
        //take into account that possibly not all column-numbers
        //are used by columns in the parent table (if any),
        //so a cell spanning three columns, might actually
        //take up more than three columnIndices...
        int startIndex = columnIndex - 1;
        int endIndex = startIndex + colSpan;
        if (getTable().columns != null) {
            List cols = getTable().columns;
            int tmpIndex = endIndex;
            for (int i = startIndex; i <= tmpIndex; ++i) {
                if (i < cols.size() && cols.get(i) == null) {
                    endIndex++;
                }
            }
        }
        for (int i = startIndex; i < endIndex; i++) {
            usedColumnIndices.set(i);
        }
        setNextColumnIndex();
        super.addChildNode(cell);
        if (cell.endsRow()) {
            if (firstRow) {
                firstRow = false;
            }
            resetColumnIndex();
        }
    }

    /**
     * If table-cells are used as direct children of a table-body|header|footer
     * they are replaced in this method by proper table-rows.
     * @throws FOPException if there's a problem binding the TableRows properties.
     */
    private void convertCellsToRows() throws FOPException {
        //getLogger().debug("Converting cells to rows...");
        List cells = new java.util.ArrayList(childNodes);
        childNodes.clear();
        Iterator i = cells.iterator();
        TableRow row = null;
        while (i.hasNext()) {
            TableCell cell = (TableCell)i.next();
            if (cell.startsRow() && (row != null)) {
                childNodes.add(row);
                row = null;
            }
            if (row == null) {
                row = new TableRow(this);
                PropertyList pList = new StaticPropertyList(row, savedPropertyList);
                pList.setWritingMode();
                row.bind(pList);
            }
            row.addReplacedCell(cell);
            if (cell.endsRow()) {
                childNodes.add(row);
                row = null;
            }
        }
        if (row != null) {
            childNodes.add(row);
        }
    }
    
    /**
     * @return the Common Border, Padding, and Background Properties.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /** @see org.apache.fop.fo.FONode#getLocalName() */
    public String getLocalName() {
        return "table-body";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_TABLE_BODY;
    }

    /**
     * @param obj table row in question
     * @return true if the given table row is the first row of this body.
     */
    public boolean isFirst(TableRow obj) {
        return (childNodes.size() > 0) 
            && (childNodes.get(0) == obj);
    }

    /**
     * @param obj table row in question
     * @return true if the given table row is the first row of this body.
     */
    public boolean isLast(TableRow obj) {
        return (childNodes.size() > 0) 
            && (childNodes.get(childNodes.size() - 1) == obj);
    }
    
    /**
     * Initializes pending spans list; used for correctly
     * assigning initial value for column-number for the
     * cells of following rows
     *
     */
    protected void initPendingSpans() {
        if (getTable().columns != null) {
            List tableCols = getTable().columns;
            pendingSpans = new java.util.ArrayList(tableCols.size());
            for (int i = tableCols.size(); --i >= 0;) {
                pendingSpans.add(null);
            }
        }
    }
        
    /**
     * Returns the current column index of the TableBody
     * 
     * @return the next column number to use
     */
    public int getCurrentColumnIndex() {
        return columnIndex;
    }

    /**
     * Sets the current column index to a specific value
     * (used by TableCell.bind() in case the column-number
     * was explicitly specified)
     * 
     * @param newIndex  the new column index
     */
    public void setCurrentColumnIndex(int newIndex) {
        columnIndex = newIndex;
    }

    /**
     * Resets the current column index for the TableBody
     *
     */
    public void resetColumnIndex() {
        columnIndex = 1;
        for (int i = 0; i < usedColumnIndices.size(); i++) {
            usedColumnIndices.clear(i);
        }
        PendingSpan pSpan;
        for (int i = pendingSpans.size(); --i >= 0;) {
            pSpan = (PendingSpan) pendingSpans.get(i);
            if (pSpan != null) {
                pSpan.rowsLeft--;
                if (pSpan.rowsLeft == 0) {
                    pendingSpans.set(i, null);
                }
            }
            if (pendingSpans.get(i) != null) {
                usedColumnIndices.set(i);
            } else {
                usedColumnIndices.clear(i);
            }
        }
        if (!firstRow) {
            setNextColumnIndex();
        }
    }

    /**
     * Increases columnIndex to the next available value
     *
     */
    private void setNextColumnIndex() {
        while (usedColumnIndices.get(columnIndex - 1)) {
            //increment columnIndex
            columnIndex++;
            //if the table has explicit columns, and
            //the updated index is not assigned to any
            //column, increment further until the next
            //index occupied by a column...
            if (getTable().columns != null) {
                while (columnIndex <= getTable().columns.size()
                        && !getTable().isColumnNumberUsed(columnIndex) ) {
                    columnIndex++;
                }
            }
        }
    }

    /**
     * Checks whether the previous cell had 'ends-row="true"'
     * 
     * @return false only if there was a previous cell, which
     *         had ends-row="false" (implicit or explicit)
     */
    public boolean lastCellEndedRow() {
        if (childNodes != null) {
            FONode prevNode = (FONode) childNodes.get(childNodes.size() - 1);
            if (prevNode.getNameId() == FO_TABLE_CELL) {
                return ((TableCell) prevNode).endsRow();
            }
        }
        return true;
    }

    /**
     * Checks whether a given column-number is already in use
     * for the current row (used by TableCell.bind());
     * 
     * @param   colNr   the column-number to check
     * @return true if column-number is already occupied
     */
    public boolean isColumnNumberUsed(int colNr) {
        return usedColumnIndices.get(colNr - 1);
    }    
}

