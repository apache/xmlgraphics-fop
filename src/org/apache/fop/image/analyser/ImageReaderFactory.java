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

// FOP
import org.apache.fop.image.FopImageException;

/**
 * Factory for ImageReader objects.
 * @author Pankaj Narula
 * @version 1.0
 */
public class ImageReaderFactory {
    static protected ArrayList formats = null;

    /**
     * ImageReader maker.
     * @param in image input stream
     * @return ImageReader object
     * @exception FopImageException  an error occured during creation or
     * image type is not supported
     */
    static public ImageReader Make(String uri,
                                   InputStream in) throws FopImageException {

        // need to use a config file and remove static methods
        formats = new ArrayList();
        formats.add(new JPEGReader());
        formats.add(new BMPReader());
        formats.add(new GIFReader());
        formats.add(new PNGReader());
        formats.add(new TIFFReader());
        formats.add(new EPSReader());
        formats.add(new SVGReader());
        //

        ImageReader reader;
        BufferedInputStream bis = new BufferedInputStream(in);
        try {
            for (int i = 0; i< formats.size(); i++) {
                reader = (ImageReader)formats.get(i);
                if (reader.verifySignature(uri, bis)) {
                    return reader;
                }
            }
        } catch (IOException ex) {
            throw new FopImageException(ex.getMessage());
        }
        return null;
    }

}

