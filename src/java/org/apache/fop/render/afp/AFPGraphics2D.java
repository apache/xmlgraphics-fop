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

package org.apache.fop.render.afp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.IOException;

import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.goca.GraphicsSetLineType;
import org.apache.fop.render.afp.modca.GraphicsObject;
import org.apache.fop.render.afp.modca.IncludeObject;
import org.apache.xmlgraphics.java2d.AbstractGraphics2D;
import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.java2d.StrokingTextHandler;
import org.apache.xmlgraphics.java2d.TextHandler;
import org.apache.xmlgraphics.ps.ImageEncodingHelper;

/**
 * This is a concrete implementation of <tt>AbstractGraphics2D</tt> (and
 * therefore of <tt>Graphics2D</tt>) which is able to generate GOCA byte
 * codes.
 * 
 * @see org.apache.xmlgraphics.java2d.AbstractGraphics2D
 */
public class AFPGraphics2D extends AbstractGraphics2D {

    private static final Log log = LogFactory.getLog(AFPGraphics2D.class);

    private GraphicsObject graphicsObj = null;

    /** Fallback text handler */
    protected TextHandler fallbackTextHandler = new StrokingTextHandler(this);

    /** Custom text handler */
    protected TextHandler customTextHandler = null;

    /** AFP info */
    private AFPInfo afpInfo = null;

    /** Current AFP state */
    private AFPState afpState = null;

    /** The SVG document URI */
    private String documentURI = null;

    /**
     * @param textAsShapes
     *            if true, all text is turned into shapes in the convertion. No
     *            text is output.
     * 
     */
    public AFPGraphics2D(boolean textAsShapes) {
        super(textAsShapes);
    }

    /**
     * Creates a new AFPGraphics2D from an existing instance.
     * 
     * @param g
     *            the AFPGraphics2D whose properties should be copied
     */
    public AFPGraphics2D(AFPGraphics2D g) {
        super(g);
        this.graphicsObj = g.graphicsObj;
        this.fallbackTextHandler = g.fallbackTextHandler;
        this.customTextHandler = g.customTextHandler;
        this.afpInfo = g.afpInfo;
        this.afpState = g.afpState;
    }

    /**
     * Sets the AFPInfo
     * 
     * @param info
     *            the AFP Info to use
     */
    public void setAFPInfo(AFPInfo info) {
        this.afpInfo = info;
        this.afpState = info.getState();
    }

