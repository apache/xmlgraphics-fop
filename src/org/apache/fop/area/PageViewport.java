/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.awt.geom.Rectangle2D;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap; 
import java.util.Iterator;

/**
 * Page viewport that specifies the viewport area and holds the page contents.
 * This is the top level object for a page and remains valid for the life
 * of the document and the area tree.
 * This object may be used as a key to reference a page.
 * This is the level that creates the page
 * The page (reference area) is then rendered inside the page object
 */
public class PageViewport implements Resolveable, Cloneable {
    private Page page;
    private Rectangle2D viewArea;
    private boolean clip = false;
    private String pageNumber = null;

    // list of id references and the rectangle on the page
    private HashMap idReferences = null;

    // this keeps a list of currently unresolved areas or extensions
    // once the thing is resolved it is removed
    // when this is empty the page can be rendered
    private HashMap unresolved = null;

    private HashMap pendingResolved = null;

    /**
     * Create a page viewport.
     * @param p the page reference area that holds the contents
     * @param bounds the bounds of this viewport
     */
    public PageViewport(Page p, Rectangle2D bounds) {
        page = p;
        viewArea = bounds;
    }

    /**
     * Set if this viewport should clip.
     * @param c true if this viewport should clip
     */
    public void setClip(boolean c) {
        clip = c;
    }

    /**
     * Get the view area rectangle of this viewport.
     * @return the rectangle for this viewport
     */
    public Rectangle2D getViewArea() {
        return viewArea;
    }

    /**
     * Get the page reference area with the contents.
     * @return the page reference area
     */
    public Page getPage() {
        return page;
    }

    /**
     * Set the page number for this page.
     * @param num the string representing the page number
     */
    public void setPageNumber(String num) {
        pageNumber = num;
    }

    /**
     * Get the page number of this page.
     * @return the string that represents this page
     */
    public String getPageNumber() {
        return pageNumber;
    }

    /**
     * Add an unresolved id to this page.
     * All unresolved ids for the contents of this page are
     * added to this page. This is so that the resolvers can be
     * serialized with the page to preserve the proper function.
     * @param id the id of the reference
     * @param res the resolver of the reference
     */
    public void addUnresolvedID(String id, Resolveable res) {
        if (unresolved == null) {
            unresolved = new HashMap();
        }
        List list = (List)unresolved.get(id);
        if (list == null) {
            list = new ArrayList();
            unresolved.put(id, list);
        }
        list.add(res);
    }

    /**
     * Check if this page has been fully resolved.
     * @return true if the page is resolved and can be rendered
     */
    public boolean isResolved() {
        return unresolved == null;
    }

    /**
     * Get the id references for this page.
     * @return always null
     */
    public String[] getIDs() {
        return null;
    }

    /**
     * This resolves reference with a list of pages.
     * The pages (PageViewport) contain the rectangle of the area.
     * @param id the id to resolve
     * @param pages the list of pages with the id area
     *              may be null if not found
     */
    public void resolve(String id, List pages) {
        if (page == null) {
            if (pendingResolved == null) {
                pendingResolved = new HashMap();
            }
            pendingResolved.put(id, pages);
        } else {
            if (unresolved != null) {
                List todo = (List)unresolved.get(id);
                if (todo != null) {
                    for (int count = 0; count < todo.size(); count++) {
                        Resolveable res = (Resolveable)todo.get(count);
                        res.resolve(id, pages);
                    }
                }
            }
        }
        if (unresolved != null) {
            unresolved.remove(id);
            if (unresolved.isEmpty()) {
                unresolved = null;
            }
        }
    }

    /**
     * Save the page contents to an object stream.
     * The map of unresolved references are set on the page so that
     * the resolvers can be properly serialized and reloaded.
     * @param out the object output stream to write the contents
     * @throws Exception if there is a problem saving the page
     */
    public void savePage(ObjectOutputStream out) throws Exception {
        // set the unresolved references so they are serialized
        page.setUnresolvedReferences(unresolved);
        out.writeObject(page);
        page = null;
    }

    /**
     * Load the page contents from an object stream.
     * This loads the page contents from the stream and
     * if there are any unresolved references that were resolved
     * while saved they will be resolved on the page contents.
     * @param in the object input stream to read the page from
     * @throws Exception if there is an error loading the page
     */
    public void loadPage(ObjectInputStream in) throws Exception {
        page = (Page) in.readObject();
        unresolved = page.getUnresolvedReferences();
        if (unresolved != null && pendingResolved != null) {
            for (Iterator iter = pendingResolved.keySet().iterator();
                         iter.hasNext();) {
                String id = (String) iter.next();
                resolve(id, (List)pendingResolved.get(id));
            }
            pendingResolved = null;
        }
    }

    /**
     * Clone this page.
     * Used by the page master to create a copy of an original page.
     * @return a copy of this page and associated viewports
     */
    public Object clone() {
        Page p = (Page)page.clone();
        PageViewport ret = new PageViewport(p, (Rectangle2D)viewArea.clone());
        return ret;
    }

    /**
     * Clear the page contents to save memory.
     * This object is kept for the life of the area tree since
     * it holds id information and is used as a key.
     */
    public void clear() {
        page = null;
    }
}
