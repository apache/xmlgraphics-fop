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
 * ImageDataDescriptor
 */
public class ImageDataDescriptor extends AbstractDescriptor {

    /**
     * Constructor for a ImageDataDescriptor for the specified
     * resolution, width and height.
     *
     * @param width The width of the image.
     * @param height The height of the height.
     * @param widthRes The horizontal resolution of the image.
     * @param heightRes The vertical resolution of the image.
     */
    public ImageDataDescriptor(int width, int height, int widthRes, int heightRes) {
        super(width, height, widthRes, heightRes);
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = new byte[22];
        copySF(data, Type.DESCRIPTOR, Category.IMAGE);

        // SF length
        byte[] len = BinaryUtils.convert(data.length - 1, 2);
        data[1] = len[0];
        data[2] = len[1];

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

        data[18] = (byte)0xF7; // ID = Set IOCA Function Set
        data[19] = 0x02; // Length
        data[20] = 0x01; // Category = Function set identifier
        data[21] = 0x0B; // FCNSET = IOCA FS 11

        os.write(data);
    }
}
