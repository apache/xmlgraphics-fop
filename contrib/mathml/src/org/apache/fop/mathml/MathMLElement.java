/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.mathml;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;

import org.apache.fop.fo.FONode;
import org.apache.fop.apps.FOPException;

import org.w3c.dom.Document;
import org.w3c.dom.*;
import org.xml.sax.Attributes;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.svg.SVGDOMImplementation;

import net.sourceforge.jeuclid.MathBase;
import net.sourceforge.jeuclid.DOMMathBuilder;

public class MathMLElement extends MathMLObj {
    Document svgDoc = null;
    float width;
    float height;
    boolean converted = false;

    public MathMLElement(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        createBasicDocument();
    }

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
            log.error("Could not convert MathML to SVG", t);
            width = 0;
            height = 0;
        }

    }

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

    public Document getDocument() {
        convertToSVG();
        return doc;
    }

    public String getDocumentNamespace() {
        if (svgDoc == null) {
            return MathMLElementMapping.URI;
        }
        return "http://www.w3.org/2000/svg";
    }

    public Point2D getDimension(Point2D view) {
        convertToSVG();
        return new Point2D.Float(width, height);
    }
}

