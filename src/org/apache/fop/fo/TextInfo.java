/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.layout.Area;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.TextLayoutManager;

import java.util.NoSuchElementException;

/**
 */
    public class TextInfo {
        public FontState fs;
        public ColorType color;
        public int wrapOption;
        public int whiteSpaceCollapse;
        public int verticalAlign;
        public int lineHeight;

        // Textdecoration
        public boolean underlined = false;
        public boolean overlined = false;
        public boolean lineThrough = false;
    }

