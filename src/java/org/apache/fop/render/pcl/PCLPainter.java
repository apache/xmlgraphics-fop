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
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Map;
import java.util.Stack;

import org.w3c.dom.Document;

import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageProcessingHints;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.render.ImageHandlerUtil;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.AbstractIFPainter;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFState;
import org.apache.fop.render.intermediate.IFUtil;
import org.apache.fop.render.java2d.FontMetricsMapper;
import org.apache.fop.render.java2d.Java2DPainter;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.CharUtilities;

/**
 * {@link org.apache.fop.render.intermediate.IFPainter} implementation that produces PCL 5.
 */
public class PCLPainter extends AbstractIFPainter<PCLDocumentHandler> implements PCLConstants {

    private static final boolean DEBUG = false;

    /** The PCL generator */
    private PCLGenerator gen;

    private PCLPageDefinition currentPageDefinition;
    private int currentPrintDirection;
    //private GeneralPath currentPath = null;

    private Stack<GraphicContext> graphicContextStack = new Stack<GraphicContext>();
    private GraphicContext graphicContext = new GraphicContext();

    /**
     * Main constructor.
     * @param parent the parent document handler
     * @param pageDefinition the page definition describing the page to be rendered
     */
    public PCLPainter(PCLDocumentHandler parent, PCLPageDefinition pageDefinition) {
        super(parent);
        this.gen = parent.getPCLGenerator();
        this.state = IFState.create();
        this.currentPageDefinition = pageDefinition;
    }

    PCLRenderingUtil getPCLUtil() {
        return getDocumentHandler().getPCLUtil();
    }

    /** @return the target resolution */
    protected int getResolution() {
        int resolution = Math.round(getUserAgent().getTargetResolution());
        if (resolution <= 300) {
            return 300;
        } else {
            return 600;
        }
    }

    private boolean isSpeedOptimized() {
        return getPCLUtil().getRenderingMode() == PCLRenderingMode.SPEED;
    }

