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

import org.apache.fop.apps.MimeConstants;
import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.image2.spi.ImageLoader;
import org.apache.fop.image2.spi.ImageLoaderFactory;

/**
 * Factory class for the ImageLoader for raw/undecoded images.
 */
public class ImageLoaderFactoryRaw implements ImageLoaderFactory {

    /** MIME type for EMF (Windows Enhanced Metafile) */
    public static final String MIME_EMF = "image/x-emf"; 
    
    private static final String[] MIMES = new String[] {
        MimeConstants.MIME_PNG,
        MimeConstants.MIME_JPEG,
        MIME_EMF};
    
    private static final ImageFlavor[][] FLAVORS = new ImageFlavor[][] {
        {ImageFlavor.RAW_PNG}, {ImageFlavor.RAW_JPEG}, {ImageFlavor.RAW_EMF}};

    
    /**
     * Returns the MIME type for a given ImageFlavor if it is from a format that is consumed
     * without being undecoded. If the ImageFlavor is no raw flavor, an IllegalArgumentException
     * is thrown.
     * @param flavor the image flavor
     * @return the associated MIME type
     */
    public static String getMimeForRawFlavor(ImageFlavor flavor) {
        for (int i = 0, ci = FLAVORS.length; i < ci; i++) {
            for (int j = 0, cj = FLAVORS[i].length; j < cj; j++) {
                if (FLAVORS[i][j].equals(flavor)) {
                    return MIMES[i];
                }
            }
        }
        throw new IllegalArgumentException("ImageFlavor is not a \"raw\" flavor: " + flavor);
    }

    /** {@inheritDoc} */
    public String[] getSupportedMIMETypes() {
        return MIMES;
    }
    
    /** {@inheritDoc} */
    public ImageFlavor[] getSupportedFlavors(String mime) {
        for (int i = 0, c = MIMES.length; i < c; i++) {
            if (MIMES[i].equals(mime)) {
                return FLAVORS[i];
            }
        }
        throw new IllegalArgumentException("Unsupported MIME type: " + mime);
    }
    
    /** {@inheritDoc} */
    public ImageLoader newImageLoader(ImageFlavor targetFlavor) {
        return new ImageLoaderRaw(targetFlavor);
    }
    
    /** {@inheritDoc} */
    public int getUsagePenalty(String mime, ImageFlavor flavor) {
        return 0;
    }

    /** {@inheritDoc} */
    public boolean isAvailable() {
        return true;
    }

}
