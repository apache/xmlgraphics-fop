/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image;

import org.apache.fop.fo.FOUserAgent;

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
        image = ImageFactory.loadImage(url, userAgent);
        if (image == null) {
            cache.invalidateImage(url, userAgent);
            valid = false;
        }
        return image;
    }

}
