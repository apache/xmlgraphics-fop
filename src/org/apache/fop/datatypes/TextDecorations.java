package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.AbstractPropertyValue;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.Properties;
import org.apache.fop.fo.PropNames;
import org.apache.fop.datatypes.TextDecorator;

/*
 * TextDecorations.java
 * $Id$
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * Class for the text decorations to be applied according to the
 * <em>text-decoration</em> property.  This class maintains the current
 * set of text decorations.  Modifications to the set are specified in a
 * TextDecorator object, which contains the on and off masks to be applied
 * to the "current" set of decorations.
 */

public class TextDecorations

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";
    extends AbstractPropertyValue implements Cloneable
{

    /**
     * The decorations specified by this object
     */
    private byte decorations;

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public TextDecorations(int property, byte decorations)
        throws PropertyException
    {
        super(property);
        this.decorations = decorations;
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public TextDecorations(String propertyName)
        throws PropertyException
    {
        super(propertyName);
        this.decorations = decorations;
    }

    /**
     * @return <tt>byte</tt> decorations value
     */
    public byte getDecorations() {
        return decorations;
    }

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
