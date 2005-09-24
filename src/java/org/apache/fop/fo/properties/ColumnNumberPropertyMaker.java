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

package org.apache.fop.fo.properties;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.flow.TableFObj;
import org.apache.fop.fo.flow.TableBody;

/**
 * Maker class for the column-number property on table-cells and
 * table-columns
 *
 */
public class ColumnNumberPropertyMaker extends NumberProperty.Maker {

    public ColumnNumberPropertyMaker(int propId) {
        super(propId);
    }

    /**
     * Set default column-number from parent's currentColumnIndex.
     *
     * @param   propertyList
     * @return  the default value for column-number
     * @throws  PropertyException
     */
    public Property make(PropertyList propertyList) throws PropertyException {
        FObj fo = propertyList.getFObj();

        if (fo.getNameId() == Constants.FO_TABLE_CELL
                || fo.getNameId() == Constants.FO_TABLE_COLUMN) {
            TableFObj parent = (TableFObj) propertyList.getParentFObj();
            int columnIndex = parent.getCurrentColumnIndex();
            if (fo.getNameId() == Constants.FO_TABLE_CELL
                    && parent.getNameId() == Constants.FO_TABLE_BODY) {
                boolean startsRow = propertyList.get(Constants.PR_STARTS_ROW)
                    .getEnum() == Constants.EN_TRUE;

                //cell w/ starts-row="true", but previous cell
                //didn't have ends-row="true", so index has still has
                //to be reset (for other cases this already happened in
                //body.addChildNode())
                if (startsRow && !((TableBody) parent).lastCellEndedRow()) {
                    //reset column index, and reassign...
                    ((TableBody) parent).resetColumnIndex();
                    columnIndex = parent.getCurrentColumnIndex();
                }
            }
            return new NumberProperty(columnIndex);
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
     * @see org.apache.fop.fo.properties.PropertyMaker#get(int, PropertyList, boolean, boolean)
     */
    public Property get(int subpropId, PropertyList propertyList,
                        boolean tryInherit, boolean tryDefault) 
            throws PropertyException {
        
        Property p = super.get(0, propertyList, tryInherit, tryDefault);
        FObj fo = propertyList.getFObj();
        TableFObj parent = (TableFObj) propertyList.getParentFObj();
        
        if (p.getNumeric().getValue() <= 0) {
            int columnIndex = parent.getCurrentColumnIndex();
            fo.getLogger().warn("Specified negative or zero value for "
                    + "column-number on " + fo.getName() + ": "
                    + p.getNumeric().getValue() + " forced to " 
                    + columnIndex);
            return new NumberProperty(columnIndex);
        }
        //TODO: check for non-integer value and round
        
        //if column-number was explicitly specified, force the parent's current
        //column index to the specified value, so that the updated index will
        //be the correct initial value for the next cell (see Rec 7.26.8)
        if (propertyList.getExplicit(Constants.PR_COLUMN_NUMBER) != null) {
            parent.setCurrentColumnIndex(p.getNumeric().getValue());
        }
        return p;
    }
}
