/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.xmlgraphics.image.GraphicsUtil;

/**
 * PDFImage implementation for alpha channel "images".
 */
public class AlphaRasterImage implements PDFImage {

    private int bitsPerComponent;
    private PDFDeviceColorSpace colorSpace;
    private Raster alpha;
    private String key;

    /**
     * Create a alpha channel image.
     * Creates a new bitmap image with the given data.
     *
     * @param k the key to be used to lookup the image
     * @param alpha the alpha channel raster
     */
    public AlphaRasterImage(String k, Raster alpha) {
        this.key = k;
        //Enable the commented line below if 16-bit alpha channels are desired.
        //Otherwise, we compress the alpha channel to 8 bit which should be sufficient.
        //this.bitsPerComponent = alpha.getSampleModel().getSampleSize(0);
        this.bitsPerComponent = 8;
        this.colorSpace = new PDFDeviceColorSpace(PDFDeviceColorSpace.DEVICE_GRAY);
        if (alpha == null) {
            throw new NullPointerException("Parameter alpha must not be null");
        }
        this.alpha = alpha;
    }

    /**
     * Create a alpha channel image.
     * Extracts the alpha channel from the RenderedImage and creates a new bitmap image
     * with the given data.
     *
     * @param k the key to be used to lookup the image
     * @param image the image (must have an alpha channel)
     */
    public AlphaRasterImage(String k, RenderedImage image) {
        this(k, GraphicsUtil.getAlphaRaster(image));
    }

    /** {@inheritDoc} */
    public void setup(PDFDocument doc) {
        //nop
    }

    /** {@inheritDoc} */
    public String getKey() {
        return key;
    }

    /** {@inheritDoc} */
    public int getWidth() {
        return alpha.getWidth();
    }

    /** {@inheritDoc} */
    public int getHeight() {
        return alpha.getHeight();
    }

    /** {@inheritDoc} */
    public PDFDeviceColorSpace getColorSpace() {
        return colorSpace;
    }

    /** {@inheritDoc} */
    public int getBitsPerComponent() {
        return bitsPerComponent;
    }

    /** {@inheritDoc} */
    public boolean isTransparent() {
        return false;
    }

    /** {@inheritDoc} */
    public PDFColor getTransparentColor() {
        return null;
    }

    /** {@inheritDoc} */
    public String getMask() {
        return null;
    }

    /** @return null (unless overridden) */
    public String getSoftMask() {
        return null;
    }

    /** {@inheritDoc} */
    public PDFReference getSoftMaskReference() {
        return null;
    }

    /** {@inheritDoc} */
    public boolean isInverted() {
        return false;
    }

    /** {@inheritDoc} */
    public void outputContents(OutputStream out) throws IOException {
        int w = getWidth();
        int h = getHeight();

        //Check Raster
        int nbands = alpha.getNumBands();
        if (nbands != 1) {
            throw new UnsupportedOperationException(
                    "Expected only one band/component for the alpha channel");
        }

        //...and write the Raster line by line with a reusable buffer
        int dataType = alpha.getDataBuffer().getDataType();
        if (dataType == DataBuffer.TYPE_BYTE) {
            byte[] line = new byte[nbands * w];
            for (int y = 0; y < h; y++) {
                alpha.getDataElements(0, y, w, 1, line);
                out.write(line);
            }
        } else if (dataType == DataBuffer.TYPE_USHORT) {
            short[] sline = new short[nbands * w];
            byte[] line = new byte[nbands * w];
            for (int y = 0; y < h; y++) {
                alpha.getDataElements(0, y, w, 1, sline);
                for (int i = 0; i < w; i++) {
                    //this compresses a 16-bit alpha channel to 8 bits!
                    //we probably don't ever need a 16-bit channel
                    line[i] = (byte)(sline[i] >> 8);
                }
                out.write(line);
            }
        } else if (dataType == DataBuffer.TYPE_INT) {
            //Is there an better way to get a 8bit raster from a TYPE_INT raster?
            int shift = 24;
            SampleModel sampleModel = alpha.getSampleModel();
            if (sampleModel instanceof SinglePixelPackedSampleModel) {
                SinglePixelPackedSampleModel m = (SinglePixelPackedSampleModel)sampleModel;
                shift = m.getBitOffsets()[0];
            }
            int[] iline = new int[nbands * w];
            byte[] line = new byte[nbands * w];
            for (int y = 0; y < h; y++) {
                alpha.getDataElements(0, y, w, 1, iline);
                for (int i = 0; i < w; i++) {
                    line[i] = (byte)(iline[i] >> shift);
                }
                out.write(line);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported DataBuffer type: "
                    + alpha.getDataBuffer().getClass().getName());
        }

    }

    /** {@inheritDoc} */
    public void populateXObjectDictionary(PDFDictionary dict) {
        //nop
    }

    /** {@inheritDoc} */
    public PDFICCStream getICCStream() {
        return null;
    }

    /** {@inheritDoc} */
    public boolean isPS() {
        return false;
    }

    /** {@inheritDoc} */
    public String getFilterHint() {
        return PDFFilterList.IMAGE_FILTER;
    }

    /** {@inheritDoc} */
    public PDFFilter getPDFFilter() {
        return null;
    }

}


