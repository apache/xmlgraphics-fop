/*
 * $Id: XMLReader.java,v 1.6 2003/03/06 21:25:45 jeremias Exp $
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
import java.io.InputStream;
import java.io.IOException;
import java.util.Map;

// XML
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

// FOP
import org.apache.fop.image.FopImage;
import org.apache.fop.apps.FOUserAgent;

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
    public FopImage.ImageInfo verifySignature(String uri, InputStream fis,
            FOUserAgent ua)
        throws IOException {
        FopImage.ImageInfo info = loadImage(uri, fis, ua);
        if (info != null) {
            try {
                fis.close();
            } catch (Exception e) {
                //ignore
            }
        }
        return info;
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
     * (todo) This means the external svg document will be loaded twice. Possibly need
     * a slightly different design for the image stuff.
     *
     * @param uri  The URI to the image
     * @param bis  The InputStream
     * @param ua   The user agent
     * @return     An ImageInfo object describing the image
     */
    protected FopImage.ImageInfo loadImage(String uri, InputStream bis,
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
    public FopImage.ImageInfo createDocument(InputStream is, FOUserAgent ua) {
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

