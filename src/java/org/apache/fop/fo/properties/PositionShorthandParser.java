/*
 * Copyright 2004 The Apache Software Foundation.
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
import org.apache.fop.fo.PropertyList;

/**
 * A shorthand parser for the position shorthand. It is used to set
 * values for absolute-position and relative-position.
 */
public class PositionShorthandParser implements ShorthandParser {
    public Property getValueForProperty(int propId,
            Property property,
            PropertyMaker maker,
            PropertyList propertyList) {
        int propVal = property.getEnum();
        if (propId == Constants.PR_ABSOLUTE_POSITION) {
            switch (propVal) {
            case Constants.STATIC:
            case Constants.RELATIVE:
                return new EnumProperty(Constants.AUTO, "AUTO");
            case Constants.ABSOLUTE:
                return new EnumProperty(Constants.ABSOLUTE, "ABSOLUTE");
            case Constants.FIXED:
                return new EnumProperty(Constants.FIXED, "FIXED");
            }
        }
        if (propId == Constants.PR_RELATIVE_POSITION) {
            switch (propVal) {
            case Constants.STATIC:
                return new EnumProperty(Constants.STATIC, "STATIC");
            case Constants.RELATIVE:
                return new EnumProperty(Constants.RELATIVE, "RELATIVE");
            case Constants.ABSOLUTE:
                return new EnumProperty(Constants.STATIC, "STATIC");
            case Constants.FIXED:
                return new EnumProperty(Constants.STATIC, "STATIC");
            }
        }
        return null;
    }
}
