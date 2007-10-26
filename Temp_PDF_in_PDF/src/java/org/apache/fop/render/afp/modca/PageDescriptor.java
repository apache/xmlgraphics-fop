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
public class PageDescriptor extends AbstractAFPObject {

    private int _width = 0;
    private int _height = 0;

    /**
     * Construct a page descriptor for the specified page width
     * and page height.
     * @param width The page width.
     * @param height The page height.
     */
    public PageDescriptor(int width, int height) {

        _width = width;
        _height = height;

    }

    /**
     * Accessor method to write the AFP datastream for the Page Descriptor
     * @param os The stream to write to
     * @throws java.io.IOException
     */
    public void writeDataStream(OutputStream os)
        throws IOException {

        byte[] data = new byte[] {
            0x5A,
            0x00,
            0x17,
            (byte) 0xD3,
            (byte) 0xA6,
            (byte) 0xAF,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x09,
            0x60,
            0x09,
            0x60,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
        };

        byte[] x = BinaryUtils.convert(_width, 3);
        data[15] = x[0];
        data[16] = x[1];
        data[17] = x[2];

        byte[] y = BinaryUtils.convert(_height, 3);
        data[18] = y[0];
        data[19] = y[1];
        data[20] = y[2];

        os.write(data);

    }

}