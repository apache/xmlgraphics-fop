/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.fop.svg;

import java.awt.Graphics2D;
import java.awt.*;
import java.text.AttributedCharacterIterator;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Font;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.awt.font.TextLayout;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.*;
import java.util.Set;

import org.apache.batik.gvt.text.Mark;
import org.apache.batik.gvt.*;
import org.apache.batik.gvt.text.*;
import org.apache.batik.gvt.renderer.*;
import org.apache.batik.gvt.font.*;

import org.apache.fop.layout.*;

/**
 * Renders the attributed character iterator of a <tt>TextNode</tt>.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 * @version $Id$
 */
public class PDFTextPainter implements TextPainter {
    FontState fontState;

    public PDFTextPainter(FontState fs) {
        fontState = fs;
    }

    /**
     * Paints the specified attributed character iterator using the
     * specified Graphics2D and context and font context.
     * @param node the TextNode to paint
     * @param g2d the Graphics2D to use
     * @param context the rendering context.
     */
    public void paint(TextNode node, Graphics2D g2d,
                      GraphicsNodeRenderContext context) {
        //System.out.println("PDFText paint");
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
        TextNode.Anchor anchor = (TextNode.Anchor) aci.getAttribute(
                                   GVTAttributedCharacterIterator.TextAttribute.ANCHOR_TYPE);

        Vector gvtFonts = (Vector) aci.getAttribute(
                            GVTAttributedCharacterIterator.TextAttribute.GVT_FONT_FAMILIES);
        Paint forg = (Paint) aci.getAttribute(TextAttribute.FOREGROUND);
        Float size = (Float) aci.getAttribute(TextAttribute.SIZE);
        Stroke stroke = (Stroke) aci.getAttribute(
                          GVTAttributedCharacterIterator.TextAttribute.STROKE);
        Float xpos = (Float) aci.getAttribute(
                       GVTAttributedCharacterIterator.TextAttribute.X);
        Float ypos = (Float) aci.getAttribute(
                       GVTAttributedCharacterIterator.TextAttribute.Y);

        Float posture = (Float) aci.getAttribute(TextAttribute.POSTURE);
        Float taWeight = (Float) aci.getAttribute(TextAttribute.WEIGHT);

        if (forg instanceof Color) {
            g2d.setColor((Color) forg);
        }
        g2d.setPaint(forg);
        g2d.setStroke(stroke);

        String style = ((posture != null) && (posture.floatValue() > 0.0)) ?
                       "italic" : "normal";
        String weight = ((taWeight != null) &&
                         (taWeight.floatValue() > 1.0)) ? "bold" : "normal";

        FontInfo fi = fontState.getFontInfo();
        boolean found = false;
        if (gvtFonts != null) {
            for (Enumeration e = gvtFonts.elements();
                    e.hasMoreElements();) {
                GVTFontFamily fam = (GVTFontFamily) e.nextElement();
                String name = fam.getFamilyName();
                if (fi.hasFont(name, weight, style)) {
                    try {
                        int fsize = (int) size.floatValue();
                        fontState = new FontState(fontState.getFontInfo(),
                                                  name, style, weight, fsize * 1000, 0);
                    } catch (org.apache.fop.apps.FOPException fope) {
                        fope.printStackTrace();
                    }
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            try {
                int fsize = (int) size.floatValue();
                fontState = new FontState(fontState.getFontInfo(), "any",
                                          style, weight, fsize * 1000, 0);
            } catch (org.apache.fop.apps.FOPException fope) {
                fope.printStackTrace();
            }
        }
        int fStyle = Font.PLAIN;
        if (fontState.getFontWeight().equals("bold")) {
            if (fontState.getFontStyle().equals("italic")) {
                fStyle = Font.BOLD | Font.ITALIC;
            } else {
                fStyle = Font.BOLD;
            }
        } else {
            if (fontState.getFontStyle().equals("italic")) {
                fStyle = Font.ITALIC;
            } else {
                fStyle = Font.PLAIN;
            }
        }
        Font font = new Font(fontState.getFontFamily(), fStyle,
                             (int)(fontState.getFontSize() / 1000));

        g2d.setFont(font);


        float advance = getStringWidth(txt);
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

    public float getStringWidth(String str) {
        float wordWidth = 0;
        float whitespaceWidth = fontState.width(fontState.mapChar(' '));

        for (int i = 0; i < str.length(); i++) {
            float charWidth;
            char c = str.charAt(i);
            if (!((c == ' ') || (c == '\n') || (c == '\r') || (c == '\t'))) {
                charWidth = fontState.width(fontState.mapChar(c));
                if (charWidth <= 0)
                    charWidth = whitespaceWidth;
            } else {
                charWidth = whitespaceWidth;
            }
            wordWidth += charWidth;
        }
        return wordWidth / 1000f;
    }

    /**
     * Initiates a text selection on a particular AttributedCharacterIterator,
     * using the text/font metrics employed by this TextPainter instance.
     * @param x the x coordinate, in the text layout's coordinate system,
     *       of the selection event.
     * @param y the y coordinate, in the text layout's coordinate system,
     *       of the selection event.
     * @param aci the AttributedCharacterIterator describing the text
     * @param context the GraphicsNodeRenderContext to use when doing text layout.
     * @return an instance of Mark which encapsulates the state necessary to
     * implement hit testing and text selection.
     */
    public Mark selectAt(double x, double y,
                         AttributedCharacterIterator aci, TextNode node,
                         GraphicsNodeRenderContext context) {
        System.out.println("PDFText selectAt");
        return null;
    }

    /**
     * Continues a text selection on a particular AttributedCharacterIterator,
     * using the text/font metrics employed by this TextPainter instance.
     * @param x the x coordinate, in the text layout's coordinate system,
     *       of the selection event.
     * @param y the y coordinate, in the text layout's coordinate system,
     *       of the selection event.
     * @param aci the AttributedCharacterIterator describing the text
     * @param context the GraphicsNodeRenderContext to use when doing text layout.
     * @return an instance of Mark which encapsulates the state necessary to
     * implement hit testing and text selection.
     */
    public Mark selectTo(double x, double y, Mark beginMark,
                         AttributedCharacterIterator aci, TextNode node,
                         GraphicsNodeRenderContext context) {
        System.out.println("PDFText selectTo");
        return null;
    }

    /**
     * Select all of the text represented by an AttributedCharacterIterator,
     * using the text/font metrics employed by this TextPainter instance.
     * @param x the x coordinate, in the text layout's coordinate system,
     *       of the selection event.
     * @param y the y coordinate, in the text layout's coordinate system,
     *       of the selection event.
     * @param aci the AttributedCharacterIterator describing the text
     * @param context the GraphicsNodeRenderContext to use when doing text layout.
     * @return an instance of Mark which encapsulates the state necessary to
     * implement hit testing and text selection.
     */
    public Mark selectAll(double x, double y,
                          AttributedCharacterIterator aci, TextNode node,
                          GraphicsNodeRenderContext context) {
        System.out.println("PDFText selectAll");
        return null;
    }


    /**
     * Selects the first glyph in the text node.
     */
    public Mark selectFirst(double x, double y,
                            AttributedCharacterIterator aci, TextNode node,
                            GraphicsNodeRenderContext context) {
        System.out.println("PDFText selectFirst");
        return null;
    }


    /**
     * Selects the last glyph in the text node.
     */
    public Mark selectLast(double x, double y,
                           AttributedCharacterIterator aci, TextNode node,
                           GraphicsNodeRenderContext context) {
        System.out.println("PDFText selectLast");
        return null;
    }

    /*
     * Get an array of index pairs corresponding to the indices within an
     * AttributedCharacterIterator regions bounded by two Marks.
     * Note that the instances of Mark passed to this function
     * <em>must come</em>
     * from the same TextPainter that generated them via selectAt() and
     * selectTo(), since the TextPainter implementation may rely on hidden
     * implementation details of its own Mark implementation.
     */
    public int[] getSelected(AttributedCharacterIterator aci,
                             Mark start, Mark finish) {
        System.out.println("PDFText getSelected");
        return null;
    }


    /*
     * Get a Shape in userspace coords which encloses the textnode
     * glyphs bounded by two Marks.
     * Note that the instances of Mark passed to this function
     * <em>must come</em>
     * from the same TextPainter that generated them via selectAt() and
     * selectTo(), since the TextPainter implementation may rely on hidden
     * implementation details of its own Mark implementation.
     */
    public Shape getHighlightShape(Mark beginMark, Mark endMark) {
        System.out.println("PDFText getHighlightShape");
        return null;
    }

    /*
     * Get a Shape in userspace coords which defines the textnode glyph outlines.
     * @param node the TextNode to measure
     * @param frc the font rendering context.
     * @param includeDecoration whether to include text decoration
     *            outlines.
     * @param includeStroke whether to create the "stroke shape outlines"
     *            instead of glyph outlines.
     */
    public Shape getShape(TextNode node, FontRenderContext frc) {
        System.out.println("PDFText getShape");
        return null;
    }

    /*
     * Get a Shape in userspace coords which defines the textnode glyph outlines.
     * @param node the TextNode to measure
     * @param frc the font rendering context.
     * @param includeDecoration whether to include text decoration
     *            outlines.
     * @param includeStroke whether to create the "stroke shape outlines"
     *            instead of glyph outlines.
     */
    public Shape getDecoratedShape(TextNode node, FontRenderContext frc) {
        System.out.println("PDFText getDecoratedShape");
        return new Rectangle(1, 1);
    }

    /*
     * Get a Rectangle2D in userspace coords which encloses the textnode
     * glyphs composed from an AttributedCharacterIterator.
     * @param node the TextNode to measure
     * @param g2d the Graphics2D to use
     * @param context rendering context.
     */
    public Rectangle2D getBounds(TextNode node, FontRenderContext frc) {
        System.out.println("PDFText getBounds");
        return null;
    }

    /*
     * Get a Rectangle2D in userspace coords which encloses the textnode
     * glyphs composed from an AttributedCharacterIterator, inclusive of
     * glyph decoration (underline, overline, strikethrough).
     * @param node the TextNode to measure
     * @param g2d the Graphics2D to use
     * @param context rendering context.
     */
    public Rectangle2D getDecoratedBounds(TextNode node,
                                          FontRenderContext frc) {
        System.out.println("PDFText getDecoratedBounds");
        return null;
    }

    /*
     * Get a Rectangle2D in userspace coords which encloses the
     * textnode glyphs (as-painted, inclusive of decoration and stroke, but
     * exclusive of filters, etc.) composed from an AttributedCharacterIterator.
     * @param node the TextNode to measure
     * @param g2d the Graphics2D to use
     * @param context rendering context.
     */
    public Rectangle2D getPaintedBounds(TextNode node,
                                        FontRenderContext frc) {
        //System.out.println("PDFText getPaintedBounds");
        return null;
    }
}

