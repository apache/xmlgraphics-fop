/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

package org.apache.fop.render.ps;

//Java
import java.text.AttributedCharacterIterator;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
/* java.awt.Font is not imported to avoid confusion with 
      org.apache.fop.fonts.Font */ 
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
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.IOException;

//Batik
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.ext.awt.g2d.AbstractGraphics2D;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//FOP
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.image.FopImage;

/**
 * This concrete implementation of <tt>AbstractGraphics2D</tt> is a
 * simple help to programmers to get started with their own
 * implementation of <tt>Graphics2D</tt>.
 * <tt>DefaultGraphics2D</tt> implements all the abstract methods
 * is <tt>AbstractGraphics2D</tt> and makes it easy to start
 * implementing a <tt>Graphic2D</tt> piece-meal.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 * @version $Id: PSGraphics2D.java,v 1.11 2003/03/11 08:42:24 jeremias Exp $
 * @see org.apache.batik.ext.awt.g2d.AbstractGraphics2D
 */
public class PSGraphics2D extends AbstractGraphics2D {

    /** the logger for this class */
    protected Log log = LogFactory.getLog(PSTextPainter.class);

    /** the PostScript generator being created */
    protected PSGenerator gen;

    private boolean clippingDisabled = true;

    /** Currently valid FontState */
    protected Font font;
    
    /** Overriding FontState */
    protected Font overrideFont = null;
    
    /** the current (internal) font name */
    protected String currentFontName;

    /** the current font size in millipoints */
    protected int currentFontSize;

    /**
     * the current colour for use in svg
     */
    protected Color currentColour = new Color(0, 0, 0);

    /** FontInfo containing all available fonts */
    protected FontInfo fontInfo;

    /**
     * Create a new Graphics2D that generates PostScript code.
     * @param textAsShapes True if text should be rendered as graphics
     * @see org.apache.batik.ext.awt.g2d.AbstractGraphics2D#AbstractGraphics2D(boolean)
     */
    public PSGraphics2D(boolean textAsShapes) {
        super(textAsShapes);
    }

    /**
     * Create a new Graphics2D that generates PostScript code.
     * @param textAsShapes True if text should be rendered as graphics
     * @param gen PostScript generator to use for output
     * @see org.apache.batik.ext.awt.g2d.AbstractGraphics2D#AbstractGraphics2D(boolean)
     */
    public PSGraphics2D(boolean textAsShapes, PSGenerator gen) {
        this(textAsShapes);
        setPSGenerator(gen);
    }

    /**
     * Constructor for creating copies
     * @param g parent PostScript Graphics2D
     */
    public PSGraphics2D(PSGraphics2D g) {
        super(g);
    }

    /**
     * Sets the PostScript generator
     * @param gen the PostScript generator
     */
    public void setPSGenerator(PSGenerator gen) {
        this.gen = gen;
    }

    /**
     * Sets the GraphicContext
     * @param c GraphicContext to use
     */
    public void setGraphicContext(GraphicContext c) {
        gc = c;
        setPrivateHints();
    }

