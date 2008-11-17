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

package org.apache.fop.afp.goca;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.afp.modca.AbstractNamedAFPObject;
import org.apache.fop.afp.modca.StructuredDataObject;

/**
 * Sets the current processing color for the following GOCA structured fields
 */
public class GraphicsSetProcessColor extends AbstractNamedAFPObject
implements StructuredDataObject {

    private final Color color;

    private final float[] colorComponents;

    /**
     * Main constructor
     *
     * @param color the color to set
     */
    public GraphicsSetProcessColor(Color color) {
        this.color = color;
        this.colorComponents = color.getColorComponents(null);
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 12 + colorComponents.length;
    }

    /** {@inheritDoc} */
    byte getOrderCode() {
        return (byte)0xB2;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {

        // COLSPCE
        byte colspace;
        int colSpaceType = color.getColorSpace().getType();
        if (colSpaceType == ColorSpace.TYPE_CMYK) {
            colspace = 0x04;
        } else if (colSpaceType == ColorSpace.TYPE_RGB) {
            colspace = 0x01;
        } else {
            log.error("unsupported colorspace " + colSpaceType);
            colspace = 0x01;
        }

        // COLSIZE(S)
        byte[] colsizes = new byte[] {0x00, 0x00, 0x00, 0x00};
        for (int i = 0; i < colorComponents.length; i++) {
            colsizes[i] = (byte)8;
        }

        int len = getDataLength();
        byte[] data = new byte[len];
        data[0] = getOrderCode(); // GSPCOL order code
        data[1] = (byte)(len - 2); // LEN
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

        // COLVALUE(S)
        for (int i = 0; i < colorComponents.length; i++) {
            data[i + 12] = (byte)(colorComponents[i] * 255);
        }

        os.write(data);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "GraphicsSetProcessColor(col=" + color + ")";
    }
}