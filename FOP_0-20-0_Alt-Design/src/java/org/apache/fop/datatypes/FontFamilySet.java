/*
 * FontFamilySet.java
 * $Id$
 *
 * Created: Mon Nov 26 22:46:05 2001
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
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.datatypes;

//import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

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
    protected final String[] fontFamilyNames;

    /**
     * @param property <tt>int</tt> index of the property.
     * @param fontFamilyNames  contains a
     * prioritized list of font names, as literals or <tt>NCName</tt>s,
     * being either the full name of a font, or an enumeration token
     * representing a font family.
     * @exception PropertyException
     */
    public FontFamilySet(int property, String[] fontFamilyNames)
        throws PropertyException
    {
        super(property, PropertyValue.FONT_FAMILY);
        this.fontFamilyNames = fontFamilyNames;
    }

    /**
     * @param propertyName <tt>String</tt> name of the property.
     * @param fontFamilyNames  contains a
     * prioritized list of font names, as literals or <tt>NCName</tt>s,
     * being either the full name of a font, or an enumeration token
     * representing a font family.
     * @exception PropertyException
     */
    public FontFamilySet(String propertyName, String[] fontFamilyNames)
        throws PropertyException
    {
        this(PropNames.getPropertyIndex(propertyName), fontFamilyNames);
    }

    /**
     * Validate the <i>FontFamilySet</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Property.FONTSET);
    }

    /**
     * An <tt>Iterator</tt> implementing member class of FontFamilySet.
     */
    public class Traverser {

        /**
         * The index for the iteration across the fontFamilyNames array.
         */
        private int index = 0;

        public Traverser() {}

        public boolean hasNext() {
            return index < fontFamilyNames.length;
        }

        public String next() {
            if (hasNext()) return fontFamilyNames[index++];
            throw new NoSuchElementException();
        }

//        public void remove() {
//            throw new UnsupportedOperationException();
//        }
    }
}
