/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import org.apache.batik.gvt.*;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.geom.Dimension2D;

/**
 * A graphics node that represents an image described as a graphics node.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 */
public class PDFANode extends CompositeGraphicsNode {

    /**
     * Constructs a new empty <tt>PDFANode</tt>.
     */
    public PDFANode() {}

    /**
     * Paints this node if visible.
     *
     * @param g2d the Graphics2D to use
     * @param rc the GraphicsNodeRenderContext to use
     */
    public void paint(Graphics2D g2d, GraphicsNodeRenderContext rc) {
        if (isVisible) {
            super.paint(g2d, rc);
        }
    }

    //
    // Properties methods
    //

}

