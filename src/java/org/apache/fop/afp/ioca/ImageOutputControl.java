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
 * The IM Image Output Control structured field specifies the position and
 * orientation of the IM image object area and the mapping of the image points
 * to presentation device pels.
 *
 */
public class ImageOutputControl extends AbstractAFPObject {

    /** the orientation of the image */
    private int orientation = 0;

    /**
     * Specifies the offset, along the X-axis, of the IM image object area
     * origin to the origin of the including page
     */
    private int xCoord = 0;

    /**
     * Specifies the offset, along the Y-axis, of the IM image object area
     * origin to the origin of the including page
     */
    private int yCoord = 0;

    /** map an image point to a single presentation device */
    private boolean singlePoint = true;

    /**
     * Constructor for the ImageOutputControl The x parameter specifies the
     * offset, along the X-axis, of the IM image object area origin to the
     * origin of the including page and the y parameter specifies the offset
     * along the Y-axis. The offset is specified in image points and is resolved
     * using the units of measure specified for the image in the IID structured
     * field.
     *
     * @param x
     *            The X-axis offset.
     * @param y
     *            The Y-axis offset.
     */
    public ImageOutputControl(int x, int y) {
        xCoord = x;
        yCoord = y;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {

        byte[] data = new byte[33];

        data[0] = 0x5A;
        data[1] = 0x00;
        data[2] = 0x20;
        data[3] = (byte) 0xD3;
        data[4] = (byte) 0xA7;
        data[5] = (byte) 0x7B;
        data[6] = 0x00;
        data[7] = 0x00;
        data[8] = 0x00;

        // XoaOset
        byte[] x1 = BinaryUtils.convert(xCoord, 3);
        data[9] = x1[0];
        data[10] = x1[1];
        data[11] = x1[2];

        // YoaOset
        byte[] x2 = BinaryUtils.convert(yCoord, 3);
        data[12] = x2[0];
        data[13] = x2[1];
        data[14] = x2[2];

        switch (orientation) {
            case 0:
                // 0 and 90 degrees respectively
                data[15] = 0x00;
                data[16] = 0x00;
                data[17] = 0x2D;
                data[18] = 0x00;
                break;
            case 90:
                // 90 and 180 degrees respectively
                data[15] = 0x2D;
                data[16] = 0x00;
                data[17] = 0x5A;
                data[18] = 0x00;
                break;
            case 180:
                // 180 and 270 degrees respectively
                data[15] = 0x5A;
                data[16] = 0x00;
                data[17] = (byte) 0x87;
                data[18] = 0x00;
                break;
            case 270:
                // 270 and 0 degrees respectively
                data[15] = (byte) 0x87;
                data[16] = 0x00;
                data[17] = 0x00;
                data[18] = 0x00;
                break;
            default:
                // 0 and 90 degrees respectively
                data[15] = 0x00;
                data[16] = 0x00;
                data[17] = 0x2D;
                data[18] = 0x00;
                break;

        }

        // Constant Data
        data[19] = 0x00;
        data[20] = 0x00;
        data[21] = 0x00;
        data[22] = 0x00;
        data[23] = 0x00;
        data[24] = 0x00;
        data[25] = 0x00;
        data[26] = 0x00;

        if (singlePoint) {
            data[27] = 0x03;
            data[28] = (byte) 0xE8;
            data[29] = 0x03;
            data[30] = (byte) 0xE8;
        } else {
            data[27] = 0x07;
            data[28] = (byte) 0xD0;
            data[29] = 0x07;
            data[30] = (byte) 0xD0;
        }

        // Constant Data
        data[31] = (byte) 0xFF;
        data[32] = (byte) 0xFF;

        os.write(data);
    }

    /**
     * Sets the orientation which specifies the amount of clockwise rotation of
     * the IM image object area.
     *
     * @param orientation
     *            The orientation to set.
     */
    public void setOrientation(int orientation) {

        if (orientation == 0 || orientation == 90 || orientation == 180
            || orientation == 270) {
            this.orientation = orientation;
        } else {
            throw new IllegalArgumentException(
                "The orientation must be one of the values 0, 90, 180, 270");
        }
    }

    /**
     * Sets the singlepoint, if true map an image point to a single presentation
     * device pel in the IM image object area. If false map an image point to
     * two presentation device pels in the IM image object area (double-dot)
     *
     * @param singlepoint
     *            Use the singlepoint basis when true.
     */
    public void setSinglepoint(boolean singlepoint) {
        singlePoint = singlepoint;
    }
}