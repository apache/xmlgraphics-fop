/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.render.ps;

//Java
import java.util.List;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.IOException;

// FOP
import org.apache.fop.layout.FontInfo;
import org.apache.fop.layout.FontState;

// Batik
import org.apache.batik.ext.awt.g2d.AbstractGraphics2D;
import org.apache.batik.ext.awt.g2d.GraphicContext;


/**
 * This concrete implementation of <tt>AbstractGraphics2D</tt> is a
 * simple help to programmers to get started with their own
 * implementation of <tt>Graphics2D</tt>.
 * <tt>DefaultGraphics2D</tt> implements all the abstract methods
 * is <tt>AbstractGraphics2D</tt> and makes it easy to start
 * implementing a <tt>Graphic2D</tt> piece-meal.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 * @version $Id$
 * @see org.apache.batik.ext.awt.g2d.AbstractGraphics2D
 */
public class PSGraphics2D extends AbstractGraphics2D {

    private boolean standalone = false;

    /**
     * the PostScript genertaor being created
     */
    protected PSGenerator gen;

    /** Currently valid FontState */
    protected FontState fontState;

    /**
     * the current (internal) font name
     */
    protected String currentFontName;

    /**
     * the current font size in millipoints
     */
    protected int currentFontSize;

    /**
     * the current vertical position in millipoints from bottom
     */
    protected int currentYPosition = 0;

    /**
     * the current horizontal position in millipoints from left
     */
    protected int currentXPosition = 0;

    /**
     * the current colour for use in svg
     */
    protected Color currentColour = new Color(0, 0, 0);

    /** FontInfo containing all available fonts */
    protected FontInfo fontInfo;

    /**
     * Create a new Graphics2D that generates PostScript code.
     * @param textAsShapes True if text should be rendered as graphics
     * @param gen PostScript generator to use for output
     * @see org.apache.batik.ext.awt.g2d.AbstractGraphics2D#AbstractGraphics2D(boolean)
     */
    public PSGraphics2D(boolean textAsShapes, PSGenerator gen) {
        super(textAsShapes);
        this.gen = gen;
    }

    /**
     * Constructor for creating copies
     * @param g parent PostScript Graphics2D
     */
    public PSGraphics2D(PSGraphics2D g) {
        super(g);
    }

    /**
     * Sets the GraphicContext
     * @param c GraphicContext to use
     */
    public void setGraphicContext(GraphicContext c) {
        gc = c;
    }

    /**
     * Creates a new <code>Graphics</code> object that is
     * a copy of this <code>Graphics</code> object.
     * @return     a new graphics context that is a copy of
     * this graphics context.
     */
    public Graphics create() {
        return new PSGraphics2D(this);
    }

    /**
     * Central handler for IOExceptions for this class.
     * @param ioe IOException to handle
     */
    protected void handleIOException(IOException ioe) {
        ioe.printStackTrace();
    }

