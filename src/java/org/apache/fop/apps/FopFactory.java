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

package org.apache.fop.apps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.util.UnitConv;

import org.apache.fop.apps.io.URIResolverWrapper;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.ElementMappingRegistry;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.hyphenation.HyphenationTreeResolver;
import org.apache.fop.layoutmgr.LayoutManagerMaker;
import org.apache.fop.render.ImageHandlerRegistry;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.RendererConfig.RendererConfigParser;
import org.apache.fop.render.RendererFactory;
import org.apache.fop.render.XMLHandlerRegistry;
import org.apache.fop.util.ColorSpaceCache;
import org.apache.fop.util.ContentHandlerFactoryRegistry;

/**
 * Factory class which instantiates new Fop and FOUserAgent instances. This
 * class also holds environmental information and configuration used by FOP.
 * Information that may potentially be different for each renderingq run can be
 * found and managed in the FOUserAgent.
 */
public final class FopFactory implements ImageContext {

    /** logger instance */
    private static Log log = LogFactory.getLog(FopFactory.class);

    /** Factory for Renderers and FOEventHandlers */
    private final RendererFactory rendererFactory;

    /** Registry for XML handlers */
    private final XMLHandlerRegistry xmlHandlers;

    /** Registry for image handlers */
    private final ImageHandlerRegistry imageHandlers;

    /** The registry for ElementMapping instances */
    private final ElementMappingRegistry elementMappingRegistry;

    /** The registry for ContentHandlerFactory instance */
    private final ContentHandlerFactoryRegistry contentHandlerFactoryRegistry
            = new ContentHandlerFactoryRegistry();

    private final ColorSpaceCache colorSpaceCache;

    private final FopFactoryConfig config;

    private final URIResolverWrapper uriResolverWrapper;

    private final Map<String, RendererConfig> rendererConfig;

    private FopFactory(FopFactoryConfig config) {
        this.config = config;
        this.uriResolverWrapper = new URIResolverWrapper(config.getBaseURI(), config.getNewURIResolver());
        this.elementMappingRegistry = new ElementMappingRegistry(this);
        this.colorSpaceCache = new ColorSpaceCache(config.getURIResolver());
        this.rendererFactory = new RendererFactory(config.preferRenderer());
        this.xmlHandlers = new XMLHandlerRegistry();
        this.imageHandlers = new ImageHandlerRegistry();
        rendererConfig = new HashMap<String, RendererConfig>();
    }

    /**
     * Map of configured names of hyphenation pattern file names: ll_CC => name
     */
    private Map/*<String,String>*/ hyphPatNames = null;

    /**
     * FOP has the ability, for some FO's, to continue processing even if the
     * input XSL violates that FO's content model.  This is the default
     * behavior for FOP.  However, this flag, if set, provides the user the
     * ability for FOP to halt on all content model violations if desired.
     * Returns a new FopFactory instance that is configured using the {@link FopFactoryConfig} object.
     *
     * @param config the fop configuration
     * @return the requested FopFactory instance.
     */
    public static FopFactory newInstance(FopFactoryConfig config) {
        return new FopFactory(config);
    }

    /**
     * Returns a new FopFactory instance that is configured using the {@link FopFactoryConfig} object that
     * is created when the fopConf is parsed.
     *
     * @param fopConf the fop conf configuration file to parse
     * @return the requested FopFactory instance.
     * @throws IOException
     * @throws SAXException
     */
    public static FopFactory newInstance(File fopConf) throws SAXException, IOException {
        return new FopConfParser(fopConf).getFopFactoryBuilder().build();
    }

    /**
     * Returns a new FopFactory instance that is configured only by the default configuration
     * parameters.
     *
     * @param baseURI the base URI to resolve resource URIs against
     * @return the requested FopFactory instance.
     */
    public static FopFactory newInstance(URI baseURI) {
        return new FopFactoryBuilder(baseURI).build();
    }

    /**
     * Returns a new FopFactory instance that is configured using the {@link FopFactoryConfig} object that
     * is created when the fopConf is parsed.
     *
     * @param baseURI the base URI to resolve resource URIs against
     * @param confStream the fop conf configuration stream to parse
     * @return the requested FopFactory instance.
     * @throws SAXException
     * @throws IOException
     */
    public static FopFactory newInstance(URI baseURI, InputStream confStream) throws SAXException,
            IOException {
        return new FopConfParser(confStream, baseURI).getFopFactoryBuilder().build();
    }

    /**
     * Returns a new FOUserAgent instance. Use the FOUserAgent to configure special values that
     * are particular to a rendering run. Don't reuse instances over multiple rendering runs but
     * instead create a new one each time and reuse the FopFactory.
     * @return the newly created FOUserAgent instance initialized with default values
     * @throws FOPException
     */
    public FOUserAgent newFOUserAgent() {
        FOUserAgent userAgent = new FOUserAgent(this, uriResolverWrapper);
        return userAgent;
    }

    boolean isComplexScriptFeaturesEnabled() {
        return config.isComplexScriptFeaturesEnabled();
    }

