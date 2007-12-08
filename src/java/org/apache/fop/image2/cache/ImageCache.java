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
 
package org.apache.fop.image2.cache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.xml.transform.Source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.image2.Image;
import org.apache.fop.image2.ImageException;
import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.image2.ImageInfo;
import org.apache.fop.image2.ImageManager;
import org.apache.fop.image2.ImageSessionContext;
import org.apache.fop.image2.util.SoftMapCache;

/**
 * This class provides a cache for images. The main key into the images is the original URI the
 * image was accessed with.
 * <p>
 * Don't use one ImageCache instance in the context of multiple base URIs because relative URIs
 * would not work correctly anymore.
 */
public class ImageCache {

    /** logger */
    protected static Log log = LogFactory.getLog(ImageCache.class);
    
    private Set invalidURIs = Collections.synchronizedSet(new java.util.HashSet());
    
    private SoftMapCache imageInfos = new SoftMapCache(true);
    private SoftMapCache images = new SoftMapCache(true);
    
    private ImageCacheListener cacheListener;

    /**
     * Sets an ImageCacheListener instance so the events in the image cache can be observed.
     * @param listener the listener instance
     */
    public void setCacheListener(ImageCacheListener listener) {
        this.cacheListener = listener;
    }
    
    /**
     * Returns an ImageInfo instance for a given URI.
     * @param uri the image's URI
     * @param session the session context
     * @param manager the ImageManager handling the images
     * @return the ImageInfo instance
     * @throws ImageException if an error occurs while parsing image data
     * @throws IOException if an I/O error occurs while loading image data
     */
    public ImageInfo needImageInfo(String uri, ImageSessionContext session, ImageManager manager)
            throws ImageException, IOException {
        //Fetch unique version of the URI and use it for synchronization so we have some sort of
        //"row-level" locking instead of "table-level" locking (to use a database analogy).
        //The fine locking strategy is necessary since preloading an image is a potentially long
        //operation.
        if (isInvalidURI(uri)) {
            throw new FileNotFoundException("Image not found: " + uri);
        }
        String lockURI = uri.intern();
        synchronized (lockURI) {
            ImageInfo info = getImageInfo(uri);
            if (info == null) {
                try {
                    Source src = session.needSource(uri);
                    if (src == null) {
                        registerInvalidURI(uri);
                        throw new FileNotFoundException("Image not found: " + uri);
                    }
                    info = manager.preloadImage(uri, src);
                    session.returnSource(uri, src);
                } catch (IOException ioe) {
                    registerInvalidURI(uri);
                    throw ioe;
                } catch (ImageException e) {
                    registerInvalidURI(uri);
                    throw e;
                }
                putImageInfo(info);
            }
            return info;
        }
    }
    
    /**
     * Indicates whether a URI has previously been identified as an invalid URI.
     * @param uri the image's URI
     * @return true if the URI is invalid
     */
    public boolean isInvalidURI(String uri) {
        if (invalidURIs.contains(uri)) {
            if (cacheListener != null) {
                cacheListener.invalidHit(uri);
            }
            return true;
        }
        return false;
    }
    
    /**
     * Returns an ImageInfo instance from the cache or null if none is found.
     * @param uri the image's URI
     * @return the ImageInfo instance or null if the requested information is not in the cache
     */
    protected ImageInfo getImageInfo(String uri) {
        ImageInfo info = (ImageInfo)imageInfos.get(uri);
        if (cacheListener != null) {
            if (info != null) {
                cacheListener.cacheHitImageInfo(uri);
            } else {
                if (!isInvalidURI(uri)) {
                    cacheListener.cacheMissImageInfo(uri);
                }
            }
        }
        return info;
    }
    
    /**
     * Registers an ImageInfo instance with the cache.
     * @param info the ImageInfo instance
     */
    protected void putImageInfo(ImageInfo info) {
        //An already existing ImageInfo is replaced.
        imageInfos.put(info.getOriginalURI(), info);
    }
    
    /**
     * Registers a URI as invalid so getImageInfo can indicate that quickly with no I/O access.
     * @param uri the URI of the invalid image
     */
    private void registerInvalidURI(String uri) {
        synchronized (invalidURIs) {
            // cap size of invalid list
            if (invalidURIs.size() > 100) {
                invalidURIs.clear();
            }
            invalidURIs.add(uri);
        }
    }
    
    /**
     * Returns an image from the cache or null if it wasn't found.
     * @param info the ImageInfo instance representing the image
     * @param flavor the requested ImageFlavor for the image
     * @return the requested image or null if the image is not in the cache
     */
    public Image getImage(ImageInfo info, ImageFlavor flavor) {
        return getImage(info.getOriginalURI(), flavor);
    }
    
    /**
     * Returns an image from the cache or null if it wasn't found.
     * @param uri the image's URI
     * @param flavor the requested ImageFlavor for the image
     * @return the requested image or null if the image is not in the cache
     */
    public Image getImage(String uri, ImageFlavor flavor) {
        if (uri == null || "".equals(uri)) {
            return null;
        }
        ImageKey key = new ImageKey(uri, flavor);
        Image img = (Image)images.get(key);
        if (cacheListener != null) {
            if (img != null) {
                cacheListener.cacheHitImage(key);
            } else {
                cacheListener.cacheMissImage(key);
            }
        }
        return img;
    }
    
    /**
     * Registers an image with the cache.
     * @param img the image
     */
    public void putImage(Image img) {
        String originalURI = img.getInfo().getOriginalURI();
        if (originalURI == null || "".equals(originalURI)) {
            return; //Don't cache if there's no URI
        }
        //An already existing Image is replaced.
        if (!img.isCacheable()) {
            throw new IllegalArgumentException(
                    "Image is not cacheable! (Flavor: " + img.getFlavor() + ")");
        }
        ImageKey key = new ImageKey(originalURI, img.getFlavor());
        images.put(key, img);
    }

    /**
     * Clears the image cache (all ImageInfo and Image objects).
     */
    public void clearCache() {
        invalidURIs.clear();
        imageInfos.clear();
        images.clear();
        doHouseKeeping();
    }
    
    /**
     * Triggers some house-keeping, i.e. removes stale entries.
     */
    public void doHouseKeeping() {
        imageInfos.doHouseKeeping();
        images.doHouseKeeping();
    }
    
}
