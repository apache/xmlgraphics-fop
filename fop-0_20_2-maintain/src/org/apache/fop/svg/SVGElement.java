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

// FOP
import org.apache.fop.fo.FObj;

import org.apache.fop.fo.PropertyList;

import org.apache.fop.fo.Status;

import org.apache.fop.layout.Area;

import org.apache.fop.layout.FontState;

import org.apache.fop.apps.FOPException;

import org.apache.fop.layout.inline.ForeignObjectArea;

import org.apache.fop.configuration.Configuration;



import org.apache.batik.dom.svg.SVGContext;

import org.apache.batik.dom.svg.SVGOMElement;

import org.apache.batik.dom.svg.SVGOMDocument;

import org.apache.batik.dom.util.XMLSupport;

import org.w3c.dom.Element;

import org.w3c.dom.svg.SVGDocument;

import org.apache.batik.bridge.BridgeContext;

import org.apache.batik.bridge.UnitProcessor;



import org.w3c.dom.DOMImplementation;

import org.apache.batik.dom.svg.SVGDOMImplementation;



import java.net.URL;

import java.awt.geom.AffineTransform;

import java.awt.geom.Rectangle2D;


/**
 * class representing svg:svg pseudo flow object.
 */
public class SVGElement extends SVGObj {

    /**
     * inner class for making SVG objects.
     */
    public static class Maker extends FObj.Maker {

        /**
         * make an SVG object.
         *
         * @param parent the parent formatting object
         * @param propertyList the explicit properties of this object
         *
         * @return the SVG object
         */
        public FObj make(FObj parent, PropertyList propertyList,
                         String systemId, int line, int column)
            throws FOPException {
            return new SVGElement(parent, propertyList,
                                  systemId, line, column);
        }
    }

    /**
     * returns the maker for this object.
     *
     * @return the maker for SVG objects
     */
    public static FObj.Maker maker() {
        return new SVGElement.Maker();
    }

    FontState fs;

    /**
     * constructs an SVG object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    public SVGElement(FObj parent, PropertyList propertyList,
                      String systemId, int line, int column) {
        super(parent, propertyList, "svg", systemId, line, column);
        init();
    }

    /**
     * layout this formatting object.
     *
     * @param area the area to layout the object into
     *
     * @return the status of the layout
     */
    public int layout(final Area area) throws FOPException {

        if (!(area instanceof ForeignObjectArea)) {
            // this is an error
            throw new FOPException("SVG not in fo:instream-foreign-object");
        }

        if (this.marker == START) {
            this.fs = area.getFontState();

            this.marker = 0;
        }

        final Element svgRoot = element;
        /* create an SVG area */
        /* if width and height are zero, get the bounds of the content. */
        final ForeignObjectArea foa = (ForeignObjectArea)area;
        SVGContext dc = new SVGContext() {
            public float getPixelToMM() {
                // 72 dpi
                return 0.35277777777777777778f;
            }

            public float getPixelUnitToMillimeter() {
                // 72 dpi
                return 0.35277777777777777778f;
            }

            public Rectangle2D getBBox() {
                return new Rectangle2D.Double(0, 0, foa.getContentWidth(), foa.getContentHeight());
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
                return (float)foa.getContentWidth();
            }

            public float getViewportHeight() {
                return (float)foa.getContentHeight();
            }

            public float getFontSize(){
                return fs.getFontSize() / 1000f;
            }
            
            public void deselectAll() {
            }
        };
        ((SVGOMElement)svgRoot).setSVGContext(dc);

        try {
            URL baseURL = Configuration.getBaseURL();
            if (baseURL != null) {
                ((SVGOMDocument)doc).setURLObject(baseURL);
            }
        } catch (Exception e) {
            log.error("Could not set base URL for svg", e);
        }

        Element e = ((SVGDocument)doc).getRootElement();

        //if(!e.hasAttributeNS(XMLSupport.XMLNS_NAMESPACE_URI, "xmlns")) {
            e.setAttributeNS(XMLSupport.XMLNS_NAMESPACE_URI, "xmlns", SVGDOMImplementation.SVG_NAMESPACE_URI);
        //}

        String s;
        SVGUserAgent userAgent = new SVGUserAgent(new AffineTransform());
        userAgent.setLogger(log);
        BridgeContext ctx = new BridgeContext(userAgent);
        UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, e);

        // 'width' attribute - default is 100%
        s = e.getAttributeNS(null, SVGOMDocument.SVG_WIDTH_ATTRIBUTE);
        if (s.length() == 0) {
            s = SVGOMDocument.SVG_SVG_WIDTH_DEFAULT_VALUE;
        }
        float width = UnitProcessor.svgHorizontalLengthToUserSpace(s, SVGOMDocument.SVG_WIDTH_ATTRIBUTE, uctx);

        // 'height' attribute - default is 100%
        s = e.getAttributeNS(null, SVGOMDocument.SVG_HEIGHT_ATTRIBUTE);
        if (s.length() == 0) {
            s = SVGOMDocument.SVG_SVG_HEIGHT_DEFAULT_VALUE;
        }
        float height = UnitProcessor.svgVerticalLengthToUserSpace(s, SVGOMDocument.SVG_HEIGHT_ATTRIBUTE, uctx);

        SVGArea svg = new SVGArea(fs, width, height);
        svg.setSVGDocument(doc);
        svg.start();

        /* finish off the SVG area */
        svg.end();

        /* add the SVG area to the containing area */
        foa.setObject(svg);
        foa.setIntrinsicWidth(svg.getWidth());
        foa.setIntrinsicHeight(svg.getHeight());

       ((SVGOMElement)svgRoot).setSVGContext(null);

        /* return status */
        return Status.OK;
    }

    private void init() {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        doc = impl.createDocument(svgNS, "svg", null);

        element = doc.getDocumentElement();

        buildTopLevel(doc, element);
    }

}
