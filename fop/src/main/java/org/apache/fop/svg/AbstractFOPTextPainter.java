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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.batik.bridge.Mark;
import org.apache.batik.bridge.StrokingTextPainter;
import org.apache.batik.bridge.TextNode;
import org.apache.batik.bridge.TextPainter;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;

import org.apache.fop.afp.AFPGraphics2D;
import org.apache.fop.fonts.Font;

/**
 * Renders the attributed character iterator of a {@link TextNode}.
 * This class draws the text directly into the Graphics2D so that
 * the text is not drawn using shapes.
 * If the text is simple enough to draw then it sets the font and calls
 * drawString. If the text is complex or the cannot be translated
 * into a simple drawString the StrokingTextPainter is used instead.
 */
public abstract class AbstractFOPTextPainter implements TextPainter {

    /** the logger for this class */
    protected Log log = LogFactory.getLog(AbstractFOPTextPainter.class);

    private final FOPTextHandler nativeTextHandler;

    /**
     * Use the stroking text painter to get the bounds and shape.
     * Also used as a fallback to draw the string with strokes.
     */
    private final TextPainter proxyTextPainter;

    /**
     * Create a new PS text painter with the given font information.
     * @param nativeTextHandler the NativeTextHandler instance used for text painting
     */
    public AbstractFOPTextPainter(FOPTextHandler nativeTextHandler, TextPainter proxyTextPainter) {
        this.nativeTextHandler = nativeTextHandler;
        this.proxyTextPainter = proxyTextPainter;
    }

    /**
     * Paints the specified attributed character iterator using the
     * specified Graphics2D and context and font context.
     *
     * @param node the TextNode to paint
     * @param g2d the Graphics2D to use
     */
    public void paint(TextNode node, Graphics2D g2d) {
        if (isSupportedGraphics2D(g2d)) {
            new TextRunPainter().paintTextRuns(node.getTextRuns(), g2d, node.getLocation());
        }
        proxyTextPainter.paint(node, g2d);
    }

    /**
     * Checks whether the Graphics2D is compatible with this text painter. Batik may
     * pass in a Graphics2D instance that paints on a special buffer image, for example
     * for filtering operations. In that case, the text painter should be bypassed.
     * @param g2d the Graphics2D instance to check
     * @return true if the Graphics2D is supported
     */
    protected abstract boolean isSupportedGraphics2D(Graphics2D g2d);

    private class TextRunPainter {

        private Point2D currentLocation;

        public void paintTextRuns(Iterable<StrokingTextPainter.TextRun> textRuns, Graphics2D g2d,
                Point2D nodeLocation) {
            currentLocation = new Point2D.Double(nodeLocation.getX(), nodeLocation.getY());
            for (StrokingTextPainter.TextRun run : textRuns) {
                paintTextRun(run, g2d);
            }
        }

        private void paintTextRun(StrokingTextPainter.TextRun run, Graphics2D g2d) {
            AttributedCharacterIterator aci = run.getACI();
            aci.first();
            updateLocationFromACI(aci, currentLocation);
            // font
            Font font = getFont(aci);
            if (font != null) {
                nativeTextHandler.setOverrideFont(font);
            }
            // color
            TextPaintInfo tpi = (TextPaintInfo) aci.getAttribute(
                    GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO);
            if (tpi == null) {
                return;
            }
            Paint foreground = tpi.fillPaint;
            if (foreground instanceof Color) {
                Color col = (Color) foreground;
                g2d.setColor(col);
            }
            g2d.setPaint(foreground);
            // text anchor
            TextNode.Anchor anchor = (TextNode.Anchor) aci.getAttribute(
                    GVTAttributedCharacterIterator.TextAttribute.ANCHOR_TYPE);
            // text
            String txt = getText(aci);
            double advance = font == null ? run.getLayout().getAdvance2D().getX() : getStringWidth(txt, font);
            double tx = 0;
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
            // draw string
            Point2D outputLocation = g2d.getTransform().transform(currentLocation, null);
            double x = outputLocation.getX();
            double y = outputLocation.getY();
            try {
                try {
                    AFPGraphics2D afpg2d = (AFPGraphics2D)g2d;
                    int fontSize = 0;
                    if (font != null) {
                        fontSize = (int) Math.round(afpg2d.convertToAbsoluteLength(font.getFontSize()));
                    }
                    if (fontSize < 6000) {
                        nativeTextHandler.drawString(g2d, txt, (float) (x + tx), (float) y);
                    } else {
                        double scaleX = g2d.getTransform().getScaleX();
                        for (int i = 0; i < txt.length(); i++) {
                            double ad = run.getLayout().getGlyphAdvances()[i] * scaleX;
                            nativeTextHandler.drawString(g2d, txt.charAt(i) + "", (float) (x + tx + ad), (float) y);
                        }
                    }
                    //TODO draw underline and overline if set
                    //TODO draw strikethrough if set
                } catch (IOException ioe) {
                    if (g2d instanceof AFPGraphics2D) {
                        ((AFPGraphics2D) g2d).handleIOException(ioe);
                    }
                }
            } finally {
                nativeTextHandler.setOverrideFont(null);
            }
            currentLocation.setLocation(currentLocation.getX() + advance, currentLocation.getY());
        }
        private void updateLocationFromACI(AttributedCharacterIterator aci, Point2D loc) {
            //Adjust position of span
            Float xpos = (Float) aci.getAttribute(
                    GVTAttributedCharacterIterator.TextAttribute.X);
            Float ypos = (Float) aci.getAttribute(
                    GVTAttributedCharacterIterator.TextAttribute.Y);
            Float dxpos = (Float) aci.getAttribute(
                    GVTAttributedCharacterIterator.TextAttribute.DX);
            Float dypos = (Float) aci.getAttribute(
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

    private Font getFont(AttributedCharacterIterator aci) {
        Font[] fonts = ACIUtils.findFontsForBatikACI(aci, nativeTextHandler.getFontInfo());
        return fonts == null ? null : fonts[0];
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
        return proxyTextPainter.getOutline(node);
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
        return proxyTextPainter.getBounds2D(node);
    }

    /**
     * Get the geometry bounds.
     * This uses the StrokingTextPainter to get the bounds
     * since in theory it should be the same.
     *
     * @param node the text node
     * @return the bounds of the text
     */
    public Rectangle2D getGeometryBounds(TextNode node) {
        return proxyTextPainter.getGeometryBounds(node);
    }

    // Methods that have no purpose for PS

    /**
     * Get the mark.
     * This does nothing since the output is AFP and not interactive.
     *
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
     * This does nothing since the output is AFP and not interactive.
     *
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
     * This does nothing since the output is AFP and not interactive.
     *
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
     * This does nothing since the output is AFP and not interactive.
     *
     * @param node the text node
     * @return null
     */
    public Mark selectFirst(TextNode node) {
        return null;
    }

    /**
     * Select last.
     * This does nothing since the output is AFP and not interactive.
     *
     * @param node the text node
     * @return null
     */
    public Mark selectLast(TextNode node) {
        return null;
    }

    /**
     * Get selected.
     * This does nothing since the output is AFP and not interactive.
     *
     * @param start the start mark
     * @param finish the finish mark
     * @return null
     */
    public int[] getSelected(Mark start, Mark finish) {
        return null;
    }

    /**
     * Get the highlighted shape.
     * This does nothing since the output is AFP and not interactive.
     *
     * @param beginMark the start mark
     * @param endMark the end mark
     * @return null
     */
    public Shape getHighlightShape(Mark beginMark, Mark endMark) {
        return null;
    }

}
