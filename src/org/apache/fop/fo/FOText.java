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
import org.apache.fop.layoutmgr.TextBPLayoutManager;

import java.util.NoSuchElementException;
import java.util.List;

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
    TextState ts;

    public FOText(char[] chars, int s, int e, TextInfo ti) {
        super(null);
        this.start = 0;
        this.ca = new char[e - s];
        System.arraycopy(chars, s, ca, 0, e - s);
        this.length = e - s;
        textInfo = ti;
    }

    public boolean willCreateArea() {
        if (textInfo.whiteSpaceCollapse == WhiteSpaceCollapse.FALSE &&
                length > 0) {
            return true;
        }

        for (int i = start; i < start + length; i++) {
            char ch = ca[i];
            if (!((ch == ' ') || (ch == '\n') || (ch == '\r') ||
                    (ch == '\t'))) { // whitespace
                return true;
            }
        }
        return false;
    }

    // Just to keep PageNumber and PageNumber citation happy for now.
    // The real code is moved to TextLayoutManager!

    public static int addText(BlockArea ba, FontState fontState,
                              float red, float green, float blue, int wrapOption,
                              LinkSet ls, int whiteSpaceCollapse, char data[],
                              int start, int end, TextState textState, int vAlign) {
        return 0;
    }

    public void addLayoutManager(List list) {
        // What if nothing left (length=0)?
        if (length < ca.length) {
            char[] tmp = ca;
            ca = new char[length];
            System.arraycopy(tmp, 0, ca, 0, length);
        }
        // list.add(new TextLayoutManager(this, ca, textInfo));
	// TEST VARIANT USING Karen's BreakPoss scheme
        list.add(new TextBPLayoutManager(this, ca, textInfo));
    }

    public CharIterator charIterator() {
        return new TextCharIterator();
    }

    private class TextCharIterator extends AbstractCharIterator {
        int curIndex = 0;
        public boolean hasNext() {
            return (curIndex < length);
        }

        public char nextChar() {
            if (curIndex < length) {
                // Just a char class? Don't actually care about the value!
                return ca[curIndex++];
            } else
                throw new NoSuchElementException();
        }

        public void remove() {
            if (curIndex > 0 && curIndex < length) {
                // copy from curIndex to end to curIndex-1
                System.arraycopy(ca, curIndex, ca, curIndex - 1,
                                 length - curIndex);
                length--;
                curIndex--;
            } else if (curIndex == length) {
                curIndex = --length;
            }
        }


        public void replaceChar(char c) {
            if (curIndex > 0 && curIndex <= length) {
                ca[curIndex - 1] = c;
            }
        }


    }
}

