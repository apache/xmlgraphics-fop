/*
 * $Id$
 *
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 */
package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;

public class BackgroundRepeat extends Property  {
    public static final int dataTypes = ENUM | INHERIT;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = RENDERING;

    public int getTraitMapping() {
        return traitMapping;
    }

    public static final int initialValueType = ENUM_IT;

    public int getInitialValueType() {
        return initialValueType;
    }

    public static final int REPEAT = 1;
    public static final int REPEAT_X = 2;
    public static final int REPEAT_Y = 3;
    public static final int NO_REPEAT = 4;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType (PropNames.BACKGROUND_REPEAT, REPEAT);
    }

    public static final int inherited = NO;

    public int getInherited() {
        return inherited;
    }


    private static final String[] rwEnums = {
        null
        ,"repeat"
        ,"repeat-x"
        ,"repeat-y"
        ,"no-repeat"
    };
    public int getEnumIndex(String enumval) throws PropertyException {
        return enumValueToIndex(enumval, rwEnums);
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }
}