    //----------------------------------------------------------------------------------------------

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
    public void startGroup(AffineTransform transform, String layer) throws IFException {
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
    public void drawImage(String uri, Rectangle rect) throws IFException {
        drawImageUsingURI(uri, rect);
    }

    /** {@inheritDoc} */
    protected RenderingContext createRenderingContext() {
        PCLRenderingContext pdfContext = new PCLRenderingContext(
                getUserAgent(), this.gen, getPCLUtil()) {

            public Point2D transformedPoint(int x, int y) {
                return PCLPainter.this.transformedPoint(x, y);
            }

            public GraphicContext getGraphicContext() {
                return PCLPainter.this.graphicContext;
            }

        };
        return pdfContext;
    }

    /** {@inheritDoc} */
    public void drawImage(Document doc, Rectangle rect) throws IFException {
        drawImageUsingDocument(doc, rect);
    }

    /** {@inheritDoc} */
    public void clipRect(Rectangle rect) throws IFException {
        //PCL cannot clip (only HP GL/2 can)
        //If you need clipping support, switch to RenderingMode.BITMAP.
    }

    /** {@inheritDoc} */
    public void clipBackground(Rectangle rect, BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd) throws IFException {
        //PCL cannot clip (only HP GL/2 can)
        //If you need clipping support, switch to RenderingMode.BITMAP.

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
    public void drawBorderRect(final Rectangle rect,
            final BorderProps top, final BorderProps bottom,
            final BorderProps left, final BorderProps right) throws IFException {
        if (isSpeedOptimized()) {
            super.drawBorderRect(rect, top, bottom, left, right, null);
            return;
        }
        if (top != null || bottom != null || left != null || right != null) {
            final Rectangle boundingBox = rect;
            final Dimension dim = boundingBox.getSize();

            Graphics2DImagePainter painter = new Graphics2DImagePainter() {

                public void paint(Graphics2D g2d, Rectangle2D area) {
                    g2d.translate(-rect.x, -rect.y);

                    Java2DPainter painter = new Java2DPainter(g2d,
                            getContext(), getFontInfo(), state);
                    try {
                        painter.drawBorderRect(rect, top, bottom, left, right);
                    } catch (IFException e) {
                        //This should never happen with the Java2DPainter
                        throw new RuntimeException("Unexpected error while painting borders", e);
                    }
                }

                public Dimension getImageSize() {
                    return dim.getSize();
                }

            };
            paintMarksAsBitmap(painter, boundingBox);
        }
    }

    /** {@inheritDoc} */
    public void drawLine(final Point start, final Point end,
                final int width, final Color color, final RuleStyle style)
            throws IFException {
        if (isSpeedOptimized()) {
            super.drawLine(start, end, width, color, style);
            return;
        }
        final Rectangle boundingBox = getLineBoundingBox(start, end, width);
        final Dimension dim = boundingBox.getSize();

        Graphics2DImagePainter painter = new Graphics2DImagePainter() {

            public void paint(Graphics2D g2d, Rectangle2D area) {
                g2d.translate(-boundingBox.x, -boundingBox.y);

                Java2DPainter painter = new Java2DPainter(g2d,
                        getContext(), getFontInfo(), state);
                try {
                    painter.drawLine(start, end, width, color, style);
                } catch (IFException e) {
                    //This should never happen with the Java2DPainter
                    throw new RuntimeException("Unexpected error while painting a line", e);
                }
            }

            public Dimension getImageSize() {
                return dim.getSize();
            }

        };
        paintMarksAsBitmap(painter, boundingBox);
    }

    private void paintMarksAsBitmap(Graphics2DImagePainter painter, Rectangle boundingBox)
            throws IFException {
        ImageInfo info = new ImageInfo(null, null);
        ImageSize size = new ImageSize();
        size.setSizeInMillipoints(boundingBox.width, boundingBox.height);
        info.setSize(size);
        ImageGraphics2D img = new ImageGraphics2D(info, painter);

        Map hints = new java.util.HashMap();
        if (isSpeedOptimized()) {
            //Gray text may not be painted in this case! We don't get dithering in Sun JREs.
            //But this approach is about twice as fast as the grayscale image.
            hints.put(ImageProcessingHints.BITMAP_TYPE_INTENT,
                    ImageProcessingHints.BITMAP_TYPE_INTENT_MONO);
        } else {
            hints.put(ImageProcessingHints.BITMAP_TYPE_INTENT,
                    ImageProcessingHints.BITMAP_TYPE_INTENT_GRAY);
        }
        hints.put(ImageHandlerUtil.CONVERSION_MODE, ImageHandlerUtil.CONVERSION_MODE_BITMAP);
        PCLRenderingContext context = (PCLRenderingContext)createRenderingContext();
        context.setSourceTransparencyEnabled(true);
        try {
            drawImage(img, boundingBox, context, true, hints);
        } catch (IOException ioe) {
            throw new IFException(
                    "I/O error while painting marks using a bitmap", ioe);
        } catch (ImageException ie) {
            throw new IFException(
                    "Error while painting marks using a bitmap", ie);
        }
    }

    /** {@inheritDoc} */
    public void drawText(int x, int y, int letterSpacing, int wordSpacing, int[][] dp, String text)
                throws IFException {
        try {
            FontTriplet triplet = new FontTriplet(
                    state.getFontFamily(), state.getFontStyle(), state.getFontWeight());
            //TODO Ignored: state.getFontVariant()
            //TODO Opportunity for font caching if font state is more heavily used
            String fontKey = getFontKey(triplet);
            boolean pclFont = getPCLUtil().isAllTextAsBitmaps() ? false
                        : HardcodedFonts.setFont(gen, fontKey, state.getFontSize(), text);
            if (pclFont) {
                drawTextNative(x, y, letterSpacing, wordSpacing, dp, text, triplet);
            } else {
                drawTextAsBitmap(x, y, letterSpacing, wordSpacing, dp, text, triplet);
                if (DEBUG) {
                    state.setTextColor(Color.GRAY);
                    HardcodedFonts.setFont(gen, "F1", state.getFontSize(), text);
                    drawTextNative(x, y, letterSpacing, wordSpacing, dp, text, triplet);
                }
            }
        } catch (IOException ioe) {
            throw new IFException("I/O error in drawText()", ioe);
        }
    }

    private void drawTextNative(int x, int y, int letterSpacing, int wordSpacing, int[][] dp,
            String text, FontTriplet triplet) throws IOException {
        Color textColor = state.getTextColor();
        if (textColor != null) {
            gen.setTransparencyMode(true, false);
            gen.selectGrayscale(textColor);
        }

        gen.setTransparencyMode(true, true);
        setCursorPos(x, y);

        float fontSize = state.getFontSize() / 1000f;
        Font font = getFontInfo().getFontInstance(triplet, state.getFontSize());
        int l = text.length();

        StringBuffer sb = new StringBuffer(Math.max(16, l));
        if (dp != null && dp[0] != null && dp[0][0] != 0) {
            if (dp[0][0] > 0) {
                sb.append("\u001B&a+").append(gen.formatDouble2(dp[0][0] / 100.0)).append('H');
            } else {
                sb.append("\u001B&a-").append(gen.formatDouble2(-dp[0][0] / 100.0)).append('H');
            }
        }
        if (dp != null && dp[0] != null && dp[0][1] != 0) {
            if (dp[0][1] > 0) {
                sb.append("\u001B&a-").append(gen.formatDouble2(dp[0][1] / 100.0)).append('V');
            } else {
                sb.append("\u001B&a+").append(gen.formatDouble2(-dp[0][1] / 100.0)).append('V');
            }
        }
        for (int i = 0; i < l; i++) {
            char orgChar = text.charAt(i);
            char ch;
            float xGlyphAdjust = 0;
            float yGlyphAdjust = 0;
            if (font.hasChar(orgChar)) {
                ch = font.mapChar(orgChar);
            } else {
                if (CharUtilities.isFixedWidthSpace(orgChar)) {
                    //Fixed width space are rendered as spaces so copy/paste works in a reader
                    ch = font.mapChar(CharUtilities.SPACE);
                    int spaceDiff = font.getCharWidth(ch) - font.getCharWidth(orgChar);
                    xGlyphAdjust = -(10 * spaceDiff / fontSize);
                } else {
                    ch = font.mapChar(orgChar);
                }
            }
            sb.append(ch);

            if ((wordSpacing != 0) && CharUtilities.isAdjustableSpace(orgChar)) {
                xGlyphAdjust += wordSpacing;
            }
            xGlyphAdjust += letterSpacing;
            if (dp != null && i < dp.length && dp[i] != null) {
                xGlyphAdjust += dp[i][2] - dp[i][0];
                yGlyphAdjust += dp[i][3] - dp[i][1];
            }
            if (dp != null && i < dp.length - 1 && dp[i + 1] != null) {
                xGlyphAdjust += dp[i + 1][0];
                yGlyphAdjust += dp[i + 1][1];
            }

            if (xGlyphAdjust != 0) {
                if (xGlyphAdjust > 0) {
                    sb.append("\u001B&a+").append(gen.formatDouble2(xGlyphAdjust / 100.0)).append('H');
                } else {
                    sb.append("\u001B&a-").append(gen.formatDouble2(-xGlyphAdjust / 100.0)).append('H');
                }
            }
            if (yGlyphAdjust != 0) {
                if (yGlyphAdjust > 0) {
                    sb.append("\u001B&a-").append(gen.formatDouble2(yGlyphAdjust / 100.0)).append('V');
                } else {
                    sb.append("\u001B&a+").append(gen.formatDouble2(-yGlyphAdjust / 100.0)).append('V');
                }
            }

        }
        gen.getOutputStream().write(sb.toString().getBytes(gen.getTextEncoding()));

    }

    private static final double SAFETY_MARGIN_FACTOR = 0.05;

    private Rectangle getTextBoundingBox(int x, int y,
            int letterSpacing, int wordSpacing, int[][] dp,
            String text,
            Font font, FontMetricsMapper metrics) {
        int maxAscent = metrics.getMaxAscent(font.getFontSize()) / 1000;
        int descent = metrics.getDescender(font.getFontSize()) / 1000; //is negative
        int safetyMargin = (int)(SAFETY_MARGIN_FACTOR * font.getFontSize());
        Rectangle boundingRect = new Rectangle(
                x, y - maxAscent - safetyMargin,
                0, maxAscent - descent + 2 * safetyMargin);

        int l = text.length();
        int[] dx = IFUtil.convertDPToDX(dp);
        int dxl = (dx != null ? dx.length : 0);

        if (dx != null && dxl > 0 && dx[0] != 0) {
            boundingRect.setLocation(boundingRect.x - (int)Math.ceil(dx[0] / 10f), boundingRect.y);
        }
        float width = 0.0f;
        for (int i = 0; i < l; i++) {
            char orgChar = text.charAt(i);
            float glyphAdjust = 0;
            int cw = font.getCharWidth(orgChar);

            if ((wordSpacing != 0) && CharUtilities.isAdjustableSpace(orgChar)) {
                glyphAdjust += wordSpacing;
            }
            glyphAdjust += letterSpacing;
            if (dx != null && i < dxl - 1) {
                glyphAdjust += dx[i + 1];
            }

            width += cw + glyphAdjust;
        }
        int extraWidth = font.getFontSize() / 3;
        boundingRect.setSize(
                (int)Math.ceil(width) + extraWidth,
                boundingRect.height);
        return boundingRect;
    }

    private void drawTextAsBitmap(final int x, final int y,
            final int letterSpacing, final int wordSpacing, final int[][] dp,
            final String text, FontTriplet triplet) throws IFException {
        //Use Java2D to paint different fonts via bitmap
        final Font font = getFontInfo().getFontInstance(triplet, state.getFontSize());

        //for cursive fonts, so the text isn't clipped
        FontMetricsMapper mapper;
        try {
            mapper = (FontMetricsMapper) getFontInfo().getMetricsFor(font.getFontName());
        } catch (Exception t) {
            throw new RuntimeException(t);
        }
        final int maxAscent = mapper.getMaxAscent(font.getFontSize()) / 1000;
        final int ascent = mapper.getAscender(font.getFontSize()) / 1000;
        final int descent = mapper.getDescender(font.getFontSize()) / 1000;
        int safetyMargin = (int)(SAFETY_MARGIN_FACTOR * font.getFontSize());
        final int baselineOffset = maxAscent + safetyMargin;

        final Rectangle boundingBox = getTextBoundingBox(x, y,
                letterSpacing, wordSpacing, dp, text, font, mapper);
        final Dimension dim = boundingBox.getSize();

        Graphics2DImagePainter painter = new Graphics2DImagePainter() {

            public void paint(Graphics2D g2d, Rectangle2D area) {
                if (DEBUG) {
                    g2d.setBackground(Color.LIGHT_GRAY);
                    g2d.clearRect(0, 0, (int)area.getWidth(), (int)area.getHeight());
                }
                g2d.translate(-x, -y + baselineOffset);

                if (DEBUG) {
                    Rectangle rect = new Rectangle(x, y - maxAscent, 3000, maxAscent);
                    g2d.draw(rect);
                    rect = new Rectangle(x, y - ascent, 2000, ascent);
                    g2d.draw(rect);
                    rect = new Rectangle(x, y, 1000, -descent);
                    g2d.draw(rect);
                }
                Java2DPainter painter = new Java2DPainter(g2d, getContext(), getFontInfo(), state);
                try {
                    painter.drawText(x, y, letterSpacing, wordSpacing, dp, text);
                } catch (IFException e) {
                    //This should never happen with the Java2DPainter
                    throw new RuntimeException("Unexpected error while painting text", e);
                }
            }

            public Dimension getImageSize() {
                return dim.getSize();
            }

        };
        paintMarksAsBitmap(painter, boundingBox);
    }

    /** Saves the current graphics state on the stack. */
    private void saveGraphicsState() {
        graphicContextStack.push(graphicContext);
        graphicContext = (GraphicContext)graphicContext.clone();
    }

    /** Restores the last graphics state from the stack. */
    private void restoreGraphicsState() {
        graphicContext = graphicContextStack.pop();
    }

    private void concatenateTransformationMatrix(AffineTransform transform) throws IOException {
        if (!transform.isIdentity()) {
            graphicContext.transform(transform);
            changePrintDirection();
        }
    }

    private Point2D transformedPoint(int x, int y) {
        return PCLRenderingUtil.transformedPoint(x, y, graphicContext.getTransform(),
                currentPageDefinition, currentPrintDirection);
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



}
