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

package org.apache.fop.render.ps;

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

import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageProcessingHints;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSResource;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.AbstractIFPainter;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFState;
import org.apache.fop.render.intermediate.IFUtil;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.CharUtilities;

/**
 * IFPainter implementation that produces PostScript.
 */
public class PSPainter extends AbstractIFPainter {

    /** logging instance */
    private static Log log = LogFactory.getLog(PSPainter.class);

    private PSDocumentHandler documentHandler;
    private PSBorderPainter borderPainter;

    private boolean inTextMode = false;

    /**
     * Default constructor.
     * @param documentHandler the parent document handler
     */
    public PSPainter(PSDocumentHandler documentHandler) {
        super();
        this.documentHandler = documentHandler;
        this.borderPainter = new PSBorderPainter(documentHandler.gen);
        this.state = IFState.create();
    }

    /** {@inheritDoc} */
    protected IFContext getContext() {
        return this.documentHandler.getContext();
    }

    PSRenderingUtil getPSUtil() {
        return this.documentHandler.psUtil;
    }

    FontInfo getFontInfo() {
        return this.documentHandler.getFontInfo();
    }

    private PSGenerator getGenerator() {
        return this.documentHandler.gen;
    }

    /** {@inheritDoc} */
    public void startViewport(AffineTransform transform, Dimension size, Rectangle clipRect)
            throws IFException {
        try {
            PSGenerator generator = getGenerator();
            saveGraphicsState();
            generator.concatMatrix(toPoints(transform));
        } catch (IOException ioe) {
            throw new IFException("I/O error in startViewport()", ioe);
        }
        if (clipRect != null) {
            clipRect(clipRect);
        }
    }

