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
 * An Include Object structured field references an object on a page or overlay.
 * It optionally contains parameters that identify the object and that specify
 * presentation parameters such as object position, size, orientation, mapping,
 * and default color.
 * <p>
 * Where the presentation parameters conflict with parameters specified in the
 * object's environment group (OEG), the parameters in the Include Object
 * structured field override. If the referenced object is a page segment, the
 * IOB parameters override the corresponding environment group parameters on all
 * data objects in the page segment.
 * </p>
 */
public class IncludeObject extends AbstractNamedAFPObject {

    /**
     * The object type
     */
    private byte objectType = (byte) 0x92;

    /**
     * The orientation on the include object
     */
    private int orientation = 0;

    /**
     * Constructor for the include object with the specified name, the name must
     * be a fixed length of eight characters and is the name of the referenced
     * object.
     *
     * @param name
     *            the name of the image
     */
    public IncludeObject(String name) {

        super(name);
        objectType = (byte) 0xFB;

    }

    /**
     * Sets the orientation to use for the Include Object.
     *
     * @param orientation
     *            The orientation (0,90, 180, 270)
     */
    public void setOrientation(int orientation) {

        if (orientation == 0 || orientation == 90 || orientation == 180
            || orientation == 270) {
            this.orientation = orientation;
        } else {
            throw new IllegalArgumentException(
                "The orientation must be one of the values 0, 90, 180, 270");
        }

    }

    /**
     * Accessor method to write the AFP datastream for the Include Object
     * @param os The stream to write to
     * @throws java.io.IOException thrown if an I/O exception of some sort has occurred
     */
    public void writeDataStream(OutputStream os)
        throws IOException {

        byte[] data = new byte[37];

        data[0] = 0x5A;

        // Set the total record length
        byte[] rl1 = BinaryUtils.convert(36, 2); //Ignore first byte
        data[1] = rl1[0];
        data[2] = rl1[1];

        // Structured field ID for a IOB
        data[3] = (byte) 0xD3;
        data[4] = (byte) 0xAF;
        data[5] = (byte) 0xC3;

        data[6] = 0x00; // Reserved
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved

        for (int i = 0; i < nameBytes.length; i++) {
            data[9 + i] = nameBytes[i];
        }

        data[17] = 0x00;
        data[18] = objectType;

        // XoaOset
        data[20] = (byte) 0xFF;
        data[21] = (byte) 0xFF;
        data[22] = (byte) 0xFF;

        // YoaOset
        data[23] = (byte) 0xFF;
        data[24] = (byte) 0xFF;
        data[25] = (byte) 0xFF;

        switch (orientation) {
            case 90:
                data[26] = 0x2D;
                data[27] = 0x00;
                data[28] = 0x5A;
                data[29] = 0x00;
                break;
            case 180:
                data[26] = 0x5A;
                data[27] = 0x00;
                data[28] = (byte) 0x87;
                data[29] = 0x00;
                break;
            case 270:
                data[26] = (byte) 0x87;
                data[27] = 0x00;
                data[28] = 0x00;
                data[29] = 0x00;
                break;
            default:
                data[26] = 0x00;
                data[27] = 0x00;
                data[28] = 0x2D;
                data[29] = 0x00;
                break;
        }

        // XocaOset
        data[30] = (byte) 0xFF;
        data[31] = (byte) 0xFF;
        data[32] = (byte) 0xFF;

        // YocaOset
        data[33] = (byte) 0xFF;
        data[34] = (byte) 0xFF;
        data[35] = (byte) 0xFF;

        data[36] = 0x01;

        os.write(data);

    }

}