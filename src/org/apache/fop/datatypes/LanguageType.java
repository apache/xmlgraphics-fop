
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.properties.*;
import org.apache.fop.datatypes.PropertyValue;

/*
 * LanguageType.java
 * $Id$
 *
 * Created: Mon Nov 26 22:46:05 2001
 * 
 *  ============================================================================
 *                    The Apache Software License, Version 1.1
 *  ============================================================================
 *  
 *  Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without modifica-
 *  tion, are permitted provided that the following conditions are met:
 *  
 *  1. Redistributions of  source code must  retain the above copyright  notice,
 *     this list of conditions and the following disclaimer.
 *  
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  
 *  3. The end-user documentation included with the redistribution, if any, must
 *     include  the following  acknowledgment:  "This product includes  software
 *     developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *     Alternately, this  acknowledgment may  appear in the software itself,  if
 *     and wherever such third-party acknowledgments normally appear.
 *  
 *  4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *     endorse  or promote  products derived  from this  software without  prior
 *     written permission. For written permission, please contact
 *     apache@apache.org.
 *  
 *  5. Products  derived from this software may not  be called "Apache", nor may
 *     "Apache" appear  in their name,  without prior written permission  of the
 *     Apache Software Foundation.
 *  
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 *  APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 *  DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 *  OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 *  ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 *  (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  This software  consists of voluntary contributions made  by many individuals
 *  on  behalf of the Apache Software  Foundation and was  originally created by
 *  James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 *  Software Foundation, please see <http://www.apache.org/>.
 *  
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * A class for <tt>language</tt> specifiers.
 */

public class LanguageType extends NCName {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public LanguageType(int property, String languageCode)
        throws PropertyException
    {
        super(property, languageCode, PropertyValue.LANGUAGE);
        // Validate the code
        String code;
        if ((code = CountryLanguageScript.canonicalLangCode(languageCode))
                == null)
            throw new PropertyException
                             ("Invalid language code: " + languageCode);
        setString(code);
    }

    public LanguageType(String propertyName, String languageCode)
        throws PropertyException
    {
        this(PropNames.getPropertyIndex(propertyName), languageCode);
    }

    /**
     * Validate the <i>LanguageType</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Property.LANGUAGE_T);
    }

    /**
     * @return the <tt>String</tt> language code.
     */
    public String getLanguage() {
        return string;
    }

}
