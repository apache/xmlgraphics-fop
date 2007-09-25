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

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Property subclass for the font shorthand
 */
public class FontShorthandProperty extends ListProperty {

    /**
     * Inner class for creating instances of FontShorthandProperty
     */
    public static class Maker extends PropertyMaker {

        private static final int[] PROP_IDS = {
            Constants.PR_FONT_SIZE, Constants.PR_FONT_FAMILY,
            Constants.PR_LINE_HEIGHT, Constants.PR_FONT_STYLE,
            Constants.PR_FONT_VARIANT, Constants.PR_FONT_WEIGHT
        };
        
        /**
         * @param propId ID of the property for which Maker should be created
         */
        public Maker(int propId) {
            super(propId);
        }
        
        /**
         * {@inheritDoc} 
         */
        public Property make(PropertyList propertyList, 
                String value, FObj fo) throws PropertyException {
            
            try {
                FontShorthandProperty newProp = new FontShorthandProperty();
                newProp.setSpecifiedValue(value);
                
                String specVal = value;
                Property prop = null;
                if ("inherit".equals(specVal)) {
                    /* fill the list with the individual properties from the parent */
                    for (int i = PROP_IDS.length; --i >= 0;) {
                        prop = propertyList.getFromParent(PROP_IDS[i]);
                        newProp.addProperty(prop, i);
                    }
                } else {
                    /* initialize list with nulls */
                    for (int pos = PROP_IDS.length; --pos >= 0;) {
                        newProp.addProperty(null, pos);
                    }
                    prop = checkEnumValues(specVal);
                    if (prop == null) {
                        /* not an enum:
                         * value should consist at least of font-size and font-family
                         * separated by a space
                         * mind the possible spaces from quoted font-family names
                         */
                        int spaceIndex = value.indexOf(' ');
                        int quoteIndex = (value.indexOf('\'') == -1)
                            ? value.indexOf('\"') : value.indexOf('\'');
                        if (spaceIndex == -1 
                                || (quoteIndex != -1 && spaceIndex > quoteIndex)) {
                            /* no spaces or first space appears after the first
                             * single/double quote, so malformed value string
                             */
                            throw new PropertyException("Invalid property value: "
                                    + "font=\"" + value + "\"");                        
                        } 
                        PropertyMaker m = null;
                        int fromIndex = spaceIndex + 1;
                        int toIndex = specVal.length();
                        /* at least one space that appears before the first
                         * single/double quote, so extract the individual properties
                         */
                        boolean fontFamilyParsed = false;
                        int commaIndex = value.indexOf(',');
                        while (!fontFamilyParsed) {
                            /* value contains a (list of) possibly quoted 
                             * font-family name(s) 
                             */
                            if (commaIndex == -1) {
                                /* no list, just a single name 
                                 * (or first name in the list)
                                 */
                                if (quoteIndex != -1) {
                                    /* a single name, quoted
                                     */
                                    fromIndex = quoteIndex;
                                }
                                m = FObj.getPropertyMakerFor(PROP_IDS[1]);
                                prop = m.make(propertyList, specVal.substring(fromIndex), fo);
                                newProp.addProperty(prop, 1);
                                fontFamilyParsed = true;                            
                            } else {
                                if (quoteIndex != -1 && quoteIndex < commaIndex) {
                                    /* a quoted font-family name as first name
                                     * in the comma-separated list
                                     * fromIndex = index of the first quote
                                     */
                                    fromIndex = quoteIndex;
                                    quoteIndex = -1;
                                } else {
                                    fromIndex = value.lastIndexOf(' ', commaIndex) + 1;
                                }
                                commaIndex = -1;
                            }
                        }
                        toIndex = fromIndex - 1;
                        fromIndex = value.lastIndexOf(' ', toIndex - 1) + 1;
                        value = specVal.substring(fromIndex, toIndex);
                        int slashIndex = value.indexOf('/');
                        String fontSize = value.substring(0, 
                                (slashIndex == -1) ? value.length() : slashIndex);
                        m = FObj.getPropertyMakerFor(PROP_IDS[0]);
                        prop = m.make(propertyList, fontSize, fo);
                        /* need to make sure subsequent call to LineHeightPropertyMaker.make()
                         * doesn't generate the default font-size property...
                         */
                        propertyList.putExplicit(PROP_IDS[0], prop);
                        newProp.addProperty(prop, 0);
                        if (slashIndex != -1) {
                            /* line-height */
                            String lineHeight = value.substring(slashIndex + 1);
                            m = FObj.getPropertyMakerFor(PROP_IDS[2]);
                            prop = m.make(propertyList, lineHeight, fo);
                            newProp.addProperty(prop, 2);
                        }
                        if (fromIndex != 0) {
                            toIndex = fromIndex - 1;
                            value = specVal.substring(0, toIndex);
                            fromIndex = 0;
                            spaceIndex = value.indexOf(' ');
                            do {
                                toIndex = (spaceIndex == -1) ? value.length() : spaceIndex;
                                String val = value.substring(fromIndex, toIndex);
                                for (int i = 6; --i >= 3;) {
                                    if (newProp.list.get(i) == null) {
                                        /* not set */
                                        m = FObj.getPropertyMakerFor(PROP_IDS[i]);
                                        val = m.checkValueKeywords(val);
                                        prop = m.checkEnumValues(val);
                                        if (prop != null) {
                                            newProp.addProperty(prop, i);
                                        }
                                    }
                                }
                                fromIndex = toIndex + 1;
                                spaceIndex = value.indexOf(' ', fromIndex);
                            } while (toIndex != value.length());
                        }
                    } else {
                        //TODO: implement enum values
                        log.warn("Enum values other than \"inherit\""
                                + " not yet supported for the font shorthand.");
                        return null;
                    }
                }
                if (newProp.list.get(0) == null || newProp.list.get(1) == null) {
                    throw new PropertyException("Invalid property value: "
                            + "font-size and font-family are required for the font shorthand"
                            + "\nfont=\"" + value + "\"");
                }
                return newProp;
           } catch (PropertyException pe) {
               pe.setLocator(propertyList.getFObj().getLocator());
               pe.setPropertyName(getName());
               throw pe;
           }
        }
    }
    
    private void addProperty(Property prop, int pos) {
        while (list.size() < (pos + 1)) {
            list.add(null);
        }
        list.set(pos, prop);
    }
}
