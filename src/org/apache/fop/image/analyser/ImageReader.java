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

// FOP
import org.apache.fop.image.FopImage;
import org.apache.fop.fo.FOUserAgent;

/**
 * ImageReader objects read image headers to determine the image size.
 *
 * @author    Pankaj Narula
 * @version   $Id$
 */
public interface ImageReader {

    /**
     * Verify image type. If the stream does not contain image data expected by
     * the reader it must reset the stream to the start. This is so that the
     * next reader can start reading from the start. The reader must not close
     * the stream unless it can handle the image and it has read the
     * information.
     *
     * @param bis              Image buffered input stream
     * @param uri              URI to the image
     * @param ua               The user agent
     * @return                 <code>true</code> if image type is the handled one
     * @exception IOException  if an I/O error occurs
     */
    FopImage.ImageInfo verifySignature(String uri, InputStream bis,
            FOUserAgent ua)
        throws IOException;

}

