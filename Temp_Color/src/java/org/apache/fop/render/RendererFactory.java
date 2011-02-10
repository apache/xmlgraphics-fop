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

import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.util.Service;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.render.intermediate.AbstractIFDocumentHandlerMaker;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.render.intermediate.IFRenderer;

/**
 * Factory for FOEventHandlers and Renderers.
 */
public class RendererFactory {

    /** the logger */
    private static Log log = LogFactory.getLog(RendererFactory.class);

    private Map rendererMakerMapping = new java.util.HashMap();
    private Map eventHandlerMakerMapping = new java.util.HashMap();
    private Map documentHandlerMakerMapping = new java.util.HashMap();

    private boolean rendererPreferred = false;

    /**
     * Main constructor.
     */
    public RendererFactory() {
        discoverRenderers();
        discoverFOEventHandlers();
        discoverDocumentHandlers();
    }

    /**
     * Controls whether a {@link Renderer} is preferred over a {@link IFDocumentHandler} if
     * both are available for the same MIME type.
     * @param value true to prefer the {@link Renderer},
     *                  false to prefer the {@link IFDocumentHandler}.
     */
    public void setRendererPreferred(boolean value) {
        this.rendererPreferred = value;
    }

    /**
     * Indicates whether a {@link Renderer} is preferred over a {@link IFDocumentHandler} if
     * both are available for the same MIME type.
     * @return true if the {@link Renderer} is preferred,
     *                  false if the {@link IFDocumentHandler} is preferred.
     */
    public boolean isRendererPreferred() {
        return this.rendererPreferred;
    }

    /**
     * Add a new RendererMaker. If another maker has already been registered for a
     * particular MIME type, this call overwrites the existing one.
     * @param maker the RendererMaker
     */
    public void addRendererMaker(AbstractRendererMaker maker) {
        String[] mimes = maker.getSupportedMimeTypes();
        for (int i = 0; i < mimes.length; i++) {
            //This overrides any renderer previously set for a MIME type
            if (rendererMakerMapping.get(mimes[i]) != null) {
                log.trace("Overriding renderer for " + mimes[i]
                        + " with " + maker.getClass().getName());
            }
            rendererMakerMapping.put(mimes[i], maker);
        }
    }

    /**
     * Add a new FOEventHandlerMaker. If another maker has already been registered for a
     * particular MIME type, this call overwrites the existing one.
     * @param maker the FOEventHandlerMaker
     */
    public void addFOEventHandlerMaker(AbstractFOEventHandlerMaker maker) {
        String[] mimes = maker.getSupportedMimeTypes();
        for (int i = 0; i < mimes.length; i++) {
            //This overrides any event handler previously set for a MIME type
            if (eventHandlerMakerMapping.get(mimes[i]) != null) {
                log.trace("Overriding FOEventHandler for " + mimes[i]
                        + " with " + maker.getClass().getName());
            }
            eventHandlerMakerMapping.put(mimes[i], maker);
        }
    }

    /**
     * Add a new document handler maker. If another maker has already been registered for a
     * particular MIME type, this call overwrites the existing one.
     * @param maker the intermediate format document handler maker
     */
    public void addDocumentHandlerMaker(AbstractIFDocumentHandlerMaker maker) {
        String[] mimes = maker.getSupportedMimeTypes();
        for (int i = 0; i < mimes.length; i++) {
            //This overrides any renderer previously set for a MIME type
            if (documentHandlerMakerMapping.get(mimes[i]) != null) {
                log.trace("Overriding document handler for " + mimes[i]
                        + " with " + maker.getClass().getName());
            }
            documentHandlerMakerMapping.put(mimes[i], maker);
        }
    }

