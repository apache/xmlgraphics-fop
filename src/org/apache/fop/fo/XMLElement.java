/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.inline.*;
import org.apache.fop.apps.FOPException;

/**
 * class representing svg:svg pseudo flow object.
 */
public class XMLElement extends XMLObj {
    String namespace = "";

    /**
     * inner class for making XML objects.
     */
    public static class Maker extends FObj.Maker {
        String tag;

        Maker(String t) {
            tag = t;
        }

        /**
         * make an XML object.
         *
         * @param parent the parent formatting object
         * @param propertyList the explicit properties of this object
         *
         * @return the XML object
         */
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new XMLElement(parent, propertyList, tag);
        }
    }

    /**
     * returns the maker for this object.
     *
     * @return the maker for XML objects
     */
    public static FObj.Maker maker(String tag) {
        return new XMLElement.Maker(tag);
    }

    /**
     * constructs an XML object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    public XMLElement(FObj parent, PropertyList propertyList, String tag) {
        super(parent, propertyList, tag);
        init();
    }

    public String getName() {
        return tagName;
    }

    /**
     * layout this formatting object.
     *
     * @param area the area to layout the object into
     *
     * @return the status of the layout
     */
    public int layout(final Area area) throws FOPException {

        if (!(area instanceof ForeignObjectArea)) {
            // this is an error
            throw new FOPException("XML not in fo:instream-foreign-object");
        }

        /* return status */
        return Status.OK;
    }

    private void init() {
        createBasicDocument();
    }

    public String getNameSpace() {
        return namespace;
    }
}
