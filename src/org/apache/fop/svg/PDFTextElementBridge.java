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

import org.apache.batik.gvt.TextNode;
import org.apache.batik.bridge.SVGTextElementBridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.TextUtilities;
import org.apache.batik.gvt.GraphicsNode;

import org.apache.fop.layout.FontInfo;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Bridge class for the &lt;text> element.
 * This bridge will use the direct text painter if the text
 * for the element is simple.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 */
public class PDFTextElementBridge extends SVGTextElementBridge {
    private PDFTextPainter pdfTextPainter;

    /**
     * Constructs a new bridge for the &lt;text> element.
     * @param fi the font infomration
     */
    public PDFTextElementBridge(FontInfo fi) {
        pdfTextPainter = new PDFTextPainter(fi);
    }

    /**
     * Create a text element bridge.
     * This set the text painter on the node if the text is simple.
     * @param ctx the bridge context
     * @param e the svg element
     * @return the text graphics node created by the super class
     */
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
        GraphicsNode node = super.createGraphicsNode(ctx, e);
        if (node != null && isSimple(ctx, e, node)) {
            ((TextNode)node).setTextPainter(getTextPainter());
        }
        return node;
    }

    private PDFTextPainter getTextPainter() {
        return pdfTextPainter;
    }

    /**
     * Check if text element contains simple text.
     * This checks the children of the text element to determine
     * if the text is simple. The text is simple if it can be rendered
     * with basic text drawing algorithms. This means there are no
     * alternate characters, the font is known and there are no effects
     * applied to the text.
     *
     * @param ctx the bridge context
     * @param element the svg text element
     * @param node the graphics node
     * @return true if this text is simple of false if it cannot be
     *         easily rendered using normal drawString on the PDFGraphics2D
     */
    private boolean isSimple(BridgeContext ctx, Element element, GraphicsNode node) {
        // Font size, in user space units.
        float fs = TextUtilities.convertFontSize(element).floatValue();
        // PDF cannot display fonts over 36pt
        if (fs > 36) {
            return false;
        }

        Element nodeElement;
        for (Node n = element.getFirstChild();
             n != null;
             n = n.getNextSibling()) {

            switch (n.getNodeType()) {
            case Node.ELEMENT_NODE:

                nodeElement = (Element)n;

                if (n.getLocalName().equals(SVG_TSPAN_TAG)
                    || n.getLocalName().equals(SVG_ALT_GLYPH_TAG)) {
                    return false;
                } else if (n.getLocalName().equals(SVG_TEXT_PATH_TAG)) {
                    return false;
                } else if (n.getLocalName().equals(SVG_TREF_TAG)) {
                    return false;
                }
                break;
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
            }
        }

        /*if (CSSUtilities.convertFilter(element, node, ctx) != null) {
            return false;
        }*/

        return true;
    }
}

