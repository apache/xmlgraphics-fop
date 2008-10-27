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

package org.apache.fop.afp.ioca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.afp.modca.AbstractAFPObject;
import org.apache.fop.afp.modca.AbstractAFPObject.Category;
import org.apache.fop.afp.modca.AbstractAFPObject.Type;
import org.apache.fop.afp.util.BinaryUtils;

/**
 * Contains the image points that define the IM image raster pattern.
 *
 * A raster pattern is the array of presentation device pels that forms
 * the image. The image data is uncompressed. Bits are grouped into
 * bytes and are ordered from left to right within each byte. Each bit
 * in the image data represents an image point and is mapped to
 * presentation device pels as specified in the IOC structured field.
 * A bit with value B'1' indicates a significant image point; a bit
 * with value B'0' indicates an insignificant image point.
 * Image points are recorded from left to right in rows that represents
 * scan lines (X direction), and rows representing scan lines are
 * recorded from top to bottom (Y direction). When the image is
 * presented, all image points in a row are presented before any
 * image points in the next sequential row are presented, and all rows
 * have the same number of image points. If the total number of image
 * points is not a multiple of 8, the last byte of the image data is
 * padded to a byte boundary. The padding bits do not represent image
 * points and are ignored by presentation devices.
 */
public class ImageRasterData extends AbstractAFPObject {

    /**
     * The image raster data
     */
    private final byte[] rasterData;

    /**
     * Constructor for the image raster data object
     * @param data The raster image data
     */
    public ImageRasterData(byte[] data) {
        this.rasterData = data;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = new byte[9];
        copySF(data, Type.DATA, Category.IM_IMAGE);
        // The size of the structured field
        byte[] len = BinaryUtils.convert(rasterData.length + 8, 2);
        data[1] = len[0];
        data[2] = len[1];
        os.write(data);

        os.write(rasterData);
    }
}