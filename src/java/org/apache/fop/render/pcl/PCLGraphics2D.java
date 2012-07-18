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

package org.apache.fop.render.pcl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.IOException;
import java.text.AttributedCharacterIterator;

import org.apache.xmlgraphics.java2d.AbstractGraphics2D;
import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.java2d.GraphicsConfigurationWithTransparency;
import org.apache.xmlgraphics.util.UnitConv;

/**
 * Graphics2D implementation implementing PCL and HP GL/2.
 * Note: This class cannot be used stand-alone to create full PCL documents.
 */
public class PCLGraphics2D extends AbstractGraphics2D {

    /** The PCL generator */
    protected PCLGenerator gen;

    private final boolean failOnUnsupportedFeature = true;
    private boolean clippingDisabled = false;

    /**
     * Create a new PCLGraphics2D.
     * @param gen the PCL Generator to paint with
     */
    public PCLGraphics2D(PCLGenerator gen) {
        super(true);
        this.gen = gen;
    }

    /**
     * Copy constructor
     * @param g parent PCLGraphics2D
     */
    public PCLGraphics2D(PCLGraphics2D g) {
        super(true);
        this.gen = g.gen;
    }

    /** {@inheritDoc} */
    public Graphics create() {
        PCLGraphics2D copy = new PCLGraphics2D(this);
        copy.setGraphicContext((GraphicContext)getGraphicContext().clone());
        return copy;
    }

    /** {@inheritDoc} */
    public void dispose() {
        this.gen = null;
    }

    /**
     * Sets the GraphicContext
     * @param c GraphicContext to use
     */
    public void setGraphicContext(GraphicContext c) {
        this.gc = c;
    }

    /**
     * Allows to disable all clipping operations.
     * @param value true if clipping should be disabled.
     */
    public void setClippingDisabled(boolean value) {
        this.clippingDisabled = value;
    }

    /**
     * Central handler for IOExceptions for this class.
     * @param ioe IOException to handle
     */
    public void handleIOException(IOException ioe) {
        //TODO Surely, there's a better way to do this.
        ioe.printStackTrace();
    }

    /**
     * Raises an UnsupportedOperationException if this instance is configured to do so and an
     * unsupported feature has been requested. Clients can make use of this to fall back to
     * a more compatible way of painting a PCL graphic.
     * @param msg the error message to be displayed
     */
    protected void handleUnsupportedFeature(String msg) {
        if (this.failOnUnsupportedFeature) {
            throw new UnsupportedOperationException(msg);
        }
    }

    /** {@inheritDoc} */
    public GraphicsConfiguration getDeviceConfiguration() {
        return new GraphicsConfigurationWithTransparency();
    }

    /**
     * Applies a new Stroke object.
     * @param stroke Stroke object to use
     * @throws IOException In case of an I/O problem
     */
    protected void applyStroke(Stroke stroke) throws IOException {
        if (stroke instanceof BasicStroke) {
            BasicStroke bs = (BasicStroke)stroke;

            float[] da = bs.getDashArray();
            if (da != null) {

                gen.writeText("UL1,");
                int len = Math.min(20, da.length);
                float patternLen = 0.0f;
                for (int idx = 0; idx < len; idx++) {
                    patternLen += da[idx];
                }
                if (len == 1) {
                    patternLen *= 2;
                }
                for (int idx = 0; idx < len; idx++) {
                    float perc = da[idx] * 100 / patternLen;
                    gen.writeText(gen.formatDouble2(perc));
                    if (idx < da.length - 1) {
                        gen.writeText(",");
                    }
                }
                if (len == 1) {
                    gen.writeText("," + gen.formatDouble2(da[0] * 100 / patternLen ));

                }
                gen.writeText(";");
                /* TODO Dash phase NYI
                float offset = bs.getDashPhase();
                gen.writeln(gen.formatDouble4(offset) + " setdash");
                */
                Point2D ptLen = new Point2D.Double(patternLen, 0);
                //interpret as absolute length
                getTransform().deltaTransform(ptLen, ptLen);
                double transLen = UnitConv.pt2mm(ptLen.distance(0, 0));
                gen.writeText("LT1," + gen.formatDouble4(transLen) + ",1;");
            } else {
                gen.writeText("LT;");
            }

            gen.writeText("LA1"); //line cap
            int ec = bs.getEndCap();
            switch (ec) {
            case BasicStroke.CAP_BUTT:
                gen.writeText(",1");
                break;
            case BasicStroke.CAP_ROUND:
                gen.writeText(",4");
                break;
            case BasicStroke.CAP_SQUARE:
                gen.writeText(",2");
                break;
            default: System.err.println("Unsupported line cap: " + ec);
            }

            gen.writeText(",2"); //line join
            int lj = bs.getLineJoin();
            switch (lj) {
            case BasicStroke.JOIN_MITER:
                gen.writeText(",1");
                break;
            case BasicStroke.JOIN_ROUND:
                gen.writeText(",4");
                break;
            case BasicStroke.JOIN_BEVEL:
                gen.writeText(",5");
                break;
            default: System.err.println("Unsupported line join: " + lj);
            }

            float ml = bs.getMiterLimit();
            gen.writeText(",3"  + gen.formatDouble4(ml));

            float lw = bs.getLineWidth();
            Point2D ptSrc = new Point2D.Double(lw, 0);
            //Pen widths are set as absolute metric values (WU0;)
            Point2D ptDest = getTransform().deltaTransform(ptSrc, null);
            double transDist = UnitConv.pt2mm(ptDest.distance(0, 0));
            //System.out.println("--" + ptDest.distance(0, 0) + " " + transDist);
            gen.writeText(";PW" + gen.formatDouble4(transDist) + ";");

        } else {
            handleUnsupportedFeature("Unsupported Stroke: " + stroke.getClass().getName());
        }
    }

