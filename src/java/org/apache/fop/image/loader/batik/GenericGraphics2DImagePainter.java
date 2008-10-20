package org.apache.fop.image.loader.batik;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.xmlgraphics.image.loader.impl.ImageXMLDOM;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

/**
 * A generic graphics 2D image painter implementation
 */
public class GenericGraphics2DImagePainter implements Graphics2DImagePainter {

    protected final ImageXMLDOM svg;
    protected final BridgeContext ctx;
    protected final GraphicsNode root;

    /**
     * Constructor
     *
     * @param svg the svg image dom
     * @param ctx the bridge context
     * @param root the graphics node root
     */
    public GenericGraphics2DImagePainter(ImageXMLDOM svg, BridgeContext ctx, GraphicsNode root) {
        this.svg = svg;
        this.ctx = ctx;
        this.root = root;
    }

    /**
     * Initialises the graphics 2d
     *
     * @param g2d the graphics 2d
     * @param area the rectangle drawing area
     */
    protected void init(Graphics2D g2d, Rectangle2D area) {
        // If no viewbox is defined in the svg file, a viewbox of 100x100 is
        // assumed, as defined in SVGUserAgent.getViewportSize()
        double tx = area.getX();
        double ty = area.getY();
        if (tx != 0 || ty != 0) {
            g2d.translate(tx, ty);
        }

        float iw = (float) ctx.getDocumentSize().getWidth();
        float ih = (float) ctx.getDocumentSize().getHeight();
        float w = (float) area.getWidth();
        float h = (float) area.getHeight();
        float sx = w / iw;
        float sy = h / ih;
        if (sx != 1.0 || sy != 1.0) {
            g2d.scale(sx, sy);
        }
    }

    /** {@inheritDoc} */
    public void paint(Graphics2D g2d, Rectangle2D area) {
        init(g2d, area);
        root.paint(g2d);
    }

    /** {@inheritDoc} */
    public Dimension getImageSize() {
        return new Dimension(svg.getSize().getWidthMpt(), svg.getSize().getHeightMpt());
    }

    /**
     * Returns the svg image dom
     * @return the svg image dom
     */
    public ImageXMLDOM getImageXMLDOM() {
        return svg;
    }

    /**
     * Returns the bridge context
     * @return the bridge context
     */
    public BridgeContext getBridgeContext() {
        return ctx;
    }

    /**
     * Returns the graphics root node
     * @return the graphics root node
     */
    public GraphicsNode getRoot() {
        return root;
    }

}