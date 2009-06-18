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
package org.apache.fop.render.pdf;

// FOP
import org.apache.fop.render.PrintRenderer;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.FopImageException;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.TextAlign;
import org.apache.fop.fo.properties.VerticalAlign;
import org.apache.fop.fo.properties.Scaling;
import org.apache.fop.fo.properties.Overflow;
import org.apache.fop.layout.inline.ForeignObjectArea;
import org.apache.fop.layout.inline.WordArea;
import org.apache.fop.svg.SVGArea;
import org.apache.fop.svg.PDFTextPainter;
import org.apache.fop.svg.PDFAElementBridge;
import org.apache.fop.svg.PDFGraphics2D;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFResources;
import org.apache.fop.pdf.PDFStream;
import org.apache.fop.pdf.PDFAnnotList;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFPathPaint;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.pdf.PDFOutline;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.Page;
import org.apache.fop.layout.LinkSet;
import org.apache.fop.layout.LinkedRectangle;
import org.apache.fop.image.SVGImage;
import org.apache.fop.extensions.Outline;
import org.apache.fop.extensions.ExtensionObj;
import org.apache.fop.extensions.Destination;
import org.apache.fop.render.pdf.fonts.LazyFont;

import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.gvt.TextPainter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.renderer.StrokingTextPainter;

import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.awt.geom.AffineTransform;

/**
 * <p>
 *
 * Renderer that renders areas to PDF.</p> <p>
 *
 * Modified by Mark Lillywhite, mark-fop@inomial.com to use the new Renderer
 * interface. The PDF renderer is by far the trickiest renderer and the best
 * supported by FOP. It also required some reworking in the way that Pages,
 * Catalogs and the Root object were written to the stream. The output document
 * should now still be a 100% compatible PDF document, but the order of the
 * document writing is significantly different. See also the changes to
 * PDFPage, PDFPages and PDFRoot.</p>
 */
public class PDFRenderer extends PrintRenderer {

    /**
     * the PDF Document being created
     */
    protected PDFDocument pdfDoc;

    /**
     * the /Resources object of the PDF document being created
     */
    protected PDFResources pdfResources;

    /**
     * the current stream to add PDF commands to
     */
    PDFStream currentStream;

    /**
     * the current annotation list to add annotations to
     */
    PDFAnnotList currentAnnotList;

    /**
     * the current page to add annotations to
     */
    PDFPage currentPage;

    PDFColor currentColor;

    float currentLetterSpacing = Float.MAX_VALUE;

    /**
     * true if a TJ command is left to be written
     */
    boolean textOpen = false;

    /**
     * the previous Y coordinate of the last word written. Used to decide if we
     * can draw the next word on the same line.
     */
    int prevWordY = 0;

    /**
     * the previous X coordinate of the last word written. used to calculate
     * how much space between two words
     */
    int prevWordX = 0;

    /**
     * The width of the previous word. Used to calculate space between
     */
    int prevWordWidth = 0;

    /**
     * reusable word area string buffer to reduce memory usage
     */
    private StringBuffer _wordAreaPDF = new StringBuffer();

    /**
     * options
     */
    protected java.util.Map options;

    protected List extensions = null;

    /**
     * create the PDF renderer
     */
    public PDFRenderer() {
        this.pdfDoc = new PDFDocument();
    }

    /**
     * set up renderer options
     *
     * @param options  Options for the renderer
     */
    public void setOptions(java.util.Map options) {
        this.options = options;

        // Process encryption options, if any exist
        boolean encrypt = false;
        String oPassword = "";
        String uPassword = "";
        boolean allowPrint           = true;
        boolean allowCopyContent     = true;
        boolean allowEditContent     = true;
        boolean allowEditAnnotations = true;
        String option;

        option = (String) options.get("ownerPassword");
        if (option != null) {
            encrypt = true;
            oPassword = option;
        }
        option = (String) options.get("userPassword");
        if (option != null) {
            encrypt = true;
            uPassword = option;
        }
        option = (String) options.get("allowPrint");
        if (option != null) {
            encrypt = true;
            allowPrint = option.equals("TRUE");
        }
        option = (String) options.get("allowCopyContent");
        if (option != null) {
            encrypt = true;
            allowCopyContent = option.equals("TRUE");
        }
        option = (String) options.get("allowEditContent");
        if (option != null) {
            encrypt = true;
            allowEditContent = option.equals("TRUE");
        }
        option = (String) options.get("allowEditAnnotations");
        if (option != null) {
            encrypt = true;
            allowEditAnnotations = option.equals("TRUE");
        }
        if (encrypt) {
            this.pdfDoc.setEncryption(oPassword,uPassword,allowPrint,allowCopyContent,
                                      allowEditContent, allowEditAnnotations);
        }
    }

