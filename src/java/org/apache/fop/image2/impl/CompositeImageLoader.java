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

import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.image2.Image;
import org.apache.fop.image2.ImageException;
import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.image2.ImageInfo;
import org.apache.fop.image2.ImageSessionContext;
import org.apache.fop.image2.spi.ImageLoader;

/**
 * Composite ImageLoader implementation in order to provide fallbacks when one ImageLoader
 * fails due to some limitation.
 */
public class CompositeImageLoader extends AbstractImageLoader {

    /** logger */
    protected static Log log = LogFactory.getLog(CompositeImageLoader.class);

    private ImageLoader[] loaders;
    
    /**
     * Main constructor.
     * @param loaders the contained image loaders
     */
    public CompositeImageLoader(ImageLoader[] loaders) {
        if (loaders == null || loaders.length == 0) {
            throw new IllegalArgumentException("Must at least pass one ImageLoader as parameter");
        }
        for (int i = 1, c = loaders.length; i < c; i++) {
            if (!loaders[0].getTargetFlavor().equals(loaders[i].getTargetFlavor())) {
                throw new IllegalArgumentException(
                        "All ImageLoaders must produce the same target flavor");
            }
        }
        this.loaders = loaders;
    }
    
    /** {@inheritDoc} */
    public ImageFlavor getTargetFlavor() {
        return loaders[0].getTargetFlavor();
    }

    /** {@inheritDoc} */
    public Image loadImage(ImageInfo info, Map hints, ImageSessionContext session)
            throws ImageException, IOException {
        ImageException firstException = null;
        for (int i = 0, c = this.loaders.length; i < c; i++) {
            ImageLoader loader = this.loaders[i];
            try {
                Image img = loader.loadImage(info, hints, session);
                if (img != null && firstException != null) {
                    log.debug("First ImageLoader failed (" + firstException.getMessage() 
                            + "). Fallback was successful.");
                }
                return img;
            } catch (ImageException ie) {
                if (firstException == null) {
                    firstException = ie;
                }
            }
        }
        throw firstException;
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer("[");
        for (int i = 0; i < this.loaders.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(this.loaders[i].toString());
        }
        sb.append("]");
        return sb.toString();
    }
    
}
