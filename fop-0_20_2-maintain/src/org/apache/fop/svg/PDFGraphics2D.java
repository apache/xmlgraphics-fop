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
package org.apache.fop.svg;

import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.image.JpegImage;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.FopImageException;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.LinkSet;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFAnnotList;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFFilter;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.pdf.PDFFunction;
import org.apache.fop.pdf.PDFPattern;
import org.apache.fop.render.pdf.CIDFont;
import org.apache.fop.render.pdf.fonts.LazyFont;

import org.apache.batik.ext.awt.g2d.AbstractGraphics2D;
import org.apache.batik.ext.awt.g2d.GraphicContext;

import java.awt.Graphics;
import java.awt.Shape;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.GraphicsConfiguration;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.GraphicsDevice;
import java.awt.GraphicsConfigTemplate;
import java.awt.AlphaComposite;
import java.awt.GradientPaint;
import java.awt.TexturePaint;
import java.awt.BasicStroke;
import java.awt.Transparency;
import java.awt.Font;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.renderable.RenderableImage;
import java.io.StringWriter;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.List;
import java.util.Map;

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
public class PDFGraphics2D extends AbstractGraphics2D {
    boolean standalone = false;

    /**
     * the PDF Document being created
     */
    protected PDFDocument pdfDoc;

    /**
     * the current annotation list to add annotations to
     */
    PDFAnnotList currentAnnotList = null;

    protected FontState fontState;
    protected FontState ovFontState = null;

    /**
     * the current stream to add PDF commands to
     */
    StringWriter currentStream = new StringWriter();

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
    PDFColor currentColour = new PDFColor(0, 0, 0);

    /**
     * A registry of images that have already been drawn. They are mapped to
     * a structure with the PDF xObjectNum, width and height. This
     * prevents multiple copies from being stored, which can greatly
     * reduce the size of a PDF graphic that uses the same image over and over
     * (e.g. graphic bullets, map icons, etc.).
     */
    private Map imageInfos = new java.util.HashMap();

    private static class ImageInfo {
        public int width;
        public int height;
        public int xObjectNum;
    }

    /**
     * Create a new PDFGraphics2D with the given pdf document info.
     * This is used to create a Graphics object for use inside an already
     * existing document.
     */
    public PDFGraphics2D(boolean textAsShapes, FontState fs, PDFDocument doc,
                         String font, int size, int xpos, int ypos) {
        super(textAsShapes);
        pdfDoc = doc;
        currentFontName = font;
        currentFontSize = size;
        currentYPosition = ypos;
        currentXPosition = xpos;
        fontState = fs;
    }

    public PDFGraphics2D(boolean textAsShapes) {
        super(textAsShapes);
    }

    public String getString() {
        return currentStream.toString();
    }

    public void setGraphicContext(GraphicContext c) {
        gc = c;
    }

    public void setOverrideFontState(FontState infont) {
        ovFontState = infont;
    }

    /**
     * This constructor supports the create method
     */
    public PDFGraphics2D(PDFGraphics2D g) {
        super(g);
    }

    /**
     * Creates a new <code>Graphics</code> object that is
     * a copy of this <code>Graphics</code> object.
     * @return     a new graphics context that is a copy of
     * this graphics context.
     */
    public Graphics create() {
        return new PDFGraphics2D(this);
    }

    /**
     * This is a pdf specific method used to add a link to the
     * pdf document.
     */
    public void addLink(Shape bounds, AffineTransform trans, String dest, int linkType) {
        if(currentAnnotList == null) {
            currentAnnotList = pdfDoc.makeAnnotList();
        }
        AffineTransform at = getTransform();
        Shape b = at.createTransformedShape(bounds);
        b = trans.createTransformedShape(b);
        Rectangle rect = b.getBounds();
        // this handles the / 1000 in PDFLink
        rect.x = rect.x * 1000;
        rect.y = rect.y * 1000;
        rect.height = -rect.height * 1000;
        rect.width = rect.width * 1000;
        if(linkType != LinkSet.EXTERNAL) {
            String pdfdest = "/XYZ " + dest;
            currentAnnotList.addLink(pdfDoc.makeLinkCurrentPage(rect, pdfdest));
        } else {
            currentAnnotList.addLink(pdfDoc.makeLink(rect,
                                                 dest, linkType));
        }
    }

    public PDFAnnotList getAnnotList() {
        return currentAnnotList;
    }

