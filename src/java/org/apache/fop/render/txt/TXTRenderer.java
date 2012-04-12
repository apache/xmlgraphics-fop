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

package org.apache.fop.render.txt;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.xmlgraphics.util.UnitConv;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.area.Area;
import org.apache.fop.area.CTM;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.render.AbstractPathOrientedRenderer;
import org.apache.fop.render.txt.border.AbstractBorderElement;
import org.apache.fop.render.txt.border.BorderManager;

/**
 * <p>Renderer that renders areas to plain text.</p>
 *
 * <p>This work was authored by Art Welch and
 * Mark Lillywhite (mark-fop@inomial.com) [to use the new Renderer interface].</p>
 */
public class TXTRenderer extends AbstractPathOrientedRenderer {

    private static final char LIGHT_SHADE = '\u2591';

    private static final char MEDIUM_SHADE = '\u2592';

    private static final char DARK_SHADE = '\u2593';

    private static final char FULL_BLOCK = '\u2588';

    private static final char IMAGE_CHAR = '#';

    /**The stream for output */
    private OutputStream outputStream;

    /** The current stream to add Text commands to. */
    private TXTStream currentStream;

    /** Buffer for text. */
    private StringBuffer[] charData;

    /** Buffer for background and images. */
    private StringBuffer[] decoData;

    /** Leading of line containing Courier font size of 10pt. */
    public static final int LINE_LEADING = 1070;

    /** Height of one symbol in Courier font size of 10pt. */
    public static final int CHAR_HEIGHT = 7860;

    /** Width of one symbol in Courier font size of 10pt. */
    public static final int CHAR_WIDTH = 6000;

    /** Current processing page width. */
    private int pageWidth;

    /** Current processing page height. */
    private int pageHeight;

    /**
     * Every line except the last line on a page (which will end with
     * pageEnding) will be terminated with this string.
     */
    private final String lineEnding = "\r\n";

    /** Every page except the last one will end with this string. */
    private final String pageEnding = "\f";

    /** Equals true, if current page is first. */
    private boolean firstPage = false;

    /** Manager for storing border's information. */
    private BorderManager bm;

    /** Char for current filling. */
    private char fillChar;

    /** Saves current coordinate transformation. */
    private final TXTState currentState = new TXTState();

    private String encoding;

    /**
     * Constructs a newly allocated <code>TXTRenderer</code> object.
     *
     * @param userAgent the user agent that contains configuration details. This cannot be null.
     */
    public TXTRenderer(FOUserAgent userAgent) {
        super(userAgent);
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return "text/plain";
    }

    /**
     * Sets the encoding of the target file.
     * @param encoding the encoding, null to select the default encoding (UTF-8)
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Indicates if point (x, y) lay inside currentPage.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return <b>true</b> if point lay inside page
     */
    public boolean isLayInside(int x, int y) {
        return (x >= 0) && (x < pageWidth) && (y >= 0) && (y < pageHeight);
    }

    /**
     * Add char to text buffer.
     *
     * @param x  x coordinate
     * @param y  y coordinate
     * @param ch  char to add
     * @param ischar boolean, repersenting is character adding to text buffer
     */
    protected void addChar(int x, int y, char ch, boolean ischar) {
        Point point = currentState.transformPoint(x, y);
        putChar(point.x, point.y, ch, ischar);
    }

    /**
     * Add char to text or background buffer.
     *
     * @param x x coordinate
     * @param y x coordinate
     * @param ch char to add
     * @param ischar indicates if it char or background
     */
    protected void putChar(int x, int y, char ch, boolean ischar) {
        if (isLayInside(x, y)) {
            StringBuffer sb = ischar ? charData[y] : decoData[y];
            while (sb.length() <= x) {
                sb.append(' ');
            }
            sb.setCharAt(x, ch);
        }
    }

    /**
     * Adds string to text buffer (<code>charData</code>). <p>
     * Chars of string map in turn.
     *
     * @param row x coordinate
     * @param col y coordinate
     * @param s string to add
     */
    protected void addString(int row, int col, String s) {
        for (int l = 0; l < s.length(); l++) {
            addChar(col + l, row, s.charAt(l), true);
        }
    }

    /**
     * Render TextArea to Text.
     *
     * @param area  inline area to render
     */
    protected void renderText(TextArea area) {
        int col = Helper.ceilPosition(this.currentIPPosition, CHAR_WIDTH);
        int row = Helper.ceilPosition(this.currentBPPosition - LINE_LEADING, CHAR_HEIGHT + 2*LINE_LEADING);

        String s = area.getText();

        addString(row, col, s);

        super.renderText(area);
    }

