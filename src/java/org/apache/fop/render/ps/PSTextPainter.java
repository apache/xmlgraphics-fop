/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.render.ps;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
/* java.awt.Font is not imported to avoid confusion with
   org.apache.fop.fonts.Font */

import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.awt.font.TextAttribute;
import java.awt.Shape;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.Color;
import java.util.List;
import java.util.Iterator;

import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.gvt.text.Mark;
import org.apache.batik.gvt.TextPainter;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.gvt.renderer.StrokingTextPainter;

import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fonts.Font;
import org.apache.fop.svg.ACIUtils;
import org.apache.fop.apps.Document;

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
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id: PSTextPainter.java,v 1.15 2003/01/08 14:03:55 jeremias Exp $
 */
public class PSTextPainter implements TextPainter {
    
    private Document document;

    /**
     * Use the stroking text painter to get the bounds and shape.
     * Also used as a fallback to draw the string with strokes.
     */
    protected static final TextPainter 
        PROXY_PAINTER = StrokingTextPainter.getInstance();

    /**
     * Create a new PS text painter with the given font information.
     * @param document the context document
     */
    public PSTextPainter(Document document) {
        this.document = document;
    }

    /**
     * Paints the specified attributed character iterator using the
     * specified Graphics2D and context and font context.
     * @param node the TextNode to paint
     * @param g2d the Graphics2D to use
     */
    public void paint(TextNode node, Graphics2D g2d) {
        // System.out.println("PSText paint");
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
        
        TextPaintInfo tpi = (TextPaintInfo) aci.getAttribute(
            GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO);
        if ((tpi != null) 
                && ((tpi.strokeStroke != null && tpi.strokePaint != null)
                    || (tpi.strikethroughStroke != null)
                    || (tpi.underlineStroke != null)
                    || (tpi.overlineStroke != null))) {
            hasunsupported = true;
        }

        //Alpha is not supported
        Paint foreground = (Paint) aci.getAttribute(TextAttribute.FOREGROUND);
        if (foreground instanceof Color) {
            Color col = (Color)foreground;
            if (col.getAlpha() != 255) {
                hasunsupported = true;
            }
        }

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
        
        Object lengthAdjust = aci.getAttribute(
                            GVTAttributedCharacterIterator.TextAttribute.LENGTH_ADJUST);
        if (lengthAdjust != null) {
            hasunsupported = true;
        }

        Object writeMod = aci.getAttribute(
                GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE);
        if (writeMod != null 
            && !GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE_LTR.equals(
                  writeMod)) {
            hasunsupported = true;
        }

        Object vertOr = aci.getAttribute(
                GVTAttributedCharacterIterator.TextAttribute.VERTICAL_ORIENTATION);
        if (GVTAttributedCharacterIterator.TextAttribute.ORIENTATION_ANGLE.equals(
                  vertOr)) {
            hasunsupported = true;
        }
        
        Object rcDel = aci.getAttribute(
                GVTAttributedCharacterIterator.TextAttribute.TEXT_COMPOUND_DELIMITER);
        if (!(rcDel instanceof SVGOMTextElement)) {
            hasunsupported = true; //Filter spans
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
        //System.out.println("==============================================");
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
        
        //Finally draw text
        if (g2d instanceof PSGraphics2D) {
            ((PSGraphics2D) g2d).setOverrideFont(font);
        }
        try {
            g2d.drawString(txt, (float)(loc.getX() + tx), (float)(loc.getY()));
        } finally {
            if (g2d instanceof PSGraphics2D) {
                ((PSGraphics2D) g2d).setOverrideFont(null);
            }
        }
        loc.setLocation(loc.getX() + (double)advance, loc.getY());
        return loc;
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
                       ? Font.BOLD
                       : Font.NORMAL;
    }

    private Font makeFont(AttributedCharacterIterator aci) {
        Float fontSize = (Float)aci.getAttribute(TextAttribute.SIZE);
        String style = getStyle(aci);
        int weight = getWeight(aci);

        boolean found = false;
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
                if (document.hasFont(fontFamily, style, weight)) {
                    String fname = document.fontLookup(
                            fontFamily, style, weight);
                    FontMetrics metrics = document.getMetricsFor(fname);
                    int fsize = (int)(fontSize.floatValue() * 1000);
                    return new Font(fname, metrics, fsize);
                }
            }
        }
        String fname = document.fontLookup(
                "any", style, Font.NORMAL);
        FontMetrics metrics = document.getMetricsFor(fname);
        int fsize = (int)(fontSize.floatValue() * 1000);
        return new Font(fname, metrics, fsize);
    }

    private java.awt.Font makeAWTFont(AttributedCharacterIterator aci, Font font) {
        final String style = getStyle(aci);
        final int weight = getWeight(aci);
        int fStyle = java.awt.Font.PLAIN;
        if (weight == Font.BOLD) {
            fStyle |= java.awt.Font.BOLD;
        }
        if ("italic".equals(style)) {
            fStyle |= java.awt.Font.ITALIC;
        }
        return new java.awt.Font(font.getFontName(), fStyle,
                             (int)(font.getFontSize() / 1000));
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
        System.out.println("PSText getMark");
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
        System.out.println("PSText selectAt");
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
        System.out.println("PSText selectTo");
        return null;
    }

    /**
     * Selec first.
     * This does nothing since the output is pdf and not interactive.
     * @param node the text node
     * @return null
     */
    public Mark selectFirst(TextNode node) {
        System.out.println("PSText selectFirst");
        return null;
    }

    /**
     * Select last.
     * This does nothing since the output is pdf and not interactive.
     * @param node the text node
     * @return null
     */
    public Mark selectLast(TextNode node) {
        System.out.println("PSText selectLast");
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
        System.out.println("PSText getSelected");
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
        System.out.println("PSText getHighlightShape");
        return null;
    }

}


