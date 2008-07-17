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
 * The Object Area Position structured field specifies the origin and
 * orientation of the object area, and the origin and orientation of the
 * object content within the object area.
 */
public class ObjectAreaPosition extends AbstractAFPObject {

    private int x;
    private int y;
    private int rotation;
    private int xOffset;
    private int yOffset;
    
    /**
     * Construct an object area position for the specified object y, y position.
     * 
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param rotation The coordinate system rotation (must be 0, 90, 180, 270).
     */
    public ObjectAreaPosition(int x, int y, int rotation) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
    }

    /** {@inheritDoc} */
    public void write(OutputStream os) throws IOException {
        byte[] data = new byte[33];
        copySF(data, Type.POSITION, Category.OBJECT_AREA);

        byte[] len = BinaryUtils.convert(32, 2);
        data[1] = len[0]; // Length
        data[2] = len[1];
            
        data[9] = 0x01; // OAPosID = 1
        data[10] = 0x17; // RGLength = 23

        byte[] xcoord = BinaryUtils.convert(x, 3);
        data[11] = xcoord[0]; // XoaOSet
        data[12] = xcoord[1];
        data[13] = xcoord[2];

        byte[] ycoord = BinaryUtils.convert(y, 3);
        data[14] = ycoord[0]; // YoaOSet
        data[15] = ycoord[1];
        data[16] = ycoord[2];
        
        byte xorient = (byte)(rotation / 2);
        data[17] = xorient; // XoaOrent
        
        byte yorient = (byte)(rotation / 2 + 45);
        data[19] = yorient; // YoaOrent

        byte[] xoffset = BinaryUtils.convert(xOffset, 3);
        data[22] = xoffset[0]; // XocaOSet
        data[23] = xoffset[1];
        data[24] = xoffset[2];

        byte[] yoffset = BinaryUtils.convert(yOffset, 3);
        data[25] = yoffset[0]; // YocaOSet
        data[26] = yoffset[1];
        data[27] = yoffset[2];

        data[28] = 0x00; // XocaOrent
        data[29] = 0x00;
        
        data[30] = 0x2D; // YocaOrent
        data[31] = 0x00;

        data[32] = 0x01; // RefCSys
        
        os.write(data);
    }
}