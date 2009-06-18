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

import java.awt.color.ColorSpace;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.xml.sax.SAXException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageManager;

import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.ElementMappingRegistry;
import org.apache.fop.fonts.FontCache;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.hyphenation.HyphenationTreeResolver;
import org.apache.fop.layoutmgr.LayoutManagerMaker;
import org.apache.fop.render.RendererFactory;
import org.apache.fop.render.XMLHandlerRegistry;
import org.apache.fop.util.ColorSpaceCache;
import org.apache.fop.util.ContentHandlerFactoryRegistry;

/**
 * Factory class which instantiates new Fop and FOUserAgent instances. This
 * class also holds environmental information and configuration used by FOP.
 * Information that may potentially be different for each rendering run can be
 * found and managed in the FOUserAgent.
 */
public class FopFactory implements ImageContext {

    /** logger instance */
    private static Log log = LogFactory.getLog(FopFactory.class);

    /** Factory for Renderers and FOEventHandlers */
    private RendererFactory rendererFactory;

    /** Registry for XML handlers */
    private XMLHandlerRegistry xmlHandlers;

    /** The registry for ElementMapping instances */
    private ElementMappingRegistry elementMappingRegistry;

    /** The registry for ContentHandlerFactory instance */
    private ContentHandlerFactoryRegistry contentHandlerFactoryRegistry
                = new ContentHandlerFactoryRegistry();

    /** The resolver for user-supplied hyphenation patterns */
    private HyphenationTreeResolver hyphResolver = null;

    private ColorSpaceCache colorSpaceCache = null;

    /** Image manager for loading and caching image objects */
    private ImageManager imageManager;

    /** Font manager for font substitution, autodetection and caching **/
    private FontManager fontManager;

    /** Configuration layer used to configure fop */
    private FopFactoryConfigurator config = null;

    /**
     *  The base URL for all URL resolutions, especially for
     *  external-graphics.
     */
    private String base = null;

    /** The base URL for all hyphen URL resolutions. */
    private String hyphenBase = null;

    /**
     * FOP has the ability, for some FO's, to continue processing even if the
     * input XSL violates that FO's content model.  This is the default
     * behavior for FOP.  However, this flag, if set, provides the user the
     * ability for FOP to halt on all content model violations if desired.
     */
    private boolean strictFOValidation = FopFactoryConfigurator.DEFAULT_STRICT_FO_VALIDATION;

    /**
     * FOP will validate the contents of the user configuration strictly
     * (e.g. base-urls and font urls/paths).
     */
    private boolean strictUserConfigValidation
        = FopFactoryConfigurator.DEFAULT_STRICT_USERCONFIG_VALIDATION;

    /** Source resolution in dpi */
    private float sourceResolution = FopFactoryConfigurator.DEFAULT_SOURCE_RESOLUTION;

    /** Target resolution in dpi */
    private float targetResolution = FopFactoryConfigurator.DEFAULT_TARGET_RESOLUTION;

    /** Page height */
    private String pageHeight = FopFactoryConfigurator.DEFAULT_PAGE_HEIGHT;

    /** Page width */
    private String pageWidth = FopFactoryConfigurator.DEFAULT_PAGE_WIDTH;

    /** @see #setBreakIndentInheritanceOnReferenceAreaBoundary(boolean) */
    private boolean breakIndentInheritanceOnReferenceAreaBoundary
        = FopFactoryConfigurator.DEFAULT_BREAK_INDENT_INHERITANCE;

    /** Optional overriding LayoutManagerMaker */
    private LayoutManagerMaker lmMakerOverride = null;

    private Set ignoredNamespaces;

    private FOURIResolver foURIResolver;

