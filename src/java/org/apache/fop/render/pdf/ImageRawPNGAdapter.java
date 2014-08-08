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

// Original author: Matthias Reichenbacher

package org.apache.fop.render.pdf;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.impl.ImageRawPNG;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;

import org.apache.fop.pdf.BitmapImage;
import org.apache.fop.pdf.FlateFilter;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFilter;
import org.apache.fop.pdf.PDFFilterException;
import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.pdf.PDFName;
import org.apache.fop.pdf.PDFReference;

public class ImageRawPNGAdapter extends AbstractImageAdapter {

    /** logging instance */
    private static Log log = LogFactory.getLog(ImageRawPNGAdapter.class);

    private static final PDFName RI_PERCEPTUAL = new PDFName("Perceptual");
    private static final PDFName RI_RELATIVE_COLORIMETRIC = new PDFName("RelativeColorimetric");
    private static final PDFName RI_SATURATION = new PDFName("Saturation");
    private static final PDFName RI_ABSOLUTE_COLORIMETRIC = new PDFName("AbsoluteColorimetric");

    private PDFFilter pdfFilter;
    private String maskRef;
    private PDFReference softMask;
    private int numberOfInterleavedComponents;

    /**
     * Creates a new PDFImage from an Image instance.
     * @param image the image
     * @param key XObject key
     */
    public ImageRawPNGAdapter(ImageRawPNG image, String key) {
        super(image, key);
    }

