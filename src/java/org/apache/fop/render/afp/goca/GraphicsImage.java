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

/* $Id: $ */

package org.apache.fop.render.afp.goca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.render.afp.modca.AbstractStructuredAFPObject;
import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * A GOCA Image
 */
public class GraphicsImage extends AbstractStructuredAFPObject {

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
    protected void writeStart(OutputStream os) throws IOException {
        byte[] xcoord = BinaryUtils.convert(x, 2);
        byte[] ycoord = BinaryUtils.convert(y, 2);
        byte[] w = BinaryUtils.convert(width, 2);
        byte[] h = BinaryUtils.convert(height, 2);
        byte[] data = new byte[] {
            (byte) 0xD1, // GBIMG order code
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
        os.write(data);
    }

    /** the maximum image data length */
    public static final short MAX_DATA_LEN = 255;

    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        byte[] dataHeader = new byte[] {
            (byte) 0x92 // GIMD
        };
        final int lengthOffset = 1;
        writeChunksToStream(imageData, dataHeader, lengthOffset, MAX_DATA_LEN, os);
    }

    /** {@inheritDoc} */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[] {
            (byte) 0x93, // GEIMG order code
            0x00 // LENGTH
        };
        os.write(data);
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
