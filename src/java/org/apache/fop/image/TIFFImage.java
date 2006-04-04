/*
 * Copyright 2004-2006 The Apache Software Foundation 
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

package org.apache.fop.image;

import java.awt.color.ColorSpace;
import java.io.IOException;

import org.apache.xmlgraphics.image.codec.util.SeekableStream;
import org.apache.xmlgraphics.image.codec.tiff.TIFFDirectory;
import org.apache.xmlgraphics.image.codec.tiff.TIFFField;
import org.apache.xmlgraphics.image.codec.tiff.TIFFImageDecoder;
import org.apache.xmlgraphics.image.rendered.CachableRed;
import org.apache.commons.io.IOUtils;

/**
 * TIFF implementation using the Batik codecs.
 */
public class TIFFImage extends XmlGraphicsCommonsImage {

    private int compression = 0;
    private int stripCount = 0;
    private long stripOffset = 0;
    private long stripLength = 0;
    
    /**
     * Constructs a new BatikImage instance.
     * @param imgReader basic metadata for the image
     */
    public TIFFImage(FopImage.ImageInfo imgReader) {
        super(imgReader);
    }

    /**
     * The compression type set in the TIFF directory
     * @return the TIFF compression type
     */
    public int getCompression() {
        return compression;
    }

    /**
     * The number of strips in the image
     * @return the number of strips in the image
     */
    public int getStripCount() {
        return stripCount;
    }

    /**
     * @see org.apache.fop.image.XmlGraphicsCommonsImage#decodeImage(
     *          org.apache.xmlgraphics.image.codec.util.SeekableStream)
     */
    protected CachableRed decodeImage(SeekableStream stream) throws IOException {
        org.apache.xmlgraphics.image.codec.tiff.TIFFImage img
            = new org.apache.xmlgraphics.image.codec.tiff.TIFFImage
                (stream, null, 0);
        TIFFDirectory dir = (TIFFDirectory)img.getProperty("tiff_directory");
        TIFFField fld = dir.getField(TIFFImageDecoder.TIFF_RESOLUTION_UNIT);
        int resUnit = fld.getAsInt(0);
        fld = dir.getField(TIFFImageDecoder.TIFF_X_RESOLUTION);
        double xRes = fld.getAsDouble(0);
        fld = dir.getField(TIFFImageDecoder.TIFF_Y_RESOLUTION);
        double yRes = fld.getAsDouble(0);
        switch (resUnit) {
        case 2: //inch
            this.dpiHorizontal = xRes;
            this.dpiVertical = yRes;
            break;
        case 3: //cm
            this.dpiHorizontal = xRes * 2.54f;
            this.dpiVertical = yRes * 2.54f;
            break;
        default:
            //ignored
            log.warn("Cannot determine bitmap resolution."
                    + " Unimplemented resolution unit: " + resUnit);
        }
        fld = dir.getField(TIFFImageDecoder.TIFF_COMPRESSION);
        if (fld != null) {
            compression = fld.getAsInt(0);
        }
        fld = dir.getField(TIFFImageDecoder.TIFF_BITS_PER_SAMPLE);
        if (fld != null) {
            bitsPerPixel = fld.getAsInt(0);
        }
        fld = dir.getField(TIFFImageDecoder.TIFF_ROWS_PER_STRIP);
        if (fld == null) {
            stripCount = 1;
        } else {
            stripCount = (int)(dir.getFieldAsLong(TIFFImageDecoder.TIFF_IMAGE_LENGTH)
                                / fld.getAsLong(0));
        }
        stripOffset = dir.getField(TIFFImageDecoder.TIFF_STRIP_OFFSETS).getAsLong(0);
        stripLength = dir.getField(TIFFImageDecoder.TIFF_STRIP_BYTE_COUNTS).getAsLong(0);
        
        if (this.bitsPerPixel == 1) {
            this.colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        }
        return img;
    }

    /**
     * Load the original TIFF data.
     * This loads only strip 1 of the original TIFF data.
     *
     * @return true if loaded false for any error
     * @see org.apache.fop.image.AbstractFopImage#loadOriginalData()
     */
    protected boolean loadOriginalData() {
        if (loadDimensions()) {
            byte[] readBuf = new byte[(int)stripLength];
            int bytesRead;

            try {
                this.seekableInput.reset();
                this.seekableInput.skip(stripOffset);
                bytesRead = seekableInput.read(readBuf);
                if (bytesRead != stripLength) {
                    log.error("Error while loading image: length mismatch on read");
                    return false;
                }

                this.raw = readBuf;
                return true;
            } catch (IOException ioe) {
                log.error("Error while loading image strip 1 (TIFF): ", ioe);
                return false;
            } finally {
                IOUtils.closeQuietly(seekableInput);
                IOUtils.closeQuietly(inputStream);
                this.seekableInput = null;
                this.inputStream = null;
                this.cr = null;
            }
        }
        return false;
    }

}