    /**
     * Draws as much of the specified image as is currently available.
     * The image is drawn with its top-left corner at
     * (<i>x</i>,&nbsp;<i>y</i>) in this graphics context's coordinate
     * space. Transparent pixels in the image do not affect whatever
     * pixels are already there.
     * <p>
     * This method returns immediately in all cases, even if the
     * complete image has not yet been loaded, and it has not been dithered
     * and converted for the current output device.
     * <p>
     * If the image has not yet been completely loaded, then
     * <code>drawImage</code> returns <code>false</code>. As more of
     * the image becomes available, the process that draws the image notifies
     * the specified image observer.
     * @param    img the specified image to be drawn.
     * @param    x   the <i>x</i> coordinate.
     * @param    y   the <i>y</i> coordinate.
     * @param    observer    object to be notified as more of
     * the image is converted.
     * @return True if the image has been fully drawn/loaded
     * @see      java.awt.Image
     * @see      java.awt.image.ImageObserver
     * @see      java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     */
    public boolean drawImage(Image img, int x, int y,
                             ImageObserver observer) {
        // System.err.println("drawImage:x, y");

        final int width = img.getWidth(observer);
        final int height = img.getHeight(observer);
        if (width == -1 || height == -1) {
            return false;
        }

        Dimension size = new Dimension(width, height);
        BufferedImage buf = buildBufferedImage(size);

        java.awt.Graphics2D g = buf.createGraphics();
        g.setComposite(AlphaComposite.SrcOver);
        g.setBackground(new Color(1, 1, 1, 0));
        g.setPaint(new Color(1, 1, 1, 0));
        g.fillRect(0, 0, width, height);
        g.clip(new Rectangle(0, 0, buf.getWidth(), buf.getHeight()));

        if (!g.drawImage(img, 0, 0, observer)) {
            return false;
        }
        g.dispose();

        final byte[] result = new byte[buf.getWidth() * buf.getHeight() * 3];
        //final byte[] mask = new byte[buf.getWidth() * buf.getHeight()];

        Raster raster = buf.getData();
        DataBuffer bd = raster.getDataBuffer();

        int count = 0;
        //int maskpos = 0;
        switch (bd.getDataType()) {
        case DataBuffer.TYPE_INT:
            int[][] idata = ((DataBufferInt)bd).getBankData();
            for (int i = 0; i < idata.length; i++) {
                for (int j = 0; j < idata[i].length; j++) {
                    // mask[maskpos++] = (byte)((idata[i][j] >> 24) & 0xFF);
                    if (((idata[i][j] >> 24) & 0xFF) != 255) {
                        result[count++] = (byte)0xFF;
                        result[count++] = (byte)0xFF;
                        result[count++] = (byte)0xFF;
                    } else {
                        result[count++] = (byte)((idata[i][j] >> 16) & 0xFF);
                        result[count++] = (byte)((idata[i][j] >> 8) & 0xFF);
                        result[count++] = (byte)((idata[i][j]) & 0xFF);
                    }
                }
            }
            break;
        default:
            // error
            break;
        }

        /*try {
            FopImage fopimg = new TempImage(width, height, result, mask);
            AffineTransform at = getTransform();
            double[] matrix = new double[6];
            at.getMatrix(matrix);
            psRenderer.write("gsave");
            Shape imclip = getClip();
            writeClip(imclip);
            // psRenderer.write("" + matrix[0] + " " + matrix[1] +
            // " " + matrix[2] + " " + matrix[3] + " " +
            // matrix[4] + " " + matrix[5] + " cm\n");
            //psRenderer.renderBitmap(fopimg, x, y, width, height);
            psRenderer.write("grestore");
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return true;
    }

    /**
     * Creates a buffered image.
     * @param size dimensions of the image to be created
     * @return the buffered image
     */
    public BufferedImage buildBufferedImage(Dimension size) {
        return new BufferedImage(size.width, size.height,
                                 BufferedImage.TYPE_INT_ARGB);
    }

    /*class TempImage implements FopImage {
        int m_height;
        int m_width;
        int m_bitsPerPixel;
        PDFColorSpace m_colorSpace;
        int m_bitmapSiye;
        byte[] m_bitmaps;
        byte[] m_mask;
        PDFColor transparent = new PDFColor(255, 255, 255);

        TempImage(int width, int height, byte[] result,
                  byte[] mask) {
            this.m_height = height;
            this.m_width = width;
            this.m_bitsPerPixel = 8;
            this.m_colorSpace = new PDFColorSpace(PDFColorSpace.DEVICE_RGB);
            // this.m_isTransparent = false;
            // this.m_bitmapsSize = this.m_width * this.m_height * 3;
            this.m_bitmaps = result;
            this.m_mask = mask;
        }

        public boolean load(int type, FOUserAgent ua) {
            return true;
        }

        public String getMimeType() {
            return "";
        }

        public String getURL() {
            return "" + m_bitmaps;
        }

        // image size
        public int getWidth() {
            return m_width;
        }

        public int getHeight() {
            return m_height;
        }

        // DeviceGray, DeviceRGB, or DeviceCMYK
        public PDFColorSpace getColorSpace() {
            return m_colorSpace;
        }

        // bits per pixel
        public int getBitsPerPixel() {
            return m_bitsPerPixel;
        }

        // For transparent images
        public boolean isTransparent() {
            return transparent != null;
        }

        public PDFColor getTransparentColor() {
            return transparent;
        }

        public boolean hasSoftMask() {
            return m_mask != null;
        }

        public byte[] getSoftMask() {
            return m_mask;
        }

        // get the image bytes, and bytes properties

        // get uncompressed image bytes
        public byte[] getBitmaps() {
            return m_bitmaps;
        }

        // width * (bitsPerPixel / 8) * height, no ?
        public int getBitmapsSize() {
            return m_width * m_height * 3;
        }

        // get compressed image bytes
        // I don't know if we really need it, nor if it
        // should be changed...
        public byte[] getRessourceBytes() {
            return null;
        }

        public int getRessourceBytesSize() {
            return 0;
        }

        // return null if no corresponding PDFFilter
        public PDFFilter getPDFFilter() {
            return null;
        }

        // release memory
        public void close() {}

    }*/


    /**
     * Draws as much of the specified image as has already been scaled
     * to fit inside the specified rectangle.
     * <p>
     * The image is drawn inside the specified rectangle of this
     * graphics context's coordinate space, and is scaled if
     * necessary. Transparent pixels do not affect whatever pixels
     * are already there.
     * <p>
     * This method returns immediately in all cases, even if the
     * entire image has not yet been scaled, dithered, and converted
     * for the current output device.
     * If the current output representation is not yet complete, then
     * <code>drawImage</code> returns <code>false</code>. As more of
     * the image becomes available, the process that draws the image notifies
     * the image observer by calling its <code>imageUpdate</code> method.
     * <p>
     * A scaled version of an image will not necessarily be
     * available immediately just because an unscaled version of the
     * image has been constructed for this output device.  Each size of
     * the image may be cached separately and generated from the original
     * data in a separate image production sequence.
     * @param    img    the specified image to be drawn.
     * @param    x      the <i>x</i> coordinate.
     * @param    y      the <i>y</i> coordinate.
     * @param    width  the width of the rectangle.
     * @param    height the height of the rectangle.
     * @param    observer    object to be notified as more of
     * the image is converted.
     * @return   True if the image has been fully loaded/drawn
     * @see      java.awt.Image
     * @see      java.awt.image.ImageObserver
     * @see      java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     */
    public boolean drawImage(Image img, int x, int y, int width, int height,
                             ImageObserver observer) {
        System.out.println("drawImage");
        return true;
    }

    /**
     * Disposes of this graphics context and releases
     * any system resources that it is using.
     * A <code>Graphics</code> object cannot be used after
     * <code>dispose</code>has been called.
     * <p>
     * When a Java program runs, a large number of <code>Graphics</code>
     * objects can be created within a short time frame.
     * Although the finalization process of the garbage collector
     * also disposes of the same system resources, it is preferable
     * to manually free the associated resources by calling this
     * method rather than to rely on a finalization process which
     * may not run to completion for a long period of time.
     * <p>
     * Graphics objects which are provided as arguments to the
     * <code>paint</code> and <code>update</code> methods
     * of components are automatically released by the system when
     * those methods return. For efficiency, programmers should
     * call <code>dispose</code> when finished using
     * a <code>Graphics</code> object only if it was created
     * directly from a component or another <code>Graphics</code> object.
     * @see         java.awt.Graphics#finalize
     * @see         java.awt.Component#paint
     * @see         java.awt.Component#update
     * @see         java.awt.Component#getGraphics
     * @see         java.awt.Graphics#create
     */
    public void dispose() {
        // System.out.println("dispose");
        this.gen = null;
        fontState = null;
        currentFontName = null;
        currentColour = null;
        fontInfo = null;
    }

    /**
     * Strokes the outline of a <code>Shape</code> using the settings of the
     * current <code>Graphics2D</code> context.  The rendering attributes
     * applied include the <code>Clip</code>, <code>Transform</code>,
     * <code>Paint</code>, <code>Composite</code> and
     * <code>Stroke</code> attributes.
     * @param s the <code>Shape</code> to be rendered
     * @see #setStroke
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see #transform
     * @see #setTransform
     * @see #clip
     * @see #setClip
     * @see #setComposite
     */
    public void draw(Shape s) {
        try {
            // System.out.println("draw(Shape)");
            gen.saveGraphicsState();
            Shape imclip = getClip();
            writeClip(imclip);
            Color c = getColor();
            gen.writeln(c.getRed() + " " + c.getGreen() + " " + c.getBlue()
                             + " setrgbcolor");
            
            applyPaint(getPaint(), false);
            applyStroke(getStroke());
            
            gen.writeln("newpath");
            PathIterator iter = s.getPathIterator(getTransform());
            while (!iter.isDone()) {
                double vals[] = new double[6];
                int type = iter.currentSegment(vals);
                switch (type) {
                case PathIterator.SEG_CUBICTO:
                    gen.writeln(gen.formatDouble(1000 * vals[0]) + " "
                              + gen.formatDouble(1000 * vals[1]) + " "
                              + gen.formatDouble(1000 * vals[2]) + " "
                              + gen.formatDouble(1000 * vals[3]) + " "
                              + gen.formatDouble(1000 * vals[4]) + " "
                              + gen.formatDouble(1000 * vals[5])
                              + " curveto");
                    break;
                case PathIterator.SEG_LINETO:
                    gen.writeln(gen.formatDouble(1000 * vals[0]) + " "
                              + gen.formatDouble(1000 * vals[1])
                              + " lineto");
                    break;
                case PathIterator.SEG_MOVETO:
                    gen.writeln(gen.formatDouble(1000 * vals[0]) + " "
                              + gen.formatDouble(1000 * vals[1])
                              + " M");
                    break;
                case PathIterator.SEG_QUADTO:
                    // psRenderer.write((1000 * PDFNumber.doubleOut(vals[0])) +
                    // " " + (1000 * PDFNumber.doubleOut(vals[1])) + " " +
                    // (1000 * PDFNumber.doubleOut(vals[2])) + " " +
                    // (1000 * PDFNumber.doubleOut(vals[3])) + " y\n");
                    break;
                case PathIterator.SEG_CLOSE:
                    gen.writeln("closepath");
                    break;
                default:
                    break;
                }
                iter.next();
            }
            doDrawing(false, true, false);
            gen.restoreGraphicsState();
        } catch (IOException ioe) {
            handleIOException(ioe);
        }
    }

    /**
     * Establishes a clipping region
     * @param s Shape defining the clipping region
     */
    protected void writeClip(Shape s) {
        try {
            PathIterator iter = s.getPathIterator(getTransform());
            gen.writeln("newpath");
            while (!iter.isDone()) {
                double vals[] = new double[6];
                int type = iter.currentSegment(vals);
                switch (type) {
                case PathIterator.SEG_CUBICTO:
                    gen.writeln(gen.formatDouble(1000 * vals[0]) + " "
                              + gen.formatDouble(1000 * vals[1]) + " "
                              + gen.formatDouble(1000 * vals[2]) + " "
                              + gen.formatDouble(1000 * vals[3]) + " "
                              + gen.formatDouble(1000 * vals[4]) + " "
                              + gen.formatDouble(1000 * vals[5])
                              + " curveto");
                    break;
                case PathIterator.SEG_LINETO:
                    gen.writeln(gen.formatDouble(1000 * vals[0]) + " "
                              + gen.formatDouble(1000 * vals[1])
                              + " lineto");
                    break;
                case PathIterator.SEG_MOVETO:
                    gen.writeln(gen.formatDouble(1000 * vals[0]) + " "
                              + gen.formatDouble(1000 * vals[1])
                              + " M");
                    break;
                case PathIterator.SEG_QUADTO:
                    // psRenderer.write(1000 * PDFNumber.doubleOut(vals[0]) +
                    // " " + 1000 * PDFNumber.doubleOut(vals[1]) + " " +
                    // 1000 * PDFNumber.doubleOut(vals[2]) + " " +
                    // 1000 * PDFNumber.doubleOut(vals[3]) + " y\n");
                    break;
                case PathIterator.SEG_CLOSE:
                    gen.writeln("closepath");
                    break;
                default:
                    break;
                }
                iter.next();
            }
            // clip area
            gen.writeln("clippath");
        } catch (IOException ioe) {
            handleIOException(ioe);
        }
    }

    /**
     * Applies a new Paint object.
     * @param paint Paint object to use
     * @param fill True if to be applied for filling
     */
    protected void applyPaint(Paint paint, boolean fill) {
        if (paint instanceof GradientPaint) {
            GradientPaint gp = (GradientPaint)paint;
            Color c1 = gp.getColor1();
            Color c2 = gp.getColor2();
            Point2D p1 = gp.getPoint1();
            Point2D p2 = gp.getPoint2();
            //boolean cyclic = gp.isCyclic();

            List theCoords = new java.util.ArrayList();
            theCoords.add(new Double(p1.getX()));
            theCoords.add(new Double(p1.getY()));
            theCoords.add(new Double(p2.getX()));
            theCoords.add(new Double(p2.getY()));

            List theExtend = new java.util.ArrayList();
            theExtend.add(new Boolean(true));
            theExtend.add(new Boolean(true));

            List theDomain = new java.util.ArrayList();
            theDomain.add(new Double(0));
            theDomain.add(new Double(1));

            List theEncode = new java.util.ArrayList();
            theEncode.add(new Double(0));
            theEncode.add(new Double(1));
            theEncode.add(new Double(0));
            theEncode.add(new Double(1));

            List theBounds = new java.util.ArrayList();
            theBounds.add(new Double(0));
            theBounds.add(new Double(1));

            //List theFunctions = new java.util.ArrayList();

            List someColors = new java.util.ArrayList();

            Color color1 = new Color(c1.getRed(), c1.getGreen(),
                                           c1.getBlue());
            someColors.add(color1);
            Color color2 = new Color(c2.getRed(), c2.getGreen(),
                                           c2.getBlue());
            someColors.add(color2);

            //PDFColorSpace aColorSpace = new PDFColorSpace(PDFColorSpace.DEVICE_RGB);
        } else if (paint instanceof TexturePaint) {
            //nop
        }
    }

    /**
     * Applies a new Stroke object.
     * @param stroke Stroke object to use
     */
    protected void applyStroke(Stroke stroke) {
        try {
            if (stroke instanceof BasicStroke) {
                BasicStroke bs = (BasicStroke)stroke;
            
                float[] da = bs.getDashArray();
                if (da != null) {
                    gen.writeln("[");
                    for (int count = 0; count < da.length; count++) {
                        gen.writeln("" + (1000 * (int)da[count]));
                        if (count < da.length - 1) {
                            gen.writeln(" ");
                        }
                    }
                    gen.writeln("] ");
                    float offset = bs.getDashPhase();
                    gen.writeln((1000 * (int)offset) + " setdash");
                }
                int ec = bs.getEndCap();
                switch (ec) {
                case BasicStroke.CAP_BUTT:
                    gen.writeln(0 + " setlinecap");
                    break;
                case BasicStroke.CAP_ROUND:
                    gen.writeln(1 + " setlinecap");
                    break;
                case BasicStroke.CAP_SQUARE:
                    gen.writeln(2 + " setlinecap");
                    break;
                }
            
                int lj = bs.getLineJoin();
                switch (lj) {
                case BasicStroke.JOIN_MITER:
                    gen.writeln("0 setlinejoin");
                    break;
                case BasicStroke.JOIN_ROUND:
                    gen.writeln("1 setlinejoin");
                    break;
                case BasicStroke.JOIN_BEVEL:
                    gen.writeln("2 setlinejoin");
                    break;
                }
                float lw = bs.getLineWidth();
                gen.writeln(gen.formatDouble(1000 * lw) + " setlinewidth");
            
                float ml = bs.getMiterLimit();
                gen.writeln(gen.formatDouble(1000 * ml) + " setmiterlimit");
            }
        } catch (IOException ioe) {
            handleIOException(ioe);
        }
    }

    /**
     * Renders a {@link RenderedImage},
     * applying a transform from image
     * space into user space before drawing.
     * The transformation from user space into device space is done with
     * the current <code>Transform</code> in the <code>Graphics2D</code>.
     * The specified transformation is applied to the image before the
     * transform attribute in the <code>Graphics2D</code> context is applied.
     * The rendering attributes applied include the <code>Clip</code>,
     * <code>Transform</code>, and <code>Composite</code> attributes. Note
     * that no rendering is done if the specified transform is
     * noninvertible.
     * @param img the image to be rendered
     * @param xform the transformation from image space into user space
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     */
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        System.out.println("drawRenderedImage");
    }