    /** {@inheritDoc} */
    public void endViewport() throws IFException {
        try {
            restoreGraphicsState();
        } catch (IOException ioe) {
            throw new IFException("I/O error in endViewport()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void startGroup(AffineTransform transform) throws IFException {
        try {
            PSGenerator generator = getGenerator();
            saveGraphicsState();
            generator.concatMatrix(toPoints(transform));
        } catch (IOException ioe) {
            throw new IFException("I/O error in startGroup()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void endGroup() throws IFException {
        try {
            restoreGraphicsState();
        } catch (IOException ioe) {
            throw new IFException("I/O error in endGroup()", ioe);
        }
    }

    /** {@inheritDoc} */
    protected Map createDefaultImageProcessingHints(ImageSessionContext sessionContext) {
        Map hints = super.createDefaultImageProcessingHints(sessionContext);

        //PostScript doesn't support alpha channels
        hints.put(ImageProcessingHints.TRANSPARENCY_INTENT,
                ImageProcessingHints.TRANSPARENCY_INTENT_IGNORE);
        //TODO We might want to support image masks in the future.
        return hints;
    }

    /** {@inheritDoc} */
    protected RenderingContext createRenderingContext() {
        PSRenderingContext psContext = new PSRenderingContext(
                getUserAgent(), getGenerator(), getFontInfo());
        return psContext;
    }

    /** {@inheritDoc} */
    protected void drawImageUsingImageHandler(ImageInfo info, Rectangle rect)
            throws ImageException, IOException {
        if (!getPSUtil().isOptimizeResources()
                || PSImageUtils.isImageInlined(info,
                        (PSRenderingContext)createRenderingContext())) {
            super.drawImageUsingImageHandler(info, rect);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Image " + info + " is embedded as a form later");
            }
            //Don't load image at this time, just put a form placeholder in the stream
            PSResource form = documentHandler.getFormForImage(info.getOriginalURI());
            PSImageUtils.drawForm(form, info, rect, getGenerator());
        }
    }

    /** {@inheritDoc} */
    public void drawImage(String uri, Rectangle rect) throws IFException {
        try {
            endTextObject();
        } catch (IOException ioe) {
            throw new IFException("I/O error in drawImage()", ioe);
        }
        drawImageUsingURI(uri, rect);
    }

    /** {@inheritDoc} */
    public void drawImage(Document doc, Rectangle rect) throws IFException {
        try {
            endTextObject();
        } catch (IOException ioe) {
            throw new IFException("I/O error in drawImage()", ioe);
        }
        drawImageUsingDocument(doc, rect);
    }

    /** {@inheritDoc} */
    public void clipRect(Rectangle rect) throws IFException {
        try {
            PSGenerator generator = getGenerator();
            endTextObject();
            generator.defineRect(rect.x / 1000.0, rect.y / 1000.0,
                    rect.width / 1000.0, rect.height / 1000.0);
            generator.writeln(generator.mapCommand("clip") + " " + generator.mapCommand("newpath"));
        } catch (IOException ioe) {
            throw new IFException("I/O error in clipRect()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void fillRect(Rectangle rect, Paint fill) throws IFException {
        if (fill == null) {
            return;
        }
        if (rect.width != 0 && rect.height != 0) {
            try {
                endTextObject();
                PSGenerator generator = getGenerator();
                if (fill != null) {
                    if (fill instanceof Color) {
                        generator.useColor((Color)fill);
                    } else {
                        throw new UnsupportedOperationException("Non-Color paints NYI");
                    }
                }
                generator.defineRect(rect.x / 1000.0, rect.y / 1000.0,
                        rect.width / 1000.0, rect.height / 1000.0);
                generator.writeln(generator.mapCommand("fill"));
            } catch (IOException ioe) {
                throw new IFException("I/O error in fillRect()", ioe);
            }
        }
    }

    /** {@inheritDoc} */
    public void drawBorderRect(Rectangle rect, BorderProps before, BorderProps after,
            BorderProps start, BorderProps end) throws IFException {
        if (before != null || after != null || start != null || end != null) {
            try {
                endTextObject();
                if (getPSUtil().getRenderingMode() == PSRenderingMode.SIZE
                    && hasOnlySolidBorders(before, after, start, end)) {
                    super.drawBorderRect(rect, before, after, start, end);
                } else {
                    this.borderPainter.drawBorders(rect, before, after, start, end);
                }
            } catch (IOException ioe) {
                throw new IFException("I/O error in drawBorderRect()", ioe);
            }
        }
    }

    /** {@inheritDoc} */
    public void drawLine(Point start, Point end, int width, Color color, RuleStyle style)
                throws IFException {
        try {
            endTextObject();
            this.borderPainter.drawLine(start, end, width, color, style);
        } catch (IOException ioe) {
            throw new IFException("I/O error in drawLine()", ioe);
        }
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

    /**
     * Saves the graphics state of the rendering engine.
     * @throws IOException if an I/O error occurs
     */
    protected void saveGraphicsState() throws IOException {
        endTextObject();
        getGenerator().saveGraphicsState();
    }

    /**
     * Restores the last graphics state of the rendering engine.
     * @throws IOException if an I/O error occurs
     */
    protected void restoreGraphicsState() throws IOException {
        endTextObject();
        getGenerator().restoreGraphicsState();
    }

    /**
     * Indicates the beginning of a text object.
     * @throws IOException if an I/O error occurs
     */
    protected void beginTextObject() throws IOException {
        if (!inTextMode) {
            PSGenerator generator = getGenerator();
            generator.saveGraphicsState();
            generator.writeln("BT");
            inTextMode = true;
        }
    }

    /**
     * Indicates the end of a text object.
     * @throws IOException if an I/O error occurs
     */
    protected void endTextObject() throws IOException {
        if (inTextMode) {
            inTextMode = false;
            PSGenerator generator = getGenerator();
            generator.writeln("ET");
            generator.restoreGraphicsState();
        }
    }

    private String formatMptAsPt(PSGenerator gen, int value) {
        return gen.formatDouble(value / 1000.0);
    }

    /* Disabled: performance experiment (incomplete)

    private static final String ZEROS = "0.00";

    private String formatMptAsPt1(int value) {
        String s = Integer.toString(value);
        int len = s.length();
        StringBuffer sb = new StringBuffer();
        if (len < 4) {
            sb.append(ZEROS.substring(0, 5 - len));
            sb.append(s);
        } else {
            int dec = len - 3;
            sb.append(s.substring(0, dec));
            sb.append('.');
            sb.append(s.substring(dec));
        }
        return sb.toString();
    }*/

    /** {@inheritDoc} */
    public void drawText(int x, int y, int letterSpacing, int wordSpacing,
            int[][] dp, String text) throws IFException {
        try {
            PSGenerator generator = getGenerator();
            generator.useColor(state.getTextColor());
            beginTextObject();
            FontTriplet triplet = new FontTriplet(
                    state.getFontFamily(), state.getFontStyle(), state.getFontWeight());
            //TODO Ignored: state.getFontVariant()
            //TODO Opportunity for font caching if font state is more heavily used
            String fontKey = getFontInfo().getInternalFontKey(triplet);
            if (fontKey == null) {
                throw new IFException("Font not available: " + triplet, null);
            }
            int sizeMillipoints = state.getFontSize();

            // This assumes that *all* CIDFonts use a /ToUnicode mapping
            Typeface tf = getTypeface(fontKey);
            SingleByteFont singleByteFont = null;
            if (tf instanceof SingleByteFont) {
                singleByteFont = (SingleByteFont)tf;
            }
            Font font = getFontInfo().getFontInstance(triplet, sizeMillipoints);

            useFont(fontKey, sizeMillipoints);

            generator.writeln("1 0 0 -1 " + formatMptAsPt(generator, x)
                    + " " + formatMptAsPt(generator, y) + " Tm");

            int textLen = text.length();
            if (singleByteFont != null && singleByteFont.hasAdditionalEncodings()) {
                //Analyze string and split up in order to paint in different sub-fonts/encodings
                int start = 0;
                int currentEncoding = -1;
                for (int i = 0; i < textLen; i++) {
                    char c = text.charAt(i);
                    char mapped = tf.mapChar(c);
                    int encoding = mapped / 256;
                    if (currentEncoding != encoding) {
                        if (i > 0) {
                            writeText(text, start, i - start,
                                    letterSpacing, wordSpacing, dp, font, tf);
                        }
                        if (encoding == 0) {
                            useFont(fontKey, sizeMillipoints);
                        } else {
                            useFont(fontKey + "_" + Integer.toString(encoding), sizeMillipoints);
                        }
                        currentEncoding = encoding;
                        start = i;
                    }
                }
                writeText(text, start, textLen - start,
                        letterSpacing, wordSpacing, dp, font, tf);
            } else {
                //Simple single-font painting
                useFont(fontKey, sizeMillipoints);
                writeText(text, 0, textLen,
                        letterSpacing, wordSpacing, dp, font, tf);
            }
        } catch (IOException ioe) {
            throw new IFException("I/O error in drawText()", ioe);
        }
    }

    private void writeText(                                      // CSOK: ParameterNumber
            String text, int start, int len,
            int letterSpacing, int wordSpacing, int[][] dp,
            Font font, Typeface tf) throws IOException {
        PSGenerator generator = getGenerator();
        int end = start + len;
        int initialSize = len;
        initialSize += initialSize / 2;

        boolean hasLetterSpacing = (letterSpacing != 0);
        boolean needTJ = false;

        int lineStart = 0;
        StringBuffer accText = new StringBuffer(initialSize);
        StringBuffer sb = new StringBuffer(initialSize);
        int[] dx = IFUtil.convertDPToDX ( dp );
        int dxl = (dx != null ? dx.length : 0);
        for (int i = start; i < end; i++) {
            char orgChar = text.charAt(i);
            char ch;
            int cw;
            int glyphAdjust = 0;
            if (CharUtilities.isFixedWidthSpace(orgChar)) {
                //Fixed width space are rendered as spaces so copy/paste works in a reader
                ch = font.mapChar(CharUtilities.SPACE);
                cw = font.getCharWidth(orgChar);
                glyphAdjust = font.getCharWidth(ch) - cw;
            } else {
                if ((wordSpacing != 0) && CharUtilities.isAdjustableSpace(orgChar)) {
                    glyphAdjust -= wordSpacing;
                }
                ch = font.mapChar(orgChar);
                cw = font.getCharWidth(orgChar);
            }

            if (dx != null && i < dxl - 1) {
                glyphAdjust -= dx[i + 1];
            }
            char codepoint = (char)(ch % 256);
            PSGenerator.escapeChar(codepoint, accText); //add character to accumulated text
            if (glyphAdjust != 0) {
                needTJ = true;
                if (sb.length() == 0) {
                    sb.append('['); //Need to start TJ
                }
                if (accText.length() > 0) {
                    if ((sb.length() - lineStart + accText.length()) > 200) {
                        sb.append(PSGenerator.LF);
                        lineStart = sb.length();
                    }
                    sb.append('(');
                    sb.append(accText);
                    sb.append(") ");
                    accText.setLength(0); //reset accumulated text
                }
                sb.append(Integer.toString(glyphAdjust)).append(' ');
            }
        }
        if (needTJ) {
            if (accText.length() > 0) {
                sb.append('(');
                sb.append(accText);
                sb.append(')');
            }
            if (hasLetterSpacing) {
                sb.append("] " + formatMptAsPt(generator, letterSpacing) + " ATJ");
            } else {
                sb.append("] TJ");
            }
        } else {
            sb.append('(').append(accText).append(")");
            if (hasLetterSpacing) {
                StringBuffer spb = new StringBuffer();
                spb.append(formatMptAsPt(generator, letterSpacing))
                    .append(" 0 ");
                sb.insert(0, spb.toString());
                sb.append(" " + generator.mapCommand("ashow"));
            } else {
                sb.append(" " + generator.mapCommand("show"));
            }
        }
        generator.writeln(sb.toString());
    }

    private void useFont(String key, int size) throws IOException {
        PSResource res = this.documentHandler.getPSResourceForFontKey(key);
        PSGenerator generator = getGenerator();
        generator.useFont("/" + res.getName(), size / 1000f);
        generator.getResourceTracker().notifyResourceUsageOnPage(res);
    }


}
