/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Font;

import java.text.AttributedCharacterIterator;
import java.awt.font.TextAttribute;
import java.awt.Shape;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.Color;
import java.util.Vector;
import java.util.Enumeration;

import org.apache.batik.gvt.text.Mark;
import org.apache.batik.gvt.TextPainter;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.bridge.SVGFontFamily;
import org.apache.batik.gvt.renderer.StrokingTextPainter;

import org.apache.fop.layout.FontState;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.layout.FontMetric;

/**
 * Renders the attributed character iterator of a <tt>TextNode</tt>.
 * This class draws the text directly into the PDFGraphics2D so that
 * the text is not drawn using shapes which makes the PDF files larger.
 * If the text is simple enough to draw then it sets the font and calls
 * drawString. If the text is complex or the cannot be translated
 * into a simple drawString the StrokingTextPainter is used instead.
 *
 * @todo handle underline, overline and strikethrough
 * @todo use drawString(AttributedCharacterIterator iterator...) for some
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 * @version $Id$
 */
public class PDFTextPainter implements TextPainter {
    private FontInfo fontInfo;

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
    public PDFTextPainter(FontInfo fi) {
        fontInfo = fi;
    }

    /**
     * Paints the specified attributed character iterator using the
     * specified Graphics2D and context and font context.
     * @param node the TextNode to paint
     * @param g2d the Graphics2D to use
     */
    public void paint(TextNode node, Graphics2D g2d) {
        // System.out.println("PDFText paint");
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

        Vector gvtFonts;
        gvtFonts = (Vector) aci.getAttribute(
                      GVTAttributedCharacterIterator.TextAttribute.GVT_FONT_FAMILIES);
        Paint forg = (Paint) aci.getAttribute(TextAttribute.FOREGROUND);
        Paint strokePaint;
        strokePaint = (Paint) aci.getAttribute(
                     GVTAttributedCharacterIterator.TextAttribute.STROKE_PAINT);
        Float size = (Float) aci.getAttribute(TextAttribute.SIZE);
        if (size == null) {
            return;
        }
        Stroke stroke = (Stroke) aci.getAttribute(
                          GVTAttributedCharacterIterator.TextAttribute.STROKE);
        Float xpos = (Float) aci.getAttribute(
                       GVTAttributedCharacterIterator.TextAttribute.X);
        Float ypos = (Float) aci.getAttribute(
                       GVTAttributedCharacterIterator.TextAttribute.Y);

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

        String style = ((posture != null) && (posture.floatValue() > 0.0))
                       ? "italic" : "normal";
        int weight = ((taWeight != null)
                       &&  (taWeight.floatValue() > 1.0)) ? FontInfo.BOLD
                       : FontInfo.NORMAL;

        FontState fontState = null;
        FontInfo fi = fontInfo;
        boolean found = false;
        String fontFamily = null;
        if (gvtFonts != null) {
            for (Enumeration e = gvtFonts.elements();
                    e.hasMoreElements();) {
                GVTFontFamily fam = (GVTFontFamily) e.nextElement();
                if (fam instanceof SVGFontFamily) {
                    PROXY_PAINTER.paint(node, g2d);
                    return;
                }
                fontFamily = fam.getFamilyName();
                if (fi.hasFont(fontFamily, style, weight)) {
                    String fname = fontInfo.fontLookup(fontFamily, style,
                                                       weight);
                    FontMetric metrics = fontInfo.getMetricsFor(fname);
                    int fsize = (int)(size.floatValue() * 1000);
                    fontState = new FontState(fname, metrics, fsize);
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            String fname =
              fontInfo.fontLookup("any", style, FontInfo.NORMAL);
            FontMetric metrics = fontInfo.getMetricsFor(fname);
            int fsize = (int)(size.floatValue() * 1000);
            fontState = new FontState(fname, metrics, fsize);
        } else {
            if (g2d instanceof PDFGraphics2D) {
                ((PDFGraphics2D) g2d).setOverrideFontState(fontState);
            }
        }
        int fStyle = Font.PLAIN;
        if (weight == FontInfo.BOLD) {
            if (style.equals("italic")) {
                fStyle = Font.BOLD | Font.ITALIC;
            } else {
                fStyle = Font.BOLD;
            }
        } else {
            if (style.equals("italic")) {
                fStyle = Font.ITALIC;
            } else {
                fStyle = Font.PLAIN;
            }
        }
        Font font = new Font(fontFamily, fStyle,
                             (int)(fontState.getFontSize() / 1000));

        g2d.setFont(font);

        float advance = getStringWidth(txt, fontState);
        float tx = 0;
        if (anchor != null) {
            switch (anchor.getType()) {
                case TextNode.Anchor.ANCHOR_MIDDLE:
                    tx = -advance / 2;
                    break;
                case TextNode.Anchor.ANCHOR_END:
                    tx = -advance;
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

    private float getStringWidth(String str, FontState fontState) {
        float wordWidth = 0;
        float whitespaceWidth = fontState.width(fontState.mapChar(' '));

        for (int i = 0; i < str.length(); i++) {
            float charWidth;
            char c = str.charAt(i);
            if (!((c == ' ') || (c == '\n') || (c == '\r') || (c == '\t'))) {
                charWidth = fontState.width(fontState.mapChar(c));
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
        System.out.println("PDFText getMark");
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
        System.out.println("PDFText selectAt");
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
        System.out.println("PDFText selectTo");
        return null;
    }

    /**
     * Selec first.
     * This does nothing since the output is pdf and not interactive.
     * @param node the text node
     * @return null
     */
    public Mark selectFirst(TextNode node) {
        System.out.println("PDFText selectFirst");
        return null;
    }

    /**
     * Select last.
     * This does nothing since the output is pdf and not interactive.
     * @param node the text node
     * @return null
     */
    public Mark selectLast(TextNode node) {
        System.out.println("PDFText selectLast");
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
        System.out.println("PDFText getSelected");
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
        System.out.println("PDFText getHighlightShape");
        return null;
    }

}

