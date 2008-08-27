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
 * GOCA Graphics Data Descriptor
 */
public class GraphicsDataDescriptor extends AbstractDescriptor {

    private final int xlwind;

    private final int xrwind;

    private final int ybwind;

    private final int ytwind;

    /**
     * Main constructor
     *
     * @param xlwind
     *            the left edge of the graphics window
     * @param xrwind
     *            the right edge of the graphics window
     * @param ybwind
     *            the top edge of the graphics window
     * @param ytwind
     *            the bottom edge of the graphics window
     * @param widthRes
     *            the width resolution of the graphics window
     * @param heightRes
     *            the height resolution of the graphics window
     */
    protected GraphicsDataDescriptor(int xlwind, int xrwind, int ybwind,
            int ytwind, int widthRes, int heightRes) {
        this.xlwind = xlwind;
        this.xrwind = xrwind;
        this.ybwind = ybwind;
        this.ytwind = ytwind;
        super.widthRes = widthRes;
        super.heightRes = heightRes;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] headerData = new byte[9];
        copySF(headerData, Type.DESCRIPTOR, Category.GRAPHICS);
        byte[] drawingOrderSubsetData = getDrawingOrderSubset();
        byte[] windowSpecificationData = getWindowSpecification();
        byte[] len = BinaryUtils.convert(
                8 + drawingOrderSubsetData.length + windowSpecificationData.length, 2);
        headerData[1] = len[0];
        headerData[2] = len[1];
        os.write(headerData);
        os.write(drawingOrderSubsetData);
        os.write(windowSpecificationData);
    }

    /**
     * Returns the drawing order subset data
     *
     * @return the drawing order subset data
     */
    private byte[] getDrawingOrderSubset() {
        final byte[] data = new byte[] {
            // Drawing order subset
            (byte) 0xF7,
            7, // LENGTH
            (byte) 0xB0, // drawing order subset
            0x00, // reserved (must be zero)
            0x00, // reserved (must be zero)
            0x02, // SUBLEV
            0x00, // VERSION 0
            0x01, // LENGTH (of following field)
            0x00 // GEOM
        };
        return data;
    }

    private static final int ABS = 2;
    private static final int IMGRES = 8;

    /**
     * Returns the window specification data
     *
     * @return the window specification data
     */
    private byte[] getWindowSpecification() {
        byte[] xlcoord = BinaryUtils.convert(xlwind, 2);
        byte[] xrcoord = BinaryUtils.convert(xrwind, 2);
        byte[] xbcoord = BinaryUtils.convert(ybwind, 2);
        byte[] ytcoord = BinaryUtils.convert(ytwind, 2);
        byte[] xResol = BinaryUtils.convert(widthRes * 10, 2);
        byte[] yResol = BinaryUtils.convert(heightRes * 10, 2);
        byte[] imxyres = xResol;

        final byte[] data = new byte[] {
        // Window specification
            (byte) 0xF6, 18, // LENGTH
            (ABS + IMGRES), // FLAGS (ABS)
            0x00, // reserved (must be zero)
            0x00, // CFORMAT (coordinate format - 16bit high byte first signed)
            0x00, // UBASE (unit base - ten inches)
            xResol[0], // XRESOL
            xResol[1],
            yResol[0], // YRESOL
            yResol[1],
            imxyres[0], // IMXYRES (Number of image points per ten inches
            imxyres[1], // in X and Y directions)
            xlcoord[0], // XLWIND
            xlcoord[1],
            xrcoord[0], // XRWIND
            xrcoord[1],
            xbcoord[0], // YBWIND
            xbcoord[1],
            ytcoord[0], // YTWIND
            ytcoord[1]
        };
        return data;
    }
}