    /**
     * Applies a new Paint object.
     * @param paint Paint object to use
     * @throws IOException In case of an I/O problem
     */
    protected void applyPaint(Paint paint) throws IOException {
        if (paint instanceof Color) {
            Color col = (Color)paint;
            int shade = gen.convertToPCLShade(col);
            gen.writeText("TR0;FT10," + shade + ";");
        } else {
            handleUnsupportedFeature("Unsupported Paint: " + paint.getClass().getName());
        }
    }

    private void writeClip(Shape imclip) throws IOException {
        if (clippingDisabled) {
            return;
        }
        if (imclip == null) {
            //gen.writeText("IW;");
        } else {
            handleUnsupportedFeature("Clipping is not supported. Shape: " + imclip);
            /* This is an attempt to clip using the "InputWindow" (IW) but this only allows to
             * clip a rectangular area. Force falling back to bitmap mode for now.
            Rectangle2D bounds = imclip.getBounds2D();
            Point2D p1 = new Point2D.Double(bounds.getX(), bounds.getY());
            Point2D p2 = new Point2D.Double(
                    bounds.getX() + bounds.getWidth(), bounds.getY() + bounds.getHeight());
            getTransform().transform(p1, p1);
            getTransform().transform(p2, p2);
            gen.writeText("IW" + gen.formatDouble4(p1.getX())
                    + "," + gen.formatDouble4(p2.getY())
                    + "," + gen.formatDouble4(p2.getX())
                    + "," + gen.formatDouble4(p1.getY()) + ";");
            */
        }
    }

    /** {@inheritDoc} */
    public void draw(Shape s) {
        try {
            AffineTransform trans = getTransform();

            Shape imclip = getClip();
            writeClip(imclip);

            if (!Color.black.equals(getColor())) {
                //TODO PCL 5 doesn't support colored pens, PCL5c has a pen color (PC) command
                handleUnsupportedFeature("Only black is supported as stroke color: " + getColor());
            }
            applyStroke(getStroke());

            PathIterator iter = s.getPathIterator(trans);
            processPathIteratorStroke(iter);
            writeClip(null);
        } catch (IOException ioe) {
            handleIOException(ioe);
        }
    }

    /** {@inheritDoc} */
    public void fill(Shape s) {
        try {
            AffineTransform trans = getTransform();
            Shape imclip = getClip();
            writeClip(imclip);

            applyPaint(getPaint());

            PathIterator iter = s.getPathIterator(trans);
            processPathIteratorFill(iter);
            writeClip(null);
        } catch (IOException ioe) {
            handleIOException(ioe);
        }
    }

