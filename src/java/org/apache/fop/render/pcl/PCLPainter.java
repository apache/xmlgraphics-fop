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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Map;
import java.util.Stack;

import org.w3c.dom.Document;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.java2d.GraphicContext;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.pdf.PDFXObject;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.AbstractBinaryWritingIFPainter;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFState;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.CharUtilities;
import org.apache.fop.util.UnitConv;

/**
 * IFPainter implementation that produces PCL.
 */
public class PCLPainter extends AbstractBinaryWritingIFPainter implements PCLConstants {

    /** logging instance */
    private static Log log = LogFactory.getLog(PCLPainter.class);

    /** Holds the intermediate format state */
    protected IFState state;

    /** Utility class for handling all sorts of peripheral tasks around PCL generation. */
    protected PCLRenderingUtil pclUtil;

    /** The PCL generator */
    private PCLGenerator gen;

    private PCLPageDefinition currentPageDefinition;
    private int currentPrintDirection = 0;
    //private GeneralPath currentPath = null;

    private Stack graphicContextStack = new Stack();
    private GraphicContext graphicContext = new GraphicContext();

    /** contains the pageWith of the last printed page */
    private long pageWidth = 0;
    /** contains the pageHeight of the last printed page */
    private long pageHeight = 0;

    /**
     * Default constructor.
     */
    public PCLPainter() {
    }

    /** {@inheritDoc} */
    public boolean supportsPagesOutOfOrder() {
        return true;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_PCL;
    }

    /** {@inheritDoc} */
    public void setUserAgent(FOUserAgent ua) {
        super.setUserAgent(ua);
        this.pclUtil = new PCLRenderingUtil(ua);
    }

    PCLRenderingUtil getPCLUtil() {
        return this.pclUtil;
    }

    /** @return the target resolution */
    protected int getResolution() {
        int resolution = (int)Math.round(getUserAgent().getTargetResolution());
        if (resolution <= 300) {
            return 300;
        } else {
            return 600;
        }
    }

    //----------------------------------------------------------------------------------------------

    /** {@inheritDoc} */
    public void startDocument() throws IFException {
        try {
            if (getUserAgent() == null) {
                throw new IllegalStateException(
                        "User agent must be set before starting PDF generation");
            }
            if (this.outputStream == null) {
                throw new IllegalStateException("OutputStream hasn't been set through setResult()");
            }
            log.debug("Rendering areas to PCL...");
            this.gen = new PCLGenerator(this.outputStream, getResolution());

            if (!pclUtil.isPJLDisabled()) {
                gen.universalEndOfLanguage();
                gen.writeText("@PJL COMMENT Produced by " + getUserAgent().getProducer() + "\n");
                if (getUserAgent().getTitle() != null) {
                    gen.writeText("@PJL JOB NAME = \"" + getUserAgent().getTitle() + "\"\n");
                }
                gen.writeText("@PJL SET RESOLUTION = " + getResolution() + "\n");
                gen.writeText("@PJL ENTER LANGUAGE = PCL\n");
            }
            gen.resetPrinter();
            gen.setUnitOfMeasure(getResolution());
            gen.setRasterGraphicsResolution(getResolution());
        } catch (IOException e) {
            throw new IFException("I/O error in startDocument()", e);
        }
    }

    /** {@inheritDoc} */
    public void endDocumentHeader() throws IFException {
    }

    /** {@inheritDoc} */
    public void endDocument() throws IFException {
        try {
            gen.separateJobs();
            gen.resetPrinter();
            if (!pclUtil.isPJLDisabled()) {
                gen.universalEndOfLanguage();
            }
        } catch (IOException ioe) {
            throw new IFException("I/O error in endDocument()", ioe);
        }
        super.endDocument();
    }

