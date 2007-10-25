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

package org.apache.fop.fo.flow;

import java.util.List;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.ValidationPercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.NumberProperty;
import org.apache.fop.fo.properties.Property;

/**
 * Common base class for table-related FOs
 */
public abstract class TableFObj extends FObj {

    private Numeric borderAfterPrecedence;
    private Numeric borderBeforePrecedence;
    private Numeric borderEndPrecedence;
    private Numeric borderStartPrecedence;
    
    /**
     * Used for determining initial values for column-numbers
     * in case of row-spanning cells
     * (for clarity)
     *
     */
    protected static class PendingSpan {
        
        /**
         * member variable holding the number of rows left
         */
        protected int rowsLeft;
        
        /**
         * Constructor
         * 
         * @param rows  number of rows spanned
         */
        public PendingSpan(int rows) {
            rowsLeft = rows;
        }        
    }
    
    /**
     * Main constructor
     * 
     * @param parent    the parent node
     */
    public TableFObj(FONode parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}
     */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        borderAfterPrecedence = pList.get(PR_BORDER_AFTER_PRECEDENCE).getNumeric();
        borderBeforePrecedence = pList.get(PR_BORDER_BEFORE_PRECEDENCE).getNumeric();
        borderEndPrecedence = pList.get(PR_BORDER_END_PRECEDENCE).getNumeric();
        borderStartPrecedence = pList.get(PR_BORDER_START_PRECEDENCE).getNumeric();
        if (getNameId() != FO_TABLE //Separate check for fo:table in Table.java
                && getNameId() != FO_TABLE_CELL
                && getCommonBorderPaddingBackground().hasPadding(
                        ValidationPercentBaseContext.getPseudoContext())) {
            attributeWarning(
                    "padding-* properties are not applicable to " + getName()
                    + ", but a non-zero value for padding was found.");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void addChildNode(FONode child) throws FOPException {
        if (!inMarker() 
                && child.getNameId() == FO_TABLE_CELL) {
            /* update current column index for the table-body/table-row */
            updateColumnIndex((TableCell) child);
        }
        super.addChildNode(child);
    }
    
    private void updateColumnIndex(TableCell cell) {
        
        int rowSpan = cell.getNumberRowsSpanned();
        int colSpan = cell.getNumberColumnsSpanned();
        int columnIndex = getCurrentColumnIndex();
        int i;
        
        if (getNameId() == FO_TABLE_ROW) {
            
            TableRow row = (TableRow) this;
            
            for (i = colSpan; 
                    --i >= 0 || row.pendingSpans.size() < cell.getColumnNumber();) {
                row.pendingSpans.add(null);
            }
            
            /* if the current cell spans more than one row,
             * update pending span list for the next row
             */
            if (rowSpan > 1) {
                for (i = colSpan; --i >= 0;) {
                    row.pendingSpans.set(columnIndex - 1 + i, 
                            new PendingSpan(rowSpan));
                }
            }
        } else {
            
            TableBody body = (TableBody) this;
            
            /* if body.firstRow is still true, and :
             * a) the cell starts a row,
             * b) there was a previous cell 
             * c) that previous cell didn't explicitly end the previous row
             *  => set firstRow flag to false
             */
            if (body.firstRow && cell.startsRow()) {
                if (!body.previousCellEndedRow()) {
                    body.firstRow = false;
                }
            }
            
            /* pendingSpans not initialized for the first row...
             */
            if (body.firstRow) {
                for (i = colSpan; 
                        --i >= 0|| body.pendingSpans.size() < cell.getColumnNumber();) {
                    body.pendingSpans.add(null);
                }
            }
            
            /* if the current cell spans more than one row,
             * update pending span list for the next row
             */
            if (rowSpan > 1) {
                for (i = colSpan; --i >= 0;) {
                    body.pendingSpans.set(columnIndex - 1 + i, 
                            new PendingSpan(rowSpan));
                }
            }
        }

        /* flag column indices used by this cell,
         * take into account that possibly not all column-numbers
         * are used by columns in the parent table (if any),
         * so a cell spanning three columns, might actually
         * take up more than three columnIndices...
         */
        int startIndex = columnIndex - 1;
        int endIndex = startIndex + colSpan;
        if (getTable().columns != null) {
            List cols = getTable().columns;
            int tmpIndex = endIndex;
            for (i = startIndex; i <= tmpIndex; ++i) {
                if (i < cols.size() && cols.get(i) == null) {
                    endIndex++;
                }
            }
        }
        flagColumnIndices(startIndex, endIndex);
        if (getNameId() != FO_TABLE_ROW && cell.endsRow()) {
            ((TableBody) this).firstRow = false;
            ((TableBody) this).resetColumnIndex();
        }
    }
    
    /**
     * 
     * @param side  the side for which to return the border precedence
     * @return the "border-precedence" value for the given side
     */
    public Numeric getBorderPrecedence(int side) {
        switch (side) {
        case CommonBorderPaddingBackground.BEFORE:
            return borderBeforePrecedence;
        case CommonBorderPaddingBackground.AFTER:
            return borderAfterPrecedence;
        case CommonBorderPaddingBackground.START:
            return borderStartPrecedence;
        case CommonBorderPaddingBackground.END:
            return borderEndPrecedence;
        default:
            return null;
        }
    }

    /**
     * Returns the current column index of the given TableFObj
     * (overridden for Table, TableBody, TableRow)
     * 
     * @return the next column number to use
     */
    protected int getCurrentColumnIndex() {
        return 0;
    }
    
    /**
     * Sets the current column index of the given TableFObj
     * used when a value for column-number is explicitly
     * specified on the child FO (TableCell or TableColumn)
     * (overridden for Table, TableBody, TableRow)
     * 
     * @param   newIndex    new value for column index
     */
    protected void setCurrentColumnIndex(int newIndex) {
        //do nothing by default
    }
        
    /**
     * Checks if a certain column-number is already occupied
     * (overridden for Table, TableBody, TableRow)
     * 
     * @param colNr the column-number to check
     * @return true if column-number is already in use
     */
    public boolean isColumnNumberUsed(int colNr) {
        return false;
    }
    
    /**
     * Convenience method to returns a reference 
     * to the base Table instance
     * 
     * @return  the base table instance
     * 
     */
    public Table getTable() {
        if (this.getNameId() == FO_TABLE) {
            //node is a Table
            //=> return itself
            return (Table) this;
        } else {
            //any other Table-node
            //=> recursive call to parent.getTable()
            return ((TableFObj) parent).getTable();
        }
    }
    
    /**
     * @return the Common Border, Padding, and Background Properties.
     */
    public abstract CommonBorderPaddingBackground getCommonBorderPaddingBackground();
    
    /**
     * Flags column indices from <code>start</code> to <code>end</code>,
     * and updates the current column index.
     * Overridden for Table, TableBody, TableRow
     * @param start start index
     * @param end   end index
     */
    protected void flagColumnIndices(int start, int end) {
        //nop
    }
    
    /**
     * PropertyMaker subclass for the column-number property
     *
     */
    public static class ColumnNumberPropertyMaker extends NumberProperty.Maker {

        /**
         * Constructor
         * @param propId    the id of the property for which the maker should 
         *                  be created
         */
        public ColumnNumberPropertyMaker(int propId) {
            super(propId);
        }

        /**
         * {@inheritDoc}
         */
        public Property make(PropertyList propertyList) 
                throws PropertyException {
            FObj fo = propertyList.getFObj();

            if (fo.getNameId() == Constants.FO_TABLE_CELL
                    || fo.getNameId() == Constants.FO_TABLE_COLUMN) {
                if (fo.getNameId() == Constants.FO_TABLE_CELL
                        && fo.getParent().getNameId() != Constants.FO_TABLE_ROW
                        && (propertyList.get(Constants.PR_STARTS_ROW).getEnum() 
                                == Constants.EN_TRUE)) {
                    TableBody parent = (TableBody) fo.getParent();
                    if (!parent.previousCellEndedRow()) {
                        parent.resetColumnIndex();
                    }
                }
            }
            return NumberProperty.getInstance(
                    ((TableFObj) fo.getParent()).getCurrentColumnIndex());
        }
        
        
        /**
         * Check the value of the column-number property. 
         * Return the parent's column index (initial value) in case 
         * of a negative or zero value
         * 
         * @see org.apache.fop.fo.properties.PropertyMaker#make(PropertyList, String, FObj)
         */
        public Property make(PropertyList propertyList, String value, FObj fo) 
                    throws PropertyException {
            Property p = super.make(propertyList, value, fo);
            
            TableFObj parent = (TableFObj) propertyList.getParentFObj();
            
            int columnIndex = p.getNumeric().getValue();
            if (columnIndex <= 0) {
                log.warn("Specified negative or zero value for "
                        + "column-number on " + fo.getName() + ": "
                        + columnIndex + " forced to " 
                        + parent.getCurrentColumnIndex());
                return NumberProperty.getInstance(parent.getCurrentColumnIndex());
            } else {
                double tmpIndex = p.getNumeric().getNumericValue();
                if (tmpIndex - columnIndex > 0.0) {
                    columnIndex = (int) Math.round(tmpIndex);
                    log.warn("Rounding specified column-number of "
                            + tmpIndex + " to " + columnIndex);
                    p = NumberProperty.getInstance(columnIndex);
                }
            }
            
            parent.setCurrentColumnIndex(columnIndex);
            
            int colSpan = propertyList.get(Constants.PR_NUMBER_COLUMNS_SPANNED)
                                .getNumeric().getValue();
            int i = -1;
            while (++i < colSpan) {
                if (parent.isColumnNumberUsed(columnIndex + i)) {
                    /* if column-number is already in use by another 
                     * cell/column => error!
                     */
                    StringBuffer errorMessage = new StringBuffer();
                    errorMessage.append(fo.getName() + " overlaps in column ")
                           .append(columnIndex + i);
                    org.xml.sax.Locator loc = fo.getLocator();
                    if (loc != null && loc.getLineNumber() != -1) {
                        errorMessage.append(" (line #")
                            .append(loc.getLineNumber()).append(", column #")
                            .append(loc.getColumnNumber()).append(")");
                    }
                    throw new PropertyException(errorMessage.toString());
                }
            }
            
            return p;
        }
    }
}
