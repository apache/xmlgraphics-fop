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
 
package org.apache.fop.image2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.image2.spi.ImageConverterPipeline;
import org.apache.fop.image2.spi.ImageImplRegistry;
import org.apache.fop.image2.spi.ImagePreloader;
import org.apache.fop.image2.util.ImageUtil;

/**
 * ImageManager is the central starting point for image access.
 */
public class ImageManager {

    /** logger */
    protected static Log log = LogFactory.getLog(ImageManager.class);

    /** Holds all registered interface implementations for the image package */
    private ImageImplRegistry registry = ImageImplRegistry.getDefaultInstance();
    
    /**
     * Returns the ImageImplRegistry in use by the ImageManager.
     * @return the ImageImplRegistry
     */
    public ImageImplRegistry getRegistry() {
        return this.registry;
    }
    
    protected ImageSource newImageSource(String uri, FOUserAgent userAgent) {
        Source source = userAgent.resolveURI(uri);
        if (source == null) {
            if (log.isDebugEnabled()) {
                log.debug("URI could not be resolved: " + uri);
            }
            return null;
        }
        
        ImageSource imageSource = null;
        
        String resolvedURI = source.getSystemId();
        URL url;
        try {
            url = new URL(resolvedURI);
        } catch (MalformedURLException e) {
            url = null;
        } 
        File f = FileUtils.toFile(url);
        if (f != null) {
            boolean directFileAccess = true;
            InputStream in = null;
            if (source instanceof StreamSource) {
                StreamSource streamSource = (StreamSource)source; 
                in = streamSource.getInputStream();
                in = ImageUtil.decorateMarkSupported(in);
                try {
                    if (ImageUtil.isGZIPCompressed(in)) {
                        //GZIPped stream are not seekable, so buffer/cache like other URLs
                        directFileAccess = false;
                    }
                } catch (IOException ioe) {
                    log.error("Error while checking the InputStream for GZIP compression."
                            + " Could not load image from system identifier '"
                            + source.getSystemId() + "' (" + ioe.getMessage() + ")");
                    return null;
                }
            }
            
            if (directFileAccess) {
                //Close as the file is reopened in a more optimal way
                IOUtils.closeQuietly(in);
                try {
                    imageSource = new ImageSource(ImageIO.createImageInputStream(f), resolvedURI);
                } catch (IOException ioe) {
                    log.error("Unable to create ImageInputStream for local file"
                            + " from system identifier '"
                            + source.getSystemId() + "' (" + ioe.getMessage() + ")");
                }
            }
        }
        
        if (imageSource == null) {
            // Got a valid source, obtain an InputStream from it
            InputStream in = null;
            if (source instanceof StreamSource) {
                in = ((StreamSource)source).getInputStream();
            }
            if (in == null && url != null) {
                try {
                    in = url.openStream();
                } catch (Exception ex) {
                    log.error("Unable to obtain stream from system identifier '" 
                        + source.getSystemId() + "'");
                }
            }
            if (in == null) {
                log.error("The Source that was returned from URI resolution didn't contain"
                        + " an InputStream for URI: " + uri);
                return null;
            }

            try {
                //Buffer and uncompress if necessary
                in = ImageUtil.autoDecorateInputStream(in);
                imageSource = new ImageSource(
                        ImageIO.createImageInputStream(in), source.getSystemId());
            } catch (IOException ioe) {
                log.error("Unable to create ImageInputStream for InputStream"
                        + " from system identifier '"
                        + source.getSystemId() + "' (" + ioe.getMessage() + ")");
            }
        }
        return imageSource;
    }
    
    /**
     * Preloads an image, i.e. the format of the image is identified and some basic information
     * (MIME type, intrinsic size and possibly other values) are loaded and returned as an
     * ImageInfo object. Note that the image is not fully loaded normally. Only with certain formats
     * the image is already fully loaded and references added to the ImageInfo's custom objects
     * (see {@link ImageInfo#getOriginalImage()}).
     * <p>
     * The reason for the preloading: Apache FOP, for example, only needs the image's intrinsic
     * size during layout. Only when the document is rendered to the final format does FOP need
     * to load the full image. Like this a lot of memory can be saved.
     * @param uri the URI to load the image from
     * @param userAgent the user agent used during the loading process
     * @return the ImageInfo object created from the image
     * @throws ImageException If no suitable ImagePreloader can be found to load the image or
     *          if an error occurred while preloading the image.
     * @throws IOException If an I/O error occurs while preloading the image
     */
    public ImageInfo preloadImage(String uri, FOUserAgent userAgent)
                throws ImageException, IOException {
        ImageSource imageSource = newImageSource(uri, userAgent);
        return preloadImage(uri, imageSource, userAgent);
    }

    private ImageInfo preloadImage(String uri, Source src, FOUserAgent userAgent)
            throws ImageException, IOException {
        Iterator iter = registry.getPreloaderIterator();
        while (iter.hasNext()) {
            ImagePreloader preloader = (ImagePreloader)iter.next();
            ImageInfo info = preloader.preloadImage(uri, src, userAgent);
            if (info != null) {
                return info;
            }
        }
        ImageUtil.closeQuietly(src);
        throw new ImageException("No ImagePreloader found for " + uri);
    }

