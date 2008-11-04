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

package org.apache.fop.afp;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.IOException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.goca.GraphicsSetLineType;
import org.apache.fop.afp.modca.GraphicsObject;
import org.apache.fop.fonts.FontInfo;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.java2d.AbstractGraphics2D;
import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.java2d.StrokingTextHandler;
import org.apache.xmlgraphics.java2d.TextHandler;
import org.apache.xmlgraphics.ps.ImageEncodingHelper;
import org.apache.xmlgraphics.util.MimeConstants;

/**
 * This is a concrete implementation of <tt>AbstractGraphics2D</tt> (and
 * therefore of <tt>Graphics2D</tt>) which is able to generate GOCA byte
 * codes.
 *
 * @see org.apache.xmlgraphics.java2d.AbstractGraphics2D
 */
public class AFPGraphics2D extends AbstractGraphics2D {

    private static final Log log = LogFactory.getLog(AFPGraphics2D.class);

    private static final int X = 0;

    private static final int Y = 1;

    private static final int X1 = 0;

    private static final int Y1 = 1;

    private static final int X2 = 2;

    private static final int Y2 = 3;


    /** graphics object */
    private GraphicsObject graphicsObj = null;

    /** Fallback text handler */
    protected TextHandler fallbackTextHandler = new StrokingTextHandler(this);

    /** Custom text handler */
    protected TextHandler customTextHandler = null;

    /** AFP resource manager */
    private AFPResourceManager resourceManager = null;

    /** AFP resource info */
    private AFPResourceInfo resourceInfo = null;

    /** Current AFP state */
    private AFPPaintingState state = null;

    /** The AFP FontInfo */
    private FontInfo fontInfo;

    /**
     * Main constructor
     *
     * @param textAsShapes
     *            if true, all text is turned into shapes in the convertion. No
     *            text is output.
     *
     */
    public AFPGraphics2D(boolean textAsShapes) {
        super(textAsShapes);
    }

    /**
     * Copy Constructor
     *
     * @param g2d
     *            a AFPGraphics2D whose properties should be copied
     */
    public AFPGraphics2D(AFPGraphics2D g2d) {
        super(g2d);
        this.graphicsObj = g2d.graphicsObj;
        this.fallbackTextHandler = g2d.fallbackTextHandler;
        this.customTextHandler = g2d.customTextHandler;
        this.resourceManager = g2d.resourceManager;
        this.resourceInfo = g2d.resourceInfo;
        this.state = g2d.state;
    }

