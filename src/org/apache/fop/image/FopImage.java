/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

// Author:       Eric SCHAEFFER
// Description:  represent an image object

package org.apache.fop.image;

import java.io.InputStream;

import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFFilter;
import org.apache.fop.fo.FOUserAgent;

public interface FopImage {
    public static final int DIMENSIONS = 1;
    public static final int ORIGINAL_DATA = 2;
    public static final int BITMAP = 4;

    public String getMimeType();

    /**
     * Load particular inforamtion for this image
     * This must be called before attempting to get
     * the information.
     * @return boolean true if the information could be loaded
     */
    public boolean load(int type, FOUserAgent ua);

    // Ressource location
    public String getURL();

    // image size
    public int getWidth();
    public int getHeight();

    // DeviceGray, DeviceRGB, or DeviceCMYK
    public ColorSpace getColorSpace();

    // bits per pixel
    public int getBitsPerPixel();

    // For transparent images
    public boolean isTransparent();
    public PDFColor getTransparentColor();

    // get the image bytes, and bytes properties

    // get uncompressed image bytes
    public byte[] getBitmaps();
    // width * (bitsPerPixel / 8) * height, no ?
    public int getBitmapsSize();

    // get compressed image bytes
    // I don't know if we really need it, nor if it
    // should be changed...
    public byte[] getRessourceBytes();
    public int getRessourceBytesSize();
    // return null if no corresponding PDFFilter
    public PDFFilter getPDFFilter();

    public static class ImageInfo {
        public InputStream stream;
        public int width;
        public int height;
        public Object data;
        public String mimeType;
        public String str;
    }

}

