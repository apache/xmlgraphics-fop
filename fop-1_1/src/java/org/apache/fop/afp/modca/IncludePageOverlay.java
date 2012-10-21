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

package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.afp.util.BinaryUtils;

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
    private int x = 0;

    /**
     * The y coordinate
     */
    private int y = 0;

    /**
     * The orientation
     */
    private int orientation = 0;

    /**
     * Constructor for the Include Page Overlay
     *
     * @param overlayName Name of the page segment
     * @param x The x position
     * @param y The y position
     * @param orientation The orientation
     */
    public IncludePageOverlay(String overlayName, int x, int y, int orientation) {
        super(overlayName);

        this.x = x;
        this.y = y;
        setOrientation(orientation);
    }

    /**
     * Sets the orientation to use for the overlay.
     *
     * @param orientation
     *            The orientation (0,90, 180, 270)
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

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = new byte[25]; //(9 +16)
        copySF(data, Type.INCLUDE, Category.PAGE_OVERLAY);

        // Set the total record length
        byte[] len = BinaryUtils.convert(24, 2); //Ignore first byte
        data[1] = len[0];
        data[2] = len[1];

        byte[] xPos = BinaryUtils.convert(x, 3);
        data[17] = xPos[0]; // x coordinate
        data[18] = xPos[1];
        data[19] = xPos[2];

        byte[] yPos = BinaryUtils.convert(y, 3);
        data[20] = yPos[0]; // y coordinate
        data[21] = yPos[1];
        data[22] = yPos[2];

        switch (orientation) {
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
