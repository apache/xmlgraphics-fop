package org.apache.fop.fo.expr;

import org.apache.fop.fo.expr.PropertyException;

/*
 * PropertyValue.java
 * $Id$
 *
 * Created: Tue Nov 20 22:18:11 2001
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * Base class for all property value types.
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
                       ,INTEGER = 12
                      ,LANGUAGE = 13
                       ,LITERAL = 14
                ,MAPPED_NUMERIC = 15
                     ,MIME_TYPE = 16
                        ,NCNAME = 17
                          ,NONE = 18
                       ,NUMERIC = 19
                        ,SCRIPT = 20
                 ,SHADOW_EFFECT = 21
                         ,SLASH = 22
              ,TEXT_DECORATIONS = 23
                ,TEXT_DECORATOR = 24
                          ,TIME = 25
                      ,URI_TYPE = 26
                          ,LIST = 27

            ,LAST_PROPERTY_TYPE = LIST;

    /**
     * @return <tt>int</tt> property index.
     */
    public int getProperty();
    public void setProperty(int index) throws PropertyException;

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
