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

package org.apache.fop.afp.ptoca;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.fop.afp.fonts.CharactersetEncoder.EncodedChars;
import org.apache.xmlgraphics.java2d.color.CIELabColorSpace;
import org.apache.xmlgraphics.java2d.color.ColorUtil;
import org.apache.xmlgraphics.java2d.color.ColorWithAlternatives;

/**
 * Generator class for PTOCA data structures.
 */
public abstract class PtocaBuilder implements PtocaConstants {

    private ByteArrayOutputStream baout = new ByteArrayOutputStream(256);

    /** the current x coordinate. */
    private int currentX = -1;

    /** the current y coordinate */
    private int currentY = -1;

    /** the current font */
    private int currentFont = Integer.MIN_VALUE;

    /** the current orientation */
    private int currentOrientation = 0;

    /** the current color */
    private Color currentColor = Color.BLACK;

    /** the current variable space increment */
    private int currentVariableSpaceCharacterIncrement = 0;

    /** the current inter character adjustment */
    private int currentInterCharacterAdjustment = 0;


    /**
     * Returns an {@link OutputStream} for the next control sequence. This gives a subclass a
     * chance to do chunking of control sequences into multiple presentation text data objects.
     * @param length the length of the following control sequence
     * @return the output stream where the control sequence will be written to
     */
    protected abstract OutputStream getOutputStreamForControlSequence(int length);

    private static byte chained(byte functionType) {
        return (byte)(functionType | CHAIN_BIT);
    }

    private void newControlSequence() {
        baout.reset();
    }

    private void commit(byte functionType) throws IOException {
        int length = baout.size() + 2;
        assert length < 256;

        OutputStream out = getOutputStreamForControlSequence(length);
        out.write(length);
        out.write(functionType);
        baout.writeTo(out);
    }

    private void write(byte[] data, int offset, int length) {
        baout.write(data, offset, length);
    }

    private void writeByte(int data) {
        baout.write(data);
    }

    private void writeShort(int data) {
        baout.write((data >>> 8) & 0xFF);
        baout.write(data & 0xFF);
    }

    /**
     * Writes the introducer for a chained control sequence.
     * @throws IOException if an I/O error occurs
     */
    public void writeIntroducer() throws IOException {
        OutputStream out = getOutputStreamForControlSequence(ESCAPE.length);
        out.write(ESCAPE);
    }

    /**
     * The Set Coded Font Local control sequence activates a coded font and
     * specifies the character attributes to be used.
     * <p>
     * This is a modal control sequence.
     *
     * @param font The font local identifier.
     * @throws IOException if an I/O error occurs
     */
    public void setCodedFont(byte font) throws IOException {
        // Avoid unnecessary specification of the font
        if (currentFont == font) {
            return;
        } else {
            currentFont = font;
        }

        newControlSequence();
        writeByte(font);
        commit(chained(SCFL));
    }

    /**
     * Establishes the current presentation position on the baseline at a new
     * I-axis coordinate, which is a specified number of measurement units from
     * the B-axis. There is no change to the current B-axis coordinate.
     *
     * @param coordinate The coordinate for the inline move.
     * @throws IOException if an I/O error occurs
     */
    public void absoluteMoveInline(int coordinate) throws IOException {
        if (coordinate == this.currentX) {
            return;
        }
        newControlSequence();
        writeShort(coordinate);
        commit(chained(AMI));

        currentX = coordinate;
    }

    /**
     * Moves the inline coordinate of the presentation position relative to the current
     * inline position.
     * @param increment the increment in 1/1440 inch units
     * @throws IOException if an I/O error occurs
     */
    public void relativeMoveInline(int increment) throws IOException {
        newControlSequence();
        writeShort(increment);
        commit(chained(RMI));
    }

    /**
     * Establishes the baseline and the current presentation position at a new
     * B-axis coordinate, which is a specified number of measurement units from
     * the I-axis. There is no change to the current I-axis coordinate.
     *
     * @param coordinate The coordinate for the baseline move.
     * @throws IOException if an I/O error occurs
     */
    public void absoluteMoveBaseline(int coordinate) throws IOException {
        if (coordinate == this.currentY) {
            return;
        }
        newControlSequence();
        writeShort(coordinate);
        commit(chained(AMB));

        currentY = coordinate;
        currentX = -1;
    }

    private static final int TRANSPARENT_MAX_SIZE = 253;

