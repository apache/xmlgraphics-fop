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
import java.awt.font.TextAttribute;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.Iterator;
import java.util.List;

import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.gvt.TextPainter;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.gvt.renderer.StrokingTextPainter;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.Mark;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.xmlgraphics.java2d.ps.PSGraphics2D;


/**
 * Renders the attributed character iterator of a <tt>TextNode</tt>.
 * This class draws the text directly into the PSGraphics2D so that
 * the text is not drawn using shapes which makes the PS files larger.
 * If the text is simple enough to draw then it sets the font and calls
 * drawString. If the text is complex or the cannot be translated
 * into a simple drawString the StrokingTextPainter is used instead.
 *
 * (todo) handle underline, overline and strikethrough
 * (todo) use drawString(AttributedCharacterIterator iterator...) for some
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 * @version $Id$
 */
public class PSTextPainter implements TextPainter {

    /** the logger for this class */
    protected Log log = LogFactory.getLog(PSTextPainter.class);

    private final NativeTextHandler nativeTextHandler;
    private final FontInfo fontInfo;

    /**
     * Use the stroking text painter to get the bounds and shape.
     * Also used as a fallback to draw the string with strokes.
     */
    protected static final TextPainter
        PROXY_PAINTER = StrokingTextPainter.getInstance();

    /**
     * Create a new PS text painter with the given font information.
     * @param nativeTextHandler the NativeTextHandler instance used for text painting
     */
    public PSTextPainter(NativeTextHandler nativeTextHandler) {
        this.nativeTextHandler = nativeTextHandler;
        this.fontInfo = nativeTextHandler.getFontInfo();
    }

    /**
     * Paints the specified attributed character iterator using the
     * specified Graphics2D and context and font context.
     * @param node the TextNode to paint
     * @param g2d the Graphics2D to use
     */
    public void paint(TextNode node, Graphics2D g2d) {
        String txt = node.getText();
        Point2D loc = node.getLocation();

        if (hasUnsupportedAttributes(node)) {
            PROXY_PAINTER.paint(node, g2d);
        } else {
            paintTextRuns(node.getTextRuns(), g2d, loc);
        }
    }


    private boolean hasUnsupportedAttributes(TextNode node) {
        Iterator i = node.getTextRuns().iterator();
        while (i.hasNext()) {
            StrokingTextPainter.TextRun
                    run = (StrokingTextPainter.TextRun)i.next();
            AttributedCharacterIterator aci = run.getACI();
            boolean hasUnsupported = hasUnsupportedAttributes(aci);
            if (hasUnsupported) {
                return true;
            }
        }
        return false;
    }

    private boolean hasUnsupportedAttributes(AttributedCharacterIterator aci) {
        boolean hasunsupported = false;

        String text = getText(aci);
        Font font = makeFont(aci);
        if (hasUnsupportedGlyphs(text, font)) {
            log.trace("-> Unsupported glyphs found");
            hasunsupported = true;
        }

        TextPaintInfo tpi = (TextPaintInfo) aci.getAttribute(
            GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO);
        if ((tpi != null)
                && ((tpi.strokeStroke != null && tpi.strokePaint != null)
                    || (tpi.strikethroughStroke != null)
                    || (tpi.underlineStroke != null)
                    || (tpi.overlineStroke != null))) {
                        log.trace("-> under/overlines etc. found");
            hasunsupported = true;
        }

        //Alpha is not supported
        Paint foreground = (Paint) aci.getAttribute(TextAttribute.FOREGROUND);
        if (foreground instanceof Color) {
            Color col = (Color)foreground;
            if (col.getAlpha() != 255) {
                log.trace("-> transparency found");
                hasunsupported = true;
            }
        }

        Object letSpace = aci.getAttribute(
                            GVTAttributedCharacterIterator.TextAttribute.LETTER_SPACING);
        if (letSpace != null) {
            log.trace("-> letter spacing found");
            hasunsupported = true;
        }

        Object wordSpace = aci.getAttribute(
                             GVTAttributedCharacterIterator.TextAttribute.WORD_SPACING);
        if (wordSpace != null) {
            log.trace("-> word spacing found");
            hasunsupported = true;
        }

        Object lengthAdjust = aci.getAttribute(
                            GVTAttributedCharacterIterator.TextAttribute.LENGTH_ADJUST);
        if (lengthAdjust != null) {
            log.trace("-> length adjustments found");
            hasunsupported = true;
        }

        Object writeMod = aci.getAttribute(
                GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE);
        if (writeMod != null
            && !GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE_LTR.equals(
                  writeMod)) {
            log.trace("-> Unsupported writing modes found");
            hasunsupported = true;
        }

        Object vertOr = aci.getAttribute(
                GVTAttributedCharacterIterator.TextAttribute.VERTICAL_ORIENTATION);
        if (GVTAttributedCharacterIterator.TextAttribute.ORIENTATION_ANGLE.equals(
                  vertOr)) {
            log.trace("-> vertical orientation found");
            hasunsupported = true;
        }

        Object rcDel = aci.getAttribute(
                GVTAttributedCharacterIterator.TextAttribute.TEXT_COMPOUND_DELIMITER);
        //Batik 1.6 returns null here which makes it impossible to determine whether this can
        //be painted or not, i.e. fall back to stroking. :-(
        if (/*rcDel != null &&*/ !(rcDel instanceof SVGOMTextElement)) {
            log.trace("-> spans found");
            hasunsupported = true; //Filter spans
        }

        if (hasunsupported) {
            log.trace("Unsupported attributes found in ACI, using StrokingTextPainter");
        }
        return hasunsupported;
    }

