/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */

package org.apache.fop.image;

// Java
import java.net.URL;
import org.w3c.dom.svg.SVGDocument;

// FOP
import org.apache.fop.svg.SVGDriver;
import org.apache.fop.messaging.*;
import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.image.analyser.ImageReader;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

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
    public static XMLReader createParser() {
        String parserClassName = System.getProperty("org.xml.sax.parser");
        if (parserClassName == null) {
            parserClassName = "org.apache.xerces.parsers.SAXParser";
        }
        MessageHandler.logln("using SAX parser " + parserClassName);

        try {
            return (XMLReader) Class.forName(
                     parserClassName).newInstance();
        } catch (ClassNotFoundException e) {
            MessageHandler.errorln("Could not find " + parserClassName);
        } catch (InstantiationException e) {
            MessageHandler.errorln("Could not instantiate " +
                                   parserClassName);
        } catch (IllegalAccessException e) {
            MessageHandler.errorln("Could not access " + parserClassName);
        } catch (ClassCastException e) {
            MessageHandler.errorln(parserClassName + " is not a SAX driver");
        }
        return null;
    }

    protected void loadImage() throws FopImageException {
        try {
            SVGDriver driver = new SVGDriver();
            driver.addElementMapping("org.apache.fop.svg.SVGElementMapping");
            driver.addPropertyList("org.apache.fop.svg.SVGPropertyListMapping");
            XMLReader parser = createParser();
            driver.buildSVGTree(parser,
                                new InputSource(this.m_href.toString()));
            doc = driver.getSVGDocument();
        } catch (Exception e) {
            MessageHandler.errorln("ERROR LOADING EXTERNAL SVG: " +
                                   e.getMessage());
        }
    }

    public SVGDocument getSVGDocument() throws FopImageException {
        if (doc == null)
            this.loadImage();
        return doc;
    }
}
