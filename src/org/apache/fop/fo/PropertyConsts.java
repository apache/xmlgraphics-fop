/**
 * $Id$
 * <br/>Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * <br/>For details on use and redistribution please refer to the
 * <br/>LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.fo;

import java.lang.Character;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.BitSet;
import java.util.StringTokenizer;

import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.Properties;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datastructs.ROIntArray;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.datastructs.ROClassArray;
import org.apache.fop.datastructs.ROMethodArray;
import org.apache.fop.fo.expr.PropertyValue;

/**
 * <p>
 * This class contains a number of arrays containing values indexed by the
 * property index value, determined from the PropNames class.  These arrays
 * provide a means of accessing information about the nature of a property
 * through the property index value.
 * </p><p>
 * Most of the values in the property-indexed arrays are initialised at run
 * time through the static{} initializers contained in this class.  This
 * process is not essential; much of the initialization could be done at
 * compile time by directly initializing individual elements of the arrays,
 * but the arrays would each then have to be kept in sync with one another
 * and with the propertyNames array, and the list of property index constants
 * in the PropNames class.  This would greatly increase the risk of errors
 * in the initialization.  Speed of startup is here being traded for
 * robustness.
 * </p><p>
 * There are also <tt>HashMap</tt>s which encode the various sets of
 * properties which are defined to apply to each of the Flow Objects,
 * and a <tt>BitSet</tt> of those properties which are <i>not</i>
 * automatically inherited.  The <tt>HashMap</tt>s provide a convenient
 * means of specifying the relationship between FOs and properties.
 */
public class PropertyConsts {

    private static final String packageName = "org.apache.fop.fo";

    /**
     * @param property <tt>String</tt> name of the FO property
     * @return <tt>int</tt> index of the named FO property in the array of
     * property names.
     * @exception PropertyException if the property name is not found.
     */
    public static int getPropertyIndex(String property)
                throws PropertyException
    {
        Integer integer = (Integer)toIndex.get((Object)property);
        if (integer == null)
            throw new PropertyException
                                    ("Property " + property + " not found.");
        return integer.intValue();
    }

    /**
     * @param propertyClassName String name of the FO property class
     * @return int index of the named FO property class in the array of
     * property class names.
     * @exception PropertyException if the property class name is not found.
     */
    public static int getPropertyClassIndex(String propertyClass)
                throws PropertyException
    {
        Integer integer =
                    (Integer)classToIndex.get((Object)propertyClass);
        if (integer == null)
            throw new PropertyException
                        ("Property class " + propertyClass + " not found.");
        return integer.intValue();
    }

    /**
     * @param property String name of the FO property
     * @return int enumerated initialValueType.  These constants are defined
     * as static final ints in this class.  Note that an undefined property
     * name will return the constant defined as NOTYPE_IT
     */
    public static int getInitialValueType(String property)
                    throws PropertyException
    {
        // Get the property index then index into the initialvaluetypes array
        return initialValueTypes[getPropertyIndex(property)];
    }

    /**
     * @param propertyIndex int index of the FO property
     * @return int enumerated initialValueType.  These constants are defined
     * as static final ints in this class.  Note that an undefined property
     * name will return the constant defined as NOTYPE_IT
     */
    public static int getInitialValueType(int propertyIndex) {
        return initialValueTypes[propertyIndex];
    }

    /**
     * @param property String name of the FO property
     * @return int type of inheritance for this property
     * (See constants defined in Properties.)
     */
    public static int inheritance(String property)
                throws PropertyException
    {
        return inherit[getPropertyIndex(property)];
    }

    /**
     * @param propertyIndex int index of the FO property
     * @return int type of inheritance for this property
     * (See constants defined in Properties.)
     */
    public static int inheritance(int propertyIndex) {
        return inherit[propertyIndex];
    }

    /**
     * @return <tt>BitSet</tt> of non-inherited properties
     */
    public static BitSet getNonInheritedSet() {
        return (BitSet)nonInheritedProps.clone();
    }

    /**
     * @param propertyIndex int index of the FO property
     * @return <tt>boolean</tt> is property a shorthand?
     */
    public static boolean isShorthand(int propertyIndex) {
        return (datatypes[propertyIndex] & Properties.SHORTHAND) != 0;
    }

    /**
     * @param property String name of the FO property
     * @return <tt>boolean</tt> is property a shorthand?
     */
    public static boolean isShorthand(String property)
                throws PropertyException
    {
        return (datatypes[getPropertyIndex(property)] & Properties.SHORTHAND)
                    != 0;
    }

