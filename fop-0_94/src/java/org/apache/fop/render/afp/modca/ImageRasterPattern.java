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

package org.apache.fop.render.afp.modca;

/**
 * Raster data is a grid of cells covering an area of interest.
 * Each pixel, the smallest unit of information in the grid, displays
 * a unique attribute. This static class generates raster data for different
 * shades of grey (betweeen 0 and 16) the lower the number being the
 * darker the shade. The image data dimensions are 64 x 8.
 */
public class ImageRasterPattern {

    /**
     * The Raster Pattern for Greyscale 16
     */
    private static final byte[] GREYSCALE16 = new byte[] {
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
    };

    /**
     * The Raster Pattern for Greyscale 15
     */
    private static final byte[] GREYSCALE15 = new byte[] {
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
    };

    /**
     * The Raster Pattern for Greyscale 14
     */
    private static final byte[] GREYSCALE14 = new byte[] {
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x44,
            0x44,
            0x44,
            0x44,
            0x44,
            0x44,
            0x44,
            0x44,
    };


    /**
     * The Raster Pattern for Greyscale 13
     */
    private static final byte[] GREYSCALE13 = new byte[] {
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
    };

    /**
     * The Raster Pattern for Greyscale 12
     */
    private static final byte[] GREYSCALE12 = new byte[] {
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
    };

    /**
     * The Raster Pattern for Greyscale 11
     */
    private static final byte[] GREYSCALE11 = new byte[] {
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x44,
            0x44,
            0x44,
            0x44,
            0x44,
            0x44,
            0x44,
            0x44,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
    };

    /**
     * The Raster Pattern for Greyscale 10
     */
    private static final byte[] GREYSCALE10 = new byte[] {
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            0x44,
            0x44,
            0x44,
            0x44,
            0x44,
            0x44,
            0x44,
            0x44,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
    };

    /**
     * The Raster Pattern for Greyscale 9
     */
    private static final byte[] GREYSCALE09 = new byte[] {
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            0x11,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
    };

    /**
     * The Raster Pattern for Greyscale 8
     */
    private static final byte[] GREYSCALE08 = new byte[] {
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
    };


    /**
     * The Raster Pattern for Greyscale 7
     */
    private static final byte[] GREYSCALE07 = new byte[] {
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
    };


    /**
     * The Raster Pattern for Greyscale 6
     */
    private static final byte[] GREYSCALE06 = new byte[] {
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            (byte)0xEE,
            (byte)0xEE,
            (byte)0xEE,
            (byte)0xEE,
            (byte)0xEE,
            (byte)0xEE,
            (byte)0xEE,
            (byte)0xEE,
    };

    /**
     * The Raster Pattern for Greyscale 5
     */
    private static final byte[] GREYSCALE05 = new byte[] {
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xEE,
            (byte)0xEE,
            (byte)0xEE,
            (byte)0xEE,
            (byte)0xEE,
            (byte)0xEE,
            (byte)0xEE,
            (byte)0xEE,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
    };


    /**
     * The Raster Pattern for Greyscale 4
     */
    private static final byte[] GREYSCALE04 = new byte[] {
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xAA,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
    };

    /**
     * The Raster Pattern for Greyscale 3
     */
    private static final byte[] GREYSCALE03 = new byte[] {
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            0x55,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xBB,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
    };

    /**
     * The Raster Pattern for Greyscale 2
     */
    private static final byte[] GREYSCALE02 = new byte[] {
            0x77,
            0x77,
            0x77,
            0x77,
            0x77,
            0x77,
            0x77,
            0x77,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xDD,
            (byte)0xDD,
            (byte)0xDD,
            (byte)0xDD,
            (byte)0xDD,
            (byte)0xDD,
            (byte)0xDD,
            (byte)0xDD,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
    };


    /**
     * The Raster Pattern for Greyscale 1
     */
    private static final byte[] GREYSCALE01 = new byte[] {
            0x77,
            0x77,
            0x77,
            0x77,
            0x77,
            0x77,
            0x77,
            0x77,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
    };


    /**
     * The Raster Pattern for Greyscale 00
     */
    private static final byte[] GREYSCALE00 = new byte[] {
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
    };

    /**
     * Static method to return the raster image data for the
     * grey scale specified. The scale should be between 0 (darkest)
     * and 16 (lightest).
     * @param greyscale The grey scale value (0 - 16)
     */
    public static byte[] getRasterData(int greyscale) {

        int repeat = 16;

        byte[] greypattern = new byte[32];
        byte[] rasterdata = new byte[32 * repeat];

        switch (greyscale) {
            case 0:
                System.arraycopy(GREYSCALE00, 0, greypattern, 0, 32);
                break;
            case 1:
                System.arraycopy(GREYSCALE01, 0, greypattern, 0, 32);
                break;
            case 2:
                System.arraycopy(GREYSCALE02, 0, greypattern, 0, 32);
                break;
            case 3:
                System.arraycopy(GREYSCALE03, 0, greypattern, 0, 32);
                break;
            case 4:
                System.arraycopy(GREYSCALE04, 0, greypattern, 0, 32);
                break;
            case 5:
                System.arraycopy(GREYSCALE05, 0, greypattern, 0, 32);
                break;
            case 6:
                System.arraycopy(GREYSCALE06, 0, greypattern, 0, 32);
                break;
            case 7:
                System.arraycopy(GREYSCALE07, 0, greypattern, 0, 32);
                break;
            case 8:
                System.arraycopy(GREYSCALE08, 0, greypattern, 0, 32);
                break;
            case 9:
                System.arraycopy(GREYSCALE09, 0, greypattern, 0, 32);
                break;
            case 10:
                System.arraycopy(GREYSCALE10, 0, greypattern, 0, 32);
                break;
            case 11:
                System.arraycopy(GREYSCALE11, 0, greypattern, 0, 32);
                break;
            case 12:
                System.arraycopy(GREYSCALE12, 0, greypattern, 0, 32);
                break;
            case 13:
                System.arraycopy(GREYSCALE13, 0, greypattern, 0, 32);
                break;
            case 14:
                System.arraycopy(GREYSCALE14, 0, greypattern, 0, 32);
                break;
            case 15:
                System.arraycopy(GREYSCALE15, 0, greypattern, 0, 32);
                break;
            case 16:
                System.arraycopy(GREYSCALE16, 0, greypattern, 0, 32);
                break;
            default :
                System.arraycopy(GREYSCALE00, 0, greypattern, 0, 32);
                break;
        }

        for (int i = 0; i < repeat; i++) {

            System.arraycopy(greypattern, 0, rasterdata, i * 32, 32);

        }

        return rasterdata;

    }

}
