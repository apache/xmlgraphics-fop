/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.extensions;

import org.apache.fop.apps.LayoutHandler;
import org.apache.fop.fo.FONode;
import org.apache.fop.area.AreaTree;

import java.util.ArrayList;

/**
 * Bookmarks data is the top level element of the pdf bookmark extension.
 * This handles the adding of outlines. When the element is ended it
 * creates the bookmark data and adds to the area tree.
 */
public class Bookmarks extends ExtensionObj {
    private ArrayList outlines = new ArrayList();
    private BookmarkData data;

    /**
     * Create a new Bookmarks object.
     *
     * @param parent the parent fo node
     */
    public Bookmarks(FONode parent) {
        super(parent);
    }

    /**
     * Add the child to the top level.
     * This handles all Outline objects added and ignores others.
     *
     * @param obj the child to add
     */
    protected void addChild(FONode obj) {
        if (obj instanceof Outline) {
            outlines.add(obj);
        }
    }

    /**
     * Get the data created for this bookmark.
     *
     * @return the bookmark data
     */
    public BookmarkData getData() {
        return data;
    }

    /**
     * When this element is finished then it can create
     * the bookmark data from the child elements and add
     * the extension to the area tree.
     */
    public void end() {
        getLogger().debug("adding bookmarks to area tree");
        data = new BookmarkData();
        for (int count = 0; count < outlines.size(); count++) {
            Outline out = (Outline)outlines.get(count);
            data.addSubData(out.getData());
        }
        // add data to area tree for resolving and handling
        if (structHandler instanceof LayoutHandler) {
            AreaTree at = ((LayoutHandler)structHandler).getAreaTree();
            at.addTreeExtension(data);
            data.setAreaTree(at);
        }
    }
}

