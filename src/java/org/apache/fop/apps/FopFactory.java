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
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.ElementMappingRegistry;
import org.apache.fop.hyphenation.HyphenationTreeResolver;
import org.apache.fop.image.ImageFactory;
import org.apache.fop.layoutmgr.LayoutManagerMaker;
import org.apache.fop.render.RendererFactory;
import org.apache.fop.render.XMLHandlerRegistry;
import org.apache.fop.util.ContentHandlerFactoryRegistry;

/**
 * Factory class which instantiates new Fop and FOUserAgent instances. This
 * class also holds environmental information and configuration used by FOP.
 * Information that may potentially be different for each rendering run can be
 * found and managed in the FOUserAgent.
 */
public class FopFactory {
    
    /** Defines the default target resolution (72dpi) for FOP */
    public static final float DEFAULT_TARGET_RESOLUTION = 72.0f; //dpi

    /** Defines the default source resolution (72dpi) for FOP */
    private static final float DEFAULT_SOURCE_RESOLUTION = 72.0f; //dpi

    /** Defines the default page-height */
    private static final String DEFAULT_PAGE_HEIGHT = "11in";
    
    /** Defines the default page-width */
    private static final String DEFAULT_PAGE_WIDTH = "8.26in";
    
    /** Defines if FOP should use strict validation for FO and user config */
    private static final boolean DEFAULT_STRICT_FO_VALIDATION = true;

    /** Defines if FOP should validate the user config strictly */
    private static final boolean DEFAULT_STRICT_USERCONFIG_VALIDATION = true;
    
    /** Defines if FOP should use an alternative rule to determine text indents */
    private static final boolean DEFAULT_BREAK_INDENT_INHERITANCE = false;

    /** logger instance */
    private static Log log = LogFactory.getLog(FopFactory.class);
    
    /** Factory for Renderers and FOEventHandlers */
    private RendererFactory rendererFactory = new RendererFactory();
    
    /** Registry for XML handlers */
    private XMLHandlerRegistry xmlHandlers = new XMLHandlerRegistry();
    
    /** The registry for ElementMapping instances */
    private ElementMappingRegistry elementMappingRegistry;

    /** The registry for ContentHandlerFactory instance */ 
    private ContentHandlerFactoryRegistry contentHandlerFactoryRegistry 
                = new ContentHandlerFactoryRegistry();
    
    /** Our default resolver if none is set */
    private URIResolver foURIResolver = new FOURIResolver();
    
    /** A user settable URI Resolver */
    private URIResolver uriResolver = null;

    /** The resolver for user-supplied hyphenation patterns */
    private HyphenationTreeResolver hyphResolver;
    
    private ImageFactory imageFactory = new ImageFactory();

    /** user configuration */
    private Configuration userConfig = null;

    /**
     *  The base URL for all URL resolutions, especially for
     *  external-graphics.
     */
    private String baseURL;

    /** The base URL for all font URL resolutions. */
    private String fontBaseURL;

    /** The base URL for all hyphen URL resolutions. */
    private String hyphenBaseURL;

    /**
     * FOP has the ability, for some FO's, to continue processing even if the
     * input XSL violates that FO's content model.  This is the default  
     * behavior for FOP.  However, this flag, if set, provides the user the
     * ability for FOP to halt on all content model violations if desired.
     */ 
    private boolean strictFOValidation = DEFAULT_STRICT_FO_VALIDATION;

    /**
     * FOP will validate the contents of the user configuration strictly
     * (e.g. base-urls and font urls/paths).
     */
    private boolean strictUserConfigValidation = DEFAULT_STRICT_USERCONFIG_VALIDATION;
    
    /** Allows enabling kerning on the base 14 fonts, default is false */
    private boolean enableBase14Kerning = false;
    
    /** Source resolution in dpi */
    private float sourceResolution = DEFAULT_SOURCE_RESOLUTION;

    /** Target resolution in dpi */
    private float targetResolution = DEFAULT_TARGET_RESOLUTION;

    /** Page height */
    private String pageHeight = DEFAULT_PAGE_HEIGHT;
    
    /** Page width */
    private String pageWidth = DEFAULT_PAGE_WIDTH;

    /** @see #setBreakIndentInheritanceOnReferenceAreaBoundary(boolean) */
    private boolean breakIndentInheritanceOnReferenceAreaBoundary
        = DEFAULT_BREAK_INDENT_INHERITANCE;