    /**
     * Map the String value of an enum to its integer equivalent.
     * @param value the enum text
     * @param values an <tt>ROStringArray</tt> of all of the enum text values.
     * This array is effectively 1-based.
     * @return the integer equivalent of the enum text
     * @exception PropertyException if the enum text is not valid.
     */
    static int enumValueToIndex(String value, ROStringArray values)
                throws PropertyException
    {
        for (int i = 1; i < values.length; i++) {
            if (value.equals(values.get(i))) {
                return i;
            }
        }
        throw new PropertyException("Enum text " + value +" not found.");
    }

    /**
     * Map the String value of an enum to its integer equivalent.
     * @param value the enum text
     * @param valuesMap a <tt>Map</tt> mapping enum text to integer
     * equivalent
     * @return the integer equivalent of the enum text
     * @exception PropertyException if the enum text is not valid.
     */
    static int enumValueToIndex(String value, Map valuesMap)
                throws PropertyException
    {
        Integer i = (Integer) valuesMap.get((Object) value);
        if (i == null)
            throw new PropertyException("Enum text " + value +" not found.");
        return i.intValue();
    }

    /**
     * Map the integer value of an enum into its mapped value.
     * Only valid when the datatype of the property includes MAPPED_ENUM.
     * <p>Generally, the path will be enumText->enumIndex->mappedEnumText.
     * @param index <tt>int</tt> containing the enumeration index.
     * @param enumMap an <tt>ROStringArray</tt> of the <tt>String</tt>s 
     * with the mapped enumeration values.
     * @return a <tt>String</tt> with the mapped enumeration text.
     */
    static String enumIndexToMapping(int index, ROStringArray enumMap) {
        return enumMap.get(index);
    }

    /**
     * @param property <tt>int</tt> property index.
     * @param enum <tt>String</tt> containing the enumeration text.
     * @return <tt>int</tt> constant representing the enumeration value.
     * @exception PropertyException
     */
    public static int getEnumIndex(int property, String enum)
                    throws PropertyException
    {
        // Get the object represented by the enumValues field in the
        // property class
        Object values;
        try {
            values
                = classes[property].getField("enumValues").get(null);
        }
        catch (NoSuchFieldException e) {
            throw new PropertyException(
                        "Missing field \"" + e.getMessage() + "\""
                        + " in class " + classNames[property]);
        }
        catch (IllegalAccessException e) {
            throw new PropertyException(
                "Illegal access on \"" + e.getMessage() + "\" in class " +
                classNames[property]);
        }
        if (values instanceof Map)
                    return enumValueToIndex(enum, (Map)values);
        return enumValueToIndex(enum, (ROStringArray)values);
    }

    /**
     * @param property <tt>int</tt> property index.
     * @param enumIndex <tt>int</tt> containing the enumeration index.
     * @return <tt>String</tt> containing the enumeration text.
     * @exception PropertyException
     */
    public static String getEnumText(int property, int enumIndex)
                    throws PropertyException
    {
        // Get the object represented by the enumValues field in the
        // property class
        Object enums;
        try {
            enums
                = classes[property].getField("enums").get(null);
        }
        catch (NoSuchFieldException e) {
            throw new PropertyException(
                        "Missing field \"" + e.getMessage() + "\""
                        + " in class " + classNames[property]);
        }
        catch (IllegalAccessException e) {
            throw new PropertyException(
                "Illegal access on \"" + e.getMessage() + "\" in class " +
                classNames[property]);
        }
        return ((ROStringArray)enums).get(enumIndex);
    }

    /**
     * @param property <tt>int</tt> property index.
     * @param enumIndex int containing the enumeration index.
     * @return <tt>String</tt> containing the mapped enumeration value.
     * @exception PropertyException
     */
    public static String getMappedEnumValue(int property, int enumIndex)
                    throws PropertyException
    {
        // Get the object represented by the enumMappings field in the
        // property class
        Object values;
        try {
            values =
                classes[property].getField("enumMappings").get(null);
        }
        catch (NoSuchFieldException e) {
            throw new PropertyException(
                        "Missing field \"" + e.getMessage() + "\""
                        + " in class " + classNames[property]);
        }
        catch (IllegalAccessException e) {
            throw new PropertyException(
                "Illegal access on \"" + e.getMessage() + "\" in class " +
                classNames[property]);
        }
        return enumIndexToMapping(enumIndex, (ROStringArray)values);
    }

    /**
     * A String[] array of the property class names.  This array is
     * effectively 1-based, with the first element being unused.
     * The array is initialized in a static initializer by converting the FO
     * property names from the array PropNames.propertyNames into class
     * names by converting the first character of every component word to
     * upper case, and removing all punctuation characters.
     * It can be indexed by the property name constants defined in
     * the PropNames class.
     */
    private static final String[] classNames;

    /**
     * An ROStringArray of the property class names.  This read-only array
     * is derived from <i>classNames</i>, above.
     * It can be indexed by the property name constants defined in
     * the PropNames class.
     */
    public static final ROStringArray propertyClassNames;