    /**
     * set the PDF document's producer
     *
     * @param producer  string indicating application producing PDF
     */
    public void setProducer(String producer) {
        this.pdfDoc.setProducer(producer);
    }

    /**
     * Starts the renderer
     *
     * @param stream           OutputStream to be written to
     * @exception IOException  In case of an IO problem
     */
    public void startRenderer(OutputStream stream)
        throws IOException {
        pdfDoc.outputHeader(stream);
    }

    /**
     * Called when the renderer has finished its work
     *
     * @param stream           OutputStream to be written to
     * @exception IOException  In cas of an IO problem
     */
    public void stopRenderer(OutputStream stream)
        throws IOException {
        renderRootExtensions(extensions);
        FontSetup.addToResources(this.pdfDoc, fontInfo);
        pdfDoc.outputTrailer(stream);

        // this frees up memory and makes the renderer reusable
        this.pdfDoc = new PDFDocument();
        this.pdfResources = null;
        extensions = null;
        currentStream = null;
        currentAnnotList = null;
        currentPage = null;
        currentColor = null;
        super.stopRenderer(stream);
    }

    /**
     * add a line to the current stream
     *
     * @param x1      the start x location in millipoints
     * @param y1      the start y location in millipoints
     * @param x2      the end x location in millipoints
     * @param y2      the end y location in millipoints
     * @param th      the thickness in millipoints
     * @param stroke  the stroke color/gradient
     */
    protected void addLine(int x1, int y1, int x2, int y2, int th,
            PDFPathPaint stroke) {
        closeText();

        currentStream.add("ET\nq\n" + stroke.getColorSpaceOut(false)
                + (x1 / 1000f) + " " + (y1 / 1000f) + " m "
                + (x2 / 1000f) + " " + (y2 / 1000f) + " l "
                + (th / 1000f) + " w S\n" + "Q\nBT\n");
    }

    /**
     * add a line to the current stream
     *
     * @param x1      the start x location in millipoints
     * @param y1      the start y location in millipoints
     * @param x2      the end x location in millipoints
     * @param y2      the end y location in millipoints
     * @param th      the thickness in millipoints
     * @param rs      the rule style
     * @param stroke  the stroke color/gradient
     */
    protected void addLine(int x1, int y1, int x2, int y2, int th, int rs,
            PDFPathPaint stroke) {
        closeText();
        currentStream.add("ET\nq\n" + stroke.getColorSpaceOut(false)
                + setRuleStylePattern(rs) + (x1 / 1000f) + " "
                + (y1 / 1000f) + " m " + (x2 / 1000f) + " "
                + (y2 / 1000f) + " l " + (th / 1000f) + " w S\n"
                + "Q\nBT\n");
    }

    /**
     * add a rectangle to the current stream
     *
     * @param x       the x position of left edge in millipoints
     * @param y       the y position of top edge in millipoints
     * @param w       the width in millipoints
     * @param h       the height in millipoints
     * @param stroke  the stroke color/gradient
     */
    protected void addRect(int x, int y, int w, int h, PDFPathPaint stroke) {
        closeText();
        currentStream.add("ET\nq\n" + stroke.getColorSpaceOut(false)
                + (x / 1000f) + " " + (y / 1000f) + " "
                + (w / 1000f) + " " + (h / 1000f) + " re s\n"
                + "Q\nBT\n");
    }

    /**
     * add a filled rectangle to the current stream
     *
     * @param x       the x position of left edge in millipoints
     * @param y       the y position of top edge in millipoints
     * @param w       the width in millipoints
     * @param h       the height in millipoints
     * @param fill    the fill color/gradient
     * @param stroke  the stroke color/gradient
     */
    protected void addRect(int x, int y, int w, int h, PDFPathPaint stroke,
            PDFPathPaint fill) {
        closeText();
        currentStream.add("ET\nq\n" + fill.getColorSpaceOut(true)
                + stroke.getColorSpaceOut(false) + (x / 1000f)
                + " " + (y / 1000f) + " " + (w / 1000f) + " "
                + (h / 1000f) + " re b\n" + "Q\nBT\n");
    }

