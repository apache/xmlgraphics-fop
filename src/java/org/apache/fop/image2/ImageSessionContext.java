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

import java.io.FileNotFoundException;

import javax.xml.transform.Source;


/**
 * This interface is used to tell the cache which images are used by a session (in FOP that would
 * be a rendering run). Images access within a session get a hard reference so they cannot be
 * discarded. That could increase memory usage but helps with performance because the images
 * don't get unloaded between layout and rendering which would mean that they have to be reloaded.
 */
public interface ImageSessionContext {

    /**
     * Returns the session-independent context object which provides configuration information.
     * @return the associated ImageContext instance
     */
    ImageContext getParentContext();
    
    /**
     * Returns the resolution (in dpi) of the target device used when painting images.
     * @return the target resolution (in dpi)
     */
    float getTargetResolution();
    
    /**
     * Attempts to create a Source object from the given URI. If possible this method returns
     * ImageSource instance which provide the best possible method to access the image.
     * @param uri URI to access
     * @return A {@link javax.xml.transform.Source} object, or null if the URI
     * cannot be resolved. 
     */
    Source newSource(String uri);
    
    /**
     * Returns a Source object for a URI. This method is not guaranteed to return an instance.
     * Implementations normally return already created Sources from a pool (normally populated
     * through the {@link #returnSource(String, Source)} method).
     * @param uri the URI of the image
     * @return the Source object to load the image from, or null
     */
    Source getSource(String uri);
    
    /**
     * Returns a Source object for a URI. This method is guaranteed to return a Source object. If
     * the image cannot be found, a {@link FileNotFoundException} is thrown.
     * @param uri the URI of the image
     * @return the Source object to load the image from
     * @throws FileNotFoundException if the image cannot be found
     */
    Source needSource(String uri) throws FileNotFoundException;
    
    /**
     * Returns a Source object to a pool. This is provided in order to reuse a Source object
     * between the preloading and the final loading of an image. Note that not all Source objects
     * can be reused! Non-reusable Sources are discarded.
     * @param uri the URI of the image
     * @param src the Source object belonging to the URI
     */
    void returnSource(String uri, Source src);
    
}
