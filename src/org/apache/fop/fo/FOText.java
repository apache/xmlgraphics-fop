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
import org.apache.fop.system.BufferManager;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.TextLayoutManager;

/**
 * a text node in the formatting object tree
 *
 * Modified by Mark Lillywhite, mark-fop@inomial.com.
 * Unfortunately the BufferManager implementatation holds
 * onto references to the character data in this object
 * longer than the lifetime of the object itself, causing
 * excessive memory consumption and OOM errors.
 */
public class FOText extends FObj {

    protected char[] ca;
    protected int start;
    protected int length;
    TextInfo textInfo;

    public static class TextInfo {
        FontState fs;
        float red;
        float green;
        float blue;
        int wrapOption;
        int whiteSpaceCollapse;
        int verticalAlign;

        // Textdecoration
        protected boolean underlined = false;
        protected boolean overlined = false;
        protected boolean lineThrough = false;
    }

    TextState ts;

    public FOText(char[] chars, int s, int e, TextInfo ti) {
        super(null);
        this.start = 0;
        this.ca = new char[e - s];
	System.arraycopy(chars, s, ca, 0, e-s);
        this.length = e - s;
        textInfo = ti;
    }

    public boolean willCreateArea() {
        if (textInfo.whiteSpaceCollapse == WhiteSpaceCollapse.FALSE
                && length > 0) {
            return true;
        }

        for (int i = start; i < start + length; i++) {
            char ch = ca[i];
            if (!((ch == ' ') || (ch == '\n') || (ch == '\r')
                    || (ch == '\t'))) {    // whitespace
                return true;
            }
        }
        return false;
    }

    // Just to keep PageNumber and PageNumber citation happy for now.
    // The real code is moved to TextLayoutManager!

    public static int addText(BlockArea ba, FontState fontState, float red,
                              float green, float blue, int wrapOption,
                              LinkSet ls, int whiteSpaceCollapse,
                              char data[], int start, int end,
                              TextState textState, int vAlign) {
	return 0;
    }

    public LayoutManager getLayoutManager() {
	return new TextLayoutManager(this, ca, textInfo);
    }
}