    /**
     * Main constructor.
     */
    protected FopFactory() {
        this.config = new FopFactoryConfigurator(this);
        this.elementMappingRegistry = new ElementMappingRegistry(this);
        this.foURIResolver = new FOURIResolver(validateUserConfigStrictly());
        this.fontManager = new FontManager() {

            /** {@inheritDoc} */
            public void setFontBaseURL(String fontBase) throws MalformedURLException {
                super.setFontBaseURL(getFOURIResolver().checkBaseURL(fontBase));
            }

        };
        this.colorSpaceCache = new ColorSpaceCache(foURIResolver);
        this.imageManager = new ImageManager(this);
        this.rendererFactory = new RendererFactory();
        this.xmlHandlers = new XMLHandlerRegistry();
        this.ignoredNamespaces = new java.util.HashSet();
    }

    /**
     * Returns a new FopFactory instance.
     * @return the requested FopFactory instance.
     */
    public static FopFactory newInstance() {
        return new FopFactory();
    }

    /**
     * Returns a new FOUserAgent instance. Use the FOUserAgent to configure special values that
     * are particular to a rendering run. Don't reuse instances over multiple rendering runs but
     * instead create a new one each time and reuse the FopFactory.
     * @return the newly created FOUserAgent instance initialized with default values
     * @throws FOPException
     */
    public FOUserAgent newFOUserAgent() {
        FOUserAgent userAgent = new FOUserAgent(this);
        return userAgent;
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
        return newFop(outputFormat, newFOUserAgent());
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
        return newFop(outputFormat, userAgent, null);
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
        return newFop(outputFormat, newFOUserAgent(), stream);
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
        if (userAgent == null) {
            throw new NullPointerException("The userAgent parameter must not be null!");
        }
        return new Fop(outputFormat, userAgent, stream);
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
                && userAgent.getFOEventHandlerOverride() == null) {
            throw new IllegalStateException("Either the overriding renderer or the overriding"
                    + " FOEventHandler must be set when this factory method is used!");
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

    /** @return the element mapping registry */
    public ElementMappingRegistry getElementMappingRegistry() {
        return this.elementMappingRegistry;
    }

    /** @return the content handler factory registry */
    public ContentHandlerFactoryRegistry getContentHandlerFactoryRegistry() {
        return this.contentHandlerFactoryRegistry;
    }

    /**
     * Returns the image manager.
     * @return the image manager
     */
    public ImageManager getImageManager() {
        return this.imageManager;
    }

    /**
     * Add the element mapping with the given class name.
     * @param elementMapping the class name representing the element mapping.
     */
    public void addElementMapping(ElementMapping elementMapping) {
        this.elementMappingRegistry.addElementMapping(elementMapping);
    }

    /**
     * Sets an explicit LayoutManagerMaker instance which overrides the one
     * defined by the AreaTreeHandler.
     * @param lmMaker the LayoutManagerMaker instance
     */
    public void setLayoutManagerMakerOverride(LayoutManagerMaker lmMaker) {
        this.lmMakerOverride = lmMaker;
    }

    /**
     * Returns the overriding LayoutManagerMaker instance, if any.
     * @return the overriding LayoutManagerMaker or null
     */
    public LayoutManagerMaker getLayoutManagerMakerOverride() {
        return this.lmMakerOverride;
    }

    /**
     * Sets the base URL.
     * @param base the base URL
     * @throws MalformedURLException if there's a problem with a file URL
     */
    public void setBaseURL(String base) throws MalformedURLException {
        this.base = foURIResolver.checkBaseURL(base);
    }

    /**
     * Returns the base URL.
     * @return the base URL
     */
    public String getBaseURL() {
        return this.base;
    }

    /**
     * Sets the font base URL.
     * @param fontBase font base URL
     * @throws MalformedURLException if there's a problem with a file URL
     * @deprecated use getFontManager().setFontBaseURL(fontBase) instead
     */
    public void setFontBaseURL(String fontBase) throws MalformedURLException {
        getFontManager().setFontBaseURL(fontBase);
    }

    /**
     * @return the font base URL
     * @deprecated use getFontManager().setFontBaseURL(fontBase) instead
     */
    public String getFontBaseURL() {
        return getFontManager().getFontBaseURL();
    }

    /** @return the hyphen base URL */
    public String getHyphenBaseURL() {
        return this.hyphenBase;
    }

    /**
     * Sets the hyphen base URL.
     * @param hyphenBase hythen base URL
     * @throws MalformedURLException if there's a problem with a file URL
     * */
    public void setHyphenBaseURL(final String hyphenBase) throws MalformedURLException {
        if (hyphenBase != null) {
            setHyphenationTreeResolver(
            new HyphenationTreeResolver() {
                public Source resolve(String href) {
                    return resolveURI(href, hyphenBase);
                }
            });
        }
        this.hyphenBase = foURIResolver.checkBaseURL(hyphenBase);
    }

    /**
     * Sets the URI Resolver. It is used for resolving factory-level URIs like hyphenation
     * patterns and as backup for URI resolution performed during a rendering run.
     * @param uriResolver the new URI resolver
     */
    public void setURIResolver(URIResolver uriResolver) {
        foURIResolver.setCustomURIResolver(uriResolver);
    }

    /**
     * Returns the URI Resolver.
     * @return the URI Resolver
     */
    public URIResolver getURIResolver() {
        return foURIResolver;
    }

    /**
     * Returns the FO URI Resolver.
     * @return the FO URI Resolver
     */
    public FOURIResolver getFOURIResolver() {
        return foURIResolver;
    }

    /** @return the HyphenationTreeResolver for resolving user-supplied hyphenation patterns. */
    public HyphenationTreeResolver getHyphenationTreeResolver() {
        return this.hyphResolver;
    }

    /**
     * Sets the HyphenationTreeResolver to be used for resolving user-supplied hyphenation files.
     * @param hyphResolver the HyphenationTreeResolver instance
     */
    public void setHyphenationTreeResolver(HyphenationTreeResolver hyphResolver) {
        this.hyphResolver = hyphResolver;
    }

    /**
     * Activates strict XSL content model validation for FOP
     * Default is false (FOP will continue processing where it can)
     * @param validateStrictly true to turn on strict validation
     */
    public void setStrictValidation(boolean validateStrictly) {
        this.strictFOValidation = validateStrictly;
    }

    /**
     * Returns whether FOP is strictly validating input XSL
     * @return true of strict validation turned on, false otherwise
     */
    public boolean validateStrictly() {
        return strictFOValidation;
    }

    /**
     * @return true if the indent inheritance should be broken when crossing reference area
     *         boundaries (for more info, see the javadoc for the relative member variable)
     */
    public boolean isBreakIndentInheritanceOnReferenceAreaBoundary() {
        return breakIndentInheritanceOnReferenceAreaBoundary;
    }

    /**
     * Controls whether to enable a feature that breaks indent inheritance when crossing
     * reference area boundaries.
     * <p>
     * This flag controls whether FOP will enable special code that breaks property
     * inheritance for start-indent and end-indent when the evaluation of the inherited
     * value would cross a reference area. This is described under
     * http://wiki.apache.org/xmlgraphics-fop/IndentInheritance as is intended to
     * improve interoperability with commercial FO implementations and to produce
     * results that are more in line with the expectation of unexperienced FO users.
     * Note: Enabling this features violates the XSL specification!
     * @param value true to enable the feature
     */
    public void setBreakIndentInheritanceOnReferenceAreaBoundary(boolean value) {
        this.breakIndentInheritanceOnReferenceAreaBoundary = value;
    }

    /**
     * @return true if kerning on base 14 fonts is enabled
     * @deprecated use getFontManager().isBase14KerningEnabled() instead
     */
    public boolean isBase14KerningEnabled() {
        return getFontManager().isBase14KerningEnabled();
    }

    /**
     * Controls whether kerning is activated on base 14 fonts.
     * @param value true if kerning should be activated
     * @deprecated use getFontManager().setBase14KerningEnabled(boolean) instead
     */
    public void setBase14KerningEnabled(boolean value) {
        getFontManager().setBase14KerningEnabled(value);
    }

    /** @return the resolution for resolution-dependant input */
    public float getSourceResolution() {
        return this.sourceResolution;
    }

    /**
     * Returns the conversion factor from pixel units to millimeters. This
     * depends on the desired source resolution.
     * @return float conversion factor
     * @see #getSourceResolution()
     */
    public float getSourcePixelUnitToMillimeter() {
        return 25.4f / getSourceResolution();
    }

    /**
     * Sets the source resolution in dpi. This value is used to interpret the pixel size
     * of source documents like SVG images and bitmap images without resolution information.
     * @param dpi resolution in dpi
     */
    public void setSourceResolution(float dpi) {
        this.sourceResolution = dpi;
        if (log.isDebugEnabled()) {
            log.debug("source-resolution set to: " + sourceResolution
                    + "dpi (px2mm=" + getSourcePixelUnitToMillimeter() + ")");
        }
    }

    /** @return the resolution for resolution-dependant output */
    public float getTargetResolution() {
        return this.targetResolution;
    }

    /**
     * Returns the conversion factor from pixel units to millimeters. This
     * depends on the desired target resolution.
     * @return float conversion factor
     * @see #getTargetResolution()
     */
    public float getTargetPixelUnitToMillimeter() {
        return 25.4f / this.targetResolution;
    }

    /**
     * Sets the source resolution in dpi. This value is used to interpret the pixel size
     * of source documents like SVG images and bitmap images without resolution information.
     * @param dpi resolution in dpi
     */
    public void setTargetResolution(float dpi) {
        this.targetResolution = dpi;
    }

    /**
     * Sets the source resolution in dpi. This value is used to interpret the pixel size
     * of source documents like SVG images and bitmap images without resolution information.
     * @param dpi resolution in dpi
     */
    public void setSourceResolution(int dpi) {
        setSourceResolution((float)dpi);
    }

    /**
     * Gets the default page-height to use as fallback,
     * in case page-height="auto"
     *
     * @return the page-height, as a String
     */
    public String getPageHeight() {
        return this.pageHeight;
    }

    /**
     * Sets the page-height to use as fallback, in case
     * page-height="auto"
     *
     * @param pageHeight    page-height as a String
     */
    public void setPageHeight(String pageHeight) {
        this.pageHeight = pageHeight;
        if (log.isDebugEnabled()) {
            log.debug("Default page-height set to: " + pageHeight);
        }
    }

    /**
     * Gets the default page-width to use as fallback,
     * in case page-width="auto"
     *
     * @return the page-width, as a String
     */
    public String getPageWidth() {
        return this.pageWidth;
    }

    /**
     * Sets the page-width to use as fallback, in case
     * page-width="auto"
     *
     * @param pageWidth    page-width as a String
     */
    public void setPageWidth(String pageWidth) {
        this.pageWidth = pageWidth;
        if (log.isDebugEnabled()) {
            log.debug("Default page-width set to: " + pageWidth);
        }
    }

    /**
     * Adds a namespace to the set of ignored namespaces.
     * If FOP encounters a namespace which it cannot handle, it issues a warning except if this
     * namespace is in the ignored set.
     * @param namespaceURI the namespace URI
     */
    public void ignoreNamespace(String namespaceURI) {
        this.ignoredNamespaces.add(namespaceURI);
    }

    /**
     * Adds a collection of namespaces to the set of ignored namespaces.
     * If FOP encounters a namespace which it cannot handle, it issues a warning except if this
     * namespace is in the ignored set.
     * @param namespaceURIs the namespace URIs
     */
    public void ignoreNamespaces(Collection namespaceURIs) {
        this.ignoredNamespaces.addAll(namespaceURIs);
    }

    /**
     * Indicates whether a namespace URI is on the ignored list.
     * @param namespaceURI the namespace URI
     * @return true if the namespace is ignored by FOP
     */
    public boolean isNamespaceIgnored(String namespaceURI) {
        return this.ignoredNamespaces.contains(namespaceURI);
    }

    /** @return the set of namespaces that are ignored by FOP */
    public Set getIgnoredNamespace() {
        return Collections.unmodifiableSet(this.ignoredNamespaces);
    }

    //------------------------------------------- Configuration stuff

    /**
     * Set the user configuration.
     * @param userConfigFile the configuration file
     * @throws IOException if an I/O error occurs
     * @throws SAXException if a parsing error occurs
     */
    public void setUserConfig(File userConfigFile) throws SAXException, IOException {
        config.setUserConfig(userConfigFile);
    }

    /**
     * Set the user configuration from an URI.
     * @param uri the URI to the configuration file
     * @throws IOException if an I/O error occurs
     * @throws SAXException if a parsing error occurs
     */
    public void setUserConfig(String uri) throws SAXException, IOException {
        config.setUserConfig(uri);
    }

    /**
     * Set the user configuration.
     * @param userConfig configuration
     * @throws FOPException if a configuration problem occurs
     */
    public void setUserConfig(Configuration userConfig) throws FOPException {
        config.setUserConfig(userConfig);
    }

    /**
     * Get the user configuration.
     * @return the user configuration
     */
    public Configuration getUserConfig() {
        return config.getUserConfig();
    }

    /**
     * Is the user configuration to be validated?
     * @param strictUserConfigValidation strict user config validation
     */
    public void setStrictUserConfigValidation(boolean strictUserConfigValidation) {
        this.strictUserConfigValidation = strictUserConfigValidation;
        this.foURIResolver.setThrowExceptions(strictUserConfigValidation);
    }

    /**
     * Is the user configuration to be validated?
     * @return if the user configuration should be validated
     */
    public boolean validateUserConfigStrictly() {
        return this.strictUserConfigValidation;
    }

    //------------------------------------------- Font related stuff

    /**
     * Whether or not to cache results of font triplet detection/auto-config
     * @param useCache use cache or not
     * @deprecated use getFontManager().setUseCache(boolean) instead
     */
    public void setUseCache(boolean useCache) {
        getFontManager().setUseCache(useCache);
    }

    /**
     * Cache results of font triplet detection/auto-config?
     * @return whether this factory is uses the cache
     * @deprecated use getFontManager().useCache() instead
     */
    public boolean useCache() {
        return getFontManager().useCache();
    }

    /**
     * Returns the font cache instance used by this factory.
     * @return the font cache
     * @deprecated use getFontManager().getFontCache() instead
     */
    public FontCache getFontCache() {
        return getFontManager().getFontCache();
    }

    /**
     * Returns the font manager.
     * @return the font manager
     */
    public FontManager getFontManager() {
        return this.fontManager;
    }

    /**
     * Attempts to resolve the given URI.
     * Will use the configured resolver and if not successful fall back
     * to the default resolver.
     * @param href URI to access
     * @param baseUri the base URI to resolve against
     * @return A {@link javax.xml.transform.Source} object, or null if the URI
     * cannot be resolved.
     * @see org.apache.fop.apps.FOURIResolver
     */
    public Source resolveURI(String href, String baseUri) {
        Source source = null;
        try {
            source = foURIResolver.resolve(href, baseUri);
        } catch (TransformerException e) {
            log.error("Attempt to resolve URI '" + href + "' failed: ", e);
        }
        return source;
    }

    /**
     * Create (if needed) and return an ICC ColorSpace instance.
     *
     * The ICC profile source is taken from the src attribute of the color-profile FO element.
     * If the ICC ColorSpace is not yet in the cache a new one is created and stored in the cache.
     *
     * The FOP URI resolver is used to try and locate the ICC file.
     * If that fails null is returned.
     *
     * @param baseUri a base URI to resolve relative URIs
     * @param iccProfileSrc ICC Profile source to return a ColorSpace for
     * @return ICC ColorSpace object or null if ColorSpace could not be created
     */
    public ColorSpace getColorSpace(String baseUri, String iccProfileSrc) {
        return colorSpaceCache.get(baseUri, iccProfileSrc);
    }

}