    /**
     * Processes a path iterator generating the nexessary painting operations.
     * @param iter PathIterator to process
     * @throws IOException In case of an I/O problem.
     */
    public void processPathIteratorStroke(PathIterator iter) throws IOException {
        gen.writeText("\n");
        double[] vals = new double[6];
        boolean penDown = false;
        double x = 0;
        double y = 0;
        StringBuffer sb = new StringBuffer(256);
        penUp(sb);
        while (!iter.isDone()) {
            int type = iter.currentSegment(vals);
            if (type == PathIterator.SEG_CLOSE) {
                gen.writeText("PM;");
                gen.writeText(sb.toString());
                gen.writeText("PM2;EP;");
                sb.setLength(0);
                iter.next();
                continue;
            } else if (type == PathIterator.SEG_MOVETO) {
                gen.writeText(sb.toString());
                sb.setLength(0);
                if (penDown) {
                    penUp(sb);
                    penDown = false;
                }
            } else {
                if (!penDown) {
                    penDown(sb);
                    penDown = true;
                }
            }
            switch (type) {
            case PathIterator.SEG_CLOSE:
                break;
            case PathIterator.SEG_MOVETO:
                x = vals[0];
                y = vals[1];
                plotAbsolute(x, y, sb);
                gen.writeText(sb.toString());
                sb.setLength(0);
                break;
            case PathIterator.SEG_LINETO:
                x = vals[0];
                y = vals[1];
                plotAbsolute(x, y, sb);
                break;
            case PathIterator.SEG_CUBICTO:
                x = vals[4];
                y = vals[5];
                bezierAbsolute(vals[0], vals[1], vals[2], vals[3], x, y, sb);
                break;
            case PathIterator.SEG_QUADTO:
                double originX = x;
                double originY = y;
                x = vals[2];
                y = vals[3];
                quadraticBezierAbsolute(originX, originY, vals[0], vals[1], x, y, sb);
                break;
            default:
                break;
            }
            iter.next();
        }
        sb.append("\n");
        gen.writeText(sb.toString());
    }

    /**
     * Processes a path iterator generating the nexessary painting operations.
     * @param iter PathIterator to process
     * @throws IOException In case of an I/O problem.
     */
    public void processPathIteratorFill(PathIterator iter) throws IOException {
        gen.writeText("\n");
        double[] vals = new double[6];
        boolean penDown = false;
        double x = 0;
        double y = 0;
        boolean pendingPM0 = true;
        StringBuffer sb = new StringBuffer(256);
        penUp(sb);
        while (!iter.isDone()) {
            int type = iter.currentSegment(vals);
            if (type == PathIterator.SEG_CLOSE) {
                sb.append("PM1;");
                iter.next();
                continue;
            } else if (type == PathIterator.SEG_MOVETO) {
                if (penDown) {
                    penUp(sb);
                    penDown = false;
                }
            } else {
                if (!penDown) {
                    penDown(sb);
                    penDown = true;
                }
            }
            switch (type) {
            case PathIterator.SEG_MOVETO:
                x = vals[0];
                y = vals[1];
                plotAbsolute(x, y, sb);
                break;
            case PathIterator.SEG_LINETO:
                x = vals[0];
                y = vals[1];
                plotAbsolute(x, y, sb);
                break;
            case PathIterator.SEG_CUBICTO:
                x = vals[4];
                y = vals[5];
                bezierAbsolute(vals[0], vals[1], vals[2], vals[3], x, y, sb);
                break;
            case PathIterator.SEG_QUADTO:
                double originX = x;
                double originY = y;
                x = vals[2];
                y = vals[3];
                quadraticBezierAbsolute(originX, originY, vals[0], vals[1], x, y, sb);
                break;
            default:
                throw new IllegalStateException("Must not get here");
            }
            if (pendingPM0) {
                pendingPM0 = false;
                sb.append("PM;");
            }
            iter.next();
        }
        sb.append("PM2;");
        fillPolygon(iter.getWindingRule(), sb);
        sb.append("\n");
        gen.writeText(sb.toString());
    }

    private void fillPolygon(int windingRule, StringBuffer sb) {
        int fillMethod = (windingRule == PathIterator.WIND_EVEN_ODD ? 0 : 1);
        sb.append("FP").append(fillMethod).append(";");
    }

    private void plotAbsolute(double x, double y, StringBuffer sb) {
        sb.append("PA").append(gen.formatDouble4(x));
        sb.append(",").append(gen.formatDouble4(y)).append(";");
    }

    private void bezierAbsolute(double x1, double y1, double x2, double y2, double x3, double y3,
            StringBuffer sb) {
        sb.append("BZ").append(gen.formatDouble4(x1));
        sb.append(",").append(gen.formatDouble4(y1));
        sb.append(",").append(gen.formatDouble4(x2));
        sb.append(",").append(gen.formatDouble4(y2));
        sb.append(",").append(gen.formatDouble4(x3));
        sb.append(",").append(gen.formatDouble4(y3)).append(";");
    }

