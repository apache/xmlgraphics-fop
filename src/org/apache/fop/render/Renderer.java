/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.render;

// Java
import java.io.OutputStream;
import java.io.IOException;
import java.util.HashMap;

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
import org.apache.fop.layout.FontInfo;
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
    void setupFontInfo(FontInfo fontInfo);

    /**
     * Set up renderer options.
     *
     * @param options  The Configuration for the renderer
     */
    void setOptions(HashMap options);

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

