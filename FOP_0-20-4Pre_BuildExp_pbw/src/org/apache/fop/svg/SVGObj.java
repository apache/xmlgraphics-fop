/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import org.apache.fop.fo.*;

public class SVGObj extends XMLObj {
    /**
     * constructs an svg object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    public SVGObj(FONode parent) {
        super(parent);
    }

    public String getNameSpace() {
        return "http://www.w3.org/2000/svg";
    }

}

