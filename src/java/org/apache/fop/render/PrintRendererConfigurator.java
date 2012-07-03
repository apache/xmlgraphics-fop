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

import java.util.Collections;
import java.util.List;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.CustomFontCollection;
import org.apache.fop.fonts.DefaultFontConfigurator;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontConfigurator;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.render.RendererConfig.RendererConfigParser;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;

/**
 * Base Print renderer configurator (mostly handles font configuration)
 */
public abstract class PrintRendererConfigurator extends AbstractRendererConfigurator
        implements IFDocumentHandlerConfigurator {

    private final RendererConfigParser rendererConfigParser;

    private final FontConfigurator<EmbedFontInfo> fontInfoConfigurator;

    /**
     * Default constructor
     * @param userAgent user agent
     */
    public PrintRendererConfigurator(FOUserAgent userAgent, RendererConfigParser rendererConfigParser) {
        this(userAgent, rendererConfigParser,
                new DefaultFontConfigurator(userAgent.getFontManager(), new FontEventAdapter(
                        userAgent.getEventBroadcaster()), userAgent.validateUserConfigStrictly()));
    }

    /**
     * Default constructor
     * @param userAgent user agent
     */
    public PrintRendererConfigurator(FOUserAgent userAgent, RendererConfigParser rendererConfigParser,
            FontConfigurator<EmbedFontInfo> fontInfoConfigurator) {
        super(userAgent);
        this.rendererConfigParser = rendererConfigParser;
        this.fontInfoConfigurator = fontInfoConfigurator;
    }

    /**
     * Returns the renderer configuration data for a specific renderer.
     *
     * @param documentHandler the document handler
     * @return the renderer configuration data
     * @throws FOPException if an error occurs
     */
    protected RendererConfig getRendererConfig(IFDocumentHandler documentHandler) throws FOPException {
        return getRendererConfig(documentHandler.getMimeType());
    }

    /**
     * gets the renderer configuration data for a specific renderer.
     *
     * @param mimeType the MIME type
     * @return the renderer configuration data
     * @throws FOPException if an error occurs
     */
    protected RendererConfig getRendererConfig(String mimeType) throws FOPException {
        return userAgent.getRendererConfig(mimeType, rendererConfigParser);
    }

    /**
     * gets the renderer configuration data for a specific renderer.
     *
     * @param renderer the renderer
     * @return the renderer configuration data
     * @throws FOPException if an error occurs
     */
    protected RendererConfig getRendererConfig(Renderer renderer) throws FOPException {
        return  getRendererConfig(renderer.getMimeType());
    }


    /**
     * Builds a list of EmbedFontInfo objects for use with the setup() method.
     *
     * @param renderer print renderer
     * @throws FOPException if something's wrong with the config data
     */
    public void configure(Renderer renderer) throws FOPException {
        PrintRenderer printRenderer = (PrintRenderer) renderer;
        List<EmbedFontInfo> embedFontInfoList = buildFontList(renderer.getMimeType());
        printRenderer.addFontList(embedFontInfoList);
    }

    /** {@inheritDoc} */
    public void configure(IFDocumentHandler documentHandler) throws FOPException {
        //nop
    }

    /** {@inheritDoc} */
    public void setupFontInfo(String mimeType, FontInfo fontInfo) throws FOPException {
        FontManager fontManager = userAgent.getFontManager();
        List<FontCollection> fontCollections = getDefaultFontCollection();
        fontCollections.add(getCustomFontCollection(fontManager.getResourceResolver(), mimeType));
        fontManager.setup(fontInfo, fontCollections.toArray(new FontCollection[fontCollections.size()]));
    }

    protected abstract List<FontCollection> getDefaultFontCollection();

    /**
     * Returns the font collection for custom configured fonts.
     *
     * @param resolver the resource resolver
     * @param mimeType the renderer MIME type
     * @return the font collection
     * @throws FOPException if an error occurs
     */
    protected FontCollection getCustomFontCollection(InternalResourceResolver resolver, String mimeType)
            throws FOPException {
        List<EmbedFontInfo> fontList;
        if (rendererConfigParser == null) {
            fontList = Collections.<EmbedFontInfo>emptyList();
        } else {
            fontList = fontInfoConfigurator.configure(getRendererConfig(mimeType).getFontInfoConfig());
        }
        return createCollectionFromFontList(resolver, fontList);
    }

    /***
     * Creates the font collection given a list of embedded font infomation.
     *
     * @param resolver the resource resolver
     * @param fontList the embedded font infomation
     * @return the font collection
     */
    protected FontCollection createCollectionFromFontList(InternalResourceResolver resolver,
            List<EmbedFontInfo> fontList) {
        return new CustomFontCollection(resolver, fontList,
                userAgent.isComplexScriptFeaturesEnabled());
    }

    private List<EmbedFontInfo> buildFontList(String mimeType) throws FOPException {
        return fontInfoConfigurator.configure(getRendererConfig(mimeType).getFontInfoConfig());
    }

    public static PrintRendererConfigurator createDefaultInstance(FOUserAgent userAgent) {
        return new PrintRendererConfigurator(userAgent, null) {
            @Override
            protected List<FontCollection> getDefaultFontCollection() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
