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

package org.apache.fop.render.intermediate.extensions;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.xmlgraphics.util.XMLizable;

/**
 * This class is the root of the bookmark tree for use in the intermediate format.
 */
public class BookmarkTree implements XMLizable, DocumentNavigationExtensionConstants {

    private List bookmarks = new java.util.ArrayList();

    /**
     * Constructs a new bookmark tree.
     */
    public BookmarkTree() {
        //nop
    }

    /**
     * Adds a new top-level bookmark.
     * @param bookmark the bookmark
     */
    public void addBookmark(Bookmark bookmark) {
        this.bookmarks.add(bookmark);
    }

    /**
     * Returns a list of top-level bookmarks.
     * @return the top-level bookmarks
     */
    public List getBookmarks() {
        return Collections.unmodifiableList(this.bookmarks);
    }

    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        handler.startElement(BOOKMARK_TREE.getNamespaceURI(),
                BOOKMARK_TREE.getLocalName(), BOOKMARK_TREE.getQName(), atts);
        Iterator iter = this.bookmarks.iterator();
        while (iter.hasNext()) {
            Bookmark b = (Bookmark)iter.next();
            b.toSAX(handler);
        }
        handler.endElement(BOOKMARK_TREE.getNamespaceURI(),
                BOOKMARK_TREE.getLocalName(), BOOKMARK_TREE.getQName());
    }

}
