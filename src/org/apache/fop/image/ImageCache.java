/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image;

// FOP
import org.apache.fop.fo.FOUserAgent;

/**
 * Image cache holder.
 * This interface is used for caching images.
 */
public interface ImageCache {
    /**
     * Get an image from the cache.
     *
     * @param url the url and key for the image
     * @param context the user agent context
     */
    public FopImage getImage(String url, FOUserAgent context);

    /**
     * Release an image in the current context.
     *
     * @param url the url and key for the image
     * @param context the user agent context
     */
    public void releaseImage(String url, FOUserAgent context);

    /**
     * Invalidate image.
     * If during loading this image is found to be invalid
     * it will be invalidated to prevent further attempts at
     * loading the image.
     *
     * @param url the url and key for the image
     * @param context the user agent context
     */
    public void invalidateImage(String url, FOUserAgent context);

    /**
     * Remove a context and handle all images in the context.
     *
     * @param context the user agent context
     */
    public void removeContext(FOUserAgent context);
}

