/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

// FOP
import org.apache.fop.fo.*;

/**
 * Since SVG objects are not layed out then this class checks
 * that this element is not being layed out inside some incorrect
 * element.
 */
public abstract class SVGObj extends XMLObj {

    /**
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    public SVGObj(FObj parent, PropertyList propertyList) {
        super(parent, propertyList);
    }

    public String getNameSpace() {
        return "http://www.w3.org/2000/svg";
    }

}

