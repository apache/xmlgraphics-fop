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

package org.apache.fop.util.bitmap;

import java.awt.Color;

/**
 * Utility methods for dithering.
 */
public class DitherUtil {

    /** Selects a 2x2 Bayer dither matrix (5 grayscales) */
    public static final int DITHER_MATRIX_2X2 = 2;
    /** Selects a 4x4 Bayer dither matrix (17 grayscales) */
    public static final int DITHER_MATRIX_4X4 = 4;
    /** Selects a 8x8 Bayer dither matrix (65 grayscales) */
    public static final int DITHER_MATRIX_8X8 = 8;

    //Bayer dither matrices (4x4 and 8x8 are derived from the 2x2 matrix)
    private static final int[] BAYER_D2 = new int[] {0, 2, 3, 1};
    private static final int[] BAYER_D4;
    private static final int[] BAYER_D8;

    static {
        BAYER_D4 = deriveBayerMatrix(BAYER_D2);
        BAYER_D8 = deriveBayerMatrix(BAYER_D4);
    }

    private static int[] deriveBayerMatrix(int[] d) {
        int[] dn = new int[d.length * 4];
        int half = (int)Math.sqrt(d.length);
        for (int part = 0; part < 4; part++) {
            for (int i = 0, c = d.length; i < c; i++) {
                setValueInMatrix(dn, half, part, i, d[i] * 4 + BAYER_D2[part]);
            }
        }
        return dn;
    }

    private static void setValueInMatrix(int[] dn, int half, int part, int idx, int value) {
        int xoff = (part & 1) * half;
        int yoff = (part & 2) * half * half;
        int matrixIndex = yoff + ((idx / half) * half * 2) + (idx % half) + xoff;
        dn[matrixIndex] = value;
    }

    /**
     * Returns the Bayer dither base pattern for a particular matrix size.
     * @param matrix the matrix size ({@link #DITHER_MATRIX_2X2}, {@link #DITHER_MATRIX_4X4}
     *                                   or {@link #DITHER_MATRIX_8X8})
     * @return the base pattern for the given size
     */
    public static int[] getBayerBasePattern(int matrix) {
        int[] result = new int[matrix * matrix];
        switch (matrix) {
        case DITHER_MATRIX_2X2:
            System.arraycopy(BAYER_D2, 0, result, 0, BAYER_D2.length);
            break;
        case DITHER_MATRIX_4X4:
            System.arraycopy(BAYER_D4, 0, result, 0, BAYER_D4.length);
            break;
        case DITHER_MATRIX_8X8:
            System.arraycopy(BAYER_D8, 0, result, 0, BAYER_D8.length);
            break;
        default:
            throw new IllegalArgumentException("Unsupported dither matrix: " + matrix);
        }
        return result;
    }

    /**
     * Returns a byte array containing the dither pattern for the given 8-bit gray value.
     * @param matrix the matrix size ({@link #DITHER_MATRIX_2X2}, {@link #DITHER_MATRIX_4X4}
     *                                   or {@link #DITHER_MATRIX_8X8})
     * @param gray255 the gray value (0-255)
     * @param doubleMatrix true if the 4x4 matrix shall be doubled to a 8x8
     * @return the dither pattern
     */
    public static byte[] getBayerDither(int matrix, int gray255, boolean doubleMatrix) {
        int ditherIndex;
        byte[] dither;
        int[] bayer;
        switch (matrix) {
        case DITHER_MATRIX_4X4:
            ditherIndex = gray255 * 17 / 255;
            bayer = BAYER_D4;
            break;
        case DITHER_MATRIX_8X8:
            ditherIndex = gray255 * 65 / 255;
            bayer = BAYER_D8;
            break;
        default:
            throw new IllegalArgumentException("Unsupported dither matrix: " + matrix);
        }
        if (doubleMatrix) {
            if (doubleMatrix && (matrix != DITHER_MATRIX_4X4)) {
                throw new IllegalArgumentException("doubleMatrix=true is only allowed for 4x4");
            }
            dither = new byte[bayer.length / 8 * 4];
            for (int i = 0, c = bayer.length; i < c; i++) {
                boolean dot = !(bayer[i] < ditherIndex - 1);
                if (dot) {
                    int byteIdx = i / 4;
                    dither[byteIdx] |= 1 << (i % 4);
                    dither[byteIdx] |= 1 << ((i % 4) + 4);
                    dither[byteIdx + 4] |= 1 << (i % 4);
                    dither[byteIdx + 4] |= 1 << ((i % 4) + 4);
                }
            }
        } else {
            dither = new byte[bayer.length / 8];
            for (int i = 0, c = bayer.length; i < c; i++) {
                boolean dot = !(bayer[i] < ditherIndex - 1);
                if (dot) {
                    int byteIdx = i / 8;
                    dither[byteIdx] |= 1 << (i % 8);
                }
            }
        }
        return dither;
    }

    /**
     * Returns a byte array containing the dither pattern for the given 8-bit gray value.
     * @param matrix the matrix size ({@link #DITHER_MATRIX_2X2}, {@link #DITHER_MATRIX_4X4}
     *                                   or {@link #DITHER_MATRIX_8X8})
     * @param col the color
     * @param doubleMatrix true if the 4x4 matrix shall be doubled to a 8x8
     * @return the dither pattern
     */
    public static byte[] getBayerDither(int matrix, Color col, boolean doubleMatrix) {
        float black = BitmapImageUtil.convertToGray(col.getRGB()) / 256f;
        return getBayerDither(matrix, Math.round(black * 256), doubleMatrix);
    }

}
