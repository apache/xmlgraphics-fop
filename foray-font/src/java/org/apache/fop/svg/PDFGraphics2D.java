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

package org.apache.fop.svg;

import org.apache.fop.pdf.FontMap;
import org.apache.fop.pdf.PDFConformanceException;
import org.apache.fop.pdf.PDFResourceContext;
import org.apache.fop.pdf.PDFResources;
import org.apache.fop.pdf.PDFGState;
import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFState;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.pdf.PDFText;
import org.apache.fop.pdf.PDFXObject;
import org.apache.fop.pdf.PDFPattern;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFLink;
import org.apache.fop.pdf.PDFAnnotList;
import org.apache.fop.pdf.BitmapImage;
import org.apache.fop.image.JpegImage;
import org.apache.fop.render.pdf.FopPDFImage;

import org.apache.xmlgraphics.java2d.AbstractGraphics2D;
import org.apache.xmlgraphics.java2d.GraphicContext;

import org.apache.batik.ext.awt.RadialGradientPaint;
import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.gvt.PatternPaint;
import org.apache.batik.gvt.GraphicsNode;

import org.axsl.fontR.Font;
import org.axsl.fontR.FontConsumer;
import org.axsl.fontR.FontException;
import org.axsl.fontR.FontUse;

import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
/*  java.awt.Font is not imported to avoid confusion with
    org.apache.fop.fonts.Font */
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.BasicStroke;
import java.awt.AlphaComposite;
import java.awt.geom.AffineTransform;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.RenderableImage;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.StringWriter;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Map;
import java.util.List;

/**
 * PDF Graphics 2D.
 * Used for drawing into a pdf document as if it is a graphics object.
 * This takes a pdf document and draws into it.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 * @version $Id$
 * @see org.apache.batik.ext.awt.g2d.AbstractGraphics2D
 */
public class PDFGraphics2D extends AbstractGraphics2D {

    private static final AffineTransform IDENTITY_TRANSFORM = new AffineTransform();
    
    /** The number of decimal places. */ 
    private static final int DEC = 8;
    
    /**
     * the PDF Document being created
     */
    protected PDFDocument pdfDoc;

    /**
     * The current resource context for adding fonts, patterns etc.
     */
    protected PDFResourceContext resourceContext;

    /**
     * The PDF reference of the current page.
     */
    protected String pageRef;

    /**
     * the current state of the pdf graphics
     */
    protected PDFState graphicsState;

    /**
     * The PDF graphics state level that this svg is being drawn into.
     */
    protected int baseLevel = 0;

    /**
     * The count of JPEG images added to document so they recieve
     * unique keys.
     */
    protected int[] jpegCount = {0};

    /**
     * The font map.
     */
    protected FontMap fontMap;

    /**
     * The override font state used when drawing text and the font cannot be
     * set using java fonts.
     */
    protected FontUse ovFontUse = null;

    /**
     * The override font size (in millipoint) used when drawing text and the font cannot be
     * set using java fonts.
     */
    protected int ovFontSize;

    /**
     * the current stream to add PDF commands to
     */
    protected StringWriter currentStream = new StringWriter();

    /**
     * the current font use
     */
    protected FontUse currentFontUse;

    /**
     * the current font size in millipoints
     */
    protected int currentFontSize;

    /**
     * The output stream for the pdf document.
     * If this is set then it can progressively output
     * the pdf document objects to reduce memory.
     * Especially with images.
     */
    protected OutputStream outputStream = null;

    /**
     * Create a new PDFGraphics2D with the given pdf document info.
     * This is used to create a Graphics object for use inside an already
     * existing document.
     *
     * @param textAsShapes if true then draw text as shapes
     * @param fontMap the font map
     * @param doc the pdf document for creating pdf objects
     * @param page the current resource context or page
     * @param pref the PDF reference of the current page
     * @param fontUse the current font use
     * @param size the current font size in millipoints
     */
    public PDFGraphics2D(boolean textAsShapes, FontMap fontMap, PDFDocument doc,
                         PDFResourceContext page, String pref, FontUse fontUse, int size) {
        this(textAsShapes, fontMap);
        pdfDoc = doc;
        resourceContext = page;
        currentFontUse = fontUse;
        currentFontSize = size;
        pageRef = pref;
        graphicsState = new PDFState();
    }

    /**
     * Create a new PDFGraphics2D.
     *
     * @param textAsShapes true if drawing text as shapes
     * @param fontMap the mappings of fonts to their corresponding internal names
     */
    protected PDFGraphics2D(boolean textAsShapes, FontMap fontMap) {
        super(textAsShapes);
        this.fontMap = fontMap;
    }

    /**
     * This constructor supports the create method.
     * This is not implemented properly.
     *
     * @param g the PDF graphics to make a copy of
     */
    public PDFGraphics2D(PDFGraphics2D g) {
        super(g);
        this.pdfDoc = g.pdfDoc;
        this.resourceContext = g.resourceContext;
        this.currentFontUse = g.currentFontUse;
        this.currentFontSize = g.currentFontSize;
        this.fontMap = g.fontMap;
        this.pageRef = g.pageRef;
        this.graphicsState = g.graphicsState;
        this.currentStream = g.currentStream;
        this.jpegCount = g.jpegCount;
        this.outputStream = g.outputStream;
        this.ovFontUse = g.ovFontUse;
        this.ovFontSize = g.ovFontSize;
    }

