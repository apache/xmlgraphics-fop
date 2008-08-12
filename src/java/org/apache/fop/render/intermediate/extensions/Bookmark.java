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

import org.apache.fop.util.XMLUtil;

/**
 * This class is a bookmark element for use in the intermediate format.
 */
public class Bookmark implements XMLizable, BookmarkExtensionConstants {

    private String title;
    private boolean show;
    private List childBookmarks;
    private AbstractAction action;

    /**
     * Creates a new bookmark.
     * @param title the bookmark's title
     * @param show true if the bookmark shall be shown, false for hidden
     * @param action the action performed when the bookmark is clicked
     */
    public Bookmark(String title, boolean show, AbstractAction action) {
        this.title = title;
        this.show = show;
        this.action = action;
    }

    /**
     * Returns the bookmark's title.
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Indicates whether the bookmark shall be shown initially.
     * @return true if it shall be shown
     */
    public boolean isShown() {
        return this.show;
    }

    /**
     * Returns the action performed when the bookmark is clicked.
     * @return the action
     */
    public AbstractAction getAction() {
        return this.action;
    }

    /**
     * Sets the action performed when the bookmark is clicked.
     * @param action the action
     */
    public void setAction(AbstractAction action) {
        this.action = action;
    }

    /**
     * Adds a child bookmark.
     * @param bookmark the child bookmark
     */
    public void addChildBookmark(Bookmark bookmark) {
        if (this.childBookmarks == null) {
            this.childBookmarks = new java.util.ArrayList();
        }
        this.childBookmarks.add(bookmark);
    }

    /**
     * Returns a list of child bookmarks.
     * @return the child bookmarks
     */
    public List getChildBookmarks() {
        if (this.childBookmarks == null) {
            return Collections.EMPTY_LIST;
        } else {
            return Collections.unmodifiableList(this.childBookmarks);
        }
    }

    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(null, "title", "title", XMLUtil.CDATA, getTitle());
        atts.addAttribute(null, "starting-state", "starting-state",
                XMLUtil.CDATA, isShown() ? "show" : "hide");
        handler.startElement(BOOKMARK.getNamespaceURI(),
                BOOKMARK.getLocalName(), BOOKMARK.getQName(), atts);
        if (getAction() != null) {
            getAction().toSAX(handler);
        }
        if (this.childBookmarks != null) {
            Iterator iter = this.childBookmarks.iterator();
            while (iter.hasNext()) {
                Bookmark b = (Bookmark)iter.next();
                b.toSAX(handler);
            }
        }
        handler.endElement(BOOKMARK.getNamespaceURI(),
                BOOKMARK.getLocalName(), BOOKMARK.getQName());
    }

}
