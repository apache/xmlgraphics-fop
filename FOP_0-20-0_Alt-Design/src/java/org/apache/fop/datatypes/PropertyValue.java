/*
 * PropertyValue.java
 * $Id$
 *
 * Created: Tue Nov 20 22:18:11 2001
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
