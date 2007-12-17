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
 
package org.apache.fop.image2.pipeline;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.image2.Image;
import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.image2.ImageInfo;
import org.apache.fop.image2.ImageManager;
import org.apache.fop.image2.impl.CompositeImageLoader;
import org.apache.fop.image2.spi.ImageConverter;
import org.apache.fop.image2.spi.ImageImplRegistry;
import org.apache.fop.image2.spi.ImageLoader;
import org.apache.fop.image2.spi.ImageLoaderFactory;
import org.apache.fop.util.dijkstra.DefaultEdgeDirectory;
import org.apache.fop.util.dijkstra.DijkstraAlgorithm;
import org.apache.fop.util.dijkstra.Vertex;

/**
 * Factory class for image processing pipelines.
 */
public class PipelineFactory {

    /** logger */
    protected static Log log = LogFactory.getLog(PipelineFactory.class);

    private ImageManager manager;
    
    private int converterEdgeDirectoryVersion = -1;
    
    /** Holds the EdgeDirectory for all image conversions */
    private DefaultEdgeDirectory converterEdgeDirectory;
    
    /**
     * Main constructor.
     * @param manager the ImageManager instance
     */
    public PipelineFactory(ImageManager manager) {
        this.manager = manager;
    }
    
    private DefaultEdgeDirectory getEdgeDirectory() {
        ImageImplRegistry registry = manager.getRegistry();
        if (registry.getImageConverterModifications() != converterEdgeDirectoryVersion) {
            Collection converters = registry.getImageConverters();
            
            //Rebuild edge directory
            DefaultEdgeDirectory dir = new DefaultEdgeDirectory();
            Iterator iter = converters.iterator();
            while (iter.hasNext()) {
                ImageConverter converter = (ImageConverter)iter.next();
                dir.addEdge(new ImageConversionEdge(converter));
            }
            
            converterEdgeDirectoryVersion = registry.getImageConverterModifications();
            this.converterEdgeDirectory = dir; //Replace (thread-safe)
        }
        return this.converterEdgeDirectory;
    }
    
    /**
     * Creates and returns an {@link ImageProviderPipeline} that allows to load an image of the
     * given MIME type and present it in the requested image flavor.
     * @param originalImage the original image that serves as the origin point of the conversion
     * @param targetFlavor the requested image flavor
     * @return an {@link ImageProviderPipeline} or null if no suitable pipeline could be assembled
     */
    public ImageProviderPipeline newImageConverterPipeline(
                Image originalImage, ImageFlavor targetFlavor) {
        //Get snapshot to avoid concurrent modification problems (thread-safety)
        DefaultEdgeDirectory dir = getEdgeDirectory();
        ImageRepresentation destination = new ImageRepresentation(targetFlavor);
        ImageProviderPipeline pipeline = findPipeline(dir, originalImage.getFlavor(), destination);
        return pipeline;
    }
    
    /**
     * Creates and returns an {@link ImageProviderPipeline} that allows to load an image of the
     * given MIME type and present it in the requested image flavor.
     * @param imageInfo the image info object of the original image
     * @param targetFlavor the requested image flavor
     * @return an {@link ImageProviderPipeline} or null if no suitable pipeline could be assembled
     */
    public ImageProviderPipeline newImageConverterPipeline(
                ImageInfo imageInfo, ImageFlavor targetFlavor) {
        String originalMime = imageInfo.getMimeType();
        ImageImplRegistry registry = manager.getRegistry();
        ImageProviderPipeline pipeline = null;
        
        //Get snapshot to avoid concurrent modification problems (thread-safety)
        DefaultEdgeDirectory dir = getEdgeDirectory();
        
        ImageLoaderFactory[] loaderFactories = registry.getImageLoaderFactories(
                imageInfo, targetFlavor);
        if (loaderFactories != null) {
            //Directly load image and return it
            ImageLoader loader;
            if (loaderFactories.length == 1) {
                 loader = loaderFactories[0].newImageLoader(targetFlavor);
            } else {
                int count = loaderFactories.length;
                ImageLoader[] loaders = new ImageLoader[count];
                for (int i = 0; i < count; i++) {
                    loaders[i] = loaderFactories[i].newImageLoader(targetFlavor);
                }
                loader = new CompositeImageLoader(loaders);
            }
            pipeline = new ImageProviderPipeline(manager.getCache(), loader);
        } else {
            //Need to use ImageConverters
            if (log.isTraceEnabled()) {
                log.trace("No ImageLoaderFactory found that can load this format directly."
                        + " Trying ImageConverters instead...");
            }
            
            ImageRepresentation destination = new ImageRepresentation(targetFlavor);
            //Get Loader for originalMIME
            // --> List of resulting flavors, possibly multiple loaders
            loaderFactories = registry.getImageLoaderFactories(originalMime);
            if (loaderFactories != null) {
                SortedSet candidates = new java.util.TreeSet(new PipelineComparator());
                //Find best pipeline -> best loader
                for (int i = 0, ci = loaderFactories.length; i < ci; i++) {
                    ImageLoaderFactory loaderFactory = loaderFactories[i];
                    ImageFlavor[] flavors = loaderFactory.getSupportedFlavors(originalMime);
                    for (int j = 0, cj = flavors.length; j < cj; j++) {
                        pipeline = findPipeline(dir, flavors[j], destination);
                        if (pipeline != null) {
                            ImageLoader loader = loaderFactory.newImageLoader(flavors[j]);
                            pipeline.setImageLoader(loader);
                            candidates.add(pipeline);
                        }
                    }
                }
                
                //Build final pipeline
                if (candidates.size() > 0) {
                    pipeline = (ImageProviderPipeline)candidates.first();
                }
            }
        }
        if (pipeline != null && log.isDebugEnabled()) {
            log.debug("Pipeline: " + pipeline + " with penalty " + pipeline.getConversionPenalty());
        }
        return pipeline;
    }
    
