/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image;

// FOP
import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFFilter;
import org.apache.fop.image.analyser.ImageReaderFactory;
import org.apache.fop.image.analyser.ImageReader;

// Java
import java.net.URL;

/**
 * Base class to implement the FopImage interface.
 * @author Eric Schaeffer
 * @author <a href="mailto:ebdalqui@mtu.edu">Eric Dalquist</a>
 * @see FopImage
 */
public abstract class AbstractFopImage implements FopImage {

    /**
    * Photoshop generated cmykl jpeg's are inverted.
    */
    protected boolean m_invertImage = false;

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
    protected ImageReader m_imageReader = null;

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
     * @return a new FopImage object
     * @exception FopImageException an error occured during initialization
     */
    public AbstractFopImage(URL href) throws FopImageException {
        this.m_href = href;
        try {
            this.m_imageReader =
                ImageReaderFactory.Make(this.m_href.toExternalForm(),
                                        this.m_href.openStream());
        } catch (Exception e) {
            throw new FopImageException(e.getMessage());
        }
        this.m_width = this.m_imageReader.getWidth();
        this.m_height = this.m_imageReader.getHeight();
    }

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
     * @exception FopImageException an error occured during initialization
     */
    public AbstractFopImage(URL href,
                            ImageReader imgReader) throws FopImageException {
        this.m_href = href;
        this.m_imageReader = imgReader;
        this.m_width = this.m_imageReader.getWidth();
        this.m_height = this.m_imageReader.getHeight();
    }

    /**
     * Load image data and initialize its properties.
     * Subclasses need to implement this method.
     * @exception FopImageException an error occured during loading
     */
    abstract protected void loadImage() throws FopImageException;

    /**
    * If true, image data are inverted
    */
    public boolean invertImage() {
        return m_invertImage;
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
     * @exception FopImageException an error occured during property retriaval
     */
    public int getWidth() throws FopImageException {
        if (this.m_width == 0)
            this.loadImage();

        return this.m_width;
    }

    /**
     * Return the image height.
     * @return the image height
     * @exception FopImageException an error occured during property retriaval
     */
    public int getHeight() throws FopImageException {
        if (this.m_height == 0)
            this.loadImage();

        return this.m_height;
    }

    /**
     * Return the image color space.
     * @return the image color space (org.apache.fop.datatypes.ColorSpace)
     * @exception FopImageException an error occured during property retriaval
     */
    public ColorSpace getColorSpace() throws FopImageException {
        if (this.m_colorSpace == null)
            this.loadImage();

        return this.m_colorSpace;
    }

    /**
     * Return the number of bits per pixel.
     * @return number of bits per pixel
     * @exception FopImageException an error occured during property retriaval
     */
    public int getBitsPerPixel() throws FopImageException {
        if (this.m_bitsPerPixel == 0)
            this.loadImage();

        return this.m_bitsPerPixel;
    }

    /**
     * Return the image transparency.
     * @return true if the image is transparent
     * @exception FopImageException an error occured during property retriaval
     */
    public boolean isTransparent() throws FopImageException {
        return this.m_isTransparent;
    }

    /**
     * Return the transparent color.
     * @return the transparent color (org.apache.fop.pdf.PDFColor)
     * @exception FopImageException an error occured during property retriaval
     */
    public PDFColor getTransparentColor() throws FopImageException {
        return this.m_transparentColor;
    }

    /**
     * Return the image data (uncompressed).
     * @return the image data
     * @exception FopImageException an error occured during loading
     */
    public byte[] getBitmaps() throws FopImageException {
        if (this.m_bitmaps == null)
            this.loadImage();

        return this.m_bitmaps;
    }

    /**
     * Return the image data size (uncompressed).
     * @return the image data size
     * @exception FopImageException an error occured during loading
     */
    public int getBitmapsSize() throws FopImageException {
        if (this.m_bitmapsSize == 0)
            this.loadImage();

        return this.m_bitmapsSize;
    }

    /**
     * Return the original image data (compressed).
     * @return the original image data
     * @exception FopImageException an error occured during loading
     */
    public byte[] getRessourceBytes() throws FopImageException {
        return null;
    }

    /**
     * Return the original image data size (compressed).
     * @return the original image data size
     * @exception FopImageException an error occured during loading
     */
    public int getRessourceBytesSize() throws FopImageException {
        return 0;
    }

    /**
     * Return the original image compression type.
     * @return the original image compression type (org.apache.fop.pdf.PDFFilter)
     * @exception FopImageException an error occured during loading
     */
    public PDFFilter getPDFFilter() throws FopImageException {

        /*
         * Added by Eric Dalquist
         * Using the bitsPerPixel var as our flag since many imges will
         * have a null m_compressionType even after being loaded
         */
        if (this.m_bitsPerPixel == 0)
            this.loadImage();

        return m_compressionType;
    }

    /**
     * Free all ressource.
     */
    public void close() {
        /*
         * For the moment, only release the bitmaps (image areas
         * can share the same FopImage object)
         * Thus, even if it had been called, other properties
         * are still available.
         */
        // this.m_width = 0;
        // this.m_height = 0;
        // this.m_href = null;
        // this.m_colorSpace = null;
        // this.m_bitsPerPixel = 0;
        this.m_bitmaps = null;
        this.m_bitmapsSize = 0;
        // this.m_isTransparent = false;
        // this.m_transparentColor = null;
    }

}

