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

package org.apache.fop.fo;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Interface for iterators that should iterate through a series of characters.
 * Extends the java.util.Iterator interface with some additional functions
 * useful for FOP's management of text.
 */
public interface CharIterator extends Iterator {

    /**
     * @return the character that is the next character in the collection
     * @throws NoSuchElementException if there are no more characters (test for
     * this condition with java.util.Iterator.hasNext()).
     */
    char nextChar() throws NoSuchElementException ;

    /**
     * Replace the current character managed by the iterator with a specified
     * character?
     * @param c character
     */
    void replaceChar(char c);

    /**
     * @return cloned Object
     */
    Object clone();
}
