/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import java.util.ArrayList;

public class MarginShorthandParser implements ShorthandParser {

    protected ArrayList list;    // ArrayList of Property objects

    public MarginShorthandParser(ListProperty listprop) {
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

    // Stores 1 to 4 values for margin-top, -right, -bottom or -left
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
        Property prop = null;
        int idx = 0;

        switch (count())
        {
        case 1: //
            idx = 0;
            break;
        case 2: // 1st value top/bottom, 2nd value left/right
            if (propName.equals("margin-top") ||
                    propName.equals("margin-bottom"))
                idx = 0;
            else
                idx = 1;
            break;
        case 3: // 1st value top, 2nd left/right, 3rd bottom
            if (propName == "margin-top")
                idx = 0;
            else if (propName.equals("margin-bottom"))
                idx = 2;
            else
                idx = 1;
            break;
        case 4: // top, right, bottom, left
            if (propName.equals("margin-top"))
                idx = 0;
            else if (propName.equals("margin-right"))
                idx = 1;
            else if (propName.equals("margin-bottom"))
                idx = 2;
            else if (propName.equals("margin-left"))
                idx = 3;
            break;
        default:
            // TODO Error Message: Wrong number of args
            return null;
        }

        Property p = getElement(idx);
        prop = maker.convertShorthandProperty(propertyList, p, null);
        return prop;
    }

}