    /**
     * Sets the AFP resource manager
     *
     * @param resourceManager the AFP resource manager
     */
    public void setResourceManager(AFPResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    /**
     * Sets the AFP resource info
     *
     * @param resourceInfo the AFP resource info
     */
    public void setResourceInfo(AFPResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

    /**
     * Sets the GraphicContext
     *
     * @param gc
     *            GraphicContext to use
     */
    public void setGraphicContext(GraphicContext gc) {
        this.gc = gc;
    }

    /**
     * Apply the stroke to the AFP graphics object.
     * This takes the java stroke and outputs the appropriate settings
     * to the AFP graphics object so that the stroke attributes are handled.
     *
     * @param stroke the java stroke
     */
    protected void applyStroke(Stroke stroke) {
        if (stroke instanceof BasicStroke) {
            BasicStroke basicStroke = (BasicStroke) stroke;
            float lineWidth = basicStroke.getLineWidth();
            if (state.setLineWidth(lineWidth)) {
                getGraphicsObject().setLineWidth(Math.round(lineWidth * 2));
            }
            // note: this is an approximation at best!
            float[] dashArray = basicStroke.getDashArray();
            if (state.setDashArray(dashArray)) {
                byte type = GraphicsSetLineType.DEFAULT; // normally SOLID
                if (dashArray != null) {
                    type = GraphicsSetLineType.DOTTED; // default to plain DOTTED if dashed line
                    // float offset = basicStroke.getDashPhase();
                    if (dashArray.length == 2) {
                        if (dashArray[0] < dashArray[1]) {
                            type = GraphicsSetLineType.SHORT_DASHED;
                        } else if (dashArray[0] > dashArray[1]) {
                            type = GraphicsSetLineType.LONG_DASHED;
                        }
                    } else if (dashArray.length == 4) {
                        if (dashArray[0] > dashArray[1]
                         && dashArray[2] < dashArray[3]) {
                            type = GraphicsSetLineType.DASH_DOT;
                        } else if (dashArray[0] < dashArray[1]
                                && dashArray[2] < dashArray[3]) {
                            type = GraphicsSetLineType.DOUBLE_DOTTED;
                        }
                    } else if (dashArray.length == 6) {
                        if (dashArray[0] > dashArray[1]
                         && dashArray[2] < dashArray[3]
                         && dashArray[4] < dashArray[5]) {
                            type = GraphicsSetLineType.DASH_DOUBLE_DOTTED;
                        }
                    }
                }
                getGraphicsObject().setLineType(type);
            }
        } else {
            log.warn("Unsupported Stroke: " + stroke.getClass().getName());
        }
    }

    /**
     * Handle the Batik drawing event
     *
     * @param shape
     *            the shape to draw
     * @param fill
     *            true if the shape is to be drawn filled
     */
    private void doDrawing(Shape shape, boolean fill) {
        getGraphicsObject();
        if (!fill) {
            graphicsObj.newSegment();
        }

        Color color = getColor();
        if (state.setColor(color)) {
            graphicsObj.setColor(color);
        }

        Stroke stroke = getStroke();
        applyStroke(stroke);

        if (fill) {
            graphicsObj.beginArea();
        }
        AffineTransform trans = super.getTransform();

        PathIterator iter = shape.getPathIterator(trans);
        double[] dstPts = new double[6];
        int[] coords = null;
        if (shape instanceof Line2D) {
            iter.currentSegment(dstPts);
            coords = new int[4];
            coords[X1] = (int) Math.round(dstPts[X]);
            coords[Y1] = (int) Math.round(dstPts[Y]);
            iter.next();
            iter.currentSegment(dstPts);
            coords[X2] = (int) Math.round(dstPts[X]);
            coords[Y2] = (int) Math.round(dstPts[Y]);
            graphicsObj.addLine(coords);
        } else if (shape instanceof Rectangle2D) {
            iter.currentSegment(dstPts);
            coords = new int[4];
            coords[X2] = (int) Math.round(dstPts[X]);
            coords[Y2] = (int) Math.round(dstPts[Y]);
            iter.next();
            iter.next();
            iter.currentSegment(dstPts);
            coords[X1] = (int) Math.round(dstPts[X]);
            coords[Y1] = (int) Math.round(dstPts[Y]);
            graphicsObj.addBox(coords);
        } else if (shape instanceof Ellipse2D) {
            Ellipse2D elip = (Ellipse2D) shape;
            double scale = trans.getScaleX();
            double radiusWidth = elip.getWidth() / 2;
            double radiusHeight = elip.getHeight() / 2;
            graphicsObj.setArcParams(
                    (int)Math.round(radiusWidth * scale),
                    (int)Math.round(radiusHeight * scale),
                    0,
                    0
            );
            double[] srcPts = new double[] {elip.getCenterX(), elip.getCenterY()};
            trans.transform(srcPts, 0, dstPts, 0, 1);
            final int mh = 1;
            final int mhr = 0;
            graphicsObj.addFullArc(
                    (int)Math.round(dstPts[X]),
                    (int)Math.round(dstPts[Y]),
                    mh,
                    mhr
            );
        } else {
            // graphics segment opening coordinates (x,y)
            // current position coordinates (x,y)
            for (int[] openingCoords = new int[2], currCoords = new int[2];
                !iter.isDone(); iter.next()) {
                // round the coordinate values and combine with current position
                // coordinates
                int type = iter.currentSegment(dstPts);
                if (type == PathIterator.SEG_MOVETO) {
                    openingCoords[X] = currCoords[X] = (int)Math.round(dstPts[X]);
                    openingCoords[Y] = currCoords[Y] = (int)Math.round(dstPts[Y]);
                } else {
                    int numCoords;
                    if (type == PathIterator.SEG_LINETO) {
                        numCoords = 2;
                    } else if (type == PathIterator.SEG_QUADTO) {
                        numCoords = 4;
                    } else if (type == PathIterator.SEG_CUBICTO) {
                        numCoords = 6;
                    } else {
                        // close of the graphics segment
                        if (type == PathIterator.SEG_CLOSE) {
                            coords = new int[] {
                                    coords[coords.length - 2], //prev X
                                    coords[coords.length - 1], //prev Y
                                    openingCoords[X],
                                    openingCoords[Y]
                            };
                            graphicsObj.addLine(coords);
                        } else {
                            log.debug("Unrecognised path iterator type: "
                                    + type);
                        }
                        continue;
                    }
                    // combine current position coordinates with new graphics
                    // segment coordinates
                    coords = new int[numCoords + 2];
                    coords[X] = currCoords[X];
                    coords[Y] = currCoords[Y];
                    for (int i = 0; i < numCoords; i++) {
                        coords[i + 2] = (int) Math.round(dstPts[i]);
                    }
                    if (type == PathIterator.SEG_LINETO) {
                        graphicsObj.addLine(coords);
                    } else if (type == PathIterator.SEG_QUADTO
                            || type == PathIterator.SEG_CUBICTO) {
                        graphicsObj.addFillet(coords);
                    }
                    // update current position coordinates
                    currCoords[X] = coords[coords.length - 2];
                    currCoords[Y] = coords[coords.length - 1];
                }
            }
        }
        if (fill) {
            graphicsObj.endArea();
        }
    }

    /** {@inheritDoc} */
    public void draw(Shape shape) {
//        log.debug("draw() shape=" + shape);
        doDrawing(shape, false);
    }

    /** {@inheritDoc} */
    public void fill(Shape shape) {
//        log.debug("fill() shape=" + shape);
        doDrawing(shape, true);
    }

    /**
     * Central handler for IOExceptions for this class.
     *
     * @param ioe
     *            IOException to handle
     */
    public void handleIOException(IOException ioe) {
        // TODO Surely, there's a better way to do this.
        log.error(ioe.getMessage());
        ioe.printStackTrace();
    }

    /** {@inheritDoc} */
    public void drawString(String str, float x, float y) {
        try {
            if (customTextHandler != null && !textAsShapes) {
                customTextHandler.drawString(str, x, y);
            } else {
                fallbackTextHandler.drawString(str, x, y);
            }
        } catch (IOException ioe) {
            handleIOException(ioe);
        }
    }

    /** {@inheritDoc} */
    public GraphicsConfiguration getDeviceConfiguration() {
        return new AFPGraphicsConfiguration();
    }

    /** {@inheritDoc} */
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        log.debug("copyArea() NYI: ");
    }

    /** {@inheritDoc} */
    public Graphics create() {
        return new AFPGraphics2D(this);
    }

    /** {@inheritDoc} */
    public void dispose() {
        this.graphicsObj = null;
    }

    /** {@inheritDoc} */
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return drawImage(img, x, y, img.getWidth(observer), img.getHeight(observer), observer);
    }

