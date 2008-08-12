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

import java.awt.Point;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.render.ps.extensions.PSExtensionAttachment;
import org.apache.fop.util.ContentHandlerFactory;
import org.apache.fop.util.XMLUtil;

/**
 * Factory for the ContentHandler that handles the IF document navigation namespace.
 */
public class DocumentNavigationExtensionHandlerFactory
        implements ContentHandlerFactory, DocumentNavigationExtensionConstants {

    /** Logger instance */
    protected static Log log = LogFactory.getLog(DocumentNavigationExtensionHandlerFactory.class);

    /** {@inheritDoc} */
    public String[] getSupportedNamespaces() {
        return new String[] {NAMESPACE};
    }

    /** {@inheritDoc} */
    public ContentHandler createContentHandler() {
        return new Handler();
    }

    private static class Handler extends DefaultHandler
                implements ContentHandlerFactory.ObjectSource {

        private StringBuffer content = new StringBuffer();
        private Stack objectStack = new Stack();

        private Object objectBuilt;
        private ObjectBuiltListener listener;

        /** {@inheritDoc} */
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            boolean handled = false;
            if (NAMESPACE.equals(uri)) {
                if (BOOKMARK_TREE.getLocalName().equals(localName)) {
                    if (!objectStack.isEmpty()) {
                        throw new SAXException(localName + " must be the root element!");
                    }
                    BookmarkTree bookmarkTree = new BookmarkTree();
                    objectStack.push(bookmarkTree);
                } else if (BOOKMARK.getLocalName().equals(localName)) {
                    String title = attributes.getValue("title");
                    String s = attributes.getValue("starting-state");
                    boolean show = !"hide".equals(s);
                    Bookmark b = new Bookmark(title, show, null);
                    Object o = objectStack.peek();
                    if (o instanceof AbstractAction) {
                        AbstractAction action = (AbstractAction)objectStack.pop();
                        o = objectStack.peek();
                        ((Bookmark)o).setAction(action);
                    }
                    if (o instanceof BookmarkTree) {
                        ((BookmarkTree)o).addBookmark(b);
                    } else {
                        ((Bookmark)o).addChildBookmark(b);
                    }
                    objectStack.push(b);
                } else if (NAMED_DESTINATION.getLocalName().equals(localName)) {
                    if (!objectStack.isEmpty()) {
                        throw new SAXException(localName + " must be the root element!");
                    }
                    String name = attributes.getValue("name");
                    NamedDestination dest = new NamedDestination(name, null);
                    objectStack.push(dest);
                } else if (GOTO_XY.getLocalName().equals(localName)) {
                    int pageIndex = XMLUtil.getAttributeAsInt(attributes, "page-index");
                    int x = XMLUtil.getAttributeAsInt(attributes, "x");
                    int y = XMLUtil.getAttributeAsInt(attributes, "y");
                    GoToXYAction action = new GoToXYAction(pageIndex, new Point(x, y));
                    objectStack.push(action);
                } else if (GOTO_URI.getLocalName().equals(localName)) {
                    String gotoURI = attributes.getValue("uri");
                    URIAction action = new URIAction(gotoURI);
                    objectStack.push(action);
                } else {
                    throw new SAXException(
                            "Invalid element " + localName + " in namespace: " + uri);
                }
                handled = true;
            }
            if (!handled) {
                if (PSExtensionAttachment.CATEGORY.equals(uri)) {
                    throw new SAXException("Unhandled element " + localName + " in namespace: "
                            + uri);
                } else {
                    log.warn("Unhandled element " + localName + " in namespace: " + uri);
                }
            }
        }

        /** {@inheritDoc} */
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (NAMESPACE.equals(uri)) {
                if (BOOKMARK_TREE.getLocalName().equals(localName)) {
                    //nop
                } else if (BOOKMARK.getLocalName().equals(localName)) {
                    if (objectStack.peek() instanceof AbstractAction) {
                        AbstractAction action = (AbstractAction)objectStack.pop();
                        Bookmark b = (Bookmark)objectStack.pop();
                        b.setAction(action);
                    } else {
                        objectStack.pop();
                    }
                } else if (NAMED_DESTINATION.getLocalName().equals(localName)) {
                    AbstractAction action = (AbstractAction)objectStack.pop();
                    NamedDestination dest = (NamedDestination)objectStack.peek();
                    dest.setAction(action);
                }
            }
            content.setLength(0); // Reset text buffer (see characters())
        }

        /** {@inheritDoc} */
        public void characters(char[] ch, int start, int length) throws SAXException {
            content.append(ch, start, length);
        }

        /** {@inheritDoc} */
        public void endDocument() throws SAXException {
            this.objectBuilt = objectStack.pop();
            assert objectStack.isEmpty();
            if (listener != null) {
                listener.notifyObjectBuilt(this.objectBuilt);
            }
        }

        /** {@inheritDoc} */
        public Object getObject() {
            return objectBuilt;
        }

        /** {@inheritDoc} */
        public void setObjectBuiltListener(ObjectBuiltListener listener) {
            this.listener = listener;
        }
    }

}
