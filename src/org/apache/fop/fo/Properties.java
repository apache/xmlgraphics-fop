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
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.datastructs.ROIntArray;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datatypes.StringType;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.UriType;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Percentage;
import org.apache.fop.datatypes.Angle;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.MappedEnumType;
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

/**
 * Parent class for all of the individual property classes.  It also contains
 * sets of integer constants for various types of data.
 */

public abstract class Properties {
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
                   ,MAPPED_ENUM = 1048576
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

                        ,NUMBER = FLOAT | INTEGER
                     ,ENUM_TYPE = ENUM | MAPPED_ENUM
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
        if ((datatypes & MAPPED_ENUM) != 0) typeNames += "<MAPPED_ENUM>|";
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
                    ,FONTSET_IT = 65536
            ,TEXT_DECORATION_IT = 131072

           ,USE_SET_FUNCTION_IT = ENUM_IT | BOOL_IT | INTEGER_IT
                                    | NUMBER_IT | LENGTH_IT | ANGLE_IT
                                    | PERCENTAGE_IT | CHARACTER_IT
                                    | LITERAL_IT | NAME_IT
                                    | URI_SPECIFICATION_IT | COLOR_IT
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
        private static final String[] rwEnumMappings = {
            null
            ,"0.5pt"
            ,"1pt"
            ,"2pt"
        };

        public static final ROStringArray enumMappings
                                        = new ROStringArray(rwEnumMappings);
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
        protected static Bool initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Bool(PropNames.AUTO_RESTORE, true);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
         * containing the expansion of the shorthand.  The elements may
         * be in any order.  A minimum of one value will be present.
         *
         *   a BackgroundColor ColorType or Inherit value
         *   a BackgroundImage UriType, None or Inherit value
         *   a BackgroundRepeat EnumType or Inherit value
         *   a BackgroundAttachment EnumType or Inherit value
         *   a BackgroundPositionHorizontal Numeric or Inherit value
         *   a BackgroundPositionVertical Numeric or Inherit value
         */
        public static PropertyValue complex(PropertyValue value)
                        throws PropertyException
        {
            if ( ! (value instanceof PropertyValueList)) {
                return processValue(value);
            } else {
                return processList((PropertyValueList)value);
            }
        }

        private static PropertyValueList processValue
            (PropertyValue value) throws PropertyException
        {
            // Can be Inherit, ColorType, UriType, None, Numeric, or an
            // NCName (i.e. enum token)
            if (value instanceof Inherit |
                    value instanceof FromParent |
                        value instanceof FromNearestSpecified) {
                // Construct a list of Inherit values
                return PropertySets.expandAndCopySHand(value);
            } else  {
                // Make a list an pass to processList
                PropertyValueList tmpList
                        = new PropertyValueList(value.getProperty());
                tmpList.add(value);
                return processList(tmpList);
            }
        }

