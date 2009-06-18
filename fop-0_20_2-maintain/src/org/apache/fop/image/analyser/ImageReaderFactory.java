/*
 * $Id$
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
    protected static List formats = null;

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
    public static ImageReader Make(String uri,
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

