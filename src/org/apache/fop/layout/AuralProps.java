/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

/**
 * Store all hyphenation related properties on an FO.
 * Public "structure" allows direct member access.
 */
public class AuralProps {
    public int azimuth;
    public String cueAfter;
    public String cueBefore;
    public int elevation;
    public int pauseAfter;
    public int pauseBefore;
    public int pitch;
    public int pitchRange;
    public int playDuring;
    public int richness;
    public int speak;
    public int speakHeader;
    public int speakNumeral;
    public int speakPunctuation;
    public int speechRate;
    public int stress;
    public int voiceFamily;
    public int volume;

    public AuralProps() {}

}
