/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import org.apache.batik.gvt.CompositeGraphicsNode;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;

import java.util.StringTokenizer;

/**
 * A graphics node that represents an image described as a graphics node.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 */
public class PDFANode extends CompositeGraphicsNode {
    private String destination;
    private AffineTransform transform;

    /**
     * Constructs a new empty <tt>PDFANode</tt>.
     */
    public PDFANode() {
    }

    /**
     * Set the destination String.
     * @param dest the target destination
     */
    public void setDestination(String dest) {
        destination = dest;
    }

    /**
     * Set the current transform of this node.
     * @param tf the transform
     */
    public void setTransform(AffineTransform tf) {
        transform = tf;
    }

    /**
     * Paints this node if visible.
     *
     * @param g2d the Graphics2D to use
     */
    public void paint(Graphics2D g2d) {
        if (isVisible) {
            super.paint(g2d);
            if (g2d instanceof PDFGraphics2D) {
                PDFGraphics2D pdfg = (PDFGraphics2D)g2d;
                int type = org.apache.fop.pdf.PDFLink.EXTERNAL;
                Shape outline = getOutline();
                if (destination.startsWith("#svgView(viewBox(")) {
                    type = org.apache.fop.pdf.PDFLink.INTERNAL;
                    String nums = destination.substring(17, destination.length() - 2);
                    float x = 0;
                    float y = 0;
                    float width = 0;
                    float height = 0;
                    int count = 0;
                    try {
                        StringTokenizer st = new StringTokenizer(nums, ",");
                        while (st.hasMoreTokens()) {
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Rectangle2D destRect = new Rectangle2D.Float(x, y, width, height);
                    destRect = transform.createTransformedShape(destRect).getBounds();
                    // these numbers need conversion to current
                    // svg position and scaled for the page
                    x = (float)destRect.getX();
                    y = (float)destRect.getY();
                    width = (float)destRect.getWidth();
                    height = (float)destRect.getHeight();

                    destination = "" + x + " " + y + " "
                                  + (x + width) + " " + (y + height);
                }
                pdfg.addLink(getBounds(), transform, destination, type);           
            }
        }
    }

}

