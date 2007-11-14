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
 
package org.apache.fop.image2.spi;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.util.dijkstra.DefaultEdgeDirectory;
import org.apache.fop.util.dijkstra.DijkstraAlgorithm;
import org.apache.fop.util.dijkstra.Vertex;
import org.apache.xmlgraphics.util.Service;

/**
 * This class is the registry for all implementations of the various service provider interfaces
 * for the image package.
 */
public class ImageImplRegistry {

    /** logger */
    protected static Log log = LogFactory.getLog(ImageImplRegistry.class);

    /** Holds the list of preloaders */
    private List preloaders = new java.util.ArrayList();
    //Content: List<ImagePreloader>
    
    /** Holds the list of ImageLoaderFactories */
    private Map loaders = new java.util.HashMap();
    //Content: Map<String,Map<ImageFlavor,ImageLoaderFactory>>

    /** Holds the list of ImageConverters */
    private List converters = new java.util.ArrayList();
    //Content: List<ImageConverter>
    
    /** Holds the EdgeDirectory for all image conversions */
    private DefaultEdgeDirectory converterEdgeDirectory = new DefaultEdgeDirectory();
    
    /** Singleton instance */
    private static ImageImplRegistry defaultInstance;
    
    /**
     * Main constructor.
     * @see #getDefaultInstance()
     */
    public ImageImplRegistry() {
        discoverClasspathImplementations();
    }
    