    /** {@inheritDoc} */
    public void setup(PDFDocument doc) {
        super.setup(doc);
        ColorModel cm = ((ImageRawPNG) this.image).getColorModel();
        if (cm instanceof IndexColorModel) {
            numberOfInterleavedComponents = 1;
        } else {
            // this can be 1 (gray), 2 (gray + alpha), 3 (rgb) or 4 (rgb + alpha)
            // numberOfInterleavedComponents = (cm.hasAlpha() ? 1 : 0) + cm.getNumColorComponents();
            numberOfInterleavedComponents = cm.getNumComponents();
        }

        // set up image compression for non-alpha channel
        FlateFilter flate;
        try {
            flate = new FlateFilter();
            flate.setApplied(true);
            flate.setPredictor(FlateFilter.PREDICTION_PNG_OPT);
            if (numberOfInterleavedComponents < 3) {
                // means palette (1) or gray (1) or gray + alpha (2)
                flate.setColors(1);
            } else {
                // means rgb (3) or rgb + alpha (4)
                flate.setColors(3);
            }
            flate.setColumns(image.getSize().getWidthPx());
            flate.setBitsPerComponent(this.getBitsPerComponent());
        } catch (PDFFilterException e) {
            throw new RuntimeException("FlateFilter configuration error", e);
        }
        this.pdfFilter = flate;
        this.disallowMultipleFilters();

        // Handle transparency channel if applicable; note that for palette images the transparency is
        // not TRANSLUCENT
        if (cm.hasAlpha() && cm.getTransparency() == ColorModel.TRANSLUCENT) {
            doc.getProfile().verifyTransparencyAllowed(image.getInfo().getOriginalURI());
            // TODO: Implement code to combine image with background color if transparency is not allowed
            // here we need to inflate the PNG pixel data, which includes alpha, separate the alpha channel
            // and then deflate it back again
            ByteArrayOutputStream baos = null;
            DeflaterOutputStream dos = null;
            InputStream in = null;
            InflaterInputStream infStream = null;
            DataInputStream dataStream = null;
            try {
                baos = new ByteArrayOutputStream();
                dos = new DeflaterOutputStream(baos, new Deflater());
                in = ((ImageRawStream) image).createInputStream();
                try {
                    infStream = new InflaterInputStream(in, new Inflater());
                    dataStream = new DataInputStream(infStream);
                    // offset is the byte offset of the alpha component
                    int offset = numberOfInterleavedComponents - 1; // 1 for GA, 3 for RGBA
                    int numColumns = image.getSize().getWidthPx();
                    int bytesPerRow = numberOfInterleavedComponents * numColumns;
                    int filter;
                    // read line by line; the first byte holds the filter
                    while ((filter = dataStream.read()) != -1) {
                        byte[] bytes = new byte[bytesPerRow];
                        dataStream.readFully(bytes, 0, bytesPerRow);
                        dos.write((byte) filter);
                        for (int j = 0; j < numColumns; j++) {
                            dos.write(bytes, offset, 1);
                            offset += numberOfInterleavedComponents;
                        }
                        offset = numberOfInterleavedComponents - 1;
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error processing transparency channel:", e);
                }
                // set up alpha channel compression
                FlateFilter transFlate;
                try {
                    transFlate = new FlateFilter();
                    transFlate.setApplied(true);
                    transFlate.setPredictor(FlateFilter.PREDICTION_PNG_OPT);
                    transFlate.setColors(1);
                    transFlate.setColumns(image.getSize().getWidthPx());
                    transFlate.setBitsPerComponent(this.getBitsPerComponent());
                } catch (PDFFilterException e) {
                    throw new RuntimeException("FlateFilter configuration error", e);
                }
                BitmapImage alphaMask = new BitmapImage("Mask:" + this.getKey(), image.getSize().getWidthPx(),
                        image.getSize().getHeightPx(), baos.toByteArray(), null);
                alphaMask.setPDFFilter(transFlate);
                alphaMask.disallowMultipleFilters();
                alphaMask.setColorSpace(new PDFDeviceColorSpace(PDFDeviceColorSpace.DEVICE_GRAY));
                softMask = doc.addImage(null, alphaMask).makeReference();
            } finally {
                IOUtils.closeQuietly(infStream);
                IOUtils.closeQuietly(dataStream);
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(dos);
                IOUtils.closeQuietly(baos);
            }
        }
    }

    /** {@inheritDoc} */
    public PDFDeviceColorSpace getColorSpace() {
        // DeviceGray, DeviceRGB, or DeviceCMYK
        return toPDFColorSpace(image.getColorSpace());
    }

    /** {@inheritDoc} */
    public int getBitsPerComponent() {
        return ((ImageRawPNG) this.image).getBitDepth();
    }

    /** {@inheritDoc} */
    public boolean isTransparent() {
        return ((ImageRawPNG) this.image).isTransparent();
    }

    /** {@inheritDoc} */
    public PDFColor getTransparentColor() {
        return new PDFColor(((ImageRawPNG) this.image).getTransparentColor());
    }

    /** {@inheritDoc} */
    public String getMask() {
        return maskRef;
    }

    /** {@inheritDoc} */
    public String getSoftMask() {
        return softMask.toString();
    }

    /** {@inheritDoc} */
    public PDFReference getSoftMaskReference() {
        return softMask;
    }

    /** {@inheritDoc} */
    public PDFFilter getPDFFilter() {
        return pdfFilter;
    }

    /** {@inheritDoc} */
    public void outputContents(OutputStream out) throws IOException {
        InputStream in = ((ImageRawStream) image).createInputStream();
        InflaterInputStream infStream = null;
        DataInputStream dataStream = null;
        DeflaterOutputStream dos = null;
        try {
            if (numberOfInterleavedComponents == 1 || numberOfInterleavedComponents == 3) {
                // means we have Gray, RGB, or Palette
                IOUtils.copy(in, out);
            } else {
                // means we have Gray + alpha or RGB + alpha
                // TODO: since we have alpha here do this when the alpha channel is extracted
                int numBytes = numberOfInterleavedComponents - 1; // 1 for Gray, 3 for RGB
                int numColumns = image.getSize().getWidthPx();
                infStream = new InflaterInputStream(in, new Inflater());
                dataStream = new DataInputStream(infStream);
                int offset = 0;
                int bytesPerRow = numberOfInterleavedComponents * numColumns;
                int filter;
                // here we need to inflate the PNG pixel data, which includes alpha, separate the alpha
                // channel and then deflate the RGB channels back again
                dos = new DeflaterOutputStream(out, new Deflater());
                while ((filter = dataStream.read()) != -1) {
                    byte[] bytes = new byte[bytesPerRow];
                    dataStream.readFully(bytes, 0, bytesPerRow);
                    dos.write((byte) filter);
                    for (int j = 0; j < numColumns; j++) {
                        dos.write(bytes, offset, numBytes);
                        offset += numberOfInterleavedComponents;
                    }
                    offset = 0;
                }
            }
        } finally {
            IOUtils.closeQuietly(dos);
            IOUtils.closeQuietly(dataStream);
            IOUtils.closeQuietly(infStream);
            IOUtils.closeQuietly(in);
        }
    }

    /** {@inheritDoc} */
    public String getFilterHint() {
        return PDFFilterList.PRECOMPRESSED_FILTER;
    }

    public void populateXObjectDictionary(PDFDictionary dict) {
        int renderingIntent = ((ImageRawPNG) image).getRenderingIntent();
        if (renderingIntent != -1) {
            switch (renderingIntent) {
            case 0:
                dict.put("Intent", RI_PERCEPTUAL);
                break;
            case 1:
                dict.put("Intent", RI_RELATIVE_COLORIMETRIC);
                break;
            case 2:
                dict.put("Intent", RI_SATURATION);
                break;
            case 3:
                dict.put("Intent", RI_ABSOLUTE_COLORIMETRIC);
                break;
            default:
                // ignore
            }
        }
        ColorModel cm = ((ImageRawPNG) image).getColorModel();
        if (cm instanceof IndexColorModel) {
            IndexColorModel icm = (IndexColorModel) cm;
            super.populateXObjectDictionaryForIndexColorModel(dict, icm);
        }
    }

    protected boolean issRGB() {
        if (((ImageRawPNG) image).getRenderingIntent() != -1) {
            return true;
        }
        return false;
    }
}
