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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.w3c.dom.Document;

import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;

import org.apache.fop.ResourceEventProducer;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFName;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.PDFTextUtil;
import org.apache.fop.pdf.PDFXObject;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.AbstractIFPainter;
import org.apache.fop.render.intermediate.BorderPainter;
import org.apache.fop.render.intermediate.GraphicsPainter;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFState;
import org.apache.fop.render.intermediate.IFUtil;
import org.apache.fop.render.pdf.PDFLogicalStructureHandler.MarkedContentInfo;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.Direction;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.CharUtilities;

/**
 * IFPainter implementation that produces PDF.
 */
public class PDFPainter extends AbstractIFPainter<PDFDocumentHandler> {

    /** The current content generator */
    protected PDFContentGenerator generator;

    private final GraphicsPainter graphicsPainter;

    private final BorderPainter borderPainter;

    private boolean accessEnabled;

    private MarkedContentInfo imageMCI;

    private PDFLogicalStructureHandler logicalStructureHandler;

    private final LanguageAvailabilityChecker languageAvailabilityChecker;

    private AffineTransform viewportContentArea;

    private static class LanguageAvailabilityChecker {

        private final IFContext context;

        private final Set<String> reportedLocations = new HashSet<String>();

        LanguageAvailabilityChecker(IFContext context) {
            this.context = context;
        }

        private void checkLanguageAvailability(String text) {
            Locale locale = context.getLanguage();
            if (locale == null && containsLettersOrDigits(text)) {
                String location = context.getLocation();
                if (!reportedLocations.contains(location)) {
                    PDFEventProducer.Provider.get(context.getUserAgent().getEventBroadcaster())
                            .unknownLanguage(this, location);
                    reportedLocations.add(location);
                }
            }
        }

        private boolean containsLettersOrDigits(String text) {
            for (int i = 0; i < text.length(); i++) {
                if (Character.isLetterOrDigit(text.charAt(i))) {
                    return true;
                }
            }
            return false;
        }

    }

    /**
     * Default constructor.
     * @param documentHandler the parent document handler
     * @param logicalStructureHandler the logical structure handler
     */
    public PDFPainter(PDFDocumentHandler documentHandler,
            PDFLogicalStructureHandler logicalStructureHandler) {
        super(documentHandler);
        this.logicalStructureHandler = logicalStructureHandler;
        this.generator = documentHandler.getGenerator();
        this.graphicsPainter = new PDFGraphicsPainter(this.generator);
        this.borderPainter = new BorderPainter(this.graphicsPainter);
        this.state = IFState.create();
        accessEnabled = this.getUserAgent().isAccessibilityEnabled();
        languageAvailabilityChecker = accessEnabled
                ? new LanguageAvailabilityChecker(documentHandler.getContext())
                : null;
    }

    /** {@inheritDoc} */
    public void startViewport(AffineTransform transform, Dimension size, Rectangle clipRect)
            throws IFException {
        generator.saveGraphicsState();
        generator.concatenate(toPoints(transform));
        this.viewportContentArea = transform;
        if (clipRect != null) {
            clipRect(clipRect);
        }
    }

    /** {@inheritDoc} */
    public void endViewport() throws IFException {
        generator.restoreGraphicsState();
    }

    /** {@inheritDoc} */
    public void startGroup(AffineTransform transform, String layer) throws IFException {
        generator.saveGraphicsState(layer);
        generator.concatenate(toPoints(transform));
    }

    /** {@inheritDoc} */
    public void endGroup() throws IFException {
        generator.restoreGraphicsState();
    }

    /** {@inheritDoc} */
    public void drawImage(String uri, Rectangle rect)
            throws IFException {
        PDFXObject xobject = getDocumentHandler().getPDFDocument().getXObject(uri);
        addStructTreeBBox(rect);
        if (xobject != null) {
            if (accessEnabled) {
                PDFStructElem structElem = (PDFStructElem) getContext().getStructureTreeElement();
                prepareImageMCID(structElem);
                placeImageAccess(rect, xobject);
            } else {
                placeImage(rect, xobject);
            }
        } else {
            drawImageUsingURI(uri, rect);
            if (!getDocumentHandler().getPDFDocument().isLinearizationEnabled()) {
                flushPDFDoc();
            }
        }
    }

