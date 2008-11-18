package org.apache.fop.image.loader.batik;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;

import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

/**
 * A generic graphics 2D image painter implementation
 */
public class Graphics2DImagePainterImpl implements Graphics2DImagePainter {

    private final GraphicsNode root;
    /** the Batik bridge context */
    protected final BridgeContext ctx;
    /** the intrinsic size of the image */
    protected final Dimension imageSize;

    /**
     * Main constructor
     *
     * @param root the graphics node root
     * @param ctx the bridge context
     * @param imageSize the image size
     */
    public Graphics2DImagePainterImpl(GraphicsNode root, BridgeContext ctx, Dimension imageSize) {
        this.root = root;
        this.imageSize = imageSize;
        this.ctx = ctx;
    }

    /** {@inheritDoc} */
    public Dimension getImageSize() {
        return imageSize;
    }

    private void prepare(Graphics2D g2d, Rectangle2D area) {
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
        prepare(g2d, area);
        root.paint(g2d);
    }

}