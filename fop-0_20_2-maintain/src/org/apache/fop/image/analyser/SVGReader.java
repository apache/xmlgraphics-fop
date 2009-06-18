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
package org.apache.fop.image.analyser;

// Java
import java.io.BufferedInputStream;
import java.io.IOException;

import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.Dimension;

// DOM
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

// FOP
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.image.SVGImage;

//Batik
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.UnitProcessor;

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

