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

import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;

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
     * @param property the index of the property to which this value is
     * being applied.
     * @param value <tt>PropertyValue</tt>
     * @return <tt>ColorValue</tt> equivalent of the argument
     * @exception PropertyException
     */
    protected static ColorType getColor(int property, PropertyValue value)
            throws PropertyException
    {
        int type = value.getType();
        if (type == PropertyValue.COLOR_TYPE) return (ColorType)value;
        // Must be a color enumval
        if (type != PropertyValue.NCNAME)
            throw new PropertyException
                (value.getClass().getName() + " instead of color for "
                                + PropNames.getPropertyName(property));
        // We have an NCName - hope it''s a color
        NCName ncname = (NCName)value;
        // Must be a standard color
        EnumType enumval = null;
        ColorType color = null;
        String name = ncname.getNCName();
        try {
            try {
                enumval = new EnumType(property, name);
            } catch (PropertyException e) {
                System.out.println("PropertyException: " + e.getMessage());
                logger.warning(name +
                         " is not a standard color for '"
                                + PropNames.getPropertyName(property)
                                         + "'. Trying as a system-color.");
            }
            if (enumval != null)
                color = new ColorType(property, enumval.getEnumValue());
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

