/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image;

// Java
import java.net.URL;

// FOP
import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFFilter;
import org.apache.fop.image.analyser.ImageReaderFactory;
import org.apache.fop.image.analyser.ImageReader;
import org.apache.fop.fo.FOUserAgent;

/**
 * Base class to implement the FopImage interface.
 * @author Eric SCHAEFFER
 * @author Modified by Eric Dalquist - 9/14/2001 - ebdalqui@mtu.edu
 * @see FopImage
 */
public abstract class AbstractFopImage implements FopImage {
    protected int loaded = 0;

    /**
     * Image width (in pixel).
     */
    protected int m_width = 0;

    /**
     * Image height (in pixel).
     */
    protected int m_height = 0;

    /**
     * Image URL.
     */
    protected URL m_href = null;

    /**
     * ImageReader object (to obtain image header informations).
     */
    protected FopImage.ImageInfo imageInfo = null;

    /**
     * Image color space (org.apache.fop.datatypes.ColorSpace).
     */
    protected ColorSpace m_colorSpace = null;

    /**
     * Bits per pixel.
     */
    protected int m_bitsPerPixel = 0;

    /**
     * Image data (uncompressed).
     */
    protected byte[] m_bitmaps = null;

    /**
     * Image data size.
     */
    protected int m_bitmapsSize = 0;

    /**
     * Image transparency.
     */
    protected boolean m_isTransparent = false;

    /**
     * Transparent color (org.apache.fop.pdf.PDFColor).
     */
    protected PDFColor m_transparentColor = null;

    /**
     * Image compression type.
     * Added by Eric Dalquist
     */
    protected PDFFilter m_compressionType = null;

    /**
     * Constructor.
     * Construct a new FopImage object and initialize its default properties:
     * <UL>
     * <LI>image width
     * <LI>image height
     * </UL>
     * The image data isn't kept in memory.
     * @param href image URL
     * imgReader ImageReader object
     * @return a new FopImage object
     */
    public AbstractFopImage(URL href, FopImage.ImageInfo info) {
        this.m_href = href;
        this.imageInfo = info;
        this.m_width = this.imageInfo.width;
        this.m_height = this.imageInfo.height;
        loaded = loaded | DIMENSIONS;
    }

    public String getMimeType() {
        return imageInfo.mimeType;
    }

    /**
     * Load image data and initialize its properties.
     */
    public synchronized boolean load(int type, FOUserAgent ua) {
        if((loaded & type) != 0) {
            return true;
        }
        boolean success = true;
        if(((type & DIMENSIONS) != 0) && ((loaded & DIMENSIONS) == 0)) {
            success = success && loadDimensions(ua);

            if(!success) {
                return false;
            }
            loaded = loaded | DIMENSIONS;
        }
        if(((type & BITMAP) != 0) && ((loaded & BITMAP) == 0)) {
            success = success && loadBitmap(ua);
            if(success) {
                loaded = loaded | BITMAP;
            }
        }
        return success;
    }

    protected boolean loadDimensions(FOUserAgent ua) {
        return false;
    }

    protected boolean loadBitmap(FOUserAgent ua) {
        return false;
    }

    /**
     * Return the image URL.
     * @return the image URL (as String)
     */
    public String getURL() {
        return this.m_href.toString();
    }

    /**
     * Return the image width.
     * @return the image width
     */
    public int getWidth() {
        return this.m_width;
    }

    /**
     * Return the image height.
     * @return the image height
     */
    public int getHeight() {
        return this.m_height;
    }

    /**
     * Return the image color space.
     * @return the image color space (org.apache.fop.datatypes.ColorSpace)
     */
    public ColorSpace getColorSpace() {
        return this.m_colorSpace;
    }

    /**
     * Return the number of bits per pixel.
     * @return number of bits per pixel
     */
    public int getBitsPerPixel() {
        return this.m_bitsPerPixel;
    }

    /**
     * Return the image transparency.
     * @return true if the image is transparent
     */
    public boolean isTransparent() {
        return this.m_isTransparent;
    }

    /**
     * Return the transparent color.
     * @return the transparent color (org.apache.fop.pdf.PDFColor)
     */
    public PDFColor getTransparentColor() {
        return this.m_transparentColor;
    }

    /**
     * Return the image data (uncompressed).
     * @return the image data
     */
    public byte[] getBitmaps() {
        return this.m_bitmaps;
    }

    /**
     * Return the image data size (uncompressed).
     * @return the image data size
     */
    public int getBitmapsSize() {
        return this.m_bitmapsSize;
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

    /**
     * Return the original image compression type.
     * @return the original image compression type (org.apache.fop.pdf.PDFFilter)
     */
    public PDFFilter getPDFFilter() {

        /*
         * Added by Eric Dalquist
         * Using the bitsPerPixel var as our flag since many imges will
         * have a null m_compressionType even after being loaded
         */
        return m_compressionType;
    }

}

