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

import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.image2.ImageInfo;
import org.apache.fop.render.Graphics2DImagePainter;

/**
 * This class is an implementation of the Image interface exposing a Graphics2DImagePainter.
 */
public class ImageGraphics2D extends AbstractImage {

    private Graphics2DImagePainter painter;
    
    /**
     * Main constructor.
     * @param info the image info object
     * @param painter the image painter that will paint the Java2D image
     */
    public ImageGraphics2D(ImageInfo info, Graphics2DImagePainter painter) {
        super(info);
        this.painter = painter;
    }
    
    /** {@inheritDoc} */
    public ImageFlavor getFlavor() {
        return ImageFlavor.GRAPHICS2D;
    }

    /**
     * Returns the contained Graphics2DImagePainter instance.
     * @return the image painter
     */
    public Graphics2DImagePainter getGraphics2DImagePainter() {
        return this.painter;
    }
}
