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
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.fo;

import java.util.StringTokenizer;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

/**
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

    public static final PropertyConsts pconsts;
    static {
            pconsts = new PropertyConsts();
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
     * Get the individual Property object denoted by the property index
     * @param propindex
     * @return
     * @throws PropertyException
     */
    public Property getProperty(int propindex) throws PropertyException {
        return setupProperty(propindex);
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
        return setupProperty(propindex).getInitialValueType();
    }

    /**
     * A <tt>PropertyValue</tt> array containing the initial values of
     * each of the properties.
     */
    private final PropertyValue[] initialValues
                    = new PropertyValue[PropNames.LAST_PROPERTY_INDEX + 1];
    /**
     * Get the initial value <tt>PropertyValue</tt> for a given property.
     * Note that this is a <b>raw</b> value; if it is
     * an unresolved percentage that value will be returned.
     * @param propindex - the property index.
     * @return a <tt>PropertyValue</tt> containing the initial property
     * value element for the indexed property.
     * @exception PropertyException
     */
    public PropertyValue getInitialValue(int propindex)
            throws PropertyException
    {
        if (initialValues[propindex] != null)
            return initialValues[propindex];
        return
            (initialValues[propindex] =
                    setupProperty(propindex).getInitialValue(propindex));
    }

    /**
     * @param propindex <tt>int</tt> index of the property
     * @param foNode the node whose properties are being constructed.
     * @param value the <tt>PropertyValue</tt> being refined.
     * @return <tt>PropertyValue</tt> constructed by the property's
     * <i>refineParsing</i> method
     * @exception PropertyException
     */
    public PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue value)
        throws PropertyException
    {
        Property property = setupProperty(propindex);
        return property.refineParsing(propindex, foNode, value);
    }

    /**
     * @param propindex <tt>int</tt> index of the property
     * @param foNode the node whose properties are being constructed.
     * @param value the <tt>PropertyValue</tt> being refined.
     * @param isNested  indicates whether this method is
     * called normally (false), or as part of another <i>refineParsing</i>
     * method
     * @return <tt>PropertyValue</tt> constructed by the property's
     * <i>refineParsing</i> method
     * @exception PropertyException
     */
    public PropertyValue refineParsing
        (int propindex, FONode foNode, PropertyValue value, boolean isNested)
        throws PropertyException
    {
        Property property = setupProperty(propindex);
        return property.refineParsing(propindex, foNode, value, isNested);
    }

    /**
     * An <tt>int[]</tt> containing the <i>inherited</i> values from the
     * <tt>Property</tt> classes.
     */
    private final int[] inherited
                            = new int[PropNames.LAST_PROPERTY_INDEX + 1];
    /**
     * @param property  name of the FO property
     * @return int type of inheritance for this property
     * (See constants defined in Properties.)
     * @throws PropertyException
     */
    public int inheritance(String property) throws PropertyException {
        return inheritance(PropNames.getPropertyIndex(property));
    }
    /**
     * @param propindex int index of the FO property
     * @return int type of inheritance for this property
     * (See constants defined in Property.)
     * @throws PropertyException
     */
    public int inheritance(int propindex) throws PropertyException {
        setupProperty(propindex);
        return inherited[propindex];
    }

    /**
     * @param propindex int index of the FO property
     * @return <tt>boolean</tt> is property inherited?
     * @throws PropertyException
     */
    public boolean isInherited(int propindex) throws PropertyException {
        Property property = setupProperty(propindex);
        return inherited[propindex] != Property.NO;
    }


    /**
     * An int[] array of the values of the <i>dataTypes</i> field of each
     * property.  The array is indexed by the index value constants that are
     * defined in the PropNames class in parallel to the
     * PropNames.propertyNames[] array.
     * The array elements are set from the values of the
     * <i>dataTypes</i> field in each property class.
     */
    private final int[] datatypes
                            = new int[PropNames.LAST_PROPERTY_INDEX + 1];

    /**
     * Get the <tt>Numeric</tt> value corresponding to an enumerated value.
     * @param foNode the <tt>FONode</tt> being built
     * @param propindex int index of the FO property
     * @param enumval - the integer equivalent of the enumeration keyword.
     * @return the <tt>Numeric</tt> result.
     * @throws PropertyException
     */
    public Numeric getMappedNumeric(FONode foNode, int propindex, int enumval)
            throws PropertyException
    {
        Property property = setupProperty(propindex);
        if ((datatypes[propindex] & Property.MAPPED_LENGTH) != 0)
            return property.getMappedLength(foNode, enumval);
        else
            throw new PropertyException
                ("MAPPED_LENGTH not valid in "
                                    + PropNames.getPropertyName(propindex));
    }
    /**
     * @param propindex int index of the FO property
     * @return <tt>boolean</tt> is property a shorthand?
     * @throws PropertyException
     */
    public boolean isShorthand(int propindex) throws PropertyException {
        Property property = setupProperty(propindex);
        return (datatypes[propindex] & Property.SHORTHAND) != 0;
    }

    /**
     * @param propertyIndex int index of the FO property
     * @return <tt>boolean</tt> is property a compound?
     * @throws PropertyException
     */
    public boolean isCompound(int propertyIndex) throws PropertyException {
        Property property = setupProperty(propertyIndex);
        return (datatypes[propertyIndex] & Property.COMPOUND) != 0;
    }

    /**
     * @param propertyIndex int index of the FO property
     * @return <tt>int</tt> dataTypes value.
     * @throws PropertyException
     */
    public int getDataTypes(int propertyIndex) throws PropertyException {
        return setupProperty(propertyIndex).getDataTypes();
    }

    /**
     * Map the integer value of an enumval into its mapped value.
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
     * @param enumval <tt>String</tt> containing the enumeration text.
     * @return <tt>int</tt> constant representing the enumeration value.
     * @exception PropertyException
     */
    public int getEnumIndex(int propindex, String enumval)
                    throws PropertyException
    {
        Property property = setupProperty(propindex);
        return property.getEnumIndex(enumval);
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

    /** An array of boolean results of the <code>isCorrespondingAbsolute</code>
     * method */
    private final boolean[] correspondingAbs =
        new boolean[PropNames.LAST_PROPERTY_INDEX + 1];
    /**
     * Is the indicated property absolute corresponding?
     * @param propindex
     * @return
     * @throws PropertyException
     */
    public boolean isCorrespondingAbs(int propindex)
    throws PropertyException {
        Property property = setupProperty(propindex);
        return correspondingAbs[propindex];
    }
    /** An array of boolean results of the <code>isCorrespondingRelative</code>
     * method */
    private final boolean[] correspondingRel =
        new boolean[PropNames.LAST_PROPERTY_INDEX + 1];
    /**
     * Is the indicated property relative corresponding?
     * @param propindex
     * @return
     * @throws PropertyException
     */
    public boolean isCorrespondingRel(int propindex)
    throws PropertyException {
        Property property = setupProperty(propindex);
        return correspondingRel[propindex];
    }

    /**
     * Set up the details of a single property and return the
     * <tt>Property</tt> object.  If the <tt>Property</tt> object
     * corresponding to the property index has not been resolved before,
     * derive the Class and Property objects, and extract certain field
     * values from the Property.
     * @param propindex - the <tt>int</tt> index.
     * @return - the <tt>Property</tt> corresponding to the index.
     * @throws PropertyException
     */
    public Property setupProperty(int propindex)
            throws PropertyException
    {
        Property property;
        if ((property = properties[propindex]) != null) return property;

        String cname = "";
        Class pclass;
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

        // Get the class for this property name
        String name = packageName + ".properties." + cname;
        try {
            pclass = Class.forName(name);
            // Instantiate the class
            property = (Property)(pclass.newInstance());
            properties[propindex] = property;
            inherited[propindex] = property.getInherited();
            correspondingAbs[propindex] = property.isCorrespondingAbsolute();
            correspondingRel[propindex] = property.isCorrespondingRelative();

        } catch (ClassNotFoundException e) {
            throw new PropertyException
                    ("ClassNotFoundException" + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new PropertyException
                    ("IllegalAccessException" + e.getMessage());
        } catch (InstantiationException e) {
            throw new PropertyException
                    ("InstantiationException" + e.getMessage());
        }

        return property;
    }


    private PropertyConsts () {}

}
