/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import org.apache.fop.fo.*;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;

import org.w3c.dom.Element;

public class SVGObj extends XMLObj {
    /**
     * constructs an svg object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    public SVGObj(FObj parent) {
        super(parent);
    }

    public String getNameSpace() {
        return "http://www.w3.org/2000/svg";
    }

}