    /**
     * Renders a
     * {@link RenderableImage},
     * applying a transform from image space into user space before drawing.
     * The transformation from user space into device space is done with
     * the current <code>Transform</code> in the <code>Graphics2D</code>.
     * The specified transformation is applied to the image before the
     * transform attribute in the <code>Graphics2D</code> context is applied.
     * The rendering attributes applied include the <code>Clip</code>,
     * <code>Transform</code>, and <code>Composite</code> attributes. Note
     * that no rendering is done if the specified transform is
     * noninvertible.
     * <p>
     * Rendering hints set on the <code>Graphics2D</code> object might
     * be used in rendering the <code>RenderableImage</code>.
     * If explicit control is required over specific hints recognized by a
     * specific <code>RenderableImage</code>, or if knowledge of which hints
     * are used is required, then a <code>RenderedImage</code> should be
     * obtained directly from the <code>RenderableImage</code>
     * and rendered using
     * {@link #drawRenderedImage(RenderedImage, AffineTransform) drawRenderedImage}.
     * @param img the image to be rendered
     * @param xform the transformation from image space into user space
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     * @see #drawRenderedImage
     */
    public void drawRenderableImage(RenderableImage img,
                                    AffineTransform xform) {
        System.out.println("drawRenderableImage");
    }

    /**
     * Renders the text specified by the specified <code>String</code>,
     * using the current <code>Font</code> and <code>Paint</code> attributes
     * in the <code>Graphics2D</code> context.
     * The baseline of the first character is at position
     * (<i>x</i>,&nbsp;<i>y</i>) in the User Space.
     * The rendering attributes applied include the <code>Clip</code>,
     * <code>Transform</code>, <code>Paint</code>, <code>Font</code> and
     * <code>Composite</code> attributes. For characters in script systems
     * such as Hebrew and Arabic, the glyphs can be rendered from right to
     * left, in which case the coordinate supplied is the location of the
     * leftmost character on the baseline.
     * @param s the <code>String</code> to be rendered
     * @param x the x-coordinate where the <code>String</code>
     * should be rendered
     * @param y the y-coordinate where the <code>String</code>
     * should be rendered
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see java.awt.Graphics#setFont
     * @see #setTransform
     * @see #setComposite
     * @see #setClip
     */
    public void drawString(String s, float x, float y) {
        try {
            System.out.println("drawString(String)");
            gen.writeln("BT");
            Shape imclip = getClip();
            writeClip(imclip);
            Color c = getColor();
            gen.writeln(c.getRed() + " " + c.getGreen() + " " + c.getBlue()
                             + " setrgbcolor");
            
            AffineTransform trans = getTransform();
            trans.translate(x, y);
            double[] vals = new double[6];
            trans.getMatrix(vals);
            
            gen.writeln(gen.formatDouble(vals[0]) + " "
                      + gen.formatDouble(vals[1]) + " "
                      + gen.formatDouble(vals[2]) + " "
                      + gen.formatDouble(vals[3]) + " "
                      + gen.formatDouble(vals[4]) + " "
                      + gen.formatDouble(vals[5]) + " "
                      + gen.formatDouble(vals[6]) + " Tm [" + s + "]");
            
            gen.writeln("ET");
        } catch (IOException ioe) {
            handleIOException(ioe);
        }
    }

