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
public class ObjectAreaDescriptor extends AbstractDescriptor {

    /**
     * Construct an object area descriptor for the specified object width
     * and object height.
     * 
     * @param width The page width.
     * @param height The page height.
     * @param widthResolution The page width resolution.
     * @param heightResolution The page height resolution.
     */
    public ObjectAreaDescriptor(int width, int height, int widthResolution, int heightResolution) {
        super(width, height, widthResolution, heightResolution);
    }

    /** {@inheritDoc} */
    public void write(OutputStream os) throws IOException {

        byte[] data = new byte[29];
        data[0] = 0x5A; 

        byte[] len = BinaryUtils.convert(data.length - 1, 2);
        data[1] = len[0]; // Length
        data[2] = len[1];

        data[3] = (byte) 0xD3;
        data[4] = (byte) 0xA6;
        data[5] = (byte) 0x6B;
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved
        data[9] = 0x03; // Triplet length
        data[10] = 0x43; // tid = Descriptor Position Triplet
        data[11] = 0x01; // DesPosId = 1
        data[12] = 0x08; // Triplet length
        data[13] = 0x4B; // tid = Measurement Units Triplet
        data[14] = 0x00; // XaoBase = 10 inches
        data[15] = 0x00; // YaoBase = 10 inches
        
        // XaoUnits
        byte[] xdpi = BinaryUtils.convert(widthResolution * 10, 2);
        data[16] = xdpi[0];
        data[17] = xdpi[1];

        // YaoUnits
        byte[] ydpi = BinaryUtils.convert(heightResolution * 10, 2);
        data[18] = ydpi[0];
        data[19] = ydpi[1];
        
        data[20] = 0x09; // Triplet length
        data[21] = 0x4C; // tid = Object Area Size
        data[22] = 0x02; // Size Type

        byte[] x = BinaryUtils.convert(width, 3);
        data[23] = x[0];
        data[24] = x[1];
        data[25] = x[2];

        byte[] y = BinaryUtils.convert(height, 3);
        data[26] = y[0];
        data[27] = y[1];
        data[28] = y[2];

        os.write(data);

    }

}