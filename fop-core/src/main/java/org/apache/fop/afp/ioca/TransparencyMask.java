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

public class TransparencyMask extends AbstractAFPObject {
    private static final int MAX_DATA_LEN = 8188;
    private ImageSizeParameter imageSizeParameter;
    private byte[] maskData;

    public TransparencyMask(byte[] maskData, ImageSizeParameter imageSizeParameter) {
        this.maskData = maskData;
        this.imageSizeParameter = imageSizeParameter;
    }

    public void writeToStream(OutputStream os) throws IOException {
        os.write(0x8E);
        os.write(0);
        imageSizeParameter.writeToStream(os);
        os.write(getImageEncodingParameter());

        final byte[] dataHeader = new byte[] {(byte) 0xFE, // ID
                (byte) 0x92, // ID
                0x00, // length
                0x00 // length
        };
        final int lengthOffset = 2;
        writeChunksToStream(maskData, dataHeader, lengthOffset, MAX_DATA_LEN, os);

        os.write(0x8F);
        os.write(0);
    }

    private byte[] getImageEncodingParameter() {
        byte encoding = (byte) 0x03;
        return new byte[] {(byte) 0x95, // ID
                0x02, // Length
                encoding,
                (byte) 0x01, // RECID
        };
    }
}
