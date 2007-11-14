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
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.image2.ImageException;
import org.apache.fop.image2.ImageInfo;
import org.apache.fop.image2.ImageSize;
import org.apache.fop.image2.impl.imageio.ImageIOUtil;
import org.apache.fop.image2.util.ImageUtil;

/**
 * Image preloader for PNG images.
 * <p>
 * Note: The implementation relies on the presence of a working ImageIO implementation which
 * provides accurate image metadata for PNG images.
 */
public class PreloaderPNG extends AbstractImagePreloader {

    private static final int PNG_SIG_LENGTH = 8;

    /** {@inheritDoc} 
     * @throws ImageException */
    public ImageInfo preloadImage(String uri, Source src, FOUserAgent userAgent)
            throws IOException, ImageException {
        if (!ImageUtil.hasImageInputStream(src)) {
            return null;
        }
        ImageInputStream in = ImageUtil.needImageInputStream(src);
        byte[] header = getHeader(in, PNG_SIG_LENGTH);
        boolean supported = ((header[0] == (byte) 0x89)
                && (header[1] == (byte) 0x50)
                && (header[2] == (byte) 0x4e)
                && (header[3] == (byte) 0x47)
                && (header[4] == (byte) 0x0d)
                && (header[5] == (byte) 0x0a)
                && (header[6] == (byte) 0x1a)
                && (header[7] == (byte) 0x0a));

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
        return MimeConstants.MIME_PNG;
    }

    private ImageSize determineSize(ImageInputStream in, FOUserAgent userAgent)
                throws IOException, ImageException {
        in.mark();
        
        Iterator iter = ImageIO.getImageReadersByMIMEType(getMimeType());
        if (!iter.hasNext()) {
            throw new UnsupportedOperationException(
                    "No ImageIO codec for " + getMimeType() + " found!");
        }
        ImageReader reader = (ImageReader)iter.next();
        reader.setInput(ImageUtil.ignoreFlushing(in), true, false);
        final int imageIndex = 0;
        IIOMetadata iiometa = reader.getImageMetadata(imageIndex);
        ImageSize size = new ImageSize();
        size.setSizeInPixels(reader.getWidth(imageIndex), reader.getHeight(imageIndex));
        
        //Resolution (first a default, then try to read the metadata)
        size.setResolution(userAgent.getSourceResolution());
        ImageIOUtil.extractResolution(iiometa, size);
        
        reader.dispose();
        
        in.reset();
        
        return size;
    }

}
