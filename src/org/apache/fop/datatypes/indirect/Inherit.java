package org.apache.fop.datatypes.indirect;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.FONode;
import org.apache.fop.datatypes.indirect.IndirectValue;

/*
 * Inherit.java
 * $Id$
 *
 * Created: Tue Nov 20 22:18:11 2001
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
 * A class representing the <tt>inherit</tt> keyword.  This keyword is
 * regarded as a property value which is always equivalent to the computed
 * value of the parent.  It cannot refer to a value defined on any other
 * property.
 */

public class Inherit extends IndirectValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * Override the dual-property constructor of <tt>IndirectValue</tt>.
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param sourceProperty the <tt>int</tt> index of the property from
     * which the inherited value is derived.
     * @exception PropertyException
     */
    public Inherit(int property, int sourceProperty)
        throws PropertyException
    {
        super(property, PropertyValue.INHERIT, sourceProperty);
    }

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public Inherit(int property)
        throws PropertyException
    {
        this(property, property);
    }

    /**
     * Override the dual-property constructor of <tt>IndirectValue</tt>.
     * <i>'inherit'</i> cannot draw a value from a different property from
     * the one on which it was defined, so this constructor is private.
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param sourcePropertyName the <tt>String</tt> name of the property
     * from which the inherited value is derived.
     * @exception PropertyException
     */
    private Inherit(String propertyName, String sourcePropertyName)
        throws PropertyException
    {
        super(propertyName, PropertyValue.INHERIT, sourcePropertyName);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public Inherit(String propertyName)
        throws PropertyException
    {
        this(propertyName, propertyName);
    }

    /**
     * validate the <i>Inherit</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(getSourceProperty(), Property.INHERIT);
    }

}
