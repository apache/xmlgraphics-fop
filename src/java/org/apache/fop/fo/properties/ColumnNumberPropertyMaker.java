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

package org.apache.fop.fo.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableFObj;

/**
 * PropertyMaker subclass for the column-number property
 *
 */
public class ColumnNumberPropertyMaker extends NumberProperty.Maker {

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
     * {@inheritDoc}
     */
    public Property make(PropertyList propertyList, String value, FObj fo) 
                throws PropertyException {
        Property p = super.make(propertyList, value, fo);
        
        TableFObj parent = (TableFObj) propertyList.getParentFObj();
        
        int columnIndex = p.getNumeric().getValue();
        if (columnIndex <= 0) {
            Log log = LogFactory.getLog(TableFObj.class);
            log.warn("Specified negative or zero value for "
                    + "column-number on " + fo.getName() + ": "
                    + columnIndex + " forced to " 
                    + parent.getCurrentColumnIndex());
            return NumberProperty.getInstance(parent.getCurrentColumnIndex());
        } else {
            double tmpIndex = p.getNumeric().getNumericValue();
            if (tmpIndex - columnIndex > 0.0) {
                columnIndex = (int) Math.round(tmpIndex);
                Log log = LogFactory.getLog(TableFObj.class);
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

