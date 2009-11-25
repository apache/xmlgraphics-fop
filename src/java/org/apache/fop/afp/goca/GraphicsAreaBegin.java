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
 * The beginning of a filled region (graphics area).
 */
public class GraphicsAreaBegin extends AbstractGraphicsDrawingOrder {

    private static final int RES1 = 128;
    private static final int BOUNDARY = 64;
    private static final int NO_BOUNDARY = 0;

    /** draw boundary lines around this area */
    private boolean drawBoundary = false;

    /**
     * Sets whether boundary lines are drawn
     *
     * @param drawBoundaryLines whether boundary lines are drawn
     */
    public void setDrawBoundaryLines(boolean drawBoundaryLines) {
        this.drawBoundary = drawBoundaryLines;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = new byte[] {
            getOrderCode(), // GBAR order code
            (byte)(RES1 + (drawBoundary ? BOUNDARY : NO_BOUNDARY))
        };
        os.write(data);
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 2;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "GraphicsAreaBegin{drawBoundary=" + drawBoundary + "}";
    }

    /** {@inheritDoc} */
    byte getOrderCode() {
        return 0x68;
    }
}
