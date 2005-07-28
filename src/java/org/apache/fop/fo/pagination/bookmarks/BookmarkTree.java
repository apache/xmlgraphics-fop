/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.fop.fo.pagination.bookmarks;

// Java
import java.util.ArrayList;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.pagination.Root;

/**
 * The fo:bookmark-tree formatting object, first introduced in the 
 * XSL 1.1 WD.  Prototype version only, subject to change as XSL 1.1 WD
 * evolves.
 */
public class BookmarkTree extends FObj {
    private ArrayList bookmarks = new ArrayList();

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public BookmarkTree(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FONode#addChildNode(FONode)
     */
    protected void addChildNode(FONode obj) {
        if (obj instanceof Bookmark) {
            bookmarks.add(obj);
        }
    }

    /**
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
        if (bookmarks == null) {
           missingChildElementError("(fo:bookmark+)");
        }
        ((Root) parent).setBookmarkTree(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
        XSL/FOP: (bookmark+)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
        if (!(FO_URI.equals(nsURI) &&
            localName.equals("bookmark"))) {
                invalidChildError(loc, nsURI, localName);
        }
    }

    public ArrayList getBookmarks() {
        return bookmarks;
    }

    public String getName() {
        return "fo:bookmark-tree";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_BOOKMARK_TREE;
    }
}
