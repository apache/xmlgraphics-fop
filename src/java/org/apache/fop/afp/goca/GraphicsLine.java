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
 * A GOCA graphics straight line drawn from the
 * given absolute position
 */
public class GraphicsLine extends AbstractGraphicsCoord {

    /**
     * Constructor
     *
     * @param coords the x/y coordinates for this object
     *
     * @param relative is this a relative drawing order
     */
    public GraphicsLine(int[] coords, boolean relative) {
        super(coords, relative);
    }

    /** {@inheritDoc} */
    byte getOrderCode() {
        if (isRelative()) {
            return (byte)0x81;
        } else {
            return (byte)0xC1;
        }
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = getData();
        os.write(data);
    }
}