    /**
     * Renders the text of the specified iterator, using the
     * <code>Graphics2D</code> context's current <code>Paint</code>. The
     * iterator must specify a font
     * for each character. The baseline of the
     * first character is at position (<i>x</i>,&nbsp;<i>y</i>) in the
     * User Space.
     * The rendering attributes applied include the <code>Clip</code>,
     * <code>Transform</code>, <code>Paint</code>, and
     * <code>Composite</code> attributes.
     * For characters in script systems such as Hebrew and Arabic,
     * the glyphs can be rendered from right to left, in which case the
     * coordinate supplied is the location of the leftmost character
     * on the baseline.
     * @param iterator the iterator whose text is to be rendered
     * @param x the x-coordinate where the iterator's text is to be
     * rendered
     * @param y the y-coordinate where the iterator's text is to be
     * rendered
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see #setTransform
     * @see #setComposite
     * @see #setClip
     */
    public void drawString(AttributedCharacterIterator iterator, float x,
                           float y) {
        try {
            System.err.println("drawString(AttributedCharacterIterator)");
            
            gen.writeln("BT");
            Shape imclip = getClip();
            writeClip(imclip);
            Color c = getColor();
            currentColour = new Color(c.getRed(), c.getGreen(), c.getBlue());
            //gen.writeln(currentColour.getColorSpaceOut(true));
            c = getBackground();
            Color col = new Color(c.getRed(), c.getGreen(), c.getBlue());
            //gen.writeln(col.getColorSpaceOut(false));
            
            AffineTransform trans = getTransform();
            trans.translate(x, y);
            double[] vals = new double[6];
            trans.getMatrix(vals);
            
            for (char ch = iterator.first(); ch != CharacterIterator.DONE;
                    ch = iterator.next()) {
                //Map attr = iterator.getAttributes();
            
                gen.writeln(gen.formatDouble(vals[0]) + " "
                          + gen.formatDouble(vals[1]) + " "
                          + gen.formatDouble(vals[2]) + " "
                          + gen.formatDouble(vals[3]) + " "
                          + gen.formatDouble(vals[4]) + " "
                          + gen.formatDouble(vals[5]) + " "
                          + gen.formatDouble(vals[6]) + " Tm [" + ch
                          + "]");
            }
            
            gen.writeln("ET");
        } catch (IOException ioe) {
            handleIOException(ioe);
        }
    }

