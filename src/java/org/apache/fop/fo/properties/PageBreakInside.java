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

import org.apache.fop.datatypes.Auto;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.indirect.FromNearestSpecified;
import org.apache.fop.datatypes.indirect.FromParent;
import org.apache.fop.datatypes.indirect.Inherit;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.ShorthandPropSets;
import org.apache.fop.fo.expr.PropertyException;

public class PageBreakInside extends Property  {
    public static final int dataTypes = SHORTHAND | AUTO | ENUM | INHERIT;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = SHORTHAND_MAP;

    public int getTraitMapping() {
        return traitMapping;
    }

    public static final int initialValueType = AUTO_IT;

    public int getInitialValueType() {
        return initialValueType;
    }

    public static final int AVOID = 1;
    public static final int inherited = NO;

    public int getInherited() {
        return inherited;
    }


    private static final String[] rwEnums = {
        null
        ,"avoid"
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
        if (value instanceof Inherit |
                value instanceof FromParent |
                    value instanceof FromNearestSpecified |
                        value instanceof Auto)
        {
            return refineExpansionList(PropNames.PAGE_BREAK_INSIDE, foNode,
                                ShorthandPropSets.expandAndCopySHand(value));
        }
        if (value instanceof NCName) {
            EnumType enumval = null;
            String ncname = ((NCName)value).getNCName();
            //PropertyValueList list =
                    //new PropertyValueList(PropNames.PAGE_BREAK_INSIDE);
            if (ncname.equals("avoid")) {
                //list.add
                    //(new EnumType(PropNames.KEEP_TOGETHER, "always"));
                //return list;
                return new EnumType(PropNames.KEEP_TOGETHER, "always");
            } else
                throw new PropertyException
                ("Unrecognized NCName in page-break-inside: " + ncname);
        }

        throw new PropertyException
            ("Invalid value for 'page-break-inside': "
                + value.getClass().getName());
    }

    public int getEnumIndex(String enumval) throws PropertyException {
        return enumValueToIndex(enumval, rwEnums);
    }
    public String getEnumText(int index) {
        return rwEnums[index];
    }
}

