/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.fo;

import java.lang.Class;
import java.lang.reflect.Method;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.Collections;

import org.apache.fop.messaging.MessageHandler;

import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FObjects;
import org.apache.fop.fo.expr.PropertyValue;
import org.apache.fop.fo.expr.PropertyValueList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.PropertyNotImplementedException;
import org.apache.fop.fo.expr.SystemFontFunction;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.datastructs.ROIntArray;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datatypes.StringType;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.CountryType;
import org.apache.fop.datatypes.LanguageType;
import org.apache.fop.datatypes.ScriptType;
import org.apache.fop.datatypes.UriType;
import org.apache.fop.datatypes.MimeType;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Ems;
import org.apache.fop.datatypes.Percentage;
import org.apache.fop.datatypes.Angle;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.MappedNumeric;
import org.apache.fop.datatypes.IntegerType;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.Bool;
import org.apache.fop.datatypes.Literal;
import org.apache.fop.datatypes.Auto;
import org.apache.fop.datatypes.None;
import org.apache.fop.datatypes.Inherit;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.FontFamilySet;
import org.apache.fop.datatypes.TextDecorations;
import org.apache.fop.datatypes.TextDecorator;
import org.apache.fop.datatypes.ShadowEffect;
import org.apache.fop.datatypes.FromParent;
import org.apache.fop.datatypes.FromNearestSpecified;
import org.apache.fop.datatypes.Slash;

/**
 * Parent class for all of the individual property classes.  It also contains
 * sets of integer constants for various types of data.
 */

public abstract class Properties {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /*
     * The list of property data types.  These are used to form a bitmap of
     * the property data types that are valid for values of each of the
     * properties.
     *
     * Maintain the following list by
     * in XEmacs:
     *  set the region to cover the list, EXCLUDING the final (-ve) value
     *  M-1 M-| followed by the command
     * perl -p -e 'BEGIN{$n=0;$n2=0};$n2=2**$n,$n++ if s/= [0-9]+/= $n2/'
     * in vi:
     *  set a mark (ma) at the end of the list but one.
     * Go to the beginning and
     *  !'aperl -p -e ... etc
     *
     * N.B. The maximum value that can be handled in this way is
     * 2^30 or 1073741824.  The -ve value is the equivalent of 2^31.
     */
    /**
     * Constant specifying a property data type or types.
     */

    public static final int
                         NOTYPE = 0
                       ,INTEGER = 1
                         ,FLOAT = 2
                        ,LENGTH = 4
                         ,ANGLE = 8
                    ,PERCENTAGE = 16
                   ,CHARACTER_T = 32
                       ,LITERAL = 64
                          ,NAME = 128
                       ,COLOR_T = 256
                     ,COUNTRY_T = 512
                    ,LANGUAGE_T = 1024
                      ,SCRIPT_T = 2048
                          ,ID_T = 4096
                         ,IDREF = 8192
             ,URI_SPECIFICATION = 16384
                          ,TIME = 32768
                     ,FREQUENCY = 65536
    // Pseudotypes
                          ,BOOL = 131072
                       ,INHERIT = 262144
                          ,ENUM = 524288
                ,MAPPED_NUMERIC = 1048576
                     ,SHORTHAND = 2097152
                       ,COMPLEX = 4194304
                          ,AUTO = 8388608
                          ,NONE = 16777216
                         ,AURAL = 33554432
    // Color plus transparent
                   ,COLOR_TRANS = 67108864
                      ,MIMETYPE = 134217728
                       ,FONTSET = 268435456
    //                   ,SPARE = 536870912
    //                   ,SPARE = 1073741824
    //                   ,SPARE = -2147483648

    // A number of questions are unresolved about the interaction of
    // complex parsing, property expression parsing & property validation.
    // At this time (2002/07/03) it looks as though the verifyParsing() method
    // will take full validation responsibility, so it will not be
    // necessary to specify any individual datatypes besides COMPLEX in the
    // property dataTypes field.  This renders some such specifications
    // redundant.  On the other hand, if such individual datatype validation
    // becomes necessary, the datatype settings for properties with COMPLEX
    // will have to be adjusted.  pbw

                        ,NUMBER = FLOAT | INTEGER
                     ,ENUM_TYPE = ENUM | MAPPED_NUMERIC
                        ,STRING = LITERAL | ENUM_TYPE
                     ,HYPH_TYPE = COUNTRY_T | LANGUAGE_T | SCRIPT_T
                       ,ID_TYPE = ID_T | IDREF
                        ,NCNAME = NAME | ID_TYPE | HYPH_TYPE | ENUM_TYPE
                   ,STRING_TYPE = STRING | NCNAME
                      ,ANY_TYPE = ~0
                                ;

    /**
     * @param datatypes <tt>int</tt> bitmap of datatype(s).
     * @return <tt>String</tt> containing a list of text names of datatypes
     * found in the bitmap.  Individual names are enclosed in angle brackets
     * and separated by a vertical bar.  Psuedo-datatypes are in upper case.
     * @exception PropertyException if no matches are found:
     */
    public static String listDataTypes(int datatypes) throws PropertyException
    {
        String typeNames = "";
        if ((datatypes & ANY_TYPE) == ANY_TYPE) return "<ALL-TYPES>";
        if ((datatypes & INTEGER) != 0) typeNames += "<integer>|";
        if ((datatypes & NUMBER) != 0) typeNames += "<number>|";
        if ((datatypes & LENGTH) != 0) typeNames += "<length>|";
        if ((datatypes & ANGLE) != 0) typeNames += "<angle>|";
        if ((datatypes & PERCENTAGE) != 0) typeNames += "<percentage>|";
        if ((datatypes & CHARACTER_T) != 0) typeNames += "<character>|";
        if ((datatypes & STRING) != 0) typeNames += "<string>|";
        if ((datatypes & NAME) != 0) typeNames += "<name>|";
        if ((datatypes & COLOR_T) != 0) typeNames += "<color>|";
        if ((datatypes & COUNTRY_T) != 0) typeNames += "<country>|";
        if ((datatypes & LANGUAGE_T) != 0) typeNames += "<language>|";
        if ((datatypes & SCRIPT_T) != 0) typeNames += "<script>|";
        if ((datatypes & ID_T) != 0) typeNames += "<id>|";
        if ((datatypes & IDREF) != 0) typeNames += "<idref>|";
        if ((datatypes & URI_SPECIFICATION) != 0) typeNames
                                                    += "<uri-specification>|";
        if ((datatypes & TIME) != 0) typeNames += "<time>|";
        if ((datatypes & FREQUENCY) != 0) typeNames += "<frequency>|";
        if ((datatypes & BOOL) != 0) typeNames += "<BOOL>|";
        if ((datatypes & INHERIT) != 0) typeNames += "<INHERIT>|";
        if ((datatypes & ENUM) != 0) typeNames += "<ENUM>|";
        if ((datatypes & MAPPED_NUMERIC) != 0) typeNames
                                                    += "<MAPPED_NUMERIC>|";
        if ((datatypes & SHORTHAND) != 0) typeNames += "<SHORTHAND>|";
        if ((datatypes & COMPLEX) != 0) typeNames += "<COMPLEX>|";
        if ((datatypes & AUTO) != 0) typeNames += "<AUTO>|";
        if ((datatypes & NONE) != 0) typeNames += "<NONE>|";
        if ((datatypes & AURAL) != 0) typeNames += "<AURAL>|";
        if ((datatypes & COLOR_TRANS) != 0) typeNames += "<COLOR_TRANS>|";
        if ((datatypes & MIMETYPE) != 0) typeNames += "<MIMETYPE>|";
        if ((datatypes & FONTSET) != 0) typeNames += "<FONTSET>|";

        if (typeNames == "") throw new PropertyException
                            ("No valid data type in " + datatypes);
        return typeNames.substring(0, typeNames.length() - 1);
    }

    /**
     * Constant specifying an initial data type or types.
     */
    public static final int
                      NOTYPE_IT = 0
                    ,INTEGER_IT = 1
                     ,NUMBER_IT = 2
                     ,LENGTH_IT = 4
                      ,ANGLE_IT = 8
                 ,PERCENTAGE_IT = 16
                  ,CHARACTER_IT = 32
                    ,LITERAL_IT = 64
                       ,NAME_IT = 128
                      ,COLOR_IT = 256
                    ,COUNTRY_IT = 512
          ,URI_SPECIFICATION_IT = 1024
                       ,BOOL_IT = 2048
                       ,ENUM_IT = 4096
                       ,AUTO_IT = 8192
                       ,NONE_IT = 16384
                      ,AURAL_IT = 32768
            ,TEXT_DECORATION_IT = 65536
  // Unused         ,FONTSET_IT = 131072

           ,USE_GET_IT_FUNCTION = //NOTYPE_IT performed in Properties
                                    INTEGER_IT
                                  | NUMBER_IT
                                  | LENGTH_IT
                                  | ANGLE_IT
                                  | PERCENTAGE_IT
                                  | CHARACTER_IT
                                  | LITERAL_IT
                                  | NAME_IT
                                  | COLOR_IT
                                  | COUNTRY_IT
                                  | URI_SPECIFICATION_IT
                                  | BOOL_IT
                                  | ENUM_IT
                                  //AUTO_IT  performed in Properties
                                  //NONE_IT  performed in Properties
                                  //AURAL_IT  performed in Properties
                                  | TEXT_DECORATION_IT
                              ;

    /**
     * Constant specifying mapping(s) of property to trait.
     */
    public static final int
                       NO_TRAIT = 0
                     ,RENDERING = 1
                    ,DISAPPEARS = 2
                 ,SHORTHAND_MAP = 4
                        ,REFINE = 8
                    ,FORMATTING = 16
                 ,SPECIFICATION = 32
                     ,NEW_TRAIT = 64
                ,FONT_SELECTION = 128
                  ,VALUE_CHANGE = 256
                     ,REFERENCE = 512
                        ,ACTION = 1024
                         ,MAGIC = 2048
                              ;

    /**
     * Constant specifying inheritance type.
     */
    public static final int
                             NO = 0
                      ,COMPUTED = 1
                     ,SPECIFIED = 2
                      ,COMPOUND = 3
                 ,SHORTHAND_INH = 4
                ,VALUE_SPECIFIC = 5
                              ;

    /**
     * Constant for nested <tt>verifyParsing</tt> methods
     */
    public static boolean IS_NESTED = true;

    /**
     * Constant for non-nested <tt>verifyParsing</tt> methods
     */
    public static boolean NOT_NESTED = false;

    /**
     * The final stage of parsing:<br>
     * 1) PropertyTokenizer<br>
     * 2) PropertyParser - returns context-free <tt>PropertyValue</tt>s
     *    recognizable by the parser<br>
     * 3) verifyParsing - verifies results from parser, translates
     *    property types like NCName into more specific value types,
     *    resolves enumeration types, etc.<br>
     *
     * <p>This method is shadowed by individual property classes which
     * require specific processing.
     * @param foTree the <tt>FOTree</tt> being built
     * @param value <tt>PropertyValue</tt> returned by the parser
     */
    public static PropertyValue verifyParsing
                                        (FOTree foTree, PropertyValue value)
            throws PropertyException
    {
        return verifyParsing(foTree, value, NOT_NESTED);
    }

    /**
     * Do the work for the two argument verifyParsing method.
     * @param foTree the <tt>FOTree</tt> being built
     * @param value <tt>PropertyValue</tt> returned by the parser
     * @param nested <tt>boolean</tt> indicating whether this method is
     * called normally (false), or as part of another <i>verifyParsing</i>
     * method.
     * @see #verifyParsing(FOTree,PropertyValue)
     */
    protected static PropertyValue verifyParsing
                        (FOTree foTree, PropertyValue value, boolean nested)
            throws PropertyException
    {
        int property = value.getProperty();
        String propName = PropNames.getPropertyName(property);
        int datatype = PropertyConsts.dataTypes.get(property);
        if (value instanceof Numeric) {
            // Can be any of
            // INTEGER, FLOAT, LENGTH, PERCENTAGE, ANGLE, FREQUENCY or TIME
            if ((datatype & (INTEGER | FLOAT | LENGTH | PERCENTAGE
                                | ANGLE | FREQUENCY | TIME)) != 0)
                return value;
        }
        if (value instanceof NCName) {
            String ncname = ((NCName)value).getNCName();
            // Can by any of
            // NAME, COUNTRY_T, LANGUAGE_T, SCRIPT_T, ID_T, IDREF, ENUM
            // MAPPED_NUMERIC or CHARACTER_T
            if ((datatype & (NAME | ID_T | IDREF | CHARACTER_T)) != 0)
                return value;
            if ((datatype & COUNTRY_T) != 0)
                return new CountryType(property, ncname);
            if ((datatype & LANGUAGE_T) != 0)
                return new LanguageType(property, ncname);
            if ((datatype & SCRIPT_T) != 0)
                return new ScriptType(property, ncname);
            if ((datatype & ENUM) != 0)
                return new EnumType(property, ncname);
            if ((datatype & MAPPED_NUMERIC) != 0)
                return (new MappedNumeric(property, ncname, foTree))
                                .getMappedNumValue();
        }
        if (value instanceof Literal) {
            // Can be LITERAL or CHARACTER_T
            if ((datatype & (LITERAL | CHARACTER_T)) != 0) return value;
            throw new PropertyException
                            ("Literal value invalid  for " + propName);
        }
        if (value instanceof Auto) {
            if ((datatype & AUTO) != 0) return value;
            throw new PropertyException("'auto' invalid  for " + propName);
        }
        if (value instanceof Bool) {
            if ((datatype & BOOL) != 0) return value;
            throw new PropertyException
                                ("Boolean value invalid for " + propName);
        }
        if (value instanceof ColorType) {
            // Can be COLOR_T or COLOR_TRANS
            if ((datatype & (COLOR_T | COLOR_TRANS)) != 0) return value;
            throw new PropertyException("'none' invalid  for " + propName);
        }
        if (value instanceof None) {
            // Some instances of 'none' are part of an enumeration, but
            // the parser will find and return 'none' as a None
            // PropertyValue.
            // In these cases, the individual property's verifyParsing
            // method must shadow this method.
            if ((datatype & NONE) != 0) return value;
            throw new PropertyException("'none' invalid  for " + propName);
        }
        if (value instanceof UriType) {
            if ((datatype & URI_SPECIFICATION) != 0) return value;
            throw new PropertyException("uri invalid for " + propName);
        }
        if (value instanceof MimeType) {
            if ((datatype & MIMETYPE) != 0) return value;
            throw new PropertyException
                        ("mimetype invalid for " + propName);
        }
        if ( ! nested) {
            if (value instanceof Inherit) {
                if ((datatype & INHERIT) != 0) return value;
                throw new PropertyException
                                    ("'inherit' invalid for " + propName);
            }
        }
        throw new PropertyException
            ("Inappropriate datatype passed to Properties.verifyParsing: "
                + value.getClass().getName());
    }

    /**
     * Check the PropertyValueList passed as an argument, to determine
     * whether it contains a space-separated list from the parser.
     * A space-separated list will be represented by a single
     * PropertyValueList as an element of the argument PropertyValueList.
     * @param list <tt>PropertyValueList</tt> the containing list.
     * @return <tt>PropertyValueList</tt> the contained space-separated list.
     * @exception <tt>PropertyException</tt>
     */
    protected static PropertyValueList spaceSeparatedList
                                                    (PropertyValueList list)
            throws PropertyException
    {
        if (list.size() != 1)
                throw new PropertyException
                        (list.getClass().getName() + " list is not a "
                                + "single list of space-separated values");
        PropertyValue val2 = (PropertyValue)(list.getFirst());
        if ( ! (val2 instanceof PropertyValueList))
                throw new PropertyException
                        (list.getClass().getName() + " list is not a "
                                + "single list of space-separated values");
        return (PropertyValueList)val2;
    }

    /**
     * Return the EnumType derived from the argument.
     * The argument must be an NCName whose string value is a
     * valid Enum for the property associated with the NCName.
     * @param value <tt>PropertyValue</tt>
     * @param property <tt>int</tt> the target property index
     * @param type <tt>String</tt> type of expected enum - for
     * exception messages only
     * @return <tt>EnumValue</tt> equivalent of the argument
     * @exception <tt>PropertyException</tt>
     */
    protected static EnumType getEnum(PropertyValue value,
                                            int property, String type)
            throws PropertyException
    {
        if ( ! (value instanceof NCName))
            throw new PropertyException
                (value.getClass().getName()
                                + " instead of " + type + " for "
                                + PropNames.getPropertyName(property));

        NCName ncname = (NCName)value;
        try {
            return new EnumType(property, ncname.getNCName());
        } catch (PropertyException e) {
            throw new PropertyException
                        (ncname.getNCName()
                                + " instead of " + type + " for "
                                + PropNames.getPropertyName(property));
        }
    }

    /**
     * Fallback getInitialValue function.  This function only handles
     * those initial value types NOT in the set USE_GET_IT_FUNCTION.  It
     * should be shadowed by all properties whose initial values come from
     * that set.
     * @param property <tt>int</tt> property index
     * @return <tt>PropertyValue</tt>
     * @exception <tt>PropertyException</tt>
     * @exception <tt>PropertyNotImplementedException</tt>
     */
    public static PropertyValue getInitialValue(int property)
            throws PropertyException
    {
        Method method = null;
        int initialValueType = PropertyConsts.getInitialValueType(property);
        if ((initialValueType & Properties.USE_GET_IT_FUNCTION) != 0)
             throw new PropertyException
                 ("Properties.getInitialValue() called for property with "
                 + "initial value type in USE_GET_IT_FUNCTION : "
                 + PropNames.getPropertyName(property));
        switch (initialValueType) {
        case NOTYPE_IT:
            return null;
        case AUTO_IT:
            return new Auto(property);
        case NONE_IT:
            return new None(property);
        case AURAL_IT:
            throw new PropertyNotImplementedException
                ("Aural properties not implemented: "
                + PropNames.getPropertyName(property));
        default:
            throw new PropertyException
                ("Unexpected initial value type " + initialValueType
                + " for " + PropNames.getPropertyName(property));
        }
    }

    /**
     * 'value' is a PropertyValueList or an individual PropertyValue.
     * If 'value' is a PropertyValueList, it must contain a single
     * PropertyValueList, which in turn contains the individual elements.
     *
     * 'value' can contain a parsed Inherit value,
     *  parsed FromParent value, parsed FromNearestSpecified value,
     *  or, in any order;
     * border-width
     *     a parsed NCName value containing a standard border width
     * border-style
     *     a parsed NCName value containing a standard border style
     * border-color
     *     a parsed ColorType value, or an NCName containing one of
     *     the standard colors
     *
     * <p>The value(s) provided, if valid, are converted into a list
     * containing the expansion of the shorthand.  The elements may
     * be in any order.  A minimum of one value will be present.
     *
     *   a border-EDGE-color ColorType or inheritance value
     *   a border-EDGE-style EnumType or inheritance value
     *   a border-EDGE-width MappedNumeric or inheritance value
     *
     *  N.B. this is the order of elements defined in
     *       PropertySets.borderRightExpansion
     */
    protected static PropertyValue borderEdge(FOTree foTree,
            PropertyValue value, int styleProp, int colorProp, int widthProp)
                throws PropertyException
    {
        return borderEdge
                (foTree, value, styleProp, colorProp, widthProp, NOT_NESTED);
    }

    protected static PropertyValue borderEdge
            (FOTree foTree, PropertyValue value, int styleProp,
                int colorProp, int widthProp, boolean nested)
                throws PropertyException
    {
        if ( ! (value instanceof PropertyValueList)) {
            return processEdgeValue
                    (foTree, value, styleProp, colorProp, widthProp, nested);
        } else {
            return processEdgeList
                (foTree, spaceSeparatedList((PropertyValueList)value),
                                            styleProp, colorProp, widthProp);
        }
    }

    private static PropertyValueList processEdgeValue
            (FOTree foTree, PropertyValue value, int styleProp,
                int colorProp, int widthProp, boolean nested)
            throws PropertyException
    {
        if ( ! nested) {
            if (value instanceof Inherit |
                    value instanceof FromParent |
                        value instanceof FromNearestSpecified)
                // Construct a list of Inherit values
                return PropertySets.expandAndCopySHand(value);
        }
        // Make a list and pass to processList
        PropertyValueList tmpList
                = new PropertyValueList(value.getProperty());
        tmpList.add(value);
        return processEdgeList
                    (foTree, tmpList, styleProp, colorProp, widthProp);
    }

