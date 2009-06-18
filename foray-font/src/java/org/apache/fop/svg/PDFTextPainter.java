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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
/* java.awt.Font is not imported to avoid confusion with
   org.axsl.font.Font */
import java.text.AttributedCharacterIterator;
import java.awt.font.TextAttribute;
import java.awt.Shape;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.Color;
import java.util.List;
import java.util.Iterator;

import org.apache.batik.gvt.text.Mark;
import org.apache.batik.gvt.TextPainter;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.bridge.SVGFontFamily;
import org.apache.batik.gvt.renderer.StrokingTextPainter;

import org.axsl.font.Font;
import org.axsl.font.FontConsumer;
import org.axsl.font.FontException;
import org.axsl.font.FontServer;
import org.axsl.font.FontUse;
import org.apache.fop.fonts.FontTriplet;

/**
 * Renders the attributed character iterator of a <tt>TextNode</tt>.
 * This class draws the text directly into the PDFGraphics2D so that
 * the text is not drawn using shapes which makes the PDF files larger.
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
public class PDFTextPainter implements TextPainter {
    private FontConsumer fontConsumer;

    /**
     * Use the stroking text painter to get the bounds and shape.
     * Also used as a fallback to draw the string with strokes.
     */
    protected static final TextPainter PROXY_PAINTER =
        StrokingTextPainter.getInstance();

    /**
     * Create a new PDF text painter with the given font information.
     * @param fi the fint info
     */
    public PDFTextPainter(FontConsumer fc) {
        fontConsumer = fc;
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
        
        AttributedCharacterIterator aci =
          node.getAttributedCharacterIterator();
        // reset position to start of char iterator
        if (aci.getBeginIndex() == aci.getEndIndex()) {
            return;
        }
        char ch = aci.first();
        if (ch == AttributedCharacterIterator.DONE) {
            return;
        }
        TextNode.Anchor anchor;
        anchor = (TextNode.Anchor) aci.getAttribute(
                      GVTAttributedCharacterIterator.TextAttribute.ANCHOR_TYPE);

        List gvtFonts;
        gvtFonts = (List) aci.getAttribute(
            GVTAttributedCharacterIterator.TextAttribute.GVT_FONT_FAMILIES);

        TextPaintInfo tpi = (TextPaintInfo) aci.getAttribute(
            GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO);
        
        if (tpi == null) {
            return;
        }        
        
        Paint forg = tpi.fillPaint;
        Paint strokePaint = tpi.strokePaint;
        Float fsize = (Float) aci.getAttribute(TextAttribute.SIZE);
        if (fsize == null) {
            return;
        }
        int size = (int) (fsize.floatValue() * 1000f);
        
        Stroke stroke = tpi.strokeStroke;
        /*
        Float xpos = (Float) aci.getAttribute(
                       GVTAttributedCharacterIterator.TextAttribute.X);
        Float ypos = (Float) aci.getAttribute(
                       GVTAttributedCharacterIterator.TextAttribute.Y);
        */

        Float posture = (Float) aci.getAttribute(TextAttribute.POSTURE);
        Float taWeight = (Float) aci.getAttribute(TextAttribute.WEIGHT);

        boolean useStrokePainter = false;

        if (forg instanceof Color) {
            Color col = (Color) forg;
            if (col.getAlpha() != 255) {
                useStrokePainter = true;
            }
            g2d.setColor(col);
        }
        g2d.setPaint(forg);
        g2d.setStroke(stroke);

        if (strokePaint != null) {
            // need to draw using AttributedCharacterIterator
            useStrokePainter = true;
        }

        if (hasUnsupportedAttributes(aci)) {
            useStrokePainter = true;
        }

        // text contains unsupported information
        if (useStrokePainter) {
            PROXY_PAINTER.paint(node, g2d);
            return;
        }

        byte style = ((posture != null) && (posture.floatValue() > 0.0))
                       ? Font.FONT_STYLE_ITALIC : Font.FONT_STYLE_NORMAL;
        short weight = ((taWeight != null)
                       &&  (taWeight.floatValue() > 1.0)) ? Font.FONT_WEIGHT_BOLD
                       : Font.FONT_WEIGHT_NORMAL;

        FontUse fontUse = null;
        FontServer fs = fontConsumer.getFontServer();
        boolean found = true;
        String fontFamily = null;
        if (gvtFonts != null) {
            Iterator i = gvtFonts.iterator();
            String[] fontFamilies = new String[gvtFonts.size()];
            int index = 0;
            while (i.hasNext()) {
                GVTFontFamily fam = (GVTFontFamily) i.next();
                if (fam instanceof SVGFontFamily) {
                    PROXY_PAINTER.paint(node, g2d);
                    return;
                }
                fontFamily = fam.getFamilyName();
                if (fi.hasFont(fontFamily, style, weight)) {
                    FontTriplet triplet = fontInfo.fontLookup(fontFamily, style,
                                                       weight);
                    int fsize = (int)(size.floatValue() * 1000);
                    fontState = fontInfo.getFontInstance(triplet, fsize);
                    found = true;
                    break;
                }
            }
            try {
                fontUse = fs.selectFontXSL(fontConsumer, fontFamilies, style, weight,
                        Font.FONT_VARIANT_NORMAL, Font.FONT_STRETCH_NORMAL, size, ch);
                fontFamily = fontUse.postscriptName();
            } catch (FontException f) {
                found = false;
            }
        }
        if (!found) {
            try {
                fontUse = fs.selectFontXSL(fontConsumer, new String[] {"any"},
                        Font.FONT_STYLE_ANY,
                        Font.FONT_WEIGHT_ANY,
                        Font.FONT_VARIANT_ANY,
                        Font.FONT_STRETCH_ANY,
                        size, ch);
            } catch (FontException e) { /* Should never happen */ }
        } else {
            if (g2d instanceof PDFGraphics2D) {
                ((PDFGraphics2D) g2d).setOverrideFontUse(fontUse);
                ((PDFGraphics2D) g2d).setOverrideFontSize(size);
            }
        }
        int fStyle = java.awt.Font.PLAIN;
        if (weight == Font.FONT_WEIGHT_BOLD) {
            if (style == Font.FONT_STYLE_ITALIC) {
                fStyle = java.awt.Font.BOLD | java.awt.Font.ITALIC;
            } else {
                fStyle = java.awt.Font.BOLD;
            }
        } else {
            if (style == Font.FONT_STYLE_ITALIC) {
                fStyle = java.awt.Font.ITALIC;
            } else {
                fStyle = java.awt.Font.PLAIN;
            }
        }
        java.awt.Font awtFont = new java.awt.Font(fontFamily, fStyle, size / 1000);

        g2d.setFont(awtFont);

        float advance = fontUse.getFont().width(txt, size, 0, 0) / 1000f;
        float tx = 0;
        if (anchor != null) {
            switch (anchor.getType()) {
                case TextNode.Anchor.ANCHOR_MIDDLE:
                    tx = -advance / 2000f; // convert back into points 
                    break;
                case TextNode.Anchor.ANCHOR_END:
                    tx = -advance / 1000f;
            }
        }
        g2d.drawString(txt, (float)(loc.getX() + tx), (float)(loc.getY()));
    }

    private boolean hasUnsupportedAttributes(AttributedCharacterIterator aci) {
        boolean hasunsupported = false;
        Object letSpace = aci.getAttribute(
                            GVTAttributedCharacterIterator.TextAttribute.LETTER_SPACING);
        if (letSpace != null) {
            hasunsupported = true;
        }

        Object wordSpace = aci.getAttribute(
                             GVTAttributedCharacterIterator.TextAttribute.WORD_SPACING);
        if (wordSpace != null) {
            hasunsupported = true;
        }

        AttributedCharacterIterator.Attribute key;
        key = GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE;
        Object writeMod = aci.getAttribute(key);
        if (!GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE_LTR.equals(
                  writeMod)) {
            hasunsupported = true;
        }

        Object vertOr = aci.getAttribute(
                          GVTAttributedCharacterIterator.TextAttribute.VERTICAL_ORIENTATION);
        if (GVTAttributedCharacterIterator.TextAttribute.ORIENTATION_ANGLE.equals(
                  vertOr)) {
            hasunsupported = true;
        }
        return hasunsupported;
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

    // Methods that have no purpose for PDF

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
