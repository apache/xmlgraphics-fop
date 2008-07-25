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

package org.apache.fop.image.loader.batik;

import org.apache.batik.transcoder.wmf.tosvg.WMFRecordStore;

import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.impl.AbstractImage;

/**
 * This class is an implementation of the Image interface exposing a RenderedImage.
 */
public class ImageWMF extends AbstractImage {

    /** MIME type for WMF */
    public static final String MIME_WMF = "image/x-wmf";

    /** ImageFlavor for Batik's WMFRecordStore */
    public static final ImageFlavor WMF_IMAGE = new ImageFlavor("WMFRecordStore");

    private WMFRecordStore store;

    /**
     * Main constructor.
     * @param info the image info object
     * @param store the WMF record store containing the loaded WMF file
     */
    public ImageWMF(ImageInfo info, WMFRecordStore store) {
        super(info);
        this.store = store;
    }

    /** {@inheritDoc} */
    public ImageFlavor getFlavor() {
        return WMF_IMAGE;
    }

    /** {@inheritDoc} */
    public boolean isCacheable() {
        return true;
    }

    /**
     * Returns the contained WMF record store.
     * @return the WMFRecordStore
     */
    public WMFRecordStore getRecordStore() {
        return this.store;
    }

}
