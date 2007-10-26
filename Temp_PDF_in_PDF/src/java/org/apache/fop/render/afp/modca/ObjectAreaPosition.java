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

    private int _x = 0;
    private int _y = 0;
    private int _rot = 0;

    /**
     * Construct an object area position for the specified object y, y position.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param rotation The coordinate system rotation (must be 0, 90, 180, 270).
     */
    public ObjectAreaPosition(int x, int y, int rotation) {

        _x = x;
        _y = y;
        _rot = rotation;
    }

    /**
     * Accessor method to write the AFP datastream for the Object Area Position
     * @param os The stream to write to
     * @throws java.io.IOException
     */
    public void writeDataStream(OutputStream os)
        throws IOException {

        byte[] data = new byte[] {
            0x5A,
            0x00, // Length
            0x20, // Length
            (byte) 0xD3,
            (byte) 0xAC,
            (byte) 0x6B,
            0x00, // Flags
            0x00, // Reserved
            0x00, // Reserved
            0x01, // OAPosID = 1
            0x17, // RGLength = 23
            0x00, // XoaOSet
            0x00,
            0x00,
            0x00, // YoaOSet
            0x00,
            0x00,
            (byte)(_rot / 2), // XoaOrent
            0x00,
            (byte)(_rot / 2 + 45), // YoaOrent
            0x00,
            0x00, // Reserved
            0x00, // XocaOSet
            0x00,
            0x00,
            0x00, // YocaOSet
            0x00,
            0x00,
            0x00, // XocaOrent
            0x00,
            0x2D, // YocaOrent
            0x00,
            0x01, // RefCSys
        };

        byte[] l = BinaryUtils.convert(data.length - 1, 2);
        data[1] = l[0];
        data[2] = l[1];

        byte[] x = BinaryUtils.convert(_x, 3);
        data[11] = x[0];
        data[12] = x[1];
        data[13] = x[2];

        byte[] y = BinaryUtils.convert(_y, 3);
        data[14] = y[0];
        data[15] = y[1];
        data[16] = y[2];

        os.write(data);

    }

}