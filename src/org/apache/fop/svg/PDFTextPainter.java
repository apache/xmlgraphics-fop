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
package org.apache.fop.svg;

import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;

import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Shape;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.font.TextAttribute;

//Batik
import org.apache.batik.gvt.TextPainter;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.gvt.text.Mark;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.gvt.renderer.StrokingTextPainter;
import org.apache.batik.gvt.renderer.StrokingTextPainter;

//FOP
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.FontInfo;

/**
 * Renders the attributed character iterator of a <tt>TextNode</tt>.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 * @version $Id$
 */
public class PDFTextPainter implements TextPainter {
    FontState fontState;

    /**
     * Use the stroking text painter to get the bounds and shape.
     * Also used as a fallback to draw the string with strokes.
     */
    protected static final TextPainter PROXY_PAINTER =
        StrokingTextPainter.getInstance();


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
    public void paint(TextNode node, Graphics2D g2d) {
        // System.out.println("PDFText paint");
        String txt = node.getText();
        Point2D loc = node.getLocation();
        /*

        AttributedCharacterIterator aci =
            node.getAttributedCharacterIterator();
        if (aci.getBeginIndex() == aci.getEndIndex()) {
            return;
        }
        // reset position to start of char iterator
        char ch = aci.first();
        if (ch == AttributedCharacterIterator.DONE) {
            return;
        }
        */
        TextNode.Anchor anchor =
            (TextNode.Anchor)node.getAttributedCharacterIterator().getAttribute(GVTAttributedCharacterIterator.TextAttribute.ANCHOR_TYPE);
        if (anchor != null) {
        }
        /*
        System.out.println("-----"+txt);
        printAttrs(node.getAttributedCharacterIterator());
        */
        paintTextRuns(node.getTextRuns(), g2d, loc);

    }


    protected void paintTextRuns(List textRuns, Graphics2D g2d, Point2D loc) {
        Point2D currentloc = loc;
        Iterator i = textRuns.iterator();
        while (i.hasNext()) {
            StrokingTextPainter.TextRun run =
                    (StrokingTextPainter.TextRun)i.next();
            currentloc = paintTextRun(run, g2d, currentloc);
        }
    }


    protected Point2D paintTextRun(StrokingTextPainter.TextRun run, Graphics2D g2d, Point2D loc) {
        AttributedCharacterIterator aci = run.getACI();
        return paintACI(aci, g2d, loc);
    }


    protected String getText(AttributedCharacterIterator aci) {
        StringBuffer sb = new StringBuffer(aci.getEndIndex()-aci.getBeginIndex());
        for (char c = aci.first(); c != CharacterIterator.DONE; c = aci.next()) {
            sb.append(c);
        }
        return sb.toString();
    }


    protected Point2D paintACI(AttributedCharacterIterator aci, Graphics2D g2d, Point2D loc) {
        aci.first();

        TextNode.Anchor anchor =
            (TextNode.Anchor)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.ANCHOR_TYPE);

        //Adjust position of span
        Float xpos =
            (Float)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.X);
        Float ypos =
            (Float)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.Y);
        Float dxpos =
            (Float)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.DX);
        Float dypos =
            (Float)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.DY);
        if (xpos != null) loc.setLocation(xpos.doubleValue(), loc.getY());
        if (ypos != null) loc.setLocation(loc.getX(), ypos.doubleValue());
        if (dxpos != null) loc.setLocation(loc.getX()+dxpos.doubleValue(), loc.getY());
        if (dypos != null) loc.setLocation(loc.getX(), loc.getY()+dypos.doubleValue());

        //Set up font
        List gvtFonts =
            (List)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.GVT_FONT_FAMILIES);
        Paint forg = (Paint)aci.getAttribute(TextAttribute.FOREGROUND);
        Float size = (Float)aci.getAttribute(TextAttribute.SIZE);
        if (size == null) {
            return loc;
        }

        TextPaintInfo tpi = (TextPaintInfo) aci.getAttribute(
            GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO);
        
        if (tpi == null) {
            return loc;
        }        

        Stroke stroke = tpi.strokeStroke;
        Float posture = (Float)aci.getAttribute(TextAttribute.POSTURE);
        Float taWeight = (Float)aci.getAttribute(TextAttribute.WEIGHT);

        if (forg instanceof Color) {
            g2d.setColor((Color)forg);
        }
        g2d.setPaint(forg);
        g2d.setStroke(stroke);

        String style = ((posture != null) && (posture.floatValue() > 0.0))
                       ? "italic" : "normal";
        String weight = ((taWeight != null) && (taWeight.floatValue() > 1.0))
                        ? "bold" : "normal";

        FontInfo fi = fontState.getFontInfo();
        boolean found = false;
        if (gvtFonts != null) {
            for (Iterator i = gvtFonts.iterator(); i.hasNext(); ) {
                GVTFontFamily fam = (GVTFontFamily)i.next();
                String name = fam.getFamilyName();
                if (fi.hasFont(name, style, weight)) {
                    try {
                        int fsize = (int)size.floatValue();
                        fontState = new FontState(fontState.getFontInfo(),
                                                  name, style, weight,
                                                  fsize * 1000, 0);
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
                int fsize = (int)size.floatValue();
                fontState = new FontState(fontState.getFontInfo(), "any",
                                          style, weight, fsize * 1000, 0);
            } catch (org.apache.fop.apps.FOPException fope) {
                fope.printStackTrace();
            }
        } else {
            if(g2d instanceof PDFGraphics2D) {
                ((PDFGraphics2D)g2d).setOverrideFontState(fontState);
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

        //Get text and paint
        String txt = getText(aci);
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
        //printAttrs(aci);
        g2d.drawString(txt, (float)(loc.getX() + tx), (float)loc.getY());
        loc.setLocation(loc.getX()+(double)advance, loc.getY());
        return loc;
    }

    private void printAttrs(AttributedCharacterIterator aci) {
        aci.first();
        Iterator i = aci.getAttributes().entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry)i.next();
            if (entry.getValue() != null) System.out.println(entry.getKey()+": "+entry.getValue());
        }
        int start = aci.getBeginIndex();
        System.out.print("AttrRuns: ");
        while (aci.current() != CharacterIterator.DONE) {
            int end   = aci.getRunLimit();
            System.out.print(""+(end-start)+", ");
            aci.setIndex(end);
            if (start == end) break;
            start = end;
        }
        System.out.println("");
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

    public Rectangle2D getBounds2D(TextNode node) {
        return PROXY_PAINTER.getBounds2D(node);
    }

    public Rectangle2D getGeometryBounds(TextNode node) {
        return PROXY_PAINTER.getGeometryBounds(node);
    }

    public Shape getOutline(TextNode node) {
        return PROXY_PAINTER.getOutline(node);
    }


}

