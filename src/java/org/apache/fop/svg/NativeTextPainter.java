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
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.batik.bridge.SVGGVTFont;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.gvt.font.FontFamilyResolver;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.renderer.StrokingTextPainter;
import org.apache.batik.gvt.renderer.StrokingTextPainter.TextChunk;
import org.apache.batik.gvt.text.GlyphLayout;
import org.apache.batik.gvt.text.TextLayoutFactory;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.gvt.text.TextSpanLayout;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.svg.font.FOPFontFamilyResolverImpl;
import org.apache.fop.svg.font.FOPGVTFont;
import org.apache.fop.svg.font.FOPGVTGlyphVector;
import org.apache.fop.svg.text.BidiAttributedCharacterIterator;
import org.apache.fop.svg.text.ComplexGlyphLayout;
import org.apache.fop.util.CharUtilities;

/**
 * Abstract base class for text painters that use specialized text commands native to an output
 * format to render text.
 */
public abstract class NativeTextPainter extends StrokingTextPainter {

    /** the logger for this class */
    protected static final Log log = LogFactory.getLog(NativeTextPainter.class);

    /** the font collection */
    protected final FontInfo fontInfo;

    protected final FontFamilyResolver fontFamilyResolver;

    protected Font font;

    protected TextPaintInfo tpi;

    /**
     * Creates a new instance.
     * @param fontInfo the font collection
     */
    public NativeTextPainter(FontInfo fontInfo) {
        this.fontInfo = fontInfo;
        this.fontFamilyResolver = new FOPFontFamilyResolverImpl(fontInfo);
    }

    /**
     * Indicates whether the given {@link Graphics2D} instance if compatible with this text painter
     * implementation.
     * @param g2d the instance to check
     * @return true if the instance is compatible.
     */
    protected abstract boolean isSupported(Graphics2D g2d);

    /**
     * Paints a single text run.
     * @param textRun the text run
     * @param g2d the target Graphics2D instance
     * @throws IOException if an I/O error occurs while rendering the text
     */
    protected final void paintTextRun(TextRun textRun, Graphics2D g2d) throws IOException {
        logTextRun(textRun);
        AttributedCharacterIterator runaci = textRun.getACI();
        runaci.first();
        tpi = (TextPaintInfo) runaci.getAttribute(PAINT_INFO);
        if (tpi == null || !tpi.visible) {
            return;
        }
        if (tpi.composite != null) {
            g2d.setComposite(tpi.composite);
        }
        TextSpanLayout layout = textRun.getLayout();
        GVTGlyphVector gv = layout.getGlyphVector();
        if (!(gv.getFont() instanceof FOPGVTFont)) {
            assert gv.getFont() == null || gv.getFont() instanceof SVGGVTFont;
            //Draw using Java2D when no native fonts are available
            textRun.getLayout().draw(g2d);
            return;
        } else {
            GeneralPath debugShapes = log.isDebugEnabled() ? new GeneralPath() : null;
            preparePainting(g2d);
            saveGraphicsState();
            setInitialTransform(g2d.getTransform());
            clip(g2d.getClip());
            beginTextObject();
            writeGlyphs((FOPGVTGlyphVector) gv, debugShapes);
            endTextObject();
            restoreGraphicsState();
            if (debugShapes != null) {
                g2d.setStroke(new BasicStroke(0));
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.draw(debugShapes);
            }
        }
    }

