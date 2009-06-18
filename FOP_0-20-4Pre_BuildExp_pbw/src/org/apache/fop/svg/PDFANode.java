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
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.AffineTransform;

import java.util.StringTokenizer;

/**
 * A graphics node that represents an image described as a graphics node.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 */
public class PDFANode extends CompositeGraphicsNode {
    String destination;
    AffineTransform transform;

    /**
     * Constructs a new empty <tt>PDFANode</tt>.
     */
    public PDFANode() {}

    /**
     * Set the destination String.
     */
    public void setDestination(String dest) {
        destination = dest;
    }

    public void setTransform(AffineTransform tf) {
        transform = tf;
    }

    /**
     * Paints this node if visible.
     *
     * @param g2d the Graphics2D to use
     * @param rc the GraphicsNodeRenderContext to use
     */
    public void paint(Graphics2D g2d) {
        if (isVisible) {
            super.paint(g2d);
            if(g2d instanceof PDFGraphics2D) {
                PDFGraphics2D pdfg = (PDFGraphics2D)g2d;
                int type = org.apache.fop.layout.LinkSet.EXTERNAL;
                Shape outline = getOutline();
                if(destination.startsWith("#svgView(viewBox(")) {
                    String nums = destination.substring(18, destination.length() - 2);
                    float x = 0;
                    float y = 0;
                    float width = 0;
                    float height;
                    int count = 0;
                    try {
                        StringTokenizer st = new StringTokenizer(nums, ",");
                        while(st.hasMoreTokens()) {
                            String tok = st.nextToken();
                            count++;
                            switch(count) {
                            case 1:
                                x = Float.parseFloat(tok);
                            break;
                            case 2:
                                y = Float.parseFloat(tok);
                            break;
                            case 3:
                                width = Float.parseFloat(tok);
                            break;
                            case 4:
                                height = Float.parseFloat(tok);
                            break;
                            default:
                            break;
                            }
                        }
                    } catch(Exception e) {
                    }
                    // these numbers need conversion to current
                    // svg position and scaled for the page
                    destination = "" + x + " " + y + " " + 200 / width;
                }
                pdfg.addLink(outline, transform, destination, type);           
            }
        }
    }

}

