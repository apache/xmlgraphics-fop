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

import org.apache.fop.afp.util.BinaryUtils;

/**
 * A GOCA Image
 */
public class GraphicsImage extends AbstractGraphicsDrawingOrder {

    /** the maximum image data length */
    public static final short MAX_DATA_LEN = 255;

    /** x coordinate */
    private final int x;

    /** y coordinate */
    private final int y;

    /** width */
    private final int width;

    /** height */
    private final int height;

    /** image data */
    private final byte[] imageData;

    /**
     * Main constructor
     *
     * @param x the x coordinate of the image
     * @param y the y coordinate of the image
     * @param width the image width
     * @param height the image height
     * @param imageData the image data
     */
    public GraphicsImage(int x, int y, int width, int height, byte[] imageData) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.imageData = imageData;
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        //TODO:
        return 0;
    }

    byte getOrderCode() {
        return (byte)0xD1;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] xcoord = BinaryUtils.convert(x, 2);
        byte[] ycoord = BinaryUtils.convert(y, 2);
        byte[] w = BinaryUtils.convert(width, 2);
        byte[] h = BinaryUtils.convert(height, 2);
        byte[] startData = new byte[] {
            getOrderCode(), // GBIMG order code
            (byte) 0x0A, // LENGTH
            xcoord[0],
            xcoord[1],
            ycoord[0],
            ycoord[1],
            0x00, // FORMAT
            0x00, // RES
            w[0], // WIDTH
            w[1], //
            h[0], // HEIGHT
            h[1] //
        };
        os.write(startData);

        byte[] dataHeader = new byte[] {
            (byte) 0x92 // GIMD
        };
        final int lengthOffset = 1;
        writeChunksToStream(imageData, dataHeader, lengthOffset, MAX_DATA_LEN, os);

        byte[] endData = new byte[] {
            (byte) 0x93, // GEIMG order code
            0x00 // LENGTH
        };
        os.write(endData);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "GraphicsImage{x=" + x
            + ", y=" + y
            + ", width=" + width
            + ", height=" + height
        + "}";
    }
}
