/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.image.analyser;

// Java
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Map;

// XML
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

// FOP
import org.apache.fop.image.FopImage;
import org.apache.fop.fo.FOUserAgent;

/** ImageReader object for XML document image type. */
public class XMLReader implements ImageReader {

    private static Map converters = new java.util.HashMap();

    /**
     * Registers a Converter implementation with XMLReader.
     *
     * @param ns    The namespace to associate with this converter
     * @param conv  The actual Converter implementation
     */
    public static void setConverter(String ns, Converter conv) {
        converters.put(ns, conv);
    }

    /** @see org.apache.fop.image.analyser.ImageReader */
    public FopImage.ImageInfo verifySignature(String uri, BufferedInputStream fis,
            FOUserAgent ua)
        throws IOException {
        return loadImage(uri, fis, ua);
    }

    /**
     * Returns the MIME type supported by this implementation.
     *
     * @return   The MIME type
     */
    public String getMimeType() {
        return "text/xml";
    }

    /**
     * Creates an ImageInfo object from an XML image read from a stream.
     *
     * @todo This means the external svg document will be loaded twice. Possibly need
     * a slightly different design for the image stuff.
     *
     * @param uri  The URI to the image
     * @param bis  The InputStream
     * @param ua   The user agent
     * @return     An ImageInfo object describing the image
     */
    protected FopImage.ImageInfo loadImage(String uri, BufferedInputStream bis,
            FOUserAgent ua) {
        return createDocument(bis, ua);
    }

    /**
     * Creates an ImageInfo object from an XML image read from a stream.
     *
     * @param is  The InputStream
     * @param ua  The user agent
     * @return    An ImageInfo object describing the image
     */
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
            ua.getLogger().debug("XML image namespace: " + root.getAttribute("xmlns"));
            String ns = root.getAttribute("xmlns");
            info.str = ns;

            Converter conv = (Converter) converters.get(ns);
            if (conv != null) {
                FopImage.ImageInfo i = conv.convert(doc);
                if (i != null) {
                    info = i;
                }
            }
        } catch (Exception e) {
            ua.getLogger().warn("Error while constructing image from XML", e);
            try {
                is.reset();
            } catch (IOException ioe) {
                // throw the original exception, not this one
            }
            return null;
        }
        return info;
    }

    /**
     * This interface is to be implemented for XML to image converters.
     */
    public static interface Converter {

        /**
         * This method is called for a DOM document to be converted into an
         * ImageInfo object.
         *
         * @param doc   The DOM document to convert
         * @return      An ImageInfo object describing the image
         */
        FopImage.ImageInfo convert(Document doc);
    }

}