    /**
     * The Transparent Data control sequence contains a sequence of code points
     * that are presented without a scan for embedded control sequences. If the data is larger
     * than fits in one chunk, additional chunks are automatically generated.
     *
     * @param data The text data to add.
     * @throws IOException if an I/O error occurs
     */
    public void addTransparentData(EncodedChars encodedChars) throws IOException {

        // data size greater than TRANSPARENT_MAX_SIZE, so slice
        int numTransData = encodedChars.getLength() / TRANSPARENT_DATA_MAX_SIZE;
        int currIndex = 0;
        for (int transDataCnt = 0; transDataCnt < numTransData; transDataCnt++) {
            addTransparentDataChunk(encodedChars, currIndex, TRANSPARENT_DATA_MAX_SIZE);
            currIndex += TRANSPARENT_DATA_MAX_SIZE;
        }
        int left = encodedChars.getLength() - currIndex;
        addTransparentDataChunk(encodedChars, currIndex, left);

    }



    private void addTransparentDataChunk(EncodedChars encodedChars, int offset, int length) throws IOException {
        newControlSequence();
        encodedChars.writeTo(baout, offset, length);
        commit(chained(TRN));
    }

    /**
     * Draws a line of specified length and specified width in the B-direction
     * from the current presentation position. The location of the current
     * presentation position is unchanged.
     *
     * @param length The length of the rule.
     * @param width The width of the rule.
     * @throws IOException if an I/O error occurs
     */
    public void drawBaxisRule(int length, int width) throws IOException {
        newControlSequence();
        writeShort(length); // Rule length
        writeShort(width); // Rule width
        writeByte(0); // Rule width fraction is always null. enough?
        commit(chained(DBR));
    }

    /**
     * Draws a line of specified length and specified width in the I-direction
     * from the current presentation position. The location of the current
     * presentation position is unchanged.
     *
     * @param length The length of the rule.
     * @param width The width of the rule.
     * @throws IOException if an I/O error occurs
     */
    public void drawIaxisRule(int length, int width) throws IOException {
        newControlSequence();
        writeShort(length); // Rule length
        writeShort(width); // Rule width
        writeByte(0); // Rule width fraction is always null. enough?
        commit(chained(DIR));
    }

    /**
     * The Set Text Orientation control sequence establishes the I-direction and
     * B-direction for the subsequent text. This is a modal control sequence.
     *
     * Semantics: This control sequence specifies the I-axis and B-axis
     * orientations with respect to the Xp-axis for the current Presentation
     * Text object. The orientations are rotational values expressed in degrees
     * and minutes.
     *
     * @param orientation The text orientation (0, 90, 180, 270).
     * @throws IOException if an I/O error occurs
     */
    public void setTextOrientation(int orientation) throws IOException {
        if (orientation == this.currentOrientation) {
            return;
        }
        newControlSequence();
        switch (orientation) {
        case 90:
            writeByte(0x2D);
            writeByte(0x00);
            writeByte(0x5A);
            writeByte(0x00);
            break;
        case 180:
            writeByte(0x5A);
            writeByte(0x00);
            writeByte(0x87);
            writeByte(0x00);
            break;
        case 270:
            writeByte(0x87);
            writeByte(0x00);
            writeByte(0x00);
            writeByte(0x00);
            break;
        default:
            writeByte(0x00);
            writeByte(0x00);
            writeByte(0x2D);
            writeByte(0x00);
            break;
        }
        commit(chained(STO));
        this.currentOrientation = orientation;
        currentX = -1;
        currentY = -1;
    }

