/*
 * Copyright 1999-2006 The Apache Software Foundation.
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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.xmlgraphics.image.GraphicsUtil;
import org.apache.xmlgraphics.image.codec.tiff.TIFFEncodeParam;
import org.apache.xmlgraphics.image.codec.tiff.TIFFField;
import org.apache.xmlgraphics.image.codec.tiff.TIFFImageDecoder;
import org.apache.xmlgraphics.image.codec.tiff.TIFFImageEncoder;
import org.apache.xmlgraphics.image.rendered.FormatRed;
import org.apache.commons.logging.Log;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.render.java2d.Java2DRenderer;

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

    /** The MIME type for tiff-Rendering */
    public static final String MIME_TYPE = MimeConstants.MIME_TIFF;

    /** */
    private TIFFEncodeParam renderParams;

    private OutputStream outputStream;

    /** @see org.apache.fop.render.AbstractRenderer */
    public String getMimeType() {
        return MIME_TYPE;
    }

    /** Creates TIFF renderer. */
    public TIFFRenderer() {
        renderParams = new TIFFEncodeParam();
        //Default to packbits compression which is widely supported
        renderParams.setCompression(TIFFEncodeParam.COMPRESSION_PACKBITS);
    }

    /**
     * Configure the TIFF renderer. Get the configuration to be used for
     * compression
     * @see org.apache.avalon.framework.configuration.Configurable#configure(Configuration)
     */
    public void configure(Configuration cfg) throws ConfigurationException {

        //TODO Support output of monochrome bitmaps (fax-style)
        int comp = cfg.getChild("compression").getAttributeAsInteger("value", 1);
        String name = null;
        switch (comp) {
        case TIFFEncodeParam.COMPRESSION_NONE:
            name = "COMPRESSION_NONE";
            break;
        case TIFFEncodeParam.COMPRESSION_JPEG_TTN2:
            name = "COMPRESSION_JPEG_TTN2";
            break;
        case TIFFEncodeParam.COMPRESSION_PACKBITS:
            name = "COMPRESSION_PACKBITS";
            break;
        case TIFFEncodeParam.COMPRESSION_DEFLATE:
            name = "COMPRESSION_DEFLATE";
            break;
        default:
            log.info("TIFF compression not supported: " + comp);
            return;
        }
        log.info("TIFF compression set to " + name);

    }

    /** @see org.apache.fop.render.Renderer#startRenderer(java.io.OutputStream) */
    public void startRenderer(OutputStream outputStream) throws IOException {
        this.outputStream = outputStream;
        super.startRenderer(outputStream);
    }

    /** @see org.apache.fop.render.Renderer#stopRenderer() */
    public void stopRenderer() throws IOException {

        super.stopRenderer();
        log.debug("Starting Tiff encoding ...");

        //Set target resolution
        float pixSzMM = userAgent.getTargetPixelUnitToMillimeter();
        // num Pixs in 100 Meters
        int numPix = (int)(((1000 * 100) / pixSzMM) + 0.5); 
        int denom = 100 * 100;  // Centimeters per 100 Meters;
        long [] rational = {numPix, denom};
        TIFFField [] fields = {
            new TIFFField(TIFFImageDecoder.TIFF_RESOLUTION_UNIT, 
                          TIFFField.TIFF_SHORT, 1, 
                          new char[] {(char)3}),
            new TIFFField(TIFFImageDecoder.TIFF_X_RESOLUTION, 
                          TIFFField.TIFF_RATIONAL, 1, 
                          new long[][] {rational}),
            new TIFFField(TIFFImageDecoder.TIFF_Y_RESOLUTION, 
                          TIFFField.TIFF_RATIONAL, 1, 
                          new long[][] {rational}) 
                };
        renderParams.setExtraFields(fields);

        // Creates encoder
        TIFFImageEncoder enc = new TIFFImageEncoder(outputStream, renderParams);

        // Creates lazy iterator over generated page images
        Iterator pageImagesItr = new LazyPageImagesIterator(getNumberOfPages(), log);

        // The first image to be passed to enc
        RenderedImage first = (RenderedImage) pageImagesItr.next();

        // The other images are set to the renderParams
        renderParams.setExtraImages(pageImagesItr);

        // Start encoding
        enc.encode(first);

        // Cleaning
        outputStream.flush();
        clearViewportList();
        log.debug("Tiff encoding done.");

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
            log.debug("[" + (current + 1) + "]");

            // Renders current page as image
            BufferedImage pageImage = null;
            try {
                pageImage = getPageImage(current++);
            } catch (FOPException e) {
                log.error(e);
                return null;
            }

            switch (renderParams.getCompression()) {
            // These types of compression require a monochrome image
            /* these compression types are not supported by the Batik codec
            case TIFFEncodeParam.COMPRESSION_GROUP3_1D:
            case TIFFEncodeParam.COMPRESSION_GROUP3_2D:
            case TIFFEncodeParam.COMPRESSION_GROUP4:
                BufferedImage faxImage = new BufferedImage(
                        pageImage.getWidth(), pageImage.getHeight(),
                        BufferedImage.TYPE_BYTE_BINARY);
                faxImage.getGraphics().drawImage(pageImage, 0, 0, null);
                return faxImage;*/
            default:
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
}
