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
import java.awt.Point;
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
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;

import org.apache.fop.ResourceEventProducer;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.Constants;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.ImageHandlerRegistry;
import org.apache.fop.render.ImageHandlerUtil;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;

/**
 * Abstract base class for IFPainter implementations.
 */
public abstract class AbstractIFPainter<T extends IFDocumentHandler> implements IFPainter {

    /** logging instance */
    private static Log log = LogFactory.getLog(AbstractIFPainter.class);

    /** non-URI that can be used in feedback messages that an image is an instream-object */
    protected static final String INSTREAM_OBJECT_URI = "(instream-object)";

    /** Holds the intermediate format state */
    protected IFState state;

    private final T documentHandler;

    /**
     * Default constructor.
     */
    public AbstractIFPainter(T documentHandler) {
        this.documentHandler = documentHandler;
    }

    protected String getFontKey(FontTriplet triplet) throws IFException {
        String key = getFontInfo().getInternalFontKey(triplet);
        if (key == null) {
            throw new IFException("The font triplet is not available: \"" + triplet + "\" "
                    + "for the MIME type: \"" + documentHandler.getMimeType() + "\"");
        }
        return key;
    }

    /**
     * Returns the intermediate format context object.
     * @return the context object
     */
    protected IFContext getContext() {
        return documentHandler.getContext();
    }

    protected FontInfo getFontInfo() {
        return documentHandler.getFontInfo();
    }

    protected T getDocumentHandler() {
        return documentHandler;
    }

    /**
     * Returns the user agent.
     * @return the user agent
     */
    protected FOUserAgent getUserAgent() {
        return getContext().getUserAgent();
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
    public void startGroup(AffineTransform[] transforms, String layer) throws IFException {
        startGroup(combine(transforms), layer);
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
        ImageManager manager = getUserAgent().getImageManager();
        ImageSessionContext sessionContext = getUserAgent().getImageSessionContext();
        ImageHandlerRegistry imageHandlerRegistry = getUserAgent().getImageHandlerRegistry();

        //Load and convert the image to a supported format
        RenderingContext context = createRenderingContext();
        Map hints = createDefaultImageProcessingHints(sessionContext);
        context.putHints(hints);

        ImageFlavor[] flavors = imageHandlerRegistry.getSupportedFlavors(context);
        info.getCustomObjects().put("warningincustomobject", true);
        org.apache.xmlgraphics.image.loader.Image img = manager.getImage(
                    info, flavors,
                    hints, sessionContext);

        if (info.getCustomObjects().get("warning") != null) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageWarning(this, (String)info.getCustomObjects().get("warning"));
        }

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
        Map hints = ImageUtil.getDefaultHints(sessionContext);

        //Transfer common foreign attributes to hints
        Object conversionMode = getContext().getForeignAttribute(ImageHandlerUtil.CONVERSION_MODE);
        if (conversionMode != null) {
            hints.put(ImageHandlerUtil.CONVERSION_MODE, conversionMode);
        }

        return hints;
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
        ImageManager manager = getUserAgent().getImageManager();
        ImageHandlerRegistry imageHandlerRegistry = getUserAgent().getImageHandlerRegistry();

        Image effImage;
        context.putHints(additionalHints);
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

        if (log.isTraceEnabled()) {
            log.trace("Using ImageHandler: " + handler.getClass().getName());
        }
        context.putHint("fontinfo", getFontInfo());
        handler.handleImage(context, effImage, rect);
    }

