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

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.RenderedImage;

import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.image2.ImageInfo;

/**
 * This class is an implementation of the Image interface exposing a RenderedImage.
 */
public class ImageRendered extends AbstractImage {

    private RenderedImage red;
    private Color transparentColor;
    
    /**
     * Main constructor.
     * @param info the image info object
     * @param red the RenderedImage instance
     * @param transparentColor the transparent color or null
     */
    public ImageRendered(ImageInfo info, RenderedImage red, Color transparentColor) {
        super(info);
        this.red = red;
        this.transparentColor = transparentColor;
    }
    
    /** {@inheritDoc} */
    public ImageFlavor getFlavor() {
        return ImageFlavor.RENDERED_IMAGE;
    }

    /** {@inheritDoc} */
    public boolean isCacheable() {
        return true;
    }
    
    /**
     * Returns the contained RenderedImage instance.
     * @return the RenderedImage instance
     */
    public RenderedImage getRenderedImage() {
        return this.red;
    }

    /** {@inheritDoc} */
    public ColorSpace getColorSpace() {
        return getRenderedImage().getColorModel().getColorSpace();
    }

    /** {@inheritDoc} */
    public ICC_Profile getICCProfile() {
        ColorSpace cs = getColorSpace();
        if (cs instanceof ICC_ColorSpace) {
            ICC_ColorSpace icccs = (ICC_ColorSpace)cs;
            return icccs.getProfile();
        }
        return null;
    }
    
    /**
     * Returns the transparent color if available.
     * @return the transparent color or null
     */
    public Color getTransparentColor() {
        return this.transparentColor;
    }
    
}
