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

import org.apache.fop.image2.ImageFlavor;

/**
 * This interface is implemented to provide information about an ImageLoader and to create new
 * instances. A separate factory allows implementation to dynamically detect if the underlying
 * libraries are available in the classpath so the caller can skip this implementation if it's
 * not functional.
 */
public interface ImageLoaderFactory {

    /**
     * Returns an array of MIME types supported by this implementation.
     * @return the MIME type array 
     */
    String[] getSupportedMIMETypes();
    
    /**
     * Returns an array of ImageFlavors that are supported by this implementation for a given
     * MIME type.
     * @param mime the MIME type
     * @return the ImageFlavor array
     */
    ImageFlavor[] getSupportedFlavors(String mime);
    
    /**
     * Creates and returns a new ImageLoader instance.
     * @param targetFlavor the target image flavor to produce
     * @return a new ImageLoader instance
     */
    ImageLoader newImageLoader(ImageFlavor targetFlavor);
    
    /**
     * Returns the usage penalty for a particular ImageLoader. This is used to select the best
     * ImageLoader implementation for loading an image.
     * @param mime the MIME type
     * @param flavor the target image flavor
     * @return the usage penalty (must be a non-negative integer)
     */
    int getUsagePenalty(String mime, ImageFlavor flavor);
    
    /**
     * Indicates whether the underlying libraries needed by the implementation are available.
     * @return true if the implementation is functional.
     */
    boolean isAvailable();
    
}
