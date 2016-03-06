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
 * The Object Byte Extent triplet is used to specify the number of bytes contained in an object
 */
public class ObjectByteExtentTriplet extends AbstractTriplet {

    private final int byteExt;

    /**
     * Main constructor
     *
     * @param byteExt the number of bytes contained in the object
     */
    public ObjectByteExtentTriplet(int byteExt) {
        super(OBJECT_BYTE_EXTENT);
        this.byteExt = byteExt;
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 6;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = getData();
        byte[] extData = BinaryUtils.convert(byteExt, 4);
        System.arraycopy(extData, 0, data, 2, extData.length);
        os.write(data);
    }
}
