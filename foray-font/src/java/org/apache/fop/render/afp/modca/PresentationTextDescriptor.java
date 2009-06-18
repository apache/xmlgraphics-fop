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
 * The Presentation Text Descriptor specifies the units of measure for the
 * Presentation Text object space, the size of the Presentation Text object
 * space, and the initial values for modal parameters, called initial text
 * conditions. Initial values not provided are defaulted by the controlling
 * environment or the receiving device.
 *
 * The Presentation Text Descriptor provides the following initial values:
 * - Unit base
 * - Xp-units per unit base
 * - Yp-units per unit base
 * - Xp-extent of the presentation space
 * - Yp-extent of the presentation space
 * - Initial text conditions.
 *
 * The initial text conditions are values provided by the Presentation Text
 * Descriptor to initialize the modal parameters of the control sequences.
 * Modal control sequences typically are characterized by the word set in
 * the name of the control sequence. Modal parameters are identified as such
 * in their semantic descriptions.
 *
 */
public class PresentationTextDescriptor extends AbstractAFPObject {

    private int _width = 0;
    private int _height = 0;

    /**
     * Constructor a PresentationTextDescriptor for the specified
     * width and height.
     * @param width The width of the page.
     * @param height The height of the page.
     */
    public PresentationTextDescriptor(int width, int height) {

        _width = width;
        _height = height;

    }

    /**
     * Accessor method to write the AFP datastream for the Presentation Text Descriptor
     * @param os The stream to write to
     * @throws java.io.IOException
     */
    public void writeDataStream(OutputStream os)
        throws IOException {

        byte[] data = new byte[] {
            0x5A,
            0x00,
            0x16,
            (byte) 0xD3,
            (byte) 0xB1,
            (byte) 0x9B,
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