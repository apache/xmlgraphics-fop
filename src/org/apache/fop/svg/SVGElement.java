/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layout.inline.*;
import org.apache.fop.configuration.Configuration;

import org.apache.batik.dom.svg.*;
import org.w3c.dom.*;
import org.w3c.dom.svg.*;
import org.w3c.dom.svg.SVGLength;
import org.apache.batik.bridge.*;
import org.apache.batik.swing.svg.*;
import org.apache.batik.swing.gvt.*;
import org.apache.batik.gvt.*;
import org.apache.batik.gvt.renderer.*;
import org.apache.batik.gvt.filter.*;
import org.apache.batik.gvt.event.*;

import org.w3c.dom.DOMImplementation;
import org.apache.batik.dom.svg.SVGDOMImplementation;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.awt.geom.AffineTransform;

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
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new SVGElement(parent, propertyList);
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
    public SVGElement(FObj parent, PropertyList propertyList) {
        super(parent, propertyList, "svg");
        init();
    }

    /**
     * layout this formatting object.
     *
     * @param area the area to layout the object into
     *
     * @return the status of the layout
     */
    public Status layout(final Area area) throws FOPException {

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
        DefaultSVGContext dc = new DefaultSVGContext() {
            public float getPixelToMM() {
                // 72 dpi
                return 0.35277777777777777778f;
            }

            public float getViewportWidth(Element e) throws IllegalStateException {
                if(e == svgRoot) {
                    ForeignObjectArea foa = (ForeignObjectArea)area;
                    if(!foa.isContentWidthAuto()) {
                        return foa.getContentWidth();
                    }
                }
                return super.getViewportWidth(e);
            }

            public float getViewportHeight(Element e) throws IllegalStateException {
                if(e == svgRoot) {
                    ForeignObjectArea foa = (ForeignObjectArea)area;
                    if(!foa.isContentHeightAuto()) {
                        return foa.getContentHeight();
                    }
                }
                return super.getViewportHeight(e);
            }

            public List getDefaultFontFamilyValue() {
                return FONT_FAMILY;
            }
        };
        ((SVGOMDocument)doc).setSVGContext(dc);

        try {
            String baseDir = Configuration.getStringValue("baseDir");
            ((SVGOMDocument)doc).setURLObject(new URL(baseDir));
        } catch (Exception e) {
            log.error("Could not set base URL for svg", e);
        }

        Element e = ((SVGDocument)doc).getRootElement();
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
        float width = UnitProcessor.svgHorizontalLengthToUserSpace
                     (s, SVGOMDocument.SVG_WIDTH_ATTRIBUTE, uctx);

        // 'height' attribute - default is 100%
        s = e.getAttributeNS(null, SVGOMDocument.SVG_HEIGHT_ATTRIBUTE);
        if (s.length() == 0) {
            s = SVGOMDocument.SVG_SVG_HEIGHT_DEFAULT_VALUE;
        }
        float height = UnitProcessor.svgVerticalLengthToUserSpace
                     (s, SVGOMDocument.SVG_HEIGHT_ATTRIBUTE, uctx);

        SVGArea svg = new SVGArea(fs, width, height);
        svg.setSVGDocument(doc);
        svg.start();

        /* finish off the SVG area */
        svg.end();

        /* add the SVG area to the containing area */
        ForeignObjectArea foa = (ForeignObjectArea)area;
        foa.setObject(svg);
        foa.setIntrinsicWidth(svg.getWidth());
        foa.setIntrinsicHeight(svg.getHeight());

        /* return status */
        return new Status(Status.OK);
    }

    private void init() {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        doc = impl.createDocument(svgNS, "svg", null);

        element = doc.getDocumentElement();

        buildTopLevel(doc, element);
    }

    public final static List FONT_FAMILY;
    static {
        FONT_FAMILY = new ArrayList();
        FONT_FAMILY.add("Helvetica");
        FONT_FAMILY.add("Times");
        FONT_FAMILY.add("Courier");
        FONT_FAMILY.add("sans-serif");
        FONT_FAMILY.add("serif");
    }

}
