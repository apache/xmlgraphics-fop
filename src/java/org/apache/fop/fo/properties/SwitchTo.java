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

import java.util.Iterator;

import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;

public class SwitchTo extends Property  {
    public static final int dataTypes = COMPLEX;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = ACTION;

    public int getTraitMapping() {
        return traitMapping;
    }

    public static final int initialValueType = ENUM_IT;

    public int getInitialValueType() {
        return initialValueType;
    }

    public static final int XSL_PRECEDING = 1;
    public static final int XSL_FOLLOWING = 2;
    public static final int XSL_ANY = 3;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType (PropNames.SWITCH_TO, XSL_ANY);
    }
    public static final int inherited = NO;

    public int getInherited() {
        return inherited;
    }


    private static final String[] rwEnums = {
        null
        ,"xsl-preceding"
        ,"xsl-following"
        ,"xsl-any"
    };

    public PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue list)
                    throws PropertyException
    {
        // Check for the enumeration.  Look for a list of NCNames.
        // N.B. it may be possible to perform further checks on the
        // validity of the NCNames - do they match multi-case case names.
        if ( ! (list instanceof PropertyValueList))
            return super.refineParsing(PropNames.SWITCH_TO, foNode, list);

        PropertyValueList ssList =
                            spaceSeparatedList((PropertyValueList)list);
        Iterator iter = ssList.iterator();
        while (iter.hasNext()) {
            Object value = iter.next();
            if ( ! (value instanceof NCName))
                throw new PropertyException
                    ("switch-to requires a list of NCNames");
        }
        return list;
    }
    public int getEnumIndex(String enumval) throws PropertyException {
        return enumValueToIndex(enumval, rwEnums);
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }
}

