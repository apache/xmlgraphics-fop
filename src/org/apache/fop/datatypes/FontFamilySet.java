
package org.apache.fop.datatypes;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.Properties;
import org.apache.fop.fo.expr.AbstractPropertyValue;

/*
 * FontFamilySet.java
 * $Id$
 *
 * Created: Mon Nov 26 22:46:05 2001
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * A base class for representing a set of font family names.
 */

public class FontFamilySet extends AbstractPropertyValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * An array of <tt>String</tt>s containing a prioritized list of
     * font family or generic font family names.
     */
    private String[] fontFamilyNames;

    /**
     * @param property <tt>int</tt> index of the property.
     * @param fontNames an array of <tt>String</tt>s containing a
     * prioritized list of font names, as literals or <tt>NCName</tt>s,
     * being either the full name of a font, or an enumeration token
     * representing a font family.
     * @exception PropertyException.
     */
    public FontFamilySet(int property, String[] fontFamilyNames)
        throws PropertyException
    {
        super(property);
        this.fontFamilyNames = fontFamilyNames;
    }

    /**
     * @param propertyName <tt>String</tt> name of the property.
     * @param fontNames an array of <tt>String</tt>s containing a
     * prioritized list of font names, as literals or <tt>NCName</tt>s,
     * being either the full name of a font, or an enumeration token
     * representing a font family.
     * @exception PropertyException.
     */
    public FontFamilySet(String propertyName, String[] fontFamilyNames)
        throws PropertyException
    {
        this(PropertyConsts.getPropertyIndex(propertyName), fontFamilyNames);
    }

    /**
     * Validate the <i>FontFamilySet</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Properties.FONTSET);
    }

    /**
     * An <tt>Iterator</tt> implementing member class of FontFamilySet.
     */
    class Traverser implements Iterator {

        /**
         * The index for the iteration across the fontFamilyNames array.
         */
        private int index = 0;

        public Traverser() {}

        public boolean hasNext() {
            return index < fontFamilyNames.length;
        }

        public Object next() {
            if (hasNext()) return (Object)fontFamilyNames[index++];
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
