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

public class ReferenceOrientation extends Property  {
    public static final int dataTypes = INTEGER | INHERIT;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = NEW_TRAIT;

    public int getTraitMapping() {
        return traitMapping;
    }

    public static final int initialValueType = INTEGER_IT;

    public int getInitialValueType() {
        return initialValueType;
    }

    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new IntegerType(PropNames.REFERENCE_ORIENTATION, 0);
    }
    public static final int inherited = COMPUTED;

    public int getInherited() {
        return inherited;
    }

    public PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue value)
                    throws PropertyException
    {
        return refineParsing(propindex, foNode, value, NOT_NESTED);
    }

    public PropertyValue refineParsing
        (int propindex, FONode foNode, PropertyValue value, boolean nested)
                    throws PropertyException
    {
        int proptype = value.getType();
        switch (proptype) {
        case PropertyValue.INTEGER:
            int angle = ((IntegerType)value).getInt();
            int absangle = Math.abs(angle);
            if (absangle == 0 || absangle == 90
                    || absangle == 180 || absangle == 270) {
                if (angle < 0) {
                    angle += 360;
                    ((IntegerType)value).setInt(angle);
                }
                return value;
            } else {
                int adjangle = angle % 360;
                if (adjangle < 0) {
                    adjangle += 360;
                }
                adjangle = (int)(Math.round(adjangle / 90.0f)) * 4;
                logger.warning("Illegal orientation value " + angle +
                        ". Replaced with " + adjangle);
                ((IntegerType)value).setInt(adjangle);
                return value;
            }
        default:
            return super.refineParsing(propindex, foNode, value, nested);
        }
    }

}

