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

package org.apache.fop.area;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.xml.sax.SAXException;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererEventProducer;

/**
 * This uses the AreaTreeModel to store the pages
 * Each page is either rendered if ready or prepared
 * for later rendering.
 * Once a page is rendered it is cleared to release the
 * contents but the PageViewport is retained. So even
 * though the pages are stored the contents are discarded.
 */
public class RenderPagesModel extends AreaTreeModel {
    /**
     * The renderer that will render the pages.
     */
    protected Renderer renderer;

    /**
     * Pages that have been prepared but not rendered yet.
     */
    protected List<PageViewport> prepared = new java.util.ArrayList<PageViewport>();

    private List<OffDocumentItem> pendingODI = new java.util.ArrayList<OffDocumentItem>();
    private List<OffDocumentItem> endDocODI = new java.util.ArrayList<OffDocumentItem>();

    /**
     * Create a new render pages model with the given renderer.
     * @param userAgent FOUserAgent object for process
     * @param outputFormat the MIME type of the output format to use (ex. "application/pdf").
     * @param fontInfo FontInfo object
     * @param stream OutputStream
     * @throws FOPException if the renderer cannot be properly initialized
     */
    public RenderPagesModel (FOUserAgent userAgent, String outputFormat,
        FontInfo fontInfo, OutputStream stream) throws FOPException {

        super();
        this.renderer = userAgent.getRendererFactory().createRenderer(
                userAgent, outputFormat);

        try {
            renderer.setupFontInfo(fontInfo);
            // check that the "any,normal,400" font exists
            if (!fontInfo.isSetupValid()) {
                throw new FOPException(
                    "No default font defined by OutputConverter");
            }
            renderer.startRenderer(stream);
        } catch (IOException e) {
            throw new FOPException(e);
        }
    }

    @Override
    public void setDocumentLocale(Locale locale) {
        renderer.setDocumentLocale(locale);
    }

    /** {@inheritDoc} */
    @Override
    public void startPageSequence(PageSequence pageSequence) {
        super.startPageSequence(pageSequence);
        if (renderer.supportsOutOfOrder()) {
            renderer.startPageSequence(getCurrentPageSequence());
        }
    }

    /**
     * Add a page to the render page model.
     * If the page is finished it can be rendered immediately.
     * If the page needs resolving then if the renderer supports
     * out of order rendering it can prepare the page. Otherwise
     * the page is added to a queue.
     * @param page the page to add to the model
     */
    @Override
    public void addPage(PageViewport page) {
        super.addPage(page);

        // for links the renderer needs to prepare the page
        // it is more appropriate to do this after queued pages but
        // it will mean that the renderer has not prepared a page that
        // could be referenced
        boolean ready = renderer.supportsOutOfOrder() && page.isResolved();
        if (ready) {
            if (!renderer.supportsOutOfOrder() && page.getPageSequence().isFirstPage(page)) {
                renderer.startPageSequence(getCurrentPageSequence());
            }
            try {
                renderer.renderPage(page);
            } catch (RuntimeException re) {
                String err = "Error while rendering page " + page.getPageNumberString();
                log.error(err, re);
                throw re;
            } catch (IOException ioe) {
                RendererEventProducer eventProducer = RendererEventProducer.Provider.get(
                        renderer.getUserAgent().getEventBroadcaster());
                eventProducer.ioError(this, ioe);
            } catch (FOPException e) {
                //TODO use error handler to handle this FOPException or propagate exception
                String err = "Error while rendering page " + page.getPageNumberString();
                log.error(err, e);
                throw new IllegalStateException("Fatal error occurred. Cannot continue. "
                        + e.getClass().getName() + ": " + err);
            }
            page.clear();
        } else {
            preparePage(page);
        }


        // check prepared pages
        boolean cont = checkPreparedPages(page, false);

        if (cont) {
            processOffDocumentItems(pendingODI);
            pendingODI.clear();
        }
    }

    /**
     * Check prepared pages
     *
     * @param newPageViewport the new page being added
     * @param renderUnresolved render pages with unresolved idref's
     *          (done at end-of-document processing)
     * @return true if the current page should be rendered
     *         false if the renderer doesn't support out of order
     *         rendering and there are pending pages
     */
    protected boolean checkPreparedPages(PageViewport newPageViewport,
                                         boolean renderUnresolved) {

        for (Iterator iter = prepared.iterator(); iter.hasNext();) {
            PageViewport pageViewport = (PageViewport)iter.next();
            if (pageViewport.isResolved() || renderUnresolved) {
                if (!renderer.supportsOutOfOrder()
                        && pageViewport.getPageSequence().isFirstPage(pageViewport)) {
                    renderer.startPageSequence(pageViewport.getPageSequence());
                }
                renderPage(pageViewport);
                pageViewport.clear();
                iter.remove();
            } else {
                // if keeping order then stop at first page not resolved
                if (!renderer.supportsOutOfOrder()) {
                    break;
                }
            }
        }
        return renderer.supportsOutOfOrder() || prepared.isEmpty();
    }

    /**
     * Renders the given page and notified about unresolved IDs if any.
     * @param pageViewport the page to be rendered.
     */
    protected void renderPage(PageViewport pageViewport) {
        try {
            renderer.renderPage(pageViewport);
            if (!pageViewport.isResolved()) {
                String[] idrefs = pageViewport.getIDRefs();
                for (String idref : idrefs) {
                    AreaEventProducer eventProducer = AreaEventProducer.Provider.get(
                            renderer.getUserAgent().getEventBroadcaster());
                    eventProducer.unresolvedIDReferenceOnPage(this,
                            pageViewport.getPageNumberString(), idref);
                }
            }
        } catch (Exception e) {
            AreaEventProducer eventProducer = AreaEventProducer.Provider.get(
                    renderer.getUserAgent().getEventBroadcaster());
            eventProducer.pageRenderingError(this,
                    pageViewport.getPageNumberString(), e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
        }
    }

    /**
     * Prepare a page.
     * An unresolved page can be prepared if the renderer supports
     * it and the page will be rendered later.
     * @param page the page to prepare
     */
    protected void preparePage(PageViewport page) {
        if (renderer.supportsOutOfOrder()) {
            renderer.preparePage(page);
        }
        prepared.add(page);
    }

    /** {@inheritDoc} */
    @Override
    public void handleOffDocumentItem(OffDocumentItem oDI) {
        switch(oDI.getWhenToProcess()) {
            case OffDocumentItem.IMMEDIATELY:
                renderer.processOffDocumentItem(oDI);
                break;
            case OffDocumentItem.AFTER_PAGE:
                pendingODI.add(oDI);
                break;
            case OffDocumentItem.END_OF_DOC:
                endDocODI.add(oDI);
                break;
            default:
                throw new RuntimeException();
        }
    }

    private void processOffDocumentItems(List<OffDocumentItem> list) {
        for (OffDocumentItem oDI : list) {
            renderer.processOffDocumentItem(oDI);
        }
    }

    /**
     * End the document. Render any end document OffDocumentItems
     * {@inheritDoc}
     */
    @Override
    public void endDocument() throws SAXException {
        // render any pages that had unresolved ids
        checkPreparedPages(null, true);

        processOffDocumentItems(pendingODI);
        pendingODI.clear();
        processOffDocumentItems(endDocODI);

        try {
            renderer.stopRenderer();
        } catch (IOException ex) {
            throw new SAXException(ex);
        }
    }
}

