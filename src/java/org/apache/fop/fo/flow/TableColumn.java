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

// XML
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;

/**
 * Class modelling the fo:table-column object.
 */
public class TableColumn extends FObj {
    // The value of properties relevant for fo:table-column.
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    // private ToBeImplementedProperty borderAfterPrecedence;
    // private ToBeImplementedProperty borderBeforePrecedence;
    // private ToBeImplementedProperty borderEndPrecedence;
    // private ToBeImplementedProperty borderStartPrecedence;
    private Numeric columnNumber;
    private Length columnWidth;
    private Numeric numberColumnsRepeated;
    private Numeric numberColumnsSpanned;
    private int visibility;
    // End of property values
    
    /**
     * @param parent FONode that is the parent of this object
     */
    public TableColumn(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        // borderAfterPrecedence = pList.get(PR_BORDER_AFTER_PRECEDENCE);
        // borderBeforePrecedence = pList.get(PR_BORDER_BEFORE_PRECEDENCE);
        // borderEndPrecedence = pList.get(PR_BORDER_END_PRECEDENCE);
        // borderStartPrecedence = pList.get(PR_BORDER_START_PRECEDENCE);
        columnNumber = pList.get(PR_COLUMN_NUMBER).getNumeric();
        columnWidth = pList.get(PR_COLUMN_WIDTH).getLength();
        numberColumnsRepeated = pList.get(PR_NUMBER_COLUMNS_REPEATED).getNumeric();
        numberColumnsSpanned = pList.get(PR_NUMBER_COLUMNS_SPANNED).getNumeric();
        visibility = pList.get(PR_VISIBILITY).getEnum();
        
        if (columnNumber.getValue() < 0) {
            //not catching 0 here because it is the indication that no 
            //column-number has been specified
            throw new PropertyException("column-number must be 1 or bigger, "
                    + "but got " + columnNumber.getValue());
        }
        if (numberColumnsRepeated.getValue() <= 0) {
            throw new PropertyException("number-columns-repeated must be 1 or bigger, "
                    + "but got " + numberColumnsRepeated.getValue());
        }
        if (numberColumnsSpanned.getValue() <= 0) {
            throw new PropertyException("number-columns-spanned must be 1 or bigger, "
                    + "but got " + numberColumnsSpanned.getValue());
        }
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode()
     */
    protected void startOfNode() throws FOPException {
        getFOEventHandler().startColumn(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
        getFOEventHandler().endColumn(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: empty
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
            invalidChildError(loc, nsURI, localName);
    }

    /**
     * @return the Common Border, Padding, and Background Properties.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /**
     * @return the "column-width" property.
     */
    public Length getColumnWidth() {
        return columnWidth;
    }

    /**
     * @return true if the "column-number" property was set.
     */
    public boolean hasColumnNumber() {
        return (columnNumber.getValue() >= 1);
    }

    /**
     * @return the "column-number" property.
     */
    public int getColumnNumber() {
        return columnNumber.getValue();
    }

    /** @return value for number-columns-repeated. */
    public int getNumberColumnsRepeated() {
        return numberColumnsRepeated.getValue();
    }
    
    /** @return value for number-columns-spanned. */
    public int getNumberColumnsSpanned() {
        return numberColumnsSpanned.getValue();
    }
    
    /** @see org.apache.fop.fo.FONode#getName() */
    public String getName() {
        return "fo:table-column";
    }

    /** @see org.apache.fop.fo.FObj#getNameId() */
    public int getNameId() {
        return FO_TABLE_COLUMN;
    }
    
    /** @see java.lang.Object#toString() */
    public String toString() {
        StringBuffer sb = new StringBuffer("fo:table-column");
        if (hasColumnNumber()) {
            sb.append(" column-number=").append(getColumnNumber());
        }
        if (getNumberColumnsRepeated() > 1) {
            sb.append(" number-columns-repeated=").append(getNumberColumnsRepeated());
        }
        if (getNumberColumnsSpanned() > 1) {
            sb.append(" number-columns-spanned=").append(getNumberColumnsSpanned());
        }
        sb.append(" column-width=").append(getColumnWidth());
        return sb.toString();
    }
}

