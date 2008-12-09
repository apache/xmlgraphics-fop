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

package org.apache.fop.render.pdf;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;

import org.apache.fop.pdf.PDFAction;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFactory;
import org.apache.fop.pdf.PDFLink;
import org.apache.fop.pdf.PDFOutline;
import org.apache.fop.pdf.PDFReference;
import org.apache.fop.render.intermediate.IFDocumentNavigationHandler;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.extensions.AbstractAction;
import org.apache.fop.render.intermediate.extensions.ActionSet;
import org.apache.fop.render.intermediate.extensions.Bookmark;
import org.apache.fop.render.intermediate.extensions.BookmarkTree;
import org.apache.fop.render.intermediate.extensions.GoToXYAction;
import org.apache.fop.render.intermediate.extensions.Link;
import org.apache.fop.render.intermediate.extensions.NamedDestination;
import org.apache.fop.render.intermediate.extensions.URIAction;
import org.apache.fop.render.pdf.PDFDocumentHandler.PageReference;

/**
 * Implementation of the {@link IFDocumentNavigationHandler} interface for PDF output.
 */
public class PDFDocumentNavigationHandler implements IFDocumentNavigationHandler {

    private PDFDocumentHandler documentHandler;

    private ActionSet actionSet = new ActionSet();
    private List deferredLinks = new java.util.ArrayList();

    /**
     * Default constructor.
     * @param documentHandler the parent document handler
     */
    public PDFDocumentNavigationHandler(PDFDocumentHandler documentHandler) {
        super();
        this.documentHandler = documentHandler;
    }

    PDFDocument getPDFDoc() {
        return this.documentHandler.pdfDoc;
    }

    /** {@inheritDoc} */
    public void renderNamedDestination(NamedDestination destination) throws IFException {
        PDFReference actionRef = getAction(destination.getAction());
        getPDFDoc().getFactory().makeDestination(
                destination.getName(), actionRef);
    }

    /** {@inheritDoc} */
    public void renderBookmarkTree(BookmarkTree tree) throws IFException {
        Iterator iter = tree.getBookmarks().iterator();
        while (iter.hasNext()) {
            Bookmark b = (Bookmark)iter.next();
            renderBookmark(b, null);
        }
    }

    private void renderBookmark(Bookmark bookmark, PDFOutline parent) {
        if (parent == null) {
            parent = getPDFDoc().getOutlineRoot();
        }
        PDFReference actionRef = getAction(bookmark.getAction());
        PDFOutline pdfOutline = getPDFDoc().getFactory().makeOutline(parent,
            bookmark.getTitle(), actionRef.toString(), bookmark.isShown());
        Iterator iter = bookmark.getChildBookmarks().iterator();
        while (iter.hasNext()) {
            Bookmark b = (Bookmark)iter.next();
            renderBookmark(b, pdfOutline);
        }
    }

    /** {@inheritDoc} */
    public void renderLink(Link link) throws IFException {
        this.deferredLinks.add(link);
    }

    /**
     * Commits all pending elements to the PDF document.
     */
    public void commit() {
        Iterator iter = this.deferredLinks.iterator();
        while (iter.hasNext()) {
            Link link = (Link)iter.next();
            Rectangle targetRect = link.getTargetRect();
            int pageHeight = documentHandler.currentPageRef.getPageDimension().height;
            Rectangle2D targetRect2D = new Rectangle2D.Double(
                    targetRect.getMinX() / 1000.0,
                    (pageHeight - targetRect.getMinY()) / 1000.0,
                    targetRect.getWidth() / 1000.0,
                    targetRect.getHeight() / 1000.0);
            PDFReference actionRef = getAction(link.getAction());
            //makeLink() currently needs a PDFAction and not a reference
            //TODO Revisit when PDFLink is converted to a PDFDictionary
            PDFAction pdfAction = (PDFAction)actionRef.getObject();
            PDFLink pdfLink = getPDFDoc().getFactory().makeLink(
                    targetRect2D, pdfAction);
            documentHandler.currentPage.addAnnotation(pdfLink);
        }
    }

    /** {@inheritDoc} */
    public void addResolvedAction(AbstractAction action) throws IFException {
        actionSet.put(action);
    }

    private PDFReference getAction(AbstractAction action) {
        if (action.isReference()) {
            String id = action.getID();
            action = actionSet.get(id);
            if (action == null) {
                throw new IllegalStateException("Action could not be resolved: " + id);
            }
        }
        PDFFactory factory = getPDFDoc().getFactory();
        if (action instanceof GoToXYAction) {
            GoToXYAction a = (GoToXYAction)action;
            PageReference pageRef = this.documentHandler.getPageReference(a.getPageIndex());
            //Convert target location from millipoints to points and adjust for different
            //page origin
            Point2D p2d = new Point2D.Double(
                    a.getTargetLocation().x / 1000.0,
                    (pageRef.getPageDimension().height - a.getTargetLocation().y) / 1000.0);
            return factory.getPDFGoTo(pageRef.getPageRef().toString(), p2d).makeReference();
        } else if (action instanceof URIAction) {
            URIAction u = (URIAction)action;
            PDFAction pdfAction = factory.getExternalAction(u.getURI(), u.isNewWindow());
            getPDFDoc().registerObject(pdfAction);
            return pdfAction.makeReference();
        } else {
            throw new UnsupportedOperationException("Unsupported action type: "
                    + action + " (" + action.getClass().getName() + ")");
        }
    }

}
