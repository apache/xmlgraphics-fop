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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.Iterator;
import java.util.List;

import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.gvt.text.TextSpanLayout;

import org.apache.xmlgraphics.java2d.ps.PSGraphics2D;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSResource;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.svg.NativeTextPainter;
import org.apache.fop.util.CharUtilities;

/**
 * Renders the attributed character iterator of a <tt>TextNode</tt>.
 * This class draws the text directly using PostScript text operators so
 * the text is not drawn using shapes which makes the PS files larger.
 * <p>
 * The text runs are split into smaller text runs that can be bundles in single
 * calls of the xshow, yshow or xyshow operators. For outline text, the charpath
 * operator is used.
 */
public class PSTextPainter extends NativeTextPainter {

    private static final boolean DEBUG = false;

    private FontResourceCache fontResources;

    private static final AffineTransform IDENTITY_TRANSFORM = new AffineTransform();

    /**
     * Create a new PS text painter with the given font information.
     * @param fontInfo the font collection
     */
    public PSTextPainter(FontInfo fontInfo) {
        super(fontInfo);
        this.fontResources = new FontResourceCache(fontInfo);
    }

    /** {@inheritDoc} */
    protected boolean isSupported(Graphics2D g2d) {
        return g2d instanceof PSGraphics2D;
    }

    /** {@inheritDoc} */
    protected void paintTextRun(TextRun textRun, Graphics2D g2d) throws IOException {
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

        final PSGraphics2D ps = (PSGraphics2D)g2d;
        final PSGenerator gen = ps.getPSGenerator();
        ps.preparePainting();

        if (DEBUG) {
            log.debug("Text: " + chars);
            gen.commentln("%Text: " + chars);
        }

        GeneralPath debugShapes = null;
        if (DEBUG) {
            debugShapes = new GeneralPath();
        }

        TextUtil textUtil = new TextUtil(gen);
        textUtil.setupFonts(runaci);
        if (!textUtil.hasFonts()) {
            //Draw using Java2D when no native fonts are available
            textRun.getLayout().draw(g2d);
            return;
        }

        gen.saveGraphicsState();
        gen.concatMatrix(g2d.getTransform());
        Shape imclip = g2d.getClip();
        clip(ps, imclip);

        gen.writeln("BT"); //beginTextObject()

        AffineTransform localTransform = new AffineTransform();
        Point2D prevPos = null;
        GVTGlyphVector gv = layout.getGlyphVector();
        PSTextRun psRun = new PSTextRun(); //Used to split a text run into smaller runs
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

            boolean flushCurrentRun = false;
            //Try to optimize by combining characters using the same font and on the same line.
            if (glyphTransform != null) {
                //Happens for text-on-a-path
                flushCurrentRun = true;
            }
            if (psRun.getRunLength() >= 128) {
                //Don't let a run get too long
                flushCurrentRun = true;
            }

            //Note the position of the glyph relative to the previous one
            Point2D relPos;
            if (prevPos == null) {
                relPos = new Point2D.Double(0, 0);
            } else {
                relPos = new Point2D.Double(
                        glyphPos.getX() - prevPos.getX(),
                        glyphPos.getY() - prevPos.getY());
            }
            if (psRun.vertChanges == 0
                    && psRun.getHorizRunLength() > 2
                    && relPos.getY() != 0) {
                //new line
                flushCurrentRun = true;
            }

            //Select the actual character to paint
            char paintChar = (CharUtilities.isAnySpace(ch) ? ' ' : ch);

            //Select (sub)font for character
            Font f = textUtil.selectFontForChar(paintChar);
            char mapped = f.mapChar(ch);
            boolean fontChanging = textUtil.isFontChanging(f, mapped);
            if (fontChanging) {
                flushCurrentRun = true;
            }

            if (flushCurrentRun) {
                //Paint the current run and reset for the next run
                psRun.paint(ps, textUtil, tpi);
                psRun.reset();
            }

            //Track current run
            psRun.addCharacter(paintChar, relPos);
            psRun.noteStartingTransformation(localTransform);

            //Change font if necessary
            if (fontChanging) {
                textUtil.setCurrentFont(f, mapped);
            }

            //Update last position
            prevPos = glyphPos;
        }
        psRun.paint(ps, textUtil, tpi);
        gen.writeln("ET"); //endTextObject()
        gen.restoreGraphicsState();

