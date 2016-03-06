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

package org.apache.fop.svg.font;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphJustificationInfo;
import java.awt.font.GlyphMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.List;

import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTGlyphMetrics;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.font.GVTLineMetrics;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fonts.GlyphMapping;
import org.apache.fop.fonts.TextFragment;
import org.apache.fop.traits.MinOptMax;

public class FOPGVTGlyphVector implements GVTGlyphVector {

    protected final TextFragment text;

    protected final FOPGVTFont font;

    private final int fontSize;

    private final FontMetrics fontMetrics;

    private final FontRenderContext frc;

    protected int[] glyphs;

    protected List associations;

    protected int[][] gposAdjustments;

    protected float[] positions;

    protected Rectangle2D[] boundingBoxes;

    protected GeneralPath outline;

    protected AffineTransform[] glyphTransforms;

    protected boolean[] glyphVisibilities;

    protected Rectangle2D logicalBounds;

    FOPGVTGlyphVector(FOPGVTFont font, final CharacterIterator iter, FontRenderContext frc) {
        this.text = new SVGTextFragment(iter);
        this.font = font;
        Font f = font.getFont();
        this.fontSize = f.getFontSize();
        this.fontMetrics = f.getFontMetrics();
        this.frc = frc;
    }

    public void performDefaultLayout() {
        Font f = font.getFont();
        MinOptMax letterSpaceIPD = MinOptMax.ZERO;
        MinOptMax[] letterSpaceAdjustments = new MinOptMax[text.getEndIndex() - text.getBeginIndex()];
        boolean retainControls = false;
        GlyphMapping mapping = GlyphMapping.doGlyphMapping(text, text.getBeginIndex(), text.getEndIndex(),
            f, letterSpaceIPD, letterSpaceAdjustments, '\0', '\0',
            false, text.getBidiLevel(), true, true, retainControls);
        CharacterIterator glyphAsCharIter =
            mapping.mapping != null ? new StringCharacterIterator(mapping.mapping) : text.getIterator();
        this.glyphs = buildGlyphs(f, glyphAsCharIter);
        this.associations = mapping.associations;
        this.gposAdjustments = mapping.gposAdjustments;
        this.positions = buildGlyphPositions(glyphAsCharIter, mapping.gposAdjustments, letterSpaceAdjustments);
        this.glyphVisibilities = new boolean[this.glyphs.length];
        Arrays.fill(glyphVisibilities, true);
        this.glyphTransforms = new AffineTransform[this.glyphs.length];
    }

    private static class SVGTextFragment implements TextFragment {

        private final CharacterIterator charIter;

        private String script;

        private String language;

        private int level = -1;

