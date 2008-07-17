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
 */
public class ImageDataDescriptor extends AbstractAFPObject {

    private int widthRes = 0;
    private int heightRes = 0;
    private int width = 0;
    private int height = 0;
    
    /**
     * Constructor for a ImageDataDescriptor for the specified
     * resolution, width and height.
     * 
     * @param widthRes The horizontal resolution of the image.
     * @param heightRes The vertical resolution of the image.
     * @param width The width of the image.
     * @param height The height of the height.
     */
    public ImageDataDescriptor(int widthRes, int heightRes, int width, int height) {
        this.widthRes = widthRes;
        this.heightRes = heightRes;
        this.width = width;
        this.height = height;
    }

    /** {@inheritDoc} */
    public void write(OutputStream os) throws IOException {

        byte[] data = new byte[] {
            0x5A,
            0x00,
            0x20,
            (byte) 0xD3,
            (byte) 0xA6,
            (byte) 0xFB,
            0x00, // Flags
            0x00, // Reserved
            0x00, // Reserved
            0x00, // Unit base - 10 Inches
            0x00, // XRESOL
            0x00, //
            0x00, // YRESOL
            0x00, //
            0x00, // XSIZE
            0x00, //
            0x00, // YSIZE
            0x00, //
            (byte)0xF7, // ID = Set IOCA Function Set
            0x02, // Length
            0x01, // Category = Function set identifier
            0x0B, // FCNSET = IOCA FS 11
        };

        byte[] l = BinaryUtils.convert(data.length - 1, 2);
        data[1] = l[0];
        data[2] = l[1];

        byte[] x = BinaryUtils.convert(widthRes, 2);
        data[10] = x[0];
        data[11] = x[1];

        byte[] y = BinaryUtils.convert(heightRes, 2);
        data[12] = y[0];
        data[13] = y[1];

        byte[] w = BinaryUtils.convert(width, 2);
        data[14] = w[0];
        data[15] = w[1];

        byte[] h = BinaryUtils.convert(height, 2);
        data[16] = h[0];
        data[17] = h[1];

        os.write(data);
    }
}