    /**
     * {@inheritDoc}
     */
    public void renderPage(PageViewport page) throws IOException, FOPException {
        if (firstPage) {
            firstPage = false;
        } else {
            currentStream.add(pageEnding);
        }

        Rectangle2D bounds = page.getViewArea();
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        pageWidth = Helper.ceilPosition((int) width, CHAR_WIDTH);
        pageHeight = Helper.ceilPosition((int) height, CHAR_HEIGHT + 2*LINE_LEADING);

        // init buffers
        charData = new StringBuffer[pageHeight];
        decoData = new StringBuffer[pageHeight];
        for (int i = 0; i < pageHeight; i++) {
            charData[i] = new StringBuffer();
            decoData[i] = new StringBuffer();
        }

        bm = new BorderManager(pageWidth, pageHeight, currentState);

        super.renderPage(page);

        flushBorderToBuffer();
        flushBuffer();
    }

    /**
     * Projects current page borders (i.e.<code>bm</code>) to buffer for
     * background and images (i.e.<code>decoData</code>).
     */
    private void flushBorderToBuffer() {
        for (int x = 0; x < pageWidth; x++) {
            for (int y = 0; y < pageHeight; y++) {
                Character c = bm.getCharacter(x, y);
                if (c != null) {
                    putChar(x, y, c.charValue(), false);
                }
            }
        }
    }

