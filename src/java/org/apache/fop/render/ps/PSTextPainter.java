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
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.batik.gvt.text.TextPaintInfo;

import org.apache.xmlgraphics.java2d.ps.PSGraphics2D;
import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.svg.NativeTextPainter;
import org.apache.fop.util.HexEncoder;

/**
 * Renders the attributed character iterator of a {@link org.apache.batik.gvt.TextNode TextNode}.
 * This class draws the text directly using PostScript text operators so
 * the text is not drawn using shapes which makes the PS files larger.
 * <p>
 * The text runs are split into smaller text runs that can be bundles in single
 * calls of the xshow, yshow or xyshow operators. For outline text, the charpath
 * operator is used.
 */
public class PSTextPainter extends NativeTextPainter {

    private FontResourceCache fontResources;

    private PSGraphics2D ps;

    private PSGenerator gen;

    private TextUtil textUtil;

    private boolean flushCurrentRun;

    private PSTextRun psRun;

    private Double relPos;

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

    @Override
    protected void preparePainting(Graphics2D g2d) {
        ps = (PSGraphics2D) g2d;
        gen = ps.getPSGenerator();
        ps.preparePainting();
    }

    @Override
    protected void saveGraphicsState() throws IOException {
        gen.saveGraphicsState();
    }

    @Override
    protected void restoreGraphicsState() throws IOException {
        gen.restoreGraphicsState();
    }

    @Override
    protected void setInitialTransform(AffineTransform transform) throws IOException {
        gen.concatMatrix(transform);
    }

    private PSFontResource getResourceForFont(Font f, String postfix) {
        String key = (postfix != null ? f.getFontName() + '_' + postfix : f.getFontName());
        return this.fontResources.getFontResourceForFontKey(key);
    }

    @Override
    protected void clip(Shape shape) throws IOException {
        if (shape == null) {
            return;
        }
        ps.getPSGenerator().writeln("newpath");
        PathIterator iter = shape.getPathIterator(IDENTITY_TRANSFORM);
        ps.processPathIterator(iter);
        ps.getPSGenerator().writeln("clip");
    }

    @Override
    protected void beginTextObject() throws IOException {
        gen.writeln("BT");
        textUtil = new TextUtil();
        psRun = new PSTextRun(); //Used to split a text run into smaller runs
    }

    @Override
    protected void endTextObject() throws IOException {
        psRun.paint(ps, textUtil, tpi);
        gen.writeln("ET");
    }

    @Override
    protected void positionGlyph(Point2D prevPos, Point2D glyphPos, boolean reposition) {
        flushCurrentRun = false;
        //Try to optimize by combining characters using the same font and on the same line.
        if (reposition) {
            //Happens for text-on-a-path
            flushCurrentRun = true;
        }
        if (psRun.getRunLength() >= 128) {
            //Don't let a run get too long
            flushCurrentRun = true;
        }

        //Note the position of the glyph relative to the previous one
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
    }

    @Override
    protected void writeGlyph(char glyph, AffineTransform localTransform) throws IOException {
        boolean fontChanging = textUtil.isFontChanging(font, glyph);
        if (fontChanging) {
            flushCurrentRun = true;
        }

        if (flushCurrentRun) {
            //Paint the current run and reset for the next run
            psRun.paint(ps, textUtil, tpi);
            psRun.reset();
        }

        //Track current run
        psRun.addGlyph(glyph, relPos);
        psRun.noteStartingTransformation(localTransform);

        //Change font if necessary
        if (fontChanging) {
            textUtil.setCurrentFont(font, glyph);
        }
    }

    private class TextUtil {

        private Font currentFont;
        private int currentEncoding = -1;

        public boolean isMultiByte(Font f) {
            FontMetrics metrics = f.getFontMetrics();
            boolean multiByte = metrics instanceof MultiByteFont || metrics instanceof LazyFont
                    && ((LazyFont) metrics).getRealFont() instanceof MultiByteFont;
            return multiByte;
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
            // this is only applicable for single byte fonts
            if (!isMultiByte(f)) {
                if (f != getCurrentFont()) {
                    return true;
                }
                if (mapped / 256 != getCurrentFontEncoding()) {
                    return true;
                }
            }
            return false; //Font is the same
        }

