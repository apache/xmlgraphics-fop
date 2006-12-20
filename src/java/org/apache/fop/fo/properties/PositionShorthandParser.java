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
import org.apache.fop.fo.PropertyList;

/**
 * A shorthand parser for the position shorthand. It is used to set
 * values for absolute-position and relative-position.
 */
public class PositionShorthandParser implements ShorthandParser {
    
    /**
     * @see org.apache.fop.fo.properties.ShorthandParser#getValueForProperty(int, Property, PropertyMaker, PropertyList)
     */
    public Property getValueForProperty(int propId,
            Property property,
            PropertyMaker maker,
            PropertyList propertyList) {
        
        int propVal = property.getEnum();
        if (propId == Constants.PR_ABSOLUTE_POSITION) {
            switch (propVal) {
            case Constants.EN_STATIC:
            case Constants.EN_RELATIVE:
                return EnumProperty.getInstance(Constants.EN_AUTO, "AUTO");
            case Constants.EN_ABSOLUTE:
                return EnumProperty.getInstance(Constants.EN_ABSOLUTE, "ABSOLUTE");
            case Constants.EN_FIXED:
                return EnumProperty.getInstance(Constants.EN_FIXED, "FIXED");
            default:
                //nop
            }
        }
        if (propId == Constants.PR_RELATIVE_POSITION) {
            switch (propVal) {
            case Constants.EN_STATIC:
                return EnumProperty.getInstance(Constants.EN_STATIC, "STATIC");
            case Constants.EN_RELATIVE:
                return EnumProperty.getInstance(Constants.EN_RELATIVE, "RELATIVE");
            case Constants.EN_ABSOLUTE:
                return EnumProperty.getInstance(Constants.EN_STATIC, "STATIC");
            case Constants.EN_FIXED:
                return EnumProperty.getInstance(Constants.EN_STATIC, "STATIC");
            default:
                //nop
            }
        }
        return null;
    }
}
