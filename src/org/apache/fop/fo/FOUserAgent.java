/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import org.apache.fop.render.XMLHandler;
import org.apache.fop.render.RendererContext;

import org.apache.avalon.framework.logger.Logger;

import org.w3c.dom.*;

import java.util.HashMap;

/**
 * The User Agent for fo.
 * This user agent is used by the processing to obtain user configurable
 * options.
 *
 * Renderer specific extensions (that do not produce normal areas on
 * the output) will be done like so:
 * The extension will create an area, custom if necessary
 * this area will be added to the user agent with a key
 * the renderer will know keys for particular extensions
 * eg. bookmarks will be held in a special hierarchical area representing
 * the title and bookmark structure
 * These areas may contain resolveable areas that will be processed
 * with other resolveable areas
 */
public class FOUserAgent {
    HashMap defaults = new HashMap();
    HashMap handlers = new HashMap();
    Logger log;
    String base;

    public void setLogger(Logger logger) {
        log = logger;
    }

    public Logger getLogger() {
        return log;
    }

    public void setBaseURL(String b) {
        base = b;
    }

    public String getBaseURL() {
        return base;
    }

    public float getPixelToMM() {
        return 0.35277777777777777778f;
    }

    /**
     * If to create hot links to footnotes and before floats.
     */
    public boolean linkToFootnotes() {
        return true;
    }

    /**
     * Set the default xml handler for the given mime type.
     */
    public void setDefaultXMLHandler(String mime, XMLHandler handler) {
        defaults.put(mime, handler);
    }

    /**
     * Add an xml handler for the given mime type and xml namespace.
     */
    public void addXMLHandler(String mime, String ns, XMLHandler handler) {
        HashMap mh = (HashMap) handlers.get(mime);
        if (mh == null) {
            mh = new HashMap();
            handlers.put(mime, mh);
        }
        mh.put(ns, handler);
    }

    /** 
     * Render the xml document with the given xml namespace.
     * The Render Context is by the handle to render into the current
     * rendering target.
     */
    public void renderXML(RendererContext ctx, Document doc,
                          String namespace) {
        String mime = ctx.getMimeType();
        HashMap mh = (HashMap) handlers.get(mime);
        XMLHandler handler = null;
        if (mh != null) {
            handler = (XMLHandler) mh.get(namespace);
        }
        if (handler == null) {
            handler = (XMLHandler) defaults.get(mime);
        }
        if (handler != null) {
            try {
                handler.handleXML(ctx, doc, namespace);
            } catch (Throwable t) {
                // could not handle document
                log.error("Could not render XML", t);
            }
        } else {
            // no handler found for document
            log.debug("No handler defined for XML: " + namespace);
        }
    }
}