    private static class PipelineComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            ImageProviderPipeline p1 = (ImageProviderPipeline)o1;
            ImageProviderPipeline p2 = (ImageProviderPipeline)o2;
            //Lowest penalty first
            return p1.getConversionPenalty() - p2.getConversionPenalty();
        }
        
    }
    
    private ImageProviderPipeline findPipeline(DefaultEdgeDirectory dir,
            ImageFlavor originFlavor, ImageRepresentation destination) {
        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(
                dir);
        ImageRepresentation origin = new ImageRepresentation(originFlavor); 
        dijkstra.execute(origin, destination);
        if (log.isTraceEnabled()) {
            log.trace("Lowest penalty: " + dijkstra.getLowestPenalty(destination));
        }
        
        Vertex prev = destination;
        Vertex pred = dijkstra.getPredecessor(destination);
        if (pred == null) {
            if (log.isTraceEnabled()) {
                log.trace("No route found!");
            }
            return null;
        } else {
            LinkedList stops = new LinkedList();
            while ((pred = dijkstra.getPredecessor(prev)) != null) {
                ImageConversionEdge edge = (ImageConversionEdge)
                        dir.getBestEdge(pred, prev);
                stops.addFirst(edge);
                prev = pred;
            }
            ImageProviderPipeline pipeline = new ImageProviderPipeline(manager.getCache(), null);
            Iterator iter = stops.iterator();
            while (iter.hasNext()) {
                ImageConversionEdge edge = (ImageConversionEdge)iter.next(); 
                pipeline.addConverter(edge.getImageConverter());
            }
            return pipeline;
        }
    }
    
    /**
     * Finds and returns an array of {@link ImageProviderPipeline} instances which can handle
     * the given MIME type and return one of the given {@link ImageFlavor}s.
     * @param imageInfo the image info object
     * @param flavors the possible target flavors
     * @return an array of pipelines
     */
    public ImageProviderPipeline[] determineCandidatePipelines(ImageInfo imageInfo,
            ImageFlavor[] flavors) {
        int count = flavors.length;
        ImageProviderPipeline[] candidates = new ImageProviderPipeline[count];
        for (int i = 0; i < count; i++) {
            candidates[i] = newImageConverterPipeline(imageInfo, flavors[i]);
        }
        return candidates;
    }
    
    /**
     * Finds and returns an array of {@link ImageProviderPipeline} instances which can handle
     * the convert the given {@link Image} and return one of the given {@link ImageFlavor}s.
     * @param sourceImage the image to be converted
     * @param flavors the possible target flavors
     * @return an array of pipelines
     */
    public ImageProviderPipeline[] determineCandidatePipelines(Image sourceImage,
            ImageFlavor[] flavors) {
        int count = flavors.length;
        ImageProviderPipeline[] candidates = new ImageProviderPipeline[count];
        for (int i = 0; i < count; i++) {
            candidates[i] = newImageConverterPipeline(sourceImage, flavors[i]);
        }
        return candidates;
    }
    
    
}
