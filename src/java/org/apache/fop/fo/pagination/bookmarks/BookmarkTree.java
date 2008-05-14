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
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_bookmark-tree">
 * <code>fo:bookmark-tree</code></a> object, first introduced in the
 * XSL 1.1 WD.
 */
public class BookmarkTree extends FObj {
    private ArrayList bookmarks = new ArrayList();

    /**
     * Create a new BookmarkTree object that is a child
     * of the given {@link FONode}.
     *
     * @param parent the {@link FONode} parent
     */
    public BookmarkTree(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    protected void addChildNode(FONode obj) {
        if (obj instanceof Bookmark) {
            bookmarks.add(obj);
        }
    }

    /** {@inheritDoc} */
        protected void endOfNode() throws FOPException {
        if (bookmarks == null) {
           missingChildElementError("(fo:bookmark+)");
        }
        ((Root) parent).setBookmarkTree(this);
    }

    /**
     * {@inheritDoc}
     * <br>XSL/FOP: (bookmark+)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (!localName.equals("bookmark")) {
                invalidChildError(loc, nsURI, localName);
            }
        }
    }

    /**
     * Get the descendant {@link Bookmark}s.
     * @return an <code>ArrayList</code> containing the {@link Bookmark} objects.
     */
    public ArrayList getBookmarks() {
        return bookmarks;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "bookmark-tree";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_BOOKMARK_TREE}
     */
    public int getNameId() {
        return FO_BOOKMARK_TREE;
    }
}
