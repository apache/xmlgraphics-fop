
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.Properties;
import org.apache.fop.fo.PropertyConsts;

/*
 * Literal.java
 * $Id$
 * Created: Wed Nov 21 15:39:30 2001
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * Class to represent Literal values.  Subclass of <tt>StringType</tt>.
 */

public class Literal extends StringType {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * Construct a <i>Literal</i> with a given <tt>String</tt>.
     * @param property the index of the property with which this value
     * is associated.
     * @param string the <tt>String</tt> value.
     */
    public Literal(int property, String string)
        throws PropertyException
    {
        super(property, string);
    }

    /**
     * Construct a <i>Literal</i> with a given <tt>String</tt>.
     * @param propertyName the name of the property with which this value
     * is associated.
     * @param string the <tt>String</tt> value.
     */
    public Literal(String propertyName, String string)
        throws PropertyException
    {
        this(PropertyConsts.getPropertyIndex(propertyName), string);
    }

    /**
     * Validate this <i>Literal</i>.  Check that it is allowed on the
     * associated property.  A <i>Literal</i> may also encode a single
     * character; i.e. a <tt>&lt;character&gt;</tt> type.  If the
     * validation against <i>LITERAL</i> fails, try <i>CHARACTER_T</i>.
     */
    public void validate() throws PropertyException {
        try {
            super.validate(Properties.LITERAL);
        } catch (PropertyException e) {
            if (string.length() == 1) {
                super.validate(Properties.CHARACTER_T);
            } else {
                throw new PropertyException(e.getMessage());
            }
        }
    }

}
