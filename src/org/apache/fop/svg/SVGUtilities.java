/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import java.util.*;
import java.text.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.font.FontRenderContext;

import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.layout.inline.*;
import org.apache.fop.svg.*;
import org.w3c.dom.*;
import org.w3c.dom.svg.*;
import org.w3c.dom.css.*;

import org.apache.batik.dom.svg.SVGDOMImplementation;

/**
 * Some utilities for creating svg DOM documents and elements.
 */
public class SVGUtilities {
    final static String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;


    public static final Document createSVGDocument(float width,
            float height) {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        Document doc = impl.createDocument(svgNS, "svg", null);

        Element svgRoot = doc.getDocumentElement();
        svgRoot.setAttributeNS(null, "width", "" + width);
        svgRoot.setAttributeNS(null, "height", "" + height);
        return doc;
    }

    /**
     * Get the string width for a particular string given the font.
     */
    public static final float getStringWidth(String str, java.awt.Font font) {
        Rectangle2D rect =
            font.getStringBounds(str, 0, str.length(),
                                 new FontRenderContext(new AffineTransform(),
                                 true, true));
        return (float)rect.getWidth();
    }

    /**
     * Get the string height for a particular string given the font.
     */
    public static final float getStringHeight(String str,
                                              java.awt.Font font) {
        Rectangle2D rect =
            font.getStringBounds(str, 0, str.length(),
                                 new FontRenderContext(new AffineTransform(),
                                 true, true));
        return (float)rect.getHeight();
    }

    /**
     * Get the string bounds for a particular string given the font.
     */
    public static final Rectangle2D getStringBounds(String str,
            java.awt.Font font) {
        return font.getStringBounds(str, 0, str.length(),
                                    new FontRenderContext(new AffineTransform(),
                                    true, true));
    }

    /**
     * Create an SVG Line
     */
    public static final Element createLine(Document doc, float x, float y,
                                           float x2, float y2) {
        Element ellipse = doc.createElementNS(svgNS, "line");
        ellipse.setAttributeNS(null, "x1", "" + x);
        ellipse.setAttributeNS(null, "x2", "" + x2);
        ellipse.setAttributeNS(null, "y1", "" + y);
        ellipse.setAttributeNS(null, "y2", "" + y2);
        return ellipse;
    }

    /**
     * Create an SVG Ellipse
     */
    public static final Element createEllipse(Document doc, float cx,
                                              float cy, float rx, float ry) {
        Element ellipse = doc.createElementNS(svgNS, "ellipse");
        ellipse.setAttributeNS(null, "cx", "" + cx);
        ellipse.setAttributeNS(null, "rx", "" + rx);
        ellipse.setAttributeNS(null, "cy", "" + cy);
        ellipse.setAttributeNS(null, "ry", "" + ry);
        return ellipse;
    }

    /**
     * Create an SVG Path.
     */
    public static final Element createPath(Document doc, String str) {
        Element path = doc.createElementNS(svgNS, "path");
        path.setAttributeNS(null, "d", str);
        return path;
    }

    /**
     * Create an SVG Text object.
     */
    public static final Element createText(Document doc, float x, float y,
                                           String str) {
        Element textGraph = doc.createElementNS(svgNS, "text");
        textGraph.setAttributeNS(null, "x", "" + x);
        textGraph.setAttributeNS(null, "y", "" + y);
        org.w3c.dom.Text text = doc.createTextNode(str);
        textGraph.appendChild(text);
        return textGraph;
    }

    /**
     * Create an SVG Rectangle.
     */
    public static final Element createRect(Document doc, float x, float y,
                                           float width, float height) {
        Element border = doc.createElementNS(svgNS, "rect");
        border.setAttributeNS(null, "x", "" + x);
        border.setAttributeNS(null, "y", "" + y);
        border.setAttributeNS(null, "width", "" + width);
        border.setAttributeNS(null, "height", "" + height);
        return border;
    }

    /**
     * Create an SVG G.
     */
    public static final Element createG(Document doc) {
        Element border = doc.createElementNS(svgNS, "g");
        return border;
    }

    /**
     * Create an SVG Clip.
     */
    public static final Element createClip(Document doc, Element els,
                                           String id) {
        Element border = doc.createElementNS(svgNS, "clipPath");
        border.setAttributeNS(null, "id", id);
        border.appendChild(els);
        return border;
    }

    public static final Element createImage(Document doc, String ref,
                                            float width, float height) {
        Element border = doc.createElementNS(svgNS, "image");
        border.setAttributeNS("http://www.w3.org/1999/xlink", "href",
                              ref);
        border.setAttributeNS(null, "width", "" + width);
        border.setAttributeNS(null, "height", "" + height);
        return border;
    }

    /**
     * Create some SVG text that is wrapped into a specified width..
     */
    public static final Element wrapText(Document doc, String str,
                                         java.awt.Font font, float width) {
        Element g = createG(doc);
        Element text;
        StringTokenizer st = new StringTokenizer(str, " \t\r\n");
        float totalWidth = 0;
        String totalStr = "";
        int line = 0;
        float height = getStringHeight(str, font);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            float strwidth = getStringWidth(token, font);
            totalWidth += strwidth;
            if (totalWidth > width) {
                if (totalStr.equals("")) {
                    totalStr = token;
                    token = "";
                    strwidth = 0;
                }
                text = createText(doc, 0, line * (height + 5), totalStr);
                g.appendChild(text);
                totalStr = token;
                totalWidth = strwidth;
                line++;
            } else {
                totalStr = totalStr + " " + token;
            }
        }

        return g;
    }

}
