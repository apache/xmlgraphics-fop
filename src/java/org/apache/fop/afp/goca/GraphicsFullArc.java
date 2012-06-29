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

import org.apache.fop.afp.util.BinaryUtils;

/**
 * A GOCA graphics arc (circle/ellipse)
 */
public class GraphicsFullArc extends AbstractGraphicsCoord {

    /** the integer portion of the multiplier */
    private final int mh;

    /** the fractional portion of the multiplier */
    private final int mhr;

    /**
     * Constructor
     *
     * @param x the x coordinate of the center of the circle/ellipse
     * @param y the y coordinate of the center of the circle/ellipse
     * @param mh the integer portion of the multiplier
     * @param mhr the fractional portion of the multiplier
     */
    public GraphicsFullArc(int x, int y, int mh, int mhr) {
        super(x, y);
        this.mh = mh;
        this.mhr = mhr;
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 8;
    }

    /** {@inheritDoc} */
    byte getOrderCode() {
        return (byte)0xC7;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = getData();

        // integer portion of multiplier
        data[6] = BinaryUtils.convert(mh, 1)[0];

        // fractional portion of multiplier
        data[7] = BinaryUtils.convert(mhr, 1)[0];

        os.write(data);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "GraphicsFullArc{"
             + ", centerx=" + coords[0]
             + ", centery=" + coords[1]
             + ", mh=" + mh
             + ", mhr=" + mhr
         + "}";
    }

}
