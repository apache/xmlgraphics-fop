/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import java.io.IOException;
import java.util.HashMap;
public class BitmapImage implements PDFImage {
        int m_height;
        int m_width;
        int m_bitsPerPixel;
        PDFColorSpace m_colorSpace;
        byte[] m_bitmaps;
        String maskRef;
        PDFColor transparent = null;
        String key;
        HashMap filters;

        public BitmapImage(String k, int width, int height, byte[] result,
                  String mask) {
            this.key = k;
            this.m_height = height;
            this.m_width = width;
            this.m_bitsPerPixel = 8;
            this.m_colorSpace = new PDFColorSpace(PDFColorSpace.DEVICE_RGB);
            this.m_bitmaps = result;
            maskRef = mask;
        }

        public void setup(PDFDocument doc) {
            filters = doc.getFilterMap();
        }

        public String getKey() {
            return key;
        }

        // image size
        public int getWidth() {
            return m_width;
        }

        public int getHeight() {
            return m_height;
        }

        public void setColorSpace(PDFColorSpace cs) {
            m_colorSpace = cs;
        }

        // DeviceGray, DeviceRGB, or DeviceCMYK
        public PDFColorSpace getColorSpace() {
            return m_colorSpace;
        }

        // bits per pixel
        public int getBitsPerPixel() {
            return m_bitsPerPixel;
        }

        public void setTransparent(PDFColor t) {
            transparent = t;
        }

        // For transparent images
        public boolean isTransparent() {
            return transparent != null;
        }

        public PDFColor getTransparentColor() {
            return transparent;
        }

        public String getMask() {
            return null;
        }

        public String getSoftMask() {
            return maskRef;
        }

        public PDFStream getDataStream() throws IOException {
            // delegate the stream work to PDFStream
            PDFStream imgStream = new PDFStream(0);

            imgStream.setData(m_bitmaps);

            imgStream.addDefaultFilters(filters, PDFStream.CONTENT_FILTER);
            return imgStream;
        }

        public PDFICCStream getICCStream() {
            return null;
        }

        public boolean isPS() {
            return false;
        }
    }

