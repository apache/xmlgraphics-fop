/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image.analyser;

// Java
import java.net.URL;
import java.util.List;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.IOException;

import java.awt.geom.AffineTransform;
import java.awt.Point;
import java.awt.geom.Dimension2D;
import java.awt.Dimension;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.w3c.dom.Element;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGSVGElement;

// FOP
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.image.SVGImage;

//Batik
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
//import org.apache.batik.dom.svg.*;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.UnitProcessor;
//import org.apache.batik.swing.svg.*;
//import org.apache.batik.swing.gvt.*;
//import org.apache.batik.gvt.U*;
//import org.apache.batik.gvt.renderer.*;
//import org.apache.batik.gvt.filter.*;
//import org.apache.batik.gvt.event.*;


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
            SVGDocument doc = (SVGDocument)factory.createDocument(uri, imageStream);

            UserAgent userAgent = new MUserAgent(new AffineTransform());
            BridgeContext ctx = new BridgeContext(userAgent);

            Element e = ((SVGDocument)doc).getRootElement();
            UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, e);

            String s;
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
        } catch (Exception e) {
            MessageHandler.errorln("Could not load external SVG: " +
                                   e.getMessage());
            // assuming any exception means this document is not svg
            // or could not be loaded for some reason
            return false;
        }
    }

    protected class MUserAgent extends UserAgentAdapter {
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
            MessageHandler.error(message);
        }

        /**
         * Displays an error resulting from the specified Exception.
         */
        public void displayError(Exception ex) {
            MessageHandler.error(org.apache.avalon.framework.ExceptionUtil.printStackTrace(ex));
        }

        /**
         * Displays a message in the User Agent interface.
         * The given message is typically displayed in a status bar.
         */
        public void displayMessage(String message) {
            MessageHandler.log(message);
        }

        /**
         * Returns a customized the pixel to mm factor.
         */
        public float getPixelToMM() {
            // this is set to 72dpi as the values in fo are 72dpi
            return 0.35277777777777777778f; // 72 dpi
            // return 0.26458333333333333333333333333333f;    // 96dpi
        }

        public float getPixelUnitToMillimeter() {
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
            return "print";
        }

        public boolean isXMLParserValidating() {
            return true;
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

        public AffineTransform getTransform() {
            return currentTransform;
        }

        public Dimension2D getViewportSize() {
            return new Dimension(100, 100);
        }

    }

}

