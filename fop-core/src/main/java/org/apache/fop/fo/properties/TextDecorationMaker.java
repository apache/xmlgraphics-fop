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

import java.util.List;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.NCnameProperty;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Dedicated {@link org.apache.fop.fo.properties.PropertyMaker} for handling the
 * <a href="http://www.w3.org/TR/xsl/#text-decoration"><code>text-decoration</code></a>
 * property.
 */
public class TextDecorationMaker extends ListProperty.Maker {

    /**
     * Create a maker for the given property id.
     * @param propId id of the property for which a maker should be created
     */
    public TextDecorationMaker(int propId) {
        super(propId);
    }

    /**
     * {@inheritDoc}
     * Add validation rules for the <code>text-decoration</code> property.
     */
    @Override
    public Property convertProperty(Property p,
                                    PropertyList propertyList,
                                    FObj fo)
                        throws PropertyException {

        ListProperty listProp = (ListProperty) super.convertProperty(p, propertyList, fo);
        List lst = listProp.getList();
        boolean none = false;
        boolean under = false;
        boolean over = false;
        boolean through = false;
        boolean blink = false;
        int enumValue = -1;
        for (int i = lst.size(); --i >= 0;) {
            Property prop = (Property)lst.get(i);
            if (prop instanceof NCnameProperty) {
                prop = checkEnumValues(prop.getString());
                lst.set(i, prop);
            }
            if (prop != null) {
                enumValue = prop.getEnum();
            }
            switch (enumValue) {
                case Constants.EN_NONE:
                    if (under | over | through | blink) {
                        throw new PropertyException("Invalid combination of values");
                    }
                    none = true;
                    break;
                case Constants.EN_UNDERLINE:
                case Constants.EN_NO_UNDERLINE:
                case Constants.EN_OVERLINE:
                case Constants.EN_NO_OVERLINE:
                case Constants.EN_LINE_THROUGH:
                case Constants.EN_NO_LINE_THROUGH:
                case Constants.EN_BLINK:
                case Constants.EN_NO_BLINK:
                    if (none) {
                        throw new PropertyException(
                                "'none' specified, no additional values allowed");
                    }
                    switch (enumValue) {
                        case Constants.EN_UNDERLINE:
                        case Constants.EN_NO_UNDERLINE:
                            if (!under) {
                                under = true;
                                continue;
                            }
                        case Constants.EN_OVERLINE:
                        case Constants.EN_NO_OVERLINE:
                            if (!over) {
                                over = true;
                                continue;
                            }
                        case Constants.EN_LINE_THROUGH:
                        case Constants.EN_NO_LINE_THROUGH:
                            if (!through) {
                                through = true;
                                continue;
                            }
                        case Constants.EN_BLINK:
                        case Constants.EN_NO_BLINK:
                            if (!blink) {
                                blink = true;
                                continue;
                            }
                        default:
                            throw new PropertyException("Invalid combination of values");
                    }
                default:
                    throw new PropertyException("Invalid value specified: " + p);
            }
        }
        return listProp;
    }
}
