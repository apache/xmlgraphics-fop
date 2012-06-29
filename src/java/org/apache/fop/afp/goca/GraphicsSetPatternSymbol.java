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

package org.apache.fop.afp.goca;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Sets the pattern symbol to use when filling following GOCA structured fields
 */
public class GraphicsSetPatternSymbol extends AbstractGraphicsDrawingOrder {

    /** dotted density 1 */
    public static final byte DOTTED_DENSITY_1 = 0x01;

    /** dotted density 2 */
    public static final byte DOTTED_DENSITY_2 = 0x02;

    /** dotted density 3 */
    public static final byte DOTTED_DENSITY_3 = 0x03;

    /** dotted density 4 */
    public static final byte DOTTED_DENSITY_4 = 0x04;

    /** dotted density 5 */
    public static final byte DOTTED_DENSITY_5 = 0x05;

    /** dotted density 6 */
    public static final byte DOTTED_DENSITY_6 = 0x06;

    /** dotted density 7 */
    public static final byte DOTTED_DENSITY_7 = 0x07;

    /** dotted density 8 */
    public static final byte DOTTED_DENSITY_8 = 0x08;

    /** dotted density 9 */
    public static final byte VERTICAL_LINES = 0x09;

    /** horizontal lines */
    public static final byte HORIZONTAL_LINES = 0x0A;

    /** diagonal lines, bottom left to top right 1 */
    public static final byte DIAGONAL_LINES_BLTR_1 = 0x0B;

    /** diagonal lines, bottom left to top right 2 */
    public static final byte DIAGONAL_LINES_BLTR_2 = 0x0C;

    /** diagonal lines, top left to bottom right 1 */
    public static final byte DIAGONAL_LINES_TLBR_1 = 0x0D;

    /** diagonal lines, top left to bottom right 2 */
    public static final byte DIAGONAL_LINES_TLBR_2 = 0x0E;

    /** no fill */
    public static final byte NO_FILL = 0x0F;

    /** solid fill */
    public static final byte SOLID_FILL = 0x10;

    /** blank (same as no fill) */
    public static final byte BLANK = 0x40; // processed same as NO_FILL

    /** the graphics pattern symbol to use */
    private final byte pattern;

    /**
     * Main constructor
     *
     * @param pattern the pattern symbol to use
     */
    public GraphicsSetPatternSymbol(byte pattern) {
        this.pattern = pattern;
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 2;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = new byte[] {
            getOrderCode(), // GSPT order code
            pattern
        };
        os.write(data);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "GraphicsSetPatternSymbol(fill="
            + (pattern == SOLID_FILL ? true : false)  + ")";
    }

    /** {@inheritDoc} */
    byte getOrderCode() {
        return 0x28;
    }
}