        SVGTextFragment(CharacterIterator charIter) {
            this.charIter = charIter;
            if (charIter instanceof AttributedCharacterIterator) {
                AttributedCharacterIterator aci = (AttributedCharacterIterator) charIter;
                aci.first();
                this.script = (String) aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.SCRIPT);
                this.language = (String) aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.LANGUAGE);
                Integer level = (Integer) aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.BIDI_LEVEL);
                if (level != null) {
                    this.level = level.intValue();
                }
            }
        }

        public CharacterIterator getIterator() {
            return charIter;
        }

        public int getBeginIndex() {
            return charIter.getBeginIndex();
        }

        public int getEndIndex() {
            return charIter.getEndIndex();
        }

        // TODO - [GA] the following appears to be broken because it ignores
        // sttart and end index arguments
        public CharSequence subSequence(int startIndex, int endIndex) {
            StringBuilder sb = new StringBuilder();
            for (char c = charIter.first(); c != CharacterIterator.DONE; c = charIter.next()) {
                sb.append(c);
            }
            return sb.toString();
        }

        public String getScript() {
            if (script != null) {
                return script;
            } else {
                return "auto";
            }
        }

        public String getLanguage() {
            if (language != null) {
                return language;
            } else {
                return "none";
            }
        }

        public int getBidiLevel() {
            return level;
        }

        public char charAt(int index) {
            return charIter.setIndex(index - charIter.getBeginIndex());
        }
    }

    private int[] buildGlyphs(Font font, final CharacterIterator glyphAsCharIter) {
        int[] glyphs = new int[glyphAsCharIter.getEndIndex() - glyphAsCharIter.getBeginIndex()];
        int index = 0;
        for (char c = glyphAsCharIter.first();  c != CharacterIterator.DONE; c = glyphAsCharIter.next()) {
            glyphs[index] = font.mapChar(c);
            index++;
        }
        return glyphs;
    }

    private static final int[] PA_ZERO = new int[4];

    /**
     * Build glyph position array.
     * @param glyphAsCharIter iterator for mapped glyphs as char codes (not glyph codes)
     * @param dp optionally null glyph position adjustments array
     * @param lsa optionally null letter space adjustments array
     * @return array of floats that denote [X,Y] position pairs for each glyph including
     * including an implied subsequent glyph; i.e., returned array contains one more pair
     * than the numbers of glyphs, where the position denoted by this last pair represents
     * the position after the last glyph has incurred advancement
     */
    private float[] buildGlyphPositions(final CharacterIterator glyphAsCharIter, int[][] dp, MinOptMax[] lsa) {
        int numGlyphs = glyphAsCharIter.getEndIndex() - glyphAsCharIter.getBeginIndex();
        float[] positions = new float[2 * (numGlyphs + 1)];
        float xc = 0f;
        float yc = 0f;
        if (dp != null) {
            for (int i = 0; i < numGlyphs + 1; ++i) {
                int[] pa = ((i >= dp.length) || (dp[i] == null)) ? PA_ZERO : dp[i];
                float xo = xc + ((float) pa[0]) / 1000f;
                float yo = yc - ((float) pa[1]) / 1000f;
                float xa = getGlyphWidth(i) + ((float) pa[2]) / 1000f;
                float ya = ((float) pa[3]) / 1000f;
                int k = 2 * i;
                positions[k + 0] = xo;
                positions[k + 1] = yo;
                xc += xa;
                yc += ya;
            }
        } else if (lsa != null) {
            for (int i = 0; i < numGlyphs + 1; ++i) {
                MinOptMax sa = (((i + 1) >= lsa.length) || (lsa[i + 1] == null)) ? MinOptMax.ZERO : lsa[i + 1];
                float xo = xc;
                float yo = yc;
                float xa = getGlyphWidth(i) + sa.getOpt() / 1000f;
                float ya = 0;
                int k = 2 * i;
                positions[k + 0] = xo;
                positions[k + 1] = yo;
                xc += xa;
                yc += ya;
            }
        }
        return positions;
    }

    private float getGlyphWidth(int index) {
        if (index < glyphs.length) {
            return fontMetrics.getWidth(glyphs[index], fontSize) / 1000000f;
        } else {
            return 0f;
        }
    }

    public GVTFont getFont() {
        return font;
    }

    public FontRenderContext getFontRenderContext() {
        return frc;
    }

    public void setGlyphCode(int glyphIndex, int glyphCode) {
        glyphs[glyphIndex] = glyphCode;
    }

    public int getGlyphCode(int glyphIndex) {
        return glyphs[glyphIndex];
    }

    public int[] getGlyphCodes(int beginGlyphIndex, int numEntries,
            int[] codeReturn) {
        if (codeReturn == null) {
            codeReturn = new int[numEntries];
        }
        System.arraycopy(glyphs, beginGlyphIndex, codeReturn, 0, numEntries);
        return codeReturn;
    }

    public GlyphJustificationInfo getGlyphJustificationInfo(int glyphIndex) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Shape getGlyphLogicalBounds(int glyphIndex) {
        GVTGlyphMetrics metrics = getGlyphMetrics(glyphIndex);
        Point2D pos = getGlyphPosition(glyphIndex);
        GVTLineMetrics fontMetrics = font.getLineMetrics(0);
        Rectangle2D bounds = new Rectangle2D.Float(0, -fontMetrics.getDescent(), metrics.getHorizontalAdvance(),
                fontMetrics.getAscent() + fontMetrics.getDescent());
        AffineTransform t = AffineTransform.getTranslateInstance(pos.getX(), pos.getY());
        AffineTransform transf = getGlyphTransform(glyphIndex);
        if (transf != null) {
            t.concatenate(transf);
        }
        t.scale(1, -1); // Translate from glyph coordinate system to user
        return t.createTransformedShape(bounds);
    }

    public GVTGlyphMetrics getGlyphMetrics(int glyphIndex) {
        Rectangle2D bbox = getBoundingBoxes()[glyphIndex];
        return new GVTGlyphMetrics(positions[2 * (glyphIndex + 1)] - positions[2 * glyphIndex],
                (fontMetrics.getAscender(fontSize) - fontMetrics.getDescender(fontSize)) / 1000000f,
                bbox, GlyphMetrics.STANDARD);
    }

    public Shape getGlyphOutline(int glyphIndex) {
        Shape glyphBox = getBoundingBoxes()[glyphIndex];
        AffineTransform tr = AffineTransform.getTranslateInstance(positions[glyphIndex * 2],
                positions[glyphIndex * 2 + 1]);
        AffineTransform glyphTransform = getGlyphTransform(glyphIndex);
        if (glyphTransform != null) {
            tr.concatenate(glyphTransform);
        }
        return tr.createTransformedShape(glyphBox);
    }

    public Rectangle2D getGlyphCellBounds(int glyphIndex) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public int[][] getGlyphPositionAdjustments() {
        return gposAdjustments;
    }

    public Point2D getGlyphPosition(int glyphIndex) {
        int positionIndex = glyphIndex * 2;
        return new Point2D.Float(positions[positionIndex], positions[positionIndex + 1]);
    }

    public float[] getGlyphPositions(int beginGlyphIndex, int numEntries, float[] positionReturn) {
        if (positionReturn == null) {
            positionReturn = new float[numEntries * 2];
        }
        System.arraycopy(positions, beginGlyphIndex * 2, positionReturn, 0, numEntries * 2);
        return positionReturn;
    }

    public AffineTransform getGlyphTransform(int glyphIndex) {
        return glyphTransforms[glyphIndex];
    }

    public Shape getGlyphVisualBounds(int glyphIndex) {
        Rectangle2D bbox = getBoundingBoxes()[glyphIndex];
        Point2D pos = getGlyphPosition(glyphIndex);
        AffineTransform t = AffineTransform.getTranslateInstance(pos.getX(), pos.getY());
        AffineTransform transf = getGlyphTransform(glyphIndex);
        if (transf != null) {
            t.concatenate(transf);
        }
        return t.createTransformedShape(bbox);
    }

    public Rectangle2D getLogicalBounds() {
        if (logicalBounds == null) {
            GeneralPath logicalBoundsPath = new GeneralPath();
            for (int i = 0; i < getNumGlyphs(); i++) {
                Shape glyphLogicalBounds = getGlyphLogicalBounds(i);
                logicalBoundsPath.append(glyphLogicalBounds, false);
            }
            logicalBounds = logicalBoundsPath.getBounds2D();
        }
        return logicalBounds;
    }

    public int getNumGlyphs() {
        return glyphs.length;
    }

    public Shape getOutline() {
        if (outline == null) {
            outline = new GeneralPath();
            for (int i = 0; i < glyphs.length; i++) {
                outline.append(getGlyphOutline(i), false);
            }
        }
        return outline;
    }

    public Shape getOutline(float x, float y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Rectangle2D getGeometricBounds() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Rectangle2D getBounds2D(AttributedCharacterIterator aci) {
        return getOutline().getBounds2D();
    }

    public void setGlyphPosition(int glyphIndex, Point2D newPos) {
        int idx = glyphIndex * 2;
        positions[idx] = (float) newPos.getX();
        positions[idx + 1] = (float) newPos.getY();
    }

    public void setGlyphTransform(int glyphIndex, AffineTransform newTX) {
        glyphTransforms[glyphIndex] = newTX;
    }

    public void setGlyphVisible(int glyphIndex, boolean visible) {
        glyphVisibilities[glyphIndex] = visible;
    }

    public boolean isGlyphVisible(int glyphIndex) {
        return glyphVisibilities[glyphIndex];
    }

    public int getCharacterCount(int startGlyphIndex, int endGlyphIndex) {
        // TODO Not that simple if complex scripts are involved
        return endGlyphIndex - startGlyphIndex + 1;
    }

    public boolean isReversed() {
        return false;
    }

    public void maybeReverse(boolean mirror) {
    }

    public void draw(Graphics2D graphics2d, AttributedCharacterIterator aci) {
        // NOP
    }

    private Rectangle2D[] getBoundingBoxes() {
        if (boundingBoxes == null) {
            buildBoundingBoxes();
        }
        return boundingBoxes;
    }

    private void buildBoundingBoxes() {
        boundingBoxes = new Rectangle2D[glyphs.length];
        for (int i = 0; i < glyphs.length; i++) {
            Rectangle bbox = fontMetrics.getBoundingBox(glyphs[i], fontSize);
            boundingBoxes[i] = new Rectangle2D.Float(bbox.x / 1000000f, -(bbox.y + bbox.height) / 1000000f,
                    bbox.width / 1000000f, bbox.height / 1000000f);
        }
    }

}
