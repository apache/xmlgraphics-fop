/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.fop.apps.FOUserAgent;

/**
 * Class to load images.
 */
class ImageLoader {
    
    private String url;
    private ImageCache cache;
    private boolean valid = true;
    private FOUserAgent userAgent;
    private FopImage image = null;

    /**
     * Main constructor.
     * @param url URL to the image
     * @param cache Image cache
     * @param ua User agent
     */
    public ImageLoader(String url, ImageCache cache, FOUserAgent ua) {
        this.url = url;
        this.cache = cache;
        this.userAgent = ua;
    }

    /**
     * Loads the image.
     * @return the loaded image
     */
    public synchronized FopImage loadImage() {
        if (!valid || image != null) {
            return image;
        }
        image = ImageFactory.getInstance().loadImage(url, userAgent);
        if (image == null) {
            cache.invalidateImage(url, userAgent);
            valid = false;
        }
        return image;
    }

}
