/*
 * ScriptType.java
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

import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

/**
 * A class for <tt>script</tt> specifiers.
 */

public class ScriptType extends NCName {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public ScriptType(int property, String scriptCode) throws PropertyException
    {
        super(property, scriptCode, PropertyValue.SCRIPT);
        // Validate the code
        String code;
        if ((code = CountryLanguageScript.canonicalScriptCode(scriptCode))
                == null)
            throw new PropertyException
                             ("Invalid script code: " + scriptCode);
        setString(code);
    }

    public ScriptType(String propertyName, String scriptCode)
        throws PropertyException
    {
        this(PropNames.getPropertyIndex(propertyName), scriptCode);
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
        super.validate(Property.SCRIPT_T);
    }

}
