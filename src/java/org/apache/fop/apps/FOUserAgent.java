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
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageSessionContext;

import org.apache.fop.Version;
import org.apache.fop.accessibility.AccessibilityUtil;
import org.apache.fop.events.DefaultEventBroadcaster;
import org.apache.fop.events.Event;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventListener;
import org.apache.fop.events.FOPEventListenerProxy;
import org.apache.fop.events.LoggingEventListener;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererFactory;
import org.apache.fop.render.XMLHandlerRegistry;
import org.apache.fop.render.intermediate.IFDocumentHandler;

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
    public static final float DEFAULT_TARGET_RESOLUTION
            = FopFactoryConfigurator.DEFAULT_TARGET_RESOLUTION;

    private static Log log = LogFactory.getLog("FOP");

    private FopFactory factory;

    /**
     *  The base URL for all URL resolutions, especially for
     *  external-graphics.
     */
    private String base = null;

    /** A user settable URI Resolver */
    private URIResolver uriResolver = null;

    private float targetResolution = FopFactoryConfigurator.DEFAULT_TARGET_RESOLUTION;
    private Map rendererOptions = new java.util.HashMap();
    private File outputFile = null;
    private IFDocumentHandler documentHandlerOverride = null;
    private Renderer rendererOverride = null;
    private FOEventHandler foEventHandlerOverride = null;
    private boolean locatorEnabled = true; // true by default (for error messages).
    private boolean conserveMemoryPolicy = false;
    private EventBroadcaster eventBroadcaster = new FOPEventBroadcaster();

    //TODO Verify that a byte array is the best solution here
    private byte[] reducedFOTree;  // accessibility: reduced FO

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
    /** Subject of the document. */
    protected String subject = null;
    /** Set of keywords applicable to this document. */
    protected String keywords = null;

    private ImageSessionContext imageSessionContext = new AbstractImageSessionContext() {

        public ImageContext getParentContext() {
            return getFactory();
        }

        public float getTargetResolution() {
            return FOUserAgent.this.getTargetResolution();
        }

        public Source resolveURI(String uri) {
            return FOUserAgent.this.resolveURI(uri);
        }

    };

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
        setBaseURL(factory.getBaseURL());
        setTargetResolution(factory.getTargetResolution());
        if (this.getRendererOptions().get(AccessibilityUtil.ACCESSIBILITY) == null) {
            this.rendererOptions.put(AccessibilityUtil.ACCESSIBILITY, Boolean.FALSE);
        }
    }

    /** @return the associated FopFactory instance */
    public FopFactory getFactory() {
        return this.factory;
    }

    // ---------------------------------------------- rendering-run dependent stuff

    /**
     * Sets an explicit document handler to use which overrides the one that would be
     * selected by default.
     * @param documentHandler the document handler instance to use
     */
    public void setDocumentHandlerOverride(IFDocumentHandler documentHandler) {
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
     * Sets the base URL.
     * @param baseUrl base URL
     */
    public void setBaseURL(String baseUrl) {
        this.base = baseUrl;
    }

    /**
     * Sets font base URL.
     * @param fontBaseUrl font base URL
     * @deprecated Use {@link FontManager#setFontBaseURL(String)} instead.
     */
    public void setFontBaseURL(String fontBaseUrl) {
        try {
            getFactory().getFontManager().setFontBaseURL(fontBaseUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage());
    }
    }

    /**
     * Returns the base URL.
     * @return the base URL
     */
    public String getBaseURL() {
        return this.base;
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
     * @param href URI to access
     * @param base the base URI to resolve against
     * @return A {@link javax.xml.transform.Source} object, or null if the URI
     * cannot be resolved.
     * @see org.apache.fop.apps.FOURIResolver
     */
    public Source resolveURI(String href, String base) {
        Source source = null;
        //RFC 2397 data URLs don't need to be resolved, just decode them through FOP's default
        //URIResolver.
        boolean bypassURIResolution = href.startsWith("data:");
        if (!bypassURIResolution && uriResolver != null) {
            try {
                source = uriResolver.resolve(href, base);
            } catch (TransformerException te) {
                log.error("Attempt to resolve URI '" + href + "' failed: ", te);
            }
        }
        if (source == null) {
            // URI Resolver not configured or returned null, use default resolver from the factory
            source = getFactory().resolveURI(href, base);
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
     * Returns the font base URL.
     * @return the font base URL
     * @deprecated Use {@link FontManager#getFontBaseURL()} instead. This method is not used by FOP.
     */
    public String getFontBaseURL() {
        String fontBase = getFactory().getFontManager().getFontBaseURL();
        return fontBase != null ? fontBase : getBaseURL();
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
     * Check if accessibility is enabled.
     * @return true if accessibility is enabled
     */
    public boolean isAccessibilityEnabled() {
        Boolean enabled = (Boolean)this.getRendererOptions().get(AccessibilityUtil.ACCESSIBILITY);
        if (enabled != null) {
            return enabled.booleanValue();
        } else {
            return false;
        }
    }

    /**
     * Used for accessibility. Stores the reduced FO tree (the result from the second transform)
     * for later use.
     * @param reducedFOTree the result from 2nd transform
     */
    public void setReducedFOTree(byte[] reducedFOTree) {
        this.reducedFOTree = reducedFOTree;
    }

    /**
     * Used for accessibility. Returns the reduced FO tree.
     * @return result from 2nd transform as byte array
     */
    public byte[] getReducedFOTree() {
        return this.reducedFOTree;
    }
}

