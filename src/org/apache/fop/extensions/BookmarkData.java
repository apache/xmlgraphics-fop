/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.extensions;

import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Resolveable;
import org.apache.fop.area.TreeExt;
import org.apache.fop.area.AreaTree;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * This class holds the PDF bookmark extension data.
 * This implements Resolveable and TreeExt so that it can be
 * added to the area tree as a resolveable tree extension.
 */
public class BookmarkData implements Resolveable, TreeExt {
    private ArrayList subData = new ArrayList();
    private HashMap idRefs = new HashMap();

    // area tree for the top level object to notify when resolved
    private AreaTree areaTree = null;

    private String idRef;
    private PageViewport pageRef = null;
    private String label = null;

    /**
     * Create a new bookmark data object.
     * This should only be call by the top level element as the
     * id reference will be null.
     */
    public BookmarkData() {
        idRef = null;
    }

    /**
     * Create a new pdf bookmark data object.
     * This is used by the outlines to create a data object
     * with a id reference. The id reference is to be resolved.
     *
     * @param id the id reference
     */
    public BookmarkData(String id) {
        idRef = id;
        idRefs.put(idRef, this);
    }

    /**
     * Set the area tree.
     * This should only be called for the top level element.
     * The area tree is used once resolving is complete.
     *
     * @param at the area tree for the current document
     */
    public void setAreaTree(AreaTree at) {
        areaTree = at;
    }

    /**
     * Get the id reference for this data.
     *
     * @return the id reference
     */
    public String getID() {
        return idRef;
    }

    /**
     * Add the child bookmark data object.
     * This adds a child bookmark in the bookmark hierarchy.
     *
     * @param sub the child bookmark data
     */
    public void addSubData(BookmarkData sub) {
        subData.add(sub);
        idRefs.put(sub.getID(), sub);
        String[] ids = sub.getIDs();
        for (int count = 0; count < ids.length; count++) {
            idRefs.put(ids[count], sub);
        }
    }

    /**
     * Set the label for this bookmark.
     *
     * @param l the string label
     */
    public void setLabel(String l) {
        label = l;
    }

    /**
     * Get the label for this bookmark object.
     *
     * @return the label string
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get the size of child data objects.
     *
     * @return the number of child bookmark data
     */
    public int getCount() {
        return subData.size();
    }

    /**
     * Get the child data object.
     *
     * @param count the index to get
     * @return the child bookmark data
     */
    public BookmarkData getSubData(int count) {
        return (BookmarkData)subData.get(count);
    }

    /**
     * Get the page that this resolves to.
     *
     * @return the PageViewport that this extension resolves to
     */
    public PageViewport getPage() {
        return pageRef;
    }

    /**
     * Check if this tree extension is resolveable.
     *
     * @return true since this also implements Resolveable
     */
    public boolean isResolveable() {
        return true;
    }

    /**
     * Get the mime type of this area tree extension.
     *
     * @return this tree extension applies to pdf
     */
    public String getMimeType() {
        return "application/pdf";
    }

    /**
     * Get the name of this area tree extension.
     *
     * @return the name of the PDF bookmark extension is "Bookmark"
     */
    public String getName() {
        return "Bookmark";
    }

    /**
     * Check if this resolveable object has been resolved.
     * Once the id reference is null then it has been resolved.
     *
     * @return true if this has been resolved
     */
    public boolean isResolved() {
        return idRefs == null;
    }

    /**
     * Get the id references held by this object.
     * Also includes all id references of all children.
     *
     * @return the array of id references
     */
    public String[] getIDs() {
        return (String[])idRefs.keySet().toArray(new String[] {});
    }

    /**
     * Resolve this resolveable object.
     * This resolves the id reference and if possible also
     * resolves id references of child elements that have the same
     * id reference.
     *
     * @param id the id reference being resolved
     * @param pages the list of pages the the id reference resolves to
     */
    public void resolve(String id, List pages) {
        // this method is buggy

        if (!id.equals(idRef)) {
            BookmarkData bd = (BookmarkData)idRefs.get(id);
            idRefs.remove(id);
            if (bd != null) {
                bd.resolve(id, pages);
                if (bd.isResolved()) {
                    checkFinish();
                }
            } else if (idRef == null) {
                checkFinish();
            }
        } else {
            if (pages != null) {
                pageRef = (PageViewport)pages.get(0);
            }
            // TODO
            // get rect area of id on page

            idRefs.remove(idRef);
            checkFinish();
        }
    }

    private void checkFinish() {
        if (idRefs.size() == 0) {
            idRefs = null;
            if (areaTree != null) {
                areaTree.handleTreeExtension(this, TreeExt.AFTER_PAGE);
            }
        }
    }
}

