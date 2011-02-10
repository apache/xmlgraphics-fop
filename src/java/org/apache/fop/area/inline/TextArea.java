/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

/**
 * A text inline area.
 */
public class TextArea extends AbstractTextArea {

    private static final long serialVersionUID = 7315900267242540809L;

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
     * Remove the old text
     */
    public void removeText() {
        inlines.clear();
    }

    /**
     * Create and add a WordArea child to this TextArea.
     *
     * @param word   the word string
     * @param offset the offset for the next area
     */
    public void addWord(String word, int offset) {
        addWord(word, offset, null);
    }

    /**
     * Create and add a WordArea child to this TextArea.
     *
     * @param word   the word string
     * @param offset the offset for the next area
     * @param letterAdjust the letter adjustment array (may be null)
     */
    public void addWord(String word, int offset, int[] letterAdjust) {
        WordArea wordArea = new WordArea(word, offset, letterAdjust);
        addChildArea(wordArea);
        wordArea.setParentArea(this);
    }

    /**
     * Create and add a SpaceArea child to this TextArea
     *
     * @param space      the space character
     * @param offset     the offset for the next area
     * @param adjustable is this space adjustable?
     */
    public void addSpace(char space, int offset, boolean adjustable) {
        SpaceArea spaceArea = new SpaceArea(space, offset, adjustable);
        addChildArea(spaceArea);
        spaceArea.setParentArea(this);
    }

    /**
     * Get the whole text string.
     * Renderers whose space adjustment handling is not affected
     * by multi-byte characters can use this method to render the
     * whole TextArea at once; the other renderers (for example
     * PDFRenderer) have to implement renderWord(WordArea) and
     * renderSpace(SpaceArea) in order to correctly place each
     * text fragment.
     *
     * @return the text string
     */
    public String getText() {
        StringBuffer text = new StringBuffer();
        // assemble the text
        for (InlineArea inline : inlines) {
            if (inline instanceof WordArea) {
                text.append(((WordArea) inline).getWord());
            } else {
                text.append(((SpaceArea) inline).getSpace());
            }
        }
        return text.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "TextArea{text=" + getText() + "}";
    }
}

