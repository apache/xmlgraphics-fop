/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
 */
public class Character extends InlineArea {
    private char character;

    /**
     * Create a new characater inline area with the given character.
     *
     * @param ch the character for this inline area
     */
    public Character(char ch) {
        character = ch;
    }

    /**
     * Get the character for this inline character area.
     *
     * @return the character
     */
    public char getChar() {
        return character;
    }
}

