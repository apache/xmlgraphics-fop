/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import java.awt.Cursor;

import org.apache.batik.bridge.*;

import org.apache.batik.css.HiddenChildElementSupport;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;

import org.apache.fop.pdf.*;

import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGAElement;

/**
 * Bridge class for the &lt;a> element.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 */
public class PDFAElementBridge extends AbstractGraphicsNodeBridge {
    PDFGraphics2D pdfDoc;

    /**
     * Constructs a new bridge for the &lt;a> element.
     */
    public PDFAElementBridge() {}

    public void setPDFGraphics2D(PDFGraphics2D doc) {
        this.pdfDoc = doc;
    }

    /**
     * Returns 'a'.
     */
    public String getLocalName() {
        return SVG_A_TAG;
    }

    /**
     * Creates a <tt>CompositeGraphicsNode</tt>.
     */
    protected GraphicsNode instantiateGraphicsNode() {
        return new PDFANode();
    }

    /**
     * Builds using the specified BridgeContext and element, the
     * specified graphics node.
     *
     * @param ctx the bridge context to use
     * @param e the element that describes the graphics node to build
     * @param node the graphics node to build
     */
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
        PDFANode aNode = (PDFANode) super.createGraphicsNode(ctx, e);
        return aNode;
    }

    /**
     * Returns true as the &lt;a> element is a container.
     */
    public boolean isComposite() {
        return true;
    }

}