    /** Optional overriding LayoutManagerMaker */
    private LayoutManagerMaker lmMakerOverride = null;

    private Set ignoredNamespaces = new java.util.HashSet();
    
    /** Map with cached ICC based ColorSpace objects. */
    private Map colorSpaceMap = null;
    
    /**
     * Main constructor.
     */
    protected FopFactory() {
        this.elementMappingRegistry = new ElementMappingRegistry(this);
        // Use a synchronized Map - I am not really sure this is needed, but better safe than sorry.
        this.colorSpaceMap = Collections.synchronizedMap(new java.util.HashMap());
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
        return new Fop(outputFormat, newFOUserAgent());
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
        if (userAgent == null) {
            throw new NullPointerException("The userAgent parameter must not be null!");
        }
        return new Fop(outputFormat, userAgent);
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
        return new Fop(outputFormat, newFOUserAgent(), stream);
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

    /** @return the image factory */
    public ImageFactory getImageFactory() {
        return this.imageFactory;
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
     * @param baseURL base URL
     */
    void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    /**
     * Returns the base URL.
     * @return the base URL
     */
    public String getBaseURL() {
        return this.baseURL;
    }

    /**
     * Sets the font base URL.
     * @param fontBaseURL font base URL
     */
    public void setFontBaseURL(String fontBaseURL) {
        this.fontBaseURL = fontBaseURL;
    }

    /** @return the font base URL */
    public String getFontBaseURL() {
        return this.fontBaseURL;
    }

    /** @return the hyphen base URL */
    public String getHyphenBaseURL() {
        return hyphenBaseURL;
    }

    /**
     * Sets the hyphen base URL.
     * @param hyphenBaseURL hythen base URL
     */
    public void setHyphenBaseURL(final String hyphenBaseURL) {
        if (hyphenBaseURL != null) {
            this.hyphResolver = new HyphenationTreeResolver() {
                public Source resolve(String href) {
                    return resolveURI(href, hyphenBaseURL);
                }
            };
        }
        this.hyphenBaseURL = hyphenBaseURL;
    }

    /**
     * Sets the URI Resolver. It is used for resolving factory-level URIs like hyphenation
     * patterns and as backup for URI resolution performed during a rendering run. 
     * @param resolver the new URI resolver
     */
    public void setURIResolver(URIResolver resolver) {
        this.uriResolver = resolver;
    }

    /**
     * Returns the URI Resolver.
     * @return the URI Resolver
     */
    public URIResolver getURIResolver() {
        return this.uriResolver;
    }

    /** @return the HyphenationTreeResolver for resolving user-supplied hyphenation patterns. */
    public HyphenationTreeResolver getHyphenationTreeResolver() {
        return this.hyphResolver;
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
    
    /** @return true if kerning on base 14 fonts is enabled */
    public boolean isBase14KerningEnabled() {
        return this.enableBase14Kerning;
    }
    
    /**
     * Controls whether kerning is activated on base 14 fonts.
     * @param value true if kerning should be activated
     */
    public void setBase14KerningEnabled(boolean value) {
        this.enableBase14Kerning = value;
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
        log.info("source-resolution set to: " + sourceResolution 
                + "dpi (px2mm=" + getSourcePixelUnitToMillimeter() + ")");
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
        log.info("Default page-height set to: " + pageHeight);
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
        log.info("Default page-width set to: " + pageWidth);
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
        try {
            DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
            setUserConfig(cfgBuilder.buildFromFile(userConfigFile));
        } catch (ConfigurationException e) {
            throw new FOPException(e);
        }
    }
    
    /**
     * Set the user configuration from an URI.
     * @param uri the URI to the configuration file
     * @throws IOException if an I/O error occurs
     * @throws SAXException if a parsing error occurs
     */
    public void setUserConfig(String uri) throws SAXException, IOException {
        try {
            DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
            setUserConfig(cfgBuilder.build(uri));
        } catch (ConfigurationException e) {
            throw new FOPException(e);
        }
    }
    
    /**
     * Set the user configuration.
     * @param userConfig configuration
     * @throws FOPException if a configuration problem occurs 
     */
    public void setUserConfig(Configuration userConfig) throws FOPException {
        this.userConfig = userConfig;
        try {
            configure(userConfig);
        } catch (ConfigurationException e) {            
            throw new FOPException(e);
        }
    }

    /**
     * Get the user configuration.
     * @return the user configuration
     */
    public Configuration getUserConfig() {
        return userConfig;
    }

    /**
     * Returns the configuration subtree for a specific renderer.
     * @param mimeType MIME type of the renderer
     * @return the requested configuration subtree, null if there's no configuration
     */
    public Configuration getUserRendererConfig(String mimeType) {
        if (userConfig == null || mimeType == null) {
            return null;
        }
        
        Configuration userRendererConfig = null;

        Configuration[] cfgs
            = userConfig.getChild("renderers").getChildren("renderer");
        for (int i = 0; i < cfgs.length; ++i) {
            Configuration child = cfgs[i];
            try {
                if (child.getAttribute("mime").equals(mimeType)) {
                    userRendererConfig = child;
                    break;
                }
            } catch (ConfigurationException e) {
                // silently pass over configurations without mime type
            }
        }
        log.debug((userRendererConfig == null ? "No u" : "U")
                  + "ser configuration found for MIME type " + mimeType);
        return userRendererConfig;
    }

    /**
     * Initializes user agent settings from the user configuration
     * file, if present: baseURL, resolution, default page size,...
     * 
     * @throws ConfigurationException when there is an entry that 
     *          misses the required attribute
     * Configures the FopFactory.
     * @param cfg Avalon Configuration Object
     * @see org.apache.avalon.framework.configuration.Configurable
     */
    public void configure(Configuration cfg) throws ConfigurationException {        
        log.info("Initializing FopFactory Configuration");        
        
        if (cfg.getChild("strict-configuration", false) != null) {
            this.strictUserConfigValidation
                    = cfg.getChild("strict-configuration").getValueAsBoolean();
        }
        if (cfg.getChild("strict-validation", false) != null) {
            this.strictFOValidation = cfg.getChild("strict-validation").getValueAsBoolean();
        }
        if (cfg.getChild("base", false) != null) {
            try {
                setBaseURL(getBaseURLfromConfig(cfg, "base"));
            } catch (ConfigurationException e) {
                if (strictUserConfigValidation) {
                    throw e;
                }
                log.error(e.getMessage());
            }
        }
        if (cfg.getChild("font-base", false) != null) {
            try {
                setFontBaseURL(getBaseURLfromConfig(cfg, "font-base"));
            } catch (ConfigurationException e) {
                if (strictUserConfigValidation) {
                    throw e;
                }
                log.error(e.getMessage());
            }
        }
        if (cfg.getChild("hyphenation-base", false) != null) {
            try {
                setHyphenBaseURL(getBaseURLfromConfig(cfg, "hyphenation-base"));
            } catch (ConfigurationException e) {
                if (strictUserConfigValidation) {
                    throw e;
                }
                log.error(e.getMessage());
            }
        }
        if (cfg.getChild("source-resolution", false) != null) {
            setSourceResolution(
                    cfg.getChild("source-resolution").getValueAsFloat(DEFAULT_SOURCE_RESOLUTION));
        }
        if (cfg.getChild("target-resolution", false) != null) {
            setTargetResolution(
                    cfg.getChild("target-resolution").getValueAsFloat(DEFAULT_TARGET_RESOLUTION));
        }
        if (cfg.getChild("break-indent-inheritance", false) != null) {
            setBreakIndentInheritanceOnReferenceAreaBoundary(
                    cfg.getChild("break-indent-inheritance").getValueAsBoolean());
        }        
        Configuration pageConfig = cfg.getChild("default-page-settings");
        if (pageConfig.getAttribute("height", null) != null) {
            setPageHeight(pageConfig.getAttribute("height", DEFAULT_PAGE_HEIGHT));
        }
        if (pageConfig.getAttribute("width", null) != null) {
            setPageWidth(pageConfig.getAttribute("width", DEFAULT_PAGE_WIDTH));
        }
    }

    /**
     * Retrieves and verifies a base URL.
     * @param cfg The Configuration object to retrieve the base URL from
     * @param name the element name for the base URL
     * @return the requested base URL or null if not available
     * @throws ConfigurationException 
     */    
    public static String getBaseURLfromConfig(Configuration cfg, String name)
    throws ConfigurationException {
        if (cfg.getChild(name, false) != null) {
            try {
                String cfgBasePath = cfg.getChild(name).getValue(null);
                if (cfgBasePath != null) {
                    // Is the path a dirname?
                    File dir = new File(cfgBasePath);
//                    if (!dir.exists()) {
//                        throw new ConfigurationException("Base URL '" + name
//                                + "' references non-existent resource '"
//                                + cfgBasePath + "'");
//                    } else if (dir.isDirectory()) {
                    if (dir.isDirectory()) {
                        // Yes, convert it into a URL
                        cfgBasePath = dir.toURL().toExternalForm(); 
                    }
                    // Otherwise, this is already a URL
                }
                log.info(name + " set to: " + cfgBasePath);
                return cfgBasePath;
            } catch (MalformedURLException mue) {
                throw new ConfigurationException("Base URL '" + name
                        + "' in user config is malformed!");
            }
        }
        return null;
    }

    /**
     * Is the user configuration to be validated?
     * @param strictUserConfigValidation strict user config validation
     */
    public void setStrictUserConfigValidation(boolean strictUserConfigValidation) {
        this.strictUserConfigValidation = strictUserConfigValidation;
    }

    /**
     * Is the user configuration to be validated?
     * @return if the user configuration should be validated
     */
    public boolean validateUserConfigStrictly() {
        return this.strictUserConfigValidation;
    }

    //------------------------------------------- URI resolution

    /**
     * Attempts to resolve the given URI.
     * Will use the configured resolver and if not successful fall back
     * to the default resolver.
     * @param uri URI to access
     * @param base the base URI to resolve against
     * @return A {@link javax.xml.transform.Source} object, or null if the URI
     * cannot be resolved. 
     * @see org.apache.fop.apps.FOURIResolver
     */
    public Source resolveURI(String uri, String base) {
        Source source = null;
        //RFC 2397 data URLs don't need to be resolved, just decode them.
        boolean bypassURIResolution = uri.startsWith("data:");
        if (!bypassURIResolution && uriResolver != null) {
            try {
                source = uriResolver.resolve(uri, base);
            } catch (TransformerException te) {
                log.error("Attempt to resolve URI '" + uri + "' failed: ", te);
            }
        }
        if (source == null) {
            // URI Resolver not configured or returned null, use default resolver
            try {
                source = foURIResolver.resolve(uri, base);
            } catch (TransformerException te) {
                log.error("Attempt to resolve URI '" + uri + "' failed: ", te);
            }
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
     * @param base a base URI to resolve relative URIs
     * @param iccProfileSrc ICC Profile source to return a ColorSpace for
     * @return ICC ColorSpace object or null if ColorSpace could not be created 
     */
    public ColorSpace getColorSpace(String base, String iccProfileSrc) {
        ColorSpace colorSpace = null;
        if (!this.colorSpaceMap.containsKey(base + iccProfileSrc)) {
            try {
                ICC_Profile iccProfile = null;
                // First attempt to use the FOP URI resolver to locate the ICC
                // profile
                Source src = this.resolveURI(iccProfileSrc, base);
                if (src != null && src instanceof StreamSource) {
                    // FOP URI resolver found ICC profile - create ICC profile
                    // from the Source
                    iccProfile = ICC_Profile.getInstance(((StreamSource) src)
                            .getInputStream());
                } else {
                    // TODO - Would it make sense to fall back on VM ICC
                    // resolution
                    // Problem is the cache might be more difficult to maintain
                    // 
                    // FOP URI resolver did not find ICC profile - perhaps the
                    // Java VM can find it?
                    // iccProfile = ICC_Profile.getInstance(iccProfileSrc);
                }
                if (iccProfile != null) {
                    colorSpace = new ICC_ColorSpace(iccProfile);
                }
            } catch (IOException e) {
                // Ignore exception - will be logged a bit further down
                // (colorSpace == null case)
            }

            if (colorSpace != null) {
                // Put in cache (not when VM resolved it as we can't control
                this.colorSpaceMap.put(base + iccProfileSrc, colorSpace);
            } else {
                // TODO To avoid an excessive amount of warnings perhaps
                // register a null ColorMap in the colorSpaceMap
                log.warn("Color profile '" + iccProfileSrc + "' not found.");
            }
        } else {
            colorSpace = (ColorSpace) this.colorSpaceMap.get(base
                    + iccProfileSrc);
        }
        return colorSpace;
    }
}
