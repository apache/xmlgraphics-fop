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

package org.apache.fop.render;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.apache.xmlgraphics.image.loader.ImageSize;

public final class RawPNGTestUtil {

    private static final int NUM_ROWS = 32;
    private static final int NUM_COLUMNS = 32;
    private static final int DPI = 72;

    private RawPNGTestUtil() {

    }

    /**
     * Builds a PNG IDAT section for a square of a given color and alpha; the filter is fixed.
     * @param gray the gray color; set to -1 if using RGB
     * @param red the red color; ignored if gray > -1
     * @param green the green color; ignored if gray > -1
     * @param blue the blue color; ignored if gray > -1
     * @param alpha the alpha color; set to -1 if not present
     * @return the PNG IDAT byte array
     * @throws IOException
     */
    public static byte[] buildGRGBAData(int gray, int red, int green, int blue, int alpha) throws IOException {
        // build an image, 32x32, Gray or RGB, with or without alpha, and with filter
        int filter = 0;
        int numRows = NUM_ROWS;
        int numColumns = NUM_COLUMNS;
        int numComponents = (gray > -1 ? 1 : 3) + (alpha > -1 ? 1 : 0);
        int numBytesPerRow = numColumns * numComponents + 1; // 1 for filter
        int numBytes = numRows * numBytesPerRow;
        byte[] data = new byte[numBytes];
        for (int r = 0; r < numRows; r++) {
            data[r * numBytesPerRow] = (byte) filter;
            for (int c = 0; c < numColumns; c++) {
                if (numComponents == 1) {
                    data[r * numBytesPerRow + numComponents * c + 1] = (byte) gray;
                } else if (numComponents == 2) {
                    data[r * numBytesPerRow + numComponents * c + 1] = (byte) gray;
                    data[r * numBytesPerRow + numComponents * c + 2] = (byte) alpha;
                } else if (numComponents == 3) {
                    data[r * numBytesPerRow + numComponents * c + 1] = (byte) red;
                    data[r * numBytesPerRow + numComponents * c + 2] = (byte) green;
                    data[r * numBytesPerRow + numComponents * c + 3] = (byte) blue;
                } else if (numComponents == 4) {
                    data[r * numBytesPerRow + numComponents * c + 1] = (byte) red;
                    data[r * numBytesPerRow + numComponents * c + 2] = (byte) green;
                    data[r * numBytesPerRow + numComponents * c + 3] = (byte) blue;
                    data[r * numBytesPerRow + numComponents * c + 4] = (byte) alpha;
                }
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos, new Deflater());
        dos.write(data);
        dos.close();
        return baos.toByteArray();
    }

    /**
     *
     * @return a default ImageSize
     */
    public static ImageSize getImageSize() {
        return new ImageSize(NUM_ROWS, NUM_COLUMNS, DPI);
    }
}
