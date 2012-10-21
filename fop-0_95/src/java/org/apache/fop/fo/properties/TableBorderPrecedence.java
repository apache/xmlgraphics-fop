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

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

public class TableBorderPrecedence extends NumberProperty.Maker{
    private static Property num0 = NumberProperty.getInstance(0);
    private static Property num1 = NumberProperty.getInstance(1);
    private static Property num2 = NumberProperty.getInstance(2);
    private static Property num3 = NumberProperty.getInstance(3);
    private static Property num4 = NumberProperty.getInstance(4);
    private static Property num5 = NumberProperty.getInstance(5);
    private static Property num6 = NumberProperty.getInstance(6);

    public TableBorderPrecedence(int propId) {
        super(propId);
    }
    
    /**
     * Set default precedence according to the parent FObj
     * 
     * {@inheritDoc}
     */
    public Property make(PropertyList propertyList) throws PropertyException {
        FObj fo = propertyList.getFObj();
        switch (fo.getNameId()) {
        case Constants.FO_TABLE:
            return num6;
        case Constants.FO_TABLE_CELL:
            return num5;
        case Constants.FO_TABLE_COLUMN:
            return num4;
        case Constants.FO_TABLE_ROW:
            return num3;
        case Constants.FO_TABLE_BODY:
            return num2;
        case Constants.FO_TABLE_HEADER:
            return num1;
        case Constants.FO_TABLE_FOOTER:
            return num0;
        }
        return null;
    }
}