    /**
     * Returns an ImageInfo instance for the given URI. If there's an error, null is returned.
     * The caller can assume that any exceptions have already been handled properly. The caller
     * simply skips painting anything in this case.
     * @param uri the URI identifying the image
     * @return the ImageInfo instance or null if there has been an error.
     */
    protected ImageInfo getImageInfo(String uri) {
        ImageManager manager = getUserAgent().getImageManager();
        try {
            ImageSessionContext sessionContext = getUserAgent().getImageSessionContext();
            return manager.getImageInfo(uri, sessionContext);
        } catch (ImageException ie) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageError(this, uri, ie, null);
        } catch (FileNotFoundException fe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageNotFound(this, uri, fe, null);
        } catch (IOException ioe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageIOError(this, uri, ioe, null);
        }
        return null;
    }

    /**
     * Default drawing method for handling an image referenced by a URI.
     * @param uri the image's URI
     * @param rect the rectangle in which to paint the image
     */
    protected void drawImageUsingURI(String uri, Rectangle rect) {
        ImageManager manager = getUserAgent().getImageManager();
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
        ImageManager manager = getUserAgent().getImageManager();
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
    public void drawBorderRect(Rectangle rect, BorderProps top, BorderProps bottom,
            BorderProps left, BorderProps right, Color innerBackgroundColor) throws IFException {
        if (top != null) {
            Rectangle b = new Rectangle(
                    rect.x, rect.y,
                    rect.width, top.width);
            fillRect(b, top.color);
        }
        if (right != null) {
            Rectangle b = new Rectangle(
                    rect.x + rect.width - right.width, rect.y,
                    right.width, rect.height);
            fillRect(b, right.color);
        }
        if (bottom != null) {
            Rectangle b = new Rectangle(
                    rect.x, rect.y + rect.height - bottom.width,
                    rect.width, bottom.width);
            fillRect(b, bottom.color);
        }
        if (left != null) {
            Rectangle b = new Rectangle(
                    rect.x, rect.y,
                    left.width, rect.height);
            fillRect(b, left.color);
        }
    }

    /**
     * Indicates whether the given border segments (if present) have only solid borders, i.e.
     * could be painted in a simplified fashion keeping the output file smaller.
     * @param top the border segment on the top edge
     * @param bottom the border segment on the bottom edge
     * @param left the border segment on the left edge
     * @param right the border segment on the right edge
     * @return true if any border segment has a non-solid border style
     */
    protected boolean hasOnlySolidBorders(BorderProps top, BorderProps bottom,
            BorderProps left, BorderProps right) {
        if (top != null && top.style != Constants.EN_SOLID) {
            return false;
        }
        if (bottom != null && bottom.style != Constants.EN_SOLID) {
            return false;
        }
        if (left != null && left.style != Constants.EN_SOLID) {
            return false;
        }
        if (right != null && right.style != Constants.EN_SOLID) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    public void drawLine(Point start, Point end, int width, Color color, RuleStyle style)
            throws IFException {
        Rectangle rect = getLineBoundingBox(start, end, width);
        fillRect(rect, color);
    }

    /**
     * Calculates the bounding box for a line. Currently, only horizontal and vertical lines
     * are needed and supported.
     * @param start the starting point of the line (coordinates in mpt)
     * @param end the ending point of the line (coordinates in mpt)
     * @param width the line width (in mpt)
     * @return the bounding box (coordinates in mpt)
     */
    protected Rectangle getLineBoundingBox(Point start, Point end, int width) {
        if (start.y == end.y) {
            int topy = start.y - width / 2;
            return new Rectangle(
                    start.x, topy,
                    end.x - start.x, width);
        } else if (start.x == end.y) {
            int leftx = start.x - width / 2;
            return new Rectangle(
                    leftx, start.x,
                    width, end.y - start.y);
        } else {
            throw new IllegalArgumentException(
                    "Only horizontal or vertical lines are supported at the moment.");
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

    /**
     * Converts a transformation matrix from millipoints to points.
     * @param transform the transformation matrix (in millipoints)
     * @return the converted transformation matrix (in points)
     */
    public static AffineTransform toPoints(AffineTransform transform) {
        final double[] matrix = new double[6];
        transform.getMatrix(matrix);
        //Convert from millipoints to points
        matrix[4] /= 1000;
        matrix[5] /= 1000;
        return new AffineTransform(matrix);
    }

    /** {@inheritDoc} */
    public boolean isBackgroundRequired(BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd) {
        return true;
    }

}
