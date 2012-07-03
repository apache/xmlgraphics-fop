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

package org.apache.fop.render.afp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.afp.AFPResourceLevelDefaults;
import org.apache.fop.afp.fonts.AFPFontCollection;
import org.apache.fop.afp.fonts.AFPFontInfo;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.render.PrintRendererConfigurator;
import org.apache.fop.render.RendererConfig.RendererConfigParser;
import org.apache.fop.render.afp.AFPFontConfig.AFPFontConfigData;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.util.LogUtil;

/**
 * AFP Renderer configurator
 */
public class AFPRendererConfigurator extends PrintRendererConfigurator implements
        IFDocumentHandlerConfigurator {
    private static Log LOG = LogFactory.getLog(AFPRendererConfigurator.class);

    private final AFPEventProducer eventProducer;

    /**
     * Default constructor
     *
     * @param userAgent user agent
     */
    public AFPRendererConfigurator(FOUserAgent userAgent, RendererConfigParser rendererConfigParser) {
        super(userAgent, rendererConfigParser);
        eventProducer = AFPEventProducer.Provider.get(userAgent.getEventBroadcaster());
    }


    @Override
    public void configure(IFDocumentHandler documentHandler) throws FOPException {
        AFPRendererConfig config = (AFPRendererConfig) getRendererConfig(documentHandler);
        if (config != null) {
            AFPDocumentHandler afpDocumentHandler = (AFPDocumentHandler) documentHandler;
            configure(afpDocumentHandler, config);
        }
    }

    private void configure(AFPDocumentHandler documentHandler, AFPRendererConfig config) {
        Boolean colorImages = config.isColorImages();
        if (colorImages != null) {
            documentHandler.setColorImages(colorImages);
            if (colorImages) {
                documentHandler.setCMYKImagesSupported(config.isCmykImagesSupported());
            } else {
                documentHandler.setBitsPerPixel(config.getBitsPerPixel());
            }
        }
        if (config.getDitheringQuality() != null) {
            documentHandler.setDitheringQuality(config.getDitheringQuality());
        }
        if (config.isNativeImagesSupported() != null) {
            documentHandler.setNativeImagesSupported(config.isNativeImagesSupported());
        }
        if (config.getShadingMode() != null) {
            documentHandler.setShadingMode(config.getShadingMode());
        }
        if (config.getResolution() != null) {
            documentHandler.setResolution(config.getResolution());
        }
        if (config.isWrapPseg() != null) {
            documentHandler.setWrapPSeg(config.isWrapPseg());
        }
        if (config.isFs45() != null) {
            documentHandler.setFS45(config.isFs45());
        }
        if (config.allowJpegEmbedding() != null) {
            documentHandler.canEmbedJpeg(config.allowJpegEmbedding());
        }
        if (config.getBitmapEncodingQuality() != null) {
            documentHandler.setBitmapEncodingQuality(config.getBitmapEncodingQuality());
        }
        if (config.getLineWidthCorrection() != null) {
            documentHandler.setLineWidthCorrection(config.getLineWidthCorrection());
        }
        if (config.isGocaEnabled() != null) {
            documentHandler.setGOCAEnabled(config.isGocaEnabled());
        }
        if (config.isStrokeGocaText() != null) {
            documentHandler.setStrokeGOCAText(config.isStrokeGocaText());
        }
        if (config.getDefaultResourceGroupUri() != null) {
            documentHandler.setDefaultResourceGroupUri(config.getDefaultResourceGroupUri());
        }
        AFPResourceLevelDefaults resourceLevelDefaults = config.getResourceLevelDefaults();
        if (resourceLevelDefaults != null) {
            documentHandler.setResourceLevelDefaults(resourceLevelDefaults);
        }
    }

    @Override
    protected List<FontCollection> getDefaultFontCollection() {
        return new ArrayList<FontCollection>();
    }

    @Override
    protected FontCollection getCustomFontCollection(InternalResourceResolver uriResolverWrapper,
            String mimeType) throws FOPException {
        AFPRendererConfig config = (AFPRendererConfig) getRendererConfig(mimeType);
        if (config != null) {
            try {
                return new AFPFontCollection(userAgent.getEventBroadcaster(), createFontsList(
                        config.getFontInfoConfig(), mimeType));
            } catch (IOException e) {
                eventProducer.invalidConfiguration(this, e);
                LogUtil.handleException(LOG, e, userAgent.validateUserConfigStrictly());
            } catch (IllegalArgumentException iae) {
                eventProducer.invalidConfiguration(this, iae);
                LogUtil.handleException(LOG, iae, userAgent.validateUserConfigStrictly());
            }
        }
        return new AFPFontCollection(userAgent.getEventBroadcaster(), null);
    }

    private List<AFPFontInfo> createFontsList(AFPFontConfig fontConfig, String mimeType)
            throws FOPException, IOException {
        List<AFPFontInfo> afpFonts = new ArrayList<AFPFontInfo>();
        for (AFPFontConfigData config : fontConfig.getFontConfig()) {
            afpFonts.add(config.getFontInfo(userAgent.getFontManager().getResourceResolver(),
                    eventProducer));
        }
        return afpFonts;
    }
}