    /**
     * add a filled rectangle to the current stream
     *
     * @param x     the x position of left edge in millipoints
     * @param y     the y position of top edge in millipoints
     * @param w     the width in millipoints
     * @param h     the height in millipoints
     * @param fill  the fill color/gradient
     */
    protected void addFilledRect(int x, int y, int w, int h,
            PDFPathPaint fill) {
        closeText();
        currentStream.add("ET\nq\n" + fill.getColorSpaceOut(true)
                + (x / 1000f) + " " + (y / 1000f) + " "
                + (w / 1000f) + " " + (h / 1000f) + " re f\n"
                + "Q\nBT\n");
    }

    /**
     * Renders an image, scaling it to the given width and height. If the
     * scaled width and height is the same intrinsic size of the image, the
     * image is not scaled.
     *
     * @param x      the x position of left edge in millipoints
     * @param y      the y position of top edge in millipoints
     * @param w      the width in millipoints
     * @param h      the height in millipoints
     * @param image  the image to be rendered
     * @param fs     the font state to use when rendering text in non-bitmapped
     *      images.
     */
    protected void drawImageScaled(int x, int y, int w, int h,
            FopImage image,
            FontState fs) {
        if (image instanceof SVGImage) {
            try {
                closeText();

                SVGDocument svg = ((SVGImage) image).getSVGDocument();
                currentStream.add("ET\nq\n");
                renderSVGDocument(svg, x, y, fs);
                currentStream.add("Q\nBT\n");
            } catch (FopImageException e) {}

        } else {
            int xObjectNum = this.pdfDoc.addImage(image);
            closeText();
            currentStream.add("ET\nq\n" + (((float) w) / 1000f) + " 0 0 "
                    + (((float) h) / 1000f) + " "
                    + (((float) x) / 1000f) + " "
                    + (((float) y - h) / 1000f) + " cm\n" + "/Im"
                    + xObjectNum + " Do\nQ\nBT\n");
        }
    }

    /**
     * Renders an image, clipping it as specified.
     *
     * @param x      the x position of left edge in millipoints.
     * @param y      the y position of top edge in millipoints.
     * @param clipX  the left edge of the clip in millipoints
     * @param clipY  the top edge of the clip in millipoints
     * @param clipW  the clip width in millipoints
     * @param clipH  the clip height in millipoints
     * @param fs     the font state to use when rendering text in non-bitmapped
     *      images.
     * @param image  the image to be painted
     */
    protected void drawImageClipped(int x, int y,
            int clipX, int clipY,
            int clipW, int clipH,
            FopImage image,
            FontState fs) {

        float cx1 = ((float) x) / 1000f;
        float cy1 = ((float) y - clipH) / 1000f;

        float cx2 = ((float) x + clipW) / 1000f;
        float cy2 = ((float) y) / 1000f;

        int imgX = x - clipX;
        int imgY = y - clipY;

        int imgW;
        int imgH;
        try {
            // XXX: do correct unit conversion here..
            imgW = image.getWidth() * 1000;
            imgH = image.getHeight() * 1000;
        } catch (FopImageException fie) {
            log.error("Error obtaining image width and height", fie);
            return;
        }

        if (image instanceof SVGImage) {
            try {
                closeText();

                SVGDocument svg = ((SVGImage) image).getSVGDocument();
                currentStream.add("ET\nq\n" +
                // clipping
                        cx1 + " " + cy1 + " m\n" +
                        cx2 + " " + cy1 + " l\n" +
                        cx2 + " " + cy2 + " l\n" +
                        cx1 + " " + cy2 + " l\n" +
                        "W\n" +
                        "n\n");
                renderSVGDocument(svg, imgX, imgY, fs);
                currentStream.add("Q\nBT\n");
            } catch (FopImageException e) {}

        } else {
            int xObjectNum = this.pdfDoc.addImage(image);
            closeText();
            currentStream.add("ET\nq\n" +
            // clipping
                    cx1 + " " + cy1 + " m\n" +
                    cx2 + " " + cy1 + " l\n" +
                    cx2 + " " + cy2 + " l\n" +
                    cx1 + " " + cy2 + " l\n" +
                    "W\n" +
                    "n\n" +
            // image matrix
                    (((float) imgW) / 1000f) + " 0 0 " +
                    (((float) imgH) / 1000f) + " " +
                    (((float) imgX) / 1000f) + " " +
                    (((float) imgY - imgH) / 1000f) + " cm\n" +
                    "s\n" +
            // the image itself
                    "/Im" + xObjectNum + " Do\nQ\nBT\n");
        }
    }

