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

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.Percentage;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.ShorthandPropSets;
import org.apache.fop.fo.expr.PropertyException;

public class BackgroundPosition extends Property  {
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


    public static final int
                            LEFT = 1
                         ,CENTER = 2
                          ,RIGHT = 3
                            ,TOP = 4
                        ,CENTERV = 5
                         ,BOTTOM = 6
                                 ;

    private static final String[] rwEnums = {
        null
        ,"left"
        ,"center"
        ,"right"
        ,"top"
        ,"center"
        ,"bottom"
    };

    // Background will need access to this array
    protected static final ROStringArray enums
                                            = new ROStringArray(rwEnums);

    /**
     * 'value' is a PropertyValueList or an individual PropertyValue.
     *
     * <p>If 'value' is an individual PropertyValue, it must contain
     * either
     *   a distance measurement,
     *   a NCName enumeration token,
     *   a FromParent value,
     *   a FromNearestSpecified value,
     *   or an Inherit value.
     * The distance measurement can be either a Length or a Percentage.
     *
     * <p>If 'value' is a PropertyValueList, it contains a
     * PropertyValueList containing either a pair of
     * distance measurement (length or percentage) or a pair of
     * enumeration tokens representing the background position offset
     * in the "height" and "width" dimensions.
     *
     * <p>The value(s) provided, if valid, are converted into a list
     * containing the expansion of the shorthand.  I.e. the first
     * element is a value for BackgroundPositionHorizontal, and the
     * second is for BackgroundPositionVertical.
     * @param propindex - the <tt>int</tt> property index.
     * @param foNode - the <tt>FONode</tt> being built
     * @param value <tt>PropertyValue</tt> returned by the parser
     * @return <tt>PropertyValue</tt> the verified value
     */
    public PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue value)
                    throws PropertyException
    {
        return refineParsing(propindex, foNode, value, NOT_NESTED);
    }

    /**
     * Do the work for the three argument refineParsing method.
     * @param propindex  the property index.
     * @param foNode  the FONode being built
     * @param value <tt>PropertyValue</tt> returned by the parser
     * @param nested  indicates whether this method is
     * called normally (false), or as part of another <i>refineParsing</i>
     * method
     * @return  the verified <tt>PropertyValue</tt>
     */
    public PropertyValue refineParsing
        (int propindex, FONode foNode, PropertyValue value, boolean nested)
                    throws PropertyException
    {
        if ( ! (value instanceof PropertyValueList)) {
            return processValue(foNode, value, nested);
        } else {
            return processList
                    (spaceSeparatedList((PropertyValueList)value));
        }
    }

    private PropertyValueList processValue
        (FONode foNode, PropertyValue value, boolean nested)
                throws PropertyException
    {
        int property = value.getProperty();
        PropertyValueList newlist = new PropertyValueList(property);
        // Can only be Inherit, NCName (i.e. ENUM token)
        // or Numeric (i.e. Length or Percentage)
        int type = value.getType();
        if ( ! nested) {
            if (type == PropertyValue.INHERIT ||
                    type == PropertyValue.FROM_PARENT ||
                        type == PropertyValue.FROM_NEAREST_SPECIFIED) {
                // Copy the value to each member of the shorthand
                newlist = refineExpansionList
                        (PropNames.BACKGROUND_POSITION, foNode,
                                ShorthandPropSets.expandAndCopySHand(value));
            }
        }

        if (type == PropertyValue.NUMERIC) {
            // Single horizontal value given
            Numeric newNum;
            try {
                newNum = (Numeric)(value.clone());
            } catch (CloneNotSupportedException cnse) {
                throw new PropertyException(cnse.getMessage());
            }
            newNum.setProperty(
                        PropNames.BACKGROUND_POSITION_HORIZONTAL);
            newlist.add(newNum);
            newlist.add(Percentage.makePercentage(
                        PropNames.BACKGROUND_POSITION_VERTICAL,
                        50.0d));
        } else if (type == PropertyValue.NCNAME) {
            String enumval = ((NCName)value).getNCName();
            int enumIndex;
            try {
                enumIndex = getEnumIndex(enumval);
            } catch (PropertyException e) {
                throw new PropertyException(
                        "Invalid ENUM value for BackgroundPosition: "
                        + enumval);
            }
            // Found an ENUM
            double horiz = 50.0, vert = 50.0;
            switch (enumIndex) {
            case LEFT:
                horiz = 0.0;
                break;
            case RIGHT:
                horiz = 100.0;
                break;
            case TOP:
                vert = 0.0;
                break;
            case BOTTOM:
                vert = 100.0;
                break;
            }
            newlist.add(Percentage.makePercentage(
                        PropNames.BACKGROUND_POSITION_HORIZONTAL,
                        horiz));
            newlist.add(Percentage.makePercentage(
                        PropNames.BACKGROUND_POSITION_VERTICAL,
                        vert));
        } else {
            throw new PropertyException
            ("Invalid " + value.getClass().getName() +
                " property value for BackgroundPosition");
        }
        return newlist;
    }

    private static PropertyValueList processList(PropertyValueList value)
                        throws PropertyException
    {
        PropertyValueList newlist
                        = new PropertyValueList(value.getProperty());
        // This is a list
        if (value.size() == 0)
            throw new PropertyException
                            ("Empty list for BackgroundPosition");
        // Only two Numerics allowed
        if (value.size() != 2)
            throw new PropertyException
                    ("Other than 2 elements in BackgroundPosition list.");
        // Analyse the list data.
        // Can be a pair of Numeric values, Length or Percentage,
        // or a pair of ENUM tokens.  One is from the set
        // [top center bottom]; the other is from the set
        // [left center right].
        Iterator positions = value.iterator();
        PropertyValue posn = (PropertyValue)(positions.next());
        int posntype = posn.getType();
        PropertyValue posn2 = (PropertyValue)(positions.next());
        int posn2type = posn2.getType();

        if (posntype == PropertyValue.NUMERIC) {
            Numeric num1;
            try {
                num1 = (Numeric)(posn.clone());
            } catch (CloneNotSupportedException cnse) {
                throw new PropertyException(cnse.getMessage());
            }
            num1.setProperty(
                        PropNames.BACKGROUND_POSITION_HORIZONTAL);
            // Now check the type of the second element
            if ( ! (posn2type == PropertyValue.NUMERIC))
                throw new PropertyException
                    ("Numeric followed by " + posn2.getClass().getName()
                    + " in BackgroundPosition list");
            Numeric num2;
            try {
                num2 = (Numeric)(posn2.clone());
            } catch (CloneNotSupportedException cnse) {
                throw new PropertyException(cnse.getMessage());
            }
            num2.setProperty(
                        PropNames.BACKGROUND_POSITION_VERTICAL);
            if ( ! (num1.isDistance() && num2.isDistance()))
                throw new PropertyException
                    ("Numerics not distances in BackgroundPosition list");

            newlist.add(num1);
            newlist.add(num2);

        } else if (posntype == PropertyValue.NCNAME) {
            // Now check the type of the second element
            if (posn2type != PropertyValue.NCNAME)
                throw new PropertyException
                    ("NCName followed by " + posn2.getClass().getName()
                    + " in BackgroundPosition list");
            int enum1, enum2;
            String enumval1 = ((NCName)posn ).getNCName();
            String enumval2 = ((NCName)posn2).getNCName();
            double percent1 = 50.0d, percent2 = 50.0d;
            try {
                enum1 = PropertyConsts.pconsts.getEnumIndex
                        (PropNames.BACKGROUND_POSITION_HORIZONTAL, enumval1);
            } catch (PropertyException e) {
                // Not a horizontal element - try vertical
                try {
                    enum1 = PropertyConsts.pconsts.getEnumIndex
                        (PropNames.BACKGROUND_POSITION_VERTICAL, enumval1);
                    enum1 += RIGHT;
                } catch (PropertyException e2) {
                    throw new PropertyException
                        ("Unrecognised value for BackgroundPosition: "
                        + enumval1);
                }
            }
            try {
                enum2 = PropertyConsts.pconsts.getEnumIndex
                        (PropNames.BACKGROUND_POSITION_VERTICAL, enumval2);
                enum2 += RIGHT;
            } catch (PropertyException e) {
                try {
                    enum2 = PropertyConsts.pconsts.getEnumIndex
                        (PropNames.BACKGROUND_POSITION_HORIZONTAL, enumval2);
                } catch (PropertyException e2) {
                    throw new PropertyException
                        ("Unrecognised value for BackgroundPosition: "
                        + enumval2);
                }
            }
            if (enum1 == CENTERV) enum1 = CENTER;
            if (enum2 == CENTERV) enum2 = CENTER;
            switch (enum1) {
            case LEFT:
                percent1 = 0.0d;
                switch (enum2) {
                case CENTER:
                    percent2 = 50.0d;
                    break;
                case TOP:
                    percent2 = 0.0d;
                    break;
                case BOTTOM:
                    percent2 = 100.0d;
                    break;
                default:
                    throw new PropertyException
                        ("Incompatible values for BackgroundPosition: "
                        + enumval1 + " " + enumval2);
                }
            case CENTER:
                switch (enum2) {
                case LEFT:
                    percent1 = 0.0d;
                    percent2 = 50.0d;
                    break;
                case CENTER:
                    percent1 = 50.0d;
                    percent2 = 50.0d;
                    break;
                case RIGHT:
                    percent1 = 100.0d;
                    percent2 = 50.0d;
                    break;
                case TOP:
                    percent1 = 50.0d;
                    percent2 = 0.0d;
                    break;
                case BOTTOM:
                    percent1 = 50.0d;
                    percent2 = 100.0d;
                    break;
                default:
                    throw new PropertyException
                        ("Incompatible values for BackgroundPosition: "
                        + enumval1 + " " + enumval2);
                }
            case RIGHT:
                percent1 = 100.0d;
                switch (enum2) {
                case CENTER:
                    percent2 = 50.0d;
                    break;
                case TOP:
                    percent2 = 0.0d;
                    break;
                case BOTTOM:
                    percent2 = 100.0d;
                    break;
                default:
                    throw new PropertyException
                        ("Incompatible values for BackgroundPosition: "
                        + enumval1 + " " + enumval2);
                }
            case TOP:
                percent2 = 0.0d;
                switch (enum2) {
                case LEFT:
                    percent1 = 0.0d;
                    break;
                case CENTER:
                    percent1 = 50.0d;
                    break;
                case RIGHT:
                    percent1 = 100.0d;
                    break;
                default:
                    throw new PropertyException
                        ("Incompatible values for BackgroundPosition: "
                        + enumval1 + " " + enumval2);
                }
            case BOTTOM:
                percent2 = 100.0d;
                switch (enum2) {
                case LEFT:
                    percent1 = 0.0d;
                    break;
                case CENTER:
                    percent1 = 50.0d;
                    break;
                case RIGHT:
                    percent1 = 100.0d;
                    break;
                default:
                    throw new PropertyException
                        ("Incompatible values for BackgroundPosition: "
                        + enumval1 + " " + enumval2);
                }
            }

            newlist.add(Percentage.makePercentage(
                        PropNames.BACKGROUND_POSITION_HORIZONTAL,
                        percent1));
            newlist.add(Percentage.makePercentage(
                        PropNames.BACKGROUND_POSITION_VERTICAL,
                        percent2));

        } else throw new PropertyException
                    ("Invalid " + posn.getClass().getName() +
                        " in BackgroundPosition list");

        return newlist;
    }

}