    /**
     * Write out the buffer to output stream.
     */
    private void flushBuffer() {
        for (int row = 0; row < pageHeight; row++) {
            StringBuffer cr = charData[row];
            StringBuffer dr = decoData[row];
            StringBuffer outr = null;

            if (cr != null && dr == null) {
                outr = cr;
            } else if (dr != null && cr == null) {
                outr = dr;
            } else if (cr != null && dr != null) {
                int len = dr.length();
                if (cr.length() > len) {
                    len = cr.length();
                }
                outr = new StringBuffer();
                for (int countr = 0; countr < len; countr++) {
                    if (countr < cr.length() && cr.charAt(countr) != ' ') {
                        outr.append(cr.charAt(countr));
                    } else if (countr < dr.length()) {
                        outr.append(dr.charAt(countr));
                    } else {
                        outr.append(' ');
                    }
                }
            }

            if (outr != null) {
                currentStream.add(outr.toString());
            }
            if (row < pageHeight) {
                currentStream.add(lineEnding);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void startRenderer(OutputStream os) throws IOException {
        log.info("Rendering areas to TEXT.");
        this.outputStream = os;
        currentStream = new TXTStream(os);
        currentStream.setEncoding(this.encoding);
        firstPage = true;
    }

    /**
     * {@inheritDoc}
     */
    public void stopRenderer() throws IOException {
        log.info("writing out TEXT");
        outputStream.flush();
        super.stopRenderer();
    }

    /**
     * Does nothing.
     * {@inheritDoc}
     */
    protected void restoreStateStackAfterBreakOut(List breakOutList) {
    }

    /**
     * Does nothing.
     * @return null
     * {@inheritDoc}
     */
    protected List breakOutOfStateStack() {
        return null;
    }

    /**
     * Does nothing.
     * {@inheritDoc}
     */
    protected void saveGraphicsState() {
        currentState.push(new CTM());
    }

    /**
     * Does nothing.
     * {@inheritDoc}
     */
    protected void restoreGraphicsState() {
        currentState.pop();
    }

    /**
     * Does nothing.
     * {@inheritDoc}
     */
    protected void beginTextObject() {
    }

    /**
     * Does nothing.
     * {@inheritDoc}
     */
    protected void endTextObject() {
    }

    /**
     * Does nothing.
     * {@inheritDoc}
     */
    protected void clip() {
    }

    /**
     * Does nothing.
     * {@inheritDoc}
     */
    protected void clipRect(float x, float y, float width, float height) {
    }

    /**
     * Does nothing.
     * {@inheritDoc}
     */
    protected void moveTo(float x, float y) {
    }

    /**
     * Does nothing.
     * {@inheritDoc}
     */
    protected void lineTo(float x, float y) {
    }

    /**
     * Does nothing.
     * {@inheritDoc}
     */
    protected void closePath() {
    }

    /**
     * Fills rectangle startX, startY, width, height with char
     * <code>charToFill</code>.
     *
     * @param startX x-coordinate of upper left point
     * @param startY y-coordinate of upper left point
     * @param width width of rectangle
     * @param height height of rectangle
     * @param charToFill filling char
     */
    private void fillRect(int startX, int startY, int width, int height,
            char charToFill) {
        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                addChar(x, y, charToFill, false);
            }
        }
    }

    /**
     * Fills a rectangular area with the current filling char.
     * {@inheritDoc}
     */
    protected void fillRect(float x, float y, float width, float height) {
        fillRect(bm.getStartX(), bm.getStartY(), bm.getWidth(), bm.getHeight(),
                fillChar);
    }

    /**
     * Changes current filling char.
     * {@inheritDoc}
     */
    protected void updateColor(Color col, boolean fill) {
        if (col == null) {
            return;
        }
        // fillShade evaluation was taken from fop-0.20.5
        // TODO: This fillShase is catually the luminance component of the color
        // transformed to the YUV (YPrBb) Colorspace. It should use standard
        // Java methods for its conversion instead of the formula given here.
        double fillShade = 0.30f / 255f * col.getRed()
                         + 0.59f / 255f * col.getGreen()
                         + 0.11f / 255f * col.getBlue();
        fillShade = 1 - fillShade;

        if (fillShade > 0.8f) {
            fillChar = FULL_BLOCK;
        } else if (fillShade > 0.6f) {
            fillChar = DARK_SHADE;
        } else if (fillShade > 0.4f) {
            fillChar = MEDIUM_SHADE;
        } else if (fillShade > 0.2f) {
            fillChar = LIGHT_SHADE;
        } else {
            fillChar = ' ';
        }
    }

    /** {@inheritDoc} */
    protected void drawImage(String url, Rectangle2D pos, Map foreignAttributes) {
        //No images are painted here
    }

    /**
     * Fills image rectangle with a <code>IMAGE_CHAR</code>.
     *
     * @param   image   the base image
     * @param   pos     the position of the image
     */
    public void renderImage(Image image, Rectangle2D pos) {
        int x1 = Helper.ceilPosition(currentIPPosition, CHAR_WIDTH);
        int y1 = Helper.ceilPosition(currentBPPosition - LINE_LEADING, CHAR_HEIGHT + 2*LINE_LEADING);
        int width = Helper.ceilPosition((int) pos.getWidth(), CHAR_WIDTH);
        int height = Helper.ceilPosition((int) pos.getHeight(), CHAR_HEIGHT + 2*LINE_LEADING);

        fillRect(x1, y1, width, height, IMAGE_CHAR);
    }


    /**
     * Returns the closest integer to the multiplication of a number and 1000.
     *
     * @param x  the value of the argument, multiplied by
     *            1000 and rounded
     * @return the value of the argument multiplied by
     *         1000 and rounded to the nearest integer
     */
    protected int toMilli(float x) {
        return Math.round(x * 1000f);
    }

    /**
     * Adds one element of border.
     *
     * @param x  x coordinate
     * @param y  y coordinate
     * @param style  integer, representing border style
     * @param type  integer, representing border element type
     */
    private void addBitOfBorder(int x, int y, int style, int type) {
        Point point = currentState.transformPoint(x, y);
        if (isLayInside(point.x, point.y)) {
            bm.addBorderElement(point.x, point.y, style, type);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void drawBorderLine(                               // CSOK: ParameterNumber
            float x1, float y1, float x2, float y2,
            boolean horz, boolean startOrBefore, int style, Color col) {

        int borderHeight = bm.getHeight();
        int borderWidth = bm.getWidth();
        int borderStartX = bm.getStartX();
        int borderStartY = bm.getStartY();

        int x;
        int y;
        if (horz && startOrBefore) { // BEFORE
            x = borderStartX;
            y = borderStartY;
        } else if (horz && !startOrBefore) { // AFTER
            x = borderStartX;
            y = borderStartY + borderHeight - 1;
        } else if (!horz && startOrBefore) { // START
            x = borderStartX;
            y = borderStartY;
        } else { // END
            x = borderStartX + borderWidth - 1;
            y = borderStartY;
        }

        int dx;
        int dy;
        int length;
        int startType;
        int endType;
        if (horz) {
            length = borderWidth;
            dx = 1;
            dy = 0;
            startType = 1 << AbstractBorderElement.RIGHT;
            endType = 1 << AbstractBorderElement.LEFT;
        } else {
            length = borderHeight;
            dx = 0;
            dy = 1;
            startType = 1 << AbstractBorderElement.DOWN;
            endType = 1 << AbstractBorderElement.UP;
        }

        addBitOfBorder(x, y, style, startType);
        for (int i = 0; i < length - 2; i++) {
            x += dx;
            y += dy;
            addBitOfBorder(x, y, style, startType + endType);
        }
        x += dx;
        y += dy;
        addBitOfBorder(x, y, style, endType);
    }

    /**
     * {@inheritDoc}
     */
    protected void drawBackAndBorders(Area area, float startx, float starty,
            float width, float height) {
        bm.setWidth(Helper.ceilPosition(toMilli(width), CHAR_WIDTH));
        bm.setHeight(Helper.ceilPosition(toMilli(height), CHAR_HEIGHT + 2*LINE_LEADING));
        bm.setStartX(Helper.ceilPosition(toMilli(startx), CHAR_WIDTH));
        bm.setStartY(Helper.ceilPosition(toMilli(starty), CHAR_HEIGHT + 2*LINE_LEADING));

        super.drawBackAndBorders(area, startx, starty, width, height);
    }

    /**
     * {@inheritDoc}
     */
    protected void startVParea(CTM ctm, Rectangle clippingRect) {
        currentState.push(ctm);
    }

    /**
     * {@inheritDoc}
     */
    protected void endVParea() {
        currentState.pop();
    }

    /** {@inheritDoc} */
    protected void concatenateTransformationMatrix(AffineTransform at) {
        currentState.push(new CTM(UnitConv.ptToMpt(at)));
    }

}