    private static PropertyValueList processEdgeList(FOTree foTree,
        PropertyValueList value, int styleProp, int colorProp, int widthProp)
                    throws PropertyException
    {
        int property = value.getProperty();
        String propName = PropNames.getPropertyName(property);
        PropertyValue   color= null,
                        style = null,
                        width = null;

        PropertyValueList newlist = new PropertyValueList(property);
        // This is a list
        if (value.size() == 0)
            throw new PropertyException
                            ("Empty list for " + propName);
        Iterator elements = ((PropertyValueList)value).iterator();

        scanning_elements: while (elements.hasNext()) {
            PropertyValue pval = (PropertyValue)(elements.next());
            if (pval instanceof ColorType) {
                if (color != null) MessageHandler.log(propName +
                            ": duplicate color overrides previous color");
                color = pval;
                continue scanning_elements;
            }
            if (pval instanceof NCName) {
                // Could be standard color, style Enum or width MappedNumeric
                PropertyValue colorFound = null;
                PropertyValue styleFound = null;
                PropertyValue widthFound = null;

                String ncname = ((NCName)pval).getNCName();
                try {
                    styleFound = new EnumType(styleProp, ncname);
                } catch (PropertyException e) {}
                if (styleFound != null) {
                    if (style != null) MessageHandler.log(propName +
                            ": duplicate style overrides previous style");
                    style = styleFound;
                    continue scanning_elements;
                }

                try {
                    widthFound =
                        (new MappedNumeric(widthProp, ncname, foTree))
                            .getMappedNumValue();
                } catch (PropertyException e) {}
                if (widthFound != null) {
                    if (width != null) MessageHandler.log(propName +
                            ": duplicate width overrides previous width");
                    width = widthFound;
                    continue scanning_elements;
                }

                try {
                    colorFound = new ColorType(colorProp, ncname);
                } catch (PropertyException e) {};
                if (colorFound != null) {
                    if (color != null) MessageHandler.log(propName +
                            ": duplicate color overrides previous color");
                    color = colorFound;
                    continue scanning_elements;
                }

                throw new PropertyException
                    ("Unknown NCName value for " + propName + ": " + ncname);
            }
            throw new PropertyException
                ("Invalid " + pval.getClass().getName() +
                    " property value for " + propName);
        }

        // Now construct the list of PropertyValues with their
        // associated property indices, as expanded from the
        // border-right shorthand.
        if (style != null) newlist.add(style);
        if (color != null) newlist.add(color);
        if (width != null) newlist.add(width);
        return newlist;
    }

    /**
     * @param value <tt>PropertyValue</tt> the value being tested
     * @param property <tt>int</tt> property index of returned value
     * @return <tt>PropertyValue</t> the same value, with its property set
     *  to the <i>property</i> argument, if it is an Auto or a
     * <tt>Numeric</tt> distance
     * @exception <tt>PropertyException</tt> if the conditions are not met
     */
    protected static PropertyValue autoOrDistance
                                        (PropertyValue value, int property)
        throws PropertyException
    {
        if (value instanceof Auto ||
                value instanceof Numeric && ((Numeric)value).isDistance()) {
            value.setProperty(property);
            return value;
        }
        else throw new PropertyException
            ("Value not 'Auto' or a distance for "
                + PropNames.getPropertyName(value.getProperty()));
    }

    /**
     * @param value <tt>PropertyValue</tt> the value being tested
     * @return <tt>PropertyValue</t> the same value if it is an Auto or a
     * <tt>Numeric</tt> distance
     * @exception <tt>PropertyException</tt> if the conditions are not met
     */
    protected static PropertyValue autoOrDistance(PropertyValue value)
        throws PropertyException
    {
        return autoOrDistance(value, value.getProperty());
    }

    /**
     * Pseudo-property class for common border style values occurring in a
     * number of classes.
     */
    public static class BorderCommonStyle extends Properties {
        public static final int HIDDEN = 1;
        public static final int DOTTED = 2;
        public static final int DASHED = 3;
        public static final int SOLID = 4;
        public static final int DOUBLE = 5;
        public static final int GROOVE = 6;
        public static final int RIDGE = 7;
        public static final int INSET = 8;
        public static final int OUTSET = 9;

        private static final String[] rwEnums = {
            null
            ,"hidden"
            ,"dotted"
            ,"dashed"
            ,"solid"
            ,"double"
            ,"groove"
            ,"ridge"
            ,"inset"
            ,"outset"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }

    /**
     * Pseudo-property class for common border width values occurring in a
     * number of classes.
     */
    public static class BorderCommonWidth extends Properties {
        public static final int THIN = 1;
        public static final int MEDIUM = 2;
        public static final int THICK = 3;

        private static final String[] rwEnums = {
            null
            ,"thin"
            ,"medium"
            ,"thick"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;

        // N.B. If these values change, all initial values expressed in these
        // terms must be manually changed.

        /**
         * @param <tt>int</tt> property index
         * @return <tt>Numeric[]</tt> containing the values corresponding
         * to the MappedNumeric enumeration constants for border width
         */
        public static Numeric[] borderWidthNumArray(int property)
            throws PropertyException
        {
            Numeric[] numarray = new Numeric[4];
            numarray[0] = null;
            numarray[1] =
                Length.makeLength(property, 0.5d, Length.PT); // thin
            numarray[2] =
                Length.makeLength(property, 1d, Length.PT); // medium
            numarray[3] =
                Length.makeLength(property, 2d, Length.PT); // thick // thick
            return numarray;
        }

    }

    /**
     * Pseudo-property class for common color values occurring in a
     * number of classes.
     */
    public static class ColorCommon extends Properties {
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

        private static final String[] rwEnums = {
            null
            ,"aqua"
            ,"black"
            ,"blue"
            ,"fuschia"
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
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwColorEnums;
        static {
            rwColorEnums = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length - 1; i++ ) {
                rwColorEnums.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
        }
        /**
         * <tt>colorEnums</tt> exclude "transparent"
         */
        public static final Map colorEnums =
                Collections.unmodifiableMap((Map)rwColorEnums);


        private static final HashMap rwColorTransEnums;
        static {
            int i = rwEnums.length - 1;
            rwColorTransEnums = new HashMap(rwColorEnums);
            rwColorTransEnums.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
        }
        /**
         * <tt>colorTransEnums</tt> include <tt>colorEnums</tt> and
         * "transparent".
         */
        public static final Map colorTransEnums =
            Collections.unmodifiableMap((Map)rwColorTransEnums);
    }


    public static class NoProperty extends Properties {
        // dataTypes was set to ANY_TYPE.  This meant that any property
        // type would be valid with NoProperty.  It caused problems with
        // initialization looking for complex().  I cannot now see the
        // rationale for such a setting.  Resetting to NOTYPE.
        // pbw 23/01/02
        public static final int dataTypes = NOTYPE;
        public static final int traitMapping = NO_TRAIT;
        public static final int initialValueType = NOTYPE_IT;

        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"-----NoEnum-----"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }


    public static class AbsolutePosition extends Properties {
        public static final int dataTypes = AUTO | ENUM | INHERIT;
        public static final int traitMapping = NEW_TRAIT;
        public static final int initialValueType = AUTO_IT;
        public static final int ABSOLUTE = 1;
        public static final int FIXED = 2;

        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"absolute"
            ,"fixed"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }


    public static class ActiveState extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = ACTION;
        public static final int initialValueType = NOTYPE_IT;
        public static final int LINK = 1;
        public static final int VISITED = 2;
        public static final int ACTIVE = 3;
        public static final int HOVER = 4;
        public static final int FOCUS = 5;

        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"link"
            ,"visited"
            ,"active"
            ,"hover"
            ,"focus"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }


    public static class AlignmentAdjust extends Properties {
        public static final int dataTypes =
                                AUTO | ENUM | PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int BASELINE = 1;
        public static final int BEFORE_EDGE = 2;
        public static final int TEXT_BEFORE_EDGE = 3;
        public static final int MIDDLE = 4;
        public static final int CENTRAL = 5;
        public static final int AFTER_EDGE = 6;
        public static final int TEXT_AFTER_EDGE = 7;
        public static final int IDEOGRAPHIC = 8;
        public static final int ALPHABETIC = 9;
        public static final int HANGING = 10;
        public static final int MATHEMATICAL = 11;

        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"baseline"
            ,"before-edge"
            ,"text-before-edge"
            ,"middle"
            ,"central"
            ,"after-edge"
            ,"text-after-edge"
            ,"ideographic"
            ,"alphabetic"
            ,"hanging"
            ,"mathematical"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }


    public static class AlignmentBaseline extends Properties {
        public static final int dataTypes = AUTO | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int BASELINE = 1;
        public static final int BEFORE_EDGE = 2;
        public static final int TEXT_BEFORE_EDGE = 3;
        public static final int MIDDLE = 4;
        public static final int CENTRAL = 5;
        public static final int AFTER_EDGE = 6;
        public static final int TEXT_AFTER_EDGE = 7;
        public static final int IDEOGRAPHIC = 8;
        public static final int ALPHABETIC = 9;
        public static final int HANGING = 10;
        public static final int MATHEMATICAL = 11;

        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"baseline"
            ,"before-edge"
            ,"text-before-edge"
            ,"middle"
            ,"central"
            ,"after-edge"
            ,"text-after-edge"
            ,"ideographic"
            ,"alphabetic"
            ,"hanging"
            ,"mathematical"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }

    public static class AutoRestore extends Properties {
        public static final int dataTypes = BOOL;
        public static final int traitMapping = ACTION;
        public static final int initialValueType = BOOL_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Bool(PropNames.AUTO_RESTORE, true);
        }
        public static final int inherited = COMPUTED;
    }

    public static class Azimuth extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = COMPUTED;
    }

    public static class Background extends Properties {
        public static final int dataTypes = SHORTHAND | INHERIT;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;

        /**
         * 'value' is a PropertyValueList or an individual PropertyValue.
         * If 'value' is a PropertyValueList, it must contain a single
         * PropertyValueList, which in turn contains the individual elements.
         *
         * 'value' can contain a parsed Inherit value or, in any order;
         * background-color
         *     a parsed ColorType value, or an NCName containing one of
         *     the standard colors
         * background-image
         *     a parsed UriType value, or a parsed None value
         * background-repeat
         *     a parsed NCName containing a repeat enumeration token
         * background-attachment
         *     a parsed NCName containing 'scroll' or 'fixed'
         * background-position
         *     one or two parsed Length or Percentage values, or
         *     one or two parsed NCNames containing enumeration tokens
         *
         * <p>The value(s) provided, if valid, are converted into a list
         * containing the expansion of the shorthand.  Any subset of the
         * elements may be present, from minimum of one.  The elements
         * which are present will always occur in the following order:
         *
         *   a BackgroundColor ColorType or Inherit value
         *   a BackgroundImage UriType, None or Inherit value
         *   a BackgroundRepeat EnumType or Inherit value
         *   a BackgroundAttachment EnumType or Inherit value
         *   a BackgroundPositionHorizontal Numeric or Inherit value
         *   a BackgroundPositionVertical Numeric or Inherit value
         */
        public static PropertyValue verifyParsing
                (FOTree foTree, PropertyValue value)
                        throws PropertyException
        {
            if ( ! (value instanceof PropertyValueList)) {
                return processValue(foTree, value);
            } else {
                return processList
                    (foTree, spaceSeparatedList((PropertyValueList)value));
            }
        }

        private static PropertyValueList processValue
            (FOTree foTree, PropertyValue value) throws PropertyException
        {
            // Can be Inherit, ColorType, UriType, None, Numeric, or an
            // NCName (i.e. enum token)
            if (value instanceof Inherit |
                    value instanceof FromParent |
                        value instanceof FromNearestSpecified)
            {
                // Construct a list of Inherit values
                return PropertySets.expandAndCopySHand(value);
            } else  {
                // Make a list and pass to processList
                PropertyValueList tmpList
                        = new PropertyValueList(value.getProperty());
                tmpList.add(value);
                return processList(foTree, tmpList);
            }
        }

        private static PropertyValueList processList
                (FOTree foTree, PropertyValueList value)
                        throws PropertyException
        {
            int property = value.getProperty();
            PropertyValue color= null,
                            image = null,
                            repeat = null,
                            attachment = null,
                            position = null;

            PropertyValueList newlist = new PropertyValueList(property);
            // This is a list
            if (value.size() == 0)
                throw new PropertyException
                                ("Empty list for Background");
            ListIterator elements = ((PropertyValueList)value).listIterator();

            scanning_elements: while (elements.hasNext()) {
                PropertyValue pval = (PropertyValue)(elements.next());
                if (pval instanceof ColorType) {
                    if (color != null) MessageHandler.log("background: " +
                                "duplicate color overrides previous color");
                    color = pval;
                    continue scanning_elements;
                }

                if (pval instanceof UriType) {
                    if (image != null) MessageHandler.log("background: " +
                        "duplicate image uri overrides previous image spec");
                    image = pval;
                    continue scanning_elements;
                }

                if (pval instanceof None) {
                    if (image != null) MessageHandler.log("background: " +
                        "duplicate image spec overrides previous image spec");
                    image = pval;
                    continue scanning_elements;
                }

                if (pval instanceof Numeric) {
                    // Must be one of the position values
                    // send it to BackgroundPosition.complex for processing
                    // If it is followed by another Numeric, form a list from
                    // the pair, else form a list from this element only
                    PropertyValueList posnList = new PropertyValueList
                                            (PropNames.BACKGROUND_POSITION);
                    PropertyValue tmpval = null;
                    // Is it followed by another Numeric?
                    if (elements.hasNext()) {
                        if ((tmpval = (PropertyValue)(elements.next()))
                                    instanceof Numeric) {
                            posnList.add(pval);
                            posnList.add(tmpval);
                        } else {
                            // Not a following Numeric, so restore the list
                            // cursor
                            Object tmpo = elements.previous();
                            tmpval = null;
                        }
                    }
                    // Now send one or two Numerics to BackgroundPosition
                    if (position != null)
                            MessageHandler.log("background: duplicate" +
                            "position overrides previous position");
                    if (tmpval == null)
                        position = BackgroundPosition.verifyParsing
                                                (foTree, pval, IS_NESTED);
                    else { // 2 elements
                        // make a space-separated list
                        PropertyValueList ssList = new PropertyValueList
                                            (PropNames.BACKGROUND_POSITION);
                        ssList.add(posnList);
                        position = BackgroundPosition.verifyParsing
                                                (foTree, ssList, IS_NESTED);
                    }
                    continue scanning_elements;
                }

                if (pval instanceof NCName) {
                    // NCName can be:
                    //  a standard color name
                    //  a background attachment mode
                    //  one or two position indicators
                    String ncname = ((NCName)pval).getNCName();
                    ColorType colorval = null;
                    try {
                        colorval = new ColorType
                                        (PropNames.BACKGROUND_COLOR, ncname);
                    } catch (PropertyException e) {};
                    if (colorval != null) {
                        if (color != null) MessageHandler.log("background: " +
                                "duplicate color overrides previous color");
                        color = colorval;
                        continue scanning_elements;
                    }

                    // Is it an attachment mode?
                    EnumType enum = null;
                    try {
                        enum = new EnumType
                                (PropNames.BACKGROUND_ATTACHMENT, ncname);
                    } catch (PropertyException e) {};
                    if (enum != null) {
                        if (attachment != null)
                                MessageHandler.log("background: duplicate" +
                                "attachment overrides previous attachment");
                        attachment = enum;
                        continue scanning_elements;
                    }

                    // Must be a position indicator
                    // send it to BackgroundPosition.complex for processing
                    // If it is followed by another NCName, form a list from
                    // the pair, else send this element only

                    // This is made messy by the syntax of the Background
                    // shorthand.  A following NCName need not be a second
                    // position indicator.  So we have to test this element
                    // and the following element individually.
                    PropertyValueList posnList = new PropertyValueList
                                            (PropNames.BACKGROUND_POSITION);
                    PropertyValue tmpval = null;
                    // Is the current NCName a position token?
                    boolean pos1ok = false, pos2ok = false;
                    try {
                        PropertyConsts.enumValueToIndex
                                        (ncname, BackgroundPosition.enums);
                        pos1ok = true;
                        if (elements.hasNext()) {
                            tmpval = (PropertyValue)(elements.next());
                            if (tmpval instanceof NCName) {
                                String ncname2 = ((NCName)tmpval).getString();
                                PropertyConsts.enumValueToIndex
                                        (ncname2, BackgroundPosition.enums);
                                pos2ok = true;
                            } else {
                                // Restore the listIterator cursor
                                Object tmpo = elements.previous();
                            }
                        }
                    } catch (PropertyException e) {};

                    if (pos1ok) {
                        if (position != null)
                                MessageHandler.log("background: duplicate" +
                                "position overrides previous position");
                        // Is it followed by another position NCName?
                        if (pos2ok) {
                            posnList.add(pval);
                            posnList.add(tmpval);
                            // Now send two NCNames to BackgroundPosition
                            // as a space-separated list
                            PropertyValueList ssList = new PropertyValueList
                                            (PropNames.BACKGROUND_POSITION);
                            ssList.add(posnList);
                            position = BackgroundPosition.verifyParsing
                                                (foTree, ssList, IS_NESTED);
                        } else { // one only
                        // Now send one NCName to BackgroundPosition
                            position = BackgroundPosition.verifyParsing
                                                (foTree, pval, IS_NESTED);
                        }
                        continue scanning_elements;
                    }
                    throw new PropertyException
                        ("Unknown NCName value for background: " + ncname);
                }

                throw new PropertyException
                    ("Invalid " + pval.getClass().getName() +
                        " property value for background");
            }

            // Now construct the list of PropertyValues with their
            // associated property indices, as expanded from the
            // Background shorthand.  Note that the position value is a list
            // containing the expansion of the BackgroundPosition shorthand.

            if (color != null) {
                color.setProperty(PropNames.BACKGROUND_COLOR);
                newlist.add(color);
            }
            if (image != null) {
                image.setProperty(PropNames.BACKGROUND_IMAGE);
                newlist.add(image);
            }
            if (repeat != null) {
                repeat.setProperty(PropNames.BACKGROUND_REPEAT);
                newlist.add(repeat);
            }
            if (attachment != null) {
                attachment.setProperty(PropNames.BACKGROUND_ATTACHMENT);
                newlist.add(attachment);
            }
            if (position != null) {
                // position must have two elements
                Iterator positions = ((PropertyValueList)position).iterator();
                newlist.add(positions.next());
                newlist.add(positions.next());
            }
            return newlist;
        }

    }

