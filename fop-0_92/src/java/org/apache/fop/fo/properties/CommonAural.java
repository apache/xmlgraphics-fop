/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
    public int azimuth;

    /**
     * The "cueAfter" property.
     */
    public String cueAfter;

    /**
     * The "cueBefore" property.
     */
    public String cueBefore;

    /**
     * The "elevation" property.
     */
    public int elevation;

    /**
     * The "pauseAfter" property.
     */
    public int pauseAfter;

    /**
     * The "pauseBefore" property.
     */
    public int pauseBefore;

    /**
     * The "pitch" property.
     */
    public int pitch;

    /**
     * The "pitch-range" property.
     */
    public int pitchRange;

    /**
     * The "playDuring" property.
     */
    public int playDuring;

    /**
     * The "richness" property.
     */
    public int richness;

    /**
     * The "speak" property.
     */
    public int speak;

    /**
     * The "speak-header" property.
     */
    public int speakHeader;

    /**
     * The "speak-numeral" property.
     */
    public int speakNumeral;

    /**
     * The "speak-punctuation" property.
     */
    public int speakPunctuation;

    /**
     * The "speech-rate" property.
     */
    public int speechRate;

    /**
     * The "stress" property.
     */
    public int stress;

    /**
     * The "voice-family" property.
     */
    public int voiceFamily;

    /**
     * The "volume" property.
     */
    public int volume;

    /**
     * Create a CommonAbsolutePosition object.
     * @param pList The PropertyList with propery values.
     */
    public CommonAural(PropertyList pList) {
    }
}
