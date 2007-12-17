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

package org.apache.fop.image2.impl.imageio;

import javax.imageio.ImageIO;

import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.image2.impl.AbstractImageLoaderFactory;
import org.apache.fop.image2.spi.ImageLoader;

/**
 * Factory class for the ImageLoader based on ImageIO.
 */
public class ImageLoaderFactoryImageIO extends AbstractImageLoaderFactory {

    private static final ImageFlavor[] FLAVORS = new ImageFlavor[] {
        ImageFlavor.RENDERED_IMAGE,
        ImageFlavor.BUFFERED_IMAGE};
    
    /** {@inheritDoc} */
    public String[] getSupportedMIMETypes() {
        return ImageIO.getReaderMIMETypes();
    }
    
    /** {@inheritDoc} */
    public ImageFlavor[] getSupportedFlavors(String mime) {
        return FLAVORS;
    }
    
    /** {@inheritDoc} */
    public ImageLoader newImageLoader(ImageFlavor targetFlavor) {
        return new ImageLoaderImageIO(targetFlavor);
    }
    
    /** {@inheritDoc} */
    public int getUsagePenalty(String mime, ImageFlavor flavor) {
        return 0;
    }

    /** {@inheritDoc} */
    public boolean isAvailable() {
        return (getSupportedMIMETypes().length > 0);
    }
    
}
