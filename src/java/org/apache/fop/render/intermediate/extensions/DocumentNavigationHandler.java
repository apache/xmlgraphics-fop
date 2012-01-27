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
import java.awt.Rectangle;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.fo.extensions.InternalElementMapping;
import org.apache.fop.render.intermediate.IFDocumentNavigationHandler;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.util.XMLUtil;

/**
 * ContentHandler that handles the IF document navigation namespace.
 */
public class DocumentNavigationHandler extends DefaultHandler
        implements DocumentNavigationExtensionConstants {

    /** Logger instance */
    protected static final Log log = LogFactory.getLog(DocumentNavigationHandler.class);

    private StringBuffer content = new StringBuffer();
    private Stack objectStack = new Stack();

    private IFDocumentNavigationHandler navHandler;

    private StructureTreeElement structureTreeElement;

    private Map<String, StructureTreeElement> structureTreeElements;

    /**
     * Main constructor.
     * @param navHandler the navigation handler that will receive the events
     * @param structureTreeElements the elements representing the structure of the document
     */
    public DocumentNavigationHandler(IFDocumentNavigationHandler navHandler,
            Map<String, StructureTreeElement> structureTreeElements) {
        this.navHandler = navHandler;
        assert structureTreeElements != null;
        this.structureTreeElements = structureTreeElements;
    }

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
            } else if (LINK.getLocalName().equals(localName)) {
                if (!objectStack.isEmpty()) {
                    throw new SAXException(localName + " must be the root element!");
                }
                Rectangle targetRect = XMLUtil.getAttributeAsRectangle(attributes, "rect");
                structureTreeElement = structureTreeElements.get(
                        attributes.getValue(InternalElementMapping.URI, InternalElementMapping.STRUCT_REF));
                Link link = new Link(null, targetRect);
                objectStack.push(link);
            } else if (GOTO_XY.getLocalName().equals(localName)) {
                String idref = attributes.getValue("idref");
                GoToXYAction action;
                if (idref != null) {
                    action = new GoToXYAction(idref);
                } else {
                    String id = attributes.getValue("id");
                    int pageIndex = XMLUtil.getAttributeAsInt(attributes, "page-index");
                    final Point location;
                    if (pageIndex < 0) {
                        location = null;
                    } else {
                        final int x = XMLUtil
                                .getAttributeAsInt(attributes, "x");
                        final int y = XMLUtil
                                .getAttributeAsInt(attributes, "y");
                        location = new Point(x, y);
                    }
                    action = new GoToXYAction(id, pageIndex, location);
                }
                if (structureTreeElement != null) {
                    action.setStructureTreeElement(structureTreeElement);
                }
                objectStack.push(action);
            } else if (GOTO_URI.getLocalName().equals(localName)) {
                String id = attributes.getValue("id");
                String gotoURI = attributes.getValue("uri");
                String showDestination = attributes.getValue("show-destination");
                boolean newWindow = "new".equals(showDestination);
                URIAction action = new URIAction(gotoURI, newWindow);
                if (id != null) {
                    action.setID(id);
                }
                if (structureTreeElement != null) {
                    action.setStructureTreeElement(structureTreeElement);
                }
                objectStack.push(action);
            } else {
                throw new SAXException(
                        "Invalid element '" + localName + "' in namespace: " + uri);
            }
            handled = true;
        }
        if (!handled) {
            if (NAMESPACE.equals(uri)) {
                throw new SAXException("Unhandled element '" + localName + "' in namespace: "
                        + uri);
            } else {
                log.warn("Unhandled element '" + localName + "' in namespace: " + uri);
            }
        }
    }

    /** {@inheritDoc} */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (NAMESPACE.equals(uri)) {
            try {
                if (BOOKMARK_TREE.getLocalName().equals(localName)) {
                    BookmarkTree tree = (BookmarkTree)objectStack.pop();
                    if (hasNavigation()) {
                        this.navHandler.renderBookmarkTree(tree);
                    }
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
                    NamedDestination dest = (NamedDestination)objectStack.pop();
                    dest.setAction(action);
                    if (hasNavigation()) {
                        this.navHandler.renderNamedDestination(dest);
                    }
                } else if (LINK.getLocalName().equals(localName)) {
                    AbstractAction action = (AbstractAction)objectStack.pop();
                    Link link = (Link)objectStack.pop();
                    link.setAction(action);
                    if (hasNavigation()) {
                        this.navHandler.renderLink(link);
                    }
                } else if (localName.startsWith("goto-")) {
                    if (objectStack.size() == 1) {
                        //Stand-alone action
                        AbstractAction action = (AbstractAction)objectStack.pop();
                        if (hasNavigation()) {
                            this.navHandler.addResolvedAction(action);
                        }
                    }
                }
            } catch (IFException ife) {
                throw new SAXException(ife);
            }
        }
        content.setLength(0); // Reset text buffer (see characters())
    }

    private boolean hasNavigation() {
        return this.navHandler != null;
    }

    /** {@inheritDoc} */
    public void characters(char[] ch, int start, int length) throws SAXException {
        content.append(ch, start, length);
    }

    /** {@inheritDoc} */
    public void endDocument() throws SAXException {
        assert objectStack.isEmpty();
    }

}
