/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */

package org.apache.fop.image.analyser;

// Java
import java.io.BufferedInputStream;
import java.io.IOException;

import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

// FOP
import org.apache.fop.svg.SVGDriver;
import org.apache.fop.messaging.*;
import org.apache.fop.image.SVGImage;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * ImageReader object for SVG document image type.
 */
public class SVGReader extends AbstractImageReader {

    public boolean verifySignature(BufferedInputStream fis)
    throws IOException {
        this.imageStream = fis;
        return loadImage();
    }

    public String getMimeType() {
        return "image/svg-xml";
    }

    /**
     * This means the external svg document will be loaded twice.
     * Possibly need a slightly different design for the image stuff.
     */
    protected boolean loadImage() {
        // parse document and get the size attributes of the svg element
        try {
            // should check the stream contains text data
            SVGDriver driver = new SVGDriver();
            driver.addElementMapping("org.apache.fop.svg.SVGElementMapping");
            driver.addPropertyList("org.apache.fop.svg.SVGPropertyListMapping");
            XMLReader parser = SVGImage.createParser();
            driver.buildSVGTree(parser, new InputSource(this.imageStream));
            SVGDocument doc = driver.getSVGDocument();
            SVGSVGElement svg = doc.getRootElement();
            this.width =
              (int) svg.getWidth().getBaseVal().getValue() * 1000;
            this.height =
              (int) svg.getHeight().getBaseVal().getValue() * 1000;
            return true;
        } catch (Exception e) {
            MessageHandler.errorln("ERROR LOADING EXTERNAL SVG: " + e.getMessage());
            // assuming any exception means this document is not svg
            // or could not be loaded for some reason
            return false;
        }
    }

}

