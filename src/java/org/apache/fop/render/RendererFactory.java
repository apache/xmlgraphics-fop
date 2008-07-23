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
import org.apache.fop.render.intermediate.AbstractIFPainterMaker;
import org.apache.fop.render.intermediate.IFPainter;
import org.apache.fop.render.intermediate.IFPainterConfigurator;
import org.apache.fop.render.intermediate.IFRenderer;

/**
 * Factory for FOEventHandlers and Renderers.
 */
public class RendererFactory {

    /** the logger */
    private static Log log = LogFactory.getLog(RendererFactory.class);

    private Map rendererMakerMapping = new java.util.HashMap();
    private Map eventHandlerMakerMapping = new java.util.HashMap();
    private Map painterMakerMapping = new java.util.HashMap();

    /**
     * Main constructor.
     */
    public RendererFactory() {
        discoverRenderers();
        discoverFOEventHandlers();
        discoverPainters();
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
     * Add a new painter maker. If another maker has already been registered for a
     * particular MIME type, this call overwrites the existing one.
     * @param maker the painter maker
     */
    public void addPainterMaker(AbstractIFPainterMaker maker) {
        String[] mimes = maker.getSupportedMimeTypes();
        for (int i = 0; i < mimes.length; i++) {
            //This overrides any renderer previously set for a MIME type
            if (painterMakerMapping.get(mimes[i]) != null) {
                log.trace("Overriding painter for " + mimes[i]
                        + " with " + maker.getClass().getName());
            }
            painterMakerMapping.put(mimes[i], maker);
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
     * Add a new painter maker. If another maker has already been registered for a
     * particular MIME type, this call overwrites the existing one.
     * @param className the fully qualified class name of the painter maker
     */
    public void addPainterMaker(String className) {
        try {
            AbstractIFPainterMaker makerInstance
                = (AbstractIFPainterMaker)Class.forName(className).newInstance();
            addPainterMaker(makerInstance);
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
                                               + AbstractIFPainterMaker.class.getName());
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
    public AbstractIFPainterMaker getPainterMaker(String mime) {
        AbstractIFPainterMaker maker
            = (AbstractIFPainterMaker)painterMakerMapping.get(mime);
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
        if (userAgent.getRendererOverride() != null) {
            return userAgent.getRendererOverride();
        } else {
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
                AbstractIFPainterMaker painterMaker = getPainterMaker(outputFormat);
                if (painterMaker != null) {
                    IFRenderer rend = new IFRenderer();
                    rend.setUserAgent(userAgent);
                    IFPainter painter = createPainter(userAgent, outputFormat);
                    rend.setPainter(painter);
                    return rend;
                } else {
                    throw new UnsupportedOperationException(
                            "No renderer for the requested format available: " + outputFormat);
                }
            }
        }
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
                AbstractIFPainterMaker painterMaker = null;
                boolean outputStreamMissing = (userAgent.getRendererOverride() == null);
                if (rendMaker == null) {
                    painterMaker = getPainterMaker(outputFormat);
                    if (painterMaker != null) {
                        outputStreamMissing &= (out == null) && (painterMaker.needsOutputStream());
                    }
                } else {
                    outputStreamMissing &= (out == null) && (rendMaker.needsOutputStream());
                }
                if (userAgent.getRendererOverride() != null
                        || rendMaker != null
                        || painterMaker != null) {
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
     * Creates a {@code IFPainter} object based on render-type desired
     * @param userAgent the user agent for access to configuration
     * @param outputFormat the MIME type of the output format to use (ex. "application/pdf").
     * @return the new {@code IFPainter} instance
     * @throws FOPException if the painter cannot be properly constructed
     */
    public IFPainter createPainter(FOUserAgent userAgent, String outputFormat)
                    throws FOPException {
        /*
        if (userAgent.getIFPainterOverride() != null) {
            return userAgent.getIFPainterOverride();
        } else {
        */
            AbstractIFPainterMaker maker = getPainterMaker(outputFormat);
            if (maker == null) {
                throw new UnsupportedOperationException(
                        "No renderer for the requested format available: " + outputFormat);
            }
            IFPainter painter = maker.makePainter(userAgent);
            painter.setUserAgent(userAgent);
            IFPainterConfigurator configurator = maker.getConfigurator(userAgent);
            if (configurator != null) {
                configurator.configure(painter);
                configurator.setupFontInfo(painter);
            }
            return painter;
        //}
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
        iter = this.painterMakerMapping.keySet().iterator();
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
     * Discovers {@code IFPainter} implementations through the classpath and dynamically
     * registers them.
     */
    private void discoverPainters() {
        // add mappings from available services
        Iterator providers
            = Service.providers(IFPainter.class);
        if (providers != null) {
            while (providers.hasNext()) {
                AbstractIFPainterMaker maker = (AbstractIFPainterMaker)providers.next();
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Dynamically adding maker for IFPainter: "
                                + maker.getClass().getName());
                    }
                    addPainterMaker(maker);
                } catch (IllegalArgumentException e) {
                    log.error("Error while adding maker for IFPainter", e);
                }

            }
        }
    }

}