    private BufferedImage buildBufferedImage(Dimension size) {
        return new BufferedImage(size.width, size.height,
                                 BufferedImage.TYPE_INT_ARGB);
    }

    private AFPImageObjectInfo getImageObjectInfo(
            RenderedImage img, int x, int y, int width, int height) throws IOException {
        ImageInfo imageInfo = new ImageInfo(null, "image/unknown");
        ImageSize size = new ImageSize(img.getWidth(), img.getHeight(), 72);
        imageInfo.setSize(size);

        ImageRendered imageRendered = new ImageRendered(imageInfo, img, null);
        RenderedImage renderedImage = imageRendered.getRenderedImage();

        // create image object info
        AFPImageObjectInfo imageObjectInfo = new AFPImageObjectInfo();

        imageObjectInfo.setMimeType(MimeConstants.MIME_AFP_IOCA_FS45);

        imageObjectInfo.setBitsPerPixel(state.getBitsPerPixel());

        imageObjectInfo.setResourceInfo(resourceInfo);

        int dataHeight = renderedImage.getHeight();
        imageObjectInfo.setDataHeight(dataHeight);

        int dataWidth = renderedImage.getWidth();
        imageObjectInfo.setDataWidth(dataWidth);

        boolean colorImages = state.isColorImages();
        imageObjectInfo.setColor(colorImages);

        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        ImageEncodingHelper.encodeRenderedImageAsRGB(renderedImage, boas);
        byte[] imageData = boas.toByteArray();

        // convert to grayscale
        if (!colorImages) {
            boas.reset();
            int bitsPerPixel = state.getBitsPerPixel();
            imageObjectInfo.setBitsPerPixel(bitsPerPixel);
            ImageEncodingHelper.encodeRGBAsGrayScale(
                  imageData, dataWidth, dataHeight, bitsPerPixel, boas);
            imageData = boas.toByteArray();
        }
        imageObjectInfo.setData(imageData);

        if (imageInfo != null) {
            imageObjectInfo.setUri(imageInfo.getOriginalURI());
        }

        // create object area info
        AFPObjectAreaInfo objectAreaInfo = new AFPObjectAreaInfo();

        AffineTransform at = gc.getTransform();
        float[] srcPts = new float[] {x, y};
        float[] dstPts = new float[srcPts.length];
        at.transform(srcPts, 0, dstPts, 0, 1);
        objectAreaInfo.setX(Math.round(dstPts[X]));
        objectAreaInfo.setY(Math.round(dstPts[Y]));

        AFPUnitConverter unitConv = state.getUnitConverter();

        int w = Math.round(unitConv.pt2units(width));
        objectAreaInfo.setWidth(w);

        int h = Math.round(unitConv.pt2units(height));
        objectAreaInfo.setHeight(h);

        int resolution = state.getResolution();
        objectAreaInfo.setWidthRes(resolution);
        objectAreaInfo.setHeightRes(resolution);

        imageObjectInfo.setObjectAreaInfo(objectAreaInfo);

        return imageObjectInfo;
    }

