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

package org.apache.fop.render;

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fonts.CustomFontCollection;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.fonts.FontEventListener;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontInfoConfigurator;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.FontResolver;
import org.apache.fop.fonts.base14.Base14FontCollection;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;

/**
 * Base Print renderer configurator (mostly handles font configuration)
 */
public class PrintRendererConfigurator extends AbstractRendererConfigurator
            implements RendererConfigurator, IFDocumentHandlerConfigurator {

    /** logger instance */
    protected static final Log log = LogFactory.getLog(PrintRendererConfigurator.class);

    /**
     * Default constructor
     * @param userAgent user agent
     */
    public PrintRendererConfigurator(FOUserAgent userAgent) {
        super(userAgent);
    }

    /**
     * Builds a list of EmbedFontInfo objects for use with the setup() method.
     *
     * @param renderer print renderer
     * @throws FOPException if something's wrong with the config data
     */
    public void configure(Renderer renderer) throws FOPException {
        Configuration cfg = getRendererConfig(renderer);
        if (cfg == null) {
            log.trace("no configuration found for " + renderer);
            return;
        }

        PrintRenderer printRenderer = (PrintRenderer)renderer;
        FontResolver fontResolver = printRenderer.getFontResolver();

        FontEventListener listener = new FontEventAdapter(
                renderer.getUserAgent().getEventBroadcaster());
        List<EmbedFontInfo> embedFontInfoList = buildFontList(cfg, fontResolver, listener);
        printRenderer.addFontList(embedFontInfoList);
    }

    /**
     * Builds the font list from configuration.
     * @param cfg the configuration object
     * @param fontResolver a font resolver
     * @param listener the font event listener
     * @return the list of {@link EmbedFontInfo} objects
     * @throws FOPException if an error occurs while processing the configuration
     */
    protected List<EmbedFontInfo> buildFontList(Configuration cfg, FontResolver fontResolver,
                    FontEventListener listener) throws FOPException {
        FopFactory factory = userAgent.getFactory();
        FontManager fontManager = factory.getFontManager();
        if (fontResolver == null) {
            //Ensure that we have minimal font resolution capabilities
            fontResolver
                = FontManager.createMinimalFontResolver
                    ( userAgent.isComplexScriptFeaturesEnabled() );
        }

        boolean strict = factory.validateUserConfigStrictly();

        //Read font configuration
        FontInfoConfigurator fontInfoConfigurator
            = new FontInfoConfigurator(cfg, fontManager, fontResolver, listener, strict);
        List<EmbedFontInfo> fontInfoList = new ArrayList<EmbedFontInfo>();
        fontInfoConfigurator.configure(fontInfoList);
        return fontInfoList;
    }

    // ---=== IFDocumentHandler configuration ===---

    /** {@inheritDoc} */
    public void configure(IFDocumentHandler documentHandler) throws FOPException {
        //nop
    }

    /** {@inheritDoc} */
    public void setupFontInfo(IFDocumentHandler documentHandler, FontInfo fontInfo)
                throws FOPException {
        FontManager fontManager = userAgent.getFactory().getFontManager();
        List<FontCollection> fontCollections = new ArrayList<FontCollection>();
        fontCollections.add(new Base14FontCollection(fontManager.isBase14KerningEnabled()));

        Configuration cfg = super.getRendererConfig(documentHandler.getMimeType());
        if (cfg != null) {
            FontResolver fontResolver = new DefaultFontResolver(userAgent);
            FontEventListener listener = new FontEventAdapter(
                    userAgent.getEventBroadcaster());
            List<EmbedFontInfo> fontList = buildFontList(cfg, fontResolver, listener);
            fontCollections.add(new CustomFontCollection(fontResolver, fontList,
                                userAgent.isComplexScriptFeaturesEnabled()));
        }

        fontManager.setup(fontInfo,
                (FontCollection[])fontCollections.toArray(
                        new FontCollection[fontCollections.size()]));
        documentHandler.setFontInfo(fontInfo);
    }
}
