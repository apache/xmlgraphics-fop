/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.XMLObj;

/**
 * Class for SVG element objects.
 * This aids in the construction of the SVG Document.
 */
public class SVGObj extends XMLObj {
    /**
     * constructs an svg object (called by Maker).
     *
     * @param parent the parent formatting object
     */
    public SVGObj(FONode parent) {
        super(parent);
    }

    /**
     * Get the namespace for svg.
     * @return the svg namespace
     */
    public String getNameSpace() {
        return "http://www.w3.org/2000/svg";
    }

}

