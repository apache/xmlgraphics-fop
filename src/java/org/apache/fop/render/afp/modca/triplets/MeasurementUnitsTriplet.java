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

package org.apache.fop.render.afp.modca.triplets;

import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * The Measurement Units triplet is used to specify the units of measure
 * for a presentation space
 */
public class MeasurementUnitsTriplet extends Triplet {

    private static final byte TEN_INCHES = 0x00; 
    private static final byte TEN_CM = 0x01;
    
    /**
     * Main constructor
     */
    public MeasurementUnitsTriplet() {
        super(MEASUREMENT_UNITS);
        //TODO: units correct?
        byte[] xUnits = BinaryUtils.convert(1, 2);
        byte[] yUnits = BinaryUtils.convert(1, 2);
        byte[] data = new byte[] {
             TEN_INCHES, // XoaBase
             TEN_INCHES, // YoaBase
             xUnits[0], // XoaUnits (x units per unit base)                
             xUnits[1],
             yUnits[0], // YoaUnits (y units per unit base)
             yUnits[1]
        };
        super.setData(data);
    }
}
