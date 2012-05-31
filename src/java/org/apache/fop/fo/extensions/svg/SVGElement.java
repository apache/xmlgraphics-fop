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

package org.apache.fop.fo.extensions.svg;

// FOP
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.net.URI;

import org.w3c.dom.Element;

import org.apache.batik.bridge.UnitProcessor;
import org.apache.batik.dom.svg.SVGContext;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.util.SVGConstants;

import org.apache.fop.fo.FONode;
import org.apache.fop.util.ContentHandlerFactory;

/**
 * Class representing the SVG root element
 * for constructing an SVG document.
 */
public class SVGElement extends SVGObj {

    /**
     * Constructs an SVG object
     *
     * @param parent the parent formatting object
     */
    public SVGElement(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public ContentHandlerFactory getContentHandlerFactory() {
        return new SVGDOMContentHandlerFactory();
    }

    /**
     * Get the dimensions of this XML document.
     * @param view the viewport dimensions
     * @return the dimensions of this SVG document
     */
    public Point2D getDimension(final Point2D view) {

        // TODO change so doesn't hold onto fo, area tree
        Element svgRoot = element;
        /* create an SVG area */
        /* if width and height are zero, get the bounds of the content. */

        try {
            URI baseUri = getUserAgent().getNewURIResolver().getBaseURI();
            baseUri = baseUri == null ? new File("").toURI() : baseUri;
            if (baseUri != null) {
                SVGOMDocument svgdoc = (SVGOMDocument)doc;
                svgdoc.setURLObject(baseUri.toURL());
                //The following line should not be called to leave FOP compatible to Batik 1.6.
                //svgdoc.setDocumentURI(baseURL.toString());
            }
        } catch (Exception e) {
            log.error("Could not set base URL for svg", e);
        }

        final float ptmm = getUserAgent().getSourcePixelUnitToMillimeter();
        // temporary svg context
        SVGContext dc = new SVGContext() {
            public float getPixelToMM() {
                return ptmm;
            }
            public float getPixelUnitToMillimeter() {
                return ptmm;
            }

            public Rectangle2D getBBox() {
                return new Rectangle2D.Double(0, 0, view.getX(), view.getY());
            }

            /**
             * Returns the transform from the global transform space to pixels.
             */
            public AffineTransform getScreenTransform() {
                throw new UnsupportedOperationException("NYI");
            }

            /**
             * Sets the transform to be used from the global transform space
             * to pixels.
             */
            public void setScreenTransform(AffineTransform at) {
                throw new UnsupportedOperationException("NYI");
            }

            public AffineTransform getCTM() {
                return new AffineTransform();
            }

            public AffineTransform getGlobalTransform() {
                return new AffineTransform();
            }

            public float getViewportWidth() {
                return (float)view.getX();
            }

            public float getViewportHeight() {
                return (float)view.getY();
            }

            public float getFontSize() {
                return 12;
            }

            public void deselectAll() {
            }
        };
        SVGOMElement e = (SVGOMElement)svgRoot;
        e.setSVGContext(dc);

        //if (!e.hasAttributeNS(XMLSupport.XMLNS_NAMESPACE_URI, "xmlns")) {
            e.setAttributeNS(XMLSupport.XMLNS_NAMESPACE_URI, "xmlns",
                                SVGDOMImplementation.SVG_NAMESPACE_URI);
        //}
        int fontSize = 12;
        Point2D p2d = getSize(fontSize, svgRoot, getUserAgent().getSourcePixelUnitToMillimeter());
        e.setSVGContext(null);

        return p2d;
    }

    /**
     * Get the size of the SVG root element.
     * @param size the font size
     * @param svgRoot the svg root element
     * @param ptmm the pixel to millimeter conversion factor
     * @return the size of the SVG document
     */
    public static Point2D getSize(int size, Element svgRoot, float ptmm) {
        String str;
        UnitProcessor.Context ctx;
        ctx = new PDFUnitContext(size, svgRoot, ptmm);
        str = svgRoot.getAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE);
        if (str.length() == 0) {
            str = "100%";
        }
        float width = UnitProcessor.svgHorizontalLengthToUserSpace
            (str, SVGConstants.SVG_WIDTH_ATTRIBUTE, ctx);

        str = svgRoot.getAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE);
        if (str.length() == 0) {
            str = "100%";
        }
        float height = UnitProcessor.svgVerticalLengthToUserSpace
            (str, SVGConstants.SVG_HEIGHT_ATTRIBUTE, ctx);
        return new Point2D.Float(width, height);
    }

    /**
     * This class is the default context for a particular
     * element. Information not available on the element are obtained from
     * the bridge context (such as the viewport or the pixel to
     * millimeter factor.
     */
    public static class PDFUnitContext implements UnitProcessor.Context {

        /** The element. */
        private Element e;
        private int fontSize;
        private float pixeltoMM;

        /**
         * Create a PDF unit context.
         * @param size the font size.
         * @param e the svg element
         * @param ptmm the pixel to millimeter factor
         */
        public PDFUnitContext(int size, Element e, float ptmm) {
            this.e = e;
            this.fontSize = size;
            this.pixeltoMM = ptmm;
        }

        /**
         * Returns the element.
         * @return the element
         */
        public Element getElement() {
            return e;
        }

        /**
         * Returns the context of the parent element of this context.
         * Since this is always for the root SVG element there never
         * should be one...
         * @return null
         */
        public UnitProcessor.Context getParentElementContext() {
            return null;
        }

        /**
         * Returns the pixel to mm factor. (this is deprecated)
         * @return the pixel to millimeter factor
         */
        public float getPixelToMM() {
            return pixeltoMM;
        }

        /**
         * Returns the pixel to mm factor.
         * @return the pixel to millimeter factor
         */
        public float getPixelUnitToMillimeter() {
            return pixeltoMM;
        }

        /**
         * Returns the font-size value.
         * @return the default font size
         */
        public float getFontSize() {
            return fontSize;
        }

        /**
         * Returns the x-height value.
         * @return the x-height value
         */
        public float getXHeight() {
            return 0.5f;
        }

        /**
         * Returns the viewport width used to compute units.
         * @return the default viewport width of 100
         */
        public float getViewportWidth() {
            return 100;
        }

        /**
         * Returns the viewport height used to compute units.
         * @return the default viewport height of 100
         */
        public float getViewportHeight() {
            return 100;
        }
    }

}