    /** {@inheritDoc} */
    public boolean drawImage(Image img, int x, int y, int width, int height,
            ImageObserver observer) {

        // draw with AWT Graphics2D
        Dimension size = new Dimension(width, height);
        BufferedImage bufferedImage = buildBufferedImage(size);

        java.awt.Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setComposite(AlphaComposite.SrcOver);

        Color color = new Color(1, 1, 1, 0);
        g2d.setBackground(color);
        g2d.setPaint(color);

        g2d.fillRect(0, 0, width, height);

        int imageWidth = bufferedImage.getWidth();
        int imageHeight = bufferedImage.getHeight();
        Rectangle clipRect = new Rectangle(0, 0, imageWidth, imageHeight);
        g2d.clip(clipRect);

        g2d.setComposite(gc.getComposite());

        boolean drawn = g2d.drawImage(img, 0, 0, imageWidth, imageHeight, observer);
        g2d.dispose(); //drawn so dispose immediately to free system resource

        if (drawn) {
            try {
                // get image object info
                AFPImageObjectInfo imageObjectInfo = getImageObjectInfo(bufferedImage, x, y, width, height);

                // create image resource
                resourceManager.createObject(imageObjectInfo);
                return true;
            } catch (IOException ioe) {
                handleIOException(ioe);
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        log.debug("drawRenderableImage() NYI: img=" + img + ", xform=" + xform);
    }

    /** {@inheritDoc} */
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        log.debug("drawRenderedImage() NYI: img=" + img + ", xform=" + xform);
    }

    /** {@inheritDoc} */
    public FontMetrics getFontMetrics(Font f) {
        log.debug("getFontMetrics() NYI: f=" + f);
        return null;
    }

    /** {@inheritDoc} */
    public void setXORMode(Color col) {
        log.debug("setXORMode() NYI: col=" + col);
    }

    /**
     * Sets a custom TextHandler implementation that is responsible for painting
     * text. The default TextHandler paints all text as shapes. A custom
     * implementation can implement text painting using text painting operators.
     *
     * @param handler
     *            the custom TextHandler implementation
     */
    public void setCustomTextHandler(TextHandler handler) {
        this.customTextHandler = handler;
    }

    /**
     * Returns the GOCA graphics object
     *
     * @return the GOCA graphics object
     */
    public GraphicsObject getGraphicsObject() {
        return this.graphicsObj;
    }

    /**
     * Sets the GOCA graphics object
     *
     * @param obj the GOCA graphics object
     */
    public void setGraphicsObject(GraphicsObject obj) {
        this.graphicsObj = obj;
    }

    /**
     * Sets the AFP painting state
     *
     * @param state the AFP painting state
     */
    public void setPaintingState(AFPPaintingState state) {
        this.state = state;
    }

    /**
     * Returns the AFP state
     *
     * @return the AFP state
     */
    public AFPPaintingState getPaintingState() {
        return this.state;
    }

    /**
     * Sets the FontInfo
     *
     * @param the FontInfo
     */
    public void setFontInfo(FontInfo fontInfo) {
        this.fontInfo = fontInfo;
    }

    /**
     * Returns the FontInfo
     *
     * @return the FontInfo
     */
    public FontInfo getFontInfo() {
        return this.fontInfo;
    }
}
