/*
 * $Id: RecursiveCharIterator.java,v 1.5 2003/03/05 21:48:02 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.fo;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Kind of a super-iterator that iterates through children of an FONode,
 * in turn managing character iterators for each of them. Caveat: Because this
 * class is itself a CharIterator, and manages a collection of CharIterators, it
 * is easy to get confused.
 */
public class RecursiveCharIterator extends AbstractCharIterator {
    /** parent node for whose children this iterator iterates */
    private FONode fobj;
    /** iterator for the child nodes */
    private Iterator childIter = null;

    /** current child object that is being managed by childIter*/
    private FONode curChild;
    /** CharIterator for curChild's characters */
    private CharIterator curCharIter = null;

    /**
     * Constructor which creates an iterator for all children
     * @param fobj FONode for which an iterator should be created
     */
    public RecursiveCharIterator(FObj fobj) {
        // Set up first child iterator
        this.fobj = fobj;
        this.childIter = fobj.getChildren();
        getNextCharIter();
    }

    /**
     * Constructor which creates an iterator for only some children
     * @param fobj FObj for which an iterator should be created
     * @param child FONode of the first child to include in iterator
     */
    public RecursiveCharIterator(FObj fobj, FONode child) {
        // Set up first child iterator
        this.fobj = fobj;
        this.childIter = fobj.getChildren(child);
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
        ci.childIter = fobj.getChildren(ci.curChild);
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

