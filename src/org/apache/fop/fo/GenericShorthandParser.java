/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import java.util.ArrayList;

public class GenericShorthandParser implements ShorthandParser {

    protected ArrayList list;    // ArrayList of Property objects

    public GenericShorthandParser(ListProperty listprop) {
        this.list = listprop.getList();
    }

    protected Property getElement(int index) {
        if (list.size() > index)
            return (Property)list.get(index);
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
            String sval = ((Property)list.get(0)).getString();
            if (sval != null && sval.equals("inherit")) {
                return propertyList.getFromParent(propName);
            }
        }
        return convertValueForProperty(propName, maker, propertyList);
    }


    protected Property convertValueForProperty(String propName,
                                               Property.Maker maker,
                                               PropertyList propertyList) {
        // Try each of the stored values in turn
        for (int i = 0; i <  list.size(); i++) {
            Property p = (Property)list.get(i);
            Property prop = maker.convertShorthandProperty(propertyList, p, null);
            if (prop!=null) {
                return prop;
            }
        }
        return null;
    }

}
