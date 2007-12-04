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

import java.util.Map;

/**
 * Represents an image that may not have been fully loaded. Usually, the loading only goes as far
 * as necessary to know the intrinsic size of the image. The image will only fully loaded later
 * when the image needs to be presented in a particular format so the consuming component can
 * actually process it. The "preloading" is done so a layout engine can work with the image without
 * having to fully load it (in memory).
 */
public class ImageInfo {

    /**
     * Key to register the "original object" among the custom objects of an ImageInfo instance.
     * @see #getOriginalImage()
     */
    public static final Object ORIGINAL_IMAGE = Image.class;

    /** Original URI the image was accessed with */
    private String originalURI;
    /** MIME type of the image */
    private String mimeType;

    /** the image size */
    private ImageSize size;

    /**
     * Map of custom objects that components further down the processing pipeline might need.
     * Example: The DOM of an XML document.
     */
    private Map customObjects = new java.util.HashMap();
    
    /**
     * Main constructor.
     * @param originalURI the original URI that was specified by the user (not the resolved URI!)
     * @param mimeType the MIME type of the image
     */
    public ImageInfo(String originalURI, String mimeType) {
        this.originalURI = originalURI;
        this.mimeType = mimeType;
    }

    /**
     * Returns the original URI of the image.
     * @return the original URI
     */
    public String getOriginalURI() {
        return this.originalURI;
    }

    /**
     * Returns the image's MIME type.
     * @return the MIME type
     */
    public String getMimeType() {
        return this.mimeType;
    }

    /**
     * Returns the image's intrinsic size.
     * @return the image size
     */
    public ImageSize getSize() {
        return this.size;
    }

    /**
     * Sets the image's intrinsic size.
     * @param size the size
     */
    public void setSize(ImageSize size) {
        this.size = size;
    }
    
    /**
     * Returns a Map of custom objects associated with this instance.
     * @return the Map of custom objects
     */
    public Map getCustomObjects() {
        return this.customObjects;
    }
    
    /**
     * Returns the original Image instance if such an Image instance is created while building
     * this ImageInfo object. Some images cannot be "preloaded". They have to be fully loaded
     * in order to determine the intrinsic image size. This method allows access to that fully
     * loaded image so no additional re-loading has to be done later.
     * <p>
     * This method is short for: (Image)this.customObjects.get(ORIGINAL_IMAGE); 
     * @return the original Image instance or null if none is set
     * @see #ORIGINAL_IMAGE
     */
    public Image getOriginalImage() {
        return (Image)this.customObjects.get(ORIGINAL_IMAGE);
    }

    /** {@inheritDoc} */
    public String toString() {
        return getOriginalURI() + " (" + getMimeType() + ")";
    }
    
}
