/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image;

import java.io.InputStream;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;

import org.apache.fop.pdf.PDFColor;
import org.apache.fop.fo.FOUserAgent;

/**
 * Fop image interface for loading images.
 *
 * @author Eric SCHAEFFER
 */
public interface FopImage {
    /**
     * Flag for loading dimensions.
     */
    public static final int DIMENSIONS = 1;

    /**
     * Flag for loading original data.
     */
    public static final int ORIGINAL_DATA = 2;

    /**
     * Flag for loading bitmap data.
     */
    public static final int BITMAP = 4;

    /**
     * Get the mime type of this image.
     * This is used so that when reading from the image it knows
     * what type of image it is.
     *
     * @return the mime type string
     */
    public String getMimeType();

    /**
     * Load particular inforamtion for this image
     * This must be called before attempting to get
     * the information.
     *
     * @param type the type of loading required
     * @param ua the user agent
     * @return boolean true if the information could be loaded
     */
    public boolean load(int type, FOUserAgent ua);

    // image size
    public int getWidth();
    public int getHeight();

    public ColorSpace getColorSpace();
    public ICC_Profile getICCProfile();

    // bits per pixel
    public int getBitsPerPixel();

    // For transparent images
    public boolean isTransparent();
    public PDFColor getTransparentColor();
    public boolean hasSoftMask();
    public byte[] getSoftMask();

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

    /**
     * Image info class.
     * Information loaded from analyser and passed to image object.
     */
    public static class ImageInfo {
        public InputStream inputStream;
        public int width;
        public int height;
        public Object data;
        public String mimeType;
        public String str;
    }

}

