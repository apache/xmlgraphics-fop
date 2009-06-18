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
    private int fillOrder = 1;

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

        fld = dir.getField(TIFFImageDecoder.TIFF_FILL_ORDER);
        if (fld != null) {
            fillOrder = fld.getAsInt(0);
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

                // need to invert bytes if fill order = 2
                if (fillOrder == 2) {
                    for (int i = 0; i < (int)stripLength; i++) {
                        readBuf[i] = flipTable[readBuf[i] & 0xff];
                    }
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

    // Table to be used when fillOrder = 2, for flipping bytes.
    // Copied from XML Graphics Commons' TIFFFaxDecoder class
    private static byte[] flipTable = {
     0,  -128,    64,   -64,    32,   -96,    96,   -32,
    16,  -112,    80,   -48,    48,   -80,   112,   -16,
     8,  -120,    72,   -56,    40,   -88,   104,   -24,
    24,  -104,    88,   -40,    56,   -72,   120,    -8,
     4,  -124,    68,   -60,    36,   -92,   100,   -28,
    20,  -108,    84,   -44,    52,   -76,   116,   -12,
    12,  -116,    76,   -52,    44,   -84,   108,   -20,
    28,  -100,    92,   -36,    60,   -68,   124,    -4,
     2,  -126,    66,   -62,    34,   -94,    98,   -30,
    18,  -110,    82,   -46,    50,   -78,   114,   -14,
    10,  -118,    74,   -54,    42,   -86,   106,   -22,
    26,  -102,    90,   -38,    58,   -70,   122,    -6,
     6,  -122,    70,   -58,    38,   -90,   102,   -26,
    22,  -106,    86,   -42,    54,   -74,   118,   -10,
    14,  -114,    78,   -50,    46,   -82,   110,   -18,
    30,   -98,    94,   -34,    62,   -66,   126,    -2,
     1,  -127,    65,   -63,    33,   -95,    97,   -31,
    17,  -111,    81,   -47,    49,   -79,   113,   -15,
     9,  -119,    73,   -55,    41,   -87,   105,   -23,
    25,  -103,    89,   -39,    57,   -71,   121,    -7,
     5,  -123,    69,   -59,    37,   -91,   101,   -27,
    21,  -107,    85,   -43,    53,   -75,   117,   -11,
    13,  -115,    77,   -51,    45,   -83,   109,   -19,
    29,   -99,    93,   -35,    61,   -67,   125,    -3,
     3,  -125,    67,   -61,    35,   -93,    99,   -29,
    19,  -109,    83,   -45,    51,   -77,   115,   -13,
    11,  -117,    75,   -53,    43,   -85,   107,   -21,
    27,  -101,    91,   -37,    59,   -69,   123,    -5,
     7,  -121,    71,   -57,    39,   -89,   103,   -25,
    23,  -105,    87,   -41,    55,   -73,   119,    -9,
    15,  -113,    79,   -49,    47,   -81,   111,   -17,
    31,   -97,    95,   -33,    63,   -65,   127,    -1,
    };
    // end
}
