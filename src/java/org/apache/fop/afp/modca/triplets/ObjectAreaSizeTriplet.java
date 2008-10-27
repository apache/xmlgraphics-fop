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

package org.apache.fop.afp.modca.triplets;

import org.apache.fop.afp.util.BinaryUtils;

/**
 * The Object Area Size triplet is used to specify the extent of an object area
 * in the X and Y directions
 */
public class ObjectAreaSizeTriplet extends Triplet {

    /**
     * Main constructor
     * 
     * @param x the object area extent for the X axis
     * @param y the object area extent for the Y axis
     * @param type the object area size type
     */
    public ObjectAreaSizeTriplet(int x, int y, byte type) {
        super(Triplet.OBJECT_AREA_SIZE);
        byte[] xOASize = BinaryUtils.convert(x, 3);
        byte[] yOASize = BinaryUtils.convert(y, 3);
        byte[] data = new byte[] {
            type, // SizeType
            xOASize[0], // XoaSize - Object area extent for X axis
            xOASize[1],
            xOASize[2],
            yOASize[0], // YoaSize - Object area extent for Y axis
            yOASize[1],
            yOASize[2]
        };
        super.setData(data);
    }

    /**
     * Main constructor
     * 
     * @param x the object area extent for the X axis
     * @param y the object area extent for the Y axis
     */
    public ObjectAreaSizeTriplet(int x, int y) {
        this(x, y, (byte)0x02);
    }
}
