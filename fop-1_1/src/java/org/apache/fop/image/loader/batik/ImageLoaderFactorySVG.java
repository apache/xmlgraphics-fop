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

package org.apache.fop.image.loader.batik;

import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.XMLNamespaceEnabledImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageLoaderFactory;
import org.apache.xmlgraphics.image.loader.spi.ImageLoader;
import org.apache.xmlgraphics.util.MimeConstants;

/**
 * Factory class for the ImageLoader for SVG.
 */
public class ImageLoaderFactorySVG extends AbstractImageLoaderFactory {

    private static final ImageFlavor[] FLAVORS = new ImageFlavor[] {
        XMLNamespaceEnabledImageFlavor.SVG_DOM};

    private static final String[] MIMES = new String[] {
        MimeConstants.MIME_SVG};

    /** {@inheritDoc} */
    public String[] getSupportedMIMETypes() {
        return MIMES;
    }

    /** {@inheritDoc} */
    public ImageFlavor[] getSupportedFlavors(String mime) {
        return FLAVORS;
    }

    /** {@inheritDoc} */
    public ImageLoader newImageLoader(ImageFlavor targetFlavor) {
        return new ImageLoaderSVG(targetFlavor);
    }

    /** {@inheritDoc} */
    public boolean isAvailable() {
        return BatikUtil.isBatikAvailable();
    }

}
