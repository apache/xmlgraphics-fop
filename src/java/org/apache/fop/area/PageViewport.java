/*
 * $Id: PageViewport.java,v 1.16 2003/03/05 15:19:31 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.area;

import java.awt.geom.Rectangle2D;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.fop.fo.Constants;

/**
 * Page viewport that specifies the viewport area and holds the page contents.
 * This is the top level object for a page and remains valid for the life
 * of the document and the area tree.
 * This object may be used as a key to reference a page.
 * This is the level that creates the page.
 * The page (reference area) is then rendered inside the page object
 */
public class PageViewport implements Resolveable, Cloneable {
    
    private Page page;
    private Rectangle2D viewArea;
    private boolean clip = false;
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
     * Get the key for this page viewport.
     * This is used so that a serializable key can be used to
     * lookup the page or some other reference.
     *
     * @return a unique page viewport key for this area tree
     */
    public String getKey() {
        return toString();
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
     * Should this logic be placed in the Page layout manager.
     *
     * @param marks the map of markers to add
     * @param start if the area being added is starting or ending
     * @param isfirst isfirst or islast flag
     */
    public void addMarkers(Map marks, boolean start, boolean isfirst) {
        if (start) {
            if (isfirst) {
                if (markerFirstStart == null) {
                    markerFirstStart = new HashMap();
                }
                if (markerFirstAny == null) {
                    markerFirstAny = new HashMap();
                }
                // only put in new values, leave current
                for (Iterator iter = marks.keySet().iterator(); iter.hasNext();) {
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
        Object mark = null;
        switch (pos) {
            case Constants.RetrievePosition.FSWP:
                if (markerFirstStart != null) {
                    mark = markerFirstStart.get(name);
                }
                if (mark == null && markerFirstAny != null) {
                    mark = markerFirstAny.get(name);
                }
            break;
            case Constants.RetrievePosition.FIC:
                if (markerFirstAny != null) {
                    mark = markerFirstAny.get(name);
                }
            break;
            case Constants.RetrievePosition.LSWP:
                if (markerLastStart != null) {
                    mark = markerLastStart.get(name);
                }
                if (mark == null && markerLastAny != null) {
                    mark = markerLastAny.get(name);
                }
            break;
            case Constants.RetrievePosition.LEWP:
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
     * it holds id and marker information and is used as a key.
     */
    public void clear() {
        page = null;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append("PageViewport: page=");
        sb.append(getPageNumber());
        return sb.toString();
    }
}