    protected void writeGlyphs(FOPGVTGlyphVector gv, GeneralPath debugShapes) throws IOException {
        AffineTransform localTransform = new AffineTransform();
        Point2D prevPos = null;
        AffineTransform prevGlyphTransform = null;
        font = ((FOPGVTFont) gv.getFont()).getFont();
        for (int index = 0, c = gv.getNumGlyphs(); index < c; index++) {
            if (!gv.isGlyphVisible(index)) {
                continue;
            }
            Point2D glyphPos = gv.getGlyphPosition(index);

            AffineTransform glyphTransform = gv.getGlyphTransform(index);
            if (log.isTraceEnabled()) {
                log.trace("pos " + glyphPos + ", transform " + glyphTransform);
            }
            if (debugShapes != null) {
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

            positionGlyph(prevPos, glyphPos, glyphTransform != null || prevGlyphTransform != null);
            char glyph = (char) gv.getGlyphCode(index);
            //Update last position
            prevPos = glyphPos;
            prevGlyphTransform = glyphTransform;

            writeGlyph(glyph, localTransform);
        }
    }

    @Override
    protected void paintTextRuns(@SuppressWarnings("rawtypes") List textRuns, Graphics2D g2d) {
        if (log.isTraceEnabled()) {
            log.trace("paintTextRuns: count = " + textRuns.size());
        }
        if (!isSupported(g2d)) {
            super.paintTextRuns(textRuns, g2d);
            return;
        }
        for (int i = 0; i < textRuns.size(); i++) {
            TextRun textRun = (TextRun) textRuns.get(i);
            try {
                paintTextRun(textRun, g2d);
            } catch (IOException ioe) {
                //No other possibility than to use a RuntimeException
                throw new RuntimeException(ioe);
            }
        }
    }

    /**
     * Collects all characters from an {@link AttributedCharacterIterator}.
     * @param runaci the character iterator
     * @return the characters
     */
    protected CharSequence collectCharacters(AttributedCharacterIterator runaci) {
        StringBuffer chars = new StringBuffer();
        for (runaci.first(); runaci.getIndex() < runaci.getEndIndex();) {
            chars.append(runaci.current());
            runaci.next();
        }
        return chars;
    }

    // Use FOP's bidi algorithm implementation and sub-divide each chunk into runs
    // that respect bidi level boundaries. N.B. batik does not sub-divide chunks at
    // bidi level boundaries because it performs eager reordering. In FOP, we need
    // to perform lazy reordering after character to glyph mapping occurs since
    // that mapping process requires logical (not visual) ordered input.
    @Override
    public List computeTextRuns(TextNode node, AttributedCharacterIterator nodeACI,
        AttributedCharacterIterator [] chunkACIs) {
        nodeACI.first();
        int defaultBidiLevel = (nodeACI.getAttribute(WRITING_MODE) == WRITING_MODE_RTL) ? 1 : 0;
        for (int i = 0, n = chunkACIs.length; i < n; ++i) {
            chunkACIs[i] = new BidiAttributedCharacterIterator(chunkACIs[i], defaultBidiLevel);
        }
        return super.computeTextRuns(node, nodeACI, chunkACIs, (int[][]) null);
    }

    // We want to sub-divide text chunks into distinct runs at bidi level boundaries.
    @Override
    protected Set getTextRunBoundaryAttributes() {
        Set textRunBoundaryAttributes = super.getTextRunBoundaryAttributes();
        if (!textRunBoundaryAttributes.contains(BIDI_LEVEL)) {
            textRunBoundaryAttributes.add(BIDI_LEVEL);
        }
        return textRunBoundaryAttributes;
    }

    // Perform reordering of runs.
    @Override
    protected List reorderTextRuns(TextChunk chunk, List runs) {
        // 1. determine min/max bidi levels for runs
        int mn = -1;
        int mx = -1;
        for (TextRun r : (List<TextRun>) runs) {
            int level = r.getBidiLevel();
            if (level >= 0) {
                if ((mn < 0) || (level < mn)) {
                    mn = level;
                }
                if ((mx < 0) || (level > mx)) {
                    mx = level;
                }
            }
        }

        // 2. reorder from maximum level to minimum odd level
        if (mx > 0) {
            for (int l1 = mx, l2 = ((mn & 1) == 0) ? (mn + 1) : mn; l1 >= l2; l1--) {
                runs = reorderRuns(runs, l1);
            }
        }

        // 3. reverse glyphs (and perform mirroring) in runs as needed
        boolean mirror = true;
        reverseGlyphs(runs, mirror);

        return runs;
    }

    private List reorderRuns(List runs, int level) {
        assert level >= 0;
        List runsNew = new java.util.ArrayList();
        for (int i = 0, n = runs.size(); i < n; i++) {
            TextRun tri = (TextRun) runs.get(i);
            if (tri.getBidiLevel() < level) {
                runsNew.add(tri);
            } else {
                int s = i;
                int e = s;
                while (e < n) {
                    TextRun tre = (TextRun) runs.get(e);
                    if (tre.getBidiLevel() < level) {
                        break;
                    } else {
                        e++;
                    }
                }
                if (s < e) {
                    runsNew.addAll(reverseRuns(runs, s, e));
                }
                i = e - 1;
            }
        }
        if (!runsNew.equals(runs)) {
            runs = runsNew;
        }
        return runs;
    }

    private List reverseRuns(List runs, int s, int e) {
        int n = e - s;
        List runsNew = new java.util.ArrayList(n);
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                int k = (n - i - 1);
                TextRun tr = (TextRun) runs.get(s + k);
                tr.reverse();
                runsNew.add(tr);
            }
        }
        return runsNew;
    }

    private void reverseGlyphs(List runs, boolean mirror) {
        for (TextRun r : (List<TextRun>) runs) {
            r.maybeReverseGlyphs(mirror);
        }
    }

    protected abstract void preparePainting(Graphics2D g2d);

    protected abstract void saveGraphicsState() throws IOException;

    protected abstract void restoreGraphicsState() throws IOException;

    protected abstract void setInitialTransform(AffineTransform transform) throws IOException;

    protected abstract void clip(Shape clip) throws IOException;

    protected abstract void beginTextObject() throws IOException;

    protected abstract void endTextObject() throws IOException;

    protected abstract void positionGlyph(Point2D prevPos, Point2D glyphPos, boolean reposition);

    protected abstract void writeGlyph(char glyph, AffineTransform transform) throws IOException;


    /**
     * @param runaci an attributed character iterator
     * @param layout a text span layout
     */
    protected final void logTextRun(TextRun textRun) {
        AttributedCharacterIterator runaci = textRun.getACI();
        TextSpanLayout layout = textRun.getLayout();
        runaci.first();
        if (log.isTraceEnabled()) {
            int charCount = runaci.getEndIndex() - runaci.getBeginIndex();
            log.trace("================================================");
            log.trace("New text run:");
            log.trace("char count: " + charCount);
            log.trace("range: "
                    + runaci.getBeginIndex() + " - " + runaci.getEndIndex());
            log.trace("glyph count: " + layout.getGlyphCount()); //=getNumGlyphs()
        }
    }

    /**
     * @param ch a character
     * @param layout a text span layout
     * @param index an index
     * @param visibleChar visible character flag
     */
    protected final void logCharacter(char ch, TextSpanLayout layout, int index,
            boolean visibleChar) {
        if (log.isTraceEnabled()) {
            log.trace("glyph " + index
                    + " -> " + layout.getGlyphIndex(index) + " => " + ch);
            if (CharUtilities.isAnySpace(ch) && ch != 32) {
                log.trace("Space found: " + Integer.toHexString(ch));
            } else if (ch == CharUtilities.ZERO_WIDTH_JOINER) {
                log.trace("ZWJ found: " + Integer.toHexString(ch));
            } else if (ch == CharUtilities.SOFT_HYPHEN) {
                log.trace("Soft hyphen found: " + Integer.toHexString(ch));
            }
            if (!visibleChar) {
                log.trace("Invisible glyph found: " + Integer.toHexString(ch));
            }
        }
    }

    @Override
    protected FontFamilyResolver getFontFamilyResolver() {
        return this.fontFamilyResolver;
    }

    private static final TextLayoutFactory COMPLEX_SCRIPT_TEXT_LAYOUT_FACTORY =
        new TextLayoutFactory() {
            public TextSpanLayout createTextLayout(AttributedCharacterIterator aci,
                int [] charMap, Point2D offset, FontRenderContext frc) {
                if (ComplexGlyphLayout.mayRequireComplexLayout(aci)) {
                    return new ComplexGlyphLayout(aci, charMap, offset, frc);
                } else {
                    return new GlyphLayout(aci, charMap, offset, frc);
                }
            }
        };

    @Override
    protected TextLayoutFactory getTextLayoutFactory() {
        return COMPLEX_SCRIPT_TEXT_LAYOUT_FACTORY;
    }

}
