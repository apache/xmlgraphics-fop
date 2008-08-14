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

package org.apache.fop.render.intermediate;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.events.ResourceEventProducer;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.ImageHandlerRegistry;
import org.apache.fop.render.RenderingContext;

/**
 * Abstract base class for IFPainter implementations.
 */
public abstract class AbstractIFPainter implements IFPainter {

    /** logging instance */
    private static Log log = LogFactory.getLog(AbstractIFPainter.class);

    /** non-URI that can be used in feedback messages that an image is an instream-object */
    protected static final String INSTREAM_OBJECT_URI = "(instream-object)";

    private FOUserAgent userAgent;

    /** Image handler registry */
    protected ImageHandlerRegistry imageHandlerRegistry = new ImageHandlerRegistry();
    //TODO Move reference to FOPFactory to the user has a chance to add his own implementations
    //and so the lookup process isn't redone for each painter instance.

    /**
     * Default constructor.
     */
    public AbstractIFPainter() {
    }

    /** {@inheritDoc} */
    public void setUserAgent(FOUserAgent ua) {
        if (this.userAgent != null) {
            throw new IllegalStateException("The user agent was already set");
        }
        this.userAgent = ua;
    }

    /**
     * Returns the user agent.
     * @return the user agent
     */
    protected FOUserAgent getUserAgent() {
        return this.userAgent;
    }

    /** {@inheritDoc} */
    public void startDocumentHeader() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void endDocumentHeader() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void startDocumentTrailer() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void endDocumentTrailer() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void startPageHeader() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void endPageHeader() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void startPageTrailer() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void endPageTrailer() throws IFException {
        //nop
    }

    private AffineTransform combine(AffineTransform[] transforms) {
        AffineTransform at = new AffineTransform();
        for (int i = 0, c = transforms.length; i < c; i++) {
            at.concatenate(transforms[i]);
        }
        return at;
    }

    /** {@inheritDoc} */
    public void startViewport(AffineTransform[] transforms, Dimension size, Rectangle clipRect)
            throws IFException {
        startViewport(combine(transforms), size, clipRect);
    }

    /** {@inheritDoc} */
    public void startGroup(AffineTransform[] transforms) throws IFException {
        startGroup(combine(transforms));
    }

    /**
     * Creates a new RenderingContext instance.
     * @return the new rendering context.
     */
    protected abstract RenderingContext createRenderingContext();

    /**
     * Loads a preloaded image and draws it using a suitable image handler.
     * @param info the information object of the preloaded image
     * @param rect the rectangle in which to paint the image
     * @throws ImageException if there's an error while processing the image
     * @throws IOException if there's an I/O error while loading the image
     */
    protected void drawImageUsingImageHandler(ImageInfo info, Rectangle rect)
                    throws ImageException, IOException {
        ImageManager manager = getUserAgent().getFactory().getImageManager();
        ImageSessionContext sessionContext = getUserAgent().getImageSessionContext();

        //Load and convert the image to a supported format
        RenderingContext context = createRenderingContext();
        Map hints = ImageUtil.getDefaultHints(sessionContext);
        org.apache.xmlgraphics.image.loader.Image img = manager.getImage(
                    info, imageHandlerRegistry.getSupportedFlavors(context),
                    hints, sessionContext);

        //First check for a dynamically registered handler
        ImageHandler handler = imageHandlerRegistry.getHandler(context, img);
        if (handler == null) {
            throw new UnsupportedOperationException(
                    "No ImageHandler available for image: "
                        + info + " (" + img.getClass().getName() + ")");
        }

        if (log.isDebugEnabled()) {
            log.debug("Using ImageHandler: " + handler.getClass().getName());
        }
        try {
            //TODO foreign attributes
            handler.handleImage(context, img, rect);
        } catch (IOException ioe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageWritingError(this, ioe);
            return;
        }
    }

    /**
     * Default drawing method for handling an image referenced by a URI.
     * @param uri the image's URI
     * @param rect the rectangle in which to paint the image
     */
    protected void drawImageUsingURI(String uri, Rectangle rect) {
        ImageManager manager = getUserAgent().getFactory().getImageManager();
        ImageInfo info = null;
        try {
            ImageSessionContext sessionContext = getUserAgent().getImageSessionContext();
            info = manager.getImageInfo(uri, sessionContext);

            drawImageUsingImageHandler(info, rect);
        } catch (ImageException ie) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageError(this, (info != null ? info.toString() : uri), ie, null);
        } catch (FileNotFoundException fe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageNotFound(this, (info != null ? info.toString() : uri), fe, null);
        } catch (IOException ioe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageIOError(this, (info != null ? info.toString() : uri), ioe, null);
        }
    }

    /**
     * Default drawing method for handling a foreign object in the form of a DOM document.
     * @param doc the DOM document containing the foreign object
     * @param rect the rectangle in which to paint the image
     */
    protected void drawImageUsingDocument(Document doc, Rectangle rect) {
        ImageManager manager = getUserAgent().getFactory().getImageManager();
        ImageInfo info = null;
        try {
            info = manager.preloadImage(null, new DOMSource(doc));

            drawImageUsingImageHandler(info, rect);
        } catch (ImageException ie) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageError(this,
                    (info != null ? info.toString() : INSTREAM_OBJECT_URI), ie, null);
        } catch (FileNotFoundException fe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageNotFound(this,
                    (info != null ? info.toString() : INSTREAM_OBJECT_URI), fe, null);
        } catch (IOException ioe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageIOError(this,
                    (info != null ? info.toString() : INSTREAM_OBJECT_URI), ioe, null);
        }
    }


}
