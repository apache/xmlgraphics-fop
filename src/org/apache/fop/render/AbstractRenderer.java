/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.render;

// Java
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FOUserAgent;
import org.apache.fop.area.Area;
import org.apache.fop.area.BeforeFloat;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.area.CTM;
import org.apache.fop.area.Flow;
import org.apache.fop.area.Footnote;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.MainReference;
import org.apache.fop.area.Span;
import org.apache.fop.area.Page;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.RegionReference;
import org.apache.fop.area.Title;
import org.apache.fop.area.TreeExt;
import org.apache.fop.area.inline.Container;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.area.inline.Word;
import org.apache.fop.area.inline.Character;

// Avalon
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * Abstract base class for all renderers. The Abstract renderer does all the
 * top level processing of the area tree and adds some abstract methods to
 * handle viewports. This keeps track of the current block and inline position.
 */
public abstract class AbstractRenderer extends AbstractLogEnabled
         implements Renderer {

    /**
     * user agent
     */
    protected FOUserAgent userAgent;

    /**
     * renderer configuration
     */
    protected HashMap options;

    /**
     * block progression position
     */
    protected int currentBPPosition = 0;

    /**
     * inline progression position
     */
    protected int currentIPPosition = 0;

    /**
     * current inline progression position in block
     */
    protected int currentBlockIPPosition = 0;

    /**
     * the block progression position of the containing block used for
     * absolutely positioned blocks
     */
    protected int containingBPPosition = 0;

    /**
     * the inline progression position of the containing block used for
     * absolutely positioned blocks
     */
    protected int containingIPPosition = 0;


    /** @see org.apache.fop.render.Renderer */
    public void setUserAgent(FOUserAgent agent) {
        userAgent = agent;
    }

    /** @see org.apache.fop.render.Renderer */
    public void setOptions(HashMap opt) {
        options = opt;
    }

    /**
     * Check if this renderer supports out of order rendering. If this renderer
     * supports out of order rendering then it means that the pages that are
     * not ready will be prepared and a future page will be rendered.
     *
     * @return   True if the renderer supports out of order rendering
     * @see      org.apache.fop.render.Renderer
     */
    public boolean supportsOutOfOrder() {
        return false;
    }

    /**
     * @param ext  @todo Description of the Parameter
     * @see        org.apache.fop.render.Renderer
     */
    public void renderExtension(TreeExt ext) { }

    /**
     * Prepare a page for rendering. This is called if the renderer supports
     * out of order rendering. The renderer should prepare the page so that a
     * page further on in the set of pages can be rendered. The body of the
     * page should not be rendered. The page will be rendered at a later time
     * by the call to render page.
     *
     * @see org.apache.fop.render.Renderer
     */
    public void preparePage(PageViewport page) { }

    /**
     * Utility method to convert a page sequence title to a string. Some
     * renderers may only be able to use a string title. A title is a sequence
     * of inline areas that this method attempts to convert to an equivalent
     * string.
     *
     * @param title  The Title to convert
     * @return       An expanded string representing the title
     */
    protected String convertTitleToString(Title title) {
        List children = title.getInlineAreas();
        String str = convertToString(children);
        return str.trim();
    }

    private String convertToString(List children) {
        StringBuffer sb = new StringBuffer();
        for (int count = 0; count < children.size(); count++) {
            InlineArea inline = (InlineArea) children.get(count);
            if (inline instanceof Character) {
                sb.append(((Character) inline).getChar());
            } else if (inline instanceof Word) {
                sb.append(((Word) inline).getWord());
            } else if (inline instanceof InlineParent) {
                sb.append(convertToString(
                        ((InlineParent) inline).getChildAreas()));
            } else {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    /** @see org.apache.fop.render.Renderer */
    public void startPageSequence(Title seqTitle) {
        //do nothing
    }

    // normally this would be overriden to create a page in the
    // output
    /** @see org.apache.fop.render.Renderer */
    public void renderPage(PageViewport page)
        throws IOException, FOPException {

        Page p = page.getPage();
        renderPageAreas(p);
    }

    /**
     * Renders page areas.
     *
     * @param page  The page whos page areas are to be rendered
     */
    protected void renderPageAreas(Page page) {
        RegionViewport viewport;
        viewport = page.getRegion(RegionReference.BEFORE);
        renderRegionViewport(viewport);
        viewport = page.getRegion(RegionReference.START);
        renderRegionViewport(viewport);
        viewport = page.getRegion(RegionReference.BODY);
        renderRegionViewport(viewport);
        viewport = page.getRegion(RegionReference.END);
        renderRegionViewport(viewport);
        viewport = page.getRegion(RegionReference.AFTER);
        renderRegionViewport(viewport);
    }

    /**
     * Renders a region viewport. <p>
     *
     * The region may clip the area and it establishes a position from where
     * the region is placed.</p>
     *
     * @param port  The region viewport to be rendered
     */
    protected void renderRegionViewport(RegionViewport port) {
        if (port != null) {
            Rectangle2D view = port.getViewArea();
            // The CTM will transform coordinates relative to
            // this region-reference area into page coords, so
            // set origin for the region to 0,0.
            currentBPPosition = 0;// (int) (view.getY() / 1000);
            currentIPPosition = 0;// (int) (view.getX() / 1000);
            currentBlockIPPosition = currentIPPosition;

            RegionReference region = port.getRegion();
            startVParea(region.getCTM());
            if (region.getRegionClass() == RegionReference.BODY) {
                renderBodyRegion((BodyRegion) region);
            } else {
                renderRegion(region);
            }
            endVParea();
        }
    }

    /**
     * @todo Description of the Method
     *
     * @param ctm  The coordinate transformation matrix to use
     */
    protected void startVParea(CTM ctm) { }

    /**
     * @todo Description of the Method
     */
    protected void endVParea() { }

    /**
     * Renders a region reference area.
     *
     * @param region  The region reference area
     */
    protected void renderRegion(RegionReference region) {
        List blocks = region.getBlocks();

        renderBlocks(blocks);

    }

    /**
     * Renders a body region area.
     *
     * @param region  The body region
     */
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

    /**
     * Renders a before float area.
     *
     * @param bf  The before float area
     */
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

    /**
     * Renders a footnote
     *
     * @param footnote  The footnote
     */
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

    /**
     * Renders the main reference area.
     * <p>
     * The main reference area contains a list of spans that are
     * stacked on the page.
     * The spans contain a list of normal flow reference areas
     * that are positioned into columns.
     * </p>
     *
     * @param mr  The main reference area
     */
    protected void renderMainReference(MainReference mr) {
        int saveIPPos = currentIPPosition;

        Span span = null;
        List spans = mr.getSpans();
        for (int count = 0; count < spans.size(); count++) {
            span = (Span) spans.get(count);
            int offset = (mr.getWidth()
                    - (span.getColumnCount() - 1) * mr.getColumnGap())
                    / span.getColumnCount() + mr.getColumnGap();
            for (int c = 0; c < span.getColumnCount(); c++) {
                Flow flow = (Flow) span.getFlow(c);

                renderFlow(flow);
                currentIPPosition += offset;
            }
            currentIPPosition = saveIPPos;
            currentBPPosition += span.getHeight();
        }
    }

    /**
     * Renders a flow reference area.
     *
     * @param flow  The flow reference area
     */
    protected void renderFlow(Flow flow) {
        // the normal flow reference area contains stacked blocks
        List blocks = flow.getBlocks();
        renderBlocks(blocks);

    }

    /**
     * Renders a block area.
     *
     * @param block  The block area
     */
    protected void renderBlock(Block block) {
        List children = block.getChildAreas();
        if (children == null) {
            // simply move position
            currentBPPosition += block.getHeight();
        } else if (block instanceof BlockViewport) {
            renderBlockViewport((BlockViewport) block, children);
        } else {
            // save position and offset
            int saveIP = currentIPPosition;
            int saveBP = currentBPPosition;

            if (block.getPositioning() == Block.ABSOLUTE) {
                currentIPPosition += block.getXOffset();
                currentBPPosition += block.getYOffset();

                renderBlocks(children);

                // absolute blocks do not effect the layout
                currentBPPosition = saveBP;
            } else {
                // relative blocks are offset
                currentIPPosition += block.getXOffset();
                currentBPPosition += block.getYOffset();

                renderBlocks(children);

                // stacked and relative blocks effect stacking
                currentBPPosition = saveBP + block.getHeight();
            }
            currentIPPosition = saveIP;
        }
    }

    /**
     * Renders a block viewport.
     *
     * @param bv        The block viewport
     * @param children  The children to render within the block viewport
     */
    protected void renderBlockViewport(BlockViewport bv, List children) {
        // clip and position viewport if necessary
        if (bv.getPositioning() == Block.ABSOLUTE) {
            // save positions
            int saveIP = currentIPPosition;
            int saveBP = currentBPPosition;

            CTM ctm = bv.getCTM();
            currentIPPosition = 0;
            currentBPPosition = 0;

            startVParea(ctm);
            renderBlocks(children);
            endVParea();

            // clip if necessary

            currentIPPosition = saveIP;
            currentBPPosition = saveBP;
        } else {
            renderBlocks(children);
        }
    }

    /**
     * Renders a line area. <p>
     *
     * A line area may have grouped styling for its children such as underline,
     * background.</p>
     *
     * @param line  The line area
     */
    protected void renderLineArea(LineArea line) {
        List children = line.getInlineAreas();

        for (int count = 0; count < children.size(); count++) {
            InlineArea inline = (InlineArea) children.get(count);
            inline.render(this);
        }
    }

    /** @see org.apache.fop.render.Renderer */
    public void renderViewport(Viewport viewport) {
        Area content = viewport.getContent();
        int saveBP = currentBPPosition;
        currentBPPosition += viewport.getOffset();
        Rectangle2D contpos = viewport.getContentPosition();
        if (content instanceof Image) {
            renderImage((Image) content, contpos);
        } else if (content instanceof Container) {
            renderContainer((Container) content);
        } else if (content instanceof ForeignObject) {
            renderForeignObject((ForeignObject) content, contpos);
        }
        currentBlockIPPosition += viewport.getWidth();
        currentBPPosition = saveBP;
    }

    /**
     * Renders an image area.
     *
     * @param image  The image
     * @param pos    The target position of the image
     * @todo Make renderImage() protected
     */
    public void renderImage(Image image, Rectangle2D pos) {
        // Default: do nothing.
        // Some renderers (ex. Text) don't support images.
    }

    /** @see org.apache.fop.render.Renderer */
    public void renderContainer(Container cont) {
        int saveIP = currentIPPosition;
        currentIPPosition = currentBlockIPPosition;
        int saveBlockIP = currentBlockIPPosition;
        int saveBP = currentBPPosition;

        List blocks = cont.getBlocks();
        renderBlocks(blocks);
        currentIPPosition = saveIP;
        currentBlockIPPosition = saveBlockIP;
        currentBPPosition = saveBP;
    }

    /**
     * Renders a foreign object area.
     *
     * @param fo   The foreign object area
     * @param pos  The target position of the foreign object
     * @todo Make renderForeignObject() protected
     */
    public void renderForeignObject(ForeignObject fo, Rectangle2D pos) {
        // Default: do nothing.
        // Some renderers (ex. Text) don't support foreign objects.
    }

    /** @see org.apache.fop.render.Renderer */
    public void renderCharacter(Character ch) {
        currentBlockIPPosition += ch.getWidth();
    }

    /** @see org.apache.fop.render.Renderer */
    public void renderInlineSpace(Space space) {
        // an inline space moves the inline progression position
        // for the current block by the width or height of the space
        // it may also have styling (only on this object) that needs
        // handling
        currentBlockIPPosition += space.getWidth();
    }

    /** @see org.apache.fop.render.Renderer */
    public void renderLeader(Leader area) {
        currentBlockIPPosition += area.getWidth();
    }

    /** @see org.apache.fop.render.Renderer */
    public void renderWord(Word word) {
        currentBlockIPPosition += word.getWidth();
    }

    /** @see org.apache.fop.render.Renderer */
    public void renderInlineParent(InlineParent ip) {
        int saveIP = currentBlockIPPosition;
        Iterator iter = ip.getChildAreas().iterator();
        while (iter.hasNext()) {
            ((InlineArea) iter.next()).render(this);
        }
        currentBlockIPPosition = saveIP + ip.getWidth();
    }

    /**
     * Renders a list of block areas.
     *
     * @param blocks  The block areas
     */
    protected void renderBlocks(List blocks) {
        // the position of the containing block is used for
        // absolutely positioned areas
        int contBP = currentBPPosition;
        int contIP = currentIPPosition;
        containingBPPosition = contBP;
        containingIPPosition = contIP;

        for (int count = 0; count < blocks.size(); count++) {
            Object obj = blocks.get(count);
            if (obj instanceof Block) {
                containingBPPosition = contBP;
                containingIPPosition = contIP;
                renderBlock((Block) obj);
                containingBPPosition = contBP;
                containingIPPosition = contIP;
            } else {
                // a line area is rendered from the top left position
                // of the line, each inline object is offset from there
                LineArea line = (LineArea) obj;
                currentBlockIPPosition =
                        currentIPPosition + line.getStartIndent();
                renderLineArea(line);
                currentBPPosition += line.getHeight();
            }
        }
    }

}
