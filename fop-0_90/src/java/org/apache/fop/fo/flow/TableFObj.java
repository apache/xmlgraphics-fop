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

package org.apache.fop.fo.flow;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.ValidationPercentBaseContext;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;

/**
 * Superclass for table-related FOs
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
     * @see FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        borderAfterPrecedence = pList.get(PR_BORDER_AFTER_PRECEDENCE).getNumeric();
        borderBeforePrecedence = pList.get(PR_BORDER_BEFORE_PRECEDENCE).getNumeric();
        borderEndPrecedence = pList.get(PR_BORDER_END_PRECEDENCE).getNumeric();
        borderStartPrecedence = pList.get(PR_BORDER_START_PRECEDENCE).getNumeric();
        //Complain if table has separate border-model and fo is not a table or cell
        //see: Rec 6.7.4, 6.7.6 - 6.7.9
        if (getNameId() != FO_TABLE && getNameId() != FO_TABLE_CELL
                && getTable().isSeparateBorderModel()
                && getCommonBorderPaddingBackground().hasBorderInfo()) {
            attributeWarning("In the separate border model (border-collapse=\"separate\")"
                    + ", borders cannot be specified on a " + getName() 
                    + ", but a non-zero value for border was found. The border will be ignored. ");
        }
        if (getNameId() != FO_TABLE //Separate check for fo:table in Table.java
                && getNameId() != FO_TABLE_CELL
                && getCommonBorderPaddingBackground().hasPadding(
                        ValidationPercentBaseContext.getPseudoContextForValidationPurposes())) {
            attributeWarning(getName() + " does not have padding"
                    + " (see the property list for " + getName() + " in XSL 1.0)"
                    + ", but a non-zero value for padding was found. The padding will be ignored.");
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
    public int getCurrentColumnIndex() {
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
    public void setCurrentColumnIndex(int newIndex) {
        //do nothing by default
    }
    
    /**
     * Checks if a certain column-number is already occupied
     * (overridden for Table, TableBody, TableRow)
     * 
     * @param colNr the column-number to check
     * @return true if column-number is already in use
     */
    protected boolean isColumnNumberUsed(int colNr) {
        return false;
    }
    
    /**
     * Convenience method to returns a reference 
     * to the base Table instance
     * 
     * @return  the base table instance
     * 
     */
    protected Table getTable() {
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

}
