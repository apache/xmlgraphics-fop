/*
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
 * $Id$
 */

package org.apache.fop.fo.properties;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.fop.apps.Fop;
import org.apache.fop.datatypes.Auto;
import org.apache.fop.datatypes.CountryType;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.LanguageType;
import org.apache.fop.datatypes.MappedNumeric;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.NoType;
import org.apache.fop.datatypes.None;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.ScriptType;
import org.apache.fop.datatypes.indirect.FromNearestSpecified;
import org.apache.fop.datatypes.indirect.FromParent;
import org.apache.fop.datatypes.indirect.Inherit;
import org.apache.fop.datatypes.indirect.InheritedValue;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.ShorthandPropSets;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.PropertyNotImplementedException;

/**
 * Parent class for all of the individual property classes.  It also contains
 * sets of integer constants for various types of data.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class Property {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";
    
    protected static final Logger logger = Logger.getLogger(Fop.fopPackage);

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
                        ,NCNAME = 128
                       ,COLOR_T = 256
                     ,COUNTRY_T = 512
                    ,LANGUAGE_T = 1024
                      ,SCRIPT_T = 2048
             ,URI_SPECIFICATION = 4096
                          ,TIME = 8192
                     ,FREQUENCY = 16384
    // Pseudotypes
                          ,BOOL = 32768
                       ,INHERIT = 65536
                          ,ENUM = 131072
                 ,MAPPED_LENGTH = 262144
                     ,SHORTHAND = 524288
                       ,COMPLEX = 1048576
                          ,AUTO = 2097152
                          ,NONE = 4194304
                         ,AURAL = 8388608
    // Color plus transparent
                   ,COLOR_TRANS = 16777216
                      ,MIMETYPE = 33554432
                       ,FONTSET = 67108864
                      ,COMPOUND = 134217728
    //                   ,SPARE = 268435456
    //                   ,SPARE = 536870912
    //                   ,SPARE = 1073741824
    //                   ,SPARE = -2147483648

    // A number of questions are unresolved about the interaction of
    // complex parsing, property expression parsing & property validation.
    // At this time (2002/07/03) it looks as though the refineParsing() method
    // will take full validation responsibility, so it will not be
    // necessary to specify any individual datatypes besides COMPLEX in the
    // property dataTypes field.  This renders some such specifications
    // redundant.  On the other hand, if such individual datatype validation
    // becomes necessary, the datatype settings for properties with COMPLEX
    // will have to be adjusted.  pbw

                        ,NUMBER = FLOAT | INTEGER
                     ,ENUM_TYPE = ENUM | MAPPED_LENGTH
                        ,STRING = LITERAL | NCNAME
                     ,HYPH_TYPE = COUNTRY_T | LANGUAGE_T | SCRIPT_T
                     ,NAME_TYPE = NCNAME | HYPH_TYPE | ENUM_TYPE
                   ,STRING_TYPE = STRING | NAME_TYPE
                      ,ANY_TYPE = ~0
                                ;

    /** Constant specifying an initial data type. */
    public static final int
                      NOTYPE_IT = 0
                    ,INTEGER_IT = 1
                     ,NUMBER_IT = 2
                     ,LENGTH_IT = 4
                      ,ANGLE_IT = 8
                 ,PERCENTAGE_IT = 16
                  ,CHARACTER_IT = 32
                    ,LITERAL_IT = 64
                     ,NCNAME_IT = 128
                      ,COLOR_IT = 256
                    ,COUNTRY_IT = 512
          ,URI_SPECIFICATION_IT = 1024
                       ,BOOL_IT = 2048
                       ,ENUM_IT = 4096
                       ,AUTO_IT = 8192
                       ,NONE_IT = 16384
                      ,AURAL_IT = 32768
            ,TEXT_DECORATION_IT = 65536
                   ,LANGUAGE_IT = 131072
                                ;

    /**
     * Bitmap set of initial data types for which getInitialType() must be
     * overriden.
     */
    public static final int
            USE_GET_IT_FUNCTION = //NOTYPE_IT performed in Property
                                    INTEGER_IT
                                  | NUMBER_IT
                                  | LENGTH_IT
                                  | ANGLE_IT
                                  | PERCENTAGE_IT
                                  | CHARACTER_IT
                                  | LITERAL_IT
                                  | NCNAME_IT
                                  | COLOR_IT
                                  | COUNTRY_IT
                                  | LANGUAGE_IT
                                  | URI_SPECIFICATION_IT
                                  | BOOL_IT
                                  | ENUM_IT
                                  //AUTO_IT  performed in Property
                                  //NONE_IT  performed in Property
                                  //AURAL_IT  performed in Property
                                  | TEXT_DECORATION_IT
                              ;

    /** Constant specifying mapping of property to trait. */
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

    /*
     * Constant specifying inheritance type.  Special cases (only line-height
     * specified as a &lt;number&gt;, so far) must be handled close to the
     * usage point of the property.  For line-height, the number is retained
     * as the specified and as the computed property value.  Because the
     * current font-size will always be present in the property set for any
     * FONode which requires line-height, the actual length value of the
     * line-height can always be calculated at the point of application.
     */
    /** Constant specifying inheritance type.  */
    public static final int
                             NO = 0
                      ,COMPUTED = 1
                              ;

    public static final int dataTypes = NOTYPE;

    public int getDataTypes() {
        return dataTypes;
    }
    public static final int initialValueType = NOTYPE_IT;

    public int getInitialValueType() {
        return initialValueType;
    }
    
    public static final int inherited = NO;

    public int getInherited() {
        return inherited;
    }
    
    private static final int traitMapping = NO_TRAIT;
    
    public int getTraitMapping() {
        return traitMapping;
    }

    public static Map enumHash = null;

    /**
     * Is this a corresponding absolute property?
     * Such properties must override this method.
     * @return answer
     */
    public boolean isCorrespondingAbsolute() {
        return false;
    }

    /**
     * Is this a corresponding relative property?
     * Such properties must override this method.
     * @return answer
     */
    public boolean isCorrespondingRelative() {
        return false;
    }

    public Property() {}

    /**
     * Form a string representation of bitmap of datatypes.
     * @param datatypes - a bitmap of datatype(s).
     * @return <tt>String</tt> containing a list of text names of datatypes
     * found in the bitmap.  Individual names are enclosed in angle brackets
     * and separated by a vertical bar.  Psuedo-datatypes are in upper case.
     * @exception PropertyException if no matches are found.
     */
    public static String listDataTypes(int datatypes)
                    throws PropertyException
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
        if ((datatypes & NCNAME) != 0) typeNames += "<ncname>|";
        if ((datatypes & COLOR_T) != 0) typeNames += "<color>|";
        if ((datatypes & COUNTRY_T) != 0) typeNames += "<country>|";
        if ((datatypes & LANGUAGE_T) != 0) typeNames += "<language>|";
        if ((datatypes & SCRIPT_T) != 0) typeNames += "<script>|";
        if ((datatypes & URI_SPECIFICATION) != 0) typeNames
                                                    += "<uri-specification>|";
        if ((datatypes & TIME) != 0) typeNames += "<time>|";
        if ((datatypes & FREQUENCY) != 0) typeNames += "<frequency>|";
        if ((datatypes & BOOL) != 0) typeNames += "<BOOL>|";
        if ((datatypes & INHERIT) != 0) typeNames += "<INHERIT>|";
        if ((datatypes & ENUM) != 0) typeNames += "<ENUM>|";
        if ((datatypes & MAPPED_LENGTH) != 0) typeNames
                                                    += "<MAPPED_LENGTH>|";
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
     * Derive inherited value for the given property.
     * This method must be overriden by properties with special requirements.
     * @param foTree the <tt>FOTree</tt> being built
     * @param property the <tt>int</tt> property index
     * @return <tt>PropertyValue</tt> the inherited property value for the
     * property.  It contains the inherited <i>computed</i> value, and no
     * <i>specified</i> value.
     * @exception <tt>PropertyException</tt>
     */
     /*
    public static PropertyValue inheritance(FOTree foTree, int property)
            throws PropertyException
    {
        // Is it inherited?  This question is not asked here.  Should it be
        // determined in here or outside?
        return foTree.getCurrentInherited(property);
    }
    */

    /** Constant for nested <tt>refineParsing</tt> methods. */
    public static boolean IS_NESTED = true;

    /** Constant for non-nested <tt>refineParsing</tt> methods. */
    public static boolean NOT_NESTED = false;

    /**
     * The final stage of property expression parsing.
     * <ol>
     *   <li>PropertyTokenizer</li>
     *   <li>PropertyParser - returns context-free <tt>PropertyValue</tt>s
     *    recognizable by the parser</li>
     *   <li>refineParsing - verifies results from parser, translates
     *    property types like NCName into more specific value types,
     *    resolves enumeration types, etc.</li>
     * </ol>
     *
     * <p>This method is overriden by individual property classes which
     * require specific processing.
     * @param propindex - the <tt>int</tt> property index.
     * @param foNode - the <tt>FONode</tt> being built
     * @param value - <tt>PropertyValue</tt> returned by the parser
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
     * @param value - <tt>PropertyValue</tt> returned by the parser
     * @param nested - <tt>boolean</tt> indicating whether this method is
     * called normally (false), or as part of another <i>refineParsing</i>
     * method.
     * @see #refineParsing(int,FONode,PropertyValue)
     */
    public PropertyValue refineParsing(int propindex,
                        FONode foNode, PropertyValue value, boolean nested)
            throws PropertyException
    {
        //int propindex = value.getProperty();
        if (propindex != value.getProperty()) // DEBUG
            throw new PropertyException
                ("Mismatched property and value.property.");
        String propName = PropNames.getPropertyName(propindex);
        int proptype = value.getType();
        int dataTypes = PropertyConsts.pconsts.getDataTypes(propindex);
        PropertyValue pv;
        if ((dataTypes & AURAL) != 0)
            throw new PropertyNotImplementedException
                ("AURAL properties are not supported");
        switch (proptype) {
        case PropertyValue.NUMERIC:
            // Can be any of
            // INTEGER, FLOAT, LENGTH, PERCENTAGE, ANGLE, FREQUENCY or TIME
            if ((dataTypes & (INTEGER | FLOAT | LENGTH | PERCENTAGE
                                | ANGLE | FREQUENCY | TIME)) != 0)
                return value;
            throw new PropertyException
                            ("Numeric value invalid  for " + propName);
        case PropertyValue.INTEGER:
            if ((dataTypes & NUMBER) != 0)
                return value;
            throw new PropertyException
                    ("IntegerType value invalid for " + propName);
        case PropertyValue.NCNAME:
            String ncname = ((NCName)value).getNCName();
            // Can by any of
            // NCNAME, COUNTRY_T, LANGUAGE_T, SCRIPT_T, ENUM
            // MAPPED_LENGTH or CHARACTER_T
            if ((dataTypes & (NCNAME | CHARACTER_T)) != 0)
                return value;
            if ((dataTypes & COUNTRY_T) != 0)
                return new CountryType(propindex, ncname);
            if ((dataTypes & LANGUAGE_T) != 0)
                return new LanguageType(propindex, ncname);
            if ((dataTypes & SCRIPT_T) != 0)
                return new ScriptType(propindex, ncname);
            if ((dataTypes & ENUM) != 0)
                return new EnumType(propindex, ncname);
            if ((dataTypes & MAPPED_LENGTH) != 0)
                return (new MappedNumeric
                            (foNode, propindex, ncname)).getMappedNumValue();
            throw new PropertyException
                            ("NCName value invalid  for " + propName);
        case PropertyValue.ENUM:
            if ((dataTypes & ENUM) != 0) return value;
            throw new PropertyException
                    ( "Enumerated value invalid for " + propName);
        case PropertyValue.LITERAL:
            // Can be LITERAL or CHARACTER_T
            if ((dataTypes & (LITERAL | CHARACTER_T)) != 0) return value;
            throw new PropertyException
                            ("Literal value invalid  for " + propName);
        case PropertyValue.AUTO:
            if ((dataTypes & AUTO) != 0) return value;
            throw new PropertyException("'auto' invalid  for " + propName);
        case PropertyValue.BOOL:
            if ((dataTypes & BOOL) != 0) return value;
            throw new PropertyException
                                ("Boolean value invalid for " + propName);
        case PropertyValue.COLOR_TYPE:
            // Can be COLOR_T or COLOR_TRANS
            if ((dataTypes & (COLOR_T | COLOR_TRANS)) != 0) return value;
            throw new PropertyException("'none' invalid  for " + propName);
        case PropertyValue.NONE:
            // Some instances of 'none' are part of an enumeration, but
            // the parser will find and return 'none' as a None
            // PropertyValue.
            // In these cases, the individual property's refineParsing
            // method must override this method.
            if ((dataTypes & NONE) != 0) return value;
            throw new PropertyException("'none' invalid  for " + propName);
        case PropertyValue.URI_TYPE:
            if ((dataTypes & URI_SPECIFICATION) != 0) return value;
            throw new PropertyException("uri invalid for " + propName);
        case PropertyValue.MIME_TYPE:
            if ((dataTypes & MIMETYPE) != 0) return value;
            throw new PropertyException
                        ("mimetype invalid for " + propName);
        // The following types cannot have their values validated in advance.
        // The result must be validated from within the property type.
        case PropertyValue.FROM_PARENT:
            pv = ((FromParent)value).resolve(foNode);
            if (pv == value) return value;  // unable to resolve
            // TODO: validate here
            return pv;
        case PropertyValue.FROM_NEAREST_SPECIFIED:
            pv = ((FromNearestSpecified)value).resolve(foNode);
            if (pv == value) return value;  // unable to resolve
            // TODO: validate here
            return pv;
        case PropertyValue.INHERITED_VALUE:
            pv = ((InheritedValue)value).resolve(foNode);
            if (pv == value) return value;  // unable to resolve
            // TODO: validate here
            return pv;
        case PropertyValue.LIST:
            throw new PropertyException
                ("PropertyValueList passed to Property.refineParsing for "
                + propName + "\n" + value.toString());
        default:
            // The COMPOUND test was orginally protected by the
            // if ( ! nested) fence.  Only within Font, Border and
            // Background shorthands is refineParsing called with a
            // nested value of 'true'.  This may cause problems, in which case
            // the COMPOUND processing will have to be repeated within the
            // (property instanceof CorrespondingProperty) case.
            if ((dataTypes & COMPOUND) != 0)
                return ShorthandPropSets.expandCompoundProperty
                                        (foNode.getFOTree(), value);
            if ( ! nested) {
                int correspIndex = 0;
                Property property =
                    PropertyConsts.pconsts.getProperty(propindex);
                if (property instanceof CorrespondingProperty) {
                    correspIndex =
                        ((CorrespondingProperty)property)
                        .getCorrespondingProperty(foNode);
                    // Note - can't call refineParsing recursively to resolve
                    // corresponding compounds, because the compound is itself
                    // a corresponding property
                    // Create a list, containing this PropertyValue on
                    // the original property, plus the value on the
                    // expansion of the corresponding property
                    PropertyValueList newlist =
                        new PropertyValueList(propindex);
                    newlist.add(value);
                    PropertyValue corresPv = null;
                    try {
                        corresPv = (PropertyValue)(value.clone());
                    } catch (CloneNotSupportedException e) {
                        throw new PropertyException(e.getMessage());
                    }
                    corresPv.setProperty(correspIndex);
                    corresPv = PropertyConsts.pconsts.refineParsing(
                            corresPv.getProperty(), foNode,
                            corresPv, IS_NESTED);
//                  if it's a list, recursively refine.  This will return a list
                    if (corresPv.getType() == PropertyValue.LIST) {
                        PropertyValueList pvl =
                            refineExpansionList(
                                    corresPv.getProperty(), foNode,
                                    (PropertyValueList)corresPv);
                        newlist.addAll(pvl);
                    } else { // single element
                        newlist.add(corresPv);
                    }
                    return newlist;
                }
                if (proptype == PropertyValue.INHERIT) {
                    if ((dataTypes & INHERIT) != 0)
                        return ((Inherit)value).resolve(foNode);
                    throw new PropertyException
                                    ("'inherit' invalid for " + propName);
                }
            }
            throw new PropertyException
                ("Inappropriate dataTypes passed to Property.refineParsing: "
                    + value.getClass().getName());
        }
    }

    /**
     * Refine a list of property values against their properties.
     * Expansion lists are generated by shorthand and compound expansion.
     * The set of properties will, in general, be different from the
     * generating property, which will be the one associated with the list
     * itself.
     * @param propindex - the <tt>int</tt> property index.
     * @param foNode - the <tt>FONode</tt> for which the properties are
     * being processed.
     * @param list - the list of <tt>PropertyValues</tt> to be refined.
     * @return a <tt>PropertyValueList>/tt> containing the refined property
     * values.
     */
    public PropertyValueList refineExpansionList
                        (int propindex, FONode foNode, PropertyValueList list)
            throws PropertyException
    {
        if (propindex != list.getProperty()) // DEBUG
            throw new PropertyException
                ("Mismatch between propindex and list property.");
        PropertyValueList newlist = new PropertyValueList(list.getProperty());
        Iterator properties = list.iterator();
        while (properties.hasNext()) {
            // refine the next element
            PropertyValue pv = (PropertyValue)(properties.next());
            pv = PropertyConsts.pconsts.refineParsing
                                            (pv.getProperty(), foNode, pv);
            // if it's a list, recursively refine.  This will return a list
            if (pv.getType() == PropertyValue.LIST) {
                PropertyValueList pvl = refineExpansionList
                        (pv.getProperty(), foNode, (PropertyValueList)pv);
                newlist.addAll(pvl);
            } else { // single element
                newlist.add(pv);
            }
        }
        return newlist;
    }

    /**
     * Determine whether argument <i>list</i> contains a space-separated list
     * from the parser.
     * A space-separated list will be represented by a
     * <tt>PropertyValueList</tt> as the only element of the argument
     * <tt>PropertyValueList</tt>.
     * @param list - the containing list.
     * @return the contained space-separated list.
     * @throws PropertyException if <i>list</i> contains more than
     * one element or if the contained element is not a list.
     */
    protected PropertyValueList spaceSeparatedList (PropertyValueList list)
            throws PropertyException
    {
        if (list.size() != 1)
                throw new PropertyException
                        (list.getClass().getName() + " list is not a "
                                + "single list of space-separated values");
        PropertyValue val2 = (PropertyValue)(list.getFirst());
        if ( ! (val2.getType() == PropertyValue.LIST))
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
     * @param type <tt>String</tt> type of expected enumval - for
     * exception messages only
     * @return <tt>EnumValue</tt> equivalent of the argument
     * @exception PropertyException
     */
    protected EnumType getEnum(PropertyValue value,
                                            int property, String type)
            throws PropertyException
    {
        if (value.getType() != PropertyValue.NCNAME)
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
     * Get the <tt>int</tt> index corresponding to an enumeration keyword
     * for this property.
     * @param enumval - a <tt>String</tt> containing the enumeration text.
     * @return <tt>int</tt> constant representing the enumeration value.
     * @exception PropertyException
     */
    public int getEnumIndex(String enumval)
            throws PropertyException
    {
        throw new PropertyException("ENUM not supported.");
    }

    /**
     * @param enumIndex <tt>int</tt> containing the enumeration index.
     * @return <tt>String</tt> containing the enumeration text.
     * @exception PropertyException
     */
    public String getEnumText(int enumIndex)
                    throws PropertyException
    {
        throw new PropertyException("ENUM not supported.");
    }

    /**
     * Map the String value of an enumval to its integer equivalent.
     * @param value the enumval text
     * @param values an <tt>ROStringArray</tt> of all of the enumval text values.
     * This array is effectively 1-based.
     * @return the integer equivalent of the enumval text
     * @exception PropertyException if the enumval text is not valid.
     */
    public int enumValueToIndex(String value, String[] values)
                throws PropertyException
    {
        for (int i = 1; i < values.length; i++) {
            if (value.equals(values[i])) {
                return i;
            }
        }
        throw new PropertyException("Enum text " + value +" not found.");
    }

    /**
     * Convert an enumeration index value to a length.  This is the
     * fallback implementation of this function for properties which do not
     * support a MAPPED_LENGTH type.  Those which do must override this
     * method.
     * @param enumval - the enumeration index.
     * @return a <tt>Numeric</tt>.  This implementation never returns.
     * @throws PropertyException
     */
    public Numeric getMappedLength(FONode node, int enumval)
            throws PropertyException
    {
        throw new PropertyException
            ("MAPPED_LENGTH not supported.");
    }

    /**
     * Fallback getInitialValue function.  This function only handles
     * those initial value types NOT in the set USE_GET_IT_FUNCTION.  It
     * should be overriden by all properties whose initial values come from
     * that set.
     * @param property <tt>int</tt> property index
     * @return <tt>PropertyValue</tt>
     * @exception PropertyException
     * @exception PropertyNotImplementedException
     */
    public PropertyValue getInitialValue(int property)
            throws PropertyException
    {
        int initialValueType =
                    PropertyConsts.pconsts.getInitialValueType(property);
        logger.fine("In Property getInitialValue property " + property);
        if ((initialValueType & Property.USE_GET_IT_FUNCTION) != 0)
             throw new PropertyException
                 ("Property.getInitialValue() called for property with "
                 + "initial value type in USE_GET_IT_FUNCTION : "
                 + property + " "
                 + PropNames.getPropertyName(property));
        switch (initialValueType) {
        case NOTYPE_IT:
            return new NoType(property);
        case AUTO_IT:
            return new Auto(property);
        case NONE_IT:
            return new None(property);
        case AURAL_IT:
            // TODO Arrange to ignore and log PropertyNotImplemented
            throw new PropertyNotImplementedException
                ("Aural properties not implemented: "
                + PropNames.getPropertyName(property));
        default:
            throw new PropertyException
                ("Unexpected initial value type " + initialValueType
                + " for " + PropNames.getPropertyName(property));
        }
    }

}
