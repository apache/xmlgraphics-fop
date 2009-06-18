/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.area.Area;

import org.w3c.dom.Document;

// cacheable object
public class ForeignObject extends Area {
    Document doc;
    String namespace;
    // dom object
    // height, width

    public ForeignObject(Document d, String ns) {
        doc = d;
        namespace = ns;
    }

    public Document getDocument() {
        return doc;
    }

    public String getNameSpace() {
        return namespace;
    }
}
