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

package org.apache.fop.fo.flow;

import java.util.NoSuchElementException;

import org.apache.fop.fo.CharIterator;

/**
 * Used by the RecursiveCharIterator to iterate over a Character
 */
public class FOCharIterator extends CharIterator {

    private boolean bFirst = true;
    private Character foChar;
    
    /**
     * Main constructor
     * @param foChar the FOCharacter
     */
    protected FOCharIterator(Character foChar) {
        this.foChar = foChar;
    }
    
    /**
     * @return true if this iterator has another character available
     */
    public boolean hasNext() {
        return bFirst;
    }

    /**
     * @return the next character
     */
    public char nextChar() {
        if (bFirst) {
            bFirst = false;
            return foChar.character;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Removes the child from the parent
     */
    public void remove() {
        foChar.getParent().removeChild(foChar);
    }

    /**
     * Replaces the character with another one
     * @param c the replacement character
     */
    public void replaceChar(char c) {
        foChar.character = c;
    }

}