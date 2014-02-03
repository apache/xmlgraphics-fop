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

import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTGlyphMetrics;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.font.GVTLineMetrics;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fonts.GlyphMapping;
import org.apache.fop.fonts.TextFragment;
import org.apache.fop.traits.MinOptMax;

class FOPGVTGlyphVector implements GVTGlyphVector {

    private final CharacterIterator charIter;

    private final FOPGVTFont font;

    private final int fontSize;

    private final FontMetrics fontMetrics;

    private final FontRenderContext frc;

    private int[] glyphs;

    private float[] positions;

    private Rectangle2D[] boundingBoxes;

    private GeneralPath outline;

    private AffineTransform[] glyphTransforms;

    private boolean[] glyphVisibilities;

    private Rectangle2D logicalBounds;

    FOPGVTGlyphVector(FOPGVTFont font, final CharacterIterator iter, FontRenderContext frc) {
        this.charIter = iter;
        this.font = font;
        Font f = font.getFont();
        this.fontSize = f.getFontSize();
        this.fontMetrics = f.getFontMetrics();
        this.frc = frc;
    }

    public void performDefaultLayout() {
        Font f = font.getFont();
        TextFragment text = new SVGTextFragment(charIter);
        MinOptMax letterSpaceIPD = MinOptMax.ZERO;
        MinOptMax[] letterSpaceAdjustments = new MinOptMax[charIter.getEndIndex() - charIter.getBeginIndex()];
        GlyphMapping mapping = GlyphMapping.doGlyphMapping(text, charIter.getBeginIndex(), charIter.getEndIndex(),
                f, letterSpaceIPD, letterSpaceAdjustments, '\0', '\0', false, 0 /* TODO */);
        glyphs = buildGlyphs(f, mapping.mapping != null ? new StringCharacterIterator(mapping.mapping) : charIter);
        buildGlyphPositions(mapping, letterSpaceAdjustments);
        this.glyphVisibilities = new boolean[this.glyphs.length];
        Arrays.fill(glyphVisibilities, true);
        this.glyphTransforms = new AffineTransform[this.glyphs.length];
    }

    private static class SVGTextFragment implements TextFragment {

        private final CharacterIterator charIter;

        SVGTextFragment(CharacterIterator charIter) {
            this.charIter = charIter;
        }

        public CharSequence subSequence(int startIndex, int endIndex) {
            StringBuilder sb = new StringBuilder();
            for (char c = charIter.first(); c != CharacterIterator.DONE; c = charIter.next()) {
                sb.append(c);
            }
            return sb.toString();
        }

        public String getScript() {
            return "DFLT"; // TODO pass on script value from SVG
        }

        public String getLanguage() {
            return "dflt"; // TODO pass on language value from SVG
        }

        public char charAt(int index) {
            return charIter.setIndex(index - charIter.getBeginIndex());
        }
    }

    private int[] buildGlyphs(Font font, final CharacterIterator charIter) {
        int[] glyphs = new int[charIter.getEndIndex() - charIter.getBeginIndex()];
        int index = 0;
        for (char c = charIter.first();  c != CharacterIterator.DONE; c = charIter.next()) {
            glyphs[index] = font.mapChar(c);
            index++;
        }
        return glyphs;
    }

    private void buildGlyphPositions(GlyphMapping ai, MinOptMax[] letterSpaceAdjustments) {
        positions = new float[2 * glyphs.length + 2];
        if (ai.gposAdjustments != null) {
            assert ai.gposAdjustments.length == glyphs.length;
            for (int glyphIndex = 0; glyphIndex < glyphs.length; glyphIndex++) {
                int n = 2 * glyphIndex;
                if (ai.gposAdjustments[glyphIndex] != null) {
                    for (int p = 0; p < 4; p++) {
                        positions[n + p] += ai.gposAdjustments[glyphIndex][p] / 1000f;
                    }
                }
                positions[n + 2] += positions[n] + getGlyphWidth(glyphIndex);
            }
        } else {
            for (int i = 0, n = 2; i < glyphs.length; i++, n += 2) {
                int kern = i < glyphs.length - 1 && letterSpaceAdjustments[i + 1] != null
                        ? letterSpaceAdjustments[i + 1].getOpt()
                        : 0;
                positions[n] = positions[n - 2] + getGlyphWidth(i) + kern / 1000f;
                positions[n + 1] = 0;
            }
        }
    }

    private float getGlyphWidth(int index) {
        return fontMetrics.getWidth(glyphs[index], fontSize) / 1000000f;
    }

    public GVTFont getFont() {
        return font;
    }

    public FontRenderContext getFontRenderContext() {
        return frc;
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
