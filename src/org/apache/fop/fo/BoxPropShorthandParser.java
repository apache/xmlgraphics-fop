/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import org.apache.fop.apps.FOPException;

public class BoxPropShorthandParser extends GenericShorthandParser {

    public BoxPropShorthandParser(ListProperty listprop) {
        super(listprop);
    }

    // Stores 1 to 4 values of same type
    // Set the given property based on the number of values set
    // Example: padding, border-width, border-color, border-style, margin
    protected Property convertValueForProperty(String propName,
                                               Property.Maker maker,
                                               PropertyList propertyList) {
        Property p = null;
        if (propName.indexOf("-top") >= 0) {
            p = getElement(0);
        } else if (propName.indexOf("-right") >= 0) {
            p = getElement(count() > 1 ? 1 : 0);
        } else if (propName.indexOf("-bottom") >= 0) {
            p = getElement(count() > 2 ? 2 : 0);
        } else if (propName.indexOf("-left") >= 0) {
            p = getElement(count() > 3 ? 3 : (count() > 1 ? 1 : 0));
        }
        // if p not null, try to convert it to a value of the correct type
        if (p != null) {
            return maker.convertShorthandProperty(propertyList, p, null);
        }
        return p;
    }

}
