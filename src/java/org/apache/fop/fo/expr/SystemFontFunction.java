/*
 * SystemFontFunction.java
 * Implement the system font function
 * $Id$
 * @author <a href="mailto: "Peter B. West</a>
 * @version $Revision$ $Name$
 */
package org.apache.fop.fo.expr;

import org.apache.fop.datastructs.ROIntArray;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.fo.ShorthandPropSets;

/**
 * Implement the system font function.
 * <p>Eventually, provision will have to be made for the configuration of
 * system font names and characteristics on a per-instance basis.
 */
public class SystemFontFunction {
    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * Return the <tt>PropertyValue</tt> appropriate to the <em>property</em>
     * of the argument.  <em>property</em> must be one of the font
     * characteristic properties in the expansion set of the <em>font</em>
     * shorthand property; <em>ShorthandPropSets.fontExpansion</em>.
     * @param property <tt>int</tt> index of the font characteristic property
     * to be returned.
     * @param font <tt>String</tt> name of the system font
     * @exception FunctionNotImplementedException
     * @exception PropertyException if the property is not appropriate or
     * if any other errors occur in the processing of the property
     */
    public static PropertyValue systemFontCharacteristic
        (int property, String font)
        throws FunctionNotImplementedException, PropertyException
    {
        throw new FunctionNotImplementedException("system-font");
    }

    /**
     * Return the <tt>PropertyValue</tt> appropriate to the <em>property</em>
     * of the argument.  The value returned is the current value of the
     * font characteristic, named by <em>propName</em>, defined on the
     * system font named by <em>font</em>.  <em>propName</em> must be one of
     * characteristic properties in the expansion set of the <em>font</em>
     * shorthand property; <em>ShorthandPropSets.fontExpansion</em>.
     * @param property <tt>int</tt> index of the property for the
     * <tt>PropertyValue</tt> to be returned.
     * @param font <tt>String</tt> name of the system font
     * @param propName <tt>String</tt> name of font characteristic whose
     * current value is to be returned.
     * @exception FunctionNotImplementedException
     * @exception PropertyException if the property is not appropriate or
     * if any other errors occur in the processing of the property
     */
    public static PropertyValue systemFontCharacteristic
        (int property, String font, String propName)
        throws FunctionNotImplementedException, PropertyException
    {
        throw new FunctionNotImplementedException("system-font");
    }

    /**
     * Expand the <em>font</em> shorthand property defined for a named
     * system font, by providing a <tt>PropertyValueList</tt> containing
     * one element for each property expansion from the <em>font</em>
     * shorthand.  Individual values are derived by calling
     * <em>SystemFontFunction.systemFontCharacteristic</em> for each
     * property in the expansion.
     * @param property <tt>int</tt> index of the property for the
     * <tt>PropertyValue</tt> to be returned.
     * @param font <tt>String</tt> name of the system font
     * @return <tt>PropertyValueList</tt> containing a list of
     *  <tt>PropertyValue</tt>s, one for each property in the expansion of
     *  the <em>font</em> shorthand property.
     * @exception PropertyException
     */
    public static PropertyValueList expandFontSHand
        (int property, String font)
        throws PropertyException
    {
        // Get the array of indices of the properties in the
        // expansion of this shorthand
        ROIntArray expansion = ShorthandPropSets.fontExpansion;
        PropertyValueList list = new PropertyValueList(property);
        for (int i = 0; i < expansion.length; i++) {
            list.add(systemFontCharacteristic(expansion.get(i), font));
        }
        return list;
    }
}
