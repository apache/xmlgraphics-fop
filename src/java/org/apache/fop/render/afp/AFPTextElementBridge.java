/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.render.afp;

import org.apache.batik.bridge.SVGTextElementBridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.gvt.TextPainter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Bridge class for the &lt;text> element.
 * This bridge will use the direct text painter if the text
 * for the element is simple.
 */
public class AFPTextElementBridge extends SVGTextElementBridge {
    
    private AFPTextPainter textPainter;

    /**
     * Constructs a new bridge for the &lt;text> element.
     * @param textPainter the text painter to use
     */
    public AFPTextElementBridge(AFPTextPainter textPainter) {
        this.textPainter = textPainter;
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

    private TextPainter getTextPainter() {
        return this.textPainter;
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
        for (Node n = element.getFirstChild();
                n != null;
                n = n.getNextSibling()) {

            switch (n.getNodeType()) {
            case Node.ELEMENT_NODE:

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
            default:
            }
        }

        /*if (CSSUtilities.convertFilter(element, node, ctx) != null) {
            return false;
        }*/

        return true;
    }
}