        private static PropertyValueList processList(PropertyValueList value)
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
                    if (color != null) MessageHandler.log("Background: " +
                                "duplicate color overrides previous color");
                    color = pval;
                    continue scanning_elements;
                }

                if (pval instanceof UriType) {
                    if (image != null) MessageHandler.log("Background: " +
                        "duplicate image uri overrides previous image spec");
                    image = pval;
                    continue scanning_elements;
                }

                if (pval instanceof None) {
                    if (image != null) MessageHandler.log("Background: " +
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
                    posnList.add(pval);
                    // Is it followed by another Numeric?
                    if (elements.hasNext()) {
                        PropertyValue tmpval;
                        if ((tmpval = (PropertyValue)(elements.next()))
                                    instanceof Numeric) {
                            posnList.add(tmpval);
                        } else {
                            // Not a following Numeric, so restore the list
                            // cursor
                            tmpval = (PropertyValue)(elements.previous());
                        }
                    }
                    // Now send one or two Numerics to BackgroundPosition
                    if (position != null)
                            MessageHandler.log("Background: duplicate" +
                            "position overrides previous position");
                    position = BackgroundPosition.complex(posnList);
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
                        if (color != null) MessageHandler.log("Background: " +
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
                                MessageHandler.log("Background: duplicate" +
                                "attachment overrides previous attachment");
                        attachment = enum;
                        continue scanning_elements;
                    }

                    // Must be a position indicator
                    // send it to BackgroundPosition.complex for processing
                    // If it is followed by another NCName, form a list from
                    // the pair, else form a list from this element only

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
                        posnList.add(pval);
                        // Is it followed by another position NCName?
                        if (pos2ok) posnList.add(tmpval);
                        // Now send one or two NCNames to BackgroundPosition
                        if (position != null)
                                MessageHandler.log("Background: duplicate" +
                                "position overrides previous position");
                        position = BackgroundPosition.complex(posnList);
                        continue scanning_elements;
                    }
                    throw new PropertyException
                        ("Unknown NCName value for Background: " + ncname);
                }

                throw new PropertyException
                    ("Invalid " + pval.getClass().getName() +
                        " property value for Background");
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                            (PropNames.BACKGROUND_ATTACHMENT, SCROLL);
            } catch (PropertyException e) {
                System.out.println("EnumType exception: " + e.getMessage()); //DEBUG
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static ColorType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new ColorType
                        (PropNames.BACKGROUND_COLOR, "transparent");
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
         * <p>If 'value' is a PropertyValueList, it contains either a pair of
         * distance measurement (length or percentage) or a pair of
         * enumeration tokens representing the background position offset
         * in the "height" and "width" dimensions.
         *
         * <p>The value(s) provided, if valid, are converted into a list
         * containing the expansion of the shorthand.  I.e. the first
         * element is a value for BackgroundPositionHorizontal, and the
         * second is for BackgroundPositionVertical.
         */
        public static PropertyValue complex(PropertyValue value)
                        throws PropertyException
        {
            if ( ! (value instanceof PropertyValueList)) {
                return processValue(value);
            } else {
                return processList((PropertyValueList)value);
            }
        }

        private static PropertyValueList processValue(PropertyValue value)
                        throws PropertyException
        {
            PropertyValueList newlist
                            = new PropertyValueList(value.getProperty());
            // Can only be Inherit, NCName (i.e. enum token)
            // or Numeric (i.e. Length or Percentage)
            if (value instanceof Inherit |
                    value instanceof FromParent |
                        value instanceof FromNearestSpecified) {
                // Construct a list of Inherit values
                newlist = PropertySets.expandAndCopySHand(value);
            } else if (value instanceof Numeric) {
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
                        ("More that 2 elements in BackgroundPosition list.");
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Percentage.makePercentage
                            (PropNames.BACKGROUND_POSITION_HORIZONTAL, 0.0d);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Percentage.makePercentage
                            (PropNames.BACKGROUND_POSITION_VERTICAL, 0.0d);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                (PropNames.BACKGROUND_REPEAT, REPEAT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                (PropNames.BASELINE_SHIFT, BASELINE);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                (PropNames.BLANK_OR_NOT_BLANK, ANY);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static ColorType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new ColorType
                        (PropNames.BACKGROUND_COLOR, ColorCommon.BLACK);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        public static final int dataTypes = MAPPED_ENUM | LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                (PropNames.BORDER_AFTER_WIDTH, 1d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonWidth.enums;
        public static final ROStringArray enumValues
                                            = BorderCommonWidth.enumValues;
        public static final ROStringArray enumMappings
                                            = BorderCommonWidth.enumMappings;
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue =
                    new EnumType(PropNames.BORDER_AFTER_WIDTH_CONDITIONALITY,
                                            Conditionality.DISCARD);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static ColorType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new ColorType
                        (PropNames.BACKGROUND_COLOR, ColorCommon.BLACK);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        public static final int dataTypes = MAPPED_ENUM | LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                (PropNames.BORDER_BEFORE_WIDTH, 1d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonWidth.enums;
        public static final ROStringArray enumValues
                                            = BorderCommonWidth.enumValues;
        public static final ROStringArray enumMappings
                                            = BorderCommonWidth.enumMappings;
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue =
                    new EnumType(PropNames.BORDER_BEFORE_WIDTH_CONDITIONALITY,
                                            Conditionality.DISCARD);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
    }

    public static class BorderBottomColor extends Properties {
        public static final int dataTypes = COLOR_T | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = COLOR_IT;
        public static final int inherited = NO;
        protected static ColorType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new ColorType
                        (PropNames.BACKGROUND_COLOR, ColorCommon.BLACK);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        public static final int dataTypes = MAPPED_ENUM | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                (PropNames.BORDER_BOTTOM_WIDTH, 1d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonWidth.enums;
        public static final ROStringArray enumValues
                                            = BorderCommonWidth.enumValues;
        public static final ROStringArray enumMappings
                                            = BorderCommonWidth.enumMappings;
    }

    public static class BorderCollapse extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int COLLAPSE = 1;
        public static final int SEPARATE = 2;
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue =
                            new EnumType
                                (PropNames.BORDER_COLLAPSE, COLLAPSE);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
         */
        public static PropertyValue complex(PropertyValue value)
                    throws PropertyException
        {
            if ( ! (value instanceof PropertyValueList)) {
                if (value instanceof Inherit
                    || value instanceof ColorType
                    || value instanceof FromParent
                    || value instanceof FromNearestSpecified
                    )
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
                // List may contain only multiple color specifiers
                // i.e. ColorTypes or NCNames specifying a standard color or
                // 'transparent'.
                PropertyValueList list = (PropertyValueList)value;
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
                list.add(left);
                list.add(bottom);
                list.add(right);
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
        protected static ColorType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new ColorType
                        (PropNames.BACKGROUND_COLOR, ColorCommon.BLACK);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        public static final int dataTypes = MAPPED_ENUM | LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                (PropNames.BORDER_END_WIDTH, 1d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonWidth.enums;
        public static final ROStringArray enumValues
                                            = BorderCommonWidth.enumValues;
        public static final ROStringArray enumMappings
                                            = BorderCommonWidth.enumMappings;
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue =
                    new EnumType(PropNames.BORDER_END_WIDTH_CONDITIONALITY,
                                            Conditionality.DISCARD);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
    }

    public static class BorderLeftColor extends Properties {
        public static final int dataTypes = COLOR_T | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = COLOR_IT;
        public static final int inherited = NO;
        protected static ColorType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new ColorType
                        (PropNames.BACKGROUND_COLOR, ColorCommon.BLACK);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        public static final int dataTypes = MAPPED_ENUM | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                (PropNames.BORDER_LEFT_WIDTH, 1d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonWidth.enums;
        public static final ROStringArray enumValues
                                            = BorderCommonWidth.enumValues;
        public static final ROStringArray enumMappings
                                            = BorderCommonWidth.enumMappings;
    }

    public static class BorderRight extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class BorderRightColor extends Properties {
        public static final int dataTypes = COLOR_T | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = COLOR_IT;
        public static final int inherited = NO;
        protected static ColorType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new ColorType
                        (PropNames.BACKGROUND_COLOR, ColorCommon.BLACK);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        public static final int dataTypes = MAPPED_ENUM | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                (PropNames.BORDER_RIGHT_WIDTH, 1d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonWidth.enums;
        public static final ROStringArray enumValues
                                            = BorderCommonWidth.enumValues;
        public static final ROStringArray enumMappings
                                            = BorderCommonWidth.enumMappings;
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                    (PropNames.BORDER_SEPARATION_BLOCK_PROGRESSION_DIRECTION,
                                                            0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class BorderSeparationInlineProgressionDirection
                                                        extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                    (PropNames.BORDER_SEPARATION_INLINE_PROGRESSION_DIRECTION,
                    0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
         */
        public static PropertyValue complex(PropertyValue value)
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
                if (((PropertyValueList)value).size() != 2)
                    throw new PropertyException
                        ("List of " + ((PropertyValueList)value).size() +
                            " for BorderSpacing");
                PropertyValue len1
                    = (PropertyValue)(((PropertyValueList)value).getFirst());
                PropertyValue len2
                    = (PropertyValue)(((PropertyValueList)value).getLast());
                // Note that this test excludes (deliberately) ems relative
                // lengths.  I don't know whether this exclusion is valid.
                if ( ! (len1 instanceof Numeric && len2 instanceof Numeric
                    && ((Numeric)len1).isLength()
                    && ((Numeric)len2).isLength()))
                    throw new PropertyException
                        ("2 values to BorderSpacing are not Lengths");
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
        protected static ColorType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new ColorType
                        (PropNames.BACKGROUND_COLOR, ColorCommon.BLACK);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        public static final int dataTypes = MAPPED_ENUM | LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                (PropNames.BORDER_START_WIDTH, 1d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonWidth.enums;
        public static final ROStringArray enumValues
                                            = BorderCommonWidth.enumValues;
        public static final ROStringArray enumMappings
                                            = BorderCommonWidth.enumMappings;
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue =
                    new EnumType(PropNames.BORDER_START_WIDTH_CONDITIONALITY,
                                            Conditionality.DISCARD);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
         */
        public static PropertyValue complex(PropertyValue value)
                    throws PropertyException
        {
            if ( ! (value instanceof PropertyValueList)) {
                if (value instanceof Inherit
                    || value instanceof FromParent
                    || value instanceof FromNearestSpecified
                    )
                    return PropertySets.expandAndCopySHand(value);
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
                // List may contain only multiple style specifiers
                // i.e. NCNames specifying a standard style
                PropertyValueList list = (PropertyValueList)value;
                EnumType top, left, bottom, right;
                int count = list.size();
                if (count < 2 || count > 4)
                    throw new PropertyException
                        ("border-style list contains " + count + " items");

                Iterator styles = list.iterator();

                // There must be at least two
                top = getEnum((PropertyValue)(styles.next()));
                left = getEnum((PropertyValue)(styles.next()));
                try {
                    bottom = (EnumType)(top.clone());
                    right = (EnumType)(left.clone());
                } catch (CloneNotSupportedException cnse) {
                    throw new PropertyException
                                ("clone() not supported on EnumType");
                }

                if (styles.hasNext()) bottom
                            = getEnum((PropertyValue)(styles.next()));
                if (styles.hasNext()) right
                            = getEnum((PropertyValue)(styles.next()));

                // Set the properties for each
                top.setProperty(PropNames.BORDER_TOP_STYLE);
                left.setProperty(PropNames.BORDER_LEFT_STYLE);
                bottom.setProperty(PropNames.BORDER_BOTTOM_STYLE);
                right.setProperty(PropNames.BORDER_RIGHT_STYLE);

                list = new PropertyValueList(PropNames.BORDER_STYLE);
                list.add(top);
                list.add(left);
                list.add(bottom);
                list.add(right);
                // Question: if less than four styles have been specified in
                // the shorthand, what border-?-style properties, if any,
                // have been specified?
                return list;
            }
        }

        /**
         * Return the EnumType derived from the argument.
         * The argument must be an NCName whose string value is a
         * standard style.
         * @param value <tt>PropertyValue</tt>
         * @return <tt>EnumValue</tt> equivalent of the argument
         * @exception <tt>PropertyException</tt>
         */
        private static EnumType getEnum(PropertyValue value)
                throws PropertyException
        {
            int property = value.getProperty();
            // Must be a style enum
            if ( ! (value instanceof NCName))
                throw new PropertyException
                    (value.getClass().getName() + " instead of style for "
                                    + PropNames.getPropertyName(property));
            // We have an NCName - hope it''s a style
            NCName ncname = (NCName)value;
            try {
                return new EnumType(property, ncname.getNCName());
            } catch (PropertyException e) {
                throw new PropertyException
                            (ncname.getNCName() + " instead of style for "
                                    + PropNames.getPropertyName(property));
            }
        }
    }

    public static class BorderTop extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;
    }

    public static class BorderTopColor extends Properties {
        public static final int dataTypes = COLOR_T | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = COLOR_IT;
        public static final int inherited = NO;
        protected static ColorType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new ColorType
                        (PropNames.BACKGROUND_COLOR, ColorCommon.BLACK);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        private static final HashMap rwEnumValues
                                            = BorderCommonStyle.rwEnumValues;
    }

    public static class BorderTopWidth extends Properties {
        public static final int dataTypes = MAPPED_ENUM | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                (PropNames.BORDER_TOP_WIDTH, 1d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }

        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonWidth.enums;
        public static final ROStringArray enumValues
                                            = BorderCommonWidth.enumValues;
        public static final ROStringArray enumMappings
                                            = BorderCommonWidth.enumMappings;
    }

    public static class BorderWidth extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = NO;

        public static final ROStringArray enums = BorderCommonWidth.enums;
        public static final ROStringArray enumValues
                                            = BorderCommonWidth.enumValues;
        public static final ROStringArray enumMappings
                                            = BorderCommonWidth.enumMappings;

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
         * <p>If 'value' is a PropertyValueList, it contains a list of
         * 2 to 4 NCName enum tokens representing border-widths.
         *
         * <p>The value(s) provided, if valid, are converted into a list
         * containing the expansion of the shorthand.
         * The first element is a value for border-top-width,
         * the second element is a value for border-right-width,
         * the third element is a value for border-bottom-width,
         * the fourth element is a value for border-left-width.
         */
        public static PropertyValue complex(PropertyValue value)
                    throws PropertyException
        {
            if ( ! (value instanceof PropertyValueList)) {
                if (value instanceof Inherit
                    || value instanceof FromParent
                    || value instanceof FromNearestSpecified
                    )
                    return PropertySets.expandAndCopySHand(value);
                if (value instanceof NCName) {
                    // Must be a border-width
                    MappedEnumType mapped;
                    try {
                        mapped = new MappedEnumType(PropNames.BORDER_WIDTH,
                                            ((NCName)value).getNCName());
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
                // List may contain only multiple width specifiers
                // i.e. NCNames specifying a standard width
                PropertyValueList list = (PropertyValueList)value;
                MappedEnumType top, left, bottom, right;
                int count = list.size();
                if (count < 2 || count > 4)
                    throw new PropertyException
                        ("border-width list contains " + count + " items");

                Iterator widths = list.iterator();

                // There must be at least two
                top = getMappedEnum((PropertyValue)(widths.next()));
                left = getMappedEnum((PropertyValue)(widths.next()));
                try {
                    bottom = (MappedEnumType)(top.clone());
                    right = (MappedEnumType)(left.clone());
                } catch (CloneNotSupportedException cnse) {
                    throw new PropertyException
                                ("clone() not supported on MappedEnumType");
                }

                if (widths.hasNext()) bottom
                            = getMappedEnum((PropertyValue)(widths.next()));
                if (widths.hasNext()) right
                            = getMappedEnum((PropertyValue)(widths.next()));

                // Set the properties for each
                top.setProperty(PropNames.BORDER_TOP_WIDTH);
                left.setProperty(PropNames.BORDER_LEFT_WIDTH);
                bottom.setProperty(PropNames.BORDER_BOTTOM_WIDTH);
                right.setProperty(PropNames.BORDER_RIGHT_WIDTH);

                list = new PropertyValueList(PropNames.BORDER_WIDTH);
                list.add(top);
                list.add(left);
                list.add(bottom);
                list.add(right);
                // Question: if less than four widths have been specified in
                // the shorthand, what border-?-width properties, if any,
                // have been specified?
                return list;
            }
        }

        /**
         * Return the MappedEnumType derived from the argument.
         * The argument must be an NCName whose string value is a
         * standard width.
         * @param value <tt>PropertyValue</tt>
         * @return <tt>MappedEnumValue</tt> equivalent of the argument
         * @exception <tt>PropertyException</tt>
         */
        private static MappedEnumType getMappedEnum(PropertyValue value)
                throws PropertyException
        {
            int property = value.getProperty();
            // Must be a width enum
            if ( ! (value instanceof NCName))
                throw new PropertyException
                    (value.getClass().getName() + " instead of width for "
                                    + PropNames.getPropertyName(property));
            // We have an NCName - hope it''s a width
            NCName ncname = (NCName)value;
            try {
                return new MappedEnumType(property, ncname.getNCName());
            } catch (PropertyException e) {
                throw new PropertyException
                            (ncname.getNCName() + " instead of width for "
                                    + PropNames.getPropertyName(property));
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

        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                (PropNames.CAPTION_SIDE, BEFORE);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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

        public static PropertyValue complex(PropertyValue value)
                        throws PropertyException
        {
            // AUTO and INHERIT will have been normally processed
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
        protected static ColorType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new ColorType
                        (PropNames.BACKGROUND_COLOR, ColorCommon.BLACK);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Numeric(PropNames.COLUMN_COUNT, 1d);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }

        public static final int inherited = NO;
    }

    public static class ColumnGap extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                    (PropNames.COLUMN_GAP, 12.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                    (PropNames.DESTINATION_PLACEMENT_OFFSET, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class Direction extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = NEW_TRAIT;
        public static final int initialValueType = ENUM_IT;
        public static final int LTR = 1;
        public static final int RTL = 2;
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType(PropNames.DIRECTION, LTR);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                        (PropNames.EMPTY_CELLS, SHOW);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                    (PropNames.END_INDENT, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPUTED;
    }

    public static class EndsRow extends Properties {
        public static final int dataTypes = BOOL;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = BOOL_IT;
        protected static Bool initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Bool(PropNames.ENDS_ROW, false);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class Extent extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                        (PropNames.EXTENT, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class ExternalDestination extends Properties {
        public static final int dataTypes = URI_SPECIFICATION;
        public static final int traitMapping = ACTION;
        public static final int initialValueType = URI_SPECIFICATION_IT;
        protected static UriType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new UriType(PropNames.EXTERNAL_DESTINATION, "");
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static NCName initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new NCName(PropNames.FLOW_NAME, "");
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class Font extends Properties {
        public static final int dataTypes = SHORTHAND;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = NOTYPE_IT;
        public static final int inherited = SHORTHAND_INH;
    }

    public static class FontFamily extends Properties {
        public static final int dataTypes = COMPLEX;
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

        public static PropertyValue complex(PropertyValue propvalue)
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

            int property = propvalue.getProperty();
            // First, check that we have a list
            if ( ! (propvalue instanceof PropertyValueList)) {
                if ( ! (propvalue instanceof StringType))
                    throw new PropertyException
                        ("Invalid " + propvalue.getClass().getName() +
                            " PropertyValue for font-family");
                return new FontFamilySet(property,
                        new String[] {((StringType)propvalue).getString() });
            }
            PropertyValueList list = (PropertyValueList)propvalue;
            String[] strings = new String[list.size()];
            int i = 0;          // the strings index
            Iterator scan = list.iterator();
            while (scan.hasNext()) {
                Object value = scan.next();
                String name = "";
                if (value instanceof PropertyValueList) {
                    // build a font name according to
                    // 7.8.2 "font-family" <family-name>
                    Iterator font = ((PropertyValueList)value).iterator();
                    while (font.hasNext())
                        name = name + (name.length() == 0 ? "" : " ")
                                + ((StringType)(font.next())).getString();
                }
                else if (value instanceof StringType)
                            name = ((StringType)value).getString();
                else throw new PropertyException
                        ("Invalid " + propvalue.getClass().getName() +
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
                                PERCENTAGE | LENGTH | MAPPED_ENUM | INHERIT;
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

        protected static Numeric initialValue;
        // N.B. This foundational value MUST be an absolute length
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue =
                        Length.makeLength(PropNames.FONT_SIZE, 12d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static Numeric getInitialValue() {
            return initialValue;
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

        private static final String[] rwEnumMappings = {
            null
            ,"7pt"
            ,"8.3pt"
            ,"10pt"
            ,"12pt"
            ,"14.4pt"
            ,"17.3pt"
            ,"20.7pt"
            ,"1.2em"
            ,"0.83em"
        };

        public static final ROStringArray enumMappings
                                        = new ROStringArray(rwEnumMappings);

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

        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                    (PropNames.FONT_STRETCH, NORMAL);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                    (PropNames.FONT_STYLE, NORMAL);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                    (PropNames.FONT_VARIANT, NORMAL);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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

        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = IntegerType.makeInteger
                                            (PropNames.FONT_WEIGHT, 400);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Literal initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Literal(PropNames.FORMAT, "1");
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class GlyphOrientationHorizontal extends Properties {
        public static final int dataTypes = ANGLE | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ANGLE_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Angle.makeAngle
                    (PropNames.GLYPH_ORIENTATION_HORIZONTAL, 0d, Angle.DEG);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Bool initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Bool(PropNames.HYPHENATE, false);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPUTED;
    }

    public static class HyphenationCharacter extends Properties {
        public static final int dataTypes = CHARACTER_T | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LITERAL_IT;
        protected static Literal initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Literal
                                (PropNames.HYPHENATION_CHARACTER, "\u2010");
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                        (PropNames.HYPHENATION_LADDER_COUNT, NO_LIMIT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Numeric
                            (PropNames.HYPHENATION_PUSH_CHARACTER_COUNT, 2d);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }

        public static final int inherited = COMPUTED;
    }

    public static class HyphenationRemainCharacterCount extends Properties {
        public static final int dataTypes = NUMBER | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NUMBER_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Numeric
                            (PropNames.HYPHENATION_REMAIN_CHARACTER_COUNT, 2d);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Bool initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Bool(PropNames.INDICATE_DESTINATION, false);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Literal initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Literal
                                        (PropNames.INTERNAL_DESTINATION, "");
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                            (PropNames.LAST_LINE_END_INDENT, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                            (PropNames.LEADER_LENGTH_MINIMUM, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class LeaderLengthOptimum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                        (PropNames.LEADER_LENGTH_OPTIMUM, 12.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class LeaderLengthMaximum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = PERCENTAGE_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Percentage.makePercentage
                                    (PropNames.LEADER_LENGTH_MAXIMUM, 100.0d);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                    (PropNames.LEADER_PATTERN, SPACE);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                    (PropNames.LEADER_PATTERN_WIDTH, USE_FONT_METRICS);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                    (PropNames.LETTER_SPACING, NORMAL);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                        (PropNames.LINEFEED_TREATMENT, TREAT_AS_SPACE);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
                        PERCENTAGE | LENGTH | NUMBER | MAPPED_ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NOTYPE_IT;
        public static final int NORMAL = 1;
        protected static Numeric initialValue;
        public static final int inherited = VALUE_SPECIFIC;

        private static final String[] rwEnums = {
            null
            ,"normal"
        };
        public static final ROStringArray enums = new ROStringArray(rwEnums);
        public static final ROStringArray enumValues = enums;

        // If this value changes, change the corresponding initialValue.
        private static final String[] rwEnumMappings = {
            null
            ,"1.2em"
        };

        public static final ROStringArray enumMappings
                                        = new ROStringArray(rwEnumMappings);
    }

    public static class LineHeightMinimum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Numeric(PropNames.LINE_HEIGHT, 1.2d);
                initialValue.multiply(foTree.currentFontSize());
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class LineHeightOptimum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Numeric(PropNames.LINE_HEIGHT, 1.2d);
                initialValue.multiply(foTree.currentFontSize());
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class LineHeightMaximum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Numeric(PropNames.LINE_HEIGHT, 1.2d);
                initialValue.multiply(foTree.currentFontSize());
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class LineHeightConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                        (PropNames.SPACE_AFTER_CONDITIONALITY,
                                            Conditionality.DISCARD);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = IntegerType.makeInteger
                                        (PropNames.LINE_HEIGHT_PRECEDENCE, 0);
            } catch(PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                    (PropNames.LINE_HEIGHT_SHIFT_ADJUSTMENT,
                                                    CONSIDER_SHIFTS);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                        (PropNames.LINE_STACKING_STRATEGY, LINE_HEIGHT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
    }

    public static class MarginBottom extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | AUTO | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                    (PropNames.MARGIN_BOTTOM, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class MarginLeft extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | AUTO | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                    (PropNames.MARGIN_LEFT, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class MarginRight extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | AUTO | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                    (PropNames.MARGIN_RIGHT, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class MarginTop extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | AUTO | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                    (PropNames.MARGIN_TOP, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class MarkerClassName extends Properties {
        public static final int dataTypes = NAME;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NAME_IT;
        protected static NCName initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new NCName(PropNames.MARKER_CLASS_NAME, "");
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class MasterName extends Properties {
        public static final int dataTypes = NAME;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = NAME_IT;
        protected static NCName initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new NCName(PropNames.MASTER_NAME, "");
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class MasterReference extends Properties {
        public static final int dataTypes = NAME;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = NAME_IT;
        protected static NCName initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new NCName(PropNames.MASTER_REFERENCE, "");
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class MaxHeight extends Properties {
        public static final int dataTypes =
                                        PERCENTAGE | LENGTH | NONE | INHERIT;
        public static final int traitMapping = SHORTHAND_MAP;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                    (PropNames.MAX_HEIGHT, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class MaximumRepeats extends Properties {
        public static final int dataTypes = NUMBER | ENUM | INHERIT;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = ENUM_IT;
        public static final int NO_LIMIT = 1;
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                (PropNames.MAXIMUM_REPEATS, NO_LIMIT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                    (PropNames.MIN_HEIGHT, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Numeric
                                    (PropNames.NUMBER_COLUMNS_REPEATED, 1d);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }

        public static final int inherited = NO;
    }

    public static class NumberColumnsSpanned extends Properties {
        public static final int dataTypes = NUMBER;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NUMBER_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Numeric
                                    (PropNames.NUMBER_COLUMNS_SPANNED, 1d);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }

        public static final int inherited = NO;
    }

    public static class NumberRowsSpanned extends Properties {
        public static final int dataTypes = NUMBER;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = NUMBER_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Numeric(PropNames.NUMBER_ROWS_SPANNED, 1d);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType(PropNames.ODD_OR_EVEN, ANY);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = IntegerType.makeInteger(PropNames.ORPHANS, 2);
            } catch(PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                    (PropNames.PADDING_AFTER, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class PaddingAfterLength extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                            (PropNames.PADDING_AFTER_LENGTH, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class PaddingAfterConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = ENUM_IT;
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                        (PropNames.PADDING_AFTER_CONDITIONALITY,
                                                Conditionality.DISCARD);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                (PropNames.PADDING_BEFORE, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class PaddingBeforeLength extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                            (PropNames.PADDING_BEFORE_LENGTH, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class PaddingBeforeConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = ENUM_IT;
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                        (PropNames.PADDING_BEFORE_CONDITIONALITY,
                                                Conditionality.DISCARD);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                (PropNames.PADDING_BOTTOM, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class PaddingEnd extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                  (PropNames.PADDING_END, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class PaddingEndLength extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                            (PropNames.PADDING_END_LENGTH, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class PaddingEndConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = ENUM_IT;
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                        (PropNames.PADDING_END_CONDITIONALITY,
                                                Conditionality.DISCARD);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                    (PropNames.PADDING_LEFT, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class PaddingRight extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = DISAPPEARS;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                    (PropNames.PADDING_RIGHT, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class PaddingStart extends Properties {
        public static final int dataTypes = PERCENTAGE | LENGTH | INHERIT;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                    (PropNames.PADDING_START, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class PaddingStartLength extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                            (PropNames.PADDING_START_LENGTH, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class PaddingStartConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING | RENDERING;
        public static final int initialValueType = ENUM_IT;
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                    (PropNames.PADDING_START_CONDITIONALITY,
                                                Conditionality.DISCARD);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                    (PropNames.PADDING_TOP, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                        (PropNames.PAGE_POSITION, ANY);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType(PropNames.POSITION, STATIC);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Bool initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Bool(PropNames.PRECEDENCE, false);
            } catch (PropertyException e) {
                throw new RuntimeException
                                ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                    (PropNames.PROVISIONAL_DISTANCE_BETWEEN_STARTS,
                        24.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPUTED;
    }

    public static class ProvisionalLabelSeparation extends Properties {
        public static final int dataTypes = LENGTH | INHERIT;
        public static final int traitMapping = SPECIFICATION;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                    (PropNames.PROVISIONAL_LABEL_SEPARATION, 6.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPUTED;
    }

    public static class ReferenceOrientation extends Properties {
        public static final int dataTypes = INTEGER | INHERIT;
        public static final int traitMapping = NEW_TRAIT;
        public static final int initialValueType = INTEGER_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = IntegerType.makeInteger
                                    (PropNames.REFERENCE_ORIENTATION, 0);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                    (PropNames.RELATIVE_ALIGN, BEFORE);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                (PropNames.RELATIVE_POSITION, STATIC);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                        (PropNames.RETRIEVE_BOUNDARY, PAGE_SEQUENCE);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                        (PropNames.RETRIEVE_POSITION,
                                            FIRST_STARTING_WITHIN_PAGE);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType(PropNames.RULE_STYLE, SOLID);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                (PropNames.RULE_THICKNESS, 1.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPUTED;
    }

    public static class Scaling extends Properties {
        public static final int dataTypes = ENUM | INHERIT;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        public static final int UNIFORM = 1;
        public static final int NON_UNIFORM = 2;
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType(PropNames.SCALING, UNIFORM);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Bool initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Bool(PropNames.SCORE_SPACES, true);
            } catch (PropertyException e) {
                throw new RuntimeException
                                ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                (PropNames.SHOW_DESTINATION, REPLACE);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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

        public static PropertyValue complex(PropertyValue list)
                        throws PropertyException
        {
            // Confirm that the list contains only UriType elements
            Iterator iter = ((PropertyValueList)list).iterator();
            while (iter.hasNext()) {
                Object value = iter.next();
                if ( ! (value instanceof UriType))
                    throw new PropertyException
                        ("source-document requires a list of uris");
            }
            return list;
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                            (PropNames.SPACE_AFTER_MINIMUM, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceAfterOptimum extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                            (PropNames.SPACE_AFTER_OPTIMUM, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceAfterMaximum extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                            (PropNames.SPACE_AFTER_MAXIMUM, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceAfterConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                        (PropNames.SPACE_AFTER_CONDITIONALITY,
                                            Conditionality.DISCARD);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = IntegerType.makeInteger
                                        (PropNames.SPACE_AFTER_PRECEDENCE, 0);
            } catch(PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                            (PropNames.SPACE_BEFORE_MINIMUM, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceBeforeOptimum extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                            (PropNames.SPACE_BEFORE_OPTIMUM, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceBeforeMaximum extends Properties {
        public static final int dataTypes = LENGTH;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                            (PropNames.SPACE_BEFORE_MAXIMUM, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceBeforeConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                        (PropNames.SPACE_BEFORE_CONDITIONALITY,
                                                Conditionality.DISCARD);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = IntegerType.makeInteger
                                        (PropNames.SPACE_BEFORE_PRECEDENCE, 0);
            } catch(PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                            (PropNames.SPACE_END_MINIMUM, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceEndOptimum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                            (PropNames.SPACE_END_OPTIMUM, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceEndMaximum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                            (PropNames.SPACE_END_MAXIMUM, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceEndConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                        (PropNames.SPACE_END_CONDITIONALITY,
                                                Conditionality.DISCARD);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = IntegerType.makeInteger
                                        (PropNames.SPACE_END_PRECEDENCE, 0);
            } catch(PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                            (PropNames.SPACE_START_MINIMUM, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceStartOptimum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                            (PropNames.SPACE_START_OPTIMUM, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceStartMaximum extends Properties {
        public static final int dataTypes = LENGTH | PERCENTAGE;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = LENGTH_IT;
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                            (PropNames.SPACE_START_MAXIMUM, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPOUND;
    }

    public static class SpaceStartConditionality extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = ENUM_IT;
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                        (PropNames.SPACE_START_CONDITIONALITY,
                                                Conditionality.DISCARD);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = IntegerType.makeInteger
                                        (PropNames.SPACE_START_PRECEDENCE, 0);
            } catch(PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                    (PropNames.START_INDENT, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = COMPUTED;
    }

    public static class StartingState extends Properties {
        public static final int dataTypes = ENUM;
        public static final int traitMapping = ACTION;
        public static final int initialValueType = ENUM_IT;
        public static final int SHOW = 1;
        public static final int HIDE = 2;
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                    (PropNames.STARTING_STATE, SHOW);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Bool initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Bool(PropNames.STARTS_ROW, false);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                        (PropNames.SWITCH_TO, XSL_ANY);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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

        public static PropertyValue complex(PropertyValue list)
                        throws PropertyException
        {
            // Assume that the enumeration has been checked for.  Look for
            // a list of NCNames.
            // N.B. it may be a possible to perform further checks on the
            // validity of the NCNames - do they match multi-case case names.
            Iterator iter = ((PropertyValueList)list).iterator();
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
        protected static Bool initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Bool
                                (PropNames.TABLE_OMIT_FOOTER_AT_BREAK, false);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class TableOmitHeaderAtBreak extends Properties {
        public static final int dataTypes = BOOL;
        public static final int traitMapping = FORMATTING;
        public static final int initialValueType = BOOL_IT;
        protected static Bool initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Bool
                                (PropNames.TABLE_OMIT_HEADER_AT_BREAK, false);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
        }
        public static final int inherited = NO;
    }

    public static class TargetPresentationContext extends Properties {
        public static final int dataTypes = URI_SPECIFICATION | ENUM;
        public static final int traitMapping = ACTION;
        public static final int initialValueType = ENUM_IT;
        public static final int USE_TARGET_PROCESSING_CONTEXT = 1;
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                        (PropNames.TARGET_PRESENTATION_CONTEXT,
                                        USE_TARGET_PROCESSING_CONTEXT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                        (PropNames.TARGET_PROCESSING_CONTEXT,
                                                        DOCUMENT_ROOT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                            (PropNames.TARGET_STYLESHEET,
                                                USE_NORMAL_STYLESHEET);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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

        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType(PropNames.TEXT_ALIGN, START);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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

        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                (PropNames.TEXT_ALIGN_LAST, RELATIVE);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                            (PropNames.TEXT_ALTITUDE, USE_FONT_METRICS);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static TextDecorations initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new TextDecorations
                            (PropNames.TEXT_DECORATION, NO_DECORATION);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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

        public static PropertyValue complex(PropertyValue list)
                        throws PropertyException
        {
            byte onMask = NO_DECORATION;
            byte offMask = NO_DECORATION;
            Iterator iter;
            LinkedList strings = new LinkedList();
            if ( ! (list instanceof PropertyValueList)) {
                if ( ! (list instanceof NCName))
                    throw new PropertyException
                        ("text-decoration require list of NCNames");
                strings.add(((NCName)list).getNCName());
            } else { // list is a PropertyValueList
                iter = ((PropertyValueList)list).iterator();
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                            (PropNames.TEXT_DEPTH, USE_FONT_METRICS);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = Length.makeLength
                                    (PropNames.TEXT_INDENT, 0.0d, Length.PT);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        public static PropertyValue complex(PropertyValue list)
                        throws PropertyException
        {
            int property = list.getProperty();
            if ( ! (list instanceof PropertyValueList) ||
                    ((PropertyValueList)list).size() == 0)
                throw new PropertyException
                    ("text-shadow requires PropertyValueList of effects");
            PropertyValueList newlist =
                    new PropertyValueList(property);
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                    (PropNames.UNICODE_BIDI, NORMAL);
            } catch (PropertyException e) {
                throw new RuntimeException
                                   ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                (PropNames.VERTICAL_ALIGN, BASELINE);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                        (PropNames.VISIBILITY, VISIBLE);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                        (PropNames.WHITE_SPACE, NORMAL);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Bool initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new Bool(PropNames.WHITE_SPACE_COLLAPSE, true);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                            (PropNames.WHITE_SPACE_TREATMENT, PRESERVE);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static Numeric initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = IntegerType.makeInteger(PropNames.WIDOWS, 2);
            } catch(PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                    (PropNames.WORD_SPACING, NORMAL);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType(PropNames.WRAP_OPTION, WRAP);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
        protected static EnumType initialValue;
        public static void setInitialValue(FOTree foTree) {
            try {
                initialValue = new EnumType
                                        (PropNames.WRITING_MODE, LR_TB);
            } catch (PropertyException e) {
                throw new RuntimeException
                                    ("PropertyException: " + e.getMessage());
            }
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