    public static class BackgroundAttachment extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = ENUM_IT;
        public static final int SCROLL = 1;
        public static final int FIXED = 2;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.BACKGROUND_ATTACHMENT, SCROLL);
        }

        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"scroll"
            ,"fixed"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class BackgroundColor extends Properties {
        public static final int dataTypes = COLOR_TRANS | INHERIT;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = COLOR_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new ColorType (PropNames.BACKGROUND_COLOR, "transparent");
        }

        public static final int inherited = NO;

        public static final ROStringArray enums = ColorCommon.enums;
        public static final Map enumValues = ColorCommon.colorTransEnums;
    }

    public static class BackgroundImage extends Properties {
        public static final int dataTypes = URI_SPECIFICATION | NONE | INHERIT;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = NONE_IT;

        public static final int inherited = NO;
    }

    public static class BackgroundPosition extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;

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
        protected static final ROStringArray enumValues = enums;

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
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @return <tt>PropertyValue</tt> the verified value
         */
        public static PropertyValue verifyParsing
                                        (FOTree foTree, PropertyValue value)
                        throws PropertyException
        {
            return verifyParsing(foTree, value, NOT_NESTED);
        }

        /**
         * Do the work for the two argument verifyParsing method.
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @param nested <tt>boolean</tt> indicating whether this method is
         * called normally (false), or as part of another <i>verifyParsing</i>
         * method.
         * @return <tt>PropertyValue</tt> the verified value
         * @see #verifyParsing(FOTree,PropertyValue)
         */
        public static PropertyValue verifyParsing
                        (FOTree foTree, PropertyValue value, boolean nested)
                        throws PropertyException
        {
            if ( ! (value instanceof PropertyValueList)) {
                return processValue(value, nested);
            } else {
                return processList
                            (spaceSeparatedList((PropertyValueList)value));
            }
        }

        private static PropertyValueList processValue
                                        (PropertyValue value, boolean nested)
                        throws PropertyException
        {
            PropertyValueList newlist
                            = new PropertyValueList(value.getProperty());
            // Can only be Inherit, NCName (i.e. enum token)
            // or Numeric (i.e. Length or Percentage)
            if ( ! nested) {
                if (value instanceof Inherit |
                        value instanceof FromParent |
                            value instanceof FromNearestSpecified) {
                    // Construct a list of Inherit values
                    newlist = PropertySets.expandAndCopySHand(value);
                    return newlist;
                }
            }

            if (value instanceof Numeric) {
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
            } else if (value instanceof NCName) {
                String enumval = ((NCName)value).getNCName();
                int enumIndex;
                try {
                    enumIndex = PropertyConsts.enumValueToIndex(
                                    enumval, enums);
                } catch (PropertyException e) {
                    throw new PropertyException(
                            "Invalid enum value for BackgroundPosition: "
                            + enumval);
                }
                // Found an enum
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
            // or a pair of enum tokens.  One is from the set
            // [top center bottom]; the other is from the set
            // [left center right].
            Iterator positions = value.iterator();
            PropertyValue posn = (PropertyValue)(positions.next());
            PropertyValue posn2 = (PropertyValue)(positions.next());

            if (posn instanceof Numeric) {
                Numeric num1;
                try {
                    num1 = (Numeric)(posn.clone());
                } catch (CloneNotSupportedException cnse) {
                    throw new PropertyException(cnse.getMessage());
                }
                num1.setProperty(
                            PropNames.BACKGROUND_POSITION_HORIZONTAL);
                // Now check the type of the second element
                if ( ! (posn2 instanceof Numeric))
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

            } else if (posn instanceof NCName) {
                // Now check the type of the second element
                if ( ! (posn2 instanceof NCName))
                    throw new PropertyException
                        ("NCName followed by " + posn2.getClass().getName()
                        + " in BackgroundPosition list");
                int enum1, enum2;
                String enumval1 = ((NCName)posn ).getNCName();
                String enumval2 = ((NCName)posn2).getNCName();
                double percent1 = 50.0d, percent2 = 50.0d;
                try {
                    enum1 = PropertyConsts.enumValueToIndex(
                                    enumval1,
                                    BackgroundPositionHorizontal.enumValues);
                } catch (PropertyException e) {
                    // Not a horizontal element - try vertical
                    try {
                        enum1 = PropertyConsts.enumValueToIndex(
                                    enumval1,
                                    BackgroundPositionVertical.enumValues);
                        enum1 += RIGHT;
                    } catch (PropertyException e2) {
                        throw new PropertyException
                            ("Unrecognised value for BackgroundPosition: "
                            + enumval1);
                    }
                }
                try {
                    enum2 = PropertyConsts.enumValueToIndex(
                                    enumval2,
                                    BackgroundPositionVertical.enumValues);
                    enum2 += RIGHT;
                } catch (PropertyException e) {
                    try {
                        enum2 = PropertyConsts.enumValueToIndex(
                                    enumval2,
                                    BackgroundPositionHorizontal.enumValues);
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

    public static class BackgroundPositionHorizontal extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | ENUM | INHERIT;
        public static final int traitMapping = VALUE_CHANGE;
        public static final int initialValueType = PERCENTAGE_IT;
        public static final int LEFT = 1;
        public static final int CENTER = 2;
        public static final int RIGHT = 3;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Percentage.makePercentage
                            (PropNames.BACKGROUND_POSITION_HORIZONTAL, 0.0d);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"left"
            ,"center"
            ,"right"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }


    public static class BackgroundPositionVertical extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | ENUM | INHERIT;
        public static final int traitMapping = VALUE_CHANGE;
        public static final int initialValueType = PERCENTAGE_IT;
        public static final int TOP = 1;
        public static final int CENTER = 2;
        public static final int BOTTOM = 3;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Percentage.makePercentage (PropNames.BACKGROUND_POSITION_VERTICAL, 0.0d);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"top"
            ,"center"
            ,"bottom"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class BackgroundRepeat extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = ENUM_IT;
        public static final int REPEAT = 1;
        public static final int REPEAT_X = 2;
        public static final int REPEAT_Y = 3;
        public static final int NO_REPEAT = 4;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.BACKGROUND_REPEAT, REPEAT);
        }

        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"repeat"
            ,"repeat-x"
            ,"repeat-y"
            ,"no-repeat"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }


    public static class BaselineShift extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int BASELINE = 1;
        public static final int SUB = 2;
        public static final int SUPER = 3;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.BASELINE_SHIFT, BASELINE);
        }

        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"baseline"
            ,"sub"
            ,"super"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class BlankOrNotBlank extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = ENUM_IT;
        public static final int BLANK = 1;
        public static final int NOT_BLANK = 2;
        public static final int ANY = 3;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.BLANK_OR_NOT_BLANK, ANY);
        }

        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"blank"
            ,"not-blank"
            ,"any"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class BlockProgressionDimension extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | AUTO | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class BlockProgressionDimensionMinimum extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | AUTO;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = COMPOUND;
    }

    public static class BlockProgressionDimensionOptimum extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | AUTO;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = COMPOUND;
    }

    public static class BlockProgressionDimensionMaximum extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | AUTO;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = COMPOUND;
    }

    public static class Border extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;

        public static PropertyValue verifyParsing
                                        (FOTree foTree, PropertyValue value)
            throws PropertyException
        {
            if (value instanceof Inherit |
                            value instanceof FromParent |
                                        value instanceof FromNearestSpecified)
                // Construct a list of Inherit values
                return PropertySets.expandAndCopySHand(value);

            PropertyValueList ssList = null;
            // Must be a space-separated list or a single value from the
            // set of choices
            if (! (value instanceof PropertyValueList)) {
                // If it's a single value, form a list from that value
                ssList = new PropertyValueList(PropNames.BORDER);
                ssList.add(value);
            } else {
                // Must be a space-separated list
                try {
                    ssList = spaceSeparatedList((PropertyValueList)value);
                } catch (PropertyException e) {
                    throw new PropertyException
                        ("Space-separated list required for 'border'");
                }
            }
            // Look for appropriate values in ssList
            PropertyValue width = null;
            PropertyValue style = null;
            PropertyValue color = null;
            Iterator values = ssList.iterator();
            while (values.hasNext()) {
                PropertyValue val = (PropertyValue)(values.next());
                PropertyValue pv = null;
                try {
                    pv = BorderWidth.verifyParsing(foTree, val, IS_NESTED);
                    if (width != null)
                        MessageHandler.log("border: duplicate" +
                        "width overrides previous width");
                    width = pv;
                    continue;
                } catch (PropertyException e) {}
                try {
                    pv = BorderStyle.verifyParsing(foTree, val, IS_NESTED);
                    if (style != null)
                        MessageHandler.log("border: duplicate" +
                        "style overrides previous style");
                    style = pv;
                    continue;
                } catch (PropertyException e) {}
                try {
                    pv = BorderColor.verifyParsing(foTree, val, IS_NESTED);
                    if (color != null)
                        MessageHandler.log("border: duplicate" +
                        "color overrides previous color");
                    color = pv;
                    continue;
                } catch (PropertyException e) {}

                throw new PropertyException
                    ("Unrecognized value; looking for style, "
                    + "width or color in border: "
                    + val.getClass().getName());
            }
            // Construct the shorthand expansion list
            PropertyValueList borderexp =
                PropertySets.initialValueExpansion(foTree, PropNames.BORDER);
            if (style != null)
                borderexp = PropertySets.overrideSHandElements(borderexp,
                                                    (PropertyValueList)style);
            if (color != null)
                borderexp = PropertySets.overrideSHandElements(borderexp,
                                                    (PropertyValueList)color);
            if (width != null)
                borderexp = PropertySets.overrideSHandElements(borderexp,
                                                    (PropertyValueList)width);
            return borderexp;
        }
    }

    public static class Conditionality extends Properties {
        public static final int DISCARD = 1;
        public static final int RETAIN = 2;

        private static final String[] rwEnums = {
            null
            ,"discard"
            ,"retain"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class BorderAfterColor extends Properties {
        public static final int dataTypes = COLOR_T | INHERIT;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = COLOR_IT;
        public static final int inherited = NO;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new ColorType (PropNames.BACKGROUND_COLOR, ColorCommon.BLACK);
        }

        public static final ROStringArray enums = ColorCommon.enums;
        public static final Map enumValues = ColorCommon.colorTransEnums;
    }

    public static class BorderAfterPrecedence extends Properties {
        public static final int dataTypes = INTEGER | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;

        public static final ROStringArray enums = PrecedenceCommon.enums;
        public static final ROStringArray enumValues
                                            = PrecedenceCommon.enumValues;
    }

    public static class BorderAfterStyle extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = NOTYPE_IT;
        public static final String initialValue = "none";

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonStyle.enums;
        public static final Map enumValues = BorderCommonStyle.enumValues;
    }

    // Initial value for BorderAfterWidth is tne mapped enumerated value
    // "medium".  This maps to 1pt.  There is no way at present to
    // automatically update the following initial Length PropertyValue
    // if the mapping changes.
    public static class BorderAfterWidth extends Properties {
        public static final int dataTypes = MAPPED_NUMERIC | LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength
                                (PropNames.BORDER_AFTER_WIDTH, 1d, Length.PT);
        }

        public static Numeric[] getMappedNumArray()
            throws PropertyException
        {
            return Properties.BorderCommonWidth.borderWidthNumArray
                                            (PropNames.BORDER_AFTER_WIDTH);
        }

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonWidth.enums;
        public static final ROStringArray enumValues
                                            = BorderCommonWidth.enumValues;
    }

    public static class BorderAfterWidthLength extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = COMPOUND;
    }

    public static class BorderAfterWidthConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = ENUM_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return  new EnumType(PropNames.BORDER_AFTER_WIDTH_CONDITIONALITY, Conditionality.DISCARD);
        }
        public static final int inherited = COMPOUND;

        public static final ROStringArray enums = Conditionality.enums;
        public static final ROStringArray enumValues
                                            = Conditionality.enumValues;
    }

    public static class BorderBeforeColor extends Properties {
        public static final int dataTypes = COLOR_T | INHERIT;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = COLOR_IT;
        public static final int inherited = NO;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new ColorType (PropNames.BACKGROUND_COLOR, ColorCommon.BLACK);
        }

        public static final ROStringArray enums = ColorCommon.enums;
        public static final Map enumValues = ColorCommon.colorTransEnums;
    }

    public static class BorderBeforePrecedence extends Properties {
        public static final int dataTypes = INTEGER | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;

        public static final ROStringArray enums = PrecedenceCommon.enums;
        public static final ROStringArray enumValues
                                            = PrecedenceCommon.enumValues;
    }

    public static class BorderBeforeStyle extends Properties {
        public static final int dataTypes = ENUM | NONE | INHERIT;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = NONE_IT;

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonStyle.enums;
        public static final Map enumValues = BorderCommonStyle.enumValues;
    }

    public static class BorderBeforeWidth extends Properties {
        public static final int dataTypes = MAPPED_NUMERIC | LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength
                            (PropNames.BORDER_BEFORE_WIDTH, 1d, Length.PT);
        }

        public static Numeric[] getMappedNumArray()
            throws PropertyException
        {
            return Properties.BorderCommonWidth.borderWidthNumArray
                                            (PropNames.BORDER_BEFORE_WIDTH);
        }

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonWidth.enums;
        public static final ROStringArray enumValues
                                            = BorderCommonWidth.enumValues;
    }

    public static class BorderBeforeWidthLength extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = COMPOUND;
    }

    public static class BorderBeforeWidthConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = ENUM_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return  new EnumType(PropNames.BORDER_BEFORE_WIDTH_CONDITIONALITY, Conditionality.DISCARD);
        }
        public static final int inherited = COMPOUND;

        public static final ROStringArray enums = Conditionality.enums;
        public static final ROStringArray enumValues
                                            = Conditionality.enumValues;
    }

    public static class BorderBottom extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;

        /**
         * 'value' is a PropertyValueList or an individual PropertyValue.
         *
         * <p>The value(s) provided, if valid, are converted into a list
         * containing the expansion of the shorthand.  The elements may
         * be in any order.  A minimum of one value will be present.
         *
         *   a border-EDGE-color ColorType or inheritance value
         *   a border-EDGE-style EnumType or inheritance value
         *   a border-EDGE-width MappedNumeric or inheritance value
         *
         *  N.B. this is the order of elements defined in
         *       PropertySets.borderRightExpansion
         *
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @return <tt>PropertyValue</tt> the verified value
         */
        public static PropertyValue verifyParsing
                                        (FOTree foTree, PropertyValue value)
                    throws PropertyException
        {
            return Properties.borderEdge(foTree, value,
                                    PropNames.BORDER_BOTTOM_STYLE,
                                    PropNames.BORDER_BOTTOM_COLOR,
                                    PropNames.BORDER_BOTTOM_WIDTH
                                    );
        }
    }

    public static class BorderBottomColor extends Properties {
        public static final int dataTypes = COLOR_T | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = COLOR_IT;
        public static final int inherited = NO;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new ColorType (PropNames.BACKGROUND_COLOR, ColorCommon.BLACK);
        }

        public static final ROStringArray enums = ColorCommon.enums;
        public static final Map enumValues = ColorCommon.colorTransEnums;
    }

    public static class BorderBottomStyle extends Properties {
        public static final int dataTypes = ENUM | NONE | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = NONE_IT;

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonStyle.enums;
        public static final Map enumValues = BorderCommonStyle.enumValues;
    }

    public static class BorderBottomWidth extends Properties {
        public static final int dataTypes = MAPPED_NUMERIC | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength
                            (PropNames.BORDER_BOTTOM_WIDTH, 1d, Length.PT);
        }

        public static Numeric[] getMappedNumArray()
            throws PropertyException
        {
            return Properties.BorderCommonWidth.borderWidthNumArray
                                            (PropNames.BORDER_BOTTOM_WIDTH);
        }

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonWidth.enums;
        public static final ROStringArray enumValues
                                            = BorderCommonWidth.enumValues;
    }

    public static class BorderCollapse extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int COLLAPSE = 1;
        public static final int SEPARATE = 2;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return  new EnumType (PropNames.BORDER_COLLAPSE, COLLAPSE);
        }

        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"collapse"
            ,"separate"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class BorderColor extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;

        public static final ROStringArray enums = ColorCommon.enums;
        public static final Map enumValues = ColorCommon.colorTransEnums;

        /**
         * 'value' is a PropertyValueList or an individual PropertyValue.
         *
         * <p>If 'value' is an individual PropertyValue, it must contain
         * either
         *   a ColorType value
         *   a NCName containing a standard color name or 'transparent'
         *   a FromParent value,
         *   a FromNearestSpecified value,
         *   or an Inherit value.
         *
         * <p>If 'value' is a PropertyValueList, it contains a list of
         * 2 to 4 ColorType values or NCName enum tokens representing colors.
         *
         * <p>The value(s) provided, if valid, are converted into a list
         * containing the expansion of the shorthand.
         * The first element is a value for border-top-color,
         * the second element is a value for border-right-color,
         * the third element is a value for border-bottom-color,
         * the fourth element is a value for border-left-color.
         *
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @return <tt>PropertyValue</tt> the verified value
         */
        public static PropertyValue verifyParsing
                                        (FOTree foTree, PropertyValue value)
                    throws PropertyException
        {
            return verifyParsing(foTree, value, NOT_NESTED);
        }

        /**
         * Do the work for the two argument verifyParsing method.
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @param nested <tt>boolean</tt> indicating whether this method is
         * called normally (false), or as part of another <i>verifyParsing</i>
         * method.
         * @return <tt>PropertyValue</tt> the verified value
         * @see #verifyParsing(FOTree,PropertyValue)
         */
        public static PropertyValue verifyParsing
                        (FOTree foTree, PropertyValue value, boolean nested)
                    throws PropertyException
        {
            if ( ! (value instanceof PropertyValueList)) {
                if ( ! nested) {
                    if (value instanceof Inherit
                        || value instanceof FromParent
                        || value instanceof FromNearestSpecified
                        )
                        return PropertySets.expandAndCopySHand(value);
                }
                if (value instanceof ColorType)
                    return PropertySets.expandAndCopySHand(value);
                if (value instanceof NCName) {
                    // Must be a standard color
                    ColorType color;
                    try {
                        color = new ColorType(PropNames.BORDER_COLOR,
                                            ((NCName)value).getNCName());
                    } catch (PropertyException e) {
                        throw new PropertyException
                            (((NCName)value).getNCName() +
                                " not a standard color for border-color");
                    }
                    return PropertySets.expandAndCopySHand(color);
                }
                else throw new PropertyException
                    ("Invalid " + value.getClass().getName() +
                                                " value for border-color");
            } else {
                if (nested) throw new PropertyException
                        ("PropertyValueList invalid for nested border-color "
                            + "verifyParsing() method");
                // List may contain only multiple color specifiers
                // i.e. ColorTypes or NCNames specifying a standard color or
                // 'transparent'.
                PropertyValueList list =
                                spaceSeparatedList((PropertyValueList)value);
                ColorType top, left, bottom, right;
                int count = list.size();
                if (count < 2 || count > 4)
                    throw new PropertyException
                        ("border-color list contains " + count + " items");

                Iterator colors = list.iterator();

                // There must be at least two
                top = getColor((PropertyValue)(colors.next()));
                left = getColor((PropertyValue)(colors.next()));
                try {
                    bottom = (ColorType)(top.clone());
                    right = (ColorType)(left.clone());
                } catch (CloneNotSupportedException cnse) {
                    throw new PropertyException
                                    ("clone() not supported on ColorType");
                }

                if (colors.hasNext()) bottom
                                = getColor((PropertyValue)(colors.next()));
                if (colors.hasNext()) right
                                = getColor((PropertyValue)(colors.next()));

                // Set the properties for each
                top.setProperty(PropNames.BORDER_TOP_COLOR);
                left.setProperty(PropNames.BORDER_LEFT_COLOR);
                bottom.setProperty(PropNames.BORDER_BOTTOM_COLOR);
                right.setProperty(PropNames.BORDER_RIGHT_COLOR);

                list = new PropertyValueList(PropNames.BORDER_COLOR);
                list.add(top);
                list.add(right);
                list.add(bottom);
                list.add(left);
                // Question: if less than four colors have been specified in
                // the shorthand, what border-?-color properties, if any,
                // have been specified?
                return list;
            }
        }

        /**
         * Return the ColorType derived from the argument.
         * The argument must be either a ColorType already, in which case
         * it is returned unchanged, or an NCName whose string value is a
         * standard color or 'transparent'.
         * @param value <tt>PropertyValue</tt>
         * @return <tt>ColorValue</tt> equivalent of the argument
         * @exception <tt>PropertyException</tt>
         */
        private static ColorType getColor(PropertyValue value)
                throws PropertyException
        {
            int property = value.getProperty();
            if (value instanceof ColorType) return (ColorType)value;
            // Must be a color enum
            if ( ! (value instanceof NCName))
                throw new PropertyException
                    (value.getClass().getName() + " instead of color for "
                                    + PropNames.getPropertyName(property));
            // We have an NCName - hope it''s a color
            NCName ncname = (NCName)value;
            try {
                return new ColorType(property, ncname.getNCName());
            } catch (PropertyException e) {
                throw new PropertyException
                            (ncname.getNCName() + " instead of color for "
                                    + PropNames.getPropertyName(property));
            }
        }
    }

    public static class BorderEndColor extends Properties {
        public static final int dataTypes = COLOR_T | INHERIT;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = COLOR_IT;
        public static final int inherited = NO;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new ColorType (PropNames.BACKGROUND_COLOR, ColorCommon.BLACK);
        }

        public static final ROStringArray enums = ColorCommon.enums;
        public static final Map enumValues = ColorCommon.colorTransEnums;
    }

    public static class BorderEndPrecedence extends Properties {
        public static final int dataTypes = INTEGER | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;

        public static final ROStringArray enums = PrecedenceCommon.enums;
        public static final ROStringArray enumValues
                                            = PrecedenceCommon.enumValues;
    }

    public static class BorderEndStyle extends Properties {
        public static final int dataTypes = ENUM | NONE | INHERIT;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = NONE_IT;

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonStyle.enums;
        public static final Map enumValues = BorderCommonStyle.enumValues;
    }

    public static class BorderEndWidth extends Properties {
        public static final int dataTypes = MAPPED_NUMERIC | LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength
                                (PropNames.BORDER_END_WIDTH, 1d, Length.PT);
        }

        public static Numeric[] getMappedNumArray()
            throws PropertyException
        {
            return Properties.BorderCommonWidth.borderWidthNumArray
                                            (PropNames.BORDER_END_WIDTH);
        }

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonWidth.enums;
        public static final ROStringArray enumValues
                                            = BorderCommonWidth.enumValues;
    }

    public static class BorderEndWidthLength extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = COMPOUND;
    }

    public static class BorderEndWidthConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = ENUM_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return  new EnumType(PropNames.BORDER_END_WIDTH_CONDITIONALITY, Conditionality.DISCARD);
        }
        public static final int inherited = COMPOUND;

        public static final ROStringArray enums = Conditionality.enums;
        public static final ROStringArray enumValues
                                            = Conditionality.enumValues;
    }

    public static class BorderLeft extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;

        /**
         * 'value' is a PropertyValueList or an individual PropertyValue.
         *
         * <p>The value(s) provided, if valid, are converted into a list
         * containing the expansion of the shorthand.  The elements may
         * be in any order.  A minimum of one value will be present.
         *
         *   a border-EDGE-color ColorType or inheritance value
         *   a border-EDGE-style EnumType or inheritance value
         *   a border-EDGE-width MappedNumeric or inheritance value
         *
         *  N.B. this is the order of elements defined in
         *       PropertySets.borderRightExpansion
         *
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @return <tt>PropertyValue</tt> the verified value
         */
        public static PropertyValue verifyParsing
                (FOTree foTree, PropertyValue value)
                    throws PropertyException
        {
            return Properties.borderEdge(foTree, value,
                                    PropNames.BORDER_LEFT_STYLE,
                                    PropNames.BORDER_LEFT_COLOR,
                                    PropNames.BORDER_LEFT_WIDTH
                                    );
        }
    }

    public static class BorderLeftColor extends Properties {
        public static final int dataTypes = COLOR_T | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = COLOR_IT;
        public static final int inherited = NO;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new ColorType (PropNames.BACKGROUND_COLOR, ColorCommon.BLACK);
        }

        public static final ROStringArray enums = ColorCommon.enums;
        public static final Map enumValues = ColorCommon.colorTransEnums;
    }

    public static class BorderLeftStyle extends Properties {
        public static final int dataTypes = ENUM | NONE | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = NONE_IT;

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonStyle.enums;
        public static final Map enumValues = BorderCommonStyle.enumValues;
    }

    public static class BorderLeftWidth extends Properties {
        public static final int dataTypes = MAPPED_NUMERIC | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength
                                (PropNames.BORDER_LEFT_WIDTH, 1d, Length.PT);
        }

        public static Numeric[] getMappedNumArray()
            throws PropertyException
        {
            return Properties.BorderCommonWidth.borderWidthNumArray
                                            (PropNames.BORDER_LEFT_WIDTH);
        }

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonWidth.enums;
        public static final ROStringArray enumValues
                                            = BorderCommonWidth.enumValues;
    }

    public static class BorderRight extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;

        /**
         * 'value' is a PropertyValueList or an individual PropertyValue.
         *
         * <p>The value(s) provided, if valid, are converted into a list
         * containing the expansion of the shorthand.  The elements may
         * be in any order.  A minimum of one value will be present.
         *
         *   a border-EDGE-color ColorType or inheritance value
         *   a border-EDGE-style EnumType or inheritance value
         *   a border-EDGE-width MappedNumeric or inheritance value
         *
         *  N.B. this is the order of elements defined in
         *       PropertySets.borderRightExpansion
         *
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @return <tt>PropertyValue</tt> the verified value
         */
        public static PropertyValue verifyParsing
                (FOTree foTree, PropertyValue value)
                    throws PropertyException
        {
            return Properties.borderEdge(foTree, value,
                                    PropNames.BORDER_RIGHT_STYLE,
                                    PropNames.BORDER_RIGHT_COLOR,
                                    PropNames.BORDER_RIGHT_WIDTH
                                    );
        }

    }

    public static class BorderRightColor extends Properties {
        public static final int dataTypes = COLOR_T | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = COLOR_IT;
        public static final int inherited = NO;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new ColorType (PropNames.BACKGROUND_COLOR, ColorCommon.BLACK);
        }

        public static final ROStringArray enums = ColorCommon.enums;
        public static final Map enumValues = ColorCommon.colorTransEnums;
    }

    public static class BorderRightStyle extends Properties {
        public static final int dataTypes = ENUM | NONE | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = NONE_IT;

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonStyle.enums;
        public static final Map enumValues = BorderCommonStyle.enumValues;
    }

    public static class BorderRightWidth extends Properties {
        public static final int dataTypes = MAPPED_NUMERIC | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength
                                (PropNames.BORDER_RIGHT_WIDTH, 1d, Length.PT);
        }

        public static Numeric[] getMappedNumArray()
            throws PropertyException
        {
            return Properties.BorderCommonWidth.borderWidthNumArray
                                            (PropNames.BORDER_RIGHT_WIDTH);
        }

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonWidth.enums;
        public static final ROStringArray enumValues
                                            = BorderCommonWidth.enumValues;
    }

    public static class BorderSeparation extends Properties {
        public static final int dataTypes = INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = COMPUTED;
    }

    public static class BorderSeparationBlockProgressionDirection
                                                        extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.BORDER_SEPARATION_BLOCK_PROGRESSION_DIRECTION, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class BorderSeparationInlineProgressionDirection
                                                        extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.BORDER_SEPARATION_INLINE_PROGRESSION_DIRECTION, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class BorderSpacing extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = SHORTHAND_INH;

        /**
         * 'value' is a PropertyValueList or an individual PropertyValue.
         *
         * Legal values are:
         *   an Inherit PropertyValue
         *   a FromParent PropertyValue
         *   a FromNearestSpecified PropertyValue
         *   a Length PropertyValue
         *   a list containing 2 Length PropertyValues
         *   Note: the Lengths cannot be percentages (what about relative
         *         lengths?)
         *
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @return <tt>PropertyValue</tt> the verified value
         */
        public static PropertyValue verifyParsing
                (FOTree foTree, PropertyValue value)
                    throws PropertyException
        {
            if ( ! (value instanceof PropertyValueList)) {
                if (value instanceof Inherit
                    || value instanceof FromParent
                    || value instanceof FromNearestSpecified
                    )
                    return PropertySets.expandAndCopySHand(value);
                if (value instanceof Numeric && ((Numeric)value).isLength())
                    return PropertySets.expandAndCopySHand(value);
                throw new PropertyException
                    ("Invalid " + value.getClass().getName() +
                        " object for border-spacing");
            } else {
                // Must be a pair of Lengths
                PropertyValueList list =
                                spaceSeparatedList((PropertyValueList)value);
                if (list.size() != 2)
                    throw new PropertyException
                        ("List of " + list.size() + " for border-spacing");
                PropertyValue len1 = (PropertyValue)(list.getFirst());
                PropertyValue len2 = (PropertyValue)(list.getLast());
                // Note that this test excludes (deliberately) ems relative
                // lengths.  I don't know whether this exclusion is valid.
                if ( ! (len1 instanceof Numeric && len2 instanceof Numeric
                    && ((Numeric)len1).isLength()
                    && ((Numeric)len2).isLength()))
                    throw new PropertyException
                        ("Values to border-spacing are not both Lengths");
                // Set the individual expanded properties of the
                // border-separation compound property
                // Should I clone these values?
                len1.setProperty
                    (PropNames.BORDER_SEPARATION_BLOCK_PROGRESSION_DIRECTION);
                len2.setProperty
                    (PropNames.BORDER_SEPARATION_INLINE_PROGRESSION_DIRECTION);
                return value;
            }
        }

    }

    public static class BorderStartColor extends Properties {
        public static final int dataTypes = COLOR_T | INHERIT;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = COLOR_IT;
        public static final int inherited = NO;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return
                new ColorType(PropNames.BACKGROUND_COLOR, ColorCommon.BLACK);
        }

        public static final ROStringArray enums = ColorCommon.enums;
        public static final Map enumValues = ColorCommon.colorTransEnums;
    }

    public static class BorderStartPrecedence extends Properties {
        public static final int dataTypes = INTEGER | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;

        public static final ROStringArray enums = PrecedenceCommon.enums;
        public static final ROStringArray enumValues
                                                = PrecedenceCommon.enumValues;
    }

    public static class BorderStartStyle extends Properties {
        public static final int dataTypes = ENUM | NONE | INHERIT;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = NONE_IT;

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonStyle.enums;
        public static final Map enumValues = BorderCommonStyle.enumValues;
    }

    public static class BorderStartWidth extends Properties {
        public static final int dataTypes = MAPPED_NUMERIC | LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength
                                (PropNames.BORDER_START_WIDTH, 1d, Length.PT);
        }

        public static Numeric[] getMappedNumArray()
            throws PropertyException
        {
            return Properties.BorderCommonWidth.borderWidthNumArray
                                            (PropNames.BORDER_START_WIDTH);
        }

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonWidth.enums;
        public static final ROStringArray enumValues
                                            = BorderCommonWidth.enumValues;
    }

    public static class BorderStartWidthLength extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = COMPOUND;
    }

    public static class BorderStartWidthConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = ENUM_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return  new EnumType(PropNames.BORDER_START_WIDTH_CONDITIONALITY, Conditionality.DISCARD);
        }
        public static final int inherited = COMPOUND;

        public static final ROStringArray enums = Conditionality.enums;
        public static final ROStringArray enumValues
                                                = Conditionality.enumValues;
    }

    public static class BorderStyle extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonStyle.enums;
        public static final Map enumValues = BorderCommonStyle.enumValues;

        /**
         * 'value' is a PropertyValueList or an individual PropertyValue.
         *
         * <p>If 'value' is an individual PropertyValue, it must contain
         * either
         *   a NCName containing a border-style name
         *   a FromParent value,
         *   a FromNearestSpecified value,
         *   or an Inherit value.
         *
         * <p>If 'value' is a PropertyValueList, it contains a list of
         * 2 to 4 NCName enum tokens representing border-styles.
         *
         * <p>The value(s) provided, if valid, are converted into a list
         * containing the expansion of the shorthand.
         * The first element is a value for border-top-style,
         * the second element is a value for border-right-style,
         * the third element is a value for border-bottom-style,
         * the fourth element is a value for border-left-style.
         *
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @return <tt>PropertyValue</tt> the verified value
         */
        public static PropertyValue verifyParsing
                                        (FOTree foTree, PropertyValue value)
                    throws PropertyException
        {
            return verifyParsing(foTree, value, NOT_NESTED);
        }

        /**
         * Do the work for the two argument verifyParsing method.
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @param nested <tt>boolean</tt> indicating whether this method is
         * called normally (false), or as part of another <i>verifyParsing</i>
         * method.
         * @return <tt>PropertyValue</tt> the verified value
         * @see #verifyParsing(FOTree,PropertyValue)
         */
        public static PropertyValue verifyParsing
                        (FOTree foTree, PropertyValue value, boolean nested)
                    throws PropertyException
        {
            if ( ! (value instanceof PropertyValueList)) {
                if ( ! nested) {
                    if (value instanceof Inherit
                        || value instanceof FromParent
                        || value instanceof FromNearestSpecified
                        )
                        return PropertySets.expandAndCopySHand(value);
                }
                if (value instanceof NCName) {
                    // Must be a border-style
                    EnumType enum;
                    try {
                        enum = new EnumType(PropNames.BORDER_STYLE,
                                            ((NCName)value).getNCName());
                    } catch (PropertyException e) {
                        throw new PropertyException
                            (((NCName)value).getNCName() +
                                                    " not a border-style");
                    }
                    return PropertySets.expandAndCopySHand(enum);
                }
                else throw new PropertyException
                    ("Invalid " + value.getClass().getName() +
                                                " value for border-style");
            } else {
                if (nested) throw new PropertyException
                        ("PropertyValueList invalid for nested border-style "
                            + "verifyParsing() method");
                // List may contain only multiple style specifiers
                // i.e. NCNames specifying a standard style
                PropertyValueList list =
                                spaceSeparatedList((PropertyValueList)value);
                EnumType top, left, bottom, right;
                int count = list.size();
                if (count < 2 || count > 4)
                    throw new PropertyException
                        ("border-style list contains " + count + " items");

                Iterator styles = list.iterator();

                // There must be at least two
                top = Properties.getEnum((PropertyValue)(styles.next()),
                                    PropNames.BORDER_TOP_STYLE , "style");
                left = Properties.getEnum((PropertyValue)(styles.next()),
                                    PropNames.BORDER_LEFT_STYLE, "style");
                try {
                    bottom = (EnumType)(top.clone());
                    bottom.setProperty(PropNames.BORDER_BOTTOM_STYLE);
                    right = (EnumType)(left.clone());
                    right.setProperty(PropNames.BORDER_RIGHT_STYLE);
                } catch (CloneNotSupportedException cnse) {
                    throw new PropertyException
                                ("clone() not supported on EnumType");
                }

                if (styles.hasNext()) bottom
                        = Properties.getEnum((PropertyValue)(styles.next()),
                                    PropNames.BORDER_BOTTOM_STYLE, "style");
                if (styles.hasNext()) right
                        = Properties.getEnum((PropertyValue)(styles.next()),
                                    PropNames.BORDER_RIGHT_STYLE, "style");

                list = new PropertyValueList(PropNames.BORDER_STYLE);
                list.add(top);
                list.add(right);
                list.add(bottom);
                list.add(left);
                // Question: if less than four styles have been specified in
                // the shorthand, what border-?-style properties, if any,
                // have been specified?
                return list;
            }
        }
    }

    public static class BorderTop extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;

        /**
         * 'value' is a PropertyValueList or an individual PropertyValue.
         *
         * <p>The value(s) provided, if valid, are converted into a list
         * containing the expansion of the shorthand.  The elements may
         * be in any order.  A minimum of one value will be present.
         *
         *   a border-EDGE-color ColorType or inheritance value
         *   a border-EDGE-style EnumType or inheritance value
         *   a border-EDGE-width MappedNumeric or inheritance value
         *
         *  N.B. this is the order of elements defined in
         *       PropertySets.borderRightExpansion
         *
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @return <tt>PropertyValue</tt> the verified value
         */
        public static PropertyValue verifyParsing
                (FOTree foTree, PropertyValue value)
                    throws PropertyException
        {
            return Properties.borderEdge(foTree, value,
                                    PropNames.BORDER_TOP_STYLE,
                                    PropNames.BORDER_TOP_COLOR,
                                    PropNames.BORDER_TOP_WIDTH
                                    );
        }
    }

    public static class BorderTopColor extends Properties {
        public static final int dataTypes = COLOR_T | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = COLOR_IT;
        public static final int inherited = NO;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new ColorType (PropNames.BACKGROUND_COLOR, ColorCommon.BLACK);
        }

        public static final ROStringArray enums = ColorCommon.enums;
        private static final HashMap rwEnumValues = ColorCommon.rwColorEnums;
    }

    public static class BorderTopStyle extends Properties {
        public static final int dataTypes = ENUM | NONE | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = NONE_IT;

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonStyle.enums;
        public static final Map enumValues = BorderCommonStyle.enumValues;
    }

    public static class BorderTopWidth extends Properties {
        public static final int dataTypes = MAPPED_NUMERIC | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength
                                (PropNames.BORDER_TOP_WIDTH, 1d, Length.PT);
        }

        public static Numeric[] getMappedNumArray()
            throws PropertyException
        {
            return Properties.BorderCommonWidth.borderWidthNumArray
                                            (PropNames.BORDER_TOP_WIDTH);
        }

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonWidth.enums;
        public static final ROStringArray enumValues
                                            = BorderCommonWidth.enumValues;
    }

    public static class BorderWidth extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonWidth.enums;
        public static final ROStringArray enumValues
                                            = BorderCommonWidth.enumValues;

        /**
         * 'value' is a PropertyValueList or an individual PropertyValue.
         *
         * <p>If 'value' is an individual PropertyValue, it must contain
         * either
         *   a NCName containing a border-width name
         *   a FromParent value,
         *   a FromNearestSpecified value,
         *   or an Inherit value.
         *
         * <p>If 'value' is a PropertyValueList, it contains a
         * PropertyValueList which in turn contains a list of
         * 2 to 4 NCName enum tokens representing border-widths.
         *
         * <p>The value(s) provided, if valid, are converted into a list
         * containing the expansion of the shorthand.
         * The first element is a value for border-top-width,
         * the second element is a value for border-right-width,
         * the third element is a value for border-bottom-width,
         * the fourth element is a value for border-left-width.
         *
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @return <tt>PropertyValue</tt> the verified value
         */
        public static PropertyValue verifyParsing
                                        (FOTree foTree, PropertyValue value)
                    throws PropertyException
        {
            return verifyParsing(foTree, value, NOT_NESTED);
        }

        /**
         * Do the work for the two argument verifyParsing method.
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @param nested <tt>boolean</tt> indicating whether this method is
         * called normally (false), or as part of another <i>verifyParsing</i>
         * method.
         * @return <tt>PropertyValue</tt> the verified value
         * @see #verifyParsing(FOTree,PropertyValue)
         */
        public static PropertyValue verifyParsing
                        (FOTree foTree, PropertyValue value, boolean nested)
                    throws PropertyException
        {
            if ( ! (value instanceof PropertyValueList)) {
                if ( ! nested) {
                    if (value instanceof Inherit
                        || value instanceof FromParent
                        || value instanceof FromNearestSpecified
                        )
                        return PropertySets.expandAndCopySHand(value);
                }
                if (value instanceof NCName) {
                    // Must be a border-width
                    Numeric mapped;
                    try {
                        mapped =
                            (new MappedNumeric(PropNames.BORDER_WIDTH,
                                ((NCName)value).getNCName(), foTree))
                                    .getMappedNumValue();
                    } catch (PropertyException e) {
                        throw new PropertyException
                            (((NCName)value).getNCName() +
                                                    " not a border-width");
                    }
                    return PropertySets.expandAndCopySHand(mapped);
                }
                else throw new PropertyException
                    ("Invalid " + value.getClass().getName() +
                                                " value for border-width");
            } else {
                if (nested) throw new PropertyException
                        ("PropertyValueList invalid for nested border-width "
                            + "verifyParsing() method");
                // List may contain only multiple width specifiers
                // i.e. NCNames specifying a standard width
                PropertyValueList list =
                                spaceSeparatedList((PropertyValueList)value);
                Numeric top, left, bottom, right;
                int count = list.size();
                if (count < 2 || count > 4)
                    throw new PropertyException
                        ("border-width list contains " + count + " items");

                Iterator widths = list.iterator();

                // There must be at least two
                top = (new MappedNumeric
                            (PropNames.BORDER_TOP_WIDTH,
                            ((NCName)(widths.next())).getNCName(), foTree)
                        ).getMappedNumValue();
                left = (new MappedNumeric
                            (PropNames.BORDER_LEFT_WIDTH,
                            ((NCName)(widths.next())).getNCName(), foTree)
                        ).getMappedNumValue();
                try {
                    bottom = (Numeric)(top.clone());
                    bottom.setProperty(PropNames.BORDER_BOTTOM_WIDTH);
                    right = (Numeric)(left.clone());
                    right.setProperty(PropNames.BORDER_RIGHT_WIDTH);
                } catch (CloneNotSupportedException cnse) {
                    throw new PropertyException
                                ("clone() not supported on Numeric");
                }

                if (widths.hasNext())
                    bottom = (new MappedNumeric
                                (PropNames.BORDER_BOTTOM_WIDTH,
                                ((NCName)(widths.next())).getNCName(), foTree)
                            ).getMappedNumValue();
                if (widths.hasNext())
                    right = (new MappedNumeric
                                (PropNames.BORDER_RIGHT_WIDTH,
                                ((NCName)(widths.next())).getNCName(), foTree)
                            ).getMappedNumValue();

                list = new PropertyValueList(PropNames.BORDER_WIDTH);
                list.add(top);
                list.add(right);
                list.add(bottom);
                list.add(left);
                // Question: if less than four widths have been specified in
                // the shorthand, what border-?-width properties, if any,
                // have been specified?
                return list;
            }
        }
    }

    public static class Bottom extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | AUTO | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class BreakCommon extends Properties {
        public static final int COLUMN = 1;
        public static final int PAGE = 2;
        public static final int EVEN_PAGE = 3;
        public static final int ODD_PAGE = 4;

        private static final String[] rwEnums = {
            null
            ,"column"
            ,"page"
            ,"even-page"
            ,"odd-page"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);

        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }

    public static class BreakAfter extends Properties {
        public static final int dataTypes = AUTO | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;

        public static final int inherited = NO;

        public static final ROStringArray enums = BreakCommon.enums;
        private static final HashMap rwEnumValues = BreakCommon.rwEnumValues;
    }

    public static class BreakBefore extends Properties {
        public static final int dataTypes = AUTO | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;

        public static final int inherited = NO;

        public static final ROStringArray enums = BreakCommon.enums;
        private static final HashMap rwEnumValues = BreakCommon.rwEnumValues;
    }

    public static class CaptionSide extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int BEFORE = 1;
        public static final int AFTER = 2;
        public static final int START = 3;
        public static final int END = 4;
        public static final int TOP = 5;
        public static final int BOTTOM = 6;
        public static final int LEFT = 7;
        public static final int RIGHT = 8;

        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.CAPTION_SIDE, BEFORE);
        }

        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"before"
            ,"after"
            ,"start"
            ,"end"
            ,"top"
            ,"bottom"
            ,"left"
            ,"right"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);

        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }

    public static class CaseName extends Properties {
        public static final int dataTypes = NAME;
        public static final int traitMapping = ACTION;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class CaseTitle extends Properties {
        public static final int dataTypes = STRING;
        public static final int traitMapping = ACTION;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class Character extends Properties {
        public static final int dataTypes = CHARACTER_T;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class Clear extends Properties {
        public static final int dataTypes = ENUM | NONE | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NONE_IT;
        public static final int START = 1;
        public static final int END = 2;
        public static final int LEFT = 3;
        public static final int RIGHT = 4;
        public static final int BOTH = 5;

        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"start"
            ,"end"
            ,"left"
            ,"right"
            ,"both"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);

        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }

    public static class Clip extends Properties {
        public static final int dataTypes = AUTO | COMPLEX | INHERIT;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;

        /*
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @return <tt>PropertyValue</tt> the verified value
         */
        public static PropertyValue verifyParsing
                                        (FOTree foTree, PropertyValue value)
                        throws PropertyException
        {
            if (value instanceof Inherit || value instanceof Auto)
                return value;
            if (! (value instanceof PropertyValueList))
                throw new PropertyException
                    ("clip: <shape> requires 4 <length> or <auto> args");
            PropertyValueList list = (PropertyValueList) value;
            if (list.size() != 4) throw new PropertyException
                    ("clip: <shape> requires 4 lengths");
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                if ( ! (obj instanceof Length || obj instanceof Auto))
                    throw new PropertyException
                        ("clip: <shape> requires 4 <length> or <auto> args");
            }
            return value;
        }
    }

    public static class Color extends Properties {
        public static final int dataTypes = COLOR_T | INHERIT;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = COLOR_IT;
        public static final int inherited = COMPUTED;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new ColorType (PropNames.BACKGROUND_COLOR, ColorCommon.BLACK);
        }

        public static final ROStringArray enums = ColorCommon.enums;
        private static final HashMap rwEnumValues = ColorCommon.rwColorEnums;
    }

    public static class ColorProfileName extends Properties {
        public static final int dataTypes = NAME | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class ColumnCount extends Properties {
        public static final int dataTypes = NUMBER | INHERIT;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = NUMBER_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Numeric(PropNames.COLUMN_COUNT, 1d);
        }

        public static final int inherited = NO;
    }

    public static class ColumnGap extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.COLUMN_GAP, 12.0d, Length.PT);
        }
        public static final int inherited = NO;
    }

    public static class ColumnNumber extends Properties {
        public static final int dataTypes = NUMBER;
        public static final int traitMapping = VALUE_CHANGE;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class ColumnWidth extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class ContentDimension extends Properties {
        public static final int SCALE_TO_FIT = 1;

        private static final String[] rwEnums = {
            null
            ,"scale-to-fit"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);

        public static final ROStringArray enumValues = enums;
    }

    public static class ContentHeight extends Properties {
        public static final int dataTypes =
                                PERCENTAGE | LENGTH | AUTO | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;

        public static final ROStringArray enums = ContentDimension.enums;
        public static final ROStringArray enumValues
                                            = ContentDimension.enumValues;
    }

    public static class ContentType extends Properties {
        public static final int dataTypes = NAME | MIMETYPE | AUTO;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class ContentWidth extends Properties {
        public static final int dataTypes =
                                PERCENTAGE | LENGTH | AUTO | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;

        public static final ROStringArray enums = ContentDimension.enums;
        public static final ROStringArray enumValues
                                            = ContentDimension.enumValues;
    }

    public static class Country extends Properties {
        public static final int dataTypes = COUNTRY_T | NONE | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NONE_IT;
        public static final int inherited = COMPUTED;
    }

    public static class Cue extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = NO;

        /**
         * 'value' is a PropertyValueList or an individual PropertyValue.
         *
         * <p>If 'value' is an individual PropertyValue, it must contain
         * either
         *   a parsed UriType value,
         *   a FromParent value,
         *   a FromNearestSpecified value,
         *   or an Inherit value.
         *
         * <p>If 'value' is a PropertyValueList, it contains an inner
         * PropertyValueList of 2 parsed UriType values.
         *
         * <p>The value(s) provided, if valid, are converted into a list
         * containing the expansion of the shorthand.
         * The first element is a value for cue-before,
         * the second element is a value for cue-after.
         *
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @return <tt>PropertyValue</tt> the verified value
         */
        public static PropertyValue verifyParsing
                (FOTree foTree, PropertyValue value)
                    throws PropertyException
        {
            if ( ! (value instanceof PropertyValueList)) {
                if (value instanceof Inherit
                    || value instanceof FromParent
                    || value instanceof FromNearestSpecified
                    || value instanceof UriType
                    )
                    return PropertySets.expandAndCopySHand(value);
                throw new PropertyException
                    ("Invalid " + value.getClass().getName() +
                        " object for cue");
            } else {
                // List may contain only 2 uri specifiers
                PropertyValueList list =
                                spaceSeparatedList((PropertyValueList)value);
                if (list.size() != 2)
                    throw new PropertyException
                        ("List of " + list.size() + " for cue");
                PropertyValue cue1 = (PropertyValue)(list.getFirst());
                PropertyValue cue2 = (PropertyValue)(list.getLast());

                if ( ! ((cue1 instanceof UriType) &&
                        (cue2 instanceof UriType)))
                    throw new PropertyException
                        ("Values to cue are not both URIs");
                // Set the individual expanded properties of the
                // cue compound property
                // Should I clone these values?
                cue1.setProperty(PropNames.CUE_BEFORE);
                cue2.setProperty(PropNames.CUE_AFTER);
                return value;
            }
        }
    }

    public static class CueAfter extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = NO;
    }

    public static class CueBefore extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = NO;
    }

    public static class DestinationPlacementOffset extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = ACTION;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.DESTINATION_PLACEMENT_OFFSET, 0.0d, Length.PT);
        }
        public static final int inherited = NO;
    }

    public static class Direction extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = NEW_TRAIT;
        public static final int initialValueType = ENUM_IT;
        public static final int LTR = 1;
        public static final int RTL = 2;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType(PropNames.DIRECTION, LTR);
        }
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"ltr"
            ,"rtl"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class DisplayAlign extends Properties {
        public static final int dataTypes = AUTO | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int BEFORE = 1;
        public static final int CENTER = 2;
        public static final int AFTER = 3;
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"before"
            ,"center"
            ,"after"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class DominantBaseline extends Properties {
        public static final int dataTypes = AUTO | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int USE_SCRIPT = 1;
        public static final int NO_CHANGE = 2;
        public static final int RESET_SIZE = 3;
        public static final int IDEOGRAPHIC = 4;
        public static final int ALPHABETIC = 5;
        public static final int HANGING = 6;
        public static final int MATHEMATICAL = 7;
        public static final int CENTRAL = 8;
        public static final int MIDDLE = 9;
        public static final int TEXT_AFTER_EDGE = 10;
        public static final int TEXT_BEFORE_EDGE = 11;
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"use-script"
            ,"no-change"
            ,"reset-size"
            ,"ideographic"
            ,"alphabetic"
            ,"hanging"
            ,"mathematical"
            ,"central"
            ,"middle"
            ,"test-after-edge"
            ,"text-before-edge"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }

    public static class Elevation extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = COMPUTED;
    }

    public static class EmptyCells extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int SHOW = 1;
        public static final int HIDE = 2;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.EMPTY_CELLS, SHOW);
        }
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"show"
            ,"hide"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class EndIndent extends Properties {
        public static final int dataTypes = LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.END_INDENT, 0.0d, Length.PT);
        }
        public static final int inherited = COMPUTED;
    }

    public static class EndsRow extends Properties {
        public static final int dataTypes = BOOL;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = BOOL_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Bool(PropNames.ENDS_ROW, false);
        }
        public static final int inherited = NO;
    }

    public static class Extent extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.EXTENT, 0.0d, Length.PT);
        }
        public static final int inherited = NO;
    }

    public static class ExternalDestination extends Properties {
        public static final int dataTypes = URI_SPECIFICATION;
        public static final int traitMapping = ACTION;
        public static final int initialValueType = URI_SPECIFICATION_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new UriType(PropNames.EXTERNAL_DESTINATION, "");
        }
        public static final int inherited = NO;
    }

    public static class Float extends Properties {
        public static final int dataTypes = ENUM | NONE | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NONE_IT;
        public static final int BEFORE = 1;
        public static final int START = 2;
        public static final int END = 3;
        public static final int LEFT = 4;
        public static final int RIGHT = 5;

        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"before"
            ,"start"
            ,"end"
            ,"left"
            ,"right"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }

    public static class FlowName extends Properties {
        public static final int dataTypes = NAME;
        public static final int traitMapping = REFERENCE;
        public static final int initialValueType = NAME_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new NCName(PropNames.FLOW_NAME, "");
        }
        public static final int inherited = NO;
    }

    public static class Font extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = SHORTHAND_INH;

        public static final int
                   CAPTION = 1
                     ,ICON = 2
                     ,MENU = 3
              ,MESSAGE_BOX = 4
            ,SMALL_CAPTION = 5
               ,STATUS_BAR = 6
                           ;

        private static final String[] rwEnums = {
            null
            ,"caption"
            ,"icon"
            ,"menu"
            ,"message-box"
            ,"small-caption"
            ,"status-bar"
        };

        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }

        /**
         * 'value' is a PropertyValueList or an individual PropertyValue.
         *
         * <p>If 'value' is a PropertyValueList it may contain, in turn, a
         * PropertyValueList with the space-separated elements of the
         * original expression, or a list of comma-separated elements if
         * these were the only elements of the expression.
         * <p>
         * If a top-level list occurs, i.e. if 'value' is a PropertyValueList
         * which contains multiple elements, it represents a comma-separated
         * list.  Commas can only legitimately occur between elements of
         * a font-family specifier, but more than one may occur.
         * The font-family specifier MUST be preceded by at least one other
         * element, a font-size, and MAY be preceded by more than one.
         * I.e., if a COMMA is present in the list,
         * <pre>
         * value (PropertyValueList)
         *   |
         *   +  PropertyValueList COMMA element [...]
         *      (space separated) 
         *              |
         *              +- PropertyValue PropertyValue [...]
         * </pre>
         * <p>The complication here is that, while the final element of this
         * list must belong with the comma-separated font-family specifier,
         * preceding elements MAY also belong to the font-family.
         *
         * <p>If 'value' is <i>not</i> a PropertyValueList,
         * 'value' can contain a parsed Inherit value or
         *  a parsed NCName value containing one of the system font
         *  enumeration tokens listed in rwEnums, above.
         *  Note that it cannot contain a font-family, because that MUST
         *  be preceded by at least one other element.
         *
         * <p>If 'value' is a space-separated list, it may contain:
         *   A trailing <em>font-family</em> specification, preceded by
         *   a dimension specification, possibly
         *   preceded by an initial detailed font specification.
         *   The font-family specification may be comprised of a single
         *   element or a comma-separated list of specific or generic font
         *   names.  A single font name may be a space-separated name.
         *
         *   <p>The optional initial detailed font specification may be
         *    comprised of a trailing dimension specifier, possibly preceded
         *    by an optional set of font descriptor elements.
         *
         *   <p>The dimension specifier is comprised of a <em>font-size</em>
         *    specifier, possibly followed by a height
         *    specifier.  The height specifier is made up of a slash
         *    character followed by a <em>line-height</em> specifier proper.
         *
         *   <p>The optional set of font descriptor elements may include,
         *    in any order, from one to three of
         *    <br>a font-style element
         *    <br>a font-variant element
         *    <br>a font-weight element
         * <p>
         * [ [&lt;font-style&gt;||&lt;font-variant&gt;||&lt;font-weight&gt;]?
         *    &lt;font-size&gt; [/ &lt;line-height&gt;]? &lt;font-family&gt; ]
         *    |caption|icon|menu|message-box|small-caption|status-bar
         *    |inherit
         *
         * <p><b>Handling the font-family list.</b>
         * <p>For any list, the last element or elements must belong to a
         * font-family specifier.  If only a space-separated list is present
         * only one font is specified in the font-family, but it may have
         * space-separated components, e.g. 'Times New' in 'Times New Roman'.
         * If a comma-separated list is present, there are COMMAs + 1 fonts
         * given in the font-family, but for the first of these, some parts
         * of the name may be present in the preceding space-separated list,
         * e.g. (again) 'Times New'.
         *
         * <p>This is handled by splitting the passed PropertyValueList into
         * two sublists:- initial-list and font-family-list.  The latter
         * contains those components which MUST bw part of a font-family
         * specifier.  The trailing elements of the initila-list are scanned
         * to determine which are components of the font-family-list, and
         * these are trimmed from initial-list and prepended to
         * font-family-list.
         *
         * <p>The value(s) provided, if valid, are converted into a list
         * containing the expansion of the shorthand.  A minimum of one
         * value will be present.
         *
         * <p>N.B. from the specification:<br>
         * <p>QUOTE<br>
         * The "font" property is, except as described below, a shorthand
         * property for setting "font-style", "font-variant", "font-weight",
         * "font-size", "line-height", and "font-family", at the same place
         * in the stylesheet. The syntax of this property is based on a
         * traditional typographical shorthand notation to set multiple
         * properties related to fonts.
         * 
         * <p>All font-related properties are first reset to their initial
         * values, including those listed in the preceding paragraph plus
         * "font-stretch" and "font-size-adjust". Then, those properties
         * that are given explicit values in the "font" shorthand are set
         * to those values. For a definition of allowed and initial values,
         * see the previously defined properties. For reasons of backward
         * compatibility, it is not possible to set "font-stretch" and
         * "font-size-adjust" to other than their initial values using the
         * "font" shorthand property; instead, set the individual properties.
         * <br>ENDQUOTE
         *
         * <p>The setup of the shorthand expansion list is determined by the
         * above considerations.
         *
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @return <tt>PropertyValue</tt> the verified value
         */
        public static PropertyValue verifyParsing
                (FOTree foTree, PropertyValue value)
                        throws PropertyException
        {
            PropertyValueList startList = null;
            PropertyValueList fontList = null;
            if ( ! (value instanceof PropertyValueList)) {
                return processValue(foTree, value);
            } else {
                fontList = (PropertyValueList)value;
                try {
                    startList = spaceSeparatedList(fontList);
                    // A space-separated list.  Only the last item(s) can
                    // be part of the font-family-specifier
                    // Remove the space-separated list element (the only
                    // element) from fontList, so that the then-empty
                    // fontList can serve as the
                    // cachement list for font-family elements discovered
                    // at the end of startList
                    fontList.removeFirst();
                } catch (PropertyException e) {
                    // Must be a comma-separated list
                    // Pop the first element off the list; this will then
                    // be equivalent to the space-separated list above.
                    // Keep the remainder as the seed for a list argument
                    // to the FontFamily verifier
                    // Note the assumption that the list is not empty.
                    // This is consistent with the values returned from the
                    // parser.
                    Object tmpo = fontList.removeFirst();
                    // This MUST be a list, because it must contain both the
                    // font-family element preceding the implict COMMA which
                    // separated the original elements, and at least a
                    // font-size element
                    if ( ! (tmpo instanceof PropertyValueList))
                        throw new PropertyException
                            ("No space-separated list preceding COMMA in "
                            + "'font' expression");
                    startList = (PropertyValueList)tmpo;
                }
                return processSpaceSepList(foTree, startList, fontList);
            }
        }

        private static PropertyValueList processValue
            (FOTree foTree, PropertyValue value)
                        throws PropertyException
        {
            // Can be Inherit, FromParent, FromNearestSpecified, a
            // system font NCName or a single element font-family specifier
            if (value instanceof Inherit |
                    value instanceof FromParent |
                        value instanceof FromNearestSpecified)
            {
                return PropertySets.expandAndCopySHand(value);
            }
            // else not Inherit/From../From..
            FontFamilySet family = null;
            if (value instanceof NCName) {
                // Is it a system font enumeration?
                EnumType enum = null;
                String ncname = ((NCName)value).getNCName();
                try {
                    enum = new EnumType(value.getProperty(), ncname);
                } catch (PropertyException e) {
                    throw new PropertyException
                        ("Unrecognized NCName in font expression: " + ncname);
                }
                // A system font enum
                return SystemFontFunction.expandFontSHand
                                                (PropNames.FONT, ncname);
            }
            throw new PropertyException("Unrecognized element in font " +
                                        "expression: " + value);
        }

        /**
         * The space separated <i>list</i> must end with the first or only
         * font named in the font-family specifier of the font expression.<br>
         * The comma-separated <i>fontList</i> contains the second and
         * subsequent font names from the font-family specifier in the font
         * expression, or is empty.<br>
         * <p>Because font names may contain spaces and may be
         * specified "raw", without enclosing quotes, the font name which
         * ends the <i>list</i> may occupy more than one element at the end
         * of the <i>list</i>.
         * <p>The font-family is preceded by a compulsory size/height element;
         * font-size [ / line-height ]?, so searching backwards for a slash
         * character will locate the penultimate and utlimate elements
         * preceding the first component of the font-family.
         * <p>If no slash is found, the preceding element must be a font-size,
         * which may be specfied as a Length, a Percentage, or with an
         * enumeration token.
         * <p>Any elements preceding a font-size[/line-height]
         * must be from the specification
         * [font-style||font-variant||font-weight]?
         *
         * @param list a <tt>PropertyValueList</tt> containing the actual
         * space-separated list; i.e. the single inner list from the
         * outer list returned by the parser. or removed from the front of
         * a comma-separated list.
         * @param fontList a <tt>PropertyValueList</tt> containing any
         * font-family elements which followed the first comma of the font
         * expression.  This list may be empty (but is not null) in the case
         * where there was only one font-family element specified.
         * @return <tt>PropertyValueList</tt> containing a
         * <tt>PropertyValue</tt> for each property in the expansion of the
         * shorthand.
         * @exception PropertyValueException
         */
        private static PropertyValueList
                processSpaceSepList(FOTree foTree,
                                    PropertyValueList list,
                                    PropertyValueList fontList)
                        throws PropertyException
        {
            PropertyValueList newlist = null;

            // copy the list into an array for random access
            Object[] props = list.toArray();
            PropertyValue[] propvals = new PropertyValue[props.length];
            int slash = -1;
            int firstcomma = -1;
            int familyStart = -1;
            int fontsize = -1;

            PropertyValue style = null;
            PropertyValue variant = null;
            PropertyValue weight = null;
            PropertyValue size = null;
            PropertyValue height = null;
            PropertyValue fontset = null;

            for (int i = 0; i < props.length; i++) {
                propvals[i] = (PropertyValue)(props[i]);
                if (propvals[i] instanceof Slash)
                    slash = i;
                else if (propvals[i] instanceof PropertyValueList
                            && firstcomma == -1)
                    firstcomma = i;
            }
            if (slash != -1 && slash >= (propvals.length -2))
                throw new PropertyException
                                    ("Invalid slash position in font list");
            if (slash != -1) {
                // know where slash and line-height are
                // font-family begins at slash + 2
                familyStart = slash + 2;
                fontsize = slash - 1;
                size = FontSize.verifyParsing
                                    (foTree, propvals[fontsize], IS_NESTED);
                // derive the line-height
                // line-height is at slash + 1
                height = LineHeight.verifyParsing
                                    (foTree, propvals[slash + 1], IS_NESTED);
            } else {
                // Don''t know where slash is.  If anything precedes the
                // font-family, it must be a font-size.  Look for that.
                if (firstcomma == -1) firstcomma = propvals.length - 1;
                for (fontsize = firstcomma - 1; fontsize >= 0; fontsize--) {
                    if (propvals[fontsize] instanceof NCName) {
                        // try for a font-size enumeration
                        String name = ((NCName)propvals[fontsize]).getNCName();
                        try {
                            size = new MappedNumeric
                                        (PropNames.FONT_SIZE, name, foTree);
                        } catch (PropertyException e) {
                            // Attempt to derive mapped numeric failed
                            continue;
                        }
                        // Presumably we have a mapped numeric
                        break;
                    }
                    if (propvals[fontsize] instanceof Numeric) {
                        // Length (incl. Ems) or Percentage allowed
                        if (((Numeric)(propvals[fontsize])).isDistance()) {
                            size = propvals[fontsize];
                            break;
                        }
                        // else don't know what this Numeric is doing here -
                        // spit the dummy
                        throw new PropertyException("Non-distance Numeric"
                            + " found in 'font' expression while looking "
                            + "for font-size");
                    }
                    // Not an NCName, not a Numeric.  What the ... ?
                    throw new PropertyException
                        (propvals[fontsize].getClass().getName() +
                        " found in 'font' "
                        + "expression while looking for font-size");
                }
                // Indicate start of font-family.
                if (size != null) familyStart = fontsize + 1;
                else  // no font-size found
                    // A font-size[/line-height] element is compulsory
                    throw new PropertyException
                        ("Required 'font-size' element not found in 'font'"
                            + " shorthand");
            }
            // Attempt to derive the FontFamilySet
            // The discovered font or font-family name must be prepended to
            // the fontList.  If the font name is a single element, and the
            // fontList is empty, only that single element is passed to the
            // verifyParsing() method of FontFamily.  Otherwise the font
            // name element or elements is formed into a PropertyValueList,
            // and that list is prepended to fontList.
            if (fontList.size() == 0
                                && familyStart == (propvals.length - 1)) {
                fontset = FontFamily.verifyParsing
                                (foTree, propvals[familyStart], IS_NESTED);
            } else {
                // Must develop a list to prepend to fontList
                PropertyValueList tmpList =
                                new PropertyValueList(PropNames.FONT_FAMILY);
                for (int i = familyStart; i < propvals.length; i++)
                    tmpList.add(propvals[i]);
                fontList.addFirst(tmpList);
                // Get a FontFamilySet
                fontset = FontFamily.verifyParsing
                                            (foTree, fontList, IS_NESTED);
            }
            // Only font-style font-variant and font-weight, in any order
            // remain as possibilities at the front of the expression
            for (int i = 0; i < fontsize; i++) {
                PropertyValue pv = null;
                try {
                    pv = FontStyle.verifyParsing
                                            (foTree, propvals[i], IS_NESTED);
                    if (style != null)
                        MessageHandler.log("font: duplicate" +
                        "style overrides previous style");
                    style = pv;
                    continue;
                } catch(PropertyException e) {}

                try {
                    pv = FontVariant.verifyParsing
                                            (foTree, propvals[i], IS_NESTED);
                    if (variant != null)
                        MessageHandler.log("font: duplicate" +
                        "variant overrides previous variant");
                    variant = pv;
                    continue;
                } catch(PropertyException e) {}

                try {
                    pv = FontWeight.verifyParsing
                                            (foTree, propvals[i], IS_NESTED);
                    if (weight != null)
                        MessageHandler.log("font: duplicate" +
                        "weight overrides previous weight");
                    weight = pv;
                    continue;
                } catch(PropertyException e) {}
                throw new PropertyException
                    ("Unrecognized value; looking for style, "
                    + "variant or color in font: "
                    + propvals[i].getClass().getName());
            }
            // Construct the shorthand expansion list from the discovered
            // values of individual components

            newlist =
                PropertySets.initialValueExpansion(foTree, PropNames.FONT);
            // For each discovered property, override the value in the
            // initial value expansion.
            ListIterator expansions = newlist.listIterator();
            while (expansions.hasNext()) {
                PropertyValue prop = (PropertyValue)expansions.next();
                switch (prop.getProperty()) {
                case PropNames.FONT_STYLE:
                    if (style != null) expansions.set(style);
                    break;
                case PropNames.FONT_VARIANT:
                    if (variant != null) expansions.set(variant);
                    break;
                case PropNames.FONT_WEIGHT:
                    if (weight != null) expansions.set(weight);
                    break;
                case PropNames.FONT_SIZE:
                    if (size != null) expansions.set(size);
                    break;
                case PropNames.FONT_FAMILY:
                    if (fontset != null) expansions.set(fontset);
                    break;
                case PropNames.LINE_HEIGHT:
                    if (height != null) expansions.set(height);
                    break;
                }
            }

            return newlist;
        }
    }

    public static class FontFamily extends Properties {
        public static final int dataTypes = COMPLEX | FONTSET;
        public static final int traitMapping = FONT_SELECTION;
        public static final int initialValueType = NOTYPE_IT;
        public static final int SERIF = 1;
        public static final int SANS_SERIF = 2;
        public static final int CURSIVE = 3;
        public static final int FANTASY = 4;
        public static final int MONOSPACE = 5;
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"serif"
            ,"sans-serif"
            ,"cursive"
            ,"fantasy"
            ,"monospace"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }

        public static PropertyValue verifyParsing
                                        (FOTree foTree, PropertyValue value)
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

            return verifyParsing(foTree, value, NOT_NESTED);
        }

        public static PropertyValue verifyParsing
                        (FOTree foTree, PropertyValue value, boolean nested)
                        throws PropertyException
        {
            int property = value.getProperty();
            // First, check that we have a list
            if ( ! (value instanceof PropertyValueList)) {
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
                if (pvalue instanceof PropertyValueList) {
                    // build a font name according to
                    // 7.8.2 "font-family" <family-name>
                    Iterator font = ((PropertyValueList)pvalue).iterator();
                    while (font.hasNext())
                        name = name + (name.length() == 0 ? "" : " ")
                                + ((StringType)(font.next())).getString();
                }
                else if (pvalue instanceof StringType)
                            name = ((StringType)pvalue).getString();
                else throw new PropertyException
                        ("Invalid " + value.getClass().getName() +
                            " PropertyValue for font-family");
                strings[i++] = name;
            }
            // Construct the FontFamilySet property value
            return new FontFamilySet(property, strings);
        }

    }

    public static class FontSelectionStrategy extends Properties {
        public static final int dataTypes = AUTO | ENUM | INHERIT;
        public static final int traitMapping = FONT_SELECTION;
        public static final int initialValueType = AUTO_IT;
        public static final int CHARACTER_BY_CHARACTER = 1;
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"character-by-character"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class FontSize extends Properties {
        public static final int dataTypes =
                            PERCENTAGE | LENGTH | MAPPED_NUMERIC | INHERIT;
        public static final int traitMapping = FORMATTING| RENDERING;
        public static final int initialValueType = LENGTH_IT;
        public static final int XX_SMALL = 1;
        public static final int X_SMALL = 2;
        public static final int SMALL = 3;
        public static final int MEDIUM = 4;
        public static final int LARGE = 5;
        public static final int X_LARGE = 6;
        public static final int XX_LARGE = 7;
        public static final int LARGER = 8;
        public static final int SMALLER = 9;

        // N.B. This foundational value MUST be an absolute length
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return  Length.makeLength(PropNames.FONT_SIZE, 12d, Length.PT);
        }

        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"xx-small"
            ,"x-small"
            ,"small"
            ,"medium"
            ,"large"
            ,"x-large"
            ,"xx-large"
            ,"larger"
            ,"smaller"
        };

        public static Numeric[] getMappedNumArray()
            throws PropertyException
        {
            int property = PropNames.FONT_SIZE;
            Numeric[] numarray = new Numeric[10];
            numarray[0] = null;
            numarray[1] =
                Length.makeLength(property, 7d, Length.PT); // xx-small
            numarray[2] =
                Length.makeLength(property, 8.3d, Length.PT); // x-small
            numarray[3] =
                Length.makeLength(property, 10d, Length.PT); // small
            numarray[4] =
                Length.makeLength(property, 12d, Length.PT); // medium
            numarray[5] =
                Length.makeLength(property, 14.4d, Length.PT); // large
            numarray[6] =
                Length.makeLength(property, 17.3d, Length.PT); // x-large
            numarray[7] =
                Length.makeLength(property, 20.7d, Length.PT); // xx-large
            numarray[8] = Ems.makeEms(property, 1.2d); // larger
            numarray[9] = Ems.makeEms(property, 0.83d); // smaller
            return numarray;
        }

        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }

    }

    public static class FontSizeAdjust extends Properties {
        public static final int dataTypes = NUMBER | NONE | INHERIT;
        public static final int traitMapping = FONT_SELECTION;
        public static final int initialValueType = NONE_IT;
        public static final int inherited = COMPUTED;
    }

    public static class FontStretch extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = FONT_SELECTION;
        public static final int initialValueType = ENUM_IT;
        public static final int NORMAL = 1;
        public static final int WIDER = 2;
        public static final int NARROWER = 3;
        public static final int ULTRA_CONDENSED = 4;
        public static final int EXTRA_CONDENSED = 5;
        public static final int CONDENSED = 6;
        public static final int SEMI_CONDENSED = 7;
        public static final int SEMI_EXPANDED = 8;
        public static final int EXPANDED = 9;
        public static final int EXTRA_EXPANDED = 10;
        public static final int ULTRA_EXPANDED = 11;

        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.FONT_STRETCH, NORMAL);
        }

        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"normal"
            ,"wider"
            ,"narrower"
            ,"ultra-condensed"
            ,"extra-condensed"
            ,"condensed"
            ,"semi-condensed"
            ,"semi-expanded"
            ,"expanded"
            ,"extra-expanded"
            ,"ultra-expanded"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }

    public static class FontStyle extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = FONT_SELECTION;
        public static final int initialValueType = ENUM_IT;
        public static final int NORMAL = 1;
        public static final int ITALIC = 2;
        public static final int OBLIQUE = 3;
        public static final int BACKSLANT = 4;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.FONT_STYLE, NORMAL);
        }

        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"normal"
            ,"italic"
            ,"oblique"
            ,"backslant"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class FontVariant extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = FONT_SELECTION;
        public static final int initialValueType = ENUM_IT;
        public static final int NORMAL = 1;
        public static final int SMALL_CAPS = 2;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.FONT_VARIANT, NORMAL);
        }
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"normal"
            ,"small-caps"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class FontWeight extends Properties {
        public static final int dataTypes = INTEGER | ENUM | INHERIT;
        public static final int traitMapping = FONT_SELECTION;
        public static final int initialValueType = INTEGER_IT;
        public static final int NORMAL = 1;
        public static final int BOLD = 2;
        public static final int BOLDER = 3;
        public static final int LIGHTER = 4;

        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new IntegerType(PropNames.FONT_WEIGHT, 400);
        }

        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"normal"
            ,"bold"
            ,"bolder"
            ,"lighter"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;

        /*
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @return <tt>PropertyValue</tt> the verified value
         */
        public static PropertyValue verifyParsing
                                        (FOTree foTree, PropertyValue value)
                        throws PropertyException
        {
            return verifyParsing(foTree, value, NOT_NESTED);
        }

        /**
         * Do the work for the two argument verifyParsing method.
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @param nested <tt>boolean</tt> indicating whether this method is
         * called normally (false), or as part of another <i>verifyParsing</i>
         * method.
         * @return <tt>PropertyValue</tt> the verified value
         * @see #verifyParsing(FOTree,PropertyValue)
         */
        public static PropertyValue verifyParsing
                        (FOTree foTree, PropertyValue value, boolean nested)
                        throws PropertyException
        {
            // Override the shadowed method to ensure that Integer values
            // are limited to the valid numbers
            PropertyValue fw = Properties.verifyParsing(foTree, value, nested);
            // If the result is an IntegerType, restrict the values
            if (fw instanceof IntegerType) {
                int weight = ((IntegerType)fw).getInt();
                if (weight % 100 != 0 || weight < 100 || weight > 900)
                    throw new PropertyException
                        ("Invalid integer font-weight value: " + weight);
            }
            return fw;
        }
    }

    public static class ForcePageCount extends Properties {
        public static final int dataTypes = AUTO | ENUM | INHERIT;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = AUTO_IT;
        public static final int EVEN = 1;
        public static final int ODD = 2;
        public static final int END_ON_EVEN = 3;
        public static final int END_ON_ODD = 4;
        public static final int NO_FORCE = 5;

        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"even"
            ,"odd"
            ,"end-on-even"
            ,"end-on-odd"
            ,"no-force"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }

    public static class Format extends Properties {
        public static final int dataTypes = STRING;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LITERAL_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Literal(PropNames.FORMAT, "1");
        }
        public static final int inherited = NO;
    }

    public static class GlyphOrientationHorizontal extends Properties {
        public static final int dataTypes = ANGLE | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ANGLE_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Angle(PropNames.GLYPH_ORIENTATION_HORIZONTAL,
                                        Angle.DEG, 0d);
        }
        public static final int inherited = COMPUTED;
    }

    public static class GlyphOrientationVertical extends Properties {
        public static final int dataTypes = ANGLE | AUTO | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = COMPUTED;
    }

    public static class GroupingSeparator extends Properties {
        public static final int dataTypes = CHARACTER_T;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class GroupingSize extends Properties {
        public static final int dataTypes = NUMBER;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class Height extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | AUTO | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class Hyphenate extends Properties {
        public static final int dataTypes = BOOL | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = BOOL_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Bool(PropNames.HYPHENATE, false);
        }
        public static final int inherited = COMPUTED;
    }

    public static class HyphenationCharacter extends Properties {
        public static final int dataTypes = CHARACTER_T | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LITERAL_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Literal (PropNames.HYPHENATION_CHARACTER, "\u2010");
        }
        public static final int inherited = COMPUTED;
    }

    public static class HyphenationKeep extends Properties {
        public static final int dataTypes = AUTO | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int COLUMN = 1;
        public static final int PAGE = 2;
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"column"
            ,"page"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class HyphenationLadderCount extends Properties {
        public static final int dataTypes = NUMBER | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int NO_LIMIT = 1;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.HYPHENATION_LADDER_COUNT, NO_LIMIT);
        }
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"no-limit"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class HyphenationPushCharacterCount extends Properties {
        public static final int dataTypes = NUMBER | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NUMBER_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Numeric (PropNames.HYPHENATION_PUSH_CHARACTER_COUNT, 2d);
        }

        public static final int inherited = COMPUTED;
    }

    public static class HyphenationRemainCharacterCount extends Properties {
        public static final int dataTypes = NUMBER | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NUMBER_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Numeric (PropNames.HYPHENATION_REMAIN_CHARACTER_COUNT, 2d);
        }

        public static final int inherited = COMPUTED;
    }

    public static class Id extends Properties {
        public static final int dataTypes = ID_T;
        public static final int traitMapping = REFERENCE;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class IndicateDestination extends Properties {
        public static final int dataTypes = BOOL;
        public static final int traitMapping = ACTION;
        public static final int initialValueType = BOOL_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Bool(PropNames.INDICATE_DESTINATION, false);
        }
        public static final int inherited = NO;
    }

    public static class InitialPageNumber extends Properties {
        public static final int dataTypes = NUMBER | AUTO | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int AUTO_ODD = 1;
        public static final int AUTO_EVEN = 2;
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"auto-odd"
            ,"auto-even"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class InlineProgressionDimension extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | AUTO | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class InlineProgressionDimensionMinimum extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | AUTO;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = COMPOUND;
    }

    public static class InlineProgressionDimensionOptimum extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | AUTO;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = COMPOUND;
    }

    public static class InlineProgressionDimensionMaximum extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | AUTO;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = COMPOUND;
    }

    public static class InternalDestination extends Properties {
        public static final int dataTypes = STRING | IDREF;
        public static final int traitMapping = ACTION;
        public static final int initialValueType = LITERAL_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Literal (PropNames.INTERNAL_DESTINATION, "");
        }
        public static final int inherited = NO;
    }

    public static class IntrusionDisplace extends Properties {
        public static final int dataTypes = AUTO | ENUM | NONE | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int LINE = 1;
        public static final int INDENT = 2;
        public static final int BLOCK = 3;
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"line"
            ,"indent"
            ,"block"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class KeepTogether extends Properties {
        public static final int dataTypes = AUTO | ENUM | INTEGER | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = COMPUTED;
    }

    public static class KeepTogetherWithinLine extends Properties {
        public static final int dataTypes = AUTO | ENUM | INTEGER | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class KeepTogetherWithinColumn extends Properties {
        public static final int dataTypes = AUTO | ENUM | INTEGER | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class KeepTogetherWithinPage extends Properties {
        public static final int dataTypes = AUTO | ENUM | INTEGER | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class KeepWithNext extends Properties {
        public static final int dataTypes = AUTO | ENUM | INTEGER | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class KeepWithNextWithinLine extends Properties {
        public static final int dataTypes = AUTO | ENUM | INTEGER | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class KeepWithNextWithinColumn extends Properties {
        public static final int dataTypes = AUTO | ENUM | INTEGER | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class KeepWithNextWithinPage extends Properties {
        public static final int dataTypes = AUTO | ENUM | INTEGER | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class KeepWithPrevious extends Properties {
        public static final int dataTypes = AUTO | ENUM | INTEGER | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class KeepWithPreviousWithinLine extends Properties {
        public static final int dataTypes = AUTO | ENUM | INTEGER | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class KeepWithPreviousWithinColumn extends Properties {
        public static final int dataTypes = AUTO | ENUM | INTEGER | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class KeepWithPreviousWithinPage extends Properties {
        public static final int dataTypes = AUTO | ENUM | INTEGER | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class Language extends Properties {
        public static final int dataTypes = LANGUAGE_T | NONE | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NONE_IT;
        public static final int inherited = COMPUTED;
    }

    public static class LastLineEndIndent extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.LAST_LINE_END_INDENT, 0.0d, Length.PT);
        }
        public static final int inherited = COMPUTED;
    }

    public static class LeaderAlignment extends Properties {
        public static final int dataTypes = ENUM | NONE | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NONE_IT;
        public static final int REFERENCE_AREA = 1;
        public static final int PAGE = 2;
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"reference-area"
            ,"page"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;

    }

    public static class LeaderLength extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = COMPUTED;
    }

    public static class LeaderLengthMinimum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.LEADER_LENGTH_MINIMUM, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class LeaderLengthOptimum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.LEADER_LENGTH_OPTIMUM, 12.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class LeaderLengthMaximum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = PERCENTAGE_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Percentage.makePercentage (PropNames.LEADER_LENGTH_MAXIMUM, 100.0d);
        }
        public static final int inherited = COMPOUND;
    }

    public static class LeaderPattern extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int SPACE = 1;
        public static final int RULE = 2;
        public static final int DOTS = 3;
        public static final int USE_CONTENT = 4;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.LEADER_PATTERN, SPACE);
        }
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"space"
            ,"rule"
            ,"dots"
            ,"use-content"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;

    }

    public static class LeaderPatternWidth extends Properties {
        public static final int dataTypes = LENGTH | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int USE_FONT_METRICS = 1;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.LEADER_PATTERN_WIDTH, USE_FONT_METRICS);
        }
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"use-font-metrics"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class Left extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | AUTO | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class LetterSpacing extends Properties {
        public static final int dataTypes = LENGTH | ENUM | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = ENUM_IT;
        public static final int NORMAL = 1;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.LETTER_SPACING, NORMAL);
        }
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"normal"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class LetterValue extends Properties {
        public static final int dataTypes = AUTO | ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int ALPHABETIC = 1;
        public static final int TRADITIONAL = 2;
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"alphabetic"
            ,"traditional"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class LinefeedTreatment extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int IGNORE = 1;
        public static final int PRESERVE = 2;
        public static final int TREAT_AS_SPACE = 3;
        public static final int TREAT_AS_ZERO_WIDTH_SPACE = 4;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.LINEFEED_TREATMENT, TREAT_AS_SPACE);
        }
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"ignore"
            ,"preserve"
            ,"treat-as-space"
            ,"treat-as-zero-width-space"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class LineHeight extends Properties {
        public static final int dataTypes =
                    PERCENTAGE | LENGTH | NUMBER | MAPPED_NUMERIC | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int NORMAL = 1;
        public static final int inherited = VALUE_SPECIFIC;

        private static final String[] rwEnums = {
            null
            ,"normal"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;

        public static Numeric[] getMappedNumArray()
            throws PropertyException
        {
            int property = PropNames.LINE_HEIGHT;
            Numeric[] numarray = new Numeric[2];
            numarray[0] = null;
            numarray[1] = Ems.makeEms(property, 1.2d); // normal
            return numarray;
        }

    }

    public static class LineHeightMinimum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Ems.makeEms(PropNames.LINE_HEIGHT, 1.2d);
        }
        public static final int inherited = COMPOUND;
    }

    public static class LineHeightOptimum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Ems.makeEms(PropNames.LINE_HEIGHT, 1.2d);
        }
        public static final int inherited = COMPOUND;
    }

    public static class LineHeightMaximum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Ems.makeEms(PropNames.LINE_HEIGHT, 1.2d);
        }
        public static final int inherited = COMPOUND;
    }

    public static class LineHeightConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.SPACE_AFTER_CONDITIONALITY, Conditionality.DISCARD);
        }
        public static final int inherited = COMPOUND;

        public static final ROStringArray enums = Conditionality.enums;
        public static final ROStringArray enumValues
                                            = Conditionality.enumValues;
    }

    public static class LineHeightPrecedence extends Properties {
        public static final int dataTypes = INTEGER | ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = INTEGER_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new IntegerType(PropNames.LINE_HEIGHT_PRECEDENCE, 0);
        }

        public static final int inherited = COMPOUND;

        public static final ROStringArray enums = PrecedenceCommon.enums;
        public static final ROStringArray enumValues
                                            = PrecedenceCommon.enumValues;
    }

    public static class LineHeightShiftAdjustment extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int CONSIDER_SHIFTS = 1;
        public static final int DISREGARD_SHIFTS = 2;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.LINE_HEIGHT_SHIFT_ADJUSTMENT, CONSIDER_SHIFTS);
        }
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"consider-shifts"
            ,"disregard-shifts"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class LineStackingStrategy extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int LINE_HEIGHT = 1;
        public static final int FONT_HEIGHT = 2;
        public static final int MAX_HEIGHT = 3;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.LINE_STACKING_STRATEGY, LINE_HEIGHT);
        }
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"line-height"
            ,"font-height"
            ,"max-height"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class Margin extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;

        /**
         * 'value' is a PropertyValueList or an individual PropertyValue.
         *
         * <p>If 'value' is an individual PropertyValue, it must contain
         * either
         *   a FromParent value,
         *   a FromNearestSpecified value,
         *   an Inherit value,
         *   an Auto value,
         *   a Numeric value which is a distance, rather than a number.
         *
         * <p>If 'value' is a PropertyValueList, it contains a list of
         * 2 to 4 length, percentage or auto values representing margin
         * dimensions.
         *
         * <p>The value(s) provided, if valid, are converted into a list
         * containing the expansion of the shorthand.
         * The first element is a value for margin-top,
         * the second element is a value for margin-right,
         * the third element is a value for margin-bottom,
         * the fourth element is a value for margin-left.
         *
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @return <tt>PropertyValue</tt> the verified value
         */
        public static PropertyValue verifyParsing
                                        (FOTree foTree, PropertyValue value)
                    throws PropertyException
        {
            if ( ! (value instanceof PropertyValueList)) {
                if (value instanceof Inherit
                    || value instanceof FromParent
                    || value instanceof FromNearestSpecified
                    )
                    return PropertySets.expandAndCopySHand(value);
                return PropertySets.expandAndCopySHand
                                        (Properties.autoOrDistance(value));
            } else {
                PropertyValueList list =
                                spaceSeparatedList((PropertyValueList)value);
                PropertyValue top, left, bottom, right;
                int count = list.size();
                if (count < 2 || count > 4)
                    throw new PropertyException
                        ("margin list contains " + count + " items");

                Iterator margins = list.iterator();

                // There must be at least two
                top = Properties.autoOrDistance
                    ((PropertyValue)(margins.next()), PropNames.MARGIN_TOP);
                left = Properties.autoOrDistance
                    ((PropertyValue)(margins.next()), PropNames.MARGIN_LEFT);
                try {
                    bottom = (PropertyValue)(top.clone());
                    bottom.setProperty(PropNames.MARGIN_BOTTOM);
                    right = (PropertyValue)(left.clone());
                    right.setProperty(PropNames.MARGIN_RIGHT);
                } catch (CloneNotSupportedException cnse) {
                    throw new PropertyException
                                (cnse.getMessage());
                }

                if (margins.hasNext())
                    bottom = Properties.autoOrDistance(
                                            (PropertyValue)(margins.next()),
                                                    PropNames.MARGIN_BOTTOM);
                if (margins.hasNext())
                    right = Properties.autoOrDistance(
                                            (PropertyValue)(margins.next()),
                                                    PropNames.MARGIN_RIGHT);

                list = new PropertyValueList(PropNames.MARGIN);
                list.add(top);
                list.add(right);
                list.add(bottom);
                list.add(left);
                return list;
            }
        }
    }

    public static class MarginBottom extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | AUTO | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength(PropNames.MARGIN_BOTTOM, 0.0d, Length.PT);
        }
        public static final int inherited = NO;
    }

    public static class MarginLeft extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | AUTO | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.MARGIN_LEFT, 0.0d, Length.PT);
        }
        public static final int inherited = NO;
    }

    public static class MarginRight extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | AUTO | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.MARGIN_RIGHT, 0.0d, Length.PT);
        }
        public static final int inherited = NO;
    }

    public static class MarginTop extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | AUTO | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.MARGIN_TOP, 0.0d, Length.PT);
        }
        public static final int inherited = NO;
    }

    public static class MarkerClassName extends Properties {
        public static final int dataTypes = NAME;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NAME_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new NCName(PropNames.MARKER_CLASS_NAME, "");
        }
        public static final int inherited = NO;
    }

    public static class MasterName extends Properties {
        public static final int dataTypes = NAME;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = NAME_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new NCName(PropNames.MASTER_NAME, "");
        }
        public static final int inherited = NO;
    }

    public static class MasterReference extends Properties {
        public static final int dataTypes = NAME;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = NAME_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new NCName(PropNames.MASTER_REFERENCE, "");
        }
        public static final int inherited = NO;
    }

    public static class MaxHeight extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | NONE | INHERIT;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.MAX_HEIGHT, 0.0d, Length.PT);
        }
        public static final int inherited = NO;
    }

    public static class MaximumRepeats extends Properties {
        public static final int dataTypes = NUMBER | ENUM | INHERIT;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = ENUM_IT;
        public static final int NO_LIMIT = 1;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.MAXIMUM_REPEATS, NO_LIMIT);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"no-limit"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;

    }

    public static class MaxWidth extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | NONE | INHERIT;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NONE_IT;
        public static final int inherited = NO;
    }

    public static class MediaUsage extends Properties {
        public static final int dataTypes = AUTO | ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int PAGINATE = 1;
        public static final int BOUNDED_IN_ONE_DIMENSION = 2;
        public static final int UNBOUNDED = 3;
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"paginate"
            ,"bounded-in-one-dimension"
            ,"unbounded"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class MinHeight extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.MIN_HEIGHT, 0.0d, Length.PT);
        }
        public static final int inherited = NO;
    }

    public static class MinWidth extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class NumberColumnsRepeated extends Properties {
        public static final int dataTypes = NUMBER;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = NUMBER_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Numeric (PropNames.NUMBER_COLUMNS_REPEATED, 1d);
        }

        public static final int inherited = NO;
    }

    public static class NumberColumnsSpanned extends Properties {
        public static final int dataTypes = NUMBER;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NUMBER_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Numeric (PropNames.NUMBER_COLUMNS_SPANNED, 1d);
        }

        public static final int inherited = NO;
    }

    public static class NumberRowsSpanned extends Properties {
        public static final int dataTypes = NUMBER;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NUMBER_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Numeric(PropNames.NUMBER_ROWS_SPANNED, 1d);
        }

        public static final int inherited = NO;
    }

    public static class OddOrEven extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = ENUM_IT;
        public static final int ODD = 1;
        public static final int EVEN = 2;
        public static final int ANY = 3;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType(PropNames.ODD_OR_EVEN, ANY);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"odd"
            ,"even"
            ,"any"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;

    }

    public static class Orphans extends Properties {
        public static final int dataTypes = INTEGER | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = INTEGER_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new IntegerType(PropNames.ORPHANS, 2);
        }

        public static final int inherited = COMPUTED;
    }

    public static class Overflow extends Properties {
        public static final int dataTypes = AUTO | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int VISIBLE = 1;
        public static final int HIDDEN = 2;
        public static final int SCROLL = 3;
        public static final int ERROR_IF_OVERFLOW = 4;
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"visible"
            ,"hidden"
            ,"scroll"
            ,"error-if-overflow"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;

    }

    public static class Padding extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class PaddingAfter extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.PADDING_AFTER, 0.0d, Length.PT);
        }
        public static final int inherited = NO;
    }

    public static class PaddingAfterLength extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.PADDING_AFTER_LENGTH, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class PaddingAfterConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = ENUM_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.PADDING_AFTER_CONDITIONALITY, Conditionality.DISCARD);
        }
        public static final int inherited = COMPOUND;

        public static final ROStringArray enums = Conditionality.enums;
        public static final ROStringArray enumValues
                                            = Conditionality.enumValues;
    }

    public static class PaddingBefore extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.PADDING_BEFORE, 0.0d, Length.PT);
        }
        public static final int inherited = NO;
    }

    public static class PaddingBeforeLength extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.PADDING_BEFORE_LENGTH, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class PaddingBeforeConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = ENUM_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.PADDING_BEFORE_CONDITIONALITY, Conditionality.DISCARD);
        }
        public static final int inherited = COMPOUND;

        public static final ROStringArray enums = Conditionality.enums;
        public static final ROStringArray enumValues
                                            = Conditionality.enumValues;
    }

    public static class PaddingBottom extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.PADDING_BOTTOM, 0.0d, Length.PT);
        }
        public static final int inherited = NO;
    }

    public static class PaddingEnd extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.PADDING_END, 0.0d, Length.PT);
        }
        public static final int inherited = NO;
    }

    public static class PaddingEndLength extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.PADDING_END_LENGTH, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class PaddingEndConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = ENUM_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.PADDING_END_CONDITIONALITY, Conditionality.DISCARD);
        }
        public static final int inherited = COMPOUND;

        public static final ROStringArray enums = Conditionality.enums;
        public static final ROStringArray enumValues
                                            = Conditionality.enumValues;
    }

    public static class PaddingLeft extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.PADDING_LEFT, 0.0d, Length.PT);
        }
        public static final int inherited = NO;
    }

    public static class PaddingRight extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.PADDING_RIGHT, 0.0d, Length.PT);
        }
        public static final int inherited = NO;
    }

    public static class PaddingStart extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.PADDING_START, 0.0d, Length.PT);
        }
        public static final int inherited = NO;
    }

    public static class PaddingStartLength extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.PADDING_START_LENGTH, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class PaddingStartConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = ENUM_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.PADDING_START_CONDITIONALITY, Conditionality.DISCARD);
        }
        public static final int inherited = COMPOUND;

        public static final ROStringArray enums = Conditionality.enums;
        public static final ROStringArray enumValues
                                            = Conditionality.enumValues;
    }

    public static class PaddingTop extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.PADDING_TOP, 0.0d, Length.PT);
        }
        public static final int inherited = NO;
    }

    public static class PageBreakAfter extends Properties {
        public static final int dataTypes = SHORTHAND | AUTO | ENUM | INHERIT;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = AUTO_IT;
        public static final int ALWAYS = 1;
        public static final int AVOID = 2;
        public static final int LEFT = 3;
        public static final int RIGHT = 4;
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"always"
            ,"avoid"
            ,"left"
            ,"right"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }
    public static class PageBreakBefore extends Properties {
        public static final int dataTypes = SHORTHAND | AUTO | ENUM | INHERIT;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;

        private static final HashMap rwEnumValues
                                            = PageBreakAfter.rwEnumValues;
    }

    public static class PageBreakInside extends Properties {
        public static final int dataTypes = SHORTHAND | AUTO | ENUM | INHERIT;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = AUTO_IT;
        public static final int AVOID = 1;
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"avoid"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class PageHeight extends Properties {
        public static final int dataTypes = LENGTH | AUTO | ENUM | INHERIT;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = AUTO_IT;
        public static final int INDEFINITE = 1;
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"indefinite"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class PagePosition extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = ENUM_IT;
        public static final int FIRST = 1;
        public static final int LAST = 2;
        public static final int REST = 3;
        public static final int ANY = 4;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.PAGE_POSITION, ANY);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"first"
            ,"last"
            ,"rest"
            ,"any"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class PageWidth extends Properties {
        public static final int dataTypes = LENGTH | ENUM | INHERIT;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;

        public static final ROStringArray enumValues = PageHeight.enumValues;
    }

    public static class Pause extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = NO;
    }

    public static class PauseAfter extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = NO;
    }

    public static class PauseBefore extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = NO;
    }

    public static class Pitch extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = COMPUTED;
    }

    public static class PitchRange extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = COMPUTED;
    }

    public static class PlayDuring extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = NO;
    }

    public static class Position extends Properties {
        public static final int dataTypes = SHORTHAND | ENUM | INHERIT;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = ENUM_IT;
        public static final int STATIC = 1;
        public static final int RELATIVE = 2;
        public static final int ABSOLUTE = 3;
        public static final int FIXED = 4;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType(PropNames.POSITION, STATIC);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"static"
            ,"relative"
            ,"absolute"
            ,"fixed"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class Precedence extends Properties {
        public static final int dataTypes = BOOL | INHERIT;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = BOOL_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Bool(PropNames.PRECEDENCE, false);
        }
        public static final int inherited = NO;
    }

    public static class PrecedenceCommon extends Properties {
        public static final int FORCE = 1;

        private static final String[] rwEnums = {
            null
            ,"force"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class ProvisionalDistanceBetweenStarts extends Properties {
        public static final int dataTypes = LENGTH | INHERIT;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.PROVISIONAL_DISTANCE_BETWEEN_STARTS, 24.0d, Length.PT);
        }
        public static final int inherited = COMPUTED;
    }

    public static class ProvisionalLabelSeparation extends Properties {
        public static final int dataTypes = LENGTH | INHERIT;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.PROVISIONAL_LABEL_SEPARATION, 6.0d, Length.PT);
        }
        public static final int inherited = COMPUTED;
    }

    public static class ReferenceOrientation extends Properties {
        public static final int dataTypes = INTEGER | INHERIT;
        public static final int traitMapping = NEW_TRAIT;
        public static final int initialValueType = INTEGER_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new IntegerType(PropNames.REFERENCE_ORIENTATION, 0);
        }
        public static final int inherited = COMPUTED;

    }

    public static class RefId extends Properties {
        public static final int dataTypes = IDREF | INHERIT;
        public static final int traitMapping = REFERENCE;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class RegionName extends Properties {
        public static final int dataTypes = NAME | ENUM;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = NOTYPE_IT;
        public static final int XSL_REGION_BODY = 1;
        public static final int XSL_REGION_START = 2;
        public static final int XSL_REGION_END = 3;
        public static final int XSL_REGION_BEFORE = 4;
        public static final int XSL_REGION_AFTER = 5;
        public static final int XSL_BEFORE_FLOAT_SEPARATOR = 6;
        public static final int XSL_FOOTNOTE_SEPARATOR = 7;
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"xsl-region-body"
            ,"xsl-region-start"
            ,"xsl-region-end"
            ,"xsl-region-before"
            ,"xsl-region-after"
            ,"xsl-before-float-separator"
            ,"xsl-footnote-separator"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }

    public static class RelativeAlign extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int BEFORE = 1;
        public static final int BASELINE = 2;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.RELATIVE_ALIGN, BEFORE);
        }
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"before"
            ,"baseline"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class RelativePosition extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = NEW_TRAIT;
        public static final int initialValueType = ENUM_IT;
        public static final int STATIC = 1;
        public static final int RELATIVE = 2;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.RELATIVE_POSITION, STATIC);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"static"
            ,"relative"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class RenderingIntent extends Properties {
        public static final int dataTypes = AUTO | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int PERCEPTUAL = 1;
        public static final int RELATIVE_COLORIMETRIC = 2;
        public static final int SATURATION = 3;
        public static final int ABSOLUTE_COLORIMETRIC = 4;
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"perceptual"
            ,"relative-colorimetric"
            ,"saturation"
            ,"absolute-colorimetric"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }

    public static class RetrieveBoundary extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int PAGE = 1;
        public static final int PAGE_SEQUENCE = 2;
        public static final int DOCUMENT = 3;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.RETRIEVE_BOUNDARY, PAGE_SEQUENCE);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"page"
            ,"page-sequence"
            ,"document"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class RetrieveClassName extends Properties {
        public static final int dataTypes = NAME;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final String initialValue = "";
        public static final int inherited = NO;
    }

    public static class RetrievePosition extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int FIRST_STARTING_WITHIN_PAGE = 1;
        public static final int FIRST_INCLUDING_CARRYOVER = 2;
        public static final int LAST_STARTING_WITHIN_PAGE = 3;
        public static final int LAST_ENDING_WITHIN_PAGE = 4;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.RETRIEVE_POSITION, FIRST_STARTING_WITHIN_PAGE);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"first-starting-within-page"
            ,"first-including-carryover"
            ,"last-starting-within-page"
            ,"last-ending-within-page"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class Richness extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = COMPUTED;
    }

    public static class Right extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | AUTO | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class Role extends Properties {
        public static final int dataTypes =
                                STRING | URI_SPECIFICATION | NONE | INHERIT;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = NONE_IT;
        public static final int inherited = NO;
    }

    public static class RuleStyle extends Properties {
        public static final int dataTypes = ENUM | NONE | INHERIT;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = ENUM_IT;
        public static final int DOTTED = 1;
        public static final int DASHED = 2;
        public static final int SOLID = 3;
        public static final int DOUBLE = 4;
        public static final int GROOVE = 5;
        public static final int RIDGE = 6;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType(PropNames.RULE_STYLE, SOLID);
        }
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"dotted"
            ,"dashed"
            ,"solid"
            ,"double"
            ,"groove"
            ,"ridge"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }

    public static class RuleThickness extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.RULE_THICKNESS, 1.0d, Length.PT);
        }
        public static final int inherited = COMPUTED;
    }

    public static class Scaling extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int UNIFORM = 1;
        public static final int NON_UNIFORM = 2;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType(PropNames.SCALING, UNIFORM);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"uniform"
            ,"non-uniform"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class ScalingMethod extends Properties {
        public static final int dataTypes = AUTO | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int INTEGER_PIXELS = 1;
        public static final int RESAMPLE_ANY_METHOD = 2;
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"integer-pixels"
            ,"resample-any-method"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class ScoreSpaces extends Properties {
        public static final int dataTypes = BOOL | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = BOOL_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Bool(PropNames.SCORE_SPACES, true);
        }
        public static final int inherited = COMPUTED;
    }

    public static class Script extends Properties {
        public static final int dataTypes = SCRIPT_T | AUTO | NONE | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = COMPUTED;
    }

    public static class ShowDestination extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = ACTION;
        public static final int initialValueType = ENUM_IT;
        public static final int REPLACE = 1;
        public static final int NEW = 2;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.SHOW_DESTINATION, REPLACE);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"replace"
            ,"new"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class Size extends Properties {
        public static final int dataTypes = SHORTHAND | AUTO | ENUM | INHERIT;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = AUTO_IT;
        public static final int LANDSCAPE = 1;
        public static final int PORTRAIT = 2;
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"landscape"
            ,"portrait"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class SourceDocument extends Properties {
        public static final int dataTypes = COMPLEX | NONE | INHERIT;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = NONE_IT;
        public static final int inherited = NO;

        /*
         * @param foTree the <tt>FOTree</tt> being built
         * @param value <tt>PropertyValue</tt> returned by the parser
         * @return <tt>PropertyValue</tt> the verified value
         */
        public static PropertyValue verifyParsing
                                        (FOTree foTree, PropertyValue list)
                        throws PropertyException
        {
            PropertyValue value =
                        Properties.verifyParsing(foTree, list, IS_NESTED);
            // Confirm that the list contains only UriType elements
            Iterator iter = ((PropertyValueList)value).iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                if ( ! (obj instanceof UriType))
                    throw new PropertyException
                        ("source-document requires a list of uris");
            }
            return value;
        }
    }

    public static class SpaceAfter extends Properties {
        public static final int dataTypes = LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class SpaceAfterMinimum extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.SPACE_AFTER_MINIMUM, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceAfterOptimum extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.SPACE_AFTER_OPTIMUM, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceAfterMaximum extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.SPACE_AFTER_MAXIMUM, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceAfterConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.SPACE_AFTER_CONDITIONALITY, Conditionality.DISCARD);
        }
        public static final int inherited = COMPOUND;

        public static final ROStringArray enums = Conditionality.enums;
        public static final ROStringArray enumValues
                                            = Conditionality.enumValues;
    }

    public static class SpaceAfterPrecedence extends Properties {
        public static final int dataTypes = INTEGER | ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = INTEGER_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new IntegerType(PropNames.SPACE_AFTER_PRECEDENCE, 0);
        }

        public static final int inherited = COMPOUND;

        public static final ROStringArray enums = PrecedenceCommon.enums;
        public static final ROStringArray enumValues
                                            = PrecedenceCommon.enumValues;
    }

    public static class SpaceBefore extends Properties {
        public static final int dataTypes = LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class SpaceBeforeMinimum extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.SPACE_BEFORE_MINIMUM, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceBeforeOptimum extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.SPACE_BEFORE_OPTIMUM, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceBeforeMaximum extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.SPACE_BEFORE_MAXIMUM, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceBeforeConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.SPACE_BEFORE_CONDITIONALITY, Conditionality.DISCARD);
        }
        public static final int inherited = COMPOUND;

        public static final ROStringArray enums = Conditionality.enums;
        public static final ROStringArray enumValues
                                            = Conditionality.enumValues;
    }

    public static class SpaceBeforePrecedence extends Properties {
        public static final int dataTypes = INTEGER | ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = INTEGER_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new IntegerType(PropNames.SPACE_BEFORE_PRECEDENCE, 0);
        }

        public static final int inherited = COMPOUND;

        public static final ROStringArray enums = PrecedenceCommon.enums;
        public static final ROStringArray enumValues
                                            = PrecedenceCommon.enumValues;
    }

    public static class SpaceEnd extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class SpaceEndMinimum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.SPACE_END_MINIMUM, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceEndOptimum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.SPACE_END_OPTIMUM, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceEndMaximum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.SPACE_END_MAXIMUM, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceEndConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.SPACE_END_CONDITIONALITY, Conditionality.DISCARD);
        }
        public static final int inherited = COMPOUND;

        public static final ROStringArray enums = Conditionality.enums;
        public static final ROStringArray enumValues
                                            = Conditionality.enumValues;
    }

    public static class SpaceEndPrecedence extends Properties {
        public static final int dataTypes = INTEGER | ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = INTEGER_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new IntegerType(PropNames.SPACE_END_PRECEDENCE, 0);
        }

        public static final int inherited = COMPOUND;

        public static final ROStringArray enums = PrecedenceCommon.enums;
        public static final ROStringArray enumValues
                                            = PrecedenceCommon.enumValues;
    }

    public static class SpaceStart extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class SpaceStartMinimum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.SPACE_START_MINIMUM, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceStartOptimum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.SPACE_START_OPTIMUM, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceStartMaximum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.SPACE_START_MAXIMUM, 0.0d, Length.PT);
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceStartConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.SPACE_START_CONDITIONALITY, Conditionality.DISCARD);
        }
        public static final int inherited = COMPOUND;

        public static final ROStringArray enums = Conditionality.enums;
        public static final ROStringArray enumValues
                                            = Conditionality.enumValues;
    }

    public static class SpaceStartPrecedence extends Properties {
        public static final int dataTypes = INTEGER | ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = INTEGER_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new IntegerType(PropNames.SPACE_START_PRECEDENCE, 0);
        }

        public static final int inherited = COMPOUND;

        public static final ROStringArray enums = PrecedenceCommon.enums;
        public static final ROStringArray enumValues
                                            = PrecedenceCommon.enumValues;
    }

    public static class Span extends Properties {
        public static final int dataTypes = ENUM | NONE | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NONE_IT;
        public static final int ALL = 1;
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"all"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class Speak extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = COMPUTED;
    }

    public static class SpeakHeader extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = COMPUTED;
    }

    public static class SpeakNumeral extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = COMPUTED;
    }

    public static class SpeakPunctuation extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = COMPUTED;
    }

    public static class SpeechRate extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = COMPUTED;
    }

    public static class Src extends Properties {
        public static final int dataTypes = URI_SPECIFICATION | INHERIT;
        public static final int traitMapping = REFERENCE;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class StartIndent extends Properties {
        public static final int dataTypes = LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.START_INDENT, 0.0d, Length.PT);
        }
        public static final int inherited = COMPUTED;
    }

    public static class StartingState extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = ACTION;
        public static final int initialValueType = ENUM_IT;
        public static final int SHOW = 1;
        public static final int HIDE = 2;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.STARTING_STATE, SHOW);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"show"
            ,"hide"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class StartsRow extends Properties {
        public static final int dataTypes = BOOL;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = BOOL_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Bool(PropNames.STARTS_ROW, false);
        }
        public static final int inherited = NO;
    }

    public static class Stress extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = COMPUTED;
    }

    public static class SuppressAtLineBreak extends Properties {
        public static final int dataTypes = AUTO | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int SUPPRESS = 1;
        public static final int RETAIN = 2;
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"suppress"
            ,"retain"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class SwitchTo extends Properties {
        public static final int dataTypes = COMPLEX;
        public static final int traitMapping = ACTION;
        public static final int initialValueType = ENUM_IT;
        public static final int XSL_PRECEDING = 1;
        public static final int XSL_FOLLOWING = 2;
        public static final int XSL_ANY = 3;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.SWITCH_TO, XSL_ANY);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"xsl-preceding"
            ,"xsl-following"
            ,"xsl-any"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;

        public static PropertyValue verifyParsing
                (FOTree foTree, PropertyValue list)
                        throws PropertyException
        {
            // Check for the enumeration.  Look for a list of NCNames.
            // N.B. it may be a possible to perform further checks on the
            // validity of the NCNames - do they match multi-case case names.
            if ( ! (list instanceof PropertyValueList))
                return Properties.verifyParsing(foTree, list);

            PropertyValueList ssList =
                                spaceSeparatedList((PropertyValueList)list);
            Iterator iter = ssList.iterator();
            while (iter.hasNext()) {
                Object value = iter.next();
                if ( ! (value instanceof NCName))
                    throw new PropertyException
                        ("switch-to requires a list of NCNames");
            }
            return list;
        }
    }

    public static class TableLayout extends Properties {
        public static final int dataTypes = AUTO | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int FIXED = 1;
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"fixed"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class TableOmitFooterAtBreak extends Properties {
        public static final int dataTypes = BOOL;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = BOOL_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Bool (PropNames.TABLE_OMIT_FOOTER_AT_BREAK, false);
        }
        public static final int inherited = NO;
    }

    public static class TableOmitHeaderAtBreak extends Properties {
        public static final int dataTypes = BOOL;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = BOOL_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Bool (PropNames.TABLE_OMIT_HEADER_AT_BREAK, false);
        }
        public static final int inherited = NO;
    }

    public static class TargetPresentationContext extends Properties {
        public static final int dataTypes = URI_SPECIFICATION | ENUM;
        public static final int traitMapping = ACTION;
        public static final int initialValueType = ENUM_IT;
        public static final int USE_TARGET_PROCESSING_CONTEXT = 1;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.TARGET_PRESENTATION_CONTEXT, USE_TARGET_PROCESSING_CONTEXT);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"use-target-processing-context"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class TargetProcessingContext extends Properties {
        public static final int dataTypes = URI_SPECIFICATION | ENUM;
        public static final int traitMapping = ACTION;
        public static final int initialValueType = ENUM_IT;
        public static final int DOCUMENT_ROOT = 1;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.TARGET_PROCESSING_CONTEXT, DOCUMENT_ROOT);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"document-root"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class TargetStylesheet extends Properties {
        public static final int dataTypes = URI_SPECIFICATION | ENUM;
        public static final int traitMapping = ACTION;
        public static final int initialValueType = ENUM_IT;
        public static final int USE_NORMAL_STYLESHEET = 1;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.TARGET_STYLESHEET, USE_NORMAL_STYLESHEET);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"use-normal-stylesheet"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class TextAlign extends Properties {
        public static final int dataTypes = STRING | ENUM | INHERIT;
        public static final int traitMapping = VALUE_CHANGE;
        public static final int initialValueType = ENUM_IT;
        public static final int START = 1;
        public static final int CENTER = 2;
        public static final int END = 3;
        public static final int JUSTIFY = 4;
        public static final int INSIDE = 5;
        public static final int OUTSIDE = 6;
        public static final int LEFT = 7;
        public static final int RIGHT = 8;

        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType(PropNames.TEXT_ALIGN, START);
        }

        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"start"
            ,"center"
            ,"end"
            ,"justify"
            ,"inside"
            ,"outside"
            ,"left"
            ,"right"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }

    public static class TextAlignLast extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = VALUE_CHANGE;
        public static final int initialValueType = ENUM_IT;
        public static final int RELATIVE = 1;
        public static final int START = 2;
        public static final int CENTER = 3;
        public static final int END = 4;
        public static final int JUSTIFY = 5;
        public static final int INSIDE = 6;
        public static final int OUTSIDE = 7;
        public static final int LEFT = 8;
        public static final int RIGHT = 9;

        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.TEXT_ALIGN_LAST, RELATIVE);
        }

        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"relative"
            ,"start"
            ,"center"
            ,"end"
            ,"justify"
            ,"inside"
            ,"outside"
            ,"left"
            ,"right"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }

    public static class TextAltitude extends Properties {
        public static final int dataTypes = LENGTH | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int USE_FONT_METRICS = 1;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.TEXT_ALTITUDE, USE_FONT_METRICS);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"use-font-metrics"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class TextDecoration extends Properties {
        public static final int dataTypes = COMPLEX | NONE | INHERIT;
        public static final int traitMapping = NEW_TRAIT;
        public static final int initialValueType = TEXT_DECORATION_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new TextDecorations (PropNames.TEXT_DECORATION, NO_DECORATION);
        }
        public static final int inherited = NO;

        /**
         * Text decoration constant
         */
        public static final byte
          NO_DECORATION = 0
             ,UNDERLINE = 1
              ,OVERLINE = 2
          ,LINE_THROUGH = 4
                 ,BLINK = 8
                        ;

        public static final ROStringArray alternatives =
            new ROStringArray(new String[] {
                                    null
                                    ,"underline"
                                    ,"overline"
                                    ,"line-through"
                                    ,"blink"
                                });

        public static final ROIntArray decorations =
            new ROIntArray(new int[] {
                                    NO_DECORATION
                                    ,UNDERLINE
                                    ,OVERLINE
                                    ,LINE_THROUGH
                                    ,BLINK
                            });

        public static PropertyValue verifyParsing
                (FOTree foTree, PropertyValue list)
                        throws PropertyException
        {
            byte onMask = NO_DECORATION;
            byte offMask = NO_DECORATION;
            Iterator iter;
            PropertyValueList ssList = null;
            LinkedList strings = new LinkedList();
            if ( ! (list instanceof PropertyValueList)) {
                if ( ! (list instanceof NCName))
                    throw new PropertyException
                        ("text-decoration require list of NCNames");
                strings.add(((NCName)list).getNCName());
            } else { // list is a PropertyValueList
                ssList = spaceSeparatedList((PropertyValueList)list);
                iter = ((PropertyValueList)ssList).iterator();
                while (iter.hasNext()) {
                    Object value = iter.next();
                    if ( ! (value instanceof NCName))
                        throw new PropertyException
                            ("text-decoration requires a list of NCNames");
                    strings.add(((NCName)value).getNCName());
                }
            }
            iter = strings.iterator();
            while (iter.hasNext()) {
                int i;
                String str, str2;
                boolean negate;
                negate = false;
                str = (String)iter.next();
                str2 = str;
                if (str.indexOf("no-") == 0) {
                    str2 = str.substring(3);
                    negate = true;
                }
                try {
                    i = PropertyConsts.enumValueToIndex(str2, alternatives);
                } catch (PropertyException e) {
                    throw new PropertyException
                                    ("text-decoration: unknown value " + str);
                }
                if (negate) offMask |= (byte)decorations.get(i);
                else         onMask |= (byte)decorations.get(i);
            }
            if ((offMask & onMask) != 0)
                throw new PropertyException
                    ("Contradictory instructions for text-decoration " +
                        list.toString());
            return new TextDecorator(list.getProperty(), onMask, offMask);
        }
    }

    public static class TextDepth extends Properties {
        public static final int dataTypes = LENGTH | ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int USE_FONT_METRICS = 1;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.TEXT_DEPTH, USE_FONT_METRICS);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"use-font-metrics"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class TextIndent extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return Length.makeLength (PropNames.TEXT_INDENT, 0.0d, Length.PT);
        }
        public static final int inherited = COMPUTED;
    }

    public static class TextShadow extends Properties {
        public static final int dataTypes = COMPLEX | NONE | INHERIT;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = NONE_IT;
        public static final int inherited = NO;

        public static final ROStringArray enums = ColorCommon.enums;
        public static final Map enumValues = ColorCommon.colorTransEnums;

        /**
         * 'list' is a PropertyValueList containing, at the top level,
         * a sequence of PropertyValueLists, each representing a single
         * shadow effect.  A shadow effect must contain, at a minimum, an
         * inline-progression offset and a block-progression offset.  It may
         * also optionally contain a blur radius.  This set of two or three
         * <tt>Length</tt>s may be preceded or followed by a color
         * specifier.
         */
        public static PropertyValue verifyParsing
                (FOTree foTree, PropertyValue list)
                        throws PropertyException
        {
            int property = list.getProperty();
            if ( ! (list instanceof PropertyValueList)) {
                return Properties.verifyParsing(foTree, list);
            }
            if (((PropertyValueList)list).size() == 0)
                throw new PropertyException
                    ("text-shadow requires PropertyValueList of effects");
            PropertyValueList newlist = new PropertyValueList(property);
            Iterator effects = ((PropertyValueList)list).iterator();
            while (effects.hasNext()) {
                newlist.add(new ShadowEffect(property,
                            (PropertyValueList)(effects.next())));
            }
            return newlist;
        }
    }

    public static class TextTransform extends Properties {
        public static final int dataTypes = ENUM | NONE | INHERIT;
        public static final int traitMapping = REFINE;
        public static final int initialValueType = NONE_IT;
        public static final int CAPITALIZE = 1;
        public static final int UPPERCASE = 2;
        public static final int LOWERCASE = 3;
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"capitalize"
            ,"uppercase"
            ,"lowercase"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class Top extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | AUTO | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class TreatAsWordSpace extends Properties {
        public static final int dataTypes = BOOL | AUTO | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class UnicodeBidi extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int NORMAL = 1;
        public static final int EMBED = 2;
        public static final int BIDI_OVERRIDE = 3;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.UNICODE_BIDI, NORMAL);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"normal"
            ,"embed"
            ,"bidi-override"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class VerticalAlign extends Properties {
        public static final int dataTypes =
                            SHORTHAND | PERCENTAGE | LENGTH | ENUM | INHERIT;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = ENUM_IT;
        public static final int BASELINE = 1;
        public static final int MIDDLE = 2;
        public static final int SUB = 3;
        public static final int SUPER = 4;
        public static final int TEXT_TOP = 5;
        public static final int TEXT_BOTTOM = 6;
        public static final int TOP = 7;
        public static final int BOTTOM = 8;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.VERTICAL_ALIGN, BASELINE);
        }
        public static final int inherited = NO;

        private static final String[] rwEnums = {
            null
            ,"baseline"
            ,"middle"
            ,"sub"
            ,"super"
            ,"text-top"
            ,"text-bottom"
            ,"top"
            ,"bottom"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }

    public static class Visibility extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = MAGIC;
        public static final int initialValueType = ENUM_IT;
        public static final int VISIBLE = 1;
        public static final int HIDDEN = 2;
        public static final int COLLAPSE = 3;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.VISIBILITY, VISIBLE);
        }
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"visible"
            ,"hidden"
            ,"collapse"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class VoiceFamily extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = COMPUTED;
    }

    public static class Volume extends Properties {
        public static final int dataTypes = AURAL;
        public static final int traitMapping = RENDERING;
        public static final int initialValueType = AURAL_IT;
        public static final int inherited = COMPUTED;
    }

    public static class WhiteSpace extends Properties {
        public static final int dataTypes = SHORTHAND | ENUM | INHERIT;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = ENUM_IT;
        public static final int NORMAL = 1;
        public static final int PRE = 2;
        public static final int NOWRAP = 3;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.WHITE_SPACE, NORMAL);
        }
        public static final int inherited = SHORTHAND_INH;

        private static final String[] rwEnums = {
            null
            ,"normal"
            ,"pre"
            ,"nowrap"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class WhiteSpaceCollapse extends Properties {
        public static final int dataTypes = BOOL | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = BOOL_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new Bool(PropNames.WHITE_SPACE_COLLAPSE, true);
        }
        public static final int inherited = COMPUTED;
    }

    public static class WhiteSpaceTreatment extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int IGNORE = 1;
        public static final int PRESERVE = 2;
        public static final int IGNORE_IF_BEFORE_LINEFEED = 3;
        public static final int IGNORE_IF_AFTER_LINEFEED = 4;
        public static final int IGNORE_IF_SURROUNDING_LINEFEED = 5;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.WHITE_SPACE_TREATMENT, PRESERVE);
        }
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"ignore"
            ,"preserve"
            ,"ignore-if-before-linefeed"
            ,"ignore-if-after-linefeed"
            ,"ignore-if-surrounding-linefeed"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }

    public static class Widows extends Properties {
        public static final int dataTypes = INTEGER | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = INTEGER_IT;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new IntegerType(PropNames.WIDOWS, 2);
        }

        public static final int inherited = COMPUTED;
    }

    public static class Width extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | AUTO | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

    public static class WordSpacing extends Properties {
        public static final int dataTypes = LENGTH | ENUM | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = ENUM_IT;
        public static final int NORMAL = 1;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.WORD_SPACING, NORMAL);
        }
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"normal"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class WrapOption extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int WRAP = 1;
        public static final int NO_WRAP = 2;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType(PropNames.WRAP_OPTION, WRAP);
        }
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"wrap"
            ,"no-wrap"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;
    }

    public static class WritingMode extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = NEW_TRAIT;
        public static final int initialValueType = ENUM_IT;
        public static final int LR_TB = 1;
        public static final int RL_TB = 2;
        public static final int TB_RL = 3;
        public static final int LR = 4;
        public static final int RL = 5;
        public static final int TB = 6;
        public static PropertyValue getInitialValue(int property)
            throws PropertyException
        {
            return new EnumType (PropNames.WRITING_MODE, LR_TB);
        }
        public static final int inherited = COMPUTED;

        private static final String[] rwEnums = {
            null
            ,"lr-tb"
            ,"rl-tb"
            ,"tb-rl"
            ,"lr"
            ,"rl"
            ,"tb"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        private static final HashMap rwEnumValues;
        public static final Map enumValues;
        static {
            rwEnumValues = new HashMap(rwEnums.length);
            for (int i = 1; i < rwEnums.length; i++ ) {
                rwEnumValues.put((Object)rwEnums[i],
                                    (Object) Ints.consts.get(i));
            }
            enumValues =
                Collections.unmodifiableMap((Map)rwEnumValues);
        }
    }

    public static class XmlLang extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = SHORTHAND_INH;
    }

    public static class ZIndex extends Properties {
        public static final int dataTypes =INTEGER | AUTO | INHERIT;
        public static final int traitMapping = VALUE_CHANGE;
        public static final int initialValueType = AUTO_IT;
        public static final int inherited = NO;
    }

}
