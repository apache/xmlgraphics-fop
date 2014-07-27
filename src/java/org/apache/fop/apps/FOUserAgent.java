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
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageSessionContext;
import org.apache.xmlgraphics.util.UnitConv;

import org.apache.fop.Version;
import org.apache.fop.accessibility.Accessibility;
import org.apache.fop.accessibility.DummyStructureTreeEventHandler;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.events.DefaultEventBroadcaster;
import org.apache.fop.events.Event;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventListener;
import org.apache.fop.events.FOPEventListenerProxy;
import org.apache.fop.events.LoggingEventListener;
import org.apache.fop.fo.ElementMappingRegistry;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.layoutmgr.LayoutManagerMaker;
import org.apache.fop.render.ImageHandlerRegistry;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.RendererConfig.RendererConfigParser;
import org.apache.fop.render.RendererConfigOption;
import org.apache.fop.render.RendererFactory;
import org.apache.fop.render.XMLHandlerRegistry;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.util.ColorSpaceCache;
import org.apache.fop.util.ContentHandlerFactoryRegistry;

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

    private static Log log = LogFactory.getLog("FOP");

    private final FopFactory factory;

    private final InternalResourceResolver resourceResolver;

    private float targetResolution = FopFactoryConfig.DEFAULT_TARGET_RESOLUTION;
    private Map rendererOptions = new java.util.HashMap();
    private File outputFile;
    private IFDocumentHandler documentHandlerOverride;
    private Renderer rendererOverride;
    private FOEventHandler foEventHandlerOverride;
    private boolean locatorEnabled = true; // true by default (for error messages).
    private boolean conserveMemoryPolicy;
    private EventBroadcaster eventBroadcaster = new FOPEventBroadcaster();
    private StructureTreeEventHandler structureTreeEventHandler
            = DummyStructureTreeEventHandler.INSTANCE;

    /** Producer:  Metadata element for the system/software that produces
     * the document. (Some renderers can store this in the document.)
     */
    protected String producer = "Apache FOP Version " + Version.getVersion();

    /** Creator:  Metadata element for the user that created the
     * document. (Some renderers can store this in the document.)
     */
    protected String creator;

    /** Creation Date:  Override of the date the document was created.
     * (Some renderers can store this in the document.)
     */
    protected Date creationDate;

    /** Author of the content of the document. */
    protected String author;
    /** Title of the document. */
    protected String title;
    /** Subject of the document. */
    protected String subject;
    /** Set of keywords applicable to this document. */
    protected String keywords;

    private final ImageSessionContext imageSessionContext;

    /**
     * Main constructor. <b>This constructor should not be called directly. Please use the
     * methods from FopFactory to construct FOUserAgent instances!</b>
     * @param factory the factory that provides environment-level information
     * @param resourceResolver the resolver used to acquire resources
     * @see org.apache.fop.apps.FopFactory
     */
    FOUserAgent(final FopFactory factory, InternalResourceResolver resourceResolver) {
        this.factory = factory;
        this.resourceResolver = resourceResolver;
        setTargetResolution(factory.getTargetResolution());
        setAccessibility(factory.isAccessibilityEnabled());
        imageSessionContext = new AbstractImageSessionContext(factory.getFallbackResolver()) {

            public ImageContext getParentContext() {
                return factory;
            }

            public float getTargetResolution() {
                return FOUserAgent.this.getTargetResolution();
            }

            public Source resolveURI(String uri) {
                return FOUserAgent.this.resolveURI(uri);
            }
        };
    }

    /**
     * Returns a new {@link Fop} instance. Use this factory method if your output type
     * requires an output stream and you want to configure this very rendering run,
     * i.e. if you want to set some metadata like the title and author of the document
     * you want to render. In that case, create a new {@link FOUserAgent} instance
     * using {@link org.apache.fop.apps.FopFactory#newFOUserAgent() newFOUserAgent()}.
     * <p>
     * MIME types are used to select the output format (ex. "application/pdf" for PDF). You can
     * use the constants defined in {@link MimeConstants}.
     * @param outputFormat the MIME type of the output format to use (ex. "application/pdf").
     * @param stream the output stream
     * @return the new Fop instance
     * @throws FOPException when the constructor fails
     */
    public Fop newFop(String outputFormat, OutputStream stream) throws FOPException {
        return new Fop(outputFormat, this, stream);
    }


    /**
     * Returns a new {@link Fop} instance. Use this factory method if you want to configure this
     * very rendering run, i.e. if you want to set some metadata like the title and author of the
     * document you want to render. In that case, create a new {@link FOUserAgent}
     * instance using {@link org.apache.fop.apps.FopFactory#newFOUserAgent() newFOUserAgent()}.
     * <p>
     * MIME types are used to select the output format (ex. "application/pdf" for PDF). You can
     * use the constants defined in {@link MimeConstants}.
     * @param outputFormat the MIME type of the output format to use (ex. "application/pdf").
     * @return the new Fop instance
     * @throws FOPException  when the constructor fails
     */
    public Fop newFop(String outputFormat) throws FOPException {
        return newFop(outputFormat, null);
    }


    /**
     * Returns the resource resolver.
     *
     * @return the resource resolver
     */
    public InternalResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    // ---------------------------------------------- rendering-run dependent stuff

    /**
     * Sets an explicit document handler to use which overrides the one that would be
     * selected by default.
     * @param documentHandler the document handler instance to use
     */
    public void setDocumentHandlerOverride(IFDocumentHandler documentHandler) {
        if (isAccessibilityEnabled()) {
            setStructureTreeEventHandler(documentHandler.getStructureTreeEventHandler());
        }
        this.documentHandlerOverride = documentHandler;
    }

    /**
     * Returns the overriding {@link IFDocumentHandler} instance, if any.
     * @return the overriding document handler or null
     */
    public IFDocumentHandler getDocumentHandlerOverride() {
        return this.documentHandlerOverride;
    }

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
     * Sets the subject of the document.
     * @param subject of document
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Returns the subject of the document
     * @return the subject
     */
    public String getSubject() {
        return subject;
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
     * Gets the renderer options given an interface representing renderer configuration options.
     *
     * @param option the renderer option
     * @return the value
    */
    public Object getRendererOption(RendererConfigOption option) {
        return rendererOptions.get(option.getName());
    }

    /**
     * Attempts to resolve the given URI.
     * Will use the configured resolver and if not successful fall back
     * to the default resolver.
     * @param uri URI to access
     * @return A {@link javax.xml.transform.Source} object, or null if the URI
     * cannot be resolved.
     */
    public StreamSource resolveURI(String uri) {
        // TODO: What do we want to do when resources aren't found??? We also need to remove this
        // method entirely
        try {
            // Have to do this so we can resolve data URIs
            StreamSource src = new StreamSource(resourceResolver.getResource(uri));
            src.setSystemId(uri);
            return src;
        } catch (URISyntaxException use) {
            return null;
        } catch (IOException ioe) {
            return null;
        }
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
        return UnitConv.IN2MM / this.targetResolution;
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
    public void setTargetResolution(float dpi) {
        this.targetResolution = dpi;
        if (log.isDebugEnabled()) {
            log.debug("target-resolution set to: " + targetResolution
                    + "dpi (px2mm=" + getTargetPixelUnitToMillimeter() + ")");
        }
    }

    /**
     * Sets the target resolution in dpi. This value defines the target resolution of
     * bitmap images generated by the bitmap renderers (such as the TIFF renderer) and of
     * bitmap images generated by filter effects in Apache Batik.
     * @param dpi resolution in dpi
     */
    public void setTargetResolution(int dpi) {
        setTargetResolution((float)dpi);
    }

    /**
     * Returns the image session context for the image package.
     * @return the ImageSessionContext instance for this rendering run
     */
    public ImageSessionContext getImageSessionContext() {
        return this.imageSessionContext;
    }

    // ---------------------------------------------- environment-level stuff
    //                                                (convenience access to FopFactory methods)

    /**
     * Returns the conversion factor from pixel units to millimeters. This
     * depends on the desired source resolution.
     * @return float conversion factor
     * @see #getSourceResolution()
     */
    public float getSourcePixelUnitToMillimeter() {
        return factory.getSourcePixelUnitToMillimeter();
    }

    /** @return the resolution for resolution-dependant input */
    public float getSourceResolution() {
        return factory.getSourceResolution();
    }

    /**
     * Gets the default page-height to use as fallback,
     * in case page-height="auto"
     *
     * @return the page-height, as a String
     * @see FopFactory#getPageHeight()
     */
    public String getPageHeight() {
        return factory.getPageHeight();
    }

    /**
     * Gets the default page-width to use as fallback,
     * in case page-width="auto"
     *
     * @return the page-width, as a String
     * @see FopFactory#getPageWidth()
     */
    public String getPageWidth() {
        return factory.getPageWidth();
    }

    /**
     * Returns whether FOP is strictly validating input XSL
     * @return true of strict validation turned on, false otherwise
     * @see FopFactory#validateStrictly()
     */
    public boolean validateStrictly() {
        return factory.validateStrictly();
    }

    /**
     * @return true if the indent inheritance should be broken when crossing reference area
     *         boundaries (for more info, see the javadoc for the relative member variable)
     * @see FopFactory#isBreakIndentInheritanceOnReferenceAreaBoundary()
     */
    public boolean isBreakIndentInheritanceOnReferenceAreaBoundary() {
        return factory.isBreakIndentInheritanceOnReferenceAreaBoundary();
    }

    /**
     * @return the RendererFactory
     */
    public RendererFactory getRendererFactory() {
        return factory.getRendererFactory();
    }

    /**
     * @return the XML handler registry
     */
    public XMLHandlerRegistry getXMLHandlerRegistry() {
        return factory.getXMLHandlerRegistry();
    }

    /**
     * Controls the use of SAXLocators to provide location information in error
     * messages.
     *
     * @param enableLocator <code>false</code> if SAX Locators should be disabled
     */
    public void setLocatorEnabled(boolean enableLocator) {
        locatorEnabled = enableLocator;
    }

    /**
     * Checks if the use of Locators is enabled
     * @return true if context information should be stored on each node in the FO tree.
     */
    public boolean isLocatorEnabled() {
        return locatorEnabled;
    }

    /**
     * Returns the event broadcaster that control events sent inside a processing run. Clients
     * can register event listeners with the event broadcaster to listen for events that occur
     * while a document is being processed.
     * @return the event broadcaster.
     */
    public EventBroadcaster getEventBroadcaster() {
        return this.eventBroadcaster;
    }

    private class FOPEventBroadcaster extends DefaultEventBroadcaster {

        private EventListener rootListener;

        public FOPEventBroadcaster() {
            //Install a temporary event listener that catches the first event to
            //do some initialization.
            this.rootListener = new EventListener() {
                public void processEvent(Event event) {
                    if (!listeners.hasEventListeners()) {
                        //Backwards-compatibility: Make sure at least the LoggingEventListener is
                        //plugged in so no events are just silently swallowed.
                        addEventListener(
                                new LoggingEventListener(LogFactory.getLog(FOUserAgent.class)));
                    }
                    //Replace with final event listener
                    rootListener = new FOPEventListenerProxy(
                            listeners, FOUserAgent.this);
                    rootListener.processEvent(event);
                }
            };
        }

        /** {@inheritDoc} */
        public void broadcastEvent(Event event) {
            rootListener.processEvent(event);
        }

    }

    /**
     * Check whether memory-conservation is enabled.
     *
     * @return true if FOP is to conserve as much as possible
     */
    public boolean isConserveMemoryPolicyEnabled() {
        return this.conserveMemoryPolicy;
    }

    /**
     * Control whether memory-conservation should be enabled
     *
     * @param conserveMemoryPolicy the cachingEnabled to set
     */
    public void setConserveMemoryPolicy(boolean conserveMemoryPolicy) {
        this.conserveMemoryPolicy = conserveMemoryPolicy;
    }

    /**
     * Check whether complex script features are enabled.
     *
     * @return true if FOP is to use complex script features
     */
    public boolean isComplexScriptFeaturesEnabled() {
        return factory.isComplexScriptFeaturesEnabled();
    }

    /**
     * Returns the renderer configuration object for a particular MIME type.
     *
     * @param mimeType the config MIME type
     * @param configCreator the parser for creating the config for the first run of parsing.
     * @return the renderer configuration object
     * @throws FOPException if an error occurs when creating the config object
     */
    public RendererConfig getRendererConfig(String mimeType, RendererConfigParser configCreator)
            throws FOPException {
        return factory.getRendererConfig(this, getRendererConfiguration(mimeType), configCreator);
    }

    /**
     * Returns a {@link Configuration} object for which contains renderer configuration for a given
     * MIME type.
     *
     * @param mimeType the renderer configuration MIME type
     * @return the configuration object
     */
    public Configuration getRendererConfiguration(String mimeType) {
        Configuration cfg = getUserConfig();
        String type = "renderer";
        String mime = "mime";
        if (cfg == null) {
            if (log.isDebugEnabled()) {
                log.debug("userconfig is null");
            }
            return null;
        }

        Configuration userConfig = null;

        Configuration[] cfgs = cfg.getChild(type + "s").getChildren(type);
        for (int i = 0; i < cfgs.length; ++i) {
            Configuration child = cfgs[i];
            try {
                if (child.getAttribute(mime).equals(mimeType)) {
                    userConfig = child;
                    break;
                }
            } catch (ConfigurationException e) {
                // silently pass over configurations without mime type
            }
        }
        log.debug((userConfig == null ? "No u" : "U")
                + "ser configuration found for MIME type " + mimeType);
        return userConfig;
    }

    /**
     * Activates accessibility (for output formats that support it).
     *
     * @param accessibility <code>true</code> to enable accessibility support
     */
    public void setAccessibility(boolean accessibility) {
        if (accessibility) {
            getRendererOptions().put(Accessibility.ACCESSIBILITY, Boolean.TRUE);
        }
    }

    /**
     * Check if accessibility is enabled.
     * @return true if accessibility is enabled
     */
    public boolean isAccessibilityEnabled() {
        Boolean enabled = (Boolean)this.getRendererOptions().get(Accessibility.ACCESSIBILITY);
        if (enabled != null) {
            return enabled.booleanValue();
        } else {
            return false;
        }
    }

    /**
     * Sets the document's structure tree event handler, for use by accessible
     * output formats.
     *
     * @param structureTreeEventHandler The structure tree event handler to set
     */
    public void setStructureTreeEventHandler(StructureTreeEventHandler structureTreeEventHandler) {
        this.structureTreeEventHandler = structureTreeEventHandler;
    }

    /**
     * Returns the document's structure tree event handler, for use by
     * accessible output formats.
     *
     * @return The structure tree event handler
     */
    public StructureTreeEventHandler getStructureTreeEventHandler() {
        return this.structureTreeEventHandler;
    }

    /** @see FopFactory#getLayoutManagerMakerOverride() */
    public LayoutManagerMaker getLayoutManagerMakerOverride() {
        return factory.getLayoutManagerMakerOverride();
    }

    /** @see FopFactory#getContentHandlerFactoryRegistry() */
    public ContentHandlerFactoryRegistry getContentHandlerFactoryRegistry() {
        return factory.getContentHandlerFactoryRegistry();
    }

    /** @see FopFactory#getImageManager() */
    public ImageManager getImageManager() {
        return factory.getImageManager();
    }

    /** @see FopFactory#getElementMappingRegistry() */
    public ElementMappingRegistry getElementMappingRegistry() {
        return factory.getElementMappingRegistry();
    }

    /** @see FopFactory#getFontManager() */
    public FontManager getFontManager() {
        return factory.getFontManager();
    }

    /**
     * Indicates whether a namespace URI is on the ignored list.
     * @param namespaceURI the namespace URI
     * @return true if the namespace is ignored by FOP
     */
    public boolean isNamespaceIgnored(String namespaceURI) {
        return factory.isNamespaceIgnored(namespaceURI);
    }

    /**
     * Is the user configuration to be validated?
     * @return if the user configuration should be validated
     */
    public boolean validateUserConfigStrictly() {
        return factory.validateUserConfigStrictly();
    }

    /**
     * Get the user configuration.
     * @return the user configuration
     */
    public Configuration getUserConfig() {
        return factory.getUserConfig();
    }

    /** @return the image handler registry */
    public ImageHandlerRegistry getImageHandlerRegistry() {
        return factory.getImageHandlerRegistry();
    }

    /** @return the color space cache */
    public ColorSpaceCache getColorSpaceCache() {
        return factory.getColorSpaceCache();
    }

    /** @see FopFactory#getHyphenationPatternNames() */
    public Map<String, String> getHyphenationPatternNames() {
        return factory.getHyphenationPatternNames();
    }
}
