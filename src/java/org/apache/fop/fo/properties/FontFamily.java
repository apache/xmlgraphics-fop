/*
 * $Id$
 *
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 * Software Foundation, please see <http://www.apache.org/>.
 *  
 */
package org.apache.fop.fo.properties;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.fop.datatypes.FontFamilySet;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.StringType;
import org.apache.fop.datatypes.indirect.Inherit;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.expr.PropertyException;

public class FontFamily extends Property  {
    public static final int dataTypes = COMPLEX | INHERIT | FONTSET;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = FONT_SELECTION;

    public int getTraitMapping() {
        return traitMapping;
    }

    public static final int initialValueType = NOTYPE_IT;

    public int getInitialValueType() {
        return initialValueType;
    }

    public static final int SERIF = 1;
    public static final int SANS_SERIF = 2;
    public static final int CURSIVE = 3;
    public static final int FANTASY = 4;
    public static final int MONOSPACE = 5;
    public static final int inherited = COMPUTED;

    public int getInherited() {
        return inherited;
    }


    private static final String[] rwEnums = {
        null
        ,"serif"
        ,"sans-serif"
        ,"cursive"
        ,"fantasy"
        ,"monospace"
    };
    private static final HashMap rwEnumHash;
    static {
        rwEnumHash = new HashMap((int)(rwEnums.length / 0.75) + 1);
        for (int i = 1; i < rwEnums.length; i++ ) {
            rwEnumHash.put(rwEnums[i],
                                Ints.consts.get(i));
        }
    }

    public PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue value)
                    throws PropertyException
    {
        // There is no point in attempting to validate the enumeration
        // tokens, because all NCNames and all Literals are valid.
        // A PropertyValueList, which itself implements propertyValue,
        // has the structure
        // (PropertyValue|PropertyValueList)+
        // Multiple members represent values that were comma-separated
        // in the original expression.  PropertyValueList members
        // represent values that were space-separated in the original
        // expression.  So, if a prioritised list of font generic or
        // family names was provided, the NCNames of font families will
        // be at the top level, and any font family names
        // that contained spaces will be in PropertyValueLists.

        return refineParsing(propindex, foNode, value, NOT_NESTED);
    }

    public PropertyValue refineParsing
        (int property, FONode foNode, PropertyValue value, boolean nested)
                    throws PropertyException
    {
        //int property = value.getProperty();
        int type = value.getType();
        // First, check that we have a list
        if (type != PropertyValue.LIST) {
            if ( ! nested && type == PropertyValue.INHERIT) {
                return ((Inherit)value).resolve(foNode);
            }
            if ( ! (value instanceof StringType))
                throw new PropertyException
                    ("Invalid " + value.getClass().getName() +
                        " PropertyValue for font-family");
            return new FontFamilySet(property,
                    new String[] {((StringType)value).getString() });
        }
        PropertyValueList list = (PropertyValueList)value;
        String[] strings = new String[list.size()];
        int i = 0;          // the strings index
        Iterator scan = list.iterator();
        while (scan.hasNext()) {
            Object pvalue = scan.next();
            String name = "";
            if (pvalue instanceof PropertyValue) {
                int ptype = ((PropertyValue)pvalue).getType();
                if (ptype == PropertyValue.LIST) {
                    // build a font name according to
                    // 7.8.2 "font-family" <family-name>
                    Iterator font = ((PropertyValueList)pvalue).iterator();
                    while (font.hasNext())
                        name = name + (name.length() == 0 ? "" : " ")
                                + ((StringType)(font.next())).getString();
                    strings[i++] = name;
                    continue;
                }
                else if (pvalue instanceof StringType) {
                    name = ((StringType)pvalue).getString();
                    strings[i++] = name;
                    continue;
                }
            }
            throw new PropertyException
                    ("Invalid " + value.getClass().getName() +
                        " PropertyValue for font-family");
        }
        // Construct the FontFamilySet property value
        return new FontFamilySet(property, strings);
    }

}

