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
 * The Object Area Size triplet is used to specify the extent of an object area
 * in the X and Y directions
 */
public class ObjectAreaSizeTriplet extends AbstractTriplet {

    private final int x;
    private final int y;
    private final byte type;

    /**
     * Main constructor
     *
     * @param x the object area extent for the X axis
     * @param y the object area extent for the Y axis
     * @param type the object area size type
     */
    public ObjectAreaSizeTriplet(int x, int y, byte type) {
        super(AbstractTriplet.OBJECT_AREA_SIZE);
        this.x = x;
        this.y = y;
        this.type = type;
    }

    /**
     * Main constructor
     *
     * @param x the object area extent for the X axis
     * @param y the object area extent for the Y axis
     */
    public ObjectAreaSizeTriplet(int x, int y) {
        this(x, y, (byte)0x02);
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 9;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = getData();

        data[2] = type; // SizeType

        byte[] xOASize = BinaryUtils.convert(x, 3);
        data[3] = xOASize[0]; // XoaSize - Object area extent for X axis
        data[4] = xOASize[1];
        data[5] = xOASize[2];

        byte[] yOASize = BinaryUtils.convert(y, 3);
        data[6] = yOASize[0]; // YoaSize - Object area extent for Y axis
        data[7] = yOASize[1];
        data[8] = yOASize[2];

        os.write(data);
    }
}
