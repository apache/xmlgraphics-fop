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

/**
 * ImageReader object for SVG document image type.
 */
public class SVGReader extends AbstractImageReader {
    public boolean verifySignature(String uri, BufferedInputStream fis)
            throws IOException {
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
            // should check the stream contains text data
            SVGSVGElement svg = doc.getRootElement();
            this.width = (int)svg.getWidth().getBaseVal().getValue();
            this.height = (int)svg.getHeight().getBaseVal().getValue();
            return true;
        } catch (NoClassDefFoundError ncdfe) {
            MessageHandler.errorln("Batik not in class path");
            return false;
        } catch (Exception e) {
            MessageHandler.errorln("Could not load external SVG: "
                                   + e.getMessage());
            // assuming any exception means this document is not svg
            // or could not be loaded for some reason
            return false;
        }
    }

}