        public void selectFont(Font f, char mapped) throws IOException {
            int encoding = mapped / 256;
            String postfix = (!isMultiByte(f) && encoding > 0 ? Integer.toString(encoding) : null);
            PSFontResource res = getResourceForFont(f, postfix);
            gen.useFont("/" + res.getName(), f.getFontSize() / 1000f);
            res.notifyResourceUsageOnPage(gen.getResourceTracker());
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

    }

    private class PSTextRun {

        private AffineTransform textTransform;
        private List<Point2D> relativePositions = new LinkedList<Point2D>();
        private StringBuffer currentGlyphs = new StringBuffer();
        private int horizChanges = 0;
        private int vertChanges = 0;

        public void reset() {
            textTransform = null;
            currentGlyphs.setLength(0);
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

        public void addGlyph(char glyph, Point2D relPos) {
            addRelativePosition(relPos);
            currentGlyphs.append(glyph);
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
            return currentGlyphs.length();
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
            char glyph = currentGlyphs.charAt(0);
            textUtil.selectFont(font, glyph);
            textUtil.setCurrentFont(font, glyph);
            applyColor(paint);

            boolean multiByte = textUtil.isMultiByte(font);
            StringBuffer sb = new StringBuffer();
            sb.append(multiByte ? '<' : '(');
            for (int i = 0, c = this.currentGlyphs.length(); i < c; i++) {
                glyph = this.currentGlyphs.charAt(i);
                if (multiByte) {
                    sb.append(HexEncoder.encode(glyph));
                } else {
                    char codepoint = (char) (glyph % 256);
                    PSGenerator.escapeChar(codepoint, sb);
                }
            }
            sb.append(multiByte ? '>' : ')');
            if (x || y) {
                sb.append("\n[");
                int idx = 0;
                Iterator<Point2D> iter = this.relativePositions.iterator();
                while (iter.hasNext()) {
                    Point2D pt = iter.next();
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

        private void applyColor(Paint paint) throws IOException {
            if (paint == null) {
                return;
            } else if (paint instanceof Color) {
                Color col = (Color) paint;
                gen.useColor(col);
            } else {
                log.warn("Paint not supported: " + paint.toString());
            }
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
            applyColor(strokePaint);
            PSGraphics2D.applyStroke(stroke, gen);

            Iterator<Point2D> iter = this.relativePositions.iterator();
            iter.next();
            Point2D pos = new Point2D.Double(0, 0);
            gen.writeln("0 0 M");
            for (int i = 0, c = this.currentGlyphs.length(); i < c; i++) {
                char mapped = this.currentGlyphs.charAt(i);
                if (i == 0) {
                    textUtil.selectFont(font, mapped);
                    textUtil.setCurrentFont(font, mapped);
                }
                //add glyph outlines to current path
                FontMetrics metrics = font.getFontMetrics();
                boolean multiByte = metrics instanceof MultiByteFont
                        || metrics instanceof LazyFont
                                && ((LazyFont) metrics).getRealFont() instanceof MultiByteFont;
                if (multiByte) {
                    gen.write("<");
                    gen.write(HexEncoder.encode(mapped));
                    gen.write(">");
                } else {
                    char codepoint = (char) (mapped % 256);
                    gen.write("(" + codepoint + ")");
                }
                gen.writeln(" false charpath");

                if (iter.hasNext()) {
                    //Position for the next character
                    Point2D pt = iter.next();
                    pos.setLocation(pos.getX() + pt.getX(), pos.getY() - pt.getY());
                    gen.writeln(gen.formatDouble5(pos.getX()) + " "
                            + gen.formatDouble5(pos.getY()) + " M");
                }
            }
            gen.writeln("stroke"); //paints all accumulated glyph outlines
        }

    }

}