    /**
     * render a foreign object area
     *
     * @param area  the foreign object area to be rendered
     */
    public void renderForeignObjectArea(ForeignObjectArea area) {
        // if necessary need to scale and align the content
        this.currentXPosition = this.currentXPosition + area.getXOffset();
        this.currentYPosition = this.currentYPosition;
        switch (area.getAlign()) {
        case TextAlign.START:
            break;
        case TextAlign.END:
            break;
        case TextAlign.CENTER:
        case TextAlign.JUSTIFY:
            break;
        }
        switch (area.getVerticalAlign()) {
        case VerticalAlign.BASELINE:
            break;
        case VerticalAlign.MIDDLE:
            break;
        case VerticalAlign.SUB:
            break;
        case VerticalAlign.SUPER:
            break;
        case VerticalAlign.TEXT_TOP:
            break;
        case VerticalAlign.TEXT_BOTTOM:
            break;
        case VerticalAlign.TOP:
            break;
        case VerticalAlign.BOTTOM:
            break;
        }
        closeText();

        // in general the content will not be text
        currentStream.add("ET\n");
        // align and scale
        currentStream.add("q\n");
        switch (area.scalingMethod()) {
        case Scaling.UNIFORM:
            break;
        case Scaling.NON_UNIFORM:
            break;
        }
        // if the overflow is auto (default), scroll or visible
        // then the contents should not be clipped, since this
        // is considered a printing medium.
        switch (area.getOverflow()) {
        case Overflow.VISIBLE:
        case Overflow.SCROLL:
        case Overflow.AUTO:
            break;
        case Overflow.HIDDEN:
            break;
        }

        area.getObject().render(this);
        currentStream.add("Q\n");
        currentStream.add("BT\n");
        this.currentXPosition += area.getEffectiveWidth();
        // this.currentYPosition -= area.getEffectiveHeight();
    }

    /**
     * render SVG area to PDF
     *
     * @param area  the SVG area to render
     */
    public void renderSVGArea(SVGArea area) {
        // place at the current instream offset
        int x = this.currentXPosition;
        int y = this.currentYPosition;
        renderSVGDocument(area.getSVGDocument(), x, y, area.getFontState());
    }

