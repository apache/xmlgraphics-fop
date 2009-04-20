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

import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.Iterator;
import java.util.List;

import org.apache.batik.bridge.SVGFontFamily;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.gvt.renderer.StrokingTextPainter;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextSpanLayout;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.util.CharUtilities;

/**
 * Abstract base class for text painters that use specialized text commands native to an output
 * format to render text.
 */
public abstract class NativeTextPainter extends StrokingTextPainter {

    /** the logger for this class */
    protected Log log = LogFactory.getLog(NativeTextPainter.class);

    /** the font collection */
    protected final FontInfo fontInfo;

    /**
     * Creates a new instance.
     * @param fontInfo the font collection
     */
    public NativeTextPainter(FontInfo fontInfo) {
        this.fontInfo = fontInfo;
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
    protected abstract void paintTextRun(TextRun textRun, Graphics2D g2d) throws IOException;

    /** {@inheritDoc} */
    protected void paintTextRuns(List textRuns, Graphics2D g2d) {
        if (log.isTraceEnabled()) {
            log.trace("paintTextRuns: count = " + textRuns.size());
        }
        if (!isSupported(g2d)) {
            super.paintTextRuns(textRuns, g2d);
            return;
        }
        for (int i = 0; i < textRuns.size(); i++) {
            TextRun textRun = (TextRun)textRuns.get(i);
            try {
                paintTextRun(textRun, g2d);
            } catch (IOException ioe) {
                //No other possibility than to use a RuntimeException
                throw new RuntimeException(ioe);
            }
        }
    }

    /**
     * Finds an array of suitable fonts for a given AttributedCharacterIterator.
     * @param aci the character iterator
     * @return the array of fonts
     */
    protected Font[] findFonts(AttributedCharacterIterator aci) {
        List fonts = new java.util.ArrayList();
        List gvtFonts = (List) aci.getAttribute(
                GVTAttributedCharacterIterator.TextAttribute.GVT_FONT_FAMILIES);
        Float posture = (Float) aci.getAttribute(TextAttribute.POSTURE);
        Float taWeight = (Float) aci.getAttribute(TextAttribute.WEIGHT);
        Float fontSize = (Float) aci.getAttribute(TextAttribute.SIZE);

        String style = ((posture != null) && (posture.floatValue() > 0.0))
                       ? Font.STYLE_ITALIC : Font.STYLE_NORMAL;
        int weight = ((taWeight != null)
                       &&  (taWeight.floatValue() > 1.0)) ? Font.WEIGHT_BOLD
                       : Font.WEIGHT_NORMAL;

        String firstFontFamily = null;

        //GVT_FONT can sometimes be different from the fonts in GVT_FONT_FAMILIES
        //or GVT_FONT_FAMILIES can even be empty and only GVT_FONT is set
        /* The following code section is not available until Batik 1.7 is released. */
        GVTFont gvtFont = (GVTFont)aci.getAttribute(
                GVTAttributedCharacterIterator.TextAttribute.GVT_FONT);
        if (gvtFont != null) {
            try {
                String gvtFontFamily = gvtFont.getFamilyName(); //Not available in Batik 1.6!
                if (log.isDebugEnabled()) {
                    log.debug("Matching font family: " + gvtFontFamily);
                }
                if (fontInfo.hasFont(gvtFontFamily, style, weight)) {
                    FontTriplet triplet = fontInfo.fontLookup(gvtFontFamily, style,
                                                       weight);
                    int fsize = (int)(fontSize.floatValue() * 1000);
                    fonts.add(fontInfo.getFontInstance(triplet, fsize));
                }
                firstFontFamily = gvtFontFamily;
            } catch (Exception e) {
                //Most likely NoSuchMethodError here when using Batik 1.6
                //Just skip this section in this case
            }
        }

        if (gvtFonts != null) {
            Iterator i = gvtFonts.iterator();
            while (i.hasNext()) {
                GVTFontFamily fam = (GVTFontFamily) i.next();
                if (fam instanceof SVGFontFamily) {
                    return null; //Let Batik paint this text!
                }
                String fontFamily = fam.getFamilyName();
                if (log.isDebugEnabled()) {
                    log.debug("Matching font family: " + fontFamily);
                }
                if (fontInfo.hasFont(fontFamily, style, weight)) {
                    FontTriplet triplet = fontInfo.fontLookup(fontFamily, style,
                                                       weight);
                    int fsize = (int)(fontSize.floatValue() * 1000);
                    fonts.add(fontInfo.getFontInstance(triplet, fsize));
                }
                if (firstFontFamily == null) {
                    firstFontFamily = fontFamily;
                }
            }
        }
        if (fonts.size() == 0) {
            if (firstFontFamily == null) {
                //This will probably never happen. Just to be on the safe side.
                firstFontFamily = "any";
            }
            //lookup with fallback possibility (incl. substitution notification)
            FontTriplet triplet = fontInfo.fontLookup(firstFontFamily, style, weight);
            int fsize = (int)(fontSize.floatValue() * 1000);
            fonts.add(fontInfo.getFontInstance(triplet, fsize));
        }
        return (Font[])fonts.toArray(new Font[fonts.size()]);
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

    protected final void logTextRun(AttributedCharacterIterator runaci, TextSpanLayout layout) {
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


}
