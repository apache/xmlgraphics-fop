/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.FObj;
import org.apache.fop.area.Area;
import org.apache.fop.area.inline.Word;

import java.util.ListIterator;

/**
 * LayoutManager for text (a sequence of characters) which generates one
 * more inline areas.
 */
public class TextLayoutManager extends AbstractLayoutManager {

    private char[] chars;
    private Word curWordArea;

    public TextLayoutManager(FObj fobj, char[] chars) {
	super(fobj);
	this.chars = chars;
    }


    /**
     * Generate inline areas for words in text.
     */
    public void generateAreas() {
	// Iterate over characters and make text areas.
	// Add each one to parent. Handle word-space.
	curWordArea = new Word();
	curWordArea.setWord(new String(chars));
	flush();
    }


    protected void flush() {
	parentLM.addChild(curWordArea);
    }


    public boolean generatesInlineAreas() {
	return true;
    }

    /**
     * This is a leaf-node, so this method is never called.
     */
    public void addChild(Area childArea) {}


    /**
     * This is a leaf-node, so this method is never called.
     */
    public Area getParentArea(Area childArea) {
	return null;
    }
	


    /** Try to split the word area by hyphenating the word. */
    public boolean splitArea(Area areaToSplit, SplitContext context) {
	context.nextArea = areaToSplit;
	return false;
    }

}
