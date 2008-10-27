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

import org.apache.fop.afp.modca.AbstractPreparedObjectContainer;

/**
 * A GOCA graphics area (container for filled shapes/objects)
 */
public final class GraphicsArea extends AbstractPreparedObjectContainer {

    private static final int RES1 = 1;
    private static final int BOUNDARY = 2;
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
    public int getDataLength() {
        // start len + end len + data len
        return 4 + super.getDataLength();
    }

    /** {@inheritDoc} */
    protected void writeStart(OutputStream os) throws IOException {
        byte[] data = new byte[] {
            (byte)0x68, // GBAR order code
            (byte)(RES1 + (drawBoundary ? BOUNDARY : NO_BOUNDARY))
        };
        os.write(data);
    }

    /** {@inheritDoc} */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] endData = new byte[] {
            (byte)0x60, // GEAR order code
            0x00, // LENGTH
        };
        os.write(endData);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "GraphicsArea{drawBoundary=" + drawBoundary + "}";
    }
}