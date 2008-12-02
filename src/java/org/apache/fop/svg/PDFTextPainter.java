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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.lang.reflect.Method;
import java.text.AttributedCharacterIterator;
import java.util.Iterator;
import java.util.List;

import org.apache.batik.bridge.SVGFontFamily;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.renderer.StrokingTextPainter;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.batik.gvt.text.TextSpanLayout;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.util.CharUtilities;

/**
 * Renders the attributed character iterator of a <tt>TextNode</tt>.
 * This class draws the text directly into the PDFGraphics2D so that
 * the text is not drawn using shapes which makes the PDF files larger.
 * If the text is simple enough to draw then it sets the font and calls
 * drawString. If the text is complex or the cannot be translated
 * into a simple drawString the StrokingTextPainter is used instead.
 *
 * @version $Id$
 */
public class PDFTextPainter extends StrokingTextPainter {

    private static final boolean DEBUG = false;

    private final boolean strokeText = false;
    private final FontInfo fontInfo;

    /**
     * Create a new PDF text painter with the given font information.
     * @param fi the font info
     */
    public PDFTextPainter(FontInfo fi) {
        fontInfo = fi;
    }

    /** {@inheritDoc} */
    protected void paintTextRuns(List textRuns, Graphics2D g2d) {
        if (DEBUG) {
            System.out.println("paintTextRuns: count = " + textRuns.size());
            //fontInfo.dumpAllTripletsToSystemOut();
        }
        if (!(g2d instanceof PDFGraphics2D) || strokeText) {
            super.paintTextRuns(textRuns, g2d);
            return;
        }
        final PDFGraphics2D pdf = (PDFGraphics2D)g2d;
        PDFTextUtil textUtil = new PDFTextUtil(pdf.fontInfo) {
            protected void write(String code) {
                pdf.currentStream.write(code);
            }
        };
        for (int i = 0; i < textRuns.size(); i++) {
            TextRun textRun = (TextRun)textRuns.get(i);
            AttributedCharacterIterator runaci = textRun.getACI();
            runaci.first();

            TextPaintInfo tpi = (TextPaintInfo)runaci.getAttribute(PAINT_INFO);
            if (tpi == null || !tpi.visible) {
                continue;
            }
            if ((tpi != null) && (tpi.composite != null)) {
                g2d.setComposite(tpi.composite);
            }

            //------------------------------------
            TextSpanLayout layout = textRun.getLayout();
            if (DEBUG) {
                int charCount = runaci.getEndIndex() - runaci.getBeginIndex();
                System.out.println("================================================");
                System.out.println("New text run:");
                System.out.println("char count: " + charCount);
                System.out.println("range: "
                        + runaci.getBeginIndex() + " - " + runaci.getEndIndex());
                System.out.println("glyph count: " + layout.getGlyphCount()); //=getNumGlyphs()
            }
            //Gather all characters of the run
            StringBuffer chars = new StringBuffer();
            for (runaci.first(); runaci.getIndex() < runaci.getEndIndex();) {
                chars.append(runaci.current());
                runaci.next();
            }
            runaci.first();
            if (DEBUG) {
                System.out.println("Text: " + chars);
                pdf.currentStream.write("%Text: " + chars + "\n");
            }

            GeneralPath debugShapes = null;
            if (DEBUG) {
                debugShapes = new GeneralPath();
            }

            Font[] fonts = findFonts(runaci);
            if (fonts == null || fonts.length == 0) {
                //Draw using Java2D
                textRun.getLayout().draw(g2d);
                continue;
            }

            textUtil.saveGraphicsState();
            textUtil.concatMatrix(g2d.getTransform());
            Shape imclip = g2d.getClip();
            pdf.writeClip(imclip);

            applyColorAndPaint(tpi, pdf);

            textUtil.beginTextObject();
            textUtil.setFonts(fonts);
            boolean stroke = (tpi.strokePaint != null)
                && (tpi.strokeStroke != null);
            textUtil.setTextRenderingMode(tpi.fillPaint != null, stroke, false);

            AffineTransform localTransform = new AffineTransform();
            Point2D prevPos = null;
            double prevVisibleCharWidth = 0.0;
            GVTGlyphVector gv = layout.getGlyphVector();
            for (int index = 0, c = gv.getNumGlyphs(); index < c; index++) {
                char ch = chars.charAt(index);
                boolean visibleChar = gv.isGlyphVisible(index)
                    || (CharUtilities.isAnySpace(ch) && !CharUtilities.isZeroWidthSpace(ch));
                if (DEBUG) {
                    System.out.println("glyph " + index
                            + " -> " + layout.getGlyphIndex(index) + " => " + ch);
                    if (CharUtilities.isAnySpace(ch) && ch != 32) {
                        System.out.println("Space found: " + Integer.toHexString(ch));
                    }
                    if (ch == CharUtilities.ZERO_WIDTH_JOINER) {
                        System.out.println("ZWJ found: " + Integer.toHexString(ch));
                    }
                    if (ch == CharUtilities.SOFT_HYPHEN) {
                        System.out.println("Soft hyphen found: " + Integer.toHexString(ch));
                    }
                    if (!visibleChar) {
                        System.out.println("Invisible glyph found: " + Integer.toHexString(ch));
                    }
                }
                if (!visibleChar) {
                    continue;
                }
                Point2D p = gv.getGlyphPosition(index);

                AffineTransform glyphTransform = gv.getGlyphTransform(index);
                //TODO Glyph transforms could be refined so not every char has to be painted
                //with its own TJ command (stretch/squeeze case could be optimized)
                if (DEBUG) {
                    System.out.println("pos " + p + ", transform " + glyphTransform);
                    Shape sh;
                    sh = gv.getGlyphLogicalBounds(index);
                    if (sh == null) {
                        sh = new Ellipse2D.Double(p.getX(), p.getY(), 2, 2);
                    }
                    debugShapes.append(sh, false);
                }

                //Exact position of the glyph
                localTransform.setToIdentity();
                localTransform.translate(p.getX(), p.getY());
                if (glyphTransform != null) {
                    localTransform.concatenate(glyphTransform);
                }
                localTransform.scale(1, -1);

                boolean yPosChanged = (prevPos == null
                        || prevPos.getY() != p.getY()
                        || glyphTransform != null);
                if (yPosChanged) {
                    if (index > 0) {
                        textUtil.writeTJ();
                        textUtil.writeTextMatrix(localTransform);
                    }
                } else {
                    double xdiff = p.getX() - prevPos.getX();
                    //Width of previous character
                    Font font = textUtil.getCurrentFont();
                    double cw = prevVisibleCharWidth;
                    double effxdiff = (1000 * xdiff) - cw;
                    if (effxdiff != 0) {
                        double adjust = (-effxdiff / font.getFontSize());
                        textUtil.adjustGlyphTJ(adjust * 1000);
                    }
                    if (DEBUG) {
                        System.out.println("==> x diff: " + xdiff + ", " + effxdiff
                                + ", charWidth: " + cw);
                    }
                }
                Font f = textUtil.selectFontForChar(ch);
                if (f != textUtil.getCurrentFont()) {
                    textUtil.writeTJ();
                    textUtil.setCurrentFont(f);
                    textUtil.writeTf(f);
                    textUtil.writeTextMatrix(localTransform);
                }
                char paintChar = (CharUtilities.isAnySpace(ch) ? ' ' : ch);
                textUtil.writeTJChar(paintChar);

                //Update last position
                prevPos = p;
                prevVisibleCharWidth = textUtil.getCurrentFont().getCharWidth(chars.charAt(index));
            }
            textUtil.writeTJ();
            textUtil.endTextObject();
            textUtil.restoreGraphicsState();
            if (DEBUG) {
                g2d.setStroke(new BasicStroke(0));
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.draw(debugShapes);
            }
        }
    }

