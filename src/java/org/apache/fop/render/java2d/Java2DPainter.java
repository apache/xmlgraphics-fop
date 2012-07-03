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

package org.apache.fop.render.java2d;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Stack;

import org.w3c.dom.Document;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
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
 * {@link org.apache.fop.render.intermediate.IFPainter} implementation that paints on a Graphics2D
 * instance.
 */
public class Java2DPainter extends AbstractIFPainter<Java2DDocumentHandler> {

    /** the IF context */
    protected IFContext ifContext;

    /** The font information */
    protected FontInfo fontInfo;

    private Java2DBorderPainter borderPainter;

    /** The current state, holds a Graphics2D and its context */
    protected Java2DGraphicsState g2dState;
    private Stack<Java2DGraphicsState> g2dStateStack = new Stack<Java2DGraphicsState>();

    /**
     * Main constructor.
     * @param g2d the target Graphics2D instance
     * @param context the IF context
     * @param fontInfo the font information
     */
    public Java2DPainter(Graphics2D g2d, IFContext context, FontInfo fontInfo) {
        this(g2d, context, fontInfo, null);
    }

    /**
     * Special constructor for embedded use (when another painter uses Java2DPainter
     * to convert part of a document into a bitmap, for example).
     * @param g2d the target Graphics2D instance
     * @param context the IF context
     * @param fontInfo the font information
     * @param state the IF state object
     */
    public Java2DPainter(Graphics2D g2d, IFContext context, FontInfo fontInfo, IFState state) {
        super(new Java2DDocumentHandler());
        this.ifContext = context;
        if (state != null) {
            this.state = state.push();
        } else {
            this.state = IFState.create();
        }
        this.fontInfo = fontInfo;
        this.g2dState = new Java2DGraphicsState(g2d, fontInfo, g2d.getTransform());
        this.borderPainter = new Java2DBorderPainter(this);
    }

    /** {@inheritDoc} */
    public IFContext getContext() {
        return this.ifContext;
    }

    /**
     * Returns the associated {@link FontInfo} object.
     * @return the font info
     */
    protected FontInfo getFontInfo() {
        return this.fontInfo;
    }

    /**
     * Returns the Java2D graphics state.
     * @return the graphics state
     */
    protected Java2DGraphicsState getState() {
        return this.g2dState;
    }

    //----------------------------------------------------------------------------------------------


    /** {@inheritDoc} */
    public void startViewport(AffineTransform transform, Dimension size, Rectangle clipRect)
            throws IFException {
        saveGraphicsState();
        try {
            concatenateTransformationMatrix(transform);
            clipRect(clipRect);
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
    public void drawImage(String uri, Rectangle rect) throws IFException {
        drawImageUsingURI(uri, rect);
    }

    /** {@inheritDoc} */
    protected RenderingContext createRenderingContext() {
        Java2DRenderingContext java2dContext = new Java2DRenderingContext(
                getUserAgent(), g2dState.getGraph(), getFontInfo());
        return java2dContext;
    }

    /** {@inheritDoc} */
    public void drawImage(Document doc, Rectangle rect) throws IFException {
        drawImageUsingDocument(doc, rect);
    }

    /** {@inheritDoc} */
    public void clipRect(Rectangle rect) throws IFException {
        getState().updateClip(rect);
    }

    /** {@inheritDoc} */
    public void clipBackground(Rectangle rect, BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd) throws IFException {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public void fillRect(Rectangle rect, Paint fill) throws IFException {
        if (fill == null) {
            return;
        }
        if (rect.width != 0 && rect.height != 0) {
            g2dState.updatePaint(fill);
            g2dState.getGraph().fill(rect);
        }
    }

    /** {@inheritDoc} */
    public void drawBorderRect(Rectangle rect, BorderProps top, BorderProps bottom,
            BorderProps left, BorderProps right) throws IFException {
        if (top != null || bottom != null || left != null || right != null) {
            this.borderPainter.drawBorders(rect, top, bottom, left, right, null);
        }
    }

    /** {@inheritDoc} */
    public void drawLine(Point start, Point end, int width, Color color, RuleStyle style)
            throws IFException {
        this.borderPainter.drawLine(start, end, width, color, style);
    }

    /** {@inheritDoc} */
    public void drawText(int x, int y, int letterSpacing, int wordSpacing, int[][] dp, String text)
            throws IFException {
        g2dState.updateColor(state.getTextColor());
        FontTriplet triplet = new FontTriplet(
                state.getFontFamily(), state.getFontStyle(), state.getFontWeight());
        //TODO Ignored: state.getFontVariant()
        //TODO Opportunity for font caching if font state is more heavily used
        Font font = getFontInfo().getFontInstance(triplet, state.getFontSize());
        //String fontName = font.getFontName();
        //float fontSize = state.getFontSize() / 1000f;
        g2dState.updateFont(font.getFontName(), state.getFontSize() * 1000);

        Graphics2D g2d = this.g2dState.getGraph();
        GlyphVector gv = g2d.getFont().createGlyphVector(g2d.getFontRenderContext(), text);
        Point2D cursor = new Point2D.Float(0, 0);

        int l = text.length();
        int[] dx = IFUtil.convertDPToDX ( dp );
        int dxl = (dx != null ? dx.length : 0);

        if (dx != null && dxl > 0 && dx[0] != 0) {
            cursor.setLocation(cursor.getX() - (dx[0] / 10f), cursor.getY());
            gv.setGlyphPosition(0, cursor);
        }
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

            cursor.setLocation(cursor.getX() + cw + glyphAdjust, cursor.getY());
            gv.setGlyphPosition(i + 1, cursor);
        }
        g2d.drawGlyphVector(gv, x, y);
    }

    /** Saves the current graphics state on the stack. */
    protected void saveGraphicsState() {
        g2dStateStack.push(g2dState);
        g2dState = new Java2DGraphicsState(g2dState);
    }

    /** Restores the last graphics state from the stack. */
    protected void restoreGraphicsState() {
        g2dState.dispose();
        g2dState = g2dStateStack.pop();
    }

    private void concatenateTransformationMatrix(AffineTransform transform) throws IOException {
        g2dState.transform(transform);
    }



}
