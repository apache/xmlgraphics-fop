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
 * A string of characters without spaces
 */
public class WordArea extends InlineArea {

    /** The text for this word area */
    protected String word;

    /** The correction offset for the next area */
    protected int offset = 0;

    /** An array of width for adjusting the individual letters (optional) */
    protected int[] letterAdjust;

    /**
     * Create a word area
     * @param w the word string
     * @param o the offset for the next area
     * @param la the letter adjust array (may be null)
     */
    public WordArea(String w, int o, int[] la) {
        word = w;
        offset = o;
        this.letterAdjust = la;
    }

    /**
     * @return Returns the word.
     */
    public String getWord() {
        return word;
    }

    /**
     * @return Returns the offset.
     */
    public int getOffset() {
        return offset;
    }
    /**
     * @param o The offset to set.
     */
    public void setOffset(int o) {
        offset = o;
    }

    /** @return the array of letter adjust widths */
    public int[] getLetterAdjustArray() {
        return this.letterAdjust;
    }

}
