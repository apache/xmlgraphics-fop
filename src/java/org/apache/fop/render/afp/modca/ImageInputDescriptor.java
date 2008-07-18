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
 * The IM Image Input Descriptor structured field contains the
 * descriptor data for an IM image data object. This data specifies
 * the resolution, size, and color of the IM image.
 */
public class ImageInputDescriptor extends AbstractAFPObject {

    /**
     * The resolution of the raster image (default 240)
     */
    private int resolution = 240;


    /** {@inheritDoc} */
    public void write(OutputStream os) throws IOException {

        byte[] data = new byte[45];
        copySF(data, Type.DESCRIPTOR, Category.IM_IMAGE);
        
        data[1] = 0x00; // length
        data[2] = 0x2C;

        // Constant data.
        data[9] = 0x00;
        data[10] = 0x00;
        data[11] = 0x09;
        data[12] = 0x60;
        data[13] = 0x09;
        data[14] = 0x60;
        data[15] = 0x00;
        data[16] = 0x00;
        data[17] = 0x00;
        data[18] = 0x00;
        data[19] = 0x00;
        data[20] = 0x00;

        // X Base (Fixed x00)
        data[21] = 0x00;
        // Y Base (Fixed x00)
        data[22] = 0x00;

        byte[] imagepoints = BinaryUtils.convert(resolution * 10, 2);

        /**
         * Specifies the number of image points per unit base for the X axis
         * of the image. This value is ten times the resolution of the image
         * in the X direction.
         */
        data[23] = imagepoints[0];
        data[24] = imagepoints[1];

        /**
         * Specifies the number of image points per unit base for the Y axis
         * of the image. This value is ten times the resolution of the image
         * in the Y direction.
         */
        data[25] = imagepoints[0];
        data[26] = imagepoints[1];

        /**
         * Specifies the extent in the X direction, in image points, of an
         * non-celled (simple) image.
         */
        data[27] = 0x00;
        data[28] = 0x01;

        /**
         * Specifies the extent in the Y direction, in image points, of an
         * non-celled (simple) image.
         */
        data[29] = 0x00;
        data[30] = 0x01;

        // Constant Data
        data[31] = 0x00;
        data[32] = 0x00;
        data[33] = 0x00;
        data[34] = 0x00;
        data[35] = 0x2D;
        data[36] = 0x00;

        // Default size of image cell in X direction
        data[37] = 0x00;
        data[38] = 0x01;

        // Default size of image cell in Y direction
        data[39] = 0x00;
        data[40] = 0x01;

        // Constant Data
        data[41] = 0x00;
        data[42] = 0x01;

        // Image Color
        data[43] = (byte)0xFF;
        data[44] = (byte)0xFF;

        os.write(data);
    }

    /**
     * Sets the resolution information for the raster image
     * the default value is a resolution of 240 dpi.
     * 
     * @param resolution The resolution value
     */
    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

}