/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */
 
package org.apache.fop.image.analyser;

// Java
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;

// FOP
import org.apache.fop.image.FopImage;
import org.apache.fop.apps.FOUserAgent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Factory for ImageReader objects.
 *
 * @author    Pankaj Narula
 * @version   $Id: ImageReaderFactory.java,v 1.13 2003/03/06 21:25:45 jeremias Exp $
 */
public class ImageReaderFactory {

    private static ArrayList formats = new ArrayList();

    protected static Log log = LogFactory.getLog(ImageReaderFactory.class);

    static {
        registerFormat(new JPEGReader());
        registerFormat(new BMPReader());
        registerFormat(new GIFReader());
        registerFormat(new PNGReader());
        registerFormat(new TIFFReader());
        registerFormat(new EPSReader());
        registerFormat(new EMFReader());
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
            log.error("Error while recovering Image Informations ("
                + uri + ")", ex);
        }
        return null;
    }

}