    /**
     * Gets the AFPInfo
     * 
     * @return the AFPInfo
     */
    public AFPInfo getAFPInfo() {
        return this.afpInfo;
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
            if (afpState.setLineWidth(lineWidth)) {
                getGraphicsObject().setLineWidth(Math.round(lineWidth * 2));
            }
            // note: this is an approximation at best!
            float[] dashArray = basicStroke.getDashArray();
            if (afpState.setDashArray(dashArray)) {
                byte type = GraphicsSetLineType.DEFAULT; // normally SOLID
                if (dashArray != null) {
                    type = GraphicsSetLineType.DOTTED; // default to DOTTED
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
        Color col = getColor();
        if (afpState.setColor(col)) {
            graphicsObj.setColor(col);
        }
        
        applyStroke(getStroke());

        if (fill) {
            graphicsObj.beginArea();
        }
        AffineTransform trans = super.getTransform();
        PathIterator iter = shape.getPathIterator(trans);
        double[] vals = new double[6];
        int[] coords = null;
        if (shape instanceof GeneralPath || shape instanceof ExtendedGeneralPath) {
            // graphics segment opening coordinates (x,y)
            int[] openingCoords = new int[2];
            // current position coordinates (x,y)
            int[] currCoords = new int[2];
            NEXT_ITER: while (!iter.isDone()) {
                // round the coordinate values and combine with current position
                // coordinates
                int type = iter.currentSegment(vals);
                if (type == PathIterator.SEG_MOVETO) {
                    log.debug("SEG_MOVETO");
                    openingCoords[0] = currCoords[0] = (int)Math.round(vals[0]);
                    openingCoords[1] = currCoords[1] = (int)Math.round(vals[1]);
                } else {
                    int numCoords;
                    if (type == PathIterator.SEG_LINETO) {
                        log.debug("SEG_LINETO");
                        numCoords = 2;
                    } else if (type == PathIterator.SEG_QUADTO) {
                        log.debug("SEG_QUADTO");
                        numCoords = 4;
                    } else if (type == PathIterator.SEG_CUBICTO) {
                        log.debug("SEG_CUBICTO");
                        numCoords = 6;
                    } else {
                        // close of the graphics segment
                        if (type == PathIterator.SEG_CLOSE) {
                            log.debug("SEG_CLOSE");
                            coords = new int[] {
                                    coords[coords.length - 2],
                                    coords[coords.length - 1],
                                    openingCoords[0],
                                    openingCoords[1]
                            };
                            graphicsObj.addLine(coords);
                        } else {
                            log.debug("Unrecognised path iterator type: "
                                    + type);
                        }
                        iter.next();
                        continue NEXT_ITER;
                    }
                    // combine current position coordinates with new graphics
                    // segment coordinates
                    coords = new int[numCoords + 2];
                    coords[0] = currCoords[0];
                    coords[1] = currCoords[1];
                    for (int i = 0; i < numCoords; i++) {
                        coords[i + 2] = (int) Math.round(vals[i]);
                    }
                    if (type == PathIterator.SEG_LINETO) {
                        graphicsObj.addLine(coords);
                    } else if (type == PathIterator.SEG_QUADTO
                            || type == PathIterator.SEG_CUBICTO) {
                        graphicsObj.addFillet(coords);
                    }
                    // update current position coordinates
                    currCoords[0] = coords[coords.length - 2];
                    currCoords[1] = coords[coords.length - 1];
                }
                iter.next();
            }
        } else if (shape instanceof Line2D) {
            iter.currentSegment(vals);
            coords = new int[4];
            coords[0] = (int) Math.round(vals[0]);
            coords[1] = (int) Math.round(vals[1]);
            iter.next();
            iter.currentSegment(vals);
            coords[2] = (int) Math.round(vals[0]);
            coords[3] = (int) Math.round(vals[1]);
            graphicsObj.addLine(coords);
        } else if (shape instanceof Rectangle2D) {
            iter.currentSegment(vals);
            coords = new int[4];
            coords[2] = (int) Math.round(vals[0]);
            coords[3] = (int) Math.round(vals[1]);
            iter.next();
            iter.next();
            iter.currentSegment(vals);
            coords[0] = (int) Math.round(vals[0]);
            coords[1] = (int) Math.round(vals[1]);
            graphicsObj.addBox(coords);
        } else if (shape instanceof Ellipse2D) {
            Ellipse2D elip = (Ellipse2D) shape;
            final double factor = afpInfo.getResolution() / 100f;
            graphicsObj.setArcParams(
                    (int)Math.round(elip.getWidth() * factor),
                    (int)Math.round(elip.getHeight() * factor),
                    0,
                    0
            );
            trans.transform(
                    new double[] {elip.getCenterX(), elip.getCenterY()}, 0,
                    vals, 0, 1);
            final int mh = 1;
            final int mhr = 0;
            graphicsObj.addFullArc(
                    (int)Math.round(vals[0]),
                    (int)Math.round(vals[1]),
                    mh,
                    mhr
            );
        } else {
            log.error("Unrecognised shape: " + shape);
        }
        if (fill) {
            graphicsObj.endArea();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void draw(Shape shape) {
        log.debug("draw() shape=" + shape);
        doDrawing(shape, false);
    }

    /**
     * {@inheritDoc}
     */
    public void fill(Shape shape) {
        log.debug("fill() shape=" + shape);
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

    /**
     * {@inheritDoc}
     */
    public void drawString(String s, float x, float y) {
        try {
            if (customTextHandler != null && !textAsShapes) {
                customTextHandler.drawString(s, x, y);
            } else {
                fallbackTextHandler.drawString(s, x, y);
            }
        } catch (IOException ioe) {
            handleIOException(ioe);
        }
    }

    /**
     * {@inheritDoc}
     */
    public GraphicsConfiguration getDeviceConfiguration() {
        return new AFPGraphicsConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        log.debug("copyArea() NYI: ");
    }

    /**
     * {@inheritDoc}
     */
    public Graphics create() {
        return new AFPGraphics2D(this);
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        this.graphicsObj = null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return drawImage(img, x, y, img.getWidth(observer), img.getHeight(observer), observer);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean drawImage(Image img, int x, int y, int width, int height,
            ImageObserver observer) {
        //TODO: this might be achieved by creating a new IOCA image (ImageObject)
        // and placing it in an Overlay - but then stacking order would not be preserved.
        log.debug("drawImage(): NYI img=" + img + ", x=" + x + ", y=" + y
                + ", width=" + width + ", height=" + height + ", obs=" + observer);
        return false;
//        log.debug("drawImage() img=" + img + ", x=" + x + ", y=" + y
//                + ", width=" + width + ", height=" + height + ", obs=" + observer);
//        if (img instanceof BufferedImage) {
//            try {
//                BufferedImage bi = (BufferedImage)img;
//                ByteArrayOutputStream baout = new ByteArrayOutputStream();
//                
//                // Serialize image
//                ImageEncodingHelper.encodeRenderedImageAsRGB(bi, baout);
//                
//                int res = afpInfo.getResolution();
//                ImageObjectParameters params = new ImageObjectParameters(
//                        //TODO: provide a real url
//                        img.toString(), x, y,
//                        width, height, res, res, baout.toByteArray(),
//                        img.getWidth(observer), img.getHeight(observer), 
//                        afpInfo.isColorSupported(), afpInfo.getBitsPerPixel());
//                
//                afpInfo.getAFPDataStream().createImageObject(params);
//
//                // Generate image
//            } catch (IOException ioe) {
//                log.error("Error while serializing bitmap: " + ioe.getMessage(),
//                    ioe);
//                return false;
//            }
//            return true;
//        } else {
//            log.debug("drawImage() image type not supported: " + img);
//        }
//        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        log.debug("drawRenderableImage() NYI: img=" + img + ", xform=" + xform);
    }

    /**
     * {@inheritDoc}
     */
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        log.debug("drawRenderedImage() NYI: img=" + img + ", xform=" + xform);
    }

    /**
     * {@inheritDoc}
     */
    public FontMetrics getFontMetrics(Font f) {
        log.debug("getFontMetrics() NYI: f=" + f);
        return null;
    }

    /**
     * {@inheritDoc}
     */
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

//    /**
//     * Sets the SVG document URI
//     * @param documentURI the SVG document URI
//     */
//    public void setDocumentURI(String documentURI) {
//        this.documentURI = documentURI;
//    }

    /**
     * @return the GOCA graphics object
     */
    protected GraphicsObject getGraphicsObject() {
//        if (this.graphicsObj == null) {
////          DataObjectParameters params = new DataObjectParameters(
////          ((AbstractDocument)doc).getDocumentURI(),
////          afpInfo.getX(), afpInfo.getY(),
////          afpInfo.getWidth(), afpInfo.getHeight(), res, res);
////  
////  afpInfo.getAFPDataStream().createGraphicsObject(params);
//
//            int x = (int)Math.round((afpInfo.getX() * 25.4f) / 1000);
//            int y = (int)Math.round((afpInfo.getY() * 25.4f) / 1000);
//            int res = afpInfo.getResolution();
//            int width = (int)Math.round((afpInfo.getWidth() * res) / 72000f);
//            int height = (int)Math.round((afpInfo.getHeight() * res) / 72000f);
//            DataObjectParameters params = new DataObjectParameters(
//                    this.documentURI, x, y, width, height, res, res);
//            IncludeObject includeObj = afpInfo.getAFPDataStream().createGraphicsObject(params);
//            this.graphicsObj = (GraphicsObject)includeObj.getReferencedObject();
//        }
        return this.graphicsObj;
    }

    protected void setGraphicsObject(GraphicsObject graphicsObj) {
        this.graphicsObj = graphicsObj;
    }
}