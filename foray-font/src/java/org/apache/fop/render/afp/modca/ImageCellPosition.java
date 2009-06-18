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

import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * The IM Image Cell Position structured field specifies the placement,
 * size, and replication of IM image cells.
 */
public class ImageCellPosition extends AbstractAFPObject {

    /**
     * Offset of image cell in X direction
     */
    private int _XcoSet = 0;

    /**
     * Offset of image cell in Y direction
     */
    private int _YcoSet = 0;

    /**
     * Size of image cell in X direction
     */
    private byte[] _XcSize = new byte[] { (byte)0xFF, (byte)0xFF };

    /**
     * Size of image cell in Y direction
     */
    private byte[] _YcSize = new byte[] { (byte)0xFF, (byte)0xFF };

    /**
     * Size of fill rectangle in X direction
     */
    private byte[] _XFillSize = new byte[] { (byte)0xFF, (byte)0xFF };

    /**
     * Size of fill rectangle in Y direction
     */
    private byte[] _YFillSize = new byte[] { (byte)0xFF, (byte)0xFF };

    /**
     * Constructor for the ImageCellPosition
     * @param x The offset of image cell in X direction
     * @param y The offset of image cell in Y direction
     */
    public ImageCellPosition(int x, int y) {

        _XcoSet = x;
        _YcoSet = y;

    }

    /**
     * Accessor method to write the AFP datastream for the Image Cell Position
     * @param os The stream to write to
     * @throws java.io.IOException
     */
    public void writeDataStream(OutputStream os)
        throws IOException {

        byte[] data = new byte[21];

        data[0] = 0x5A;

        data[1] = 0x00;
        data[2] = 0x14;

        data[3] = (byte) 0xD3;
        data[4] = (byte) 0xAC;
        data[5] = (byte) 0x7B;
        data[6] = 0x00;
        data[7] = 0x00;
        data[8] = 0x00;

        /**
         * Specifies the offset along the Xp direction, in image points,
         * of this image cell from the IM image object area origin.
         */
        byte[] x1 = BinaryUtils.convert(_XcoSet, 2);
        data[9] = x1[0];
        data[10] = x1[1];

        /**
         * Specifies the offset along the Yp direction, in image points,
         * of this image cell from the IM image object area origin.
         */
        byte[] x2 = BinaryUtils.convert(_YcoSet, 2);
        data[11] = x2[0];
        data[12] = x2[1];

        data[13] = _XcSize[0];
        data[14] = _XcSize[1];

        data[15] = _YcSize[0];
        data[16] = _YcSize[1];

        data[17] = _XFillSize[0];
        data[18] = _XFillSize[1];

        data[19] = _YFillSize[0];
        data[20] = _YFillSize[1];

        os.write(data);

    }

    /**
     * Specifies the extent in the X direction, in image points,
     * of this image cell. A value of X'FFFF' indicates that the
     * default extent specified in bytes 28 and 29 of the Image
     * Input Descriptor (IID) is to be used.
     * @param xcSize The size to set.
     */
    public void setXSize(int xcSize) {

        byte[] x = BinaryUtils.convert(xcSize, 2);
        _XcSize[0] = x[0];
        _XcSize[1] = x[1];

    }

    /**
     * Specifies the extent of the fill rectangle in the X direction,
     * in image points. This value can be smaller than, equal to, or
     * larger than the image cell extent in the X direction (XCSize).
     * A value of X'FFFF' indicates that the image cell X-extent should
     * be used as the fill rectangle X-extent. The fill rectangle is
     * filled in the X direction by repeating the image cell in the
     * X direction. The image cell can be truncated to fit the rectangle.
     * @param xFillSize The size to set.
     */
    public void setXFillSize(int xFillSize) {

        byte[] x = BinaryUtils.convert(xFillSize, 2);
        _XFillSize[0] = x[0];
        _XFillSize[1] = x[1];

    }

    /**
     * Specifies the extent in the Y direction, in image points,
     * of this image cell. A value of X'FFFF' indicates that the
     * default extent specified in bytes 30 and 31 of the Image
     * Input Descriptor (IID) is to be used.
     * @param ycSize The size to set.
     */
    public void setYSize(int ycSize) {

        byte[] x = BinaryUtils.convert(ycSize, 2);
        _YcSize[0] = x[0];
        _YcSize[1] = x[1];

    }

    /**
     * Specifies the extent of the fill rectangle in the Y direction,
     * in image points. This value can be smaller than, equal to, or
     * larger than the image cell extent in the Y direction (YCSize).
     * A value of X'FFFF' indicates that the image cell Y-extent should
     * be used as the fill rectangle Y-extent. The fill rectangle is
     * filled in the Y direction by repeating the image cell in the
     * Y direction. The image cell can be truncated to fit the rectangle.
     * @param yFillSize The size to set.
     */
    public void setYFillSize(int yFillSize) {

        byte[] x = BinaryUtils.convert(yFillSize, 2);
        _YFillSize[0] = x[0];
        _YFillSize[1] = x[1];

    }

}