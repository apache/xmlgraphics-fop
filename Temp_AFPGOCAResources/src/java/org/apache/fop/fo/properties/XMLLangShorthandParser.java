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
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

public class XMLLangShorthandParser extends GenericShorthandParser {

    private static final char HYPHEN_MINUS = '-';

    /** {@inheritDoc} */
    public Property getValueForProperty(int propId,
                                        Property property,
                                        PropertyMaker maker,
                                        PropertyList propertyList)
                    throws PropertyException {

        String shorthandValue = property.getString();
        int hyphenIndex = shorthandValue.indexOf(HYPHEN_MINUS);
        if (propId == Constants.PR_LANGUAGE) {
            if (hyphenIndex == -1) {
                /* only language specified; use the whole property */
                return property;
            } else {
                /* use only the primary tag */
                return StringProperty.getInstance(
                        shorthandValue.substring(0, hyphenIndex));
            }
        } else if (propId == Constants.PR_COUNTRY) {
            if (hyphenIndex != -1) {
                int nextHyphenIndex = shorthandValue.indexOf(HYPHEN_MINUS, hyphenIndex + 1);
                if (nextHyphenIndex != -1) {
                    return StringProperty.getInstance(
                            shorthandValue.substring(hyphenIndex + 1, nextHyphenIndex));
                } else {
                    return StringProperty.getInstance(
                            shorthandValue.substring(hyphenIndex + 1));
                }
            }
        }
        return null;
    }
}
