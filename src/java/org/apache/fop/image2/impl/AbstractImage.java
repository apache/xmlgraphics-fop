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

package org.apache.fop.image2.impl;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;

import org.apache.fop.image2.Image;
import org.apache.fop.image2.ImageInfo;
import org.apache.fop.image2.ImageSize;

/**
 * Abstract base class for Image implementations.
 */
public abstract class AbstractImage implements Image {

    private ImageInfo info;
    
    /**
     * Main constructor
     * @param info the image info object associated with this image
     */
    public AbstractImage(ImageInfo info) {
        this.info = info;
    }
    
    /** {@inheritDoc} */
    public ImageInfo getInfo() {
        return this.info;
    }

    /** {@inheritDoc} */
    public ImageSize getSize() {
        return getInfo().getSize();
    }

    /** {@inheritDoc} */
    public ColorSpace getColorSpace() {
        return null;
    }

    /** {@inheritDoc} */
    public ICC_Profile getICCProfile() {
        return null;
    }

    /** {@inheritDoc} */
    public String toString() {
        return getClass().getName() + ": " + getInfo();
    }

}
