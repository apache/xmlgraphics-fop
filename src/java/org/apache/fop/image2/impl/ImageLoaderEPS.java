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
import java.io.InputStream;
import java.util.Map;

import javax.xml.transform.Source;

import org.apache.xmlgraphics.util.io.SubInputStream;

import org.apache.fop.apps.MimeConstants;
import org.apache.fop.image2.Image;
import org.apache.fop.image2.ImageException;
import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.image2.ImageInfo;
import org.apache.fop.image2.ImageSessionContext;
import org.apache.fop.image2.util.ImageUtil;

/**
 * ImageLoader for EPS (Encapsulated PostScript) images.
 */
public class ImageLoaderEPS extends AbstractImageLoader {

    /**
     * Main constructor.
     */
    public ImageLoaderEPS() {
    }        

    /** {@inheritDoc} */
    public ImageFlavor getTargetFlavor() {
        return ImageFlavor.RAW_EPS;
    }

    /** {@inheritDoc} */
    public Image loadImage(ImageInfo info, Map hints, ImageSessionContext session)
                throws ImageException, IOException {
        if (!MimeConstants.MIME_EPS.equals(info.getMimeType())) {
            throw new IllegalArgumentException(
                    "ImageInfo must be from a image with MIME type: " + MimeConstants.MIME_EPS);
        }
        Source src = session.needSource(info.getOriginalURI());
        InputStream in = ImageUtil.needInputStream(src);
        ImageUtil.removeStreams(src); //so others cannot close them, we take them over

        PreloaderEPS.EPSBinaryFileHeader binaryHeader;
        binaryHeader = (PreloaderEPS.EPSBinaryFileHeader)info.getCustomObjects().get(
                PreloaderEPS.EPS_BINARY_HEADER); 
        if (binaryHeader != null) {
            //Binary EPS: just extract the EPS part
            in.skip(binaryHeader.getPSStart());
            in = new SubInputStream(in, binaryHeader.getPSLength());
        }
        
        ImageRawEPS epsImage = new ImageRawEPS(info, in);
        return epsImage;
    }

}
