/*
 * $Id: PDFImage.java,v 1.3 2003/03/07 08:25:46 jeremias Exp $
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

/**
 * Interface for a PDF image.
 * This is used for inserting an image into PDF.
 */
public interface PDFImage {

    /**
     * Key to look up XObject.
     * This should be a unique key to refer to the image.
     *
     * @return the key for this image
     */
    String getKey();

    /**
     * Setup the PDF image for the current document.
     * Some image formats may need to access the document (for example to
     * add an ICC profile to the document).
     *
     * @param doc the PDF parent document
     * @todo Remove this and deletgate to the XObject
     */
    void setup(PDFDocument doc);

    /**
     * Get the image width in pixels.
     *
     * @return the image width
     */
    int getWidth();

    /**
     * Get the image height in pixels.
     *
     * @return the image height
     */
    int getHeight();

    /**
     * Get the color space for this image.
     * Possible results are: DeviceGray, DeviceRGB, or DeviceCMYK
     *
     * @return the color space
     */
    PDFColorSpace getColorSpace();

    /**
     * Get the bits per pixel for this image.
     *
     * @return the bits per pixel
     */
    int getBitsPerPixel();

    /**
     * Check if this image is a PostScript image.
     *
     * @return true if this is a PostScript image
     */
    boolean isPS();

    /**
     * Check if this image is a DCT encoded image (for JPEG images).
     *
     * @return true if this is a DCT image
     */
    boolean isDCT();

    /**
     * Check if this image has a transparent color transparency.
     *
     * @return true if it has transparency
     */
    boolean isTransparent();

    /**
     * Get the transparent color.
     *
     * @return the transparent color for this image
     */
    PDFColor getTransparentColor();

    /**
     * Get the PDF reference for a bitmap mask.
     *
     * @return the PDF reference for the mask image
     */
    String getMask();

    /**
     * Get the PDF reference for a soft mask.
     *
     * @return the PDF reference for a soft mask image
     */
    String getSoftMask();

    // get the image bytes, and bytes properties

    /**
     * Writes the raw, unencoded contents of the image to a given output stream.
     *
     * @param out OutputStream to write to
     * @throws IOException if there creating stream
     */
    void outputContents(OutputStream out) throws IOException;

    /**
     * Get the ICC stream for this image.
     *
     * @return the ICC stream for this image if any
     */
    PDFICCStream getICCStream();

    /**
     * Returns a hint in form of a String (Possible values from PDFFilterList)
     * indicating which filter setup should be used to encode the object.
     * @return the filter setup hint
     */
    String getFilterHint();

}