    /**
     * Returns a new {@link Fop} instance. FOP will be configured with a default user agent
     * instance.
     * <p>
     * MIME types are used to select the output format (ex. "application/pdf" for PDF). You can
     * use the constants defined in {@link MimeConstants}.
     * @param outputFormat the MIME type of the output format to use (ex. "application/pdf").
     * @return the new Fop instance
     * @throws FOPException when the constructor fails
     */
    public Fop newFop(String outputFormat) throws FOPException {
        return newFOUserAgent().newFop(outputFormat);
    }

    /**
     * Returns a new {@link Fop} instance. Use this factory method if you want to configure this
     * very rendering run, i.e. if you want to set some metadata like the title and author of the
     * document you want to render. In that case, create a new {@link FOUserAgent}
     * instance using {@link #newFOUserAgent()}.
     * <p>
     * MIME types are used to select the output format (ex. "application/pdf" for PDF). You can
     * use the constants defined in {@link MimeConstants}.
     * @param outputFormat the MIME type of the output format to use (ex. "application/pdf").
     * @param userAgent the user agent that will be used to control the rendering run
     * @return the new Fop instance
     * @throws FOPException  when the constructor fails
     */
    public Fop newFop(String outputFormat, FOUserAgent userAgent) throws FOPException {
        return userAgent.newFop(outputFormat, null);
    }

    /**
     * Returns a new {@link Fop} instance. FOP will be configured with a default user agent
     * instance. Use this factory method if your output type requires an output stream.
     * <p>
     * MIME types are used to select the output format (ex. "application/pdf" for PDF). You can
     * use the constants defined in {@link MimeConstants}.
     * @param outputFormat the MIME type of the output format to use (ex. "application/pdf").
     * @param stream the output stream
     * @return the new Fop instance
     * @throws FOPException when the constructor fails
     */
    public Fop newFop(String outputFormat, OutputStream stream) throws FOPException {
        return newFOUserAgent().newFop(outputFormat, stream);
    }

    /**
     * Returns a new {@link Fop} instance. Use this factory method if your output type
     * requires an output stream and you want to configure this very rendering run,
     * i.e. if you want to set some metadata like the title and author of the document
     * you want to render. In that case, create a new {@link FOUserAgent} instance
     * using {@link #newFOUserAgent()}.
     * <p>
     * MIME types are used to select the output format (ex. "application/pdf" for PDF). You can
     * use the constants defined in {@link MimeConstants}.
     * @param outputFormat the MIME type of the output format to use (ex. "application/pdf").
     * @param userAgent the user agent that will be used to control the rendering run
     * @param stream the output stream
     * @return the new Fop instance
     * @throws FOPException when the constructor fails
     */
    public Fop newFop(String outputFormat, FOUserAgent userAgent, OutputStream stream)
            throws FOPException {
        return userAgent.newFop(outputFormat, stream);
    }

    /**
     * Returns a new {@link Fop} instance. Use this factory method if you want to supply your
     * own {@link org.apache.fop.render.Renderer Renderer} or
     * {@link org.apache.fop.fo.FOEventHandler FOEventHandler}
     * instance instead of the default ones created internally by FOP.
     * @param userAgent the user agent that will be used to control the rendering run
     * @return the new Fop instance
     * @throws FOPException when the constructor fails
     */
    public Fop newFop(FOUserAgent userAgent) throws FOPException {
        if (userAgent.getRendererOverride() == null
                && userAgent.getFOEventHandlerOverride() == null
                && userAgent.getDocumentHandlerOverride() == null) {
            throw new IllegalStateException("An overriding renderer,"
                    + " FOEventHandler or IFDocumentHandler must be set on the user agent"
                    + " when this factory method is used!");
        }
        return newFop(null, userAgent);
    }

    /** @return the RendererFactory */
    public RendererFactory getRendererFactory() {
        return this.rendererFactory;
    }

    /** @return the XML handler registry */
    public XMLHandlerRegistry getXMLHandlerRegistry() {
        return this.xmlHandlers;
    }

    /** @return the image handler registry */
    public ImageHandlerRegistry getImageHandlerRegistry() {
        return this.imageHandlers;
    }

    /** @return the element mapping registry */
    public ElementMappingRegistry getElementMappingRegistry() {
        return this.elementMappingRegistry;
    }

    /** @return the content handler factory registry */
    public ContentHandlerFactoryRegistry getContentHandlerFactoryRegistry() {
        return this.contentHandlerFactoryRegistry;
    }

    /**
     * Returns the renderer configuration object for a specific renderer given the parser and
     * configuration to read. The renderer config is cached such that the {@link Configuration} is
     * only parsed once per renderer, per FopFactory instance.
     *
     * @param userAgent the user agent
     * @param cfg the configuration to be parsed
     * @param configCreator the parser that creates the config object
     * @return the config object
     * @throws FOPException when an error occurs while creating the configuration object
     */
    public RendererConfig getRendererConfig(FOUserAgent userAgent, Configuration cfg,
            RendererConfigParser configCreator) throws FOPException {
        RendererConfig config = rendererConfig.get(configCreator.getMimeType());
        if (config == null) {
            try {
                config = configCreator.build(userAgent, cfg);
                rendererConfig.put(configCreator.getMimeType(), config);
            } catch (Exception e) {
                throw new FOPException(e);
            }
        }
        return config;
    }