    private void applyColorAndPaint(TextPaintInfo tpi, PDFGraphics2D pdf) {
        Paint fillPaint = tpi.fillPaint;
        Paint strokePaint = tpi.strokePaint;
        Stroke stroke = tpi.strokeStroke;
        int fillAlpha = PDFGraphics2D.OPAQUE;
        if (fillPaint instanceof Color) {
            Color col = (Color)fillPaint;
            pdf.applyColor(col, true);
            fillAlpha = col.getAlpha();
        }
        if (strokePaint instanceof Color) {
            Color col = (Color)strokePaint;
            pdf.applyColor(col, false);
        }
        pdf.applyPaint(fillPaint, true);
        pdf.applyStroke(stroke);
        if (strokePaint != null) {
            pdf.applyPaint(strokePaint, false);
        }
        pdf.applyAlpha(fillAlpha, PDFGraphics2D.OPAQUE);
    }

    private Font[] findFonts(AttributedCharacterIterator aci) {
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
                Method method = gvtFont.getClass().getMethod("getFamilyName", null);
                String gvtFontFamily = (String)method.invoke(gvtFont, null);
                //TODO Uncomment the following line when Batik 1.7 is shipped with FOP
                //String gvtFontFamily = gvtFont.getFamilyName(); //Not available in Batik 1.6
                if (DEBUG) {
                    System.out.print(gvtFontFamily + ", ");
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
                if (DEBUG) {
                    System.out.print(fontFamily + ", ");
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
        if (DEBUG) {
            System.out.println();
        }
        return (Font[])fonts.toArray(new Font[fonts.size()]);
    }

}