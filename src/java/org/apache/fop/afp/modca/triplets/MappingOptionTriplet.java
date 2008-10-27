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

package org.apache.fop.afp.modca.triplets;

/**
 * Specifies the mapping of data object presentation space to object area
 */
public class MappingOptionTriplet extends Triplet {
    /**
     * the data object is placed in the upper left corner, all data must be presented
     * within the object area extents
     */
    public static final byte POSITION = 0x00;

    /**
     * the data object is placed in the upper left corner, all data that falls within
     * the object area extents will be presented but data that falls outside will not be presented
     */
    public static final byte POSITION_AND_TRIM = 0x10;
    
    /**
     * the data object is centred and symmetrically scaled up or down
     * while preserving aspect ratio
     */
    public static final byte SCALE_TO_FIT = 0x20;
    
    /**
     * the data object is centred, all data that falls within the object area extents
     * will be presented but data that falls outside will not be presented
     */
    public static final byte CENTER_AND_TRIM = 0x30;
    
//    public static final byte MIGRATION_MAPPING_1 = 0x41;
//    public static final byte MIGRATION_MAPPING_2 = 0x42;
//    public static final byte MIGRATION_MAPPING_3 = 0x50;
    
    /** the data object is centred, aspect ratio is not always preserved */
    public static final byte SCALE_TO_FILL = 0x60;
    
    /** used to map ip3i print data objects */
    public static final byte UP3I_PRINT_DATA = 0x70;

    /**
     * Main constructor
     * 
     * @param mapValue the mapping option to use
     */
    public MappingOptionTriplet(byte mapValue) {
        super(Triplet.MAPPING_OPTION, mapValue);
    }
}
