/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.List;
import java.util.Map;

import org.apache.fop.fo.pagination.bookmarks.Bookmark;
import org.apache.fop.fo.pagination.bookmarks.BookmarkTree;

/**
 * An instance of this class is either a PDF bookmark-tree and
 * its child bookmark-items, or a bookmark-item and the child
 * child bookmark-items under it.
 */
public class BookmarkData extends AbstractOffDocumentItem implements Resolvable {

    private List<BookmarkData> subData = new java.util.ArrayList<BookmarkData>();

    // bookmark-title for this fo:bookmark
    private String bookmarkTitle = null;

    // indicator of whether to initially display/hide child bookmarks of this object
    private boolean showChildren = true;

    // ID Reference for this bookmark
    private String idRef;

    // PageViewport that the idRef item refers to
    private PageViewport pageRef = null;

    // unresolved idrefs by this bookmark and child bookmarks below it
    private Map<String, List<Resolvable>> unresolvedIDRefs
            = new java.util.HashMap<String, List<Resolvable>>();

    /**
     * Create a new bookmark data object.
     * This should only be called by the bookmark-tree item because
     * it has no idref item that needs to be resolved.
     *
     * @param bookmarkTree fo:bookmark-tree for this document
     */
    public BookmarkData(BookmarkTree bookmarkTree) {
        this.idRef = null;
        this.whenToProcess = END_OF_DOC;
        // top level defined in Rec to show all child bookmarks
        this.showChildren = true;

        for (int count = 0; count < bookmarkTree.getBookmarks().size(); count++) {
            Bookmark bkmk = (Bookmark)(bookmarkTree.getBookmarks()).get(count);
            addSubData(createBookmarkData(bkmk));
        }
    }

    /**
     * Create a new pdf bookmark data object.
     * This is used by the bookmark-items to create a data object
     * with a idref.  During processing, this idref will be
     * subsequently resolved to a particular PageViewport.
     *
     * @param bookmark the fo:bookmark object
     */
    public BookmarkData(Bookmark bookmark) {
        this.bookmarkTitle = bookmark.getBookmarkTitle();
        this.showChildren = bookmark.showChildItems();
        this.idRef = bookmark.getInternalDestination();
    }

    private void putUnresolved(String id, BookmarkData bd) {
        List<Resolvable> refs = unresolvedIDRefs.get(id);
        if (refs == null) {
            refs = new java.util.ArrayList<Resolvable>();
            unresolvedIDRefs.put(id, refs);
        }
        refs.add(bd);
    }

    /**
     * Create a new bookmark data root object.
     * This constructor is called by the AreaTreeParser when the
     * <bookmarkTree> element is read from the XML file
     */
    public BookmarkData() {
        idRef = null;
        whenToProcess = END_OF_DOC;
        showChildren = true;
    }

    /**
     * Create a new bookmark data object.
     * This constructor is called by the AreaTreeParser when a
     * <bookmark> element is read from the XML file.
     *
     * @param title the bookmark's title
     * @param showChildren whether to initially display the bookmark's children
     * @param pv the target PageViewport
     * @param idRef the target ID
     */
    public BookmarkData(String title, boolean showChildren, PageViewport pv, String idRef) {
        bookmarkTitle = title;
        this.showChildren = showChildren;
        pageRef = pv;
        this.idRef = idRef;
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
     * Add a child bookmark data object.
     * This adds a child bookmark in the bookmark hierarchy.
     *
     * @param sub the child bookmark data
     */
    public void addSubData(BookmarkData sub) {
        subData.add(sub);
        if (sub.pageRef == null) {
            putUnresolved(sub.getIDRef(), sub);
            String[] ids = sub.getIDRefs();
            for (String id : ids) {
                putUnresolved(id, sub);
            }
        }
    }

    /**
     * Get the title for this bookmark object.
     *
     * @return the bookmark title
     */
    public String getBookmarkTitle() {
        return bookmarkTitle;
    }

    /**
     * Indicator of whether to initially display child bookmarks.
     *
     * @return true to initially display child bookmarks, false otherwise
     */
    public boolean showChildItems() {
        return showChildren;
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
        return subData.get(count);
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
     * {@inheritDoc}
     */
    public String[] getIDRefs() {
        return unresolvedIDRefs.keySet().toArray(
                new String[unresolvedIDRefs.keySet().size()]);
    }

    /**
     * Resolve this resolvable object.
     * This resolves the idref of this object and if possible also
     * resolves id references of child elements that have the same
     * id reference.
     *
     * {@inheritDoc}
     */
    public void resolveIDRef(String id, List<PageViewport> pages) {
        if (id.equals(idRef)) {
            //Own ID has been resolved, so note the page
            pageRef = pages.get(0);
            //Note: Determining the placement inside the page is the renderer's job.
        }

        //Notify all child bookmarks
        List<Resolvable> refs = unresolvedIDRefs.get(id);
        if (refs != null) {
            for (Resolvable res : refs) {
                res.resolveIDRef(id, pages);
            }
        }
        unresolvedIDRefs.remove(id);
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "Bookmarks";
    }

    /**
     * Create and return the bookmark data for this bookmark
     * This creates a bookmark data with the destination
     * and adds all the data from child bookmarks
     *
     * @param bookmark the Bookmark object for which a bookmark entry should be
     * created
     * @return the new bookmark data
     */
    private BookmarkData createBookmarkData(Bookmark bookmark) {
        BookmarkData data = new BookmarkData(bookmark);
        for (int count = 0; count < bookmark.getChildBookmarks().size(); count++) {
            Bookmark bkmk = (Bookmark)(bookmark.getChildBookmarks()).get(count);
            data.addSubData(createBookmarkData(bkmk));
        }
        return data;
    }

}
