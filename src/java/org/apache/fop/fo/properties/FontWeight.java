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

import org.apache.fop.datatypes.IntegerType;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;

public class FontWeight extends Property  {
    public static final int dataTypes = INTEGER | ENUM | INHERIT;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = FONT_SELECTION;

    public int getTraitMapping() {
        return traitMapping;
    }

    public static final int initialValueType = INTEGER_IT;

    public int getInitialValueType() {
        return initialValueType;
    }

    public static final int NORMAL = 1;
    public static final int BOLD = 2;
    public static final int BOLDER = 3;
    public static final int LIGHTER = 4;

    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new IntegerType(PropNames.FONT_WEIGHT, 400);
    }

    public static final int inherited = COMPUTED;

    public int getInherited() {
        return inherited;
    }


    private static final String[] rwEnums = {
        null
        ,"normal"
        ,"bold"
        ,"bolder"
        ,"lighter"
    };

    /*
     * @param propindex - the <tt>int</tt> property index.
     * @param foNode - the <tt>FONode</tt> being built
     * @param value <tt>PropertyValue</tt> returned by the parser
     * @return <tt>PropertyValue</tt> the verified value
     */
    public PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue value)
                    throws PropertyException
    {
        return refineParsing(propindex, foNode, value, NOT_NESTED);
    }

    /**
     * Do the work for the three argument refineParsing method.
     * @param propindex - the <tt>int</tt> property index.
     * @param foNode - the <tt>FONode</tt> being built
     * @param value <tt>PropertyValue</tt> returned by the parser
     * @param nested <tt>boolean</tt> indicating whether this method is
     * called normally (false), or as part of another <i>refineParsing</i>
     * method.
     * @return <tt>PropertyValue</tt> the verified value
     */
    public PropertyValue refineParsing
        (int propindex, FONode foNode, PropertyValue value, boolean nested)
                    throws PropertyException
    {
        // Override the shadowed method to ensure that Integer values
        // are limited to the valid numbers
        PropertyValue fw =
                    super.refineParsing(propindex, foNode, value, nested);
        // If the result is an IntegerType, restrict the values
        if (fw instanceof IntegerType) {
            int weight = ((IntegerType)fw).getInt();
            if (weight % 100 != 0 || weight < 100 || weight > 900)
                throw new PropertyException
                    ("Invalid integer font-weight value: " + weight);
        }
        return fw;
    }
    public int getEnumIndex(String enumval) throws PropertyException {
        return enumValueToIndex(enumval, rwEnums);
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }
}

