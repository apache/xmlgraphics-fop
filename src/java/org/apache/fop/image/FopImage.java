/*
 * $Id: FopImage.java,v 1.15 2003/03/06 21:25:44 jeremias Exp $
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
    String getMimeType();

    /**
     * Load particular inforamtion for this image
     * This must be called before attempting to get
     * the information.
     *
     * @param type the type of loading required
     * @param ua the user agent
     * @return boolean true if the information could be loaded
     */
    boolean load(int type, FOUserAgent ua);

    /**
     * Returns the image width.
     * @return the width in pixels
     */
    int getWidth();

    /**
     * Returns the image height.
     * @return the height in pixels
     */
    int getHeight();

    /**
     * Returns the color space of the image.
     * @return the color space
     */
    ColorSpace getColorSpace();
    
    /**
     * Returns the ICC profile.
     * @return the ICC profile, null if none is available
     */
    ICC_Profile getICCProfile();

    /**
     * Returns the number of bits per pixel for the image.
     * @return the number of bits per pixel
     */
    int getBitsPerPixel();

    /**
     * Indicates whether the image is transparent.
     * @return True if it is transparent
     */
    boolean isTransparent();
    
    /**
     * For transparent images. Returns the transparent color.
     * @return the transparent color
     * @todo Remove the PDF dependency
     */
    PDFColor getTransparentColor();
    
    /**
     * Indicates whether the image has a Soft Mask (See section 7.5.4 in the 
     * PDF specs)
     * @return True if a Soft Mask exists
     */
    boolean hasSoftMask();
    
    /**
     * For images with a Soft Mask. Returns the Soft Mask as an array.
     * @return the Soft Mask
     */
    byte[] getSoftMask();

    /**
     * Returns the decoded and uncompressed image as a array of 
     * width * height * [colorspace-multiplicator] pixels.
     * @return the bitmap
     */
    byte[] getBitmaps();
    /**
     * Returns the size of the image.
     * width * (bitsPerPixel / 8) * height, no ?
     * @return the size
     */
    int getBitmapsSize();

    /**
     * Returns the encoded/compressed image as an array of bytes.
     * @return the raw image
     */
    byte[] getRessourceBytes();
        
    /**
     * Returns the number of bytes of the raw image.
     * @return the size in bytes
     */
    int getRessourceBytesSize();

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

