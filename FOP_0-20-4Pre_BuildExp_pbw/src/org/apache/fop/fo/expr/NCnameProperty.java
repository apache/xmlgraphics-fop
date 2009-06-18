/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.expr;

import org.apache.fop.fo.Property;
import org.apache.fop.datatypes.ColorType;

public class NCnameProperty extends Property {

    private final String ncName;

    public NCnameProperty(String ncName) {
        this.ncName = ncName;
    }

    public ColorType getColor() throws PropertyException {
        // If a system color, return the corresponding value
        throw new PropertyException("Not a Color");
    }

    /**
     * Return the name as a String (should be specified with quotes!)
     */
    public String getString() {
        return this.ncName;
    }

    public String getNCname() {
        return this.ncName;
    }

}
