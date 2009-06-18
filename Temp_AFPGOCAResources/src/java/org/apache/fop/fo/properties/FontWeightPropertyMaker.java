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
 *
/* $Id$ */

package org.apache.fop.fo.properties;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.PropertyInfo;
import org.apache.fop.fo.expr.PropertyParser;

public class FontWeightPropertyMaker extends EnumProperty.Maker {

    /**
     * Main constructor
     * @param propId    the property id
     */
    public FontWeightPropertyMaker(int propId) {
        super(propId);
    }

    /**
     * {@inheritDoc}
     */
    public Property make(PropertyList pList, String value, FObj fo)
                        throws PropertyException {
        if ("inherit".equals(value)) {
            return super.make(pList, value, fo);
        } else {
            String pValue = checkValueKeywords(value);
            Property newProp = checkEnumValues(pValue);
            int enumValue = -1;
            if (newProp != null
                    && ((enumValue = newProp.getEnum()) == Constants.EN_BOLDER
                        || enumValue == Constants.EN_LIGHTER)) {
                /* check for relative enum values, compute in relation to parent */
                Property parentProp = pList.getInherited(Constants.PR_FONT_WEIGHT);
                if (enumValue == Constants.EN_BOLDER) {
                    enumValue = parentProp.getEnum();
                    switch (enumValue) {
                    case Constants.EN_100:
                        newProp = EnumProperty.getInstance(Constants.EN_200, "200");
                        break;
                    case Constants.EN_200:
                        newProp = EnumProperty.getInstance(Constants.EN_300, "300");
                        break;
                    case Constants.EN_300:
                        newProp = EnumProperty.getInstance(Constants.EN_400, "400");
                        break;
                    case Constants.EN_400:
                        newProp = EnumProperty.getInstance(Constants.EN_500, "500");
                        break;
                    case Constants.EN_500:
                        newProp = EnumProperty.getInstance(Constants.EN_600, "600");
                        break;
                    case Constants.EN_600:
                        newProp = EnumProperty.getInstance(Constants.EN_700, "700");
                        break;
                    case Constants.EN_700:
                        newProp = EnumProperty.getInstance(Constants.EN_800, "800");
                        break;
                    case Constants.EN_800:
                    case Constants.EN_900:
                        newProp = EnumProperty.getInstance(Constants.EN_900, "900");
                        break;
                    default:
                        //nop
                    }
                } else {
                    enumValue = parentProp.getEnum();
                    switch (enumValue) {
                    case Constants.EN_100:
                    case Constants.EN_200:
                        newProp = EnumProperty.getInstance(Constants.EN_100, "100");
                        break;
                    case Constants.EN_300:
                        newProp = EnumProperty.getInstance(Constants.EN_200, "200");
                        break;
                    case Constants.EN_400:
                        newProp = EnumProperty.getInstance(Constants.EN_300, "300");
                        break;
                    case Constants.EN_500:
                        newProp = EnumProperty.getInstance(Constants.EN_400, "400");
                        break;
                    case Constants.EN_600:
                        newProp = EnumProperty.getInstance(Constants.EN_500, "500");
                        break;
                    case Constants.EN_700:
                        newProp = EnumProperty.getInstance(Constants.EN_600, "600");
                        break;
                    case Constants.EN_800:
                        newProp = EnumProperty.getInstance(Constants.EN_700, "700");
                        break;
                    case Constants.EN_900:
                        newProp = EnumProperty.getInstance(Constants.EN_800, "800");
                        break;
                    default:
                        //nop
                    }
                }
            } else if (enumValue == -1) {
                /* neither a keyword, nor an enum
                 * still maybe a valid expression, so send it through the parser... */
                newProp = PropertyParser.parse(value, new PropertyInfo(this, pList));
            }
            if (newProp != null) {
                newProp = convertProperty(newProp, pList, fo);
            }
            return newProp;
        }
    }

}
