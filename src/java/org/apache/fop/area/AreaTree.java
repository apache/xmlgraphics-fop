/*
 * $Id: AreaTree.java,v 1.16 2003/03/05 15:19:31 jeremias Exp $
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

import org.apache.fop.area.extensions.BookmarkData;
import org.apache.fop.fo.extensions.Outline;
import org.apache.fop.apps.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Area tree for formatting objects.
 *
 * Concepts:
 * The area tree is to be as small as possible. With minimal classes
 * and data to fully represent an area tree for formatting objects.
 * The area tree needs to be simple to render and follow the spec
 * closely.
 * This area tree has the concept of page sequences.
 * Where ever possible information is discarded or optimised to
 * keep memory use low. The data is also organised to make it
 * possible for renderers to minimise their output.
 * A page can be saved if not fully resolved and once rendered
 * a page contains only size and id reference information.
 * The area tree pages are organised in a model that depends on the
 * type of renderer.
 */
public class AreaTree {
    // allows for different models to deal with adding/rendering
    // in different situations
    private AreaTreeModel model;

    // hashmap of arraylists containing pages with id area
    private Map idLocations = new HashMap();
    // list of id's yet to be resolved and arraylists of pages
    private Map resolve = new HashMap();
    private List treeExtensions = new ArrayList();

    /**
     * Create a new store pages model.
     * @return StorePagesModel the new model
     */
    public static StorePagesModel createStorePagesModel() {
        return new StorePagesModel();
    }

    /**
     * Set the tree model to use for this area tree.
     * The different models can have different behaviour
     * when pages area added and other changes.
     * @param m the area tree model
     */
    public void setTreeModel(AreaTreeModel m) {
        model = m;
    }

    /**
     * Get the area tree model for this area tree.
     *
     * @return AreaTreeModel the model being used for this area tree
     */
    public AreaTreeModel getAreaTreeModel() {
        return model;
    }

    /**
     * Start a new page sequence.
     * This signals that a new page sequence has started in the document.
     * @param title the title of the new page sequence or null if no title
     */
    public void startPageSequence(Title title) {
        model.startPageSequence(title);
    }

    /**
     * Add a new page to the area tree.
     * @param page the page to add
     */
    public void addPage(PageViewport page) {
        model.addPage(page);
    }

    /**
     * Add an id reference pointing to a page viewport.
     * @param id the id of the reference
     * @param pv the page viewport that contains the id reference
     */
    public void addIDRef(String id, PageViewport pv) {
        List list = (List)idLocations.get(id);
        if (list == null) {
            list = new ArrayList();
            idLocations.put(id, list);
        }
        list.add(pv);

        Set todo = (Set)resolve.get(id);
        if (todo != null) {
            for (Iterator iter = todo.iterator(); iter.hasNext();) {
                Resolveable res = (Resolveable)iter.next();
                res.resolve(id, list);
            }
            resolve.remove(id);
        }
    }

    /**
     * Get the list of id references for an id.
     * @param id the id to lookup
     * @return the list of id references.
     */
    public List getIDReferences(String id) {
        return (List)idLocations.get(id);
    }

    /**
     * Add an unresolved object with a given id.
     * @param id the id reference that needs resolving
     * @param res the Resolveable object to resolve
     */
    public void addUnresolvedID(String id, Resolveable res) {
        Set todo = (Set)resolve.get(id);
        if (todo == null) {
            todo = new HashSet();
            resolve.put(id, todo);
        }
        todo.add(res);
    }

    /**
     * Add a tree extension.
     * This checks if the extension is resolveable and attempts
     * to resolve or add the resolveable ids for later resolution.
     * @param ext the tree extension to add.
     */
    public void addTreeExtension(TreeExt ext) {
        treeExtensions.add(ext);
        if (ext.isResolveable()) {
            Resolveable res = (Resolveable)ext;
            String[] ids = res.getIDs();
            for (int count = 0; count < ids.length; count++) {
                if (idLocations.containsKey(ids[count])) {
                    res.resolve(ids[count], (List)idLocations.get(ids[count]));
                } else {
                    Set todo = (Set)resolve.get(ids[count]);
                    if (todo == null) {
                        todo = new HashSet();
                        resolve.put(ids[count], todo);
                    }
                    todo.add(ext);
                }
            }
        } else {
            handleTreeExtension(ext, TreeExt.IMMEDIATELY);
        }
    }

    /**
     * Handle a tree extension.
     * This sends the extension to the model for handling.
     * @param ext the tree extension to handle
     * @param when when the extension should be handled by the model
     */
    public void handleTreeExtension(TreeExt ext, int when) {
        // queue tree extension according to the when
        model.addExtension(ext, when);
    }

    /**
     * Signal end of document.
     * This indicates that the document is complete and any unresolved
     * reference can be dealt with.
     */
    public void endDocument() {
        for (Iterator iter = resolve.keySet().iterator(); iter.hasNext();) {
            String id = (String)iter.next();
            Set list = (Set)resolve.get(id);
            for (Iterator resIter = list.iterator(); resIter.hasNext();) {
                Resolveable res = (Resolveable)resIter.next();
                if (!res.isResolved()) {
                    res.resolve(id, null);
                }
            }
        }
        model.endDocument();
    }

    /**
     * When this element is finished then it can create
     * the bookmark data from the child elements and add
     * the extension to the area tree.
     */
    public void addBookmarksToAreaTree(Document document) {
        if (document.getBookmarks() == null) {
            return;
        }
        document.getDriver().getLogger().debug("adding bookmarks to area tree");
        BookmarkData data = new BookmarkData();
        for (int count = 0; count < document.getBookmarks().getOutlines().size(); count++) {
            Outline out = (Outline)(document.getBookmarks().getOutlines()).get(count);
            data.addSubData(createBookmarkData(out));
        }
        // add data to area tree for resolving and handling
        AreaTree at = document.getAreaTree();
        at.addTreeExtension(data);
        data.setAreaTree(at);
    }

    /**
     * Create and return the bookmark data for this outline.
     * This creates a bookmark data with the destination
     * and adds all the data from child outlines.
     *
     * @return the new bookmark data
     */
    public BookmarkData createBookmarkData(Outline outline) {
        BookmarkData data = new BookmarkData(outline.getInternalDestination());
        data.setLabel(outline.getLabel());
        for (int count = 0; count < outline.getOutlines().size(); count++) {
            Outline out = (Outline)(outline.getOutlines()).get(count);
            data.addSubData(createBookmarkData(out));
        }
        return data;
    }

}