    /**
     * Return the font consumer associated to this document.
     * @return the font consumer.
     */
    public FontConsumer getFontConsumer() {
        return fontMap.getFontConsumer();
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
     * Central handler for IOExceptions for this class.
     * @param ioe IOException to handle
     */
    protected void handleIOException(IOException ioe) {
        //TODO Surely, there's a better way to do this.
        ioe.printStackTrace();
    }

    /**
     * This method is used by PDFDocumentGraphics2D to prepare a new page if
     * necessary.
     */
    protected void preparePainting() {
        //nop, used by PDFDocumentGraphics2D
    }

    /**
     * Set the PDF state to use when starting to draw
     * into the PDF graphics.
     *
     * @param state the PDF state
     */
    public void setPDFState(PDFState state) {
        graphicsState = state;
        baseLevel = graphicsState.getStackLevel();
    }

    /**
     * Set the output stream that this PDF document is
     * being drawn to. This is so that it can progressively
     * use the PDF document to output data such as images.
     * This results in a significant saving on memory.
     *
     * @param os the output stream that is being used for the PDF document
     */
    public void setOutputStream(OutputStream os) {
        outputStream = os;
    }

    /**
     * Get the string containing all the commands written into this
     * Grpahics.
     * @return the string containing the PDF markup
     */
    public String getString() {
        return currentStream.toString();
    }

    /**
     * Get the string buffer from the currentStream, containing all
     * the commands written into this Grpahics so far.
     * @return the StringBuffer containing the PDF markup
     */
    public StringBuffer getBuffer() {
        return currentStream.getBuffer();
    }

    /**
     * Set the Grpahics context.
     * @param c the graphics context to use
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
     * Set the override font use for drawing text.
     * This is used by the PDF text painter so that it can temporarily
     * set the font state when a java font cannot be used.
     * The next drawString will use this font state.
     *
     * @param infont the FontUse to use
     */
    public void setOverrideFontUse(FontUse infont) {
        ovFontUse = infont;
    }

    /**
     * Set the override font size for drawing text.
     * This is used by the PDF text painter so that it can temporarily
     * set the font state when a java font cannot be used.
     * The next drawString will use this font state.
     *
     * @param insize the font size to use
     */
    public void setOverrideFontSize(int insize) {
        ovFontSize = insize;
    }

    /**
     * Restore the PDF graphics state to the starting state level.
     */
    /* seems not to be used
    public void restorePDFState() {
        for (int count = graphicsState.getStackLevel(); count > baseLevel; count--) {
            currentStream.write("Q\n");
        }
        graphicsState.restoreLevel(baseLevel);
    }*/

    private void concatMatrix(double[] matrix) {
        currentStream.write(PDFNumber.doubleOut(matrix[0], DEC) + " "
                + PDFNumber.doubleOut(matrix[1], DEC) + " "
                + PDFNumber.doubleOut(matrix[2], DEC) + " "
                + PDFNumber.doubleOut(matrix[3], DEC) + " "
                + PDFNumber.doubleOut(matrix[4], DEC) + " "
                + PDFNumber.doubleOut(matrix[5], DEC) + " cm\n");
    }

    /**
     * This is mainly used for shading patterns which use the document-global coordinate system
     * instead of the local one.
     * @return the transformation matrix that established the basic user space for this document
     */
    protected AffineTransform getBaseTransform() {
        AffineTransform at = new AffineTransform(graphicsState.getTransform());
        return at;
    }
    
    /**
     * This is a pdf specific method used to add a link to the
     * pdf document.
     *
     * @param bounds the bounds of the link in user coordinates
     * @param trans the transform of the current drawing position
     * @param dest the PDF destination
     * @param linkType the type of link, internal or external
     */
    public void addLink(Rectangle2D bounds, AffineTransform trans, String dest, int linkType) {
        if (!pdfDoc.getProfile().isAnnotationAllowed()) {
            return;
        }
        preparePainting();
        AffineTransform at = getTransform();
        Shape b = at.createTransformedShape(bounds);
        b = trans.createTransformedShape(b);
        if (b != null) {
            Rectangle rect = b.getBounds();

            if (linkType != PDFLink.EXTERNAL) {
                String pdfdest = "/FitR " + dest;
                resourceContext.addAnnotation(
                    pdfDoc.getFactory().makeLink(rect, pageRef, pdfdest));
            } else {
                resourceContext.addAnnotation(
                    pdfDoc.getFactory().makeLink(rect, dest, linkType, 0));
            }
        }
    }

    /**
     * Add a JPEG image directly to the PDF document.
     * This is used by the PDFImageElementBridge to draw a JPEG
     * directly into the pdf document rather than converting the image into
     * a bitmap and increasing the size.
     *
     * @param jpeg the jpeg image to draw
     * @param x the x position
     * @param y the y position
     * @param width the width to draw the image
     * @param height the height to draw the image
     */
    public void addJpegImage(JpegImage jpeg, float x, float y, 
                             float width, float height) {
        preparePainting();
        // Need to include hash code as when invoked from FO you
        // may have several 'independent' PDFGraphics2D so the
        // count is not enough.
        String key = "__AddJPEG_" + hashCode() + "_" + jpegCount[0];
        jpegCount[0]++;
        FopPDFImage fopimage = new FopPDFImage(jpeg, key);
        int xObjectNum = this.pdfDoc.addImage(resourceContext, 
                                              fopimage).getXNumber();
        AffineTransform at = getTransform();
        double[] matrix = new double[6];
        at.getMatrix(matrix);
        currentStream.write("q\n");
        if (!at.isIdentity()) {
            concatMatrix(matrix);
        }
        Shape imclip = getClip();
        writeClip(imclip);

        currentStream.write("" + width + " 0 0 "
                          + (-height) + " "
                          + x + " "
                          + (y + height) + " cm\n" + "/Im"
                          + xObjectNum + " Do\nQ\n");

        if (outputStream != null) {
            try {
                this.pdfDoc.output(outputStream);
            } catch (IOException ioe) {
                // ignore exception, will be thrown again later
            }
        }
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
     * @return true if the image was drawn
     * @see      java.awt.Image
     * @see      java.awt.image.ImageObserver
     * @see      java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     */
    public boolean drawImage(Image img, int x, int y,
                             ImageObserver observer) {
        preparePainting();

        int width = img.getWidth(observer);
        int height = img.getHeight(observer);

        if (width == -1 || height == -1) {
            return false;
        }

        return drawImage(img, x, y, width, height, observer);
    }

    private BufferedImage buildBufferedImage(Dimension size) {
        return new BufferedImage(size.width, size.height,
                                 BufferedImage.TYPE_INT_ARGB);
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
     * @return true if the image was drawn
     * @see      java.awt.Image
     * @see      java.awt.image.ImageObserver
     * @see      java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     */
    public boolean drawImage(Image img, int x, int y, int width, int height,
                               ImageObserver observer) {
        preparePainting();
        // first we look to see if we've already added this image to
        // the pdf document. If so, we just reuse the reference;
        // otherwise we have to build a FopImage and add it to the pdf
        // document
        PDFXObject imageInfo = pdfDoc.getImage("TempImage:" + img.toString());
        if (imageInfo == null) {
            // OK, have to build and add a PDF image

            Dimension size = new Dimension(width, height);
            BufferedImage buf = buildBufferedImage(size);

            java.awt.Graphics2D g = buf.createGraphics();
            g.setComposite(AlphaComposite.SrcOver);
            g.setBackground(new Color(1, 1, 1, 0));
            g.setPaint(new Color(1, 1, 1, 0));
            g.fillRect(0, 0, width, height);
            g.clip(new Rectangle(0, 0, buf.getWidth(), buf.getHeight()));
            g.setComposite(gc.getComposite());

            if (!g.drawImage(img, 0, 0, buf.getWidth(), buf.getHeight(), observer)) {
                return false;
            }
            g.dispose();

            final byte[] result = new byte[buf.getWidth() * buf.getHeight() * 3 /*for RGB*/];
            byte[] mask = new byte[buf.getWidth() * buf.getHeight()];
            boolean hasMask = false;
            //boolean binaryMask = true;

            Raster raster = buf.getData();
            DataBuffer bd = raster.getDataBuffer();

            int count = 0;
            int maskpos = 0;
            int[] iarray;
            int i, j, val, alpha;
            switch (bd.getDataType()) {
                case DataBuffer.TYPE_INT:
                int[][] idata = ((DataBufferInt)bd).getBankData();
                for (i = 0; i < idata.length; i++) {
                    iarray = idata[i];
                    for (j = 0; j < iarray.length; j++) {
                        val = iarray[j];
                        alpha = val >>> 24;
                        mask[maskpos++] = (byte)(alpha & 0xFF);
                        if (alpha != 255) {
                            hasMask = true;
                        }
                        result[count++] = (byte)((val >> 16) & 0xFF);
                        result[count++] = (byte)((val >> 8) & 0xFF);
                        result[count++] = (byte)((val) & 0xFF);
                    }
                }
                break;
                default:
                // error
                break;
            }
            String ref = null;
            if (hasMask) {
                // if the mask is binary then we could convert it into a bitmask
                BitmapImage fopimg = new BitmapImage("TempImageMask:"
                                             + img.toString(), buf.getWidth(),
                                             buf.getHeight(), mask, null);
                fopimg.setColorSpace(new PDFDeviceColorSpace(PDFDeviceColorSpace.DEVICE_GRAY));
                PDFXObject xobj = pdfDoc.addImage(resourceContext, fopimg);
                ref = xobj.referencePDF();

                if (outputStream != null) {
                    try {
                        this.pdfDoc.output(outputStream);
                    } catch (IOException ioe) {
                        // ignore exception, will be thrown again later
                    }
                }
            } else {
                mask = null;
            }

            BitmapImage fopimg = new BitmapImage("TempImage:"
                                          + img.toString(), buf.getWidth(),
                                          buf.getHeight(), result, ref);
            imageInfo = pdfDoc.addImage(resourceContext, fopimg);
            //int xObjectNum = imageInfo.getXNumber();

            if (outputStream != null) {
                try {
                    this.pdfDoc.output(outputStream);
                } catch (IOException ioe) {
                    // ignore exception, will be thrown again later
                }
            }
        } else {
            resourceContext.getPDFResources().addXObject(imageInfo);
        }

        // now do any transformation required and add the actual image
        // placement instance
        AffineTransform at = getTransform();
        double[] matrix = new double[6];
        at.getMatrix(matrix);
        currentStream.write("q\n");
        if (!at.isIdentity()) {
            concatMatrix(matrix);
        }
        Shape imclip = getClip();
        writeClip(imclip);
        currentStream.write("" + width + " 0 0 " + (-height) + " " + x
                            + " " + (y + height) + " cm\n" + "/Im"
                            + imageInfo.getXNumber() + " Do\nQ\n");
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
        pdfDoc = null;
        fontMap = null;
        currentStream = null;
        currentFontUse = null;
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

        //Transparency shortcut
        Color c;
        c = getColor();
        if (c.getAlpha() == 0) {
            return;
        }

        AffineTransform trans = getTransform();
        double[] tranvals = new double[6];
        trans.getMatrix(tranvals);

        Shape imclip = getClip();
        boolean newClip = graphicsState.checkClip(imclip);
        boolean newTransform = graphicsState.checkTransform(trans)
                               && !trans.isIdentity();

        if (newClip || newTransform) {
            currentStream.write("q\n");
            graphicsState.push();
            if (newTransform) {
                concatMatrix(tranvals);
            }
            if (newClip) {
                writeClip(imclip);
            }
        }

        if (c.getAlpha() != 255) {
            checkTransparencyAllowed();
            Map vals = new java.util.HashMap();
            vals.put(PDFGState.GSTATE_ALPHA_STROKE, 
                    new Float(c.getAlpha() / 255f));
            PDFGState gstate = pdfDoc.getFactory().makeGState(
                    vals, graphicsState.getGState());
            resourceContext.addGState(gstate);
            currentStream.write("/" + gstate.getName() + " gs\n");
        }

        c = getColor();
        if (graphicsState.setColor(c)) {
            applyColor(c, false);
        }
        c = getBackground();
        if (graphicsState.setBackColor(c)) {
            applyColor(c, true);
        }

        Paint paint = getPaint();
        if (graphicsState.setPaint(paint)) {
            if (!applyPaint(paint, false)) {
                // Stroke the shape and use it to 'clip'
                // the paint contents.
                Shape ss = getStroke().createStrokedShape(s);
                applyUnknownPaint(paint, ss);

                if (newClip || newTransform) {
                    currentStream.write("Q\n");
                    graphicsState.pop();
                }
                return;
            }
        }
        applyStroke(getStroke());

        PathIterator iter = s.getPathIterator(IDENTITY_TRANSFORM);
        processPathIterator(iter);
        doDrawing(false, true, false);
        if (newClip || newTransform) {
            currentStream.write("Q\n");
            graphicsState.pop();
        }
    }
    
/*
    // in theory we could set the clip using these methods
    // it doesn't seem to improve the file sizes much
    // and makes everything more complicated

    Shape lastClip = null;

    public void clip(Shape cl) {
        super.clip(cl);
        Shape newClip = getClip();
        if (newClip == null || lastClip == null
                || !(new Area(newClip).equals(new Area(lastClip)))) {
        graphicsState.setClip(newClip);
        writeClip(newClip);
        }

        lastClip = newClip;
    }

    public void setClip(Shape cl) {
        super.setClip(cl);
        Shape newClip = getClip();
        if (newClip == null || lastClip == null
                || !(new Area(newClip).equals(new Area(lastClip)))) {
        for (int count = graphicsState.getStackLevel(); count > baseLevel; count--) {
            currentStream.write("Q\n");
        }
        graphicsState.restoreLevel(baseLevel);
        currentStream.write("q\n");
        graphicsState.push();
        if (newClip != null) {
            graphicsState.setClip(newClip);
        }
        writeClip(newClip);
        }

        lastClip = newClip;
    }
*/

    /**
     * Set the clipping shape for future PDF drawing in the current graphics state.
     * This sets creates and writes a clipping shape that will apply
     * to future drawings in the current graphics state.
     *
     * @param s the clipping shape
     */
    protected void writeClip(Shape s) {
        if (s == null) {
            return;
        }
        preparePainting();
        PathIterator iter = s.getPathIterator(IDENTITY_TRANSFORM);
        processPathIterator(iter);
        // clip area
        currentStream.write("W\n");
        currentStream.write("n\n");
    }

    /**
     * Apply the java Color to PDF.
     * This converts the java colour to a PDF colour and
     * sets it for the next drawing.
     *
     * @param col the java colour
     * @param fill true if the colour will be used for filling
     */
    protected void applyColor(Color col, boolean fill) {
        preparePainting();
        Color c = col;
        if (c.getColorSpace().getType()
                == ColorSpace.TYPE_RGB) {
            PDFColor currentColour = new PDFColor(c.getRed(), c.getGreen(),
                                         c.getBlue());
            currentStream.write(currentColour.getColorSpaceOut(fill));
        } else if (c.getColorSpace().getType()
                   == ColorSpace.TYPE_CMYK) {
            if (pdfDoc.getProfile().getPDFAMode().isPDFA1LevelB()) {
                //See PDF/A-1, ISO 19005:1:2005(E), 6.2.3.3
                //FOP is currently restricted to DeviceRGB if PDF/A-1 is active.
                throw new PDFConformanceException(
                        "PDF/A-1 does not allow mixing DeviceRGB and DeviceCMYK.");
            }
            float[] cComps = c.getColorComponents(new float[3]);
            double[] cmyk = new double[3];
            for (int i = 0; i < 3; i++) {
                // convert the float elements to doubles for pdf
                cmyk[i] = cComps[i];
            }
            PDFColor currentColour = new PDFColor(cmyk[0], cmyk[1], cmyk[2], cmyk[3]);
            currentStream.write(currentColour.getColorSpaceOut(fill));
        } else if (c.getColorSpace().getType()
                   == ColorSpace.TYPE_2CLR) {
            // used for black/magenta
            float[] cComps = c.getColorComponents(new float[1]);
            double[] blackMagenta = new double[1];
            for (int i = 0; i < 1; i++) {
                blackMagenta[i] = cComps[i];
            }
            //PDFColor  currentColour = new PDFColor(blackMagenta[0], blackMagenta[1]);
            //currentStream.write(currentColour.getColorSpaceOut(fill));
        } else {
            throw new UnsupportedOperationException(
                    "Color Space not supported by PDFGraphics2D");
        }
    }

    /**
     * Apply the java paint to the PDF.
     * This takes the java paint sets up the appropraite PDF commands
     * for the drawing with that paint.
     * Currently this supports the gradients and patterns from batik.
     *
     * @param paint the paint to convert to PDF
     * @param fill true if the paint should be set for filling
     * @return true if the paint is handled natively, false if the paint should be rasterized
     */
    protected boolean applyPaint(Paint paint, boolean fill) {
        preparePainting();

        if (paint instanceof Color) {
            return true;
        }
        // convert java.awt.GradientPaint to LinearGradientPaint to avoid rasterization
        if (paint instanceof GradientPaint) {
            GradientPaint gpaint = (GradientPaint) paint;
            paint = new LinearGradientPaint(
                    (float) gpaint.getPoint1().getX(),
                    (float) gpaint.getPoint1().getY(),
                    (float) gpaint.getPoint2().getX(),
                    (float) gpaint.getPoint2().getY(), 
                    new float[] {0, 1}, 
                    new Color[] {gpaint.getColor1(), gpaint.getColor2()},
                    gpaint.isCyclic() ? LinearGradientPaint.REPEAT : LinearGradientPaint.NO_CYCLE);
        }
        if (paint instanceof LinearGradientPaint) {
            LinearGradientPaint gp = (LinearGradientPaint)paint;

            // This code currently doesn't support 'repeat'.
            // For linear gradients it is possible to construct
            // a 'tile' that is repeated with a PDF pattern, but
            // it would be very tricky as you would have to rotate
            // the coordinate system so the repeat was axially
            // aligned.  At this point I'm just going to rasterize it.
            MultipleGradientPaint.CycleMethodEnum cycle = gp.getCycleMethod();
            if (cycle != MultipleGradientPaint.NO_CYCLE) {
                return false;
            }

            Color[] cols = gp.getColors();
            float[] fractions = gp.getFractions();

            // Build proper transform from gradient space to page space
            // ('Patterns' don't get userspace transform).
            AffineTransform transform;
            transform = new AffineTransform(getBaseTransform());
            transform.concatenate(getTransform());
            transform.concatenate(gp.getTransform());

            List theMatrix = new java.util.ArrayList();
            double [] mat = new double[6];
            transform.getMatrix(mat);
            for (int idx = 0; idx < mat.length; idx++) {
                theMatrix.add(new Double(mat[idx]));
            }

            Point2D p1 = gp.getStartPoint();
            Point2D p2 = gp.getEndPoint();
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

            List someColors = new java.util.ArrayList();

            for (int count = 0; count < cols.length; count++) {
                Color c1 = cols[count];
                if (c1.getAlpha() != 255) {
                    return false;  // PDF can't do alpha
                }

                PDFColor color1 = new PDFColor(c1.getRed(), c1.getGreen(),
                                               c1.getBlue());
                someColors.add(color1);
                if (count > 0 && count < cols.length - 1) {
                    theBounds.add(new Double(fractions[count]));
                }
            }

            PDFDeviceColorSpace aColorSpace;
            aColorSpace = new PDFDeviceColorSpace(PDFDeviceColorSpace.DEVICE_RGB);
            PDFPattern myPat = pdfDoc.getFactory().makeGradient(
                    resourceContext, false, aColorSpace,
                    someColors, theBounds, theCoords, theMatrix);
            currentStream.write(myPat.getColorSpaceOut(fill));

            return true;
        }
        if (paint instanceof RadialGradientPaint) {
            RadialGradientPaint rgp = (RadialGradientPaint)paint;

            // There is essentially no way to support repeate
            // in PDF for radial gradients (the one option would
            // be to 'grow' the outer circle until it fully covered
            // the bounds and then grow the stops accordingly, the
            // problem is that this may require an extremely large
            // number of stops for cases where the focus is near
            // the edge of the outer circle).  so we rasterize.
            MultipleGradientPaint.CycleMethodEnum cycle = rgp.getCycleMethod();
            if (cycle != MultipleGradientPaint.NO_CYCLE) {
                return false;
            }

            AffineTransform transform;
            transform = new AffineTransform(getBaseTransform());
            transform.concatenate(getTransform());
            transform.concatenate(rgp.getTransform());

            List theMatrix = new java.util.ArrayList();
            double [] mat = new double[6];
            transform.getMatrix(mat);
            for (int idx = 0; idx < mat.length; idx++) {
                theMatrix.add(new Double(mat[idx]));
            }

            double ar = rgp.getRadius();
            Point2D ac = rgp.getCenterPoint();
            Point2D af = rgp.getFocusPoint();

            List theCoords = new java.util.ArrayList();
            double dx = af.getX() - ac.getX();
            double dy = af.getY() - ac.getY();
            double d = Math.sqrt(dx * dx + dy * dy);
            if (d > ar) {
                // the center point af must be within the circle with
                // radius ar centered at ac so limit it to that.
                double scale = (ar * .9999) / d;
                dx = dx * scale;
                dy = dy * scale;
            }

            theCoords.add(new Double(ac.getX() + dx)); // Fx
            theCoords.add(new Double(ac.getY() + dy)); // Fy
            theCoords.add(new Double(0));
            theCoords.add(new Double(ac.getX()));
            theCoords.add(new Double(ac.getY()));
            theCoords.add(new Double(ar));

            Color[] cols = rgp.getColors();
            List someColors = new java.util.ArrayList();
            for (int count = 0; count < cols.length; count++) {
                Color cc = cols[count];
                if (cc.getAlpha() != 255) {
                    return false;  // PDF can't do alpha
                }

                someColors.add(new PDFColor(cc.getRed(), cc.getGreen(), 
                                            cc.getBlue()));
            }

            float[] fractions = rgp.getFractions();
            List theBounds = new java.util.ArrayList();
            for (int count = 1; count < fractions.length - 1; count++) {
                float offset = fractions[count];
                theBounds.add(new Double(offset));
            }
            PDFDeviceColorSpace colSpace;
            colSpace = new PDFDeviceColorSpace(PDFDeviceColorSpace.DEVICE_RGB);

            PDFPattern myPat = pdfDoc.getFactory().makeGradient
                (resourceContext, true, colSpace,
                 someColors, theBounds, theCoords, theMatrix);

            currentStream.write(myPat.getColorSpaceOut(fill));

            return true;
        } 
        if (paint instanceof PatternPaint) {
            PatternPaint pp = (PatternPaint)paint;
            return createPattern(pp, fill);
        }
        return false; // unknown paint
    }

    private boolean createPattern(PatternPaint pp, boolean fill) {
        preparePainting();

        PDFResources res = pdfDoc.getFactory().makeResources();
        PDFResourceContext context = new PDFResourceContext(res);
        PDFGraphics2D pattGraphic = new PDFGraphics2D(textAsShapes, fontMap,
                                        pdfDoc, context, pageRef,
                                        null, 0);
        pattGraphic.setGraphicContext(new GraphicContext());
        pattGraphic.gc.validateTransformStack();
        pattGraphic.setRenderingHints(this.getRenderingHints());
        pattGraphic.setOutputStream(outputStream);

        GraphicsNode gn = pp.getGraphicsNode();
        Rectangle2D gnBBox = gn.getBounds();
        Rectangle2D rect = pp.getPatternRect();

        // if (!pp.getOverflow()) {
            gn.paint(pattGraphic);
        // } else {
        // /* Commented out until SVN version of Batik is included */
        //     // For overflow we need to paint the content from
        //     // all the tiles who's overflow will intersect one
        //     // tile (left->right, top->bottom).  Then we can
        //     // simply replicate that tile as normal.
        //     double gnMinX = gnBBox.getX();
        //     double gnMaxX = gnBBox.getX() + gnBBox.getWidth();
        //     double gnMinY = gnBBox.getY();
        //     double gnMaxY = gnBBox.getY() + gnBBox.getHeight();
        //     double patMaxX = rect.getX() + rect.getWidth();
        //     double patMaxY = rect.getY() + rect.getHeight();
        //     double stepX = rect.getWidth();
        //     double stepY = rect.getHeight();            
        // 
        //     int startX = (int)((rect.getX() - gnMaxX)/stepX);
        //     int startY = (int)((rect.getY() - gnMaxY)/stepY);
        // 
        //     int endX   = (int)((patMaxX - gnMinX)/stepX);
        //     int endY   = (int)((patMaxY - gnMinY)/stepY);
        // 
        //     pattGraphic.translate(startX*stepX, startY*stepY);
        //     for (int yIdx=startY; yIdx<=endY; yIdx++) {
        //         for (int xIdx=startX; xIdx<=endX; xIdx++) {
        //             gn.paint(pattGraphic);
        //             pattGraphic.translate(stepX,0);
        //         }
        //         pattGraphic.translate(-(endX-startX+1)*stepX, stepY);
        //     }
        // }

        List bbox = new java.util.ArrayList();
        bbox.add(new Double(rect.getX()));
        bbox.add(new Double(rect.getHeight() + rect.getY()));
        bbox.add(new Double(rect.getWidth() + rect.getX()));
        bbox.add(new Double(rect.getY()));

        AffineTransform transform;
        transform = new AffineTransform(getBaseTransform());
        transform.concatenate(getTransform());
        transform.concatenate(pp.getPatternTransform());

        List theMatrix = new java.util.ArrayList();
        double [] mat = new double[6];
        transform.getMatrix(mat);
        for (int idx = 0; idx < mat.length; idx++) {
            theMatrix.add(new Double(mat[idx]));
        }

        /** @todo see if pdfDoc and res can be linked here,
        (currently res <> PDFDocument's resources) so addFonts() 
        can be moved to PDFDocument class */
//        res.addFonts(pdfDoc, fontInfo);

        PDFPattern myPat = pdfDoc.getFactory().makePattern(
                                resourceContext, 1, res, 1, 1, bbox,
                                rect.getWidth(), rect.getHeight(),
                                theMatrix, null, 
                                pattGraphic.getBuffer());

        currentStream.write(myPat.getColorSpaceOut(fill));

        PDFAnnotList annots = context.getAnnotations();
        if (annots != null) {
            this.pdfDoc.addObject(annots);
        }

        if (outputStream != null) {
            try {
                this.pdfDoc.output(outputStream);
            } catch (IOException ioe) {
                // ignore exception, will be thrown again later
            }
        }
        return true;
    }

    protected boolean applyUnknownPaint(Paint paint, Shape shape) {
        preparePainting();

        Shape clip = getClip();
        Rectangle2D usrClipBounds, usrBounds;
        usrBounds = shape.getBounds2D();
        usrClipBounds  = clip.getBounds2D();
        if (!usrClipBounds.intersects(usrBounds)) {
            return true;
        }
        Rectangle2D.intersect(usrBounds, usrClipBounds, usrBounds);
        double usrX = usrBounds.getX();
        double usrY = usrBounds.getY();
        double usrW = usrBounds.getWidth();
        double usrH = usrBounds.getHeight();

        Rectangle devShapeBounds, devClipBounds, devBounds;
        AffineTransform at = getTransform();
        devShapeBounds = at.createTransformedShape(shape).getBounds();
        devClipBounds  = at.createTransformedShape(clip).getBounds();
        if (!devClipBounds.intersects(devShapeBounds)) {
            return true;
        }
        devBounds = devShapeBounds.intersection(devClipBounds);
        int devX = devBounds.x;
        int devY = devBounds.y;
        int devW = devBounds.width;
        int devH = devBounds.height;

        ColorSpace rgbCS = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel rgbCM = new DirectColorModel
            (rgbCS, 32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000,
             false, DataBuffer.TYPE_BYTE);

        PaintContext pctx = paint.createContext(rgbCM, devBounds, usrBounds, 
                                                at, getRenderingHints());
        PDFXObject imageInfo = pdfDoc.getImage
            ("TempImage:" + pctx.toString());
        if (imageInfo != null) {
            resourceContext.getPDFResources().addXObject(imageInfo);
        } else {
            Raster r = pctx.getRaster(devX, devY, devW, devH);
            WritableRaster wr = (WritableRaster)r;
            wr = wr.createWritableTranslatedChild(0, 0);

            ColorModel pcm = pctx.getColorModel();
            BufferedImage bi = new BufferedImage
                (pcm, wr, pcm.isAlphaPremultiplied(), null);
            final byte[] rgb  = new byte[devW * devH * 3];
            final int[]  line = new int[devW];
            final byte[] mask;
            int x, y, val, rgbIdx = 0;
        
            if (pcm.hasAlpha()) {
                mask = new byte[devW * devH];
                int maskIdx = 0;
                for (y = 0; y < devH; y++) {
                    bi.getRGB(0, y, devW, 1, line, 0, devW);
                    for (x = 0; x < devW; x++) {
                        val = line[x];
                        mask[maskIdx++] = (byte)(val >>> 24);
                        rgb[rgbIdx++]   = (byte)((val >> 16) & 0x0FF);
                        rgb[rgbIdx++]   = (byte)((val >> 8 ) & 0x0FF);
                        rgb[rgbIdx++]   = (byte)((val      ) & 0x0FF);
                    }
                }
            } else {
                mask = null;
                for (y = 0; y < devH; y++) {
                    bi.getRGB(0, y, devW, 1, line, 0, devW);
                    for (x = 0; x < devW; x++) {
                        val = line[x];
                        rgb[rgbIdx++]  = (byte)((val >> 16) & 0x0FF);
                        rgb[rgbIdx++]  = (byte)((val >> 8 ) & 0x0FF);
                        rgb[rgbIdx++]  = (byte)((val      ) & 0x0FF);
                    }
                }
            }

            String maskRef = null;
            if (mask != null) {
                BitmapImage fopimg = new BitmapImage
                    ("TempImageMask:" + pctx.toString(), devW, devH, mask, null);
                fopimg.setColorSpace(new PDFDeviceColorSpace(PDFDeviceColorSpace.DEVICE_GRAY));
                PDFXObject xobj = pdfDoc.addImage(resourceContext, fopimg);
                maskRef = xobj.referencePDF();

                if (outputStream != null) {
                    try {
                        this.pdfDoc.output(outputStream);
                    } catch (IOException ioe) {
                        // ignore exception, will be thrown again later
                    }
                }
            }
            BitmapImage fopimg;
            fopimg = new BitmapImage("TempImage:" + pctx.toString(),
                                     devW, devH, rgb, maskRef);
            fopimg.setTransparent(new PDFColor(255, 255, 255));
            imageInfo = pdfDoc.addImage(resourceContext, fopimg);
            if (outputStream != null) {
                try {
                    this.pdfDoc.output(outputStream);
                } catch (IOException ioe) {
                    // ignore exception, will be thrown again later
                }
            }
        }

        currentStream.write("q\n");
        writeClip(shape);
        currentStream.write("" + usrW + " 0 0 " + (-usrH) + " " + usrX
                            + " " + (usrY + usrH) + " cm\n" + "/Im"
                            + imageInfo.getXNumber() + " Do\nQ\n");
        return true;
    }

    /**
     * Apply the stroke to the PDF.
     * This takes the java stroke and outputs the appropriate settings
     * to the PDF so that the stroke attributes are handled.
     *
     * @param stroke the java stroke
     */
    protected void applyStroke(Stroke stroke) {
        preparePainting();
        if (stroke instanceof BasicStroke) {
            BasicStroke bs = (BasicStroke)stroke;

            float[] da = bs.getDashArray();
            if (da != null) {
                currentStream.write("[");
                for (int count = 0; count < da.length; count++) {
                    currentStream.write(PDFNumber.doubleOut(da[count]));
                    if (count < da.length - 1) {
                        currentStream.write(" ");
                    }
                }
                currentStream.write("] ");
                float offset = bs.getDashPhase();
                currentStream.write(PDFNumber.doubleOut(offset) + " d\n");
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
        //NYI
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
        //NYI
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
     * @param x the coordinate where the <code>String</code>
     * should be rendered
     * @param y the coordinate where the <code>String</code>
     * should be rendered
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see java.awt.Graphics#setFont
     * @see #setTransform
     * @see #setComposite
     * @see #setClip
     */
    public void drawString(String s, float x, float y) {
        preparePainting();

        AffineTransform fontTransform = null;
        FontUse fontUse = null;
        int size;
        if (ovFontUse == null) {
            FontConsumer fontConsumer = fontMap.getFontConsumer();
            java.awt.Font gFont = getFont();
            fontTransform = gFont.getTransform();
            String n = gFont.getFamily();
            if (n.equals("sanserif")) {
                n = "sans-serif";
            }
            // TODO adapt
//            size = gFont.getSize() * 1000;
            size = (int) gFont.getSize2D() * 1000;
            byte style = gFont.isItalic() ? Font.FONT_STYLE_ITALIC : Font.FONT_STYLE_NORMAL;
            short weight = gFont.isBold() ? Font.FONT_WEIGHT_BOLD : Font.FONT_WEIGHT_NORMAL;
            try {
                fontUse = fontConsumer.selectFontXSL(Font.FONT_SELECTION_AUTO,
                        new String[] {n}, style, weight, Font.FONT_VARIANT_NORMAL,
                        Font.FONT_STRETCH_NORMAL, size, s.charAt(0));
            } catch (FontException e) {
                try {
                    fontUse = fontConsumer.selectFontXSL(Font.FONT_SELECTION_AUTO,
                            new String[] {"any"},
                            Font.FONT_STYLE_ANY,
                            Font.FONT_WEIGHT_ANY,
                            Font.FONT_VARIANT_ANY,
                            Font.FONT_STRETCH_ANY,
                            size, s.charAt(0));
                } catch (FontException e1) { /* Should never happen */ }
            }
        } else {
            fontUse = ovFontUse;
            ovFontUse = null;
            size = ovFontSize;
        }
        
        if ((!fontUse.equals(this.currentFontUse))
                || (size != this.currentFontSize)) {
            this.currentFontUse = fontUse;
            this.currentFontSize = size;
            String name = fontMap.getInternalName(fontUse);
            currentStream.write("/" + name + " " + ((float)size) / 1000f + " Tf\n");

        }

        currentStream.write("q\n");

        Color c = getColor();
        applyColor(c, true);
        applyPaint(getPaint(), true);
        int salpha = c.getAlpha();

        if (salpha != 255) {
            checkTransparencyAllowed();
            Map vals = new java.util.HashMap();
            vals.put(PDFGState.GSTATE_ALPHA_NONSTROKE, new Float(salpha / 255f));
            PDFGState gstate = pdfDoc.getFactory().makeGState(
                    vals, graphicsState.getGState());
            resourceContext.addGState(gstate);
            currentStream.write("/" + gstate.getName() + " gs\n");
        }

        Map kerning = null;
        boolean kerningAvailable = false;

        /* TODO vh: kerning is yet unimplemented
        kerning = fontState.getKerning();
        if (kerning != null && !kerning.isEmpty()) {
            kerningAvailable = true;
        }
        */

        // This assumes that *all* CIDFonts use a /ToUnicode mapping
        boolean useMultiByte = fontUse.getFont().getFontComplexity() == Font.FONT_COMPOSITE;

        // String startText = useMultiByte ? "<FEFF" : "(";
        String startText = useMultiByte ? "<" : "(";
        String endText = useMultiByte ? "> " : ") ";

        AffineTransform trans = getTransform();
        //trans.translate(x, y);
        double[] vals = new double[6];
        trans.getMatrix(vals);

        concatMatrix(vals);
        Shape imclip = getClip();
        writeClip(imclip);

        currentStream.write("BT\n");

        AffineTransform localTransform = new AffineTransform();
        localTransform.translate(x, y);
        if (fontTransform != null) {
            localTransform.concatenate(fontTransform);
        }
        localTransform.scale(1, -1);
        double[] lt = new double[6];
        localTransform.getMatrix(lt);
        currentStream.write(PDFNumber.doubleOut(lt[0]) + " "
                + PDFNumber.doubleOut(lt[1]) + " " + PDFNumber.doubleOut(lt[2]) + " "
                + PDFNumber.doubleOut(lt[3]) + " " + PDFNumber.doubleOut(lt[4]) + " "
                + PDFNumber.doubleOut(lt[5]) + " Tm [" + startText);

        int l = s.length();

        for (int i = 0; i < l; i++) {
            int ch = fontUse.encodeCharacter(s.charAt(i));

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
                currentStream.write(PDFText.toUnicodeHex(ch));
            }

            if (kerningAvailable && (i + 1) < l) {
//                addKerning(currentStream, (new Integer((int)ch)),
//                           (new Integer((int)fontState.mapChar(s.charAt(i + 1)))),
//                           kerning, startText, endText);
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
        preparePainting();
        Map kernPair = (Map)kerning.get(ch1);

        if (kernPair != null) {
            Integer width = (Integer)kernPair.get(ch2);
            if (width != null) {
                currentStream.write(endText + (-width.intValue()) + " " + startText);
            }
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
     * @param x the coordinate where the iterator's text is to be
     * rendered
     * @param y the coordinate where the iterator's text is to be
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

        Font fontState = null;

        Shape imclip = getClip();
        writeClip(imclip);
        Color c = getColor();
        applyColor(c, true);
        applyPaint(getPaint(), true);

        boolean fill = true;
        boolean stroke = false;
        if (true) {
            Stroke currentStroke = getStroke();
            stroke = true;
            applyStroke(currentStroke);
            applyColor(c, false);
            applyPaint(getPaint(), false);
        }

        currentStream.write("BT\n");

        // set text rendering mode:
        // 0 - fill, 1 - stroke, 2 - fill then stroke
        int textr = 0;
        if (fill && stroke) {
            textr = 2;
        } else if (stroke) {
            textr = 1;
        }
        currentStream.write(textr + " Tr\n");

        AffineTransform trans = getTransform();
        trans.translate(x, y);
        double[] vals = new double[6];
        trans.getMatrix(vals);

        for (char ch = iterator.first(); ch != CharacterIterator.DONE;
                ch = iterator.next()) {
            //Map attr = iterator.getAttributes();

            // TODO vh: commented out because obsolete
            // anyway it couldn't work (NPE because fontState == null)
//            int size = fontState.getFontSize();
//            if ((!fontUse.equals(this.currentFontUse))
//                    || (size != this.currentFontSize)) {
//                this.currentFontUse = fontUse;
//                this.currentFontSize = size;
//                currentStream.write("/" + name + " " + (size / 1000)
//                                    + " Tf\n");
//
//            }

            currentStream.write(PDFNumber.doubleOut(vals[0], DEC) + " "
                                + PDFNumber.doubleOut(vals[1], DEC) + " "
                                + PDFNumber.doubleOut(vals[2], DEC) + " "
                                + PDFNumber.doubleOut(vals[3], DEC) + " "
                                + PDFNumber.doubleOut(vals[4], DEC) + " "
                                + PDFNumber.doubleOut(vals[5], DEC) + " Tm (" + ch
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
        preparePainting();

        //Transparency shortcut
        Color c;
        c = getBackground();
        if (c.getAlpha() == 0) {
            c = getColor();
            if (c.getAlpha() == 0) {
                return;
            }
        }
        
        AffineTransform trans = getTransform();
        double[] tranvals = new double[6];
        trans.getMatrix(tranvals);

        Shape imclip = getClip();
        boolean newClip = graphicsState.checkClip(imclip);
        boolean newTransform = graphicsState.checkTransform(trans)
                               && !trans.isIdentity();

        if (newClip || newTransform) {
            currentStream.write("q\n");
            graphicsState.push();
            if (newTransform) {
                concatMatrix(tranvals);
            }
            if (newClip) {
                writeClip(imclip);
            }
        }

        if (c.getAlpha() != 255) {
            checkTransparencyAllowed();
            Map vals = new java.util.HashMap();
            vals.put(PDFGState.GSTATE_ALPHA_NONSTROKE, 
                    new Float(c.getAlpha() / 255f));
            PDFGState gstate = pdfDoc.getFactory().makeGState(
                    vals, graphicsState.getGState());
            resourceContext.addGState(gstate);
            currentStream.write("/" + gstate.getName() + " gs\n");
        }

        c = getColor();
        if (graphicsState.setColor(c)) {
            applyColor(c, true);
        }
        c = getBackground();
        if (graphicsState.setBackColor(c)) {
            applyColor(c, false);
        }

        Paint paint = getPaint();
        if (graphicsState.setPaint(paint)) {
            if (!applyPaint(paint, true)) {
                // Use the shape to 'clip' the paint contents.
                applyUnknownPaint(paint, s);

                if (newClip || newTransform) {
                    currentStream.write("Q\n");
                    graphicsState.pop();
                }
                return;
            }
        }

        //PathIterator iter = s.getPathIterator(getTransform());
        PathIterator iter = s.getPathIterator(IDENTITY_TRANSFORM);
        processPathIterator(iter);
        doDrawing(true, false,
                  iter.getWindingRule() == PathIterator.WIND_EVEN_ODD);
        if (newClip || newTransform) {
            currentStream.write("Q\n");
            graphicsState.pop();
        }
    }

    /** Checks whether the use of transparency is allowed. */
    protected void checkTransparencyAllowed() {
        pdfDoc.getProfile().verifyTransparencyAllowed("Java2D graphics");
    }

    /**
     * Processes a path iterator generating the necessary painting operations.
     * @param iter PathIterator to process
     */
    public void processPathIterator(PathIterator iter) {
        while (!iter.isDone()) {
            double[] vals = new double[6];
            int type = iter.currentSegment(vals);
            switch (type) {
            case PathIterator.SEG_CUBICTO:
                currentStream.write(PDFNumber.doubleOut(vals[0], DEC) + " "
                                    + PDFNumber.doubleOut(vals[1], DEC) + " "
                                    + PDFNumber.doubleOut(vals[2], DEC) + " "
                                    + PDFNumber.doubleOut(vals[3], DEC) + " "
                                    + PDFNumber.doubleOut(vals[4], DEC) + " "
                                    + PDFNumber.doubleOut(vals[5], DEC) + " c\n");
                break;
            case PathIterator.SEG_LINETO:
                currentStream.write(PDFNumber.doubleOut(vals[0], DEC) + " "
                                    + PDFNumber.doubleOut(vals[1], DEC) + " l\n");
                break;
            case PathIterator.SEG_MOVETO:
                currentStream.write(PDFNumber.doubleOut(vals[0], DEC) + " "
                                    + PDFNumber.doubleOut(vals[1], DEC) + " m\n");
                break;
            case PathIterator.SEG_QUADTO:
                currentStream.write(PDFNumber.doubleOut(vals[0], DEC) + " "
                                    + PDFNumber.doubleOut(vals[1], DEC) + " "
                                    + PDFNumber.doubleOut(vals[2], DEC) + " "
                                    + PDFNumber.doubleOut(vals[3], DEC) + " y\n");
                break;
            case PathIterator.SEG_CLOSE:
                currentStream.write("h\n");
                break;
            default:
                break;
            }
            iter.next();
        }
    }
    
    /**
     * Do the PDF drawing command.
     * This does the PDF drawing command according to fill
     * stroke and winding rule.
     *
     * @param fill true if filling the path
     * @param stroke true if stroking the path
     * @param nonzero true if using the non-zero winding rule
     */
    protected void doDrawing(boolean fill, boolean stroke, boolean nonzero) {
        preparePainting();
        if (fill) {
            if (stroke) {
                if (nonzero) {
                    currentStream.write("B*\n");
                } else {
                    currentStream.write("B\n");
                }
            } else {
                if (nonzero) {
                    currentStream.write("f*\n");
                } else {
                    currentStream.write("f\n");
                }
            }
        } else {
            // if (stroke)
            currentStream.write("S\n");
        }
    }

    /**
     * Returns the device configuration associated with this
     * <code>Graphics2D</code>.
     *
     * @return the PDF graphics configuration
     */
    public GraphicsConfiguration getDeviceConfiguration() {
        return new PDFGraphicsConfiguration();
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
        //NYI
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
        //NYI
    }

}
