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

import org.apache.fop.datatypes.AbstractPropertyValue;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.expr.PropertyException;

public class Clip extends Property  {
    public static final int dataTypes = AUTO | COMPLEX | INHERIT;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = RENDERING;

    public int getTraitMapping() {
        return traitMapping;
    }

    public static final int initialValueType = AUTO_IT;

    public int getInitialValueType() {
        return initialValueType;
    }

    public static final int inherited = NO;

    public int getInherited() {
        return inherited;
    }


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
        int type = value.getType();
        if (type == PropertyValue.INHERIT || type == PropertyValue.AUTO)
            return value;
        if (type != PropertyValue.LIST)
            throw new PropertyException
                ("clip: <shape> requires 4 <length> or <auto> args");
        PropertyValueList list = (PropertyValueList) value;
        if (list.size() != 4) throw new PropertyException
                ("clip: <shape> requires 4 lengths");
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if ( obj instanceof AbstractPropertyValue)  {
                AbstractPropertyValue pv = (AbstractPropertyValue)obj;
                if (pv.type == PropertyValue.AUTO ||
                    (pv.type == PropertyValue.NUMERIC &&
                        ((Numeric)pv).isLength())
                ) continue;
            }
            throw new PropertyException
                    ("clip: <shape> requires 4 <length> or <auto> args");
        }
        return value;
    }
}

