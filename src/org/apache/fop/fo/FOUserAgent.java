/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

// Java
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;

// XML
import org.w3c.dom.Document;

// Avalon
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

// FOP
import org.apache.fop.render.XMLHandler;
import org.apache.fop.render.RendererContext;

/**
 * The User Agent for fo.
 * This user agent is used by the processing to obtain user configurable
 * options.
 * <p>
 * Renderer specific extensions (that do not produce normal areas on
 * the output) will be done like so:
 * <br>
 * The extension will create an area, custom if necessary
 * <br>
 * this area will be added to the user agent with a key
 * <br>
 * the renderer will know keys for particular extensions
 * <br>
 * eg. bookmarks will be held in a special hierarchical area representing
 * the title and bookmark structure
 * <br>
 * These areas may contain resolveable areas that will be processed
 * with other resolveable areas
 */
public class FOUserAgent implements LogEnabled {
    
    private Logger log;
    private Map defaults = new java.util.HashMap();
    private Map handlers = new java.util.HashMap();
    private String baseURL;

    /**
     * Sets the logger.
     * @param log Logger to use
     * @see org.apache.avalon.framework.logger.LogEnabled#enableLogging(Logger)
     */
    public void enableLogging(Logger log) {
        this.log = log;
    }

    /**
     * Returns the logger to use.
     * @see org.apache.avalon.framework.logger.AbstractLogEnabled#getLogger()
     * @todo This breaks IoC/SoC. Should be improved.
     */
    public Logger getLogger() {
        return this.log;
    }

    /**
     * Sets the base URL.
     * @param baseURL base URL
     */
    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    /**
     * Returns the base URL.
     * @return the base URL
     */
    public String getBaseURL() {
        return this.baseURL;
    }

    /**
     * Get an input stream for a reference.
     * Temporary solution until API better.
     * @param uri URI to access
     * @return InputStream for accessing the resource.
     * @throws IOException in case of an I/O problem
     */
    public InputStream getStream(String uri) throws IOException {
        return null;
    }

    /**
     * Returns the conversion factor from pixel units to millimeters. This
     * depends on the desired reolution.
     * @return float conversion factor
     */
    public float getPixelUnitToMillimeter() {
        return 0.35277777777777777778f;
    }

    /**
     * If to create hot links to footnotes and before floats.
     * @return True if hot links dhould be created
     */
    public boolean linkToFootnotes() {
        return true;
    }

    /**
     * Set the default xml handler for the given mime type.
     * @param mime MIME type
     * @param handler XMLHandler to use
     */
    public void setDefaultXMLHandler(String mime, XMLHandler handler) {
        defaults.put(mime, handler);
    }

    /**
     * Add an xml handler for the given mime type and xml namespace.
     * @param mime MIME type
     * @param ns Namespace URI
     * @param handler XMLHandler to use
     */
    public void addXMLHandler(String mime, String ns, XMLHandler handler) {
        Map mh = (Map) handlers.get(mime);
        if (mh == null) {
            mh = new java.util.HashMap();
            handlers.put(mime, mh);
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
    public void renderXML(RendererContext ctx, Document doc,
                          String namespace) {
        String mime = ctx.getMimeType();
        Map mh = (Map) handlers.get(mime);
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
                getLogger().error("Some XML content will be ignored. "
                        + "Could not render XML", t);
            }
        } else {
            // no handler found for document
            getLogger().warn("Some XML content will be ignored. "
                    + "No handler defined for XML: " + namespace);
        }
    }
}

