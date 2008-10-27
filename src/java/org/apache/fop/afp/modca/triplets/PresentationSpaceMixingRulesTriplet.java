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
 * This triplet is used to specify the resulting appearance when data in a new
 * presentation space is merged with data in an existing presentation space.
 */
public class PresentationSpaceMixingRulesTriplet extends Triplet {

    /** background on background mixing rule */ 
    public static final byte RULE_BACK_ON_BACK = 0x70;

    /** background on foreground mixing rule */ 
    public static final byte RULE_BACK_ON_FORE = 0x71;

    /** foreground on background mixing rule */ 
    public static final byte RULE_FORE_ON_BACK = 0x72;

    /** foreground on foreground mixing rule */ 
    public static final byte RULE_FORE_ON_FORE = 0x73;

    
    /** overpaint */
    public static final byte OVERPAINT = (byte)0x01;
    
    /** underpaint */
    public static final byte UNDERPAINT = (byte)0x02;

    /** blend */
    public static final byte BLEND = (byte)0x03;

    /** MO:DCA default mixing */
    public static final byte DEFAULT = (byte)0xFF;
    
    
    /**
     * Main constructor
     * 
     * @param rules the mixing rules
     */
    public PresentationSpaceMixingRulesTriplet(byte[] rules) {
        super(PRESENTATION_SPACE_MIXING_RULE, rules);
    }
}
