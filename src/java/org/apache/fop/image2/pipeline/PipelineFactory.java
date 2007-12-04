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
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.image2.ImageManager;
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
     * Creates and returns an ImageConverterPipeline that allows to load an image of the given
     * MIME type and present it in the requested image flavor.
     * @param originalMime the MIME type of the original image
     * @param targetFlavor the requested image flavor
     * @return an ImageConverterPipeline or null if no suitable pipeline could be assembled
     */
    public ImageProviderPipeline newImageConverterPipeline(
                String originalMime, ImageFlavor targetFlavor) {
        ImageImplRegistry registry = manager.getRegistry();
        ImageProviderPipeline pipeline = null;
        
        //Get snapshot to avoid concurrent modification problems (thread-safety)
        DefaultEdgeDirectory dir = getEdgeDirectory();
        
        ImageLoaderFactory loaderFactory = registry.getImageLoaderFactory(
                originalMime, targetFlavor);
        if (loaderFactory != null) {
            //Directly load image and return it
            ImageLoader loader = loaderFactory.newImageLoader(targetFlavor);
            pipeline = new ImageProviderPipeline(manager.getCache(), loader);
        } else {
            //Need to use ImageConverters
            if (log.isDebugEnabled()) {
                log.debug("No ImageLoaderFactory found that can load this format directly."
                        + " Trying ImageConverters instead...");
            }
            
            ImageRepresentation destination = new ImageRepresentation(targetFlavor);
            //Get Loader for originalMIME
            // --> List of resulting flavors, possibly multiple loaders
            ImageLoaderFactory[] loaderFactories = registry.getImageLoaderFactories(originalMime);
            if (loaderFactories != null) {
                //Find best pipeline -> best loader
                for (int i = 0, ci = loaderFactories.length; i < ci; i++) {
                    loaderFactory = loaderFactories[i];
                    ImageFlavor[] flavors = loaderFactory.getSupportedFlavors(originalMime);
                    for (int j = 0, cj = flavors.length; j < cj; j++) {
                        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(
                                dir);
                        ImageRepresentation origin = new ImageRepresentation(flavors[j]); 
                        dijkstra.execute(origin, destination);
                        if (log.isDebugEnabled()) {
                            log.debug("Lowest penalty: " + dijkstra.getLowestPenalty(destination));
                        }
                        
                        Vertex prev = destination;
                        Vertex pred = dijkstra.getPredecessor(destination);
                        if (pred == null) {
                            if (log.isDebugEnabled()) {
                                log.debug("No route found!");
                            }
                        } else {
                            LinkedList stops = new LinkedList();
                            //stops.addLast(destination);
                            while ((pred = dijkstra.getPredecessor(prev)) != null) {
                                ImageConversionEdge edge = (ImageConversionEdge)
                                        dir.getBestEdge(pred, prev);
                                stops.addFirst(edge);
                                prev = pred;
                            }
                            ImageLoader loader = loaderFactory.newImageLoader(flavors[i]);
                            pipeline = new ImageProviderPipeline(manager.getCache(), loader);
                            Iterator iter = stops.iterator();
                            while (iter.hasNext()) {
                                ImageConversionEdge edge = (ImageConversionEdge)iter.next(); 
                                pipeline.addConverter(edge.getImageConverter());
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("Pipeline: " + pipeline);
                            }
                            return pipeline;
                        }
                    }
                }
                
                //Build final pipeline
                
            }
            
        }
        if (log.isDebugEnabled()) {
            log.debug("Pipeline: " + pipeline);
        }
        return pipeline;
    }
    
}
