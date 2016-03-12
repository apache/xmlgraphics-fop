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

import org.apache.fop.afp.modca.triplets.MappingOptionTriplet;
import org.apache.fop.afp.util.BinaryUtils;

/**
 * The Map Container Data structured field specifies how a presentation data object
 * that is carried in an Object Container is mapped into its object area.
 */
public class MapContainerData extends AbstractTripletStructuredObject {

    /**
     * Main constructor
     *
     * @param optionValue the mapping option value
     */
    public MapContainerData(byte optionValue) {
        super.addTriplet(new MappingOptionTriplet(optionValue));
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = new byte[11];
        copySF(data, Type.MAP, Category.OBJECT_CONTAINER);
        int tripletLen = getTripletDataLength();

        byte[] len = BinaryUtils.convert(10 + tripletLen, 2);
        data[1] = len[0];
        data[2] = len[1];

        len = BinaryUtils.convert(2 + tripletLen, 2);
        data[9] = len[0];
        data[10] = len[1];
        os.write(data);
        writeTriplets(os);
    }
}
