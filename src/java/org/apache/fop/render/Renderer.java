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
import java.io.IOException;
// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.PageViewport;
import org.apache.fop.configuration.FOUserAgent;

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
public interface Renderer extends Runnable {

    void setOutputStream(OutputStream outputStream);

    /**
     * Gets the <code>FontData</code> object representing the database of
     * <code>Font</code>s available to this renderer.
     * @return the fonts database
     */
    FontData getFontData();

    /**
     * Set the User Agent.
     *
     * @param agent  The User Agent
     */
    void setUserAgent(FOUserAgent agent);

    void setOption(String key, Object value);

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
     * Tells the renderer to render a particular page. A renderer typically
     * reponds by packing up the current page and writing it immediately to the
     * output device.
     *
     * @param page              The page to be rendered
     * @exception IOException   if an I/O error occurs
     * @exception FOPException  if a FOP interal error occurs.
     */
    void renderPage(PageViewport page)
        throws IOException, FOPException;

}

