/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
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
        if ((this.baseURL == null) || (this.baseURL.trim().equals(""))) {
            return "file:.";
        } else {
            return this.baseURL;
        }
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