    /**
     * Paint a list of text runs on the Graphics2D at a given location.
     * @param textRuns the list of text runs
     * @param g2d the Graphics2D to paint to
     * @param loc the current location of the "cursor"
     */
    protected void paintTextRuns(List textRuns, Graphics2D g2d, Point2D loc) {
        Point2D currentloc = loc;
        Iterator i = textRuns.iterator();
        while (i.hasNext()) {
            StrokingTextPainter.TextRun
                    run = (StrokingTextPainter.TextRun)i.next();
            currentloc = paintTextRun(run, g2d, currentloc);
        }
    }

    /**
     * Paint a single text run on the Graphics2D at a given location.
     * @param run the text run to paint
     * @param g2d the Graphics2D to paint to
     * @param loc the current location of the "cursor"
     * @return the new location of the "cursor" after painting the text run
     */
    protected Point2D paintTextRun(StrokingTextPainter.TextRun run, Graphics2D g2d, Point2D loc) {
        AttributedCharacterIterator aci = run.getACI();
        return paintACI(aci, g2d, loc);
    }

    /**
     * Extract the raw text from an ACI.
     * @param aci ACI to inspect
     * @return the extracted text
     */
    protected String getText(AttributedCharacterIterator aci) {
        StringBuffer sb = new StringBuffer(aci.getEndIndex() - aci.getBeginIndex());
        for (char c = aci.first(); c != CharacterIterator.DONE; c = aci.next()) {
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Paint an ACI on a Graphics2D at a given location. The method has to
     * update the location after painting.
     * @param aci ACI to paint
     * @param g2d Graphics2D to paint on
     * @param loc start location
     * @return new current location
     */
    protected Point2D paintACI(AttributedCharacterIterator aci, Graphics2D g2d, Point2D loc) {
        //ACIUtils.dumpAttrs(aci);

        aci.first();

        updateLocationFromACI(aci, loc);

        TextPaintInfo tpi = (TextPaintInfo) aci.getAttribute(
            GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO);

        if (tpi == null) {
            return loc;
        }

        TextNode.Anchor anchor = (TextNode.Anchor)aci.getAttribute(
                GVTAttributedCharacterIterator.TextAttribute.ANCHOR_TYPE);

        //Set up font
        List gvtFonts = (List)aci.getAttribute(
                GVTAttributedCharacterIterator.TextAttribute.GVT_FONT_FAMILIES);
        Paint foreground = tpi.fillPaint;
        Paint strokePaint = tpi.strokePaint;
        Stroke stroke = tpi.strokeStroke;

        Float fontSize = (Float)aci.getAttribute(TextAttribute.SIZE);
        if (fontSize == null) {
            return loc;
        }
        Float posture = (Float)aci.getAttribute(TextAttribute.POSTURE);
        Float taWeight = (Float)aci.getAttribute(TextAttribute.WEIGHT);

        if (foreground instanceof Color) {
            Color col = (Color)foreground;
            g2d.setColor(col);
        }
        g2d.setPaint(foreground);
        g2d.setStroke(stroke);

        Font font = makeFont(aci);
        java.awt.Font awtFont = makeAWTFont(aci, font);

        g2d.setFont(awtFont);

        String txt = getText(aci);
        float advance = getStringWidth(txt, font);
        float tx = 0;
        if (anchor != null) {
            switch (anchor.getType()) {
                case TextNode.Anchor.ANCHOR_MIDDLE:
                    tx = -advance / 2;
                    break;
                case TextNode.Anchor.ANCHOR_END:
                    tx = -advance;
                    break;
                default: //nop
            }
        }

        drawPrimitiveString(g2d, loc, font, txt, tx);
        loc.setLocation(loc.getX() + advance, loc.getY());
        return loc;
    }

    protected void drawPrimitiveString(Graphics2D g2d, Point2D loc, Font font, String txt, float tx) {
        //Finally draw text
        nativeTextHandler.setOverrideFont(font);
        try {
            try {
                nativeTextHandler.drawString(txt, (float)(loc.getX() + tx), (float)(loc.getY()));
            } catch (IOException ioe) {
                if (g2d instanceof PSGraphics2D) {
                    ((PSGraphics2D)g2d).handleIOException(ioe);
                }
            }
        } finally {
            nativeTextHandler.setOverrideFont(null);
        }
    }

    private void updateLocationFromACI(
                AttributedCharacterIterator aci,
                Point2D loc) {
        //Adjust position of span
        Float xpos = (Float)aci.getAttribute(
                GVTAttributedCharacterIterator.TextAttribute.X);
        Float ypos = (Float)aci.getAttribute(
                GVTAttributedCharacterIterator.TextAttribute.Y);
        Float dxpos = (Float)aci.getAttribute(
                GVTAttributedCharacterIterator.TextAttribute.DX);
        Float dypos = (Float)aci.getAttribute(
                GVTAttributedCharacterIterator.TextAttribute.DY);
        if (xpos != null) {
            loc.setLocation(xpos.doubleValue(), loc.getY());
        }
        if (ypos != null) {
            loc.setLocation(loc.getX(), ypos.doubleValue());
        }
        if (dxpos != null) {
            loc.setLocation(loc.getX() + dxpos.doubleValue(), loc.getY());
        }
        if (dypos != null) {
            loc.setLocation(loc.getX(), loc.getY() + dypos.doubleValue());
        }
    }

    private String getStyle(AttributedCharacterIterator aci) {
        Float posture = (Float)aci.getAttribute(TextAttribute.POSTURE);
        return ((posture != null) && (posture.floatValue() > 0.0))
                       ? "italic"
                       : "normal";
    }

    private int getWeight(AttributedCharacterIterator aci) {
        Float taWeight = (Float)aci.getAttribute(TextAttribute.WEIGHT);
        return ((taWeight != null) &&  (taWeight.floatValue() > 1.0))
                       ? Font.WEIGHT_BOLD
                       : Font.WEIGHT_NORMAL;
    }

    private Font makeFont(AttributedCharacterIterator aci) {
        Float fontSize = (Float)aci.getAttribute(TextAttribute.SIZE);
        if (fontSize == null) {
            fontSize = new Float(10.0f);
        }
        String style = getStyle(aci);
        int weight = getWeight(aci);

        String fontFamily = null;
        List gvtFonts = (List) aci.getAttribute(
                      GVTAttributedCharacterIterator.TextAttribute.GVT_FONT_FAMILIES);
        if (gvtFonts != null) {
            Iterator i = gvtFonts.iterator();
            while (i.hasNext()) {
                GVTFontFamily fam = (GVTFontFamily) i.next();
                /* (todo) Enable SVG Font painting
                if (fam instanceof SVGFontFamily) {
                    PROXY_PAINTER.paint(node, g2d);
                    return;
                }*/
                fontFamily = fam.getFamilyName();
                if (fontInfo.hasFont(fontFamily, style, weight)) {
                    FontTriplet triplet = fontInfo.fontLookup(
                            fontFamily, style, weight);
                    int fsize = (int)(fontSize.floatValue() * 1000);
                    return fontInfo.getFontInstance(triplet, fsize);
                }
            }
        }
        FontTriplet triplet = fontInfo.fontLookup("any", style, Font.WEIGHT_NORMAL);
        int fsize = (int)(fontSize.floatValue() * 1000);
        return fontInfo.getFontInstance(triplet, fsize);
    }

    private java.awt.Font makeAWTFont(AttributedCharacterIterator aci, Font font) {
        final String style = getStyle(aci);
        final int weight = getWeight(aci);
        int fStyle = java.awt.Font.PLAIN;
        if (weight == Font.WEIGHT_BOLD) {
            fStyle |= java.awt.Font.BOLD;
        }
        if ("italic".equals(style)) {
            fStyle |= java.awt.Font.ITALIC;
        }
        return new java.awt.Font(font.getFontName(), fStyle,
                             (font.getFontSize() / 1000));
    }

    private float getStringWidth(String str, Font font) {
        float wordWidth = 0;
        float whitespaceWidth = font.getWidth(font.mapChar(' '));

        for (int i = 0; i < str.length(); i++) {
            float charWidth;
            char c = str.charAt(i);
            if (!((c == ' ') || (c == '\n') || (c == '\r') || (c == '\t'))) {
                charWidth = font.getWidth(font.mapChar(c));
                if (charWidth <= 0) {
                    charWidth = whitespaceWidth;
                }
            } else {
                charWidth = whitespaceWidth;
            }
            wordWidth += charWidth;
        }
        return wordWidth / 1000f;
    }

    private boolean hasUnsupportedGlyphs(String str, Font font) {
        for (int i = 0; i < str.length(); i++) {
            float charWidth;
            char c = str.charAt(i);
            if (!((c == ' ') || (c == '\n') || (c == '\r') || (c == '\t'))) {
                if (!font.hasChar(c)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the outline shape of the text characters.
     * This uses the StrokingTextPainter to get the outline
     * shape since in theory it should be the same.
     *
     * @param node the text node
     * @return the outline shape of the text characters
     */
    public Shape getOutline(TextNode node) {
        return PROXY_PAINTER.getOutline(node);
    }

    /**
     * Get the bounds.
     * This uses the StrokingTextPainter to get the bounds
     * since in theory it should be the same.
     *
     * @param node the text node
     * @return the bounds of the text
     */
    public Rectangle2D getBounds2D(TextNode node) {
        /* (todo) getBounds2D() is too slow
         * because it uses the StrokingTextPainter. We should implement this
         * method ourselves. */
        return PROXY_PAINTER.getBounds2D(node);
    }

    /**
     * Get the geometry bounds.
     * This uses the StrokingTextPainter to get the bounds
     * since in theory it should be the same.
     * @param node the text node
     * @return the bounds of the text
     */
    public Rectangle2D getGeometryBounds(TextNode node) {
        return PROXY_PAINTER.getGeometryBounds(node);
    }

    // Methods that have no purpose for PS

    /**
     * Get the mark.
     * This does nothing since the output is pdf and not interactive.
     * @param node the text node
     * @param pos the position
     * @param all select all
     * @return null
     */
    public Mark getMark(TextNode node, int pos, boolean all) {
        return null;
    }

    /**
     * Select at.
     * This does nothing since the output is pdf and not interactive.
     * @param x the x position
     * @param y the y position
     * @param node the text node
     * @return null
     */
    public Mark selectAt(double x, double y, TextNode node) {
        return null;
    }

    /**
     * Select to.
     * This does nothing since the output is pdf and not interactive.
     * @param x the x position
     * @param y the y position
     * @param beginMark the start mark
     * @return null
     */
    public Mark selectTo(double x, double y, Mark beginMark) {
        return null;
    }

    /**
     * Selec first.
     * This does nothing since the output is pdf and not interactive.
     * @param node the text node
     * @return null
     */
    public Mark selectFirst(TextNode node) {
        return null;
    }

    /**
     * Select last.
     * This does nothing since the output is pdf and not interactive.
     * @param node the text node
     * @return null
     */
    public Mark selectLast(TextNode node) {
        return null;
    }

    /**
     * Get selected.
     * This does nothing since the output is pdf and not interactive.
     * @param start the start mark
     * @param finish the finish mark
     * @return null
     */
    public int[] getSelected(Mark start, Mark finish) {
        return null;
    }

    /**
     * Get the highlighted shape.
     * This does nothing since the output is pdf and not interactive.
     * @param beginMark the start mark
     * @param endMark the end mark
     * @return null
     */
    public Shape getHighlightShape(Mark beginMark, Mark endMark) {
        return null;
    }

}


