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
    protected abstract void loadImage() throws FopImageException;


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
        synchronized(this) {
            if (this.m_width == 0)
                this.loadImage();
        }

        return this.m_width;
    }

    /**
     * Return the image height.
     * @return the image height
     * @exception FopImageException an error occured during property retriaval
     */
    public int getHeight() throws FopImageException {
        synchronized(this) {
            if (this.m_height == 0)
                this.loadImage();
        }

        return this.m_height;
    }

    /**
     * Return the image color space.
     * @return the image color space (org.apache.fop.datatypes.ColorSpace)
     * @exception FopImageException an error occured during property retriaval
     */
    public ColorSpace getColorSpace() throws FopImageException {
        synchronized(this) {
            if (this.m_colorSpace == null)
                this.loadImage();
        }

        return this.m_colorSpace;
    }

    /**
     * Return the number of bits per pixel.
     * @return number of bits per pixel
     * @exception FopImageException an error occured during property retriaval
     */
    public int getBitsPerPixel() throws FopImageException {
        synchronized(this) {
            if (this.m_bitsPerPixel == 0)
                this.loadImage();
        }

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
        synchronized(this) {
            if (this.m_bitmaps == null)
                this.loadImage();
        }

        return this.m_bitmaps;
    }

    /**
     * Return the image data size (uncompressed).
     * @return the image data size
     * @exception FopImageException an error occured during loading
     */
    public int getBitmapsSize() throws FopImageException {
        synchronized(this) {
            if (this.m_bitmapsSize == 0)
                this.loadImage();
        }

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
        synchronized(this) {
            if (this.m_bitsPerPixel == 0)
                this.loadImage();
        }

        return m_compressionType;
    }

    /**
     * Free all ressource.
     */
    public void close() {
        //org.apache.fop.messaging.MessageHandler.debug(getClass().getName()+".close(): "+this.m_href);
        /*
         * For the moment, only release the bitmaps (image areas
         * can share the same FopImage object)
         * Thus, even if it had been called, other properties
         * are still available.
         */
        synchronized(this) {
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

}

