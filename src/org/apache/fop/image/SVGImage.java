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
import org.apache.fop.image.analyser.ImageReader;
import org.apache.fop.image.analyser.SVGReader;
import org.apache.fop.fo.FOUserAgent;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;

/**
 * @see AbstractFopImage
 * @see FopImage
 */
public class SVGImage extends AbstractFopImage {
    SVGDocument doc;

    public SVGImage(URL href, FopImage.ImageInfo imgInfo) {
        super(href, imgInfo);
        if(imgInfo.data instanceof SVGDocument) {
            doc = (SVGDocument)imgInfo.data;
        }
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

    protected boolean loadData(FOUserAgent ua) {
        try {
            SAXSVGDocumentFactory factory =
              new SAXSVGDocumentFactory(SVGImage.getParserName());
            doc = factory.createDocument(this.m_href.toExternalForm());
        } catch (Exception e) {
            ua.getLogger().error("Could not load external SVG: "
                                   + e.getMessage(), e);
            return false;
        }
        return true;
    }

    public SVGDocument getSVGDocument() {
        return doc;
    }

}
