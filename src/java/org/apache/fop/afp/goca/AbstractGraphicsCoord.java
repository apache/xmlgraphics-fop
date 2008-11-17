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

import org.apache.fop.afp.modca.AbstractNamedAFPObject;
import org.apache.fop.afp.modca.StructuredDataObject;
import org.apache.fop.afp.util.BinaryUtils;

/**
 * A base class encapsulating the structure of coordinate based GOCA objects
 */
public abstract class AbstractGraphicsCoord extends AbstractNamedAFPObject
    implements StructuredDataObject {

    /** array of x/y coordinates */
    protected int[] coords = null;

    /**
     * Constructor
     *
     * @param coords the x/y coordinates for this object
     */
    public AbstractGraphicsCoord(int[] coords) {
        this.coords = coords;
    }

    /**
     * Constructor
     *
     * @param x the x coordinate for this object
     * @param y the y coordinate for this object
     */
    public AbstractGraphicsCoord(int x, int y) {
        this(new int[] {x, y});
    }

    /**
     * Constructor
     *
     * @param x1 the x1 coordinate for this object
     * @param y1 the y1 coordinate for this object
     * @param x2 the x2 coordinate for this object
     * @param y2 the y2 coordinate for this object
     */
    public AbstractGraphicsCoord(int x1, int y1, int x2, int y2) {
        this(new int[] {x1, y1, x2, y2});
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 2 + (coords.length * 2);
    }

    /**
     * Returns the order code of this structured field
     *
     * @return the order code of this structured field
     */
    abstract byte getOrderCode();

    /**
     * Returns the coordinate data start index
     *
     * @return the coordinate data start index
     */
    int getCoordinateDataStartIndex() {
        return 2;
    }

    /**
     * Returns the coordinate data
     *
     * @return the coordinate data
     */
    byte[] getData() {
        int len = getDataLength();
        byte[] data = new byte[len];
        data[0] = getOrderCode();
        data[1] = (byte)(len - 2);

        if (coords != null) {
            addCoords(data, getCoordinateDataStartIndex());
        }

        return data;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        os.write(getData());
    }

    /**
     * Adds the coordinates to the structured field data
     *
     * @param data the structured field data
     * @param fromIndex the start index
     */
    protected void addCoords(byte[] data, int fromIndex) {
        // X/Y POS
        for (int i = 0; i < coords.length; i++, fromIndex += 2) {
            byte[] coord = BinaryUtils.convert(coords[i], 2);
            data[fromIndex] = coord[0];
            data[fromIndex + 1] = coord[1];
        }
    }

    /**
     * Returns the short name of this GOCA object
     *
     * @return the short name of this GOCA object
     */
    public String getName() {
        String className = getClass().getName();
        return className.substring(className.lastIndexOf(".") + 1);
    }

    /** {@inheritDoc} */
    public String toString() {
        String coordsStr = "";
        for (int i = 0; i < coords.length; i++) {
            coordsStr += (i % 2 == 0) ? "x" : "y";
            coordsStr += (i / 2) + "=" + coords[i] + ",";
        }
        coordsStr = coordsStr.substring(0, coordsStr.length() - 1);
        return getName() + "{" + coordsStr + "}";
    }
}