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
import java.nio.ByteOrder;

import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.image2.ImageException;
import org.apache.fop.image2.ImageInfo;
import org.apache.fop.image2.ImageSize;
import org.apache.fop.image2.util.ImageUtil;
import org.apache.fop.util.UnitConv;

/**
 * Image preloader for BMP images.
 */
public class PreloaderBMP extends AbstractImagePreloader {

    /** Length of the BMP header */
    protected static final int BMP_SIG_LENGTH = 2;
    
    /** offset to width */
    private static final int WIDTH_OFFSET = 18;

    /** {@inheritDoc} */
    public ImageInfo preloadImage(String uri, Source src, FOUserAgent userAgent)
                throws IOException, ImageException {
        if (!ImageUtil.hasImageInputStream(src)) {
            return null;
        }
        ImageInputStream in = ImageUtil.needImageInputStream(src);
        byte[] header = getHeader(in, BMP_SIG_LENGTH);
        boolean supported = ((header[0] == (byte) 0x42)
                && (header[1] == (byte) 0x4d));

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
        return "image/bmp";
    }

    private ImageSize determineSize(ImageInputStream in, FOUserAgent userAgent) throws IOException,
            ImageException {
        in.mark();
        ByteOrder oldByteOrder = in.getByteOrder();
        try {
            ImageSize size = new ImageSize();
            
            // BMP uses little endian notation!
            in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            
            in.skipBytes(WIDTH_OFFSET);
            int width = in.readInt();
            int height = in.readInt();
            size.setSizeInPixels(width, height);
            
            in.skipBytes(12);
            int xRes = in.readInt();
            double xResDPI = UnitConv.in2mm(xRes / 1000d);
            if (xResDPI == 0) {
                xResDPI = userAgent.getSourceResolution();
            }

            int yRes = in.readInt();
            double yResDPI = UnitConv.in2mm(yRes / 1000d);
            if (yResDPI == 0) {
                yResDPI = userAgent.getSourceResolution();
            }
            
            size.setResolution(xResDPI, yResDPI);
            size.calcSizeFromPixels();
            return size;
        } finally {
            in.setByteOrder(oldByteOrder);
            in.reset();
        }
    }

}
