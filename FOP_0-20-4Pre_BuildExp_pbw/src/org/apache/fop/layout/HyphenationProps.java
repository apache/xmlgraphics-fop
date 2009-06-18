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
public class HyphenationProps {
    public int hyphenate;      // Enum true or false: store as boolean!
    public char hyphenationChar;
    public int hyphenationPushCharacterCount;
    public int hyphenationRemainCharacterCount;
    public String language;    // Language code or enum "NONE"
    public String country;     // Country code or enum "NONE"

    public HyphenationProps() {}

}
