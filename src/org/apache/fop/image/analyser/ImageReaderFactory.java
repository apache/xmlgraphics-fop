/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.image.analyser;

// Java
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;

// FOP
import org.apache.fop.image.FopImage;
import org.apache.fop.fo.FOUserAgent;

/**
 * Factory for ImageReader objects.
 *
 * @author    Pankaj Narula
 * @version   $Id$
 */
public class ImageReaderFactory {

    private static ArrayList formats = new ArrayList();

    static {
        registerFormat(new JPEGReader());
        registerFormat(new BMPReader());
        registerFormat(new GIFReader());
        registerFormat(new PNGReader());
        registerFormat(new TIFFReader());
        registerFormat(new EPSReader());
        // the xml parser through batik closes the stream when finished
        // so there is a workaround in the SVGReader
        registerFormat(new SVGReader());
        registerFormat(new XMLReader());
    }

    /**
     * Registers a new ImageReader.
     *
     * @param reader  An ImageReader instance
     */
    public static void registerFormat(ImageReader reader) {
        formats.add(reader);
    }

    /**
     * ImageReader maker.
     *
     * @param uri  URI to the image
     * @param in   image input stream
     * @param ua   user agent
     * @return     An ImageInfo object describing the image
     */
    public static FopImage.ImageInfo make(String uri, InputStream in,
            FOUserAgent ua) {

        ImageReader reader;
        try {
            for (int count = 0; count < formats.size(); count++) {
                reader = (ImageReader) formats.get(count);
                FopImage.ImageInfo info = reader.verifySignature(uri, in, ua);
                if (info != null) {
                    return info;
                }
            }
        } catch (IOException ex) {
            ua.getLogger().error(
                    "Error while recovering Image Informations ("
                    + uri + ")", ex);
        }
        return null;
    }

}