    /**
     * Loads an image. The caller can indicate what kind of image flavor is requested. When this
     * method is called the code looks for a suitable ImageLoader and, if necessary, builds
     * a conversion pipeline so it can return the image in exactly the form the caller needs.
     * <p>
     * Optionally, it is possible to pass in Map of hints. These hints may be used by ImageLoaders
     * and ImageConverters to act on the image. See {@link ImageProcessingHints} for common hints
     * used by the bundled implementations. You can, of course, define your own hints.
     * @param info the ImageInfo instance for the image (obtained by 
     *                  {@link #preloadImage(String, FOUserAgent)})
     * @param flavor the requested image flavor.
     * @param hints a Map of hints to any of the background components or null
     * @return the fully loaded image
     * @throws ImageException If no suitable loader/converter combination is available to fulfill
     *                  the request or if an error occurred while loading the image.
     * @throws IOException If an I/O error occurs
     */
    public Image getImage(ImageInfo info, ImageFlavor flavor, Map hints)
            throws ImageException, IOException {
        if (hints == null) {
            hints = Collections.EMPTY_MAP;
        }
        String mime = info.getMimeType();
        
        Image img = null;
        ImageConverterPipeline pipeline = registry.newImageConverterPipeline(mime, flavor);
        if (pipeline != null) {
            img = pipeline.execute(info, hints);
        }
        if (img == null) {
            throw new ImageException(
                    "Cannot load image (no suitable loader/converter combination available) for "
                        + info);
        }
        return img;
    }
    
    /**
     * Loads an image. The caller can indicate what kind of image flavors are requested. When this
     * method is called the code looks for a suitable ImageLoader and, if necessary, builds
     * a conversion pipeline so it can return the image in exactly the form the caller needs.
     * The array of image flavors is ordered, so the first image flavor is given highest priority.
     * <p>
     * Optionally, it is possible to pass in Map of hints. These hints may be used by ImageLoaders
     * and ImageConverters to act on the image. See {@link ImageProcessingHints} for common hints
     * used by the bundled implementations. You can, of course, define your own hints.
     * @param info the ImageInfo instance for the image (obtained by 
     *                  {@link #preloadImage(String, FOUserAgent)})
     * @param flavors the requested image flavors (in preferred order).
     * @param hints a Map of hints to any of the background components or null
     * @return the fully loaded image
     * @throws ImageException If no suitable loader/converter combination is available to fulfill
     *                  the request or if an error occurred while loading the image.
     * @throws IOException If an I/O error occurs
     */
    public Image getImage(ImageInfo info, ImageFlavor[] flavors, Map hints)
                throws ImageException, IOException {
        if (hints == null) {
            hints = Collections.EMPTY_MAP;
        }
        String mime = info.getMimeType();

        Image img = null;
        int count = flavors.length;
        ImageConverterPipeline[] candidates = new ImageConverterPipeline[count];
        for (int i = 0; i < count; i++) {
            candidates[i] = registry.newImageConverterPipeline(mime, flavors[i]);
        }
        ImageConverterPipeline pipeline = null;
        int minPenalty = Integer.MAX_VALUE;
        for (int i = count - 1; i >= 0; i--) {
            if (candidates[i] == null) {
                continue;
            }
            int penalty = candidates[i].getConversionPenalty();
            if (penalty <= minPenalty) {
                pipeline = candidates[i];
                minPenalty = penalty;
            }
        }    
        if (pipeline != null) {
            img = pipeline.execute(info, hints);
        }
        if (img == null) {
            throw new ImageException(
                    "Cannot load image (no suitable loader/converter combination available) for "
                            + info);
        }
        return img;
    }

    /**
     * Loads an image with no hints. See {@link #getImage(ImageInfo, ImageFlavor, Map)} for more
     * information.
     * @param info the ImageInfo instance for the image (obtained by 
     *                  {@link #preloadImage(String, FOUserAgent)})
     * @param flavor the requested image flavor.
     * @return the fully loaded image
     * @throws ImageException If no suitable loader/converter combination is available to fulfill
     *                  the request or if an error occurred while loading the image.
     * @throws IOException If an I/O error occurs
     */
    public Image getImage(ImageInfo info, ImageFlavor flavor)
            throws ImageException, IOException {
        return getImage(info, flavor, null);
    }

    /**
     * Loads an image with no hints. See {@link #getImage(ImageInfo, ImageFlavor[], Map)} for more
     * information.
     * @param info the ImageInfo instance for the image (obtained by 
     *                  {@link #preloadImage(String, FOUserAgent)})
     * @param flavors the requested image flavors (in preferred order).
     * @return the fully loaded image
     * @throws ImageException If no suitable loader/converter combination is available to fulfill
     *                  the request or if an error occurred while loading the image.
     * @throws IOException If an I/O error occurs
     */
    public Image getImage(ImageInfo info, ImageFlavor[] flavors)
            throws ImageException, IOException {
        return getImage(info, flavors, null);
    }
    
}