    /**
     * A Class[] array containing Class objects corresponding to each of the
     * class names in the classNames array.  It is initialized in a
     * static initializer in parallel to the creation of the class names in
     * the classNames array.  It can be indexed by the property name
     * constants defined in this file.
     */
    private static final Class[] classes;

    /**
     * An ROClassArray of the property classes.  This read-only array
     * is derived fo <i>classes</i>, above.
     * It can be indexed by the property name constants defined in
     * the PropNames class.
     */
    public static final ROClassArray propertyClasses;

    /**
     * A HashMap whose elements are an integer index value keyed by a
     * property name.  The index value is the index of the property name in
     * the PropNames.propertyNames[] array.
     * It is initialized in a static initializer.
     */
    private static final HashMap toIndex;

    /**
     * An unmodifiable Map of property name to property index.  It is derived 
     * from the <tt>HashMap</tt> toIndex, above.
     */
    //public static final Map propertyToIndex;

    /**
     * A HashMap whose elements are an integer index value keyed by the name
     * of a property class.  the index value is the index of the property
     * class name in the classNames[] array.  It is initialized in a
     * static initializer.
     */
    private static final HashMap classToIndex;

    /**
     * An unmodifiable Map of property class name to property index.  It is
     * derived from the <tt>HashMap</tt> classToIndex, above.
     */
    //public static final Map propertyClassToIndex;

    /** <p>
     * An int[] array of values specifying the type of inheritance of a
     * property.  The array is indexed by the index value constants that are
     * defined in the PropNames class in parallel to the
     * PropNames.propertyNames[] array.
     * </p><p>
     * The array is initialized in a static initializer from the values of the
     * <i>inherited</i> field in each property class.
     */
    private static final int[] inherit;

    /**
     * An ROIntArray of the property <i>inherited</i> values.
     * This read-only array is derived from <i>inherit</i>, above.
     * It can be indexed by the property name constants defined in
     * the PropNames class.
     */
    public static final ROIntArray inherited;

    /**
     * A <tt>BitSet</tt> of properties which are not normally inherited.
     * It is defined relative to the set of all properties; i.e. the
     * non-inheritance of any property can be established by testing the
     * bit in this set that corresponds to the queried property's index.
     * <p>The <tt>BitSet</tt> is private.  An accessor method is defined
     * which returns a clone of this set.
     */
    private static final BitSet nonInheritedProps;

    /** <p>
     * An int[] array of the types of the <i>initialValue</i> field of each
     * property.  The array is indexed by the index value constants that are
     * defined in the PropNames class in parallel to the
     * PropNames.propertyNames[] array.
     * </p><p>
     * The array is initialized in a static initializer from the values of the
     * <i>initialValueType</i> field in each property class.
     */
    private static final int[] initialValueTypes;

    /** <p>
     * An int[] array of the values of the <i>traitMapping</i> field of each
     * property.  The array is indexed by the index value constants that are
     * defined in the PropNames class in parallel to the
     * PropNames.propertyNames[] array.
     * </p><p>
     * The array is initialized in a static initializer from the values of the
     * <i>traitMapping</i> field in each property class.
     */
    private static final int[] traitMappings;

    /**
     * <p>An int[] array of the values of the <i>dataTypes</i> field of each
     * property.  The array is indexed by the index value constants that are
     * defined in the PropNames class in parallel to the
     * PropNames.propertyNames[] array.
     * </p><p>
     * The array is initialized in a static initializer from the values of the
     * <i>dataTypes</i> field in each property class.
     * </p>
     */
    private static final int[] datatypes;

    /**
     * An ROIntArray of the property <i>dataTypes</i> values.
     * This read-only array is derived from <i>datatypes</i>, above.
     * It can be indexed by the property name constants defined in
     * the PropNames class.
     */
    public static final ROIntArray dataTypes;

    /**
     * A sparsely populated array of <tt>Method</tt> objects.  Although this
     * array has a slot for every property, only positions corresponding to
     * properties which have a <i>complex()</i> method for processing
     * complex property value specifications, will hold a valid
     * <tt>Method</tt> object.
     */
    private static final Method[] complexmethods;

    /**
     * An ROMethodArray of the property <i>complex</i> methods.
     * This read-only array is derived from <i>complexmethods</i>, above.
     * It can be indexed by the property name constants defined in
     * the PropNames class.
     */
    public static final ROMethodArray complexMethods;

    /**
     * A sparsely populated array of <tt>Method</tt> objects.  Although this
     * array has a slot for every property, only positions corresponding to
     * properties which have a <i>setInitialValue()</i> method for creating
     * initial property value objects, will hold a valid
     * <tt>Method</tt> object.
     */
    private static final Method[] initialvaluemethods;

