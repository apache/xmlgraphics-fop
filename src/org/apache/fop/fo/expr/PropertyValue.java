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
                          ,AUTO = 1
                          ,BOOL = 1
                    ,COLOR_TYPE = 1
                       ,COUNTRY = 1
                          ,ENUM = 1
                   ,FONT_FAMILY = 1
                     ,FREQUENCY = 1
        ,FROM_NEAREST_SPECIFIED = 1
                   ,FROM_PARENT = 1
                       ,INHERIT = 1
                       ,INTEGER = 1
                      ,LANGUAGE = 1
                       ,LITERAL = 1
                ,MAPPED_NUMERIC = 1
                     ,MIME_TYPE = 1
                        ,NCNAME = 1
                          ,NONE = 1
                       ,NUMERIC = 1
                        ,SCRIPT = 1
                 ,SHADOW_EFFECT = 1
                         ,SLASH = 1
              ,TEXT_DECORATIONS = 1
                ,TEXT_DECORATOR = 1
                          ,TIME = 1
                      ,URI_TYPE = 1
                          ,LIST = 2

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
