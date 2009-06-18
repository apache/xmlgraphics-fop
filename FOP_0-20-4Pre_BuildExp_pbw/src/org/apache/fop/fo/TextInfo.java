/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.layout.FontState;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.traits.SpaceVal;

/**
 * Collection of properties used in
 */
    public class TextInfo {
        public FontState fs;
        public ColorType color;
        public int wrapOption;
        public boolean bWrap ; // True if wrap-option = WRAP
        public int whiteSpaceCollapse;
        public int verticalAlign;
        public int lineHeight;

	// Props used for calculating inline-progression-dimension
        public SpaceVal wordSpacing;
        public SpaceVal letterSpacing;

	// Add hyphenation props too

        // Textdecoration
        public boolean underlined = false;
        public boolean overlined = false;
        public boolean lineThrough = false;
    }