    /**
     * render SVG document to PDF
     *
     * @param doc  the document to render
     * @param x    the x offset
     * @param y    the y offset
     * @param fs   the fontstate to use
     */
    protected void renderSVGDocument(Document doc, int x, int y,
            FontState fs) {
        float sx = 1;
        float sy = -1;
        int xOffset = x;
        int yOffset = y;

        org.apache.fop.svg.SVGUserAgent userAgent
                 = new org.apache.fop.svg.SVGUserAgent(new AffineTransform());
        userAgent.setLogger(log);

        GVTBuilder builder = new GVTBuilder();
        BridgeContext ctx = new BridgeContext(userAgent);
        TextPainter textPainter = null;
        Boolean bl =
                org.apache.fop.configuration.Configuration.getBooleanValue("strokeSVGText");
        if (bl == null || bl.booleanValue()) {
            textPainter = new StrokingTextPainter();
        } else {
            textPainter = new PDFTextPainter(fs);
        }
        ctx.setTextPainter(textPainter);

        PDFAElementBridge aBridge = new PDFAElementBridge();
        aBridge.setCurrentTransform(new AffineTransform(sx, 0, 0,
                sy, xOffset / 1000f, yOffset / 1000f));
        ctx.putBridge(aBridge);

        GraphicsNode root;
        try {
            root = builder.build(ctx, doc);
        } catch (Exception e) {
            log.error("svg graphic could not be built: "
                    + e.getMessage(), e);
            return;
        }
        // get the 'width' and 'height' attributes of the SVG document
        float w = (float) ctx.getDocumentSize().getWidth() * 1000f;
        float h = (float) ctx.getDocumentSize().getHeight() * 1000f;
        ctx = null;
        builder = null;

        /*
         * Clip to the svg area.
         * Note: To have the svg overlay (under) a text area then use
         * an fo:block-container
         */
        currentStream.add("q\n");
        if (w != 0 && h != 0) {
            currentStream.add(x / 1000f + " " + y / 1000f + " m\n");
            currentStream.add((x + w) / 1000f + " " + y / 1000f + " l\n");
            currentStream.add((x + w) / 1000f + " " + (y - h) / 1000f
                    + " l\n");
            currentStream.add(x / 1000f + " " + (y - h) / 1000f + " l\n");
            currentStream.add("h\n");
            currentStream.add("W\n");
            currentStream.add("n\n");
        }
        // transform so that the coordinates (0,0) is from the top left
        // and positive is down and to the right. (0,0) is where the
        // viewBox puts it.
        currentStream.add(sx + " 0 0 " + sy + " " + xOffset / 1000f + " "
                + yOffset / 1000f + " cm\n");

        SVGSVGElement svg = ((SVGDocument) doc).getRootElement();
        AffineTransform at = ViewBox.getPreserveAspectRatioTransform(svg,
                w / 1000f, h / 1000f);
        if (!at.isIdentity()) {
            double[] vals = new double[6];
            at.getMatrix(vals);
            currentStream.add(PDFNumber.doubleOut(vals[0], 8) + " "
                    + PDFNumber.doubleOut(vals[1], 8) + " "
                    + PDFNumber.doubleOut(vals[2], 8) + " "
                    + PDFNumber.doubleOut(vals[3], 8) + " "
                    + PDFNumber.doubleOut(vals[4], 8) + " "
                    + PDFNumber.doubleOut(vals[5], 8) + " cm\n");
        }

        PDFGraphics2D graphics = new PDFGraphics2D(true, fs, pdfDoc,
                currentFontName,
                currentFontSize,
                currentXPosition,
                currentYPosition);
        graphics.setGraphicContext(
                new org.apache.batik.ext.awt.g2d.GraphicContext());

        try {
            root.paint(graphics);
            currentStream.add(graphics.getString());
        } catch (Exception e) {
            log.error("svg graphic could not be rendered: "
                    + e.getMessage(), e);
        }

        currentAnnotList = graphics.getAnnotList();

        currentStream.add("Q\n");
    }

