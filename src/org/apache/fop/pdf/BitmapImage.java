/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import java.io.IOException;
import java.util.Map;

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
    private Map filters;

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
        filters = doc.getFilterMap();
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
     * Get the pdf data stream for the bitmap data.
     *
     * @return a pdf stream containing the filtered image data
     * @throws IOException if there is an error handling the data
     */
    public PDFStream getDataStream() throws IOException {
        // delegate the stream work to PDFStream
        PDFStream imgStream = new PDFStream(0);

        imgStream.setData(bitmaps);

        imgStream.addDefaultFilters(filters, PDFStream.CONTENT_FILTER);
        return imgStream;
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
}


