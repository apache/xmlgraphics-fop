/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */
 
package org.apache.fop.area;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import org.apache.fop.fo.pagination.bookmarks.BookmarkTree;
import org.apache.fop.fo.extensions.Outline;

/**
 * An instance of this class is either a PDF bookmark-tree and
 * its child bookmark-items, or a bookmark-item and the child
 * child bookmark-items under it.
 */
public class BookmarkData extends OffDocumentItem implements Resolvable {
    private ArrayList subData = new ArrayList();

    // bookmark label
    private String label = null;

    // ID Reference for this bookmark
    private String idRef;

    // PageViewport that the idRef item refers to
    private PageViewport pageRef = null;

    // unresolved idrefs by this bookmark and child bookmarks below it
    private HashMap unresolvedIDRefs = new HashMap();

    /**
     * Create a new bookmark data object.
     * This should only be called by the bookmark-tree item because
     * it has no idref item that needs to be resolved.
     *
     * @param bookmarks fo:bookmark-tree for this document
     */
    public BookmarkData(BookmarkTree bookmarkTree) {
        idRef = null;
        whenToProcess = END_OF_DOC;
        
        for (int count = 0; count < bookmarkTree.getBookmarks().size(); count++) {
            Outline out = (Outline)(bookmarkTree.getBookmarks()).get(count);
            addSubData(createBookmarkData(out));
        }
    }

    /**
     * Create a new pdf bookmark data object.
     * This is used by the bookmark-items to create a data object
     * with a idref.  During processing, this idref will be
     * subsequently resolved to a particular PageViewport.
     *
     * @param idref the id reference
     */
    public BookmarkData(String idref) {
        this.idRef = idref;
        unresolvedIDRefs.put(idRef, this);
    }

    /**
     * Get the idref for this bookmark-item
     *
     * @return the idref for the bookmark-item
     */
    public String getIDRef() {
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
        unresolvedIDRefs.put(sub.getIDRef(), sub);
        String[] ids = sub.getIDRefs();
        for (int count = 0; count < ids.length; count++) {
            unresolvedIDRefs.put(ids[count], sub);
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
        return (BookmarkData) subData.get(count);
    }

    /**
     * Get the PageViewport object that this bookmark refers to
     *
     * @return the PageViewport that this bookmark points to
     */
    public PageViewport getPageViewport() {
        return pageRef;
    }

    /**
     * Check if this resolvable object has been resolved.
     * A BookmarkData object is considered resolved once the idrefs for it
     * and for all of its child bookmark-items have been resolved.
     *
     * @return true if this object has been resolved
     */
    public boolean isResolved() {
        return unresolvedIDRefs == null || (unresolvedIDRefs.size() == 0);
    }

    /**
     * @see org.apache.fop.area.Resolvable#getIDRefs()
     */
    public String[] getIDRefs() {
        return (String[])unresolvedIDRefs.keySet().toArray(new String[] {});
    }

    /**
     * Resolve this resolvable object.
     * This resolves the idref of this object and if possible also
     * resolves id references of child elements that have the same
     * id reference.
     *
     * @see org.apache.fop.area.Resolvable#resolveIDRef(String, List)
     * @todo check to make sure works when multiple bookmark-items
     * have the same idref
     */
    public void resolveIDRef(String id, List pages) {
        if (!id.equals(idRef)) {
            BookmarkData bd = (BookmarkData) unresolvedIDRefs.get(id);
            if (bd != null) {
                bd.resolveIDRef(id, pages);
                unresolvedIDRefs.remove(id);
            }
        } else {
            pageRef = (PageViewport) pages.get(0);
            // TODO get rect area of id on page
            unresolvedIDRefs.remove(idRef);
        }
    }

    /**
     * @see org.apache.fop.area.OffDocumentItem#getName()
     */
    public String getName() {
        return "Bookmarks";
    }

    /**
     * Create and return the bookmark data for this outline.
     * This creates a bookmark data with the destination
     * and adds all the data from child outlines.
     *
     * @param outline the Outline object for which a bookmark entry should be
     * created
     * @return the new bookmark data
     */
    private BookmarkData createBookmarkData(Outline outline) {
        BookmarkData data = new BookmarkData(outline.getInternalDestination());
        data.setLabel(outline.getLabel());
        for (int count = 0; count < outline.getOutlines().size(); count++) {
            Outline out = (Outline)(outline.getOutlines()).get(count);
            data.addSubData(createBookmarkData(out));
        }
        return data;
    }

}

