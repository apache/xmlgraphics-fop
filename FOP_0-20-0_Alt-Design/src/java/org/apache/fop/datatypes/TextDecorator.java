/*
 * TextDecorator.java
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

/**
 * Class for "text-decoration" specification.  This class specifies the
 * text decoration modifiers which are to be applied to the current
 * text-decoration value.
 * <p>It is applied to a TextDecorations object, to modify the decorations
 * by applying these values.
 */

public class TextDecorator extends AbstractPropertyValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * OR mask to turn decorations on
     */
    public final byte onMask;

    /**
     * NAND mask to turn decorations off
     */
    public final byte offMask;

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public TextDecorator(int property, byte onMask, byte offMask)
        throws PropertyException
    {
        super(property, PropertyValue.TEXT_DECORATOR);
        this.onMask = onMask;
        this.offMask = offMask;
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public TextDecorator(String propertyName, byte onMask, byte offMask)
        throws PropertyException
    {
        super(propertyName, PropertyValue.TEXT_DECORATOR);
        this.onMask = onMask;
        this.offMask = offMask;
    }

    /**
     * validate the <i>TextDecorator</i> against the associated property.
     */
    public void validate() throws PropertyException {
        if (property != PropNames.TEXT_DECORATION)
            throw new PropertyException
                    ("TextDecorator only valid for 'text-decoration'");
    }

}
