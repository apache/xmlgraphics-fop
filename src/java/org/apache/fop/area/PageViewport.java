/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: PageViewport.java,v 1.16 2003/03/05 15:19:31 jeremias Exp $
 */ 
package org.apache.fop.area;

import java.awt.geom.Rectangle2D;
//import java.io.IOException;
//import java.io.ObjectOutputStream;
//import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.FoPageSequence;
import org.apache.fop.fo.properties.RetrievePosition;

/**
 * Page viewport that specifies the viewport area.
 * This is the top level object for a page and remains valid for the life
 * of the document and the area tree.
 * This object may be used as a key to reference a page.
 * The immediate and only child of any viewport is a reference-area; in this
 * cast the page-reference-area.
 * The page reference area is then rendered inside the PageRefArea object
 */
public class PageViewport
extends AbstractViewport
implements Viewport, Resolveable {

    /** Unique ID for this page.  0 is an invalid ID.  */
    private long pageId = 0;
    /** The formatted page number */
    private String pageNumber = null;

    // list of id references and the rectangle on the page
    private Map idReferences = null;

    // this keeps a list of currently unresolved areas or extensions
    // once the thing is resolved it is removed
    // when this is empty the page can be rendered
    private Map unresolved = null;

    private Map pendingResolved = null;

    // hashmap of markers for this page
    // start and end are added by the fo that contains the markers
    private Map markerFirstStart = null;
    private Map markerLastStart = null;
    private Map markerFirstAny = null;
    private Map markerLastEnd = null;
    private Map markerLastAny = null;

    /**
     * Create a page viewport at the root of a tree, synchronized on itself,
     * with a given page reference area and viewport dimensions
     * @param pageId
     * @param p the page reference area for the contents of this page
     * @param bounds the dimensions of the viewport
     */
    public PageViewport(
            FoPageSequence pageSeq,
            FONode generatedBy,
            long pageId,
            Rectangle2D bounds,
            PageRefArea p) {
        super(bounds, pageSeq, generatedBy);
        this.pageId = pageId;
        refArea = p;
    }

    /**
     * Create a page viewport.
     * @param parent node of this viewport
     * @param sync object on which the Area is synchronized
     * @param pageId the unique identifier of this page
     * @param p the page reference area for the contents of this page
     * @param bounds the dimensions of the viewport
     */
    public PageViewport(
            FoPageSequence pageSeq,
            FONode generatedBy,
            long pageId,
            Rectangle2D bounds,
            PageRefArea p,
            Node parent,
            Object sync) {
        super(bounds, pageSeq, generatedBy, parent, sync);
        this.pageId = pageId;
        refArea = p;
    }

    /**
     * Create a page viewport with a given parent node, sync object and ID
     * @param parent
     * @param sync
     * @param pageId
     */
    public PageViewport(
            FoPageSequence pageSeq,
            FONode generatedBy,
            long pageId,
            Node parent,
            Object sync) {
        super(pageSeq, generatedBy, parent, sync);
        this.pageId = pageId;
        refArea = null;
        viewArea = null;
    }

    /**
     * @return the pageId
     */
    public long getPageId() {
        synchronized (sync) {
            return pageId;
        }
    }

    /**
     * @param pageId to set
     */
    public void setPageId(long pageId) {
        synchronized (sync) {
            this.pageId = pageId;
        }
    }
    
    /**
     * Get the page reference area with the contents.
     * @return the page reference area
     */
    public PageRefArea getPageRefArea() {
        synchronized (sync) {
            return (PageRefArea)getReferenceArea();
        }
    }

    /**
     * Set the page number for this page.
     * @param num the string representing the page number
     */
    public void setPageNumber(String num) {
        synchronized (sync) {
            pageNumber = num;
        }
    }

    /**
     * Get the page number of this page.
     * @return the string that represents this page
     */
    public String getPageNumber() {
        synchronized (sync) {
            return pageNumber;
        }
    }

    /**
     * Add an unresolved id to this page.
     * All unresolved ids for the contents of this page are
     * added to this page. This is so that the resolvers can be
     * serialized with the pageRefArea to preserve the proper function.
     * @param id the id of the reference
     * @param res the resolver of the reference
     */
    public void addUnresolvedID(String id, Resolveable res) {
        synchronized (sync) {
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
    }

    /**
     * Check if this page has been fully resolved.
     * @return true if the page is resolved and can be rendered
     */
    public boolean isResolved() {
        synchronized (sync) {
            return unresolved == null;
        }
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
        synchronized (sync) {
            if (refArea == null) {
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
    }

    /**
     * Add the markers for this page.
     * Only the required markers are kept.
     * For "first-starting-within-page" it adds the markers
     * that are starting only if the marker class name is not
     * already added.
     * For "first-including-carryover" it adds any starting marker
     * if the marker class name is not already added.
     * For "last-starting-within-page" it adds all marks that
     * are starting, replacing earlier markers.
     * For "last-ending-within-page" it adds all markers that
     * are ending, replacing earlier markers.
     * 
     * Should this logic be placed in the page layout manager.
     *
     * @param marks the map of markers to add
     * @param start if the area being added is starting or ending
     * @param isfirst isfirst or islast flag
     */
    public void addMarkers(Map marks, boolean start, boolean isfirst) {
        synchronized (sync) {
            if (start) {
                if (isfirst) {
                    if (markerFirstStart == null) {
                        markerFirstStart = new HashMap();
                    }
                    if (markerFirstAny == null) {
                        markerFirstAny = new HashMap();
                    }
                    // only put in new values, leave current
                    for (
                            Iterator iter = marks.keySet().iterator();
                            iter.hasNext();
                            ) {
                        Object key = iter.next();
                        if (!markerFirstStart.containsKey(key)) {
                            markerFirstStart.put(key, marks.get(key));
                        }
                        if (!markerFirstAny.containsKey(key)) {
                            markerFirstAny.put(key, marks.get(key));
                        }
                    }
                    if (markerLastStart == null) {
                        markerLastStart = new HashMap();
                    }
                    // replace all
                    markerLastStart.putAll(marks);
                    
                } else {
                    if (markerFirstAny == null) {
                        markerFirstAny = new HashMap();
                    }
                    // only put in new values, leave current
                    for (Iterator iter = marks.keySet().iterator(); iter.hasNext();) {
                        Object key = iter.next();
                        if (!markerFirstAny.containsKey(key)) {
                            markerFirstAny.put(key, marks.get(key));
                        }
                    }
                }
            } else {
                if (!isfirst) {
                    if (markerLastEnd == null) {
                        markerLastEnd = new HashMap();
                    }
                    // replace all
                    markerLastEnd.putAll(marks);
                }
                if (markerLastAny == null) {
                    markerLastAny = new HashMap();
                }
                // replace all
                markerLastAny.putAll(marks);
            }
        }
    }

    /**
     * Get a marker from this page.
     * This will retrieve a marker with the class name
     * and position.
     *
     * @param name The class name of the marker to retrieve 
     * @param pos the position to retrieve
     * @return Object the marker found or null
     */
    public Object getMarker(String name, int pos) {
        synchronized (sync) {
            Object mark = null;
            switch (pos) {
                case RetrievePosition.FIRST_STARTING_WITHIN_PAGE:
                    if (markerFirstStart != null) {
                        mark = markerFirstStart.get(name);
                    }
                if (mark == null && markerFirstAny != null) {
                    mark = markerFirstAny.get(name);
                }
                break;
                case RetrievePosition.FIRST_INCLUDING_CARRYOVER:
                    if (markerFirstAny != null) {
                        mark = markerFirstAny.get(name);
                    }
                break;
                case RetrievePosition.LAST_STARTING_WITHIN_PAGE:
                    if (markerLastStart != null) {
                        mark = markerLastStart.get(name);
                    }
                if (mark == null && markerLastAny != null) {
                    mark = markerLastAny.get(name);
                }
                break;
                case RetrievePosition.LAST_ENDING_WITHIN_PAGE:
                    if (markerLastEnd != null) {
                        mark = markerLastEnd.get(name);
                    }
                if (mark == null && markerLastAny != null) {
                    mark = markerLastAny.get(name);
                }
                break;
            }
            return mark;
        }
    }

//    /**
//     * Save the viewport pageRefArea to an object stream.
//     * The map of unresolved references are set on the pageRefArea so that
//     * the resolvers can be properly serialized and reloaded.
//     * @param out the object output stream to write the contents
//     */
//    public void savePage(ObjectOutputStream out) {
//        // set the unresolved references so they are serialized
//        synchronized (sync) {
//            ((PageRefArea)refArea).setUnresolvedReferences(unresolved);
//            try {
//                out.writeObject(refArea);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            refArea = null;
//        }
//    }

//    /**
//     * Load the viewport pageRefArea from an object stream.
//     * This loads the pageRefArea from the stream and
//     * if there are any unresolved references that were resolved
//     * while saved they will be resolved on the page contents.
//     * @param in the object input stream to read the pageRefArea from
//     * @throws Exception if there is an error loading the pageRefArea
//     */
//    public void loadPage(ObjectInputStream in) throws Exception {
//        synchronized (sync) {
//            PageRefArea pageRefArea = (PageRefArea) in.readObject();
//            refArea = pageRefArea;
//            unresolved = pageRefArea.getUnresolvedReferences();
//            if (unresolved != null && pendingResolved != null) {
//                for (Iterator iter = pendingResolved.keySet().iterator();
//                iter.hasNext();) {
//                    String id = (String) iter.next();
//                    resolve(id, (List)pendingResolved.get(id));
//                }
//                pendingResolved = null;
//            }
//        }
//    }

    /**
     * Clone this page.
     * Used by the page master to create a copy of an original page.
     * @return a copy of this page and associated viewports
     */
    public Object clone() {
        synchronized (sync) {
            PageViewport pv;
            try {
                pv = (PageViewport)(super.clone());
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            pageId = 0;  // N.B. This invalidates the page id
            pv.refArea = (PageRefArea)(refArea.clone());
            return pv;
        }
    }

    /**
     * Clear the pageRefArea contents to save memory.
     * This object is kept for the life of the area tree since
     * it holds id and marker information and is used as a key.
     */
    public void clear() {
        synchronized (sync) {
            refArea = null;
        }
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        synchronized (sync) {
            StringBuffer sb = new StringBuffer(64);
            sb.append("PageViewport: page=");
            sb.append(getPageNumber());
            return sb.toString();
        }
    }
}
