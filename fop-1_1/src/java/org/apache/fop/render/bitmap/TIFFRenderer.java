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

package org.apache.fop.render.bitmap;

// Code originaly contributed by Oleg Tkachenko of Multiconn International Ltd
// (olegt@multiconn.com).

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;

import org.apache.xmlgraphics.image.GraphicsUtil;
import org.apache.xmlgraphics.image.rendered.FormatRed;
import org.apache.xmlgraphics.image.writer.ImageWriter;
import org.apache.xmlgraphics.image.writer.ImageWriterRegistry;
import org.apache.xmlgraphics.image.writer.MultiImageWriter;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.render.java2d.Java2DRenderer;

import static org.apache.fop.render.bitmap.TIFFCompressionValue.CCITT_T4;
import static org.apache.fop.render.bitmap.TIFFCompressionValue.CCITT_T6;
import static org.apache.fop.render.bitmap.TIFFCompressionValue.PACKBITS;

/**
 * <p>
 * This class represents renderer to TIFF (Tagged Image File Format) format. It
 * is one of the most popular and flexible of the current public domain raster
 * file formats, which was is primarily designed for raster data interchange.
 * Supported compression types are:
 * <ul>
 * <li>Raw noncompressed data</li>
 * <li>Byte-oriented run-length encoding "PackBits" compression.</li>
 * <li>Modified Huffman Compression (CCITT Group 3 1D facsimile compression)</li>
 * <li>CCITT T.4 bilevel compression (CCITT Group 3 2D facsimile compression)</li>
 * <li>CCITT T.6 bilevel compression (CCITT Group 4 facsimile compression)</li>
 * <li>JPEG-in-TIFF compression</li>
 * <li>DEFLATE lossless compression (also known as "Zip-in-TIFF")</li>
 * <li>LZW compression</li>
 * TODO
 * <p>
 * This class actually does not render itself, instead it extends
 * <code>org.apache.fop.render.java2D.Java2DRenderer</code> and just encode
 * rendering results into TIFF format using Batik's image codec
 */
public class TIFFRenderer extends Java2DRenderer {

    private BitmapRenderingSettings imageSettings;

    private OutputStream outputStream;

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_TIFF;
    }

    /**
     * Creates TIFF renderer.
     *
     * @param userAgent the user agent that contains configuration details. This cannot be null.
     */
    public TIFFRenderer(FOUserAgent userAgent) {
        super(userAgent);
        imageSettings = new BitmapRenderingSettings();
        imageSettings.setCompressionMethod(PACKBITS.getName());
        imageSettings.setBufferedImageType(BufferedImage.TYPE_INT_ARGB);
        int dpi = Math.round(userAgent.getTargetResolution());
        imageSettings.setResolution(dpi);
    }

    /** {@inheritDoc} */
    public void startRenderer(OutputStream outputStream) throws IOException {
        this.outputStream = outputStream;
        super.startRenderer(outputStream);
    }

    /** {@inheritDoc} */
    public void stopRenderer() throws IOException {
        super.stopRenderer();
        log.debug("Starting TIFF encoding ...");

        // Creates lazy iterator over generated page images
        Iterator pageImagesItr = new LazyPageImagesIterator(getNumberOfPages(), log);

        // Creates writer
        ImageWriter writer = ImageWriterRegistry.getInstance().getWriterFor(getMimeType());
        if (writer == null) {
            BitmapRendererEventProducer eventProducer
                = BitmapRendererEventProducer.Provider.get(
                        getUserAgent().getEventBroadcaster());
            eventProducer.noImageWriterFound(this, getMimeType());
        }
        if (writer.supportsMultiImageWriter()) {
            MultiImageWriter multiWriter = writer.createMultiImageWriter(outputStream);
            try {
                // Write all pages/images
                while (pageImagesItr.hasNext()) {
                    RenderedImage img = (RenderedImage) pageImagesItr.next();
                    multiWriter.writeImage(img, imageSettings.getWriterParams());
                }
            } finally {
                multiWriter.close();
            }
        } else {
            RenderedImage renderedImage = null;
            if (pageImagesItr.hasNext()) {
                renderedImage = (RenderedImage) pageImagesItr.next();
            }
            writer.writeImage(renderedImage, outputStream, imageSettings.getWriterParams());
            if (pageImagesItr.hasNext()) {
                BitmapRendererEventProducer eventProducer
                    = BitmapRendererEventProducer.Provider.get(
                            getUserAgent().getEventBroadcaster());
                eventProducer.stoppingAfterFirstPageNoFilename(this);
            }
        }

        // Cleaning
        outputStream.flush();
        clearViewportList();
        log.debug("TIFF encoding done.");
    }

    /** {@inheritDoc} */
    protected BufferedImage getBufferedImage(int bitmapWidth, int bitmapHeight) {
        return new BufferedImage(bitmapWidth, bitmapHeight, imageSettings.getBufferedImageType());
    }

    /** Private inner class to lazy page rendering. */
    private class LazyPageImagesIterator implements Iterator {
        /** logging instance */
        private Log log;

        private int count;

        private int current = 0;

        /**
         * Main constructor
         * @param c number of pages to iterate over
         * @param log the logger to use (this is a hack so this compiles under JDK 1.3)
         */
        public LazyPageImagesIterator(int c, Log log) {
            count = c;
            this.log = log;
        }

        public boolean hasNext() {
            return current < count;
        }

        public Object next() {
            if (log.isDebugEnabled()) {
                log.debug("[" + (current + 1) + "]");
            }

            // Renders current page as image
            BufferedImage pageImage = null;
            try {
                pageImage = getPageImage(current++);
            } catch (FOPException e) {
                throw new NoSuchElementException(e.getMessage());
            }

            TIFFCompressionValue compression = TIFFCompressionValue.getType(imageSettings.getCompressionMethod());
            if (compression == CCITT_T4 || compression == CCITT_T6) {
                return pageImage;
            } else {
                //Decorate the image with a packed sample model for encoding by the codec
                SinglePixelPackedSampleModel sppsm;
                sppsm = (SinglePixelPackedSampleModel)pageImage.getSampleModel();

                int bands = sppsm.getNumBands();
                int[] off = new int[bands];
                int w = pageImage.getWidth();
                int h = pageImage.getHeight();
                for (int i = 0; i < bands; i++) {
                    off[i] = i;
                }
                SampleModel sm = new PixelInterleavedSampleModel(
                        DataBuffer.TYPE_BYTE, w, h, bands, w * bands, off);

                RenderedImage rimg = new FormatRed(GraphicsUtil.wrap(pageImage), sm);
                return rimg;
            }
        }

        public void remove() {
            throw new UnsupportedOperationException(
                    "Method 'remove' is not supported.");
        }
    }

    /** @param bufferedImageType an image type */
    public void setBufferedImageType(int bufferedImageType) {
        imageSettings.setBufferedImageType(bufferedImageType);
    }

    /**
     * Returns the settings for the image rendering.
     * @return the image rendering settings
     */
    public BitmapRenderingSettings getRenderingSettings() {
        return imageSettings;
    }
}
