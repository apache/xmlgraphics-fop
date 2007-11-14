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
 
package org.apache.fop.image2.impl;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.image2.ImageException;
import org.apache.fop.image2.ImageInfo;
import org.apache.fop.image2.ImageSize;
import org.apache.fop.image2.util.ImageUtil;
import org.apache.fop.image2.util.SeekableStreamAdapter;
import org.apache.fop.util.UnitConv;
import org.apache.xmlgraphics.image.codec.tiff.TIFFDirectory;
import org.apache.xmlgraphics.image.codec.tiff.TIFFField;
import org.apache.xmlgraphics.image.codec.tiff.TIFFImageDecoder;
import org.apache.xmlgraphics.image.codec.util.SeekableStream;

/**
 * Image preloader for TIFF images.
 * <p>
 * Note: The implementation relies on the TIFF codec code in Apache XML Graphics Commons for
 * access to the TIFF directory.
 */
public class PreloaderTIFF extends AbstractImagePreloader {

    private static final int TIFF_SIG_LENGTH = 8;

    /** {@inheritDoc} 
     * @throws ImageException */
    public ImageInfo preloadImage(String uri, Source src, FOUserAgent userAgent)
            throws IOException, ImageException {
        if (!ImageUtil.hasImageInputStream(src)) {
            return null;
        }
        ImageInputStream in = ImageUtil.needImageInputStream(src);
        byte[] header = getHeader(in, TIFF_SIG_LENGTH);
        boolean supported = false;

        // first 2 bytes = II (little endian encoding)
        if (header[0] == (byte) 0x49 && header[1] == (byte) 0x49) {

            // look for '42' in byte 3 and '0' in byte 4
            if (header[2] == 42 && header[3] == 0) {
                supported = true;
            }
        }

        // first 2 bytes == MM (big endian encoding)
        if (header[0] == (byte) 0x4D && header[1] == (byte) 0x4D) {

            // look for '42' in byte 4 and '0' in byte 3
            if (header[2] == 0 && header[3] == 42) {
                supported = true;
            }
        }

        if (supported) {
            ImageInfo info = new ImageInfo(uri, src, getMimeType());
            info.setSize(determineSize(in, userAgent));
            return info;
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_TIFF;
    }

    private ImageSize determineSize(ImageInputStream in, FOUserAgent userAgent)
                throws IOException, ImageException {
        in.mark();

        SeekableStream seekable = new SeekableStreamAdapter(in);
        TIFFDirectory dir = new TIFFDirectory(seekable, 0);
        int width = (int)dir.getFieldAsLong(TIFFImageDecoder.TIFF_IMAGE_WIDTH);
        int height = (int)dir.getFieldAsLong(TIFFImageDecoder.TIFF_IMAGE_LENGTH);
        ImageSize size = new ImageSize();
        size.setSizeInPixels(width, height);
        int unit = 2; //inch is default
        if (dir.isTagPresent(TIFFImageDecoder.TIFF_RESOLUTION_UNIT)) {
            unit = (int)dir.getFieldAsLong(TIFFImageDecoder.TIFF_RESOLUTION_UNIT);
        }
        if (unit == 2 || unit == 3) {
            float xRes, yRes;
            TIFFField fldx = dir.getField(TIFFImageDecoder.TIFF_X_RESOLUTION);
            TIFFField fldy = dir.getField(TIFFImageDecoder.TIFF_Y_RESOLUTION);
            if (fldx == null || fldy == null) {
                unit = 2;
                xRes = userAgent.getSourceResolution();
                yRes = xRes;
            } else {
                xRes = fldx.getAsFloat(0);
                yRes = fldy.getAsFloat(0);
            }
            if (unit == 2) {
                size.setResolution(xRes, yRes); //Inch
            } else {
                size.setResolution(
                        UnitConv.in2mm(xRes) / 10,
                        UnitConv.in2mm(yRes) / 10); //Centimeters
            }
        } else {
            size.setResolution(userAgent.getSourceResolution());
        }
        size.calcSizeFromPixels();

        in.reset();

        return size;
    }

}
