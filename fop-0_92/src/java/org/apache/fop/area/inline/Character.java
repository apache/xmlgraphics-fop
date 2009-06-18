/*
 * Copyright 1999-2006 The Apache Software Foundation.
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

/**
 * Single character inline area.
 * This inline area holds a single character.
 * @deprecated A TextArea with a single WordArea as its child should be used instead.
 */
public class Character extends AbstractTextArea {
    // use a String instead of a character because if this character
    // ends a syllable the hyphenation character must be added
    private String character;

    /**
     * Create a new character inline area with the given character.
     *
     * @param ch the character for this inline area
     */
    public Character(char ch) {
        character = new String() + ch;
    }

    /**
     * Get the character for this inline character area.
     *
     * @return the character
     */
    public String getChar() {
        return character;
    }

    /**
     * Add the hyphenation character and its length.
     *
     * @param hyphChar the hyphenation character
     * @param hyphSize the size of the hyphenation character
     */
    public void addHyphen(char hyphChar, int hyphSize) {
        character += hyphChar;
        this.setIPD(this.getIPD() + hyphSize);
    }

}