    private void addStructTreeBBox(Rectangle rect) {
        if (accessEnabled && getDocumentHandler().getPDFDocument().getProfile().getPDFUAMode().isEnabled()) {
            PDFStructElem structElem = (PDFStructElem) getContext().getStructureTreeElement();
            if (structElem != null) { //structElem is null if the image is marked as an artifact
                PDFDictionary d = new PDFDictionary();
                int x = rect.x / 1000;
                int y = (getDocumentHandler().getCurrentPageRef().getPageDimension().height - rect.y) / 1000;
                int w = rect.width / 1000;
                int h = rect.height / 1000;

                if (viewportContentArea != null) {
                    x += (int) viewportContentArea.getTranslateX() / 1000;
                    y -= (int) viewportContentArea.getTranslateY() / 1000;
                }

                d.put("BBox", new PDFArray(x, y - h, x + w, y));
                d.put("O", new PDFName("Layout"));
                structElem.put("A", d);
            }
        }
    }

    @Override
    protected void drawImageUsingURI(String uri, Rectangle rect) {
        ImageManager manager = getUserAgent().getImageManager();
        ImageInfo info = null;
        try {
            ImageSessionContext sessionContext = getUserAgent().getImageSessionContext();
            info = manager.getImageInfo(uri, sessionContext);
            if (accessEnabled) {
                PDFStructElem structElem = (PDFStructElem) getContext().getStructureTreeElement();
                String mimeType = info.getMimeType();
                if (!mimeType.equalsIgnoreCase("application/pdf")) {
                    prepareImageMCID(structElem);
                }
            }
            drawImageUsingImageHandler(info, rect);
        } catch (ImageException ie) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageError(this, (info != null ? info.toString() : uri), ie, null);
        } catch (FileNotFoundException fe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageNotFound(this, (info != null ? info.toString() : uri), fe, null);
        } catch (IOException ioe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageIOError(this, (info != null ? info.toString() : uri), ioe, null);
        }
    }

    private void prepareImageMCID(PDFStructElem structElem) {
        imageMCI = logicalStructureHandler.addImageContentItem(structElem);
        if (structElem != null) {
            languageAvailabilityChecker.checkLanguageAvailability((String) structElem.get("Alt"));
        }
    }

    /** {@inheritDoc} */
    @Override
    protected RenderingContext createRenderingContext() {
        PDFRenderingContext pdfContext = new PDFRenderingContext(
                getUserAgent(), generator, getDocumentHandler().getCurrentPage(), getFontInfo());
        pdfContext.setMarkedContentInfo(imageMCI);
        pdfContext.setUsedFieldNames(getDocumentHandler().getUsedFieldNames());
        pdfContext.setPageNumbers(getDocumentHandler().getPageNumbers());
        pdfContext.setPdfLogicalStructureHandler(logicalStructureHandler);
        pdfContext.setCurrentSessionStructElem((PDFStructElem) getContext().getStructureTreeElement());
        return pdfContext;
    }

    /**
     * Places a previously registered image at a certain place on the page.
     * @param rect the rectangle for the image
     * @param xobj the image XObject
     */
    private void placeImage(Rectangle rect, PDFXObject xobj) {
        generator.saveGraphicsState();
        generator.add(format(rect.width) + " 0 0 "
                          + format(-rect.height) + " "
                          + format(rect.x) + " "
                          + format(rect.y + rect.height)
                          + " cm " + xobj.getName() + " Do\n");
        generator.restoreGraphicsState();
    }
    /**
     * Places a previously registered image at a certain place on the page - Accessibility version
     * @param rect the rectangle for the image
     * @param xobj the image XObject
     */
    private void placeImageAccess(Rectangle rect, PDFXObject xobj) {
        generator.saveGraphicsState(imageMCI.tag, imageMCI.mcid);
        generator.add(format(rect.width) + " 0 0 "
                          + format(-rect.height) + " "
                          + format(rect.x) + " "
                          + format(rect.y + rect.height)
                          + " cm " + xobj.getName() + " Do\n");
        generator.restoreGraphicsStateAccess();
    }

    /** {@inheritDoc} */
    public void drawImage(Document doc, Rectangle rect) throws IFException {
        if (accessEnabled) {
            PDFStructElem structElem = (PDFStructElem) getContext().getStructureTreeElement();
            prepareImageMCID(structElem);
            addStructTreeBBox(rect);
        }
        drawImageUsingDocument(doc, rect);
        if (!getDocumentHandler().getPDFDocument().isLinearizationEnabled()) {
            flushPDFDoc();
        }
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
            generator.endTextObject();
            if (accessEnabled && getUserAgent().isPdfUAEnabled()) {
                generator.beginMarkedContentSequence(null, 0, null);
            }
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
            if (accessEnabled && getUserAgent().isPdfUAEnabled()) {
                generator.endMarkedContentSequence();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void drawBorderRect(Rectangle rect, BorderProps top, BorderProps bottom,
            BorderProps left, BorderProps right, Color innerBackgroundColor) throws IFException {
        if (top != null || bottom != null || left != null || right != null) {
            generator.endTextObject();
            if (accessEnabled && getUserAgent().isPdfUAEnabled()) {
                generator.beginMarkedContentSequence(null, 0, null);
            }
            this.borderPainter.drawBorders(rect, top, bottom, left, right, innerBackgroundColor);
            if (accessEnabled && getUserAgent().isPdfUAEnabled()) {
                generator.endMarkedContentSequence();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void drawLine(Point start, Point end, int width, Color color, RuleStyle style)
        throws IFException {
        generator.endTextObject();
        if (accessEnabled && getUserAgent().isPdfUAEnabled()) {
            generator.beginMarkedContentSequence(null, 0, null);
        }
        try {
            this.graphicsPainter.drawLine(start, end, width, color, style);
        } catch (IOException ioe) {
            throw new IFException("Cannot draw line", ioe);
        }
        if (accessEnabled && getUserAgent().isPdfUAEnabled()) {
            generator.endMarkedContentSequence();
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

    /** {@inheritDoc} */
    public void drawText(int x, int y, int letterSpacing, int wordSpacing, int[][] dp,
            String text)
            throws IFException {
        if (accessEnabled) {
            PDFStructElem structElem = (PDFStructElem) getContext().getStructureTreeElement();
            languageAvailabilityChecker.checkLanguageAvailability(text);
            MarkedContentInfo mci = logicalStructureHandler.addTextContentItem(structElem);
            String actualText = getContext().isHyphenated() ? text.substring(0, text.length() - 1) : null;
            generator.endTextObject();
            generator.updateColor(state.getTextColor(), true, null);
            generator.beginTextObject(mci.tag, mci.mcid, actualText);
        } else {
            generator.updateColor(state.getTextColor(), true, null);
            generator.beginTextObject();
        }

        FontTriplet triplet = new FontTriplet(
                state.getFontFamily(), state.getFontStyle(), state.getFontWeight());
        String fontKey = getFontInfo().getInternalFontKey(triplet);
        Typeface typeface = getTypeface(fontKey);
        if (typeface instanceof MultiByteFont && ((MultiByteFont) typeface).hasSVG()) {
            boolean success = drawSVGText((MultiByteFont) typeface, triplet, x, y, text, state);
            if (success) {
                return;
            }
        }
        if ((dp == null) || IFUtil.isDPOnlyDX(dp)) {
            drawTextWithDX(x, y, text, triplet, letterSpacing,
                             wordSpacing, IFUtil.convertDPToDX(dp));
        } else {
            drawTextWithDP(x, y, text, triplet, letterSpacing,
                             wordSpacing, dp);
        }
    }

    public void drawText(int x, int y, int letterSpacing, int wordSpacing, int[][] dp, String text, boolean nextIsSpace)
            throws IFException {
        if (accessEnabled && nextIsSpace) {
            text += ' ';
        }
        drawText(x, y, letterSpacing, wordSpacing, dp, text);
    }

    private void drawTextWithDX(int x, int y, String text, FontTriplet triplet,
            int letterSpacing, int wordSpacing, int[] dx) throws IFException {
        //TODO Ignored: state.getFontVariant()
        //TODO Opportunity for font caching if font state is more heavily used
        String fontKey = getFontKey(triplet);
        int sizeMillipoints = state.getFontSize();
        float fontSize = sizeMillipoints / 1000f;

        // This assumes that *all* CIDFonts use a /ToUnicode mapping
        Typeface tf = getTypeface(fontKey);
        Font font = getFontInfo().getFontInstance(triplet, sizeMillipoints);
        String fontName = font.getFontName();

        PDFTextUtil textutil = generator.getTextUtil();
        textutil.updateTf(fontKey, fontSize, tf.isMultiByte(), tf.isCID());

        double shear = startSimulateStyle(tf, triplet);

        generator.updateCharacterSpacing(letterSpacing / 1000f);

        textutil.writeTextMatrix(new AffineTransform(1, 0, shear, -1, x / 1000f, y / 1000f));
        int l = text.length();
        int dxl = (dx != null ? dx.length : 0);

        if (dx != null && dxl > 0 && dx[0] != 0) {
            textutil.adjustGlyphTJ(-dx[0] / fontSize);
        }
        for (int i = 0; i < l; i++) {
            int orgChar = text.charAt(i);
            int ch;

            // surrogate pairs have to be merged in a single code point
            if (CharUtilities.containsSurrogatePairAt(text, i)) {
                orgChar = Character.toCodePoint((char) orgChar, text.charAt(++i));
            }

            float glyphAdjust = 0;
            if (font.hasCodePoint(orgChar)) {
                ch = font.mapCodePoint(orgChar);
                ch = selectAndMapSingleByteFont(tf, fontName, fontSize, textutil, ch);
                if ((wordSpacing != 0) && CharUtilities.isAdjustableSpace(orgChar)) {
                    glyphAdjust += wordSpacing;
                }
            } else {
                if (CharUtilities.isFixedWidthSpace(orgChar)) {
                    //Fixed width space are rendered as spaces so copy/paste works in a reader
                    ch = font.mapChar(CharUtilities.SPACE);
                    int spaceDiff = font.getCharWidth(CharUtilities.SPACE) - font.getCharWidth(orgChar);
                    glyphAdjust = -spaceDiff;
                } else {
                    ch = font.mapCodePoint(orgChar);
                    if ((wordSpacing != 0) && CharUtilities.isAdjustableSpace(orgChar)) {
                        glyphAdjust += wordSpacing;
                    }
                }
                ch = selectAndMapSingleByteFont(tf, fontName, fontSize, textutil, ch);
            }
            textutil.writeTJMappedCodePoint(ch);

            if (dx != null && i < dxl - 1) {
                glyphAdjust += dx[i + 1];
            }

            if (glyphAdjust != 0) {
                textutil.adjustGlyphTJ(-glyphAdjust / fontSize);
            }

        }
        textutil.writeTJ();
        generator.resetCharacterSpacing();
        endSimulateStyle(tf, triplet);
    }

    private double startSimulateStyle(Typeface tf, FontTriplet triplet) {
        double shear = 0;
        boolean simulateStyle = tf instanceof CustomFont && ((CustomFont) tf).getSimulateStyle();
        if (simulateStyle) {
            if (triplet.getWeight() == 700) {
                generator.updateColor(state.getTextColor(), false, null);
                generator.add("2 Tr 0.31543 w\n");
            }
            if (triplet.getStyle().equals("italic")) {
                shear = 0.3333;
            }
        }
        return shear;
    }

    private void endSimulateStyle(Typeface tf, FontTriplet triplet) {
        boolean simulateStyle = tf instanceof CustomFont && ((CustomFont) tf).getSimulateStyle();
        if (simulateStyle && triplet.getWeight() == 700) {
            generator.add("0 Tr\n");
        }
    }

    private static int[] paZero = new int[4];

    private void drawTextWithDP(int x, int y, String text, FontTriplet triplet,
                                  int letterSpacing, int wordSpacing, int[][] dp) {
        assert text != null;
        assert triplet != null;
        assert dp != null;
        String          fk              = getFontInfo().getInternalFontKey(triplet);
        Typeface        tf              = getTypeface(fk);
        if (tf.isMultiByte() || tf.isCID()) {
            int         fs              = state.getFontSize();
            float       fsPoints        = fs / 1000f;
            Font        f               = getFontInfo().getFontInstance(triplet, fs);
            PDFTextUtil tu              = generator.getTextUtil();
            double      xc              = 0f;
            double      yc              = 0f;
            double      xoLast          = 0f;
            double      yoLast          = 0f;
            double      wox             = wordSpacing;

            double shear = startSimulateStyle(tf, triplet);

            tu.writeTextMatrix(new AffineTransform(1, 0, shear, -1, x / 1000f, y / 1000f));
            tu.updateTf(fk, fsPoints, tf.isMultiByte(), true);
            generator.updateCharacterSpacing(letterSpacing / 1000f);
            for (int i = 0, n = text.length(); i < n; i++) {
                int ch = text.charAt(i);
                int mp;
                if (CharUtilities.containsSurrogatePairAt(text, i)) {
                    ch = Character.toCodePoint((char) ch, text.charAt(++i));
                    mp = f.mapCodePoint(ch);
                } else {
                    mp = f.mapChar((char)ch);
                }
                int[]   pa              = ((i >= dp.length) || (dp[i] == null)) ? paZero : dp[i];
                double  xo              = xc + pa[0];
                double  yo              = yc + pa[1];
                double  xa              = f.getCharWidth(ch) + maybeWordOffsetX(wox, ch, null);
                double  ya              = 0;
                double  xd              = (xo - xoLast) / 1000f;
                double  yd              = (yo - yoLast) / 1000f;
                tu.writeTd(xd, yd);
                tu.writeTj(mp, tf.isMultiByte(), true);
                xc += xa + pa[2];
                yc += ya + pa[3];
                xoLast = xo;
                yoLast = yo;
            }
            generator.resetCharacterSpacing();
            endSimulateStyle(tf, triplet);
        }
    }

    private double maybeWordOffsetX(double wox, int ch, Direction dir) {
        if ((wox != 0)
             && CharUtilities.isAdjustableSpace(ch)
             && ((dir == null) || dir.isHorizontal())) {
            return wox;
        } else {
            return 0;
        }
    }

    /*
    private double maybeWordOffsetY ( double woy, char ch, Direction dir ) {
        if ( ( woy != 0 )
             && CharUtilities.isAdjustableSpace ( ch ) && dir.isVertical()
             && ( ( dir != null ) && dir.isVertical() ) ) {
            return woy;
        } else {
            return 0;
        }
    }
    */

    private int selectAndMapSingleByteFont(Typeface tf, String fontName, float fontSize, PDFTextUtil textutil,
                                            int ch) {
        if ((tf instanceof SingleByteFont && ((SingleByteFont)tf).hasAdditionalEncodings()) || tf.isCID()) {
            int encoding = ch / 256;
            if (encoding == 0) {
                textutil.updateTf(fontName, fontSize, tf.isMultiByte(), tf.isCID());
            } else {
                textutil.updateTf(fontName + "_" + Integer.toString(encoding),
                        fontSize, tf.isMultiByte(), tf.isCID());
                ch = (char)(ch % 256);
            }
        }
        return ch;
    }

}