    /**
     * Add the element mapping with the given class name.
     * @param elementMapping the class name representing the element mapping.
     */
    public void addElementMapping(ElementMapping elementMapping) {
        this.elementMappingRegistry.addElementMapping(elementMapping);
    }

    /**
     * Returns whether accessibility is enabled.
     * @return true if accessibility is enabled
     */
    boolean isAccessibilityEnabled() {
        return config.isAccessibilityEnabled();
    }

    /**
     * Returns the image manager.
     * @return the image manager
     */
    public ImageManager getImageManager() {
        return config.getImageManager();
    }

    /**
     * Returns the overriding LayoutManagerMaker instance, if any.
     * @return the overriding LayoutManagerMaker or null
     */
    public LayoutManagerMaker getLayoutManagerMakerOverride() {
        return config.getLayoutManagerMakerOverride();
    }

    /** @return the hyphen base URI */
    public String getHyphenBaseURI() {
        return config.getHyphenationBaseURI().toASCIIString();
    }

    /** @return the HyphenationTreeResolver for resolving user-supplied hyphenation patterns. */
    public HyphenationTreeResolver getHyphenationTreeResolver() {
        return config.getHyphenationTreeResolver();
    }

    public Map<String, String> getHyphPatNames() {
        return config.getHyphPatNames();
    }

    /**
     * Returns whether FOP is strictly validating input XSL
     * @return true of strict validation turned on, false otherwise
     */
    public boolean validateStrictly() {
        return config.validateStrictly();
    }

    /**
     * @return true if the indent inheritance should be broken when crossing reference area
     *         boundaries (for more info, see the javadoc for the relative member variable)
     */
    public boolean isBreakIndentInheritanceOnReferenceAreaBoundary() {
        return config.isBreakIndentInheritanceOnReferenceAreaBoundary();
    }

    /** @return the resolution for resolution-dependent input */
    public float getSourceResolution() {
        return config.getSourceResolution();
    }

    /**
     * Returns the conversion factor from pixel units to millimeters. This
     * depends on the desired source resolution.
     * @return float conversion factor
     * @see #getSourceResolution()
     */
    public float getSourcePixelUnitToMillimeter() {
        return UnitConv.IN2MM / getSourceResolution();
    }

    /** @return the resolution for resolution-dependant output */
    public float getTargetResolution() {
        return config.getTargetResolution();
    }

    /**
     * Returns the conversion factor from pixel units to millimeters. This
     * depends on the desired target resolution.
     * @return float conversion factor
     * @see #getTargetResolution()
     */
    public float getTargetPixelUnitToMillimeter() {
        return 25.4f / getTargetResolution();
    }

    /**
     * Gets the default page-height to use as fallback,
     * in case page-height="auto"
     *
     * @return the page-height, as a String
     */
    public String getPageHeight() {
        return config.getPageHeight();
    }

    /**
     * Gets the default page-width to use as fallback,
     * in case page-width="auto"
     *
     * @return the page-width, as a String
     */
    public String getPageWidth() {
        return config.getPageWidth();
    }

    /**
     * Indicates whether a namespace URI is on the ignored list.
     * @param namespaceURI the namespace URI
     * @return true if the namespace is ignored by FOP
     */
    public boolean isNamespaceIgnored(String namespaceURI) {
        return config.isNamespaceIgnored(namespaceURI);
    }

    /** @return the set of namespaces that are ignored by FOP */
    public Set<String> getIgnoredNamespace() {
        return config.getIgnoredNamespaces();
    }

    //------------------------------------------- Configuration stuff

    /**
     * Get the user configuration.
     * @return the user configuration
     */
    public Configuration getUserConfig() {
        return config.getUserConfig();
    }

    /**
     * Is the user configuration to be validated?
     * @return if the user configuration should be validated
     */
    public boolean validateUserConfigStrictly() {
        return config.validateUserConfigStrictly();
    }

    //------------------------------------------- Font related stuff

    /**
     * Returns the font manager.
     * @return the font manager
     */
    public FontManager getFontManager() {
        return config.getFontManager();
    }

    /**
     * Attempts to resolve the given URI.
     * Will use the configured resolver and if not successful fall back
     * to the default resolver.
     * @param href URI to access
     * @param baseUri the base URI to resolve against
     * @return A {@link javax.xml.transform.Source} object, or null if the URI
     * cannot be resolved.
     * @see org.apache.fop.apps.io.FOURIResolver
     */
    public Source resolveURI(String href, String baseUri) {
        Source source = null;
        try {
            source = config.getURIResolver().resolve(href, baseUri);
        } catch (TransformerException e) {
            log.error("Attempt to resolve URI '" + href + "' failed: ", e);
        }
        return source;
    }

    /**
     * Returns the color space cache for this instance.
     * <p>
     * Note: this method should not be considered as part of FOP's external API.
     * @return the color space cache
     */
    public ColorSpaceCache getColorSpaceCache() {
        return this.colorSpaceCache;
    }
}