    private void setPrivateHints() {
        setRenderingHint(RenderingHintsKeyExt.KEY_AVOID_TILE_PAINTING, 
                RenderingHintsKeyExt.VALUE_AVOID_TILE_PAINTING_ON);
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
     * Return the font information associated with this object
     * @return the FontInfo object
     */
    public FontInfo getFontInfo() {
        return fontInfo;
    }

    /**
     * Central handler for IOExceptions for this class.
     * @param ioe IOException to handle
     */
    protected void handleIOException(IOException ioe) {
        ioe.printStackTrace();
    }

    /**
     * This method is used by AbstractPSDocumentGraphics2D to prepare a new page if
     * necessary.
     */
    protected void preparePainting() {
        //nop, used by AbstractPSDocumentGraphics2D
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
        preparePainting();
        log.debug("drawImage: " + x + ", " + y + " " + img.getClass().getName());

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

        try {
            FopImage fopimg = new TempImage(width, height, result, null);
            AffineTransform at = getTransform();
            gen.saveGraphicsState();
            Shape imclip = getClip();
            writeClip(imclip);
            gen.concatMatrix(at);
            PSImageUtils.renderFopImage(fopimg, 
                1000 * x, 1000 * y, 1000 * width, 1000 * height, gen);
            gen.restoreGraphicsState();
        } catch (IOException ioe) {
            handleIOException(ioe);
        }

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


    class TempImage implements FopImage {
        private int height;
        private int width;
        private int bitsPerPixel;
        private ColorSpace colorSpace;
        private int bitmapSiye;
        private byte[] bitmaps;
        private byte[] mask;
        private Color transparentColor;

        TempImage(int width, int height, byte[] bitmaps,
                  byte[] mask) {
            this.height = height;
            this.width = width;
            this.bitsPerPixel = 8;
            this.colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            this.bitmaps = bitmaps;
            this.mask = mask;
        }

        public String getMimeType() {
            return "application/octet-stream";
        }

        /**
         * @see org.apache.fop.image.FopImage#load(int, org.apache.commons.logging.Log)
         */
        public boolean load(int type) {
            switch (type) {
                case FopImage.DIMENSIONS: break;
                case FopImage.BITMAP: break;
                case FopImage.ORIGINAL_DATA: break;
                default: throw new RuntimeException("Unknown load type: " + type);
            }
            return true;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }

        public ColorSpace getColorSpace() {
            return this.colorSpace;
        }

        public ICC_Profile getICCProfile() {
            return null;
        }

        public int getBitsPerPixel() {
            return this.bitsPerPixel;
        }

        // For transparent images
        public boolean isTransparent() {
            return getTransparentColor() != null;
        }

        public Color getTransparentColor() {
            return this.transparentColor;
        }

        public boolean hasSoftMask() {
            return this.mask != null;
        }

        public byte[] getSoftMask() {
            return this.mask;
        }

        public byte[] getBitmaps() {
            return this.bitmaps;
        }

        // width * (bitsPerPixel / 8) * height, no ?
        public int getBitmapsSize() {
            return getWidth() * getHeight() * 3; //Assumes RGB!
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

        /** @see org.apache.fop.image.FopImage#getIntrinsicWidth() */
        public int getIntrinsicWidth() {
            return (int)(getWidth() * 72000 / getHorizontalResolution());
        }

        /** @see org.apache.fop.image.FopImage#getIntrinsicHeight() */
        public int getIntrinsicHeight() {
            return (int)(getHeight() * 72000 / getVerticalResolution());
        }

        /** @see org.apache.fop.image.FopImage#getHorizontalResolution() */
        public double getHorizontalResolution() {
            return 72;
        }

        /** @see org.apache.fop.image.FopImage#getVerticalResolution() */
        public double getVerticalResolution() {
            return 72;
        }

    }


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
        preparePainting();
        log.warn("NYI: drawImage");
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
        this.gen = null;
        this.font = null;
        this.currentColour = null;
        this.fontInfo = null;
    }

    /**
     * Processes a path iterator generating the nexessary painting operations.
     * @param iter PathIterator to process
     * @throws IOException In case of an I/O problem.
     */
    public void processPathIterator(PathIterator iter) throws IOException {
        double[] vals = new double[6];
        while (!iter.isDone()) {
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
                gen.writeln(gen.formatDouble(1000 * vals[0]) + " " 
                          + gen.formatDouble(1000 * vals[1]) + " " 
                          + gen.formatDouble(1000 * vals[2]) + " " 
                          + gen.formatDouble(1000 * vals[3]) + " QUADTO ");
                break;
            case PathIterator.SEG_CLOSE:
                gen.writeln("closepath");
                break;
            default:
                break;
            }
            iter.next();
        }
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
        preparePainting();
        try {
            gen.saveGraphicsState();
            Shape imclip = getClip();
            writeClip(imclip);
            establishColor(getColor());

            applyPaint(getPaint(), false);
            applyStroke(getStroke());

            gen.writeln("newpath");
            PathIterator iter = s.getPathIterator(getTransform());
            processPathIterator(iter);
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
        if (s == null) {
            return;
        }
        if (!this.clippingDisabled) {
            preparePainting();
            try {
                gen.writeln("newpath");
                PathIterator iter = s.getPathIterator(getTransform());
                processPathIterator(iter);
                // clip area
                gen.writeln("clippath");
            } catch (IOException ioe) {
                handleIOException(ioe);
            }
        }
    }

    /**
     * Applies a new Paint object.
     * @param paint Paint object to use
     * @param fill True if to be applied for filling
     */
    protected void applyPaint(Paint paint, boolean fill) {
        preparePainting();
        if (paint instanceof GradientPaint) {
            log.warn("NYI: Gradient paint");
        } else if (paint instanceof TexturePaint) {
            log.warn("NYI: texture paint");
        }
    }

    /**
     * Applies a new Stroke object.
     * @param stroke Stroke object to use
     */
    protected void applyStroke(Stroke stroke) {
        preparePainting();
        try {
            if (stroke instanceof BasicStroke) {
                BasicStroke bs = (BasicStroke)stroke;

                float[] da = bs.getDashArray();
                if (da != null) {
                    gen.write("[");
                    for (int count = 0; count < da.length; count++) {
                        gen.write("" + (1000 * (int)da[count]));
                        if (count < da.length - 1) {
                            gen.write(" ");
                        }
                    }
                    gen.write("] ");
                    float offset = bs.getDashPhase();
                    gen.writeln((1000 * (int)offset) + " setdash");
                }
                int ec = bs.getEndCap();
                switch (ec) {
                case BasicStroke.CAP_BUTT:
                    gen.writeln("0 setlinecap");
                    break;
                case BasicStroke.CAP_ROUND:
                    gen.writeln("1 setlinecap");
                    break;
                case BasicStroke.CAP_SQUARE:
                    gen.writeln("2 setlinecap");
                    break;
                default: log.warn("Unsupported line cap: " + ec);
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
                default: log.warn("Unsupported line join: " + lj);
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
        preparePainting();
        log.warn("NYI: drawRenderedImage");
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
        preparePainting();
        log.warn("NYI: drawRenderableImage");
    }

    /**
     * Establishes the given color in the PostScript interpreter.
     * @param c the color to set
     * @throws IOException In case of an I/O problem
     */
    protected void establishColor(Color c) throws IOException {
        StringBuffer p = new StringBuffer();
        float[] comps = c.getColorComponents(null);
        
        if (c.getColorSpace().getType() == ColorSpace.TYPE_RGB) {
            // according to pdfspec 12.1 p.399
            // if the colors are the same then just use the g or G operator
            boolean same = (comps[0] == comps[1] 
                        && comps[0] == comps[2]);
            // output RGB
            if (same) {
                p.append(gen.formatDouble(comps[0]));
            } else {
                for (int i = 0; i < c.getColorSpace().getNumComponents(); i++) {
                    if (i > 0) {
                        p.append(" ");
                    }
                    p.append(gen.formatDouble(comps[i]));
                }
            }
            if (same) {
                p.append(" setgray");
            } else {
                p.append(" setrgbcolor");
            }
        } else if (c.getColorSpace().getType() == ColorSpace.TYPE_CMYK) {
            // colorspace is CMYK
            for (int i = 0; i < c.getColorSpace().getNumComponents(); i++) {
                if (i > 0) {
                    p.append(" ");
                }
                p.append(gen.formatDouble(comps[i]));
            }
            p.append(" setcmykcolor");
        } else {
            // means we're in DeviceGray or Unknown.
            // assume we're in DeviceGray, because otherwise we're screwed.
            p.append(gen.formatDouble(comps[0]));
            p.append(" setgray");
        }
        gen.writeln(p.toString());
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
        if (this.textAsShapes) {
            drawStringAsShapes(s, x, y);
        } else {
            drawStringAsText(s, x, y);
        }
    }

    /**
     * Draw a string to the PostScript document. The text is painted as shapes.
     * @param s the string to draw
     * @param x the x position
     * @param y the y position
     */
    public void drawStringAsShapes(String s, float x, float y) {
        java.awt.Font awtFont = super.getFont();
        FontRenderContext frc = super.getFontRenderContext();
        GlyphVector gv = awtFont.createGlyphVector(frc, s);
        Shape glyphOutline = gv.getOutline(x, y);
        fill(glyphOutline);
    }

    /**
     * Draw a string to the PostScript document. The text is painted using 
     * text operations.
     * @param s the string to draw
     * @param x the x position
     * @param y the y position
     */
    public void drawStringAsText(String s, float x, float y) {
        preparePainting();
        log.trace("drawString('" + s + "', " + x + ", " + y + ")");
        try {
            if (this.overrideFont == null) {
                java.awt.Font awtFont = getFont();
                this.font = createFont(awtFont);
            } else {
                this.font = this.overrideFont;
                this.overrideFont = null;
            }
            
            //Color and Font state
            establishColor(getColor());
            establishCurrentFont();

            //Clip
            Shape imclip = getClip();
            writeClip(imclip);

            gen.saveGraphicsState();

            //Prepare correct transformation
            AffineTransform trans = getTransform();
            gen.writeln("[" + toArray(trans) + "] concat"); 
            gen.writeln(gen.formatDouble(1000 * x) + " "
                      + gen.formatDouble(1000 * y) + " moveto ");
            gen.writeln("1 -1 scale");
      
            StringBuffer sb = new StringBuffer("(");
            escapeText(s, sb);
            sb.append(") t ");
    
            gen.writeln(sb.toString());
            
            gen.restoreGraphicsState();        
        } catch (IOException ioe) {
            handleIOException(ioe);
        }
    }

    /**
     * Converts an AffineTransform to a value array.
     * @param at AffineTransform to convert
     * @return a String (array of six space-separated values)
     */
    protected String toArray(AffineTransform at) {
        final double[] vals = new double[6];
        at.getMatrix(vals);
        return gen.formatDouble5(vals[0]) + " " 
                + gen.formatDouble5(vals[1]) + " " 
                + gen.formatDouble5(vals[2]) + " "   
                + gen.formatDouble5(vals[3]) + " "   
                + gen.formatDouble(1000 * vals[4]) + " "   
                + gen.formatDouble(1000 * vals[5]); 
    }

    private void escapeText(final String text, StringBuffer target) {
        final int l = text.length();
        for (int i = 0; i < l; i++) {
            final char ch = text.charAt(i);
            final char mch = this.font.mapChar(ch);
            PSGenerator.escapeChar(mch, target);
        }
    }

    private Font createFont(java.awt.Font f) {
        String fontFamily = f.getFamily();
        if (fontFamily.equals("sanserif")) {
            fontFamily = "sans-serif";
        }
        int fontSize = 1000 * f.getSize();
        String style = f.isItalic() ? "italic" : "normal";
        int weight = f.isBold() ? Font.BOLD : Font.NORMAL;
                
        String fontKey = fontInfo.findAdjustWeight(fontFamily, style, weight);
        if (fontKey == null) {
            fontKey = fontInfo.findAdjustWeight("sans-serif", style, weight);
        }
        return new Font(fontKey, fontInfo.getMetricsFor(fontKey), fontSize);
    }

    private void establishCurrentFont() throws IOException {
        if ((currentFontName != this.font.getFontName()) 
                || (currentFontSize != this.font.getFontSize())) {
            gen.writeln(this.font.getFontName() + " " + this.font.getFontSize() + " F");
            currentFontName = this.font.getFontName();
            currentFontSize = this.font.getFontSize();
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
        preparePainting();
        log.warn("NYI: drawString(AttributedCharacterIterator)");
        /*
        try {
            gen.writeln("BT");
            Shape imclip = getClip();
            writeClip(imclip);
            establishColor(getColor());

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
        }*/
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
        preparePainting();
        try {
            gen.saveGraphicsState();
            Shape imclip = getClip();
            writeClip(imclip);
            establishColor(getColor());

            applyPaint(getPaint(), true);

            gen.writeln("newpath");
            PathIterator iter = s.getPathIterator(getTransform());
            processPathIterator(iter);
            doDrawing(true, false,
                      iter.getWindingRule() == PathIterator.WIND_EVEN_ODD);
            gen.restoreGraphicsState();
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
        preparePainting();
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
     * Sets the overriding font.
     * @param font Font to set
     */
    public void setOverrideFont(Font font) {
        this.overrideFont = font;
    }
    
    /**
     * Gets the font metrics for the specified font.
     * @return    the font metrics for the specified font.
     * @param     f the specified font
     * @see       java.awt.Graphics#getFont
     * @see       java.awt.FontMetrics
     * @see       java.awt.Graphics#getFontMetrics()
     */
    public java.awt.FontMetrics getFontMetrics(java.awt.Font f) {
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
        log.warn("NYI: setXORMode");
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
        log.warn("NYI: copyArea");
    }

    /* --- for debugging
    public void transform(AffineTransform tx) {
        System.out.println("transform(" + toArray(tx) + ")");
        super.transform(zx);
    }

    public void scale(double sx, double sy) {
        System.out.println("scale(" + sx + ", " + sy + ")");
        super.scale(sx, sy);
    }

    public void translate(double tx, double ty) {
        System.out.println("translate(double " + tx + ", " + ty + ")");
        super.translate(tx, ty);
    }

    public void translate(int tx, int ty) {
        System.out.println("translate(int " + tx + ", " + ty + ")");
        super.translate(tx, ty);
    }
    */

}
