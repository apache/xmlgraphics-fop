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

import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.Collections;
import java.util.List;

import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;

import org.apache.fop.complexscripts.util.CharAssociation;
import org.apache.fop.complexscripts.util.CharMirror;
import org.apache.fop.fonts.Font;

class ComplexGlyphVector extends FOPGVTGlyphVector {

    public static final AttributedCharacterIterator.Attribute WRITING_MODE
        = GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE;

    public static final Integer WRITING_MODE_RTL
        = GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE_RTL;

    private boolean reversed; // true if this GV was reversed
    private boolean mirrored; // true if this GV required some mirroring

    ComplexGlyphVector(FOPGVTFont font, final CharacterIterator iter, FontRenderContext frc) {
        super(font, iter, frc);
    }

    public void performDefaultLayout() {
        super.performDefaultLayout();
    }

    public boolean isReversed() {
        return reversed;
    }

    public void maybeReverse(boolean mirror) {
        if (!reversed) {
            if (glyphs != null) {
                if (glyphs.length > 1) {
                    reverse(glyphs);
                    if (associations != null) {
                        Collections.reverse(associations);
                    }
                    if (positions != null) {
                        reverse(positions);
                    }
                    if (boundingBoxes != null) {
                        reverse(boundingBoxes);
                    }
                    if (glyphTransforms != null) {
                        reverse(glyphTransforms);
                    }
                    if (glyphVisibilities != null) {
                        reverse(glyphVisibilities);
                    }
                }
                if (maybeMirror()) {
                    mirrored = true;
                }
            }
            reversed = true;
        }
    }

    // For each mirrorable character in source text, perform substitution of
    // associated glyph with a mirrored glyph. N.B. The source text is NOT
    // modified, only the mapped glyphs.
    private boolean maybeMirror() {
        boolean mirrored = false;
        String s = text.subSequence(text.getBeginIndex(), text.getEndIndex()).toString();
        if (CharMirror.hasMirrorable(s)) {
            String m = CharMirror.mirror(s);
            assert m.length() == s.length();
            for (int i = 0, n = m.length(); i < n; ++i) {
                char cs = s.charAt(i);
                char cm = m.charAt(i);
                if (cm != cs) {
                    if (substituteMirroredGlyph(i, cm)) {
                        mirrored = true;
                    }
                }
            }
        }
        return mirrored;
    }

    private boolean substituteMirroredGlyph(int index, char mirror) {
        Font f = font.getFont();
        int gi = 0;
        for (CharAssociation ca : (List<CharAssociation>) associations) {
            if (ca.contained(index, 1)) {
                setGlyphCode(gi, f.mapChar(mirror));
                return true;
            } else {
                ++gi;
            }
        }
        return false;
    }

    private static void reverse(boolean[] ba) {
        for (int i = 0, n = ba.length, m = n / 2; i < m; i++) {
            int k = n - i - 1;
            boolean t = ba [ k ];
            ba [ k ] = ba [ i ];
            ba [ i ] = t;
        }
    }

    private static void reverse(int[] ia) {
        for (int i = 0, n = ia.length, m = n / 2; i < m; i++) {
            int k = n - i - 1;
            int t = ia [ k ];
            ia [ k ] = ia [ i ];
            ia [ i ] = t;
        }
    }

    private static void reverse(float[] fa) {
        int skip = 2;
        int numPositions = fa.length / skip;
        for (int i = 0, n = numPositions, m = n / 2; i < m; ++i) {
            int j = n - i - 1;
            for (int k = 0; k < skip; ++k) {
                int l1 = i * skip + k;
                int l2 = j * skip + k;
                float t = fa [ l2 ];
                fa [ l2 ] = fa [ l1 ];
                fa [ l1 ] = t;
            }
        }
        float runAdvanceX = fa [ 0 ];
        for (int i = 0, n = fa.length; i < n; i += 2) {
            fa [ i ] = runAdvanceX - fa [ i ];
        }
    }

    private static void reverse(Rectangle2D[] ra) {
        for (int i = 0, n = ra.length, m = n / 2; i < m; i++) {
            int k = n - i - 1;
            Rectangle2D t = ra [ k ];
            ra [ k ] = ra [ i ];
            ra [ i ] = t;
        }
    }

    private static void reverse(AffineTransform[] ta) {
        for (int i = 0, n = ta.length, m = n / 2; i < m; i++) {
            int k = n - i - 1;
            AffineTransform t = ta [ k ];
            ta [ k ] = ta [ i ];
            ta [ i ] = t;
        }
    }


}
