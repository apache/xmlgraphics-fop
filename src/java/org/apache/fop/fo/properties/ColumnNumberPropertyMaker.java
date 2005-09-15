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

public class ColumnNumberPropertyMaker extends NumberProperty.Maker {

    public ColumnNumberPropertyMaker(int propId) {
        super(propId);
    }
    
    /**
     * Set default column-number from parent's currentColumnIndex
     * 
     * @return  the default value for column-number
     */
    public Property make(PropertyList propertyList) throws PropertyException {
        FObj fo = propertyList.getFObj();
        
        if( fo.getNameId() == Constants.FO_TABLE_CELL 
                || fo.getNameId() == Constants.FO_TABLE_COLUMN ) {
            TableFObj parent = (TableFObj) propertyList.getParentFObj();
            return new NumberProperty(parent.getCurrentColumnIndex());
        } else {
            throw new PropertyException("column-number property is only allowed on " 
                    + "fo:table-cell or fo:table-column, not on " + fo.getName());
        }
    }
}
