/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.IOException;
import java.text.AttributedCharacterIterator;

import org.apache.fop.util.UnitConv;
import org.apache.xmlgraphics.java2d.AbstractGraphics2D;
import org.apache.xmlgraphics.java2d.GraphicContext;

/**
 * Graphics2D implementation implementing PCL and HP GL/2.
 */
public class PCLGraphics2D extends AbstractGraphics2D {

    /** The PCL generator */
    protected PCLGenerator gen;
    
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

    /** @see java.awt.Graphics#create() */
    public Graphics create() {
        return new PCLGraphics2D(this);
    }

    /** @see java.awt.Graphics#dispose() */
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
     * Central handler for IOExceptions for this class.
     * @param ioe IOException to handle
     */
    public void handleIOException(IOException ioe) {
        //TODO Surely, there's a better way to do this.
        ioe.printStackTrace();
    }

    /** @see java.awt.Graphics2D#getDeviceConfiguration() */
    public GraphicsConfiguration getDeviceConfiguration() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration();
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
                for (int idx = 0, len = Math.min(20, da.length); idx < len; idx++) {
                    gen.writeText(gen.formatDouble4(da[idx]));
                    if (idx < da.length - 1) {
                        gen.writeText(",");
                    }
                }
                gen.writeText(";");
                /* TODO Dash phase NYI
                float offset = bs.getDashPhase();
                gen.writeln(gen.formatDouble4(offset) + " setdash");
                */
                gen.writeText("LT1;");
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
            Point2D ptDest = getTransform().transform(ptSrc, null);
            double transDist = UnitConv.pt2mm(ptDest.distance(0, 0));
            //System.out.println("--" + ptDest.distance(0, 0) + " " + transDist);
            gen.writeText(";PW" + gen.formatDouble4(transDist) + ";");
            
        } else {
            System.err.println("Unsupported Stroke: " + stroke.getClass().getName());
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
            System.err.println("Unsupported Paint: " + paint.getClass().getName());
        }
    }

    /** @see java.awt.Graphics2D#draw(java.awt.Shape) */
    public void draw(Shape s) {
        try {
            AffineTransform trans = getTransform();
    
            Shape imclip = getClip();
            //writeClip(imclip);
            //establishColor(getColor());
    
            applyPaint(getPaint());
            applyStroke(getStroke());
    
            //gen.writeln("newpath");
            PathIterator iter = s.getPathIterator(trans);
            processPathIterator(iter);
            gen.writeText("EP;");
        } catch (IOException ioe) {
            handleIOException(ioe);
        }
    }

    /** @see java.awt.Graphics2D#fill(java.awt.Shape) */
    public void fill(Shape s) {
        try {
            AffineTransform trans = getTransform();
            Shape imclip = getClip();
            //writeClip(imclip);
            
            //establishColor(getColor());

            applyPaint(getPaint());

            PathIterator iter = s.getPathIterator(trans);
            processPathIterator(iter);
            int fillMethod = (iter.getWindingRule() == PathIterator.WIND_EVEN_ODD ? 0 : 1);
            gen.writeText("FP" + fillMethod + ";");
        } catch (IOException ioe) {
            handleIOException(ioe);
        }
    }

    /**
     * Processes a path iterator generating the nexessary painting operations.
     * @param iter PathIterator to process
     * @throws IOException In case of an I/O problem.
     */
    public void processPathIterator(PathIterator iter) throws IOException {
        double[] vals = new double[6];
        boolean penDown = false;
        boolean hasFirst = false;
        double x = 0, firstX = 0;
        double y = 0, firstY = 0;
        boolean pendingPM0 = true;
        penUp();
        while (!iter.isDone()) {
            int type = iter.currentSegment(vals);
            if (type == PathIterator.SEG_CLOSE) {
                hasFirst = false;
                /*
                if (firstX != x && firstY != y) {
                    plotAbsolute(firstX, firstY);
                }*/
                //penUp();
                gen.writeText("PM1;");
                iter.next();
                continue;
            }
            if (type == PathIterator.SEG_MOVETO) {
                if (penDown) {
                    penUp();
                    penDown = false;
                }
            } else {
                if (!penDown) {
                    penDown();
                    penDown = true;
                }
            }
            switch (type) {
            case PathIterator.SEG_CUBICTO:
                x = vals[4];
                y = vals[5];
                bezierAbsolute(vals[0], vals[1], vals[2], vals[3], x, y);
                break;
            case PathIterator.SEG_LINETO:
                x = vals[0];
                y = vals[1];
                plotAbsolute(x, y);
                break;
            case PathIterator.SEG_MOVETO:
                x = vals[0];
                y = vals[1];
                plotAbsolute(x, y);
                break;
            case PathIterator.SEG_QUADTO:
                double originX = x;
                double originY = y;
                x = vals[2];
                y = vals[3];
                quadraticBezierAbsolute(originX, originY, vals[0], vals[1], x, y);
                break;
            case PathIterator.SEG_CLOSE:
                break;
            default:
                break;
            }
            if (pendingPM0) {
                pendingPM0 = false;
                gen.writeText("PM;");
            }
            if (!hasFirst) {
                firstX = x;
                firstY = y;
            }
            iter.next();
        }
        gen.writeText("PM2;");
    }

    private void plotAbsolute(double x, double y) throws IOException {
        gen.writeText("PA" + gen.formatDouble4(x) + ","
                + gen.formatDouble4(y) + ";");
    }

    private void bezierAbsolute(double x1, double y1, double x2, double y2, double x3, double y3) 
                throws IOException {
        gen.writeText("BZ" + gen.formatDouble4(x1) + ","
                + gen.formatDouble4(y1) + ","
                + gen.formatDouble4(x2) + ","
                + gen.formatDouble4(y2) + ","
                + gen.formatDouble4(x3) + ","
                + gen.formatDouble4(y3) + ";");
    }

    private void quadraticBezierAbsolute(double originX, double originY, 
            double x1, double y1, double x2, double y2) 
            throws IOException {
        //Quadratic Bezier curve can be mapped to a normal bezier curve
        //See http://pfaedit.sourceforge.net/bezier.html
        double nx1 = originX + (2.0 / 3.0) * (x1 - originX);
        double ny1 = originY + (2.0 / 3.0) * (y1 - originY);
        
        double nx2 = nx1 + (1.0 / 3.0) * (x2 - originX);
        double ny2 = ny1 + (1.0 / 3.0) * (y2 - originY);
        
        bezierAbsolute(nx1, ny1, nx2, ny2, x2, y2);
    }

    private void penDown() throws IOException {
        gen.writeText("PD;");
    }

    private void penUp() throws IOException {
        gen.writeText("PU;");
    }

    /** @see java.awt.Graphics2D#drawString(java.lang.String, float, float) */
    public void drawString(String s, float x, float y) {
        // TODO Auto-generated method stub
        System.err.println("drawString NYI");
    }

    /** @see java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator, float, float) */
    public void drawString(AttributedCharacterIterator iterator, float x,
            float y) {
        // TODO Auto-generated method stub
        System.err.println("drawString NYI");
    }

    /**
     * @see java.awt.Graphics2D#drawRenderedImage(java.awt.image.RenderedImage, 
     *          java.awt.geom.AffineTransform)
     */
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        // TODO Auto-generated method stub
        System.err.println("drawRenderedImage NYI");
    }

    /**
     * @see java.awt.Graphics2D#drawRenderableImage(java.awt.image.renderable.RenderableImage, 
     *          java.awt.geom.AffineTransform)
     */
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        // TODO Auto-generated method stub
        System.err.println("drawRenderedImage NYI");
    }

    /**
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, 
     *          java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int x, int y, int width, int height,
            ImageObserver observer) {
        // TODO Auto-generated method stub
        System.err.println("drawImage NYI");
        return false;
    }

    /**
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        // TODO Auto-generated method stub
        System.err.println("drawImage NYI");
        return false;
    }

    /** @see java.awt.Graphics#copyArea(int, int, int, int, int, int) */
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        // TODO Auto-generated method stub
        System.err.println("copyArea NYI");
    }

    /** @see java.awt.Graphics#setXORMode(java.awt.Color) */
    public void setXORMode(Color c1) {
        // TODO Auto-generated method stub
        System.err.println("setXORMode NYI");
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

    /** @see java.awt.Graphics#getFontMetrics(java.awt.Font) */
    public java.awt.FontMetrics getFontMetrics(java.awt.Font f) {
        return fmg.getFontMetrics(f);
    }

}
