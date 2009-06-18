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

package org.apache.fop.pdf;

import java.awt.geom.AffineTransform;

/**
 * Utility class for generating PDF text objects. It needs to be subclassed to add writing
 * functionality (see {@link #write(String)}).
 */
public abstract class PDFTextUtil {

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

    private boolean inTextObject = false;
    private String startText;
    private String endText;
    private boolean useMultiByte;
    private StringBuffer bufTJ;
    private int textRenderingMode = TR_FILL;

    private String currentFontName;
    private double currentFontSize;

    /**
     * Main constructor.
     */
    public PDFTextUtil() {
        //nop
    }

    /**
     * Writes PDF code.
     * @param code the PDF code to write
     */
    protected abstract void write(String code);

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
            if (ch < 32 || ch > 127) {
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
     * Indicates whether we are in a text object or not.
     * @return true if we are in a text object
     */
    public boolean isInTextObject() {
        return inTextObject;
    }

    /**
     * Called when a new text object should be started. Be sure to call setFont() before
     * issuing any text painting commands.
     */
    public void beginTextObject() {
        if (inTextObject) {
            throw new IllegalStateException("Already in text object");
        }
        write("BT\n");
        this.inTextObject = true;
    }

    /**
     * Called when a text object should be ended.
     */
    public void endTextObject() {
        checkInTextObject();
        write("ET\n");
        this.inTextObject = false;
        initValues();
    }

    /**
     * Resets the state fields.
     */
    protected void initValues() {
        this.currentFontName = null;
        this.currentFontSize = 0.0;
        this.textRenderingMode = TR_FILL;
    }

    /**
     * Creates a "q" command, pushing a copy of the entire graphics state onto the stack.
     */
    public void saveGraphicsState() {
        write("q\n");
    }

    /**
     * Creates a "Q" command, restoring the entire graphics state to its former value by popping
     * it from the stack.
     */
    public void restoreGraphicsState() {
        write("Q\n");
    }

    /**
     * Creates a "cm" command.
     * @param at the transformation matrix
     */
    public void concatMatrix(AffineTransform at) {
        if (!at.isIdentity()) {
            writeTJ();
            StringBuffer sb = new StringBuffer();
            writeAffineTransform(at, sb);
            sb.append(" cm\n");
            write(sb.toString());
        }
    }

    /**
     * Writes a "Tf" command, setting a new current font.
     * @param fontName the name of the font to select
     * @param fontSize the font size (in points)
     */
    public void writeTf(String fontName, double fontSize) {
        checkInTextObject();
        write("/" + fontName + " " + PDFNumber.doubleOut(fontSize) + " Tf\n");

        this.startText = useMultiByte ? "<" : "(";
        this.endText = useMultiByte ? ">" : ")";
    }

    /**
     * Updates the current font. This method only writes a "Tf" if the current font changes.
     * @param fontName the name of the font to select
     * @param fontSize the font size (in points)
     * @param multiByte true indicates the font is a multi-byte font, false means single-byte
     */
    public void updateTf(String fontName, double fontSize, boolean multiByte) {
        checkInTextObject();
        if (!fontName.equals(this.currentFontName) || (fontSize != this.currentFontSize)) {
            writeTJ();
            this.currentFontName = fontName;
            this.currentFontSize = fontSize;
            this.useMultiByte = multiByte;
            writeTf(fontName, fontSize);
        }
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
            writeTJ();
            this.textRenderingMode = mode;
            write(this.textRenderingMode + " Tr\n");
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
        sb.append(" Tm ");
        write(sb.toString());
    }

    /**
     * Writes a char to the "TJ-Buffer".
     * @param codepoint the mapped character (code point/character code)
     */
    public void writeTJMappedChar(char codepoint) {
        if (bufTJ == null) {
            bufTJ = new StringBuffer();
        }
        if (bufTJ.length() == 0) {
            bufTJ.append("[").append(startText);
        }
        writeChar(codepoint, bufTJ);
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
        if (isInString()) {
            bufTJ.append(endText).append("] TJ\n");
            write(bufTJ.toString());
            bufTJ.setLength(0);
        }
    }

    private boolean isInString() {
        return bufTJ != null && bufTJ.length() > 0;
    }

}
