/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image;

// Java
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.InputStream;

// FOP
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.fo.FOUserAgent;

/**
 * Base class to implement the FopImage interface.
 * @author Eric SCHAEFFER
 * @author Modified by Eric Dalquist - 9/14/2001 - ebdalqui@mtu.edu
 * @see FopImage
 */
public abstract class AbstractFopImage implements FopImage {
    /**
     * Keeps track of what has been loaded.
     */
    protected int loaded = 0;

    /**
     * Image width (in pixel).
     */
    protected int width = 0;

    /**
     * Image height (in pixel).
     */
    protected int height = 0;

    /**
     * Image input stream.
     */
    protected InputStream inputStream = null;

    /**
     * ImageReader object (to obtain image header informations).
     */
    protected FopImage.ImageInfo imageInfo = null;

    /**
     * Image color space (java.awt.color.ColorSpace).
     */
    protected ColorSpace colorSpace = null;

    /**
     * Bits per pixel.
     */
    protected int bitsPerPixel = 0;

    /**
     * Image data (uncompressed).
     */
    protected byte[] bitmaps = null;

    /**
     * Image data size.
     */
    protected int bitmapsSize = 0;

    /**
     * Image transparency.
     */
    protected boolean isTransparent = false;

    /**
     * Transparent color (org.apache.fop.pdf.PDFColor).
     */
    protected PDFColor transparentColor = null;

    /**
     * Constructor.
     * Construct a new FopImage object and initialize its default properties:
     * <UL>
     * <LI>image width
     * <LI>image height
     * </UL>
     * The image data isn't kept in memory.
     * @param info image information
     */
    public AbstractFopImage(FopImage.ImageInfo info) {
        this.inputStream = info.inputStream;
        this.imageInfo = info;
        if (this.imageInfo.width != -1) {
            width = imageInfo.width;
            height = imageInfo.height;
            loaded = loaded | DIMENSIONS;
        }
    }

    /**
     * Get the mime type for this image.
     *
     * @return the mime type for the image
     */
    public String getMimeType() {
        return imageInfo.mimeType;
    }

    /**
     * Load image data and initialize its properties.
     *
     * @param type the type of loading to do
     * @param ua the user agent for handling logging etc.
     * @return true if the loading was successful
     */
    public synchronized boolean load(int type, FOUserAgent ua) {
        if ((loaded & type) != 0) {
            return true;
        }
        boolean success = true;
        if (((type & DIMENSIONS) != 0) && ((loaded & DIMENSIONS) == 0)) {
            success = success && loadDimensions(ua);

            if (!success) {
                return false;
            }
            loaded = loaded | DIMENSIONS;
        }
        if (((type & BITMAP) != 0) && ((loaded & BITMAP) == 0)) {
            success = success && loadBitmap(ua);
            if (success) {
                loaded = loaded | BITMAP;
            }
        }
        if (((type & ORIGINAL_DATA) != 0) && ((loaded & ORIGINAL_DATA) == 0)) {
            success = success && loadOriginalData(ua);
            if (success) {
                loaded = loaded | ORIGINAL_DATA;
            }
        }
        return success;
    }

    /**
     * Load the dimensions of the image.
     * All implementations should override this to get and
     * return the dimensions.
     *
     * @param ua the user agent
     * @return true if the loading was successful
     */
    protected boolean loadDimensions(FOUserAgent ua) {
        return false;
    }

    /**
     * Load a bitmap array of the image.
     * If the renderer requires a bitmap image then the
     * implementations should override this to load the bitmap.
     * 
     * @param ua the user agent
     * @return true if the loading was successful
     */
    protected boolean loadBitmap(FOUserAgent ua) {
        return false;
    }

    /**
     * Load the original image data.
     * In some cases the original data can be used by the renderer.
     * This should load the data and any other associated information. 
     *
     * @param ua the user agent
     * @return true if the loading was successful
     */
    protected boolean loadOriginalData(FOUserAgent ua) {
        return false;
    }

    /**
     * Return the image width.
     * @return the image width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Return the image height.
     * @return the image height
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Return the image color space.
     * @return the image color space (java.awt.color.ColorSpace)
     */
    public ColorSpace getColorSpace() {
        return this.colorSpace;
    }

    /**
     * Get ICC profile for this image.
     * @return the icc profile or null if not applicable
     */
    public ICC_Profile getICCProfile() {
        return null;
    }

    /**
     * Return the number of bits per pixel.
     * @return number of bits per pixel
     */
    public int getBitsPerPixel() {
        return this.bitsPerPixel;
    }

    /**
     * Return the image transparency.
     * @return true if the image is transparent
     */
    public boolean isTransparent() {
        return this.isTransparent;
    }

    /**
     * Check if this image has a soft mask.
     *
     * @return true if the image also has a soft transparency mask
     */
    public boolean hasSoftMask() {
        return false;
    }

    /**
     * Get the soft mask.
     * The soft mask should have the same bitdepth as the image data.
     *
     * @return the data array of soft mask values
     */
    public byte[] getSoftMask() {
        return null;
    }

    /**
     * Return the transparent color.
     * @return the transparent color (org.apache.fop.pdf.PDFColor)
     */
    public PDFColor getTransparentColor() {
        return this.transparentColor;
    }

    /**
     * Return the image data (uncompressed).
     * @return the image data
     */
    public byte[] getBitmaps() {
        return this.bitmaps;
    }

    /**
     * Return the image data size (uncompressed).
     * @return the image data size
     */
    public int getBitmapsSize() {
        return this.bitmapsSize;
    }

    /**
     * Return the original image data (compressed).
     * @return the original image data
     */
    public byte[] getRessourceBytes() {
        return null;
    }

    /**
     * Return the original image data size (compressed).
     * @return the original image data size
     */
    public int getRessourceBytesSize() {
        return 0;
    }

}

