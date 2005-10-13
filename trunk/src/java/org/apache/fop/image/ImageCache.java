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

// FOP
import org.apache.fop.apps.FOUserAgent;

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
     * @return the requested image
     */
    FopImage getImage(String url, FOUserAgent context);

    /**
     * Release an image in the current context.
     *
     * @param url the url and key for the image
     * @param context the user agent context
     */
    void releaseImage(String url, FOUserAgent context);

    /**
     * Invalidate image.
     * If during loading this image is found to be invalid
     * it will be invalidated to prevent further attempts at
     * loading the image.
     *
     * @param url the url and key for the image
     * @param context the user agent context
     */
    void invalidateImage(String url, FOUserAgent context);

    /**
     * Remove a context and handle all images in the context.
     *
     * @param context the user agent context
     */
    void removeContext(FOUserAgent context);
    
    /**
     * Forces the cache to fully cleared.
     */
    void clearAll();
    
}