    /**
     * Add a new RendererMaker. If another maker has already been registered for a
     * particular MIME type, this call overwrites the existing one.
     * @param className the fully qualified class name of the RendererMaker
     */
    public void addRendererMaker(String className) {
        try {
            AbstractRendererMaker makerInstance
                = (AbstractRendererMaker)Class.forName(className).newInstance();
            addRendererMaker(makerInstance);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not find "
                                               + className);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate "
                                               + className);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access "
                                               + className);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(className
                                               + " is not an "
                                               + AbstractRendererMaker.class.getName());
        }
    }

    /**
     * Add a new FOEventHandlerMaker. If another maker has already been registered for a
     * particular MIME type, this call overwrites the existing one.
     * @param className the fully qualified class name of the FOEventHandlerMaker
     */
    public void addFOEventHandlerMaker(String className) {
        try {
            AbstractFOEventHandlerMaker makerInstance
                = (AbstractFOEventHandlerMaker)Class.forName(className).newInstance();
            addFOEventHandlerMaker(makerInstance);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not find "
                                               + className);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate "
                                               + className);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access "
                                               + className);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(className
                                               + " is not an "
                                               + AbstractFOEventHandlerMaker.class.getName());
        }
    }

    /**
     * Add a new document handler maker. If another maker has already been registered for a
     * particular MIME type, this call overwrites the existing one.
     * @param className the fully qualified class name of the document handler maker
     */
    public void addDocumentHandlerMaker(String className) {
        try {
            AbstractIFDocumentHandlerMaker makerInstance
                = (AbstractIFDocumentHandlerMaker)Class.forName(className).newInstance();
            addDocumentHandlerMaker(makerInstance);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not find "
                                               + className);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate "
                                               + className);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access "
                                               + className);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(className
                                               + " is not an "
                                               + AbstractIFDocumentHandlerMaker.class.getName());
        }
    }

    /**
     * Returns a RendererMaker which handles the given MIME type.
     * @param mime the requested output format
     * @return the requested RendererMaker or null if none is available
     */
    public AbstractRendererMaker getRendererMaker(String mime) {
        AbstractRendererMaker maker
            = (AbstractRendererMaker)rendererMakerMapping.get(mime);
        return maker;
    }

    /**
     * Returns a FOEventHandlerMaker which handles the given MIME type.
     * @param mime the requested output format
     * @return the requested FOEventHandlerMaker or null if none is available
     */
    public AbstractFOEventHandlerMaker getFOEventHandlerMaker(String mime) {
        AbstractFOEventHandlerMaker maker
            = (AbstractFOEventHandlerMaker)eventHandlerMakerMapping.get(mime);
        return maker;
    }

    /**
     * Returns a RendererMaker which handles the given MIME type.
     * @param mime the requested output format
     * @return the requested RendererMaker or null if none is available
     */
    public AbstractIFDocumentHandlerMaker getDocumentHandlerMaker(String mime) {
        AbstractIFDocumentHandlerMaker maker
            = (AbstractIFDocumentHandlerMaker)documentHandlerMakerMapping.get(mime);
        return maker;
    }

    /**
     * Creates a Renderer object based on render-type desired
     * @param userAgent the user agent for access to configuration
     * @param outputFormat the MIME type of the output format to use (ex. "application/pdf").
     * @return the new Renderer instance
     * @throws FOPException if the renderer cannot be properly constructed
     */
    public Renderer createRenderer(FOUserAgent userAgent, String outputFormat)
                    throws FOPException {
        if (userAgent.getDocumentHandlerOverride() != null) {
            return createRendererForDocumentHandler(userAgent.getDocumentHandlerOverride());
        } else if (userAgent.getRendererOverride() != null) {
            return userAgent.getRendererOverride();
        } else {
            Renderer renderer;
            if (isRendererPreferred()) {
                //Try renderer first
                renderer = tryRendererMaker(userAgent, outputFormat);
                if (renderer == null) {
                    renderer = tryIFDocumentHandlerMaker(userAgent, outputFormat);
                }
            } else {
                //Try document handler first
                renderer = tryIFDocumentHandlerMaker(userAgent, outputFormat);
                if (renderer == null) {
                    renderer = tryRendererMaker(userAgent, outputFormat);
                }
            }
            if (renderer == null) {
                throw new UnsupportedOperationException(
                        "No renderer for the requested format available: " + outputFormat);
            }
            return renderer;
        }
    }

    private Renderer tryIFDocumentHandlerMaker(FOUserAgent userAgent, String outputFormat)
            throws FOPException {
        AbstractIFDocumentHandlerMaker documentHandlerMaker
            = getDocumentHandlerMaker(outputFormat);
        if (documentHandlerMaker != null) {
            IFDocumentHandler documentHandler = createDocumentHandler(
                    userAgent, outputFormat);
            return createRendererForDocumentHandler(documentHandler);
        } else {
            return null;
        }
    }

    private Renderer tryRendererMaker(FOUserAgent userAgent, String outputFormat)
                throws FOPException {
        AbstractRendererMaker maker = getRendererMaker(outputFormat);
        if (maker != null) {
            Renderer rend = maker.makeRenderer(userAgent);
            rend.setUserAgent(userAgent);
            RendererConfigurator configurator = maker.getConfigurator(userAgent);
            if (configurator != null) {
                configurator.configure(rend);
            }
            return rend;
        } else {
            return null;
        }
    }

    private Renderer createRendererForDocumentHandler(IFDocumentHandler documentHandler) {
        IFRenderer rend = new IFRenderer();
        rend.setUserAgent(documentHandler.getContext().getUserAgent());
        rend.setDocumentHandler(documentHandler);
        return rend;
    }

    /**
     * Creates FOEventHandler instances based on the desired output.
     * @param userAgent the user agent for access to configuration
     * @param outputFormat the MIME type of the output format to use (ex. "application/pdf").
     * @param out the OutputStream where the output is written to (if applicable)
     * @return the newly constructed FOEventHandler
     * @throws FOPException if the FOEventHandler cannot be properly constructed
     */
    public FOEventHandler createFOEventHandler(FOUserAgent userAgent,
                String outputFormat, OutputStream out) throws FOPException {

        if (userAgent.getFOEventHandlerOverride() != null) {
            return userAgent.getFOEventHandlerOverride();
        } else {
            AbstractFOEventHandlerMaker maker = getFOEventHandlerMaker(outputFormat);
            if (maker != null) {
                return maker.makeFOEventHandler(userAgent, out);
            } else {
                AbstractRendererMaker rendMaker = getRendererMaker(outputFormat);
                AbstractIFDocumentHandlerMaker documentHandlerMaker = null;
                boolean outputStreamMissing = (userAgent.getRendererOverride() == null)
                    && (userAgent.getDocumentHandlerOverride() == null);
                if (rendMaker == null) {
                    documentHandlerMaker = getDocumentHandlerMaker(outputFormat);
                    if (documentHandlerMaker != null) {
                        outputStreamMissing &= (out == null)
                                && (documentHandlerMaker.needsOutputStream());
                    }
                } else {
                    outputStreamMissing &= (out == null) && (rendMaker.needsOutputStream());
                }
                if (userAgent.getRendererOverride() != null
                        || rendMaker != null
                        || userAgent.getDocumentHandlerOverride() != null
                        || documentHandlerMaker != null) {
                    if (outputStreamMissing) {
                        throw new FOPException(
                            "OutputStream has not been set");
                    }
                    //Found a Renderer so we need to construct an AreaTreeHandler.
                    return new AreaTreeHandler(userAgent, outputFormat, out);
                } else {
                    throw new UnsupportedOperationException(
                            "Don't know how to handle \"" + outputFormat + "\" as an output format."
                            + " Neither an FOEventHandler, nor a Renderer could be found"
                            + " for this output format.");
                }
            }
        }
    }

    /**
     * Creates a {@link IFDocumentHandler} object based on the desired output format.
     * @param userAgent the user agent for access to configuration
     * @param outputFormat the MIME type of the output format to use (ex. "application/pdf").
     * @return the new {@link IFDocumentHandler} instance
     * @throws FOPException if the document handler cannot be properly constructed
     */
    public IFDocumentHandler createDocumentHandler(FOUserAgent userAgent, String outputFormat)
                    throws FOPException {
        if (userAgent.getDocumentHandlerOverride() != null) {
            return userAgent.getDocumentHandlerOverride();
        }
        AbstractIFDocumentHandlerMaker maker = getDocumentHandlerMaker(outputFormat);
        if (maker == null) {
            throw new UnsupportedOperationException(
                "No IF document handler for the requested format available: " + outputFormat);
        }
        IFDocumentHandler documentHandler = maker.makeIFDocumentHandler(userAgent);
        IFDocumentHandlerConfigurator configurator = documentHandler.getConfigurator();
        if (configurator != null) {
            configurator.configure(documentHandler);
        }
        return documentHandler;
    }

    /**
     * @return an array of all supported MIME types
     */
    public String[] listSupportedMimeTypes() {
        List lst = new java.util.ArrayList();
        Iterator iter = this.rendererMakerMapping.keySet().iterator();
        while (iter.hasNext()) {
            lst.add(((String)iter.next()));
        }
        iter = this.eventHandlerMakerMapping.keySet().iterator();
        while (iter.hasNext()) {
            lst.add(((String)iter.next()));
        }
        iter = this.documentHandlerMakerMapping.keySet().iterator();
        while (iter.hasNext()) {
            lst.add(((String)iter.next()));
        }
        Collections.sort(lst);
        return (String[])lst.toArray(new String[lst.size()]);
    }

    /**
     * Discovers Renderer implementations through the classpath and dynamically
     * registers them.
     */
    private void discoverRenderers() {
        // add mappings from available services
        Iterator providers
            = Service.providers(Renderer.class);
        if (providers != null) {
            while (providers.hasNext()) {
                AbstractRendererMaker maker = (AbstractRendererMaker)providers.next();
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Dynamically adding maker for Renderer: "
                                + maker.getClass().getName());
                    }
                    addRendererMaker(maker);
                } catch (IllegalArgumentException e) {
                    log.error("Error while adding maker for Renderer", e);
                }

            }
        }
    }

    /**
     * Discovers FOEventHandler implementations through the classpath and dynamically
     * registers them.
     */
    private void discoverFOEventHandlers() {
        // add mappings from available services
        Iterator providers
            = Service.providers(FOEventHandler.class);
        if (providers != null) {
            while (providers.hasNext()) {
                AbstractFOEventHandlerMaker maker = (AbstractFOEventHandlerMaker)providers.next();
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Dynamically adding maker for FOEventHandler: "
                                + maker.getClass().getName());
                    }
                    addFOEventHandlerMaker(maker);
                } catch (IllegalArgumentException e) {
                    log.error("Error while adding maker for FOEventHandler", e);
                }

            }
        }
    }

    /**
     * Discovers {@link IFDocumentHandler} implementations through the classpath and dynamically
     * registers them.
     */
    private void discoverDocumentHandlers() {
        // add mappings from available services
        Iterator providers = Service.providers(IFDocumentHandler.class);
        if (providers != null) {
            while (providers.hasNext()) {
                AbstractIFDocumentHandlerMaker maker
                    = (AbstractIFDocumentHandlerMaker)providers.next();
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Dynamically adding maker for IFDocumentHandler: "
                                + maker.getClass().getName());
                    }
                    addDocumentHandlerMaker(maker);
                } catch (IllegalArgumentException e) {
                    log.error("Error while adding maker for IFDocumentHandler", e);
                }

            }
        }
    }

}
