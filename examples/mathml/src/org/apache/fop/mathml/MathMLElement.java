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
package org.apache.fop.mathml;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;

import org.apache.fop.fo.FONode;
import org.apache.fop.apps.FOPException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xml.sax.Attributes;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.svg.SVGDOMImplementation;

import net.sourceforge.jeuclid.MathBase;
import net.sourceforge.jeuclid.DOMMathBuilder;

/**
 * Defines the top-level element for MathML.
 */
public class MathMLElement extends MathMLObj {

    private Document svgDoc = null;
    private float width;
    private float height;
    private boolean converted = false;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public MathMLElement(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FONode#handleAttrs(Attributes)
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        createBasicDocument();
    }

    /**
     * Converts the MathML to SVG.
     */
    public void convertToSVG() {
        try {
            if (!converted) {
                converted = true;
                String fontname = "Helvetica";
                int fontstyle = 0;
                int inlinefontstyle = 0;
                int displayfontsize = 12;
                int inlinefontsize = 12;

                MathBase base = new MathBase(
                                  (new DOMMathBuilder(doc)).getMathRootElement(),
                                  fontname, fontstyle, inlinefontsize,
                                  displayfontsize);

                base.setDebug(false);

                svgDoc = createSVG(base);

                width = base.getWidth();
                height = base.getHeight();

                doc = svgDoc;
            }
        } catch (Throwable t) {
            getLogger().error("Could not convert MathML to SVG", t);
            width = 0;
            height = 0;
        }

    }

    /**
     * Create the SVG from MathML.
     * @return the DOM document containing SVG
     */
    public static Document createSVG(MathBase base) {

        DOMImplementation impl =
            SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        Document svgdocument = impl.createDocument(svgNS, "svg", null);

        SVGGraphics2D g = new SVGGraphics2D(svgdocument);

        g.setSVGCanvasSize(
          new Dimension(base.getWidth(), base.getHeight()));

        //g.setColor(Color.white);
        //g.fillRect(0, 0, base.getWidth(), base.getHeight());
        g.setColor(Color.black);

        base.paint(g);

        //if (antialiasing)
        //element.setAttribute("text-rendering", "optimizeLegibility");
        //else
        //element.setAttribute("text-rendering", "geometricPrecision");

        // this should be done in a better way
        Element root = g.getRoot();
        svgdocument = impl.createDocument(svgNS, "svg", null);
        Node node = svgdocument.importNode(root, true);
        ((org.apache.batik.dom.svg.SVGOMDocument) svgdocument).
        getRootElement().appendChild(node);

        return svgdocument;

    }

    /**
     * @see org.apache.fop.fo.XMLObj#getDocument()
     */
    public Document getDocument() {
        convertToSVG();
        return doc;
    }

    /**
     * @see org.apache.fop.fo.XMLObj#getDocumentNamespace()
     */
    public String getDocumentNamespace() {
        if (svgDoc == null) {
            return MathMLElementMapping.NAMESPACE;
        }
        return "http://www.w3.org/2000/svg";
    }

    /**
     * @see org.apache.fop.fo.XMLObj#getDimension(Point2D)
     */
    public Point2D getDimension(Point2D view) {
        convertToSVG();
        return new Point2D.Float(width, height);
    }
}

