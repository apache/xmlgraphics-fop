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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.image2.Image;
import org.apache.fop.image2.ImageException;
import org.apache.fop.image2.ImageInfo;

/**
 * Represents a pipeline of ImageConverters with an ImageLoader at the beginning of the
 * pipeline.
 */
public class ImageConverterPipeline {

    /** logger */
    protected static Log log = LogFactory.getLog(ImageConverterPipeline.class);

    private ImageLoader loader;
    private List converters = new java.util.LinkedList();
    
    /**
     * Main constructor.
     * @param loader the image loader to drive the pipeline with
     */
    public ImageConverterPipeline(ImageLoader loader) {
        this.loader = loader;
    }
    
    /**
     * Executes the image converter pipeline. First, the image indicated by the ImageInfo instance
     * is loaded through an ImageLoader and then optionally converted by a series of
     * ImageConverters. At the end of the pipeline, the fully loaded and converted image is
     * returned.
     * @param info the image info object indicating the image to load
     * @param hints a Map of image conversion hints
     * @return the requested image
     * @throws ImageException if an error occurs while loader or converting the image
     * @throws IOException if an I/O error occurs
     */
    public Image execute(ImageInfo info, Map hints) throws ImageException, IOException {
        long start, duration;
        start = System.currentTimeMillis();
        Image img = loader.loadImage(info, hints);
        if (log.isTraceEnabled()) {
            duration = System.currentTimeMillis() - start;
            log.trace("Image loading using " + loader + " took " + duration + " ms.");
        }
        
        Iterator iter = converters.iterator();
        while (iter.hasNext()) {
            ImageConverter converter = (ImageConverter)iter.next();
            start = System.currentTimeMillis();
            img = converter.convert(img, hints);
            if (log.isTraceEnabled()) {
                duration = System.currentTimeMillis() - start;
                log.trace("Image conversion using " + converter + " took " + duration + " ms.");
            }
        }
        return img;
    }

    /**
     * Adds an additional ImageConverter to the end of the pipeline.
     * @param converter the ImageConverter instance
     */
    public void addConverter(ImageConverter converter) {
        //TODO check for compatibility
        this.converters.add(converter);
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Loader: ").append(loader);
        if (converters.size() > 0) {
            sb.append(" Converters: ");
            sb.append(converters);
        }
        return sb.toString();
    }
    
}
