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
import java.util.LinkedList;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.None;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.TextDecorator;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;

public class TextDecoration extends Property  {
    public static final int dataTypes = COMPLEX | NONE;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = NEW_TRAIT;

    public int getTraitMapping() {
        return traitMapping;
    }

    public static final int initialValueType = TEXT_DECORATION_IT;

    public int getInitialValueType() {
        return initialValueType;
    }

    /**
     * text-decoration cannot be inherited, even though the current value
     * is passed down to all text-bearing chikdren of a block on which it is
     * defined.  Just return a NoProperty.
     * 
     */
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new TextDecorator(
                PropNames.TEXT_DECORATION, NULL_DECORATION, NULL_DECORATION);
    }
    public static final int inherited = NO;

    public int getInherited() {
        return inherited;
    }


    /** Text decoration constant. */
    public static final byte
      NULL_DECORATION = -1
       ,NO_DECORATION = 0
           ,UNDERLINE = 1
            ,OVERLINE = 2
        ,LINE_THROUGH = 4
               ,BLINK = 8

     ,ALL_DECORATIONS = UNDERLINE | OVERLINE | LINE_THROUGH | BLINK
                    ;


    private static final String[] alternatives = {
                                null
                                ,"underline"
                                ,"overline"
                                ,"line-through"
                                ,"blink"
                            };

    public static final ROStringArray enums
                                        = new ROStringArray(alternatives);

    private static final int[] decorations = {
                                NO_DECORATION
                                ,UNDERLINE
                                ,OVERLINE
                                ,LINE_THROUGH
                                ,BLINK
                            };

    private int getAlternativeIndex(String alt) throws PropertyException {
        for (int i = 1; i < alternatives.length; i++)
            if (alt.equals(alternatives[i])) return i;
        throw new PropertyException("Invalid text decoration: " + alt);
    }

    public PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue list)
                    throws PropertyException
    {
        byte onMask = NO_DECORATION;
        byte offMask = NO_DECORATION;
        int type = list.getType();
        Iterator iter;
        PropertyValueList ssList = null;
        LinkedList strings = new LinkedList();
        if ( ! (type == PropertyValue.LIST)) {
            switch (type) {
            case PropertyValue.NCNAME:
                strings.add(((NCName)list).getNCName());
                break;
            case PropertyValue.NONE:
                strings.add("none");
                break;
            default:
                throw new PropertyException
                    ("text-decoration requires list of NCNames");
            }
        } else { // list is a PropertyValueList
            ssList = spaceSeparatedList((PropertyValueList)list);
            iter = ssList.iterator();
            while (iter.hasNext()) {
                Object value = iter.next();
                if (value instanceof NCName)
                    strings.add(((NCName)value).getNCName());
                else if(value instanceof None)
                    strings.add("none");
                else
                    throw new PropertyException
                        ("text-decoration requires a list of NCNames");
            }
        }
        iter = strings.iterator();
        while (iter.hasNext()) {
            int i;
            String str, str2;
            boolean negate;
            negate = false;
            str = (String)iter.next();
            if (str.equals("none")) {
                offMask |= ALL_DECORATIONS;
                break;
            }
            str2 = str;
            if (str.indexOf("no-") == 0) {
                str2 = str.substring(3);
                negate = true;
            }
            i = getAlternativeIndex(str2);
            if (negate) offMask |= decorations[i];
            else         onMask |= decorations[i];
        }
        if ((offMask & onMask) != 0)
            throw new PropertyException
                ("Contradictory instructions for text-decoration " +
                    list.toString());
        return new TextDecorator(list.getProperty(), onMask, offMask);
    }

}

