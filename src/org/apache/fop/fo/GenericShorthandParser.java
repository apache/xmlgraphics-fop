/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import java.util.Vector;
import java.util.Enumeration;

public class GenericShorthandParser implements ShorthandParser {

    protected Vector list;    // Vector of Property objects

    public GenericShorthandParser(ListProperty listprop) {
        this.list = listprop.getList();
    }

    protected Property getElement(int index) {
        if (list.size() > index)
            return (Property)list.elementAt(index);
        else
            return null;
    }

    protected int count() {
        return list.size();
    }

    // Stores 1 to 3 values for border width, style, color
    // Used for: border, border-top, border-right etc
    public Property getValueForProperty(String propName,
                                        Property.Maker maker,
                                        PropertyList propertyList) {
        Property prop = null;
        // Check for keyword "inherit"
        if (count() == 1) {
            String sval = ((Property)list.elementAt(0)).getString();
            if (sval != null && sval.equals("inherit")) {
                return propertyList.getFromParent(propName);
            }
        }
        return convertValueForProperty(propName, maker, propertyList);
    }


    protected Property convertValueForProperty(String propName,
                                               Property.Maker maker,
                                               PropertyList propertyList) {
        Property prop = null;
        // Try each of the stored values in turn
        Enumeration eprop = list.elements();
        while (eprop.hasMoreElements() && prop == null) {
            Property p = (Property)eprop.nextElement();
            prop = maker.convertShorthandProperty(propertyList, p, null);
        }
        return prop;
    }

}
