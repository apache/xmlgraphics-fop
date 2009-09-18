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

package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.afp.util.BinaryUtils;

/**
 * The Map Data Resource structured field specifies resources that are
 * required for presentation.
 */
public class MapDataResource extends AbstractTripletStructuredObject {

    /**
     * Main constructor
     */
    public MapDataResource() {
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        super.writeStart(os);
        byte[] data = new byte[11];
        copySF(data, Type.MAP, Category.DATA_RESOURCE);

        int tripletDataLen = getTripletDataLength();

        byte[] len = BinaryUtils.convert(10 + tripletDataLen, 2);
        data[1] = len[0];
        data[2] = len[1];

        len = BinaryUtils.convert(2 + tripletDataLen, 2);
        data[9] = len[0];
        data[10] = len[1];

        os.write(data);
        writeTriplets(os);
    }
}