    /** {@inheritDoc} */
    public void startPageSequence(String id) throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void endPageSequence() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void startPage(int index, String name, Dimension size) throws IFException {
        saveGraphicsState();

        try {
            //TODO Add support for paper-source and duplex-mode
            /*
            //Paper source
            String paperSource = page.getForeignAttributeValue(
                    new QName(PCLElementMapping.NAMESPACE, null, "paper-source"));
            if (paperSource != null) {
                gen.selectPaperSource(Integer.parseInt(paperSource));
            }

            // Is Page duplex?
            String pageDuplex = page.getForeignAttributeValue(
                    new QName(PCLElementMapping.NAMESPACE, null, "duplex-mode"));
            if (pageDuplex != null) {
                gen.selectDuplexMode(Integer.parseInt(pageDuplex));
            }*/

            //Page size
            final long pagewidth = size.width;
            final long pageheight = size.height;
            selectPageFormat(pagewidth, pageheight);
        } catch (IOException ioe) {
            throw new IFException("I/O error in startPage()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void startPageContent() throws IFException {
        this.state = IFState.create();
    }

    /** {@inheritDoc} */
    public void endPageContent() throws IFException {
        assert this.state.pop() == null;
        //nop
    }

    /** {@inheritDoc} */
    public void endPage() throws IFException {
        try {
            //Eject page
            gen.formFeed();
            restoreGraphicsState();
        } catch (IOException ioe) {
            throw new IFException("I/O error in endPage()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void startViewport(AffineTransform transform, Dimension size, Rectangle clipRect)
            throws IFException {
        saveGraphicsState();
        try {
            concatenateTransformationMatrix(transform);
            /* PCL cannot clip!
            if (clipRect != null) {
                clipRect(clipRect);
            }*/
        } catch (IOException ioe) {
            throw new IFException("I/O error in startViewport()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void endViewport() throws IFException {
        restoreGraphicsState();
    }

    /** {@inheritDoc} */
    public void startGroup(AffineTransform transform) throws IFException {
        saveGraphicsState();
        try {
            concatenateTransformationMatrix(transform);
        } catch (IOException ioe) {
            throw new IFException("I/O error in startGroup()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void endGroup() throws IFException {
        restoreGraphicsState();
    }

    /** {@inheritDoc} */
    public void drawImage(String uri, Rectangle rect, Map foreignAttributes) throws IFException {
        /*
        PDFXObject xobject = pdfDoc.getXObject(uri);
        if (xobject != null) {
            placeImage(rect, xobject);
            return;
        }

        drawImageUsingURI(uri, rect);

        flushPDFDoc();
        */
    }

    /** {@inheritDoc} */
    protected RenderingContext createRenderingContext() {
        /*
        PCLRenderingContext pdfContext = new PCLRenderingContext(
                getUserAgent(), generator, currentPage, getFontInfo());
        return pdfContext;
        */
        return null;
    }

    /**
     * Places a previously registered image at a certain place on the page.
     * @param x X coordinate
     * @param y Y coordinate
     * @param w width for image
     * @param h height for image
     * @param xobj the image XObject
     */
    private void placeImage(Rectangle rect, PDFXObject xobj) {
        /*
        generator.saveGraphicsState();
        generator.add(format(rect.width) + " 0 0 "
                          + format(-rect.height) + " "
                          + format(rect.x) + " "
                          + format(rect.y + rect.height )
                          + " cm " + xobj.getName() + " Do\n");
        generator.restoreGraphicsState();
        */
    }

    /** {@inheritDoc} */
    public void drawImage(Document doc, Rectangle rect, Map foreignAttributes) throws IFException {
        /*
        drawImageUsingDocument(doc, rect);

        flushPDFDoc();
        */
    }

    /** {@inheritDoc} */
    public void clipRect(Rectangle rect) throws IFException {
        //PCL cannot clip (only HP GL/2 can)
        /*
        generator.endTextObject();
        generator.clipRect(rect);
        */
    }

    /** {@inheritDoc} */
    public void fillRect(Rectangle rect, Paint fill) throws IFException {
        if (fill == null) {
            return;
        }
        if (rect.width != 0 && rect.height != 0) {
            Color fillColor = null;
            if (fill != null) {
                if (fill instanceof Color) {
                    fillColor = (Color)fill;
                } else {
                    throw new UnsupportedOperationException("Non-Color paints NYI");
                }
                try {
                    setCursorPos(rect.x, rect.y);
                    gen.fillRect(rect.width, rect.height, fillColor);
                } catch (IOException ioe) {
                    throw new IFException("I/O error in fillRect()", ioe);
                }
            }
        }
    }

    /** {@inheritDoc} */
    public void drawBorderRect(Rectangle rect, BorderProps before, BorderProps after,
            BorderProps start, BorderProps end) throws IFException {
        if (before != null || after != null || start != null || end != null) {
            /*
            generator.endTextObject();
            this.borderPainter.drawBorders(rect, before, after, start, end);
            */
        }
    }

    /** {@inheritDoc} */
    public void drawLine(Point start, Point end, int width, Color color, RuleStyle style)
            throws IFException {
        /*
        generator.endTextObject();
        this.borderPainter.drawLine(start, end, width, color, style);
        */
    }

    /** {@inheritDoc} */
    public void drawText(int x, int y, int[] dx, int[] dy, String text) throws IFException {
        //Note: dy is currently ignored
        try {
            FontTriplet triplet = new FontTriplet(
                    state.getFontFamily(), state.getFontStyle(), state.getFontWeight());
            //TODO Ignored: state.getFontVariant()
            //TODO Opportunity for font caching if font state is more heavily used
            String fontKey = fontInfo.getInternalFontKey(triplet);
            boolean pclFont = pclUtil.isAllTextAsBitmaps()
                        ? false
                        : setFont(fontKey, state.getFontSize(), text);
            if (true || pclFont) {
                drawTextNative(x, y, dx, text, triplet);
            } else {
                drawTextAsBitmap(x, y, dx, dy, text, triplet);
            }
        } catch (IOException ioe) {
            throw new IFException("I/O error in drawText()", ioe);
        }
    }

    private void drawTextNative(int x, int y, int[] dx, String text, FontTriplet triplet)
                throws IOException {
        Color textColor = state.getTextColor();
        if (textColor != null) {
            gen.setTransparencyMode(true, false);
            gen.selectGrayscale(textColor);
        }

        gen.setTransparencyMode(true, true);
        setCursorPos(x, y);

        float fontSize = state.getFontSize() / 1000f;
        Font font = fontInfo.getFontInstance(triplet, state.getFontSize());
        int l = text.length();
        int dxl = (dx != null ? dx.length : 0);

        StringBuffer sb = new StringBuffer(Math.max(16, l));
        if (dx != null && dxl > 0 && dx[0] != 0) {
            sb.append("\u001B&a+").append(gen.formatDouble2(dx[0] / 100.0)).append('H');
        }
        for (int i = 0; i < l; i++) {
            char orgChar = text.charAt(i);
            char ch;
            float glyphAdjust = 0;
            if (font.hasChar(orgChar)) {
                ch = font.mapChar(orgChar);
            } else {
                if (CharUtilities.isFixedWidthSpace(orgChar)) {
                    //Fixed width space are rendered as spaces so copy/paste works in a reader
                    ch = font.mapChar(CharUtilities.SPACE);
                    int spaceDiff = font.getCharWidth(ch) - font.getCharWidth(orgChar);
                    glyphAdjust = -(10 * spaceDiff / fontSize);
                } else {
                    ch = font.mapChar(orgChar);
                }
            }
            sb.append(ch);

            if (dx != null && i < dxl - 1) {
                glyphAdjust += dx[i + 1];
            }

            if (glyphAdjust != 0) {
                sb.append("\u001B&a+").append(gen.formatDouble2(glyphAdjust / 100.0)).append('H');
            }

        }
        gen.getOutputStream().write(sb.toString().getBytes(gen.getTextEncoding()));

    }

    private void drawTextAsBitmap(int x, int y, int[] dx, int[] dy,
            String text, FontTriplet triplet) throws IOException {
        /*
        //Use Java2D to paint different fonts via bitmap
        final Font font = getFontFromArea(text);
        final int baseline = text.getBaselineOffset();

        //for cursive fonts, so the text isn't clipped
        int extraWidth = font.getFontSize() / 3;
        final FontMetricsMapper mapper = (FontMetricsMapper)fontInfo.getMetricsFor(
                font.getFontName());
        int maxAscent = mapper.getMaxAscent(font.getFontSize()) / 1000;
        final int additionalBPD = maxAscent - baseline;

        Graphics2DAdapter g2a = getGraphics2DAdapter();
        final Rectangle paintRect = new Rectangle(
                rx, currentBPPosition + text.getOffset() - additionalBPD,
                text.getIPD() + extraWidth, text.getBPD() + additionalBPD);
        RendererContext rc = createRendererContext(paintRect.x, paintRect.y,
                paintRect.width, paintRect.height, null);
        Map atts = new java.util.HashMap();
        atts.put(CONV_MODE, "bitmap");
        atts.put(SRC_TRANSPARENCY, "true");
        rc.setProperty(RendererContextConstants.FOREIGN_ATTRIBUTES, atts);

        Graphics2DImagePainter painter = new Graphics2DImagePainter() {

            public void paint(Graphics2D g2d, Rectangle2D area) {
                g2d.setFont(mapper.getFont(font.getFontSize()));
                g2d.translate(0, baseline + additionalBPD);
                g2d.scale(1000, 1000);
                g2d.setColor(col);
                Java2DRenderer.renderText(text, g2d, font);
                renderTextDecoration(g2d, mapper, fontsize, text, 0, 0);
            }

            public Dimension getImageSize() {
                return paintRect.getSize();
            }

        };
        g2a.paintImage(painter, rc,
                paintRect.x, paintRect.y, paintRect.width, paintRect.height);
        currentIPPosition = saveIP + text.getAllocIPD();
        */
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

    /** {@inheritDoc} */
    public void handleExtensionObject(Object extension) throws IFException {
        if (false) {
            //TODO Handle extensions
        } else {
            log.debug("Ignored extension object: "
                    + extension + " (" + extension.getClass().getName() + ")");
        }
    }

    //----------------------------------------------------------------------------------------------

    /** Saves the current graphics state on the stack. */
    private void saveGraphicsState() {
        graphicContextStack.push(graphicContext);
        graphicContext = (GraphicContext)graphicContext.clone();
    }

    /** Restores the last graphics state from the stack. */
    private void restoreGraphicsState() {
        graphicContext = (GraphicContext)graphicContextStack.pop();
    }

    private void concatenateTransformationMatrix(AffineTransform transform) throws IOException {
        if (!transform.isIdentity()) {
            graphicContext.transform(transform);
            changePrintDirection();
        }
    }

    private Point2D transformedPoint(int x, int y) {
        AffineTransform at = graphicContext.getTransform();
        if (log.isTraceEnabled()) {
            log.trace("Current transform: " + at);
        }
        Point2D.Float orgPoint = new Point2D.Float(x, y);
        Point2D.Float transPoint = new Point2D.Float();
        at.transform(orgPoint, transPoint);
        //At this point we have the absolute position in FOP's coordinate system

        //Now get PCL coordinates taking the current print direction and the logical page
        //into account.
        Dimension pageSize = currentPageDefinition.getPhysicalPageSize();
        Rectangle logRect = currentPageDefinition.getLogicalPageRect();
        switch (currentPrintDirection) {
        case 0:
            transPoint.x -= logRect.x;
            transPoint.y -= logRect.y;
            break;
        case 90:
            float ty = transPoint.x;
            transPoint.x = pageSize.height - transPoint.y;
            transPoint.y = ty;
            transPoint.x -= logRect.y;
            transPoint.y -= logRect.x;
            break;
        case 180:
            transPoint.x = pageSize.width - transPoint.x;
            transPoint.y = pageSize.height - transPoint.y;
            transPoint.x -= pageSize.width - logRect.x - logRect.width;
            transPoint.y -= pageSize.height - logRect.y - logRect.height;
            //The next line is odd and is probably necessary due to the default value of the
            //Text Length command: "1/2 inch less than maximum text length"
            //I wonder why this isn't necessary for the 90 degree rotation. *shrug*
            transPoint.y -= UnitConv.in2mpt(0.5);
            break;
        case 270:
            float tx = transPoint.y;
            transPoint.y = pageSize.width - transPoint.x;
            transPoint.x = tx;
            transPoint.x -= pageSize.height - logRect.y - logRect.height;
            transPoint.y -= pageSize.width - logRect.x - logRect.width;
            break;
        default:
            throw new IllegalStateException("Illegal print direction: " + currentPrintDirection);
        }
        return transPoint;
    }

    private void changePrintDirection() throws IOException {
        AffineTransform at = graphicContext.getTransform();
        int newDir;
        newDir = PCLRenderingUtil.determinePrintDirection(at);
        if (newDir != this.currentPrintDirection) {
            this.currentPrintDirection = newDir;
            gen.changePrintDirection(this.currentPrintDirection);
        }
    }

    /**
     * Sets the current cursor position. The coordinates are transformed to the absolute position
     * on the logical PCL page and then passed on to the PCLGenerator.
     * @param x the x coordinate (in millipoints)
     * @param y the y coordinate (in millipoints)
     */
    void setCursorPos(int x, int y) throws IOException {
        Point2D transPoint = transformedPoint(x, y);
        gen.setCursorPos(transPoint.getX(), transPoint.getY());
    }

    private void selectPageFormat(long pagewidth, long pageheight) throws IOException {
        //Only set the page format if it changes (otherwise duplex printing won't work)
        if ((pagewidth != this.pageWidth) || (pageheight != this.pageHeight))  {
            this.pageWidth = pagewidth;
            this.pageHeight = pageheight;

            this.currentPageDefinition = PCLPageDefinition.getPageDefinition(
                    pagewidth, pageheight, 1000);

            if (this.currentPageDefinition == null) {
                this.currentPageDefinition = PCLPageDefinition.getDefaultPageDefinition();
                log.warn("Paper type could not be determined. Falling back to: "
                        + this.currentPageDefinition.getName());
            }
            if (log.isDebugEnabled()) {
                log.debug("page size: " + currentPageDefinition.getPhysicalPageSize());
                log.debug("logical page: " + currentPageDefinition.getLogicalPageRect());
            }

            if (this.currentPageDefinition.isLandscapeFormat()) {
                gen.writeCommand("&l1O"); //Landscape Orientation
            } else {
                gen.writeCommand("&l0O"); //Portrait Orientation
            }
            gen.selectPageSize(this.currentPageDefinition.getSelector());

            gen.clearHorizontalMargins();
            gen.setTopMargin(0);
        }
    }

    /**
     * Sets the current font (NOTE: Hard-coded font mappings ATM!)
     * @param name the font name (internal F* names for now)
     * @param size the font size (in millipoints)
     * @param text the text to be rendered (used to determine if there are non-printable chars)
     * @return true if the font can be mapped to PCL
     * @throws IOException if an I/O problem occurs
     */
    public boolean setFont(String name, int size, String text) throws IOException {
        byte[] encoded = text.getBytes("ISO-8859-1");
        for (int i = 0, c = encoded.length; i < c; i++) {
            if (encoded[i] == 0x3F && text.charAt(i) != '?') {
                return false;
            }
        }
        int fontcode = 0;
        if (name.length() > 1 && name.charAt(0) == 'F') {
            try {
                fontcode = Integer.parseInt(name.substring(1));
            } catch (Exception e) {
                log.error(e);
            }
        }
        //Note "(ON" selects ISO 8859-1 symbol set as used by PCLGenerator
        String formattedSize = gen.formatDouble2(size / 1000.0);
        switch (fontcode) {
        case 1:     // F1 = Helvetica
            // gen.writeCommand("(8U");
            // gen.writeCommand("(s1p" + formattedSize + "v0s0b24580T");
            // Arial is more common among PCL5 printers than Helvetica - so use Arial

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v0s0b16602T");
            break;
        case 2:     // F2 = Helvetica Oblique

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v1s0b16602T");
            break;
        case 3:     // F3 = Helvetica Bold

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v0s3b16602T");
            break;
        case 4:     // F4 = Helvetica Bold Oblique

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v1s3b16602T");
            break;
        case 5:     // F5 = Times Roman
            // gen.writeCommand("(8U");
            // gen.writeCommand("(s1p" + formattedSize + "v0s0b25093T");
            // Times New is more common among PCL5 printers than Times - so use Times New

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v0s0b16901T");
            break;
        case 6:     // F6 = Times Italic

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v1s0b16901T");
            break;
        case 7:     // F7 = Times Bold

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v0s3b16901T");
            break;
        case 8:     // F8 = Times Bold Italic

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v1s3b16901T");
            break;
        case 9:     // F9 = Courier

            gen.writeCommand("(0N");
            gen.writeCommand("(s0p" + gen.formatDouble2(120.01f / (size / 1000.00f))
                    + "h0s0b4099T");
            break;
        case 10:    // F10 = Courier Oblique

            gen.writeCommand("(0N");
            gen.writeCommand("(s0p" + gen.formatDouble2(120.01f / (size / 1000.00f))
                    + "h1s0b4099T");
            break;
        case 11:    // F11 = Courier Bold

            gen.writeCommand("(0N");
            gen.writeCommand("(s0p" + gen.formatDouble2(120.01f / (size / 1000.00f))
                    + "h0s3b4099T");
            break;
        case 12:    // F12 = Courier Bold Oblique

            gen.writeCommand("(0N");
            gen.writeCommand("(s0p" + gen.formatDouble2(120.01f / (size / 1000.00f))
                    + "h1s3b4099T");
            break;
        case 13:    // F13 = Symbol

            return false;
            //gen.writeCommand("(19M");
            //gen.writeCommand("(s1p" + formattedSize + "v0s0b16686T");
            // ECMA Latin 1 Symbol Set in Times Roman???
            // gen.writeCommand("(9U");
            // gen.writeCommand("(s1p" + formattedSize + "v0s0b25093T");
            //break;
        case 14:    // F14 = Zapf Dingbats

            return false;
            //gen.writeCommand("(14L");
            //gen.writeCommand("(s1p" + formattedSize + "v0s0b45101T");
            //break;
        default:
            //gen.writeCommand("(0N");
            //gen.writeCommand("(s" + formattedSize + "V");
            return false;
        }
        return true;
    }

}
