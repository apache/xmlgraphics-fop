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

package org.apache.fop.afp.svg;

import java.awt.geom.AffineTransform;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.gvt.TextPainter;
import org.apache.fop.afp.AFPGraphics2D;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.svg.AbstractFOPBridgeContext;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;

public class AFPBridgeContext extends AbstractFOPBridgeContext {

    private final AFPGraphics2D g2d;

    /**
     * Constructs a new bridge context.
     *
     * @param userAgent the user agent
     * @param fontInfo the font list for the text painter, may be null
     *                 in which case text is painted as shapes
     * @param imageManager an image manager
     * @param imageSessionContext an image session context
     * @param linkTransform AffineTransform to properly place links,
     *                      may be null
     * @param g2d an AFPGraphics 2D implementation
     */
    public AFPBridgeContext(UserAgent userAgent, FontInfo fontInfo,
            ImageManager imageManager, ImageSessionContext imageSessionContext,
            AffineTransform linkTransform, AFPGraphics2D g2d) {
        super(userAgent, fontInfo, imageManager, imageSessionContext, linkTransform);
        this.g2d = g2d;
    }

    /**
     * Constructs a new bridge context.
     * @param userAgent the user agent
     * @param loader the Document Loader to use for referenced documents.
     * @param fontInfo the font list for the text painter, may be null
     *                 in which case text is painted as shapes
     * @param linkTransform AffineTransform to properly place links,
     *                      may be null
     * @param imageManager an image manager
     * @param imageSessionContext an image session context
     * @param linkTransform AffineTransform to properly place links,
     *                      may be null
     * @param an AFPGraphics 2D implementation
     */
    public AFPBridgeContext(UserAgent userAgent, DocumentLoader documentLoader,
            FontInfo fontInfo, ImageManager imageManager,
            ImageSessionContext imageSessionContext,
            AffineTransform linkTransform, AFPGraphics2D g2d) {
        super(userAgent, documentLoader, fontInfo, imageManager, imageSessionContext, linkTransform);
        this.g2d = g2d;
    }

    /** {@inheritDoc} */
    public void registerSVGBridges() {
        super.registerSVGBridges();

        if (fontInfo != null) {
            AFPTextHandler textHandler = new AFPTextHandler(fontInfo);
            g2d.setCustomTextHandler(textHandler);

            TextPainter textPainter = new AFPTextPainter(textHandler);
            setTextPainter(textPainter);

            putBridge(new AFPTextElementBridge(textPainter));
        }

        putBridge(new AFPImageElementBridge());
    }

    /** {@inheritDoc} */
    public BridgeContext createBridgeContext() {
        return new AFPBridgeContext(getUserAgent(), getDocumentLoader(),
                fontInfo,
                getImageManager(),
                getImageSessionContext(),
                linkTransform, g2d);
    }

}
