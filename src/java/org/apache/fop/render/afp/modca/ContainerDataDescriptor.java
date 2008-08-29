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

package org.apache.fop.render.afp.modca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * Container data descriptor (to maintain compatibility with pre-year 2000 applications)
 */
public class ContainerDataDescriptor extends AbstractDescriptor {

    /**
     * Main constructor
     *
     * @param width the container data width
     * @param height  the container data height
     * @param widthRes the container width resolution
     * @param heightRes the container height resolution
     */
    public ContainerDataDescriptor(int width, int height, int widthRes,
            int heightRes) {
        super(width, height, widthRes, heightRes);
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = new byte[21];
        copySF(data, Type.DESCRIPTOR, Category.OBJECT_CONTAINER);

        // SF length
        byte[] len = BinaryUtils.convert(data.length - 1, 2);
        data[1] = len[0];
        data[2] = len[1];

        // XocBase = 10 inches
        data[9] = 0x00;

        // YocBase = 10 inches
        data[10] = 0x00;

        // XocUnits
        byte[] xdpi = BinaryUtils.convert(widthRes * 10, 2);
        data[11] = xdpi[0];
        data[12] = xdpi[1];

        // YocUnits
        byte[] ydpi = BinaryUtils.convert(heightRes * 10, 2);
        data[13] = ydpi[0];
        data[14] = ydpi[1];

        // XocSize
        byte[] xsize = BinaryUtils.convert(width, 3);
        data[15] = xsize[0];
        data[16] = xsize[1];
        data[17] = xsize[2];

        // YocSize
        byte[] ysize = BinaryUtils.convert(height, 3);
        data[18] = ysize[0];
        data[19] = ysize[1];
        data[20] = ysize[2];
    }

}
