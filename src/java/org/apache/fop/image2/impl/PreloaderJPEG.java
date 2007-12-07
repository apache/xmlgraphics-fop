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

import org.apache.fop.apps.MimeConstants;
import org.apache.fop.image2.ImageContext;
import org.apache.fop.image2.ImageException;
import org.apache.fop.image2.ImageInfo;
import org.apache.fop.image2.ImageSize;
import org.apache.fop.image2.util.ImageUtil;

/**
 * Image preloader for JPEG images.
 */
public class PreloaderJPEG extends AbstractImagePreloader implements JPEGConstants {

    private static final int JPG_SIG_LENGTH = 3;

    /** {@inheritDoc} 
     * @throws ImageException */
    public ImageInfo preloadImage(String uri, Source src, ImageContext context)
                throws IOException, ImageException {
        if (!ImageUtil.hasImageInputStream(src)) {
            return null;
        }
        ImageInputStream in = ImageUtil.needImageInputStream(src);
        byte[] header = getHeader(in, JPG_SIG_LENGTH);
        boolean supported = ((header[0] == (byte)MARK)
                && (header[1] == (byte)SOI)
                && (header[2] == (byte)MARK));

        if (supported) {
            ImageInfo info = new ImageInfo(uri, MimeConstants.MIME_JPEG);
            info.setSize(determineSize(in, context));
            return info;
        } else {
            return null;
        }
    }

    private ImageSize determineSize(ImageInputStream in, ImageContext context)
            throws IOException, ImageException {
        in.mark();
        try {
            ImageSize size = new ImageSize();

            //TODO Read resolution from EXIF if there's no APP0
            //(for example with JPEGs from digicams)
            while (true) {
                int segID = readMarkerSegment(in);
                //System.out.println("Segment: " + Integer.toHexString(segID));
                switch (segID) {
                case SOI:
                case NULL:
                    break;
                case APP0:
                    int reclen = in.readUnsignedShort();
                    in.skipBytes(7);
                    int densityUnits = in.read();
                    int xdensity = in.readUnsignedShort();
                    int ydensity = in.readUnsignedShort();
                    if (densityUnits == 2) {
                        //dots per centimeter
                        size.setResolution(xdensity * 28.3464567 / 72, ydensity * 28.3464567 / 72);
                        //TODO Why is 28.3464567 used for cm to inch conversion?
                        //We normally use 25.4.
                    } else if (densityUnits == 1) {
                        //dots per inch
                        size.setResolution(xdensity, ydensity);
                    } else {
                        //resolution not specified
                        size.setResolution(context.getSourceResolution());
                    }
                    if (size.getWidthPx() != 0) {
                        size.calcSizeFromPixels();
                        return size;
                    }
                    in.skipBytes(reclen - 14);
                    break;
                case SOF0:
                case SOF1:
                case SOF2: // SOF2 and SOFA are only supported by PDF 1.3
                case SOFA:
                    in.skipBytes(2); //length field
                    in.skipBytes(1);
                    int height = in.readUnsignedShort();
                    int width = in.readUnsignedShort();
                    size.setSizeInPixels(width, height);
                    if (size.getDpiHorizontal() != 0) {
                        size.calcSizeFromPixels();
                        return size;
                    }
                    break;
                case SOS:
                case EOI:
                    if (size.getDpiHorizontal() == 0) {
                        size.setResolution(context.getSourceResolution());
                        size.calcSizeFromPixels();
                    }
                    return size;
                default:
                    reclen = in.readUnsignedShort();
                    in.skipBytes(reclen - 2);
                }
            }
        } finally {
            in.reset();
        }
    }

    private int readMarkerSegment(ImageInputStream in) throws IOException {
        int marker;
        do {
            marker = in.read();
        } while (marker != MARK);
        int segID = in.read();
        return segID;
    }
    
}
