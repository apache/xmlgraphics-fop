/*
 * $Id$
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


public class RecursiveCharIterator extends AbstractCharIterator {
    private Iterator childIter = null; // Child flow objects
    private CharIterator curCharIter = null; // Children's characters
    private FONode fobj;
    private FONode curChild;

    public RecursiveCharIterator(FObj fobj) {
        // Set up first child iterator
        this.fobj = fobj;
        this.childIter = fobj.getChildren();
        getNextCharIter();
    }

    public RecursiveCharIterator(FObj fobj, FONode child) {
        // Set up first child iterator
        this.fobj = fobj;
        this.childIter = fobj.getChildren(child);
        getNextCharIter();
    }

    public CharIterator mark() {
        return (CharIterator) this.clone();
    }

    public Object clone() {
        RecursiveCharIterator ci = (RecursiveCharIterator) super.clone();
        ci.childIter = fobj.getChildren(ci.curChild);
        // Need to advance to the next child, else we get the same one!!!
        ci.childIter.next();
        ci.curCharIter = (CharIterator) curCharIter.clone();
        return ci;
    }


    public void replaceChar(char c) {
        if (curCharIter != null) {
            curCharIter.replaceChar(c);
        }
    }


    private void getNextCharIter() {
        if (childIter != null && childIter.hasNext()) {
            this.curChild = (FONode) childIter.next();
            this.curCharIter = curChild.charIterator();
        } else {
            curChild = null;
            curCharIter = null;
        }
    }

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

    public char nextChar() throws NoSuchElementException {
        if (curCharIter != null) {
            return curCharIter.nextChar();
        } else {
            throw new NoSuchElementException();
        }
    }


    public void remove() {
        if (curCharIter != null) {
            curCharIter.remove();
        }
    }
}

