/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.extensions;

import org.apache.fop.fo.FONode;

import java.util.*;

import org.xml.sax.Attributes;

public class Bookmarks extends ExtensionObj {
    private ArrayList outlines = new ArrayList();
    private BookmarkData data;

    public Bookmarks(FONode parent) {
        super(parent);
    }

    protected void addChild(FONode obj) {
        if (obj instanceof Outline) {
            outlines.add(obj);
        }
    }

    public BookmarkData getData() {
        return data;
    }

    public void end() {
        log.debug("adding bookmarks to area tree");
        data = new BookmarkData();
        for(int count = 0; count < outlines.size(); count++) {
            Outline out = (Outline)outlines.get(count);
            data.addSubData(out.getData());
        }
        // add data to area tree for resolving and handling
    }
}

