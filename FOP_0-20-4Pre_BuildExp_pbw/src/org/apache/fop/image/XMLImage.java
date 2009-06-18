/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image;

// Java
import java.net.URL;
import org.w3c.dom.Document;

// FOP
import org.apache.fop.apps.Driver;
import org.apache.fop.image.analyser.ImageReader;
import org.apache.fop.image.analyser.SVGReader;
import org.apache.fop.fo.FOUserAgent;

/**
 * @see AbstractFopImage
 * @see FopImage
 */
public class XMLImage extends AbstractFopImage {
    Document doc;
    String ns = "";

    public XMLImage(URL href, FopImage.ImageInfo imgInfo) {
        super(href, imgInfo);
        if(imgInfo.data instanceof Document) {
            doc = (Document)imgInfo.data;
        }
        ns = imgInfo.str;
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
        return true;
    }

    public Document getDocument() {
        return doc;
    }

    public String getNameSpace() {
        return ns;
    }
}
