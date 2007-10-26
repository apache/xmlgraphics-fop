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
import java.io.UnsupportedEncodingException;

import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 *
 * The Include Page Overlay structured field references an overlay resource
 * definition that is to be positioned on the page. A page overlay can be
 * referenced at any time during the page state, but not during an object state.
 * The overlay contains its own active environment group definition.
 *
 * Note: There is no need for the triplets, so I have ignored them.
 *
 * A real example of where this will be used is for static overlays, such as an
 * address on the page.
 *
 */
public class IncludePageOverlay extends AbstractNamedAFPObject {

    /**
     * The x coordinate
     */
    private int _xCoor = 0;

    /**
     * The y coordinate
     */
    private int _yCoor = 0;

    /**
     * The orientation
     */
    private int _orientation = 0;

    /**
     * Constructor for the Include Page Overlay
     * @param overlayName Name of the page segment
     * @param x The x position
     * @param y The y position
     * @param orientation The orientation
     */
    public IncludePageOverlay(String overlayName, int x, int y, int orientation) {

        super(overlayName);

        _xCoor = x;
        _yCoor = y;
        setOrientation(orientation);
    }

    /**
     * Sets the orienation to use for the overlay.
     *
     * @param orientation
     *            The orientation (0,90, 180, 270)
     */
    public void setOrientation(int orientation) {

        if (orientation == 0 || orientation == 90 || orientation == 180
            || orientation == 270) {
            _orientation = orientation;
        } else {
            throw new IllegalArgumentException(
                "The orientation must be one of the values 0, 90, 180, 270");
        }

    }

    /**
     * Accessor method to write the AFP datastream for the Include Page Overlay
     * @param os The stream to write to
     * @throws java.io.IOException
     */
    public void writeDataStream(OutputStream os)
        throws IOException {

        byte[] data = new byte[25]; //(9 +16)

        data[0] = 0x5A;

        // Set the total record length
        byte[] rl1 = BinaryUtils.convert(24, 2); //Ignore first byte
        data[1] = rl1[0];
        data[2] = rl1[1];

        // Structured field ID for a IPO
        data[3] = (byte) 0xD3;
        data[4] = (byte) 0xAF;
        data[5] = (byte) 0xD8;

        data[6] = 0x00; // Reserved
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved

        for (int i = 0; i < _nameBytes.length; i++) {

            data[9 + i] = _nameBytes[i];

        }

        byte[] r2 = BinaryUtils.convert(_xCoor, 3);
        data[17] = r2[0]; // x coordinate
        data[18] = r2[1];
        data[19] = r2[2];

        byte[] r3 = BinaryUtils.convert(_yCoor, 3);
        data[20] = r3[0]; // y coordinate
        data[21] = r3[1];
        data[22] = r3[2];

        switch (_orientation) {
            case 90:
                data[23] = 0x2D;
                data[24] = 0x00;
                break;
            case 180:
                data[23] = 0x5A;
                data[24] = 0x00;
                break;
            case 270:
                data[23] = (byte) 0x87;
                data[24] = 0x00;
                break;
            default:
                data[23] = 0x00;
                data[24] = 0x00;
                break;
        }

        os.write(data);

    }

}