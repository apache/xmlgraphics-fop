/*
 * $Id: BitmapImage.java,v 1.5 2003/03/07 08:25:47 jeremias Exp $
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
package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
//import java.util.Map;

/**
 * Bitmap image.
 * This is used to create a bitmap image that will be inserted
 * into pdf.
 */
public class BitmapImage implements PDFImage {
    private int height;
    private int width;
    private int bitsPerPixel;
    private PDFColorSpace colorSpace;
    private byte[] bitmaps;
    private String maskRef;
    private PDFColor transparent = null;
    private String key;
    private PDFDocument pdfDoc;

    /**
     * Create a bitmap image.
     * Creates a new bitmap image with the given data.
     *
     * @param k the key to be used to lookup the image
     * @param width the width of the image
     * @param height the height of the image
     * @param data the bitmap data
     * @param mask the transparancy mask reference if any
     */
    public BitmapImage(String k, int width, int height, byte[] data,
                  String mask) {
        this.key = k;
        this.height = height;
        this.width = width;
        this.bitsPerPixel = 8;
        this.colorSpace = new PDFColorSpace(PDFColorSpace.DEVICE_RGB);
        this.bitmaps = data;
        maskRef = mask;
    }

    /**
     * Setup this image with the pdf document.
     *
     * @param doc the pdf document this will be inserted into
     */
    public void setup(PDFDocument doc) {
        this.pdfDoc = doc;
    }

    /**
     * Get the key for this image.
     * This key is used by the pdf document so that it will only
     * insert an image once. All other references to the same image
     * will use the same XObject reference.
     *
     * @return the unique key to identify this image
     */
    public String getKey() {
        return key;
    }

    /**
     * Get the width of this image.
     *
     * @return the width of the image
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the height of this image.
     *
     * @return the height of the image
     */
    public int getHeight() {
        return height;
    }

    /**
     * Set the color space for this image.
     *
     * @param cs the pdf color space
     */
    public void setColorSpace(PDFColorSpace cs) {
        colorSpace = cs;
    }

    /**
     * Get the color space for the image data.
     * Possible options are: DeviceGray, DeviceRGB, or DeviceCMYK
     *
     * @return the pdf doclor space
     */
    public PDFColorSpace getColorSpace() {
        return colorSpace;
    }

    /**
     * Get the number of bits per pixel.
     *
     * @return the number of bits per pixel
     */
    public int getBitsPerPixel() {
        return bitsPerPixel;
    }

    /**
     * Set the transparent color for this iamge.
     *
     * @param t the transparent color
     */
    public void setTransparent(PDFColor t) {
        transparent = t;
    }

    /**
     * Check if this image has a transparent color.
     *
     * @return true if it has a transparent color
     */
    public boolean isTransparent() {
        return transparent != null;
    }

    /**
     * Get the transparent color for this image.
     *
     * @return the transparent color if any
     */
    public PDFColor getTransparentColor() {
        return transparent;
    }

    /**
     * Get the bitmap mask reference for this image.
     * Current not supported.
     *
     * @return the bitmap mask reference
     */
    public String getMask() {
        return null;
    }

    /**
     * Get the soft mask reference for this image.
     *
     * @return the soft mask reference if any
     */
    public String getSoftMask() {
        return maskRef;
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#outputContents(OutputStream)
     */
    public void outputContents(OutputStream out) throws IOException {
        out.write(bitmaps);
    }
    
    /**
     * Get the ICC stream.
     * @return always returns null since this has no icc color space
     */
    public PDFICCStream getICCStream() {
        return null;
    }

    /**
     * Check if this is a postscript image.
     * @return always returns false
     */
    public boolean isPS() {
        return false;
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#isDCT()
     */
    public boolean isDCT() {
        return false;
    }

    /**
     * @see org.apache.fop.pdf.PDFImage#getFilterHint()
     */
    public String getFilterHint() {
        return PDFFilterList.IMAGE_FILTER;
    }

}


