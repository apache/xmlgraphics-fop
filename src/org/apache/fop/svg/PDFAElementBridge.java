/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import java.awt.geom.AffineTransform;

import org.apache.batik.bridge.AbstractGraphicsNodeBridge;
import org.apache.batik.bridge.BridgeContext;

import org.apache.batik.gvt.GraphicsNode;

import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAElement;

/**
 * Bridge class for the &lt;a> element.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 */
public class PDFAElementBridge extends AbstractGraphicsNodeBridge {
    private AffineTransform transform;

    /**
     * Constructs a new bridge for the &lt;a> element.
     */
    public PDFAElementBridge() {
    }

    /**
     * Set the current transform of this element.
     * @param tf the transform
     */
    public void setCurrentTransform(AffineTransform tf) {
        transform = tf;
    }

    /**
     * Returns 'a'.
     * @return the name of this node
     */
    public String getLocalName() {
        return SVG_A_TAG;
    }

    /**
     * Creates a <tt>CompositeGraphicsNode</tt>.
     * @return a new PDFANode
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
     * @return node the new graphics node
     */
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
        PDFANode aNode = (PDFANode)super.createGraphicsNode(ctx, e);
        aNode.setDestination(((SVGAElement)e).getHref().getBaseVal());
        aNode.setTransform(transform);
        return aNode;
    }

    /**
     * Returns true as the &lt;a> element is a container.
     * @return true if the a element is a container
     */
    public boolean isComposite() {
        return true;
    }

}
