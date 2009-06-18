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

/* $Id$ */

package org.apache.fop.render;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
// XML
import org.w3c.dom.Document;

// FOP
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.apps.Fop;
import org.apache.fop.configuration.FOUserAgent;

/**
 * Abstract base class for all renderers. The Abstract renderer does all the
 * top level processing of the area tree and adds some abstract methods to
 * handle viewports. This keeps track of the current block and inline position.
 */
public abstract class AbstractRenderer
         implements Renderer {

    protected OutputStream output;
    /** user agent */
    protected FOUserAgent userAgent;

    /** renderer configuration */
    protected Map options = new HashMap();


    protected Logger log = Logger.getLogger(Fop.fopPackage);

    /**
     * Implements Runnable.run() so that this thread can be started.
     * Set up the fonts and perform other initialization.
     * Respond to requests from layout thread for information
     * Wait for requests from layout thread for layout.
     */
    public void run() {
    }

    public synchronized void setOutputStream(OutputStream output) {
        this.output = output;
    }

    /**  @see org.apache.fop.render.Renderer */
    public synchronized void setUserAgent(FOUserAgent agent) {
        userAgent = agent;
    }

    public synchronized void setOption(String key, Object value) {
        options.put(key, value);
    }

    /**
     * Check if this renderer supports out of order rendering. If this renderer
     * supports out of order rendering then it means that the pages that are
     * not ready will be prepared and a future page will be rendered.
     *
     * @return   True if the renderer supports out of order rendering
     * @see      org.apache.fop.render.Renderer
     */
    public boolean supportsOutOfOrder() {
        return false;
    }

    /**
     * Prepare a page for rendering. This is called if the renderer supports
     * out of order rendering. The renderer should prepare the page so that a
     * page further on in the set of pages can be rendered. The body of the
     * page should not be rendered. The page will be rendered at a later time
     * by the call to render page.
     *
     * @see org.apache.fop.render.Renderer
     */
    public void preparePage(PageViewport page) { }


    /**
     * Handle the traits for a region
     * This is used to draw the traits for the given page region.
     * (See Sect. 6.4.1.2 of XSL-FO spec.)
     * @param rv the RegionViewport whose region is to be drawn
     */
    protected void handleRegionTraits(RegionViewport rv) {
        // draw border and background
    }

    /**
     * (todo) Description of the Method
     */
    protected void endVParea() { }


    /**
     * Add an xml handler for the given mime type and xml namespace.
     * @param mime MIME type
     * @param ns Namespace URI
     * @param handler XMLHandler to use
     */
    public void addXMLHandler(FOUserAgent foua, String mime, String ns,
                              XMLHandler handler) {
        Map mh = (Map) foua.handlers.get(mime);
        if (mh == null) {
            mh = new java.util.HashMap();
            foua.handlers.put(mime, mh);
        }
        mh.put(ns, handler);
    }

    /**
     * Render the xml document with the given xml namespace.
     * The Render Context is by the handle to render into the current
     * rendering target.
     * @param ctx rendering context
     * @param doc DOM Document containing the source document
     * @param namespace Namespace URI of the document
     */
    public void renderXML(FOUserAgent foua, RendererContext ctx, Document doc,
                          String namespace) {
        String mime = ctx.getMimeType();
        Map mh = (Map) foua.handlers.get(mime);
        XMLHandler handler = null;
        if (mh != null) {
            handler = (XMLHandler) mh.get(namespace);
        }
        if (handler == null) {
            handler = (XMLHandler) foua.defaults.get(mime);
        }
        if (handler != null) {
            try {
                handler.handleXML(ctx, doc, namespace);
            } catch (Throwable t) {
                // could not handle document
                log.severe("Some XML content will be ignored. "
                        + "Could not render XML\n" + t.getMessage());
            }
        } else {
            // no handler found for document
            log.warning("Some XML content will be ignored. "
                    + "No handler defined for XML: " + namespace);
        }
    }
}

