/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image;

// Java
import java.net.URL;
import org.w3c.dom.svg.SVGDocument;

// FOP
import org.apache.fop.apps.Driver;
import org.apache.fop.messaging.*;
import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.image.analyser.ImageReader;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;

/**
 * @see AbstractFopImage
 * @see FopImage
 */
public class SVGImage extends AbstractFopImage {
    SVGDocument doc;

    public SVGImage(URL href) throws FopImageException {
        super(href);
    }

    public SVGImage(URL href,
                    ImageReader imgReader) throws FopImageException {
        super(href, imgReader);
    }

    /**
     * creates a SAX parser, using the value of org.xml.sax.parser
     * defaulting to org.apache.xerces.parsers.SAXParser
     *
     * @return the created SAX parser
     */
    public static String getParserName() {
        String parserClassName = Driver.getParserClassName();
        return parserClassName;
    }

    protected void loadImage() throws FopImageException {
        try {
            SAXSVGDocumentFactory factory =
                new SAXSVGDocumentFactory(SVGImage.getParserName());
            doc = factory.createDocument(this.m_href.toExternalForm());
        } catch (Exception e) {
            MessageHandler.errorln("Could not load external SVG: "
                                   + e.getMessage());
        }
    }

    public SVGDocument getSVGDocument() throws FopImageException {
        if (doc == null)
            this.loadImage();
        return doc;
    }

}
