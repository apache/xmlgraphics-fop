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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;

import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.gvt.text.TextSpanLayout;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.util.CharUtilities;

/**
 * Renders the attributed character iterator of a {@link org.apache.batik.gvt.TextNode}.
 * This class draws the text directly into the PDFGraphics2D so that
 * the text is not drawn using shapes which makes the PDF files larger.
 * If the text is simple enough to draw then it sets the font and calls
 * drawString. If the text is complex or the cannot be translated
 * into a simple drawString the StrokingTextPainter is used instead.
 *
 * @version $Id$
 */
class PDFTextPainter extends NativeTextPainter {

    private static final boolean DEBUG = false;

    /**
     * Create a new PDF text painter with the given font information.
     * @param fi the font info
     */
    public PDFTextPainter(FontInfo fi) {
        super(fi);
    }

    /** {@inheritDoc} */
    protected boolean isSupported(Graphics2D g2d) {
        return g2d instanceof PDFGraphics2D;
    }

    /** {@inheritDoc} */
    protected void paintTextRun(TextRun textRun, Graphics2D g2d) {
        AttributedCharacterIterator runaci = textRun.getACI();
        runaci.first();

        TextPaintInfo tpi = (TextPaintInfo)runaci.getAttribute(PAINT_INFO);
        if (tpi == null || !tpi.visible) {
            return;
        }
        if ((tpi != null) && (tpi.composite != null)) {
            g2d.setComposite(tpi.composite);
        }

        //------------------------------------
        TextSpanLayout layout = textRun.getLayout();
        logTextRun(runaci, layout);
        CharSequence chars = collectCharacters(runaci);
        runaci.first(); //Reset ACI

        final PDFGraphics2D pdf = (PDFGraphics2D)g2d;
        PDFTextUtil textUtil = new PDFTextUtil(pdf.fontInfo) {
            protected void write(String code) {
                pdf.currentStream.write(code);
            }
        };

        if (DEBUG) {
            log.debug("Text: " + chars);
            pdf.currentStream.write("%Text: " + chars + "\n");
        }

        GeneralPath debugShapes = null;
        if (DEBUG) {
            debugShapes = new GeneralPath();
        }

        Font[] fonts = findFonts(runaci);
        if (fonts == null || fonts.length == 0) {
            //Draw using Java2D when no native fonts are available
            textRun.getLayout().draw(g2d);
            return;
        }

        textUtil.saveGraphicsState();
        textUtil.concatMatrix(g2d.getTransform());
        Shape imclip = g2d.getClip();
        pdf.writeClip(imclip);

        applyColorAndPaint(tpi, pdf);

        textUtil.beginTextObject();
        textUtil.setFonts(fonts);
        boolean stroke = (tpi.strokePaint != null)
            && (tpi.strokeStroke != null);
        textUtil.setTextRenderingMode(tpi.fillPaint != null, stroke, false);

        AffineTransform localTransform = new AffineTransform();
        Point2D prevPos = null;
        double prevVisibleCharWidth = 0.0;
        GVTGlyphVector gv = layout.getGlyphVector();
        for (int index = 0, c = gv.getNumGlyphs(); index < c; index++) {
            char ch = chars.charAt(index);
            boolean visibleChar = gv.isGlyphVisible(index)
                || (CharUtilities.isAnySpace(ch) && !CharUtilities.isZeroWidthSpace(ch));
            logCharacter(ch, layout, index, visibleChar);
            if (!visibleChar) {
                continue;
            }
            Point2D glyphPos = gv.getGlyphPosition(index);

            AffineTransform glyphTransform = gv.getGlyphTransform(index);
            //TODO Glyph transforms could be refined so not every char has to be painted
            //with its own TJ command (stretch/squeeze case could be optimized)
            if (log.isTraceEnabled()) {
                log.trace("pos " + glyphPos + ", transform " + glyphTransform);
            }
            if (DEBUG) {
                Shape sh = gv.getGlyphLogicalBounds(index);
                if (sh == null) {
                    sh = new Ellipse2D.Double(glyphPos.getX(), glyphPos.getY(), 2, 2);
                }
                debugShapes.append(sh, false);
            }

            //Exact position of the glyph
            localTransform.setToIdentity();
            localTransform.translate(glyphPos.getX(), glyphPos.getY());
            if (glyphTransform != null) {
                localTransform.concatenate(glyphTransform);
            }
            localTransform.scale(1, -1);

            boolean yPosChanged = (prevPos == null
                    || prevPos.getY() != glyphPos.getY()
                    || glyphTransform != null);
            if (yPosChanged) {
                if (index > 0) {
                    textUtil.writeTJ();
                    textUtil.writeTextMatrix(localTransform);
                }
            } else {
                double xdiff = glyphPos.getX() - prevPos.getX();
                //Width of previous character
                Font font = textUtil.getCurrentFont();
                double cw = prevVisibleCharWidth;
                double effxdiff = (1000 * xdiff) - cw;
                if (effxdiff != 0) {
                    double adjust = (-effxdiff / font.getFontSize());
                    textUtil.adjustGlyphTJ(adjust * 1000);
                }
                if (log.isTraceEnabled()) {
                    log.trace("==> x diff: " + xdiff + ", " + effxdiff
                            + ", charWidth: " + cw);
                }
            }
            Font f = textUtil.selectFontForChar(ch);
            if (f != textUtil.getCurrentFont()) {
                textUtil.writeTJ();
                textUtil.setCurrentFont(f);
                textUtil.writeTf(f);
                textUtil.writeTextMatrix(localTransform);
            }
            char paintChar = (CharUtilities.isAnySpace(ch) ? ' ' : ch);
            textUtil.writeTJChar(paintChar);

            //Update last position
            prevPos = glyphPos;
            prevVisibleCharWidth = textUtil.getCurrentFont().getCharWidth(chars.charAt(index));
        }
        textUtil.writeTJ();
        textUtil.endTextObject();
        textUtil.restoreGraphicsState();
        if (DEBUG) {
            g2d.setStroke(new BasicStroke(0));
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.draw(debugShapes);
        }
    }

    private void applyColorAndPaint(TextPaintInfo tpi, PDFGraphics2D pdf) {
        Paint fillPaint = tpi.fillPaint;
        Paint strokePaint = tpi.strokePaint;
        Stroke stroke = tpi.strokeStroke;
        int fillAlpha = PDFGraphics2D.OPAQUE;
        if (fillPaint instanceof Color) {
            Color col = (Color)fillPaint;
            pdf.applyColor(col, true);
            fillAlpha = col.getAlpha();
        }
        if (strokePaint instanceof Color) {
            Color col = (Color)strokePaint;
            pdf.applyColor(col, false);
        }
        pdf.applyPaint(fillPaint, true);
        pdf.applyStroke(stroke);
        if (strokePaint != null) {
            pdf.applyPaint(strokePaint, false);
        }
        pdf.applyAlpha(fillAlpha, PDFGraphics2D.OPAQUE);
    }

}