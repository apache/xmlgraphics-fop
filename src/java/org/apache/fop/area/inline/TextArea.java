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

import java.util.Arrays;

import org.apache.fop.util.CharUtilities;

/**
 * A text inline area.
 */
public class TextArea extends AbstractTextArea {

    private static final long serialVersionUID = 7315900267242540809L;

    private boolean isHyphenated;

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
        addWord(word, 0, null, null, null, offset);
    }

    /**
     * Create and add a WordArea child to this TextArea.
     *
     * @param word   the word string
     * @param offset the offset for the next area
     * @param level  bidirectional level that applies to entire word
     */
    public void addWord(String word, int offset, int level) {
        addWord(word, 0, null, makeLevels(level, word.length()), null, offset);
    }

    /**
     * Create and add a WordArea child to this TextArea.
     *
     * @param word the word string
     * @param ipd the word's ipd
     * @param letterAdjust the letter adjustment array (may be null)
     * @param levels array of resolved bidirectional levels of word characters,
     * or null if default level
     * @param gposAdjustments array of general position adjustments or null if none apply
     * @param blockProgressionOffset the offset for the next area
     */
    public void addWord(
        String word, int ipd, int[] letterAdjust, int[] levels,
          int[][] gposAdjustments, int blockProgressionOffset) {
        int minWordLevel = findMinLevel(levels, getBidiLevel());
        WordArea wordArea = new WordArea(
            blockProgressionOffset, minWordLevel, word, letterAdjust, levels, gposAdjustments);
        wordArea.setIPD(ipd);
        addChildArea(wordArea);
        wordArea.setParentArea(this);
        updateLevel(minWordLevel);
    }

    /**
     * Create and add a SpaceArea child to this TextArea
     *
     * @param space the space character
     * @param ipd the space's ipd
     * @param blockProgressionOffset     the offset for the next area
     * @param adjustable is this space adjustable?
     * @param level resolved bidirection level of space character
     */
    public void addSpace(
        char space, int ipd, boolean adjustable, int blockProgressionOffset, int level) {
        SpaceArea spaceArea = new SpaceArea(blockProgressionOffset, level, space, adjustable);
        spaceArea.setIPD(ipd);
        addChildArea(spaceArea);
        spaceArea.setParentArea(this);
        updateLevel(level);
    }

    /**
     * Records that the last word in this text area is hyphenated.
     */
    public void setHyphenated() {
        this.isHyphenated = true;
    }

    /**
     * Returns {@code true} if the last word in this area is hyphenated.
     */
    public boolean isHyphenated() {
        return isHyphenated;
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
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(" {text=\"");
        sb.append(CharUtilities.toNCRefs(getText()));
        sb.append("\"");
        sb.append("}");
        return sb.toString();
    }

    public void updateLevel(int newLevel) {
        if (newLevel >= 0) {
            int curLevel = getBidiLevel();
            if (curLevel >= 0) {
                if (newLevel < curLevel) {
                    setBidiLevel(newLevel);
                }
            } else {
                setBidiLevel(newLevel);
            }
        }
    }

    private static int findMinLevel(int[] levels, int defaultLevel) {
        if (levels != null) {
            int lMin = Integer.MAX_VALUE;
            for (int i = 0, n = levels.length; i < n; i++) {
                int l = levels [ i ];
                if ((l >= 0) && (l < lMin)) {
                    lMin = l;
                }
            }
            if (lMin == Integer.MAX_VALUE) {
                return -1;
            } else {
                return lMin;
            }
        } else {
            return defaultLevel;
        }
    }

    private int[] makeLevels(int level, int count) {
        if (level >= 0) {
            int[] levels = new int [ count ];
            Arrays.fill(levels, level);
            return levels;
        } else {
            return null;
        }
    }

    public int getEffectiveIPD() {
        return getIPD();
    }
}

