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
import java.util.Map;

import javax.xml.transform.Source;

import org.apache.fop.image2.Image;
import org.apache.fop.image2.ImageException;
import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.image2.ImageInfo;
import org.apache.fop.image2.ImageSessionContext;
import org.apache.fop.image2.util.ImageUtil;

/**
 * ImageLoader for formats consumed "raw" (undecoded). Provides a raw/undecoded stream.
 */
public class ImageLoaderRaw extends AbstractImageLoader {

    private String mime;
    private ImageFlavor targetFlavor;

    /**
     * Main constructor.
     * @param targetFlavor the target flavor
     */
    public ImageLoaderRaw(ImageFlavor targetFlavor) {
        this.targetFlavor = targetFlavor;
        this.mime = ImageLoaderFactoryRaw.getMimeForRawFlavor(targetFlavor);
    }        

    /** {@inheritDoc} */
    public ImageFlavor getTargetFlavor() {
        return this.targetFlavor;
    }

    /** {@inheritDoc} */
    public Image loadImage(ImageInfo info, Map hints, ImageSessionContext session)
                throws ImageException, IOException {
        if (!this.mime.equals(info.getMimeType())) {
            throw new IllegalArgumentException(
                    "ImageInfo must be from a image with MIME type: " + this.mime);
        }
        Source src = session.needSource(info.getOriginalURI());
        ImageRawStream rawImage = new ImageRawStream(info, getTargetFlavor(),
                ImageUtil.needInputStream(src));
        return rawImage;
    }

}
