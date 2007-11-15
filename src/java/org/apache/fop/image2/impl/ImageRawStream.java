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

import java.io.InputStream;

import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.image2.ImageInfo;
import org.apache.fop.image2.util.ImageUtil;

/**
 * This class is an implementation of the Image interface exposing an InputStream for loading the
 * raw/undecoded image.
 */
public class ImageRawStream extends AbstractImage {

    private ImageFlavor flavor;
    private InputStream in;
    
    /**
     * Constructor for use with ImageLoaders.
     * @param info the image info object
     * @param flavor the image flavor for the raw image
     */
    public ImageRawStream(ImageInfo info, ImageFlavor flavor) {
        this(info, flavor, ImageUtil.needInputStream(info.getSource()));
    }
    
    /**
     * Main constructor.
     * @param info the image info object
     * @param flavor the image flavor for the raw image
     * @param in the InputStream with the raw content
     */
    public ImageRawStream(ImageInfo info, ImageFlavor flavor, InputStream in) {
        super(info);
        this.flavor = flavor;
        this.in = in;
    }
    
    /** {@inheritDoc} */
    public ImageFlavor getFlavor() {
        return this.flavor;
    }

    /**
     * Returns an InputStream to access the raw image.
     * @return the InputStream
     */
    public InputStream getInputStream() {
        return this.in;
    }
}
