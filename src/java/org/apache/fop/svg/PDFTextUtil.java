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

import java.awt.geom.AffineTransform;

import org.apache.fop.fonts.Font;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.pdf.PDFText;

/**
 * Utility class for generating PDF text objects.
 */
public class PDFTextUtil {

    /** The number of decimal places. */ 
    private static final int DEC = 8;
    
    /** PDF text rendering mode: Fill text */
    public static final int TR_FILL = 0;
    /** PDF text rendering mode: Stroke text */
    public static final int TR_STROKE = 1;
    /** PDF text rendering mode: Fill, then stroke text */
    public static final int TR_FILL_STROKE = 2;
    /** PDF text rendering mode: Neither fill nor stroke text (invisible) */
    public static final int TR_INVISIBLE = 3;
    /** PDF text rendering mode: Fill text and add to path for clipping */
    public static final int TR_FILL_CLIP = 4;
    /** PDF text rendering mode: Stroke text and add to path for clipping */
    public static final int TR_STROKE_CLIP = 5;
    /** PDF text rendering mode: Fill, then stroke text and add to path for clipping */
    public static final int TR_FILL_STROKE_CLIP = 6;
    /** PDF text rendering mode: Add text to path for clipping */
    public static final int TR_CLIP = 7;
    
    
    private PDFGraphics2D g2d;
    private boolean inTextObject = false;
    private Font[] fonts;
    private Font font;
    private String startText;
    private String endText;
    private boolean useMultiByte;
    private StringBuffer bufTJ;
    private int textRenderingMode = 0;
    
    /**
     * Main constructor.
     * @param g2d the PDFGraphics2D instance to work with
     */
    public PDFTextUtil(PDFGraphics2D g2d) {
        this.g2d = g2d;
    }
    
    private void writeAffineTransform(AffineTransform at, StringBuffer sb) {
        double[] lt = new double[6];
        at.getMatrix(lt);
        sb.append(PDFNumber.doubleOut(lt[0], DEC)).append(" ");
        sb.append(PDFNumber.doubleOut(lt[1], DEC)).append(" ");
        sb.append(PDFNumber.doubleOut(lt[2], DEC)).append(" ");
        sb.append(PDFNumber.doubleOut(lt[3], DEC)).append(" ");
        sb.append(PDFNumber.doubleOut(lt[4], DEC)).append(" ");
        sb.append(PDFNumber.doubleOut(lt[5], DEC));
    }

    private void writeChar(char ch, StringBuffer sb) {
        if (!useMultiByte) {
            if (ch > 127) {
                sb.append("\\").append(Integer.toOctalString((int)ch));
            } else {
                switch (ch) {
                case '(':
                case ')':
                case '\\':
                    sb.append("\\");
                    break;
                default:
                }
                sb.append(ch);
            }
        } else {
            sb.append(PDFText.toUnicodeHex(ch));
        }
    }
    
    private void checkInTextObject() {
        if (!inTextObject) {
            throw new IllegalStateException("Not in text object");
        }
    }
    
    /**
     * Called when a new text object should be started. Be sure to call setFont() before
     * issuing any text painting commands.
     */
    public void beginTextObject() {
        if (inTextObject) {
            throw new IllegalStateException("Already in text object");
        }
        g2d.currentStream.write("BT\n");
        this.inTextObject = true;
    }
    
    /**
     * Called when a text object should be ended.
     */
    public void endTextObject() {
        checkInTextObject();
        g2d.currentStream.write("ET\n");
        this.inTextObject = false;
        initValues();
    }
    
    private void initValues() {
        this.font = null;
        this.textRenderingMode = TR_FILL;
    }
    
    /**
     * Creates a "q" command, pushing a copy of the entire graphics state onto the stack.
     */
    public void saveGraphicsState() {
        g2d.currentStream.write("q\n");
    }
    
    /**
     * Creates a "Q" command, restoring the entire graphics state to its former value by popping
     * it from the stack.
     */
    public void restoreGraphicsState() {
        g2d.currentStream.write("Q\n");
    }
    
    /**
     * Creates a "cm" command using the current transformation as the matrix.
     */
    public void concatMatrixCurrentTransform() {
        StringBuffer sb = new StringBuffer();
        if (!g2d.getTransform().isIdentity()) {
            writeAffineTransform(g2d.getTransform(), sb);
            sb.append(" cm\n");
        }
        g2d.currentStream.write(sb.toString());
    }
    
