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

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.image2.cache.ImageCache;
import org.apache.fop.image2.pipeline.ImageProviderPipeline;
import org.apache.fop.image2.pipeline.PipelineFactory;
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
    
    /** Provides session-independent information */
    private ImageContext imageContext;

    /** The image cache for this instance */
    private ImageCache cache = new ImageCache();
    
    private PipelineFactory pipelineFactory = new PipelineFactory(this);
    
    /**
     * Main constructor.
     * @param context the session-independent context information
     */
    public ImageManager(ImageContext context) {
        this.imageContext = context;
    }
    
    /**
     * Returns the ImageImplRegistry in use by the ImageManager.
     * @return the ImageImplRegistry
     */
    public ImageImplRegistry getRegistry() {
        return this.registry;
    }
    
    /**
     * Returns the ImageCache in use by the ImageManager.
     * @return the ImageCache
     */
    public ImageCache getCache() {
        return this.cache;
    }
    
    /**
     * Returns the PipelineFactory in use by the ImageManager.
     * @return the PipelineFactory
     */
    public PipelineFactory getPipelineFactory() {
        return this.pipelineFactory;
    }
    
    /**
     * Returns an ImageInfo object containing its intrinsic size for a given URI. The ImageInfo
     * is retrieved from an image cache if it has been requested before.
     * @param uri the URI of the image 
     * @param session the session context through which to resolve the URI if the image is not in
     *                the cache
     * @return the ImageInfo object created from the image
     * @throws ImageException If no suitable ImagePreloader can be found to load the image or
     *          if an error occurred while preloading the image.
     * @throws IOException If an I/O error occurs while preloading the image
     */
    public ImageInfo getImageInfo(String uri, ImageSessionContext session)
                throws ImageException, IOException {
        if (getCache() != null) {
            return getCache().needImageInfo(uri, session, this);
        } else {
            return preloadImage(uri, session);
        }
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
     * @param uri the original URI of the image
     * @param session the session context through which to resolve the URI
     * @return the ImageInfo object created from the image
     * @throws ImageException If no suitable ImagePreloader can be found to load the image or
     *          if an error occurred while preloading the image.
     * @throws IOException If an I/O error occurs while preloading the image
     */
    public ImageInfo preloadImage(String uri, ImageSessionContext session)
            throws ImageException, IOException {
        Source src = session.needSource(uri);
        ImageInfo info = preloadImage(uri, src);
        session.returnSource(uri, src);
        return info;
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
     * @param uri the original URI of the image
     * @param src the Source object to load the image from
     * @return the ImageInfo object created from the image
     * @throws ImageException If no suitable ImagePreloader can be found to load the image or
     *          if an error occurred while preloading the image.
     * @throws IOException If an I/O error occurs while preloading the image
     */
    public ImageInfo preloadImage(String uri, Source src)
            throws ImageException, IOException {
        Iterator iter = registry.getPreloaderIterator();
        while (iter.hasNext()) {
            ImagePreloader preloader = (ImagePreloader)iter.next();
            ImageInfo info = preloader.preloadImage(uri, src, imageContext);
            if (info != null) {
                return info;
            }
        }
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
     *                  {@link #getImageInfo(String, ImageSessionContext)})
     * @param flavor the requested image flavor.
     * @param hints a Map of hints to any of the background components or null
     * @param session the session context
     * @return the fully loaded image
     * @throws ImageException If no suitable loader/converter combination is available to fulfill
     *                  the request or if an error occurred while loading the image.
     * @throws IOException If an I/O error occurs
     */
    public Image getImage(ImageInfo info, ImageFlavor flavor, Map hints,
                ImageSessionContext session)
            throws ImageException, IOException {
        if (hints == null) {
            hints = Collections.EMPTY_MAP;
        }
        
        Image img = null;
        ImageProviderPipeline pipeline = getPipelineFactory().newImageConverterPipeline(
                info, flavor);
        if (pipeline != null) {
            img = pipeline.execute(info, hints, session);
        }
        if (img == null) {
            throw new ImageException(
                    "Cannot load image (no suitable loader/converter combination available) for "
                        + info);
        }
        ImageUtil.closeQuietly(session.getSource(info.getOriginalURI()));
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
     *                  {@link #getImageInfo(String, ImageSessionContext)})
     * @param flavors the requested image flavors (in preferred order).
     * @param hints a Map of hints to any of the background components or null
     * @param session the session context
     * @return the fully loaded image
     * @throws ImageException If no suitable loader/converter combination is available to fulfill
     *                  the request or if an error occurred while loading the image.
     * @throws IOException If an I/O error occurs
     */
    public Image getImage(ImageInfo info, ImageFlavor[] flavors, Map hints,
                        ImageSessionContext session)
                throws ImageException, IOException {
        if (hints == null) {
            hints = Collections.EMPTY_MAP;
        }

        Image img = null;
        ImageProviderPipeline[] candidates = getPipelineFactory().determineCandidatePipelines(
                info, flavors);
        ImageProviderPipeline pipeline = choosePipeline(candidates);
        
        if (pipeline != null) {
            img = pipeline.execute(info, hints, session);
        }
        if (img == null) {
            throw new ImageException(
                    "Cannot load image (no suitable loader/converter combination available) for "
                            + info);
        }
        ImageUtil.closeQuietly(session.getSource(info.getOriginalURI()));
        return img;
    }

    /**
     * Loads an image with no hints. See
     * {@link #getImage(ImageInfo, ImageFlavor, Map, ImageSessionContext)} for more
     * information.
     * @param info the ImageInfo instance for the image (obtained by 
     *                  {@link #getImageInfo(String, ImageSessionContext)})
     * @param flavor the requested image flavor.
     * @param session the session context
     * @return the fully loaded image
     * @throws ImageException If no suitable loader/converter combination is available to fulfill
     *                  the request or if an error occurred while loading the image.
     * @throws IOException If an I/O error occurs
     */
    public Image getImage(ImageInfo info, ImageFlavor flavor, ImageSessionContext session)
            throws ImageException, IOException {
        return getImage(info, flavor, ImageUtil.getDefaultHints(session), session);
    }

    /**
     * Loads an image with no hints. See
     * {@link #getImage(ImageInfo, ImageFlavor[], Map, ImageSessionContext)} for more
     * information.
     * @param info the ImageInfo instance for the image (obtained by 
     *                  {@link #getImageInfo(String, ImageSessionContext)})
     * @param flavors the requested image flavors (in preferred order).
     * @param session the session context
     * @return the fully loaded image
     * @throws ImageException If no suitable loader/converter combination is available to fulfill
     *                  the request or if an error occurred while loading the image.
     * @throws IOException If an I/O error occurs
     */
    public Image getImage(ImageInfo info, ImageFlavor[] flavors, ImageSessionContext session)
            throws ImageException, IOException {
        return getImage(info, flavors, ImageUtil.getDefaultHints(session), session);
    }
    
    /**
     * Converts an image. The caller can indicate what kind of image flavors are requested. When
     * this method is called the code looks for a suitable combination of ImageConverters so it
     * can return the image in exactly the form the caller needs.
     * The array of image flavors is ordered, so the first image flavor is given highest priority.
     * <p>
     * Optionally, it is possible to pass in Map of hints. These hints may be used by
     * ImageConverters to act on the image. See {@link ImageProcessingHints} for common hints
     * used by the bundled implementations. You can, of course, define your own hints.
     * @param image the image to convert
     * @param flavors the requested image flavors (in preferred order).
     * @param hints a Map of hints to any of the background components or null
     * @return the fully loaded image
     * @throws ImageException If no suitable loader/converter combination is available to fulfill
     *                  the request or if an error occurred while loading the image.
     * @throws IOException If an I/O error occurs
     */
    public Image convertImage(Image image, ImageFlavor[] flavors, Map hints)
                throws ImageException, IOException {
        if (hints == null) {
            hints = Collections.EMPTY_MAP;
        }
        ImageInfo info = image.getInfo();
        
        Image img = null;
        int count = flavors.length;
        for (int i = 0; i < count; i++) {
            if (image.getFlavor().equals(flavors[i])) {
                //Shortcut (the image is already in one of the requested formats)
                return image;
            }
        }
        ImageProviderPipeline[] candidates = getPipelineFactory().determineCandidatePipelines(
                image, flavors);
        ImageProviderPipeline pipeline = choosePipeline(candidates);
        
        if (pipeline != null) {
            img = pipeline.execute(info, image, hints, null);
        }
        if (img == null) {
            throw new ImageException(
                    "Cannot convert image " + image 
                    + " (no suitable converter combination available)");
        }
        return img;
    }

    /**
     * Converts an image with no hints. See
     * {@link #convertImage(Image, ImageFlavor[], Map)} for more
     * information.
     * @param image the image to convert
     * @param flavors the requested image flavors (in preferred order).
     * @return the fully loaded image
     * @throws ImageException If no suitable loader/converter combination is available to fulfill
     *                  the request or if an error occurred while loading the image.
     * @throws IOException If an I/O error occurs
     */
    public Image convertImage(Image image, ImageFlavor[] flavors)
                throws ImageException, IOException {
        return convertImage(image, flavors, null);
    }
    
    /**
     * Chooses the best {@link ImageProviderPipeline} from a set of candidates.
     * @param candidates the candidates
     * @return the best pipeline
     */
    public ImageProviderPipeline choosePipeline(ImageProviderPipeline[] candidates) {
        ImageProviderPipeline pipeline = null;
        int minPenalty = Integer.MAX_VALUE;
        int count = candidates.length;
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
        if (log.isDebugEnabled()) {
            log.debug("Chosen pipeline: " + pipeline);
        }
        return pipeline;
    }
    
}
