/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.render;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.*;
import org.apache.fop.area.inline.*;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.fo.FOUserAgent;

// Avalon
import org.apache.avalon.framework.logger.Logger;

// Java
import java.io.OutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Interface implemented by all renderers.
 * This interface is used to control the rendering of pages
 * and to let block and inline level areas call the appropriate
 * method to render themselves
 *
 * a Renderer implementation takes areas/spaces and produces output in
 * some format.
 */
public interface Renderer {

    public void startRenderer(OutputStream outputStream) throws IOException;

    public void stopRenderer() throws IOException;

    /**
     * Set the logger
     */
    public void setLogger(Logger logger);

    /**
     * Set the User Agent
     */
    public void setUserAgent(FOUserAgent agent);

    /**
     * set up the given FontInfo
     */
    public void setupFontInfo(FontInfo fontInfo);

    /**
     * set up renderer options
     */
    public void setOptions(HashMap options);

    /**
     * set the producer of the rendering
     */
    public void setProducer(String producer);

    public boolean supportsOutOfOrder();

    public void preparePage(PageViewport page);

    public void startPageSequence(Title seqTitle);

    public void renderPage(PageViewport page) throws IOException, FOPException;

    public void renderViewport(Viewport viewport);

    public void renderContainer(Container cont);

    public void renderWord(Word area);

    public void renderInlineParent(InlineParent ip);

    public void renderCharacter(
              org.apache.fop.area.inline.Character ch);

    public void renderInlineSpace(Space space);

    public void renderLeader(Leader area);
}

