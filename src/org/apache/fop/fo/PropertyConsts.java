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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.BitSet;
import java.util.StringTokenizer;

import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datastructs.ROIntArray;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.datastructs.ROBitSet;
import org.apache.fop.datatypes.PropertyValue;

/**
 * <p>
 * This class contains a number of arrays containing values indexed by the
 * property index value, determined from the PropNames class.  These arrays
 * provide a means of accessing information about the nature of a property
 * through the property index value.
 * <p>Most of these arrays are initialised piecemeal as information is
 * required about a particular property.
 * There are also <tt>HashMap</tt>s which encode the various sets of
 * properties which are defined to apply to each of the Flow Objects,
 * and a <tt>BitSet</tt> of those properties which are
 * automatically inherited.  The <tt>HashMap</tt>s provide a convenient
 * means of specifying the relationship between FOs and properties.
 */
public class PropertyConsts {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    private static final String packageName = "org.apache.fop.fo";

    private static final PropertyConsts pconsts;
    static {
        try {
            pconsts = new PropertyConsts();
        } catch (PropertyException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static final PropertyConsts getPropertyConsts() {
        return pconsts;
    }


    /**
     * A Property[] array containing Property objects corresponding to each
     * of the property indices in <tt>PropNames</tt>.
     * Initially empty, entries are filled on demand as calls for details
     * about individual properties are made.
     */
    private final Property[] properties
                        = new Property[PropNames.LAST_PROPERTY_INDEX + 1];

    /**
     * A Class[] array containing Class objects corresponding to each of the
     * class names in the classNames array.  It is initialized in a
     * static initializer in parallel to the creation of the class names in
     * the classNames array.  It can be indexed by the property name
     * constants defined in this file.
     */
    private final Class[] classes
                            = new Class[PropNames.LAST_PROPERTY_INDEX + 1];

    /**
     * A HashMap whose elements are an integer index value keyed by a
     * property name.  The index value is the index of the property name in
     * the PropNames.propertyNames[] array.
     * It is initialized in a static initializer.
     */
    private final HashMap toIndex
                        = new HashMap(PropNames.LAST_PROPERTY_INDEX + 1);

    /**
     * An <tt>int[]</tt> containing the <i>inherited</i> values from the
     * <tt>Property</tt> classes.
     */
    private final int[] inherited
                            = new int[PropNames.LAST_PROPERTY_INDEX + 1];

    /**
     * A <tt>BitSet</tt> of properties which are normally inherited
     * (strictly, not not inherited).
     * It is defined relative to the set of all properties; i.e. the
     * inheritability of any property can be established by testing the
     * bit in this set that corresponds to the queried property's index.
     * <p>The <tt>BitSet</tt> is private and is the basis for
     * <i>inheritedProperties</i>.
     */
    private final BitSet inheritedprops
                            = new BitSet(PropNames.LAST_PROPERTY_INDEX + 1);

    /**
     * An int[] array of the types of the <i>initialValue</i> field of each
     * property.  The array is indexed by the index value constants that are
     * defined in the PropNames class in parallel to the
     * PropNames.propertyNames[] array.
     */
    private final int[] initialValueTypes
                            = new int[PropNames.LAST_PROPERTY_INDEX + 1];

    /**
     * An int[] array of the values of the <i>dataTypes</i> field of each
     * property.  The array is indexed by the index value constants that are
     * defined in the PropNames class in parallel to the
     * PropNames.propertyNames[] array.
     * <p>
     * The array is initialized in a static initializer from the values of the
     * <i>dataTypes</i> field in each property class.
     */
    private final int[] datatypes
                            = new int[PropNames.LAST_PROPERTY_INDEX + 1];

    /**
     * Get the initial value type for a property name.
     * @param property String name of the FO property
     * @return int enumerated initialValueType.  These constants are defined
     * as static final ints in this class.  Note that an undefined property
     * name will return the constant defined as NOTYPE_IT
     */
    public int getInitialValueType(String property)
                    throws PropertyException
    {
        // Get the property index then index into the initialvaluetypes array
        return getInitialValueType(PropNames.getPropertyIndex(property));
    }

    /**
     * get the initial value type for a property index.
     * @param propindex int index of the FO property
     * @return int enumerated initialValueType.  These constants are defined
     * as static final ints in the Property class.
     * @throws PropertyException
     */
    public int getInitialValueType(int propindex)
            throws PropertyException
    {
        setupProperty(propindex);
        return initialValueTypes[propindex];
    }

    /**
     * Get the initial value for a property index.
     * @param propindex <tt>int</tt> index of the property
     * @return <tt>PropertyValue</tt> from property's <i>getInitialValue</i>
     * method
     * @exception <tt>PropertyException</tt>
     */
    public PropertyValue getInitialValue(int propindex)
            throws PropertyException
    {
        Property property = setupProperty(propindex);
        return property.getInitialValue(propindex);
    }

    /**
     * @param foNode the node whose properties are being constructed.
     * @param value the <tt>PropertyValue</tt> being refined.
     * @return <tt>PropertyValue</tt> constructed by the property's
     * <i>refineParsing</i> method
     * @exception <tt>PropertyException</tt>
     */
    public PropertyValue refineParsing(FONode foNode, PropertyValue value)
        throws PropertyException
    {
        int propindex = value.getProperty();
        Property property = setupProperty(propindex);
        return property.refineParsing(foNode, value);
    }

    /**
     * Get the <tt>Numeric</tt> value corresponding to an enumerated value.
     * @param propindex int index of the FO property
     * @param enum - the integer equivalent of the enumeration keyword.
     * @return the <tt>Numeric</tt> result.
     * @throws PropertyException.
     */
    public Numeric getMappedNumeric(int propindex, int enum)
            throws PropertyException
    {
        Property property = setupProperty(propindex);
        if ((datatypes[propindex] & Property.MAPPED_LENGTH) != 0)
            return property.getMappedLength(enum);
        else
            throw new PropertyException
                ("MAPPED_LENGTH not valid in "
                                    + PropNames.getPropertyName(propindex));
    }

    /**
     * @param propindex int index of the FO property
     * @return int type of inheritance for this property
     * (See constants defined in Properties.)
     * @throws PropertyException.
     */
    public int inheritance(String property) throws PropertyException {
        return inheritance(PropNames.getPropertyIndex(property));
    }

    /**
     * @param propindex int index of the FO property
     * @return int type of inheritance for this property
     * (See constants defined in Property.)
     * @throws PropertyException.
     */
    public int inheritance(int propindex) throws PropertyException {
        setupProperty(propindex);
        return inherited[propindex];
    }

    /**
     * @param propindex int index of the FO property
     * @return <tt>boolean</tt> is property inherited?
     * @throws PropertyException.
     */
    public boolean isInherited(int propindex) throws PropertyException {
        Property property = setupProperty(propindex);
        return (inherited[propindex] & Property.NO) == 0;
    }

    /**
     * @param property String name of the FO property
     * @return <tt>boolean</tt> is property inherited?
     * @throws PropertyException.
     */
    public boolean isInherited(String property) throws PropertyException {
        return isInherited(PropNames.getPropertyIndex(property));
    }

    /**
     * @param propindex int index of the FO property
     * @return <tt>boolean</tt> is property a shorthand?
     * @throws PropertyException.
     */
    public boolean isShorthand(int propindex) throws PropertyException {
        Property property = setupProperty(propindex);
        return (datatypes[propindex] & Property.SHORTHAND) != 0;
    }

    /**
     * @param property String name of the FO property
     * @return <tt>boolean</tt> is property a shorthand?
     * @throws PropertyException.
     */
    public boolean isShorthand(String property) throws PropertyException {
        return isShorthand(PropNames.getPropertyIndex(property));
    }

    /**
     * @param propertyIndex int index of the FO property
     * @return <tt>boolean</tt> is property a compound?
     * @throws PropertyException.
     */
    public boolean isCompound(int propertyIndex) throws PropertyException {
        Property property = setupProperty(propertyIndex);
        return (datatypes[propertyIndex] & Property.COMPOUND) != 0;
    }

    /**
     * @param property String name of the FO property
     * @return <tt>boolean</tt> is property a compound?
     * @throws PropertyException.
     */
    public boolean isCompound(String property) throws PropertyException {
        return isCompound(PropNames.getPropertyIndex(property));
    }

    /**
     * @param propertyIndex int index of the FO property
     * @return <tt>int</tt> dataTypes value.
     * @throws PropertyException.
     */
    public int getDataTypes(int propertyIndex) throws PropertyException {
        Property property = setupProperty(propertyIndex);
        return datatypes[propertyIndex];
    }

    /**
     * @param property String name of the FO property
     * @return <tt>int</tt> dataTypes value.
     * @throws PropertyException.
     */
    public int getDataTypes(String property) throws PropertyException {
        return getDataTypes(PropNames.getPropertyIndex(property));
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
    public String enumIndexToMapping(int index, ROStringArray enumMap)
    {
        return enumMap.get(index);
    }

    /**
     * @param propindex <tt>int</tt> property index.
     * @param enum <tt>String</tt> containing the enumeration text.
     * @return <tt>int</tt> constant representing the enumeration value.
     * @exception PropertyException
     */
    public int getEnumIndex(int propindex, String enum)
                    throws PropertyException
    {
        Property property = setupProperty(propindex);
        return property.getEnumIndex(enum);
    }

    /**
     * @param propindex <tt>int</tt> property index.
     * @param enumIndex <tt>int</tt> containing the enumeration index.
     * @return <tt>String</tt> containing the enumeration text.
     * @exception PropertyException
     */
    public String getEnumText(int propindex, int enumIndex)
                    throws PropertyException
    {
        Property property = setupProperty(propindex);
        return property.getEnumText(enumIndex);
    }

    /**
     * Set up the details of a single property and return the
     * <tt>Property</tt> object.  If the <tt>Property</tt> object
     * corresponding to the property index has not been resolved before,
     * derive the Class and Property objects, and extract certain field
     * values from the Property.
     * @param propindex - the <tt>int</tt> index.
     * @return - the <tt>Property</tt> corresponding to the index.
     * @throws <tt>PropertyException.
     */
    public Property setupProperty(int propindex)
            throws PropertyException
    {
        String cname = "";
        Class pclass;
        Property property;

        if ((property = properties[propindex]) != null) return property;

        // Get the property class name
        StringTokenizer stoke;
        stoke = new StringTokenizer
                            (PropNames.getPropertyName(propindex), "-.:");
        while (stoke.hasMoreTokens()) {
            String token = stoke.nextToken();
            String pname = new Character(
                                Character.toUpperCase(token.charAt(0))
                            ).toString() + token.substring(1);
            cname = cname + pname;
        }
        //classNames[propindex] = cname;

        // Get the class for this property name
        String name = packageName + "." + cname;
        try {
            //System.out.println("classes["+propindex+"] "+name);//DEBUG
            pclass = Class.forName(name);
            classes[propindex] = pclass;

            // Instantiate the class
            property = (Property)(pclass.newInstance());
            properties[propindex] = property;

            // Set inheritance value
            if ((inherited[propindex] = property.inherited) != Property.NO)
                        inheritedprops.set(propindex);
            // Set datatypes
            datatypes[propindex] = property.dataTypes;

            // Set initialValueTypes
            initialValueTypes[propindex] = property.initialValueType;

        } catch (ClassNotFoundException e) {
            throw new PropertyException(e);
        } catch (IllegalAccessException e) {
            throw new PropertyException(e);
        } catch (InstantiationException e) {
            throw new PropertyException(e);
        }

        return property;
    }


    private PropertyConsts () throws PropertyException {
        
        // Set up the toIndex Hashmap with the name of the
        // property as a key, and the integer index as a value
        for (int i = 0; i <= PropNames.LAST_PROPERTY_INDEX; i++) {
            if (toIndex.put(PropNames.getPropertyName(i),
                                    Ints.consts.get(i)) != null) {
                throw new PropertyException(
                    "Duplicate values in toIndex for key " +
                    PropNames.getPropertyName(i));
            }
        }
    }

}
