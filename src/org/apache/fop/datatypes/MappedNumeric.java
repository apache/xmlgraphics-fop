/*
 * MappedEnumType.java
 * $Id$
 *
 * 
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 * Software Foundation, please see <http://www.apache.org/>.
 *  
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
package org.apache.fop.datatypes;

import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.properties.*;
import org.apache.fop.fo.FONode;

/**
 * Class to represent an enumerated type whose values map onto a
 * <tt>Numeric</tt>.  This is a rethinking of the MappedEnumType, because
 * all mapped enums mapped to one form or other of <tt>Numeric</tt>.
 */

public class MappedNumeric extends EnumType {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * The <tt>Numeric</tt> value to which the associated ENUM token maps.
     */
    private Numeric mappedNum;

    /**
     * @param foNode the <tt>FONode</tt> being built
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param enumText the <tt>String</tt> containing the enumeration text.
     * An <i>NCName</i>.
     * @exception PropertyException
     */
    public MappedNumeric(FONode foNode, int property, String enumText)
        throws PropertyException
    {
        // Set property index in AbstractPropertyValue
        // and enumValue enum constant in EnumType
        super(property, enumText, PropertyValue.MAPPED_NUMERIC);
        mappedNum =
                propertyConsts.getMappedNumeric(foNode, property, enumValue);
    }

    /**
     * @param foNode the <tt>FONode</tt> being built
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param enumText the <tt>String</tt> containing the enumeration text.
     * An <i>NCName</i>.
     * @exception PropertyException
     */
    public MappedNumeric(FONode foNode, String propertyName, String enumText)
        throws PropertyException
    {
        // Set property index in AbstractPropertyValue
        // and enumValue enum constant in EnumType
        super(propertyName, enumText, PropertyValue.MAPPED_NUMERIC);
        mappedNum =
                propertyConsts.getMappedNumeric(foNode, property, enumValue);
    }

    /**
     * @return a <tt>Numeric</tt> containing the value to which
     * this ENUM token is mapped.
     */
    public Numeric getMappedNumValue() {
        return mappedNum;
    }

    /**
     * validate the <i>MappedNumeric</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Property.MAPPED_LENGTH);
    }

    public String toString() {
        return mappedNum.toString() + " " + super.toString();
    }

}
