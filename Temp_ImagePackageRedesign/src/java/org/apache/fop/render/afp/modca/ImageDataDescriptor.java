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

    /** x resolution */
    private int xresol = 0;
    
    /** y resolution */
    private int yresol = 0;
    
    /** width */
    private int width = 0;
    
    /** height */
    private int height = 0;

    /**
     * Constructor for a ImageDataDescriptor for the specified
     * resolution, width and height.
     * @param xresol The horizontal resolution of the image.
     * @param yresol The vertical resolution of the image.
     * @param width The width of the image.
     * @param height The height of the height.
     */
    public ImageDataDescriptor(int xresol, int yresol, int width, int height) {
        this.xresol = xresol;
        this.yresol = yresol;
        this.width = width;
        this.height = height;
    }

    /**
     * {@inheritDoc}
     */
    public void writeDataStream(OutputStream os) throws IOException {
        byte[] len = BinaryUtils.convert(21, 2);
        byte[] xres = BinaryUtils.convert(xresol, 2);
        byte[] yres = BinaryUtils.convert(yresol, 2);
        byte[] w = BinaryUtils.convert(width, 2);
        byte[] h = BinaryUtils.convert(height, 2);
        byte[] data = new byte[] {
            0x5A,
            len[0],
            len[1],
            (byte) 0xD3,
            (byte) 0xA6,
            (byte) 0xFB,
            0x00, // Flags
            0x00, // Reserved
            0x00, // Reserved
            0x00, // Unit base - 10 Inches
            xres[0], // XRESOL
            xres[1], //
            yres[0], // YRESOL
            yres[1], //
            w[0], // XSIZE
            w[1], //
            h[0], // YSIZE
            h[1], //
            (byte)0xF7, // ID = Set IOCA Function Set
            0x02, // Length
            0x01, // Category = Function set identifier
            0x0B, // FCNSET = IOCA FS 11
        };
        os.write(data);
    }
}
