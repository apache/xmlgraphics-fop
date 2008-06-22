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

package org.apache.fop.fonts;

import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.properties.CommonFont;

/**
 * Helper class for automatic font selection.
 * <p>
 * TODO: Check if this could be merged with another font class, such as
 * {@link FontManager}.
 */
public final class FontSelector {
    private FontSelector() {
        // Static since this is an utility class.
    }

    private static Font selectFontForCharacter(char c, FONode fonode,
            CommonFont commonFont, PercentBaseContext context) {
        FontInfo fi = fonode.getFOEventHandler().getFontInfo();
        FontTriplet[] fontkeys = commonFont.getFontState(fi);
        for (int i = 0; i < fontkeys.length; i++) {
            Font font = fi.getFontInstance(fontkeys[i], commonFont.fontSize
                    .getValue(context));
            if (font.hasChar(c)) {
                return font;
            }
        }
        return fi.getFontInstance(fontkeys[0], commonFont.fontSize
                .getValue(context));

    }

    /**
     * Selects a font which is able to display the given character.
     * 
     * @param fobj
     *            a Character object containing the character and its
     *            attributes.
     * @param context
     *            the Percent-based context needed for creating the actual font.
     * @return a Font object.
     */
    public static Font selectFontForCharacter(Character fobj,
            PercentBaseContext context) {
        return FontSelector.selectFontForCharacter(fobj.getCharacter(), fobj,
                fobj.getCommonFont(), context);
    }

    /**
     * Selects a font which is able to display the given character.
     * 
     * @param c
     *            character to find.
     * @param text
     *            the text object which contains the character
     * @param context
     *            the Percent-based context needed for creating the actual font.
     * @return a Font object.
     */
    public static Font selectFontForCharacterInText(char c, FOText text,
            PercentBaseContext context) {
        return FontSelector.selectFontForCharacter(c, text, text
                .getCommonFont(), context);
    }

    /**
     * Selects a font which is able to display the most of the given characters.
     * 
     * @param charSeq
     *            Text to go through
     * @param firstIndex
     *            first index within text.
     * @param breakIndex
     *            last index +1 within text.
     * @param text
     *            the text object which contains the character
     * @param context
     *            the Percent-based context needed for creating the actual font.
     * @return a Font object.
     */
    public static Font selectFontForCharactersInText(CharSequence charSeq,
            int firstIndex, int breakIndex, FOText text,
            PercentBaseContext context) {

        final FontInfo fi = text.getFOEventHandler().getFontInfo();
        final CommonFont commonFont = text.getCommonFont();
        final FontTriplet[] fontkeys = commonFont.getFontState(fi);
        final int numFonts = fontkeys.length;
        final Font[] fonts = new Font[numFonts];
        final int[] fontCount = new int[numFonts];

        for (int fontnum = 0; fontnum < numFonts; fontnum++) {
            final Font font = fi.getFontInstance(fontkeys[fontnum],
                    commonFont.fontSize.getValue(context));
            fonts[fontnum] = font;
            for (int pos = firstIndex; pos < breakIndex; pos++) {
                if (font.hasChar(charSeq.charAt(pos))) {
                    fontCount[fontnum]++;
                }
            }

            // quick fall through if all characters can be displayed
            if (fontCount[fontnum] == (breakIndex - firstIndex)) {
                return font;
            }
        }

        Font font = fonts[0];
        int max = fontCount[0];

        for (int fontnum = 1; fontnum < numFonts; fontnum++) {
            final int curCount = fontCount[fontnum];
            if (curCount > max) {
                font = fonts[fontnum];
                max = curCount;
            }
        }
        return font;
    }

}
