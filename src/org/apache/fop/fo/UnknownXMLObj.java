/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import org.apache.fop.fo.*;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.inline.*;
import org.apache.fop.apps.FOPException;

import org.w3c.dom.Element;

public class UnknownXMLObj extends XMLObj {
    String namespace;

    /**
     * inner class for making unknown xml objects.
     */
    public static class Maker extends FObj.Maker {
        String space;
        String tag;

        Maker(String sp, String t) {
            space = sp;
            tag = t;
        }

        /**
         * make an unknown xml object.
         *
         * @param parent the parent formatting object
         * @param propertyList the explicit properties of this object
         *
         * @return the unknown xml object
         */
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new UnknownXMLObj(parent, propertyList, space, tag);
        }
    }

    /**
     * returns the maker for this object.
     *
     * @return the maker for an unknown xml object
     */
    public static FObj.Maker maker(String space, String tag) {
        return new UnknownXMLObj.Maker(space, tag);
    }

    /**
     * constructs an unknown xml object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    protected UnknownXMLObj(FObj parent, PropertyList propertyList, String space, String tag) {
        super(parent, propertyList, tag);
        this.namespace = space;
        this.name = this.namespace + ":" + tag;
    }

    public String getNameSpace() {
        return this.namespace;
    }

    protected void addChild(FONode child) {
        if(doc == null) {
            createBasicDocument();
        }
        super.addChild(child);
    }

    protected void addCharacters(char data[], int start, int length) {
        if(doc == null) {
            createBasicDocument();
        }
        super.addCharacters(data, start, length);
    }

    public Status layout(Area area) throws FOPException {
        //if (!(area instanceof ForeignObjectArea)) {
            // this is an error
            //throw new FOPException("Foreign XML not in fo:instream-foreign-object");
        //}
        log.error("no handler defined for " + this.name + " foreign xml");

        /* return status */
        return new Status(Status.OK);
    }
}