    public void addJpegImage(JpegImage jpeg, float x, float y, float width, float height) {
        int xObjectNum = this.pdfDoc.addImage(jpeg);

        AffineTransform at = getTransform();
        double[] matrix = new double[6];
        at.getMatrix(matrix);
        currentStream.write("q\n");
        Shape imclip = getClip();
        writeClip(imclip);
        currentStream.write("" + matrix[0] + " " + matrix[1] + " "
                            + matrix[2] + " " + matrix[3] + " "
                            + matrix[4] + " " + matrix[5] + " cm\n");

        currentStream.write("" + width + " 0 0 "
                          + (-height) + " "
                          + x + " "
                          + (y + height) + " cm\n" + "/Im"
                          + xObjectNum + " Do\nQ\n");
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
     * @see      java.awt.Image
     * @see      java.awt.image.ImageObserver
     * @see      java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     */
    public boolean drawImage(Image img, int x, int y,
                             ImageObserver observer) {
        // System.err.println("drawImage:x, y");

        // first we look to see if we've already added this image to
        // the pdf document. If so, we just reuse the reference;
        // otherwise we have to build a FopImage and add it to the pdf
        // document
        ImageInfo imageInfo = (ImageInfo)imageInfos.get(img);
        if (imageInfo == null) {
            // OK, have to build and add a PDF image
            imageInfo = new ImageInfo();
            imageInfo.width = img.getWidth(observer);
            imageInfo.height = img.getHeight(observer);

            if (imageInfo.width == -1 || imageInfo.height == -1) {
                return false;
            }

            Dimension size = new Dimension(imageInfo.width * 3, imageInfo.height * 3);
            BufferedImage buf = buildBufferedImage(size);

            java.awt.Graphics2D g = buf.createGraphics();
            g.setComposite(AlphaComposite.SrcOver);
            g.setBackground(new Color(1, 1, 1, 0));
            g.setPaint(new Color(1, 1, 1, 0));
            g.fillRect(0, 0, imageInfo.width * 3, imageInfo.height * 3);
            g.clip(new Rectangle(0, 0, buf.getWidth(), buf.getHeight()));

            if (!g.drawImage(img, 0, 0, buf.getWidth(), buf.getHeight(), observer)) {
                return false;
            }
            g.dispose();

            final byte[] result = new byte[buf.getWidth() * buf.getHeight() * 3];
            final byte[] mask = new byte[buf.getWidth() * buf.getHeight()];

            Raster raster = buf.getData();
            DataBuffer bd = raster.getDataBuffer();

            int count = 0;
            int maskpos = 0;
            int[] iarray;
            int i, j, val, alpha, add, mult;
            switch (bd.getDataType()) {
            case DataBuffer.TYPE_INT:
                int[][] idata = ((DataBufferInt)bd).getBankData();
                for (i = 0; i < idata.length; i++) {
                    iarray = idata[i];
                    for (j = 0; j < iarray.length; j++) {
                        val = iarray[j];
                        alpha = val >>> 24;
                        // mask[maskpos++] = (byte)((idata[i][j] >> 24) & 0xFF);
                        if (alpha != 255) {
                            // System.out.println("Alpha: " + alpha);
                            // Composite with opaque white...
                            add = (255 - alpha);
                            mult = (alpha << 16) / 255;
                            result[count++] =
                                (byte)(add
                                       + ((((val >> 16) & 0xFF) * mult) >> 16));
                            result[count++] =
                                (byte)(add
                                       + ((((val >> 8) & 0xFF) * mult) >> 16));
                            result[count++] = (byte)(add
                                                     + ((((val) & 0xFF) * mult)
                                                        >> 16));
                        } else {
                            result[count++] = (byte)((val >> 16) & 0xFF);
                            result[count++] = (byte)((val >> 8) & 0xFF);
                            result[count++] = (byte)((val) & 0xFF);
                        }
                    }
                }
                break;
            default:
                // error
                break;
                }

            try {
                FopImage fopimg = new TempImage("TempImage:" + img.toString(), buf.getWidth(), buf.getHeight(), result, mask);
                imageInfo.xObjectNum = this.pdfDoc.addImage(fopimg);
                imageInfos.put(img, imageInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // now do any transformation required and add the actual image
        // placement instance
        AffineTransform at = getTransform();
        double[] matrix = new double[6];
        at.getMatrix(matrix);
        currentStream.write("q\n");
        Shape imclip = getClip();
        writeClip(imclip);
        currentStream.write("" + matrix[0] + " " + matrix[1] + " "
                            + matrix[2] + " " + matrix[3] + " "
                            + matrix[4] + " " + matrix[5] + " cm\n");
        currentStream.write("" + imageInfo.width + " 0 0 " + (-imageInfo.height) + " " + x
                            + " " + (y + imageInfo.height) + " cm\n" + "/Im"
                            + imageInfo.xObjectNum + " Do\nQ\n");
        return true;
    }

    public BufferedImage buildBufferedImage(Dimension size) {
        return new BufferedImage(size.width, size.height,
                                 BufferedImage.TYPE_INT_ARGB);
    }

    class TempImage implements FopImage {
        int m_height;
        int m_width;
        int m_bitsPerPixel;
        ColorSpace m_colorSpace;
        int m_bitmapSiye;
        byte[] m_bitmaps;
        byte[] m_mask;
        PDFColor transparent = new PDFColor(255, 255, 255);
        String url;

        TempImage(String url, int width, int height, byte[] result,
                  byte[] mask) throws FopImageException {
            this.url = url;
            this.m_height = height;
            this.m_width = width;
            this.m_bitsPerPixel = 8;
            this.m_colorSpace = new ColorSpace(ColorSpace.DEVICE_RGB);
            // this.m_isTransparent = false;
            // this.m_bitmapsSize = this.m_width * this.m_height * 3;
            this.m_bitmaps = result;
            this.m_mask = mask;
        }

        public boolean invertImage() {
            return false;
        }

        public String getURL() {
            return url;
        }

        // image size
        public int getWidth() throws FopImageException {
            return m_width;
        }

        public int getHeight() throws FopImageException {
            return m_height;
        }

        // DeviceGray, DeviceRGB, or DeviceCMYK
        public ColorSpace getColorSpace() throws FopImageException {
            return m_colorSpace;
        }

        // bits per pixel
        public int getBitsPerPixel() throws FopImageException {
            return m_bitsPerPixel;
        }

        // For transparent images
        public boolean isTransparent() throws FopImageException {
            return transparent != null;
        }

        public PDFColor getTransparentColor() throws FopImageException {
            return transparent;
        }

        public byte[] getMask() throws FopImageException {
            return m_mask;
        }

        // get the image bytes, and bytes properties

        // get uncompressed image bytes
        public byte[] getBitmaps() throws FopImageException {
            return m_bitmaps;
        }

        // width * (bitsPerPixel / 8) * height, no ?
        public int getBitmapsSize() throws FopImageException {
            return m_width * m_height * 3;
        }

        // get compressed image bytes
        // I don't know if we really need it, nor if it
        // should be changed...
        public byte[] getRessourceBytes() throws FopImageException {
            return null;
        }

        public int getRessourceBytesSize() throws FopImageException {
            return 0;
        }

        // return null if no corresponding PDFFilter
        public PDFFilter getPDFFilter() throws FopImageException {
            return null;
        }

        // release memory
        public void close() {}

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
        pdfDoc = null;
        fontState = null;
        currentStream = null;
        currentFontName = null;
        currentColour = null;
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
        // System.out.println("draw(Shape)");
        Color c;
        c = getColor();
        if(c.getAlpha() == 0) {
            return;
        }

        currentStream.write("q\n");
        Shape imclip = getClip();
        writeClip(imclip);
        applyColor(c, false);

        applyPaint(getPaint(), false);
        applyStroke(getStroke());

        AffineTransform trans = getTransform();
        double[] tranvals = new double[6];
        trans.getMatrix(tranvals);
        currentStream.write(PDFNumber.doubleOut(tranvals[0], 8) + " "
                            + PDFNumber.doubleOut(tranvals[1], 8) + " "
                            + PDFNumber.doubleOut(tranvals[2], 8) + " "
                            + PDFNumber.doubleOut(tranvals[3], 8) + " "
                            + PDFNumber.doubleOut(tranvals[4], 8) + " "
                            + PDFNumber.doubleOut(tranvals[5], 8) + " cm\n");

        PathIterator iter = s.getPathIterator(new AffineTransform());
        while (!iter.isDone()) {
            double vals[] = new double[6];
            int type = iter.currentSegment(vals);
            switch (type) {
            case PathIterator.SEG_CUBICTO:
                currentStream.write(PDFNumber.doubleOut(vals[0]) + " "
                                    + PDFNumber.doubleOut(vals[1]) + " "
                                    + PDFNumber.doubleOut(vals[2]) + " "
                                    + PDFNumber.doubleOut(vals[3]) + " "
                                    + PDFNumber.doubleOut(vals[4]) + " "
                                    + PDFNumber.doubleOut(vals[5]) + " c\n");
                break;
            case PathIterator.SEG_LINETO:
                currentStream.write(PDFNumber.doubleOut(vals[0]) + " "
                                    + PDFNumber.doubleOut(vals[1]) + " l\n");
                break;
            case PathIterator.SEG_MOVETO:
                currentStream.write(PDFNumber.doubleOut(vals[0]) + " "
                                    + PDFNumber.doubleOut(vals[1]) + " m\n");
                break;
            case PathIterator.SEG_QUADTO:
                currentStream.write(PDFNumber.doubleOut(vals[0]) + " "
                                    + PDFNumber.doubleOut(vals[1]) + " "
                                    + PDFNumber.doubleOut(vals[2]) + " "
                                    + PDFNumber.doubleOut(vals[3]) + " y\n");
                break;
            case PathIterator.SEG_CLOSE:
                currentStream.write("h\n");
                break;
            default:
                break;
            }
            iter.next();
        }
        doDrawing(false, true, false);
        currentStream.write("Q\n");
    }

    protected void writeClip(Shape s) {
        if (s == null) {
            return;
        }
        PathIterator iter = s.getPathIterator(getTransform());
        while (!iter.isDone()) {
            double vals[] = new double[6];
            int type = iter.currentSegment(vals);
            switch (type) {
            case PathIterator.SEG_CUBICTO:
                currentStream.write(PDFNumber.doubleOut(vals[0]) + " "
                                    + PDFNumber.doubleOut(vals[1]) + " "
                                    + PDFNumber.doubleOut(vals[2]) + " "
                                    + PDFNumber.doubleOut(vals[3]) + " "
                                    + PDFNumber.doubleOut(vals[4]) + " "
                                    + PDFNumber.doubleOut(vals[5]) + " c\n");
                break;
            case PathIterator.SEG_LINETO:
                currentStream.write(PDFNumber.doubleOut(vals[0]) + " "
                                    + PDFNumber.doubleOut(vals[1]) + " l\n");
                break;
            case PathIterator.SEG_MOVETO:
                currentStream.write(PDFNumber.doubleOut(vals[0]) + " "
                                    + PDFNumber.doubleOut(vals[1]) + " m\n");
                break;
            case PathIterator.SEG_QUADTO:
                currentStream.write(PDFNumber.doubleOut(vals[0]) + " "
                                    + PDFNumber.doubleOut(vals[1]) + " "
                                    + PDFNumber.doubleOut(vals[2]) + " "
                                    + PDFNumber.doubleOut(vals[3]) + " y\n");
                break;
            case PathIterator.SEG_CLOSE:
                currentStream.write("h\n");
                break;
            default:
                break;
            }
            iter.next();
        }
        // clip area
        currentStream.write("W\n");
        currentStream.write("n\n");
    }

    protected void applyColor(Color col, boolean fill) {
        Color c = col;
        if (c.getColorSpace().getType()
                == java.awt.color.ColorSpace.TYPE_RGB) {
            currentColour = new PDFColor(c.getRed(), c.getGreen(),
                                         c.getBlue());
            currentStream.write(currentColour.getColorSpaceOut(fill));
        } else if (c.getColorSpace().getType()
                   == java.awt.color.ColorSpace.TYPE_CMYK) {
            float[] cComps = c.getColorComponents(new float[3]);
            double[] cmyk = new double[3];
            for (int i = 0; i < 3; i++) {
                // convert the float elements to doubles for pdf
                cmyk[i] = cComps[i];
            }
            currentColour = new PDFColor(cmyk[0], cmyk[1], cmyk[2], cmyk[3]);
            currentStream.write(currentColour.getColorSpaceOut(fill));
        } else if (c.getColorSpace().getType()
                   == java.awt.color.ColorSpace.TYPE_2CLR) {
            // used for black/magenta
            float[] cComps = c.getColorComponents(new float[1]);
            double[] blackMagenta = new double[1];
            for (int i = 0; i < 1; i++) {
                blackMagenta[i] = cComps[i];
            }
            // currentColour = new PDFColor(blackMagenta[0], blackMagenta[1]);
            currentStream.write(currentColour.getColorSpaceOut(fill));
        } else {
            System.err.println("Color Space not supported by PDFGraphics2D");
        }
    }

    protected void applyPaint(Paint paint, boolean fill) {
        if (paint instanceof GradientPaint) {
            GradientPaint gp = (GradientPaint)paint;
            Color c1 = gp.getColor1();
            Color c2 = gp.getColor2();
            Point2D p1 = gp.getPoint1();
            Point2D p2 = gp.getPoint2();
            boolean cyclic = gp.isCyclic();

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

            List theFunctions = new java.util.ArrayList();

            List someColors = new java.util.ArrayList();

            PDFColor color1 = new PDFColor(c1.getRed(), c1.getGreen(),
                                           c1.getBlue());
            someColors.add(color1);
            PDFColor color2 = new PDFColor(c2.getRed(), c2.getGreen(),
                                           c2.getBlue());
            someColors.add(color2);

            PDFFunction myfunc = this.pdfDoc.makeFunction(2, theDomain, null,
                    color1.getVector(), color2.getVector(), 1.0);

            ColorSpace aColorSpace = new ColorSpace(ColorSpace.DEVICE_RGB);
            PDFPattern myPat = this.pdfDoc.createGradient(false, aColorSpace,
                    someColors, null, theCoords);
            currentStream.write(myPat.getColorSpaceOut(fill));

        } else if (paint instanceof TexturePaint) {}
    }

    protected void applyStroke(Stroke stroke) {
        if (stroke instanceof BasicStroke) {
            BasicStroke bs = (BasicStroke)stroke;

            float[] da = bs.getDashArray();
            if (da != null) {
                currentStream.write("[");
                for (int count = 0; count < da.length; count++) {
                    if(((int)da[count]) == 0) {
                        // the dasharray units in pdf are (whole) numbers
                        // in user space units, cannot be 0
                        currentStream.write("1");
                    } else {
                        currentStream.write("" + ((int)da[count]));
                    }
                    if (count < da.length - 1) {
                        currentStream.write(" ");
                    }
                }
                currentStream.write("] ");
                float offset = bs.getDashPhase();
                currentStream.write(((int)offset) + " d\n");
            }
            int ec = bs.getEndCap();
            switch (ec) {
            case BasicStroke.CAP_BUTT:
                currentStream.write(0 + " J\n");
                break;
            case BasicStroke.CAP_ROUND:
                currentStream.write(1 + " J\n");
                break;
            case BasicStroke.CAP_SQUARE:
                currentStream.write(2 + " J\n");
                break;
            }

            int lj = bs.getLineJoin();
            switch (lj) {
            case BasicStroke.JOIN_MITER:
                currentStream.write(0 + " j\n");
                break;
            case BasicStroke.JOIN_ROUND:
                currentStream.write(1 + " j\n");
                break;
            case BasicStroke.JOIN_BEVEL:
                currentStream.write(2 + " j\n");
                break;
            }
            float lw = bs.getLineWidth();
            currentStream.write(PDFNumber.doubleOut(lw) + " w\n");

            float ml = bs.getMiterLimit();
            currentStream.write(PDFNumber.doubleOut(ml) + " M\n");
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
     * @param x,&nbsp;y the coordinates where the <code>String</code>
     * should be rendered
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see java.awt.Graphics#setFont
     * @see #setTransform
     * @see #setComposite
     * @see #setClip
     */
    public void drawString(String s, float x, float y) {
        // System.out.println("drawString(String)");

        if(ovFontState == null) {
            Font gFont = getFont();
            String n = gFont.getFamily();
            if (n.equals("sanserif")) {
                n = "sans-serif";
            }
            int siz = gFont.getSize();
            String style = gFont.isItalic() ? "italic" : "normal";
            String weight = gFont.isBold() ? "bold" : "normal";
            try {
                fontState = new FontState(fontState.getFontInfo(), n, style,
                                          weight, siz * 1000, 0);
            } catch (org.apache.fop.apps.FOPException fope) {
                fope.printStackTrace();
            }
        } else {
            fontState = ovFontState;
            ovFontState = null;
        }
        String name;
        int size;
        name = fontState.getFontName();
        size = fontState.getFontSize() / 1000;

        if ((!name.equals(this.currentFontName))
                || (size != this.currentFontSize)) {
            this.currentFontName = name;
            this.currentFontSize = size;
            currentStream.write("/" + name + " " + size + " Tf\n");

        }

        currentStream.write("q\n");

        Shape imclip = getClip();
        writeClip(imclip);
        Color c = getColor();
        applyColor(c, true);
        c = getBackground();
        applyColor(c, false);

        currentStream.write("BT\n");

        Map kerning = null;
        boolean kerningAvailable = false;

        kerning = fontState.getKerning();
        if (kerning != null &&!kerning.isEmpty()) {
            kerningAvailable = true;
        }

        // This assumes that *all* CIDFonts use a /ToUnicode mapping
        boolean useMultiByte = false;
        org.apache.fop.render.pdf.Font f =
            (org.apache.fop.render.pdf.Font)fontState.getFontInfo().getFonts().get(name);
        if (f instanceof LazyFont){
            if(((LazyFont) f).getRealFont() instanceof CIDFont){
                useMultiByte = true;
            }
        } else if (f instanceof CIDFont){
            useMultiByte = true;
        }

        // String startText = useMultiByte ? "<FEFF" : "(";
        String startText = useMultiByte ? "<" : "(";
        String endText = useMultiByte ? "> " : ") ";

        AffineTransform trans = getTransform();
        trans.translate(x, y);
        double[] vals = new double[6];
        trans.getMatrix(vals);

        currentStream.write(PDFNumber.doubleOut(vals[0], 8) + " "
                            + PDFNumber.doubleOut(vals[1], 8) + " "
                            + PDFNumber.doubleOut(vals[2], 8) + " "
                            + PDFNumber.doubleOut(vals[3], 8) + " "
                            + PDFNumber.doubleOut(vals[4], 8) + " "
                            + PDFNumber.doubleOut(vals[5], 8) + " cm\n");
        currentStream.write("1 0 0 -1 0 0 Tm [" + startText);

        int l = s.length();

        for (int i = 0; i < l; i++) {
            char ch = fontState.mapChar(s.charAt(i));

            if (!useMultiByte) {
                if (ch > 127) {
                    currentStream.write("\\");
                    currentStream.write(Integer.toOctalString((int)ch));
                } else {
                    switch (ch) {
                    case '(':
                    case ')':
                    case '\\':
                        currentStream.write("\\");
                        break;
                    }
                    currentStream.write(ch);
                }
            } else {
                currentStream.write(getUnicodeString(ch));
            }

            if (kerningAvailable && (i + 1) < l) {
                addKerning(currentStream, (new Integer((int)ch)),
                           (new Integer((int)fontState.mapChar(s.charAt(i + 1)))),
                           kerning, startText, endText);
            }

        }
        currentStream.write(endText);


        currentStream.write("] TJ\n");

        currentStream.write("ET\n");
        currentStream.write("Q\n");
    }

    private void addKerning(StringWriter buf, Integer ch1, Integer ch2,
                            Map kerning, String startText,
                            String endText) {
        Map kernPair = (Map)kerning.get(ch1);

        if (kernPair != null) {
            Integer width = (Integer)kernPair.get(ch2);
            if (width != null) {
                currentStream.write(endText + (-width.intValue()) + " " + startText);
            }
        }
    }

    /**
     * Convert a char to a multibyte hex representation
     */
    private String getUnicodeString(char c) {

        StringBuffer buf = new StringBuffer(4);
        byte[] uniBytes = null;
        try {
            char[] a = {
                c
            };
            uniBytes = new String(a).getBytes("UnicodeBigUnmarked");
        } catch (Exception e) {
            // This should never fail
        }

        for (int i = 0; i < uniBytes.length; i++) {
            int b = (uniBytes[i] < 0) ? (int)(256 + uniBytes[i])
                    : (int)uniBytes[i];

            String hexString = Integer.toHexString(b);
            if (hexString.length() == 1)
                buf = buf.append("0" + hexString);
            else
                buf = buf.append(hexString);
        }

        return buf.toString();
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
     * @param x,&nbsp;y the coordinates where the iterator's text is to be
     * rendered
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see #setTransform
     * @see #setComposite
     * @see #setClip
     */
    public void drawString(AttributedCharacterIterator iterator, float x,
                           float y) {
        System.err.println("drawString(AttributedCharacterIterator)");

        Shape imclip = getClip();
        writeClip(imclip);
        Color c = getColor();
        applyColor(c, true);
        c = getBackground();
        applyColor(c, false);

        currentStream.write("BT\n");

        AffineTransform trans = getTransform();
        trans.translate(x, y);
        double[] vals = new double[6];
        trans.getMatrix(vals);

        for (char ch = iterator.first(); ch != CharacterIterator.DONE;
                ch = iterator.next()) {
            Map attr = iterator.getAttributes();

            String name = fontState.getFontName();
            int size = fontState.getFontSize();
            if ((!name.equals(this.currentFontName))
                    || (size != this.currentFontSize)) {
                this.currentFontName = name;
                this.currentFontSize = size;
                currentStream.write("/" + name + " " + (size / 1000)
                                    + " Tf\n");

            }

            currentStream.write(PDFNumber.doubleOut(vals[0], 8) + " "
                                + PDFNumber.doubleOut(vals[1], 8) + " "
                                + PDFNumber.doubleOut(vals[2], 8) + " "
                                + PDFNumber.doubleOut(vals[3], 8) + " "
                                + PDFNumber.doubleOut(vals[4], 8) + " "
                                + PDFNumber.doubleOut(vals[5], 8) + " Tm (" + ch
                                + ") Tj\n");
        }

        currentStream.write("ET\n");
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
        // System.err.println("fill");
        Color c;
        c = getBackground();
        if(c.getAlpha() == 0) {
            c = getColor();
            if(c.getAlpha() == 0) {
                return;
            }
        }
        currentStream.write("q\n");
        Shape imclip = getClip();
        writeClip(imclip);
        c = getColor();
        applyColor(c, true);
        c = getBackground();
        applyColor(c, false);

        applyPaint(getPaint(), true);

        PathIterator iter = s.getPathIterator(getTransform());
        while (!iter.isDone()) {
            double vals[] = new double[6];
            int type = iter.currentSegment(vals);
            switch (type) {
            case PathIterator.SEG_CUBICTO:
                currentStream.write(PDFNumber.doubleOut(vals[0]) + " "
                                    + PDFNumber.doubleOut(vals[1]) + " "
                                    + PDFNumber.doubleOut(vals[2]) + " "
                                    + PDFNumber.doubleOut(vals[3]) + " "
                                    + PDFNumber.doubleOut(vals[4]) + " "
                                    + PDFNumber.doubleOut(vals[5]) + " c\n");
                break;
            case PathIterator.SEG_LINETO:
                currentStream.write(PDFNumber.doubleOut(vals[0]) + " "
                                    + PDFNumber.doubleOut(vals[1]) + " l\n");
                break;
            case PathIterator.SEG_MOVETO:
                currentStream.write(PDFNumber.doubleOut(vals[0]) + " "
                                    + PDFNumber.doubleOut(vals[1]) + " m\n");
                break;
            case PathIterator.SEG_QUADTO:
                currentStream.write(PDFNumber.doubleOut(vals[0]) + " "
                                    + PDFNumber.doubleOut(vals[1]) + " "
                                    + PDFNumber.doubleOut(vals[2]) + " "
                                    + PDFNumber.doubleOut(vals[3]) + " y\n");
                break;
            case PathIterator.SEG_CLOSE:
                currentStream.write("h\n");
                break;
            default:
                break;
            }
            iter.next();
        }
        doDrawing(true, false,
                  iter.getWindingRule() == PathIterator.WIND_EVEN_ODD);
        currentStream.write("Q\n");
    }

    protected void doDrawing(boolean fill, boolean stroke, boolean nonzero) {
        if (fill) {
            if (stroke) {
                if (nonzero)
                    currentStream.write("B*\n");
                else
                    currentStream.write("B\n");
            } else {
                if (nonzero)
                    currentStream.write("f*\n");
                else
                    currentStream.write("f\n");
            }
        } else {
            // if(stroke)
            currentStream.write("S\n");
        }
    }

    /**
     * Returns the device configuration associated with this
     * <code>Graphics2D</code>.
     */
    public GraphicsConfiguration getDeviceConfiguration() {
        return new PDFGraphicsConfiguration();
    }

    /**
     * Our implementation of the class that returns information about
     * roughly what we can handle and want to see (alpha for example).
     */
    static class PDFGraphicsConfiguration extends GraphicsConfiguration {
        // We use this to get a good colormodel..
        static BufferedImage BIWithAlpha = new BufferedImage(1, 1,
                BufferedImage.TYPE_INT_ARGB);
        // We use this to get a good colormodel..
        static BufferedImage BIWithOutAlpha = new BufferedImage(1, 1,
                BufferedImage.TYPE_INT_RGB);

        /**
         * Construct a buffered image with an alpha channel, unless
         * transparencty is OPAQUE (no alpha at all).
         */
        public BufferedImage createCompatibleImage(int width, int height,
                                                   int transparency) {
            if (transparency == Transparency.OPAQUE)
                return new BufferedImage(width, height,
                                         BufferedImage.TYPE_INT_RGB);
            else
                return new BufferedImage(width, height,
                                         BufferedImage.TYPE_INT_ARGB);
        }

        /**
         * Construct a buffered image with an alpha channel.
         */
        public BufferedImage createCompatibleImage(int width, int height) {
            return new BufferedImage(width, height,
                                     BufferedImage.TYPE_INT_ARGB);
        }

        /**
         * FIXX ME: This should return the page bounds in Pts,
         * I couldn't figure out how to get this for the current
         * page from the PDFDocument (this still works for now,
         * but it should be fixed...).
         */
        public Rectangle getBounds() {
System.out.println("getting getBounds");
            return null;
        }

        /**
         * Return a good default color model for this 'device'.
         */
        public ColorModel getColorModel() {
            return BIWithAlpha.getColorModel();
        }

        /**
         * Return a good color model given <tt>transparency</tt>
         */
        public ColorModel getColorModel(int transparency) {
            if (transparency == Transparency.OPAQUE)
                return BIWithOutAlpha.getColorModel();
            else
                return BIWithAlpha.getColorModel();
        }

        /**
         * The default transform (1:1).
         */
        public AffineTransform getDefaultTransform() {
System.out.println("getting getDefaultTransform");
            return new AffineTransform();
        }

        /**
         * The normalizing transform (1:1) (since we currently
         * render images at 72dpi, which we might want to change
         * in the future).
         */
        public AffineTransform getNormalizingTransform() {
System.out.println("getting getNormalizingTransform");
            return new AffineTransform(2, 0, 0, 2, 0, 0);
        }

        /**
         * Return our dummy instance of GraphicsDevice
         */
        public GraphicsDevice getDevice() {
            return new PDFGraphicsDevice(this);
        }

        // needed for compiling under jdk1.4
        @jdk14codestart@
        public java.awt.image.VolatileImage createCompatibleVolatileImage(int width, int height) {
            return null;
        }
        @jdk14codeend@
        
        @jdk14codestart@
        /**
         * @see java.awt.GraphicsConfiguration#createCompatibleVolatileImage(int, int, int)
         * @since JDK 1.5
         */
        public java.awt.image.VolatileImage createCompatibleVolatileImage(int width, int height, int transparency) {
            return null;
        }
        @jdk14codeend@
    }

    /**
     * This implements the GraphicsDevice interface as appropriate for
     * a PDFGraphics2D.  This is quite simple since we only have one
     * GraphicsConfiguration for now (this might change in the future
     * I suppose).
     */
    static class PDFGraphicsDevice extends GraphicsDevice {

        /**
         * The Graphics Config that created us...
         */
        GraphicsConfiguration gc;

        /**
         * @param The gc we should reference
         */
        PDFGraphicsDevice(PDFGraphicsConfiguration gc) {
            this.gc = gc;
        }

        /**
         * Ignore template and return the only config we have
         */
        public GraphicsConfiguration getBestConfiguration(GraphicsConfigTemplate gct) {
            return gc;
        }

        /**
         * Return an array of our one GraphicsConfig
         */
        public GraphicsConfiguration[] getConfigurations() {
            return new GraphicsConfiguration[] {
                gc
            };
        }

        /**
         * Return out sole GraphicsConfig.
         */
        public GraphicsConfiguration getDefaultConfiguration() {
            return gc;
        }

        /**
         * Generate an IdString..
         */
        public String getIDstring() {
            return toString();
        }

        /**
         * Let the caller know that we are "a printer"
         */
        public int getType() {
            return GraphicsDevice.TYPE_PRINTER;
        }

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
    public FontMetrics getFontMetrics(Font f) {
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
