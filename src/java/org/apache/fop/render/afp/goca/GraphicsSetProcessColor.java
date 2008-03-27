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

import java.awt.Color;
import java.awt.color.ColorSpace;

import org.apache.fop.render.afp.modca.AbstractPreparedAFPObject;
import org.apache.fop.render.afp.modca.GraphicsObject;

/**
 * Sets the current processing color for the following GOCA structured fields
 */
public class GraphicsSetProcessColor extends AbstractPreparedAFPObject {
    /** the color to set */
    private Color col;

    /**
     * Main constructor
     * @param col the color to set
     */
    public GraphicsSetProcessColor(Color col) {
        this.col = col;
        prepareData();
    }

    /**
     * {@inheritDoc}
     */
    protected void prepareData() {
        // COLSPCE
        byte colspace;
        int colSpaceType = col.getColorSpace().getType();
        if (colSpaceType == ColorSpace.TYPE_CMYK) {
            colspace = 0x04;
        } else if (colSpaceType == ColorSpace.TYPE_RGB) {
            colspace = 0x01;
        } else {
            GraphicsObject.log.error("unsupported colorspace " + colSpaceType);
            colspace = 0x01;
        }
        
        // COLSIZE(S)
        float[] colcomp = col.getColorComponents(null);
        byte[] colsizes = new byte[] {0x00, 0x00, 0x00, 0x00};
        for (int i = 0; i < colcomp.length; i++) {
            colsizes[i] = (byte)8;
        }

        int len = 10 + colcomp.length;
        super.data = new byte[len + 2];
        data[0] = (byte)0xB2; // GSPCOL order code 
        data[1] = (byte)len; // LEN
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
        for (int i = 0; i < colcomp.length; i++) {
            data[i + 12] = (byte)(colcomp[i] * 255);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "GraphicsSetProcessColor(col=" + col + ")";
    }
}