/*
 * $Id$
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
package org.apache.fop.area.extensions;

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