        if (DEBUG) {
            //Paint debug shapes
            g2d.setStroke(new BasicStroke(0));
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.draw(debugShapes);
        }
    }

    private void applyColor(Paint paint, final PSGenerator gen) throws IOException {
        if (paint == null) {
            return;
        } else if (paint instanceof Color) {
            Color col = (Color)paint;
            gen.useColor(col);
        } else {
            log.warn("Paint not supported: " + paint.toString());
        }
    }

    private PSResource getResourceForFont(Font f, String postfix) {
        String key = (postfix != null ? f.getFontName() + '_' + postfix : f.getFontName());
        return this.fontResources.getPSResourceForFontKey(key);
    }

    private void clip(PSGraphics2D ps, Shape shape) throws IOException {
        if (shape == null) {
            return;
        }
        ps.getPSGenerator().writeln("newpath");
        PathIterator iter = shape.getPathIterator(IDENTITY_TRANSFORM);
        ps.processPathIterator(iter);
        ps.getPSGenerator().writeln("clip");
    }

    private class TextUtil {

        private PSGenerator gen;
        private Font[] fonts;
        private Font currentFont;
        private int currentEncoding = -1;

        public TextUtil(PSGenerator gen) {
            this.gen = gen;
        }

        public Font selectFontForChar(char ch) {
            for (int i = 0, c = fonts.length; i < c; i++) {
                if (fonts[i].hasChar(ch)) {
                    return fonts[i];
                }
            }
            return fonts[0]; //TODO Maybe fall back to painting with shapes
        }

        public void writeTextMatrix(AffineTransform transform) throws IOException {
            double[] matrix = new double[6];
            transform.getMatrix(matrix);
            gen.writeln(gen.formatDouble5(matrix[0]) + " "
                + gen.formatDouble5(matrix[1]) + " "
                + gen.formatDouble5(matrix[2]) + " "
                + gen.formatDouble5(matrix[3]) + " "
                + gen.formatDouble5(matrix[4]) + " "
                + gen.formatDouble5(matrix[5]) + " Tm");
        }

        public boolean isFontChanging(Font f, char mapped) {
            if (f != getCurrentFont()) {
                int encoding = mapped / 256;
                if (encoding != getCurrentFontEncoding()) {
                    return true; //Font is changing
                }
            }
            return false; //Font is the same
        }

        public void selectFont(Font f, char mapped) throws IOException {
            int encoding = mapped / 256;
            String postfix = (encoding == 0 ? null : Integer.toString(encoding));
            PSResource res = getResourceForFont(f, postfix);
            gen.useFont("/" + res.getName(), f.getFontSize() / 1000f);
            gen.getResourceTracker().notifyResourceUsageOnPage(res);
        }

        public Font getCurrentFont() {
            return this.currentFont;
        }

        public int getCurrentFontEncoding() {
            return this.currentEncoding;
        }

        public void setCurrentFont(Font font, int encoding) {
            this.currentFont = font;
            this.currentEncoding = encoding;
        }

        public void setCurrentFont(Font font, char mapped) {
            int encoding = mapped / 256;
            setCurrentFont(font, encoding);
        }

        public void setupFonts(AttributedCharacterIterator runaci) {
            this.fonts = findFonts(runaci);
        }

        public boolean hasFonts() {
            return (fonts != null) && (fonts.length > 0);
        }

    }

    private class PSTextRun {

        private AffineTransform textTransform;
        private List relativePositions = new java.util.LinkedList();
        private StringBuffer currentChars = new StringBuffer();
        private int horizChanges = 0;
        private int vertChanges = 0;

        public void reset() {
            textTransform = null;
            currentChars.setLength(0);
            horizChanges = 0;
            vertChanges = 0;
            relativePositions.clear();
        }

        public int getHorizRunLength() {
            if (this.vertChanges == 0
                    && getRunLength() > 0) {
                return getRunLength();
            }
            return 0;
        }

        public void addCharacter(char paintChar, Point2D relPos) {
            addRelativePosition(relPos);
            currentChars.append(paintChar);
        }

        private void addRelativePosition(Point2D relPos) {
            if (getRunLength() > 0) {
                if (relPos.getX() != 0) {
                    horizChanges++;
                }
                if (relPos.getY() != 0) {
                    vertChanges++;
                }
            }
            relativePositions.add(relPos);
        }

        public void noteStartingTransformation(AffineTransform transform) {
            if (textTransform == null) {
                this.textTransform = new AffineTransform(transform);
            }
        }

        public int getRunLength() {
            return currentChars.length();
        }

        private boolean isXShow() {
            return vertChanges == 0;
        }

        private boolean isYShow() {
            return horizChanges == 0;
        }

        public void paint(PSGraphics2D g2d, TextUtil textUtil, TextPaintInfo tpi)
                    throws IOException {
            if (getRunLength() > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Text run: " + currentChars);
                }
                textUtil.writeTextMatrix(this.textTransform);
                if (isXShow()) {
                    log.debug("Horizontal text: xshow");
                    paintXYShow(g2d, textUtil, tpi.fillPaint, true, false);
                } else if (isYShow()) {
                    log.debug("Vertical text: yshow");
                    paintXYShow(g2d, textUtil, tpi.fillPaint, false, true);
                } else {
                    log.debug("Arbitrary text: xyshow");
                    paintXYShow(g2d, textUtil, tpi.fillPaint, true, true);
                }
                boolean stroke = (tpi.strokePaint != null) && (tpi.strokeStroke != null);
                if (stroke) {
                    log.debug("Stroked glyph outlines");
                    paintStrokedGlyphs(g2d, textUtil, tpi.strokePaint, tpi.strokeStroke);
                }
            }
        }

        private void paintXYShow(PSGraphics2D g2d, TextUtil textUtil, Paint paint,
                boolean x, boolean y) throws IOException {
            PSGenerator gen = textUtil.gen;
            char firstChar = this.currentChars.charAt(0);
            //Font only has to be setup up before the first character
            Font f = textUtil.selectFontForChar(firstChar);
            char mapped = f.mapChar(firstChar);
            textUtil.selectFont(f, mapped);
            textUtil.setCurrentFont(f, mapped);
            applyColor(paint, gen);

            StringBuffer sb = new StringBuffer();
            sb.append('(');
            for (int i = 0, c = this.currentChars.length(); i < c; i++) {
                char ch = this.currentChars.charAt(i);
                mapped = f.mapChar(ch);
                PSGenerator.escapeChar(mapped, sb);
            }
            sb.append(')');
            if (x || y) {
                sb.append("\n[");
                int idx = 0;
                Iterator iter = this.relativePositions.iterator();
                while (iter.hasNext()) {
                    Point2D pt = (Point2D)iter.next();
                    if (idx > 0) {
                        if (x) {
                            sb.append(format(gen, pt.getX()));
                        }
                        if (y) {
                            if (x) {
                                sb.append(' ');
                            }
                            sb.append(format(gen, -pt.getY()));
                        }
                        if (idx % 8 == 0) {
                            sb.append('\n');
                        } else {
                            sb.append(' ');
                        }
                    }
                    idx++;
                }
                if (x) {
                    sb.append('0');
                }
                if (y) {
                    if (x) {
                        sb.append(' ');
                    }
                    sb.append('0');
                }
                sb.append(']');
            }
            sb.append(' ');
            if (x) {
                sb.append('x');
            }
            if (y) {
                sb.append('y');
            }
            sb.append("show"); // --> xshow, yshow or xyshow
            gen.writeln(sb.toString());
        }

        private String format(PSGenerator gen, double coord) {
            if (Math.abs(coord) < 0.00001) {
                return "0";
            } else {
                return gen.formatDouble5(coord);
            }
        }

        private void paintStrokedGlyphs(PSGraphics2D g2d, TextUtil textUtil,
                Paint strokePaint, Stroke stroke) throws IOException {
            PSGenerator gen = textUtil.gen;

            applyColor(strokePaint, gen);
            PSGraphics2D.applyStroke(stroke, gen);

            Font f = null;
            Iterator iter = this.relativePositions.iterator();
            iter.next();
            Point2D pos = new Point2D.Double(0, 0);
            gen.writeln("0 0 M");
            for (int i = 0, c = this.currentChars.length(); i < c; i++) {
                char ch = this.currentChars.charAt(0);
                if (i == 0) {
                    //Font only has to be setup up before the first character
                    f = textUtil.selectFontForChar(ch);
                }
                char mapped = f.mapChar(ch);
                if (i == 0) {
                    textUtil.selectFont(f, mapped);
                    textUtil.setCurrentFont(f, mapped);
                }
                mapped = f.mapChar(this.currentChars.charAt(i));
                //add glyph outlines to current path
                char codepoint = (char)(mapped % 256);
                gen.write("(" + codepoint + ")");
                gen.writeln(" false charpath");

                if (iter.hasNext()) {
                    //Position for the next character
                    Point2D pt = (Point2D)iter.next();
                    pos.setLocation(pos.getX() + pt.getX(), pos.getY() - pt.getY());
                    gen.writeln(gen.formatDouble5(pos.getX()) + " "
                            + gen.formatDouble5(pos.getY()) + " M");
                }
            }
            gen.writeln("stroke"); //paints all accumulated glyph outlines
        }

    }

}


