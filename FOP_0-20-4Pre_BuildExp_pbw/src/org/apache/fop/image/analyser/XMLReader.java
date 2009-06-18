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

import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;
import org.w3c.dom.DOMImplementation;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import org.apache.fop.image.FopImage;
import org.apache.fop.fo.FOUserAgent;
import org.apache.fop.image.XMLImage;

/**
 * ImageReader object for XML document image type.
 */
public class XMLReader implements ImageReader {
    private static HashMap converters = new HashMap();

    public static void setConverter(String ns, Converter conv) {
        converters.put(ns, conv);
    }

    public XMLReader() {
    }

    public FopImage.ImageInfo verifySignature(String uri, BufferedInputStream fis,
                                   FOUserAgent ua) throws IOException {
        return loadImage(uri, fis, ua);
    }

    public String getMimeType() {
        return "text/xml";
    }

    /**
     * This means the external svg document will be loaded twice.
     * Possibly need a slightly different design for the image stuff.
     */
    protected FopImage.ImageInfo loadImage(String uri, BufferedInputStream fis,
                                   FOUserAgent ua) {
        return createDocument(fis, ua);
    }

    public FopImage.ImageInfo createDocument(BufferedInputStream is, FOUserAgent ua) {
        Document doc = null;
        FopImage.ImageInfo info = new FopImage.ImageInfo();
        info.mimeType = getMimeType();

        try {
            int length = is.available();
            is.mark(length);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            doc = dbf.newDocumentBuilder().parse(is);
            info.data = doc;

            Element root = doc.getDocumentElement();
            ua.getLogger().debug("ns:" + root.getAttribute("xmlns"));
            String ns = root.getAttribute("xmlns");
            info.str = ns;

            Converter conv = (Converter)converters.get(ns);
            if(conv != null) {
                FopImage.ImageInfo i = conv.convert(doc);
                if(i != null) {
                    info = i;
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            try {
                is.reset();
            } catch (IOException ioe) { }
            return null;
        }
        return info;
    }

    public static interface Converter {
        public FopImage.ImageInfo convert(Document doc);
    }
}

