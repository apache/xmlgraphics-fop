/*
 * $Id$
 *
 *  ============================================================================
 *                    The Apache Software License, Version 1.1
 *  ============================================================================
 *  
 *  Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without modifica-
 *  tion, are permitted provided that the following conditions are met:
 *  
 *  1. Redistributions of  source code must  retain the above copyright  notice,
 *     this list of conditions and the following disclaimer.
 *  
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  
 *  3. The end-user documentation included with the redistribution, if any, must
 *     include  the following  acknowledgment:  "This product includes  software
 *     developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *     Alternately, this  acknowledgment may  appear in the software itself,  if
 *     and wherever such third-party acknowledgments normally appear.
 *  
 *  4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *     endorse  or promote  products derived  from this  software without  prior
 *     written permission. For written permission, please contact
 *     apache@apache.org.
 *  
 *  5. Products  derived from this software may not  be called "Apache", nor may
 *     "Apache" appear  in their name,  without prior written permission  of the
 *     Apache Software Foundation.
 *  
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 *  APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 *  DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 *  OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 *  ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 *  (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  This software  consists of voluntary contributions made  by many individuals
 *  on  behalf of the Apache Software  Foundation and was  originally created by
 *  James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 *  Software Foundation, please see <http://www.apache.org/>.
 *  
 */
package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.None;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.TextDecorations;
import org.apache.fop.datatypes.TextDecorator;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.datastructs.ROIntArray;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.properties.Property;

import java.util.LinkedList;
import java.util.Iterator;

public class TextDecoration extends Property  {
    public static final int dataTypes = COMPLEX | NONE | INHERIT;
    public static final int traitMapping = NEW_TRAIT;
    public static final int initialValueType = TEXT_DECORATION_IT;
    public PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new TextDecorations
                            (PropNames.TEXT_DECORATION, NO_DECORATION);
    }
    public static final int inherited = NO;

    /** Text decoration constant. */
    public static final byte
      NO_DECORATION = 0
         ,UNDERLINE = 1
          ,OVERLINE = 2
      ,LINE_THROUGH = 4
             ,BLINK = 8

   ,ALL_DECORATIONS = UNDERLINE | OVERLINE | LINE_THROUGH | BLINK;
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
            iter = ((PropertyValueList)ssList).iterator();
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

