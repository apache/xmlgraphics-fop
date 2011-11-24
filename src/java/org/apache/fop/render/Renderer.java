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

package org.apache.fop.render;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.OffDocumentItem;
import org.apache.fop.area.PageSequence;
import org.apache.fop.area.PageViewport;
import org.apache.fop.fonts.FontInfo;


/**
 * Interface implemented by all renderers. This interface is used to control
 * the rendering of pages and to let block and inline level areas call the
 * appropriate method to render themselves. <p>
 *
 * A Renderer implementation takes areas/spaces and produces output in some
 * format.</p> <p>
 *
 * Typically, most renderers are subclassed from FOP's abstract implementations
 * ({@link AbstractRenderer}, {@link PrintRenderer}) which already handle a lot
 * of things letting you concentrate on the details of the output format.
 */
public interface Renderer {

    /**
     * Role constant for Avalon.
     */
    String ROLE = Renderer.class.getName();


    /**
     * Get the MIME type of the renderer.
     *
     * @return The MIME type of the renderer, may return null if not applicable.
     */
    String getMimeType();

        /**
     * Initiates the rendering phase.
     * This must only be called once for a rendering. If
     * stopRenderer is called then this may be called again
     * for a new document rendering.
     *
     * @param outputStream     The OutputStream to use for output
     * @exception IOException  If an I/O error occurs
     */
    void startRenderer(OutputStream outputStream)
        throws IOException;

    /**
     * Signals the end of the rendering phase.
     * The renderer should reset to an initial state and dispose of
     * any resources for the completed rendering.
     *
     * @exception IOException  If an I/O error occurs
     */
    void stopRenderer()
        throws IOException;

    /**
     * Set the User Agent.
     *
     * @param agent  The User Agent
     */
    void setUserAgent(FOUserAgent agent);

    /**
     * Returns the associated user agent.
     * @return the user agent
     */
    FOUserAgent getUserAgent();

    /**
     * Set up the given FontInfo.
     *
     * @param fontInfo  The font information
     * @throws FOPException if an error occurs while setting up the font info object
     */
    void setupFontInfo(FontInfo fontInfo) throws FOPException;

    /**
     * Reports if out of order rendering is supported. <p>
     *
     * Normally, all pages of a document are rendered in their natural order
     * (page 1, page 2, page 3 etc.). Some output formats (such as PDF) allow
     * pages to be output in random order. This is helpful to reduce resource
     * strain on the system because a page that cannot be fully resolved
     * doesn't block subsequent pages that are already fully resolved. </p>
     *
     * @return   True if this renderer supports out of order rendering.
     */
    boolean supportsOutOfOrder();

    /**
     *
     * @param locale Locale of the language
     */
    void setDocumentLocale(Locale locale);

    /**
     * Tells the renderer to process an item not explicitly placed on the
     * document (e.g., PDF bookmarks).  Note - not all renderers will process
     * all off-document items.
     *
     * @param odi  The off-document item to be rendered
     */
    void processOffDocumentItem(OffDocumentItem odi);

    /**
     * @return the adapter for painting Java2D images (or null if not supported)
     */
    Graphics2DAdapter getGraphics2DAdapter();

    /**
     * @return the adapter for painting RenderedImages (or null if not supported)
     */
    ImageAdapter getImageAdapter();

    /**
     * This is called if the renderer supports out of order rendering. The
     * renderer should prepare the page so that a page further on in the set of
     * pages can be rendered. The body of the page should not be rendered. The
     * page will be rendered at a later time by the call to {@link
     * #renderPage(PageViewport)}.
     *
     * @param page  The page viewport to use
     */
    void preparePage(PageViewport page);

    /**
     * Tells the renderer that a new page sequence starts.
     *
     * @param seqTitle  The title of the page sequence
     * @deprecated Use {@link #startPageSequence(PageSequence)} instead
     */
    void startPageSequence(LineArea seqTitle);

    /**
     * Tells the renderer that a new page sequence starts.
     *
     * @param pageSequence the page sequence
     */
    void startPageSequence(PageSequence pageSequence);

    /**
     * Tells the renderer to render a particular page. A renderer typically
     * responds by packing up the current page and writing it immediately to the
     * output device.
     *
     * @param page              The page to be rendered
     * @exception IOException   if an I/O error occurs
     * @exception FOPException  if a FOP interal error occurs.
     */
    void renderPage(PageViewport page)
        throws IOException, FOPException;

}

