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

import org.apache.fop.datatypes.CountryType;
import org.apache.fop.datatypes.LanguageType;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;


public class XmlLang extends Property  {
    public static final int dataTypes = SHORTHAND;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = SHORTHAND_MAP;

    public int getTraitMapping() {
        return traitMapping;
    }

    public static final int initialValueType = NOTYPE_IT;

    public int getInitialValueType() {
        return initialValueType;
    }

    public static final int inherited = NO;

    public int getInherited() {
        return inherited;
    }

    /**
     * The value returned from the first stage of parsing must be an NCNAME.
     * This can be any valid RFC 3066 language tag.  I.e. it must have at least
     * a primary-subtag, which may be followed by 0 or more subtags, separated
     * by hyphens.  The primary subtag must be an ISO 639-1 2-letter or
     * ISO 639-2 3-letter code.  The second subtag, if it exists, may be an
     * ISO 3166 2-letter country code.
     * <p>If the primary subtag exists, and is a valid ISO 639 code, it will
     * be used to generate a <code>LanguageType</code>
     * <code>PropertyValue</code> assigned to the <code>Language</code>
     * property.  If not, the property assignment is invalid.
     * <p>If the second subtag exists, and is a valid ISO 3166 code, it
     * will be used to generate a <code>CountryType</code>
     * <code>PropertyValue</code> assigned to the <code>Country</code>.
     * <p>All text beyond the last valid subtag detected, is ignored.
     */
    public PropertyValue refineParsing(
            int propindex, FONode foNode, PropertyValue value)
    throws PropertyException {
        if (value.getType() != PropertyValue.NCNAME) {
            throw new PropertyException("xml:lang requires NCNAME");
        }
        String text = ((NCName)value).getNCName();
        if (text.charAt(text.length() - 1) == '-') {
            throw new PropertyException("Invalid xml:lang code: " + text);
        }
        String[] bits = text.split("-");
        // bits[0] = lang; if bits[1], bits[1] = country
        // Is lang OK?
        LanguageType lang = new LanguageType(PropNames.LANGUAGE, bits[0]);
        // Is there a country code?
        CountryType country = null;
        if (bits.length > 1) {
            try {
                country = new CountryType(PropNames.COUNTRY, bits[1]);
            } catch (PropertyException e) {
                logger.fine("Invalid country code " + bits[1] + " in " + text);
            }
        }
        if (country == null) {
            return lang;
        }
        PropertyValueList list = new PropertyValueList(PropNames.XML_LANG);
        list.add(lang);
        list.add(country);
        return list;
    }

}