    /**
     * Fills the interior of a <code>Shape</code> using the settings of the
     * <code>Graphics2D</code> context. The rendering attributes applied
     * include the <code>Clip</code>, <code>Transform</code>,
     * <code>Paint</code>, and <code>Composite</code>.
     * @param s the <code>Shape</code> to be filled
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     */
    public void fill(Shape s) {
        try {
            // System.err.println("fill");
            gen.writeln("gsave");
            Shape imclip = getClip();
            writeClip(imclip);
            Color c = getColor();
            gen.writeln(c.getRed() + " " + c.getGreen() + " " + c.getBlue()
                             + " setrgbcolor");
            
            applyPaint(getPaint(), true);
            
            gen.writeln("newpath");
            PathIterator iter = s.getPathIterator(getTransform());
            while (!iter.isDone()) {
                double vals[] = new double[6];
                int type = iter.currentSegment(vals);
                switch (type) {
                case PathIterator.SEG_CUBICTO:
                    gen.writeln(gen.formatDouble(1000 * vals[0]) + " "
                              + gen.formatDouble(1000 * vals[1]) + " "
                              + gen.formatDouble(1000 * vals[2]) + " "
                              + gen.formatDouble(1000 * vals[3]) + " "
                              + gen.formatDouble(1000 * vals[4]) + " "
                              + gen.formatDouble(1000 * vals[5])
                              + " curveto");
                    break;
                case PathIterator.SEG_LINETO:
                    gen.writeln(gen.formatDouble(1000 * vals[0]) + " "
                              + gen.formatDouble(1000 * vals[1])
                              + " lineto");
                    break;
                case PathIterator.SEG_MOVETO:
                    gen.writeln(gen.formatDouble(1000 * vals[0]) + " "
                              + gen.formatDouble(1000 * vals[1])
                              + " M");
                    break;
                case PathIterator.SEG_QUADTO:
                    // psRenderer.write(1000 * PDFNumber.doubleOut(vals[0]) +
                    // " " + 1000 * PDFNumber.doubleOut(vals[1]) + " " +
                    // 1000 * PDFNumber.doubleOut(vals[2]) + " " +
                    // 1000 * PDFNumber.doubleOut(vals[3]) + " y\n");
                    break;
                case PathIterator.SEG_CLOSE:
                    gen.writeln("closepath");
                    break;
                default:
                    break;
                }
                iter.next();
            }
            doDrawing(true, false,
                      iter.getWindingRule() == PathIterator.WIND_EVEN_ODD);
            gen.writeln("grestore");
        } catch (IOException ioe) {
            handleIOException(ioe);
        }
    }

