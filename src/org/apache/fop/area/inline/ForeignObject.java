/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.area.Area;

import org.w3c.dom.Document;

// cacheable object
/**
 * Foreign object inline area.
 * This inline area represents an instream-foreign object.
 * This holds an xml document and the associated namespace.
 */
public class ForeignObject extends Area {
    private Document doc;
    private String namespace;

    /**
     * Create a new foreign object with the given dom and namespace.
     *
     * @param d the xml document
     * @param ns the namespace of the document
     */
    public ForeignObject(Document d, String ns) {
        doc = d;
        namespace = ns;
    }

    /**
     * Get the document for this foreign object.
     *
     * @return the xml document
     */
    public Document getDocument() {
        return doc;
    }

    /**
     * Get the namespace of this foreign object.
     *
     * @return the namespace of this document
     */
    public String getNameSpace() {
        return namespace;
    }
}

