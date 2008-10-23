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

import java.awt.Color;
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

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
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

    /** Holds the intermediate format state */
    protected IFState state;


    /**
     * Default constructor.
     */
    public AbstractIFPainter() {
    }

    /**
     * Returns the user agent.
     * @return the user agent
     */
    protected abstract FOUserAgent getUserAgent();

    /**
     * Returns the FOP factory.
     * @return the FOP factory.
     */
    protected FopFactory getFopFactory() {
        return getUserAgent().getFactory();
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
        ImageManager manager = getFopFactory().getImageManager();
        ImageSessionContext sessionContext = getUserAgent().getImageSessionContext();
        ImageHandlerRegistry imageHandlerRegistry = getFopFactory().getImageHandlerRegistry();

        //Load and convert the image to a supported format
        RenderingContext context = createRenderingContext();
        Map hints = createDefaultImageProcessingHints(sessionContext);
        org.apache.xmlgraphics.image.loader.Image img = manager.getImage(
                    info, imageHandlerRegistry.getSupportedFlavors(context),
                    hints, sessionContext);

        try {
            drawImage(img, rect, context);
        } catch (IOException ioe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageWritingError(this, ioe);
        }
    }

    /**
     * Creates the default map of processing hints for the image loading framework.
     * @param sessionContext the session context for access to resolution information
     * @return the default processing hints
     */
    protected Map createDefaultImageProcessingHints(ImageSessionContext sessionContext) {
        return ImageUtil.getDefaultHints(sessionContext);
    }

    /**
     * Draws an image using a suitable image handler.
     * @param image the image to be painted (it needs to of a supported image flavor)
     * @param rect the rectangle in which to paint the image
     * @param context a suitable rendering context
     * @throws IOException in case of an I/O error while handling/writing the image
     * @throws ImageException if an error occurs while converting the image to a suitable format
     */
    protected void drawImage(Image image, Rectangle rect,
            RenderingContext context) throws IOException, ImageException {
        drawImage(image, rect, context, false, null);
    }

    /**
     * Draws an image using a suitable image handler.
     * @param image the image to be painted (it needs to of a supported image flavor)
     * @param rect the rectangle in which to paint the image
     * @param context a suitable rendering context
     * @param convert true to run the image through image conversion if that is necessary
     * @param additionalHints additional image processing hints
     * @throws IOException in case of an I/O error while handling/writing the image
     * @throws ImageException if an error occurs while converting the image to a suitable format
     */
    protected void drawImage(Image image, Rectangle rect,
            RenderingContext context, boolean convert, Map additionalHints)
                    throws IOException, ImageException {
        ImageManager manager = getFopFactory().getImageManager();
        ImageHandlerRegistry imageHandlerRegistry = getFopFactory().getImageHandlerRegistry();

        Image effImage;
        if (convert) {
            Map hints = createDefaultImageProcessingHints(getUserAgent().getImageSessionContext());
            if (additionalHints != null) {
                hints.putAll(additionalHints);
            }
            effImage = manager.convertImage(image,
                    imageHandlerRegistry.getSupportedFlavors(context), hints);
        } else {
            effImage = image;
        }

        //First check for a dynamically registered handler
        ImageHandler handler = imageHandlerRegistry.getHandler(context, effImage);
        if (handler == null) {
            throw new UnsupportedOperationException(
                    "No ImageHandler available for image: "
                        + effImage.getInfo() + " (" + effImage.getClass().getName() + ")");
        }

        if (log.isDebugEnabled()) {
            log.debug("Using ImageHandler: " + handler.getClass().getName());
        }

        //TODO foreign attributes
        handler.handleImage(context, effImage, rect);
    }

    /**
     * Default drawing method for handling an image referenced by a URI.
     * @param uri the image's URI
     * @param rect the rectangle in which to paint the image
     */
    protected void drawImageUsingURI(String uri, Rectangle rect) {
        ImageManager manager = getFopFactory().getImageManager();
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
        ImageManager manager = getFopFactory().getImageManager();
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

    /** {@inheritDoc} */
    public void setFont(String family, String style, Integer weight, String variant, Integer size,
            Color color) throws IFException {
        if (family != null) {
            state.setFontFamily(family);
        }
        if (style != null) {
            state.setFontStyle(style);
        }
        if (weight != null) {
            state.setFontWeight(weight.intValue());
        }
        if (variant != null) {
            state.setFontVariant(variant);
        }
        if (size != null) {
            state.setFontSize(size.intValue());
        }
        if (color != null) {
            state.setTextColor(color);
        }
    }

}
