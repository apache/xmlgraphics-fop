/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import org.apache.batik.gvt.TextNode;
import org.apache.batik.bridge.*;

import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;

import org.apache.fop.layout.FontState;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.apache.batik.gvt.GraphicsNode;

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
     */
    public PDFTextElementBridge(FontState fs) {
        pdfTextPainter = new PDFTextPainter(fs);
    }

    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
        GraphicsNode node = super.createGraphicsNode(ctx, e);
        if(isSimple(ctx, e, node)) {
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
     */
    private boolean isSimple(BridgeContext ctx, Element element, GraphicsNode node) {
        // Font size, in user space units.
        float fs = TextUtilities.convertFontSize(element).floatValue();
        if(((int)fs) != fs) {
            return false;
        }
        if(fs > 36) {
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

        if(CSSUtilities.convertFilter(element, node, ctx) != null) {
            return false;
        }

        return true;
    }
}
