/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image.analyser;

// Java
import java.io.BufferedInputStream;
import java.io.IOException;

import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

// FOP
import org.apache.fop.messaging.*;
import org.apache.fop.image.SVGImage;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;

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
import java.awt.Point;
import java.awt.geom.Dimension2D;
import java.awt.Dimension;

/**
 * ImageReader object for SVG document image type.
 */
public class SVGReader extends AbstractImageReader {
    public boolean verifySignature(String uri,
                                   BufferedInputStream fis) throws IOException {
        this.imageStream = fis;
        return loadImage(uri);
    }

    public String getMimeType() {
        return "image/svg+xml";
    }

    /**
     * This means the external svg document will be loaded twice.
     * Possibly need a slightly different design for the image stuff.
     */
    protected boolean loadImage(String uri) {
        // parse document and get the size attributes of the svg element
        try {
            SAXSVGDocumentFactory factory =
              new SAXSVGDocumentFactory(SVGImage.getParserName());
            SVGDocument doc = factory.createDocument(uri, imageStream);

            Element e = ((SVGDocument)doc).getRootElement();
            String s;
            UserAgent userAgent = new MUserAgent(new AffineTransform());
            BridgeContext ctx = new BridgeContext(userAgent);
            UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, e);

            // 'width' attribute - default is 100%
            s = e.getAttributeNS(null, SVGOMDocument.SVG_WIDTH_ATTRIBUTE);
            if (s.length() == 0) {
                s = SVGOMDocument.SVG_SVG_WIDTH_DEFAULT_VALUE;
            }
            width = (int)UnitProcessor.svgHorizontalLengthToUserSpace
                         (s, SVGOMDocument.SVG_WIDTH_ATTRIBUTE, uctx);

            // 'height' attribute - default is 100%
            s = e.getAttributeNS(null, SVGOMDocument.SVG_HEIGHT_ATTRIBUTE);
            if (s.length() == 0) {
                s = SVGOMDocument.SVG_SVG_HEIGHT_DEFAULT_VALUE;
            }
            height = (int)UnitProcessor.svgVerticalLengthToUserSpace
                         (s, SVGOMDocument.SVG_HEIGHT_ATTRIBUTE, uctx);

            return true;
        } catch (NoClassDefFoundError ncdfe) {
            MessageHandler.errorln("Batik not in class path");
            return false;
        }
        catch (Exception e) {
            MessageHandler.errorln("Could not load external SVG: " +
                                   e.getMessage());
            // assuming any exception means this document is not svg
            // or could not be loaded for some reason
            return false;
        }
    }

    protected class MUserAgent implements UserAgent {
        AffineTransform currentTransform = null;

        /**
         * Creates a new SVGUserAgent.
         */
        protected MUserAgent(AffineTransform at) {
            currentTransform = at;
        }

        /**
         * Displays an error message.
         */
        public void displayError(String message) {
            System.err.println(message);
        }

        /**
         * Displays an error resulting from the specified Exception.
         */
        public void displayError(Exception ex) {
            ex.printStackTrace(System.err);
        }

        /**
         * Displays a message in the User Agent interface.
         * The given message is typically displayed in a status bar.
         */
        public void displayMessage(String message) {
            System.out.println(message);
        }

        /**
         * Returns a customized the pixel to mm factor.
         */
        public float getPixelToMM() {
            // this is set to 72dpi as the values in fo are 72dpi
            return 0.35277777777777777778f; // 72 dpi
            // return 0.26458333333333333333333333333333f;    // 96dpi
        }

        /**
         * Returns the language settings.
         */
        public String getLanguages() {
            return "en"; // userLanguages;
        }

        public String getMedia() {
            return "";
        }

        /**
         * Returns the user stylesheet uri.
         * @return null if no user style sheet was specified.
         */
        public String getUserStyleSheetURI() {
            return null; // userStyleSheetURI;
        }

        /**
         * Returns the class name of the XML parser.
         */
        public String getXMLParserClassName() {
            return org.apache.fop.apps.Driver.getParserClassName();
        }

        public boolean isXMLParserValidating() {
            return false;
        }

        /**
         * Opens a link in a new component.
         * @param doc The current document.
         * @param uri The document URI.
         */
        public void openLink(SVGAElement elt) {
        }

        public Point getClientAreaLocationOnScreen() {
            return new Point(0, 0);
        }

        public void setSVGCursor(java.awt.Cursor cursor) {}


        public AffineTransform getTransform() {
            return currentTransform;
        }

        public Dimension2D getViewportSize() {
            return new Dimension(100, 100);
        }

        public EventDispatcher getEventDispatcher() {
            return null;
        }

        public boolean supportExtension(String str) {
            return false;
        }

        public boolean hasFeature(String str) {
            return false;
        }

        public void registerExtension(BridgeExtension be) {}

        public void handleElement(Element elt, Object data) {}

    }

}

