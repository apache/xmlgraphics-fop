/*
 * $Id: Renderer.java,v 1.31 2003/03/07 09:46:33 jeremias Exp $
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
package org.apache.fop.render;

// Java
import java.io.OutputStream;
import java.io.IOException;
import java.util.Map;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Title;
import org.apache.fop.area.TreeExt;
import org.apache.fop.area.inline.Container;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.area.inline.Word;
import org.apache.fop.fo.FOTreeControl;
import org.apache.fop.fo.FOUserAgent;

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
     * Set up the given FontInfo.
     *
     * @param fontInfo  The fonts
     */
    void setupFontInfo(FOTreeControl foTreeControl);

    /**
     * Set up renderer options.
     *
     * @param options  The Configuration for the renderer
     */
    void setOptions(Map options);

    /**
     * Set the producer of the rendering. If this method isn't called the
     * renderer uses a default. Note: Not all renderers support this feature.
     *
     * @param producer  The name of the producer (normally "FOP") to be
     *      embedded in the generated file.
     */
    void setProducer(String producer);

    /**
     * Set the creator of the document to be rendered. If this method
     * isn't called the renderer uses a default.
     * Note: Not all renderers support this feature.
     *
     * @param creator  The name of the document creator
     */
    void setCreator(String creator);

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
     * Tells the renderer to render an extension element.
     *
     * @param ext  The extension element to be rendered
     */
    void renderExtension(TreeExt ext);

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
     */
    void startPageSequence(Title seqTitle);

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

    /**
     * Tells the renderer to render an inline viewport. It sets up clipping as
     * necessary.
     *
     * @param viewport  The viewport area
     */
    void renderViewport(Viewport viewport);

    /**
     * Tells the renderer to render an inline container.
     *
     * @param cont  The inline container area
     */
    void renderContainer(Container cont);

    /**
     * Tells the renderer to render an inline word.
     *
     * @param area  The word area
     */
    void renderWord(Word area);

    /**
     * Tells the renderer to render an inline parent area.
     *
     * @param ip  The inline parent area
     */
    void renderInlineParent(InlineParent ip);

    /**
     * Tells the renderer to render an inline character.
     *
     * @param ch  The inline character
     */
    void renderCharacter(
            org.apache.fop.area.inline.Character ch);

    /**
     * Tells the renderer to render an inline space.
     *
     * @param space  The inline space
     */
    void renderInlineSpace(Space space);

    /**
     * Tells the renderer to render an inline leader area.
     *
     * @param area  The inline leader area.
     */
    void renderLeader(Leader area);

}

