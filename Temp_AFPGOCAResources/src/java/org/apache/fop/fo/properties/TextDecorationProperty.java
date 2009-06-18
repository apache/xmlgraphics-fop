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
package org.apache.fop.fo.properties;

import java.util.Iterator;
import java.util.List;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.NCnameProperty;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Special list property for text-decoration.
 */
public class TextDecorationProperty extends ListProperty {

    /**
     * Inner class for creating instances of ListProperty
     */
    public static class Maker extends PropertyMaker {

        /**
         * @param propId ID of the property for which Maker should be created
         */
        public Maker(int propId) {
            super(propId);
        }

        /**
         * {@inheritDoc}
         */
        public Property convertProperty(Property p,
                                        PropertyList propertyList, FObj fo)
                    throws PropertyException {
            if (p instanceof TextDecorationProperty) {
                return p;
            } else {
                if (p instanceof ListProperty) {
                    ListProperty lst = (ListProperty)p;
                    lst = checkEnums(lst);
                    return new TextDecorationProperty((ListProperty)p);
                } else if (p instanceof EnumProperty) {
                    ListProperty lst = new ListProperty(p);
                    return new TextDecorationProperty(lst);
                } else {
                    throw new PropertyException("Cannot convert anything other "
                            + "than a list property, got a " + p.getClass().getName());
                }
            }
        }

        private ListProperty checkEnums(ListProperty lst) throws PropertyException {
            List l = lst.getList();
            for (int i = 0; i < l.size(); i++) {
                Property prop = (Property)l.get(i);
                if (prop instanceof EnumProperty) {
                    //skip
                } else if (prop instanceof NCnameProperty) {
                    Property prop_enum = checkEnumValues(((NCnameProperty)prop).getString());
                    if (prop_enum == null) {
                        throw new PropertyException("Illegal enum value: " + prop.getString());
                    }
                    l.set(i, prop_enum);
                } else {
                    throw new PropertyException("Invalid content for text-decoration "
                            + "property: " + prop);
                }
            }
            return lst;
        }

    }

    /**
     * Constructs a new instance by converting a ListProperty.
     * @param listProp the ListProperty to be converted
     * @throws PropertyException in case the conversion fails
     */
    public TextDecorationProperty(ListProperty listProp) throws PropertyException {
        List lst = listProp.getList();
        boolean none = false;
        boolean under = false;
        boolean over = false;
        boolean through = false;
        boolean blink = false;
        Iterator i = lst.iterator();
        while (i.hasNext()) {
            Property prop = (Property)i.next();
            switch (prop.getEnum()) {
                case Constants.EN_NONE:
                    if (under | over | through | blink) {
                        throw new PropertyException(
                                "Invalid combination of values");
                    }
                    none = true;
                    break;
                case Constants.EN_UNDERLINE:
                case Constants.EN_NO_UNDERLINE:
                    if (none) {
                        throw new PropertyException("'none' specified, no additional values allowed");
                    }
                    if (under) {
                        throw new PropertyException("Invalid combination of values");
                    }
                    under = true;
                    break;
                case Constants.EN_OVERLINE:
                case Constants.EN_NO_OVERLINE:
                    if (none) {
                        throw new PropertyException("'none' specified, no additional values allowed");
                    }
                    if (over) {
                        throw new PropertyException("Invalid combination of values");
                    }
                    over = true;
                    break;
                case Constants.EN_LINE_THROUGH:
                case Constants.EN_NO_LINE_THROUGH:
                    if (none) {
                        throw new PropertyException("'none' specified, no additional values allowed");
                    }
                    if (through) {
                        throw new PropertyException("Invalid combination of values");
                    }
                    through = true;
                    break;
                case Constants.EN_BLINK:
                case Constants.EN_NO_BLINK:
                    if (none) {
                        throw new PropertyException("'none' specified, no additional values allowed");
                    }
                    if (blink) {
                        throw new PropertyException("Invalid combination of values");
                    }
                    blink = true;
                    break;
                default:
                    throw new PropertyException("Invalid value specified: " + prop);
            }
            addProperty(prop);
        }
    }

}
