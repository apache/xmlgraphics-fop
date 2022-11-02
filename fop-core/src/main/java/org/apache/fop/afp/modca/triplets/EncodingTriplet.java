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

package org.apache.fop.afp.modca.triplets;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.afp.util.BinaryUtils;
/**
 *
 * Represents a CCSID encoding triplet.
 *
 */
public class EncodingTriplet extends AbstractTriplet {


    private int encoding;
    /**
     * @param encoding the CCSID character set encoding
     */
    public EncodingTriplet(int encoding) {
        super(CODED_GRAPHIC_CHARACTER_SET_GLOBAL_IDENTIFIER);
        this.encoding = encoding;
    }
    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {

        // [len,id,0,0,0,0]
        byte[] data = getData();

        byte[] encodingBytes = BinaryUtils.convert(encoding, 2);

        // [len,id,0,0,0,0] -> [len.id,0,0,encodingBytes[0],encodingBytes[1]]
        System.arraycopy(encodingBytes, 0, data, 4, encodingBytes.length);

        os.write(data);

    }

    /** {@inheritDoc} */
    public int getDataLength() {
        //len(1b) + id(1b) + 0x0000 (2b) + encoding (2b) = 6b
        return 6;
    }

}
