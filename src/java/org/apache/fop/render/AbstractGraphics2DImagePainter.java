package org.apache.fop.render;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.apache.batik.gvt.GraphicsNode;
import org.apache.xmlgraphics.java2d.Graphics2DPainterPreparator;

/**
 * An initializable graphics 2D image painter
 */
public abstract class AbstractGraphics2DImagePainter
    implements org.apache.xmlgraphics.java2d.Graphics2DImagePainter {

    private final GraphicsNode root;

    protected Graphics2DPainterPreparator preparator;

    /**
     * Main constructor
     *
     * @param root a graphics node root
     */
    public AbstractGraphics2DImagePainter(GraphicsNode root) {
        this.root = root;
    }

    /**
     * Sets the graphics 2D painter preparator
     *
     * @param initializer the graphics 2D preparator
     */
    public void setPreparator(Graphics2DPainterPreparator preparator) {
        this.preparator = preparator;
    }

    /**
     * Returns the graphics 2D painter preparator
     *
     * @return the graphics 2D painter preparator
     */
    protected Graphics2DPainterPreparator getPreparator() {
        return this.preparator;
    }

    /** {@inheritDoc} */
    public void paint(Graphics2D g2d, Rectangle2D area) {
        Graphics2DPainterPreparator preparator = getPreparator();
        if (preparator != null) {
            preparator.prepare(g2d, area);
        }
        root.paint(g2d);
    }

}
