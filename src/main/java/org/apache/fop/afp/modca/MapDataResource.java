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
import java.util.ArrayList;
import java.util.List;

import org.apache.fop.afp.modca.triplets.AbstractTriplet;
import org.apache.fop.afp.modca.triplets.Triplet;
import org.apache.fop.afp.util.BinaryUtils;

/**
 * The Map Data Resource structured field specifies resources that are
 * required for presentation.
 */
public class MapDataResource extends AbstractTripletStructuredObject {
    private List<List<AbstractTriplet>> tripletsList = new ArrayList<List<AbstractTriplet>>();

    /**
     * Main constructor
     */
    public MapDataResource() {
    }

    public void finishElement() {
        tripletsList.add(triplets);
        triplets = new ArrayList<AbstractTriplet>();
    }

    @Override
    protected int getTripletDataLength() {
        int dataLength = 0;
        for (List<AbstractTriplet> l : tripletsList) {
            dataLength += getTripletDataLength(l) + 2;
        }
        return dataLength;
    }

    private int getTripletDataLength(List<AbstractTriplet> l) {
        int dataLength = 0;
        for (Triplet triplet : l) {
            dataLength += triplet.getDataLength();
        }
        return dataLength;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        super.writeStart(os);
        byte[] data = new byte[9];
        copySF(data, Type.MAP, Category.DATA_RESOURCE);

        int tripletDataLen = getTripletDataLength();

        byte[] len = BinaryUtils.convert(8 + tripletDataLen, 2);
        data[1] = len[0];
        data[2] = len[1];
        os.write(data);

        for (List<AbstractTriplet> l : tripletsList) {
            len = BinaryUtils.convert(2 + getTripletDataLength(l), 2);
            os.write(len);
            writeObjects(l, os);
        }
    }
}
