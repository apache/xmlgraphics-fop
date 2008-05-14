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
 * The Page Descriptor structured field specifies the size and attributes of
 * a page or overlay presentation space.
 *
 */
public class PageDescriptor extends AbstractDescriptor {

    /**
     * Construct a page descriptor for the specified page width
     * and page height.
     * @param width The page width.
     * @param height The page height.
     * @param widthResolution The page width resolution
     * @param heightResolution The page height resolution
     */
    public PageDescriptor(int width, int height, int widthResolution, int heightResolution) {
        super(width, height, widthResolution, heightResolution);
    }

    /**
     * Accessor method to write the AFP datastream for the Page Descriptor
     * @param os The stream to write to
     * @throws java.io.IOException in the event that an I/O Exception occurred
     */
    public void write(OutputStream os)
        throws IOException {

        log.debug("width=" + width);
        log.debug("height=" + height);
        byte[] data = new byte[24];
        data[0] = 0x5A;
        data[1] = 0x00;
        data[2] = 0x17;
        data[3] = (byte) 0xD3;
        data[4] = (byte) 0xA6;
        data[5] = (byte) 0xAF;
        
        data[6] = 0x00; // Flags 
        data[7] = 0x00; // Reserved 
        data[8] = 0x00;  // Reserved
        
        data[9] = 0x00; // XpgBase = 10 inches 
        data[10] = 0x00; // YpgBase = 10 inches 
        
        // XpgUnits
        byte[] xdpi = BinaryUtils.convert(widthResolution * 10, 2);
        data[11] = xdpi[0];
        data[12] = xdpi[1];

        // YpgUnits
        byte[] ydpi = BinaryUtils.convert(heightResolution * 10, 2);
        data[13] = ydpi[0];
        data[14] = ydpi[1];
            
        // XpgSize
        byte[] x = BinaryUtils.convert(width, 3);
        data[15] = x[0];
        data[16] = x[1];
        data[17] = x[2];

        // YpgSize
        byte[] y = BinaryUtils.convert(height, 3);
        data[18] = y[0];
        data[19] = y[1];
        data[20] = y[2];

        data[21] = 0x00; // Reserved
        data[22] = 0x00; // Reserved
        data[23] = 0x00; // Reserved

        os.write(data);
    }

}