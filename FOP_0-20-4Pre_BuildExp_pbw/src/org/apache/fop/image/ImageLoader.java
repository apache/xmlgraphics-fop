/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image;

import org.apache.fop.configuration.Configuration;
import org.apache.fop.fo.FOUserAgent;

/**
 * Class to load images.
 */
class ImageLoader {
    String url;
    ImageCache cache;
    boolean valid = true;
    FOUserAgent userAgent;
    FopImage image = null;

    public ImageLoader(String u, ImageCache c, FOUserAgent ua) {
        url = u;
        cache = c;
        userAgent = ua;
    }

    public synchronized FopImage loadImage() {
        if (!valid || image != null) {
            return image;
        }
        String base = userAgent.getBaseURL();
        image = ImageFactory.loadImage(url, base, userAgent);
        if (image == null) {
            cache.invalidateImage(url, userAgent);
            valid = false;
        }
        return image;
    }

}
