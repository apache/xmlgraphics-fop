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

import java.util.NoSuchElementException;

/**
 * Abstract base class for character iterators.
 */
public abstract class AbstractCharIterator implements CharIterator, Cloneable {

    /**
     * @see java.util.Iterator#hasNext()
     */
    public abstract boolean hasNext();

    /**
     * @see org.apache.fop.fo.CharIterator#nextChar()
     */
    public abstract char nextChar() throws NoSuchElementException ;

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
     * @see org.apache.fop.fo.CharIterator#replaceChar(char)
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

