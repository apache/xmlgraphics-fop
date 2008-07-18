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

package org.apache.fop.render.afp.modca.triplets;

/**
 * This triplet is used to specify the resulting appearance when data in a new
 * presentation space is merged with data in an existing presentation space.
 */
public class PresentationSpaceResetMixingTriplet extends Triplet {

    /**
     * Do not reset to the color of the medium prior to
     * placing data into this MO:DCA presentation space.
     */
    public static final byte NOT_RESET = 0x00;
    
    /**
     * Reset to the color of the medium prior to placing
     * data into this MO:DCA presentation space.
     */
    public static final byte RESET = 0x01;
    
    /**
     * Main constructor
     * 
     * @param backgroundMixFlag the background mixing flag
     */
    public PresentationSpaceResetMixingTriplet(byte backgroundMixFlag) {
        super(PRESENTATION_SPACE_RESET_MIXING, backgroundMixFlag);
    }
}
