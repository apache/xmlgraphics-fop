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

import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.messaging.MessageHandler;

/**
 * Pseudo-property class for common color values occurring in a
 * number of classes.
 */
public class ColorCommon extends Property  {
    public static final int AQUA = 1;
    public static final int BLACK = 2;
    public static final int BLUE = 3;
    public static final int FUSCHIA = 4;
    public static final int GRAY = 5;
    public static final int GREEN = 6;
    public static final int LIME = 7;
    public static final int MAROON = 8;
    public static final int NAVY = 9;
    public static final int OLIVE = 10;
    public static final int PURPLE = 11;
    public static final int RED = 12;
    public static final int SILVER = 13;
    public static final int TEAL = 14;
    public static final int WHITE = 15;
    public static final int YELLOW = 16;
    public static final int TRANSPARENT = 17;

    protected static final String[] rwEnums = {
	null
	,"aqua"
	,"black"
	,"blue"
	,"fuchsia"
	,"gray"
	,"green"
	,"lime"
	,"maroon"
	,"navy"
	,"olive"
	,"purple"
	,"red"
	,"silver"
	,"teal"
	,"white"
	,"yellow"
	,"transparent"
    };

    /**
     * Return the ColorType derived from the argument.
     * The argument must be either a ColorType already, in which case
     * it is returned unchanged, or an NCName whose string value is a
     * standard color or 'transparent'.
     * @param propindex the index of the property to which this value is
     * being applied.
     * @param value <tt>PropertyValue</tt>
     * @return <tt>ColorValue</tt> equivalent of the argument
     * @exception <tt>PropertyException</tt>
     */
    protected static ColorType getColor(int property, PropertyValue value)
            throws PropertyException
    {
        int type = value.getType();
        if (type == PropertyValue.COLOR_TYPE) return (ColorType)value;
        // Must be a color enum
        if (type != PropertyValue.NCNAME)
            throw new PropertyException
                (value.getClass().getName() + " instead of color for "
                                + PropNames.getPropertyName(property));
        // We have an NCName - hope it''s a color
        NCName ncname = (NCName)value;
        // Must be a standard color
        EnumType enum = null;
        ColorType color = null;
        String name = ncname.getNCName();
        try {
            try {
                enum = new EnumType(property, name);
            } catch (PropertyException e) {
                System.out.println("PropertyException: " + e.getMessage());
                MessageHandler.logln(name +
                         " is not a standard color for '"
                                + PropNames.getPropertyName(property)
                                         + "'. Trying as a system-color.");
            }
            if (enum != null)
                color = new ColorType(property, enum.getEnumValue());
            else
                color = new ColorType(property, name);
        } catch (PropertyException e) {
            throw new PropertyException
                (name + " not a standard or system color for "
                                + PropNames.getPropertyName(property));
        }
        return color;
    }

    /**
     * 'value' is a PropertyValue.
     *
     * It must contain
     * either
     *   a ColorType value
     *   a NCName containing a standard color name or 'transparent'
     *   a FromParent value,
     *   a FromNearestSpecified value,
     *   or an Inherit value.
     *
     * @param propindex - the <tt>int</tt> property index.
     * @param foNode - the <tt>FONode</tt> being built
     * @param value <tt>PropertyValue</tt> returned by the parser
     * @return <tt>PropertyValue</tt> the refined value
     */
    public PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue value)
                throws PropertyException
    {
        return refineParsing(propindex, foNode, value, NOT_NESTED);
    }

    /**
     * Do the work for the three argument refineParsing method.
     * @param propindex - the <tt>int</tt> property index.
     * @param foNode - the <tt>FONode</tt> being built
     * @param value <tt>PropertyValue</tt> returned by the parser
     * @param nested <tt>boolean</tt> indicating whether this method is
     * called normally (false), or as part of another <i>refineParsing</i>
     * method.
     * @return <tt>PropertyValue</tt> the verified value
     * @see #refineParsing(FONode,PropertyValue)
     */
    public PropertyValue refineParsing
        (int propindex, FONode foNode, PropertyValue value, boolean nested)
                throws PropertyException
    {
        int type = value.getType();
        switch (type) {
        case PropertyValue.COLOR_TYPE:
            return value;
        case PropertyValue.NCNAME:
            return getColor(propindex, value);
        default:
            PropertyValue pv;
            switch (type) {
            case PropertyValue.FROM_PARENT:
            case PropertyValue.FROM_NEAREST_SPECIFIED:
            case PropertyValue.INHERITED_VALUE:
            case PropertyValue.INHERIT:
                pv = super.refineParsing(propindex, foNode, value, nested);
                if (pv.getType() == PropertyValue.COLOR_TYPE)
                    return pv;
            }
            throw new PropertyException
                        ("Inappropriate dataTypes passed to "
                         + PropNames.getPropertyName(propindex)
                         + ".refineParsing: " + value.getClass().getName());
        }
    }

}

