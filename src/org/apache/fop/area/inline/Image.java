/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.area.Area;

/**
 * Image area for external-graphic.
 * This area holds information for rendering an image.
 * The url of the image is used as a key to reference the image cache.
 */
public class Image extends Area {
    private String url;

    /**
     * Create a new image with the given url.
     *
     * @param u the url of the image
     */
    public Image(String u) {
        url = u;
    }

    /**
     * Get the url of this image.
     * This url is used as a key to locate the actual image data.
     *
     * @return the url of this image
     */
    public String getURL() {
        return url;
    }

}
