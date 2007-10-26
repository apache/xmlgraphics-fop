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
 * The Object Area Descriptor structured field specifies the size and attributes
 * of an object area presentation space.
 *
 */
public class ObjectAreaDescriptor extends AbstractAFPObject {

    private int _width = 0;
    private int _height = 0;

    /**
     * Construct an object area descriptor for the specified object width
     * and object height.
     * @param width The page width.
     * @param height The page height.
     */
    public ObjectAreaDescriptor(int width, int height) {

        _width = width;
        _height = height;

    }

    /**
     * Accessor method to write the AFP datastream for the Object Area Descriptor
     * @param os The stream to write to
     * @throws java.io.IOException
     */
    public void writeDataStream(OutputStream os)
        throws IOException {

        byte[] data = new byte[] {
            0x5A,
            0x00, // Length
            0x1C, // Length
            (byte) 0xD3,
            (byte) 0xA6,
            (byte) 0x6B,
            0x00, // Flags
            0x00, // Reserved
            0x00, // Reserved
            0x03, // Triplet length
            0x43, // tid = Descriptor Position Triplet
            0x01, // DesPosId = 1
            0x08, // Triplet length
            0x4B, // tid = Measurement Units Triplet
            0x00, // XaoBase = 10 inches
            0x00, // YaoBase = 10 inches
            0x09, // XaoUnits = 2400
            0x60, // XaoUnits =
            0x09, // YaoUnits = 2400
            0x60, // YaoUnits =
            0x09, // Triplet length
            0x4C, // tid = Object Area Size
            0x02, // Size Type
            0x00, // XoaSize
            0x00,
            0x00,
            0x00, // YoaSize
            0x00,
            0x00,
        };

        byte[] l = BinaryUtils.convert(data.length - 1, 2);
        data[1] = l[0];
        data[2] = l[1];

        byte[] x = BinaryUtils.convert(_width, 3);
        data[23] = x[0];
        data[24] = x[1];
        data[25] = x[2];

        byte[] y = BinaryUtils.convert(_height, 3);
        data[26] = y[0];
        data[27] = y[1];
        data[28] = y[2];

        os.write(data);

    }

}