    /**
     * Returns the default instance of the Image implementation registry.
     * @return the default instance
     */
    public static ImageImplRegistry getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new ImageImplRegistry();
        }
        return defaultInstance;
    }

    /**
     * Discovers all implementations in the application's classpath.
     */
    public void discoverClasspathImplementations() {
        //Dynamic registration of ImagePreloaders
        Iterator iter = Service.providers(ImagePreloader.class, true);
        while (iter.hasNext()) {
            registerPreloader((ImagePreloader)iter.next());
        }
        
        //Dynamic registration of ImageLoaderFactories
        iter = Service.providers(ImageLoaderFactory.class, true);
        while (iter.hasNext()) {
            registerLoaderFactory((ImageLoaderFactory)iter.next());
        }
        
        //Dynamic registration of ImageConverters
        iter = Service.providers(ImageConverter.class, true);
        while (iter.hasNext()) {
            registerConverter((ImageConverter)iter.next());
        }
    }
    
    /**
     * Registers a new ImagePreloader.
     * @param preloader An ImagePreloader instance
     */
    public void registerPreloader(ImagePreloader preloader) {
        if (log.isDebugEnabled()) {
            log.debug("Registered " + preloader.getClass().getName()
                    + ": MIME = " + preloader.getMimeType());
        }
        preloaders.add(preloader);
    }

    /**
     * Registers a new ImageLoaderFactory.
     * @param loaderFactory An ImageLoaderFactory instance
     */
    public void registerLoaderFactory(ImageLoaderFactory loaderFactory) {
        if (!loaderFactory.isAvailable()) {
            if (log.isDebugEnabled()) {
                log.debug("ImageLoaderFactory reports not available: "
                        + loaderFactory.getClass().getName());
            }
            return;
        }
        String[] mimes = loaderFactory.getSupportedMIMETypes();
        for (int i = 0, ci = mimes.length; i < ci; i++) {
            String mime = mimes[i];
            
            synchronized (loaders) {
                Map flavorMap = (Map)loaders.get(mime);
                if (flavorMap == null) {
                    flavorMap = new java.util.HashMap();
                    loaders.put(mime, flavorMap);
                }
                
                ImageFlavor[] flavors = loaderFactory.getSupportedFlavors(mime);
                for (int j = 0, cj = flavors.length; j < cj; j++) {
                    ImageFlavor flavor = flavors[j];
                    
                    List factoryList = (List)flavorMap.get(flavor);
                    if (factoryList == null) {
                        factoryList = new java.util.ArrayList();
                        flavorMap.put(flavor, factoryList);
                    }
                    factoryList.add(loaderFactory);
                    
                    if (log.isDebugEnabled()) {
                        log.debug("Registered " + loaderFactory.getClass().getName()
                                + ": MIME = " + mime + ", Flavor = " + flavor);
                    }
                }
            }
        }
    }
    
    /**
     * Registers a new ImageConverter.
     * @param converter An ImageConverter instance
     */
    public void registerConverter(ImageConverter converter) {
        converters.add(converter);
        converterEdgeDirectory.addEdge(new ImageConversionEdge(converter));
        if (log.isDebugEnabled()) {
            log.debug("Registered: " + converter.getClass().getName());
        }
    }

    /**
     * Returns an iterator over all registered ImagePreloader instances.
     * @return an iterator over ImagePreloader instances.
     */
    public Iterator getPreloaderIterator() {
        return this.preloaders.iterator();
    }

    /**
     * Returns the best ImageLoaderFactory supporting the given MIME type and image flavor.
     * If there are multiple ImageLoaderFactories the one with the least usage penalty is selected.
     * @param mime the MIME type
     * @param flavor the image flavor.
     * @return an ImageLoaderFactory instance or null, if no suitable implementation was found
     */
    public ImageLoaderFactory getImageLoaderFactory(String mime, ImageFlavor flavor) {
        Map flavorMap = (Map)loaders.get(mime);
        if (flavorMap != null) {
            List factoryList = (List)flavorMap.get(flavor);
            if (factoryList != null && factoryList.size() > 0) {
                Iterator iter = factoryList.iterator();
                int bestPenalty = Integer.MAX_VALUE;
                ImageLoaderFactory bestFactory = null;
                while (iter.hasNext()) {
                    ImageLoaderFactory factory = (ImageLoaderFactory)iter.next();
                    int penalty = factory.getUsagePenalty(mime, flavor); 
                    if (penalty < bestPenalty) {
                        bestPenalty = penalty;
                        bestFactory = factory;
                    }
                }
                return bestFactory;
            }
        }
        return null;
    }

    private ImageLoaderFactory[] getImageLoaderFactories(String mime) {
        Map flavorMap = (Map)loaders.get(mime);
        if (flavorMap != null) {
            Set factories = new java.util.HashSet();
            Iterator iter = flavorMap.values().iterator();
            while (iter.hasNext()) {
                List factoryList = (List)iter.next();
                factories.addAll(factoryList);
            }
            int factoryCount = factories.size(); 
            if (factoryCount > 0) {
                return (ImageLoaderFactory[])factories.toArray(
                        new ImageLoaderFactory[factoryCount]);
            }
        }
        return null;
    }
    
    /**
     * Creates and returns an ImageConverterPipeline that allows to load an image of the given
     * MIME type and present it in the requested image flavor.
     * @param originalMime the MIME type of the original image
     * @param targetFlavor the requested image flavor
     * @return an ImageConverterPipeline or null if no suitable pipeline could be assembled
     */
    public ImageConverterPipeline newImageConverterPipeline(
                String originalMime, ImageFlavor targetFlavor) {
        ImageConverterPipeline pipeline = null;
        ImageLoaderFactory loaderFactory = getImageLoaderFactory(originalMime, targetFlavor);
        if (loaderFactory != null) {
            //Directly load image and return it
            ImageLoader loader = loaderFactory.newImageLoader(targetFlavor);
            pipeline = new ImageConverterPipeline(loader);
        } else {
            //Need to use ImageConverters
            if (log.isDebugEnabled()) {
                log.debug("No ImageLoaderFactory found that can load this format directly."
                        + " Trying ImageConverters instead...");
            }
            
            ImageRepresentation destination = new ImageRepresentation(targetFlavor);
            //Get Loader for originalMIME
            // --> List of resulting flavors, possibly multiple loaders
            ImageLoaderFactory[] loaderFactories = getImageLoaderFactories(originalMime);
            if (loaderFactories != null) {
                //Find best pipeline -> best loader
                for (int i = 0, ci = loaderFactories.length; i < ci; i++) {
                    loaderFactory = loaderFactories[i];
                    ImageFlavor[] flavors = loaderFactory.getSupportedFlavors(originalMime);
                    for (int j = 0, cj = flavors.length; j < cj; j++) {
                        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(
                                this.converterEdgeDirectory);
                        ImageRepresentation origin = new ImageRepresentation(flavors[i]); 
                        dijkstra.execute(origin, destination);
                        if (log.isDebugEnabled()) {
                            log.debug("Lowest penalty: " + dijkstra.getLowestPenalty(destination));
                        }
                        
                        Vertex prev = destination;
                        Vertex pred = dijkstra.getPredecessor(destination);
                        if (pred == null) {
                            log.error("No route found!");
                        }
                        LinkedList stops = new LinkedList();
                        //stops.addLast(destination);
                        while ((pred = dijkstra.getPredecessor(prev)) != null) {
                            ImageConversionEdge edge = (ImageConversionEdge)
                                    this.converterEdgeDirectory.getBestEdge(pred, prev);
                            stops.addFirst(edge);
                            prev = pred;
                        }
                        ImageLoader loader = loaderFactory.newImageLoader(flavors[i]);
                        pipeline = new ImageConverterPipeline(loader);
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
                
                //Build final pipeline
                
            }
            
        }
        if (log.isDebugEnabled()) {
            log.debug("Pipeline: " + pipeline);
        }
        return pipeline;
    }
    
}
