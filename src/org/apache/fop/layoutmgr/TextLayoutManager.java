/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOText; // For TextInfo: TODO: make independent!
import org.apache.fop.area.Area;
import org.apache.fop.area.inline.Word;

import java.util.ListIterator;

/**
 * LayoutManager for text (a sequence of characters) which generates one
 * more inline areas.
 */
public class TextLayoutManager extends LeafNodeLayoutManager {

    private char[] chars;
    private FOText.TextInfo textInfo;

    public TextLayoutManager(FObj fobj, char[] chars, 
			     FOText.TextInfo textInfo) {
	super(fobj);
	this.chars = chars;
	this.textInfo = textInfo;
    }

    /**
     * Generate inline areas for words in text.
     */
    public void generateAreas() {
	// Handle white-space characteristics. Maybe there is no area to
	// generate....

	// Iterate over characters and make text areas.
	// Add each one to parent. Handle word-space.
	Word curWordArea = new Word();
	curWordArea.setWord(new String(chars));
	setCurrentArea(curWordArea);
	flush();
    }



    /** Try to split the word area by hyphenating the word. */
    public boolean splitArea(Area areaToSplit, SplitContext context) {
	context.nextArea = areaToSplit;
	return false;
    }


}