    /**
     * The Set Extended Text Color control sequence specifies a color value and
     * defines the color space and encoding for that value. The specified color
     * value is applied to foreground areas of the text presentation space.
     * <p>
     * This is a modal control sequence.
     *
     * @param col The color to be set.
     * @throws IOException if an I/O error occurs
     */
    public void setExtendedTextColor(Color col) throws IOException {
        if (ColorUtil.isSameColor(col, currentColor)) {
            return;
        }
        if (col instanceof ColorWithAlternatives) {
            ColorWithAlternatives cwa = (ColorWithAlternatives)col;
            Color alt = cwa.getFirstAlternativeOfType(ColorSpace.TYPE_CMYK);
            if (alt != null) {
                col = alt;
            }
        }
        ColorSpace cs = col.getColorSpace();

        newControlSequence();
        if (col.getColorSpace().getType() == ColorSpace.TYPE_CMYK) {
            writeByte(0x00); // Reserved; must be zero
            writeByte(0x04); // Color space - 0x04 = CMYK
            writeByte(0x00); // Reserved; must be zero
            writeByte(0x00); // Reserved; must be zero
            writeByte(0x00); // Reserved; must be zero
            writeByte(0x00); // Reserved; must be zero
            writeByte(8); // Number of bits in component 1
            writeByte(8); // Number of bits in component 2
            writeByte(8); // Number of bits in component 3
            writeByte(8); // Number of bits in component 4
            float[] comps = col.getColorComponents(null);
            assert comps.length == 4;
            for (int i = 0; i < 4; i++) {
                int component = Math.round(comps[i] * 255);
                writeByte(component);
            }
        } else if (cs instanceof CIELabColorSpace) {
            writeByte(0x00); // Reserved; must be zero
            writeByte(0x08); // Color space - 0x08 = CIELAB
            writeByte(0x00); // Reserved; must be zero
            writeByte(0x00); // Reserved; must be zero
            writeByte(0x00); // Reserved; must be zero
            writeByte(0x00); // Reserved; must be zero
            writeByte(8); // Number of bits in component 1
            writeByte(8); // Number of bits in component 2
            writeByte(8); // Number of bits in component 3
            writeByte(0); // Number of bits in component 4
            //Sadly, 16 bit components don't seem to work
            float[] colorComponents = col.getColorComponents(null);
            int l = Math.round(colorComponents[0] * 255f);
            int a = Math.round(colorComponents[1] * 255f) - 128;
            int b = Math.round(colorComponents[2] * 255f) - 128;
            writeByte(l); // L*
            writeByte(a); // a*
            writeByte(b); // b*
        } else {
            writeByte(0x00); // Reserved; must be zero
            writeByte(0x01); // Color space - 0x01 = RGB
            writeByte(0x00); // Reserved; must be zero
            writeByte(0x00); // Reserved; must be zero
            writeByte(0x00); // Reserved; must be zero
            writeByte(0x00); // Reserved; must be zero
            writeByte(8); // Number of bits in component 1
            writeByte(8); // Number of bits in component 2
            writeByte(8); // Number of bits in component 3
            writeByte(0); // Number of bits in component 4
            writeByte(col.getRed()); // Red intensity
            writeByte(col.getGreen()); // Green intensity
            writeByte(col.getBlue()); // Blue intensity
        }
        commit(chained(SEC));
        this.currentColor = col;
    }

    /**
     * Sets the variable space character increment.
     * <p>
     * This is a modal control sequence.
     *
     * @param incr The increment to be set (positive integer, 1/1440 inch)
     * @throws IOException if an I/O error occurs
     */
    public void setVariableSpaceCharacterIncrement(int incr) throws IOException {
        if (incr == this.currentVariableSpaceCharacterIncrement) {
            return;
        }
        assert incr >= 0 && incr < (1 << 16);
        newControlSequence();
        writeShort(Math.abs(incr)); //Increment
        commit(chained(SVI));

        this.currentVariableSpaceCharacterIncrement = incr;
    }

    /**
     * Sets the intercharacter adjustment (additional increment or decrement between graphic
     * characters).
     * <p>
     * This is a modal control sequence.
     *
     * @param incr The increment to be set (1/1440 inch)
     * @throws IOException if an I/O error occurs
     */
    public void setInterCharacterAdjustment(int incr) throws IOException {
        if (incr == this.currentInterCharacterAdjustment) {
            return;
        }
        assert incr >= Short.MIN_VALUE && incr <= Short.MAX_VALUE;
        newControlSequence();
        writeShort(Math.abs(incr)); //Increment
        writeByte(incr >= 0 ? 0 : 1); // Direction
        commit(chained(SIA));

        this.currentInterCharacterAdjustment = incr;
    }

    /**
     * A control sequence is a sequence of bytes that specifies a control
     * function. A control sequence consists of a control sequence introducer
     * and zero or more parameters. The control sequence can extend multiple
     * presentation text data objects, but must eventually be terminated. This
     * method terminates the control sequence (by using a NOP command).
     *
     * @throws IOException if an I/O error occurs
     */
    public void endChainedControlSequence() throws IOException {
        newControlSequence();
        commit(NOP);
    }
}
