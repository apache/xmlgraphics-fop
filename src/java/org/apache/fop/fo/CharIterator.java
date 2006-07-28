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
 
package org.apache.fop.fo;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Abstract base class for iterators that should iterate through a series
 * of characters.  Extends the java.util.Iterator interface with some
 * additional functions useful for FOP's management of text.
 */
public abstract class CharIterator implements Iterator, Cloneable {

    /**
     * @see java.util.Iterator#hasNext()
     */
    public abstract boolean hasNext();

    /**
     * @return the character that is the next character in the collection
     * @throws NoSuchElementException if there are no more characters (test for
     * this condition with java.util.Iterator.hasNext()).
     */
    public abstract char nextChar() throws NoSuchElementException;

    /**
     * @see java.util.Iterator#next()
     */
    public Object next() throws NoSuchElementException {
        return new Character(nextChar());
    }

    /**
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }


    /**
     * Replace the current character managed by the iterator with a specified
     * character?
     * @param c character
     */
    public void replaceChar(char c) {
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }
}

