package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.FONode;

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
     * Set the reference to the <tt>FONode</tt> that stacked this value.
     * @param node - the stacking <tt.FONode</tt>.
     */
    public void setStackedBy(FONode node);

    /**
     * Get a reference to the <tt>FONode</tt> that stacked this value.
     * @return <tt>FONode</tt> that stacked this value.
     */
    public FONode getStackedBy();

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
