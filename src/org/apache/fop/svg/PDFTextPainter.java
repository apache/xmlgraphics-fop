/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import java.awt.Graphics2D;
import java.awt.*;
import java.text.AttributedCharacterIterator;
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
    FontInfo fontInfo;
    protected final static TextPainter proxyPainter
                                 = StrokingTextPainter.getInstance();

    public PDFTextPainter(FontInfo fi) {
        fontInfo = fi;
    }

    /**
     * Paints the specified attributed character iterator using the
     * specified Graphics2D and context and font context.
     * @param node the TextNode to paint
     * @param g2d the Graphics2D to use
     * @param context the rendering context.
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
        TextNode.Anchor anchor =
            (TextNode.Anchor)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.ANCHOR_TYPE);

        Vector gvtFonts =
            (Vector)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.GVT_FONT_FAMILIES);
        Paint forg = (Paint)aci.getAttribute(TextAttribute.FOREGROUND);
        Paint strokePaint = (Paint)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.STROKE_PAINT);
        Float size = (Float)aci.getAttribute(TextAttribute.SIZE);
        if(size == null) {
            return;
        }
        Stroke stroke =
            (Stroke)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.STROKE);
        Float xpos =
            (Float)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.X);
        Float ypos =
            (Float)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.Y);

        Float posture = (Float)aci.getAttribute(TextAttribute.POSTURE);
        Float taWeight = (Float)aci.getAttribute(TextAttribute.WEIGHT);

        boolean useStrokePainter = false;

        if (forg instanceof Color) {
            Color col = (Color)forg;
            if(col.getAlpha() != 255) {
                useStrokePainter = true;
            }
            g2d.setColor(col);
        }
        g2d.setPaint(forg);
        g2d.setStroke(stroke);

        if(strokePaint != null) {
            // need to draw using AttributedCharacterIterator
            useStrokePainter = true;
        }

        if(useStrokePainter) {
            proxyPainter.paint(node, g2d);
            return;
        }

        String style = ((posture != null) && (posture.floatValue() > 0.0))
                       ? "italic" : "normal";
        int weight = ((taWeight != null) && (taWeight.floatValue() > 1.0))
                        ? FontInfo.BOLD : FontInfo.NORMAL;

        FontState fontState = null;
        FontInfo fi = fontInfo;
        boolean found = false;
        String fontFamily = null;
        if (gvtFonts != null) {
            for (Enumeration e = gvtFonts.elements(); e.hasMoreElements(); ) {
                GVTFontFamily fam = (GVTFontFamily)e.nextElement();
                fontFamily = fam.getFamilyName();
                if (fi.hasFont(fontFamily, style, weight)) {
                    String fname = fontInfo.fontLookup(fontFamily, style, weight);
                    FontMetric metrics = fontInfo.getMetricsFor(fname);
                    int fsize = (int)(size.floatValue() * 1000);
                    fontState = new FontState(fname, metrics, fsize);
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            String fname = fontInfo.fontLookup("any", style, FontInfo.NORMAL);
            FontMetric metrics = fontInfo.getMetricsFor(fname);
            int fsize = (int)(size.floatValue() * 1000);
            fontState = new FontState(fname, metrics, fsize);
        } else {
            if(g2d instanceof PDFGraphics2D) {
                ((PDFGraphics2D)g2d).setOverrideFontState(fontState);
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

    public float getStringWidth(String str, FontState fontState) {
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

    public Mark getMark(TextNode node, int pos, boolean all) {
        System.out.println("PDFText getMark");
        return null;
    }

    public Mark selectAt(double x, double y,
                         TextNode node) {
        System.out.println("PDFText selectAt");
        return null;
    }

    public Mark selectTo(double x, double y, Mark beginMark) {
        System.out.println("PDFText selectTo");
        return null;
    }

    public Mark selectAll(double x, double y,
                          TextNode node) {
        System.out.println("PDFText selectAll");
        return null;
    }

    public Mark selectFirst(TextNode node) {
        System.out.println("PDFText selectFirst");
        return null;
    }

    public Mark selectLast(TextNode node) {
        System.out.println("PDFText selectLast");
        return null;
    }

    public int[] getSelected(Mark start,
                             Mark finish) {
        System.out.println("PDFText getSelected");
        return null;
    }

    public Shape getHighlightShape(Mark beginMark, Mark endMark) {
        System.out.println("PDFText getHighlightShape");
        return null;
    }

    public Shape getShape(TextNode node) {
        System.out.println("PDFText getShape");
        return proxyPainter.getShape(node);
    }

    public Shape getDecoratedShape(TextNode node) {
        //System.out.println("PDFText getDecoratedShape");
        return proxyPainter.getDecoratedShape(node);
    }

    public Rectangle2D getBounds(TextNode node) {
        //System.out.println("PDFText getBounds");
        return proxyPainter.getBounds(node);
    }

    public Rectangle2D getDecoratedBounds(TextNode node) {
        System.out.println("PDFText getDecoratedBounds");
        return proxyPainter.getDecoratedBounds(node);
    }

    public Rectangle2D getPaintedBounds(TextNode node) {
        // System.out.println("PDFText getPaintedBounds");
        return proxyPainter.getPaintedBounds(node);
    }

}

