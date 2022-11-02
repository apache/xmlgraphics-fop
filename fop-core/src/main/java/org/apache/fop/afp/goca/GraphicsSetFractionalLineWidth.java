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

package org.apache.fop.afp.goca;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Sets the line width to use when stroking GOCA shapes (structured fields)
 */
public class GraphicsSetFractionalLineWidth extends AbstractGraphicsDrawingOrder {

    /** line width multiplier */
    private final float multiplier;

    /**
     * Main constructor
     *
     * @param multiplier the line width multiplier
     */
    public GraphicsSetFractionalLineWidth(float multiplier) {
        this.multiplier = multiplier;
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 4;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        int integral = (int) multiplier;
        int fractional = (int) ((multiplier - (float) integral) * 256);
        byte[] data = new byte[] {
                getOrderCode(), // GSLW order code
                0x02, // two bytes next
                (byte) integral, // integral line with
                (byte) fractional // and fractional
        };
        os.write(data);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "GraphicsSetFractionalLineWidth{multiplier=" + multiplier + "}";
    }

    /** {@inheritDoc} */
    byte getOrderCode() {
        return 0x11;
    }
}
