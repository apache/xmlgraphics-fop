/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image.analyser;

// Java
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.fop.fo.FOUserAgent;

/**
 * Factory for ImageReader objects.
 * @author Pankaj Narula
 * @version 1.0
 */
public class ImageReaderFactory {
    static protected ArrayList formats = new ArrayList();
    static {
        formats.add(new JPEGReader());
        formats.add(new BMPReader());
        formats.add(new GIFReader());
        formats.add(new PNGReader());
        formats.add(new TIFFReader());
        formats.add(new EPSReader());
        formats.add(new SVGReader());
    };

    // TODO - a way to add other readers

    /**
     * ImageReader maker.
     * @param in image input stream
     * @return ImageReader object
     * image type is not supported
     */
    static public ImageReader make(String uri, InputStream in,
                                   FOUserAgent ua) {

        ImageReader reader;
        BufferedInputStream bis = new BufferedInputStream(in);
        try {
            for (int count = 0; count < formats.size(); count++) {
                reader = (ImageReader) formats.get(count);
                if (reader.verifySignature(uri, bis, ua)) {
                    return reader;
                }
            }
        } catch (IOException ex) {
            ua.getLogger().error(
              "Error while recovering Image Informations (" +
              uri + ") : " + ex.getMessage());
        }
        return null;
    }

}

