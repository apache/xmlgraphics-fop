/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image.analyser;

// Java
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.List;

// FOP
import org.apache.fop.image.FopImageException;

/**
 * Factory for ImageReader objects.
 * @author Pankaj Narula
 */
public class ImageReaderFactory {
    static protected List formats = null;

    static {
        /**@todo  make configurable one day...*/
        formats = new java.util.Vector();
        try {
            registerImageReader("org.apache.fop.image.analyser.JPEGReader");
            registerImageReader("org.apache.fop.image.analyser.BMPReader");
            registerImageReader("org.apache.fop.image.analyser.GIFReader");
            registerImageReader("org.apache.fop.image.analyser.PNGReader");
            registerImageReader("org.apache.fop.image.analyser.TIFFReader");
            registerImageReader("org.apache.fop.image.analyser.EPSReader");
            registerImageReader("org.apache.fop.image.analyser.SVGReader");
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("One of the default ImageReader implementations is not available: "+cnfe.getMessage());
        }
    }

    /**
     * Registers a new ImageReader implementation.
     *
     * @param classname The fully qualified classname
     * @throws ClassNotFoundException In case the given class cannot be found
     */
    public static void registerImageReader(String classname) throws ClassNotFoundException {
        Class clazz = Class.forName(classname);
        registerImageReader(clazz);
    }


    /**
     * Registers a new ImageReader implementation.
     *
     * @param clazz The ImageReader implementation class.
     */
    public static void registerImageReader(Class clazz) {
        if (!ImageReader.class.isAssignableFrom(clazz)) throw new RuntimeException("This class does not implement the ImageReader interface: "+clazz.getName());
        formats.add(clazz);
    }

    /**
     * ImageReader maker.
     *
     * @param uri URI of the image
     * @param in image input stream
     * @return ImageReader object
     * @exception FopImageException  an error occured during creation or
     * image type is not supported
     */
    static public ImageReader Make(String uri,
                                   InputStream in) throws FopImageException {

        ImageReader reader;
        BufferedInputStream bis = new BufferedInputStream(in);
        try {
            for (int i = 0; i< formats.size(); i++) {
                Class clazz = (Class)formats.get(i);
                try {
                    reader = (ImageReader)clazz.newInstance();
                } catch (Exception e) {
                    throw new FopImageException("ImageReader implementation cannot be instantiated: "+e.getMessage());
                }
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

