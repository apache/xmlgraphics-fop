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

package org.apache.fop.render.pdf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.Map;

import org.w3c.dom.Document;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.pdf.PDFTextUtil;
import org.apache.fop.pdf.PDFXObject;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.AbstractIFPainter;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFState;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.CharUtilities;

/**
 * IFPainter implementation that produces PDF.
 */
public class PDFPainter extends AbstractIFPainter {

    /** logging instance */
    private static Log log = LogFactory.getLog(PDFPainter.class);

    private PDFDocumentHandler documentHandler;

    /** Holds the intermediate format state */
    protected IFState state;

    /** The current content generator */
    protected PDFContentGenerator generator;

    private PDFBorderPainter borderPainter;

    /**
     * Default constructor.
     * @param documentHandler the parent document handler
     */
    public PDFPainter(PDFDocumentHandler documentHandler) {
        super();
        this.documentHandler = documentHandler;
        this.generator = documentHandler.generator;
        this.borderPainter = new PDFBorderPainter(this.generator);
        this.state = IFState.create();
    }

    /** {@inheritDoc} */
    protected FOUserAgent getUserAgent() {
        return this.documentHandler.getUserAgent();
    }

    PDFRenderingUtil getPDFUtil() {
        return this.documentHandler.pdfUtil;
    }

    PDFDocument getPDFDoc() {
        return this.documentHandler.pdfDoc;
    }

    FontInfo getFontInfo() {
        return this.documentHandler.getFontInfo();
    }

    /** {@inheritDoc} */
    public void startViewport(AffineTransform transform, Dimension size, Rectangle clipRect)
            throws IFException {
        generator.saveGraphicsState();
        generator.concatenate(generator.toPoints(transform));
        if (clipRect != null) {
            clipRect(clipRect);
        }
    }

    /** {@inheritDoc} */
    public void endViewport() throws IFException {
        generator.restoreGraphicsState();
    }

    /** {@inheritDoc} */
    public void startGroup(AffineTransform transform) throws IFException {
        generator.saveGraphicsState();
        generator.concatenate(generator.toPoints(transform));
    }

    /** {@inheritDoc} */
    public void endGroup() throws IFException {
        generator.restoreGraphicsState();
    }

    /** {@inheritDoc} */
    public void drawImage(String uri, Rectangle rect, Map foreignAttributes) throws IFException {
        PDFXObject xobject = getPDFDoc().getXObject(uri);
        if (xobject != null) {
            placeImage(rect, xobject);
            return;
        }

        drawImageUsingURI(uri, rect);

        flushPDFDoc();
    }

    /** {@inheritDoc} */
    protected RenderingContext createRenderingContext() {
        PDFRenderingContext pdfContext = new PDFRenderingContext(
                getUserAgent(), generator, this.documentHandler.currentPage, getFontInfo());
        return pdfContext;
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
        generator.saveGraphicsState();
        generator.add(format(rect.width) + " 0 0 "
                          + format(-rect.height) + " "
                          + format(rect.x) + " "
                          + format(rect.y + rect.height )
                          + " cm " + xobj.getName() + " Do\n");
        generator.restoreGraphicsState();
    }

    /** {@inheritDoc} */
    public void drawImage(Document doc, Rectangle rect, Map foreignAttributes) throws IFException {
        drawImageUsingDocument(doc, rect);

        flushPDFDoc();
    }

    private void flushPDFDoc() throws IFException {
        // output new data
        try {
            generator.flushPDFDoc();
        } catch (IOException ioe) {
            throw new IFException("I/O error flushing the PDF document", ioe);
        }
    }

    /**
     * Formats a integer value (normally coordinates in millipoints) to a String.
     * @param value the value (in millipoints)
     * @return the formatted value
     */
    protected static String format(int value) {
        return PDFNumber.doubleOut(value / 1000f);
    }

    /** {@inheritDoc} */
    public void clipRect(Rectangle rect) throws IFException {
        generator.endTextObject();
        generator.clipRect(rect);
    }

