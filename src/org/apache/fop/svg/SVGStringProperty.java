/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import java.util.*;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.apps.FOPException;

/**
 * a class representing all properties in SVG
 */
public class SVGStringProperty extends Property {

    /**
     * inner class for making SVG String objects.
     */
    public static class Maker extends Property.Maker {

        /**
         * whether this property is inherited or not.
         *
         * @return is this inherited?
         */
        public boolean isInherited() {
            return false;
        }

        /**
         * make an SVG String property with the given value.
         *
         * @param propertyList the property list this is a member of
         * @param value the explicit string value of the property
         */
        public Property make(PropertyList propertyList, String value,
                             FObj fo) throws FOPException {
            return new SVGStringProperty(propertyList, value);
        }

        /**
         * make an SVG String property with the default value.
         *
         * @param propertyList the property list the property is a member of
         */
        public Property make(PropertyList propertyList) throws FOPException {
            return make(propertyList, null, null);
        }

    }

    /**
     * returns the maker for this object.
     *
     * @return the maker for SVG Length objects
     */
    public static Property.Maker maker(String name) {
        return new SVGStringProperty.Maker();
    }

    /**
     * the value
     */
    protected String value;

    /**
     * construct an SVG String (called by the Maker).
     *
     * @param propertyList the property list this is a member of
     * @param explicitValue the explicit value as a Length object
     */
    protected SVGStringProperty(PropertyList propertyList,
                                String explicitValue) {
        this.value = explicitValue;
    }

    /**
     * get the value
     *
     * @return the length as a Length object
     */
    public String getString() {
        return this.value;
    }

}
