/*
 * Copyright 2005-2006 The Apache Software Foundation.
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

package org.apache.fop.fo.properties;

import java.util.Iterator;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableFObj;

/**
 * Maker class for the column-number property on table-cells and
 * table-columns
 *
 */
public class ColumnNumberPropertyMaker extends NumberProperty.Maker {

    /**
     * Constructor
     * @param propId    the id of the property for which the maker should be created
     */
    public ColumnNumberPropertyMaker(int propId) {
        super(propId);
    }

    /**
     * @see PropertyMaker#make(PropertyList)
     */
    public Property make(PropertyList propertyList) throws PropertyException {
        FObj fo = propertyList.getFObj();

        if (fo.getNameId() == Constants.FO_TABLE_CELL
                || fo.getNameId() == Constants.FO_TABLE_COLUMN) {
            if (fo.getNameId() == Constants.FO_TABLE_CELL
                    && fo.getParent().getNameId() != Constants.FO_TABLE_ROW
                    && (propertyList.get(Constants.PR_STARTS_ROW).getEnum() 
                            == Constants.EN_TRUE)) {
                TableBody parent = (TableBody) fo.getParent();
                TableCell prevCell = null;                
                for (Iterator i = parent.getChildNodes(); 
                        (i != null && i.hasNext());) {
                    prevCell = (TableCell) i.next();
                }
                if (prevCell != null && !prevCell.endsRow()) {
                    parent.resetColumnIndex();
                }
            }
            return new NumberProperty(((TableFObj) fo.getParent())
                                        .getCurrentColumnIndex());
        } else {
            throw new PropertyException("column-number property is only allowed"
                    + " on fo:table-cell or fo:table-column, not on "
                    + fo.getName());
        }
    }
    
    /**
     * Check the value of the column-number property. 
     * Return the parent's column index (initial value) in case 
     * of a negative or zero value
     * 
     * @see org.apache.fop.fo.properties.PropertyMaker#get(
     *                      int, PropertyList, boolean, boolean)
     */
    public Property get(int subpropId, PropertyList propertyList,
                        boolean tryInherit, boolean tryDefault) 
            throws PropertyException {
        
        Property p = super.get(0, propertyList, tryInherit, tryDefault);
        TableFObj fo = (TableFObj) propertyList.getFObj();
        TableFObj parent = (TableFObj) propertyList.getParentFObj();
        int columnIndex = p.getNumeric().getValue();
        
        if (columnIndex <= 0) {
            fo.getLogger().warn("Specified negative or zero value for "
                    + "column-number on " + fo.getName() + ": "
                    + columnIndex + " forced to " 
                    + parent.getCurrentColumnIndex());
            return new NumberProperty(parent.getCurrentColumnIndex());
        }
        //TODO: check for non-integer value and round
                
        //if column-number was explicitly specified, force the parent's current
        //column index to the specified value, so that the updated index will
        //be the correct initial value for the next cell/column (see Rec 7.26.8)
        if (propertyList.getExplicit(Constants.PR_COLUMN_NUMBER) != null) {
            parent.setCurrentColumnIndex(p.getNumeric().getValue());
        }
        return p;
    }
}
