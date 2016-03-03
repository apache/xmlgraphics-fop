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
import org.apache.fop.afp.util.BinaryUtils;

public class TileSize extends AbstractAFPObject {

    private int hSize;
    private int vSize;
//    private int hRes; // hRes and vRes not used yet -probably need them in the future
//    private int vRes;

    public TileSize(int hsize, int vsize, int hresol, int vresol) {
        this.hSize = hsize;
        this.vSize = vsize;
//        this.hRes = hresol;
//        this.vRes = vresol;
    }

    @Override
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = new byte[] {
                (byte)0xB6, // ID = Image Size Parameter
                0x09, // Length
                0x00, // THSIZE
                0x00, //
                0x00, //
                0x00, //
                0x00, // TVSIZE
                0x00, //
                0x00, //
                0x00, //
                0x01 // RELRES, can also be 0x02
            };

        byte[] w = BinaryUtils.convert(hSize, 4);
        data[2] = w[0];
        data[3] = w[1];
        data[4] = w[2];
        data[5] = w[3];

        byte[] h = BinaryUtils.convert(vSize, 4);
        data[6] = h[0];
        data[7] = h[1];
        data[8] = h[2];
        data[9] = h[3];

        os.write(data);
    }

}
