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
import java.awt.Transparency;
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
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.AbstractIFPainter;
import org.apache.fop.render.intermediate.BorderPainter;
import org.apache.fop.render.intermediate.GraphicsPainter;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFState;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.CharUtilities;
import org.apache.fop.util.HexEncoder;

/**
 * IFPainter implementation that produces PostScript.
 */
public class PSPainter extends AbstractIFPainter<PSDocumentHandler> {

    /** logging instance */
    private static Log log = LogFactory.getLog(PSPainter.class);

    private final GraphicsPainter graphicsPainter;

    private BorderPainter borderPainter;

    private boolean inTextMode;

    /**
     * Default constructor.
     * @param documentHandler the parent document handler
     */
    public PSPainter(PSDocumentHandler documentHandler) {
        this(documentHandler, IFState.create());
    }

    protected PSPainter(PSDocumentHandler documentHandler, IFState state) {
        super(documentHandler);
        this.graphicsPainter = new PSGraphicsPainter(getGenerator());
        this.borderPainter = new BorderPainter(graphicsPainter);
        this.state = state;
    }

    private PSGenerator getGenerator() {
        return getDocumentHandler().getGenerator();
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
    public void startGroup(AffineTransform transform, String layer) throws IFException {
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
        return new PSRenderingContext(
                getUserAgent(), getGenerator(), getFontInfo());
    }

    /** {@inheritDoc} */
    protected void drawImageUsingImageHandler(ImageInfo info, Rectangle rect)
            throws ImageException, IOException {
        if (!getDocumentHandler().getPSUtil().isOptimizeResources()
                || PSImageUtils.isImageInlined(info,
                        (PSRenderingContext)createRenderingContext())) {
            super.drawImageUsingImageHandler(info, rect);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Image " + info + " is embedded as a form later");
            }
            //Don't load image at this time, just put a form placeholder in the stream
            PSResource form = getDocumentHandler().getFormForImage(info.getOriginalURI());
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
    public void clipBackground(Rectangle rect,
            BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd) throws IFException {

        try {
            borderPainter.clipBackground(rect,
                    bpsBefore,  bpsAfter, bpsStart,  bpsEnd);
        } catch (IOException ioe) {
            throw new IFException("I/O error while clipping background", ioe);
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
                if (fill.getTransparency() != Transparency.OPAQUE) {
                    PSPainterUtil.drawTransparency(generator, rect, fill);
                } else {
                    generator.defineRect(rect.x / 1000.0, rect.y / 1000.0, rect.width / 1000.0, rect.height / 1000.0);
                    generator.writeln(generator.mapCommand("fill"));
                }
            } catch (IOException ioe) {
                throw new IFException("I/O error in fillRect()", ioe);
            }
        }
    }

    /** {@inheritDoc} */
    public void drawBorderRect(Rectangle rect, BorderProps top, BorderProps bottom,
            BorderProps left, BorderProps right, Color innerBackgroundColor) throws IFException {
        if (top != null || bottom != null || left != null || right != null) {
            try {
                endTextObject();
                if (getDocumentHandler().getPSUtil().getRenderingMode() == PSRenderingMode.SIZE
                    && hasOnlySolidBorders(top, bottom, left, right)) {
                    super.drawBorderRect(rect, top, bottom, left, right, innerBackgroundColor);
                } else {
                    this.borderPainter.drawBorders(rect, top, bottom, left, right, innerBackgroundColor);
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
            this.graphicsPainter.drawLine(start, end, width, color, style);
        } catch (IOException ioe) {
            throw new IFException("I/O error in drawLine()", ioe);
        }
    }

    private Typeface getTypeface(String fontName) {
        if (fontName == null) {
            throw new NullPointerException("fontName must not be null");
        }
        Typeface tf = getFontInfo().getFonts().get(fontName);
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
            //Do not draw text if font-size is 0 as it creates an invalid PostScript file
            if (state.getFontSize() == 0) {
                return;
            }
            PSGenerator generator = getGenerator();
            generator.useColor(state.getTextColor());
            FontTriplet triplet = new FontTriplet(state.getFontFamily(), state.getFontStyle(), state.getFontWeight());
            String fontKey = getFontKey(triplet);
            Typeface typeface = getTypeface(fontKey);
            if (typeface instanceof MultiByteFont && ((MultiByteFont) typeface).hasSVG()) {
                boolean success = drawSVGText((MultiByteFont) typeface, triplet, x, y, text, state);
                if (success) {
                    return;
                }
            }
            beginTextObject();
            //TODO Ignored: state.getFontVariant()
            //TODO Opportunity for font caching if font state is more heavily used
            int sizeMillipoints = state.getFontSize();

            // This assumes that *all* CIDFonts use a /ToUnicode mapping
            SingleByteFont singleByteFont = null;
            if (typeface instanceof SingleByteFont) {
                singleByteFont = (SingleByteFont)typeface;
            }
            Font font = getFontInfo().getFontInstance(triplet, sizeMillipoints);

            PSFontResource res = getDocumentHandler().getPSResourceForFontKey(fontKey);
            boolean isOpenTypeFont = typeface instanceof MultiByteFont && ((MultiByteFont)typeface).isOTFFile();
            useFont(fontKey, sizeMillipoints, isOpenTypeFont);

            if (dp != null && dp[0] != null) {
                x += dp[0][0];
                y -= dp[0][1];
            }
            generator.writeln("1 0 0 -1 " + formatMptAsPt(generator, x)
                    + " " + formatMptAsPt(generator, y) + " Tm");

            int textLen = text.length();
            int start = 0;
            if (singleByteFont != null) {
                //Analyze string and split up in order to paint in different sub-fonts/encodings
                int currentEncoding = -1;
                for (int i = 0; i < textLen; i++) {
                    char c = text.charAt(i);
                    char mapped = typeface.mapChar(c);
                    int encoding = mapped / 256;
                    if (currentEncoding != encoding) {
                        if (i > 0) {
                            writeText(text, start, i - start,
                                    letterSpacing, wordSpacing, dp, font, typeface, false);
                        }
                        if (encoding == 0) {
                            useFont(fontKey, sizeMillipoints, false);
                        } else {
                            useFont(fontKey + "_" + Integer.toString(encoding), sizeMillipoints, false);
                        }
                        currentEncoding = encoding;
                        start = i;
                    }
                }
            } else {
                if (typeface instanceof MultiByteFont && ((MultiByteFont)typeface).isOTFFile()) {
                    //Analyze string and split up in order to paint in different sub-fonts/encodings
                    int curEncoding = 0;
                    for (int i = start; i < textLen; i++) {
                        char orgChar = text.charAt(i);

                        MultiByteFont mbFont = (MultiByteFont)typeface;
                        mbFont.mapChar(orgChar);
                        int origGlyphIdx = mbFont.findGlyphIndex(orgChar);
                        int newGlyphIdx = mbFont.getUsedGlyphs().get(origGlyphIdx);
                        int encoding = newGlyphIdx / 256;
                        if (encoding != curEncoding) {
                            if (i != 0) {
                                writeText(text, start, i - start, letterSpacing, wordSpacing, dp, font, typeface,
                                        true);
                                start = i;
                            }
                            generator.useFont("/" + res.getName() + "." + encoding, sizeMillipoints / 1000f);
                            curEncoding = encoding;
                        }
                    }
                } else {
                    useFont(fontKey, sizeMillipoints, false);
                }
            }
            writeText(text, start, textLen - start, letterSpacing, wordSpacing, dp, font, typeface,
                    typeface instanceof MultiByteFont);
        } catch (IOException ioe) {
            throw new IFException("I/O error in drawText()", ioe);
        }
    }

    private void writeText(String text, int start, int len,
            int letterSpacing, int wordSpacing, int[][] dp,
            Font font, Typeface tf, boolean multiByte) throws IOException {
        PSGenerator generator = getGenerator();
        int end = start + len;
        int initialSize = len;
        initialSize += initialSize / 2;

        boolean hasLetterSpacing = (letterSpacing != 0);
        boolean needTJ = false;

        int lineStart = 0;
        StringBuffer accText = new StringBuffer(initialSize);
        StringBuffer sb = new StringBuffer(initialSize);
        boolean isOTF = multiByte && ((MultiByteFont)tf).isOTFFile();
        for (int i = start; i < end; i++) {
            int orgChar = text.charAt(i);
            int ch;
            int cw;
            int xGlyphAdjust = 0;
            int yGlyphAdjust = 0;

            if (CharUtilities.isFixedWidthSpace(orgChar)) {
                //Fixed width space are rendered as spaces so copy/paste works in a reader
                ch = font.mapChar(CharUtilities.SPACE);
                cw = font.getCharWidth(orgChar);
                xGlyphAdjust = font.getCharWidth(ch) - cw;
            } else {
                if ((wordSpacing != 0) && CharUtilities.isAdjustableSpace(orgChar)) {
                    xGlyphAdjust -= wordSpacing;
                }

                // surrogate pairs have to be merged in a single code point
                if (CharUtilities.containsSurrogatePairAt(text, i)) {
                    orgChar = Character.toCodePoint((char) orgChar, text.charAt(++i));
                }

                ch = font.mapCodePoint(orgChar);
            }

            if (dp != null && i < dp.length && dp[i] != null) {
                // get x advancement adjust
                xGlyphAdjust -= dp[i][2] - dp[i][0];
                yGlyphAdjust += dp[i][3] - dp[i][1];
            }
            if (dp != null && i < dp.length - 1 && dp[i + 1] != null) {
                // get x placement adjust for next glyph
                xGlyphAdjust -= dp[i + 1][0];
                yGlyphAdjust += dp[i + 1][1];
            }
            if (!multiByte || isOTF) {
                char codepoint = (char)(ch % 256);
                if (isOTF) {
                    accText.append(HexEncoder.encode(codepoint, 2));
                } else {
                    PSGenerator.escapeChar(codepoint, accText); //add character to accumulated text
                }
            } else {
                accText.append(HexEncoder.encode(ch));
            }
            if (xGlyphAdjust != 0 || yGlyphAdjust != 0) {
                needTJ = true;
                if (sb.length() == 0) {
                    sb.append('['); //Need to start TJ
                }
                if (accText.length() > 0) {
                    if ((sb.length() - lineStart + accText.length()) > 200) {
                        sb.append(PSGenerator.LF);
                        lineStart = sb.length();
                    }
                    lineStart = writePostScriptString(sb, accText, multiByte, lineStart);
                    sb.append(' ');
                    accText.setLength(0); //reset accumulated text
                }
                if (yGlyphAdjust == 0) {
                    sb.append(Integer.toString(xGlyphAdjust)).append(' ');
                } else {
                    sb.append('[');
                    sb.append(Integer.toString(yGlyphAdjust)).append(' ');
                    sb.append(Integer.toString(xGlyphAdjust)).append(']').append(' ');
                }
            }
        }
        if (needTJ) {
            if (accText.length() > 0) {
                if ((sb.length() - lineStart + accText.length()) > 200) {
                    sb.append(PSGenerator.LF);
                }
                writePostScriptString(sb, accText, multiByte);
            }
            if (hasLetterSpacing) {
                sb.append("] " + formatMptAsPt(generator, letterSpacing) + " ATJ");
            } else {
                sb.append("] TJ");
            }
        } else {
            writePostScriptString(sb, accText, multiByte);
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

    private void writePostScriptString(StringBuffer buffer, StringBuffer string,
            boolean multiByte) {
        writePostScriptString(buffer, string, multiByte, 0);
    }

    private int writePostScriptString(StringBuffer buffer, StringBuffer string, boolean multiByte,
            int lineStart) {
        buffer.append(multiByte ? '<' : '(');
        int l = string.length();
        int index = 0;
        int maxCol = 200;
        buffer.append(string.substring(index, Math.min(index + maxCol, l)));
        index += maxCol;
        while (index < l) {
            if (!multiByte) {
                buffer.append('\\');
            }
            buffer.append(PSGenerator.LF);
            lineStart = buffer.length();
            buffer.append(string.substring(index, Math.min(index + maxCol, l)));
            index += maxCol;
        }
        buffer.append(multiByte ? '>' : ')');
        return lineStart;
    }

    private void useFont(String key, int size, boolean otf) throws IOException {
        PSFontResource res = getDocumentHandler().getPSResourceForFontKey(key);
        PSGenerator generator = getGenerator();
        String name = "/" + res.getName();
        if (otf) {
            name += ".0";
        }
        generator.useFont(name, size / 1000f);
        res.notifyResourceUsageOnPage(generator.getResourceTracker());
    }
}
