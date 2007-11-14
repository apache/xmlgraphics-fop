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
import java.util.Map;

import org.apache.fop.image2.Image;
import org.apache.fop.image2.ImageException;
import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.image2.ImageInfo;

/**
 * This interface is implemented by classes which load images from a source. Normally, such a
 * source will be an InputStream but can also be something else.
 */
public interface ImageLoader {

    /**
     * Loads and returns an image.
     * @param info the image info object indicating the image
     * @param hints a Map of hints that can be used by implementations to customize the loading
     *                  process.
     * @return the fully loaded image
     * @throws ImageException if an error occurs while loading the image
     * @throws IOException if an I/O error occurs while loading the image
     */
    Image loadImage(ImageInfo info, Map hints) throws ImageException, IOException;
 
    /**
     * Loads and returns an image.
     * @param info the image info object indicating the image
     * @return the fully loaded image
     * @throws ImageException if an error occurs while loading the image
     * @throws IOException if an I/O error occurs while loading the image
     */
    Image loadImage(ImageInfo info) throws ImageException, IOException;
    
    /**
     * Returns the image flavor that is returned by this ImageLoader implementation.
     * @return the target image flavor
     */
    ImageFlavor getTargetFlavor();
    
}
