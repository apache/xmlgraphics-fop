/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render;

// FOP
import org.apache.fop.image.ImageArea;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.*;
import org.apache.fop.area.Span;
import org.apache.fop.area.inline.*;
import org.apache.fop.area.inline.Space;
import org.apache.fop.fo.FOUserAgent;

import org.apache.log.Logger;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

/**
 * Abstract base class for all renderers.
 * The Abstract renderer does all the top level processing
 * of the area tree and adds some abstract methods to handle
 * viewports. This keeps track of the current block and inline
 * position.
 */
public abstract class AbstractRenderer implements Renderer {
    protected Logger log;
    protected FOUserAgent userAgent;
    protected HashMap options;

    // block progression position
    protected int currentBPPosition = 0;

    // inline progression position
    protected int currentIPPosition = 0;

    protected int currentBlockIPPosition = 0;

    public void setLogger(Logger logger) {
        log = logger;
    }

    public void setUserAgent(FOUserAgent agent) {
        userAgent = agent;
    }

    public void setOptions(HashMap opt) {
        options = opt;
    }

    public void startPageSequence(Title seqTitle) {
    }

    // normally this would be overriden to create a page in the
    // output
    public void renderPage(PageViewport page) throws IOException,
    FOPException {

        Page p = page.getPage();
        renderPageAreas(p);
    }

    protected void renderPageAreas(Page page) {
        RegionViewport viewport;
        viewport = page.getRegion(Region.BEFORE);
        renderRegionViewport(viewport);
        viewport = page.getRegion(Region.START);
        renderRegionViewport(viewport);
        viewport = page.getRegion(Region.BODY);
        renderRegionViewport(viewport);
        viewport = page.getRegion(Region.END);
        renderRegionViewport(viewport);
        viewport = page.getRegion(Region.AFTER);
        renderRegionViewport(viewport);

    }

    // the region may clip the area and it establishes
    // a position from where the region is placed
    protected void renderRegionViewport(RegionViewport port) {
        if (port != null) {
            Region region = port.getRegion();
            if (region.getRegionClass() == Region.BODY) {
                renderBodyRegion((BodyRegion) region);
            } else {
                renderRegion(region);
            }
        }
    }

    protected void renderRegion(Region region) {
        List blocks = region.getBlocks();

        renderBlocks(blocks);

    }

    protected void renderBodyRegion(BodyRegion region) {
        BeforeFloat bf = region.getBeforeFloat();
        if (bf != null) {
            renderBeforeFloat(bf);
        }
        MainReference mr = region.getMainReference();
        if (mr != null) {
            renderMainReference(mr);
        }
        Footnote foot = region.getFootnote();
        if (foot != null) {
            renderFootnote(foot);
        }
    }

    protected void renderBeforeFloat(BeforeFloat bf) {
        List blocks = bf.getBlocks();
        if (blocks != null) {
            renderBlocks(blocks);
            Block sep = bf.getSeparator();
            if (sep != null) {
                renderBlock(sep);
            }
        }
    }

    protected void renderFootnote(Footnote footnote) {
        List blocks = footnote.getBlocks();
        if (blocks != null) {
            Block sep = footnote.getSeparator();
            if (sep != null) {
                renderBlock(sep);
            }
            renderBlocks(blocks);
        }
    }

    // the main reference area contains a list of spans that are
    // stacked on the page
    // the spans contain a list of normal flow reference areas
    // that are positioned into columns.
    protected void renderMainReference(MainReference mr) {
        int saveIPPos = currentIPPosition;

        Span span = null;
        List spans = mr.getSpans();
        for (int count = 0; count < spans.size(); count++) {
            span = (Span) spans.get(count);
            int offset = (mr.getWidth() -
                          (span.getColumnCount() - 1) * mr.getColumnGap()) /
                         span.getColumnCount() + mr.getColumnGap();
            for (int c = 0; c < span.getColumnCount(); c++) {
                Flow flow = (Flow) span.getFlow(c);

                renderFlow(flow);
                currentIPPosition += offset;
            }
            currentIPPosition = saveIPPos;
            currentBPPosition += span.getHeight();
        }
    }

    // the normal flow reference area contains stacked blocks
    protected void renderFlow(Flow flow) {
        List blocks = flow.getBlocks();
        renderBlocks(blocks);

    }

    protected void renderBlock(Block block) {
        boolean childrenblocks = block.isChildrenBlocks();
        List children = block.getChildAreas();
        if (childrenblocks) {
            renderBlocks(children);
        } else {
            if (children == null) {
                // simply move position
            } else {
                // a line area is rendered from the top left position
                // of the line, each inline object is offset from there
                for (int count = 0; count < children.size(); count++) {
                    LineArea line = (LineArea) children.get(count);
                    renderLineArea(line);
                }

            }
        }
    }

    // a line area may have grouped styling for its children
    // such as underline, background
    protected void renderLineArea(LineArea line) {
        List children = line.getInlineAreas();

        for (int count = 0; count < children.size(); count++) {
            InlineArea inline = (InlineArea) children.get(count);
            inline.render(this);
        }

    }

    public void renderViewport(Viewport viewport) {
        Area content = viewport.getContent();
        if (content instanceof Image) {
            renderImage((Image) content);
        } else if (content instanceof Container) {
            renderContainer((Container) content);
        } else if (content instanceof ForeignObject) {
            renderForeignObject((ForeignObject) content);
        }
    }

    public void renderImage(Image image) {
    }

    public void renderContainer(Container cont) {
        List blocks = cont.getBlocks();
        renderBlocks(blocks);
    }

    public void renderForeignObject(ForeignObject fo) {

    }

    public void renderCharacter(org.apache.fop.area.inline.Character ch) {
        currentBlockIPPosition += ch.getWidth();
    }

    // an inline space moves the inline progression position
    // for the current block by the width or height of the space
    // it may also have styling (only on this object) that needs
    // handling
    public void renderInlineSpace(Space space) {
        currentBlockIPPosition += space.getWidth();
    }

    public void renderLeader(Leader area) {
        currentBlockIPPosition += area.getWidth();
    }

    public void renderWord(Word word) {
        currentBlockIPPosition += word.getWidth();
    }

    protected void renderBlocks(List blocks) {
        for (int count = 0; count < blocks.size(); count++) {
            Block block = (Block) blocks.get(count);
            renderBlock(block);
        }
    }
}
