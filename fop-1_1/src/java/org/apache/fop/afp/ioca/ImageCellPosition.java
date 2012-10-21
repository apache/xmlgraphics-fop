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

package org.apache.fop.afp.ioca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.afp.modca.AbstractAFPObject;
import org.apache.fop.afp.util.BinaryUtils;

/**
 * The IM Image Cell Position structured field specifies the placement,
 * size, and replication of IM image cells.
 */
public class ImageCellPosition extends AbstractAFPObject {

    /** offset of image cell in X direction */
    private int xOffset = 0;

    /** offset of image cell in Y direction */
    private int yOffset = 0;

    /** size of image cell in X direction */
    private final byte[] xSize = new byte[] {(byte)0xFF, (byte)0xFF};

    /** size of image cell in Y direction */
    private final byte[] ySize = new byte[] {(byte)0xFF, (byte)0xFF};

    /** size of fill rectangle in X direction */
    private final byte[] xFillSize = new byte[] {(byte)0xFF, (byte)0xFF};

    /** size of fill rectangle in Y direction */
    private final byte[] yFillSize = new byte[] {(byte)0xFF, (byte)0xFF};

    /**
     * Main Constructor
     *
     * @param x The offset of image cell in X direction
     * @param y The offset of image cell in Y direction
     */
    public ImageCellPosition(int x, int y) {
        xOffset = x;
        yOffset = y;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = new byte[21];
        copySF(data, Type.POSITION, Category.IM_IMAGE);

        data[1] = 0x00; // length
        data[2] = 0x14;

        /**
         * Specifies the offset along the Xp direction, in image points,
         * of this image cell from the IM image object area origin.
         */
        byte[] x1 = BinaryUtils.convert(xOffset, 2);
        data[9] = x1[0];
        data[10] = x1[1];

        /**
         * Specifies the offset along the Yp direction, in image points,
         * of this image cell from the IM image object area origin.
         */
        byte[] x2 = BinaryUtils.convert(yOffset, 2);
        data[11] = x2[0];
        data[12] = x2[1];

        data[13] = xSize[0];
        data[14] = xSize[1];

        data[15] = ySize[0];
        data[16] = ySize[1];

        data[17] = xFillSize[0];
        data[18] = xFillSize[1];

        data[19] = yFillSize[0];
        data[20] = yFillSize[1];

        os.write(data);
    }

    /**
     * Specifies the extent in the X direction, in image points,
     * of this image cell. A value of X'FFFF' indicates that the
     * default extent specified in bytes 28 and 29 of the Image
     * Input Descriptor (IID) is to be used.
     *
     * @param xcSize The size to set.
     */
    public void setXSize(int xcSize) {
        byte[] x = BinaryUtils.convert(xcSize, 2);
        xSize[0] = x[0];
        xSize[1] = x[1];
    }

    /**
     * Specifies the extent of the fill rectangle in the X direction,
     * in image points. This value can be smaller than, equal to, or
     * larger than the image cell extent in the X direction (XCSize).
     * A value of X'FFFF' indicates that the image cell X-extent should
     * be used as the fill rectangle X-extent. The fill rectangle is
     * filled in the X direction by repeating the image cell in the
     * X direction. The image cell can be truncated to fit the rectangle.
     *
     * @param size The size to set.
     */
    public void setXFillSize(int size) {
        byte[] x = BinaryUtils.convert(size, 2);
        this.xFillSize[0] = x[0];
        this.xFillSize[1] = x[1];
    }

    /**
     * Specifies the extent in the Y direction, in image points,
     * of this image cell. A value of X'FFFF' indicates that the
     * default extent specified in bytes 30 and 31 of the Image
     * Input Descriptor (IID) is to be used.
     *
     * @param size The size to set.
     */
    public void setYSize(int size) {
        byte[] x = BinaryUtils.convert(size, 2);
        this.ySize[0] = x[0];
        this.ySize[1] = x[1];
    }

    /**
     * Specifies the extent of the fill rectangle in the Y direction,
     * in image points. This value can be smaller than, equal to, or
     * larger than the image cell extent in the Y direction (YCSize).
     * A value of X'FFFF' indicates that the image cell Y-extent should
     * be used as the fill rectangle Y-extent. The fill rectangle is
     * filled in the Y direction by repeating the image cell in the
     * Y direction. The image cell can be truncated to fit the rectangle.
     *
     * @param size The size to set.
     */
    public void setYFillSize(int size) {
        byte[] x = BinaryUtils.convert(size, 2);
        this.yFillSize[0] = x[0];
        this.yFillSize[1] = x[1];
    }
}