    /**
     * An ROMethodArray of the property <i>setInitialValue</i> methods.
     * This read-only array is derived from <i>initialvaluemethods</i>, above.
     * It can be indexed by the property name constants defined in
     * the PropNames class.
     */
    public static final ROMethodArray initialValueMethods;

    static {
        String prefix = packageName + "." + "Properties" + "$";
        String cname = "";

        classNames   = new String[PropNames.LAST_PROPERTY_INDEX + 1];
        toIndex      = new HashMap(PropNames.LAST_PROPERTY_INDEX + 1);
        classToIndex = new HashMap(PropNames.LAST_PROPERTY_INDEX + 1);
        inherit            = new int[PropNames.LAST_PROPERTY_INDEX + 1];
        nonInheritedProps  = new BitSet(PropNames.LAST_PROPERTY_INDEX + 1);
        initialValueTypes  = new int[PropNames.LAST_PROPERTY_INDEX + 1];
        traitMappings      = new int[PropNames.LAST_PROPERTY_INDEX + 1];
        datatypes          = new int[PropNames.LAST_PROPERTY_INDEX + 1];
        classes            = new Class[PropNames.LAST_PROPERTY_INDEX + 1];
        complexmethods     = new Method[PropNames.LAST_PROPERTY_INDEX + 1];
        initialvaluemethods = new Method[PropNames.LAST_PROPERTY_INDEX + 1];

        for (int i = 0; i <= PropNames.LAST_PROPERTY_INDEX; i++) {
            cname = "";

            // Set the array of property class names
            StringTokenizer stoke;
            try {
                stoke = new StringTokenizer
                                        (PropNames.getPropertyName(i), "-.:");
            } catch (PropertyException e) {
                throw new RuntimeException(e.getMessage());
            }
            while (stoke.hasMoreTokens()) {
                String token = stoke.nextToken();
                String pname = new Character(
                                    Character.toUpperCase(token.charAt(0))
                                ).toString() + token.substring(1);
                cname = cname + pname;
            }
            classNames[i] = cname;

            // Set up the array of Class objects representing each of the
            //  member classes of the Properties class
            String name = prefix + cname;
            try {
                //System.out.println("classes["+i+"] "+name);//DEBUG
                classes[i] = Class.forName(name);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(
                                "Class " + name + " could not be found.");
            }

            // Set up the toIndex Hashmap with the name of the
            // property as a key, and the integer index as a value
            
            try {
                if (toIndex.put((Object) PropNames.getPropertyName(i),
                                        Ints.consts.get(i)) != null) {
                    throw new RuntimeException(
                        "Duplicate values in toIndex for key " +
                        PropNames.getPropertyName(i));
                }
            } catch (PropertyException e) {
                throw new RuntimeException(e.getMessage());
            }

            // Set up the classToIndex Hashmap with the name of the
            // property class as a key, and the integer index as a value
            
            if (classToIndex.put((Object) classNames[i],
                                    Ints.consts.get(i)) != null) {
                throw new RuntimeException(
                    "Duplicate values in classToIndex for key " +
                    classNames[i]);
            }

            try {
                Class vclass = classes[i];
                cname = vclass.getName();
                inherit[i] = classes[i].getField("inherited").getInt(null);
                if (inherit[i] == Properties.NO) nonInheritedProps.set(i);
                initialValueTypes[i] =
                classes[i].getField("initialValueType").getInt(null);
                traitMappings[i] =
                classes[i].getField("traitMapping").getInt(null);
                datatypes[i] = classes[i].getField("dataTypes").getInt(null);
                if ((datatypes[i] & Properties.COMPLEX) != 0)
                    complexmethods[i] =
                            classes[i].getMethod
                                        ("complex", new Class[]
                                                    {int.class,
                                                     PropertyValue.class});
                if ((initialValueTypes[i] & Properties.USE_SET_FUNCTION_IT)
                                        != 0)
                    initialvaluemethods[i] =
                            classes[i].getMethod
                                    ("setInitialValue",
                                     new Class[]
                                        {org.apache.fop.fo.FOTree.class});
            }
            catch (NoSuchFieldException e) {
                throw new RuntimeException(
                            "Missing field \"" + e.getMessage() + "\""
                            + " in class " + cname);
            }
            catch (NoSuchMethodException e) {
                throw new RuntimeException(
                            "Missing method \"" + e.getMessage() + "\""
                            + " in class " + cname);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(
                    "Illegal access on \"" + e.getMessage() + "\" in class " +
                    cname);
            }

        }

        // Initialise the RO arrays
        propertyClassNames  = new ROStringArray(classNames);
        propertyClasses     = new ROClassArray(classes);
        inherited           = new ROIntArray(inherit);
        dataTypes           = new ROIntArray(datatypes);
        complexMethods      = new ROMethodArray(complexmethods);
        initialValueMethods = new ROMethodArray(initialvaluemethods);

    }


    private PropertyConsts (){}

}

