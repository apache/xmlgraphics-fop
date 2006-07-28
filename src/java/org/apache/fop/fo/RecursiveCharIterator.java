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
 * Kind of a super-iterator that iterates through child nodes of an FONode,
 * in turn managing character iterators for each of them. Caveat: Because this
 * class is itself a CharIterator, and manages a collection of CharIterators, it
 * is easy to get confused.
 */
public class RecursiveCharIterator extends CharIterator {
    /** parent node for whose child nodes this iterator iterates */
    private FONode fobj;
    /** iterator for the child nodes */
    private Iterator childIter = null;

    /** current child object that is being managed by childIter*/
    private FONode curChild;
    /** CharIterator for curChild's characters */
    private CharIterator curCharIter = null;

    /**
     * Constructor which creates an iterator for all child nodes
     * @param fobj FONode for which an iterator should be created
     */
    public RecursiveCharIterator(FObj fobj) {
        // Set up first child iterator
        this.fobj = fobj;
        this.childIter = fobj.getChildNodes();
        getNextCharIter();
    }

    /**
     * Constructor which creates an iterator for only some child nodes
     * @param fobj FObj for which an iterator should be created
     * @param child FONode of the first child to include in iterator
     */
    public RecursiveCharIterator(FObj fobj, FONode child) {
        // Set up first child iterator
        this.fobj = fobj;
        this.childIter = fobj.getChildNodes(child);
        getNextCharIter();
    }

    /**
     * @return clone of this, cast as a CharIterator
     */
    public CharIterator mark() {
        return (CharIterator) this.clone();
    }

    /**
     * @return a clone of this
     */
    public Object clone() {
        RecursiveCharIterator ci = (RecursiveCharIterator) super.clone();
        ci.childIter = fobj.getChildNodes(ci.curChild);
        // Need to advance to the next child, else we get the same one!!!
        ci.childIter.next();
        ci.curCharIter = (CharIterator) curCharIter.clone();
        return ci;
    }

    /**
     * Replaces the current character in the CharIterator with a specified
     * character
     * @param c the character which should be used to replace the current
     * character
     */
    public void replaceChar(char c) {
        if (curCharIter != null) {
            curCharIter.replaceChar(c);
        }
    }

    /**
     * advances curChild to the next child in the collection, and curCharIter
     * to the CharIterator for that item, or sets them to null if the iterator
     * has no more items
     */
    private void getNextCharIter() {
        if (childIter != null && childIter.hasNext()) {
            this.curChild = (FONode) childIter.next();
            this.curCharIter = curChild.charIterator();
        } else {
            curChild = null;
            curCharIter = null;
        }
    }

    /**
     * @return true if there are more items in the CharIterator
     */
    public boolean hasNext() {
        while (curCharIter != null) {
            if (curCharIter.hasNext() == false) {
                getNextCharIter();
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * @see org.apache.fop.fo.CharIterator#nextChar()
     */
    public char nextChar() throws NoSuchElementException {
        if (curCharIter != null) {
            return curCharIter.nextChar();
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * @see java.util.Iterator#remove
     */
    public void remove() {
        if (curCharIter != null) {
            curCharIter.remove();
        }
    }
}

