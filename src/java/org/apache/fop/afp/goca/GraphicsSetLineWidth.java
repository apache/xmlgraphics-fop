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
public class GraphicsSetLineWidth extends AbstractGraphicsDrawingOrder {

    /** line width multiplier */
    private int multiplier = 1;

    /**
     * Main constructor
     *
     * @param multiplier the line width multiplier
     */
    public GraphicsSetLineWidth(int multiplier) {
        this.multiplier = multiplier;
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 2;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = new byte[] {
           getOrderCode(), // GSLW order code
           (byte)multiplier // MH (line-width)
        };
        os.write(data);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "GraphicsSetLineWidth{multiplier=" + multiplier + "}";
    }

    /** {@inheritDoc} */
    byte getOrderCode() {
        return 0x19;
    }
}