    private void quadraticBezierAbsolute(double originX, double originY,
            double x1, double y1, double x2, double y2, StringBuffer sb) {
        //Quadratic Bezier curve can be mapped to a normal bezier curve
        //See http://pfaedit.sourceforge.net/bezier.html
        double nx1 = originX + (2.0 / 3.0) * (x1 - originX);
        double ny1 = originY + (2.0 / 3.0) * (y1 - originY);

        double nx2 = nx1 + (1.0 / 3.0) * (x2 - originX);
        double ny2 = ny1 + (1.0 / 3.0) * (y2 - originY);

        bezierAbsolute(nx1, ny1, nx2, ny2, x2, y2, sb);
    }

    private void penDown(StringBuffer sb) {
        sb.append("PD;");
    }

    private void penUp(StringBuffer sb) {
        sb.append("PU;");
    }

    /** {@inheritDoc} */
    public void drawString(String s, float x, float y) {
        java.awt.Font awtFont = getFont();
        FontRenderContext frc = getFontRenderContext();
        GlyphVector gv = awtFont.createGlyphVector(frc, s);
        Shape glyphOutline = gv.getOutline(x, y);
        fill(glyphOutline);
    }

    /** {@inheritDoc} */
    public void drawString(AttributedCharacterIterator iterator, float x,
            float y) {
        // TODO Auto-generated method stub
        handleUnsupportedFeature("drawString NYI");
    }

    /** {@inheritDoc} */
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        handleUnsupportedFeature("Bitmap images are not supported");
    }

    /** {@inheritDoc} */
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        handleUnsupportedFeature("Bitmap images are not supported");
    }

    /** {@inheritDoc} */
    public boolean drawImage(Image img, int x, int y, int width, int height,
            ImageObserver observer) {
        handleUnsupportedFeature("Bitmap images are not supported");
        return false;
    }

    /** {@inheritDoc} */
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        handleUnsupportedFeature("Bitmap images are not supported");
        return false;
        /*
         * First attempt disabled.
         * Reasons: Lack of transparency control, positioning and rotation issues
        final int width = img.getWidth(observer);
        final int height = img.getHeight(observer);
        if (width == -1 || height == -1) {
            return false;
        }

        Dimension size = new Dimension(width, height);
        BufferedImage buf = buildBufferedImage(size);

        java.awt.Graphics2D g = buf.createGraphics();
        try {
            g.setComposite(AlphaComposite.SrcOver);
            g.setBackground(new Color(255, 255, 255));
            g.setPaint(new Color(255, 255, 255));
            g.fillRect(0, 0, width, height);
            g.clip(new Rectangle(0, 0, buf.getWidth(), buf.getHeight()));

            if (!g.drawImage(img, 0, 0, observer)) {
                return false;
            }
        } finally {
            g.dispose();
        }

        try {
            AffineTransform at = getTransform();
            gen.enterPCLMode(false);
            //Shape imclip = getClip(); Clipping is not available in PCL
            Point2D p1 = new Point2D.Double(x, y);
            at.transform(p1, p1);
            pclContext.getTransform().transform(p1, p1);
            gen.setCursorPos(p1.getX(), p1.getY());
            gen.paintBitmap(buf, 72);
            gen.enterHPGL2Mode(false);
        } catch (IOException ioe) {
            handleIOException(ioe);
        }

        return true;*/
    }

    /** {@inheritDoc} */
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        // TODO Auto-generated method stub
        handleUnsupportedFeature("copyArea NYI");
    }

    /** {@inheritDoc} */
    public void setXORMode(Color c1) {
        // TODO Auto-generated method stub
        handleUnsupportedFeature("setXORMode NYI");
    }

    /**
     * Used to create proper font metrics
     */
    private Graphics2D fmg;

    {
        BufferedImage bi = new BufferedImage(1, 1,
                                             BufferedImage.TYPE_INT_ARGB);

        fmg = bi.createGraphics();
    }

    /**
     * Creates a buffered image.
     * @param size dimensions of the image to be created
     * @return the buffered image
     */
    protected BufferedImage buildBufferedImage(Dimension size) {
        return new BufferedImage(size.width, size.height,
                                 BufferedImage.TYPE_BYTE_GRAY);
    }

    /** {@inheritDoc} */
    public java.awt.FontMetrics getFontMetrics(java.awt.Font f) {
        return fmg.getFontMetrics(f);
    }

}
