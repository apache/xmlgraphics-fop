/*
 * PropertyValue.java
 * $Id$
 *
 * Created: Tue Nov 20 22:18:11 2001
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

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Base interface for all property value types.
 */

public interface PropertyValue {

    public static final int
                        NO_TYPE = 0
                         ,ANGLE = 1
                          ,AUTO = 2
                          ,BOOL = 3
                    ,COLOR_TYPE = 4
                       ,COUNTRY = 5
                          ,ENUM = 6
                   ,FONT_FAMILY = 7
                     ,FREQUENCY = 8
        ,FROM_NEAREST_SPECIFIED = 9
                   ,FROM_PARENT = 10
                       ,INHERIT = 11
               ,INHERITED_VALUE = 12
                       ,INTEGER = 13
                      ,LANGUAGE = 14
                       ,LITERAL = 15
                ,MAPPED_NUMERIC = 16
                     ,MIME_TYPE = 17
                        ,NCNAME = 18
                          ,NONE = 19
                       ,NUMERIC = 20
                        ,SCRIPT = 21
                 ,SHADOW_EFFECT = 22
                         ,SLASH = 23
              ,TEXT_DECORATIONS = 24
                ,TEXT_DECORATOR = 25
                          ,TIME = 26
                      ,URI_TYPE = 27
                          ,LIST = 28

            ,LAST_PROPERTY_TYPE = LIST;

    public static final ROStringArray propertyTypes =
        new ROStringArray(new String[] {
        "NO_TYPE"
        ,"ANGLE"
        ,"AUTO"
        ,"BOOL"
        ,"COLOR_TYPE"
        ,"COUNTRY"
        ,"ENUM"
        ,"FONT_FAMILY"
        ,"FREQUENCY"
        ,"FROM_NEAREST_SPECIFIED"
        ,"FROM_PARENT"
        ,"INHERIT"
        ,"INHERITED_VALUE"
        ,"INTEGER"
        ,"LANGUAGE"
        ,"LITERAL"
        ,"MAPPED_NUMERIC"
        ,"MIME_TYPE"
        ,"NCNAME"
        ,"NONE"
        ,"NUMERIC"
        ,"SCRIPT"
        ,"SHADOW_EFFECT"
        ,"SLASH"
        ,"TEXT_DECORATIONS"
        ,"TEXT_DECORATOR"
        ,"TIME"
        ,"URI_LIST"
        ,"LIST"
    });

    /**
     * @return <tt>int</tt> property index.
     */
    public int getProperty();
    public void setProperty(int index) throws PropertyException;

    /**
     * Get the <tt>int</tt> type of property value.
     * @return type field of the <tt>PropertyValue</tt>.
     */
    public int getType();

    /**
     * In some circumstances, the property against which a type is to be
     * validated may not be the same as the property against which this
     * <i>PropertyValue</i> is defined.  A specific property argument is
     * then required.
     * @param testProperty <tt>int</tt> property index of the property
     * for which the type is to be validated.
     * @param type <tt>int</tt> bitmap of data types to check for
     * validity against this property.
     */
    public void validate(int testProperty, int type)
        throws PropertyException;

    /**
     * @param type <tt>int</tt> bitmap of data types to check for
     * validity against this property.
     */
    public void validate(int type) throws PropertyException;
    public Object clone() throws CloneNotSupportedException;

}
