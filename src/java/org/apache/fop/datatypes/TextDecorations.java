/*
 * TextDecorations.java
 * $Id$
 *
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

import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.TextDecoration;

/**
 * Class for the text decorations to be applied according to the
 * <em>text-decoration</em> property.  This class maintains the current
 * set of text decorations.  Modifications to the set are specified in a
 * TextDecorator object, which contains the on and off masks to be applied
 * to the "current" set of decorations.
 * TODO Should this be a PropertyValue at all?  I don't think so.
 */

public class TextDecorations
    extends AbstractPropertyValue implements Cloneable
{

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** The decorations specified by this object. */
    private byte decorations;

    /** The color of these text decorations. */
    private ColorType color;

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param decorations the decorations being applied.
     * @exception PropertyException
     */
    public TextDecorations(int property, byte decorations)
        throws PropertyException
    {
        super(property, PropertyValue.TEXT_DECORATIONS);
        this.decorations = decorations;
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param decorations the decorations being applied.
     * @exception PropertyException
     */
    public TextDecorations(String propertyName, byte decorations)
        throws PropertyException
    {
        super(propertyName, PropertyValue.TEXT_DECORATIONS);
        this.decorations = decorations;
    }

    public boolean overlined() {
        if ((decorations & TextDecoration.OVERLINE) != 0) {
            return true;
        }
        return false;
    }

    public boolean struckthrough() {
        if ((decorations & TextDecoration.LINE_THROUGH) != 0) {
            return true;
        }
        return false;
    }

    public boolean underlined() {
        if ((decorations & TextDecoration.UNDERLINE) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Get the set of decorations.
     * @return <tt>byte</tt> decorations value
     */
    public byte getDecorations() {
        return decorations;
    }

    /**
     * Get the color associated with this set of decorations.
     * @return the color.
     */
    public ColorType getColor() {
        return color;
    }

    /**
     * Set the color associated with this set of decorations.
     * @param color the color.
     */
    public void setColor(ColorType color) {
        this.color = color;
    }

    /**
     * Apply the decoration masks of a <tt>TextDecorator</tt> object to
     * these decorations.
     * @param decorator the <tt>TextDecorator</tt>.
     */
    public byte maskDecorations(TextDecorator decorator) {
        decorations |= decorator.onMask;
        decorations &= ( ~ decorator.offMask );
        return decorations;
    }

    /**
     * validate the <i>TextDecorations</i> against the associated property.
     */
    public void validate() throws PropertyException {
        if (property != PropNames.TEXT_DECORATION)
            throw new PropertyException
                    ("TextDecorations only valid for 'text-decoration'");
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
