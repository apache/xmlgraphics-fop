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
 * The Measurement Units triplet is used to specify the units of measure
 * for a presentation space
 */
public class MeasurementUnitsTriplet extends AbstractTriplet {

    private static final byte TEN_INCHES = 0x00;
    private static final byte TEN_CM = 0x01;
    private final int xRes;
    private final int yRes;

    /**
     * Main constructor
     *
     * @param xRes units per base on the x-axis
     * @param yRes units per base on the y-axis
     */
    public MeasurementUnitsTriplet(int xRes, int yRes) {
        super(MEASUREMENT_UNITS);
        this.xRes = xRes;
        this.yRes = yRes;
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 8;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = getData();

        data[2] = TEN_INCHES; // XoaBase
        data[3] = TEN_INCHES; // YoaBase

        byte[] xUnits = BinaryUtils.convert(xRes * 10, 2);
        data[4] = xUnits[0]; // XoaUnits (x units per unit base)
        data[5] = xUnits[1];

        byte[] yUnits = BinaryUtils.convert(yRes * 10, 2);
        data[6] = yUnits[0]; // YoaUnits (y units per unit base)
        data[7] = yUnits[1];

        os.write(data);
    }
}
