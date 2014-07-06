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

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import org.apache.xmlgraphics.java2d.color.CIELabColorSpace;
import org.apache.xmlgraphics.java2d.color.ColorUtil;
import org.apache.xmlgraphics.java2d.color.ColorWithAlternatives;

/**
 * Sets the current processing color for the following GOCA structured fields
 */
public class GraphicsSetProcessColor extends AbstractGraphicsDrawingOrder {

    /*
     * GOCA Color space support:
     * X'01' RGB
     * X'04' CMYK
     * X'06' Highlight color space
     * X'08' CIELAB
     * X'40' Standard OCA color space
     */
    private static final byte RGB = 0x01;
    private static final byte CMYK = 0x04;
    private static final byte CIELAB = 0x08;

    private final Color color;
    private final int componentsSize;

    /**
     * Main constructor
     *
     * @param color the color to set
     */
    public GraphicsSetProcessColor(Color color) {
        if (color instanceof ColorWithAlternatives) {
            ColorWithAlternatives cwa = (ColorWithAlternatives)color;
            Color alt = cwa.getFirstAlternativeOfType(ColorSpace.TYPE_CMYK);
            if (alt != null) {
                this.color = alt;
                this.componentsSize = 4;
                return;
            }
        }
        ColorSpace cs = color.getColorSpace();
        int colSpaceType = cs.getType();
        if (colSpaceType == ColorSpace.TYPE_CMYK) {
            this.color = color;
        } else if (cs instanceof CIELabColorSpace) {
            //TODO Convert between illuminants if not D50 according to rendering intents
            //Right now, we're assuming D50 as the GOCA spec requires.
            this.color = color;
            //16bit components didn't work, and 8-bit sadly has reduced accuracy.
        } else {
            if (!color.getColorSpace().isCS_sRGB()) {
                this.color = ColorUtil.toSRGBColor(color);
            } else {
                this.color = color;
            }
        }
        this.componentsSize = this.color.getColorSpace().getNumComponents();
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 12 + this.componentsSize;
    }

    /** {@inheritDoc} */
    @Override
    byte getOrderCode() {
        return (byte) 0xB2;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        float[] colorComponents = color.getColorComponents(null);

        // COLSPCE
        byte colspace;
        ColorSpace cs = color.getColorSpace();
        int colSpaceType = cs.getType();
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        DataOutputStream dout = null;
        byte[] colsizes;
        if (colSpaceType == ColorSpace.TYPE_CMYK) {
            colspace = CMYK;
            colsizes = new byte[] {0x08, 0x08, 0x08, 0x08};
            for (int i = 0; i < colorComponents.length; i++) {
                baout.write(Math.round(colorComponents[i] * 255));
            }
        } else if (colSpaceType == ColorSpace.TYPE_RGB) {
            colspace = RGB;
            colsizes = new byte[] {0x08, 0x08, 0x08, 0x00};
            for (int i = 0; i < colorComponents.length; i++) {
                baout.write(Math.round(colorComponents[i] * 255));
            }
        } else if (cs instanceof CIELabColorSpace) {
            colspace = CIELAB;
            colsizes = new byte[] {0x08, 0x08, 0x08, 0x00};
            dout = new DataOutputStream(baout);
            //According to GOCA, I'd expect the multiplicator below to be 255f, not 100f
            //But only IBM AFP Workbench seems to support Lab colors and it requires "c * 100f"
            int l = Math.round(colorComponents[0] * 100f);
            int a = Math.round(colorComponents[1] * 255f) - 128;
            int b = Math.round(colorComponents[2] * 255f) - 128;
            dout.writeByte(l);
            dout.writeByte(a);
            dout.writeByte(b);
        } else {
            IOUtils.closeQuietly(dout);
            IOUtils.closeQuietly(baout);
            throw new IllegalStateException();
        }

        int len = getDataLength();
        byte[] data = new byte[12];
        data[0] = getOrderCode(); // GSPCOL order code
        data[1] = (byte) (len - 2); // LEN
        data[2] = 0x00; // reserved; must be zero
        data[3] = colspace; // COLSPCE
        data[4] = 0x00; // reserved; must be zero
        data[5] = 0x00; // reserved; must be zero
        data[6] = 0x00; // reserved; must be zero
        data[7] = 0x00; // reserved; must be zero
        data[8] = colsizes[0]; // COLSIZE(S)
        data[9] = colsizes[1];
        data[10] = colsizes[2];
        data[11] = colsizes[3];

        os.write(data);
        baout.writeTo(os);
        IOUtils.closeQuietly(dout);
        IOUtils.closeQuietly(baout);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "GraphicsSetProcessColor(col=" + color + ")";
    }
}
