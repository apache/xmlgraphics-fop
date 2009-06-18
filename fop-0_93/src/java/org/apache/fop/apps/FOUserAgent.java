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

// Java
import java.io.File;
import java.util.Date;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

// avalon configuration
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

// commons logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// FOP
import org.apache.fop.Version;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.pdf.PDFEncryptionParams;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererFactory;
import org.apache.fop.render.XMLHandlerRegistry;
import org.apache.fop.render.pdf.PDFRenderer;

/**
 * This is the user agent for FOP.
 * It is the entity through which you can interact with the XSL-FO processing and is
 * used by the processing to obtain user configurable options.
 * <p>
 * Renderer specific extensions (that do not produce normal areas on
 * the output) will be done like so:
 * <br>
 * The extension will create an area, custom if necessary
 * <br>
 * this area will be added to the user agent with a key
 * <br>
 * the renderer will know keys for particular extensions
 * <br>
 * eg. bookmarks will be held in a special hierarchical area representing
 * the title and bookmark structure
 * <br>
 * These areas may contain resolvable areas that will be processed
 * with other resolvable areas
 */
public class FOUserAgent {

    /** Defines the default target resolution (72dpi) for FOP */
    public static final float DEFAULT_TARGET_RESOLUTION = 72.0f; //dpi

    private static Log log = LogFactory.getLog("FOP");

    private FopFactory factory;
    
    /** The base URL for all URL resolutions, especially for external-graphics */
    private String baseURL;
    
    /** A user settable URI Resolver */
    private URIResolver uriResolver = null;
    
    private float targetResolution = DEFAULT_TARGET_RESOLUTION;
    private Map rendererOptions = new java.util.HashMap();
    private File outputFile = null;
    private Renderer rendererOverride = null;
    private FOEventHandler foEventHandlerOverride = null;
    
    /** Producer:  Metadata element for the system/software that produces
     * the document. (Some renderers can store this in the document.)
     */
    protected String producer = "Apache FOP Version " + Version.getVersion();

    /** Creator:  Metadata element for the user that created the
     * document. (Some renderers can store this in the document.)
     */
    protected String creator = null;

    /** Creation Date:  Override of the date the document was created. 
     * (Some renderers can store this in the document.)
     */
    protected Date creationDate = null;
    
    /** Author of the content of the document. */
    protected String author = null;
    /** Title of the document. */
    protected String title = null;
    /** Set of keywords applicable to this document. */
    protected String keywords = null;
    
    /**
     * Default constructor
     * @see org.apache.fop.apps.FopFactory
     * @deprecated Provided for compatibility only. Please use the methods from 
     *             FopFactory to construct FOUserAgent instances!
     */
    public FOUserAgent() {
        this(FopFactory.newInstance());
    }
    
    /**
     * Main constructor. <b>This constructor should not be called directly. Please use the 
     * methods from FopFactory to construct FOUserAgent instances!</b>
     * @param factory the factory that provides environment-level information
     * @see org.apache.fop.apps.FopFactory
     */
    public FOUserAgent(FopFactory factory) {
        if (factory == null) {
            throw new NullPointerException("The factory parameter must not be null");
        }
        this.factory = factory;
        if (factory.getUserConfig() != null) {
            configure(factory.getUserConfig());
        }
    }
    
    /** @return the associated FopFactory instance */
    public FopFactory getFactory() {
        return this.factory;
    }
    
    // ---------------------------------------------- rendering-run dependent stuff
    
    /**
     * Sets an explicit renderer to use which overrides the one defined by the 
     * render type setting.  
     * @param renderer the Renderer instance to use
     */
    public void setRendererOverride(Renderer renderer) {
        this.rendererOverride = renderer;
    }

    /**
     * Returns the overriding Renderer instance, if any.
     * @return the overriding Renderer or null
     */
    public Renderer getRendererOverride() {
        return rendererOverride;
    }

    /**
     * Sets an explicit FOEventHandler instance which overrides the one
     * defined by the render type setting.  
     * @param handler the FOEventHandler instance
     */
    public void setFOEventHandlerOverride(FOEventHandler handler) {
        this.foEventHandlerOverride = handler;
    }

    /**
     * Returns the overriding FOEventHandler instance, if any.
     * @return the overriding FOEventHandler or null
     */
    public FOEventHandler getFOEventHandlerOverride() {
        return this.foEventHandlerOverride;
    }

    /**
     * Sets the producer of the document.  
     * @param producer source of document
     */
    public void setProducer(String producer) {
        this.producer = producer;
    }

    /**
     * Returns the producer of the document
     * @return producer name
     */
    public String getProducer() {
        return producer;
    }

    /**
     * Sets the creator of the document.  
     * @param creator of document
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * Returns the creator of the document
     * @return creator name
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Sets the creation date of the document.  
     * @param creationDate date of document
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Returns the creation date of the document
     * @return creation date of document
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the author of the document.  
     * @param author of document
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Returns the author of the document
     * @return author name
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the title of the document. This will override any title coming from
     * an fo:title element.  
     * @param title of document
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the title of the document
     * @return title name
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the keywords for the document.  
     * @param keywords for the document
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * Returns the keywords for the document
     * @return the keywords
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Returns the renderer options
     * @return renderer options
     */
    public Map getRendererOptions() {
        return rendererOptions;
    }

