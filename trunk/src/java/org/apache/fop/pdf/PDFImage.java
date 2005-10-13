/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

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
     * (todo) Remove this and delegate to the XObject
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

    /**
     * Get the PDF Filter to be applied to the image.
     *
     * @return the PDF Filter or null
     */
    PDFFilter getPDFFilter();
    
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