    /**
     * render inline area to PDF
     *
     * @param area  inline area to render
     */
    public void renderWordArea(WordArea area) {
        synchronized (_wordAreaPDF) {
            StringBuffer pdf = _wordAreaPDF;
            pdf.setLength(0);

            Map kerning = null;
            boolean kerningAvailable = false;

            kerning = area.getFontState().getKerning();
            if (kerning != null && !kerning.isEmpty()) {
                kerningAvailable = true;
            }

            String name = area.getFontState().getFontName();
            int size = area.getFontState().getFontSize();

            // This assumes that *all* CIDFonts use a /ToUnicode mapping
            boolean useMultiByte = false;
            Font f = (Font) area.getFontState().
                    getFontInfo().getFonts().get(name);
            if (f instanceof LazyFont) {
                if (((LazyFont) f).getRealFont() instanceof CIDFont) {
                    useMultiByte = true;
                }
            } else if (f instanceof CIDFont) {
                useMultiByte = true;
            }
            // String startText = useMultiByte ? "<FEFF" : "(";
            String startText = useMultiByte ? "<" : "(";
            String endText = useMultiByte ? "> " : ") ";

            if ((!name.equals(this.currentFontName))
                    || (size != this.currentFontSize)) {
                closeText();

                this.currentFontName = name;
                this.currentFontSize = size;
                pdf = pdf.append("/" + name + " " + ((float)size / 1000) + " Tf\n");
            }

            //Do letter spacing (must be outside of [..] TJ)
            float letterspacing =
                    ((float) area.getFontState().getLetterSpacing()) / 1000;
            if (letterspacing != this.currentLetterSpacing) {
                this.currentLetterSpacing = letterspacing;
                closeText();
                pdf.append(letterspacing).append(" Tc\n");
            }

            PDFColor areaColor = null;
            if (this.currentFill instanceof PDFColor) {
                areaColor = (PDFColor) this.currentFill;
            }

            if (areaColor == null || areaColor.red() != (double) area.getRed()
                    || areaColor.green() != (double) area.getGreen()
                    || areaColor.blue() != (double) area.getBlue()) {

                areaColor = new PDFColor((double) area.getRed(),
                        (double) area.getGreen(),
                        (double) area.getBlue());

                closeText();
                this.currentFill = areaColor;
                pdf.append(this.currentFill.getColorSpaceOut(true));
            }

            int rx = this.currentXPosition;
            int bl = this.currentYPosition;

            addWordLines(area, rx, bl, size, areaColor);

            if (!textOpen || bl != prevWordY) {
                closeText();

                pdf.append("1 0 0 1 " + (rx / 1000f) + " " + (bl / 1000f)
                        + " Tm [" + startText);
                prevWordY = bl;
                textOpen = true;
            } else {
                // express the space between words in thousandths of an em
                int space = prevWordX - rx + prevWordWidth;
                float emDiff = (float) space / (float) currentFontSize * 1000f;
                // this prevents a problem in Acrobat Reader where large
                // numbers cause text to disappear or default to a limit
                if (emDiff < -33000) {
                    closeText();

                    pdf.append("1 0 0 1 " + (rx / 1000f) + " " + (bl / 1000f)
                            + " Tm [" + startText);
                    textOpen = true;
                } else {
                    pdf.append(Float.toString(emDiff));
                    pdf.append(" ");
                    pdf.append(startText);
                }
            }
            prevWordWidth = area.getContentWidth();
            prevWordX = rx;

            String s = area.getText();
            int l = s.length();

            for (int i = 0; i < l; i++) {
                char ch = area.getFontState().mapChar(s.charAt(i));

                if (!useMultiByte) {
                    if (ch > 127) {
                        pdf.append("\\");
                        pdf.append(Integer.toOctalString((int) ch));

                    } else {
                        switch (ch) {
                        case '(':
                        case ')':
                        case '\\':
                            pdf.append("\\");
                            break;
                        }
                        pdf.append(ch);
                    }
                } else {
                    pdf.append(getUnicodeString(ch));
                }

                if (kerningAvailable && (i + 1) < l) {
                    addKerning(pdf, (new Integer((int) ch)),
                            (new Integer((int) area.getFontState().mapChar(s.charAt(i + 1)))),
                            kerning, startText, endText);
                }
            }
            pdf.append(endText);

            currentStream.add(pdf.toString());

            this.currentXPosition += area.getContentWidth();

        }
    }

    /**
     * Convert a char to a multibyte hex representation
     *
     * @param c  character to be converted
     * @return   the string representation of the character
     */
    private String getUnicodeString(char c) {

        StringBuffer buf = new StringBuffer(4);

        byte[] uniBytes = null;
        try {
            char[] a = {c};
            uniBytes = new String(a).getBytes("UnicodeBigUnmarked");
        } catch (Exception e) {
            // This should never fail
            throw new org.apache.avalon.framework.CascadingRuntimeException("Incompatible VM", e);
        }

        for (int i = 0; i < uniBytes.length; i++) {
            int b = (uniBytes[i] < 0) ? (int) (256 + uniBytes[i])
                    : (int) uniBytes[i];

            String hexString = Integer.toHexString(b);
            if (hexString.length() == 1) {
                buf = buf.append("0" + hexString);
            } else {
                buf = buf.append(hexString);
            }
        }

        return buf.toString();
    }

    /**
     * Checks to see if we have some text rendering commands open still and
     * writes out the TJ command to the stream if we do
     */
    private void closeText() {
        if (textOpen) {
            currentStream.add("] TJ\n");
            textOpen = false;
            prevWordX = 0;
            prevWordY = 0;
        }
    }

    private void addKerning(StringBuffer buf, Integer ch1, Integer ch2,
            Map kerning, String startText,
            String endText) {
        Map kernPair = (Map) kerning.get(ch1);

        if (kernPair != null) {
            Integer width = (Integer) kernPair.get(ch2);
            if (width != null) {
                buf.append(endText).append(-(width.intValue())).
                        append(' ').append(startText);
            }
        }
    }


    /**
     * render page to PDF
     *
     * @param page              the page render
     * @param outputStream      the target OutputStream
     * @exception FOPException  in case of an internal problem
     * @exception IOException   in case of an IO problem
     */
    public void render(Page page, OutputStream outputStream)
        throws FOPException, IOException {
        // log.debug("rendering single page to PDF");
        this.idReferences = page.getIDReferences();
        this.pdfResources = this.pdfDoc.getResources();
        this.pdfDoc.setIDReferences(idReferences);
        this.renderPage(page);

        List exts = page.getExtensions();
        if (exts != null) {
            extensions = exts;
        }

        // log.debug("writing out PDF");
        this.pdfDoc.output(outputStream);
    }

