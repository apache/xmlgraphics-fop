/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import java.io.IOException;

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
     * Some image formats may need to access the document.
     *
     * @param doc the PDF parent document
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

    // get the image bytes, and bytes properties

    /**
     * Get the data stream containing the image contents.
     *
     * @throws IOException if there creating stream
     * @return the PDFStream containing the image data
     */
    PDFStream getDataStream() throws IOException;

    /**
     * Get the ICC stream for this image.
     *
     * @return the ICC stream for this image if any
     */
    PDFICCStream getICCStream();

}

