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
     * inner class for making svg objects.
     */
    public static class Maker extends FObj.Maker {
        String tag;

        Maker(String str) {
            tag = str;
        }

        /**
         * make an svg object.
         *
         * @param parent the parent formatting object
         * @param propertyList the explicit properties of this object
         *
         * @return the svg object
         */
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new SVGObj(parent, propertyList, tag);
        }
    }

    /**
     * returns the maker for this object.
     *
     * @return the maker for an svg object
     */
    public static FObj.Maker maker(String str) {
        return new SVGObj.Maker(str);
    }

    /**
     * constructs an svg object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    protected SVGObj(FObj parent, PropertyList propertyList, String tag) {
        super(parent, propertyList, tag);
        this.name = "svg:" + tag;
    }

    public String getNameSpace() {
        return "http://www.w3.org/2000/svg";
    }
}

