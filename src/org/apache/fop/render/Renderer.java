/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.render;

// FOP
import org.apache.fop.svg.SVGArea;
import org.apache.fop.image.ImageArea;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layout.*;
import org.apache.fop.layout.inline.*;

// Avalon
import org.apache.avalon.framework.logger.Logger;

// Java
import java.io.OutputStream;
import java.io.IOException;

/**
 * interface implement by all renderers.
 * 
 * a Renderer implementation takes areas/spaces and produces output in
 * some format.
 */
public interface Renderer {

    /**
     * Set the logger
     */
    public void setLogger(Logger logger);

    /**
     * set up the given FontInfo
     */
    public void setupFontInfo(FontInfo fontInfo) throws FOPException;

    /**
     * set up renderer options
     */
    public void setOptions(java.util.HashMap options);

    /**
     * set the producer of the rendering
     */
    public void setProducer(String producer);

    /**
     * render the given area tree to the given stream
     */
    //public void render(AreaTree areaTree, OutputStream stream) throws IOException, FOPException;
    public void render(Page page, OutputStream stream)
    throws IOException, FOPException;

    /**
     * render the given area container
     */
    public void renderAreaContainer(AreaContainer area);

    /**
     * render the given area container
     */
    public void renderBodyAreaContainer(BodyAreaContainer area);

    /**
     * render the given span area
     */
    public void renderSpanArea(SpanArea area);

    /**
     * render the given block area
     */
    public void renderBlockArea(BlockArea area);

    /**
     * render the given display space
     */
    public void renderDisplaySpace(DisplaySpace space);

    /**
     * render the given SVG area
     */
    public void renderSVGArea(SVGArea area);

    /**
     * render a foreign object area
     */
    public void renderForeignObjectArea(ForeignObjectArea area);

    /**
     * render the given image area
     */
    public void renderImageArea(ImageArea area);

    /**
     * render the given inline area
     */
    public void renderWordArea(WordArea area);

    /**
     * render the given inline space
     */
    public void renderInlineSpace(InlineSpace space);

    /**
     * render the given line area
     */
    public void renderLineArea(LineArea area);

    /**
     * render the given page
     */
    public void renderPage(Page page);

    /**
     * render the given leader area
     */
    public void renderLeaderArea(LeaderArea area);

    public void startRenderer(OutputStream outputStream)
    throws IOException;

    public void stopRenderer(OutputStream outputStream)
    throws IOException;

}
