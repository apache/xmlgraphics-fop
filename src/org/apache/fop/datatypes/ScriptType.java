
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.Properties;
import org.apache.fop.configuration.Configuration;

/*
 * ScriptType.java
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
 * A class for <tt>script</tt> specifiers.
 */

public class ScriptType extends NCName {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public ScriptType(int property, String scriptCode) throws PropertyException
    {
        super(property, scriptCode);
        // Validate the code
        if (Configuration.getHashMapEntry("scriptsMap", scriptCode)
            == null) throw new PropertyException
                             ("Invalid script code: " + scriptCode);
    }

    public ScriptType(String propertyName, String scriptCode)
        throws PropertyException
    {
        this(PropertyConsts.getPropertyIndex(propertyName), scriptCode);
    }

    /**
     * @return the <tt>String</tt> script code.
     */
    public String getScript() {
        return string;
    }

    /**
     * Validate the <i>ScriptType</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Properties.SCRIPT_T);
    }

}
