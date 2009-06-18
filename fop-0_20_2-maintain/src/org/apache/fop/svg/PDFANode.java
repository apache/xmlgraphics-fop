/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.svg;

import org.apache.batik.gvt.CompositeGraphicsNode;

import java.awt.Graphics2D;
import java.awt.Shape;
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
                    type = org.apache.fop.layout.LinkSet.INTERNAL;
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