    /**
     * render page into PDF
     *
     * @param page  page to render
     */
    public void renderPage(Page page) {
        currentStream = this.pdfDoc.makeStream();

        this.currentFontName = "";
        this.currentFontSize = 0;

        currentStream.add("BT\n");

        renderRegions(page);

        closeText();

        float w = page.getWidth();
        float h = page.getHeight();
        currentStream.add("ET\n");

        currentPage = this.pdfDoc.makePage(this.pdfResources, currentStream,
                Math.round(w / 1000),
                Math.round(h / 1000), page);

        if (page.hasLinks() || currentAnnotList != null) {
            if (currentAnnotList == null) {
                currentAnnotList = this.pdfDoc.makeAnnotList();
            }
            currentPage.setAnnotList(currentAnnotList);

            List linkSets = page.getLinkSets();
            for (int i = 0; i < linkSets.size(); i++) {
                LinkSet linkSet = (LinkSet)linkSets.get(i);

                linkSet.align();
                String dest = linkSet.getDest();
                int linkType = linkSet.getLinkType();
                List linkRects = linkSet.getRects();
                for (int j = 0; j < linkRects.size(); j++) {
                    LinkedRectangle lrect = (LinkedRectangle)linkRects.get(j);
                    currentAnnotList.addLink(
                            this.pdfDoc.makeLink(lrect.getRectangle(),
                            dest, linkType));
                }
            }
            currentAnnotList = null;
        } else {
            // just to be on the safe side
            currentAnnotList = null;
        }

        // ensures that color is properly reset for blocks that carry over pages
        this.currentFill = null;
    }

    /**
     * defines a string containing dashArray and dashPhase for the rule style
     *
     * @param style  the rule style
     * @return       PDF code to setup the rule style
     */
    private String setRuleStylePattern(int style) {
        String rs = "";
        switch (style) {
        case org.apache.fop.fo.properties.RuleStyle.SOLID:
            rs = "[] 0 d ";
            break;
        case org.apache.fop.fo.properties.RuleStyle.DASHED:
            rs = "[3 3] 0 d ";
            break;
        case org.apache.fop.fo.properties.RuleStyle.DOTTED:
            rs = "[1 3] 0 d ";
            break;
        case org.apache.fop.fo.properties.RuleStyle.DOUBLE:
            rs = "[] 0 d ";
            break;
        default:
            rs = "[] 0 d ";
        }
        return rs;
    }

    /**
     * render root extensions such as outlines
     *
     * @param exts  the list of root extensions to process
     */
    protected void renderRootExtensions(List extensions) {
        if (extensions != null) {
            for (int i = 0; i < extensions.size(); i++) {
                ExtensionObj ext = (ExtensionObj) extensions.get(i);
                if (ext instanceof Outline) {
                    renderOutline((Outline) ext);
                } else if (ext instanceof Destination) {
                    Destination d = (Destination)ext;
                    pdfDoc.addDestination(d.getDestinationName(), d.getInternalDestination());
                }
            }
        }
    }

    private void renderOutline(Outline outline) {
        PDFOutline outlineRoot = pdfDoc.getOutlineRoot();
        PDFOutline pdfOutline = null;
        Outline parent = outline.getParentOutline();
        if (parent == null) {
            pdfOutline =
                    this.pdfDoc.makeOutline(outlineRoot,
                    outline.getLabel().toString(),
                    outline.getInternalDestination());
        } else {
            PDFOutline pdfParentOutline =
                    (PDFOutline) parent.getRendererObject();
            if (pdfParentOutline == null) {
                log.error("pdfParentOutline is null");
            } else {
                pdfOutline =
                        this.pdfDoc.makeOutline(pdfParentOutline,
                        outline.getLabel().toString(),
                        outline.getInternalDestination());
            }

        }
        outline.setRendererObject(pdfOutline);

        // handle sub outlines
        List v = outline.getOutlines();
        for (int i = 0; i < v.size(); i++) {
            renderOutline((Outline) v.get(i));
        }
    }

}
