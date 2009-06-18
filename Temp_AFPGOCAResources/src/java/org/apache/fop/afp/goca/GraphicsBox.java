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

/* $Id: $ */

package org.apache.fop.afp.goca;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A GOCA graphics rectangular box
 */
public final class GraphicsBox extends AbstractGraphicsCoord {

    /**
     * Constructor
     *
     * @param coords the x/y coordinates for this object
     */
    public GraphicsBox(int[] coords) {
        super(coords);
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 12;
    }

    /** {@inheritDoc} */
    int getCoordinateDataStartIndex() {
        return 4;
    }

    /** {@inheritDoc} */
    byte getOrderCode() {
        return (byte)0xC0;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = getData();
        data[2] = (byte)0x20; // CONTROL draw control flags
        data[3] = 0x00; // reserved

        os.write(data);
    }

}