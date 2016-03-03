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

package org.apache.fop.render.bitmap;

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.render.RendererConfig.RendererConfigParser;
import org.apache.fop.render.bitmap.BitmapRendererConfig.BitmapRendererConfigParser;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.java2d.Base14FontCollection;
import org.apache.fop.render.java2d.ConfiguredFontCollection;
import org.apache.fop.render.java2d.InstalledFontCollection;
import org.apache.fop.render.java2d.Java2DFontMetrics;
import org.apache.fop.render.java2d.Java2DRendererConfigurator;

/**
 * Configurator for bitmap output.
 */
public class BitmapRendererConfigurator extends Java2DRendererConfigurator {

    /**
     * Default constructor
     * @param userAgent user agent
     */
    public BitmapRendererConfigurator(FOUserAgent userAgent, RendererConfigParser rendererConfigParser) {
        super(userAgent, rendererConfigParser);
    }

    // ---=== IFDocumentHandler configuration ===---

    /** {@inheritDoc} */
    public void configure(IFDocumentHandler documentHandler) throws FOPException {
        AbstractBitmapDocumentHandler bitmapHandler = (AbstractBitmapDocumentHandler) documentHandler;
        BitmapRenderingSettings settings = bitmapHandler.getSettings();
        configure(documentHandler, settings,
                new BitmapRendererConfigParser(MimeConstants.MIME_BITMAP));
    }

    void configure(IFDocumentHandler documentHandler, BitmapRenderingSettings settings,
            BitmapRendererConfigParser parser) throws FOPException {
        BitmapRendererConfig config = (BitmapRendererConfig) userAgent.getRendererConfig(
                documentHandler.getMimeType(), parser);
        configure(config, settings);
    }

    private void configure(BitmapRendererConfig config, BitmapRenderingSettings settings)
            throws FOPException {
        if (config.hasTransparentBackround()) {
            settings.setPageBackgroundColor(null);
        } else if (config.getBackgroundColor() != null) {
            settings.setPageBackgroundColor(config.getBackgroundColor());
        }
        if (config.hasAntiAliasing() != null) {
            settings.setAntiAliasing(config.hasAntiAliasing());
        }
        if (config.isRenderHighQuality() != null) {
            settings.setQualityRendering(config.isRenderHighQuality());
        }
        if (config.getColorMode() != null) {
            settings.setBufferedImageType(config.getColorMode());
        }
    }

    @Override
    protected FontCollection createCollectionFromFontList(InternalResourceResolver resourceResolver,
            List<EmbedFontInfo> fontList) {
        return new ConfiguredFontCollection(resourceResolver, fontList, userAgent.isComplexScriptFeaturesEnabled());
    }

    @Override
    protected List<FontCollection> getDefaultFontCollection() {
        final Java2DFontMetrics java2DFontMetrics = new Java2DFontMetrics();
        final List<FontCollection> fontCollection = new ArrayList<FontCollection>();
        fontCollection.add(new Base14FontCollection(java2DFontMetrics));
        fontCollection.add(new InstalledFontCollection(java2DFontMetrics));
        return fontCollection;
    }
}