    /**
     * Configures the FOUserAgent through the factory's configuration.
     * @param cfg Avalon Configuration Object
     * @see org.apache.avalon.framework.configuration.Configurable
     */
    protected void configure(Configuration cfg) {
        setBaseURL(FopFactory.getBaseURLfromConfig(cfg, "base"));
        if (cfg.getChild("target-resolution", false) != null) {
            this.targetResolution 
                = cfg.getChild("target-resolution").getValueAsFloat(
                        DEFAULT_TARGET_RESOLUTION);
            log.info("Target resolution set to: " + targetResolution 
                    + "dpi (px2mm=" + getTargetPixelUnitToMillimeter() + ")");
        }
    }
    
    /**
     * Returns the configuration subtree for a specific renderer.
     * @param mimeType MIME type of the renderer
     * @return the requested configuration subtree, null if there's no configuration
     */
    public Configuration getUserRendererConfig(String mimeType) {

        Configuration cfg = getFactory().getUserConfig();
        if (cfg == null || mimeType == null) {
            return null;
        }

        Configuration userRendererConfig = null;

        Configuration[] cfgs
            = cfg.getChild("renderers").getChildren("renderer");
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
     * Sets the base URL.
     * @param baseURL base URL
     */
    public void setBaseURL(String baseURL) {
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
     * Sets the URI Resolver.
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

    /**
     * Returns the parameters for PDF encryption.
     * @return the PDF encryption parameters, null if not applicable
     * @deprecated Use (PDFEncryptionParams)getRendererOptions().get("encryption-params") 
     *             instead.
     */
    public PDFEncryptionParams getPDFEncryptionParams() {
        return (PDFEncryptionParams)getRendererOptions().get(PDFRenderer.ENCRYPTION_PARAMS);
    }

    /**
     * Sets the parameters for PDF encryption.
     * @param pdfEncryptionParams the PDF encryption parameters, null to
     * disable PDF encryption
     * @deprecated Use getRendererOptions().put("encryption-params", 
     *             new PDFEncryptionParams(..)) instead or set every parameter separately: 
     *             getRendererOptions().put("noprint", Boolean.TRUE).
     */
    public void setPDFEncryptionParams(PDFEncryptionParams pdfEncryptionParams) {
        getRendererOptions().put(PDFRenderer.ENCRYPTION_PARAMS, pdfEncryptionParams);
    }


    /**
     * Attempts to resolve the given URI.
     * Will use the configured resolver and if not successful fall back
     * to the default resolver.
     * @param uri URI to access
     * @return A {@link javax.xml.transform.Source} object, or null if the URI
     * cannot be resolved. 
     * @see org.apache.fop.apps.FOURIResolver
     */
    public Source resolveURI(String uri) {
        return resolveURI(uri, getBaseURL());
    }

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
            // URI Resolver not configured or returned null, use default resolver from the factory
            source = getFactory().resolveURI(uri, base);
        }
        return source;
    }

    /**
     * Sets the output File.
     * @param f the output File
     */
    public void setOutputFile(File f) {
        this.outputFile = f;
    }

    /**
     * Gets the output File.
     * @return the output File
     */
    public File getOutputFile() {
        return outputFile;
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
    
    /** @return the resolution for resolution-dependant output */
    public float getTargetResolution() {
        return this.targetResolution;
    }

    /**
     * Sets the target resolution in dpi. This value defines the target resolution of
     * bitmap images generated by the bitmap renderers (such as the TIFF renderer) and of
     * bitmap images generated by filter effects in Apache Batik.
     * @param dpi resolution in dpi
     */
    public void setTargetResolution(int dpi) {
        this.targetResolution = dpi;
    }
    
    // ---------------------------------------------- environment-level stuff
    //                                                (convenience access to FopFactory methods)

    /** @return the font base URL */
    public String getFontBaseURL() {
        String fontBaseURL = getFactory().getFontBaseURL(); 
        return fontBaseURL != null ? fontBaseURL : this.baseURL;
    }

    /**
     * Returns the conversion factor from pixel units to millimeters. This
     * depends on the desired source resolution.
     * @return float conversion factor
     * @see #getSourceResolution()
     */
    public float getSourcePixelUnitToMillimeter() {
        return getFactory().getSourcePixelUnitToMillimeter(); 
    }
    
    /** @return the resolution for resolution-dependant input */
    public float getSourceResolution() {
        return getFactory().getSourceResolution();
    }

    /**
     * Gets the default page-height to use as fallback,
     * in case page-height="auto"
     * 
     * @return the page-height, as a String
     * @see FopFactory#getPageHeight()
     */
    public String getPageHeight() {
        return getFactory().getPageHeight();
    }
    
    /**
     * Gets the default page-width to use as fallback,
     * in case page-width="auto"
     * 
     * @return the page-width, as a String
     * @see FopFactory#getPageWidth()
     */
    public String getPageWidth() {
        return getFactory().getPageWidth();
    }
    
    /**
     * Returns whether FOP is strictly validating input XSL
     * @return true of strict validation turned on, false otherwise
     * @see FopFactory#validateStrictly()
     */
    public boolean validateStrictly() {
        return getFactory().validateStrictly();
    }

    /**
     * @return true if the indent inheritance should be broken when crossing reference area 
     *         boundaries (for more info, see the javadoc for the relative member variable)
     * @see FopFactory#isBreakIndentInheritanceOnReferenceAreaBoundary()
     */
    public boolean isBreakIndentInheritanceOnReferenceAreaBoundary() {
        return getFactory().isBreakIndentInheritanceOnReferenceAreaBoundary();
    }

    /**
     * @return the RendererFactory
     */
    public RendererFactory getRendererFactory() {
        return getFactory().getRendererFactory();
    }

    /**
     * @return the XML handler registry
     */
    public XMLHandlerRegistry getXMLHandlerRegistry() {
        return getFactory().getXMLHandlerRegistry();
    }

}