    /** {@inheritDoc} */
    public void fillRect(Rectangle rect, Paint fill) throws IFException {
        if (fill == null) {
            return;
        }
        generator.endTextObject();
        if (rect.width != 0 && rect.height != 0) {
            if (fill != null) {
                if (fill instanceof Color) {
                    generator.updateColor((Color)fill, true, null);
                } else {
                    throw new UnsupportedOperationException("Non-Color paints NYI");
                }
            }
            StringBuffer sb = new StringBuffer();
            sb.append(format(rect.x)).append(' ');
            sb.append(format(rect.y)).append(' ');
            sb.append(format(rect.width)).append(' ');
            sb.append(format(rect.height)).append(" re");
            if (fill != null) {
                sb.append(" f");
            }
            /* Removed from method signature as it is currently not used
            if (stroke != null) {
                sb.append(" S");
            }*/
            sb.append('\n');
            generator.add(sb.toString());
        }
    }

    /** {@inheritDoc} */
    public void drawBorderRect(Rectangle rect, BorderProps before, BorderProps after,
            BorderProps start, BorderProps end) throws IFException {
        if (before != null || after != null || start != null || end != null) {
            generator.endTextObject();
            this.borderPainter.drawBorders(rect, before, after, start, end);
        }
    }

    /** {@inheritDoc} */
    public void drawLine(Point start, Point end, int width, Color color, RuleStyle style)
        throws IFException {
        generator.endTextObject();
        this.borderPainter.drawLine(start, end, width, color, style);
    }

    private Typeface getTypeface(String fontName) {
        if (fontName == null) {
            throw new NullPointerException("fontName must not be null");
        }
        Typeface tf = (Typeface)getFontInfo().getFonts().get(fontName);
        if (tf instanceof LazyFont) {
            tf = ((LazyFont)tf).getRealFont();
        }
        return tf;
    }

    /** {@inheritDoc} */
    public void drawText(int x, int y, int[] dx, int[] dy, String text) throws IFException {
        //Note: dy is currently ignored
        generator.updateColor(state.getTextColor(), true, null);
        generator.beginTextObject();
        FontTriplet triplet = new FontTriplet(
                state.getFontFamily(), state.getFontStyle(), state.getFontWeight());
        //TODO Ignored: state.getFontVariant()
        //TODO Opportunity for font caching if font state is more heavily used
        String fontKey = getFontInfo().getInternalFontKey(triplet);
        int sizeMillipoints = state.getFontSize();
        float fontSize = sizeMillipoints / 1000f;

        // This assumes that *all* CIDFonts use a /ToUnicode mapping
        Typeface tf = getTypeface(fontKey);
        SingleByteFont singleByteFont = null;
        if (tf instanceof SingleByteFont) {
            singleByteFont = (SingleByteFont)tf;
        }
        Font font = getFontInfo().getFontInstance(triplet, sizeMillipoints);
        String fontName = font.getFontName();

        PDFTextUtil textutil = generator.getTextUtil();
        textutil.updateTf(fontKey, fontSize, tf.isMultiByte());

        textutil.writeTextMatrix(new AffineTransform(1, 0, 0, -1, x / 1000f, y / 1000f));
        int l = text.length();
        int dxl = (dx != null ? dx.length : 0);

        if (dx != null && dxl > 0 && dx[0] != 0) {
            textutil.adjustGlyphTJ(-dx[0] / 10f);
        }
        for (int i = 0; i < l; i++) {
            char orgChar = text.charAt(i);
            char ch;
            float glyphAdjust = 0;
            if (font.hasChar(orgChar)) {
                ch = font.mapChar(orgChar);
                if (singleByteFont != null && singleByteFont.hasAdditionalEncodings()) {
                    int encoding = ch / 256;
                    if (encoding == 0) {
                        textutil.updateTf(fontName, fontSize, tf.isMultiByte());
                    } else {
                        textutil.updateTf(fontName + "_" + Integer.toString(encoding),
                                fontSize, tf.isMultiByte());
                        ch = (char)(ch % 256);
                    }
                }
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
            textutil.writeTJMappedChar(ch);

            if (dx != null && i < dxl - 1) {
                glyphAdjust += dx[i + 1];
            }

            if (glyphAdjust != 0) {
                textutil.adjustGlyphTJ(-glyphAdjust / 10f);
            }

        }
        textutil.writeTJ();
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

}
