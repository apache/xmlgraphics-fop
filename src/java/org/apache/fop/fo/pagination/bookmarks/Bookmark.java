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

/* $Id $ */

package org.apache.fop.fo.pagination.bookmarks;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.extensions.Label;


/**
 * The fo:bookmark formatting object, first introduced in the 
 * XSL 1.1 WD.  Prototype version only, subject to change as
 * XSL 1.1 WD evolves.
 */
public class Bookmark extends FObj {
    private Label bookmarkTitle;
    private ArrayList childBookmarks = new ArrayList();

    private String internalDestination;
    private String externalDestination;

    /**
     * Create a new bookmark object.
     *
     * @param parent the parent fo node
     */
    public Bookmark(FONode parent) {
        super(parent);
    }

    /**
     * The attributes on the bookmark object are the internal and external
     * destination. One of these is required.
     *
     * @see org.apache.fop.fo.FObj#processNode
     * @todo to include all properties of fo:bookmark
     */
    public void processNode(String elementName, Locator locator, 
            Attributes attlist, PropertyList propertyList) throws FOPException 
    {
        internalDestination =
            attlist.getValue("internal-destination");
        externalDestination =
            attlist.getValue("external-destination");
        if (externalDestination != null && !externalDestination.equals("")) {
            getLogger().warn("fo:bookmark external-destination not supported currently.");
        }

        if (internalDestination == null || internalDestination.equals("")) {
            getLogger().warn("fo:bookmark requires an internal-destination.");
        }

    }

    /**
     * @see org.apache.fop.fo.FONode#addChildNode(FONode)
     */
    protected void addChildNode(FONode obj) {
        if (obj instanceof Label) {
            bookmarkTitle = (Label)obj;
        } else if (obj instanceof Bookmark) {
            childBookmarks.add(obj);
        }
    }

    /**
     * Get the bookmark title for this bookmark
     *
     * @return the bookmark title string or an empty string if not found
     */
    public String getBookmarkTitle() {
        return bookmarkTitle == null ? "" : bookmarkTitle.toString();
    }

    public String getInternalDestination() {
        return internalDestination;
    }

    public String getExternalDestination() {
        return externalDestination;
    }

    public ArrayList getChildBookmarks() {
        return childBookmarks;
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:bookmark";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_BOOKMARK;
    }
}
