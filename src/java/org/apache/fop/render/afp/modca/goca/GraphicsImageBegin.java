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

package org.apache.fop.render.afp.modca.goca;

import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * A GOCA graphics begin image object
 */
public final class GraphicsImageBegin extends AbstractPreparedAFPObject {
    /** x coordinate */
    private int x;

    /** y coordinate */
    private int y;

    /** width */
    private int width;

    /** height */
    private int height;

    /**
     * @param x the x coordinate of the image
     * @param y the y coordinate of the image
     * @param width the image width
     * @param height the image height
     */
    public GraphicsImageBegin(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        prepareData();
    }

    /**
     * {@inheritDoc}
     */
    protected void prepareData() {
        byte[] xcoord = BinaryUtils.convert(x, 2);
        byte[] ycoord = BinaryUtils.convert(y, 2);
        byte[] w = BinaryUtils.convert(width, 2);
        byte[] h = BinaryUtils.convert(height, 2);
        super.data = new byte[] {
            (byte) 0xD1, // GBIMG order code
            (byte) 0x0A, // LENGTH
            xcoord[0],
            xcoord[1],
            ycoord[0],
            ycoord[1],
            0x00, // FORMAT
            0x00, // RES
            w[0], // WIDTH
            w[1], //
            h[0], // HEIGHT
            h[1] //
        };
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "GraphicsImageBegin(x=" + x + ",y=" + y
            + ",width=" + width + ",height=" + height + ")";
    }
}