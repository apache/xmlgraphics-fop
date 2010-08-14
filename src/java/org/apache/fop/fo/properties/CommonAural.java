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

package org.apache.fop.fo.properties;

import org.apache.fop.fo.PropertyList;

/**
 * Stores all common aural properties.
 * See Sec. 7.6 of the XSL-FO Standard.
 * Public "structure" allows direct member access.
 */
public class CommonAural {
    /**
     * The "azimuth" property.
     */
    public int azimuth;                                         // CSOK: VisibilityModifier

    /**
     * The "cueAfter" property.
     */
    public String cueAfter;                                     // CSOK: VisibilityModifier

    /**
     * The "cueBefore" property.
     */
    public String cueBefore;                                    // CSOK: VisibilityModifier

    /**
     * The "elevation" property.
     */
    public int elevation;                                       // CSOK: VisibilityModifier

    /**
     * The "pauseAfter" property.
     */
    public int pauseAfter;                                      // CSOK: VisibilityModifier

    /**
     * The "pauseBefore" property.
     */
    public int pauseBefore;                                     // CSOK: VisibilityModifier

    /**
     * The "pitch" property.
     */
    public int pitch;                                           // CSOK: VisibilityModifier

    /**
     * The "pitch-range" property.
     */
    public int pitchRange;                                      // CSOK: VisibilityModifier

    /**
     * The "playDuring" property.
     */
    public int playDuring;                                      // CSOK: VisibilityModifier

    /**
     * The "richness" property.
     */
    public int richness;                                        // CSOK: VisibilityModifier

    /**
     * The "speak" property.
     */
    public int speak;                                           // CSOK: VisibilityModifier

    /**
     * The "speak-header" property.
     */
    public int speakHeader;                                     // CSOK: VisibilityModifier

    /**
     * The "speak-numeral" property.
     */
    public int speakNumeral;                                    // CSOK: VisibilityModifier

    /**
     * The "speak-punctuation" property.
     */
    public int speakPunctuation;                                // CSOK: VisibilityModifier

    /**
     * The "speech-rate" property.
     */
    public int speechRate;                                      // CSOK: VisibilityModifier

    /**
     * The "stress" property.
     */
    public int stress;                                          // CSOK: VisibilityModifier

    /**
     * The "voice-family" property.
     */
    public int voiceFamily;                                     // CSOK: VisibilityModifier

    /**
     * The "volume" property.
     */
    public int volume;                                          // CSOK: VisibilityModifier

    /**
     * Create a CommonAbsolutePosition object.
     * @param pList The PropertyList with propery values.
     */
    public CommonAural(PropertyList pList) {
    }
}