    /**
     * Commits a painting operation.
     * @param fill filling
     * @param stroke stroking
     * @param nonzero ???
     * @exception IOException In case of an I/O problem
     */
    protected void doDrawing(boolean fill, boolean stroke, boolean nonzero) 
                throws IOException {
        if (fill) {
            if (stroke) {
                if (!nonzero) {
                    gen.writeln("stroke");
                } else {
                    gen.writeln("stroke");
                }
            } else {
                if (!nonzero) {
                    gen.writeln("fill");
                } else {
                    gen.writeln("fill");
                }
            }
        } else {
            // if(stroke)
            gen.writeln("stroke");
        }
    }

    /**
     * Returns the device configuration associated with this
     * <code>Graphics2D</code>.
     * @return the device configuration
     */
    public GraphicsConfiguration getDeviceConfiguration() {
        // System.out.println("getDeviceConviguration");
        return GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration();
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
     * Gets the font metrics for the specified font.
     * @return    the font metrics for the specified font.
     * @param     f the specified font
     * @see       java.awt.Graphics#getFont
     * @see       java.awt.FontMetrics
     * @see       java.awt.Graphics#getFontMetrics()
     */
    public java.awt.FontMetrics getFontMetrics(Font f) {
        return fmg.getFontMetrics(f);
    }

    /**
     * Sets the paint mode of this graphics context to alternate between
     * this graphics context's current color and the new specified color.
     * This specifies that logical pixel operations are performed in the
     * XOR mode, which alternates pixels between the current color and
     * a specified XOR color.
     * <p>
     * When drawing operations are performed, pixels which are the
     * current color are changed to the specified color, and vice versa.
     * <p>
     * Pixels that are of colors other than those two colors are changed
     * in an unpredictable but reversible manner; if the same figure is
     * drawn twice, then all pixels are restored to their original values.
     * @param     c1 the XOR alternation color
     */
    public void setXORMode(Color c1) {
        System.out.println("setXORMode");
    }


    /**
     * Copies an area of the component by a distance specified by
     * <code>dx</code> and <code>dy</code>. From the point specified
     * by <code>x</code> and <code>y</code>, this method
     * copies downwards and to the right.  To copy an area of the
     * component to the left or upwards, specify a negative value for
     * <code>dx</code> or <code>dy</code>.
     * If a portion of the source rectangle lies outside the bounds
     * of the component, or is obscured by another window or component,
     * <code>copyArea</code> will be unable to copy the associated
     * pixels. The area that is omitted can be refreshed by calling
     * the component's <code>paint</code> method.
     * @param       x the <i>x</i> coordinate of the source rectangle.
     * @param       y the <i>y</i> coordinate of the source rectangle.
     * @param       width the width of the source rectangle.
     * @param       height the height of the source rectangle.
     * @param       dx the horizontal distance to copy the pixels.
     * @param       dy the vertical distance to copy the pixels.
     */
    public void copyArea(int x, int y, int width, int height, int dx,
                         int dy) {
        System.out.println("copyArea");
    }

}
