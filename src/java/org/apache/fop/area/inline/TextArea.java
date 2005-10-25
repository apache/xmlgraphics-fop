/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

package org.apache.fop.area.inline;

import org.apache.fop.util.CharUtilities;

/**
 * A text inline area.
 */
public class TextArea extends AbstractTextArea {

    /**
     * Create a text inline area
     */
    public TextArea() {
    }

    /**
     * Constructor with extra parameters:
     * create a TextAdjustingInfo object
     * @param stretch  the available stretch of the text
     * @param shrink   the available shrink of the text
     * @param adj      the current total adjustment
     */
    public TextArea(int stretch, int shrink, int adj) {
        super(stretch, shrink, adj);
    }

    /**
     * Set the text string
     *
     * @param t the text string
     */
    public void setText(String t) {
        // split the text and create WordAreas and SpaceAreas
        char charArray[] = t.toCharArray();
        int wordStartIndex = -1;
        for (int i = 0; i < charArray.length; i ++) {
            if (CharUtilities.isAnySpace(charArray[i])) {
                // a space character
                // create a SpaceArea child
                SpaceArea space = new SpaceArea(charArray[i]);
                this.addChildArea(space);
                space.setParentArea(this);
            } else {
                // a non-space character
                if (wordStartIndex == -1) {
                    // first character of the text, or after a space
                    wordStartIndex = i;
                }
                if (i == charArray.length - 1
                        || CharUtilities.isAnySpace(charArray[i + 1])) {
                    // last character before the end of the text or a space:
                    // create a WordArea child
                    WordArea word = new WordArea(t.substring(wordStartIndex, i + 1));
                    this.addChildArea(word);
                    word.setParentArea(this);
                    wordStartIndex = -1;
                }
            }
        }
    }

    /**
     * Get the text string.
     *
     * @return the text string
     */
    public String getTextArea() {
        StringBuffer text = new StringBuffer();
        InlineArea child;
        // assemble the text
        for (int i = 0; i < inlines.size(); i ++) {
            child = (InlineArea) inlines.get(i);
            if (child instanceof WordArea) {
                text.append(((WordArea) child).getWord());
            } else {
                text.append(((SpaceArea) child).getSpace());
            }
        }
        return text.toString();
    }

    /**
     * set the ipd and notify the parent area about the variation;
     * this happens when a page-number or a page-number-citation
     * is resolved to its actual value
     * @param newIPD the new ipd of the area
     */
    public void updateIPD(int newIPD) {
        // remember the old ipd
        int oldIPD = getIPD();
        // set the new ipd
        setIPD(newIPD);
        // check if the line needs to be adjusted because of the ipd variation
        if (newIPD != oldIPD) {
            notifyIPDVariation(newIPD - oldIPD);
        }
    }

}