    /**
     * Sets the current fonts for the text object. For every character, the suitable font will
     * be selected.
     * @param fonts the new fonts
     */
    public void setFonts(Font[] fonts) {
        this.fonts = fonts;
    }
    
    /**
     * Sets the current font for the text object.
     * @param font the new font
     */
    public void setFont(Font font) {
        setFonts(new Font[] {font});
    }
    
    /**
     * Returns the current font in use.
     * @return the current font or null if no font is currently active.
     */
    public Font getCurrentFont() {
        return this.font;
    }
    
    /**
     * Sets the current font.
     * @param f the new font to use
     */
    public void setCurrentFont(Font f) {
        this.font = f;
    }
    
    /**
     * Writes a "Tf" command, setting a new current font.
     * @param f the font to select
     */
    public void writeTf(Font f) {
        checkInTextObject();
        String fontName = f.getFontName();
        float fontSize = (float)f.getFontSize() / 1000f;
        g2d.currentStream.write("/" + fontName + " " + PDFNumber.doubleOut(fontSize) + " Tf\n");
        
        this.useMultiByte = g2d.isMultiByteFont(fontName);
        this.startText = useMultiByte ? "<" : "(";
        this.endText = useMultiByte ? ">" : ")";
    }

    /**
     * Sets the text rendering mode.
     * @param mode the rendering mode (value 0 to 7, see PDF Spec, constants: TR_*)
     */
    public void setTextRenderingMode(int mode) {
        if (mode < 0 || mode > 7) {
            throw new IllegalArgumentException(
                    "Illegal value for text rendering mode. Expected: 0-7");
        }
        if (mode != this.textRenderingMode) {
            this.textRenderingMode = mode;
            g2d.currentStream.write(this.textRenderingMode + " Tr\n");
        }
    }
    
    /**
     * Sets the text rendering mode.
     * @param fill true if the text should be filled
     * @param stroke true if the text should be stroked
     * @param addToClip true if the path should be added for clipping
     */
    public void setTextRenderingMode(boolean fill, boolean stroke, boolean addToClip) {
        int mode;
        if (fill) {
            mode = (stroke ? 2 : 0);
        } else {
            mode = (stroke ? 1 : 3);
        }
        if (addToClip) {
            mode += 4;
        }
        setTextRenderingMode(mode);
    }
    
    /**
     * Writes a "Tm" command, setting a new text transformation matrix.
     * @param localTransform the new text transformation matrix
     */
    public void writeTextMatrix(AffineTransform localTransform) {
        StringBuffer sb = new StringBuffer();
        writeAffineTransform(localTransform, sb);
        sb.append(" Tm\n");
        g2d.currentStream.write(sb.toString());
    }

    /**
     * Selects a font from the font list suitable to display the given character.
     * @param ch the character
     * @return the recommended Font to use
     */
    public Font selectFontForChar(char ch) {
        for (int i = 0, c = fonts.length; i < c; i++) {
            if (fonts[i].hasChar(ch)) {
                return fonts[i];
            }
        }
        return fonts[0]; //TODO Maybe fall back to painting with shapes
    }
    
    /**
     * Writes a char to the "TJ-Buffer".
     * @param ch the unmapped character
     */
    public void writeTJChar(char ch) {
        if (bufTJ == null) {
            bufTJ = new StringBuffer();
        }
        if (bufTJ.length() == 0) {
            bufTJ.append("[").append(startText);
        }
        char mappedChar = font.mapChar(ch);
        writeChar(mappedChar, bufTJ);
    }

    /**
     * Writes a glyph adjust value to the "TJ-Buffer".
     * @param adjust the glyph adjust value in thousands of text unit space.
     */
    public void adjustGlyphTJ(double adjust) {
        bufTJ.append(endText).append(" ");
        bufTJ.append(PDFNumber.doubleOut(adjust, DEC - 4));
        bufTJ.append(" ");
        bufTJ.append(startText);
    }

    /**
     * Writes a "TJ" command, writing out the accumulated buffer with the characters and glyph
     * positioning values. The buffer is reset afterwards.
     */
    public void writeTJ() {
        if (bufTJ != null && bufTJ.length() > 0) {
            bufTJ.append(endText).append("] TJ\n");
            g2d.currentStream.write(bufTJ.toString());
            bufTJ.setLength(0);
        }
    }

}
